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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.fop.util.CharUtilities;

/**
 * Provides methods to get font information.
 * Naming:
 * glyph index: original index of the glyph in the non-subset font (!= unicode index)
 * character selector: index into a set of glyphs. For subset CID fonts, this starts at 0. For non-subset
 * fonts, this is the same as the glyph index.
 * Unicode index: The Unicode codepoint of a character.
 * Glyph name: the Adobe glyph name (as found in Glyphs.java)
 */
public class CIDSubset implements CIDSet {

    /**
     * usedGlyphs contains orginal, new glyph index (glyph index -> char selector)
     */
    private Map<Integer, Integer> usedGlyphs = new HashMap<Integer, Integer>();

    /**
     * usedGlyphsIndex contains new glyph, original index (char selector -> glyph index)
     */
    private Map<Integer, Integer> usedGlyphsIndex = new HashMap<Integer, Integer>();
    private int usedGlyphsCount;

    /**
     * usedCharsIndex contains new glyph, original char (char selector -> Unicode)
     */
    private Map<Integer, Character> usedCharsIndex = new HashMap<Integer, Character>();

    /**
     * A map between the original character and it's GID in the original font.
     */
    private Map<Character, Integer> charToGIDs = new HashMap<Character, Integer>();


    private final MultiByteFont font;

    public CIDSubset(MultiByteFont mbf) {
        font = mbf;
        // The zeroth value is reserved for .notdef
        usedGlyphs.put(0, 0);
        usedGlyphsIndex.put(0, 0);
        usedGlyphsCount++;
    }

    /** {@inheritDoc} */
    public int getOriginalGlyphIndex(int index) {
        Integer glyphIndex = usedGlyphsIndex.get(index);
        if (glyphIndex != null) {
            return glyphIndex;
        } else {
            return -1;
        }
    }

    /** {@inheritDoc} */
    public char getUnicode(int index) {
        Character mapValue = usedCharsIndex.get(index);
        if (mapValue != null) {
            return mapValue.charValue();
        } else {
            return CharUtilities.NOT_A_CHARACTER;
        }
    }

    /** {@inheritDoc} */
    public int mapChar(int glyphIndex, char unicode) {
        // Reencode to a new subset font or get the reencoded value
        // IOW, accumulate the accessed characters and build a character map for them
        Integer subsetCharSelector = usedGlyphs.get(glyphIndex);
        if (subsetCharSelector == null) {
            int selector = usedGlyphsCount;
            usedGlyphs.put(glyphIndex, selector);
            usedGlyphsIndex.put(selector, glyphIndex);
            usedCharsIndex.put(selector, unicode);
            charToGIDs.put(unicode, glyphIndex);
            usedGlyphsCount++;
            return selector;
        } else {
            return subsetCharSelector;
        }
    }

    /** {@inheritDoc} */
    public Map<Integer, Integer> getGlyphs() {
        return Collections.unmodifiableMap(this.usedGlyphs);
    }

    /** {@inheritDoc} */
    public char getUnicodeFromGID(int glyphIndex) {
        int selector = usedGlyphs.get(glyphIndex);
        return usedCharsIndex.get(selector);
    }

    /** {@inheritDoc} */
    public int getGIDFromChar(char ch) {
        return charToGIDs.get(ch);
    }

    /** {@inheritDoc} */
    public char[] getChars() {
        char[] charArray = new char[usedGlyphsCount];
        for (int i = 0; i < usedGlyphsCount; i++) {
            charArray[i] = getUnicode(i);
        }
        return charArray;
    }

    /** {@inheritDoc} */
    public int getNumberOfGlyphs() {
        return this.usedGlyphsCount;
    }

    /** {@inheritDoc} */
    public BitSet getGlyphIndices() {
        BitSet bitset = new BitSet();
        for (Integer cid : usedGlyphs.keySet()) {
            bitset.set(cid);
        }
        return bitset;
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] widths = font.getWidths();
        int[] tmpWidth = new int[getNumberOfGlyphs()];
        for (int i = 0, c = getNumberOfGlyphs(); i < c; i++) {
            int nwx = Math.max(0, getOriginalGlyphIndex(i));
            tmpWidth[i] = widths[nwx];
        }
        return tmpWidth;
    }

}
