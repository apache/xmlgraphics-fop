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

import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

// CSOFF: LineLengthCheck

/**
 * Default script processor, which enables default glyph composition/decomposition, common ligatures, localized forms
 * and kerning.
 *
 * @author Glenn Adams
 */
public class DefaultScriptProcessor extends ScriptProcessor {

    /** features to use for substitutions */
    private static final String[] gsubFeatures =                                                                        // CSOK: ConstantNameCheck
    {
        "ccmp",                                                 // glyph composition/decomposition
        "liga",                                                 // common ligatures
        "locl"                                                  // localized forms
    };

    /** features to use for positioning */
    private static final String[] gposFeatures =                                                                        // CSOK: ConstantNameCheck
    {
        "kern",                                                 // kerning
        "mark",                                                 // mark to base or ligature positioning
        "mkmk"                                                  // mark to mark positioning
    };

    DefaultScriptProcessor ( String script ) {
        super ( script );
    }

    @Override
    /** {@inheritDoc} */
    public String[] getSubstitutionFeatures() {
        return gsubFeatures;
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getSubstitutionContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public String[] getPositioningFeatures() {
        return gposFeatures;
    }

    @Override
    /** {@inheritDoc} */
    public ScriptContextTester getPositioningContextTester() {
        return null;
    }

    @Override
    /** {@inheritDoc} */
    public GlyphSequence reorderCombiningMarks ( GlyphDefinitionTable gdef, GlyphSequence gs, int[][] gpa, String script, String language ) {
        int   ng  = gs.getGlyphCount();
        int[] ga  = gs.getGlyphArray ( false );
        int   nm  = 0;
        // count combining marks
        for ( int i = 0; i < ng; i++ ) {
            int gid = ga [ i ];
            if ( gdef.isGlyphClass ( gid, GlyphDefinitionTable.GLYPH_CLASS_MARK ) ) {
                nm++;
            }
        }
        // only reorder if there is at least one mark and at least one non-mark glyph
        if ( ( nm > 0 ) && ( ( ng - nm ) > 0 ) ) {
            GlyphSequence.CharAssociation[] aa = gs.getAssociations ( 0, -1 );
            int[] nga = new int [ ng ];
            int[][] npa = ( gpa != null ) ? new int [ ng ][] : null;
            GlyphSequence.CharAssociation[] naa = new GlyphSequence.CharAssociation [ ng ];
            int k = 0;
            GlyphSequence.CharAssociation ba = null;
            int bg = -1;
            int[] bpa = null;
            for ( int i = 0; i < ng; i++ ) {
                int gid = ga [ i ];
                int[] pa = ( gpa != null ) ? gpa [ i ] : null;
                GlyphSequence.CharAssociation ca = aa [ i ];
                if ( gdef.isGlyphClass ( gid, GlyphDefinitionTable.GLYPH_CLASS_MARK ) ) {
                    nga [ k ] = gid;
                    naa [ k ] = ca;
                    if ( npa != null ) {
                        npa [ k ] = pa;
                    }
                    k++;
                } else {
                    if ( bg != -1 ) {
                        nga [ k ] = bg;
                        naa [ k ] = ba;
                        if ( npa != null ) {
                            npa [ k ] = bpa;
                        }
                        k++;
                        bg = -1;
                        ba = null;
                        bpa = null;
                    }
                    if ( bg == -1 ) {
                        bg = gid;
                        ba = ca;
                        bpa = pa;
                    }
                }
            }
            if ( bg != -1 ) {
                nga [ k ] = bg;
                naa [ k ] = ba;
                if ( npa != null ) {
                    npa [ k ] = bpa;
                }
                k++;
            }
            assert k == ng;
            if ( npa != null ) {
                System.arraycopy ( npa, 0, gpa, 0, ng );
            }
            return new GlyphSequence ( gs, null, nga, null, null, naa, null );
        } else {
            return gs;
        }
    }

}
