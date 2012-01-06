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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Test case for {@link PDFRectangle}.
 */
public class PDFRectangleTestCase {

    /**
     * Test outputInline() - ensure properly formatted co-ords are printed to the output stream.
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testOutputInline() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        // These are arbitrary values thus have no meaning
        PDFRectangle rect = new PDFRectangle(1, 2, 3, 4);

        StringBuilder textBuffer = new StringBuilder();
        // Ensure text before the outputInline() is maintained
        textBuffer.append("Test ");

        rect.outputInline(out, textBuffer);
        assertEquals("Test [1 2 3 4]", textBuffer.toString());
    }
}
