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

/**
 * Test case for {@link PDFNull}.
 */
public class PDFNullTestCase extends PDFObjectTestCase {

    /**
     * Test outputInline() - test that "null" is printed to the output stream.
     */
    @Test
    public void testOutputInline() throws IOException {
        PDFNull obj = PDFNull.INSTANCE;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder text = new StringBuilder();
        obj.outputInline(out, text);
        assertEquals("null", text.toString());

        // Ensure previously written text is not discarded
        obj.outputInline(out, text);
        assertEquals("nullnull", text.toString());
    }
}
