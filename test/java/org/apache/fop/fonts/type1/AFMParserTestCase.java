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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link AFMParser}.
 */
public class AFMParserTestCase {

    private AFMParser sut = new AFMParser();

    /**
     * We're testing with two identical files except one has:
     * EncodingScheme AdobeStandardEncoding
     * the other has:
     * EncodingScheme ExpectedEncoding
     * Both files have the correct character metrics data, and we're checking that both are handled
     * consistently with both encoding settings.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testMappingAgainstAdobeStandardEncoding() throws IOException {
        InputStream expectedStream = getClass().getResourceAsStream(
                "adobe-charset_unknown-encoding.afm");
        InputStream adobeStandardStream = getClass().getResourceAsStream(
                "adobe-charset_adobe-encoding.afm");
        AFMFile expectedParser = sut.parse(expectedStream, null);
        AFMFile adobeStandard = sut.parse(adobeStandardStream, null);
        List<AFMCharMetrics> adobeMetrics = adobeStandard.getCharMetrics();
        checkCharMtrxList(true, expectedParser.getCharMetrics(), adobeMetrics);

        compareMetrics(adobeMetrics);

        nonAdobeCharsetUnknownEncoding(adobeMetrics);

        nonAdobeCharsetAdobeEncoding(adobeMetrics);
    }

    private void compareMetrics(List<AFMCharMetrics> charMetrics) {
        // in order to ensure that every character is parsed properly, we're going to check them
        // against the AFM file (bboxes were created with a counter)
        AdobeStandardEncoding[] standardEncoding = AdobeStandardEncoding.values();
        for (int i = 0; i < charMetrics.size(); i++) {
            Rectangle expectedBbox = new Rectangle(i + 1, i + 1, 0, 0);
            AFMCharMetrics thisMetric = charMetrics.get(i);
            assertTrue(thisMetric.getBBox().equals(expectedBbox));
            assertEquals(thisMetric.getCharName(), standardEncoding[i].getAdobeName());
        }
    }

    /**
     * A non-adobe encoded file is tested, all the character codes are not AdobeStandardEncoding and
     * the encoding is not AdobeStandardEncoding, we are checking a failure case here. Checking that
     * the AdobeStandardEncoding isn't forced on other encodings.
     *
     * @param expected the AdobeStandardEncoding encoded character metrics list
     * @throws IOException if an IO error occurs
     */
    private void nonAdobeCharsetUnknownEncoding(List<AFMCharMetrics> expected)
            throws IOException {
        InputStream inStream = getClass().getResourceAsStream(
                "notadobe-charset_unknown-encoding.afm");
        AFMFile afmFile = sut.parse(inStream, null);
        List<AFMCharMetrics> unknownEncodingMetrics = afmFile.getCharMetrics();
        checkCharMtrxList(false, expected, unknownEncodingMetrics);
    }

    /**
     * This tests a poorly encoded file, it has AdobeStandardEncoding. We are checking that the
     * metrics are correctly analysed against properly encoded char metrics.
     *
     * @param expected
     * @throws IOException
     */
    private void nonAdobeCharsetAdobeEncoding(List<AFMCharMetrics> expected)
            throws IOException {
        InputStream inStream = getClass().getResourceAsStream(
                "notadobe-charset_adobe-encoding.afm");
        AFMFile afmFile = sut.parse(inStream, null);
        List<AFMCharMetrics> correctedCharMetrics = afmFile.getCharMetrics();
        checkCharMtrxList(true, expected, correctedCharMetrics);
    }

    private boolean charMetricsEqual(AFMCharMetrics o1, AFMCharMetrics o2) {
        return o1.getCharCode() == o2.getCharCode()
                && objectEquals(o1.getCharacter(), o2.getCharacter())
                && o1.getWidthX() == o2.getWidthX()
                && o1.getWidthY() == o2.getWidthY()
                && objectEquals(o1.getBBox(), o2.getBBox());
    }

    private void checkCharMtrxList(boolean expectedResult, List<AFMCharMetrics> expectedList,
            List<AFMCharMetrics> actualList) {
        assertEquals(expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            assertEquals(expectedResult, charMetricsEqual(expectedList.get(i), actualList.get(i)));
        }
    }

    private boolean objectEquals(Object o1, Object o2) {
        return o1 == null ? o2 == null : (o1 == o2 || o1.equals(o2));
    }
}
