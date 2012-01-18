/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fop.afp.goca;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharacterSetBuilder;
import org.apache.fop.fonts.Typeface;
import org.junit.Before;
import org.junit.Test;

public class GraphicsCharacterStringTestCase {
    private GraphicsCharacterString gcsCp500;
    private GraphicsCharacterString gcsCp1146;
    // consider the EBCDIC code page variants Cp500 and Cp1146
    // the £ corresponds to byte 5B (position 91) in the CCSID 285 and CCSID 1146
    // the $ corresponds to byte 5B (position 91) in the CCSID 500
    private final String poundsText = "££££";
    private final String dollarsText = "$$$$";
    private final byte[] bytesToCheck = {(byte) 0x5b, (byte) 0x5b, (byte) 0x5b, (byte) 0x5b};

    @Before
    public void setUp() throws Exception {
        CharacterSetBuilder csb = CharacterSetBuilder.getSingleByteInstance();
        CharacterSet cs1146 = csb.build("C0H200B0", "T1V10500", "Cp1146",
                Class.forName("org.apache.fop.fonts.base14.Helvetica").asSubclass(Typeface.class)
                .newInstance(), null);
        gcsCp1146 = new GraphicsCharacterString(poundsText, 0, 0, cs1146);
        CharacterSet cs500 = csb.build("C0H200B0", "T1V10500", "Cp500",
                Class.forName("org.apache.fop.fonts.base14.Helvetica").asSubclass(Typeface.class)
                .newInstance(), null);
        gcsCp500 = new GraphicsCharacterString(dollarsText, 0, 0, cs500);
    }

    @Test
    public void testWriteToStream() throws IOException {
        // check pounds
        ByteArrayOutputStream baos1146 = new ByteArrayOutputStream();
        gcsCp1146.writeToStream(baos1146);
        byte[] bytes1146 = baos1146.toByteArray();
        for (int i = 0; i < bytesToCheck.length; i++) {
            assertEquals(bytesToCheck[i], bytes1146[6 + i]);
        }
        assertEquals(bytesToCheck.length + 6, bytes1146.length);
        // check dollars
        ByteArrayOutputStream baos500 = new ByteArrayOutputStream();
        gcsCp500.writeToStream(baos500);
        byte[] bytes500 = baos500.toByteArray();
        for (int i = 0; i < bytesToCheck.length; i++) {
            assertEquals(bytesToCheck[i], bytes500[6 + i]);
        }
        assertEquals(bytesToCheck.length + 6, bytes500.length);
    }
}
