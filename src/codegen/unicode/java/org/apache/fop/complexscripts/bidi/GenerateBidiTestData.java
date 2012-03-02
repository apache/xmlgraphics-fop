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

package org.apache.fop.text.bidi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.fop.complexscripts.bidi.BidiConstants;
import org.apache.fop.util.License;

// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck
// CSOFF: EmptyForIteratorPadCheck

/**
 * <p>Utility for generating a Java class and associated data files representing
 * bidirectional confomance test data from the Unicode Character Database and
 * Unicode BidiTest data files.</p>
 *
 * <p>This code is derived in part from GenerateBidiClassUtils.java.</p>
 *
 * @author Glenn Adams
 */
public final class GenerateBidiTestData {

    // local constants
    private static final String PFX_TYPE = "@Type:";
    private static final String PFX_LEVELS = "@Levels:";
    private static final String PFX_REORDER = "@Reorder:";

    // command line options
    private static boolean ignoreDeprecatedTypeData;
    private static boolean verbose;

    // instrumentation
    private static int lineNumber;
    private static int numTypeRanges;
    private static int numLevelSpecs;
    private static int numTestSpecs;

    // compiled data
    private static int[][] td;                  // types data
    private static int[][] ld;                  // levels data

    // ensure non-instantiation
    private GenerateBidiTestData() {
    }

    /**
     * Generate a class managing bidi test data for Unicode characters.
     *
     * @param ucdFileName name (as URL) of file containing unicode character database data
     * @param bidiFileName name (as URL) of file containing bidi test data
     * @param outFileName name of the output class file
     * @throws Exception
     */
    private static void convertBidiTestData(String ucdFileName, String bidiFileName, String outFileName) throws Exception {

        // read type data from UCD if ignoring deprecated type data
        if ( ignoreDeprecatedTypeData ) {
            readBidiTypeData(ucdFileName);
        }

        // read bidi test data
        readBidiTestData(bidiFileName);

        // generate class
        PrintWriter out = new PrintWriter(new FileWriter(outFileName));
        License.writeJavaLicenseId(out);
        out.println();
        out.println("package org.apache.fop.complexscripts.bidi;");
        out.println();
        out.println("import java.io.IOException;");
        out.println("import java.io.InputStream;");
        out.println("import java.io.ObjectInputStream;");
        out.println();
        out.println("// CSOFF: WhitespaceAfterCheck");
        out.println();
        out.println("/*");
        out.println(" * !!! THIS IS A GENERATED FILE !!!");
        out.println(" * If updates to the source are needed, then:");
        out.println(" * - apply the necessary modifications to");
        out.println(" *   'src/codegen/unicode/java/org/apache/fop/text/bidi/GenerateBidiTestData.java'");
        out.println(" * - run 'ant codegen-unicode', which will generate a new BidiTestData.java");
        out.println(" *   in 'test/java/org/apache/fop/complexscripts/bidi'");
        out.println(" * - commit BOTH changed files");
        out.println(" */");
        out.println();
        out.println("/** Bidirectional test data. */");
        out.println("public final class BidiTestData {");
        out.println();
        out.println("    private BidiTestData() {");
        out.println("    }");
        out.println();
        dumpData ( out, outFileName );
        out.println("    public static final int NUM_TEST_SEQUENCES = " + numTestSpecs + ";");
        out.println();
        out.println("    public static int[] readTestData ( String prefix, int index ) {");
        out.println("        int[] data = null;");
        out.println("        InputStream is = null;");
        out.println("        Class btc = BidiTestData.class;");
        out.println("        String name = btc.getSimpleName() + \"$\" + prefix + index + \".ser\";");
        out.println("        try {");
        out.println("            if ( ( is = btc.getResourceAsStream ( name ) ) != null ) {");
        out.println("                ObjectInputStream ois = new ObjectInputStream ( is );");
        out.println("                data = (int[]) ois.readObject();");
        out.println("                ois.close();");
        out.println("            }");
        out.println("        } catch ( IOException e ) {");
        out.println("            data = null;");
        out.println("        } catch ( ClassNotFoundException e ) {");
        out.println("            data = null;");
        out.println("        } finally {");
        out.println("            if ( is != null ) {");
        out.println("                try { is.close(); } catch ( Exception e ) {}");
        out.println("            }");
        out.println("        }");
        out.println("        return data;");
        out.println("    }");
        out.println("}");
        out.flush();
        out.close();

    }

    /**
     * Read bidi type data.
     *
     * @param ucdFileName name (as URL) of unicode character database data
     */
    private static void readBidiTypeData(String ucdFileName) throws Exception {
        BufferedReader b = new BufferedReader(new InputStreamReader(new URL(ucdFileName).openStream()));
        String line;
        int n;
        // singleton map - derived from single char entry
        Map/*<Integer,List>*/ sm = new HashMap/*<Integer,List>*/();
        // interval map - derived from pair of block endpoint entries
        Map/*<String,int[3]>*/ im = new HashMap/*<String,int[3]>*/();
        if ( verbose ) {
            System.out.print("Reading bidi type data...");
        }
        for ( lineNumber = 0; ( line = b.readLine() ) != null; ) {
            lineNumber++;
            if ( line.length() == 0 ) {
                continue;
            } else if ( line.startsWith("#") ) {
                continue;
            } else {
                parseTypeProperties ( line, sm, im );
            }
        }
        // extract type data list
        List tdl = processTypeData ( sm, im, new ArrayList() );
        // dump instrumentation
        if ( verbose ) {
            System.out.println();
            System.out.println("Read type ranges : " + numTypeRanges );
            System.out.println("Read lines       : " + lineNumber );
        }
        td = (int[][]) tdl.toArray ( new int [ tdl.size() ] [] );
    }

