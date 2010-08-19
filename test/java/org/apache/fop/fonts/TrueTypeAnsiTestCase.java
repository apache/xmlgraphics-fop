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

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.render.pdf.PDFRenderer;

/**
 * Tests XML font metrics file generation and usage for WinAnsi mode.
 */
public class TrueTypeAnsiTestCase extends TestCase {

    /**
     * Tests a TrueType font in WinAnsi mode.
     * @throws Exception if an error occurs
     */
    public void testTrueTypeAnsi() throws Exception {
        String fontFamily = "Gladiator Bold";
        File ttfFile = new File("./test/resources/fonts/glb12.ttf");
        File workDir = new File("./build/test-results");
        if (!workDir.isDirectory()) {
            assertTrue(workDir.mkdirs());
        }
        File metricsFile = new File(workDir, ttfFile.getName() + ".xml");
        if (metricsFile.isFile()) {
            assertTrue(metricsFile.delete());
        }

        String[] args = new String[] {"-enc", "ansi",
                ttfFile.getCanonicalPath(), metricsFile.getCanonicalPath()};
        TTFReader.main(args);
        assertTrue(metricsFile.isFile());

        FopFactory fopFactory = FopFactory.newInstance();
        FOUserAgent ua = fopFactory.newFOUserAgent();
        PDFRenderer renderer = new PDFRenderer();
        renderer.setUserAgent(ua);
        List fontList = new java.util.ArrayList();
        List triplets = new java.util.ArrayList();
        triplets.add(new FontTriplet(fontFamily, "normal", Font.WEIGHT_NORMAL));
        EmbedFontInfo font = new EmbedFontInfo(
                metricsFile.toURI().toASCIIString(),
                true, true, triplets,
                ttfFile.toURI().toASCIIString(), null);
        fontList.add(font);
        renderer.addFontList(fontList);

        ua.setRendererOverride(renderer);
        OutputStream out = new NullOutputStream();

        Fop fop = fopFactory.newFop(null, ua, out);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Source src = new StreamSource(new StringReader(
                "<root font-family='" + fontFamily + "'>Test!</root>"));
        Result res = new SAXResult(fop.getDefaultHandler());
        Transformer transformer = tFactory.newTransformer(
                getSourceForResource(this, "fonttest.xsl"));
        transformer.transform(src, res);
    }

    private static Source getSourceForResource(Object reference, String name) {
        URL url = reference.getClass().getResource(name);
        if (url == null) {
            throw new NullPointerException("Resource not found: " + name);
        }
        return new StreamSource(url.toExternalForm());
    }

}
