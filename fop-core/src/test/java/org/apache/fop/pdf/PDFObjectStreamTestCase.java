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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.render.pdf.PDFContentGenerator;

public class PDFObjectStreamTestCase {
    @Test
    public void testObjectStreamsEnabled() throws IOException {
        PDFDocument doc = new PDFDocument("");
        String out = buildObjectStreamsPDF(doc);
        Assert.assertTrue(out.contains("<<\n  /Type /ObjStm\n  /N 3\n  /First 15\n  /Length 260\n>>\n"
                + "stream\n8 0\n9 52\n4 121\n<<\n/Producer"));
    }

    @Test
    public void testObjectStreamsWithEncryption() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.setEncryption(new PDFEncryptionParams());
        String out = buildObjectStreamsPDF(doc);
        Assert.assertTrue(out.contains("<<\n  /Type /ObjStm\n  /N 3\n  /First 16\n  /Length"));
    }

    private String buildObjectStreamsPDF(PDFDocument doc) throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Map<String, List<String>> filterMap = new HashMap<>();
        List<String> filterList = new ArrayList<>();
        filterList.add("null");
        filterMap.put("default", filterList);
        doc.setFilterMap(filterMap);
        doc.setObjectStreamsEnabled(true);
        PDFResources resources = new PDFResources(doc);
        doc.addObject(resources);
        PDFResourceContext context = new PDFResourceContext(resources);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFContentGenerator gen = new PDFContentGenerator(doc, out, context);
        Rectangle2D.Float f = new Rectangle2D.Float();
        PDFPage page = new PDFPage(resources, 0, f, f, f, f);
        doc.registerObject(page);
        doc.addImage(context, new BitmapImage("", 1, 1, new byte[0], null));
        gen.flushPDFDoc();
        doc.outputTrailer(out);
        Assert.assertTrue(out.toString().contains("/Subtype /Image"));
        return out.toString();
    }
}
