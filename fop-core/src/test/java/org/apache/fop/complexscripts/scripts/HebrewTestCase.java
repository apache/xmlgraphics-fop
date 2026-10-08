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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.complexscripts.fonts.GlyphClassTable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.MultiByteFont;

public class HebrewTestCase {

    private static final char ALEF  = 'א'; // hebrew letter (base)
    private static final char SHEVA = 'ְ'; // hebrew point (combining mark)

    @Test
    public void testProcessorIsHebrew() {
        ScriptProcessor processor = ScriptProcessor.getInstance(OTFScript.HEBREW,
                new HashMap<String, ScriptProcessor>());
        Assert.assertTrue(processor instanceof HebrewScriptProcessor);
    }

    @Test
    public void testReorderOnNonZeroWidth() {
        // The combining mark (glyph index 1) carries a non-zero advance width, so the
        // Hebrew processor reorders it ahead of the base, leaving the base alef last.
        String actual = reorder(new int[] {0, 10}).toString();
        Assert.assertTrue(actual.endsWith(String.valueOf(ALEF)));
    }

    @Test
    public void testNoReorderOnZeroWidth() {
        // With a zero advance width the mark is not treated as reorderable, so the
        // sequence is returned unchanged (base alef first).
        String actual = reorder(new int[] {0, 0}).toString();
        Assert.assertTrue(actual.startsWith(String.valueOf(ALEF)));
    }

    private CharSequence reorder(int[] widths) {
        String input = String.valueOf(ALEF) + SHEVA;
        MyMultiByteFont font = new MyMultiByteFont(null, null);
        font.setWidthArray(widths);
        font.callAddPrivateUseMapping(ALEF, 2);
        font.callAddPrivateUseMapping(SHEVA, 1);
        // glyph 1 (sheva) is a mark, glyph 2 (alef) is a base; first list entry is the first glyph id
        GlyphSubtable table = GlyphDefinitionTable.createSubtable(1, "lu0d", 0, 0, 1,
                GlyphClassTable.createClassTable(Arrays.asList(1, GlyphDefinitionTable.GLYPH_CLASS_MARK)), null);
        font.setGDEF(new GlyphDefinitionTable(Collections.singletonList(table),
                new HashMap<String, ScriptProcessor>()));
        return font.reorderCombiningMarks(input, null, OTFScript.HEBREW, null, null);
    }

    static class MyMultiByteFont extends MultiByteFont {
        MyMultiByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
            super(resourceResolver, embeddingMode);
        }

        void callAddPrivateUseMapping(int pu, int gi) {
            addPrivateUseMapping(pu, gi);
        }
    }
}
