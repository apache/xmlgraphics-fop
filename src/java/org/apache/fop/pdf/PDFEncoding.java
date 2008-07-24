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
     * Indicates whether a given encoding is one of the predefined encodings.
     * @param name the encoding name (ex. "StandardEncoding")
     * @return true if it is a predefined encoding
     */
    public static boolean isPredefinedEncoding(String name) {
        return PREDEFINED_ENCODINGS.contains(name);
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

        private PDFArray differences = new PDFArray();
        private int currentCode = -1;

        /**
         * Start a new difference.
         * @param code the starting code index inside the encoding
         * @return this builder instance
         */
        public DifferencesBuilder addDifference(int code) {
            this.currentCode = code;
            this.differences.add(new Integer(code));
            return this;
        }

        /**
         * Adds a character name to the current difference.
         * @param name the character name
         * @return this builder instance
         */
        public DifferencesBuilder addName(String name) {
            if (this.currentCode < 0) {
                throw new IllegalStateException("addDifference(int) must be called first");
            }
            this.differences.add(new PDFName(name));
            return this;
        }

        /**
         * Indicates whether any differences have been recorded.
         * @return true if there are differences.
         */
        public boolean hasDifferences() {
            return (this.differences.length() > 0);
        }

        /**
         * Creates and returns the PDFArray representing the Differences entry.
         * @return the Differences entry
         */
        public PDFArray toPDFArray() {
            return this.differences;
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
