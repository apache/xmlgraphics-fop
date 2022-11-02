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

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.area.inline.WordArea;

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
        Assert.assertTrue(wordAreas.get(0).isNextIsSpace());
        Assert.assertFalse(wordAreas.get(1).isNextIsSpace());
    }
}
