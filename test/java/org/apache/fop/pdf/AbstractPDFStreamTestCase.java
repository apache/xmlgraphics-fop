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

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link AbstractPDFStream}.
 */
public class AbstractPDFStreamTestCase extends PDFObjectTestCase {

    private AbstractPDFStream abstractStream;

    private String textData = "This is an arbitrary string for testing.";

    private static byte[] encodedBytes;
    static {
        int[] encoded = { 0x78, 0x9c, 0x0b, 0xc9, 0xc8, 0x2c, 0x56, 0x00, 0xa2, 0xc4, 0x3c, 0x85,
                0xc4, 0xa2, 0xa4, 0xcc, 0x92, 0xa2, 0xc4, 0xa2, 0x4a, 0x85, 0xe2, 0x92, 0xa2, 0xcc,
                0xbc, 0x74, 0x85, 0xb4, 0xfc, 0x22, 0x85, 0x92, 0xd4, 0xe2, 0x12, 0x20, 0x5b, 0x0f,
                0x00, 0x2d, 0x2b, 0x0e, 0xde, 0x0a };
        encodedBytes = new byte[encoded.length];
        int i = 0;
        for (int in : encoded) {
            encodedBytes[i++] = (byte) (in & 0xff);
        }
    }
    private String startStream = "<< /Length 5 0 R /Filter /FlateDecode >>\n"
                + "stream\n";

    private String endStream = "endstream";

    @Before
    public void setUp() {
        abstractStream = new AbstractPDFStream() {

            @Override
            protected void outputRawStreamData(OutputStream out) throws IOException {
                out.write(textData.getBytes());
            }

            @Override
            protected int getSizeHint() throws IOException {
                return textData.length();
            }
        };
        abstractStream.setDocument(doc);
        abstractStream.setParent(parent);

        pdfObjectUnderTest = abstractStream;
    }

    /**
     * Tests output() - ensure that this object is correctly formatted to the output stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testOutput() throws IOException {
        // This differs from most other objects, if the object number = 0 an exception is thrown
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        abstractStream.setObjectNumber(1);
        ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
        expectedStream.write(startStream.getBytes());
        expectedStream.write(encodedBytes);
        expectedStream.write(endStream.getBytes());
        assertEquals(expectedStream.size(), abstractStream.output(outStream));
        assertEquals(expectedStream.toString(), outStream.toString());
    }
}
