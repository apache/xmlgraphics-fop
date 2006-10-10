/*
 * $Id: PDFToUnicodeCMap.java,v 1.3.2.1 2005/12/01 12:00:00 ono Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.pdf;

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
import org.apache.fop.fonts.CIDFont;

public class PDFToUnicodeCMap extends PDFCMap {

    /**
     * handle to read font
     */
    protected CIDFont cidFont;

    /**
     * Constructor.
     *
     * @param name One of the registered names found in Table 5.14 in PDF
     * Reference, Second Edition.
     * @param sysInfo The attributes of the character collection of the CIDFont.
     */
    public PDFToUnicodeCMap(CIDFont cidMetrics, String name, PDFCIDSystemInfo sysInfo) {
        super(name, sysInfo);
        cidFont = cidMetrics;
    }

    public void fillInPDF(StringBuffer p) {
        writeCIDInit(p);
        writeCIDSystemInfo(p);
        writeVersionTypeName(p);
        writeCodeSpaceRange(p);
        writeBFEntries(p);
        writeWrapUp(p);
        add(p.toString());
    }

    protected void writeCIDSystemInfo(StringBuffer p) {
        p.append("/CIDSystemInfo\n");
        p.append("<< /Registry (Adobe)\n");
        p.append("/Ordering (UCS)\n");
        p.append("/Supplement 0\n");
        p.append(">> def\n");
    }

    protected void writeVersionTypeName(StringBuffer p) {
        p.append("/CMapName /Adobe-Identity-UCS def\n");
        p.append("/CMapType 2 def\n");
    }

    /**
     * Writes the character mappings for this font.
     */
    protected void writeBFEntries(StringBuffer p) {
        if(cidFont == null) return;

        char[] charArray = cidFont.getCharsUsed();

        if(charArray != null) {
            writeBFCharEntries(p, charArray);
            writeBFRangeEntries(p, charArray);
        }
    }

    protected void writeBFCharEntries(StringBuffer p, char[] charArray) {
        int completedEntries = 0;
        int totalEntries = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (! partOfRange(charArray, i)) {
                totalEntries ++;
            }
        }
        if (totalEntries < 1) {
            return;
        }
        int remainingEntries = totalEntries;
        /* Limited to 100 entries in each section */
        int entriesThisSection = Math.min(remainingEntries, 100);
        int remainingEntriesThisSection = entriesThisSection;
        p.append(entriesThisSection + " beginbfchar\n");
        for (int i = 0; i < charArray.length; i++) {
            if (partOfRange(charArray, i)) {
                continue;
            }
            p.append("<" + padHexString(Integer.toHexString(i), 4)
                    + "> ");
            p.append("<" + padHexString(Integer.toHexString(charArray[i]), 4)
                    + ">\n");
            /* Compute the statistics. */
            completedEntries ++;
            remainingEntries = totalEntries - completedEntries;
            remainingEntriesThisSection --;
            if (remainingEntriesThisSection < 1) {
                if (remainingEntries > 0) {
                    p.append("endbfchar\n");
                    entriesThisSection = Math.min(remainingEntries, 100);
                    remainingEntriesThisSection = entriesThisSection;
                    p.append(entriesThisSection + " beginbfchar\n");
                }
            }
        }
        p.append("endbfchar\n");
    }

    protected void writeBFRangeEntries(StringBuffer p, char[] charArray) {
        int completedEntries = 0;
        int totalEntries = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (startOfRange(charArray, i)) {
                totalEntries ++;
            }
        }
        if (totalEntries < 1) {
            return;
        }
        int remainingEntries = totalEntries;
        int entriesThisSection = Math.min(remainingEntries, 100);
        int remainingEntriesThisSection = entriesThisSection;
        p.append(entriesThisSection + " beginbfrange\n");
        for (int i = 0; i < charArray.length; i++) {
            if (! startOfRange(charArray, i)) {
                continue;
            }
            p.append("<"
                    + padHexString(Integer.toHexString(i), 4)
                    + "> ");
            p.append("<"
                    + padHexString(Integer.toHexString
                            (endOfRange(charArray, i)), 4)
                    + "> ");
            p.append("<"
                    + padHexString(Integer.toHexString(charArray[i]), 4)
                    + ">\n");
            /* Compute the statistics. */
            completedEntries ++;
            remainingEntries = totalEntries - completedEntries;
            if (remainingEntriesThisSection < 1) {
                if (remainingEntries > 0) {
                    p.append("endbfrange\n");
                    entriesThisSection = Math.min(remainingEntries, 100);
                    remainingEntriesThisSection = entriesThisSection;
                    p.append(entriesThisSection + " beginbfrange\n");
                }
            }
        }
        p.append("endbfrange\n");
    }

    /**
     * Find the end of the current range.
     * @param charArray The array which is being tested.
     * @param startOfRange The index to the array element that is the start of
     * the range.
     * @return The index to the element that is the end of the range.
     */
    private int endOfRange(char[] charArray, int startOfRange) {
        int endOfRange = -1;
        for (int i = startOfRange; i < charArray.length - 1 && endOfRange < 0;
                i++) {
            if (! sameRangeEntryAsNext(charArray, i)) {
                endOfRange = i;
            }
        }
        return endOfRange;
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
        if (! partOfRange(charArray, arrayIndex)) {
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
