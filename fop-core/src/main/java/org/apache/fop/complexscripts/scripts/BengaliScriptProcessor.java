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

import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;

// CSOFF: LineLengthCheck

/**
 * <p>The <code>BengaliScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Bengali script.</p>
 */
public class BengaliScriptProcessor extends IndicScriptProcessor {

    BengaliScriptProcessor(String script) {
        super(script);
    }

    @Override
    protected Class<? extends BengaliSyllabizer> getSyllabizerClass() {
        return BengaliSyllabizer.class;
    }

    @Override
    // find rightmost pre-base matra
    protected int findPreBaseMatra(GlyphSequence glyphs) {
        int   glyphCount = glyphs.getGlyphCount();
        int   matraIndex = -1;
        for (int i = glyphCount; i > 0; i--) {
            int glyphIndex = i - 1;
            if (containsPreBaseMatra(glyphs, glyphIndex)) {
                matraIndex = glyphIndex;
                break;
            }
        }
        return matraIndex;
    }

    @Override
    // find leftmost pre-base matra target, starting from source
    protected int findPreBaseMatraTarget(GlyphSequence glyphs, int source) {
        int   glyphCount = glyphs.getGlyphCount();
        int   targetIndex = -1;
        for (int i = (source < glyphCount) ? source : glyphCount; i > 0; i--) {
            int glyphIndex = i - 1;
            if (containsConsonant(glyphs, glyphIndex)) {
                if (containsHalfConsonant(glyphs, glyphIndex)) {
                    targetIndex = glyphIndex;
                } else if (targetIndex == -1) {
                    targetIndex = glyphIndex;
                } else {
                    break;
                }
            }
        }
        return targetIndex;
    }

