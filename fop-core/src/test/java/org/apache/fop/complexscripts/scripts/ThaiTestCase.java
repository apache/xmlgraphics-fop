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

import org.apache.fop.complexscripts.fonts.GlyphClassTable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.MultiByteFont;

public class ThaiTestCase {
    @Test
    public void testProcessor() {
        String in = "\u0E2A\u0E31\u0E0D\u0E0D\u0E32 \u0E40\u0E25\u0E02\u0E17\u0E35\u0E48";
        MultiByteFont font = new MultiByteFont(null, null);
        font.setWidthArray(new int[0]);
        font.setCMap(new CMapSegment[]{new CMapSegment('\u0E2A', '\u0E2A', 1)});
        GlyphSubtable table = GlyphDefinitionTable.createSubtable(1, "lu0d", 0, 0, 1,
                GlyphClassTable.createClassTable(Arrays.asList(0, GlyphDefinitionTable.GLYPH_CLASS_MARK)), null);
        font.setGDEF(
                new GlyphDefinitionTable(Collections.singletonList(table), new HashMap<String, ScriptProcessor>()));
        String actual = font.reorderCombiningMarks(in, null, OTFScript.THAI, null, null).toString();
        Assert.assertTrue(actual.endsWith("\u0E2A"));
    }
}
