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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.util.GlyphSequence;

/**
 * Which format characters {@link MultiByteFont#performSubstitution} keeps in the mapped
 * sequence, and so in the PDF's text layer, and which it elides (FOP-3347).
 */
public class MultiByteFontFormatCharacterTestCase {

    private static final int GI_NOT_FOUND = 3;
    private static final int GI_A = 10;
    private static final int GI_RLM = 11;
    private static final int GI_INHIBIT_SWAPPING = 12;
    private static final int GI_WIDE_FORMAT = 13;

    /**
     * A Latin font of a common shape: zero-width glyphs for the right-to-left mark and for
     * U+206A, a glyph of nonzero width for U+206B, and no glyph for the zero-width non-joiner.
     * The substitution table leaves every glyph alone.
     */
    private MultiByteFont createFont() {
        MultiByteFont font = new MultiByteFont(null, null);
        font.setCMap(new CMapSegment[] {
            new CMapSegment('a', 'a', GI_A),
            new CMapSegment(0x200F, 0x200F, GI_RLM),
            new CMapSegment(0x206A, 0x206A, GI_INHIBIT_SWAPPING),
            new CMapSegment(0x206B, 0x206B, GI_WIDE_FORMAT),
            new CMapSegment(Typeface.NOT_FOUND, Typeface.NOT_FOUND, GI_NOT_FOUND)
        });
        int[] widths = new int[GI_WIDE_FORMAT + 1];
        widths[GI_A] = 500;
        widths[GI_RLM] = 0;
        widths[GI_INHIBIT_SWAPPING] = 0;
        widths[GI_WIDE_FORMAT] = 750;
        widths[GI_NOT_FOUND] = 600;
        font.setWidthArray(widths);
        GlyphSubstitutionTable gsub = mock(GlyphSubstitutionTable.class);
        when(gsub.preProcess(any(CharSequence.class), anyString(), any(MultiByteFont.class),
                any(List.class))).thenAnswer(new Answer<CharSequence>() {
                    public CharSequence answer(InvocationOnMock invocation) {
                        return (CharSequence) invocation.getArguments()[0];
                    }
                });
        when(gsub.substitute(any(GlyphSequence.class), anyString(), anyString()))
                .thenAnswer(new Answer<GlyphSequence>() {
                    public GlyphSequence answer(InvocationOnMock invocation) {
                        return (GlyphSequence) invocation.getArguments()[0];
                    }
                });
        font.setGSUB(gsub);
        return font;
    }

    private String substitute(String text) {
        return createFont().performSubstitution(text, "latn", "dflt", new ArrayList(), false).toString();
    }

    /** A format character the font has a zero-width glyph for stays in the mapped sequence. */
    @Test
    public void testFormatCharacterWithZeroWidthGlyphIsKept() {
        assertEquals("a⁪a", substitute("a⁪a"));
    }

    /**
     * A bidi control is elided even with a zero-width glyph: the text layer is in visual order
     * already, and a reader would apply the control a second time.
     */
    @Test
    public void testBidiControlIsElided() {
        assertEquals("aa", substitute("a‏a"));
    }

    /** One the font has no glyph for is elided, rather than drawn as the missing glyph. */
    @Test
    public void testFormatCharacterWithoutGlyphIsElided() {
        assertEquals("aa", substitute("a‌a"));
    }

    /** One whose glyph has an advance is elided: keeping it would move the text. */
    @Test
    public void testFormatCharacterWithWideGlyphIsElided() {
        assertEquals("aa", substitute("a⁫a"));
    }

    /** A C0 control is never kept, glyph or no glyph. */
    @Test
    public void testControlCharacterIsElided() {
        assertEquals("aa", substitute("a\ra"));
    }
}
