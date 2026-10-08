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

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;

/**
 * Exercises the Bengali specific reordering heuristics (pre-base matra and reph
 * placement) and the Bengali syllabizer that drive {@link BengaliScriptProcessor}.
 */
public class BengaliScriptProcessorTestCase {

    // bengali code points used by the fixtures
    private static final int KA     = 0x0995;   // consonant
    private static final int KHA    = 0x0996;   // consonant
    private static final int RA     = 0x09B0;   // consonant carrying reph flag
    private static final int A      = 0x0985;   // independent vowel
    private static final int VIRAMA = 0x09CD;   // halant
    private static final int NUKTA  = 0x09BC;   // nukta
    private static final int AA     = 0x09BE;   // post-base matra
    private static final int ISIGN  = 0x09BF;   // pre-base matra
    private static final int ANUSVARA = 0x0982; // other (modifying) mark

    private final BengaliScriptProcessor bengali =
            (BengaliScriptProcessor) IndicScriptProcessor.makeProcessor(OTFScript.BENGALI_V2);

    private static GlyphSequence seq(int... chars) {
        int[] glyphs = new int[chars.length];
        List<CharAssociation> associations = new ArrayList<CharAssociation>();
        for (int i = 0; i < chars.length; i++) {
            glyphs[i] = i + 1;
            associations.add(new CharAssociation(i, 1));
        }
        return new GlyphSequence(IntBuffer.wrap(chars), IntBuffer.wrap(glyphs), associations, true);
    }

    @Test
    public void testFindPreBaseMatra() {
        // rightmost pre-base matra is located
        Assert.assertEquals(2, bengali.findPreBaseMatra(seq(KA, KHA, ISIGN)));
        // no pre-base matra present
        Assert.assertEquals(-1, bengali.findPreBaseMatra(seq(KA, AA)));
    }

    @Test
    public void testFindPreBaseMatraTarget() {
        // scanning right-to-left from the matra, the first (rightmost) non-half consonant
        // is taken as the target, and the preceding consonant terminates the search
        Assert.assertEquals(1, bengali.findPreBaseMatraTarget(seq(KA, KHA, ISIGN), 2));
    }

    @Test
    public void testFindPreBaseMatraTargetHalfConsonant() {
        // a half consonant is always preferred as the target
        GlyphSequence glyphs = seq(KA, KHA, ISIGN);
        glyphs.getAssociation(0).setPredication("half", Boolean.TRUE);
        Assert.assertEquals(0, bengali.findPreBaseMatraTarget(glyphs, 2));
    }

    @Test
    public void testFindPreBaseMatraTargetNoConsonant() {
        // nothing precedes the matra, so there is no target
        Assert.assertEquals(-1, bengali.findPreBaseMatraTarget(seq(ISIGN), 0));
    }

    @Test
    public void testFindReph() {
        GlyphSequence glyphs = seq(RA, KA);
        Assert.assertEquals(-1, bengali.findReph(glyphs));
        glyphs.getAssociation(0).setPredication("rphf", Boolean.TRUE);
        Assert.assertEquals(0, bengali.findReph(glyphs));
    }

    @Test
    public void testFindRephTargetMatraAndOtherMark() {
        // target advances past post-base matra and stops at the following vedic/other mark
        GlyphSequence glyphs = seq(RA, KA, AA, ANUSVARA);
        Assert.assertEquals(3, bengali.findRephTarget(glyphs, 0));
    }

    @Test
    public void testFindRephTargetPreBaseMatraIgnored() {
        // a pre-base matra does not advance the reph target
        GlyphSequence glyphs = seq(RA, KA, ISIGN);
        Assert.assertEquals(2, bengali.findRephTarget(glyphs, 0));
    }

    @Test
    public void testFindRephTargetConsonantOnly() {
        // with no matra or other mark the target is just after the first consonant
        GlyphSequence glyphs = seq(RA, KA);
        Assert.assertEquals(2, bengali.findRephTarget(glyphs, 0));
    }

    @Test
    public void testFindRephTargetFallsBackToSource() {
        // only half consonants follow, so neither candidate target is found
        GlyphSequence glyphs = seq(RA, KA);
        glyphs.getAssociation(1).setPredication("half", Boolean.TRUE);
        Assert.assertEquals(0, bengali.findRephTarget(glyphs, 0));
    }

    private String substitute(int... chars) {
        GlyphSequence glyphs = seq(chars);
        GlyphSequence result = bengali.substitute(glyphs, OTFScript.BENGALI_V2, "dflt",
                new GlyphTable.UseSpec[0], null);
        return result.getCharacters().toString();
    }

    @Test
    public void testSyllabizeDeadAndLiveConsonant() {
        // conjunct (dead consonant) followed by a live consonant and a matra
        Assert.assertNotNull(substitute(KA, VIRAMA, KA, AA));
    }

    @Test
    public void testSyllabizeLeadingNonConsonant() {
        // a leading independent vowel forms a non-syllable segment before the syllable
        Assert.assertNotNull(substitute(A, KA));
    }

    @Test
    public void testSyllabizeLiveVowel() {
        // live position occupied by an independent vowel
        Assert.assertNotNull(substitute(KA, VIRAMA, A));
    }

    @Test
    public void testSyllabizeNukta() {
        // nukta between a consonant and a matra
        Assert.assertNotNull(substitute(KA, NUKTA, AA));
    }

    @Test
    public void testSyllabizePreBaseMatraReorder() {
        // pre-base matra following a consonant is reordered ahead of the base
        Assert.assertNotNull(substitute(KA, ISIGN));
    }

    @Test
    public void testSyllabizeNoConsonant() {
        // input containing no consonant produces a single non-syllable segment
        Assert.assertNotNull(substitute(A, A));
    }

    @Test
    public void testSyllabizeEmpty() {
        Assert.assertNotNull(substitute());
    }
}
