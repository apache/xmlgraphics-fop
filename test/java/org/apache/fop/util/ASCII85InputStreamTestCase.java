/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtil;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.pdf.PDFText;

import junit.framework.TestCase;

/**
 * Test case for ASCII85InputStream.
 * <p>
 * ATTENTION: Some of the tests here depend on the correct behaviour of
 * ASCII85OutputStream. If something fails here make sure 
 * ASCII85OutputStreamTestCase runs!
 * 
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class ASCII85InputStreamTestCase extends TestCase {

    private static final boolean DEBUG = false;

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public ASCII85InputStreamTestCase(String name) {
        super(name);
    }

    private byte[] decode(String text) throws Exception {
        byte[] ascii85 = text.getBytes("US-ASCII");
        InputStream in = new ByteArrayInputStream(ascii85);
        InputStream decoder = new ASCII85InputStream(in);
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        IOUtil.copy(decoder, baout);
        baout.close();
        return baout.toByteArray();
    }

    private byte[] getChunk(int count) {
        byte[] buf = new byte[count];
        System.arraycopy(ASCII85OutputStreamTestCase.DATA, 0, buf, 0, buf.length);
        return buf;
    }
    
    private String encode(byte[] data, int len) throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        java.io.OutputStream out = new ASCII85OutputStream(baout);
        out.write(data, 0, len);
        out.close();
        return new String(baout.toByteArray(), "US-ASCII");
    }
    
    
    private void innerTestDecode(byte[] data) throws Exception {
        String encoded = encode(data, data.length);
        if (DEBUG) {
            if (data[0] == 0) {
                System.out.println("self-encode: " + data.length + " chunk 000102030405...");
            } else {
                System.out.println("self-encode: " + new String(data, "US-ASCII") 
                    + " " + PDFText.toHex(data));
            }
            System.out.println("  ---> " + encoded);
        }
        byte[] decoded = decode(encoded);
        if (DEBUG) {
            if (data[0] == 0) {
                System.out.println("decoded: " + data.length + " chunk 000102030405...");
            } else {
                System.out.println("decoded: " + new String(decoded, "US-ASCII") 
                    + " " + PDFText.toHex(decoded));
            }
        }
        assertEquals(PDFText.toHex(data), PDFText.toHex(decoded));
    }
    
    /**
     * Tests the output of ASCII85.
     * @throws Exception if an error occurs
     */
    public void testDecode() throws Exception {
        byte[] buf;
        innerTestDecode("1. Bodypart".getBytes("US-ASCII"));
        if (DEBUG) {
            System.out.println("===========================================");
        } 

        innerTestDecode(getChunk(1));
        innerTestDecode(getChunk(2));
        innerTestDecode(getChunk(3));
        innerTestDecode(getChunk(4));
        innerTestDecode(getChunk(5));
        if (DEBUG) {
            System.out.println("===========================================");
        } 
        
        innerTestDecode(getChunk(10));
        innerTestDecode(getChunk(62));
        innerTestDecode(getChunk(63));
        innerTestDecode(getChunk(64));
        innerTestDecode(getChunk(65));

        if (DEBUG) {
            System.out.println("===========================================");
        } 
        String sz;
        sz = PDFText.toHex(decode("zz~>"));
        assertEquals(PDFText.toHex(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}), sz);
        sz = PDFText.toHex(decode("z\t \0z\n~>"));
        assertEquals(PDFText.toHex(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}), sz);
        if (DEBUG) {
            System.out.println("===========================================");
        } 
        try {
            decode("vz~>");
            fail("Illegal character should be detected");
        } catch (IOException ioe) {
            //expected
        }
        /* DISABLED because of try/catch in InputStream.read(byte[], int, int).
         * Only the exception happening on the first byte in a block is being
         * reported. BUG in JDK???
         *
        try {
            decode("zv~>");
            fail("Illegal character should be detected");
        } catch (IOException ioe) {
            //expected
        }*/
    }
    
    private byte[] getFullASCIIRange() {
        java.io.ByteArrayOutputStream baout = new java.io.ByteArrayOutputStream(256);
        for (int i = 254; i < 256; i++) {
            baout.write(i);
        }
        return baout.toByteArray();
    }

    /**
     * Tests the full 8-bit ASCII range.
     * @throws Exception if an error occurs
     */
    public void testFullASCIIRange() throws Exception {
        innerTestDecode(getFullASCIIRange());
    }
    
}
