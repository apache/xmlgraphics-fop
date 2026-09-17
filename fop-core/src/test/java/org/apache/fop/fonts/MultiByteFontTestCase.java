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

import java.nio.IntBuffer;
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
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;

/**
 * Tests the reverse mapping of glyphs to characters performed at the end of
 * {@link MultiByteFont#performSubstitution}.
 */
public class MultiByteFontTestCase {

    /** glyph shared by U+2F08 (Kangxi radical) and U+4EBA (ideograph) */
    private static final int GI_REN = 8966;
    /** glyph shared by U+2F45 (Kangxi radical) and U+65B9 (ideograph) */
    private static final int GI_FANG = 14819;
    /** glyph shared by U+2F63 (Kangxi radical) and U+751F (ideograph) */
    private static final int GI_SHENG = 18742;
    /** glyph of U+724B, an ideograph no radical shares */
    private static final int GI_JIAN = 29000;
    /** glyph of U+2000B, a supplementary plane ideograph */
    private static final int GI_SUPPLEMENTARY = 40000;

    /**
     * A CJK font maps a Kangxi radical and the ideograph it is the radical of to one glyph,
     * as Source Han Sans CN does for the three pairs used here.
     */
    private MultiByteFont createFont() {
        MultiByteFont font = new MultiByteFont(null, null);
        font.setCMap(new CMapSegment[] {
            new CMapSegment(0x2F08, 0x2F08, GI_REN),
            new CMapSegment(0x2F45, 0x2F45, GI_FANG),
            new CMapSegment(0x2F63, 0x2F63, GI_SHENG),
            new CMapSegment(0x4EBA, 0x4EBA, GI_REN),
            new CMapSegment(0x65B9, 0x65B9, GI_FANG),
            new CMapSegment(0x724B, 0x724B, GI_JIAN),
            new CMapSegment(0x751F, 0x751F, GI_SHENG),
            new CMapSegment(0x2000B, 0x2000B, GI_SUPPLEMENTARY)
        });
        return font;
    }

    private GlyphSubstitutionTable mockGSUB(Answer<GlyphSequence> substitution) {
        GlyphSubstitutionTable gsub = mock(GlyphSubstitutionTable.class);
        when(gsub.preProcess(any(CharSequence.class), anyString(), any(MultiByteFont.class),
                any(List.class))).thenAnswer(new Answer<CharSequence>() {
                    public CharSequence answer(InvocationOnMock invocation) {
                        return (CharSequence) invocation.getArguments()[0];
                    }
                });
        when(gsub.substitute(any(GlyphSequence.class), anyString(), anyString())).thenAnswer(substitution);
        return gsub;
    }

    /** A substitution which leaves every glyph of the sequence alone. */
    private static class IdentityAnswer implements Answer<GlyphSequence> {
        public GlyphSequence answer(InvocationOnMock invocation) {
            return (GlyphSequence) invocation.getArguments()[0];
        }
    }

    private CharSequence substitute(MultiByteFont font, String text) {
        return font.performSubstitution(text, "hani", "dflt", new ArrayList(), false);
    }

    /**
     * An ideograph whose glyph is shared with a Kangxi radical must come back as the ideograph,
     * not as the radical, which is merely the lower of the two code points mapped to that glyph.
     */
    @Test
    public void testIdeographSharingGlyphWithRadical() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        assertEquals("生方人牋", substitute(font, "生方人牋").toString());
    }

    /** A radical which really was written stays a radical. */
    @Test
    public void testRadicalItself() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        assertEquals("⽣⽅⼈", substitute(font, "⽣⽅⼈").toString());
    }

    /** A glyph the substitution did produce is still mapped back through the character map. */
    @Test
    public void testSubstitutedGlyphUsesCharacterMap() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new Answer<GlyphSequence>() {
            public GlyphSequence answer(InvocationOnMock invocation) {
                GlyphSequence gs = (GlyphSequence) invocation.getArguments()[0];
                List associations = new ArrayList();
                associations.add(new CharAssociation(0, gs.getCharacterCount()));
                return new GlyphSequence(gs.getCharacters(), IntBuffer.wrap(new int[] {GI_JIAN}),
                        associations);
            }
        }));
        assertEquals("牋", substitute(font, "生方").toString());
    }

    /** A supplementary plane character still comes back as its surrogate pair. */
    @Test
    public void testSupplementaryPlaneCharacter() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        assertEquals("𠀋", substitute(font, "𠀋").toString());
    }
}
