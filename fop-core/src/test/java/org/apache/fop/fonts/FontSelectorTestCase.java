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

package org.apache.fop.fonts;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fo.properties.FontFamilyProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.Property;


public class FontSelectorTestCase {

    private static final FontTriplet LATIN_FONT_TRIPLET = new FontTriplet("Verdana", "normal", 400);
    private static final FontTriplet EMOJI_FONT_TRIPLET = new FontTriplet("Emoji", "normal", 400);

    private FOText foText;
    private PercentBaseContext context;
    private Font latinFont;
    private Font emojiFont;

    @Before
    public void setUp() throws Exception {
        FontTriplet[] fontState = new FontTriplet[] { LATIN_FONT_TRIPLET, EMOJI_FONT_TRIPLET };

        foText = mock(FOText.class);
        context = mock(PercentBaseContext.class);
        FOEventHandler eventHandler = mock(FOEventHandler.class);
        FontInfo fontInfo = mock(FontInfo.class);
        CommonFont commonFont = makeCommonFont();
        latinFont = mock(Font.class, "Latin Font");
        emojiFont = mock(Font.class, "Emoji Font");

        when(eventHandler.getFontInfo()).thenReturn(fontInfo);
        when(foText.getFOEventHandler()).thenReturn(eventHandler);
        when(foText.getCommonFont()).thenReturn(commonFont);
        when(commonFont.getFontState(fontInfo)).thenReturn(fontState);
        when(fontInfo.getFontInstance(eq(LATIN_FONT_TRIPLET), anyInt())).thenReturn(latinFont);
        when(fontInfo.getFontInstance(eq(EMOJI_FONT_TRIPLET), anyInt())).thenReturn(emojiFont);
        when(latinFont.hasCodePoint(anyInt())).thenAnswer(new LatinFontAnswer());
        when(emojiFont.hasCodePoint(anyInt())).thenAnswer(new EmojiFontAnswer());
    }

    @Test
    public void selectFontForCharactersInText() throws Exception {
        String latinText = "Hello FontSelector";
        String emojiText = "\uD83D\uDE48\uD83D\uDE49\uD83D\uDE4A";
        String mixedText = latinText + emojiText;


        Font f = FontSelector.selectFontForCharactersInText(latinText, 0, latinText.length(), foText, context);
        assertEquals(latinFont, f);

        f = FontSelector.selectFontForCharactersInText(emojiText, 0, emojiText.length(), foText, context);
        assertEquals(emojiFont, f);

        // When the text is mixed the font that can cover most chars should be returned
        f = FontSelector.selectFontForCharactersInText(mixedText, 0, mixedText.length(), foText, context);
        assertEquals(latinFont, f);

        f = FontSelector.selectFontForCharactersInText(mixedText, latinText.length() - 1, mixedText.length(), foText,
                context);
        assertEquals(emojiFont, f);
    }

    private static class LatinFontAnswer implements Answer<Boolean> {

        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            int codepoint = (Integer) invocation.getArguments()[0];
            return codepoint <= 0xFFFF;
        }
    }

    private static class EmojiFontAnswer implements Answer<Boolean> {

        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            int codepoint = (Integer) invocation.getArguments()[0];
            return codepoint > 0xFFFF;
        }
    }

    private CommonFont makeCommonFont() throws PropertyException {
        PropertyList pList = mock(PropertyList.class);

        String fontFamilyVal = LATIN_FONT_TRIPLET.getName() + "," + EMOJI_FONT_TRIPLET.getName();
        Property fontFamilyProp = new FontFamilyProperty.Maker(Constants.PR_FONT_FAMILY).make(pList, fontFamilyVal,
                null);
        Property fontWeightProp = EnumProperty.getInstance(Constants.PR_FONT_WEIGHT, "400");
        Property fontStyle = EnumProperty.getInstance(Constants.PR_FONT_STYLE, "normal");
        Property fontSizeAdjustProp = NumberProperty.getInstance(1);
        Property fontSizeProp = FixedLength.getInstance(12);

        when(pList.get(Constants.PR_FONT_FAMILY)).thenReturn(fontFamilyProp);
        when(pList.get(Constants.PR_FONT_WEIGHT)).thenReturn(fontWeightProp);
        when(pList.get(Constants.PR_FONT_STYLE)).thenReturn(fontStyle);
        when(pList.get(Constants.PR_FONT_SIZE_ADJUST)).thenReturn(fontSizeAdjustProp);
        when(pList.get(Constants.PR_FONT_SIZE)).thenReturn(fontSizeProp);

        return CommonFont.getInstance(pList);
    }

}
