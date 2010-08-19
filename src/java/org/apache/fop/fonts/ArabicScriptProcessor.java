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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * <p>The <code>ArabicScriptProcessor</code> class implements script processor for
 * performing glypph substitution and positioning operations on content associated with the Arabic script.</p>
 * @author Glenn Adams
 */
public class ArabicScriptProcessor extends ScriptProcessor {

    /**
     * logging instance
     */
    protected static final Log log = LogFactory.getLog(ArabicScriptProcessor.class);                                    // CSOK: ConstantNameCheck

    ArabicScriptProcessor ( String script ) {
        super ( script );
    }

    /** {@inheritDoc} */
    public GlyphSequence substitute ( GlyphSequence gs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups ) {
        // finals
        gs = subFina ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "fina" ) ) );

        // medials
        gs = subMedi ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "medi" ) ) );

        // initials
        gs = subInit ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "init" ) ) );

        // isolates
        gs = subIsol ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "isol" ) ) );

        // required ligatures
        gs = subLiga ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "rlig" ) ) );

        // standard ligatures
        gs = subLiga ( gs, script, language, (GlyphSubtable[]) lookups.get ( new GlyphTable.LookupSpec ( script, language, "liga" ) ) );

        return gs;
    }

    /** {@inheritDoc} */
    public int[] position ( GlyphSequence gs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups ) {
        return null;
    }

    private static GlyphContextTester finalContextTester
        = new GlyphContextTester() { public boolean test ( GlyphSequence gs, GlyphSequence.CharAssociation ca ) { return inFinalContext ( gs, ca ); } };

    private GlyphSequence subFina ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        return substituteSingle ( gs, script, language, "fina", sta, finalContextTester, false );
    }

    private static GlyphContextTester medialContextTester
        = new GlyphContextTester() { public boolean test ( GlyphSequence gs, GlyphSequence.CharAssociation ca ) { return inMedialContext ( gs, ca ); } };

    private GlyphSequence subMedi ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        return substituteSingle ( gs, script, language, "medi", sta, medialContextTester, false );
    }
    
    private static GlyphContextTester initialContextTester
        = new GlyphContextTester() { public boolean test ( GlyphSequence gs, GlyphSequence.CharAssociation ca ) { return inInitialContext ( gs, ca ); } };

    private GlyphSequence subInit ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        return substituteSingle ( gs, script, language, "init", sta, initialContextTester, false );
    }
    
    private static GlyphContextTester isolateContextTester
        = new GlyphContextTester() { public boolean test ( GlyphSequence gs, GlyphSequence.CharAssociation ca ) { return inIsolateContext ( gs, ca ); } };

    private GlyphSequence subIsol ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        return substituteSingle ( gs, script, language, "isol", sta, isolateContextTester, false );
    }
    
    private static GlyphContextTester ligatureContextTester
        = new GlyphContextTester() { public boolean test ( GlyphSequence gs, GlyphSequence.CharAssociation ca ) { return inLigatureContext ( gs, ca ); } };

    private GlyphSequence subLiga ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        return substituteMultiple ( gs, script, language, "liga", sta, ligatureContextTester, false );
    }

    private GlyphSequence substituteSingle ( GlyphSequence gs, String script, String language, String feature, GlyphSubtable[] sta, GlyphContextTester tester, boolean reverse ) {
        if ( ( sta != null ) && ( sta.length > 0 ) ) {
            // enforce subtable type constraints
            for ( int i = 0, n = sta.length; i < n; i++ ) {
                GlyphSubtable st = sta [ i ];
                if ( ! ( st instanceof GlyphSubstitutionSubtable ) ) {
                    throw new IncompatibleSubtableException ( "'" + feature + "' feature requires glyph substitution subtable" );
                }
            }
            CharSequence ga = gs.getGlyphs();
            GlyphSequence.CharAssociation[] aa = gs.getAssociations();
            List gsl = new ArrayList();
            List cal = new ArrayList();
            for ( int i = 0, n = ga.length(); i < n; i++ ) {
                int k = reverse ? ( n - i - 1 ) : i;
                GlyphSequence.CharAssociation a = aa [ k ];
                GlyphSequence iss = gs.getGlyphSubsequence ( k, k + 1 );
                GlyphSequence oss;
                if ( tester.test ( iss, a ) ) {
                    oss = doSubstitutions ( iss, script, language, sta );
                } else {
                    oss = iss;
                }
                gsl.add ( oss );
                cal.add ( a );
            }
            gs = new GlyphSequence ( gs.getCharacters(), gsl, cal, reverse );
        }
        return gs;
    }

    private GlyphSequence substituteMultiple ( GlyphSequence gs, String script, String language, String feature, GlyphSubtable[] sta, GlyphContextTester tester, boolean reverse ) {
        if ( ( sta != null ) && ( sta.length > 0 ) ) {
            gs = doSubstitutions ( gs, script, language, sta );
        }
        return gs;
    }

    private GlyphSequence doSubstitutions ( GlyphSequence gs, String script, String language, GlyphSubtable[] sta ) {
        for ( int i = 0, n = sta.length; i < n; i++ ) {
            GlyphSubtable st = sta [ i ];
            assert st instanceof GlyphSubstitutionSubtable;
            gs = ( (GlyphSubstitutionSubtable) st ) . substitute ( gs, script, language );
        }
        return gs;
    }

    private static boolean inFinalContext ( GlyphSequence gs, GlyphSequence.CharAssociation a ) {
        CharSequence cs = gs.getCharacters();
        if ( cs.length() == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasFinalPrecedingContext ( cs, s, e ) ) {
                return false;
            } else if ( forcesFinalThisContext ( cs, s, e ) ) {
                if (log.isDebugEnabled()) {
                    log.debug ( "+FIN: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
                }
                return true;
            } else if ( ! hasFinalFollowingContext ( cs, s, e ) ) {
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug ( "+FIN: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
                }
                return true;
            }
        }
    }

    private static boolean inMedialContext ( GlyphSequence gs, GlyphSequence.CharAssociation a ) {
        CharSequence cs = gs.getCharacters();
        if ( cs.length() == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasMedialPrecedingContext ( cs, s, e ) ) {
                return false;
            } else if ( ! hasMedialThisContext ( cs, s, e ) ) {
                return false;
            } else if ( ! hasMedialFollowingContext ( cs, s, e ) ) {
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug ( "+MED: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
                }
                return true;
            }
        }
    }

    private static boolean inInitialContext ( GlyphSequence gs, GlyphSequence.CharAssociation a ) {
        CharSequence cs = gs.getCharacters();
        if ( cs.length() == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasInitialPrecedingContext ( cs, s, e ) ) {
                return false;
            } else if ( ! hasInitialFollowingContext ( cs, s, e ) ) {
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug ( "+INI: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
                }
                return true;
            }
        }
    }

    private static boolean inIsolateContext ( GlyphSequence gs, GlyphSequence.CharAssociation a ) {
        CharSequence cs = gs.getCharacters();
        int n;
        if ( ( n = cs.length() ) == 0 ) {
            return false;
        } else if ( ( a.getStart() == 0 ) && ( a.getEnd() == n ) ) {
            if (log.isDebugEnabled()) {
                log.debug ( "+ISO: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean inLigatureContext ( GlyphSequence gs, GlyphSequence.CharAssociation a ) {
        CharSequence cs = gs.getCharacters();
        if ( cs.length() == 0 ) {
            return false;
        } else {
            int s = a.getStart();
            int e = a.getEnd();
            if ( ! hasLigaturePrecedingContext ( cs, s, e ) ) {
                return false;
            } else if ( ! hasLigatureFollowingContext ( cs, s, e ) ) {
                return false;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug ( "+LIG: [" + a.getStart() + "," + a.getEnd() + "]: " + GlyphUtils.toString ( (CharSequence) gs ) );
                }
                return true;
            }
        }
    }

    private static boolean hasFinalPrecedingContext ( CharSequence cs, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            chp = cs.charAt ( i - 1 );
            clp = BidiClassUtils.getBidiClass ( chp );
            if ( clp != BidiConstants.NSM ) {
                break;
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

    private static boolean forcesFinalThisContext ( CharSequence cs, int s, int e ) {
        int chl = 0;
        int cll = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            int k = n - i - 1;
            chl = cs.charAt ( s + k );
            cll = BidiClassUtils.getBidiClass ( chl );
            if ( cll != BidiConstants.NSM ) {
                break;
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

    private static boolean hasFinalFollowingContext ( CharSequence cs, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = cs.length(); i < n; i++ ) {
            chf = cs.charAt ( i );
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

    private static boolean hasInitialPrecedingContext ( CharSequence cs, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            chp = cs.charAt ( i - 1 );
            clp = BidiClassUtils.getBidiClass ( chp );
            if ( clp != BidiConstants.NSM ) {
                break;
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

    private static boolean hasInitialFollowingContext ( CharSequence cs, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = cs.length(); i < n; i++ ) {
            chf = cs.charAt ( i );
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

    private static boolean hasMedialPrecedingContext ( CharSequence cs, int s, int e ) {
        int chp = 0;
        int clp = 0;
        for ( int i = s; i > 0; i-- ) {
            chp = cs.charAt ( i - 1 );
            clp = BidiClassUtils.getBidiClass ( chp );
            if ( clp != BidiConstants.NSM ) {
                break;
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

    private static boolean hasMedialThisContext ( CharSequence cs, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            chf = cs.charAt ( s + i );
            clf = BidiClassUtils.getBidiClass ( chf );
            if ( clf != BidiConstants.NSM ) {
                break;
            }
        }
        if ( clf != BidiConstants.AL ) {
            return false;
        }
        int chl = 0;
        int cll = 0;
        for ( int i = 0, n = e - s; i < n; i++ ) {
            int k = n - i - 1;
            chl = cs.charAt ( s + k );
            cll = BidiClassUtils.getBidiClass ( chl );
            if ( cll != BidiConstants.NSM ) {
                break;
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

    private static boolean hasMedialFollowingContext ( CharSequence cs, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = cs.length(); i < n; i++ ) {
            chf = cs.charAt ( i );
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

    private static boolean hasLigaturePrecedingContext ( CharSequence cs, int s, int e ) {
        return true;
    }

    private static boolean hasLigatureFollowingContext ( CharSequence cs, int s, int e ) {
        int chf = 0;
        int clf = 0;
        for ( int i = e, n = cs.length(); i < n; i++ ) {
            chf = cs.charAt ( i );
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