    private static boolean containsPreBaseMatra(GlyphSequence glyphs, int glyphIndex) {
        CharAssociation association = glyphs.getAssociation(glyphIndex);
        int[] chars = glyphs.getCharacterArray(false);
        for (int i = association.getStart(), end = association.getEnd(); i < end; i++) {
            if (isPreM(chars[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsConsonant(GlyphSequence glyphs, int glyphIndex) {
        CharAssociation association = glyphs.getAssociation(glyphIndex);
        int[] chars = glyphs.getCharacterArray(false);
        for (int i = association.getStart(), end = association.getEnd(); i < end; i++) {
            if (isC(chars[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsHalfConsonant(GlyphSequence glyphs, int glyphIndex) {
        Boolean half = (Boolean) glyphs.getAssociation(glyphIndex).getPredication("half");
        return half != null && half;
    }

    @Override
    protected int findReph(GlyphSequence glyphs) {
        int glyphCount = glyphs.getGlyphCount();
        int rephIndex = -1;
        for (int i = 0; i < glyphCount; i++) {
            if (containsReph(glyphs, i)) {
                rephIndex = i;
                break;
            }
        }
        return rephIndex;
    }

    @Override
    protected int findRephTarget(GlyphSequence glyphs, int source) {
        int glyphCount = glyphs.getGlyphCount();
        int firstCandidate = -1;
        int secondCandidate = -1;
        // first candidate target is after first non-half consonant
        for (int i = 0; i < glyphCount; i++) {
            if ((i != source) && containsConsonant(glyphs, i) && !containsHalfConsonant(glyphs, i)) {
                firstCandidate = i + 1;
                break;
            }
        }
        // second candidate target is after last non-prebase matra after first candidate or before first syllable or vedic mark
        for (int i = (firstCandidate >= 0) ? firstCandidate : 0; i < glyphCount; i++) {
            if (containsMatra(glyphs, i) && !containsPreBaseMatra(glyphs, i)) {
                secondCandidate = i + 1;
            } else if (containsOtherMark(glyphs, i)) {
                secondCandidate = i;
                break;
            }
        }
        if (secondCandidate >= 0) {
            return secondCandidate;
        } else if (firstCandidate >= 0) {
            return firstCandidate;
        } else {
            return source;
        }
    }

    private static boolean containsReph(GlyphSequence glyphs, int glyphIndex) {
        Boolean rphf = (Boolean) glyphs.getAssociation(glyphIndex).getPredication("rphf");
        return rphf != null && rphf;
    }

    private static boolean containsMatra(GlyphSequence glyphs, int glyphIndex) {
        CharAssociation association = glyphs.getAssociation(glyphIndex);
        int[] chars = glyphs.getCharacterArray(false);
        for (int i = association.getStart(), end = association.getEnd(); i < end; i++) {
            if (isM(chars[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsOtherMark(GlyphSequence glyphs, int glyphIndex) {
        CharAssociation association = glyphs.getAssociation(glyphIndex);
        int[] chars = glyphs.getCharacterArray(false);
        for (int i = association.getStart(), end = association.getEnd(); i < end; i++) {
            switch (typeOf(chars[i])) {
            case C_T:   // tone (e.g., udatta, anudatta)
            case C_A:   // accent (e.g., acute, grave)
            case C_O:   // other (e.g., candrabindu, anusvara, visarga, etc)
                return true;
            default:
                break;
            }
        }
        return false;
    }

    private static class BengaliSyllabizer extends DefaultSyllabizer {
        BengaliSyllabizer(String script, String language) {
            super(script, language);
        }
        @Override
        // | C ...
        protected int findStartOfSyllable(int[] chars, int start, int end) {
            if ((start < 0) || (start >= end)) {
                return -1;
            } else {
                while (start < end) {
                    int codePoint = chars[start];
                    if (isC(codePoint)) {
                        break;
                    } else {
                        start++;
                    }
                }
                return start;
            }
        }
        @Override
        // D* L? | ...
        protected int findEndOfSyllable(int[] chars, int start, int end) {
            if ((start < 0) || (start >= end)) {
                return -1;
            } else {
                int deadCount = 0;
                int liveCount = 0;
                int next;
                // consume dead consonants
                while ((next = isDeadConsonant(chars, start, end)) > start) {
                    start = next;
                    deadCount++;
                }
                // consume zero or one live consonant
                next = isLiveConsonant(chars, start, end);
                if (next > start) {
                    start = next;
                    liveCount++;
                }
                return ((deadCount > 0) || (liveCount > 0)) ? start : -1;
            }
        }
        // D := ( C N? H )?
        private int isDeadConsonant(int[] chars, int start, int end) {
            if (start < 0) {
                return -1;
            }
            int i = 0;
            int consonantCount = 0;
            int halantCount = 0;
            // C
            if (((start + i) < end) && isC(chars[start + i])) {
                i++;
                consonantCount++;
            }
            // N?
            if ((consonantCount > 0) && ((start + i) < end) && isN(chars[start + i])) {
                i++;
            }
            // H
            if ((consonantCount > 0) && ((start + i) < end) && isH(chars[start + i])) {
                i++;
                halantCount++;
            }
            return ((consonantCount > 0) && (halantCount > 0)) ? start + i : -1;
        }
        // L := ( (C|V) N? X* )?; where X = ( MATRA | ACCENT MARK | TONE MARK | OTHER MARK )
        private int isLiveConsonant(int[] chars, int start, int end) {
            if (start < 0) {
                return -1;
            }
            int i = 0;
            int consonantCount = 0;
            int vowelCount = 0;
            int markCount = 0;
            // (C|V)
            if (((start + i) < end) && isC(chars[start + i])) {
                i++;
                consonantCount++;
            } else if (((start + i) < end) && isV(chars[start + i])) {
                i++;
                vowelCount++;
            }
            boolean live = (consonantCount > 0) || (vowelCount > 0);
            if (live) {
                // N?
                if (((start + i) < end) && isN(chars[start + i])) {
                    i++;
                }
                // X*
                while (((start + i) < end) && isX(chars[start + i])) {
                    i++;
                    markCount++;
                }
                // if no X but followed by H, then the (C|V) belongs to a dead consonant, not a live one
                if ((markCount == 0) && ((start + i) < end) && isH(chars[start + i])) {
                    live = false;
                }
            }
            return live ? start + i : -1;
        }
    }

    // bengali character types
    static final short C_U          = 0;            // unassigned
    static final short C_C          = 1;            // consonant
    static final short C_V          = 2;            // vowel
    static final short C_M          = 3;            // vowel sign (matra)
    static final short C_S          = 4;            // symbol or sign
    static final short C_T          = 5;            // tone mark
    static final short C_A          = 6;            // accent mark
    static final short C_P          = 7;            // punctuation
    static final short C_D          = 8;            // digit
    static final short C_H          = 9;            // halant (virama)
    static final short C_O          = 10;           // other signs
    static final short C_N          = 0x0100;       // nukta(ized)
    static final short C_R          = 0x0200;       // reph(ized)
    static final short C_PRE        = 0x0400;       // pre-base
    static final short C_M_TYPE     = 0x00FF;       // type mask
    static final short C_M_FLAGS    = 0x7F00;       // flag mask
    // bengali block range
    static final int CCA_START       =  0x0980;      // first code point mapped by cca
    static final int CCA_END         =  0x0A00;      // last code point + 1 mapped by cca
    // bengali character type lookups
    static final short[] CCA = {
        C_S,                        // 0x0980       // ANJI
        C_O,                        // 0x0981       // CANDRABINDU
        C_O,                        // 0x0982       // ANUSVARA
        C_O,                        // 0x0983       // VISARGA
        C_U,                        // 0x0984       // UNASSIGNED
        C_V,                        // 0x0985       // A
        C_V,                        // 0x0986       // AA
        C_V,                        // 0x0987       // I
        C_V,                        // 0x0988       // II
        C_V,                        // 0x0989       // U
        C_V,                        // 0x098A       // UU
        C_V,                        // 0x098B       // VOCALIC R
        C_V,                        // 0x098C       // VOCALIC L
        C_U,                        // 0x098D       // UNASSIGNED
        C_U,                        // 0x098E       // UNASSIGNED
        C_V,                        // 0x098F       // E
        C_V,                        // 0x0990       // AI
        C_U,                        // 0x0991       // UNASSIGNED
        C_U,                        // 0x0992       // UNASSIGNED
        C_V,                        // 0x0993       // O
        C_V,                        // 0x0994       // AU
        C_C,                        // 0x0995       // KA
        C_C,                        // 0x0996       // KHA
        C_C,                        // 0x0997       // GA
        C_C,                        // 0x0998       // GHA
        C_C,                        // 0x0999       // NGA
        C_C,                        // 0x099A       // CA
        C_C,                        // 0x099B       // CHA
        C_C,                        // 0x099C       // JA
        C_C,                        // 0x099D       // JHA
        C_C,                        // 0x099E       // NYA
        C_C,                        // 0x099F       // TTA
        C_C,                        // 0x09A0       // TTHA
        C_C,                        // 0x09A1       // DDA
        C_C,                        // 0x09A2       // DDHA
        C_C,                        // 0x09A3       // NNA
        C_C,                        // 0x09A4       // TA
        C_C,                        // 0x09A5       // THA
        C_C,                        // 0x09A6       // DA
        C_C,                        // 0x09A7       // DHA
        C_C,                        // 0x09A8       // NA
        C_U,                        // 0x09A9       // UNASSIGNED
        C_C,                        // 0x09AA       // PA
        C_C,                        // 0x09AB       // PHA
        C_C,                        // 0x09AC       // BA
        C_C,                        // 0x09AD       // BHA
        C_C,                        // 0x09AE       // MA
        C_C,                        // 0x09AF       // YA
        C_C | C_R,                  // 0x09B0       // RA
        C_U,                        // 0x09B1       // UNASSIGNED
        C_C,                        // 0x09B2       // LA
        C_U,                        // 0x09B3       // UNASSIGNED
        C_U,                        // 0x09B4       // UNASSIGNED
        C_U,                        // 0x09B5       // UNASSIGNED
        C_C,                        // 0x09B6       // SHA
        C_C,                        // 0x09B7       // SSA
        C_C,                        // 0x09B8       // SA
        C_C,                        // 0x09B9       // HA
        C_U,                        // 0x09BA       // UNASSIGNED
        C_U,                        // 0x09BB       // UNASSIGNED
        C_N,                        // 0x09BC       // NUKTA
        C_S,                        // 0x09BD       // AVAGRAHA
        C_M,                        // 0x09BE       // AA
        C_M | C_PRE,                // 0x09BF       // I
        C_M,                        // 0x09C0       // II
        C_M,                        // 0x09C1       // U
        C_M,                        // 0x09C2       // UU
        C_M,                        // 0x09C3       // VOCALIC R
        C_M,                        // 0x09C4       // VOCALIC RR
        C_U,                        // 0x09C5       // UNASSIGNED
        C_U,                        // 0x09C6       // UNASSIGNED
        C_M | C_PRE,                // 0x09C7       // E
        C_M | C_PRE,                // 0x09C8       // AI
        C_U,                        // 0x09C9       // UNASSIGNED
        C_U,                        // 0x09CA       // UNASSIGNED
        C_M,                        // 0x09CB       // O
        C_M,                        // 0x09CC       // AU
        C_H,                        // 0x09CD       // VIRAMA (HALANT)
        C_C,                        // 0x09CE       // KHANDA TA
        C_U,                        // 0x09CF       // UNASSIGNED
        C_U,                        // 0x09D0       // UNASSIGNED
        C_U,                        // 0x09D1       // UNASSIGNED
        C_U,                        // 0x09D2       // UNASSIGNED
        C_U,                        // 0x09D3       // UNASSIGNED
        C_U,                        // 0x09D4       // UNASSIGNED
        C_U,                        // 0x09D5       // UNASSIGNED
        C_U,                        // 0x09D6       // UNASSIGNED
        C_M,                        // 0x09D7       // AU LENGTH MARK
        C_U,                        // 0x09D8       // UNASSIGNED
        C_U,                        // 0x09D9       // UNASSIGNED
        C_U,                        // 0x09DA       // UNASSIGNED
        C_U,                        // 0x09DB       // UNASSIGNED
        C_C | C_N,                  // 0x09DC       // RRA          = 09A1+09BC
        C_C | C_N,                  // 0x09DD       // RHA          = 09A2+09BC
        C_U,                        // 0x09DE       // UNASSIGNED
        C_C | C_N,                  // 0x09DF       // YYA          = 09AF+09BC
        C_V,                        // 0x09E0       // VOCALIC RR
        C_V,                        // 0x09E1       // VOCALIC LL
        C_M,                        // 0x09E2       // VOCALIC L
        C_M,                        // 0x09E3       // VOCALIC LL
        C_U,                        // 0x09E4       // UNASSIGNED
        C_U,                        // 0x09E5       // UNASSIGNED
        C_D,                        // 0x09E6       // ZERO
        C_D,                        // 0x09E7       // ONE
        C_D,                        // 0x09E8       // TWO
        C_D,                        // 0x09E9       // THREE
        C_D,                        // 0x09EA       // FOUR
        C_D,                        // 0x09EB       // FIVE
        C_D,                        // 0x09EC       // SIX
        C_D,                        // 0x09ED       // SEVEN
        C_D,                        // 0x09EE       // EIGHT
        C_D,                        // 0x09EF       // NINE
        C_C,                        // 0x09F0       // RA WITH MIDDLE DIAGONAL
        C_C,                        // 0x09F1       // RA WITH LOWER DIAGONAL
        C_S,                        // 0x09F2       // RUPEE MARK
        C_S,                        // 0x09F3       // RUPEE SIGN
        C_S,                        // 0x09F4       // CURRENCY NUMERATOR ONE
        C_S,                        // 0x09F5       // CURRENCY NUMERATOR TWO
        C_S,                        // 0x09F6       // CURRENCY NUMERATOR THREE
        C_S,                        // 0x09F7       // CURRENCY NUMERATOR FOUR
        C_S,                        // 0x09F8       // CURRENCY NUMERATOR ONE LESS THAN THE DENOMINATOR
        C_S,                        // 0x09F9       // CURRENCY DENOMINATOR SIXTEEN
        C_S,                        // 0x09FA       // ISSHAR
        C_S,                        // 0x09FB       // GANDA MARK
        C_C,                        // 0x09FC       // VEDIC ANUSVARA
        C_P,                        // 0x09FD       // ABBREVIATION SIGN
        C_O,                        // 0x09FE       // SANDHI MARK
        C_U                         // 0x09FF       // UNASSIGNED
    };
    static int typeOf(int codePoint) {
        if ((codePoint >= CCA_START) && (codePoint < CCA_END)) {
            return CCA[codePoint - CCA_START] & C_M_TYPE;
        } else {
            return C_U;
        }
    }
    static boolean isType(int codePoint, int type) {
        return typeOf(codePoint) == type;
    }
    static boolean hasFlag(int codePoint, int flag) {
        if ((codePoint >= CCA_START) && (codePoint < CCA_END)) {
            return (CCA[codePoint - CCA_START] & flag) == flag;
        } else {
            return false;
        }
    }
    static boolean isC(int codePoint) {
        return isType(codePoint, C_C);
    }
    static boolean isR(int codePoint) {
        return isType(codePoint, C_C) && hasR(codePoint);
    }
    static boolean isV(int codePoint) {
        return isType(codePoint, C_V);
    }
    static boolean isN(int codePoint) {
        return codePoint == 0x09BC;
    }
    static boolean isH(int codePoint) {
        return codePoint == 0x09CD;
    }
    static boolean isM(int codePoint) {
        return isType(codePoint, C_M);
    }
    static boolean isPreM(int codePoint) {
        return isType(codePoint, C_M) && hasFlag(codePoint, C_PRE);
    }
    static boolean isX(int codePoint) {
        switch (typeOf(codePoint)) {
        case C_M: // matra (combining vowel)
        case C_A: // accent mark
        case C_T: // tone mark
        case C_O: // other (modifying) mark
            return true;
        default:
            return false;
        }
    }
    static boolean hasR(int codePoint) {
        return hasFlag(codePoint, C_R);
    }
    static boolean hasN(int codePoint) {
        return hasFlag(codePoint, C_N);
    }

}
