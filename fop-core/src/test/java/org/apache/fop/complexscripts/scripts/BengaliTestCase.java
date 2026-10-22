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
package org.apache.fop.complexscripts.scripts;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.complexscripts.fonts.GlyphClassTable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.OTFLanguage;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.OFFontLoader;

public class BengaliTestCase {

    @Test
    public void testProcessorIsBengali() {
        // The Bengali script tags must resolve to a BengaliScriptProcessor.
        Assert.assertTrue(IndicScriptProcessor.makeProcessor(OTFScript.BENGALI)
                instanceof BengaliScriptProcessor);
        Assert.assertTrue(IndicScriptProcessor.makeProcessor(OTFScript.BENGALI_V2)
                instanceof BengaliScriptProcessor);
    }

    @Test
    public void testProcessor() {
        // A base glyph (Bengali KA, 0x0995, maps to glyph 1, a non-mark) followed by a
        // combining mark glyph (Bengali candrabindu, 0x0981, maps to glyph 0, classified
        // as a mark) that carries a horizontal positioning adjustment. The adjusted mark
        // must be reordered ahead of the base, leaving the base (0x0995) as the final glyph.
        String input = "\u0995\u0981";
        MyMultiByteFont font = new MyMultiByteFont(null, null);
        font.setWidthArray(new int[0]);
        font.callAddPrivateUseMapping('\u0995', 1);
        GlyphSubtable table = GlyphDefinitionTable.createSubtable(1, "lu0d", 0, 0, 1,
                GlyphClassTable.createClassTable(Arrays.asList(0, GlyphDefinitionTable.GLYPH_CLASS_MARK)), null);
        font.setGDEF(new GlyphDefinitionTable(Collections.singletonList(table),
                new HashMap<String, ScriptProcessor>()));
        int[][] gpa = new int[][] {{0, 0, 0, 0}, {5, 0, 0, 0}};
        String actual = font.reorderCombiningMarks(input, gpa, OTFScript.BENGALI_V2, null, null).toString();
        Assert.assertTrue(actual.endsWith("\u0995"));
    }

    @Test
    public void testHelloWorld() throws Exception {
        final String font = "test/resources/fonts/ttf/Lohit-Bengali.ttf";
        // "\u09b9\u09cd\u09af\u09be\u09b2\u09cb \u0993\u09af\u09bc\u09be\u09b0\u09cd\u09b2\u09cd\u09a1" (hello world)
        final String text =
                "\u09b9\u09cd\u09af\u09be\u09b2\u09cb \u0993\u09af\u09bc\u09be\u09b0\u09cd\u09b2\u09cd\u09a1";
        // glyph ids in Lohit-Bengali.ttf
        final int[] glyphs = {
                98, 156, 101,   // habeng, yabeng_viramabeng.pstf, aavowelbeng
                108, 94, 101,   // evowelbeng, labeng, aavowelbeng: O sign split, pre-base E reordered before LA
                3,              // space
                65,             // obeng
                117, 101,       // yyabeng, aavowelbeng
                360, 440        // labeng_viramabeng_ddabeng, rabeng_viramabeng: reph reordered after base
        };
        Assert.assertArrayEquals(glyphs, shape(font, OTFScript.BENGALI_V2, text));
        Assert.assertArrayEquals(glyphs, shape(font, OTFScript.BENGALI, text));
    }

    @Test
    public void testConjuncts() throws Exception {
        // "হ্যালো ক্ত ল্ড": ya-phala and conjuncts formed by contextual half forms
        final String text = "\u09B9\u09CD\u09AF\u09BE\u09B2\u09CB \u0995\u09CD\u09A4 \u09B2\u09CD\u09A1";
        // glyph ids in NotoSansBengali-Regular.ttf, as shaped by HarfBuzz
        final int[] glyphs = {
                56, 409, 59,    // uni09B9, uni09AF.pstf.short, uni09BE
                66, 52, 59,     // uni09C7, uni09B2, uni09BE
                7,              // space
                151, 320, 346,  // uni09A409CD09A4, uni0995.arm2, headlinebeng.200: KA + VIRAMA + TA conjunct
                7,              // space
                243, 262        // uni09B209CD.half3, uni09A1.side: half LA + DDA
        };
        final String font = "test/resources/fonts/ttf/NotoSansBengali-Regular.ttf";
        Assert.assertArrayEquals(glyphs, shape(font, OTFScript.BENGALI_V2, text));
    }

