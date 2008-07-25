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

package org.apache.fop.text.linebreak;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Utility for generating a Java class representing line break properties
 * from the Unicode property files.</p>
 * <p>Customizations:
 * <ul>
 * <li>The pair table file is a cut+paste of the sample table from the TR14
 * HTML file into a text file.</li>
 * <li>Because the sample table does not cover all line break classes, check the
 * 'not in pair table' list of property value short names.</li>
 * <li>Check MAX_LINE_LENGTH.</li>
 * </ul>
 *
 */
public class GenerateLineBreakUtils {

    private static final int MAX_LINE_LENGTH = 110;

    private static final byte DIRECT_BREAK = 0;                 // _ in table
    private static final byte INDIRECT_BREAK = 1;               // % in table
    private static final byte COMBINING_INDIRECT_BREAK = 2;     // # in table
    private static final byte COMBINING_PROHIBITED_BREAK = 3;   // @ in table
    private static final byte PROHIBITED_BREAK = 4;             // ^ in table
    private static final byte EXPLICIT_BREAK = 5;               // ! in rules
    private static final String BREAK_CLASS_TOKENS = "_%#@^!";
    private static final String notInPairTable[] = { "AI", "BK", "CB", "CR", "LF", "NL", "SA", "SG", "SP", "XX" };

    private static final byte lineBreakProperties[] = new byte[0x10000];
    private static final Map lineBreakPropertyValues = new HashMap();
    private static final List lineBreakPropertyShortNames = new ArrayList();
    private static final List lineBreakPropertyLongNames = new ArrayList();

