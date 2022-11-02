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
 * Test case for {@link PDFDocument}
 */
public class PDFDocumentTestCase {

    /**
     * Test flushTextBuffer() - ensure that the text given will stream to the PDF document as
     * expected.
     * @throws IOException when an I/O error occurs
     */
    @Test
    public void testFlushTextBuffer() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder textBuffer = new StringBuilder();
        String testString = "This is a test string, just some arbitrary data.";
        textBuffer.append(testString);

        PDFDocument.flushTextBuffer(textBuffer, out);
        assertEquals(testString, out.toString());

        // Should reset the textBuffer
        assertEquals(0, textBuffer.length());
        assertEquals("", textBuffer.toString());
        out.reset();

        String[] strArray = { "Try ", "with ", "multiple ", "strings." };
        for (String str : strArray) {
            textBuffer.append(str);
        }
        String fullString = textBuffer.toString();
        PDFDocument.flushTextBuffer(textBuffer, out);
        assertEquals(fullString, out.toString());
    }
}
