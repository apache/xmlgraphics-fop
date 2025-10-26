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

package org.apache.fop.accessibility.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.ConfigurationException;

public class TaggingTestCase {

    @Test
    public void testMultipleStaticContentArtifact() throws ConfigurationException, FOPException,
            TransformerException, IOException {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setAccessibility(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:svg=\"http://www.w3.org/2000/svg\">"
                + "  <fo:layout-master-set>\n"
                + "    <fo:simple-page-master page-width=\"8.5in\" page-height=\"11in\" master-name=\"Page\">\n"
                + "      <fo:region-body region-name=\"Body\"/>\n"
                + "      <fo:region-before region-name=\"xsl-region-before\" extent=\"0.5in\"/>\n"
                + "    </fo:simple-page-master>\n"
                + "  </fo:layout-master-set>\n"
                + "  <fo:page-sequence master-reference=\"Page\">\n"
                + "    <fo:static-content flow-name=\"xsl-region-before\" role=\"artifact\">\n"
                + "      <fo:block>\n"
                + "        <fo:retrieve-marker retrieve-class-name=\"headline-current\"/>\n"
                + "      </fo:block>\n"
                + "    </fo:static-content>\n"
                + "    <fo:flow flow-name=\"Body\">\n"
                + "      <fo:block>\n"
                + "        <fo:marker marker-class-name=\"headline-current\">1</fo:marker>\n"
                + "        <fo:block>A</fo:block>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "  <fo:page-sequence master-reference=\"Page\">\n"
                + "    <fo:static-content flow-name=\"xsl-region-before\" role=\"artifact\">\n"
                + "      <fo:block>\n"
                + "        <fo:retrieve-marker retrieve-class-name=\"headline-current\"/>\n"
                + "      </fo:block>\n"
                + "    </fo:static-content>\n"
                + "    <fo:flow flow-name=\"Body\">\n"
                + "      <fo:block>\n"
                + "        <fo:marker marker-class-name=\"headline-current\">2</fo:marker>\n"
                + "        <fo:block>B</fo:block>\n"
                + "      </fo:block>\n"
                + "    </fo:flow>\n"
                + "  </fo:page-sequence>\n"
                + "</fo:root>";
        Source src = new StreamSource(new ByteArrayInputStream(fo.getBytes()));
        Result res = new SAXResult(fop.getDefaultHandler());

        try {
            transformer.transform(src, res);
        } finally {
            out.close();
        }

        try (PDDocument pdfDocument = Loader.loadPDF(out.toByteArray())) {
            PDFMarkedContentExtractor extractor = new PDFMarkedContentExtractor();
            assertEquals(2, pdfDocument.getPages().getCount());
            extractor.processPage(pdfDocument.getPages().get(0));
            extractor.processPage(pdfDocument.getPages().get(1));

            List<PDMarkedContent> markedContents = extractor.getMarkedContents();
            assertEquals(4, markedContents.size());

            assertEquals("Artifact", markedContents.get(0).getTag());
            assertEquals("1", markedContents.get(0).getContents().get(0).toString());

            assertEquals("P", markedContents.get(1).getTag());
            assertEquals("A", markedContents.get(1).getContents().get(0).toString());

            assertEquals("Artifact", markedContents.get(2).getTag());
            assertEquals("2", markedContents.get(2).getContents().get(0).toString());

            assertEquals("P", markedContents.get(3).getTag());
            assertEquals("B", markedContents.get(3).getContents().get(0).toString());
        }
    }
}
