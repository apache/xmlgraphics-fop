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
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.render.pdf.PDFContentGenerator;

public class PDFUATestCase {
    @Test
    public void testXMP() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        Metadata metadata = PDFMetadata.createXMPFromPDFDocument(doc);
        StringBuilder sb = new StringBuilder();
        Iterator i = metadata.iterator();
        while (i.hasNext()) {
            QName k = (QName) i.next();
            sb.append(k + ": " + metadata.getProperty(k).getValue() + "\n");
        }
        String s = sb.toString();
        Assert.assertTrue(s, s.contains("pdfuaid:part: 1"));
    }

    @Test
    public void testPDF() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.getRoot().makeTagged();
        doc.getRoot().setStructTreeRoot(new PDFStructTreeRoot(null));
        doc.getInfo().setTitle("title");
        doc.getProfile().setPDFUAMode(PDFUAMode.PDFUA_1);
        PDFResources resources = new PDFResources(doc);
        doc.addObject(resources);
        PDFResourceContext context = new PDFResourceContext(resources);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFContentGenerator gen = new PDFContentGenerator(doc, out, context);
        Rectangle2D.Float f = new Rectangle2D.Float();
        PDFPage page = new PDFPage(resources, 0, f, f, f, f);
        doc.addImage(context, new BitmapImage("", 1, 1, new byte[0], null));
        doc.registerObject(page);
        gen.flushPDFDoc();
        doc.outputTrailer(out);

        Collection<StringBuilder> objs = PDFLinearizationTestCase.readObjs(
                new ByteArrayInputStream(out.toByteArray())).values();
        Assert.assertTrue(getObj(objs, "/Type /Catalog").contains("/ViewerPreferences << /DisplayDocTitle true >>"));
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
