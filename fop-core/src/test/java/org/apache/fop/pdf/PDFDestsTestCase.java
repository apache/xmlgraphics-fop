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
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link PDFDests}.
 */
public class PDFDestsTestCase extends PDFObjectTestCase {

    private PDFDests dests = new PDFDests();
    private String expectedString = "<< /Names [(number) 10 (name) /Test#20name] >>";

    @Before
    public void setUp() {
        List<PDFDestination> destinations = new ArrayList<PDFDestination>();
        PDFNumber number = new PDFNumber();
        number.setNumber(10);
        PDFDestination testNumber = new PDFDestination("number", number);
        testNumber.setDocument(doc);
        destinations.add(testNumber);
        PDFDestination testName = new PDFDestination("name", new PDFName("Test name"));
        testName.setDocument(doc);
        destinations.add(testName);

        dests = new PDFDests(destinations);
        dests.setDocument(doc);
        dests.setParent(parent);
        pdfObjectUnderTest = dests;
    }

    /**
     * Populate the object with some arbitrary values and ensure they are wrapped properly.
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testConstructor() throws IOException {
        // Seems the only way to test this is by testing the output
        testOutputStreams(expectedString, dests);
    }
}
