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

package org.apache.fop.pdf;

// Java
import java.util.Collections;
import java.util.Set;

import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.SingleByteEncoding;

/**
 * Class representing an /Encoding object.
 *
 * A small object expressing the base encoding name and
 * the differences from the base encoding.
 *
 * The three base encodings are given by their name.
 *
 * Encodings are specified in section 5.5.5 of the PDF 1.4 spec.
 */
public class PDFEncoding extends PDFDictionary {

    /** the name for the standard encoding scheme */
    public static final String STANDARD_ENCODING = "StandardEncoding";
    /** the name for the Mac Roman encoding scheme */
    public static final String MAC_ROMAN_ENCODING = "MacRomanEncoding";
    /** the name for the Mac Export encoding scheme */
    public static final String MAC_EXPERT_ENCODING = "MacExpertEncoding";
    /** the name for the WinAnsi encoding scheme */
    public static final String WIN_ANSI_ENCODING = "WinAnsiEncoding";
    /** the name for the PDF document encoding scheme */
    public static final String PDF_DOC_ENCODING = "PDFDocEncoding";

    /** the set of predefined encodings that can be assumed present in a PDF viewer */
    private static final Set PREDEFINED_ENCODINGS;

    static {
        Set encodings = new java.util.HashSet();
        encodings.add(STANDARD_ENCODING);
        encodings.add(MAC_ROMAN_ENCODING);
        encodings.add(MAC_EXPERT_ENCODING);
        encodings.add(WIN_ANSI_ENCODING);
        encodings.add(PDF_DOC_ENCODING);
        PREDEFINED_ENCODINGS = Collections.unmodifiableSet(encodings);
    }

    /**
     * Create a new /Encoding object.
     *
     * @param basename the name of the character encoding schema
     */
    public PDFEncoding(String basename) {
        super();

        put("Type", new PDFName("Encoding"));
        if (basename != null) {
            put("BaseEncoding", new PDFName(basename));
        }
    }

    /**
     * Creates a PDFEncoding instance from a CodePointMapping instance.
     * @param encoding the code point mapping (encoding)
     * @param fontName ...
     * @return the PDF Encoding dictionary (or a String with the predefined encoding)
     */
    static Object createPDFEncoding(SingleByteEncoding encoding, String fontName) {
        //If encoding type is null, return null which causes /Encoding to be omitted.
        if (encoding == null) {
            return null;
        }
        String encodingName = null;
        SingleByteEncoding baseEncoding;
        if (fontName.indexOf("Symbol") >= 0) {
            baseEncoding = CodePointMapping.getMapping(CodePointMapping.SYMBOL_ENCODING);
            encodingName = baseEncoding.getName();
        } else {
            baseEncoding = CodePointMapping.getMapping(CodePointMapping.STANDARD_ENCODING);
        }
        PDFEncoding pdfEncoding = new PDFEncoding(encodingName);
        PDFEncoding.DifferencesBuilder builder = pdfEncoding.createDifferencesBuilder();
        PDFArray differences = builder.buildDifferencesArray(baseEncoding, encoding);
        // TODO This method should not be returning an Object with two different outcomes
        // resulting in subsequent `if (X instanceof Y)` statements.
        if (differences.length() > 0) {
            pdfEncoding.setDifferences(differences);
            return pdfEncoding;
        } else {
            return encodingName;
        }
    }

    /**
     * Indicates whether a given encoding is one of the predefined encodings.
     * @param name the encoding name (ex. "StandardEncoding")
     * @return true if it is a predefined encoding
     */
    public static boolean isPredefinedEncoding(String name) {
        return PREDEFINED_ENCODINGS.contains(name);
    }

    /**
     * Indicates whether the given encoding type is that of standard encoding
     * @param name The encoding name
     * @return Returns true if it is of type standard encoding
     */
    static boolean hasStandardEncoding(String encodingName) {
        return encodingName.equals(STANDARD_ENCODING);
    }

    /**
     * Creates and returns a new DifferencesBuilder instance for constructing the Differences
     * array.
     * @return the DifferencesBuilder
     */
    public DifferencesBuilder createDifferencesBuilder() {
        return new DifferencesBuilder();
    }

    /**
     * Sets the Differences value.
     * @param differences the differences.
     */
    public void setDifferences(PDFArray differences) {
        put("Differences", differences);
    }

    /**
     * Builder class for constructing the Differences array.
     */
    public class DifferencesBuilder {

        private int currentCode = -1;

        /**
         * Creates an array containing the differences between two single-byte.
         * font encodings.
         * @param encoding_A The first single-byte encoding
         * @param encoding_B The second single-byte encoding
         * @return The PDFArray of differences between encodings
         */
        public PDFArray buildDifferencesArray(SingleByteEncoding encodingA,
                SingleByteEncoding encodingB) {
            PDFArray differences = new PDFArray();
            int start = -1;
            String[] baseNames = encodingA.getCharNameMap();
            String[] charNameMap = encodingB.getCharNameMap();
            for (int i = 0, ci = charNameMap.length; i < ci; i++) {
                String basec = baseNames[i];
                String c = charNameMap[i];
                if (!basec.equals(c)) {
                    if (start != i) {
                        addDifference(i, differences);
                        start = i;
                    }
                    addName(c, differences);
                    start++;
                }
            }
            return differences;
        }

        /**
         * Start a new difference.
         * @param code the starting code index inside the encoding
         * @return this builder instance
         */
        private void addDifference(int code, PDFArray differences) {
            this.currentCode = code;
            differences.add(Integer.valueOf(code));
        }

        /**
         * Adds a character name to the current difference.
         * @param name the character name
         * @return this builder instance
         */
        private void addName(String name, PDFArray differences) {
            if (this.currentCode < 0) {
                throw new IllegalStateException("addDifference(int) must be called first");
            }
            differences.add(new PDFName(name));
        }
    }

    /*
     * example (p. 214)
     * 25 0 obj
     * <<
     * /Type /Encoding
     * /Differences [39 /quotesingle 96 /grave 128
     * /Adieresis /Aring /Ccedilla /Eacute /Ntilde
     * /Odieresis /Udieresis /aacute /agrave]
     * >>
     * endobj
     */
}
