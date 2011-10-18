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

package org.apache.fop.complexscripts.arabic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.complexscripts.util.TTXFile;
import org.apache.fop.fonts.GlyphPositioningTable;
import org.apache.fop.fonts.GlyphSequence;
import org.apache.fop.fonts.GlyphSubstitutionTable;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for functionality related to the arabic script.
 */
public class ArabicTestCase implements ArabicTestConstants {

    @Test
    public void testArabicWordForms() {
        for ( String sfn : srcFiles ) {
            try {
                processWordForms ( new File ( datFilesDir ) );
            } catch ( Exception e ) {
                fail ( e.getMessage() );
            }
        }
    }

    private void processWordForms ( File dfd ) {
        String[] files = listWordFormFiles ( dfd );
        for ( String fn : files ) {
            File dff = new File ( dfd, fn );
            processWordForms ( dff.getAbsolutePath() );
        }
    }

    private String[] listWordFormFiles ( File dfd ) {
        return dfd.list ( new FilenameFilter() {
                public boolean accept ( File f, String name ) {
                    return hasPrefixFrom ( name, srcFiles ) && hasExtension ( name, WF_FILE_DAT_EXT );
                }
                private boolean hasPrefixFrom ( String name, String[] prefixes ) {
                    for ( String p : prefixes ) {
                        if ( name.startsWith ( p ) ) {
                            return true;
                        }
                    }
                    return false;
                }
                private boolean hasExtension ( String name, String extension ) {
                    return name.endsWith ( "." + extension );
                }
            } );
    }

    private void processWordForms ( String dpn ) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream ( dpn );
            if ( fis != null ) {
                ObjectInputStream ois = new ObjectInputStream ( fis );
                List<Object[]> data = (List<Object[]>) ois.readObject();
                if ( data != null ) {
                    processWordForms ( data );
                }
                ois.close();
            }
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException ( e.getMessage(), e );
        } catch ( IOException e ) {
            throw new RuntimeException ( e.getMessage(), e );
        } catch ( Exception e ) {
            throw new RuntimeException ( e.getMessage(), e );
        } finally {
            if ( fis != null ) {
                try { fis.close(); } catch ( Exception e ) {}
            }
        }
    }

    private void processWordForms ( List<Object[]> data ) {
        assert data != null;
        assert data.size() > 0;
        String script = null;
        String language = null;
        String tfn = null;
        TTXFile tf = null;
        GlyphSubstitutionTable gsub = null;
        GlyphPositioningTable gpos = null;
        int[] widths = null;
        for ( Object[] d : data ) {
            if ( script == null ) {
                assert d.length >= 4;
                script = (String) d[0];
                language = (String) d[1];
                tfn = (String) d[3];
                tf = TTXFile.getFromCache ( ttxFontsDir + File.separator + tfn );
                assertTrue ( tf != null );
                gsub = tf.getGSUB();
                assertTrue ( gsub != null );
                gpos = tf.getGPOS();
                assertTrue ( gpos != null );
                widths = tf.getWidths();
                assertTrue ( widths != null );
            } else {
                assert tf != null;
                assert gsub != null;
                assert gpos != null;
                assert tfn != null;
                assert d.length >= 4;
                String wf = (String) d[0];
                int[] iga = (int[]) d[1];
                int[] oga = (int[]) d[2];
                int[][] paa = (int[][]) d[3];
                GlyphSequence tigs = tf.mapCharsToGlyphs ( wf );
                assertSameGlyphs ( iga, getGlyphs ( tigs ), "input glyphs", wf, tfn );
                GlyphSequence togs = gsub.substitute ( tigs, script, language );
                assertSameGlyphs ( oga, getGlyphs ( togs ), "output glyphs", wf, tfn );
                int[][] tpaa = new int [ togs.getGlyphCount() ] [ 4 ];
                if ( gpos.position ( togs, script, language, 1000, widths, tpaa ) ) {
                    assertSameAdjustments ( paa, tpaa, wf, tfn );
                } else if ( paa != null ) {
                    assertEquals ( "unequal adjustment count, word form(" + wf + "), font (" + tfn + ")", paa.length, 0 );
                }
            }
        }
    }

    private void assertSameGlyphs ( int[] expected, int[] actual, String label, String wf, String tfn ) {
        assertEquals ( label + ": unequal glyph count, word form(" + wf + "), font (" + tfn + ")", expected.length, actual.length );
        for ( int i = 0, n = expected.length; i < n; i++ ) {
            int e = expected[i];
            int a = actual[i];
            assertEquals ( label + ": unequal glyphs[" + i + "], word form(" + wf + "), font (" + tfn + ")", e, a );
        }
    }

    private void assertSameAdjustments ( int[][] expected, int[][] actual, String wf, String tfn  ) {
        assertEquals ( "unequal adjustment count, word form(" + wf + "), font (" + tfn + ")", expected.length, actual.length );
        for ( int i = 0, n = expected.length; i < n; i++ ) {
            int[] ea = expected[i];
            int[] aa = actual[i];
            assertEquals ( "bad adjustments length, word form(" + wf + "), font (" + tfn + ")", ea.length, aa.length );
            for ( int k = 0; k < 4; k++ ) {
                int e = ea[k];
                int a = aa[k];
                assertEquals ( "unequal adjustment[" + i + "][" + k + "], word form(" + wf + "), font (" + tfn + ")", e, a );
            }
        }
    }

    private static int[] getGlyphs ( GlyphSequence gs ) {
        IntBuffer gb = gs.getGlyphs();
        int[] ga = new int [ gb.limit() ];
        gb.rewind();
        gb.get ( ga );
        return ga;
    }

}
