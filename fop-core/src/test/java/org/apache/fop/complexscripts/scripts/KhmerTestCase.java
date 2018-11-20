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
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.complexscripts.fonts.GlyphCoverageTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.fonts.OTFLanguage;
import org.apache.fop.complexscripts.fonts.OTFScript;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.fonts.MultiByteFont;

public class KhmerTestCase {
    @Test
    public void testProcessor() {
        String in = "\u179b\u17c1\u1781\u179a\u17c0\u1784\u17b7\u179c\u17d2\u1780\u1780\u1799\u1794\u17d2\u178f\u179a";
        String out =
               "\u17c1\u179b\u1781\u17c1\u179a\u17c0\u1784\u17b7\u179c\u17d2\u1780\u1780\u1799\u1794\u17d2\u178f\u179a";
        assertEquals(
                new KhmerScriptProcessor(OTFScript.KHMER).preProcess(in, new MultiByteFont(null, null), null), out);
    }

    @Test
    public void testPositioning() {
        GlyphSubtable subtable5 = GlyphPositioningTable.createSubtable(5, "lu1", 0, 0, 1,
                GlyphCoverageTable.createCoverageTable(Collections.singletonList(0)),
                Arrays.asList(
                        GlyphCoverageTable.createCoverageTable(Collections.singletonList(0)),
                        0,
                        1,
                        new GlyphPositioningTable.MarkAnchor[] {
                                new GlyphPositioningTable.MarkAnchor(0, new GlyphPositioningTable.Anchor(0, 0))
                        },
                        new GlyphPositioningTable.Anchor[][][] {
                                new GlyphPositioningTable.Anchor[][] {
                                        new GlyphPositioningTable.Anchor[] {
                                                new GlyphPositioningTable.Anchor(12, 0)
                                        }
                                }
                        }
                ));
        Map<GlyphTable.LookupSpec, List> lookups = new HashMap<GlyphTable.LookupSpec, List>();
        lookups.put(new GlyphTable.LookupSpec(OTFScript.KHMER, OTFLanguage.DEFAULT, "abvm"),
                Collections.singletonList("lu1"));
        Map<String, ScriptProcessor> processors = new HashMap<String, ScriptProcessor>();
        processors.put(OTFScript.KHMER, new KhmerScriptProcessor(OTFScript.KHMER));
        GlyphPositioningTable gpt =
                new GlyphPositioningTable(null, lookups, Collections.singletonList(subtable5), processors);

        ScriptProcessor scriptProcessor = ScriptProcessor.getInstance(OTFScript.KHMER, processors);
        MultiByteFont multiByteFont = new MultiByteFont(null, null);
        GlyphSequence glyphSequence = multiByteFont.charSequenceToGlyphSequence("test", null);
        scriptProcessor.preProcess("test", multiByteFont, null);
        scriptProcessor.substitute(
                glyphSequence, OTFScript.KHMER, OTFLanguage.DEFAULT, new GlyphTable.UseSpec[0], null);
        int[][] adjustments = new int[4][1];
        gpt.position(glyphSequence, OTFScript.KHMER, OTFLanguage.DEFAULT, 0, null, adjustments);
        Assert.assertArrayEquals(adjustments[1], new int[]{12});
    }

    @Test
    public void testMakeProcessor() {
        Assert.assertTrue(IndicScriptProcessor.makeProcessor(OTFScript.KHMER) instanceof KhmerScriptProcessor);
        Assert.assertTrue(CharScript.isIndicScript(OTFScript.KHMER));
    }

    @Test
    public void testKhmerRenderer() {
        KhmerRenderer khmerRenderer = new KhmerRenderer();
        StringBuilder stringBuilder = new StringBuilder();
        int khmerStart = 6016;
        for (int i = khmerStart; i < khmerStart + 128; i++) {
            stringBuilder.append((char)i);
        }
        String allKhmerChars = stringBuilder.toString();
        String expected = khmerRenderer.render(allKhmerChars);
        assertEquals(expected.length(), 133);

        StringBuilder diff = new StringBuilder();
        for (int i = 0; i < allKhmerChars.length(); i++) {
            if (allKhmerChars.charAt(i) != expected.charAt(i)) {
                diff.append(expected.charAt(i));
            }
        }
        assertEquals(diff.length(), 66);
        assertEquals(diff.charAt(0), (char) 6081);
    }
}
