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

import java.io.CharArrayWriter;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Pins the exact ToUnicode CMap this writer produces today.
 *
 * <p>These are characterisation tests, not specifications: they record current output so a
 * change to the writer has to be deliberate. Every PDF FOP produces gets its text layer
 * from here, so a silent change to the range packing would be invisible in rendering and
 * visible only to search, copy and paste, and screen readers. FOP-3345 and FOP-3346 touch
 * this class.
 *
 * <p>If one of these fails, decide whether the new output is correct before updating it.
 * Do not update the expectation to make the build pass.</p>
 */
public class ToUnicodeCharacterisationTestCase {

    private String cmapOf(char[] chars, boolean singleByte) throws IOException {
        PDFToUnicodeCMap cmap = new PDFToUnicodeCMap(chars, PDFCMap.ENC_IDENTITY_H,
                new PDFCIDSystemInfo("Adobe", "Identity", 0), singleByte, null);
        CharArrayWriter writer = new CharArrayWriter();
        cmap.createCMapBuilder(writer).writeCMap();
        return writer.toString();
    }

    private String body(String cmap) {
        int from = cmap.indexOf("endcodespacerange\n");
        int to = cmap.indexOf("endcmap");
        return cmap.substring(from + "endcodespacerange\n".length(), to);
    }

    /** Contiguous code points pack into a single range. */
    @Test
    public void testContiguousRunBecomesOneRange() throws IOException {
        assertEquals("1 beginbfrange\n<0000> <0003> <0041>\nendbfrange\n",
                body(cmapOf(new char[] {'A', 'B', 'C', 'D'}, false)));
    }

    /** Isolated code points are written one by one. */
    @Test
    public void testScatteredCodePointsBecomeChars() throws IOException {
        assertEquals("3 beginbfchar\n<0000> <0041>\n<0001> <005a>\n<0002> <0072>\nendbfchar\n",
                body(cmapOf(new char[] {'A', 'Z', 'r'}, false)));
    }

    /** A surrogate pair is one code point across two array slots, and still ranges. */
    @Test
    public void testSurrogatePairIsOneCodePoint() throws IOException {
        assertEquals("1 beginbfchar\n<0000> <d800df00>\nendbfchar\n",
                body(cmapOf(new char[] {'\uD800', '\uDF00'}, false)));
    }

    /**
     * The defect FOP-3345 addresses, pinned as it stands: a ligature glyph carries a
     * private-use code point, and consecutive ones pack into a range, so the text layer
     * says U+E000 upward rather than the letters.
     */
    @Test
    public void testPrivateUseLigaturesRangeTogetherToday() throws IOException {
        assertEquals("1 beginbfrange\n<0000> <0002> <e000>\nendbfrange\n",
                body(cmapOf(new char[] {'', '', ''}, false)));
    }

    /**
     * Destinations are written in lower-case hex while the code space is upper-case. Pinned
     * because it is a byte-level property of every PDF FOP writes, and easy to change by
     * accident when reworking the writer.
     */
    @Test
    public void testHexCaseIsMixedByDesign() throws IOException {
        String cmap = cmapOf(new char[] {'\uABCD'}, false);
        assertEquals("1 beginbfchar\n<0000> <abcd>\nendbfchar\n", body(cmap));
        assertEquals(true, cmap.contains("<0000> <FFFF>"));
    }

    private String cmapOf(String[] destinations) throws IOException {
        PDFToUnicodeCMap cmap = new PDFToUnicodeCMap(destinations, PDFCMap.ENC_IDENTITY_H,
                new PDFCIDSystemInfo("Adobe", "Identity", 0), false, null);
        CharArrayWriter writer = new CharArrayWriter();
        cmap.createCMapBuilder(writer).writeCMap();
        return writer.toString();
    }

    /**
     * A glyph standing for several characters publishes them as a string, in a bfchar,
     * never in a range; its single-character neighbours still range.
     */
    @Test
    public void testLigaturePublishesItsLetters() throws IOException {
        assertEquals("1 beginbfchar\n<0002> <00660069>\nendbfchar\n"
                + "1 beginbfrange\n<0000> <0001> <0066>\nendbfrange\n",
                body(cmapOf(new String[] {"f", "g", "fi"})));
    }

    /**
     * A surrogate pair is one selector, so the selectors after it do not drift by one as they
     * did when the pair occupied two slots of a positional array; the positional constructor
     * now gives the same CMap as the per-selector one.
     */
    @Test
    public void testSelectorsDoNotDriftAfterASurrogatePair() throws IOException {
        String expected = "3 beginbfchar\n<0000> <0041>\n<0001> <d835dc00>\n<0002> <0042>\nendbfchar\n";
        assertEquals(expected, body(cmapOf(new String[] {"A", "\uD835\uDC00", "B"})));
        assertEquals(expected, body(cmapOf(new char[] {'A', '\uD835', '\uDC00', 'B'}, false)));
    }

    /** Consecutive supplementary-plane code points still pack into a range. */
    @Test
    public void testSurrogatePairsStillRange() throws IOException {
        assertEquals("1 beginbfrange\n<0000> <0001> <d835dc00>\nendbfrange\n",
                body(cmapOf(new String[] {"\uD835\uDC00", "\uD835\uDC01"})));
    }

    /** An empty destination is written as an empty string. */
    @Test
    public void testEmptyDestination() throws IOException {
        assertEquals("2 beginbfchar\n<0000> <0041>\n<0001> <>\nendbfchar\n",
                body(cmapOf(new String[] {"A", ""})));
    }

    /** Single-byte code space, for the simple-font path. */
    @Test
    public void testSingleByteCodeSpace() throws IOException {
        assertEquals("1 beginbfrange\n<00> <03> <0041>\nendbfrange\n",
                body(cmapOf(new char[] {'A', 'B', 'C', 'D'}, true)));
    }
}
