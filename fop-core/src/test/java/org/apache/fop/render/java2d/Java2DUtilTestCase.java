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

package org.apache.fop.render.java2d;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.util.CharUtilities;


public class Java2DUtilTestCase {

    private static final String MULTI_BYTE_FONT_NAME = "multi";
    private static final String SINGLE_BYTE_FONT_NAME = "single";

    private static final String TEXT = "Hello World!\uD83D\uDCA9";
    private static final String EXPECTED_TEXT_SINGLE = "Hello World!#";
    private static final String EXPECTED_TEXT_MULTI = "Hello World!\uD83D\uDCA9";

    @Test
    public void createGlyphVectorMultiByte() throws Exception {
        Graphics2D g2d = mock(Graphics2D.class);
        java.awt.Font awtFont = mock(java.awt.Font.class);
        Font font = makeFont(MULTI_BYTE_FONT_NAME);
        FontInfo fontInfo = makeFontInfo();

        int[] codepoints = new int[EXPECTED_TEXT_MULTI.codePointCount(0, EXPECTED_TEXT_MULTI.length())];

        int i = 0;
        for (int cp : CharUtilities.codepointsIter(EXPECTED_TEXT_MULTI)) {
            codepoints[i++] = cp;
        }

        when(g2d.getFont()).thenReturn(awtFont);

        Java2DUtil.createGlyphVector(TEXT, g2d, font, fontInfo);
        verify(awtFont).createGlyphVector(any(FontRenderContext.class), eq(codepoints));
    }

    @Test
    public void createGlyphVectorSingleByte() throws Exception {
        Graphics2D g2d = mock(Graphics2D.class);
        java.awt.Font awtFont = mock(java.awt.Font.class);
        Font font = makeFont(SINGLE_BYTE_FONT_NAME);
        FontInfo fontInfo = makeFontInfo();

        when(g2d.getFont()).thenReturn(awtFont);

        Java2DUtil.createGlyphVector(TEXT, g2d, font, fontInfo);
        verify(awtFont).createGlyphVector(any(FontRenderContext.class), eq(EXPECTED_TEXT_SINGLE));
    }


    private FontInfo makeFontInfo() {
        Map<String, Typeface> fonts = new HashMap<String, Typeface>();

        SingleByteFont singleByteFont = mock(SingleByteFont.class);
        MultiByteFont multiByteFont = mock(MultiByteFont.class);
        FontInfo fontInfo = mock(FontInfo.class);

        fonts.put(MULTI_BYTE_FONT_NAME, multiByteFont);
        fonts.put(SINGLE_BYTE_FONT_NAME, singleByteFont);

        when(multiByteFont.findGlyphIndex(anyInt())).thenAnswer(new FindGlyphIndexAnswer());
        when(fontInfo.getFonts()).thenReturn(fonts);

        return fontInfo;
    }

    private Font makeFont(String fontName) {
        Font font = mock(Font.class);
        when(font.getFontName()).thenReturn(fontName);
        return font;
    }


    private static class FindGlyphIndexAnswer implements Answer<Integer> {

        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            return (Integer) invocation.getArguments()[0];
        }
    }
}
