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

import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

import junit.framework.TestCase;

/**
 * Test case for ASCII85OutputStream
 * 
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class ASCII85OutputStreamTestCase extends TestCase {

    /** Test data */
    public static final byte[] DATA = new byte[100];
    
    static {
        //Fill in some data
        for (int i = 0; i < 100; i++) {
            DATA[i] = (byte)i;
        }
    }

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public ASCII85OutputStreamTestCase(String name) {
        super(name);
    }

    private String encode(int count) throws Exception {
        return encode(DATA, count);
    }
    
    private String encode(byte[] data, int len) throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        OutputStream out = new ASCII85OutputStream(baout);
        out.write(data, 0, len);
        out.close();
        return new String(baout.toByteArray(), "US-ASCII");
    }

    /**
     * Tests the output of ASCII85.
     * @throws Exception if an error occurs
     */
    public void testOutput() throws Exception {
        String sz = encode(new byte[] {0, 0, 0, 0, 0, 0, 0, 0}, 8);
        assertEquals("zz~>", sz);
        
        String s3 = encode(3);
        //System.out.println(">>>" + s3 + "<<<");
        assertEquals("!!*-~>", s3);
        
        String s10 = encode(10);
        //System.out.println(">>>" + s10 + "<<<");
        assertEquals("!!*-'\"9eu7#RL~>", s10);
        
        String s62 = encode(62);
        //System.out.println(">>>" + s62 + "<<<");
        assertEquals("!!*-'\"9eu7#RLhG$k3[W&.oNg'GVB\"(`=52*$$(B+<_pR,"
            + "UFcb-n-Vr/1iJ-0JP==1c70M3&s#]4?W~>", s62);
        
        String s63 = encode(63);
        //System.out.println(">>>" + s63 + "<<<");
        assertEquals("!!*-'\"9eu7#RLhG$k3[W&.oNg'GVB\"(`=52*$$(B+<_pR,"
            + "UFcb-n-Vr/1iJ-0JP==1c70M3&s#]4?Yk\n~>", s63);

        String s64 = encode(64);
        //System.out.println(">>>" + s64 + "<<<");
        assertEquals("!!*-'\"9eu7#RLhG$k3[W&.oNg'GVB\"(`=52*$$(B+<_pR,"
            + "UFcb-n-Vr/1iJ-0JP==1c70M3&s#]4?Ykm\n~>", s64);
        
        String s65 = encode(65);
        //System.out.println(">>>" + s65 + "<<<");
        assertEquals("!!*-'\"9eu7#RLhG$k3[W&.oNg'GVB\"(`=52*$$(B+<_pR,"
            + "UFcb-n-Vr/1iJ-0JP==1c70M3&s#]4?Ykm\n5Q~>", s65);
        
    }

}
