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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.text.bidi.BidiClassUtils;
import org.apache.fop.util.BidiConstants;

// CSOFF: AvoidNestedBlocksCheck
// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck
// CSOFF: LineLengthCheck

/**
 * <p>The <code>ArabicScriptProcessor</code> class implements a script processor for
 * performing glyph substitution and positioning operations on content associated with the Arabic script.</p>
 * @author Glenn Adams
 */
public class ArabicScriptProcessor extends DefaultScriptProcessor {

    /** logging instance */
    private static final Log log = LogFactory.getLog(ArabicScriptProcessor.class);                                      // CSOK: ConstantNameCheck

    /** features to use for substitutions */
    private static final String[] gsubFeatures =                                                                        // CSOK: ConstantNameCheck
    {
        "calt",                                                 // contextual alternates
        "ccmp",                                                 // glyph composition/decomposition
        "fina",                                                 // final (terminal) forms
        "init",                                                 // initial forms
        "isol",                                                 // isolated formas
        "liga",                                                 // standard ligatures
        "medi",                                                 // medial forms
        "rlig"                                                  // required ligatures
    };

    /** features to use for positioning */
    private static final String[] gposFeatures =                                                                        // CSOK: ConstantNameCheck
    {
        "curs",                                                 // cursive positioning
        "kern",                                                 // kerning
        "mark",                                                 // mark to base or ligature positioning
        "mkmk"                                                  // mark to mark positioning
    };

    private static class SubstitutionScriptContextTester implements ScriptContextTester {
        private static Map/*<String,GlyphContextTester>*/ testerMap = new HashMap/*<String,GlyphContextTester>*/();
        static {
            testerMap.put ( "fina", new GlyphContextTester() { public boolean test ( GlyphSequence gs, int index ) { return inFinalContext ( gs, index ); } } );
            testerMap.put ( "init", new GlyphContextTester() { public boolean test ( GlyphSequence gs, int index ) { return inInitialContext ( gs, index ); } } );
            testerMap.put ( "isol", new GlyphContextTester() { public boolean test ( GlyphSequence gs, int index ) { return inIsolateContext ( gs, index ); } } );
            testerMap.put ( "medi", new GlyphContextTester() { public boolean test ( GlyphSequence gs, int index ) { return inMedialContext ( gs, index ); } } );
            testerMap.put ( "liga", new GlyphContextTester() { public boolean test ( GlyphSequence gs, int index ) { return inLigatureContext ( gs, index ); } } );
        }
        public GlyphContextTester getTester ( String feature ) {
            return (GlyphContextTester) testerMap.get ( feature );
        }
    }

    private static class PositioningScriptContextTester implements ScriptContextTester {
        private static Map/*<String,GlyphContextTester>*/ testerMap = new HashMap/*<String,GlyphContextTester>*/();
        public GlyphContextTester getTester ( String feature ) {
            return (GlyphContextTester) testerMap.get ( feature );
        }
    }

    private final ScriptContextTester subContextTester;
    private final ScriptContextTester posContextTester;

    ArabicScriptProcessor ( String script ) {
        super ( script );
        this.subContextTester = new SubstitutionScriptContextTester();
        this.posContextTester = new PositioningScriptContextTester();
    }

    /** {@inheritDoc} */
    public String[] getSubstitutionFeatures() {
        return gsubFeatures;
    }

    /** {@inheritDoc} */
    public ScriptContextTester getSubstitutionContextTester() {
        return subContextTester;
    }

    /** {@inheritDoc} */
    public String[] getPositioningFeatures() {
        return gposFeatures;
    }

    /** {@inheritDoc} */
    public ScriptContextTester getPositioningContextTester() {
        return posContextTester;
    }

