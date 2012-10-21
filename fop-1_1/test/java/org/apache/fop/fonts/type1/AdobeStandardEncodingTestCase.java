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

package org.apache.fop.fonts.type1;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for {@link AdobeStandardEncoding}.
 */
public class AdobeStandardEncodingTestCase {

    private static BufferedReader adobeStandardEncoding;

    /**
     * Sets up the file reader, this file was retrieved from the url below.
     * http://unicode.org/Public/MAPPINGS/VENDORS/ADOBE/stdenc.txt
     *
     * @throws FileNotFoundException if the file was not found
     */
    @BeforeClass
    public static void setupReader() throws FileNotFoundException {
        InputStream inStream = AdobeStandardEncodingTestCase.class.getResourceAsStream(
                                                                  "AdobeStandardEncoding.txt");
        adobeStandardEncoding = new BufferedReader(new InputStreamReader(inStream));
    }

    /**
     * Probably the best way to test the encoding is by converting it back to format specified in
     * the file, that way we can ensure data has been migrated properly.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testCorrectEncoding() throws IOException {
        for (AdobeStandardEncoding encoding : AdobeStandardEncoding.values()) {
            String expectedLine = getLine();
            String hexUnicode = toHexString(encoding.getUnicodeIndex(), 4);
            String hexAdobe = toHexString(encoding.getAdobeCodePoint(), 2);
            String actualLine = hexUnicode + "\t"
                    + hexAdobe + "\t# "
                    + encoding.getUnicodeName() + "\t# "
                    + encoding.getAdobeName();
            assertEquals(expectedLine, actualLine);
        }
    }

    private String getLine() throws IOException {
        String line = "# The first few lines are comments, these should be ignored";
        while (line.startsWith("#")) {
            line = adobeStandardEncoding.readLine();
        }
        return line;
    }

    private String toHexString(int number, int length) {
        return String.format("%0" + length + "X", number);
    }
}
