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

package org.apache.fop.pdf;

import org.junit.Test;

import org.apache.fop.fonts.CodePointMapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PDFEncodingTestCase {

    /**
     * Tests the createPDFEncoding method to ensure a null encoding type
     * is handled correctly.
     */
    @Test
    public void testCreatePDFEncodingForNull() {
        Object encoding = PDFEncoding.createPDFEncoding(null, "Test");
        assertEquals(encoding, null);
    }

    /**
     * Tests that when a PDFEncoding object is created, if the encoding type is
     * that of StandardEncoding, the baseEncoding tag is omitted.
     */
    @Test
    public void testStandardEncodingDiffs() {
        Object encoding = PDFEncoding.createPDFEncoding(CodePointMapping.getMapping(
                CodePointMapping.SYMBOL_ENCODING), "Test");
        if (encoding instanceof PDFEncoding) {
            PDFEncoding pdfEncoding = (PDFEncoding) encoding;
            assertFalse(pdfEncoding.entries.containsKey("BaseEncoding"));
        }
    }

    /**
     * Tests that when the StandardEncoding type is provided and there are no
     * differences, the returned encoding object is null.
     */
    @Test
    public void testStandardEncodingNoDiff() {
        Object encoding = PDFEncoding.createPDFEncoding(CodePointMapping.getMapping(
                CodePointMapping.STANDARD_ENCODING), "Test");
        assertEquals(encoding, null);
    }

    /**
     * Tests that when the SymbolEncoding type is provided and there are no
     * differences, the returned encoding string is that of SymbolEncoding.
     */
    @Test
    public void testCreatePDFEncodingSymbol() {
        Object encoding = PDFEncoding.createPDFEncoding(CodePointMapping.getMapping(
                CodePointMapping.SYMBOL_ENCODING), "Symbol");
        assert (encoding instanceof String);
        String pdfEncoding = (String) encoding;
        assertEquals(pdfEncoding, "SymbolEncoding");
    }
}
