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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.URIAction;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.extensions.PDFEmbeddedFileAttachment;



public class PDFAttachmentTestCase {
    private FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();

    @Test
    public void testAddEmbeddedFile() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());
        docHandler.handleExtensionObject(new PDFEmbeddedFileAttachment("filename", "src", "desc"));
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:filename", false), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (filename)\n  /UF (filename)\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"filename\", nLaunch:2}\\);)\n>>"));
    }

    @Test
    public void testAddEmbeddedFileGermanUmlaut() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());

        String germanAe = "\u00E4";
        String unicodeFilename = "t" + germanAe + "st";
        PDFEmbeddedFileAttachment fileAtt = new PDFEmbeddedFileAttachment(unicodeFilename,
                "src", "desc");
        docHandler.handleExtensionObject(fileAtt);
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:" + unicodeFilename, false), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (" + fileAtt.getFilename() + ")\n  /UF "
                        + PDFText.escapeText(fileAtt.getUnicodeFilename()) + "\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"" + fileAtt.getFilename() + "\", nLaunch:2}\\);)\n>>"));
    }

    @Test
    public void testAddEmbeddedFileParenthesis() throws IFException {
        PDFDocumentHandler docHandler = new PDFDocumentHandler(new IFContext(ua));
        docHandler.setFontInfo(new FontInfo());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        docHandler.setResult(new StreamResult(out));
        docHandler.startDocument();
        docHandler.startPage(0, "", "", new Dimension());

        String unicodeFilename = "t(st";
        PDFEmbeddedFileAttachment fileAtt = new PDFEmbeddedFileAttachment(unicodeFilename,
                "src", "desc");
        docHandler.handleExtensionObject(fileAtt);
        docHandler.getDocumentNavigationHandler().renderLink(new Link(
                new URIAction("embedded-file:" + unicodeFilename, false), new Rectangle()));
        docHandler.endDocument();
        Assert.assertTrue(out.toString().contains(
                "<<\n  /Type /Filespec\n  /F (t\\(st)\n  /UF (t\\(st)\n  /AFRelationship /Data"));
        Assert.assertTrue(out.toString().contains("<<\n/S /JavaScript\n"
                + "/JS (this.exportDataObject\\({cName:\"t\\(st\", nLaunch:2}\\);)\n>>"));
    }
}
