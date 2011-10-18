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

/* $Id:$ */

package org.apache.fop.afp.modca;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.fop.afp.util.BinaryUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link IncludeObject}
 */
public class IncludeObjectTestCase extends AbstractNamedAFPObjectTestCase<IncludeObject> {

    @Before
    public void setUp() throws Exception {
        setSut(new IncludeObject("8__chars"));
    }

    /**
     * Test writeToStream()
     * @throws IOException -
     */
    @Test
    public void testWriteToStream() throws IOException {
        final IncludeObject sut = getSut();

        byte[] expected = defaultIncludeObjectBytes(sut.getTripletDataLength(), sut.getNameBytes());

        testWriteToStreamHelper(sut, expected);
    }

    /**
     * Test writeToStream() - the orientation of the referenced object is a right-
     * handed with a 180 x-axis
     * @throws IOException -
     */
    @Test
    public void testWriteToStreamForOrientation() throws IOException {
        final IncludeObject sut = getSut();

        byte[] expected = defaultIncludeObjectBytes(sut.getTripletDataLength(), sut.getNameBytes());

        expected[25] = (byte)0x5A;
        expected[26] = (byte)0x00;
        expected[27] = (byte)0x87;
        expected[28] = (byte)0x00;

        sut.setObjectAreaOrientation(180);

        testWriteToStreamHelper(sut, expected);
    }

    private void testWriteToStreamHelper(IncludeObject sut, byte[] expected) throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        sut.writeToStream(baos);

        byte[] actual = baos.toByteArray();

        assertTrue(Arrays.equals(actual, expected));
    }

    private byte[] defaultIncludeObjectBytes(int tripletDataLength, byte[] nameData) {

        byte[] expected = new byte[36];

        byte[] header = new byte[] {
                0x5A, // Structured field identifier
                0x00, // Length byte 1
                0x10, // Length byte 2
                (byte)0xD3, // Structured field id byte 1
                (byte)0xAF, // Structured field id byte 2 - type 'input'
                (byte)0xC3, // Structured field id byte 3 - category 'data resource'
                0x00, // Flags
                0x00, // Reserved
                0x00, // Reserved
        };

        System.arraycopy(header, 0, expected, 0, header.length);

        byte[] lengthBytes = BinaryUtils.convert(35 + tripletDataLength, 2); //Ignore first byte
        expected[1] = lengthBytes[0];
        expected[2] = lengthBytes[1];

        System.arraycopy(nameData, 0, expected, 9, nameData.length);

        expected[18] = (byte)0x92; // object type 'other'

        expected[27] = (byte)0x2D; // orientation of the reference object
        writeOsetTo(expected, 29, -1); // the X-axis origin defined in the object
        writeOsetTo(expected, 32, -1); // the Y-axis origin defined in the object

        expected[35] = 0x01; // Page or overlay coordinate system

        return expected;
    }

    private static void writeOsetTo(byte[] out, int offset, int oset) {
        if (oset > -1) {
            byte[] y = BinaryUtils.convert(oset, 3);
            out[offset] = y[0];
            out[offset + 1] = y[1];
            out[offset + 2] = y[2];
        } else {
            out[offset] = (byte)0xFF;
            out[offset + 1] = (byte)0xFF;
            out[offset + 2] = (byte)0xFF;
        }
    }
}