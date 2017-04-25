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
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.CIDSet;
import org.apache.fop.fonts.CIDSubset;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.OFFontLoader;

/**
 * Test case for {@link PDFFactory}.
 */
public class PDFFactoryTestCase {

    /**
     * This tests that when a font is subset embedded in a PDF, the font name is prefixed with a
     * pseudo-random tag as per the PDF spec.
     */
    @Test
    public void testSubsetFontNamePrefix() {
        class MockedFont extends MultiByteFont {
            public MockedFont(InternalResourceResolver resolver) {
                super(resolver, EmbeddingMode.AUTO);
            }

            @Override
            public int[] getWidths() {
                return new int[] {0};
            }

            @Override
            public CIDSet getCIDSet() {
                return new CIDSubset(this);
            }
        }
        PDFDocument doc = new PDFDocument("Test");
        PDFFactory pdfFactory = new PDFFactory(doc);
        URI thisURI = new File(".").toURI();
        ResourceResolver resolver = ResourceResolverFactory.createDefaultResourceResolver();
        InternalResourceResolver resourceResolver = ResourceResolverFactory.createInternalResourceResolver(
                thisURI, resolver);
        MockedFont font = new MockedFont(resourceResolver);

        PDFFont pdfDejaVu = pdfFactory.makeFont("DejaVu", "DejaVu", "TTF", font, font);
        assertEquals("/EAAAAA+DejaVu", pdfDejaVu.getBaseFont().toString());

        PDFFont pdfArial = pdfFactory.makeFont("Arial", "Arial", "TTF", font, font);
        assertEquals("/EAAAAB+Arial", pdfArial.getBaseFont().toString());
    }

    @Test
    public void testMakeOTFFont() throws IOException {
        InternalResourceResolver rr =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        URI uri = new File("test/resources/fonts/otf/SourceSansProBold.otf").toURI();
        CustomFont sb = OFFontLoader.loadFont(new FontUris(uri, null),
                null, true, EmbeddingMode.SUBSET, null, false, false, rr, false, false);
        for (char c = 0; c < 512; c++) {
            sb.mapChar(c);
        }
        pdfFactory.makeFont("a", "a", "WinAnsiEncoding", sb, sb);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.outputTrailer(bos);

        assertEquals(pdfFactory.getDocument().getFontMap().size(), 2);
        PDFFont pdfFont = pdfFactory.getDocument().getFontMap().get("a_1");
        PDFEncoding enc = (PDFEncoding) pdfFont.get("Encoding");
        PDFArray diff = (PDFArray) enc.get("Differences");
        assertEquals(diff.length(), 80);
        assertEquals(diff.get(1).toString(), "/nacute");

        pdfFont = pdfFactory.getDocument().getFontMap().get("a");
        enc = (PDFEncoding) pdfFont.get("Encoding");
        diff = (PDFArray) enc.get("Differences");
        assertEquals(diff.length(), 257);
        assertEquals(diff.get(2).toString(), "/space");

        assertTrue(bos.toString().contains("/Subtype /Type1\n"));
        assertTrue(bos.toString().contains("/Subtype /Type1C"));
    }
}
