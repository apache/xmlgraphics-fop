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
            new CMapSegment(0x2000B, 0x2000B, GI_SUPPLEMENTARY),
            new CMapSegment(Typeface.NOT_FOUND, Typeface.NOT_FOUND, GI_NOT_FOUND)
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

    /** the glyph of Typeface.NOT_FOUND, '#', drawn for a character the font lacks */
    private static final int GI_NOT_FOUND = 3;

    /** a glyph no character maps to, as a ligature glyph usually is */
    private static final int GI_LIGATURE = 50000;
    /** a second such glyph */
    private static final int GI_FORM = 50001;
    /** a third, a mark glyph a decomposition splits off */
    private static final int GI_MARK = 50002;

    /** A substitution that maps the whole input to the given glyphs, with the given associations. */
    private static Answer<GlyphSequence> substitutionTo(final int[] glyphs, final CharAssociation... associations) {
        return new Answer<GlyphSequence>() {
            public GlyphSequence answer(InvocationOnMock invocation) {
                GlyphSequence gs = (GlyphSequence) invocation.getArguments()[0];
                List list = new ArrayList();
                for (CharAssociation a : associations) {
                    list.add(a);
                }
                return new GlyphSequence(gs.getCharacters(), IntBuffer.wrap(glyphs), list);
            }
        };
    }

    /**
     * A ligature glyph, produced from two characters and mapped by none, still comes back
     * as one private-use character for layout, but records the two characters as what it stands
     * for, which is what the ToUnicode CMap publishes.
     */
    @Test
    public void testLigatureGlyphRecordsItsCharacters() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_LIGATURE}, new CharAssociation(0, 2))));
        CharSequence out = substitute(font, "生方");
        assertEquals(1, out.length());
        assertEquals(0xE000, out.charAt(0));
        assertEquals("生方", font.getGlyphMeaning(GI_LIGATURE));
    }

    /** A contextual form, one character to one unmapped glyph, records that character. */
    @Test
    public void testContextualFormRecordsItsCharacter() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_FORM}, new CharAssociation(0, 1))));
        substitute(font, "生");
        assertEquals("生", font.getGlyphMeaning(GI_FORM));
    }

    /** A glyph substitution left alone records nothing; its character map entry is its meaning. */
    @Test
    public void testUnsubstitutedGlyphRecordsNothing() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        substitute(font, "人");
        assertEquals(null, font.getGlyphMeaning(GI_REN));
    }

    /**
     * A decomposition puts one character's association on each glyph it produces. The first
     * glyph records the character; the second records nothing, since a ToUnicode entry cannot
     * say that two glyphs share one character, and keeps its private-use code point.
     */
    @Test
    public void testSecondGlyphOfOneCharacterRecordsNothing() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_FORM, GI_MARK},
                new CharAssociation(0, 1), new CharAssociation(0, 1))));
        substitute(font, "生");
        assertEquals("生", font.getGlyphMeaning(GI_FORM));
        assertEquals(null, font.getGlyphMeaning(GI_MARK));
    }

    /** A glyph seen standing for two different characters has no one meaning, and records none. */
    @Test
    public void testGlyphWithTwoMeaningsRecordsNone() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_FORM}, new CharAssociation(0, 1))));
        substitute(font, "生");
        assertEquals("生", font.getGlyphMeaning(GI_FORM));
        substitute(font, "方");
        assertEquals(null, font.getGlyphMeaning(GI_FORM));
    }

    /**
     * A ligature whose components had an ignored glyph between them carries a disjoint
     * association; it records its components only, the glyph between keeping its own.
     */
    @Test
    public void testDisjointAssociationRecordsItsComponentsOnly() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_LIGATURE, GI_JIAN},
                new CharAssociation(new int[] {0, 1, 2, 3}), new CharAssociation(1, 1))));
        substitute(font, "生牋方");
        assertEquals("生方", font.getGlyphMeaning(GI_LIGATURE));
        assertEquals(null, font.getGlyphMeaning(GI_JIAN));
    }

    /**
     * A character the font lacks is drawn with the stand-in glyph of Typeface.NOT_FOUND. That
     * glyph must not record the missing character as its meaning: it is not that character,
     * and a real '#' in the same document uses the same glyph.
     */
    @Test
    public void testStandInForMissingCharacterRecordsNothing() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        assertEquals("#", substitute(font, "\uF0A7").toString());
        assertEquals(null, font.getGlyphMeaning(GI_NOT_FOUND));
    }

    /** The subset publishes the recorded characters for the glyph's selector, and the code point otherwise. */
    @Test
    public void testSubsetPublishesTheRecordedCharacters() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(substitutionTo(new int[] {GI_LIGATURE}, new CharAssociation(0, 2))));
        CharSequence out = substitute(font, "生方");
        CIDSubset subset = new CIDSubset(font);
        subset.mapCodePoint(GI_JIAN, 0x724B);
        subset.mapCodePoint(GI_LIGATURE, out.charAt(0));
        String[] sequences = subset.getUnicodeSequences();
        assertEquals(3, sequences.length);
        assertEquals("\uFFFF", sequences[0]);
        assertEquals("牋", sequences[1]);
        assertEquals("生方", sequences[2]);
    }

    /** A supplementary plane character still comes back as its surrogate pair. */
    @Test
    public void testSupplementaryPlaneCharacter() {
        MultiByteFont font = createFont();
        font.setGSUB(mockGSUB(new IdentityAnswer()));
        assertEquals("𠀋", substitute(font, "𠀋").toString());
    }
}