    /**
     * Generate a class managing line break properties for Unicode characters and a sample
     * table for the table driven line breaking algorithm described in
     * <a href="http://unicode.org/reports/tr14/#PairBasedImplementation">UTR #14</a>.
     * TODO: Code points above the base plane are simply ignored.
     *
     * @param lineBreakFileName Name of line break property file (part of Unicode files).
     * @param propertyValueFileName Name of property values alias file (part of Unicode files).
     * @param breakPairFileName Name of pair table file (<i>not</i> part of the unicode files).
     * @param outFileName Name of the output file.
     * @throws Exception in case anything goes wrong.
     */
    private static void convertLineBreakProperties(
        String lineBreakFileName,
        String propertyValueFileName,
        String breakPairFileName,
        String outFileName)
        throws Exception {

        readLineBreakProperties(lineBreakFileName, propertyValueFileName);
        // read break pair table
        int lineBreakPropertyValueCount = lineBreakPropertyValues.size();
        int tableSize = lineBreakPropertyValueCount - notInPairTable.length;
        Map notInPairTableMap = new HashMap(notInPairTable.length);
        for (int i = 0; i < notInPairTable.length; i++) {
            Object v = lineBreakPropertyValues.get(notInPairTable[i]);
            if (v == null) {
                throw new Exception("'not in pair table' property not found: " + notInPairTable[i]);
            }
            notInPairTableMap.put(notInPairTable[i], v);
        }
        byte pairTable[][] = new byte[tableSize][];
        byte columnHeader[] = new byte[tableSize];
        byte rowHeader[] = new byte[tableSize];
        byte columnMap[] = new byte[lineBreakPropertyValueCount + 1];
        Arrays.fill(columnMap, (byte)255);
        byte rowMap[] = new byte[lineBreakPropertyValueCount + 1];
        Arrays.fill(rowMap, (byte)255);
        BufferedReader b = new BufferedReader(new FileReader(breakPairFileName));
        String line = b.readLine();
        int lineNumber = 1;
        String[] lineTokens;
        String name;
        // read header
        if (line != null) {
            lineTokens = line.split("\\s+");
            byte columnNumber = 0;

            for (int i = 0; i < lineTokens.length; ++i) {
                name = lineTokens[i];
                if (name.length() > 0) {
                    if (columnNumber >= columnHeader.length) {
                        throw new Exception(breakPairFileName + ':' + lineNumber + ": unexpected column header " + name);
                    }
                    if (notInPairTableMap.get(name) != null) {
                        throw new Exception(breakPairFileName + ':' + lineNumber + ": invalid column header " + name);
                    }
                    Byte v = (Byte)lineBreakPropertyValues.get(name);
                    if (v != null) {
                        byte vv = v.byteValue();
                        columnHeader[columnNumber] = vv;
                        columnMap[vv] = columnNumber;
                    } else {
                        throw new Exception(breakPairFileName + ':' + lineNumber + ": unknown column header " + name);
                    }
                    columnNumber++;
                }
            }
            if (columnNumber < columnHeader.length) {
                StringBuffer missing = new StringBuffer();
                for (int j = 0; j < lineBreakPropertyShortNames.size(); j++) {
                    boolean found = false;
                    for (int k = 0; k < columnNumber; k++) {
                        if (columnHeader[k] == j + 1) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if (missing.length() > 0) {
                            missing.append(", ");
                        }
                        missing.append((String)lineBreakPropertyShortNames.get(j));
                    }
                }
                throw new Exception(
                    breakPairFileName + ':' + lineNumber + ": missing column for properties: " + missing.toString());
            }
        } else {
            throw new Exception(breakPairFileName + ':' + lineNumber + ": can't read table header");
        }
        line = b.readLine().trim();
        lineNumber++;
        byte rowNumber = 0;
        while (line != null && line.length() > 0) {
            if (rowNumber >= rowHeader.length) {
                throw new Exception(breakPairFileName + ':' + lineNumber + ": unexpected row " + line);
            }
            pairTable[rowNumber] = new byte[tableSize];
            lineTokens = line.split("\\s+");
            if (lineTokens.length > 0) {
                name = lineTokens[0];
                if (notInPairTableMap.get(name) != null) {
                    throw new Exception(breakPairFileName + ':' + lineNumber + ": invalid row header " + name);
                }
                Byte v = (Byte)lineBreakPropertyValues.get(name);
                if (v != null) {
                    byte vv = v.byteValue();
                    rowHeader[rowNumber] = vv;
                    rowMap[vv] = rowNumber;
                } else {
                    throw new Exception(breakPairFileName + ':' + lineNumber + ": unknown row header " + name);
                }
            } else {
                throw new Exception(breakPairFileName + ':' + lineNumber + ": can't read row header");
            }
            int columnNumber = 0;
            String token;
            for (int i = 1; i < lineTokens.length; ++i) {
                token = lineTokens[i];
                if (token.length() == 1) {
                    byte tokenBreakClass = (byte)BREAK_CLASS_TOKENS.indexOf(token.charAt(0));
                    if (tokenBreakClass >= 0) {
                        pairTable[rowNumber][columnNumber] = tokenBreakClass;
                    } else {
                        throw new Exception(breakPairFileName + ':' + lineNumber + ": unexpected token: " + token);
                    }
                } else {
                    throw new Exception(breakPairFileName + ':' + lineNumber + ": token too long: " + token);
                }
                columnNumber++;
            }
            line = b.readLine().trim();
            lineNumber++;
            rowNumber++;
        }
        if (rowNumber < rowHeader.length) {
            StringBuffer missing = new StringBuffer();
            for (int j = 0; j < lineBreakPropertyShortNames.size(); j++) {
                boolean found = false;
                for (int k = 0; k < rowNumber; k++) {
                    if (rowHeader[k] == j + 1) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (missing.length() > 0) {
                        missing.append(", ");
                    }
                    missing.append((String)lineBreakPropertyShortNames.get(j));
                }
            }
            throw new Exception(
                breakPairFileName + ':' + lineNumber + ": missing row for properties: " + missing.toString());
        }

        // generate class
        int rowsize = 512;
        int blocksize = lineBreakProperties.length / rowsize;
        byte row[][] = new byte[rowsize][];
        int idx = 0;
        StringBuffer doStaticLinkCode = new StringBuffer();
        PrintWriter out = new PrintWriter(new FileWriter(outFileName));
        out.println("/*");
        out.println(" * Licensed to the Apache Software Foundation (ASF) under one or more");
        out.println(" * contributor license agreements.  See the NOTICE file distributed with");
        out.println(" * this work for additional information regarding copyright ownership.");
        out.println(" * The ASF licenses this file to You under the Apache License, Version 2.0");
        out.println(" * (the \"License\"); you may not use this file except in compliance with");
        out.println(" * the License.  You may obtain a copy of the License at");
        out.println(" * ");
        out.println(" *      http://www.apache.org/licenses/LICENSE-2.0");
        out.println(" * ");
        out.println(" * Unless required by applicable law or agreed to in writing, software");
        out.println(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
        out.println(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        out.println(" * See the License for the specific language governing permissions and");
        out.println(" * limitations under the License.");
        out.println(" */");
        out.println();
        out.println("/* $Id$ */");
        out.println();
        out.println("package org.apache.fop.text.linebreak;");
        out.println();
        out.println("/* ");
        out.println(" * !!! THIS IS A GENERATED FILE !!! ");
        out.println(" * If updates to the source are needed, then:");
        out.println(" * - apply the necessary modifications to ");
        out.println(" *   'src/codegen/unicode/java/org/apache/fop/text/linebreak/GenerateLineBreakUtils.java'");
        out.println(" * - run 'ant codegen-unicode', which will generate a new LineBreakUtils.java");
        out.println(" *   in 'src/java/org/apache/fop/text/linebreak'");
        out.println(" * - commit BOTH changed files");
        out.println(" */");
        out.println();
        out.println("public final class LineBreakUtils {");
        out.println();
        out.println("    /** Break class constant */");
        out.println("    public static final byte DIRECT_BREAK = " + DIRECT_BREAK + ';');
        out.println("    /** Break class constant */");
        out.println("    public static final byte INDIRECT_BREAK = " + INDIRECT_BREAK + ';');
        out.println("    /** Break class constant */");
        out.println("    public static final byte COMBINING_INDIRECT_BREAK = " + COMBINING_INDIRECT_BREAK + ';');
        out.println("    /** Break class constant */");
        out.println("    public static final byte COMBINING_PROHIBITED_BREAK = " + COMBINING_PROHIBITED_BREAK + ';');
        out.println("    /** Break class constant */");
        out.println("    public static final byte PROHIBITED_BREAK = " + PROHIBITED_BREAK + ';');
        out.println("    /** Break class constant */");
        out.println("    public static final byte EXPLICIT_BREAK = " + EXPLICIT_BREAK + ';');
        out.println();
        out.println("    private static final byte PAIR_TABLE[][] = {");
        boolean printComma = false;
        for (int i = 1; i <= lineBreakPropertyValueCount; i++) {
            if (printComma) {
                out.println(", ");
            } else {
                printComma = true;
            }
            out.print("        {");
            boolean localPrintComma = false;
            for (int j = 1; j <= lineBreakPropertyValueCount; j++) {
                if (localPrintComma) {
                    out.print(", ");
                } else {
                    localPrintComma = true;
                }
                if (columnMap[j] != -1 && rowMap[i] != -1) {
                    out.print(pairTable[rowMap[i]][columnMap[j]]);
                } else {
                    out.print('0');
                }
            }
            out.print('}');
        }
        out.println("};");
        out.println();
        out.println("    private static byte lineBreakProperties[][] = new byte[" + rowsize + "][];");
        out.println();
        out.println("    private static void init_0() {");
        int rowsPrinted = 0;
        int initSections = 0;
        for (int i = 0; i < rowsize; i++) {
            boolean found = false;
            for (int j = 0; j < i; j++) {
                if (row[j] != null) {
                    boolean matched = true;
                    for (int k = 0; k < blocksize; k++) {
                        if (row[j][k] != lineBreakProperties[idx + k]) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        found = true;
                        doStaticLinkCode.append("        lineBreakProperties[");
                        doStaticLinkCode.append(i);
                        doStaticLinkCode.append("] = lineBreakProperties[");
                        doStaticLinkCode.append(j);
                        doStaticLinkCode.append("];\n");
                        break;
                    }
                }
            }
            if (!found) {
                if (rowsPrinted >= 64) {
                    out.println("    }");
                    out.println();
                    initSections++;
                    out.println("    private static void init_" + initSections + "() {");
                    rowsPrinted = 0;
                }
                row[i] = new byte[blocksize];
                boolean printLocalComma = false;
                out.print("        lineBreakProperties[" + i + "] = new byte[] { ");
                for (int k = 0; k < blocksize; k++) {
                    row[i][k] = lineBreakProperties[idx + k];
                    if (printLocalComma) {
                        out.print(", ");
                    } else {
                        printLocalComma = true;
                    }
                    out.print(row[i][k]);
                }
                out.println("};");
                rowsPrinted++;
            }
            idx += blocksize;
        }
        out.println("    }");
        out.println();
        out.println("    static {");
        for (int i = 0; i <= initSections; i++) {
            out.println("        init_" + i + "();");
        }
        out.print(doStaticLinkCode);
        out.println("    }");
        out.println();
        for (int i = 0; i < lineBreakPropertyShortNames.size(); i++) {
            String shortName = (String)lineBreakPropertyShortNames.get(i);
            out.println("    /** Linebreak property constant */");
            out.print("    public static final byte LINE_BREAK_PROPERTY_");
            out.print(shortName);
            out.print(" = ");
            out.print(i + 1);
            out.println(';');
        }
        out.println();
        final String shortNamePrefix = "    private static String lineBreakPropertyShortNames[] = {";
        out.print(shortNamePrefix);
        int lineLength = shortNamePrefix.length();
        printComma = false;
        for (int i = 0; i < lineBreakPropertyShortNames.size(); i++) {
            name = (String)lineBreakPropertyShortNames.get(i);
            if (printComma) {
                out.print(", ");
                lineLength++;
            } else {
                printComma = true;
            }
            if (lineLength > MAX_LINE_LENGTH) {
                out.println();
                out.print("        ");
                lineLength = 8;
            }
            out.print('"');
            out.print(name);
            out.print('"');
            lineLength += (2 + name.length());
        }
        out.println("};");
        out.println();
        final String longNamePrefix = "    private static String lineBreakPropertyLongNames[] = {";
        out.print(longNamePrefix);
        lineLength = longNamePrefix.length();
        printComma = false;
        for (int i = 0; i < lineBreakPropertyLongNames.size(); i++) {
            name = (String)lineBreakPropertyLongNames.get(i);
            if (printComma) {
                out.print(',');
                lineLength++;
            } else {
                printComma = true;
            }
            if (lineLength > MAX_LINE_LENGTH) {
                out.println();
                out.print("        ");
                lineLength = 8;
            }
            out.print('"');
            out.print(name);
            out.print('"');
            lineLength += (2 + name.length());
        }
        out.println("};");
        out.println();
        out.println("    /**");
        out.println("     * Return the short name for the linebreak property corresponding ");
        out.println("     * to the given symbolic constant.");
        out.println("     *");
        out.println("     * @param i the numeric value of the linebreak property");
        out.println("     * @return the short name of the linebreak property");
        out.println("     */");
        out.println("    public static String getLineBreakPropertyShortName(byte i) {");
        out.println("        if (i > 0 && i <= lineBreakPropertyShortNames.length) {");
        out.println("            return lineBreakPropertyShortNames[i - 1];");
        out.println("        } else {");
        out.println("            return null;");
        out.println("        }");
        out.println("    }");
        out.println();
        out.println("    /**");
        out.println("     * Return the long name for the linebreak property corresponding ");
        out.println("     * to the given symbolic constant.");
        out.println("     *");
        out.println("     * @param i the numeric value of the linebreak property");
        out.println("     * @return the long name of the linebreak property");
        out.println("     */");
        out.println("    public static String getLineBreakPropertyLongName(byte i) {");
        out.println("        if (i > 0 && i <= lineBreakPropertyLongNames.length) {");
        out.println("            return lineBreakPropertyLongNames[i - 1];");
        out.println("        } else {");
        out.println("            return null;");
        out.println("        }");
        out.println("    }");
        out.println();
        out.println("    /**");
        out.println("     * Return the linebreak property constant for the given <code>char</code>");
        out.println("     *");
        out.println("     * @param c the <code>char</code> whose linebreak property to return");
        out.println("     * @return the constant representing the linebreak property");
        out.println("     */");
        out.println("    public static byte getLineBreakProperty(char c) {");
        out.println("        return lineBreakProperties[c / " + blocksize + "][c % " + blocksize + "];");
        out.println("    }");
        out.println();
        out.println("    /**");
        out.println("     * Return the break class constant for the given pair of linebreak ");
        out.println("     * property constants.");
        out.println("     *");
        out.println("     * @param lineBreakPropertyBefore the linebreak property for the first character");
        out.println("     *        in a two-character sequence");
        out.println("     * @param lineBreakPropertyAfter the linebreak property for the second character");
        out.println("     *        in a two-character sequence");
        out.println("     * @return the constant representing the break class");
        out.println("     */");
        out.println(
            "    public static byte getLineBreakPairProperty(int lineBreakPropertyBefore, int lineBreakPropertyAfter) {");
        out.println("        return PAIR_TABLE[lineBreakPropertyBefore - 1][lineBreakPropertyAfter - 1];");
        out.println("    }");
        out.println();
        out.println("}");
        out.flush();
        out.close();
    }

    /**
     * Read line break property value names and the actual properties for the Unicode
     * characters from the respective Unicode files.
     * TODO: Code points above the base plane are simply ignored.
     *
     * @param lineBreakFileName Name of line break property file.
     * @param propertyValueFileName Name of property values alias file.
     * @throws Exception in case anything goes wrong.
     */
    private static void readLineBreakProperties(String lineBreakFileName, String propertyValueFileName)
        throws Exception {
        // read property names
        BufferedReader b = new BufferedReader(new InputStreamReader(new URL(propertyValueFileName).openStream()));
        String line = b.readLine();
        int lineNumber = 1;
        byte propertyIndex = 1;
        byte indexForUnknown = 0;
        while (line != null) {
            if (line.startsWith("lb")) {
                String shortName;
                String longName = null;
                int semi = line.indexOf(';');
                if (semi < 0) {
                    throw new Exception(
                        propertyValueFileName + ':' + lineNumber + ": missing property short name in " + line);
                }
                line = line.substring(semi + 1);
                semi = line.indexOf(';');
                if (semi > 0) {
                    shortName = line.substring(0, semi).trim();
                    longName = line.substring(semi + 1).trim();
                    semi = longName.indexOf(';');
                    if (semi > 0) {
                        longName = longName.substring(0, semi).trim();
                    }
                } else {
                    shortName = line.trim();
                }
                if (shortName.equals("XX")) {
                    indexForUnknown = propertyIndex;
                }
                lineBreakPropertyValues.put(shortName, new Byte((byte)propertyIndex));
                lineBreakPropertyShortNames.add(shortName);
                lineBreakPropertyLongNames.add(longName);
                propertyIndex++;
                if (propertyIndex <= 0) {
                    throw new Exception(propertyValueFileName + ':' + lineNumber + ": property rolled over in " + line);
                }
            }
            line = b.readLine();
            lineNumber++;
        }
        if (indexForUnknown == 0) {
            throw new Exception("index for XX (unknown) line break property value not found");
        }

        // read property values
        Arrays.fill(lineBreakProperties, (byte)0);
        b = new BufferedReader(new InputStreamReader(new URL(lineBreakFileName).openStream()));
        line = b.readLine();
        lineNumber = 1;
        while (line != null) {
            int idx = line.indexOf('#');
            if (idx >= 0) {
                line = line.substring(0, idx);
            }
            line = line.trim();
            if (line.length() > 0) {
                idx = line.indexOf(';');
                if (idx <= 0) {
                    throw new Exception(lineBreakFileName + ':' + lineNumber + ": No field delimiter in " + line);
                }
                Byte v = (Byte)lineBreakPropertyValues.get(line.substring(idx + 1).trim());
                if (v == null) {
                    throw new Exception(lineBreakFileName + ':' + lineNumber + ": Unknown property value in " + line);
                }
                String codepoint = line.substring(0, idx);
                int low, high;
                idx = codepoint.indexOf("..");
                try {
                    if (idx >= 0) {
                        low = Integer.parseInt(codepoint.substring(0, idx), 16);
                        high = Integer.parseInt(codepoint.substring(idx + 2), 16);
                    } else {
                        low = Integer.parseInt(codepoint, 16);
                        high = low;
                    }
                } catch (NumberFormatException e) {
                    throw new Exception(lineBreakFileName + ':' + lineNumber + ": Invalid codepoint number in " + line);
                }
                if (high > 0xFFFF) {
                    // ignore non-baseplane characters for now

                } else {
                    if (low < 0 || high < 0) {
                        throw new Exception(
                            lineBreakFileName + ':' + lineNumber + ": Negative codepoint(s) in " + line);
                    }
                    byte vv = v.byteValue();
                    for (int i = low; i <= high; i++) {
                        if (lineBreakProperties[i] != 0) {
                            throw new Exception(
                                lineBreakFileName
                                    + ':'
                                    + lineNumber
                                    + ": Property already set for "
                                    + ((char)i)
                                    + " in "
                                    + line);
                        }
                        lineBreakProperties[i] = vv;
                    }
                }
            }
            line = b.readLine();
            lineNumber++;
        }
    }

    /**
     * Determine a good block size for the two stage optimized storage of the
     * line breaking properties. Note: the memory utilization calculation is a rule of thumb,
     * don't take it too serious.
     *
     * @param lineBreakFileName Name of line break property file.
     * @param propertyValueFileName Name of property values alias file.
     * @throws Exception in case anything goes wrong.
     */
    private static void optimizeBlocks(String lineBreakFileName, String propertyValueFileName) throws Exception {
        readLineBreakProperties(lineBreakFileName, propertyValueFileName);
        for (int i = 0; i < 16; i++) {
            int rowsize = 1 << i;
            int blocksize = lineBreakProperties.length / (rowsize);
            byte row[][] = new byte[rowsize][];
            int idx = 0;
            int nrOfDistinctBlocks = 0;
            for (int j = 0; j < rowsize; j++) {
                byte block[] = new byte[blocksize];
                for (int k = 0; k < blocksize; k++) {
                    block[k] = lineBreakProperties[idx];
                    idx++;
                }
                boolean found = false;
                for (int k = 0; k < j; k++) {
                    if (row[k] != null) {
                        boolean matched = true;
                        for (int l = 0; l < blocksize; l++) {
                            if (row[k][l] != block[l]) {
                                matched = false;
                                break;
                            }
                        }
                        if (matched) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    row[j] = block;
                    nrOfDistinctBlocks++;
                } else {
                    row[j] = null;
                }
            }
            int size = rowsize * 4 + nrOfDistinctBlocks * blocksize;
            System.out.println(
                "i=" + i + " blocksize=" + blocksize + " blocks=" + nrOfDistinctBlocks + " size=" + size);
        }
    }

    public static void main(String[] args) {
        String lineBreakFileName = "http://www.unicode.org/Public/UNIDATA/LineBreak.txt";
        String propertyValueFileName = "http://www.unicode.org/Public/UNIDATA/PropertyValueAliases.txt";
        String breakPairFileName = "src/codegen/unicode/data/LineBreakPairTable.txt";
        String outFileName = "LineBreakUtils.java";
        boolean ok = true;
        for (int i = 0; i < args.length; i = i + 2) {
            if (i + 1 == args.length) {
                ok = false;
            } else {
                String opt = args[i];
                if ("-l".equals(opt)) {
                    lineBreakFileName = args[i+1];
                } else if ("-p".equals(opt)) {
                    propertyValueFileName = args[i+1];
                } else if ("-b".equals(opt)) {
                    breakPairFileName = args[i+1];
                } else if("-o".equals(opt)) {
                    outFileName = args[i+1];
                } else {
                    ok = false;
                }
            }
        }
        if (!ok) {
            System.out.println("Usage: GenerateLineBreakUtils [-l <lineBreakFile>] [-p <propertyValueFile>] [-b <breakPairFile>] [-o <outputFile>]");
            System.out.println("  defaults:");
            System.out.println("    <lineBreakFile>:     " + lineBreakFileName);
            System.out.println("    <propertyValueFile>: " + propertyValueFileName);
            System.out.println("    <breakPairFile>:     " + breakPairFileName);
            System.out.println("    <outputFile>:        " + outFileName);
        } else {
            try {
                convertLineBreakProperties(lineBreakFileName, propertyValueFileName, breakPairFileName, outFileName);
                System.out.println("Generated " + outFileName + " from");
                System.out.println("  <lineBreakFile>:     " + lineBreakFileName);
                System.out.println("  <propertyValueFile>: " + propertyValueFileName);
                System.out.println("  <breakPairFile>:     " + breakPairFileName);
            } catch (Exception e) {
                System.out.println("An unexpected error occured");
                e.printStackTrace();
            }
        }
    }
}
