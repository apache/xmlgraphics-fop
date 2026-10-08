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

public class HindiTestCase {
    @Test
    public void testProcessor() {
        // A base glyph (0x0E2A maps to glyph 1, a non-mark) followed by a combining
        // mark glyph (0x0939 maps to glyph 0, classified as a mark) that carries a
        // horizontal positioning adjustment. The adjusted mark must be reordered ahead
        // of the base, leaving the base (0x0E2A) as the final glyph.
        String input = "\u0E2A\u0939";
        MyMultiByteFont font = new MyMultiByteFont(null, null);
        font.setWidthArray(new int[0]);
        font.callAddPrivateUseMapping('\u0E2A', 1);
        GlyphSubtable table = GlyphDefinitionTable.createSubtable(1, "lu0d", 0, 0, 1,
                GlyphClassTable.createClassTable(Arrays.asList(0, GlyphDefinitionTable.GLYPH_CLASS_MARK)), null);
        font.setGDEF(new GlyphDefinitionTable(Collections.singletonList(table),
                new HashMap<String, ScriptProcessor>()));
        int[][] gpa = new int[][] {{0, 0, 0, 0}, {5, 0, 0, 0}};
        String actual = font.reorderCombiningMarks(input, gpa, OTFScript.DEVANAGARI_V2, null, null).toString();
        Assert.assertTrue(actual.endsWith("\u0E2A"));
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
