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
 * Test case for {@link PDFNumsArray}. Uses default PDF doc (version 1.4) - requires text values and number values to be
 * included as refs.
 */
public class PDFNumsArrayTestCase extends PDFObjectTestCase {
    private static final String TEST_NAME = "Test name";
    private static final int TEST_NUMBER = 10;
    private PDFNumsArray numsArray;
    private String expectedString = "[0 1 0 R 1 2 0 R]";
    private String expectedStringName = "/Test#20name";
    private String expectedStringNumber = Integer.valueOf(TEST_NUMBER).toString();

    @Before
    public void setUp() {
        numsArray = new PDFNumsArray(parent);
        numsArray.setDocument(doc);
        numsArray.put(0, new PDFName(TEST_NAME));
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
        testOutputStreams(expectedString, numsArray);
        testOutputStreams(expectedStringName, doc.objects.get(1));
        testOutputStreams(expectedStringNumber, doc.objects.get(2));
    }
}
