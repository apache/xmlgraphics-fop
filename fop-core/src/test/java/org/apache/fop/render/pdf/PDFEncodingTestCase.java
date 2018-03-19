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

package org.apache.fop.render.pdf;



import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.fop.apps.FOUserAgent;


/** Test that characters are correctly encoded in a generated PDF file */
public class PDFEncodingTestCase extends BasePDFTest {
    private File foBaseDir = new File("test/xml/pdf-encoding");
    private final boolean dumpPDF = Boolean.getBoolean("PDFEncodingTestCase.dumpPDF");
    static final String INPUT_FILE = "test/xml/pdf-encoding/pdf-encoding-test.xconf";
    static final String TEST_MARKER = "PDFE_TEST_MARK_";

    public PDFEncodingTestCase() throws SAXException, IOException {
        super(INPUT_FILE);
    }


    /**
     * create an FOUserAgent for our tests
     * @return an initialized FOUserAgent
     */
    protected FOUserAgent getUserAgent() {
        final FOUserAgent a = fopFactory.newFOUserAgent();
        return a;
    }

    /**
     * Test using a standard FOP font
     * @throws Exception checkstyle wants a comment here, even a silly one
     */
    @Test
    public void testPDFEncodingWithStandardFont() throws Exception {

        /*  If the PDF encoding is correct, a text dump of the generated PDF file contains this (excerpts)
         *     ...Tm [(PDFE_TEST_MARK_2:) ( ) (This) ( ) (is) ...(acute:) ( ) (XX_\351_XX) ] TJ
         *     ...Tm [(PDFE_TEST_MARK_3:) ( ) (This) ( ) (is) ...(letter:) ( ) (XX_\342\352\356\364\373_XX) ] TJ
         *  The following array is used to look for these patterns
         */
        final String[] testPatterns = {
                TEST_MARKER + "1", "Standard",
                TEST_MARKER + "2", "XX_\u00E9_XX",
                TEST_MARKER + "3", "XX_\u00E2\u00EA\u00EE\u00F4\u00FB_XX"
              };

        runTest("test-standard-font.fo", testPatterns);
    }

    /**
     * Test encoding with a Custom Font using BMP characters.
     *
     * NB: The Gladiator font do not contain '_' Glyph
     *
     * @throws Exception
     *             checkstyle wants a comment here, even a silly one
     */
    @Test
    public void testPDFEncodingWithCustomFont() throws Exception {

        /*  If the PDF encoding is correct, a text dump of the generated PDF file contains this (excerpts)
         *     ...Tm [(PDFE_TEST_MARK_2:) ( ) (This) ( ) (is) ...(acute:) ( ) (XX_\351_XX) ] TJ
         *     ...Tm [(PDFE_TEST_MARK_3:) ( ) (This) ( ) (is) ...(letter:) ( ) (XX_\342\352\356\364\373_XX) ] TJ
         *  The following array is used to look for these patterns
         */
        final String[] testPatterns = {
                TEST_MARKER + "1", "Gladiator",
                TEST_MARKER + "2", "XX_\u00E9_XX",
                TEST_MARKER + "3", "XX_\u00E2\u00EA\u00EE\u00F4\u00FB_XX"
        };

        runTest("test-custom-font.fo", testPatterns);
    }

    /**
     * Test encoding with a Custom Font using non-BMP characters
     *
     * @throws Exception
     *              checkstyle wants a comment here, even a silly one
     */
    @Test
    public void testPDFEncodingWithNonBMPFont() throws Exception {

        final String[] testPatterns = {
            TEST_MARKER + "1", "AndroidEmoji",
            TEST_MARKER + "2", "\uD800\uDF00",
        };

        runTest("test-custom-non-bmp-font.fo", testPatterns);
    }

    /** Test encoding using specified input file and test patterns array */
    private void runTest(String inputFile, String[] testPatterns)
            throws Exception {
        File foFile = new File(foBaseDir, inputFile);
        byte[] pdfData = convertFO(foFile, getUserAgent(), dumpPDF);
        checkEncoding(pdfData, testPatterns);
    }

    /**
     * Check character encodings in the generated PDF data, by reading text
     * lines identified by markers and checking their content
     *
     * @throws IOException
     */
    private void checkEncoding(byte[] pdf, String[] testPattern)
            throws IOException {

        String s = extractTextFromPDF(pdf);

        int markersFound = 0;
        for (String line : s.split("\n")) {
            if (!line.contains(TEST_MARKER)) {
                 continue;
            }

            markersFound++;

            for (int i = 0; i < testPattern.length; i++) {
                String marker = testPattern[i];
                String pattern = testPattern[++i];

                if (!line.contains(marker)) {
                     continue;
                }

                String msg = String.format("line containing '%s' must contain '%s'", marker, pattern);
                assertTrue(msg, line.contains(pattern));
            }
        }

        final int nMarkers = testPattern.length / 2;
        assertEquals(nMarkers + " " + TEST_MARKER + " markers must be found",
                nMarkers, markersFound);
    }

    private static String extractTextFromPDF(byte[] pdfContent) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        PDDocument pdDoc =  PDDocument.load(pdfContent);
        return pdfStripper.getText(pdDoc);
    }
}
