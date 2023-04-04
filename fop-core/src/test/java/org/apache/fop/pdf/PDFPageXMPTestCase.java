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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFParser;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.intermediate.IFUtil;

public class PDFPageXMPTestCase {
    private static final String XMP = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n"
        + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
        + "<rdf:Description xmlns:abc=\"http://www.abc.de/abc/\" abc:def=\"val\" rdf:about=\"\"/>\n"
        + "<rdf:Description xmlns:pdfaExtension=\"http://www.aiim.org/pdfa/ns/extension/\" rdf:about=\"\">\n"
        + "<pdfaExtension:schemas>\n"
        + "<rdf:Bag>\n"
        + "<rdf:li rdf:parseType=\"Resource\">\n"
        + "<pdfaSchema:property xmlns:pdfaSchema=\"http://www.aiim.org/pdfa/ns/schema#\">\n"
        + "<rdf:Seq>\n"
        + "<rdf:li rdf:parseType=\"Resource\">\n"
        + "<pdfaProperty:name xmlns:pdfaProperty=\"http://www.aiim.org/pdfa/ns/property#\">split</pdfaProperty:name>\n"
        + "</rdf:li>\n"
        + "</rdf:Seq>\n"
        + "</pdfaSchema:property>\n"
        + "</rdf:li>\n"
        + "</rdf:Bag>\n"
        + "</pdfaExtension:schemas>\n"
        + "</rdf:Description>\n"
        + "</rdf:RDF>\n"
        + "</x:xmpmeta>";

    @Test
    public void textFO() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, MimeConstants.MIME_PDF);
        String pdf = trimLines(out.toString());
        Assert.assertTrue(pdf, pdf.contains(XMP));
    }

    @Test
    public void textIF() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, MimeConstants.MIME_FOP_IF);
        out = iFToPDF(new ByteArrayInputStream(out.toByteArray()));
        String pdf = trimLines(out.toString());
        Assert.assertTrue(pdf, pdf.contains(XMP));
    }

    private String trimLines(String pdf) {
        pdf = pdf.replace("\r", "");
        StringBuilder sb = new StringBuilder();
        for (String line : pdf.split("\n")) {
            sb.append(line.trim()).append("\n");
        }
        return sb.toString();
    }

    private ByteArrayOutputStream iFToPDF(InputStream is) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FOUserAgent userAgent = getFopFactory().newFOUserAgent();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(is);
        IFDocumentHandler documentHandler
                = userAgent.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        documentHandler.setResult(new StreamResult(out));
        IFUtil.setupFonts(documentHandler);
        IFParser parser = new IFParser();
        Result res = new SAXResult(parser.getContentHandler(documentHandler, userAgent));
        transformer.transform(src, res);
        return out;
    }

    private void foToOutput(ByteArrayOutputStream out, String mimeFopIf) throws Exception {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        if (mimeFopIf.equals(MimeConstants.MIME_FOP_IF)) {
            IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
            IFDocumentHandler targetHandler
                    = userAgent.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
            serializer.mimicDocumentHandler(targetHandler);
            userAgent.setDocumentHandlerOverride(serializer);
        }
        Fop fop = fopFactory.newFop(mimeFopIf, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(PDFPageXMPTestCase.class.getResource("PDFPageXMP.fo").openStream());
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    private FopFactory getFopFactory() {
        return FopFactory.newInstance(new File(".").toURI());
    }
}
