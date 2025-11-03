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
package org.apache.fop.render.intermediate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.util.CharUtilities;

public class IFRendererTestCase {
    private List<WordArea> wordAreas = new ArrayList<WordArea>();

    private void foToOutput(InputStream fo) throws FOPException, TransformerException {
        FopFactoryBuilder fopFactoryBuilder = new FopFactoryBuilder(new File(".").toURI());
        fopFactoryBuilder.setAccessibility(true);
        FopFactory fopFactory = fopFactoryBuilder.build();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        IFRenderer ifRenderer = new IFRenderer(userAgent) {
            protected void renderWord(WordArea word) {
                wordAreas.add(word);
                super.renderWord(word);
            }
        };
        userAgent.setRendererOverride(ifRenderer);
        Fop fop = fopFactory.newFop("application/pdf", userAgent, new ByteArrayOutputStream());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(fo);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    @Test
    public void testWordSpace() throws FOPException, TransformerException {
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master master-name=\"simple\" page-height=\"27.9cm\" page-width=\"21.6cm\">\n"
                + "      <fo:region-body />\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"simple\">\n"
                + "    <fo:flow flow-name=\"xsl-region-body\">\n"
                + "      <fo:block>test test</fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        foToOutput(new ByteArrayInputStream(fo.getBytes()));
        assertTrue(wordAreas.get(0).isNextIsSpace());
        assertFalse(wordAreas.get(1).isNextIsSpace());
    }

    @Test
    public void testSoftHyphen() throws FOPException, TransformerException {
        String fo = getSoftHyphenTestFO("&#x00AD;");
        foToOutput(new ByteArrayInputStream(fo.getBytes()));
        assertTrue("PDF files are able to handle the soft hyphen, so must not replace with normal hyphen",
                wordAreas.get(1).getWord().endsWith(String.valueOf(CharUtilities.SOFT_HYPHEN)));


        wordAreas = new ArrayList<>();
        fo = getSoftHyphenTestFO("-");
        foToOutput(new ByteArrayInputStream(fo.getBytes()));
        assertTrue("Must not replace the hyphenation character", wordAreas.get(1).getWord().endsWith("-"));

        wordAreas = new ArrayList<>();
        fo = getSoftHyphenTestFO("/");
        foToOutput(new ByteArrayInputStream(fo.getBytes()));
        assertTrue("Must not replace the hyphenation character", wordAreas.get(1).getWord().endsWith("/"));
    }

    private String getSoftHyphenTestFO(String hyphenationCharacter) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" "
                + "xmlns:fox=\"http://xmlgraphics.apache.org/fop/extensions\">\n"
                + "\t<fo:layout-master-set>\n"
                + "\t\t<fo:simple-page-master master-name=\"mainPage\" page-width=\"90pt\">\n"
                + "\t\t\t<fo:region-body/>\n"
                + "\t\t</fo:simple-page-master>\n"
                + "\t</fo:layout-master-set>\n"
                + "\t<fo:page-sequence master-reference=\"mainPage\">\n"
                + "\t\t<fo:flow flow-name=\"xsl-region-body\">\n"
                + "\t\t\t<fo:block language=\"en\" country=\"GB\" hyphenate=\"true\" page-break-inside=\"auto\" "
                + "hyphenation-character=\"" + hyphenationCharacter + "\">\n"
                + "\t\t\t\tcomputer computer computer computer computer computer computer computer\n"
                + "\t\t\t</fo:block>\n"
                + "\t\t</fo:flow>\n"
                + "\t</fo:page-sequence>\n"
                + "</fo:root>";
    }
}
