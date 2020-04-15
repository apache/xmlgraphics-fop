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

package org.apache.fop.complexscripts.fonts;

import java.io.File;
import java.nio.IntBuffer;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.fop.complexscripts.fonts.GlyphTable.LookupTable;
import org.apache.fop.complexscripts.fonts.ttx.TTXFile;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

// CSOFF: LineLength

public class GSUBTestCase implements ScriptContextTester, GlyphContextTester {

    private static String ttxFilesRoot = "test/resources/complexscripts";

    private static String[][] ttxFonts = {
        { "f0", "arab/ttx/arab-001.ttx" },              // simplified arabic
        { "f1", "arab/ttx/arab-002.ttx" },              // traditional arabic
        { "f2", "arab/ttx/arab-003.ttx" },              // lateef
        { "f3", "arab/ttx/arab-004.ttx" },              // scheherazade
    };

    @Test
    public void testGSUBSingle() throws Exception {
        performSubstitutions(GSUBData.ltSingle);
    }

    @Test
    public void testGSUBMultiple() throws Exception {
        performSubstitutions(GSUBData.ltMultiple);
    }

    @Test
    public void testGSUBAlternate() throws Exception {
        performSubstitutions(GSUBData.ltAlternate);
    }

    @Test
    public void testGSUBLigature() throws Exception {
        performSubstitutions(GSUBData.ltLigature);
    }

    @Test
    public void testGSUBContextual() throws Exception {
        performSubstitutions(GSUBData.ltContextual);
    }

    @Test
    public void testGSUBChainedContextual() throws Exception {
        performSubstitutions(GSUBData.ltChainedContextual);
    }

    /**
     * Perform substitutions on all test data in test specification TS.
     * @param ts test specification
     */
    private void performSubstitutions(Object[][] ts) {
        assert ts.length > 0;
        Object[] tp = ts[0];
        for (int i = 1; i < ts.length; i++) {
            performSubstitutions(tp, ts[i]);
        }
    }

    /**
     * Perform substitutions on all test data TD using test parameters TP.
     * @param tp test parameters
     * @param td test data
     */
    private void performSubstitutions(Object[] tp, Object[] td) {
        assert tp.length > 0;
        if (td.length > 5) {
            String fid = (String) td[0];
            String lid = (String) td[1];
            String script = (String) td[2];
            String language = (String) td[3];
            String feature = (String) td[4];
            TTXFile tf = findTTX(fid);
            assertTrue(tf != null);
            GlyphSubstitutionTable gsub = tf.getGSUB();
            assertTrue(gsub != null);
            GlyphSubstitutionSubtable[] sta = findGSUBSubtables(gsub, script, language, feature, lid);
            assertTrue(sta != null);
            assertTrue(sta.length > 0);
            ScriptContextTester sct = findScriptContextTester(script, language, feature);
            String[][][] tia = (String[][][]) td[5];            // test instance array
            for (String[][] ti : tia) {                       // test instance
                if (ti != null) {
                    if (ti.length > 1) {                      // must have at least input and output glyph id arrays
                        String[] igia = ti[0];                  // input glyph id array
                        String[] ogia = ti[1];                  // output glyph id array
                        GlyphSequence igs = tf.getGlyphSequence(igia);
                        GlyphSequence ogs = tf.getGlyphSequence(ogia);
                        GlyphSequence tgs = GlyphSubstitutionSubtable.substitute(igs, script, language, feature, sta, sct);
                        assertSameGlyphs(ogs, tgs);
                    }
                }
            }
        }
    }

    private String findTTXPath(String fid) {
        for (String[] fs : ttxFonts) {
            if ((fs != null) && (fs.length > 1)) {
                if (fs[0].equals(fid)) {
                    return ttxFilesRoot + File.separator + fs[1];
                }
            }
        }
        return null;
    }

    private TTXFile findTTX(String fid) {
        String pn = findTTXPath(fid);
        assertTrue(pn != null);
        try {
            TTXFile tf = TTXFile.getFromCache(pn);
            return tf;
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    private GlyphSubstitutionSubtable[] findGSUBSubtables(GlyphSubstitutionTable gsub, String script, String language, String feature, String lid) {
        LookupTable lt = gsub.getLookupTable(lid);
        if (lt != null) {
            return (GlyphSubstitutionSubtable[]) lt.getSubtables();
        } else {
            return null;
        }
    }

    private ScriptContextTester findScriptContextTester(String script, String language, String feature) {
        return this;
    }

    public GlyphContextTester getTester(String feature) {
        return this;
    }

    public boolean test(String script, String language, String feature, GlyphSequence gs, int index, int flags) {
        return true;
    }

    private void assertSameGlyphs(GlyphSequence gs1, GlyphSequence gs2) {
        assertNotNull(gs1);
        assertNotNull(gs2);
        IntBuffer gb1 = gs1.getGlyphs();
        IntBuffer gb2 = gs2.getGlyphs();
        assertEquals("unequal glyph count", gb1.limit(), gb2.limit());
        for (int i = 0; i < gb1.limit(); i++) {
            int g1 = gb1.get(i);
            int g2 = gb2.get(i);
            assertEquals("unequal glyph code", g1, g2);
        }
    }

    @Test
    public void testCreateClassTable() {
        GlyphCoverageTable coverageTable = GlyphCoverageTable.createCoverageTable(null);
        GlyphClassTable classTable = GlyphClassTable.createClassTable(Collections.singletonList(coverageTable));
        assertNotNull(classTable);
    }
}
