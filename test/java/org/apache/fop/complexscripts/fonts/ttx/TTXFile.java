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

package org.apache.fop.complexscripts.fonts.ttx;

import java.io.File;
import java.io.IOException;

import java.nio.IntBuffer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.complexscripts.fonts.GlyphClassTable;
import org.apache.fop.complexscripts.fonts.GlyphCoverageTable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionSubtable;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphMappingTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningSubtable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable.Anchor;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable.MarkAnchor;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable.PairValues;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable.Value;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionSubtable;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable.Ligature;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable.LigatureSet;
import org.apache.fop.complexscripts.fonts.GlyphSubtable;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.fonts.GlyphTable.RuleLookup;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.UTF32;
import org.apache.fop.util.CharUtilities;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck

/**
 * This class supports a subset of the <code>TTX</code> file as produced by the Adobe FLEX
 * SDK (AFDKO). In particular, it is used to parse a <code>TTX</code> file in order to
 * extract character to glyph code mapping data, glyph definition data, glyph substitution
 * data, and glyph positioning data.
 *
 * <code>TTX</code> files are used in FOP for testing and debugging purposes only. Such
 * files are used to represent font data employed by complex script processing, and
 * normally extracted directly from an opentype (or truetype) file. However, due to
 * copyright restrictions, it is not possible to include most opentype (or truetype) font
 * files directly in the FOP distribution. In such cases, <code>TTX</code> files are used
 * to distribute a subset of the complex script advanced table information contained in
 * certain font files to facilitate testing.
 *
 * @author Glenn Adams
 */
public class TTXFile {

    /** logging instance */
    private static final Log log = LogFactory.getLog(TTXFile.class);                                                    // CSOK: ConstantNameCheck
    /** default script tag */
    private static final String DEFAULT_SCRIPT_TAG = "dflt";
    /** default language tag */
    private static final String DEFAULT_LANGUAGE_TAG = "dflt";

    /** ttxfile cache */
    private static Map<String,TTXFile> cache = new HashMap<String,TTXFile>();

    // transient parsing state
    private Locator locator;                                    // current document locator
    private Stack<String[]> elements;                           // stack of ttx elements being parsed
    private Map<String,Integer> glyphIds;                       // map of glyph names to glyph identifiers
    private List<int[]> cmapEntries;                            // list of <charCode,glyphCode> pairs
    private Vector<int[]> hmtxEntries;                          // vector of <width,lsb> pairs
    private Map<String,Integer> glyphClasses;                   // map of glyph names to glyph classes
    private Map<String,Map<String,List<String>>> scripts;       // map of script tag to Map<language-tag,List<features-id>>>
    private Map<String,List<String>> languages;                 // map of language tag to List<feature-id>
    private Map<String,Object[]> features;                      // map of feature id to Object[2] : { feature-tag, List<lookup-id> }
    private List<String> languageFeatures;                      // list of language system feature ids, where first is (possibly null) required feature id
    private List<String> featureLookups;                        // list of lookup ids for feature being constructed
    private List<Integer> coverageEntries;                      // list of entries for coverage table being constructed
    private Map<String,GlyphCoverageTable> coverages;           // map of coverage table keys to coverage tables
    private List subtableEntries;                               // list of lookup subtable entries
    private List<GlyphSubtable> subtables;                      // list of constructed subtables
    private List<Integer> alternates;                           // list of alternates in alternate set being constructed
    private List<Ligature> ligatures;                           // list of ligatures in ligature set being constructed
    private List<Integer> substitutes;                          // list of substitutes in (multiple substitution) sequence being constructed
    private List<PairValues> pairs;                             // list of pair value records being constructed
    private List<PairValues[]> pairSets;                        // list of pair value sets (as arrays) being constructed
    private List<Anchor> anchors;                               // list of anchors of base|mark|component record being constructed
    private List<Anchor[]> components;                          // list of ligature component anchors being constructed
    private List<MarkAnchor> markAnchors;                       // list of mark anchors being constructed
    private List<Anchor[]> baseOrMarkAnchors;                   // list of base|mark2 anchors being constructed
    private List<Anchor[][]> ligatureAnchors;                   // list of ligature anchors being constructed
    private List<Anchor[]> attachmentAnchors;                   // list of entry|exit attachment anchors being constructed
    private List<RuleLookup> ruleLookups;                       // list of rule lookups being constructed
    private int glyphIdMax;                                     // maximum glyph id
    private int cmPlatform;                                     // plaform id of cmap being constructed
    private int cmEncoding;                                     // plaform id of cmap being constructed
    private int cmLanguage;                                     // plaform id of cmap being constructed
    private int flIndex;                                        // index of feature being constructed
    private int flSequence;                                     // feature sequence within feature list
    private int ltIndex;                                        // index of lookup table being constructed
    private int ltSequence;                                     // lookup sequence within table
    private int ltFlags;                                        // flags of current lookup being constructed
    private int stSequence;                                     // subtable sequence number within lookup
    private int stFormat;                                       // format of current subtable being constructed
    private int ctFormat;                                       // format of coverage table being constructed
    private int ctIndex;                                        // index of coverage table being constructed
    private int rlSequence;                                     // rule lookup sequence index
    private int rlLookup;                                       // rule lookup lookup index
    private int psIndex;                                        // pair set index
    private int vf1;                                            // value format 1 (used with pair pos and single pos)
    private int vf2;                                            // value format 2 (used with pair pos)
    private int g2;                                             // glyph id 2 (used with pair pos)
    private int xCoord;                                         // x coordinate of anchor being constructed
    private int yCoord;                                         // y coordinate of anchor being constructed
    private int markClass;                                      // mark class of mark anchor being constructed
    private String defaultScriptTag;                            // tag of default script
    private String scriptTag;                                   // tag of script being constructed
    private String defaultLanguageTag;                          // tag of default language system
    private String languageTag;                                 // tag of language system being constructed
    private String featureTag;                                  // tag of feature being constructed
    private Value v1;                                           // positioining value 1
    private Value v2;                                           // positioining value 2

    // resultant state
    private int upem;                                           // units per em
    private Map<Integer,Integer> cmap;                          // constructed character map
    private Map<Integer,Integer> gmap;                          // constructed glyph map
    private int[][] hmtx;                                       // constructed horizontal metrics - array of design { width, lsb } pairs, indexed by glyph code
    private int[] widths;                                       // pdf normalized widths (millipoints)
    private GlyphDefinitionTable gdef;                          // constructed glyph definition table
    private GlyphSubstitutionTable gsub;                        // constructed glyph substitution table
    private GlyphPositioningTable gpos;                         // constructed glyph positioning table