    private static void parseTypeProperties ( String line, Map/*<Integer,List>*/ sm, Map/*<String,int[3]>*/ im ) {
        String[] sa = line.split(";");
        if ( sa.length >= 5 ) {
            int uc = Integer.parseInt ( sa[0], 16 );
            int bc = parseBidiClassAny ( sa[4] );
            if ( bc >= 0 ) {
                String ucName = sa[1];
                if ( isBlockStart ( ucName ) ) {
                    String ucBlock = getBlockName ( ucName );
                    if ( ! im.containsKey ( ucBlock ) ) {
                        im.put ( ucBlock, new int[] { uc, -1, bc } );
                    } else {
                        throw new IllegalArgumentException ( "duplicate start of block '" + ucBlock + "' at entry: " + line );
                    }
                } else if ( isBlockEnd ( ucName ) ) {
                    String ucBlock = getBlockName ( ucName );
                    if ( im.containsKey ( ucBlock ) ) {
                        int[] ba = (int[]) im.get ( ucBlock );
                        assert ba.length == 3;
                        if ( ba[1] < 0 ) {
                            ba[1] = uc;
                        } else {
                            throw new IllegalArgumentException ( "duplicate end of block '" + ucBlock + "' at entry: " + line );
                        }
                    } else {
                        throw new IllegalArgumentException ( "missing start of block '" + ucBlock + "' at entry: " + line );
                    }
                } else {
                    Integer k = Integer.valueOf ( bc );
                    List sl;
                    if ( ! sm.containsKey ( k ) ) {
                        sl = new ArrayList();
                        sm.put ( k, sl );
                    } else {
                        sl = (List) sm.get ( k );
                    }
                    assert sl != null;
                    sl.add ( Integer.valueOf ( uc ) );
                }
            } else {
                throw new IllegalArgumentException ( "invalid bidi class '" + sa[4] + "' at entry: " + line );
            }
        } else {
            throw new IllegalArgumentException ( "invalid unicode character database entry: " + line );
        }
    }

    private static boolean isBlockStart ( String s ) {
        return s.startsWith("<") && s.endsWith("First>");
    }

    private static boolean isBlockEnd ( String s ) {
        return s.startsWith("<") && s.endsWith("Last>");
    }

    private static String getBlockName ( String s ) {
        String[] sa = s.substring ( 1, s.length() - 1 ).split(",");
        assert ( sa != null ) && ( sa.length > 0 );
        return sa[0].trim();
    }

    private static List processTypeData ( Map/*<Integer,List>*/ sm, Map/*<String,int[3]>*/ im, List tdl ) {
        for ( int i = BidiConstants.FIRST, k = BidiConstants.LAST; i <= k; i++ ) {
            Map/*<Integer,Integer>*/ rm = new TreeMap/*<Integer,Integer>*/();
            // populate intervals from singleton map
            List sl = (List) sm.get ( Integer.valueOf ( i ) );
            if ( sl != null ) {
                for ( Iterator it = sl.iterator(); it.hasNext(); ) {
                    Integer s = (Integer) it.next();
                    int uc = s.intValue();
                    rm.put ( Integer.valueOf ( uc ), Integer.valueOf ( uc + 1 ) );
                }
            }
            // populate intervals from (block) interval map
            if ( ! im.isEmpty() ) {
                for ( Iterator it = im.values().iterator(); it.hasNext(); ) {
                    int[] ba = (int[]) it.next();
                    assert ( ba != null ) && ( ba.length > 2 );
                    if ( ba[2] == i ) {
                        rm.put ( Integer.valueOf ( ba[0] ), Integer.valueOf ( ba[1] + 1 ) );
                    }
                }
            }
            tdl.add ( createTypeData ( i, extractRanges ( rm ) ) );
        }
        return tdl;
    }

    private static List extractRanges ( Map/*<Integer,Integer>*/ rm ) {
        List ranges = new ArrayList();
        int sLast = 0;
        int eLast = 0;
        for ( Iterator it = rm.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry/*<Integer,Integer>*/ me = (Map.Entry/*<Integer,Integer>*/) it.next();
            int s = ((Integer) me.getKey()).intValue();
            int e = ((Integer) me.getValue()).intValue();
            if ( s > eLast ) {
                if ( eLast > sLast ) {
                    ranges.add ( new int[] { sLast, eLast } );
                    if ( verbose ) {
                        if ( ( ++numTypeRanges % 10 ) == 0 ) {
                            System.out.print("#");
                        }
                    }
                }
                sLast = s;
                eLast = e;
            } else if ( ( s >= sLast ) && ( e >= eLast ) ) {
                eLast = e;
            }
        }
        if ( eLast > sLast ) {
            ranges.add ( new int[] { sLast, eLast } );
            if ( verbose ) {
                if ( ( ++numTypeRanges % 10 ) == 0 ) {
                    System.out.print("#");
                }
            }
        }
        return ranges;
    }

