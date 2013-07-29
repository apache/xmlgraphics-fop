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

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTLineMetrics;

import org.apache.fop.fonts.FontInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FOPFontFamilyResolverTestCase {

    private static FontInfo fontInfo;

    private FOPFontFamilyResolver resolver;

    @BeforeClass
    public static void setUpFontInfo() {
        fontInfo = new FontInfoBuilder()
                .useDejaVuLGCSerif()
                .useDroidSansMono()
                .build();
    }

    @Before
    public void createFontFamilyResolver() {
        resolver = new FOPFontFamilyResolverImpl(fontInfo);
    }

    @Test
    public void testResolve() {
        assertNull(resolver.resolve("Unavailable"));
        assertNotNull(resolver.resolve(FontInfoBuilder.DEJAVU_LGC_SERIF));
    }

    @Test
    public void testGetFamilyThatCanDisplay() {
        GVTFontFamily family = resolver.getFamilyThatCanDisplay('\u0180');
        assertEquals(FontInfoBuilder.DEJAVU_LGC_SERIF, family.getFamilyName());
        family = resolver.getFamilyThatCanDisplay('\u02F3');
        assertEquals(FontInfoBuilder.DROID_SANS_MONO, family.getFamilyName());
        family = resolver.getFamilyThatCanDisplay('\u02DF');
        assertNull(family);
    }

    @Test
    public void testDeriveFont() {
        FOPGVTFontFamily family = (FOPGVTFontFamily) resolver.resolve(FontInfoBuilder.DEJAVU_LGC_SERIF);
        FOPGVTFont font = family.deriveFont(10, Collections.emptyMap());
        assertEquals(10, font.getSize(), 0);
        assertTrue(font.canDisplay('\u01F6'));
        assertFalse(font.canDisplay('\u01F7'));
    }

    @Test
    @Ignore("FOP metrics don't match AWT, but not sure who is right and who is wrong")
    public void testLineMetrics() throws FontFormatException, IOException {
        FOPGVTFontFamily family = (FOPGVTFontFamily) resolver.resolve(FontInfoBuilder.DEJAVU_LGC_SERIF);
        FOPGVTFont font = family.deriveFont(10, Collections.emptyMap());
        GVTLineMetrics fopMetrics = font.getLineMetrics("", null);
        LineMetrics awtMetrics = getAWTLineMetrics();
        printDifference("Ascent", awtMetrics.getAscent(), fopMetrics.getAscent());
        printDifference("Descent", awtMetrics.getDescent(), fopMetrics.getDescent());
        printDifference("Height", awtMetrics.getHeight(), fopMetrics.getHeight());
        printDifference("Leading", awtMetrics.getLeading(), fopMetrics.getLeading());
        printDifference("StrikethroughOffset", awtMetrics.getStrikethroughOffset(),
                fopMetrics.getStrikethroughOffset());
        printDifference("StrikethroughThickness", awtMetrics.getStrikethroughThickness(),
                fopMetrics.getStrikethroughThickness());
        printDifference("UnderlineOffset", awtMetrics.getUnderlineOffset(),
                fopMetrics.getUnderlineOffset());
        printDifference("UnderlineThickness", awtMetrics.getUnderlineThickness(),
                fopMetrics.getUnderlineThickness());
    }

    private LineMetrics getAWTLineMetrics() throws FontFormatException, IOException {
        File fontFile = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontFile).deriveFont(10f);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        BufferedImage dummyImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        FontRenderContext frc = ge.createGraphics(dummyImage).getFontRenderContext();
        LineMetrics awtMetrics = awtFont.getLineMetrics("ABC", frc);
        return awtMetrics;
    }

    private void printDifference(String value, float awt, float fop) {
        System.out.println(String.format("%22s  AWT: %10f  FOP: %10f  Difference: %.2f%%", value, awt, fop,
                (fop - awt) / awt * 100));
    }

}
