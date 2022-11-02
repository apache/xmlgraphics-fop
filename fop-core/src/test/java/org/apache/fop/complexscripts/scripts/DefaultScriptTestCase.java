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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.complexscripts.fonts.GlyphClassTable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.MultiByteFont;

public class DefaultScriptTestCase {
    @Test
    public void testProcessorReorder() {
        String in = "\u00F6\u0323";
        int[][] gpa = new int[2][1];
        gpa[0][0] = 1;
        gpa[1][0] = 1;
        String actual = getFont().reorderCombiningMarks(in, gpa, OTFScript.DEFAULT, null, null).toString();
        Assert.assertEquals(actual.charAt(0), 803);
    }

    @Test
    public void testProcessorNoReorder() {
        String in = "\u00F6\u0323";
        int[][] gpa = new int[2][1];
        String actual = getFont().reorderCombiningMarks(in, gpa, OTFScript.DEFAULT, null, null).toString();
        Assert.assertEquals(actual.charAt(0), 57344);
    }

    @Test
    public void testProcessorReorder2() {
        String in = "S\u0323\u0323;";
        int[][] gpa = new int[4][1];
        gpa[2][0] = 1;
        String actual = getFont().reorderCombiningMarks(in, gpa, OTFScript.DEFAULT, null, null).toString();
        Assert.assertEquals(actual.charAt(0), 803);
        Assert.assertEquals(actual.charAt(1), 57344);
        Assert.assertEquals(actual.charAt(2), 803);
        Assert.assertEquals(actual.charAt(3), 57344);
    }

    private MultiByteFont getFont() {
        MultiByteFont font = new MultiByteFont(null, null);
        font.setWidthArray(new int[0]);
        font.setCMap(new CMapSegment[]{new CMapSegment('\u0323', '\u0323', 1)});
        List<Integer> entries = Arrays.asList(0, GlyphDefinitionTable.GLYPH_CLASS_BASE,
                GlyphDefinitionTable.GLYPH_CLASS_MARK);
        GlyphSubtable table = GlyphDefinitionTable.createSubtable(1, "lu0d", 0, 0, 1,
                GlyphClassTable.createClassTable(entries), null);
        font.setGDEF(
                new GlyphDefinitionTable(Collections.singletonList(table), new HashMap<String, ScriptProcessor>()));
        return font;
    }
}
