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

import java.awt.geom.Point2D;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.batik.gvt.font.GVTLineMetrics;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;


public class KerningGlyphLayoutTestCase extends FOPGVTGlyphVectorTest {
    private final int fontSize = 37500;

    @Before
    public void createGlyphVector() {
        FontMetrics metrics = mockFontMetrics();
        Font font = mockFont(metrics);
        FOPGVTFont gvtFont = mockGVTFont(font);
        CharacterIterator it = new StringCharacterIterator("AVo", 1, 3, 1);
        glyphVector = new FOPGVTGlyphVector(gvtFont, it, null);
        glyphVector.performDefaultLayout();
    }

    private FontMetrics mockFontMetrics() {
        FontMetrics metrics = mock(FontMetrics.class);
        when(metrics.getWidth(eq(1), eq(fontSize))).thenReturn(25012000);
        when(metrics.getWidth(eq(2), eq(fontSize))).thenReturn(22912000);
        when(metrics.getWidth(eq(3), eq(fontSize))).thenReturn(20850000);
        return metrics;
    }

    private Font mockFont(FontMetrics metrics) {
        Font font = mock(Font.class);
        when(font.getFontMetrics()).thenReturn(metrics);
        when(font.getFontSize()).thenReturn(fontSize);
        when(font.mapChar(eq('A'))).thenReturn((char) 1);
        when(font.mapChar(eq('V'))).thenReturn((char) 2);
        when(font.mapChar(eq('o'))).thenReturn((char) 3);
        when(font.hasKerning()).thenReturn(true);
        when(font.getCharWidth('A')).thenReturn(25012);
        when(font.getCharWidth('V')).thenReturn(22912);
        when(font.getCharWidth('o')).thenReturn(20850);
        when(font.getCharWidth(65)).thenReturn(25012);
        when(font.getCharWidth(86)).thenReturn(22912);
        when(font.getCharWidth(111)).thenReturn(20850);
        when(font.getKernValue('V', 'o')).thenReturn(-2812);
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
    public void testGlyphPositions() {
        assertEquals(new Point2D.Float(20.1f, 0), glyphVector.getGlyphPosition(1));
        assertEquals(new Point2D.Float(40.95f, 0), glyphVector.getGlyphPosition(2));
    }
}
