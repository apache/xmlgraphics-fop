/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.CopyUtils;
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
        CopyUtils.copy(decoder, baout);
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
