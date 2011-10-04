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

package org.apache.fop.afp.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

/**
 * MODCAParser and MODCAParser.UnparsedStructuredField Unit tests
 */
public class MODCAParserTestCase {

    /** The carriage control character (0x5A) used to indicate the start of a structured field. */
    public static final byte CARRIAGE_CONTROL_CHAR = (byte)0x5A;
    /**ASCII carriage return control character*/
    public static final byte CARRIAGE_RETURN = (byte)0x0A;
    /**ASCII line feed control character */
    public static final byte LINE_FEED = (byte)0x0D;
    /** 8 byte introducer describe the SF */
    private static final int INTRODUCER_LENGTH = 8;

    /**
     * Test that the MODCA parser recognises carriage control (0x5A) as the Structured Field
     * delimeter
     *
     * @throws Exception *
     */
    @Test
    public void testReadNextStructuredField1() throws Exception {

        // carriage control (0x5A) delimits structured fields,
        // and control is handed to readStructuredField(DataInputStream)
        byte[][] goodInputStream = new byte[][]{
                new byte[]{CARRIAGE_CONTROL_CHAR}
        };

        for (byte[] b : goodInputStream) {
            try {
                new MODCAParser(new ByteArrayInputStream(b))
                        .readNextStructuredField();
                fail("BAD SF should throw EOF: " + byteArrayToString(b));
            } catch (EOFException eof) {
                //passed
            }
        }

        // EOFException thrown when reading the input stream are caught and
        // a null value is returned
        byte[][] badInputStream = new byte[][]{
                new byte[]{},
                new byte[]{CARRIAGE_RETURN},
                new byte[]{LINE_FEED}
        };

        for (byte[] b : badInputStream) {
            UnparsedStructuredField usf = new MODCAParser(new ByteArrayInputStream(b))
            .readNextStructuredField();
            assertNull(usf);
        }
    }


    /**
     * Test that the MODCA parser correctly constructs an UnparsedStructuredField
     * from a well formed structured field
     *
     * @throws Exception *
     */
    @Test
    public void testReadNextStructuredField2() throws Exception {

        // no extension data
        testSF((byte)0xd3, (byte)0xa8, (byte)0x89, //SFTypeID
                (byte)0, //flags excluding the bits for
                //extension present, segmented data and padding present
                false, false,
                new byte[]{0, 0},
                new byte[]{1}, null);

        // with extension data
        testSF((byte)0xd3, (byte)0xa8, (byte)0x89, //SFTypeID
                (byte)0, //flags excluding the bits for
                //extension present, segmented data and padding present
                false, false,
                new byte[]{0, 0},
                new byte[]{1}, new byte[]{10});

        // with ignored reserved bits
        testSF((byte)0xd3, (byte)0xa8, (byte)0x89, //SFTypeID
                (byte)0, //flags excluding the bits for
                //extension present, segmented data and padding present
                false, false,
                new byte[]{1, 2},
                new byte[]{1}, null);

        // with padding present and segmented data
        testSF((byte)0xd3, (byte)0xa8, (byte)0x89, //SFTypeID
                (byte)(1 << 3), //flags excluding the bits for
                //extension present, segmented data and padding present
                true, true,
                new byte[]{0, 0},
                new byte[]{1}, null);

     // with flags non zero
        testSF((byte)0xd3, (byte)0xa8, (byte)0x89, //SFTypeID
                (byte)(1 << 3), //flags excluding the bits for
                //extension present, segmented data and padding present
                false, false,
                new byte[]{0, 0},
                new byte[]{1}, null);
    }


    private void testSF(byte classCode, byte typeCode, byte categoryCode,
            byte flags, boolean segmentedData, boolean paddingPresent, byte[] reserved,
            byte[] data, byte[] extData) throws Exception {

        byte extDataLength = 0;
        boolean extensionPresent = (extData != null);

        if (extensionPresent) {
            flags = (byte)(flags | 0x01);
            extDataLength = (byte)(extData.length + 1); //length includes length byte
        }

        if (segmentedData) {
            flags = (byte)(flags | 0x04);
        }

        if (paddingPresent) {
            flags = (byte)(flags | 0x10);
        }

        short length = (short)(INTRODUCER_LENGTH + data.length + extDataLength);
        byte[] lengthBytes = new byte[]{(byte)(length >> 8), (byte)(length & 0xFF)};

        byte[] sfBytes = new byte[length];

        //introducer bytes
        sfBytes[0] = lengthBytes[0];
        sfBytes[1] = lengthBytes[1];
        sfBytes[2] = classCode;
        sfBytes[3] = typeCode;
        sfBytes[4] = categoryCode;
        sfBytes[5] = flags;
        sfBytes[6] = reserved[0];
        sfBytes[7] = reserved[1];

        if (extDataLength > 0) {
            sfBytes[8] = (byte)(extData.length + 1);
            System.arraycopy(extData, 0, sfBytes, 9, extData.length);
        }

        System.arraycopy(data, 0, sfBytes, length - data.length, data.length);


        byte[] delimiteredSF = new byte[length + 1];

        delimiteredSF[0] = (byte)0x5A;

        System.arraycopy(sfBytes, 0, delimiteredSF, 1, length);

        InputStream bis = new ByteArrayInputStream(delimiteredSF);

        UnparsedStructuredField actual =  new MODCAParser(bis)
        .readNextStructuredField();

        //check introducer
        assertEquals(length, actual.getSfLength());
        assertEquals(classCode, actual.getSfClassCode());
        assertEquals(typeCode, actual.getSfTypeCode());
        assertEquals(categoryCode, actual.getSfCategoryCode());
        assertEquals(extensionPresent, actual.isSfiExtensionPresent());
        assertEquals(segmentedData, actual.isSfiSegmentedData());
        assertEquals(paddingPresent, actual.isSfiPaddingPresent());

        byte[] introducerData = new byte[]{(byte)(length >> 8), (byte)(length & 0xFF),
                classCode,  typeCode, categoryCode, flags, reserved[0], reserved[1]};

        assertTrue(Arrays.equals(introducerData, actual.getIntroducerData()));

        //check data
        assertTrue(Arrays.equals(data, actual.getData()));

        //check extension data
        if (extData != null) {
            assertTrue(Arrays.equals(extData, actual.getExtData()));
        }
        assertEquals(
                (extData == null) ? 0 : extData.length + 1, // 1 byte for length byte
                        actual.getExtLength());

        assertTrue(Arrays.equals(data, actual.getData()));

        int expectedSfTypeID = ((classCode & 0xFF) << 16)
                | ((typeCode & 0xFF) << 8)
                | (categoryCode & 0xFF);

        assertEquals(expectedSfTypeID, actual.getSfTypeID());

        assertTrue(Arrays.equals(sfBytes, actual.getCompleteFieldAsBytes()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        actual.writeTo(baos);
        assertTrue(Arrays.equals(sfBytes, baos.toByteArray()));

    }


    private static String byteArrayToString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(Integer.toHexString(b & 0xFF)).append(" ");
        }
        return sb.toString();
    }

}
