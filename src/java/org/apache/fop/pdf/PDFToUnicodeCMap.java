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

import org.apache.fop.fonts.CIDFont;

/**
 * Class representing ToUnicode CMaps.
 * Here are some documentation resources:
 * <ul>
 * <li>PDF Reference, Second Edition, Section 5.6.4, for general information
 * about CMaps in PDF Files.</li>
 * <li>PDF Reference, Second Edition, Section 5.9, for specific information
 * about ToUnicodeCMaps in PDF Files.</li>
 * <li>
 * <a href="http://partners.adobe.com/asn/developer/pdfs/tn/5411.ToUnicode.pdf">
 * Adobe Technical Note #5411, "ToUnicode Mapping File Tutorial"</a>.
 * </ul>
 */
public class PDFToUnicodeCMap extends PDFCMap {

    /**
     * handle to read font
     */
    protected CIDFont cidFont;

    /**
     * Constructor.
     *
     * @param cidMetrics the CID font for which this Unicode CMap is built
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     */
    public PDFToUnicodeCMap(CIDFont cidMetrics, String name, PDFCIDSystemInfo sysInfo) {
        super(name, sysInfo);
        cidFont = cidMetrics;
    }

    /** @see org.apache.fop.pdf.PDFCMap#fillInPDF(java.lang.StringBuffer) */
    public void fillInPDF(StringBuffer p) {
        writeCIDInit(p);
        writeCIDSystemInfo(p);
        writeVersionTypeName(p);
        writeCodeSpaceRange(p);
        writeBFEntries(p);
        writeWrapUp(p);
        add(p.toString());
    }

    /** @see org.apache.fop.pdf.PDFCMap#writeCIDSystemInfo(java.lang.StringBuffer) */
    protected void writeCIDSystemInfo(StringBuffer p) {
        p.append("/CIDSystemInfo\n");
        p.append("<< /Registry (Adobe)\n");
        p.append("/Ordering (UCS)\n");
        p.append("/Supplement 0\n");
        p.append(">> def\n");
    }

    /** @see org.apache.fop.pdf.PDFCMap#writeVersionTypeName(java.lang.StringBuffer) */
    protected void writeVersionTypeName(StringBuffer p) {
        p.append("/CMapName /Adobe-Identity-UCS def\n");
        p.append("/CMapType 2 def\n");
    }

    /**
     * Writes the character mappings for this font.
     * @param p StingBuffer to write to
     */
    protected void writeBFEntries(StringBuffer p) {
        if (cidFont == null) {
            return;
        }

        char[] charArray = cidFont.getCharsUsed();

        if (charArray != null) {
            writeBFCharEntries(p, charArray);
            writeBFRangeEntries(p, charArray);
        }
    }

