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

package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.batik.bridge.TextPainter;
import org.apache.batik.gvt.text.TextPaintInfo;

import org.apache.xmlgraphics.java2d.GraphicContext;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.base14.Base14FontCollection;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;

public class PDFTextPainterTestCase extends NativeTextPainterTest {

    private static class OperatorCheckingPDFGraphics2D extends PDFGraphics2D {

        OperatorCheckingPDFGraphics2D(FontInfo fontInfo, final OperatorValidator validator) {
            super(false, fontInfo, new PDFDocument("test"), null, null, null, 0, null);
            this.currentStream = new StringWriter() {

                @Override
                public void write(String str) {
                    validator.check(str);
                }

            };
        }
    }

    @Override
    protected TextPainter createTextPainter(FontInfo fontInfo) {
        return new PDFTextPainter(fontInfo);
    }

    @Override
    protected Graphics2D createGraphics2D(FontInfo fontInfo, OperatorValidator validator) {
        PDFGraphics2D g2d = new OperatorCheckingPDFGraphics2D(fontInfo, validator);
        g2d.setGraphicContext(new GraphicContext());
        return g2d;
    }

    @Test
    public void testRotatedGlyph() throws Exception {
        runTest("rotated-glyph.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "1 0 0 -1 40 110 Tm ")
                .addOperatorMatch("TJ", "[(A)] TJ\n")
                .addOperatorMatch("Tm", "0.70710677 0.7071068 0.7071068 -0.70710677 106.69999695 110 Tm ")
                .addOperatorMatch("TJ", "[(B)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 173.3999939 110 Tm ")
                .addOperatorMatch("TJ", "[(C)] TJ\n"));
    }

    @Test
    public void testDxDy() throws Exception {
        runTest("dx-dy.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "1 0 0 -1 55 35 Tm ")
                .addOperatorMatch("TJ", "[(ABCDE)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 55 75 Tm ")
                .addOperatorMatch("TJ", "[(A)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 69 85 Tm ")
                .addOperatorMatch("TJ", "[(B)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 109 80 Tm ")
                .addOperatorMatch("TJ", "[(C)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 91 65 Tm ")
                .addOperatorMatch("TJ", "[(D)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 127 75 Tm ")
                .addOperatorMatch("TJ", "[(E)] TJ\n"));
    }

    @Test
    public void testSpacing() throws Exception {
        runTest("spacing.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "1 0 0 -1 0 0 Tm ")
                .addOperatorMatch("TJ", "[(V) 80 (A) 70 (V)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 0 0 Tm ")
                .addOperatorMatch("TJ", "[(V) 80 (A) 70 (V)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 0 0 Tm ")
                .addOperatorMatch("TJ", "[(V) -20 (A) -30 (V)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 0 0 Tm ")
                .addOperatorMatch("TJ", "[(ab) -111 ( ) -389 (cd)] TJ\n"));
    }

    @Test
    public void testGlyphOrientation() throws Exception {
        runTest("glyph-orientation.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "0 1 1 0 738.5 0 Tm ")
                .addOperatorMatch("TJ", "[(A)] TJ\n")
                .addOperatorMatch("Tm", "0 1 1 0 738.5 667 Tm ")
                .addOperatorMatch("TJ", "[(B)] TJ\n")
                .addOperatorMatch("Tm", "0 1 1 0 738.5 1334 Tm ")
                .addOperatorMatch("TJ", "[(C)] TJ\n")
                .addOperatorMatch("Tm", "0 1 1 0 738.5 2056 Tm ")
                .addOperatorMatch("TJ", "[(D)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 2149 718 Tm ")
                .addOperatorMatch("TJ", "[(E)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 2165.5 1643 Tm ")
                .addOperatorMatch("TJ", "[(F)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 2124 2568 Tm ")
                .addOperatorMatch("TJ", "[(G)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 2138.5 3493 Tm ")
                .addOperatorMatch("TJ", "[(H)] TJ\n")
                .addOperatorMatch("Tm", "0 -1 -1 0 718 5000 Tm ")
                .addOperatorMatch("TJ", "[(I)] TJ\n")
                .addOperatorMatch("Tm", "0 -1 -1 0 1643 5000 Tm ")
                .addOperatorMatch("TJ", "[(J)] TJ\n")
                .addOperatorMatch("Tm", "0 -1 -1 0 2568 5000 Tm ")
                .addOperatorMatch("TJ", "[(K)] TJ\n")
                .addOperatorMatch("Tm", "0 -1 -1 0 3493 5000 Tm ")
                .addOperatorMatch("TJ", "[(L)] TJ\n"));
    }

