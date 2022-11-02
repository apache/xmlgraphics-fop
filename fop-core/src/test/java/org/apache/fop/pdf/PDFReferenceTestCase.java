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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link PDFReference}.
 */
public class PDFReferenceTestCase {

    /**
     * Tests outputInline() - ensure that this object is properly formatted when printed to the
     * output stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testOutputInline() throws IOException {
        PDFName name = new PDFName("Test name");
        name.setObjectNumber(2);
        PDFReference pdfRef = new PDFReference(name);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder textBuffer = new StringBuilder();
        // Ensure that text before outputInline() is kept
        textBuffer.append("Text ");

        pdfRef.outputInline(out, textBuffer);
        assertEquals("Text 2 0 R", textBuffer.toString());
    }

    /**
     * Tests toString() - since this is used quite a lot, we have to ensure the format is correct.
     */
    @Test
    public void testToString() {
        PDFName name = new PDFName("arbitrary");
        name.setObjectNumber(10);
        PDFReference ref = new PDFReference(name);
        assertEquals("10 0 R", ref.toString());
    }
}
