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

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFParser;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.pdf.PDFContentGenerator;

public class PDFVTTestCase {
    @Test
    public void testXMP() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.getProfile().setPDFXMode(PDFXMode.PDFX_4);
        doc.getProfile().setPDFVTMode(PDFVTMode.PDFVT_1);
        Metadata metadata = PDFMetadata.createXMPFromPDFDocument(doc);
        StringBuilder sb = new StringBuilder();
        Iterator i = metadata.iterator();
        while (i.hasNext()) {
            QName k = (QName) i.next();
            sb.append(k + ": " + metadata.getProperty(k).getValue() + "\n");
        }
        String s = sb.toString();
        Assert.assertTrue(s.contains("pdfxid:GTS_PDFXVersion: PDF/X-4"));
        Assert.assertTrue(s.contains("xmpMM:VersionID: 1"));
        Assert.assertTrue(s.contains("pdf:Trapped: False"));
        Assert.assertTrue(s.contains("xmpMM:RenditionClass: default"));
        Assert.assertTrue(s.contains("pdf:PDFVersion: 1.4"));
        Assert.assertTrue(s.contains("pdfvtid:GTS_PDFVTVersion: PDF/VT-1"));
    }

    @Test
    public void testPDF() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.getInfo().setTitle("title");
        doc.getProfile().setPDFXMode(PDFXMode.PDFX_4);
        doc.getProfile().setPDFVTMode(PDFVTMode.PDFVT_1);
        PDFResources resources = new PDFResources(doc);
        doc.addObject(resources);
        PDFResourceContext context = new PDFResourceContext(resources);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFContentGenerator gen = new PDFContentGenerator(doc, out, context);
        Rectangle2D.Float f = new Rectangle2D.Float();
        PDFPage page = new PDFPage(resources, 0, f, f, f, f);
        doc.addImage(context, new BitmapImage("", 1, 1, new byte[0], null));
        doc.registerObject(page);
        doc.getFactory().makeDPart(page, "master");
        gen.flushPDFDoc();
        doc.outputTrailer(out);

        Collection<StringBuilder> objs = PDFLinearizationTestCase.readObjs(
                new ByteArrayInputStream(out.toByteArray())).values();
        Assert.assertTrue(getObj(objs, "/Type /Catalog").contains("/DPartRoot "));
        Assert.assertTrue(getObj(objs, "/Type /DPartRoot").contains("/NodeNameList [/root /record]"));
        Assert.assertTrue(
                getObj(objs, "/Subtype /Image").contains("/GTS_XID (uuid:d41d8cd9-8f00-3204-a980-0998ecf8427e)"));
    }

    @Test
    public void textFO() throws IOException, SAXException, TransformerException, IFException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, MimeConstants.MIME_PDF);
        checkPDF(out);
    }

    @Test
    public void textIF() throws IOException, SAXException, TransformerException, IFException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        foToOutput(out, MimeConstants.MIME_FOP_IF);
        iFToPDF(new ByteArrayInputStream(out.toByteArray()));
    }


    private void foToOutput(ByteArrayOutputStream out, String mimeFopIf)
        throws IOException, SAXException, TransformerException {
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
        Source src = new StreamSource(new FileInputStream("test/java/org/apache/fop/pdf/PDFVT.fo"));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    private FopFactory getFopFactory() throws IOException, SAXException {
        return FopFactory.newInstance(new File(".").toURI(),
                new FileInputStream("test/java/org/apache/fop/pdf/PDFVT.xconf"));
    }

    private void iFToPDF(InputStream is) throws IOException, SAXException, TransformerException, IFException {
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

        checkPDF(out);
    }

    private void checkPDF(ByteArrayOutputStream out) throws IOException {
        Map<String, StringBuilder> objs =
                PDFLinearizationTestCase.readObjs(new ByteArrayInputStream(out.toByteArray()));
        String dpart = getObj(objs.values(), "/DParts");
        int v = getValue("/DParts", dpart);
        String dpm = objs.get(v + " 0 obj").toString();
        Assert.assertTrue(dpm.contains(
                "/DPM << /CIP4_Root << /CIP4_Production << /CIP4_Part << /CIP4_ProductType (frontpages) >>"));
    }

    private int getValue(String name, String firstObj) throws IOException {
        String[] split = firstObj.split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(name)) {
                return Integer.valueOf(split[i + 1].replace("[[", ""));
            }
        }
        throw new IOException(name + " not found " + firstObj);
    }

    private String getObj(Collection<StringBuilder> objs, String x) {
        for (StringBuilder s : objs) {
            if (s.toString().contains(x)) {
                return s.toString();
            }
        }
        return null;
    }
}