    /**
     * Read biditest data.
     *
     * @param bidiFileName name (as URL) of bidi test data
     */
    private static void readBidiTestData(String bidiFileName) throws Exception {
        BufferedReader b = new BufferedReader(new InputStreamReader(new URL(bidiFileName).openStream()));
        String line;
        int n;
        List tdl = new ArrayList();
        List ldl = new ArrayList();
        if ( verbose ) {
            System.out.print("Reading bidi test data...");
        }
        for ( lineNumber = 0; ( line = b.readLine() ) != null; ) {
            lineNumber++;
            if ( line.length() == 0 ) {
                continue;
            } else if ( line.startsWith("#") ) {
                continue;
            } else if ( line.startsWith(PFX_TYPE) && ! ignoreDeprecatedTypeData ) {
                List lines = new ArrayList();
                if ( ( n = readType ( line, b, lines ) ) < 0 ) {
                    break;
                } else {
                    lineNumber += n;
                    tdl.add ( parseType ( lines ) );
                }
            } else if ( line.startsWith(PFX_LEVELS) ) {
                List lines = new ArrayList();
                if ( ( n = readLevels ( line, b, lines ) ) < 0 ) {
                    break;
                } else {
                    lineNumber += n;
                    ldl.add ( parseLevels ( lines ) );
                }
            }
        }
        // dump instrumentation
        if ( verbose ) {
            System.out.println();
            if ( ! ignoreDeprecatedTypeData ) {
                System.out.println("Read type ranges : " + numTypeRanges );
            }
            System.out.println("Read level specs : " + numLevelSpecs );
            System.out.println("Read test specs  : " + numTestSpecs );
            System.out.println("Read lines       : " + lineNumber );
        }
        if ( ! ignoreDeprecatedTypeData ) {
            td = (int[][]) tdl.toArray ( new int [ tdl.size() ] [] );
        }
        ld = (int[][]) ldl.toArray ( new int [ ldl.size() ] [] );
    }

    private static int readType ( String line, BufferedReader b, List lines ) throws IOException {
        lines.add ( line );
        return 0;
    }

    private static int readLevels ( String line, BufferedReader b, List lines ) throws IOException {
        boolean done = false;
        int n = 0;
        lines.add ( line );
        while ( ! done ) {
            switch ( testPrefix ( b, PFX_LEVELS ) ) {
            case 0:     // within current levels
                if ( ( line = b.readLine() ) != null ) {
                    n++;
                    if ( ( line.length() > 0 ) && ! line.startsWith("#") ) {
                        lines.add ( line );
                    }
                } else {
                    done = true;
                }
                break;
            case 1:     // end of current levels
            case -1:    // eof
            default:
                done = true;
                break;
            }
        }
        return n;
    }

    private static int testPrefix ( BufferedReader b, String pfx ) throws IOException {
        int rv = 0;
        int pfxLen = pfx.length();
        b.mark ( pfxLen );
        for ( int i = 0, n = pfxLen; i < n; i++ ) {
            int c = b.read();
            if ( c < 0 ) {
                rv = -1;
                break;
            } else if ( c != pfx.charAt ( i ) ) {
                rv = 0;
                break;
            } else {
                rv = 1;
            }
        }
        b.reset();
        return rv;
    }

    private static int[] parseType ( List lines ) {
        if ( ( lines != null ) && ( lines.size() >= 1 ) ) {
            String line = (String) lines.get(0);
            if ( line.startsWith(PFX_TYPE) ) {
                // @Type: BIDI_CLASS ':' LWSP CHARACTER_CLASS
                String[] sa = line.split ( ":" );
                if ( sa.length == 3 ) {
                    String bcs = sa[1].trim();
                    String crs = sa[2].trim();
                    int bc = parseBidiClass ( bcs );
                    List rl = parseCharacterRanges ( crs );
                    return createTypeData ( bc, rl );
                }
            }
        }
        return null;
    }

    private static int[] createTypeData ( int bc, List ranges ) {
        int[] data = new int [ 1 + ( 2 * ranges.size() ) ];
        int k = 0;
        data [ k++ ] = bc;
        for ( Iterator it = ranges.iterator(); it.hasNext(); ) {
            int[] r = (int[]) it.next();
            data [ k++ ] = r [ 0 ];
            data [ k++ ] = r [ 1 ];
        }
        return data;
    }

    private static int parseBidiClass ( String bidiClass ) {
        int bc = 0;
        if ( "L".equals ( bidiClass ) ) {
            bc = BidiConstants.L;
        } else if ( "LRE".equals ( bidiClass ) ) {
            bc = BidiConstants.LRE;
        } else if ( "LRO".equals ( bidiClass ) ) {
            bc = BidiConstants.LRO;
        } else if ( "R".equals ( bidiClass ) ) {
            bc = BidiConstants.R;
        } else if ( "AL".equals ( bidiClass ) ) {
            bc = BidiConstants.AL;
        } else if ( "RLE".equals ( bidiClass ) ) {
            bc = BidiConstants.RLE;
        } else if ( "RLO".equals ( bidiClass ) ) {
            bc = BidiConstants.RLO;
        } else if ( "PDF".equals ( bidiClass ) ) {
            bc = BidiConstants.PDF;
        } else if ( "EN".equals ( bidiClass ) ) {
            bc = BidiConstants.EN;
        } else if ( "ES".equals ( bidiClass ) ) {
            bc = BidiConstants.ES;
        } else if ( "ET".equals ( bidiClass ) ) {
            bc = BidiConstants.ET;
        } else if ( "AN".equals ( bidiClass ) ) {
            bc = BidiConstants.AN;
        } else if ( "CS".equals ( bidiClass ) ) {
            bc = BidiConstants.CS;
        } else if ( "NSM".equals ( bidiClass ) ) {
            bc = BidiConstants.NSM;
        } else if ( "BN".equals ( bidiClass ) ) {
            bc = BidiConstants.BN;
        } else if ( "B".equals ( bidiClass ) ) {
            bc = BidiConstants.B;
        } else if ( "S".equals ( bidiClass ) ) {
            bc = BidiConstants.S;
        } else if ( "WS".equals ( bidiClass ) ) {
            bc = BidiConstants.WS;
        } else if ( "ON".equals ( bidiClass ) ) {
            bc = BidiConstants.ON;
        } else {
            throw new IllegalArgumentException ( "unknown bidi class: " + bidiClass );
        }
        return bc;
    }

