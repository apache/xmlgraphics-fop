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

package org.apache.fop.svg.font;

import java.awt.Rectangle;
import java.awt.font.GlyphMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTLineMetrics;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;

/**
 * Tests all the methods of {@link FOPGVTGlyphVector} with a mocked font.
 */
public class BasicGlyphVectorTestCase extends FOPGVTGlyphVectorTest {

    private final int fontSize = 10000;

    @Before
    public void createGlyphVector() {
        FontMetrics metrics = mockFontMetrics();
        Font font = mockFont(metrics);
        FOPGVTFont gvtFont = mockGVTFont(font);
        CharacterIterator it = new StringCharacterIterator("ABC");
        glyphVector = new FOPGVTGlyphVector(gvtFont, it, null);
        glyphVector.performDefaultLayout();
    }

    private FontMetrics mockFontMetrics() {
        FontMetrics metrics = mock(FontMetrics.class);
        when(metrics.getAscender(eq(fontSize))).thenReturn(8000000);
        when(metrics.getDescender(eq(fontSize))).thenReturn(-4000000);
        when(metrics.getWidth(eq(1), eq(fontSize))).thenReturn(10000000);
        when(metrics.getBoundingBox(eq(1), eq(fontSize))).thenReturn(
                new Rectangle(-1000000, -2000000, 3000000, 4000000));
        when(metrics.getWidth(eq(2), eq(fontSize))).thenReturn(11000000);
        when(metrics.getBoundingBox(eq(2), eq(fontSize))).thenReturn(
                new Rectangle(-5000000, -6000000, 7000000, 9000000));
        when(metrics.getWidth(eq(3), eq(fontSize))).thenReturn(12000000);
        when(metrics.getBoundingBox(eq(3), eq(fontSize))).thenReturn(
                new Rectangle(-9000000, -10000000, 11000000, 14000000));
        return metrics;
    }

    private Font mockFont(FontMetrics metrics) {
        Font font = mock(Font.class);
        when(font.getFontMetrics()).thenReturn(metrics);
        when(font.getFontSize()).thenReturn(fontSize);
        when(font.mapChar(eq('A'))).thenReturn((char) 1);
        when(font.mapChar(eq('B'))).thenReturn((char) 2);
        when(font.mapChar(eq('C'))).thenReturn((char) 3);
        return font;
    }