    private static boolean inFinalContext ( GlyphSequence gs, int index ) {
        GlyphSequence.CharAssociation a = gs.getAssociation ( index );
        int[] ca = gs.getCharacterArray ( false );
        int   nc = gs.getCharacterCount();
        if ( nc == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasFinalPrecedingContext ( ca, nc, s, e ) ) {
                return false;
            } else if ( forcesFinalThisContext ( ca, nc, s, e ) ) {
                return true;
            } else if ( ! hasFinalFollowingContext ( ca, nc, s, e ) ) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean inMedialContext ( GlyphSequence gs, int index ) {
        GlyphSequence.CharAssociation a = gs.getAssociation ( index );
        int[] ca = gs.getCharacterArray ( false );
        int   nc = gs.getCharacterCount();
        if ( nc == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasMedialPrecedingContext ( ca, nc, s, e ) ) {
                return false;
            } else if ( ! hasMedialThisContext ( ca, nc, s, e ) ) {
                return false;
            } else if ( ! hasMedialFollowingContext ( ca, nc, s, e ) ) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean inInitialContext ( GlyphSequence gs, int index ) {
        GlyphSequence.CharAssociation a = gs.getAssociation ( index );
        int[] ca = gs.getCharacterArray ( false );
        int   nc = gs.getCharacterCount();
        if ( nc == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasInitialPrecedingContext ( ca, nc, s, e ) ) {
                return false;
            } else if ( ! hasInitialFollowingContext ( ca, nc, s, e ) ) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean inIsolateContext ( GlyphSequence gs, int index ) {
        GlyphSequence.CharAssociation a = gs.getAssociation ( index );
        int   nc = gs.getCharacterCount();
        if ( nc == 0 ) {
            return false;
        } else if ( ( a.getStart() == 0 ) && ( a.getEnd() == nc ) ) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean inLigatureContext ( GlyphSequence gs, int index ) {
        GlyphSequence.CharAssociation a = gs.getAssociation ( index );
        int[] ca = gs.getCharacterArray ( false );
        int   nc = gs.getCharacterCount();
        if ( nc == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasLigaturePrecedingContext ( ca, nc, s, e ) ) {
                return false;
            } else if ( ! hasLigatureFollowingContext ( ca, nc, s, e ) ) {
                return false;
            } else {
                return true;
            }
        }
    }

    private static boolean hasFinalPrecedingContext ( int[] ca, int nc, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            int k = i - 1;
            if ( ( k >= 0 ) && ( k < nc ) ) {
                chp = ca [ k ];
                clp = BidiClassUtils.getBidiClass ( chp );
                if ( clp != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( clp != BidiConstants.AL ) {
            return false;
        } else if ( hasIsolateInitial ( chp ) ) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean forcesFinalThisContext ( int[] ca, int nc, int s, int e ) {
        int chl = 0;
        int cll = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            int k = n - i - 1;
            int j = s + k;
            if ( ( j >= 0 ) && ( j < nc ) ) {
                chl = ca [ j ];
                cll = BidiClassUtils.getBidiClass ( chl );
                if ( cll != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( cll != BidiConstants.AL ) {
            return false;
        }
        if ( hasIsolateInitial ( chl ) ) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean hasFinalFollowingContext ( int[] ca, int nc, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = nc; i < n; i++ ) {
            chf = ca [ i ];
            clf = BidiClassUtils.getBidiClass ( chf );
            if ( clf != BidiConstants.NSM ) {
                break;
            }
        }
        if ( clf != BidiConstants.AL ) {
            return true;
        } else if ( hasIsolateFinal ( chf ) ) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean hasInitialPrecedingContext ( int[] ca, int nc, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            int k = i - 1;
            if ( ( k >= 0 ) && ( k < nc ) ) {
                chp = ca [ k ];
                clp = BidiClassUtils.getBidiClass ( chp );
                if ( clp != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( clp != BidiConstants.AL ) {
            return true;
        } else if ( hasIsolateInitial ( chp ) ) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean hasInitialFollowingContext ( int[] ca, int nc, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = nc; i < n; i++ ) {
            chf = ca [ i ];
            clf = BidiClassUtils.getBidiClass ( chf );
            if ( clf != BidiConstants.NSM ) {
                break;
            }
        }
        if ( clf != BidiConstants.AL ) {
            return false;
        } else if ( hasIsolateFinal ( chf ) ) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasMedialPrecedingContext ( int[] ca, int nc, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            int k = i - 1;
            if ( ( k >= 0 ) && ( k < nc ) ) {
                chp = ca [ k ];
                clp = BidiClassUtils.getBidiClass ( chp );
                if ( clp != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( clp != BidiConstants.AL ) {
            return false;
        } else if ( hasIsolateInitial ( chp ) ) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasMedialThisContext ( int[] ca, int nc, int s, int e ) {
        int chf = 0;    // first non-NSM char in [s,e)
        int clf = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            int k = s + i;
            if ( ( k >= 0 ) && ( k < nc ) ) {
                chf = ca [ s + i ];
                clf = BidiClassUtils.getBidiClass ( chf );
                if ( clf != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( clf != BidiConstants.AL ) {
            return false;
        }
        int chl = 0;    // last non-NSM char in [s,e)
        int cll = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            int k = n - i - 1;
            int j = s + k;
            if ( ( j >= 0 ) && ( j < nc ) ) {
                chl = ca [ j ];
                cll = BidiClassUtils.getBidiClass ( chl );
                if ( cll != BidiConstants.NSM ) {
                    break;
                }
            }
        }
        if ( cll != BidiConstants.AL ) {
            return false;
        }
        if ( hasIsolateFinal ( chf ) ) {
            return false;
        } else if ( hasIsolateInitial ( chl ) ) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasMedialFollowingContext ( int[] ca, int nc, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = nc; i < n; i++ ) {
            chf = ca [ i ];
            clf = BidiClassUtils.getBidiClass ( chf );
            if ( clf != BidiConstants.NSM ) {
                break;
            }
        }
        if ( clf != BidiConstants.AL ) {
            return false;
        } else if ( hasIsolateFinal ( chf ) ) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean hasLigaturePrecedingContext ( int[] ca, int nc, int s, int e ) {
        return true;
    }

    private static boolean hasLigatureFollowingContext ( int[] ca, int nc, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = nc; i < n; i++ ) {
            chf = ca [ i ];
            clf = BidiClassUtils.getBidiClass ( chf );
            if ( clf != BidiConstants.NSM ) {
                break;
            }
        }
        if ( clf == BidiConstants.AL ) {
            return true;
        } else  {
            return false;
        }
    }

    /**
     * Ordered array of Unicode scalars designating those Arabic (Script) Letters
     * which exhibit an isolated form in word initial position.
     */
    private static int[] isolatedInitials = {
        0x0621, // HAMZA
        0x0622, // ALEF WITH MADDA ABOVE
        0x0623, // ALEF WITH HAMZA ABOVE
        0x0624, // WAW WITH HAMZA ABOVE
        0x0625, // ALEF WITH HAMZA BELOWW
        0x0627, // ALEF
        0x062F, // DAL
        0x0630, // THAL
        0x0631, // REH
        0x0632, // ZAIN
        0x0648, // WAW
        0x0671, // ALEF WASLA
        0x0672, // ALEF WITH WAVY HAMZA ABOVE
        0x0673, // ALEF WITH WAVY HAMZA BELOW
        0x0675, // HIGH HAMZA ALEF
        0x0676, // HIGH HAMZA WAW
        0x0677, // U WITH HAMZA ABOVE
        0x0688, // DDAL
        0x0689, // DAL WITH RING
        0x068A, // DAL WITH DOT BELOW
        0x068B, // DAL WITH DOT BELOW AND SMALL TAH
        0x068C, // DAHAL
        0x068D, // DDAHAL
        0x068E, // DUL
        0x068F, // DUL WITH THREE DOTS ABOVE DOWNWARDS
        0x0690, // DUL WITH FOUR DOTS ABOVE
        0x0691, // RREH
        0x0692, // REH WITH SMALL V
        0x0693, // REH WITH RING
        0x0694, // REH WITH DOT BELOW
        0x0695, // REH WITH SMALL V BELOW
        0x0696, // REH WITH DOT BELOW AND DOT ABOVE
        0x0697, // REH WITH TWO DOTS ABOVE
        0x0698, // JEH
        0x0699, // REH WITH FOUR DOTS ABOVE
        0x06C4, // WAW WITH RING
        0x06C5, // KIRGHIZ OE
        0x06C6, // OE
        0x06C7, // U
        0x06C8, // YU
        0x06C9, // KIRGHIZ YU
        0x06CA, // WAW WITH TWO DOTS ABOVE
        0x06CB, // VE
        0x06CF, // WAW WITH DOT ABOVE
        0x06EE, // DAL WITH INVERTED V
        0x06EF  // REH WITH INVERTED V
    };

    private static boolean hasIsolateInitial ( int ch ) {
        return Arrays.binarySearch ( isolatedInitials, ch ) >= 0;
    }

    /**
     * Ordered array of Unicode scalars designating those Arabic (Script) Letters
     * which exhibit an isolated form in word final position.
     */
    private static int[] isolatedFinals = {
        0x0621  // HAMZA
    };

    private static boolean hasIsolateFinal ( int ch ) {
        return Arrays.binarySearch ( isolatedFinals, ch ) >= 0;
    }

}
