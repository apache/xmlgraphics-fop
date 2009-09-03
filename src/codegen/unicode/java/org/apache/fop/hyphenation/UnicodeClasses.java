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

package org.apache.fop.hyphenation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.fop.util.License;

/**
 * Create the default classes file classes.xml,
 * for use in building hyphenation patterns
 * from pattern files which do not contain their own classes.
 * The class contains three methods to do that.
 * The method fromJava gets its infirmation from Java's compiled-in Unicode Character Database,
 * the method fromUCD gets its information from the UCD files,
 * the method fromTeX gets its information from the file unicode-letters-XeTeX.tex,
 * which is the basis of XeTeX's unicode support.
 * In the build file only the method from UCD is used;
 * the other two methods are there for demonstration.
 * The methods fromJava and fromTeX are commented out because they are not Java 1.4 compliant.
 */
public final class UnicodeClasses {
    
    public static String UNICODE_DIR = "http://www.unicode.org/Public/UNIDATA/";

    /**
     * Disallow constructor for this utility class
     */
    private UnicodeClasses() { }

    /**
     * Write a comment that this is a generated file,
     * and instructions on how to generate it
     * @param w the writer which writes the comment
     * @throws IOException if the write operation fails
     */
    public static void writeGenerated(Writer w) throws IOException {
        w.write("<!-- !!! THIS IS A GENERATED FILE !!!             -->\n");
        w.write("<!-- If updates are needed, then:                 -->\n");
        w.write("<!-- * run 'ant codegen-hyphenation-classes',     -->\n");
        w.write("<!--   which will generate a new file classes.xml -->\n");
        w.write("<!--   in 'src/java/org/apache/fop/hyphenation'   -->\n");
        w.write("<!-- * commit the changed file                    -->\n");
    }
    
    /**
     * Generate classes.xml from Java's compiled-in Unicode Character Database
     * @param hexcode whether to prefix each class with the hexcode (only for debugging purposes)
     * @param outfilePath output file
     * @throws IOException
     */
    public static void fromJava(boolean hexcode, String outfilePath) throws IOException {
        File f = new File(outfilePath);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        FileOutputStream fw = new FileOutputStream(f);
        OutputStreamWriter ow = new OutputStreamWriter(fw, "utf-8");
        int maxChar;
        maxChar = Character.MAX_VALUE;

        ow.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        License.writeXMLLicenseId(ow);
        ow.write("\n");
        writeGenerated(ow);
        ow.write("\n");
        ow.write("<classes>\n");
        // loop over the first Unicode plane
        for (int code = Character.MIN_VALUE; code <= maxChar; ++code) {
            
            // skip surrogate area
            if (code == Character.MIN_SURROGATE) {
                code = Character.MAX_SURROGATE;
                continue;
            }

            // we are only interested in LC, UC and TC letters which are their own LC,
            // and in 'other letters'
            if (!(((Character.isLowerCase(code) || Character.isUpperCase(code)
                    || Character.isTitleCase(code))
                    && code == Character.toLowerCase(code))
                    || Character.getType(code) == Character.OTHER_LETTER)) {
                continue;
            }
            
            // skip a number of blocks
            Character.UnicodeBlock ubi = Character.UnicodeBlock.of(code);
            if (ubi.equals(Character.UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS)
                    || ubi.equals(Character.UnicodeBlock.LETTERLIKE_SYMBOLS)
                    || ubi.equals(Character.UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS)
                    || ubi.equals(Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)
                    || ubi.equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                    || ubi.equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
                    || ubi.equals(Character.UnicodeBlock.HANGUL_SYLLABLES)) {
                continue;
            }

            int uppercode = Character.toUpperCase(code);
            int titlecode = Character.toTitleCase(code);
            StringBuilder s = new StringBuilder();
            if (hexcode) {
                s.append("0x" + Integer.toHexString(code) + " ");
            }
            s.append(Character.toChars(code));
            if (uppercode != code) {
                s.append(Character.toChars(uppercode));
            }
            if (titlecode != code && titlecode != uppercode) {
                s.append(Character.toChars(titlecode));
            }
            ow.write(s.toString() + "\n");
        }
        ow.write("</classes>\n");
        ow.flush();
        ow.close();
    }
    
    
    /**
     * The column numbers in the UCD file
     */
    public static final int UNICODE = 0, GENERAL_CATEGORY = 2, SIMPLE_UPPERCASE_MAPPING = 12,
    SIMPLE_LOWERCASE_MAPPING = 13, SIMPLE_TITLECASE_MAPPING = 14, NUM_FIELDS = 15;
    