    public TTXFile() {
        elements = new Stack<String[]>();
        glyphIds = new HashMap<String,Integer>();
        cmapEntries = new ArrayList<int[]>();
        hmtxEntries = new Vector<int[]>();
        glyphClasses = new HashMap<String,Integer>();
        scripts = new HashMap<String,Map<String,List<String>>>();
        languages = new HashMap<String,List<String>>();
        features = new HashMap<String,Object[]>();
        languageFeatures = new ArrayList<String>();
        featureLookups = new ArrayList<String>();
        coverageEntries = new ArrayList<Integer>();
        coverages = new HashMap<String,GlyphCoverageTable>();
        subtableEntries = new ArrayList();
        subtables = new ArrayList<GlyphSubtable>();
        alternates = new ArrayList<Integer>();
        ligatures = new ArrayList<Ligature>();
        substitutes = new ArrayList<Integer>();
        pairs = new ArrayList<PairValues>();
        pairSets = new ArrayList<PairValues[]>();
        anchors = new ArrayList<Anchor>();
        markAnchors = new ArrayList<MarkAnchor>();
        baseOrMarkAnchors = new ArrayList<Anchor[]>();
        ligatureAnchors = new ArrayList<Anchor[][]>();
        components = new ArrayList<Anchor[]>();
        attachmentAnchors = new ArrayList<Anchor[]>();
        ruleLookups = new ArrayList<RuleLookup>();
        glyphIdMax = -1;
        cmPlatform = -1;
        cmEncoding = -1;
        cmLanguage = -1;
        flIndex = -1;
        flSequence = 0;
        ltIndex = -1;
        ltSequence = 0;
        ltFlags = 0;
        stSequence = 0;
        stFormat = 0;
        ctFormat = -1;
        ctIndex = -1;
        rlSequence = -1;
        rlLookup = -1;
        psIndex = -1;
        vf1 = -1;
        vf2 = -1;
        g2 = -1;
        xCoord = Integer.MIN_VALUE;
        yCoord = Integer.MIN_VALUE;
        markClass = -1;
        defaultScriptTag = DEFAULT_SCRIPT_TAG;
        scriptTag = null;
        defaultLanguageTag = DEFAULT_LANGUAGE_TAG;
        languageTag = null;
        featureTag = null;
        v1 = null;
        v2 = null;
        upem = -1;
    }
    public void parse ( String filename ) {
        parse ( new File ( filename ) );
    }
    public void parse ( File f ) {
        assert f != null;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse ( f, new Handler() );
        } catch ( FactoryConfigurationError e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( ParserConfigurationException e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( SAXException e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( IOException e ) {
            throw new RuntimeException ( e.getMessage() );
        }
    }
    public GlyphSequence mapCharsToGlyphs ( String s ) {
        Integer[] ca = UTF32.toUTF32 ( s, 0, true );
        int ng = ca.length;
        IntBuffer cb = IntBuffer.allocate ( ng );
        IntBuffer gb = IntBuffer.allocate ( ng );
        for ( Integer c : ca ) {
            int g = mapCharToGlyph ( (int) c );
            if ( g >= 0 ) {
                cb.put ( c );
                gb.put ( g );
            } else {
                throw new IllegalArgumentException ( "character " + CharUtilities.format ( c ) + " has no corresponding glyph" );
            }
        }
        cb.rewind();
        gb.rewind();
        return new GlyphSequence ( cb, gb, null );
    }
    public int mapCharToGlyph ( int c ) {
        if ( cmap != null ) {
            Integer g = cmap.get ( Integer.valueOf ( c ) );
            if ( g != null ) {
                return (int) g;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
    public int getGlyph ( String gid ) {
        return mapGlyphId0 ( gid );
    }
    public GlyphSequence getGlyphSequence ( String[] gids ) {
        assert gids != null;
        int ng = gids.length;
        IntBuffer cb = IntBuffer.allocate ( ng );
        IntBuffer gb = IntBuffer.allocate ( ng );
        for ( String gid : gids ) {
            int g = mapGlyphId0 ( gid );
            if ( g >= 0 ) {
                int c = mapGlyphIdToChar ( gid );
                if ( c < 0 ) {
                    c = CharUtilities.NOT_A_CHARACTER;
                }
                cb.put ( c );
                gb.put ( g );
            } else {
                throw new IllegalArgumentException ( "unmapped glyph id \"" + gid + "\"" );
            }
        }
        cb.rewind();
        gb.rewind();
        return new GlyphSequence ( cb, gb, null );
    }
    public int[] getWidths ( String[] gids ) {
        assert gids != null;
        int ng = gids.length;
        int[] widths = new int [ ng ];
        int i = 0;
        for ( String gid : gids ) {
            int g = mapGlyphId0 ( gid );
            int w = 0;
            if ( g >= 0 ) {
                if ( ( hmtx != null ) && ( g < hmtx.length ) ) {
                    int[] mtx = hmtx [ g ];
                    assert mtx != null;
                    assert mtx.length > 0;
                    w = mtx[0];
                }
            }
            widths [ i++ ] = w;
        }
        assert i == ng;
        return widths;
    }
    public int[] getWidths() {
        if ( this.widths == null ) {
            if ( ( hmtx != null ) && ( upem > 0 ) ) {
                int[] widths = new int [ hmtx.length ];
                for ( int i = 0, n = widths.length; i < n; i++ ) {
                    widths [ i ] = getPDFWidth ( hmtx [ i ] [ 0 ], upem );
                }
                this.widths = widths;
            }
        }
        return this.widths;
    }
    public static int getPDFWidth ( int tw, int upem ) {
        // N.B. The following is copied (with minor edits) from TTFFile to insure same results
        int pw;
        if ( tw < 0 ) {
            long rest1 = tw % upem;
            long storrest = 1000 * rest1;
            long ledd2 = ( storrest != 0 ) ? ( rest1 / storrest ) : 0;
            pw = - ( ( -1000 * tw ) / upem - (int) ledd2 );
        } else {
            pw = ( tw / upem ) * 1000 + ( ( tw % upem ) * 1000 ) / upem;
        }
        return pw;
    }
    public GlyphDefinitionTable getGDEF() {
        return gdef;
    }
    public GlyphSubstitutionTable getGSUB() {
        return gsub;
    }
    public GlyphPositioningTable getGPOS() {
        return gpos;
    }
    public static synchronized TTXFile getFromCache ( String filename ) {
        assert cache != null;
        TTXFile f;
        if ( ( f = (TTXFile) cache.get ( filename ) ) == null ) {
            f = new TTXFile();
            f.parse ( filename );
            cache.put ( filename, f );
        }
        return f;
    }
    public static synchronized void clearCache() {
        cache.clear();
    }
    private class Handler extends DefaultHandler {
        private Handler() {
        }
        @Override
        public void startDocument() {
        }
        @Override
        public void endDocument() {
        }
        @Override
        public void setDocumentLocator ( Locator locator ) {
            TTXFile.this.locator = locator;
        }
        @Override
        public void startElement ( String uri, String localName, String qName, Attributes attrs ) throws SAXException {
            String[] en = makeExpandedName ( uri, localName, qName );
            if ( en[0] != null ) {
                unsupportedElement ( en );
            } else if ( en[1].equals ( "Alternate" ) ) {
                String[] pn = new String[] { null, "AlternateSet" };
                if ( isParent ( pn ) ) {
                    String glyph = attrs.getValue ( "glyph" );
                    if ( glyph == null ) {
                        missingRequiredAttribute ( en, "glyph" );
                    }
                    int gid = mapGlyphId ( glyph, en );
                    alternates.add ( Integer.valueOf ( gid ) );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "AlternateSet" ) ) {
                String[] pn = new String[] { null, "AlternateSubst" };
                if ( isParent ( pn ) ) {
                    String glyph = attrs.getValue ( "glyph" );
                    if ( glyph == null ) {
                        missingRequiredAttribute ( en, "glyph" );
                    }
                    int gid = mapGlyphId ( glyph, en );
                    coverageEntries.add ( Integer.valueOf ( gid ) );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "AlternateSubst" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = 1;
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "BacktrackCoverage" ) ) {
                String[] pn1 = new String[] { null, "ChainContextSubst" };
                String[] pn2 = new String[] { null, "ChainContextPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    int ci = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        ci = Integer.parseInt ( index );
                    }
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = ci;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "BaseAnchor" ) ) {
                String[] pn = new String[] { null, "BaseRecord" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "BaseArray" ) ) {
                String[] pn = new String[] { null, "MarkBasePos" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "BaseCoverage" ) ) {
                String[] pn = new String[] { null, "MarkBasePos" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "BaseRecord" ) ) {
                String[] pn = new String[] { null, "BaseArray" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ChainContextPos" ) || en[1].equals ( "ChainContextSubst" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                        case 3:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Class" ) ) {
                String[] pn = new String[] { null, "MarkRecord" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    assert markClass == -1;
                    markClass = v;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ClassDef" ) ) {
                String[] pn1 = new String[] { null, "GlyphClassDef" };
                String[] pn2 = new String[] { null, "MarkAttachClassDef" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String glyph = attrs.getValue ( "glyph" );
                    if ( glyph == null ) {
                        missingRequiredAttribute ( en, "glyph" );
                    }
                    String glyphClass = attrs.getValue ( "class" );
                    if ( glyphClass == null ) {
                        missingRequiredAttribute ( en, "class" );
                    }
                    if ( ! glyphIds.containsKey ( glyph ) ) {
                        unsupportedGlyph ( en, glyph );
                    } else if ( isParent ( pn1 ) ) {
                        if ( glyphClasses.containsKey ( glyph ) ) {
                            duplicateGlyphClass ( en, glyph, glyphClass );
                        } else {
                            glyphClasses.put ( glyph, Integer.parseInt(glyphClass) );
                        }
                    } else if ( isParent ( pn2 ) ) {
                        if ( glyphClasses.containsKey ( glyph ) ) {
                            duplicateGlyphClass ( en, glyph, glyphClass );
                        } else {
                            glyphClasses.put ( glyph, Integer.parseInt(glyphClass) );
                        }
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "ComponentRecord" ) ) {
                String[] pn = new String[] { null, "LigatureAttach" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    assert anchors.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Coverage" ) ) {
                String[] pn1 = new String[] { null, "CursivePos" };
                String[] pn2 = new String[] { null, "LigCaretList" };
                String[] pn3 = new String[] { null, "MultipleSubst" };
                String[] pn4 = new String[] { null, "PairPos" };
                String[] pn5 = new String[] { null, "SinglePos" };
                String[][] pnx = new String[][] { pn1, pn2, pn3, pn4, pn5 };
                if ( isParent ( pnx ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "CursivePos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                    assert attachmentAnchors.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "DefaultLangSys" ) ) {
                String[] pn = new String[] { null, "Script" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                } else {
                    assertLanguageFeaturesClear();
                    assert languageTag == null;
                    languageTag = defaultLanguageTag;
                }
            } else if ( en[1].equals ( "EntryAnchor" ) ) {
                String[] pn = new String[] { null, "EntryExitRecord" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "EntryExitRecord" ) ) {
                String[] pn = new String[] { null, "CursivePos" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ExitAnchor" ) ) {
                String[] pn = new String[] { null, "EntryExitRecord" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Feature" ) ) {
                String[] pn = new String[] { null, "FeatureRecord" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                } else {
                    assertFeatureLookupsClear();
                }
            } else if ( en[1].equals ( "FeatureIndex" ) ) {
                String[] pn1 = new String[] { null, "DefaultLangSys" };
                String[] pn2 = new String[] { null, "LangSys" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    if ( languageFeatures.size() == 0 ) {
                        languageFeatures.add ( null );
                    }
                    if ( ( v >= 0 ) && ( v < 65535 ) ) {
                        languageFeatures.add ( makeFeatureId ( v ) );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "FeatureList" ) ) {
                String[] pn1 = new String[] { null, "GSUB" };
                String[] pn2 = new String[] { null, "GPOS" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( ! isParent ( pnx ) ) {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "FeatureRecord" ) ) {
                String[] pn = new String[] { null, "FeatureList" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    int fi = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        fi = Integer.parseInt ( index );
                    }
                    assertFeatureClear();
                    assert flIndex == -1;
                    flIndex = fi;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "FeatureTag" ) ) {
                String[] pn = new String[] { null, "FeatureRecord" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        assert featureTag == null;
                        featureTag = value;
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "GDEF" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( isParent ( pn ) ) {
                    assertSubtablesClear();
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "GPOS" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( isParent ( pn ) ) {
                    assertCoveragesClear();
                    assertSubtablesClear();
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "GSUB" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( isParent ( pn ) ) {
                    assertCoveragesClear();
                    assertSubtablesClear();
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Glyph" ) ) {
                String[] pn1 = new String[] { null, "Coverage" };
                String[] pn2 = new String[] { null, "InputCoverage" };
                String[] pn3 = new String[] { null, "LookAheadCoverage" };
                String[] pn4 = new String[] { null, "BacktrackCoverage" };
                String[] pn5 = new String[] { null, "MarkCoverage" };
                String[] pn6 = new String[] { null, "Mark1Coverage" };
                String[] pn7 = new String[] { null, "Mark2Coverage" };
                String[] pn8 = new String[] { null, "BaseCoverage" };
                String[] pn9 = new String[] { null, "LigatureCoverage" };
                String[][] pnx = new String[][] { pn1, pn2, pn3, pn4, pn5, pn6, pn7, pn8, pn9 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        int gid = mapGlyphId ( value, en );
                        coverageEntries.add ( Integer.valueOf ( gid ) );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "GlyphClassDef" ) ) {
                String[] pn = new String[] { null, "GDEF" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    // force format 1 since TTX always writes entries as non-range entries
                    if ( sf != 1 ) {
                        sf = 1;
                    }
                    stFormat = sf;
                    assert glyphClasses.isEmpty();
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "GlyphID" ) ) {
                String[] pn = new String[] { null, "GlyphOrder" };
                if ( isParent ( pn ) ) {
                    String id = attrs.getValue ( "id" );
                    int gid = -1;
                    if ( id == null ) {
                        missingRequiredAttribute ( en, "id" );
                    } else {
                        gid = Integer.parseInt ( id );
                    }
                    String name = attrs.getValue ( "name" );
                    if ( name == null ) {
                        missingRequiredAttribute ( en, "name" );
                    }
                    if ( glyphIds.containsKey ( name ) ) {
                        duplicateGlyph ( en, name, gid );
                    } else {
                        if ( gid > glyphIdMax ) {
                            glyphIdMax = gid;
                        }
                        glyphIds.put ( name, gid );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "GlyphOrder" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "InputCoverage" ) ) {
                String[] pn1 = new String[] { null, "ChainContextSubst" };
                String[] pn2 = new String[] { null, "ChainContextPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    int ci = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        ci = Integer.parseInt ( index );
                    }
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = ci;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "LangSys" ) ) {
                String[] pn = new String[] { null, "LangSysRecord" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                } else {
                    assertLanguageFeaturesClear();
                }
            } else if ( en[1].equals ( "LangSysRecord" ) ) {
                String[] pn = new String[] { null, "Script" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LangSysTag" ) ) {
                String[] pn = new String[] { null, "LangSysRecord" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        assert languageTag == null;
                        languageTag = value;
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigCaretList" ) ) {
                String[] pn = new String[] { null, "GDEF" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Ligature" ) ) {
                String[] pn = new String[] { null, "LigatureSet" };
                if ( isParent ( pn ) ) {
                    String components = attrs.getValue ( "components" );
                    if ( components == null ) {
                        missingRequiredAttribute ( en, "components" );
                    }
                    int[] cids = mapGlyphIds ( components, en );
                    String glyph = attrs.getValue ( "glyph" );
                    if ( glyph == null ) {
                        missingRequiredAttribute ( en, "glyph" );
                    }
                    int gid = mapGlyphId ( glyph, en );
                    ligatures.add ( new Ligature ( gid, cids ) );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureAnchor" ) ) {
                String[] pn = new String[] { null, "ComponentRecord" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureArray" ) ) {
                String[] pn = new String[] { null, "MarkLigPos" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureAttach" ) ) {
                String[] pn = new String[] { null, "LigatureArray" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    assert components.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureCoverage" ) ) {
                String[] pn = new String[] { null, "MarkLigPos" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureSet" ) ) {
                String[] pn = new String[] { null, "LigatureSubst" };
                if ( isParent ( pn ) ) {
                    String glyph = attrs.getValue ( "glyph" );
                    if ( glyph == null ) {
                        missingRequiredAttribute ( en, "glyph" );
                    }
                    int gid = mapGlyphId ( glyph, en );
                    coverageEntries.add ( Integer.valueOf ( gid ) );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LigatureSubst" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = 1;
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LookAheadCoverage" ) ) {
                String[] pn1 = new String[] { null, "ChainContextSubst" };
                String[] pn2 = new String[] { null, "ChainContextPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    int ci = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        ci = Integer.parseInt ( index );
                    }
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = ci;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "Lookup" ) ) {
                String[] pn = new String[] { null, "LookupList" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    int li = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        li = Integer.parseInt ( index );
                    }
                    assertLookupClear();
                    assert ltIndex == -1;
                    ltIndex = li;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LookupFlag" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int lf = 0;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        lf = Integer.parseInt ( value );
                    }
                    assert ltFlags == 0;
                    ltFlags = lf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "LookupList" ) ) {
                String[] pn1 = new String[] { null, "GSUB" };
                String[] pn2 = new String[] { null, "GPOS" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( ! isParent ( pnx ) ) {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "LookupListIndex" ) ) {
                String[] pn1 = new String[] { null, "Feature" };
                String[] pn2 = new String[] { null, "SubstLookupRecord" };
                String[] pn3 = new String[] { null, "PosLookupRecord" };
                String[][] pnx = new String[][] { pn1, pn2, pn3 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    String[][] pny = new String[][] { pn2, pn3 };
                    if ( isParent ( pny ) ) {
                        assert rlLookup == -1;
                        assert v != -1;
                        rlLookup = v;
                    } else {
                        featureLookups.add ( makeLookupId ( v ) );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "LookupType" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark1Array" ) ) {
                String[] pn = new String[] { null, "MarkMarkPos" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark1Coverage" ) ) {
                String[] pn = new String[] { null, "MarkMarkPos" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark2Anchor" ) ) {
                String[] pn = new String[] { null, "Mark2Record" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark2Array" ) ) {
                String[] pn = new String[] { null, "MarkMarkPos" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark2Coverage" ) ) {
                String[] pn = new String[] { null, "MarkMarkPos" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Mark2Record" ) ) {
                String[] pn = new String[] { null, "Mark2Array" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkAnchor" ) ) {
                String[] pn = new String[] { null, "MarkRecord" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    assert yCoord == Integer.MIN_VALUE;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkArray" ) ) {
                String[] pn1 = new String[] { null, "MarkBasePos" };
                String[] pn2 = new String[] { null, "MarkLigPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( ! isParent ( pnx ) ) {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "MarkAttachClassDef" ) ) {
                String[] pn = new String[] { null, "GDEF" };
                if ( isParent ( pn ) ) {
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    // force format 1 since TTX always writes entries as non-range entries
                    if ( sf != 1 ) {
                        sf = 1;
                    }
                    stFormat = sf;
                    assert glyphClasses.isEmpty();
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkBasePos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                    assert markAnchors.size() == 0;
                    assert baseOrMarkAnchors.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkCoverage" ) ) {
                String[] pn1 = new String[] { null, "MarkBasePos" };
                String[] pn2 = new String[] { null, "MarkLigPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String format = attrs.getValue ( "Format" );
                    int cf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        cf = Integer.parseInt ( format );
                        switch ( cf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, cf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = cf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "MarkLigPos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                    assert markAnchors.size() == 0;
                    assert ligatureAnchors.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkMarkPos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                    assert markAnchors.size() == 0;
                    assert baseOrMarkAnchors.size() == 0;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "MarkRecord" ) ) {
                String[] pn1 = new String[] { null, "MarkArray" };
                String[] pn2 = new String[] { null, "Mark1Array" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "MultipleSubst" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "PairPos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "PairSet" ) ) {
                String[] pn = new String[] { null, "PairPos" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    int psi = -1;
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        psi = Integer.parseInt ( index );
                    }
                    assert psIndex == -1;
                    psIndex = psi;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "PairValueRecord" ) ) {
                String[] pn = new String[] { null, "PairSet" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        assertPairClear();
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "PosLookupRecord" ) ) {
                String[] pn1 = new String[] { null, "ChainContextSubst" };
                String[] pn2 = new String[] { null, "ChainContextPos" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "ReqFeatureIndex" ) ) {
                String[] pn1 = new String[] { null, "DefaultLangSys" };
                String[] pn2 = new String[] { null, "LangSys" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    String fid;
                    if ( ( v >= 0 ) && ( v < 65535 ) ) {
                        fid = makeFeatureId ( v );
                    } else {
                        fid = null;
                    }
                    assertLanguageFeaturesClear();
                    languageFeatures.add ( fid );
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "Script" ) ) {
                String[] pn = new String[] { null, "ScriptRecord" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ScriptList" ) ) {
                String[] pn1 = new String[] { null, "GSUB" };
                String[] pn2 = new String[] { null, "GPOS" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( ! isParent ( pnx ) ) {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "ScriptRecord" ) ) {
                String[] pn = new String[] { null, "ScriptList" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ScriptTag" ) ) {
                String[] pn = new String[] { null, "ScriptRecord" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        assert scriptTag == null;
                        scriptTag = value;
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "SecondGlyph" ) ) {
                String[] pn = new String[] { null, "PairValueRecord" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        int gid = mapGlyphId ( value, en );
                        assert g2 == -1;
                        g2 = gid;
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Sequence" ) ) {
                String[] pn = new String[] { null, "MultipleSubst" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        int i = Integer.parseInt ( index );
                        if ( i != subtableEntries.size() ) {
                            invalidIndex ( en, i, subtableEntries.size() );
                        }
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "SequenceIndex" ) ) {
                String[] pn1 = new String[] { null, "PosLookupRecord" };
                String[] pn2 = new String[] { null, "SubstLookupRecord" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    assert rlSequence == -1;
                    assert v != -1;
                    rlSequence = v;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "SinglePos" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "SingleSubst" ) ) {
                String[] pn = new String[] { null, "Lookup" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                    String format = attrs.getValue ( "Format" );
                    int sf = -1;
                    if ( format == null ) {
                        missingRequiredAttribute ( en, "Format" );
                    } else {
                        sf = Integer.parseInt ( format );
                        switch ( sf ) {
                        case 1:
                        case 2:
                            break;
                        default:
                            unsupportedFormat ( en, sf );
                            break;
                        }
                    }
                    assertCoverageClear();
                    ctIndex = 0;
                    ctFormat = 1;
                    assertSubtableClear();
                    assert sf >= 0;
                    stFormat = sf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "SubstLookupRecord" ) ) {
                String[] pn = new String[] { null, "ChainContextSubst" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Substitute" ) ) {
                String[] pn = new String[] { null, "Sequence" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( index == null ) {
                        missingRequiredAttribute ( en, "index" );
                    } else {
                        int i = Integer.parseInt ( index );
                        if ( i != substitutes.size() ) {
                            invalidIndex ( en, i, substitutes.size() );
                        }
                    }
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        int gid = mapGlyphId ( value, en );
                        substitutes.add ( Integer.valueOf ( gid ) );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Substitution" ) ) {
                String[] pn = new String[] { null, "SingleSubst" };
                if ( isParent ( pn ) ) {
                    String in = attrs.getValue ( "in" );
                    int igid = -1;
                    int ogid = -1;
                    if ( in == null ) {
                        missingRequiredAttribute ( en, "in" );
                    } else {
                        igid = mapGlyphId ( in, en );
                    }
                    String out = attrs.getValue ( "out" );
                    if ( out == null ) {
                        missingRequiredAttribute ( en, "out" );
                    } else {
                        ogid = mapGlyphId ( out, en );
                    }
                    coverageEntries.add ( Integer.valueOf ( igid ) );
                    subtableEntries.add ( Integer.valueOf ( ogid ) );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Value" ) ) {
                String[] pn = new String[] { null, "SinglePos" };
                if ( isParent ( pn ) ) {
                    String index = attrs.getValue ( "index" );
                    if ( vf1 < 0 ) {
                        missingParameter ( en, "value format" );
                    } else {
                        subtableEntries.add ( parseValue ( en, attrs, vf1 ) );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Value1" ) ) {
                String[] pn = new String[] { null, "PairValueRecord" };
                if ( isParent ( pn ) ) {
                    if ( vf1 < 0 ) {
                        missingParameter ( en, "value format 1" );
                    } else {
                        assert v1 == null;
                        v1 = parseValue ( en, attrs, vf1 );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Value2" ) ) {
                String[] pn = new String[] { null, "PairValueRecord" };
                if ( isParent ( pn ) ) {
                    if ( vf2 < 0 ) {
                        missingParameter ( en, "value format 2" );
                    } else {
                        assert v2 == null;
                        v2 = parseValue ( en, attrs, vf2 );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ValueFormat" ) ) {
                String[] pn = new String[] { null, "SinglePos" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int vf = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        vf = Integer.parseInt ( value );
                    }
                    assert vf1 == -1;
                    vf1 = vf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ValueFormat1" ) ) {
                String[] pn = new String[] { null, "PairPos" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int vf = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        vf = Integer.parseInt ( value );
                    }
                    assert vf1 == -1;
                    vf1 = vf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "ValueFormat2" ) ) {
                String[] pn = new String[] { null, "PairPos" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int vf = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        vf = Integer.parseInt ( value );
                    }
                    assert vf2 == -1;
                    vf2 = vf;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "Version" ) ) {
                String[] pn1 = new String[] { null, "GDEF" };
                String[] pn2 = new String[] { null, "GPOS" };
                String[] pn3 = new String[] { null, "GSUB" };
                String[][] pnx = new String[][] { pn1, pn2, pn3 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "XCoordinate" ) ) {
                String[] pn1 = new String[] { null, "BaseAnchor" };
                String[] pn2 = new String[] { null, "EntryAnchor" };
                String[] pn3 = new String[] { null, "ExitAnchor" };
                String[] pn4 = new String[] { null, "LigatureAnchor" };
                String[] pn5 = new String[] { null, "MarkAnchor" };
                String[] pn6 = new String[] { null, "Mark2Anchor" };
                String[][] pnx = new String[][] { pn1, pn2, pn3, pn4, pn5, pn6 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    int x = 0;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        x = Integer.parseInt ( value );
                    }
                    assert xCoord == Integer.MIN_VALUE;
                    xCoord = x;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "YCoordinate" ) ) {
                String[] pn1 = new String[] { null, "BaseAnchor" };
                String[] pn2 = new String[] { null, "EntryAnchor" };
                String[] pn3 = new String[] { null, "ExitAnchor" };
                String[] pn4 = new String[] { null, "LigatureAnchor" };
                String[] pn5 = new String[] { null, "MarkAnchor" };
                String[] pn6 = new String[] { null, "Mark2Anchor" };
                String[][] pnx = new String[][] { pn1, pn2, pn3, pn4, pn5, pn6 };
                if ( isParent ( pnx ) ) {
                    String value = attrs.getValue ( "value" );
                    int y = 0;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        y = Integer.parseInt ( value );
                    }
                    assert yCoord == Integer.MIN_VALUE;
                    yCoord = y;
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "checkSumAdjustment" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "cmap" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "cmap_format_0" ) ) {
                String[] pn = new String[] { null, "cmap" };
                if ( isParent ( pn ) ) {
                    String platformID = attrs.getValue ( "platformID" );
                    if ( platformID == null ) {
                        missingRequiredAttribute ( en, "platformID" );
                    }
                    String platEncID = attrs.getValue ( "platEncID" );
                    if ( platEncID == null ) {
                        missingRequiredAttribute ( en, "platEncID" );
                    }
                    String language = attrs.getValue ( "language" );
                    if ( language == null ) {
                        missingRequiredAttribute ( en, "language" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "cmap_format_4" ) ) {
                String[] pn = new String[] { null, "cmap" };
                if ( isParent ( pn ) ) {
                    String platformID = attrs.getValue ( "platformID" );
                    int pid = -1;
                    if ( platformID == null ) {
                        missingRequiredAttribute ( en, "platformID" );
                    } else {
                        pid = Integer.parseInt ( platformID );
                    }
                    String platEncID = attrs.getValue ( "platEncID" );
                    int eid = -1;
                    if ( platEncID == null ) {
                        missingRequiredAttribute ( en, "platEncID" );
                    } else {
                        eid = Integer.parseInt ( platEncID );
                    }
                    String language = attrs.getValue ( "language" );
                    int lid = -1;
                    if ( language == null ) {
                        missingRequiredAttribute ( en, "language" );
                    } else {
                        lid = Integer.parseInt ( language );
                    }
                    assert cmapEntries.size() == 0;
                    assert cmPlatform == -1;
                    assert cmEncoding == -1;
                    assert cmLanguage == -1;
                    cmPlatform = pid;
                    cmEncoding = eid;
                    cmLanguage = lid;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "created" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "flags" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "fontDirectionHint" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "fontRevision" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "glyphDataFormat" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "head" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "hmtx" ) ) {
                String[] pn = new String[] { null, "ttFont" };
                if ( ! isParent ( pn ) ) {
                    notPermittedInElementContext ( en, getParent(), pn );
                } else if ( glyphIdMax > 0 ) {
                    hmtxEntries.setSize ( glyphIdMax + 1 );
                }
            } else if ( en[1].equals ( "indexToLocFormat" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "lowestRecPPEM" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "macStyle" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "magicNumber" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "map" ) ) {
                String[] pn1 = new String[] { null, "cmap_format_0" };
                String[] pn2 = new String[] { null, "cmap_format_4" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pnx ) ) {
                    String code = attrs.getValue ( "code" );
                    int cid = -1;
                    if ( code == null ) {
                        missingRequiredAttribute ( en, "code" );
                    } else {
                        code = code.toLowerCase();
                        if ( code.startsWith ( "0x" ) ) {
                            cid = Integer.parseInt ( code.substring ( 2 ), 16 );
                        } else {
                            cid = Integer.parseInt ( code, 10 );
                        }
                    }
                    String name = attrs.getValue ( "name" );
                    int gid = -1;
                    if ( name == null ) {
                        missingRequiredAttribute ( en, "name" );
                    } else {
                        gid = mapGlyphId ( name, en );
                    }
                    if ( ( cmPlatform == 3 ) && ( cmEncoding == 1 ) ) {
                        cmapEntries.add ( new int[] { cid, gid } );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "modified" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "mtx" ) ) {
                String[] pn = new String[] { null, "hmtx" };
                if ( isParent ( pn ) ) {
                    String name = attrs.getValue ( "name" );
                    int gid = -1;
                    if ( name == null ) {
                        missingRequiredAttribute ( en, "name" );
                    } else {
                        gid = mapGlyphId ( name, en );
                    }
                    String width = attrs.getValue ( "width" );
                    int w = -1;
                    if ( width == null ) {
                        missingRequiredAttribute ( en, "width" );
                    } else {
                        w = Integer.parseInt ( width );
                    }
                    String lsb = attrs.getValue ( "lsb" );
                    int l = -1;
                    if ( lsb == null ) {
                        missingRequiredAttribute ( en, "lsb" );
                    } else {
                        l = Integer.parseInt ( lsb );
                    }
                    hmtxEntries.set ( gid, new int[] { w, l } );
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "tableVersion" ) ) {
                String[] pn1 = new String[] { null, "cmap" };
                String[] pn2 = new String[] { null, "head" };
                String[][] pnx = new String[][] { pn1, pn2 };
                if ( isParent ( pn1 ) ) {               // child of cmap
                    String version = attrs.getValue ( "version" );
                    if ( version == null ) {
                        missingRequiredAttribute ( en, "version" );
                    }
                } else if ( isParent ( pn2 ) ) {        // child of head
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pnx );
                }
            } else if ( en[1].equals ( "ttFont" ) ) {
                String[] pn = new String[] { null, null };
                if ( isParent ( pn ) ) {
                    String sfntVersion = attrs.getValue ( "sfntVersion" );
                    if ( sfntVersion == null ) {
                        missingRequiredAttribute ( en, "sfntVersion" );
                    }
                    String ttLibVersion = attrs.getValue ( "ttLibVersion" );
                    if ( ttLibVersion == null ) {
                        missingRequiredAttribute ( en, "ttLibVersion" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), null );
                }
            } else if ( en[1].equals ( "unitsPerEm" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    int v = -1;
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    } else {
                        v = Integer.parseInt ( value );
                    }
                    assert upem == -1;
                    upem = v;
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "xMax" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "xMin" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "yMax" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else if ( en[1].equals ( "yMin" ) ) {
                String[] pn = new String[] { null, "head" };
                if ( isParent ( pn ) ) {
                    String value = attrs.getValue ( "value" );
                    if ( value == null ) {
                        missingRequiredAttribute ( en, "value" );
                    }
                } else {
                    notPermittedInElementContext ( en, getParent(), pn );
                }
            } else {
                unsupportedElement ( en );
            }
            elements.push ( en );
        }
        @Override
        public void endElement ( String uri, String localName, String qName ) throws SAXException {
            if ( elements.empty() ) {
                throw new SAXException ( "element stack is unbalanced, no elements on stack!" );
            }
            String[] enParent = elements.peek();
            if ( enParent == null ) {
                throw new SAXException ( "element stack is empty, elements are not balanced" );
            }
            String[] en = makeExpandedName ( uri, localName, qName );
            if ( ! sameExpandedName ( enParent, en ) ) {
                throw new SAXException ( "element stack is unbalanced, expanded name mismatch" );
            }
            if ( en[0] != null ) {
                unsupportedElement ( en );
            } else if ( isAnchorElement ( en[1] ) ) {
                if ( xCoord == Integer.MIN_VALUE ) {
                    missingParameter ( en, "x coordinate" );
                } else if ( yCoord == Integer.MIN_VALUE ) {
                    missingParameter ( en, "y coordinate" );
                } else {
                    if ( en[1].equals ( "EntryAnchor" ) ) {
                        if ( anchors.size() > 0 ) {
                            duplicateParameter ( en, "entry anchor" );
                        }
                    } else if ( en[1].equals ( "ExitAnchor" ) ) {
                        if ( anchors.size() > 1 ) {
                            duplicateParameter ( en, "exit anchor" );
                        } else if ( anchors.size() == 0 ) {
                            anchors.add ( null );
                        }
                    }
                    anchors.add ( new GlyphPositioningTable.Anchor ( xCoord, yCoord ) );
                    xCoord = yCoord = Integer.MIN_VALUE;
                }
            } else if ( en[1].equals ( "AlternateSet" ) ) {
                subtableEntries.add ( extractAlternates() );
            } else if ( en[1].equals ( "AlternateSubst" ) ) {
                if ( ! sortEntries ( coverageEntries, subtableEntries ) ) {
                    mismatchedEntries ( en, coverageEntries.size(), subtableEntries.size() );
                }
                addGSUBSubtable ( GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_ALTERNATE, extractCoverage() );
            } else if ( en[1].equals ( "BacktrackCoverage" ) ) {
                String ck = makeCoverageKey ( "bk", ctIndex );
                if ( coverages.containsKey ( ck ) ) {
                    duplicateCoverageIndex ( en, ctIndex );
                } else {
                    coverages.put ( ck, extractCoverage() );
                }
            } else if ( en[1].equals ( "BaseCoverage" ) ) {
                coverages.put ( "base", extractCoverage() );
            } else if ( en[1].equals ( "BaseRecord" ) ) {
                baseOrMarkAnchors.add ( extractAnchors() );
            } else if ( en[1].equals ( "ChainContextPos" ) || en[1].equals ( "ChainContextSubst" ) ) {
                GlyphCoverageTable coverage = null;
                if ( stFormat == 3 ) {
                    GlyphCoverageTable igca[] = getCoveragesWithPrefix ( "in" );
                    GlyphCoverageTable bgca[] = getCoveragesWithPrefix ( "bk" );
                    GlyphCoverageTable lgca[] = getCoveragesWithPrefix ( "la" );
                    if ( ( igca.length == 0 ) || hasMissingCoverage ( igca ) ) {
                        missingCoverage ( en, "input", igca.length );
                    } else if ( hasMissingCoverage ( bgca ) ) {
                        missingCoverage ( en, "backtrack", bgca.length );
                    } else if ( hasMissingCoverage ( lgca ) ) {
                        missingCoverage ( en, "lookahead", lgca.length );
                    } else {
                        GlyphTable.Rule r = new GlyphTable.ChainedCoverageSequenceRule ( extractRuleLookups(), igca.length, igca, bgca, lgca );
                        GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet ( new GlyphTable.Rule[] {r} );
                        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[] {rs};
                        coverage = igca [ 0 ];
                        subtableEntries.add ( rsa );
                    }
                } else {
                    unsupportedFormat ( en, stFormat );
                }
                if ( en[1].equals ( "ChainContextPos" ) ) {
                    addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_CHAINED_CONTEXTUAL, coverage );
                } else if ( en[1].equals ( "ChainContextSubst" ) ) {
                    addGSUBSubtable ( GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL, coverage );
                }
            } else if ( en[1].equals ( "ComponentRecord" ) ) {
                components.add ( extractAnchors() );
            } else if ( en[1].equals ( "Coverage" ) ) {
                coverages.put ( "main", extractCoverage() );
            } else if ( en[1].equals ( "DefaultLangSys" ) || en[1].equals ( "LangSysRecord" ) ) {
                if ( languageTag == null ) {
                    missingTag ( en, "language" );
                } else if ( languages.containsKey ( languageTag ) ) {
                    duplicateTag ( en, "language", languageTag );
                } else {
                    languages.put ( languageTag, extractLanguageFeatures() );
                    languageTag = null;
                }
            } else if ( en[1].equals ( "CursivePos" ) ) {
                GlyphCoverageTable ct = coverages.get ( "main" );
                if ( ct == null ) {
                    missingParameter ( en, "coverages" );
                } else if ( stFormat == 1 ) {
                    subtableEntries.add ( extractAttachmentAnchors() );
                } else {
                    unsupportedFormat ( en, stFormat );
                }
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_CURSIVE, ct );
            } else if ( en[1].equals ( "EntryExitRecord" ) ) {
                int na = anchors.size();
                if ( na == 0 ) {
                    missingParameter ( en, "entry or exit anchor" );
                } else if ( na == 1 ) {
                    anchors.add ( null );
                } else if ( na > 2 ) {
                    duplicateParameter ( en, "entry or exit anchor" );
                }
                attachmentAnchors.add ( extractAnchors() );
            } else if ( en[1].equals ( "BaseRecord" ) ) {
                baseOrMarkAnchors.add ( extractAnchors() );
            } else if ( en[1].equals ( "FeatureRecord" ) ) {
                if ( flIndex != flSequence ) {
                    mismatchedIndex ( en, "feature", flIndex, flSequence );
                } else if ( featureTag == null ) {
                    missingTag ( en, "feature" );
                } else {
                    String fid = makeFeatureId ( flIndex );
                    features.put ( fid, extractFeature() );
                    nextFeature();
                }
            } else if ( en[1].equals ( "GDEF" ) ) {
                if ( subtables.size() > 0 ) {
                    gdef = new GlyphDefinitionTable ( subtables );
                }
                clearTable();
            } else if ( en[1].equals ( "GPOS" ) ) {
                if ( subtables.size() > 0 ) {
                    gpos = new GlyphPositioningTable ( gdef, extractLookups(), subtables );
                }
                clearTable();
            } else if ( en[1].equals ( "GSUB" ) ) {
                if ( subtables.size() > 0 ) {
                    gsub = new GlyphSubstitutionTable ( gdef, extractLookups(), subtables );
                }
                clearTable();
            } else if ( en[1].equals ( "GlyphClassDef" ) ) {
                GlyphMappingTable mapping = extractClassDefMapping ( glyphClasses, stFormat, true );
                addGDEFSubtable ( GlyphDefinitionTable.GDEF_LOOKUP_TYPE_GLYPH_CLASS, mapping );
            } else if ( en[1].equals ( "InputCoverage" ) ) {
                String ck = makeCoverageKey ( "in", ctIndex );
                if ( coverages.containsKey ( ck ) ) {
                    duplicateCoverageIndex ( en, ctIndex );
                } else {
                    coverages.put ( ck, extractCoverage() );
                }
            } else if ( en[1].equals ( "LigatureAttach" ) ) {
                ligatureAnchors.add ( extractComponents() );
            } else if ( en[1].equals ( "LigatureCoverage" ) ) {
                coverages.put ( "liga", extractCoverage() );
            } else if ( en[1].equals ( "LigatureSet" ) ) {
                subtableEntries.add ( extractLigatures() );
            } else if ( en[1].equals ( "LigatureSubst" ) ) {
                if ( ! sortEntries ( coverageEntries, subtableEntries ) ) {
                    mismatchedEntries ( en, coverageEntries.size(), subtableEntries.size() );
                }
                GlyphCoverageTable coverage = extractCoverage();
                addGSUBSubtable ( GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_LIGATURE, coverage );
            } else if ( en[1].equals ( "LookAheadCoverage" ) ) {
                String ck = makeCoverageKey ( "la", ctIndex );
                if ( coverages.containsKey ( ck ) ) {
                    duplicateCoverageIndex ( en, ctIndex );
                } else {
                    coverages.put ( ck, extractCoverage() );
                }
            } else if ( en[1].equals ( "Lookup" ) ) {
                if ( ltIndex != ltSequence ) {
                    mismatchedIndex ( en, "lookup", ltIndex, ltSequence );
                } else {
                    nextLookup();
                }
            } else if ( en[1].equals ( "MarkAttachClassDef" ) ) {
                GlyphMappingTable mapping = extractClassDefMapping ( glyphClasses, stFormat, true );
                addGDEFSubtable ( GlyphDefinitionTable.GDEF_LOOKUP_TYPE_MARK_ATTACHMENT, mapping );
            } else if ( en[1].equals ( "MarkCoverage" ) ) {
                coverages.put ( "mark", extractCoverage() );
            } else if ( en[1].equals ( "Mark1Coverage" ) ) {
                coverages.put ( "mrk1", extractCoverage() );
            } else if ( en[1].equals ( "Mark2Coverage" ) ) {
                coverages.put ( "mrk2", extractCoverage() );
            } else if ( en[1].equals ( "MarkBasePos" ) ) {
                GlyphCoverageTable mct = coverages.get ( "mark" );
                GlyphCoverageTable bct = coverages.get ( "base" );
                if ( mct == null ) {
                    missingParameter ( en, "mark coverages" );
                } else if ( bct == null ) {
                    missingParameter ( en, "base coverages" );
                } else if ( stFormat == 1 ) {
                    MarkAnchor[] maa = extractMarkAnchors();
                    Anchor[][] bam = extractBaseOrMarkAnchors();
                    subtableEntries.add ( bct );
                    subtableEntries.add ( computeClassCount ( bam ) );
                    subtableEntries.add ( maa );
                    subtableEntries.add ( bam );
                } else {
                    unsupportedFormat ( en, stFormat );
                }
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_BASE, mct );
            } else if ( en[1].equals ( "MarkLigPos" ) ) {
                GlyphCoverageTable mct = coverages.get ( "mark" );
                GlyphCoverageTable lct = coverages.get ( "liga" );
                if ( mct == null ) {
                    missingParameter ( en, "mark coverages" );
                } else if ( lct == null ) {
                    missingParameter ( en, "ligature coverages" );
                } else if ( stFormat == 1 ) {
                    MarkAnchor[] maa = extractMarkAnchors();
                    Anchor[][][] lam = extractLigatureAnchors();
                    subtableEntries.add ( lct );
                    subtableEntries.add ( computeLigaturesClassCount ( lam ) );
                    subtableEntries.add ( computeLigaturesComponentCount ( lam ) );
                    subtableEntries.add ( maa );
                    subtableEntries.add ( lam );
                } else {
                    unsupportedFormat ( en, stFormat );
                }
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE, mct );
            } else if ( en[1].equals ( "MarkMarkPos" ) ) {
                GlyphCoverageTable mct1 = coverages.get ( "mrk1" );
                GlyphCoverageTable mct2 = coverages.get ( "mrk2" );
                if ( mct1 == null ) {
                    missingParameter ( en, "mark coverages 1" );
                } else if ( mct2 == null ) {
                    missingParameter ( en, "mark coverages 2" );
                } else if ( stFormat == 1 ) {
                    MarkAnchor[] maa = extractMarkAnchors();
                    Anchor[][] mam = extractBaseOrMarkAnchors();
                    subtableEntries.add ( mct2 );
                    subtableEntries.add ( computeClassCount ( mam ) );
                    subtableEntries.add ( maa );
                    subtableEntries.add ( mam );
                } else {
                    unsupportedFormat ( en, stFormat );
                }
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_MARK, mct1 );
            } else if ( en[1].equals ( "MarkRecord" ) ) {
                if ( markClass == -1 ) {
                    missingParameter ( en, "mark class" );
                } else if ( anchors.size() == 0 ) {
                    missingParameter ( en, "mark anchor" );
                } else if ( anchors.size() > 1 ) {
                    duplicateParameter ( en, "mark anchor" );
                } else {
                    markAnchors.add ( new GlyphPositioningTable.MarkAnchor ( markClass, anchors.get(0) ) );
                    markClass = -1;
                    anchors.clear();
                }
            } else if ( en[1].equals ( "Mark2Record" ) ) {
                baseOrMarkAnchors.add ( extractAnchors() );
            } else if ( en[1].equals ( "MultipleSubst" ) ) {
                GlyphCoverageTable coverage = coverages.get ( "main" );
                addGSUBSubtable ( GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_MULTIPLE, coverage, extractSequenceEntries() );
            } else if ( en[1].equals ( "PairPos" ) ) {
                assertSubtableEntriesClear();
                if ( stFormat == 1 ) {
                    if ( pairSets.size() == 0 ) {
                        missingParameter ( en, "pair set" );
                    } else {
                        subtableEntries.add ( extractPairSets() );
                    }
                } else if ( stFormat == 2 ) {
                    unsupportedFormat ( en, stFormat );
                }
                GlyphCoverageTable coverage = coverages.get ( "main" );
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_PAIR, coverage );
                vf1 = vf2 = -1; psIndex = -1;
            } else if ( en[1].equals ( "PairSet" ) ) {
                if ( psIndex != pairSets.size() ) {
                    invalidIndex ( en, psIndex, pairSets.size() );
                } else {
                    pairSets.add ( extractPairs() );
                }
            } else if ( en[1].equals ( "PairValueRecord" ) ) {
                if ( g2 == -1 ) {
                    missingParameter ( en, "second glyph" );
                } else if ( ( v1 == null ) && ( v2 == null ) ) {
                    missingParameter ( en, "first or second value" );
                } else {
                    pairs.add ( new PairValues ( g2, v1, v2 ) );
                    clearPair();
                }
            } else if ( en[1].equals ( "PosLookupRecord" ) || en[1].equals ( "SubstLookupRecord" ) ) {
                if ( rlSequence < 0 ) {
                    missingParameter ( en, "sequence index" );
                } else if ( rlLookup < 0 ) {
                    missingParameter ( en, "lookup index" );
                } else {
                    ruleLookups.add ( new GlyphTable.RuleLookup ( rlSequence, rlLookup ) );
                    rlSequence = rlLookup = -1;
                }
            } else if ( en[1].equals ( "Script" ) ) {
                if ( scriptTag == null ) {
                    missingTag ( en, "script" );
                } else if ( scripts.containsKey ( scriptTag ) ) {
                    duplicateTag ( en, "script", scriptTag );
                } else {
                    scripts.put ( scriptTag, extractLanguages() );
                    scriptTag = null;
                }
            } else if ( en[1].equals ( "Sequence" ) ) {
                subtableEntries.add ( extractSubstitutes() );
            } else if ( en[1].equals ( "SinglePos" ) ) {
                int nv = subtableEntries.size();
                if ( stFormat == 1 ) {
                    if ( nv < 0 ) {
                        missingParameter ( en, "value"  );
                    } else if ( nv > 1 ) {
                        duplicateParameter ( en, "value" );
                    }
                } else if ( stFormat == 2 ) {
                    GlyphPositioningTable.Value[] pva = (GlyphPositioningTable.Value[]) subtableEntries.toArray ( new GlyphPositioningTable.Value [ nv ] );
                    subtableEntries.clear();
                    subtableEntries.add ( pva );
                }
                GlyphCoverageTable coverage = coverages.get ( "main" );
                addGPOSSubtable ( GlyphPositioningTable.GPOS_LOOKUP_TYPE_SINGLE, coverage );
                vf1 = -1;
            } else if ( en[1].equals ( "SingleSubst" ) ) {
                if ( ! sortEntries ( coverageEntries, subtableEntries ) ) {
                    mismatchedEntries ( en, coverageEntries.size(), subtableEntries.size() );
                }
                GlyphCoverageTable coverage = extractCoverage();
                addGSUBSubtable ( GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_SINGLE, coverage );
            } else if ( en[1].equals ( "cmap" ) ) {
                cmap = getCMAP();
                gmap = getGMAP();
                cmapEntries.clear();
            } else if ( en[1].equals ( "cmap_format_4" ) ) {
                cmPlatform = cmEncoding = cmLanguage = -1;
            } else if ( en[1].equals ( "hmtx" ) ) {
                hmtx = getHMTX();
                hmtxEntries.clear();
            } else if ( en[1].equals ( "ttFont" ) ) {
                if ( cmap == null ) {
                    missingParameter ( en, "cmap" );
                }
                if ( hmtx == null ) {
                    missingParameter ( en, "hmtx" );
                }
            }
            elements.pop();
        }
        @Override
        public void characters ( char[] chars, int start, int length ) {
        }
        private String[] getParent() {
            if ( ! elements.empty() ) {
                return elements.peek();
            } else {
                return new String[] { null, null };
            }
        }
        private boolean isParent ( Object enx ) {
            if ( enx instanceof String[][] ) {
                for ( String[] en : (String[][]) enx ) {
                    if ( isParent ( en ) ) {
                        return true;
                    }
                }
                return false;
            } else if ( enx instanceof String[] ) {
                String[] en = (String[]) enx;
                if ( ! elements.empty() ) {
                    String[] pn = elements.peek();
                    return ( pn != null ) && sameExpandedName ( en, pn );
                } else if ( ( en[0] == null ) && ( en[1] == null ) ) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        private boolean isAnchorElement ( String ln ) {
            if ( ln.equals ( "BaseAnchor" ) ) {
                return true;
            } else if ( ln.equals ( "EntryAnchor" ) ) {
                return true;
            } else if ( ln.equals ( "ExitAnchor" ) ) {
                return true;
            } else if ( ln.equals ( "LigatureAnchor" ) ) {
                return true;
            } else if ( ln.equals ( "MarkAnchor" ) ) {
                return true;
            } else if ( ln.equals ( "Mark2Anchor" ) ) {
                return true;
            } else {
                return false;
            }
        }
        private Map<Integer,Integer> getCMAP() {
            Map<Integer,Integer> cmap = new TreeMap();
            for ( int[] cme : cmapEntries ) {
                Integer c = Integer.valueOf ( cme[0] );
                Integer g = Integer.valueOf ( cme[1] );
                cmap.put ( c, g );
            }
            return cmap;
        }
        private Map<Integer,Integer> getGMAP() {
            Map<Integer,Integer> gmap = new TreeMap();
            for ( int[] cme : cmapEntries ) {
                Integer c = Integer.valueOf ( cme[0] );
                Integer g = Integer.valueOf ( cme[1] );
                gmap.put ( g, c );
            }
            return gmap;
        }
        private int[][] getHMTX() {
            int ne = hmtxEntries.size();
            int[][] hmtx = new int [ ne ] [ 2 ];
            for ( int i = 0; i < ne; i++ ) {
                int[] ea = hmtxEntries.get(i);
                if ( ea != null ) {
                    hmtx [ i ] [ 0 ] = ea[0];
                    hmtx [ i ] [ 1 ] = ea[1];
                }
            }
            return hmtx;
        }
        private GlyphClassTable extractClassDefMapping ( Map<String,Integer> glyphClasses, int format, boolean clearSourceMap ) {
            GlyphClassTable ct;
            if ( format == 1 ) {
                ct = extractClassDefMapping1 ( extractClassMappings ( glyphClasses, clearSourceMap ) );
            } else if ( format == 2 ) {
                ct = extractClassDefMapping2 ( extractClassMappings ( glyphClasses, clearSourceMap ) );
            } else {
                ct = null;
            }
            return ct;
        }
        private GlyphClassTable extractClassDefMapping1 ( int[][] cma ) {
            List entries = new ArrayList<Integer>();
            int s = -1;
            int l = -1;
            Integer zero = Integer.valueOf(0);
            for ( int[] m : cma ) {
                int g = m[0];
                int c = m[1];
                if ( s < 0 ) {
                    s = g;
                    l = g - 1;
                    entries.add ( Integer.valueOf ( s ) );
                }
                while ( g > ( l + 1 ) ) {
                    entries.add ( zero );
                    l++;
                }
                assert l == ( g - 1 );
                entries.add ( Integer.valueOf ( c ) );
                l = g;
            }
            return GlyphClassTable.createClassTable ( entries );
        }
        private GlyphClassTable extractClassDefMapping2 ( int[][] cma ) {
            List entries = new ArrayList<Integer>();
            int s = -1;
            int e =  s;
            int l = -1;
            for ( int[] m : cma ) {
                int g = m[0];
                int c = m[1];
                if ( c != l ) {
                    if ( s >= 0 ) {
                        entries.add ( new GlyphClassTable.MappingRange ( s, e, l ) );
                    }
                    s = e = g;
                } else {
                    e = g;
                }
                l = c;
            }
            return GlyphClassTable.createClassTable ( entries );
        }
        private int[][] extractClassMappings ( Map<String,Integer> glyphClasses, boolean clearSourceMap ) {
            int nc = glyphClasses.size();
            int i = 0;
            int[][] cma = new int [ nc ] [ 2 ];
            for ( Map.Entry<String,Integer> e : glyphClasses.entrySet() ) {
                Integer gid = glyphIds.get ( e.getKey() );
                assert gid != null;
                int[] m = cma [ i ];
                m [ 0 ] = (int) gid;
                m [ 1 ] = (int) e.getValue();
                i++;
            }
            if ( clearSourceMap ) {
                glyphClasses.clear();
            }
            return sortClassMappings ( cma );
        }
        private int[][] sortClassMappings ( int[][] cma ) {
            Arrays.sort ( cma, new Comparator<int[]>() {
                    public int compare ( int[] m1, int[] m2 ) {
                        assert m1.length > 0;
                        assert m2.length > 0;
                        if ( m1[0] < m2[0] ) {
                            return -1;
                        } else if ( m1[0] > m2[0] ) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            );
            return cma;
        }
        // sort coverage entries and subtable entries together
        private boolean sortEntries ( List cel, List sel ) {
            assert cel != null;
            assert sel != null;
            if ( cel.size() == sel.size() ) {
                int np = cel.size();
                Object[][] pa = new Object [ np ] [ 2 ];
                for ( int i = 0; i < np; i++ ) {
                    pa [ i ] [ 0 ] = cel.get ( i );
                    pa [ i ] [ 1 ] = sel.get ( i );
                }
                Arrays.sort ( pa, new Comparator<Object[]>() {
                        public int compare ( Object[] p1, Object[] p2 ) {
                            assert p1.length == 2;
                            assert p2.length == 2;
                            int c1 = (Integer) p1[0];
                            int c2 = (Integer) p2[0];
                            if ( c1 < c2 ) {
                                return -1;
                            } else if ( c1 > c2 ) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                );
                cel.clear();
                sel.clear();
                for ( int i = 0; i < np; i++ ) {
                    cel.add ( pa [ i ] [ 0 ] );
                    sel.add ( pa [ i ] [ 1 ] );
                }
                assert cel.size() == sel.size();
                return true;
            } else {
                return false;
            }
        }
        private String makeCoverageKey ( String prefix, int index ) {
            assert prefix != null;
            assert prefix.length() == 2;
            assert index < 100;
            return prefix + CharUtilities.padLeft ( Integer.toString ( index, 10 ), 2, '0' );
        }
        private List extractCoverageEntries() {
            List entries = new ArrayList<Integer> ( coverageEntries );
            clearCoverage();
            return entries;
        }
        private void clearCoverageEntries() {
            coverageEntries.clear();
            ctFormat = -1;
            ctIndex = -1;
        }
        private void assertCoverageEntriesClear() {
            assert coverageEntries.size() == 0;
        }
        private GlyphCoverageTable extractCoverage() {
            assert ( ctFormat == 1 ) || ( ctFormat == 2 );
            assert ctIndex >= 0;
            GlyphCoverageTable coverage = GlyphCoverageTable.createCoverageTable ( extractCoverageEntries() );
            clearCoverage();
            return coverage;
        }
        private void clearCoverages() {
            coverages.clear();
        }
        private void assertCoverageClear() {
            assert ctFormat == -1;
            assert ctIndex == -1;
            assertCoverageEntriesClear();
        }
        private void clearCoverage() {
            ctFormat = -1;
            ctIndex = -1;
            clearCoverageEntries();
        }
        private void assertCoveragesClear() {
            assert coverages.size() == 0;
        }
        private GlyphCoverageTable[] getCoveragesWithPrefix ( String prefix ) {
            assert prefix != null;
            int prefixLength = prefix.length();
            Set<String> keys = coverages.keySet();
            int mi = -1; // maximum coverage table index
            for ( String k : keys ) {
                if ( k.startsWith ( prefix ) ) {
                    int i = Integer.parseInt ( k.substring ( prefixLength ) );
                    if ( i > mi ) {
                        mi = i;
                    }
                }
            }
            GlyphCoverageTable[] gca = new GlyphCoverageTable [ mi + 1 ];
            for ( String k : keys ) {
                if ( k.startsWith ( prefix ) ) {
                    int i = Integer.parseInt ( k.substring ( prefixLength ) );
                    if ( i >= 0 ) {
                        gca [ i ] = coverages.get ( k );
                    }
                }
            }
            return gca;
        }
        private boolean hasMissingCoverage ( GlyphCoverageTable[] gca ) {
            assert gca != null;
            int nc = 0;
            for ( int i = 0, n = gca.length; i < n; i++ ) {
                if ( gca [ i ] != null ) {
                    nc++;
                }
            }
            return nc != gca.length;
        }
        private String makeFeatureId ( int fid ) {
            assert fid >= 0;
            return "f" + fid;
        }
        private String makeLookupId ( int lid ) {
            assert lid >= 0;
            return "lu" + lid;
        }
        private void clearScripts() {
            scripts.clear();
        }
        private List<String> extractLanguageFeatures() {
            List<String> lfl = new ArrayList<String>(languageFeatures);
            clearLanguageFeatures();
            return lfl;
        }
        private void assertLanguageFeaturesClear() {
            assert languageFeatures.size() == 0;
        }
        private void clearLanguageFeatures() {
            languageFeatures.clear();
        }
        private Map<String,List<String>> extractLanguages() {
            Map<String,List<String>> lm = new HashMap ( languages );
            clearLanguages();
            return lm;
        }
        private void clearLanguages() {
            languages.clear();
        }
        private void assertFeatureLookupsClear() {
            assert featureLookups.size() == 0;
        }
        private List extractFeatureLookups() {
            List lookups = new ArrayList<String> ( featureLookups );
            clearFeatureLookups();
            return lookups;
        }
        private void clearFeatureLookups() {
            featureLookups.clear();
        }
        private void assertFeatureClear() {
            assert flIndex == -1;
            assert featureTag == null;
            assertFeatureLookupsClear();
        }
        private Object[] extractFeature() {
            Object[] fa = new Object [ 2 ];
            fa[0] = featureTag;
            fa[1] = extractFeatureLookups();
            clearFeature();
            return fa;
        }
        private void clearFeature() {
            flIndex = -1;
            featureTag = null;
            clearFeatureLookups();
        }
        private void nextFeature() {
            flSequence++;
        }
        private void clearFeatures() {
            features.clear();
        }
        private void clearSubtableInLookup() {
            stFormat = 0;
            clearCoverages();
        }
        private void clearSubtablesInLookup() {
            clearSubtableInLookup();
            stSequence = 0;
        }
        private void clearSubtablesInTable() {
            clearSubtablesInLookup();
            subtables.clear();
        }
        private void nextSubtableInLookup() {
            stSequence++;
            clearSubtableInLookup();
        }
        private void assertLookupClear() {
            assert ltIndex == -1;
            assert ltFlags == 0;
        }
        private void clearLookup() {
            ltIndex = -1;
            ltFlags = 0;
            clearSubtablesInLookup();
        }
        private Map<GlyphTable.LookupSpec,List<String>> extractLookups() {
            Map<GlyphTable.LookupSpec,List<String>> lookups = new LinkedHashMap<GlyphTable.LookupSpec,List<String>>();
            for ( String st : scripts.keySet() ) {
                Map<String,List<String>> lm = scripts.get ( st );
                if ( lm != null ) {
                    for ( String lt : lm.keySet() ) {
                        List<String> fids = lm.get ( lt );
                        if ( fids != null ) {
                            for ( String fid : fids ) {
                                if ( fid != null ) {
                                    Object[] fa = features.get ( fid );
                                    if ( fa != null ) {
                                        assert fa.length == 2;
                                        String ft = (String) fa[0];
                                        List<String> lids = (List<String>) fa[1];
                                        if ( ( lids != null ) && ( lids.size() > 0 ) ) {
                                            GlyphTable.LookupSpec ls = new GlyphTable.LookupSpec ( st, lt, ft );
                                            lookups.put ( ls, lids );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            clearScripts();
            clearLanguages();
            clearFeatures();
            return lookups;
        }
        private void clearLookups() {
            clearLookup();
            clearSubtablesInTable();
            ltSequence = 0;
            flSequence = 0;
        }
        private void nextLookup() {
            ltSequence++;
            clearLookup();
        }
        private void clearTable() {
            clearLookups();
        }
        private void assertSubtableClear() {
            assert stFormat == 0;
            assertCoverageEntriesClear();
        }
        private void assertSubtablesClear() {
            assertSubtableClear();
            assert subtables.size() == 0;
        }
        private void clearSubtableEntries() {
            subtableEntries.clear();
        }
        private void assertSubtableEntriesClear() {
            assert subtableEntries.size() == 0;
        }
        private List extractSubtableEntries() {
            List entries = new ArrayList ( subtableEntries );
            clearSubtableEntries();
            return entries;
        }
        private int[] extractAlternates() {
            int[] aa = new int [ alternates.size() ];
            int i = 0;
            for ( Integer a : alternates ) {
                aa[i++] = (int) a;
            }
            clearAlternates();
            return aa;
        }
        private void clearAlternates() {
            alternates.clear();
        }
        private LigatureSet extractLigatures() {
            LigatureSet ls = new LigatureSet ( ligatures );
            clearLigatures();
            return ls;
        }
        private void clearLigatures() {
            ligatures.clear();
        }
        private int[] extractSubstitutes() {
            int[] aa = new int [ substitutes.size() ];
            int i = 0;
            for ( Integer a : substitutes ) {
                aa[i++] = (int) a;
            }
            clearSubstitutes();
            return aa;
        }
        private void clearSubstitutes() {
            substitutes.clear();
        }
        private List extractSequenceEntries() {
            List sequences = extractSubtableEntries();
            int[][] sa = new int [ sequences.size() ] [];
            int i = 0;
            for ( Object s : sequences ) {
                if ( s instanceof int[] ) {
                    sa[i++] = (int[]) s;
                }
            }
            List entries = new ArrayList();
            entries.add ( sa );
            return entries;
        }
        private RuleLookup[] extractRuleLookups() {
            RuleLookup[] lookups = (RuleLookup[]) ruleLookups.toArray ( new RuleLookup [ ruleLookups.size() ] );
            clearRuleLookups();
            return lookups;
        }
        private void clearRuleLookups() {
            ruleLookups.clear();
        }
        private GlyphPositioningTable.Value parseValue ( String[] en, Attributes attrs, int format ) throws SAXException {
            String xPlacement = attrs.getValue ( "XPlacement" );
            int xp = 0;
            if ( xPlacement != null ) {
                xp = Integer.parseInt ( xPlacement );
            } else if ( ( format & GlyphPositioningTable.Value.X_PLACEMENT ) != 0 ) {
                missingParameter ( en, "xPlacement" );
            }
            String yPlacement = attrs.getValue ( "YPlacement" );
            int yp = 0;
            if ( yPlacement != null ) {
                yp = Integer.parseInt ( yPlacement );
            } else if ( ( format & GlyphPositioningTable.Value.Y_PLACEMENT ) != 0 ) {
                missingParameter ( en, "yPlacement" );
            }
            String xAdvance = attrs.getValue ( "XAdvance" );
            int xa = 0;
            if ( xAdvance != null ) {
                xa = Integer.parseInt ( xAdvance );
            } else if ( ( format & GlyphPositioningTable.Value.X_ADVANCE ) != 0 ) {
                missingParameter ( en, "xAdvance" );
            }
            String yAdvance = attrs.getValue ( "YAdvance" );
            int ya = 0;;
            if ( yAdvance != null ) {
                ya = Integer.parseInt ( yAdvance );
            } else if ( ( format & GlyphPositioningTable.Value.Y_ADVANCE ) != 0 ) {
                missingParameter ( en, "yAdvance" );
            }
            return new GlyphPositioningTable.Value ( xp, yp, xa, ya, null, null, null, null );
        }
        private void assertPairClear() {
            assert g2 == -1;
            assert v1 == null;
            assert v2 == null;
        }
        private void clearPair() {
            g2 = -1;
            v1 = null;
            v2 = null;
        }
        private void assertPairsClear() {
            assert pairs.size() == 0;
        }
        private void clearPairs() {
            pairs.clear();
            psIndex = -1;
        }
        private PairValues[] extractPairs() {
            PairValues[] pva = (PairValues[]) pairs.toArray ( new PairValues [ pairs.size() ] );
            clearPairs();
            return pva;
        }
        private void assertPairSetsClear() {
            assert pairSets.size() == 0;
        }
        private void clearPairSets() {
            pairSets.clear();
        }
        private PairValues[][] extractPairSets() {
            PairValues[][] pvm = (PairValues[][]) pairSets.toArray ( new PairValues [ pairSets.size() ][] );
            clearPairSets();
            return pvm;
        }
        private Anchor[] extractAnchors() {
            Anchor[] aa = (Anchor[]) anchors.toArray ( new Anchor [ anchors.size() ] );
            anchors.clear();
            return aa;
        }
        private MarkAnchor[] extractMarkAnchors() {
            MarkAnchor[] maa = new MarkAnchor [ markAnchors.size() ];
            maa = (MarkAnchor[]) markAnchors.toArray ( new MarkAnchor [ maa.length ] );
            markAnchors.clear();
            return maa;
        }
        private Anchor[][] extractBaseOrMarkAnchors() {
            int na = baseOrMarkAnchors.size();
            int ncMax = 0;
            for ( Anchor[] aa : baseOrMarkAnchors ) {
                if ( aa != null ) {
                    int nc = aa.length;
                    if ( nc > ncMax ) {
                        ncMax = nc;
                    }
                }
            }
            Anchor[][] am = new Anchor [ na ][ ncMax ];
            for ( int i = 0; i < na; i++ ) {
                Anchor[] aa = baseOrMarkAnchors.get(i);
                if ( aa != null ) {
                    for ( int j = 0; j < ncMax; j++ ) {
                        if ( j < aa.length ) {
                            am [ i ] [ j ] = aa [ j ];
                        }
                    }
                }
            }
            baseOrMarkAnchors.clear();
            return am;
        }
        private Integer computeClassCount ( Anchor[][] am ) {
            int ncMax = 0;
            for ( int i = 0, n = am.length; i < n; i++ ) {
                Anchor[] aa = am [ i ];
                if ( aa != null ) {
                    int nc = aa.length;
                    if ( nc > ncMax ) {
                        ncMax = nc;
                    }
                }
            }
            return Integer.valueOf ( ncMax );
        }
        private Anchor[][] extractComponents() {
            Anchor[][] cam = new Anchor [ components.size() ][];
            cam = (Anchor[][]) components.toArray ( new Anchor [ cam.length ][] );
            components.clear();
            return cam;
        }
        private Anchor[][][] extractLigatureAnchors() {
            int na = ligatureAnchors.size();
            int ncMax = 0;
            int nxMax = 0;
            for ( Anchor[][] cm : ligatureAnchors ) {
                if ( cm != null ) {
                    int nx = cm.length;
                    if ( nx > nxMax ) {
                        nxMax = nx;
                    }
                    for ( Anchor[] aa : cm ) {
                        if ( aa != null ) {
                            int nc = aa.length;
                            if ( nc > ncMax ) {
                                ncMax = nc;
                            }
                        }
                    }

                }
            }
            Anchor[][][] lam = new Anchor [ na ] [ nxMax ] [ ncMax ];
            for ( int i = 0; i < na; i++ ) {
                Anchor[][] cm = ligatureAnchors.get(i);
                if ( cm != null ) {
                    for ( int j = 0; j < nxMax; j++ ) {
                        if ( j < cm.length ) {
                            Anchor[] aa = cm [ j ];
                            if ( aa != null ) {
                                for ( int k = 0; k < ncMax; k++ ) {
                                    if ( k < aa.length ) {
                                        lam [ i ] [ j ] [ k ] = aa [ k ];
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ligatureAnchors.clear();
            return lam;
        }
        private Integer computeLigaturesClassCount ( Anchor[][][] lam ) {
            int ncMax = 0;
            if ( lam != null ) {
                for ( Anchor[][] cm : lam ) {
                    if ( cm != null ) {
                        for ( Anchor[] aa : cm ) {
                            if ( aa != null ) {
                                int nc = aa.length;;
                                if ( nc > ncMax ) {
                                    ncMax = nc;
                                }
                            }
                        }
                    }
                }
            }
            return Integer.valueOf ( ncMax );
        }
        private Integer computeLigaturesComponentCount ( Anchor[][][] lam ) {
            int nxMax = 0;
            if ( lam != null ) {
                for ( Anchor[][] cm : lam ) {
                    if ( cm != null ) {
                        int nx = cm.length;;
                        if ( nx > nxMax ) {
                            nxMax = nx;
                        }
                    }
                }
            }
            return Integer.valueOf ( nxMax );
        }
        private Anchor[] extractAttachmentAnchors() {
            int na = attachmentAnchors.size();
            Anchor[] aa = new Anchor [ na * 2 ];
            for ( int i = 0; i < na; i++ ) {
                Anchor[] ea = attachmentAnchors.get(i);
                int ne = ea.length;
                if ( ne > 0 ) {
                    aa [ ( i * 2 ) + 0 ] = ea[0];
                }
                if ( ne > 1 ) {
                    aa [ ( i * 2 ) + 1 ] = ea[1];
                }
            }
            attachmentAnchors.clear();
            return aa;
        }
        private void addGDEFSubtable ( int stType, GlyphMappingTable mapping ) {
            subtables.add ( GlyphDefinitionTable.createSubtable ( stType, makeLookupId ( ltSequence ), stSequence, ltFlags, stFormat, mapping, extractSubtableEntries() ) );
            nextSubtableInLookup();
        }
        private void addGSUBSubtable ( int stType, GlyphCoverageTable coverage, List entries ) {
            subtables.add ( GlyphSubstitutionTable.createSubtable ( stType, makeLookupId ( ltSequence ), stSequence, ltFlags, stFormat, coverage, entries ) );
            nextSubtableInLookup();
        }
        private void addGSUBSubtable ( int stType, GlyphCoverageTable coverage ) {
            addGSUBSubtable ( stType, coverage, extractSubtableEntries() );
        }
        private void addGPOSSubtable ( int stType, GlyphCoverageTable coverage, List entries ) {
            subtables.add ( GlyphPositioningTable.createSubtable ( stType, makeLookupId ( ltSequence ), stSequence, ltFlags, stFormat, coverage, entries ) );
            nextSubtableInLookup();
        }
        private void addGPOSSubtable ( int stType, GlyphCoverageTable coverage ) {
            addGPOSSubtable ( stType, coverage, extractSubtableEntries() );
        }
    }
    private int mapGlyphId0 ( String glyph ) {
        assert glyphIds != null;
        Integer gid = glyphIds.get ( glyph );
        if ( gid != null ) {
            return (int) gid;
        } else {
            return -1;
        }
    }
    private int mapGlyphId ( String glyph, String[] currentElement ) throws SAXException {
        int g = mapGlyphId0 ( glyph );
        if ( g < 0 ) {
            unsupportedGlyph ( currentElement, glyph );
            return -1;
        } else {
            return g;
        }
    }
    private int[] mapGlyphIds ( String glyphs, String[] currentElement ) throws SAXException {
        String[] ga = glyphs.split(",");
        int[] gids = new int [ ga.length ];
        int i = 0;
        for ( String glyph : ga ) {
            gids[i++] = mapGlyphId ( glyph, currentElement );
        }
        return gids;
    }
    private int mapGlyphIdToChar ( String glyph ) {
        assert glyphIds != null;
        Integer gid = glyphIds.get ( glyph );
        if ( gid != null ) {
            if ( gmap != null ) {
                Integer cid = gmap.get ( gid ); 
                if ( cid != null ) {
                    return cid.intValue();
                }
            }
        }
        return -1;
    }
    private String formatLocator() {
        if ( locator == null ) {
            return "{null}";
        } else {
            return "{" + locator.getSystemId() + ":" + locator.getLineNumber() + ":" + locator.getColumnNumber() + "}";
        }
    }
    private void unsupportedElement ( String[] en ) throws SAXException {
        throw new SAXException ( formatLocator() + ": unsupported element " + formatExpandedName ( en ) );
    }
    private void notPermittedInElementContext ( String[] en, String[] cn, Object xns ) throws SAXException {
        assert en != null;
        assert cn != null;
        String s = "element " + formatExpandedName(en) + " not permitted in current element context " + formatExpandedName(cn);
        if ( xns == null ) {
            s += ", expected root context";
        } else if ( xns instanceof String[][] ) {
            int nxn = 0;
            s += ", expected one of { ";
            for ( String[] xn : (String[][]) xns ) {
                if ( nxn++ > 0 ) {
                    s += ", ";
                }
                s += formatExpandedName ( xn );
            }
            s += " }";
        } else if ( xns instanceof String[] ) {
            s += ", expected " + formatExpandedName ( (String[]) xns );
        }
        throw new SAXException ( formatLocator() + ": " + s );
    }
    private void missingRequiredAttribute ( String[] en, String name ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " missing required attribute " + name );
    }
    private void duplicateGlyph ( String[] en, String name, int gid ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " contains duplicate name \"" + name + "\", with identifier value " + gid );
    }
    private void unsupportedGlyph ( String[] en, String name ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " refers to unsupported glyph id \"" + name + "\"" );
    }
    private void duplicateCMAPCharacter ( String[] en, int cid ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " contains duplicate cmap character code: " + CharUtilities.format ( cid ) );
    }
    private void duplicateCMAPGlyph ( String[] en, int gid ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " contains duplicate cmap glyph code: " + gid );
    }
    private void duplicateGlyphClass ( String[] en, String name, String glyphClass ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " contains duplicate glyph class for \"" + name + "\", with class value " + glyphClass );
    }
    private void unsupportedFormat ( String[] en, int format ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " refers to unsupported table format \"" + format + "\"" );
    }
    private void invalidIndex ( String[] en, int actual, int expected ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " specifies invalid index " + actual + ", expected " + expected );
    }
    private void mismatchedIndex ( String[] en, String label, int actual, int expected ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " mismatched " + label + " index: got " + actual + ", expected " + expected );
    }
    private void mismatchedEntries ( String[] en, int nce, int nse ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " mismatched coverage and subtable entry counts, # coverages " + nce + ", # entries " + nse );
    }
    private void missingParameter ( String[] en, String label ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " missing " + label + " parameter" );
    }
    private void duplicateParameter ( String[] en, String label ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " duplicate " + label + " parameter" );
    }
    private void duplicateCoverageIndex ( String[] en, int index ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " duplicate coverage table index " + index );
    }
    private void missingCoverage ( String[] en, String type, int expected ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " missing " + type + " coverage table, expected " + ( ( expected > 0 ) ? expected : 1 ) + " table(s)" );
    }
    private void missingTag ( String[] en, String label ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " missing " + label + " tag" );
    }
    private void duplicateTag ( String[] en, String label, String tag ) throws SAXException {
        throw new SAXException ( formatLocator() + ": element " + formatExpandedName(en) + " duplicate " + label + " tag: " + tag );
    }
    private static String[] makeExpandedName ( String uri, String localName, String qName ) {
        if ( ( uri != null ) && ( uri.length() == 0 ) ) {
            uri = null;
        }
        if ( ( localName != null ) && ( localName.length() == 0 ) ) {
            localName = null;
        }
        if ( ( uri == null ) && ( localName == null ) ) {
            uri = extractPrefix ( qName );
            localName = extractLocalName ( qName );
        }
        return new String[] { uri, localName };
    }
    private static String extractPrefix ( String qName ) {
        String[] sa = qName.split(":");
        if ( sa.length == 2 ) {
            return sa[0];
        } else {
            return null;
        }
    }
    private static String extractLocalName ( String qName ) {
        String[] sa = qName.split(":");
        if ( sa.length == 2 ) {
            return sa[1];
        } else if ( sa.length == 1 ) {
            return sa[0];
        } else {
            return null;
        }
    }
    private static boolean sameExpandedName ( String[] n1, String[] n2 ) {
        String u1 = n1[0];
        String u2 = n2[0];
        if ( ( u1 == null ) ^ ( u2 == null ) ) {
            return false;
        }
        if ( ( u1 != null ) && ( u2 != null ) ) {
            if ( ! u1.equals ( u2 ) ) {
                return false;
            }
        }
        String l1 = n1[1];
        String l2 = n2[1];
        if ( ( l1 == null ) ^ ( l2 == null ) ) {
            return false;
        }
        if ( ( l1 != null ) && ( l2 != null ) ) {
            if ( ! l1.equals ( l2 ) ) {
                return false;
            }
        }
        return true;
    }
    private static String formatExpandedName ( String[] n ) {
        String u = ( n[0] != null ) ? n[0] : "null";
        String l = ( n[1] != null ) ? n[1] : "null";
        return "{" + u + "}" + l;
    }
}
