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
