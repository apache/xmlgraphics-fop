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
import java.util.List;
import java.util.Map;

import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningSubtable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphTable.LookupSpec;
import org.apache.fop.complexscripts.fonts.GlyphTable.LookupTable;
import org.apache.fop.complexscripts.fonts.ttx.TTXFile;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GPOSTestCase implements ScriptContextTester, GlyphContextTester {

    private static String ttxFilesRoot = "test/resources/complexscripts";

    private static String[][] ttxFonts = {
        { "f0", "arab/ttx/arab-001.ttx" },              // simplified arabic
        { "f1", "arab/ttx/arab-002.ttx" },              // traditional arabic
        { "f2", "arab/ttx/arab-003.ttx" },              // lateef
        { "f3", "arab/ttx/arab-004.ttx" },              // scheherazade
    };

    private static Object[][] ltSingle = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_SINGLE },
        // arab-001.ttx
        { "f0", "lu1", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "fathatan" },
                    new int[][] {
                        { 0, 0, -412, 0 }
                    }
                },
                {
                    new String[] { "fatha" },
                    new int[][] {
                        { 0, 0, -410, 0 }
                    }
                },
          },
        },
        { "f0", "lu9", "arab", "*", "*",
          new Object[][] {
                {
                    new String[] { "fathatan" },
                    new int[][] {
                        { 50, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "fatha" },
                    new int[][] {
                        { 50, 0, 0, 0 }
                    }
                },
          },
        },
        { "f0", "lu10", "arab", "*", "*",
          new Object[][] {
                {
                    new String[] { "kasratan" },
                    new int[][] {
                        { 0, -200, 0, 0 }
                    }
                },
                {
                    new String[] { "kasra" },
                    new int[][] {
                        { 0, -200, 0, 0 }
                    }
                },
          },
        },
        { "f0", "lu11", "arab", "*", "*",
          new Object[][] {
                {
                    new String[] { "kasratan" },
                    new int[][] {
                        { 0, -300, 0, 0 }
                    }
                },
                {
                    new String[] { "kasra" },
                    new int[][] {
                        { 0, -300, 0, 0 }
                    }
                },
                {
                    new String[] { "uni0655" },
                    new int[][] {
                        { 0, -250, 0, 0 }
                    }
                },
          },
        },
        // arab-002.ttx - maybe add tests
        // arab-003.ttx - maybe add tests
        // arab-004.ttx - maybe add tests
    };

    private static Object[][] ltPair = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_PAIR },
        // arab-001.ttx
        { "f0", "lu0", "arab", "dflt", "kern",
          new Object[][] {
                {
                    new String[] { "wawwithhamzaabove", "hamza" },
                    new int[][] {
                        { -300, 0, -300, 0 }, { 0, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "reh", "alefwithmaddaabove" },
                    new int[][] {
                        { -500, 0, -500, 0 }, { 0, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "zain", "zain" },
                    new int[][] {
                        { -190, 0, -190, 0 }, { 0, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "waw", "uni0649.init" },
                    new int[][] {
                        { -145, 0, -145, 0 }, { 0, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "jeh", "uni06A5.init" },
                    new int[][] {
                        { -345, 0, -345, 0 }, { 0, 0, 0, 0 }
                    }
                },
          },
        },
        // arab-002.ttx - maybe add tests
        // arab-003.ttx - maybe add tests
        // arab-004.ttx - maybe add tests
    };

    private static Object[][] ltCursive = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_CURSIVE },
        // arab-001.ttx - none used
        // arab-002.ttx - none used
        // arab-003.ttx - maybe add tests
        { "f2", "lu0", "arab", "dflt", "curs",
          new Object[][] {
                {
                    new String[] { "uni0644.init.preAlef", "uni0622.fina.postLamIni" },
                    new int[][] {
                        // { 576, 0, 0, 0 }, { 0, 0, 0, 0 } - with zero widths
                        { 295, 0, 0, 0 }, { 0, 0, 0, 0 }
                    }
                },
                {
                    new String[] { "uni0644.medi.preAlef", "uni0622.fina.postLamMed" },
                    new int[][] {
                        // { 550, 0, 0, 0 }, { 0, 0, 0, 0 } - with zero widths
                        { 282, 0, 0, 0 }, { 0, 0, 0, 0 }
                    }
                },
          },
        },
        // arab-004.ttx - none used
    };

    private static Object[][] ltMarkToBase = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_BASE },
        // arab-001.ttx - maybe add tests
        // arab-002.ttx
        { "f1", "lu4", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "beh", "fatha" },
                    new int[][] {
                        // { 0, 0, 0, 0 }, { 266, -672, 0, 0 } - with zero widths
                        { 0, 0, 0, 0 }, { 266, -672, -199, 0 }
                    }
                },
                {
                    new String[] { "alefwithhamzabelow", "kasra" },
                    new int[][] {
                        // { 0, 0, 0, 0 }, { -48, 344, 0, 0 } - with zero widths
                        { 0, 0, 0, 0 }, { -48, 344, -199, 0 }
                    }
                },
          },
        },
        // arab-003.ttx - maybe add tests
        // arab-004.ttx - maybe add tests
    };

    private static Object[][] ltMarkToLigature = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE },
        // arab-001.ttx
        { "f0", "lu4", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "rayaleflam", "fatha", "fatha", "fatha", "fatha" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { 1260, -1150, 0, 0 }, { 910, -1020, 0, 0 }, { 590, -630, 0, 0 }, { 110, -720, 0, 0 }
                    }
                },
                {
                    new String[] { "rayaleflam", "kasra", "kasra", "kasra", "kasra" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { 1110 , 225, 0, 0 }, { 760, 275, 0, 0 }, { 520, 475, 0, 0 }, { 110, 425, 0, 0 }
                    }
                },
          },
        },
        // arab-002.ttx - maybe add tests
        // arab-003.ttx - maybe add tests
        // arab-004.ttx - maybe add tests
    };

    private static Object[][] ltMarkToMark = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_MARK },
        // arab-001.ttx - maybe add tests
        // arab-002.ttx - maybe add tests
        // arab-003.ttx - maybe add tests
        // arab-004.ttx
        { "f3", "lu3", "arab", "dflt", "mkmk",
          new Object[][] {
                {
                    new String[] { "uni064F", "uni064E" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { -15, 495, 0, 0 }
                    }
                },
                {
                    new String[] { "uni0651", "uni0670" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { -30, 705, 0, 0 }
                    }
                },
          },
        },
    };

    private static Object[][] ltContextual = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_CONTEXTUAL },
        // arab-001.ttx - none used
        // arab-002.ttx - none used
        // arab-003.ttx - none used
        // arab-004.ttx - none used
    };

    private static Object[][] ltChainedContextual = {
        { GlyphPositioningTable.GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL },
        // arab-001.ttx
        { "f0", "lu3", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "behmedial", "fatha", "lam" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { 50, 0, 0, 0 }, { 0, 0, 0, 0 }
                    }
                },
          },
        },
        // arab-002.ttx
        { "f1", "lu6", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "zain", "fatha", "kafinitial" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { 0, 250, 0, 0 }, { 0, 0, 0, 0 }
                    }
                },
          },
        },
        // arab-003.ttx - none used
        // arab-004.ttx
        { "f3", "lu5", "arab", "dflt", "mark",
          new Object[][] {
                {
                    new String[] { "uni064D", "uni0622.fina.postLamIni", "uni0650" },
                    new int[][] {
                        { 0, 0, 0, 0 }, { 0, 0, 0, 0 }, { 55, 424, 0, 0 }
                    }
                },
          },
        },
    };

    @Test
    public void testGPOSSingle() throws Exception {
        performPositioning ( ltSingle );
    }

    @Test
    public void testGPOSPair() throws Exception {
        performPositioning ( ltPair );
    }

    @Test
    public void testGPOSCursive() throws Exception {
        performPositioning ( ltCursive );
    }

    @Test
    public void testGPOSMarkToBase() throws Exception {
        performPositioning ( ltMarkToBase );
    }

    @Test
    public void testGPOSMarkToLigature() throws Exception {
        performPositioning ( ltMarkToLigature );
    }

    @Test
    public void testGPOSMarkToMark() throws Exception {
        performPositioning ( ltMarkToMark );
    }

    @Test
    public void testGPOSContextual() throws Exception {
        performPositioning ( ltContextual );
    }

    @Test
    public void testGPOSChainedContextual() throws Exception {
        performPositioning ( ltChainedContextual );
    }

    /**
     * Perform positioning on all test data in test specification TS.
     * @param ts test specification
     */
    private void performPositioning ( Object[][] ts ) {
        assert ts.length > 0;
        Object[] tp = ts[0];
        for ( int i = 1; i < ts.length; i++ ) {
            performPositioning ( tp, ts[i] );
        }
    }

    /**
     * Perform positioning on all test data TD using test parameters TP.
     * @param tp test parameters
     * @param td test data
     */
    private void performPositioning ( Object[] tp, Object[] td ) {
        assert tp.length > 0;
        if ( td.length > 5 ) {
            String fid = (String) td[0];
            String lid = (String) td[1];
            String script = (String) td[2];
            String language = (String) td[3];
            String feature = (String) td[4];
            TTXFile tf = findTTX ( fid );
            assertTrue ( tf != null );
            GlyphPositioningTable gpos = tf.getGPOS();
            assertTrue ( gpos != null );
            GlyphPositioningSubtable[] sta = findGPOSSubtables ( gpos, script, language, feature, lid );
            assertTrue ( sta != null );
            assertTrue ( sta.length > 0 );
            ScriptContextTester sct = findScriptContextTester ( script, language, feature );
            Object[][] tia = (Object[][]) td[5];                // test instance array
            for ( Object[] ti : tia ) {                         // test instance
                if ( ti != null ) {
                    if ( ti.length > 0 ) {                      // must have at least input glyphs
                        String[] igia = (String[]) ti[0];       // input glyph id array
                        int[][] ogpa = (int[][]) ti[1];         // output glyph positioning array
                        GlyphSequence igs = tf.getGlyphSequence ( igia );
                        int[] widths = tf.getWidths();
                        int[][] tgpa = new int [ igia.length ] [ 4 ];
                        boolean adjusted = GlyphPositioningSubtable.position ( igs, script, language, feature, 1000, sta, widths, tgpa, sct );
                        assertTrue ( adjusted );
                        assertSamePositions ( ogpa, tgpa );
                    }
                }
            }
        }
    }

    private String findTTXPath ( String fid ) {
        for ( String[] fs : ttxFonts ) {
            if ( ( fs != null ) && ( fs.length > 1 ) ) {
                if ( fs[0].equals ( fid ) ) {
                    return ttxFilesRoot + File.separator + fs[1];
                }
            }
        }
        return null;
    }

    private TTXFile findTTX ( String fid ) {
        String pn = findTTXPath ( fid );
        assertTrue ( pn != null );
        try { 
            TTXFile tf = TTXFile.getFromCache ( pn );
            return tf;
        } catch ( Exception e ) {
            fail ( e.getMessage() );
            return null;
        }
    }

    private GlyphPositioningSubtable[] findGPOSSubtables ( GlyphPositioningTable gpos, String script, String language, String feature, String lid ) {
        LookupTable lt = gpos.getLookupTable ( lid );
        if ( lt != null ) {
            return (GlyphPositioningSubtable[]) lt.getSubtables();
        } else {
            return null;
        }
    }

    private ScriptContextTester findScriptContextTester ( String script, String language, String feature ) {
        return this;
    }

    public GlyphContextTester getTester ( String feature ) {
        return this;
    }

    public boolean test ( String script, String language, String feature, GlyphSequence gs, int index, int flags ) {
        return true;
    }

    private void assertSamePositions ( int[][] pa1, int[][] pa2 ) {
        assertNotNull ( pa1 );
        assertNotNull ( pa2 );
        assertEquals ( "unequal adjustment count", pa1.length, pa2.length );
        for ( int i = 0; i < pa1.length; i++ ) {
            int[] a1 = pa1 [ i ];
            int[] a2 = pa2 [ i ];
            assertNotNull ( a1 );
            assertNotNull ( a2 );
            assertEquals ( "bad adjustment array length", 4, a1.length );
            assertEquals ( "bad adjustment array length", 4, a2.length );
            for ( int k = 0; k < a1.length; k++ ) {
                int p1 = a1[k];
                int p2 = a2[k];
                assertEquals ( "bad adjustment", p1, p2 );
            }
        }
    }
}