    private FOPGVTFont mockGVTFont(Font font) {
        FOPGVTFont gvtFont = mock(FOPGVTFont.class);
        when(gvtFont.getFont()).thenReturn(font);
        when(gvtFont.getLineMetrics(anyInt())).thenReturn(
                new GVTLineMetrics(8, 0, null, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        return gvtFont;
    }

    @Test
    public void getGlyphCodeReturnsGlyphIndex() {
        assertEquals(1, glyphVector.getGlyphCode(0));
        assertEquals(2, glyphVector.getGlyphCode(1));
        assertEquals(3, glyphVector.getGlyphCode(2));
    }

    @Test
    public void testGetGlyphCodes() {
        assertArrayEquals(new int[] {1, 2, 3}, glyphVector.getGlyphCodes(0, 3, null));
        assertArrayEquals(new int[] {2, 3}, glyphVector.getGlyphCodes(1, 2, null));
    }

    @Test
    public void testGetGlyphMetrics() {
        assertGlyphMetricsEqual(new GVTGlyphMetrics(10, 12, new Rectangle(-1, -2, 3, 4), GlyphMetrics.STANDARD),
                glyphVector.getGlyphMetrics(0));
        assertGlyphMetricsEqual(new GVTGlyphMetrics(11, 12, new Rectangle(-5, -3, 7, 9), GlyphMetrics.STANDARD),
                glyphVector.getGlyphMetrics(1));
        assertGlyphMetricsEqual(new GVTGlyphMetrics(12, 12, new Rectangle(-9, -4, 11, 14), GlyphMetrics.STANDARD),
                glyphVector.getGlyphMetrics(2));
    }

    private void assertGlyphMetricsEqual(GVTGlyphMetrics expected, GVTGlyphMetrics actual) {
        assertEquals(expected.getHorizontalAdvance(), actual.getHorizontalAdvance(), 0);
        assertEquals(expected.getVerticalAdvance(), actual.getVerticalAdvance(), 0);
        assertEquals(expected.getBounds2D(), actual.getBounds2D());
        assertEquals(expected.getLSB(), actual.getLSB(), 0);
        assertEquals(expected.getRSB(), actual.getRSB(), 0);
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.isCombining(), actual.isCombining());
        assertEquals(expected.isComponent(), actual.isComponent());
        assertEquals(expected.isLigature(), actual.isLigature());
        assertEquals(expected.isStandard(), actual.isStandard());
        assertEquals(expected.isWhitespace(), actual.isWhitespace());
    }

    @Test
    public void testGetGlyphPosition() {
        assertEquals(new Point2D.Float(0, 0), glyphVector.getGlyphPosition(0));
        assertEquals(new Point2D.Float(10, 0), glyphVector.getGlyphPosition(1));
        assertEquals(new Point2D.Float(21, 0), glyphVector.getGlyphPosition(2));
        assertEquals(new Point2D.Float(33, 0), glyphVector.getGlyphPosition(3));
    }

    @Test
    public void testGetGlyphPositions() {
        float[] expectedPositions = new float[] {0, 0, 10, 0, 21, 0, 33, 0};
        assertArrayEquals(expectedPositions, glyphVector.getGlyphPositions(0, 4, null), 0);
        assertArrayEquals(expectedPositions, glyphVector.getGlyphPositions(0, 4, new float[8]), 0);
    }

    @Test
    public void testGetGlyphOutline() {
        assertEquals(new Rectangle(-1, -2, 3, 4), glyphVector.getGlyphOutline(0).getBounds());
        assertEquals(new Rectangle(5, -3, 7, 9), glyphVector.getGlyphOutline(1).getBounds());
        assertEquals(new Rectangle(12, -4, 11, 14), glyphVector.getGlyphOutline(2).getBounds());
    }

    @Test
    public void testGetOutline() {
        assertEquals(new Rectangle(-1, -4, 24, 14), glyphVector.getOutline().getBounds());
    }

    @Test
    public void testGetLogicalBounds() {
        assertEquals(new Rectangle(0, -8, 33, 12), glyphVector.getLogicalBounds());
    }

    @Test
    public void testGetLogicalBoundsRotated() {
        for (int i = 0; i < 3; i++) {
            glyphVector.setGlyphTransform(i, new AffineTransform(0.7, 0.7, -0.7, 0.7, 0, 0));
        }
        assertEquals(new Rectangle2D.Float(-2.8f, -5.6f, 37.8f, 16.8f), glyphVector.getLogicalBounds());
    }

    @Test
    public void testGetBounds() {
        assertEquals(new Rectangle(-1, -4, 24, 14), glyphVector.getBounds2D(null));
    }

    @Test
    public void testGetGlyphVisualBounds() {
        assertEquals(new Rectangle(-1, -2, 3, 4), glyphVector.getGlyphVisualBounds(0).getBounds());
        assertEquals(new Rectangle(5, -3, 7, 9), glyphVector.getGlyphVisualBounds(1).getBounds());
        assertEquals(new Rectangle(12, -4, 11, 14), glyphVector.getGlyphVisualBounds(2).getBounds());
    }

    @Test
    public void testGetGlyphLogicalBounds() {
        assertEquals(new Rectangle(0, -8, 10, 12), glyphVector.getGlyphLogicalBounds(0).getBounds());
        assertEquals(new Rectangle(10, -8, 11, 12), glyphVector.getGlyphLogicalBounds(1).getBounds());
        assertEquals(new Rectangle(21, -8, 12, 12), glyphVector.getGlyphLogicalBounds(2).getBounds());
    }

}
