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
import java.util.StringTokenizer;

import org.apache.fop.apps.FOUserAgent;

/** Test that characters are correctly encoded in a generated PDF file */
public class PDFEncodingTestCase extends BasePDFTestCase {
    private File foBaseDir = new File("test/xml/pdf-encoding");
    private final boolean dumpPDF = Boolean.getBoolean("PDFEncodingTestCase.dumpPDF");
    static final String INPUT_FILE = "test/xml/pdf-encoding/pdf-encoding-test.xconf";
    static final String TEST_MARKER = "PDFE_TEST_MARK_";

    /**
     * @param name the name of the test case
     */
    public PDFEncodingTestCase(String name) {
        super(name);
    }

    /**
     * create an FOUserAgent for our tests
     * @return an initialized FOUserAgent
     */
    protected FOUserAgent getUserAgent() {
        final FOUserAgent a = fopFactory.newFOUserAgent();
        return a;
    }

    /** @return our specific config */
    protected File getUserConfigFile() {
        return new File(INPUT_FILE);
    }

    /**
     * Test using a standard FOP font
     * @throws Exception checkstyle wants a comment here, even a silly one
     */
    public void testPDFEncodingWithStandardFont() throws Exception {

        return;
        /*  If the PDF encoding is correct, a text dump of the generated PDF file contains this (excerpts) 
         *     ...Tm [(PDFE_TEST_MARK_2:) ( ) (This) ( ) (is) ...(acute:) ( ) (XX_\351_XX) ] TJ
         *     ...Tm [(PDFE_TEST_MARK_3:) ( ) (This) ( ) (is) ...(letter:) ( ) (XX_\342\352\356\364\373_XX) ] TJ
         *  The following array is used to look for these patterns
         */ 
//        final String[] testPatterns = { 
//                TEST_MARKER + "1", "(Standard)",
//                TEST_MARKER + "2", "XX_\\351_XX", 
//                TEST_MARKER + "3", "XX_\\342\\352\\356\\364\\373_XX" 
//              };
//
//        runTest("test-standard-font.fo", testPatterns);
    }

    /**
     * TODO test disabled for now, fails due (probably) do different PDF
     * encoding when custom font is used
     * 
     * @throws Exception
     *             checkstyle wants a comment here, even a silly one
     */
    public void DISABLEDtestPDFEncodingWithCustomFont() throws Exception {

        /*  If the PDF encoding is correct, a text dump of the generated PDF file contains this (excerpts) 
         *     ...Tm [(PDFE_TEST_MARK_2:) ( ) (This) ( ) (is) ...(acute:) ( ) (XX_\351_XX) ] TJ
         *     ...Tm [(PDFE_TEST_MARK_3:) ( ) (This) ( ) (is) ...(letter:) ( ) (XX_\342\352\356\364\373_XX) ] TJ
         *  The following array is used to look for these patterns
         */ 
        final String[] testPatterns = { 
          TEST_MARKER + "1", "(Gladiator)",
          TEST_MARKER + "2", "XX_\\351_XX", 
          TEST_MARKER + "3", "XX_\\342\\352\\356\\364\\373_XX" 
        };

        runTest("test-custom-font.fo", testPatterns);
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

        int markersFound = 0;
        final String input = new String(pdf);
        int pos = 0;
        if ((pos = input.indexOf(TEST_MARKER)) >= 0) {
            final StringTokenizer tk = new StringTokenizer(
                    input.substring(pos), "\n");

            while (tk.hasMoreTokens()) {
                final String line = tk.nextToken();
                if (line.indexOf(TEST_MARKER) >= 0) {
                    markersFound++;
                    for (int i = 0; i < testPattern.length; i += 2) {
                        if (line.indexOf(testPattern[i]) >= 0) {
                            final String ref = testPattern[i + 1];
                            final boolean patternFound = line.indexOf(ref) >= 0;
                            assertTrue("line containing '" + testPattern[i]
                                    + "' must contain '" + ref, patternFound);
                        }
                    }
                }
            }
        }

        final int nMarkers = testPattern.length / 2;
        assertEquals(nMarkers + " " + TEST_MARKER + " markers must be found",
                nMarkers, markersFound);
    }
}
