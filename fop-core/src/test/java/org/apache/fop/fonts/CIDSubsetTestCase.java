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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.util.CharUtilities;

public class CIDSubsetTestCase {

    /** The surrogate pair is expected to be in the end of the string. Change it carefully. */
    private static final String TEXT = "Hello CIDSubset \uD83D\uDCA9";

    private CIDSubset cidSub;
    private BitSet bs;
    private int[] codepoints;
    private int[] widths;
    private Map<Integer, Integer> glyphToSelector;
    private Map<Integer, Integer> charToSelector;
    private HashMap<Integer, Integer> charToGlyph;

    @Before
    public void setup() {
        bs = new BitSet();
        glyphToSelector = new HashMap<Integer, Integer>();
        charToSelector = new HashMap<Integer, Integer>();
        charToGlyph = new HashMap<Integer, Integer>();

        codepoints = new int[TEXT.length() - 1]; // skip one char because of surrogate pair
        bs.set(0); // .notdef

        int glyphIdx = 0;
        for (int i = 0; i < TEXT.length(); i++) {
            int cp = TEXT.codePointAt(i);
            i += CharUtilities.incrementIfNonBMP(cp);

            codepoints[glyphIdx] = cp;

            glyphIdx++;

            // Assign glyphIdx for each character
            // glyphIndex 0 is reserved for .notdef
            if (!charToGlyph.containsKey(cp)) {
                charToGlyph.put(cp, glyphIdx);
                bs.set(glyphIdx);
            }
        }

        // fill widths up to max glyph index + 1 for .notdef
        widths = new int[glyphIdx + 1];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 100 * i;
        }

        MultiByteFont mbFont = mock(MultiByteFont.class);
        when(mbFont.getGlyphIndices()).thenReturn(bs);
        when(mbFont.getWidths()).thenReturn(widths);
        cidSub = new CIDSubset(mbFont);

        for (int i = 0; i < codepoints.length; i++) {
            int codepoint = codepoints[i];
            int glyphIndex = charToGlyph.get(codepoint);
            int subsetCharSelector = cidSub.mapCodePoint(glyphIndex, codepoint);
            glyphToSelector.put(glyphIndex, subsetCharSelector);
            charToSelector.put(codepoint, subsetCharSelector);
        }
    }

    @Test
    public void testGetOriginalGlyphIndex() {
        // index 5 exists
        int codepoint = (int) TEXT.charAt(0);
        int subsetCharSelector = charToSelector.get(codepoint);
        int originalIdx = charToGlyph.get(codepoint);
        assertEquals(originalIdx, cidSub.getOriginalGlyphIndex(subsetCharSelector));
    }

    @Test
    public void testGetUnicode() {
        int bmpCodepoint = codepoints[5];
        int nonBmpCodepoint = codepoints[codepoints.length - 1];

        assertEquals(bmpCodepoint, cidSub.getUnicode(charToSelector.get(bmpCodepoint)));
        assertEquals(nonBmpCodepoint, cidSub.getUnicode(charToSelector.get(nonBmpCodepoint)));

        // not exist
        assertEquals(CharUtilities.NOT_A_CHARACTER, cidSub.getUnicode(-1));
    }

    @Test
    public void testMapChar() {
        for (Map.Entry<Integer, Integer> entry : glyphToSelector.entrySet()) {
            int glyphIndex = entry.getKey();
            int subsetCharSelector = entry.getValue();
            // the value of codepoint is not relevant for the purpose of this test: safe to take a random value.
            int codepoint = 'a';
            assertEquals(subsetCharSelector, cidSub.mapChar(glyphIndex, (char) codepoint));
        }
    }

    @Test
    public void testMapCodePoint() {
        for (Map.Entry<Integer, Integer> entry : glyphToSelector.entrySet()) {
            int glyphIndex = entry.getKey();
            int subsetCharSelector = entry.getValue();
            // the value of codepoint is not relevant for the purpose of this test: safe to take a random value.
            int codepoint = 'a';
            assertEquals(subsetCharSelector, cidSub.mapCodePoint(glyphIndex, codepoint));
        }
    }

    @Test
    public void testGetGlyphs() {
        Map<Integer, Integer> fontGlyphs = cidSub.getGlyphs();

        for (Integer key : fontGlyphs.keySet()) {
            if (key == 0) {
                // the entry 0 -> 0 is set in the CIDSubset constructor
                assertEquals(0, fontGlyphs.get(key).intValue());
                continue;
            }
            assertEquals(glyphToSelector.get(key), fontGlyphs.get(key));
        }

        assertEquals(glyphToSelector.size() + 1, fontGlyphs.size());
    }

    @Test
    public void testGetChars() {
        char[] chars = cidSub.getChars();
        char[] expected = TEXT.toCharArray();

        Arrays.sort(chars);
        Arrays.sort(expected);

        // checks if the returned arrays contains all the expected chars
        for (char c : expected) {
            assertTrue(Arrays.binarySearch(chars, c) >= 0);
        }

        // checks if the returned array do not contains unexpected chars
        for (char c : chars) {
            if (c == CharUtilities.NOT_A_CHARACTER) {
                continue;
            }
            assertTrue(Arrays.binarySearch(expected, c) >= 0);
        }
    }

    @Test
    public void testGetNumberOfGlyphs() {
        // +1 because of .notdef
        assertEquals(glyphToSelector.size() + 1, cidSub.getNumberOfGlyphs());
    }

    @Test
    public void testGetGlyphIndices() {
        assertEquals(bs, cidSub.getGlyphIndices());
    }

    @Test
    public void testGetWidths() {
        Arrays.sort(widths);

        for (int width : cidSub.getWidths()) {
            assertTrue(Arrays.binarySearch(widths, width) >= 0);
        }
    }
}