    private static int[] shape(String fontFile, String script, String text) throws Exception {
        InternalResourceResolver resourceResolver =
                ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
        MultiByteFont font = (MultiByteFont) new OFFontLoader(new File(fontFile).toURI(), resourceResolver).getFont();
        GlyphSequence sequence = font.charSequenceToGlyphSequence(text, null);
        return font.getGSUB().substitute(sequence, script, OTFLanguage.DEFAULT).getGlyphs(0, -1);
    }

    static class MyMultiByteFont extends MultiByteFont {
        MyMultiByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
            super(resourceResolver, embeddingMode);
        }

        void callAddPrivateUseMapping(int pu, int gi) {
            addPrivateUseMapping(pu, gi);
        }
    }

    @Test
    public void testConsonant() {
        Assert.assertTrue(BengaliScriptProcessor.isC(0x0995));   // KA
        Assert.assertTrue(BengaliScriptProcessor.isC(0x09B9));   // HA
        Assert.assertFalse(BengaliScriptProcessor.isC(0x0985));  // vowel A
        Assert.assertFalse(BengaliScriptProcessor.isC(0x0041));  // latin A (out of block)
    }

    @Test
    public void testVowel() {
        Assert.assertTrue(BengaliScriptProcessor.isV(0x0985));   // A
        Assert.assertTrue(BengaliScriptProcessor.isV(0x0994));   // AU
        Assert.assertFalse(BengaliScriptProcessor.isV(0x0995));  // consonant KA
    }

    @Test
    public void testMatra() {
        Assert.assertTrue(BengaliScriptProcessor.isM(0x09BE));   // AA sign
        Assert.assertTrue(BengaliScriptProcessor.isM(0x09BF));   // I sign
        Assert.assertFalse(BengaliScriptProcessor.isM(0x0995));  // consonant
    }

    @Test
    public void testPreBaseMatra() {
        // I, E and AI signs are pre-base; AA is not.
        Assert.assertTrue(BengaliScriptProcessor.isPreM(0x09BF));  // I sign
        Assert.assertTrue(BengaliScriptProcessor.isPreM(0x09C7));  // E sign
        Assert.assertTrue(BengaliScriptProcessor.isPreM(0x09C8));  // AI sign
        Assert.assertFalse(BengaliScriptProcessor.isPreM(0x09BE)); // AA sign (post-base)
    }

    @Test
    public void testReph() {
        // RA carries the reph flag; other consonants do not.
        Assert.assertTrue(BengaliScriptProcessor.isR(0x09B0));    // RA
        Assert.assertFalse(BengaliScriptProcessor.isR(0x0995));   // KA
    }

    @Test
    public void testHalantAndNukta() {
        Assert.assertTrue(BengaliScriptProcessor.isH(0x09CD));    // virama (halant)
        Assert.assertFalse(BengaliScriptProcessor.isH(0x09BC));   // nukta is not halant
        Assert.assertTrue(BengaliScriptProcessor.isN(0x09BC));    // nukta
        Assert.assertFalse(BengaliScriptProcessor.isN(0x09CD));   // halant is not nukta
    }

    @Test
    public void testNuktaizedConsonant() {
        // Precomposed nukta consonants carry the nukta flag.
        Assert.assertTrue(BengaliScriptProcessor.hasN(0x09DC));   // RRA
        Assert.assertTrue(BengaliScriptProcessor.hasN(0x09DD));   // RHA
        Assert.assertTrue(BengaliScriptProcessor.hasN(0x09DF));   // YYA
        Assert.assertFalse(BengaliScriptProcessor.hasN(0x0995));  // KA
    }

    @Test
    public void testCombiningMark() {
        // isX recognises matras and other modifying marks, but not consonants or digits.
        Assert.assertTrue(BengaliScriptProcessor.isX(0x09BE));    // AA matra
        Assert.assertTrue(BengaliScriptProcessor.isX(0x0982));    // anusvara (other mark)
        Assert.assertFalse(BengaliScriptProcessor.isX(0x0995));   // consonant
        Assert.assertFalse(BengaliScriptProcessor.isX(0x09E6));   // digit zero
    }

    @Test
    public void testTypeOfOutOfRange() {
        // Code points outside the Bengali block are unassigned.
        Assert.assertEquals(BengaliScriptProcessor.C_U, BengaliScriptProcessor.typeOf(0x0041));
        Assert.assertEquals(BengaliScriptProcessor.C_C, BengaliScriptProcessor.typeOf(0x0995));
        Assert.assertEquals(BengaliScriptProcessor.C_H, BengaliScriptProcessor.typeOf(0x09CD));
    }
}