    /**
     * Generate classes.xml from Unicode Character Database files
     * @param hexcode whether to prefix each class with the hexcode (only for debugging purposes)
     * @param unidataPath path to the directory with UCD files  
     * @param outfilePath output file
     * @throws IOException if the input files are not found
     * @throws URISyntaxException 
     * @throws FOPException 
     */
    public static void fromUCD(boolean hexcode, String unidataPath, String outfilePath)
    throws IOException, URISyntaxException {
        URI unidata;
        if (unidataPath.endsWith("/")) {
            unidata = new URI(unidataPath);
        } else {
            unidata = new URI(unidataPath + "/");
        }
        String scheme = unidata.getScheme();
        if (scheme == null || !(scheme.equals("file") || scheme.equals("http"))) {
            throw new FileNotFoundException
            ("URI with file or http scheme required for UNIDATA input directory");
        }
        
        File f = new File(outfilePath);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        FileOutputStream fw = new FileOutputStream(f);
        OutputStreamWriter ow = new OutputStreamWriter(fw, "utf-8");
        
        URI inuri = unidata.resolve("Blocks.txt");
        InputStream inis = null;
        if (scheme.equals("file")) {
            File in = new File(inuri);
            inis = new FileInputStream(in);
        } else if (scheme.equals("http")) {
            inis = inuri.toURL().openStream();
        }
        InputStreamReader insr = new InputStreamReader(inis, "utf-8");
        BufferedReader inbr = new BufferedReader(insr);
        Map blocks = new HashMap();
        for (String line = inbr.readLine(); line != null; line = inbr.readLine()) {
            if (line.startsWith("#") || line.matches("^\\s*$")) {
                continue;
            }
            String[] parts = line.split(";");
            String block = parts[1].trim();
            String[] indices = parts[0].split("\\.\\.");
            int[] ind = {Integer.parseInt(indices[0], 16), Integer.parseInt(indices[1], 16)};
            blocks.put(block, ind);
        }
        inbr.close();

        inuri = unidata.resolve("UnicodeData.txt");
        if (scheme.equals("file")) {
            File in = new File(inuri);
            inis = new FileInputStream(in);
        } else if (scheme.equals("http")) {
            inis = inuri.toURL().openStream();
        }
        insr = new InputStreamReader(inis, "utf-8");
        inbr = new BufferedReader(insr);
        int maxChar;
        maxChar = Character.MAX_VALUE;

        ow.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        License.writeXMLLicenseId(ow);
        ow.write("\n");
        writeGenerated(ow);
        ow.write("\n");
        ow.write("<classes>\n");
        for (String line = inbr.readLine(); line != null; line = inbr.readLine()) {
            String[] fields = line.split(";", NUM_FIELDS);
            int code = Integer.parseInt(fields[UNICODE], 16);
            if (code > maxChar) {
                break;
            }
            if (((fields[GENERAL_CATEGORY].equals("Ll") || fields[GENERAL_CATEGORY].equals("Lu")
                            || fields[GENERAL_CATEGORY].equals("Lt"))
                        && ("".equals(fields[SIMPLE_LOWERCASE_MAPPING])
                                || fields[UNICODE].equals(fields[SIMPLE_LOWERCASE_MAPPING])))
                    || fields[GENERAL_CATEGORY].equals("Lo")) {
                String[] blockNames = {"Superscripts and Subscripts",
                                       "Letterlike Symbols",
                                       "Alphabetic Presentation Forms",
                                       "Halfwidth and Fullwidth Forms",
                                       "CJK Unified Ideographs",
                                       "CJK Unified Ideographs Extension A",
                                       "Hangul Syllables"};
                int j;
                for (j = 0; j < blockNames.length; ++j) {
                    int[] ind = (int[]) blocks.get(blockNames[j]);
                    if (code >= ind[0] && code <= ind[1]) {
                        break;
                    }
                }
                if (j < blockNames.length) {
                    continue;
                }
            
                int uppercode = -1, titlecode = -1;
                if (!"".equals(fields[SIMPLE_UPPERCASE_MAPPING])) {
                    uppercode = Integer.parseInt(fields[SIMPLE_UPPERCASE_MAPPING], 16);
                }
                if (!"".equals(fields[SIMPLE_TITLECASE_MAPPING])) {
                    titlecode = Integer.parseInt(fields[SIMPLE_TITLECASE_MAPPING], 16);
                }
                StringBuilder s = new StringBuilder();
                if (hexcode) {
                    s.append("0x" + fields[UNICODE].replaceFirst("^0+", "").toLowerCase() + " ");
                }
                s.append(Character.toChars(code));
                if (uppercode != -1 && uppercode != code) {
                    s.append(Character.toChars(uppercode));
                }
                if (titlecode != -1 && titlecode != code && titlecode != uppercode) {
                    s.append(Character.toChars(titlecode));
                }
                ow.write(s.toString() + "\n");
            }
        }
        ow.write("</classes>\n");
        ow.flush();
        ow.close();
        inbr.close();
    }

