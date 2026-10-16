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
import static java.lang.Character.isHighSurrogate;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.render.pdf.PDFEventProducer;

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
     * One destination per character selector, in selector order: the UTF-16 text the glyph
     * stands for. Usually one code point; several for a ligature or other glyph produced from
     * more than one character; empty for a glyph whose text is carried by a neighbouring glyph;
     * a lone high surrogate for an unpaired one, which is reported and written with a zero
     * low surrogate.
     */
    protected String[] destinations;

    private boolean singleByte;

    private EventBroadcaster eventBroadcaster;

    /**
     * Constructor.
     *
     * @param destinations One destination string per character selector, in selector order
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     * @param singleByte true for single-byte, false for double-byte
     * @param eventBroadcaster Event broadcaster. May be null.
     */
    public PDFToUnicodeCMap(String[] destinations, String name, PDFCIDSystemInfo sysInfo,
                            boolean singleByte, EventBroadcaster eventBroadcaster) {
        super(name, sysInfo);
        if (singleByte && destinations.length > 256) {
            throw new IllegalArgumentException("unicodeCharMap may not contain more than"
                    + " 256 characters for single-byte encodings");
        }
        this.destinations = destinations;
        this.singleByte = singleByte;
        this.eventBroadcaster = eventBroadcaster;
    }

    /**
     * Constructor from a positional array of UTF-16 code units, where a surrogate pair
     * occupies two slots and stands for one character selector.
     *
     * @param unicodeCharMap An array of Unicode characters ordered by character code
     *                          (maps from character code to Unicode code point)
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     * @param singleByte true for single-byte, false for double-byte
     * @param eventBroadcaster Event broadcaster. May be null.
     */
    public PDFToUnicodeCMap(char[] unicodeCharMap, String name, PDFCIDSystemInfo sysInfo,
                            boolean singleByte, EventBroadcaster eventBroadcaster) {
        this(toDestinations(unicodeCharMap), name, sysInfo, singleByte, eventBroadcaster);
    }

    /**
     * Turns a positional array of UTF-16 code units into one destination per character
     * selector: a high surrogate takes the unit after it as its low surrogate, and a high
     * surrogate at the end of the array stands alone.
     * @param unicodeCharMap the positional array
     * @return one destination per selector
     */
    public static String[] toDestinations(char[] unicodeCharMap) {
        int count = 0;
        for (int i = 0; i < unicodeCharMap.length; i++) {
            if (isHighSurrogate(unicodeCharMap[i]) && i + 1 < unicodeCharMap.length) {
                i++;
            }
            count++;
        }
        String[] destinations = new String[count];
        int d = 0;
        for (int i = 0; i < unicodeCharMap.length; i++) {
            if (isHighSurrogate(unicodeCharMap[i]) && i + 1 < unicodeCharMap.length) {
                destinations[d++] = new String(unicodeCharMap, i, 2);
                i++;
            } else {
                destinations[d++] = String.valueOf(unicodeCharMap[i]);
            }
        }
        return destinations;
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
         */
        protected void writeBFEntries() throws IOException {
            if (destinations != null) {
                writeBFCharEntries();
                writeBFRangeEntries();
            }
        }

        /**
         * Writes the entries for single selectors (those which cannot be expressed as part of
         * a range), in sections of at most 100.
         * @throws IOException if an I/O error occurs
         */
        protected void writeBFCharEntries() throws IOException {
            int totalEntries = 0;
            for (int i = 0; i < destinations.length; i++) {
                if (!partOfRange(i)) {
                    totalEntries++;
                }
            }
            if (totalEntries < 1) {
                return;
            }
            int remainingEntries = totalEntries;
            int index = 0;
            do {
                /* Limited to 100 entries in each section */
                int entriesThisSection = Math.min(remainingEntries, 100);
                writer.write(entriesThisSection + " beginbfchar\n");
                int sectionEntryCount = 0;
                do {
                    /* Go to the next selector not in a range */
                    while (partOfRange(index)) {
                        index++;
                    }
                    writer.write("<" + padSelector(index) + "> ");
                    writer.write("<" + destinationHex(index) + ">\n");
                    index++;
                } while (++sectionEntryCount < entriesThisSection);
                remainingEntries -= entriesThisSection;
                writer.write("endbfchar\n");
            } while (remainingEntries > 0);
        }

        /**
         * Writes the entries for selector ranges, in sections of at most 100.
         * @throws IOException if an I/O error occurs
         */
        protected void writeBFRangeEntries() throws IOException {
            int totalEntries = 0;
            for (int i = 0; i < destinations.length; i++) {
                if (startOfRange(i)) {
                    totalEntries++;
                }
            }
            if (totalEntries < 1) {
                return;
            }
            int remainingEntries = totalEntries;
            int index = 0;
            do {
                /* Limited to 100 entries in each section */
                int entriesThisSection = Math.min(remainingEntries, 100);
                writer.write(entriesThisSection + " beginbfrange\n");
                int sectionEntryCount = 0;
                do {
                    /* Go to the next start of a range */
                    while (!startOfRange(index)) {
                        index++;
                    }
                    writer.write("<" + padSelector(index) + "> ");
                    writer.write("<" + padSelector(endOfRange(index)) + "> ");
                    writer.write("<" + destinationHex(index) + ">\n");
                    index++;
                } while (++sectionEntryCount < entriesThisSection);
                remainingEntries -= entriesThisSection;
                writer.write("endbfrange\n");
            } while (remainingEntries > 0);
        }

        private String padSelector(int index) {
            return padHexString(Integer.toHexString(index), (singleByte ? 2 : 4));
        }

        /**
         * The destination of a selector as UTF-16BE hex, four lower-case digits per code unit.
         * A lone high surrogate is reported and written with a zero low surrogate, as before.
         */
        private String destinationHex(int index) {
            String d = destinations[index];
            StringBuilder hex = new StringBuilder(4 * Math.max(1, d.length()));
            for (int i = 0; i < d.length(); i++) {
                hex.append(padHexString(Integer.toHexString(d.charAt(i)), 4));
            }
            if (d.length() == 1 && isHighSurrogate(d.charAt(0))) {
                if (eventBroadcaster != null) {
                    PDFEventProducer pdfEventProducer = PDFEventProducer.Provider.get(eventBroadcaster);
                    pdfEventProducer.unpairedSurrogate(this);
                }
                hex.append("0000");
            }
            return hex.toString();
        }

        /**
         * The value a destination contributes to a range, or -1 if it can be in no range. Only
         * a destination of exactly one code point can: a single non-surrogate code unit, or a
         * high surrogate followed by one more unit. Two destinations are consecutive when their
         * keys differ by one, which for a pair means the same high surrogate and the next low.
         */
        private long rangeKey(int index) {
            String d = destinations[index];
            if (d.length() == 1 && !Character.isSurrogate(d.charAt(0))) {
                return d.charAt(0);
            } else if (d.length() == 2 && isHighSurrogate(d.charAt(0))) {
                return ((long) d.charAt(0) << 16) | d.charAt(1);
            } else {
                return -1;
            }
        }

        /**
         * Determine whether two consecutive selectors can be in the same bfrange entry: both
         * destinations are one code point, the second is the next code point, and the two
         * selectors are in the same block of 256, since only the low byte may vary in a range.
         * @param index the first of the two selectors
         * @return true if both are in the same range
         */
        private boolean sameRangeEntryAsNext(int index) {
            if (index < 0 || index >= destinations.length - 1) {
                return false;
            }
            long key = rangeKey(index);
            return key >= 0 && rangeKey(index + 1) == key + 1
                    && index / 256 == (index + 1) / 256;
        }

        /**
         * Determine whether this selector should be part of a bfrange entry rather than a
         * bfchar entry.
         * @param index the selector
         * @return true if it is in a range
         */
        private boolean partOfRange(int index) {
            return sameRangeEntryAsNext(index - 1) || sameRangeEntryAsNext(index);
        }

        /**
         * Determine whether this selector starts a bfrange entry.
         * @param index the selector
         * @return true if it is the first of a range
         */
        private boolean startOfRange(int index) {
            return sameRangeEntryAsNext(index) && !sameRangeEntryAsNext(index - 1);
        }

        /**
         * Find the end of the range that starts at a selector.
         * @param startOfRange the selector that starts the range
         * @return the last selector of the range
         */
        private int endOfRange(int startOfRange) {
            int i = startOfRange;
            while (sameRangeEntryAsNext(i)) {
                i++;
            }
            return i;
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
