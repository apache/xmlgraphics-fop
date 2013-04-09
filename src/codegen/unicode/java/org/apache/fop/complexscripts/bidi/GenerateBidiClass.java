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

package org.apache.fop.complexscripts.bidi;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.fop.util.License;

// CSOFF: LineLength
// CSOFF: NoWhitespaceAfter

/**
 * <p>Utility for generating a Java class representing bidirectional
 * class properties from the Unicode property files.</p>
 *
 * <p>This code is derived in part from GenerateLineBreakUtils.java.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
public final class GenerateBidiClass {

    private GenerateBidiClass() {
    }

    private static byte[] bcL1 = new byte[256]; // ascii and basic latin blocks ( 0x0000 - 0x00FF )
    private static byte[] bcR1 = new byte[368]; // hebrew and arabic blocks     ( 0x0590 - 0x06FF )
    private static int[]  bcS1;                 // interval start indices
    private static int[]  bcE1;                 // interval end indices
    private static byte[] bcC1;                 // interval bid classes

    /**
     * Generate a class managing bidi class properties for Unicode characters.
     *
     * @param bidiFileName name (as URL) of file containing bidi type data
     * @param outFileName name of the output file
     * @throws Exception
     */
    private static void convertBidiClassProperties(String bidiFileName, String outFileName) throws Exception {

        readBidiClassProperties(bidiFileName);

        // generate class
        PrintWriter out = new PrintWriter(new FileWriter(outFileName));
        License.writeJavaLicenseId(out);
        out.println();
        out.println("package org.apache.fop.complexscripts.bidi;");
        out.println();
        out.println("import java.util.Arrays;");
        out.println("import org.apache.fop.complexscripts.bidi.BidiConstants;");
        out.println();
        out.println("// CSOFF: WhitespaceAfterCheck");
        out.println("// CSOFF: LineLengthCheck");
        out.println();
        out.println("/*");
        out.println(" * !!! THIS IS A GENERATED FILE !!!");
        out.println(" * If updates to the source are needed, then:");
        out.println(" * - apply the necessary modifications to");
        out.println(" *   'src/codegen/unicode/java/org/apache/fop/complexscripts/bidi/GenerateBidiClass.java'");
        out.println(" * - run 'ant codegen-unicode', which will generate a new BidiClass.java");
        out.println(" *   in 'src/java/org/apache/fop/complexscripts/bidi'");
        out.println(" * - commit BOTH changed files");
        out.println(" */");
        out.println();
        out.println("/** Bidirectional class utilities. */");
        out.println("public final class BidiClass {");
        out.println();
        out.println("private BidiClass() {");
        out.println("}");
        out.println();
        dumpData(out);
        out.println ("/**");
        out.println (" * Lookup bidi class for character expressed as unicode scalar value.");
        out.println (" * @param ch a unicode scalar value");
        out.println (" * @return bidi class");
        out.println (" */");
        out.println("public static int getBidiClass ( int ch ) {");
        out.println("  if ( ch <= 0x00FF ) {");
        out.println("    return bcL1 [ ch - 0x0000 ];");
        out.println("  } else if ( ( ch >= 0x0590 ) && ( ch <= 0x06FF ) ) {");
        out.println("    return bcR1 [ ch - 0x0590 ];");
        out.println("  } else {");
        out.println("    return getBidiClass ( ch, bcS1, bcE1, bcC1 );");
        out.println("  }");
        out.println("}");
        out.println();
        out.println("private static int getBidiClass ( int ch, int[] sa, int[] ea, byte[] ca ) {");
        out.println("  int k = Arrays.binarySearch ( sa, ch );");
        out.println("  if ( k >= 0 ) {");
        out.println("    return ca [ k ];");
        out.println("  } else {");
        out.println("    k = - ( k + 1 );");
        out.println("    if ( k == 0 ) {");
        out.println("      return BidiConstants.L;");
        out.println("    } else if ( ch <= ea [ k - 1 ] ) {");
        out.println("      return ca [ k - 1 ];");
        out.println("    } else {");
        out.println("      return BidiConstants.L;");
        out.println("    }");
        out.println("  }");
        out.println("}");
        out.println();
        out.println("}");
        out.flush();
        out.close();
    }

    /**
     * Read bidi class property data.
     *
     * @param bidiFileName name (as URL) of bidi type data
     */
    private static void readBidiClassProperties(String bidiFileName) throws Exception {
        // read property names
        BufferedReader b = new BufferedReader(new InputStreamReader(new URL(bidiFileName).openStream()));
        String line;
        int lineNumber = 0;
        TreeSet intervals = new TreeSet();
        while ((line = b.readLine()) != null) {
            lineNumber++;
            if (line.startsWith("#")) {
                continue;
            } else if (line.length() == 0) {
                continue;
            } else {
                if (line.indexOf ("#") != -1) {
                    line = (line.split ("#")) [ 0 ];
                }
                String[] fa = line.split (";");
                if (fa.length == 2) {
                    int[] interval = parseInterval (fa[0].trim());
                    byte bidiClass = (byte) parseBidiClass (fa[1].trim());
                    if (interval[1] == interval[0]) { // singleton
                        int c = interval[0];
                        if (c <= 0x00FF) {
                            if (bcL1 [ c - 0x0000 ] == 0) {
                                bcL1 [ c - 0x0000 ] = bidiClass;
                            } else {
                                throw new Exception ("duplicate singleton entry: " + c);
                            }
                        } else if ((c >= 0x0590) && (c <= 0x06FF)) {
                            if (bcR1 [ c - 0x0590 ] == 0) {
                                bcR1 [ c - 0x0590 ] = bidiClass;
                            } else {
                                throw new Exception ("duplicate singleton entry: " + c);
                            }
                        } else {
                            addInterval (intervals, c, c, bidiClass);
                        }
                    } else {                            // non-singleton
                        int s = interval[0];
                        int e = interval[1];            // inclusive
                        if (s <= 0x00FF) {
                            for (int i = s; i <= e; i++) {
                                if (i <= 0x00FF) {
                                    if (bcL1 [ i - 0x0000 ] == 0) {
                                        bcL1 [ i - 0x0000 ] = bidiClass;
                                    } else {
                                        throw new Exception ("duplicate singleton entry: " + i);
                                    }
                                } else {
                                    addInterval (intervals, i, e, bidiClass);
                                    break;
                                }
                            }
                        } else if ((s >= 0x0590) && (s <= 0x06FF)) {
                            for (int i = s; i <= e; i++) {
                                if (i <= 0x06FF) {
                                    if (bcR1 [ i - 0x0590 ] == 0) {
                                        bcR1 [ i - 0x0590 ] = bidiClass;
                                    } else {
                                        throw new Exception ("duplicate singleton entry: " + i);
                                    }
                                } else {
                                    addInterval (intervals, i, e, bidiClass);
                                    break;
                                }
                            }
                        } else {
                            addInterval (intervals, s, e, bidiClass);
                        }
                    }
                } else {
                    throw new Exception ("bad syntax, line(" + lineNumber + "): " + line);
                }
            }
        }
        // compile interval search data
        int ivIndex = 0;
        int niv = intervals.size();
        bcS1 = new int [ niv ];
        bcE1 = new int [ niv ];
        bcC1 = new byte [ niv ];
        for (Iterator it = intervals.iterator(); it.hasNext(); ivIndex++) {
            Interval iv = (Interval) it.next();
            bcS1[ivIndex] = iv.start;
            bcE1[ivIndex] = iv.end;
            bcC1[ivIndex] = (byte) iv.bidiClass;
        }
        // test data
        test();
    }

    private static int[] parseInterval (String interval) throws Exception {
        int s;
        int e;
        String[] fa = interval.split("\\.\\.");
        if (fa.length == 1) {
            s = Integer.parseInt (fa[0], 16);
            e = s;
        } else if (fa.length == 2) {
            s = Integer.parseInt (fa[0], 16);
            e = Integer.parseInt (fa[1], 16);
        } else {
            throw new Exception ("bad interval syntax: " + interval);
        }
        if (e < s) {
            throw new Exception ("bad interval, start must be less than or equal to end: " + interval);
        }
        return new int[] {s, e};
    }

    private static int parseBidiClass (String bidiClass) {
        int bc = 0;
        if ("L".equals (bidiClass)) {
            bc = BidiConstants.L;
        } else if ("LRE".equals (bidiClass)) {
            bc = BidiConstants.LRE;
        } else if ("LRO".equals (bidiClass)) {
            bc = BidiConstants.LRO;
        } else if ("R".equals (bidiClass)) {
            bc = BidiConstants.R;
        } else if ("AL".equals (bidiClass)) {
            bc = BidiConstants.AL;
        } else if ("RLE".equals (bidiClass)) {
            bc = BidiConstants.RLE;
        } else if ("RLO".equals (bidiClass)) {
            bc = BidiConstants.RLO;
        } else if ("PDF".equals (bidiClass)) {
            bc = BidiConstants.PDF;
        } else if ("EN".equals (bidiClass)) {
            bc = BidiConstants.EN;
        } else if ("ES".equals (bidiClass)) {
            bc = BidiConstants.ES;
        } else if ("ET".equals (bidiClass)) {
            bc = BidiConstants.ET;
        } else if ("AN".equals (bidiClass)) {
            bc = BidiConstants.AN;
        } else if ("CS".equals (bidiClass)) {
            bc = BidiConstants.CS;
        } else if ("NSM".equals (bidiClass)) {
            bc = BidiConstants.NSM;
        } else if ("BN".equals (bidiClass)) {
            bc = BidiConstants.BN;
        } else if ("B".equals (bidiClass)) {
            bc = BidiConstants.B;
        } else if ("S".equals (bidiClass)) {
            bc = BidiConstants.S;
        } else if ("WS".equals (bidiClass)) {
            bc = BidiConstants.WS;
        } else if ("ON".equals (bidiClass)) {
            bc = BidiConstants.ON;
        } else {
            throw new IllegalArgumentException ("unknown bidi class: " + bidiClass);
        }
        return bc;
    }

    private static void addInterval (SortedSet intervals, int start, int end, int bidiClass) {
        intervals.add (new Interval (start, end, bidiClass));
    }

    private static void dumpData (PrintWriter out) {
        boolean first;
        StringBuffer sb = new StringBuffer();

        // bcL1
        first = true;
        sb.setLength(0);
        out.println ("private static byte[] bcL1 = {");
        for (int i = 0; i < bcL1.length; i++) {
            if (! first) {
                sb.append (",");
            } else {
                first = false;
            }
            sb.append (bcL1[i]);
            if (sb.length() > 120) {
                sb.append(',');
                out.println(sb);
                first = true;
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            out.println(sb);
        }
        out.println ("};");
        out.println();

        // bcR1
        first = true;
        sb.setLength(0);
        out.println ("private static byte[] bcR1 = {");
        for (int i = 0; i < bcR1.length; i++) {
            if (! first) {
                sb.append (",");
            } else {
                first = false;
            }
            sb.append (bcR1[i]);
            if (sb.length() > 120) {
                sb.append(',');
                out.println(sb);
                first = true;
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            out.println(sb);
        }
        out.println ("};");
        out.println();

        // bcS1
        first = true;
        sb.setLength(0);
        out.println ("private static int[] bcS1 = {");
        for (int i = 0; i < bcS1.length; i++) {
            if (! first) {
                sb.append (",");
            } else {
                first = false;
            }
            sb.append (bcS1[i]);
            if (sb.length() > 120) {
                sb.append(',');
                out.println(sb);
                first = true;
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            out.println(sb);
        }
        out.println ("};");
        out.println();

        // bcE1
        first = true;
        sb.setLength(0);
        out.println ("private static int[] bcE1 = {");
        for (int i = 0; i < bcE1.length; i++) {
            if (! first) {
                sb.append (",");
            } else {
                first = false;
            }
            sb.append (bcE1[i]);
            if (sb.length() > 120) {
                sb.append(',');
                out.println(sb);
                first = true;
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            out.println(sb);
        }
        out.println ("};");
        out.println();

        // bcC1
        first = true;
        sb.setLength(0);
        out.println ("private static byte[] bcC1 = {");
        for (int i = 0; i < bcC1.length; i++) {
            if (! first) {
                sb.append (",");
            } else {
                first = false;
            }
            sb.append (bcC1[i]);
            if (sb.length() > 120) {
                sb.append(',');
                out.println(sb);
                first = true;
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            out.println(sb);
        }
        out.println ("};");
        out.println();
    }

    private static int getBidiClass (int ch) {
        if (ch <= 0x00FF) {
            return bcL1 [ ch - 0x0000 ];
        } else if ((ch >= 0x0590) && (ch <= 0x06FF)) {
            return bcR1 [ ch - 0x0590 ];
        } else {
            return getBidiClass (ch, bcS1, bcE1, bcC1);
        }
    }

    private static int getBidiClass (int ch, int[] sa, int[] ea, byte[] ca) {
        int k = Arrays.binarySearch (sa, ch);
        if (k >= 0) {
            return ca [ k ];
        } else {
            k = - (k + 1);
            if (k == 0) {
                return BidiConstants.L;
            } else if (ch <= ea [ k - 1 ]) {
                return ca [ k - 1 ];
            } else {
                return BidiConstants.L;
            }
        }
    }

    private static final int[] testData =                       // CSOK: ConstantName
    {
        0x000000, BidiConstants.BN,
        0x000009, BidiConstants.S,
        0x00000A, BidiConstants.B,
        0x00000C, BidiConstants.WS,
        0x000020, BidiConstants.WS,
        0x000023, BidiConstants.ET,
        0x000028, BidiConstants.ON,
        0x00002B, BidiConstants.ES,
        0x00002C, BidiConstants.CS,
        0x000031, BidiConstants.EN,
        0x00003A, BidiConstants.CS,
        0x000041, BidiConstants.L,
        0x000300, BidiConstants.NSM,
        0x000374, BidiConstants.ON,
        0x0005BE, BidiConstants.R,
        0x000601, BidiConstants.AN,
        0x000608, BidiConstants.AL,
        0x000670, BidiConstants.NSM,
        0x000710, BidiConstants.AL,
        0x0007FA, BidiConstants.R,
        0x000970, BidiConstants.L,
        0x001392, BidiConstants.ON,
        0x002000, BidiConstants.WS,
        0x00200E, BidiConstants.L,
        0x00200F, BidiConstants.R,
        0x00202A, BidiConstants.LRE,
        0x00202B, BidiConstants.RLE,
        0x00202C, BidiConstants.PDF,
        0x00202D, BidiConstants.LRO,
        0x00202E, BidiConstants.RLO,
        0x0020E1, BidiConstants.NSM,
        0x002212, BidiConstants.ES,
        0x002070, BidiConstants.EN,
        0x003000, BidiConstants.WS,
        0x003009, BidiConstants.ON,
        0x00FBD4, BidiConstants.AL,
        0x00FE69, BidiConstants.ET,
        0x00FF0C, BidiConstants.CS,
        0x00FEFF, BidiConstants.BN,
        0x01034A, BidiConstants.L,
        0x010E60, BidiConstants.AN,
        0x01F100, BidiConstants.EN,
        0x0E0001, BidiConstants.BN,
        0x0E0100, BidiConstants.NSM,
        0x10FFFF, BidiConstants.BN
    };

    private static void test() throws Exception {
        for (int i = 0, n = testData.length / 2; i < n; i++) {
            int ch = testData [ i * 2 + 0 ];
            int tc = testData [ i * 2 + 1 ];
            int bc = getBidiClass (ch);
            if (bc != tc) {
                throw new Exception ("test mapping failed for character (0x" + Integer.toHexString(ch) + "): expected " + tc + ", got " + bc);
            }
        }
    }

    /**
     * Main entry point for generator.
     * @param args array of command line arguments
     */
    public static void main(String[] args) {
        String bidiFileName = "http://www.unicode.org/Public/UNIDATA/extracted/DerivedBidiClass.txt";
        String outFileName = "BidiClass.java";
        boolean ok = true;
        for (int i = 0; i < args.length; i = i + 2) {
            if (i + 1 == args.length) {
                ok = false;
            } else {
                String opt = args[i];
                if ("-b".equals(opt)) {
                    bidiFileName = args [i + 1];
                } else if ("-o".equals(opt)) {
                    outFileName = args [i + 1];
                } else {
                    ok = false;
                }
            }
        }
        if (!ok) {
            System.out.println("Usage: GenerateBidiClass [-b <bidiFile>] [-o <outputFile>]");
            System.out.println("  defaults:");
            System.out.println("    <bidiFile>:     " + bidiFileName);
            System.out.println("    <outputFile>:        " + outFileName);
        } else {
            try {
                convertBidiClassProperties(bidiFileName, outFileName);
                System.out.println("Generated " + outFileName + " from");
                System.out.println("  <bidiFile>:     " + bidiFileName);
            } catch (Exception e) {
                System.out.println("An unexpected error occured");
                e.printStackTrace();
            }
        }
    }

    private static class Interval implements Comparable {
        int start;                                              // CSOK: VisibilityModifier
        int end;                                                // CSOK: VisibilityModifier
        int bidiClass;                                          // CSOK: VisibilityModifier
        Interval (int start, int end, int bidiClass) {
            this.start = start;
            this.end = end;
            this.bidiClass = bidiClass;
        }
        public int compareTo (Object o) {
            Interval iv = (Interval) o;
            if (start < iv.start) {
                return -1;
            } else if (start > iv.start) {
                return 1;
            } else if (end < iv.end) {
                return -1;
            } else if (end > iv.end) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