    /**
     * Generate classes.xml from XeTeX's Unicode letters file
     * @param hexcode whether to prefix each class with the hexcode (only for debugging purposes)
     * @param lettersPath path to XeTeX's Unicode letters file unicode-letters-XeTeX.tex  
     * @param outfilePath output file
     * @throws IOException
     */
    public static void fromTeX(boolean hexcode, String lettersPath, String outfilePath)
      throws IOException {
        File in = new File(lettersPath);

        File f = new File(outfilePath);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        FileOutputStream fw = new FileOutputStream(f);
        OutputStreamWriter ow = new OutputStreamWriter(fw, "utf-8");

        FileInputStream inis = new FileInputStream(in);
        InputStreamReader insr = new InputStreamReader(inis, "utf-8");
        BufferedReader inbr = new BufferedReader(insr);

        ow.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        License.writeXMLLicenseId(ow);
        ow.write("\n");
        writeGenerated(ow);
        ow.write("\n");
        ow.write("<classes>\n");
        for (String line = inbr.readLine(); line != null; line = inbr.readLine()) {
            String[] codes = line.split("\\s+");
            if (!(codes[0].equals("\\L") || codes[0].equals("\\l"))) {
                continue;
            }
            if (codes.length == 3) {
                ow.write("\"" + line + "\" has two codes");
                continue;
            }
            if (codes[0].equals("\\l") && codes.length != 2) {
                ow.write("\"" + line + "\" should have one code");
                continue;
            }
            else if (codes[0].equals("\\L") && codes.length != 4) {
                ow.write("\"" + line + "\" should have three codes");
                continue;
            }
            if (codes[0].equals("\\l") || (codes[0].equals("\\L") && codes[1].equals(codes[3]))) {
                StringBuilder s = new StringBuilder();
                if (hexcode) {
                    s.append("0x" + codes[1].replaceFirst("^0+", "").toLowerCase() + " ");
                }
                s.append(Character.toChars(Integer.parseInt(codes[1], 16)));
                if (codes[0].equals("\\L")) {
                    s.append(Character.toChars(Integer.parseInt(codes[2], 16)));
                }
                ow.write(s.toString() + "\n");
            }
        }
        ow.write("</classes>\n");
        ow.flush();
        ow.close();
        inbr.close();
    }

    
    /**
     * @param args [--hexcode] [--java|--ucd|--tex] outfile [infile]
     * @throws IOException if the input file cannot be found
     * @throws URISyntaxException if the input URI is incorrect
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        String type = "ucd", prefix = "--", infile = null, outfile = null;
        boolean hexcode = false;
        int i;
        for (i = 0; i < args.length && args[i].startsWith(prefix); ++i) {
            String option = args[i].substring(prefix.length());
            if (option.equals("java") || option.equals("ucd") || option.equals("tex")) {
                type = option;
            } else if (option.equals("hexcode")) {
                hexcode = true;
            } else {
                System.err.println("Unknown option: " + option);
                System.exit(1);
            }
        }
        if (i < args.length) {
            outfile = args[i];
        } else {
            System.err.println("Output file is required; aborting");
            System.exit(1);
        }
        if (++i < args.length) {
            infile = args[i];
        }
        
        if (type.equals("java") && infile != null) {
                System.err.println("Type java does not allow an infile");
                System.exit(1);
        } else if (type.equals("ucd") && infile == null) {
                infile = UNICODE_DIR;
        } else if (type.equals("tex") && infile == null) {
                System.err.println("Type tex requires an input file");
                System.exit(1);
        }
        if (type.equals("java")) {
            fromJava(hexcode, outfile);
        } else if (type.equals("ucd")) {
            fromUCD(hexcode, infile, outfile);
        } else if (type.equals("tex")) {
            fromTeX(hexcode, infile, outfile);
        } else {
            System.err.println("Unknown type: " + type + ", nothing done");
            System.exit(1);
        }
    }

}