    /**
     * Writes the entries for single characters of a base font (only characters which cannot be
     * expressed as part of a character range).
     * @param p StringBuffer to write to
     * @param charArray all the characters to map
     */
    protected void writeBFCharEntries(StringBuffer p, char[] charArray) {
        int totalEntries = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (!partOfRange(charArray, i)) {
                totalEntries++;
            }
        }
        if (totalEntries < 1) {
            return;
        }
        int remainingEntries = totalEntries;
        int charIndex = 0;
        do {
            /* Limited to 100 entries in each section */
            int entriesThisSection = Math.min(remainingEntries, 100);
            p.append(entriesThisSection + " beginbfchar\n");
            for (int i = 0; i < entriesThisSection; i++) {
                /* Go to the next char not in a range */
                while (partOfRange(charArray, charIndex)) {
                    charIndex++;
                }
                p.append("<" + padHexString(Integer.toHexString(charIndex), 4) + "> ");
                p.append("<" + padHexString(Integer.toHexString(charArray[charIndex]), 4) + ">\n");
                charIndex++;
            }
            remainingEntries -= entriesThisSection;
            p.append("endbfchar\n");
        } while (remainingEntries > 0);
    }

    /**
     * Writes the entries for character ranges for a base font.
     * @param p StringBuffer to write to
     * @param charArray all the characters to map
     */
    protected void writeBFRangeEntries(StringBuffer p, char[] charArray) {
        int totalEntries = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (startOfRange(charArray, i)) {
                totalEntries++;
            }
        }
        if (totalEntries < 1) {
            return;
        }
        int remainingEntries = totalEntries;
        int charIndex = 0;
        do {
            /* Limited to 100 entries in each section */
            int entriesThisSection = Math.min(remainingEntries, 100);
            p.append(entriesThisSection + " beginbfrange\n");
            for (int i = 0; i < entriesThisSection; i++) {
                /* Go to the next start of a range */
                while (!startOfRange(charArray, charIndex)) {
                    charIndex++;
                }
                p.append("<" + padHexString(Integer.toHexString(charIndex), 4) + "> ");
                p.append("<"
                        + padHexString(Integer.toHexString(endOfRange(charArray, charIndex)), 4)
                        + "> ");
                p.append("<" + padHexString(Integer.toHexString(charArray[charIndex]), 4) + ">\n");
                charIndex++;
            }
            remainingEntries -= entriesThisSection;
            p.append("endbfrange\n");
        } while (remainingEntries > 0);
    }

    /**
     * Find the end of the current range.
     * @param charArray The array which is being tested.
     * @param startOfRange The index to the array element that is the start of
     * the range.
     * @return The index to the element that is the end of the range.
     */
    private int endOfRange(char[] charArray, int startOfRange) {
        int i = startOfRange;
        while (i < charArray.length - 1 && sameRangeEntryAsNext(charArray, i)) {
            i++;
        }
        return i;
    }

    /**
     * Determine whether this array element should be part of a bfchar entry or
     * a bfrange entry.
     * @param charArray The array to be tested.
     * @param arrayIndex The index to the array element to be tested.
     * @return True if this array element should be included in a range.
     */
    private boolean partOfRange(char[] charArray, int arrayIndex) {
        if (charArray.length < 2) {
            return false;
        }
        if (arrayIndex == 0) {
            return sameRangeEntryAsNext(charArray, 0);
        }
        if (arrayIndex == charArray.length - 1) {
            return sameRangeEntryAsNext(charArray, arrayIndex - 1);
        }
        if (sameRangeEntryAsNext(charArray, arrayIndex - 1)) {
            return true;
        }
        if (sameRangeEntryAsNext(charArray, arrayIndex)) {
            return true;
        }
        return false;
    }

    /**
     * Determine whether two bytes can be written in the same bfrange entry.
     * @param charArray The array to be tested.
     * @param firstItem The first of the two items in the array to be tested.
     * The second item is firstItem + 1.
     * @return True if both 1) the next item in the array is sequential with
     * this one, and 2) the first byte of the character in the first position
     * is equal to the first byte of the character in the second position.
     */
    private boolean sameRangeEntryAsNext(char[] charArray, int firstItem) {
        if (charArray[firstItem] + 1 != charArray[firstItem + 1]) {
            return false;
        }
        if (firstItem / 256 != (firstItem + 1) / 256) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether this array element should be the start of a bfrange
     * entry.
     * @param charArray The array to be tested.
     * @param arrayIndex The index to the array element to be tested.
     * @return True if this array element is the beginning of a range.
     */
    private boolean startOfRange(char[] charArray, int arrayIndex) {
        // Can't be the start of a range if not part of a range.
        if (!partOfRange(charArray, arrayIndex)) {
            return false;
        }
        // If first element in the array, must be start of a range
        if (arrayIndex == 0) {
            return true;
        }
        // If last element in the array, cannot be start of a range
        if (arrayIndex == charArray.length - 1) {
            return false;
        }
        /*
         * If part of same range as the previous element is, cannot be start
         * of range.
         */
        if (sameRangeEntryAsNext(charArray, arrayIndex - 1)) {
            return false;
        }
        // Otherwise, this is start of a range.
        return true;
    }

    /**
     * Prepends the input string with a sufficient number of "0" characters to
     * get the returned string to be numChars length.
     * @param input The input string.
     * @param numChars The minimum characters in the output string.
     * @return The padded string.
     */
    public static String padHexString(String input, int numChars) {
        int length = input.length();
        if (length >= numChars) {
            return input;
        }
        StringBuffer returnString = new StringBuffer();
        for (int i = 1; i <= numChars - length; i++) {
            returnString.append("0");
        }
        returnString.append(input);
        return returnString.toString();
    }

}