    @Test
    public void testBaselineShift() throws Exception {
        runTest("baseline-shift.svg", new OperatorValidator()
                .addOperatorMatch("Tm", "1 0 0 -1 0 0 Tm ")
                .addOperatorMatch("TJ", "[(AB)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 1334 -462.5 Tm ")
                .addOperatorMatch("TJ", "[(CD)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 2778 0 Tm ")
                .addOperatorMatch("TJ", "[(EF)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 4056 462.5 Tm ")
                .addOperatorMatch("TJ", "[(GH)] TJ\n")
                .addOperatorMatch("Tm", "1 0 0 -1 5556 0 Tm ")
                .addOperatorMatch("TJ", "[(IJ)] TJ\n"));
    }

    /**
     * Tests that glyph vectors in single-byte fonts with glyph position adjustments are properly written.
     * @throws Exception
     */
    @Test
    public void testSingleByteAdjustments() throws Exception {
        defaultSingleByteAdjustmentsTest(1, "BT\n"
                + "3 Tr\n"
                + "0.002 0.003 Td\n"
                + "/F5 0.012 Tf\n"
                + "1 0 0 -1 0 0 Tm [(\\0)] TJ\n"
                + "ET\n");
    }

    @Test
    public void testSingleByteAdjustmentsMultipleGlyphs() throws Exception {
        defaultSingleByteAdjustmentsTest(3, "BT\n"
                + "3 Tr\n"
                + "0.002 0.003 Td\n"
                + "/F5 0.012 Tf\n"
                + "1 0 0 -1 0 0 Tm 0.008 0.009 Td\n"
                + "[(\\0)] TJ\n"
                + "1 0 0 -1 1 1 Tm 0.009 0.009 Td\n"
                + "[(\\1)] TJ\n"
                + "1 0 0 -1 2 2 Tm [(\\2)] TJ\n"
                + "ET\n");
    }

    private void defaultSingleByteAdjustmentsTest(int numberOfGlyphs, String expected) throws Exception {
        FontInfo fontInfo = new FontInfo();
        new Base14FontCollection(true).setup(0, fontInfo);

        TextPaintInfo tpi = new TextPaintInfo();
        tpi.visible = true;

        PDFGraphics2D g2d = mock(PDFGraphics2D.class);
        g2d.fontInfo = fontInfo;
        g2d.currentStream = new StringWriter();

        PDFTextPainter painter = new PDFTextPainter(fontInfo);
        painter.preparePainting(g2d);
        painter.setInitialTransform(new AffineTransform());
        painter.tpi = tpi;
        painter.beginTextObject();

        FOPGVTGlyphVector mockGV = mock(FOPGVTGlyphVector.class);
        FontTriplet triplet = new FontTriplet("Times", "normal", 400);
        Font font = fontInfo.getFontInstance(triplet, 12);

        FOPGVTFont mockGvtFont = mock(FOPGVTFont.class);
        FontMetrics fontMetrics = font.getFontMetrics();
        when(mockGvtFont.getFont()).thenReturn(new Font("F5", triplet, fontMetrics, 12));
        when(mockGvtFont.getFontKey()).thenReturn("F5");

        when(mockGV.getFont()).thenReturn(mockGvtFont);
        addGlyphs(mockGV, numberOfGlyphs);

        GeneralPath gp = new GeneralPath();

        painter.writeGlyphs(mockGV, gp);
        painter.endTextObject();

        assertEquals("Must write a Text Matrix for each glyph and use the glyph adjustments",
                expected, g2d.currentStream.toString());
    }

    private void addGlyphs(FOPGVTGlyphVector mockGV, int max) {
        int[][] array = new int[max][4];
        for (int i = 0; i < max; i++) {
            when(mockGV.getGlyphPosition(i)).thenReturn(new Point(i, i));
            when(mockGV.getNumGlyphs()).thenReturn(max);
            when(mockGV.getGlyphCode(i)).thenReturn(i);
            when(mockGV.isGlyphVisible(i)).thenReturn(true);
            array[i][0] = 2 + i * 4;
            array[i][1] = 3 + i * 4;
            array[i][2] = 4 + i * 4;
            array[i][3] = 5 + i * 4;
        }

        when(mockGV.getGlyphPositionAdjustments()).thenReturn(array);
    }
}
