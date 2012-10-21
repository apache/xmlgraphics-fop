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
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.util.CharUtilities;

public class CIDFullTestCase {

    private CIDFull cidFull;
    private MultiByteFont mbFont;
    private BitSet bs;
    private char[] chars;
    private int[] widths;
    private Map<Integer, Integer> glyphs;

    @Before
    public void setup() {
        bs = new BitSet();
        glyphs = new HashMap<Integer, Integer>();
        chars = new char[18];
        widths = new int[18];
        int i = 0;
        for (int j = 0; j < 20; j++) {
            if (j == 10 || j == 11) {
                continue;
            }
            bs.set(j);
            glyphs.put(Integer.valueOf(j), Integer.valueOf(j));
            chars[i] = (char) j;
            widths[i] = 100;
            i++;
        }
        mbFont = mock(MultiByteFont.class);
        when(mbFont.getGlyphIndices()).thenReturn(bs);
        when(mbFont.getChars()).thenReturn(chars);
        when(mbFont.getWidths()).thenReturn(widths);
        cidFull = new CIDFull(mbFont);
    }

    @Test
    public void testGetOriginalGlyphIndex() {
        // index 5 exists
        assertEquals(cidFull.getOriginalGlyphIndex(5), 5);
    }

    @Test
    public void testGetUnicode() {
        // index 9 exists
        assertEquals(cidFull.getUnicode(9), (char) 9);
        // index 10 does not
        assertEquals(cidFull.getUnicode(10), CharUtilities.NOT_A_CHARACTER);
    }

    @Test
    public void testMapChar() {
        // index 9 exists
        char c = 'a';
        assertEquals(cidFull.mapChar(9, c), (char) 9);
    }

    @Test
    public void testGetGlyphs() {
        Map<Integer, Integer> fontGlyphs = cidFull.getGlyphs();
        for (Integer key : fontGlyphs.keySet()) {
            assertEquals(fontGlyphs.get(key), glyphs.get(key));
        }
        assertTrue(fontGlyphs.size() == glyphs.size());
    }

    @Test
    public void testGetChars() {
        assertArrayEquals(cidFull.getChars(), chars);
    }

    @Test
    public void testGetNumberOfGlyphs() {
        assertTrue(cidFull.getNumberOfGlyphs() == 20);
    }

    @Test
    public void testGetGlyphIndices() {
        assertEquals(bs, cidFull.getGlyphIndices());
    }

    @Test
    public void testGetWidths() {
        assertArrayEquals(cidFull.getWidths(), widths);
    }

}
