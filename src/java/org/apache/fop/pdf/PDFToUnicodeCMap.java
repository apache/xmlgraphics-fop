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

import java.io.IOException;
import java.io.Writer;

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
     * The array of Unicode characters ordered by character code
     * (maps from character code to Unicode code point).
     */
    protected char[] unicodeCharMap;

    private boolean singleByte;

    /**
     * Constructor.
     *
     * @param unicodeCharMap An array of Unicode characters ordered by character code
     *                          (maps from character code to Unicode code point)
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     * @param singleByte true for single-byte, false for double-byte
     */
    public PDFToUnicodeCMap(char[] unicodeCharMap, String name, PDFCIDSystemInfo sysInfo,
            boolean singleByte) {
        super(name, sysInfo);
        if (singleByte && unicodeCharMap.length > 256) {
            throw new IllegalArgumentException("unicodeCharMap may not contain more than"
                    + " 256 characters for single-byte encodings");
        }
        this.unicodeCharMap = unicodeCharMap;
        this.singleByte = singleByte;
    }

    /** {@inheritDoc} */
    protected CMapBuilder createCMapBuilder(Writer writer) {
        return new ToUnicodeCMapBuilder(writer);
    }

    class ToUnicodeCMapBuilder extends CMapBuilder {

        public ToUnicodeCMapBuilder(Writer writer) {
            super(writer, null);
        }

        /**
         * Writes the CMap to a Writer.
         * @param writer the writer
         * @throws IOException if an I/O error occurs
         */
        public void writeCMap() throws IOException {
            writeCIDInit();
            writeCIDSystemInfo("Adobe", "UCS", 0);
            writeName("Adobe-Identity-UCS");
            writeType("2");
            writeCodeSpaceRange(singleByte);
            writeBFEntries();
            writeWrapUp();
        }

        /**
         * Writes the character mappings for this font.
         * @param p StingBuffer to write to
         */
        protected void writeBFEntries() throws IOException {
            if (unicodeCharMap != null) {
                writeBFCharEntries(unicodeCharMap);
                writeBFRangeEntries(unicodeCharMap);
            }
        }

        /**
         * Writes the entries for single characters of a base font (only characters which cannot be
         * expressed as part of a character range).
         * @param p StringBuffer to write to
         * @param charArray all the characters to map
         * @throws IOException
         */
        protected void writeBFCharEntries(char[] charArray) throws IOException {
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
                writer.write(entriesThisSection + " beginbfchar\n");
                for (int i = 0; i < entriesThisSection; i++) {
                    /* Go to the next char not in a range */
                    while (partOfRange(charArray, charIndex)) {
                        charIndex++;
                    }
                    writer.write("<" + padCharIndex(charIndex) + "> ");
                    writer.write("<" + padHexString(Integer.toHexString(charArray[charIndex]), 4)
                            + ">\n");
                    charIndex++;
                }
                remainingEntries -= entriesThisSection;
                writer.write("endbfchar\n");
            } while (remainingEntries > 0);
        }

        private String padCharIndex(int charIndex) {
            return padHexString(Integer.toHexString(charIndex), (singleByte ? 2 : 4));
        }

        /**
         * Writes the entries for character ranges for a base font.
         * @param p StringBuffer to write to
         * @param charArray all the characters to map
         * @throws IOException
         */
        protected void writeBFRangeEntries(char[] charArray) throws IOException {
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
                writer.write(entriesThisSection + " beginbfrange\n");
                for (int i = 0; i < entriesThisSection; i++) {
                    /* Go to the next start of a range */
                    while (!startOfRange(charArray, charIndex)) {
                        charIndex++;
                    }
                    writer.write("<" + padCharIndex(charIndex) + "> ");
                    writer.write("<"
                            + padCharIndex(endOfRange(charArray, charIndex))
                            + "> ");
                    writer.write("<" + padHexString(Integer.toHexString(charArray[charIndex]), 4)
                            + ">\n");
                    charIndex++;
                }
                remainingEntries -= entriesThisSection;
                writer.write("endbfrange\n");
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
        private String padHexString(String input, int numChars) {
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

}
