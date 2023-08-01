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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.CIDSet;
import org.apache.fop.fonts.CIDSubset;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.NamedCharacter;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
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
    public void testMakeFont() throws IOException {
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        SingleByteFont sb = new TestSingleByteFont(null);
        sb.setFontName("test");
        sb.setWidth(0, 0);
        sb.setFlags(0);
        sb.setEmbedResourceName("");
        sb.mapChar('a');
        sb.addUnencodedCharacter(new NamedCharacter("xyz", String.valueOf((char) 0x2202)), 0, new Rectangle());
        sb.mapChar((char) 0x2202);
        sb.setEncoding(new CodePointMapping("FOPPDFEncoding", new int[0]));
        PDFFont font = pdfFactory.makeFont("a", "a", "WinAnsiEncoding", sb, sb);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        font.output(bos);
        assertTrue(bos.toString().contains("/BaseFont /EAAAAA+a"));
        assertEquals(sb.getAdditionalEncodingCount(), 1);
    }

    @Test
    public void testDifferencesStartValue() throws IOException {
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        TestSingleByteFont sb = new TestSingleByteFont(null);
        sb.setFlags(0);
        sb.setMapping(new SingleByteEncoding() {
            public String getName() {
                return "FOPPDFEncoding";
            }

            public char mapChar(char c) {
                return 0;
            }

            public String[] getCharNameMap() {
                return new String[]{"a"};
            }

            public char[] getUnicodeCharMap() {
                return new char[]{1};
            }
        });
        pdfFactory.makeFont("a", "a", "WinAnsiEncoding", sb, sb);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.outputTrailer(bos);
        assertTrue(bos.toString().contains("/Differences [1 /a]"));
    }

    @Test
    public void testMakeTrueTypeFont() throws IOException {
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        SingleByteFont sb = new TestSingleByteFont(null);
        sb.setFontType(FontType.TRUETYPE);
        sb.setFontName("test");
        sb.setWidth(0, 0);
        sb.setFlags(0);
        sb.setEncoding(new CodePointMapping("FOPPDFEncoding", new int[0]));
        String enc = "MacRomanEncoding";
        PDFFont font = pdfFactory.makeFont("a", "a", enc, sb, sb);
        font.output(new ByteArrayOutputStream());
        assertEquals(((PDFName)font.entries.get("Encoding")).getName(), enc);
    }

    class TestSingleByteFont extends SingleByteFont {
        public TestSingleByteFont(InternalResourceResolver resourceResolver) {
            super(resourceResolver, EmbeddingMode.SUBSET);
        }

        public InputStream getInputStream() throws IOException {
            File f = new File("test/resources/fonts/type1/a010013l.pfb");
            return new FileInputStream(f);
        }

        public void setMapping(SingleByteEncoding mapping) {
            this.mapping = mapping;
        }
    }

    @Test
    public void testMakeOTFFont() throws IOException {
        InternalResourceResolver rr =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        URI uri = new File("test/resources/fonts/otf/SourceSansProBold.otf").toURI();
        CustomFont sb = OFFontLoader.loadFont(new FontUris(uri, null),
                null, true, EmbeddingMode.SUBSET, null, false, false, rr, false, false, true);
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

    @Test
    public void testMakeOTFFontPDFA() throws IOException {
        InternalResourceResolver rr =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        PDFDocument doc = new PDFDocument("");
        doc.getProfile().setPDFAMode(PDFAMode.PDFA_2A);
        PDFFactory pdfFactory = new PDFFactory(doc);
        URI uri = new File("test/resources/fonts/otf/SourceSansProBold.otf").toURI();
        CustomFont sb = OFFontLoader.loadFont(new FontUris(uri, null),
                null, true, EmbeddingMode.SUBSET, null, false, false, rr, false, false, true);
        for (char c = 0; c < 512; c++) {
            sb.mapChar(c);
        }
        pdfFactory.makeFont("a", "a", "WinAnsiEncoding", sb, sb);
        PDFFont pdfFont = pdfFactory.getDocument().getFontMap().get("a_1");
        PDFFontDescriptor fontDescriptor = (PDFFontDescriptor) pdfFont.get("FontDescriptor");
        assertNull(fontDescriptor.getCIDSet());
    }

    @Test
    public void testGetExternalAction() {

        String germanAe = "\u00E4";
        String filename = "test";
        String unicodeFilename = "t" + germanAe + "st.pdf";
        PDFFileSpec fileSpec = new PDFFileSpec(filename, unicodeFilename);

        PDFDocument doc = new PDFDocument("");
        doc.registerObject(fileSpec);
        PDFNames names = doc.getRoot().getNames();
        if (names == null) {
            //Add Names if not already present
            names = doc.getFactory().makeNames();
            doc.getRoot().setNames(names);
        }

        PDFEmbeddedFiles embeddedFiles = names.getEmbeddedFiles();
        if (embeddedFiles == null) {
            embeddedFiles = new PDFEmbeddedFiles();
            doc.assignObjectNumber(embeddedFiles);
            doc.addTrailerObject(embeddedFiles);
            names.setEmbeddedFiles(embeddedFiles);
        }

        PDFArray nameArray = embeddedFiles.getNames();
        if (nameArray == null) {
            nameArray = new PDFArray();
            embeddedFiles.setNames(nameArray);
        }
        nameArray.add(fileSpec.getFilename());
        nameArray.add(new PDFReference(fileSpec));

        PDFFactory pdfFactory = new PDFFactory(doc);
        String target = "embedded-file:" + unicodeFilename;
        PDFJavaScriptLaunchAction pdfAction = (PDFJavaScriptLaunchAction)
                pdfFactory.getExternalAction(target, false);

        String expectedString = "<<\n/S /JavaScript\n/JS (this.exportDataObject\\({cName:\""
                + fileSpec.getFilename() + "\", nLaunch:2}\\);)\n>>";

        assertEquals(expectedString, pdfAction.toPDFString());
    }

    @Test
    public void testMakeLink() {
        PDFDocument doc = new PDFDocument("");
        PDFFactory pdfFactory = new PDFFactory(doc);
        Rectangle2D rect = new Rectangle(10, 20);
        PDFLink link = pdfFactory.makeLink(rect, "dest", true);

        String expectedString = "<< /Type /Annot\n" + "/Subtype /Link\n" + "/Rect [ "
                + "0.0 0.0 10.0 20.0 ]\n/C [ 0 0 0 ]\n"
                + "/Border [ 0 0 0 ]\n" + "/A 1 0 R"
                + "\n/H /I\n\n>>";

        assertEquals(expectedString, link.toPDFString());
    }
}
