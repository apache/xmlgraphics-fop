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

package org.apache.fop.render.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * One PDF holding FO text and SVG text in DejaVuLGCSerif, read back glyph by glyph through its ToUnicode. The CMap
 * is one per document, so a glyph given the wrong text in SVG text was wrong in the FO text too. Each line is read
 * as its text and the number of its glyphs that stand for more than one letter.
 */
public class SvgTextToUnicodeTestCase {

    private static final String WORDS = "office reflection";

    private static final String TEXT = "font-family=\"DejaVuLGCSerif\" font-size=\"12\"";

    private static List<Line> lines;

    private static final class Line {
        private final StringBuilder text = new StringBuilder();
        private int ligatures;
    }

    @BeforeClass
    public static void render() throws Exception {
        String conf = "<fop version=\"1.0\"><renderers><renderer mime=\"application/pdf\">"
                + "<filterList><value>null</value></filterList><fonts>"
                + "<font embed-url=\"test/resources/fonts/ttf/DejaVuLGCSerif.ttf\">"
                + "<font-triplet name=\"DejaVuLGCSerif\" style=\"normal\" weight=\"normal\"/></font>"
                + "</fonts></renderer></renderers></fop>";
        FopFactory factory = new FopConfParser(new ByteArrayInputStream(conf.getBytes("UTF-8")),
                new File(".").toURI()).getFopFactoryBuilder().build();
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"300\" height=\"50\">"
                + "<text x=\"0\" y=\"20\" " + TEXT + ">" + WORDS + "</text>"
                + "<text x=\"0 10 20 30 40 50\" y=\"40\" " + TEXT + ">office</text>"
                + "</svg>";
        String fo = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"><fo:layout-master-set>"
                + "<fo:simple-page-master master-name=\"m\" page-width=\"400pt\" page-height=\"400pt\" margin=\"20pt\">"
                + "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
                + "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\" "
                + "font-family=\"DejaVuLGCSerif\" font-size=\"12pt\">"
                + "<fo:block>Before " + WORDS + "</fo:block>"
                + "<fo:block><fo:instream-foreign-object>" + svg + "</fo:instream-foreign-object></fo:block>"
                + "<fo:block>After " + WORDS + "</fo:block>"
                + "</fo:flow></fo:page-sequence></fo:root>";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = factory.newFop(MimeConstants.MIME_PDF, factory.newFOUserAgent(), out);
        TransformerFactory.newInstance().newTransformer().transform(
                new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))),
                new SAXResult(fop.getDefaultHandler()));
        lines = read(out.toByteArray());
    }

    private static List<Line> read(byte[] pdf) throws IOException {
        final Map<Integer, Line> byY = new TreeMap<Integer, Line>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(TextPosition position) {
                Integer y = Math.round(position.getYDirAdj());
                Line line = byY.get(y);
                if (line == null) {
                    line = new Line();
                    byY.put(y, line);
                }
                String unicode = position.getUnicode();
                line.text.append(unicode);
                if (unicode.length() > 1) {
                    line.ligatures++;
                }
            }
        };
        try (PDDocument document = Loader.loadPDF(pdf)) {
            stripper.getText(document);
        }
        return new ArrayList<Line>(byY.values());
    }

    private static void assertLine(int index, String text, int ligatures) {
        Line line = lines.get(index);
        assertEquals("line " + index, text, line.text.toString());
        assertEquals("ligatures on line " + index + " (" + text + ")", ligatures, line.ligatures);
    }

    /** Every line reads its own letters, the FO text's before and after the SVG included. */
    @Test
    public void everyLineReadsItsOwnLetters() {
        assertEquals(4, lines.size());
        assertLine(0, "Before " + WORDS, 2);
        assertLine(1, WORDS, 2);
        assertLine(2, "office", 1);
        assertLine(3, "After " + WORDS, 2);
    }
}