    private static int parseBidiClassAny ( String bidiClass ) {
        try {
            return parseBidiClass ( bidiClass );
        } catch ( IllegalArgumentException e ) {
            return -1;
        }
    }

    private static List parseCharacterRanges ( String charRanges ) {
        List ranges = new ArrayList();
        CharacterIterator ci = new StringCharacterIterator ( charRanges );
        // read initial list delimiter
        skipSpace ( ci );
        if ( ! readStartOfList ( ci ) ) {
            badRangeSpec ( "missing initial list delimiter", charRanges );
        }
        // read negation token if present
        boolean negated = false;
        skipSpace ( ci );
        if ( maybeReadNext ( ci, '^' ) ) {
            negated = true;
        }
        // read item
        int[] r;
        skipSpace ( ci );
        if ( ( r = maybeReadItem ( ci ) ) != null ) {
            ranges.add ( r );
            if ( verbose ) {
                if ( ( ++numTypeRanges % 10 ) == 0 ) {
                    System.out.print("#");
                }
            }
        } else {
            badRangeSpec ( "must contain at least one item", charRanges );
        }
        // read more items if present
        boolean more = true;
        while ( more ) {
            // read separator if present
            String s;
            skipSpace ( ci );
            if ( ( s = maybeReadSeparator ( ci ) ) != null ) {
                if ( ( s.length() != 0 ) && ! s.equals("||") ) {
                    badRangeSpec ( "invalid item separator \"" + s + "\"", charRanges );
                }
            }
            // read item
            skipSpace ( ci );
            if ( ( r = maybeReadItem ( ci ) ) != null ) {
                ranges.add ( r );
                if ( verbose ) {
                    if ( ( ++numTypeRanges % 10 ) == 0 ) {
                        System.out.print("#");
                    }
                }
            } else {
                more = false;
            }
        }
        // read terminating list delimiter
        skipSpace ( ci );
        if ( ! readEndOfList ( ci ) ) {
            badRangeSpec ( "missing terminating list delimiter", charRanges );
        }
        if ( ! atEnd ( ci ) ) {
            badRangeSpec ( "extraneous content prior to end of line", ci );
        }
        if ( negated ) {
            ranges = complementRanges ( ranges );
        }
        return removeSurrogates ( ranges );
    }

    private static boolean atEnd ( CharacterIterator ci ) {
        return ci.getIndex() >= ci.getEndIndex();
    }

    private static boolean readStartOfList ( CharacterIterator ci ) {
        return maybeReadNext ( ci, '[' );
    }

    private static void skipSpace ( CharacterIterator ci ) {
        while ( ! atEnd ( ci ) ) {
            char c = ci.current();
            if ( ! Character.isWhitespace ( c ) ) {
                break;
            } else {
                ci.next();
            }
        }
    }

    private static boolean maybeReadNext ( CharacterIterator ci, char next ) {
        while ( ! atEnd ( ci ) ) {
            char c = ci.current();
            if ( c == next ) {
                ci.next();
                return true;
            } else {
                break;
            }
        }
        return false;
    }

    private static int[] maybeReadItem ( CharacterIterator ci ) {
        // read first code point
        int p1 = -1;
        skipSpace ( ci );
        if ( ( p1 = maybeReadCodePoint ( ci ) ) < 0 ) {
            return null;
        }
        // read second code point if present
        int p2 = -1;
        skipSpace ( ci );
        if ( maybeReadNext ( ci, '-' ) ) {
            skipSpace ( ci );
            if ( ( p2 = maybeReadCodePoint ( ci ) ) < 0 ) {
                badRangeSpec ( "incomplete item range, requires second item", ci );
            }
        }
        if ( p2 < 0 ) {
            return new int[] { p1, p1 + 1 };    // convert to half open interval [ P1, P1+1 )
        } else if ( p1 <= p2 ) {
            return new int[] { p1, p2 + 1 };    // convert to half open interval [ P1, P2+2 )
        } else {
            badRangeSpec ( "invalid item range, second item must be greater than or equal to first item", ci );
            return null;
        }
    }

    private static int maybeReadCodePoint ( CharacterIterator ci ) {
        if ( maybeReadNext ( ci, '\\' ) ) {
            if ( maybeReadNext ( ci, 'u' ) ) {
                String s = maybeReadHexDigits ( ci, 4 );
                if ( s != null ) {
                    return Integer.parseInt ( s, 16 );
                } else {
                    badRangeSpec ( "incomplete escaped code point, requires 4 hex digits", ci );
                }
            } else if ( maybeReadNext ( ci, 'U' ) ) {
                String s = maybeReadHexDigits ( ci, 8 );
                if ( s != null ) {
                    return Integer.parseInt ( s, 16 );
                } else {
                    badRangeSpec ( "incomplete escaped code point, requires 8 hex digits", ci );
                }
            } else {
                char c = ci.current();
                if ( c == CharacterIterator.DONE ) {
                    badRangeSpec ( "incomplete escaped code point", ci );
                } else {
                    ci.next();
                    return (int) c;
                }
            }
        } else {
            char c = ci.current();
            if ( ( c == CharacterIterator.DONE ) || ( c == ']' ) ) {
                return -1;
            } else {
                ci.next();
                return (int) c;
            }
        }
        return -1;
    }

