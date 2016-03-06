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
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.pdf.PDFContentGenerator;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.PDFPainter;

public class PDFLinearizationTestCase {
    private int objectLeast;
    private int[] objects;

    @Test
    public void testPDF() throws IOException {
        PDFDocument doc = new PDFDocument("");
        doc.setLinearizationEnabled(true);
        PDFResources resources = new PDFResources(doc);
        PDFResourceContext context = new PDFResourceContext(resources);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFContentGenerator gen = null;
        for (int i = 0; i < 2; i++) {
            gen = new PDFContentGenerator(doc, out, context);
            Rectangle2D.Float f = new Rectangle2D.Float();
            PDFPage page = new PDFPage(resources, i, f, f, f, f);
            doc.registerObject(page);
            doc.registerObject(gen.getStream());
            page.setContents(gen.getStream());
        }
        gen.flushPDFDoc();
        byte[] data = out.toByteArray();
        checkPDF(data);
    }

    @Test
    public void testImage() throws Exception {
        String fopxconf = "<fop version=\"1.0\"><renderers>"
                + "<renderer mime=\"application/pdf\">"
                + "<linearization>true</linearization>"
                + "</renderer></renderers></fop>";
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes()));
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        IFContext ifContext = new IFContext(foUserAgent);
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(ifContext);
        documentHandler.getConfigurator().configure(documentHandler);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        documentHandler.setFontInfo(new FontInfo());
        documentHandler.setResult(new StreamResult(out));
        documentHandler.startDocument();
        documentHandler.startPage(0, "", "", new Dimension());
        PDFPainter pdfPainter = new PDFPainter(documentHandler, null);
        pdfPainter.drawImage("test/resources/fop/svg/logo.jpg", new Rectangle());
        documentHandler.endPage();
        Assert.assertFalse(out.toString().contains("/Subtype /Image"));
        documentHandler.endDocument();
        Assert.assertTrue(out.toString().contains("/Subtype /Image"));
    }

    private void checkPDF(byte[] data) throws IOException {
        checkHintTable(data);
        InputStream is = new ByteArrayInputStream(data);
        Map<String, StringBuilder> objs = readObjs(is);

        List<String> keys = new ArrayList<String>(objs.keySet());
        int start = keys.indexOf("1 0 obj");
        Assert.assertTrue(start > 1);
        int j = 1;
        for (int i = start; i < keys.size(); i++) {
            Assert.assertEquals(keys.get(i), j + " 0 obj");
            j++;
        }
        for (int i = 0; i < start; i++) {
            Assert.assertEquals(keys.get(i), j + " 0 obj");
            j++;
        }

        checkFirstObj(data);
        checkTrailer(data);

        String firstObj = objs.values().iterator().next().toString().replace("\n", "");
        Assert.assertTrue(firstObj.startsWith("<<  /Linearized 1  /L " + data.length));
        Assert.assertTrue(firstObj.endsWith("startxref0%%EOF"));
        int pageObjNumber = getValue("/O", firstObj);
        Assert.assertTrue(objs.get(pageObjNumber + " 0 obj").toString().contains("/Type /Page"));
        Assert.assertTrue(objs.get("5 0 obj").toString().contains("/Type /Pages"));

        int total = 0;
        for (int i : objects) {
            total += i;
        }
        Assert.assertEquals(total, objs.size() - 6);
    }

    private void checkFirstObj(byte[] data) throws IOException {
        int firstObjPos = getValue("/E", getFirstObj(data));
        InputStream is = new ByteArrayInputStream(data);
        Assert.assertEquals(is.skip(firstObjPos), firstObjPos);
        byte[] obj = new byte[10];
        Assert.assertEquals(is.read(obj), obj.length);
        Assert.assertTrue(new String(obj).startsWith("1 0 obj"));
    }

    private void checkTrailer(byte[] data) throws IOException {
        int trailerPos = getValue("/T", getFirstObj(data));
        InputStream is = new ByteArrayInputStream(data);
        Assert.assertEquals(is.skip(trailerPos), trailerPos);
        byte[] obj = new byte[20];
        Assert.assertEquals(is.read(obj), obj.length);
        Assert.assertTrue(new String(obj).startsWith("0000000000 65535 f"));
    }

    private int getValue(String name, String firstObj) throws IOException {
        String[] split = firstObj.split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(name)) {
                return Integer.valueOf(split[i + 1].replace(">>", ""));
            }
        }
        throw new IOException(name + " not found " + firstObj);
    }

    private int[] getArrayValue(String name, String firstObj) throws IOException {
        String[] split = firstObj.split(" ");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals(name)) {
                int[] v = new int[2];
                v[0] = Integer.valueOf(split[i + 1].replace("[", ""));
                v[1] = Integer.valueOf(split[i + 2].replace("]", ""));
                return v;
            }
        }
        throw new IOException(name + " not found " + firstObj);
    }

    private String getFirstObj(byte[] out) throws IOException {
        InputStream data = new ByteArrayInputStream(out);
        Map<String, StringBuilder> objs = readObjs(data);
        return objs.values().iterator().next().toString().replace("\n", "");
    }

    private void checkHintTable(byte[] out) throws IOException {
        String firstObj = getFirstObj(out);
        int hintPos = getArrayValue("/H", firstObj)[0];
        int hintLength = getArrayValue("/H", firstObj)[1];

        InputStream data = new ByteArrayInputStream(out);
        Assert.assertEquals(data.skip(hintPos), hintPos);

        byte[] hintTable = new byte[hintLength];
        Assert.assertEquals(data.read(hintTable), hintLength);
        String hintTableStr = new String(hintTable);

        Assert.assertTrue(hintTableStr.contains("/S "));
        Assert.assertTrue(hintTableStr.contains("/C "));
        Assert.assertTrue(hintTableStr.contains("/E "));
        Assert.assertTrue(hintTableStr.contains("/L "));
        Assert.assertTrue(hintTableStr.contains("/V "));
        Assert.assertTrue(hintTableStr.contains("/O "));
        Assert.assertTrue(hintTableStr.contains("/I "));
        Assert.assertTrue(hintTableStr.contains("/Length "));
        Assert.assertTrue(hintTableStr.contains("stream"));
        Assert.assertTrue(hintTableStr.contains("endstream"));
        Assert.assertTrue(hintTableStr.endsWith("endobj\n"));

        data = new ByteArrayInputStream(hintTable);
        readStart(data);
        int pages = getValue("/N", firstObj);
        readObjectsTable(data, pages);
        readSharedObjectsTable(data);
        Assert.assertEquals(objectLeast, 1);
    }

    private void readObjectsTable(InputStream data, int pages)
            throws IOException {
        objectLeast = read32(data);
        read32(data);
        int bitsDiffObjects = read16(data);
        read32(data);
        int bitsDiffPageLength = read16(data);
        read32(data);
        read16(data);
        read32(data);
        read16(data);
        read16(data);
        read16(data);
        read16(data);
        read16(data);

        objects = new int[pages];
        for (int i = 0; i < pages; i++) {
            objects[i] = objectLeast + readBits(bitsDiffObjects, data);
        }
        for (int i = 0; i < pages; i++) {
            readBits(bitsDiffPageLength, data);
        }
        for (int i = 0; i < pages; i++) {
            readBits(32, data);
        }
    }

    private void readSharedObjectsTable(InputStream str) throws IOException {
        readBits(32, str);
        readBits(32, str);
        readBits(32, str);
        int sharedGroups = readBits(32, str);
        readBits(16, str);
        readBits(32, str);
        int bitsDiffGroupLength = readBits(16, str);
        for (int i = 0; i < sharedGroups; i++) {
            readBits(bitsDiffGroupLength, str);
        }
    }

    private int readBits(int bits, InputStream data) throws IOException {
        if (bits == 32) {
            return read32(data);
        }
        if (bits == 16) {
            return read16(data);
        }
        throw new IOException("Wrong bits");
    }

    private int read32(InputStream data) throws IOException {
        int ch1 = data.read();
        int ch2 = data.read();
        int ch3 = data.read();
        int ch4 = data.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    private int read16(InputStream data) throws IOException {
        int ch1 = data.read();
        int ch2 = data.read();
        return (ch1 << 8) + (ch2);
    }

    private void readStart(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (inputStream.available() > 0) {
            int data = inputStream.read();
            if (data == '\n') {
                if (sb.toString().equals("stream")) {
                    return;
                }
                sb.setLength(0);
            } else {
                sb.append((char)data);
            }
        }
    }

    public static Map<String, StringBuilder> readObjs(InputStream inputStream) throws IOException {
        Map<String, StringBuilder> objs = new LinkedHashMap<String, StringBuilder>();
        StringBuilder sb = new StringBuilder();
        String key = null;
        while (inputStream.available() > 0) {
            int data = inputStream.read();
            if (data == '\n') {
                if (sb.toString().endsWith(" 0 obj")) {
                    key = sb.toString().trim();
                    objs.put(key, new StringBuilder());
                } else if (key != null) {
                    objs.get(key).append(sb).append("\n");
                }
                sb.setLength(0);
            } else {
                sb.append((char)data);
            }
        }
        return objs;
    }
}
