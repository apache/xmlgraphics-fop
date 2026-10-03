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
package org.apache.fop.fonts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.traits.MinOptMax;

public class GlyphMappingTestCase {

    @Test
    public void testSpecialCharacterSpacing() throws Exception {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\" font-family=\"Aegean\">"
                + "<fo:layout-master-set>\n"
                + "<fo:simple-page-master master-name=\"page\" page-height=\"11in\" page-width=\"8.5in\">\n"
                + "<fo:region-body/>\n"
                + "</fo:simple-page-master>\n"
                + "</fo:layout-master-set>\n"
                + "<fo:page-sequence master-reference=\"page\">\n"
                + "<fo:flow flow-name=\"xsl-region-body\">\n"
                + "<fo:block font-family=\"Aegean\" text-align=\"justify\">\n"
                + "<fo:inline letter-spacing.optimum=\"0pt\" letter-spacing.maximum=\"2pt\">\n"
                + "£££\n"
                + "</fo:inline>\n"
                + "</fo:block>"
                + "</fo:flow>\n"
                + "</fo:page-sequence>\n"
                + "</fo:root>";



        String output = foToIF(fo);
        assertTrue(output + "No exception should be thrown when using a custom ttf font with special characters",
                output.contains("£££"));
    }

    /**
     * A word's letter spaces are part of its width on both paths. On the path for fonts with
     * substitution or positioning tables they were counted but left out of the width, so the
     * line breaker measured letter-spaced text short and it ran past the end of the line, while
     * the painter spaced every glyph.
     */
    @Test
    public void testLetterSpacesInTheWidthOfAWordInAFontWithLayoutTables() throws Exception {
        InternalResourceResolver resolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        CustomFont typeface = FontLoader.loadFont(new FontUris(file.toURI(), null), "", true,
                EmbeddingMode.AUTO, EncodingMode.AUTO, false, true, resolver, false, false, true);
        Font font = new Font("F1", null, typeface, 12000);
        assertTrue(font.performsSubstitution() || font.performsPositioning());

        TextFragment word = new StringFragment("word");
        GlyphMapping unspaced = GlyphMapping.doGlyphMapping(word, 0, 4, font, MinOptMax.ZERO, null,
                '\0', ' ', false, 0, false, false, false);
        GlyphMapping spaced = GlyphMapping.doGlyphMapping(word, 0, 4, font, MinOptMax.getInstance(3000), null,
                '\0', ' ', false, 0, false, false, false);
        assertEquals(3, spaced.letterSpaceCount);
        assertEquals(3 * 3000, spaced.areaIPD.getOpt() - unspaced.areaIPD.getOpt());
    }

    private static final class StringFragment implements TextFragment {

        private final String text;

        private StringFragment(String text) {
            this.text = text;
        }

        public CharacterIterator getIterator() {
            return new StringCharacterIterator(text);
        }

        public int getBeginIndex() {
            return 0;
        }

        public int getEndIndex() {
            return text.length();
        }

        public String getScript() {
            return "latn";
        }

        public String getLanguage() {
            return "dflt";
        }

        public int getBidiLevel() {
            return 0;
        }

        public char charAt(int index) {
            return text.charAt(index);
        }

        public CharSequence subSequence(int startIndex, int endIndex) {
            return text.subSequence(startIndex, endIndex);
        }
    }

    private String foToIF(String fo) throws SAXException, TransformerException, IOException {
        String fopxconf = "<fop version=\"1.0\">\n"
                + "<font-base>../fop/test/resources/fonts/ttf</font-base>\n"
                + "<renderers>\n"
                + "<renderer mime=\"application/pdf\">\n"
                + "<fonts>\n"
                + "<font kerning=\"yes\" name=\"Aegean\" embed-url=\"Aegean600.ttf\">\n"
                + "<font-triplet name=\"Aegean\" style=\"normal\" weight=\"normal\"/>\n"
                + "</font>\n"
                + "</fonts>\n"
                + "</renderer>\n"
                + "</renderers>"
                + "</fop>";
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes()));

        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
        IFDocumentHandler targetHandler
                = userAgent.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        serializer.mimicDocumentHandler(targetHandler);
        userAgent.setDocumentHandlerOverride(serializer);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_FOP_IF, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes(StandardCharsets.UTF_8)));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
        return out.toString(StandardCharsets.UTF_8.name());
    }
}
