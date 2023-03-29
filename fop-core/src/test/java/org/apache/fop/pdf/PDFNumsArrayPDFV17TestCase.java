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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link PDFNumsArray}. PDF1.7 requires text values to be included as refs and
 * number values to be included as direct objects.
 */
public class PDFNumsArrayPDFV17TestCase extends PDFObjectTestCase {
    private static final int TEST_NUMBER = 44;
    private static final String TEST_TEXT = "Test text";
    private PDFNumsArray numsArray;
    private String expectedStringNumsArray = "[0 1 0 R 1 " + TEST_NUMBER + "]";
    private String expectedStringText = "((" + TEST_TEXT + "))";

    @Before
    public void setUp() {
        doc.setPDFVersion(Version.V1_7);
        numsArray = new PDFNumsArray(parent);
        numsArray.setDocument(doc);
        PDFText pdfText = new PDFText();
        pdfText.setText(TEST_TEXT);
        numsArray.put(0, pdfText);
        PDFNumber num = new PDFNumber();
        num.setNumber(TEST_NUMBER);
        numsArray.put(1, num);

        pdfObjectUnderTest = numsArray;
    }

    /**
     * Test output() - ensure that this object is properly outputted to the PDF document.
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testOutput() throws IOException {
        testOutputStreams(expectedStringNumsArray, numsArray);
        testOutputStreams(expectedStringText, doc.objects.get(1));
    }
}