    private static String maybeReadHexDigits ( CharacterIterator ci, int numDigits ) {
        StringBuffer sb = new StringBuffer();
        while ( ( numDigits < 0 ) || ( sb.length() < numDigits ) ) {
            char c = ci.current();
            if ( c != CharacterIterator.DONE ) {
                if ( isHexDigit ( c ) ) {
                    ci.next();
                    sb.append ( c );
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if ( ( ( numDigits < 0 ) && ( sb.length() > 0 ) ) || ( sb.length() == numDigits ) ) {
            return sb.toString();
        } else {
            return null;
        }
    }

    private static boolean isHexDigit ( char c ) {
        return ( ( c >= '0' ) && ( c <= '9' ) ) || ( ( c >= 'a' ) && ( c <= 'f' ) ) || ( ( c >= 'A' ) && ( c <= 'F' ) );
    }

    private static String maybeReadSeparator ( CharacterIterator ci ) {
        if ( maybeReadNext ( ci, '|' ) ) {
            if ( maybeReadNext ( ci, '|' ) ) {
                return "||";
            } else {
                return "|";
            }
        } else {
            return "";
        }
    }

    private static boolean readEndOfList ( CharacterIterator ci ) {
        return maybeReadNext ( ci, ']' );
    }

    private static List complementRanges ( List ranges ) {
        Map/*<Integer,Integer>*/ rm = new TreeMap/*<Integer,Integer>*/();
        for ( Iterator it = ranges.iterator(); it.hasNext(); ) {
            int[] r = (int[]) it.next();
            rm.put ( Integer.valueOf ( r[0] ), Integer.valueOf ( r[1] ) );
        }
        // add complement ranges save last
        int s;
        int e;
        int cs = 0;
        List compRanges = new ArrayList ( rm.size() + 1 );
        for ( Iterator it = rm.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry/*<Integer,Integer>*/ me = (Map.Entry/*<Integer,Integer>*/) it.next();
            s = ( (Integer) me.getKey() ).intValue();
            e = ( (Integer) me.getValue() ).intValue();
            if ( s > cs ) {
                compRanges.add ( new int[] { cs, s } );
            }
            cs = e;
        }
        // add trailing complement range
        if ( cs < 0x110000 ) {
            compRanges.add ( new int[] { cs, 0x110000 } );
        }
        return compRanges;
    }

    private static final int[] SURROGATES = new int[] { 0xD800, 0xE000 };

    private static List removeSurrogates ( List ranges ) {
        List rsl = new ArrayList ( ranges.size() );
        for ( Iterator it = ranges.iterator(); it.hasNext(); ) {
            int[] r = (int[]) it.next();
            if ( intersectsRange ( r, SURROGATES ) ) {
                rsl.addAll ( removeRange ( r, SURROGATES ) );
            } else {
                rsl.add ( r );
            }
        }
        return rsl;
    }

    /**
     * Determine if range r2 intersects with range r1.
     */
    private static boolean intersectsRange ( int[] r1, int[] r2 ) {
        if ( r1[1] <= r2[0] ) {                                 // r1 precedes r2 or abuts r2 on right
            return false;
        } else if ( r1[0] >= r2[1] ) {                          // r2 precedes r1 or abuts r1 on left
            return false;
        } else if ( ( r1[0] < r2[0] ) && ( r1[1] > r2[1] ) ) {  // r1 encloses r2
            return true;
        } else if ( r1[0] < r2[0] ) {                           // r1 precedes and overlaps r2
            return true;
        } else if ( r2[1] < r1[1] ) {                           // r2 precedes and overlaps r1
            return true;
        } else {                                                // r2 encloses r1
            return true;
        }
    }

    /**
     * Remove range r2 from range r1, leaving zero, one, or two
     * remaining ranges.
     */
    private static List removeRange ( int[] r1, int[] r2 ) {
        List rl = new ArrayList();
        if ( r1[1] <= r2[0] ) {                                 // r1 precedes r2 or abuts r2 on right
            rl.add ( r1 );
        } else if ( r1[0] >= r2[1] ) {                          // r2 precedes r1 or abuts r1 on left
            rl.add ( r1 );
        } else if ( ( r1[0] < r2[0] ) && ( r1[1] > r2[1] ) ) {  // r1 encloses r2
            rl.add ( new int[] { r1[0], r2[0] } );
            rl.add ( new int[] { r2[1], r1[1] } );
        } else if ( r1[0] < r2[0] ) {                           // r1 precedes and overlaps r2
            rl.add ( new int[] { r1[0], r2[0] } );
        } else if ( r2[1] < r1[1] ) {                           // r2 precedes and overlaps r1
            rl.add ( new int[] { r2[1], r1[1] } );
        }
        return rl;
    }

    private static void badRangeSpec ( String reason, String charRanges ) throws IllegalArgumentException {
        if ( verbose ) {
            System.out.println();
        }
        throw new IllegalArgumentException ( "bad range specification: " + reason + ": \"" + charRanges + "\"" );
    }

    private static void badRangeSpec ( String reason, CharacterIterator ci ) throws IllegalArgumentException {
        if ( verbose ) {
            System.out.println();
        }
        throw new IllegalArgumentException ( "bad range specification: " + reason + ": starting at \"" + remainder ( ci ) + "\"" );
    }

    private static String remainder ( CharacterIterator ci ) {
        StringBuffer sb = new StringBuffer();
        for ( char c; ( c = ci.current() ) != CharacterIterator.DONE; ) {
            ci.next();
            sb.append ( c );
        }
        return sb.toString();
    }

    /**
     * Parse levels segment, consisting of multiple lines as follows:
     *
     * LEVEL_SPEC \n
     * REORDER_SPEC \n
     * ( TEST_SPEC \n )+
     */
    private static int[] parseLevels ( List lines ) {
        int[] la = null;        // levels array
        int[] ra = null;        // reorder array
        List tal = new ArrayList();
        if ( ( lines != null ) && ( lines.size() >= 3 ) ) {
            for ( Iterator it = lines.iterator(); it.hasNext(); ) {
                String line = (String) it.next();
                if ( line.startsWith(PFX_LEVELS) ) {
                    if ( la == null ) {
                        la = parseLevelSpec ( line );
                        if ( verbose ) {
                            if ( ( ++numLevelSpecs % 10 ) == 0 ) {
                                System.out.print("&");
                            }
                        }
                    } else {
                        throw new IllegalArgumentException ( "redundant levels array: \"" + line + "\"" );
                    }
                } else if ( line.startsWith(PFX_REORDER) ) {
                    if ( la == null ) {
                        throw new IllegalArgumentException ( "missing levels array before: \"" + line + "\"" );
                    } else if ( ra == null ) {
                        ra = parseReorderSpec ( line, la );
                    } else {
                        throw new IllegalArgumentException ( "redundant reorder array: \"" + line + "\"" );
                    }
                } else if ( ( la != null ) && ( ra != null ) ) {
                    int[] ta = parseTestSpec ( line, la );
                    if ( ta != null ) {
                        if ( verbose ) {
                            if ( ( ++numTestSpecs % 100 ) == 0 ) {
                                System.out.print("!");
                            }
                        }
                        tal.add ( ta );
                    }
                } else if ( la == null ) {
                    throw new IllegalArgumentException ( "missing levels array before: \"" + line + "\"" );
                } else if ( ra == null ) {
                    throw new IllegalArgumentException ( "missing reorder array before: \"" + line + "\"" );
                }
            }
        }
        if ( ( la != null ) && ( ra != null ) ) {
            return createLevelData ( la, ra, tal );
        } else {
            return null;
        }
    }

    private static int[] createLevelData ( int[] la, int[] ra, List tal ) {
        int nl = la.length;
        int[] data = new int [ 1 + nl * 2 + ( ( nl + 1 ) * tal.size() ) ];
        int k = 0;
        data [ k++ ] = nl;
        for ( int i = 0, n = nl; i < n; i++ ) {
            data [ k++ ] = la [ i ];
        }
        int nr = ra.length;
        for ( int i = 0, n = nr; i < n; i++ ) {
            data [ k++ ] = ra [ i ];
        }
        for ( Iterator it = tal.iterator(); it.hasNext(); ) {
            int[] ta = (int[]) it.next();
            if ( ta == null ) {
                throw new IllegalStateException ( "null test array" );
            } else if ( ta.length == ( nl + 1 ) ) {
                for ( int i = 0, n = ta.length; i < n; i++ ) {
                    data [ k++ ] = ta [ i ];
                }
            } else {
                throw new IllegalStateException ( "test array length error, expected " + ( nl + 1 ) + " entries, got " + ta.length + " entries" );
            }
        }
        assert k == data.length;
        return data;
    }

    /**
     * Parse level specification, which follows the following syntax:
     *
     * @Levels: ( LWSP ( NUMBER | 'x' ) )+
     */
    private static int[] parseLevelSpec ( String line ) {
        CharacterIterator ci = new StringCharacterIterator ( line );
        List ll = new ArrayList();
        // read prefix
        skipSpace ( ci );
        if ( ! maybeReadToken ( ci, PFX_LEVELS ) ) {
            badLevelSpec ( "missing prefix \"" + PFX_LEVELS + "\"", ci );
        }
        // read level values
        boolean more = true;
        while ( more ) {
            Integer l;
            skipSpace ( ci );
            if ( ( l = maybeReadInteger ( ci ) ) != null ) {
                ll.add ( l );
            } else if ( maybeReadToken ( ci, "x" ) ) {
                ll.add ( Integer.valueOf ( -1 ) );
            } else {
                more = false;
            }
        }
        // read to end of line
        skipSpace ( ci );
        if ( ! atEnd ( ci ) ) {
            badLevelSpec ( "extraneous content prior to end of line", ci );
        }
        if ( ll.size() == 0 ) {
            badLevelSpec ( "must have at least one level value", ci );
        }
        return createLevelsArray ( ll );
    }

    private static Integer maybeReadInteger ( CharacterIterator ci ) {
        // read optional minus sign if present
        boolean negative;
        if ( maybeReadNext ( ci, '-' ) ) {
            negative = true;
        } else {
            negative = false;
        }
        // read digits
        StringBuffer sb = new StringBuffer();
        while ( true ) {
            char c = ci.current();
            if ( ( c != CharacterIterator.DONE ) && isDigit ( c ) ) {
                ci.next();
                sb.append ( c );
            } else {
                break;
            }
        }
        if ( sb.length() == 0 ) {
            return null;
        } else {
            int value = Integer.parseInt ( sb.toString() );
            if ( negative ) {
                value = -value;
            }
            return Integer.valueOf ( value );
        }
    }

    private static boolean isDigit ( char c ) {
        return ( ( c >= '0' ) && ( c <= '9' ) );
    }

    private static boolean maybeReadToken ( CharacterIterator ci, String s ) {
        int startIndex = ci.getIndex();
        for ( int i = 0, n = s.length(); i < n; i++ ) {
            char c = s.charAt ( i );
            if ( ci.current() == c ) {
                ci.next();
            } else {
                ci.setIndex ( startIndex );
                return false;
            }
        }
        return true;
    }

    private static void badLevelSpec ( String reason, CharacterIterator ci ) throws IllegalArgumentException {
        if ( verbose ) {
            System.out.println();
        }
        throw new IllegalArgumentException ( "bad level specification: " + reason + ": starting at \"" + remainder ( ci ) + "\"" );
    }

    private static int[] createLevelsArray ( List levels ) {
        int[] la = new int [ levels.size() ];
        int k = 0;
        for ( Iterator it = levels.iterator(); it.hasNext(); ) {
            la [ k++ ] = ( (Integer) it.next() ).intValue(); 
        }
        return la;
    }

    /**
     * Parse reorder specification, which follows the following syntax:
     *
     * @Reorder: ( LWSP NUMBER )*
     */
    private static int[] parseReorderSpec ( String line, int[] levels ) {
        CharacterIterator ci = new StringCharacterIterator ( line );
        List rl = new ArrayList();
        // read prefix
        skipSpace ( ci );
        if ( ! maybeReadToken ( ci, PFX_REORDER ) ) {
            badReorderSpec ( "missing prefix \"" + PFX_REORDER + "\"", ci );
        }
        // read reorder values
        boolean more = true;
        while ( more ) {
            skipSpace ( ci );
            Integer l;
            if ( ( l = maybeReadInteger ( ci ) ) != null ) {
                rl.add ( l );
            } else {
                more = false;
            }
        }
        // read to end of line
        skipSpace ( ci );
        if ( ! atEnd ( ci ) ) {
            badReorderSpec ( "extraneous content prior to end of line", ci );
        }
        return createReorderArray ( rl, levels );
    }

    private static void badReorderSpec ( String reason, CharacterIterator ci ) throws IllegalArgumentException {
        if ( verbose ) {
            System.out.println();
        }
        throw new IllegalArgumentException ( "bad reorder specification: " + reason + ": starting at \"" + remainder ( ci ) + "\"" );
    }

    private static int[] createReorderArray ( List reorders, int[] levels ) {
        int nr = reorders.size();
        int nl = levels.length;
        if ( nr <= nl ) {
            int[] ra = new int [ nl ];
            Iterator it = reorders.iterator();
            for ( int i = 0, n = nl; i < n; i++ ) {
                int r = -1;
                if ( levels [ i ] >= 0 ) {
                    if ( it.hasNext() ) {
                        r = ( (Integer) it.next() ).intValue();
                    }
                } 
                ra [ i ] = r;
            }
            return ra;
        } else {
            throw new IllegalArgumentException ( "excessive number of reorder array entries, expected no more than " + nl + ", but got " + nr + " entries" );
        }
    }

    /**
     * Parse test specification, which follows the following syntax:
     *
     * BIDI_CLASS ( LWSP BIDI_CLASS )+ ';' LWSP NUMBER
     */
    private static int[] parseTestSpec ( String line, int[] levels ) {
        CharacterIterator ci = new StringCharacterIterator ( line );
        List cl = new ArrayList();
        // read bidi class identifier sequence
        while ( ! atEnd ( ci ) && ! maybeReadNext ( ci, ';' ) ) {
            skipSpace ( ci );
            int bc;
            if ( ( bc = maybeReadBidiClass ( ci ) ) >= 0 ) {
                cl.add ( Integer.valueOf ( bc ) );
            } else {
                break;
            }
        }
        // read bit set
        skipSpace ( ci );
        String s;
        int bs = 0;
        if ( ( s = maybeReadHexDigits ( ci, -1 ) ) != null ) {
            bs = Integer.parseInt ( s, 16 );
        } else {
            badTestSpec ( "missing bit set", ci );
        }
        // read to end of line
        skipSpace ( ci );
        if ( ! atEnd ( ci ) ) {
            badTestSpec ( "extraneous content prior to end of line", ci );
        }
        return createTestArray ( cl, bs, levels );
    }

    private static String maybeReadIdentifier ( CharacterIterator ci ) {
        // read keyword chars ([A-Z])
        StringBuffer sb = new StringBuffer();
        while ( true ) {
            char c = ci.current();
            if ( c == CharacterIterator.DONE ) {
                break;
            } else if ( sb.length() == 0 ) {
                if ( Character.isUnicodeIdentifierStart ( c ) ) {
                    ci.next();
                    sb.append ( c );
                } else {
                    break;
                }
            } else {
                if ( Character.isUnicodeIdentifierPart ( c ) ) {
                    ci.next();
                    sb.append ( c );
                } else {
                    break;
                }
            }
        }
        if ( sb.length() == 0 ) {
            return null;
        } else {
            return sb.toString();
        }
    }

    private static int maybeReadBidiClass ( CharacterIterator ci ) {
        int bc = -1;
        int i = ci.getIndex();
        String s;
        if ( ( s = maybeReadIdentifier ( ci ) ) != null ) {
            try {
                bc = parseBidiClass ( s );
            } catch ( IllegalArgumentException e ) {
                throw e;
            }
        }
        if ( bc < 0 ) {
            ci.setIndex ( i );
        }
        return bc;
    }

    private static void badTestSpec ( String reason, CharacterIterator ci ) throws IllegalArgumentException {
        if ( verbose ) {
            System.out.println();
        }
        throw new IllegalArgumentException ( "bad test specification: " + reason + ": starting at \"" + remainder ( ci ) + "\"" );
    }

    private static int[] createTestArray ( List classes, int bitset, int[] levels ) {
        int nc = classes.size();
        if ( nc <= levels.length ) {
            int[] ta = new int [ 1 + nc ];
            int k = 0;
            ta [ k++ ] = bitset;
            for ( Iterator it = classes.iterator(); it.hasNext(); ) {
                ta [ k++ ] = ( (Integer) it.next() ).intValue(); 
            }
            return ta;
        } else {
            throw new IllegalArgumentException ( "excessive number of test array entries, expected no more than " + levels.length + ", but got " + nc + " entries" );
        }
    }

    /**
     * Dump data arrays to output and resource files.
     * @param out - bidi test data java class file print writer
     * @param outFileName - (full path) name of bidi test data java class file
     */
    private static void dumpData ( PrintWriter out, String outFileName ) throws IOException {
        File f = new File ( outFileName );
        File p = f.getParentFile();
        if ( td != null ) {
            String pfxTD = "TD";
            dumpResourcesDescriptor ( out, pfxTD, td.length );
            dumpResourcesData ( p, f.getName(), pfxTD, td );
        }
        if ( ld != null ) {
            String pfxTD = "LD";
            dumpResourcesDescriptor ( out, pfxTD, ld.length );
            dumpResourcesData ( p, f.getName(), pfxTD, ld );
        }
    }

    private static void dumpResourcesDescriptor ( PrintWriter out, String prefix, int numResources ) {
        out.println ( "    public static final String " + prefix + "_PFX = \"" + prefix + "\";" );
        out.println ( "    public static final int " + prefix + "_CNT = " + numResources + ";" );
        out.println("");
    }

    private static void dumpResourcesData ( File btcDir, String btcName, String prefix, int[][] data ) throws IOException {
        String btdName = extractDataFileName ( btcName );
        for ( int i = 0, n = data.length; i < n; i++ ) {
            File f = new File ( btcDir, btdName + "$" + prefix + i + ".ser" );
            ObjectOutputStream os = new ObjectOutputStream ( new FileOutputStream ( f ) );
            os.writeObject ( data[i] );
            os.close();
        }
    }

    private static final String JAVA_EXT = ".java";

    private static String extractDataFileName ( String btcName ) {
        if ( btcName.endsWith ( JAVA_EXT ) ) {
            return btcName.substring ( 0, btcName.length() - JAVA_EXT.length() );
        } else {
            return btcName;
        }
    }

    /**
     * Main entry point for generator.
     * @param args array of command line arguments
     */
    public static void main(String[] args) {
        String bidiFileName = "http://www.unicode.org/Public/UNIDATA/BidiTest.txt";
        String ucdFileName = "http://www.unicode.org/Public/UNIDATA/BidiTest.txt";
        String outFileName = "BidiTestData.java";
        boolean ok = true;
        for (int i = 0; ok && ( i < args.length ); i++) {
            String opt = args[i];
            if ("-b".equals(opt)) {
                if ( ( i + 1 ) <= args.length ) {
                    bidiFileName = args[++i];
                } else {
                    ok = false;
                }
            } else if ("-d".equals(opt)) {
                if ( ( i + 1 ) <= args.length ) {
                    ucdFileName = args[++i];
                } else {
                    ok = false;
                }
            } else if ("-i".equals(opt)) {
                ignoreDeprecatedTypeData = true;
            } else if ("-o".equals(opt)) {
                if ( ( i + 1 ) <= args.length ) {
                    outFileName = args[++i];
                } else {
                    ok = false;
                }
            } else if ("-v".equals(opt)) {
                verbose = true;
            } else {
                ok = false;
            }
        }
        if ( ! ok ) {
            System.out.println("Usage: GenerateBidiTestData [-v] [-i] [-d <ucdFile>] [-b <bidiFile>] [-o <outputFile>]");
            System.out.println("  defaults:");
            if ( ignoreDeprecatedTypeData ) {
                System.out.println("    <ucdFile>    : " + ucdFileName);
            }
            System.out.println("    <bidiFile>   : " + bidiFileName);
            System.out.println("    <outputFile> : " + outFileName);
        } else {
            try {
                convertBidiTestData(ucdFileName, bidiFileName, outFileName);
                System.out.println("Generated " + outFileName + " from");
                if ( ignoreDeprecatedTypeData ) {
                    System.out.println("    <ucdFile>  :     " + ucdFileName);
                }
                System.out.println("    <bidiFile> :     " + bidiFileName);
            } catch (Exception e) {
                System.out.println("An unexpected error occured at line: " + lineNumber );
                e.printStackTrace();
            }
        }
    }
}
