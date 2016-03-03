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

/* $Id$ */

package org.apache.fop.afp.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link CharactersetEncoder}
 */
public class CharactersetEncoderTestCase {
    private CharactersetEncoder singlebyteEncoder;
    private CharactersetEncoder doublebyteEncoder;

    @Before
    public void setUp() {
        singlebyteEncoder = CharacterSetType.SINGLE_BYTE.getEncoder("cp500");
        doublebyteEncoder = CharacterSetType.DOUBLE_BYTE_LINE_DATA.getEncoder("cp937");
    }

    // This is just an arbitrary CJK string
    private final String testCJKText = "\u8ACB\u65BC\u627F\u505A\u65E5\u4E03\u65E5\u5167\u672A\u9054"
            + "\u4E03\u65E5\u4E4B\u5B9A\u5B58\u8005\u4EE5\u5BE6\u969B\u5230\u671F\u65E5\u5167\u78BA"
            + "\u8A8D\u672C\u4EA4\u6613\u5167\u5BB9\u3002\u5982\u672A\u65BC\u4E0A\u8FF0\u671F\u9593"
            + "\u5167\u63D0\u51FA\u7570\u8B70\uFF0C\u8996\u540C\u610F\u627F\u8A8D\u672C\u4EA4\u6613"
            + "\u3002";

    private final byte[] test6CJKChars = {
            (byte) 0x61, (byte) 0x99,
            (byte) 0x50, (byte) 0xf4,
            (byte) 0x50, (byte) 0xd4,
            (byte) 0x56, (byte) 0x99,
            (byte) 0x4c, (byte) 0xc9,
            (byte) 0x4c, (byte) 0x44 };

    private final String testEngText = "Hello World!";
    private final byte[] testEngChars = {
            (byte) 0xc8, // H
            (byte) 0x85, // e
            (byte) 0x93, // l
            (byte) 0x93, // l
            (byte) 0x96, // o
            (byte) 0x40, // " "
            (byte) 0xe6, // W
            (byte) 0x96, // o
            (byte) 0x99, // r
            (byte) 0x93, // l
            (byte) 0x84, // d
            (byte) 0x4f  // !
    };

    /**
     * Tests canEncode() - tests that canEncode() responds properly to various input characters.
     */
    @Test
    public void testCanEncode() {
        // Both SBCS and DBCS should support Latin characters
        for (char c = '!'; c < '~'; c++) {
            assertTrue(singlebyteEncoder.canEncode(c));
            assertTrue(doublebyteEncoder.canEncode(c));
        }
        // ONLY the double byte characters can handle CJK text
        for (char c : testCJKText.toCharArray()) {
            assertFalse(singlebyteEncoder.canEncode(c));
            assertTrue(doublebyteEncoder.canEncode(c));
        }
        // Ensure that double byte encoder doesn't just return true all the time...
        assertFalse(doublebyteEncoder.canEncode('\u00BB'));
    }

    @Test
    public void testEncode() throws CharacterCodingException, IOException {
        CharactersetEncoder.EncodedChars encChars; // = doublebyteEncoder.encode(testCJKText);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        // JAVA 1.5 has a bug in the JVM in which these err for some reason... JAVA 1.6 no issues
        /*encChars.writeTo(bOut, 0, encChars.getLength());
        byte[] bytes = bOut.toByteArray();
        for (int i = 0; i < 12; i++) {
            assertEquals(test6CJKChars[i], bytes[i]);
        }
        bOut.reset();*/

        encChars = singlebyteEncoder.encode(testEngText);
        encChars.writeTo(bOut, 0, encChars.getLength());
        byte[] engBytes = bOut.toByteArray();
        for (int i = 0; i < testEngChars.length; i++) {
            assertEquals(testEngChars[i], engBytes[i]);
        }
        assertEquals(testEngChars.length, engBytes.length);
    }
}
