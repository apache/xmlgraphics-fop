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

package org.apache.fop.fonts.type1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.fonts.Glyphs;

import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.type1.PostscriptParser.PSDictionary;
import org.apache.fop.fonts.type1.PostscriptParser.PSElement;
import org.apache.fop.fonts.type1.PostscriptParser.PSFixedArray;
import org.apache.fop.fonts.type1.Type1SubsetFile.BinaryCoder;
import org.apache.fop.fonts.type1.Type1SubsetFile.BytesNumber;

public class Type1SubsetFileTestCase {

    private List<byte[]> decodedSections;
    private static final String TEST_FONT_A = "./test/resources/fonts/type1/c0419bt_.pfb";

    @Test
    public void test() throws IOException {
        InputStream in = new FileInputStream(TEST_FONT_A);
        compareCharStringData(TEST_FONT_A, createFontASubset(in, TEST_FONT_A));
    }

    @Test
    public void testStitchFont() throws IOException {
        ByteArrayOutputStream baosHeader = new ByteArrayOutputStream();
        ByteArrayOutputStream baosMain = new ByteArrayOutputStream();
        ByteArrayOutputStream baosTrailer = new ByteArrayOutputStream();

        //Header
        for (int i = 0; i < 10; i++) {
            baosHeader.write(123);
            baosMain.write(123);
        }
        for (int i = 0; i < 10; i++) {
            baosTrailer.write(0);
        }

        Type1SubsetFile subset = new Type1SubsetFile();
        byte[] result = subset.stitchFont(baosHeader, baosMain, baosTrailer);
        ByteArrayInputStream bais = new ByteArrayInputStream(result);
        assertEquals(result.length, 50);
        PFBParser parser = new PFBParser();
        parser.parsePFB(bais);
    }

    @Test
    public void testUpdateSectionSize() throws IOException {
        Type1SubsetFile subset = new Type1SubsetFile();
        ByteArrayOutputStream baos = subset.updateSectionSize(456);
        byte[] lowOrderSize = baos.toByteArray();
        assertEquals(lowOrderSize[0], -56);
        assertEquals(lowOrderSize[1], 1);
    }

    @Test
    public void testVariableContents() {
        Type1SubsetFile subset = new Type1SubsetFile();
        String result = subset.readVariableContents("/myvariable {some variable contents}");
        assertEquals(result, "some variable contents");
        result = subset.readVariableContents("/myvariable {hello {some more text {test} and some more}test}");
        //Should only reads one level deep
        assertEquals(result, "hello test");
    }

    @Test
    public void getOpPositionAndLength() {
        Type1SubsetFile subset = new Type1SubsetFile();
        ArrayList<BytesNumber> ops = new ArrayList<BytesNumber>();
        ops.add(new BytesNumber(10, 1));
        ops.add(new BytesNumber(255, 2));
        ops.add(new BytesNumber(100, 1));
        ops.add(new BytesNumber(97, 1));
        ops.add(new BytesNumber(856, 2));
        assertEquals(subset.getOpPosition(4, ops), 4);
        assertEquals(subset.getOperandsLength(ops), 7);
    }

    @Test
    public void testConcatArrays() {
        byte[] arrayA = {(byte)1, (byte)2, (byte)3, (byte)4, (byte)5};
        byte[] arrayB = {(byte)6, (byte)7, (byte)8, (byte)9, (byte)10};
        Type1SubsetFile subset = new Type1SubsetFile();
        byte[] concatArray = subset.concatArray(arrayA, arrayB);
        assertEquals(concatArray.length, 10);
        assertEquals(concatArray[5], 6);
        assertEquals(concatArray[3], 4);
    }

    @Test
    public void testGetBinaryEntry() {
        byte[] decoded = {(byte)34, (byte)23, (byte)78, (byte)55, (byte)12,
                         (byte)2, (byte)65, (byte)49, (byte)90, (byte)10};
        int[] section = {3, 7};
        Type1SubsetFile subset = new Type1SubsetFile();
        byte[] segment = subset.getBinaryEntry(section, decoded);
        assertEquals(segment.length, 4);
        assertEquals(segment[0], 55);
        assertEquals(segment[3], 65);
    }

    private void compareCharStringData(String font, byte[] subsetFont)
            throws IOException {
        decodedSections = new ArrayList<byte[]>();

        //Reinitialise the input stream as reset only supports 1000 bytes.
        InputStream in = new FileInputStream(font);
        List<PSElement> origElements = parseElements(in);
        List<PSElement> subsetElements = parseElements(new ByteArrayInputStream(subsetFont));

        PSFixedArray origSubs = (PSFixedArray)findElement(origElements, "/Subrs");
        PSFixedArray subsetSubs = (PSFixedArray)findElement(subsetElements, "/Subrs");
        PSDictionary origCharStrings = (PSDictionary)findElement(origElements, "/CharStrings");
        PSDictionary subsetCharStrings = (PSDictionary)findElement(subsetElements, "/CharStrings");
        for (String element : subsetCharStrings.getEntries().keySet()) {
            if (element.equals("/.notdef")) {
                continue;
            }
            int[] origBinaryCharLocation = origCharStrings.getBinaryEntries().get(element);
            int[] subsetBinaryCharLocation = subsetCharStrings.getBinaryEntries().get(element);

            int origLength = origBinaryCharLocation[1] - origBinaryCharLocation[0];
            int subsetLength = subsetBinaryCharLocation[1] - subsetBinaryCharLocation[0];
            byte[] origCharData = new byte[origLength];
            byte[] subsetCharData = new byte[subsetLength];
            System.arraycopy(decodedSections.get(0), origBinaryCharLocation[0], origCharData, 0, origLength);
            System.arraycopy(decodedSections.get(1), subsetBinaryCharLocation[0], subsetCharData, 0, subsetLength);
            origCharData = BinaryCoder.decodeBytes(origCharData, 4330, 4);
            subsetCharData = BinaryCoder.decodeBytes(subsetCharData, 4330, 4);
            byte[] origFullCharData = readFullCharString(decodedSections.get(0), origCharData, origSubs);
            byte[] subsetFullCharData = readFullCharString(decodedSections.get(1), subsetCharData, subsetSubs);
            assertArrayEquals(origFullCharData, subsetFullCharData);
        }
    }

    private byte[] createFontASubset(InputStream in, String font) throws IOException {
        SingleByteFont sbfont = mock(SingleByteFont.class);
        //Glyph index & selector
        Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
        Map<Integer, String> usedCharNames = new HashMap<Integer, String>();
        int count = 0;
        for (int i = 32; i < 127; i++) {
            glyphs.put(i, count++);
            when(sbfont.getUnicodeFromSelector(count)).thenReturn((char)i);
            usedCharNames.put(i, String.format("/%s", Glyphs.charToGlyphName((char)i)));
            when(sbfont.getGlyphName(i)).thenReturn(AdobeStandardEncoding.getCharFromCodePoint(i));
        }
        for (int i = 161; i < 204; i++) {
            glyphs.put(i, count++);
            when(sbfont.getUnicodeFromSelector(count)).thenReturn((char)i);
            usedCharNames.put(i, String.format("/%s", Glyphs.charToGlyphName((char)i)));
            when(sbfont.getGlyphName(i)).thenReturn(AdobeStandardEncoding.getCharFromCodePoint(i));
        }
        int[] randomGlyphs = {205, 206, 207, 208, 225, 227, 232, 233, 234, 235, 241, 245,
                248, 249, 250, 251
        };
        for (int i = 0; i < randomGlyphs.length; i++) {
            glyphs.put(randomGlyphs[i], count++);
            when(sbfont.getUnicodeFromSelector(count)).thenReturn((char)randomGlyphs[i]);
            usedCharNames.put(i, String.format("/%s", Glyphs.charToGlyphName((char)i)));
            when(sbfont.getGlyphName(i)).thenReturn(AdobeStandardEncoding.getCharFromCodePoint(i));
        }
        for (int i = 256; i < 335; i++) {
            glyphs.put(i, count++);
            when(sbfont.getUnicodeFromSelector(count)).thenReturn((char)i);
            usedCharNames.put(i, String.format("/%s", Glyphs.charToGlyphName((char)i)));
            when(sbfont.getGlyphName(i)).thenReturn(AdobeStandardEncoding.getCharFromCodePoint(i));
        }
        when(sbfont.getUsedGlyphNames()).thenReturn(usedCharNames);
        when(sbfont.getUsedGlyphs()).thenReturn(glyphs);
        when(sbfont.getEmbedFileURI()).thenReturn(URI.create(font));
        Type1SubsetFile subset = new Type1SubsetFile();
        return subset.createSubset(in, sbfont);
    }

    private List<PSElement> parseElements(InputStream in)
            throws IOException {
        PFBParser pfbParser = new PFBParser();
        PFBData origData = pfbParser.parsePFB(in);
        PostscriptParser parser = new PostscriptParser();
        byte[] decoded = BinaryCoder.decodeBytes(origData.getEncryptedSegment(), 55665, 4);
        decodedSections.add(decoded);
        return parser.parse(decoded);
    }

    private PSElement findElement(List<PSElement> elements, String operator) {
        for (PSElement element : elements) {
            if (element.getOperator().equals(operator)) {
                return element;
            }
        }
        return null;
    }

    private byte[] readFullCharString(byte[] decoded, byte[] data, PSFixedArray subroutines) {
        List<BytesNumber> operands = new ArrayList<BytesNumber>();
        for (int i = 0; i < data.length; i++) {
            int cur = data[i] & 0xFF;
            if (cur >= 0 && cur <= 31) {
                //Found subroutine. Read subroutine, recursively scan and update references
                if (cur == 10) {
                    if (operands.size() == 0) {
                        continue;
                    }
                    int[] subrData = subroutines.getBinaryEntryByIndex(operands.get(0).getNumber());
                    byte[] subroutine = getBinaryEntry(subrData, decoded);
                    subroutine = BinaryCoder.decodeBytes(subroutine, 4330, 4);
                    subroutine = readFullCharString(decoded, subroutine, subroutines);
                    data = replaceReference(data, subroutine, i - 1 + operands.get(0).getNumBytes(), i);
                } else {
                    int next = -1;
                    if (cur == 12) {
                        next = data[++i] & 0xFF;
                    }
                    BytesNumber operand = new BytesNumber(cur, i);
                    operand.setName(getName(cur, next));
                }
                operands.clear();
            }
            if (cur >= 32 && cur <= 246) {
                operands.add(new BytesNumber(cur - 139, 1));
            } else if (cur >= 247 && cur <= 250) {
                operands.add(new BytesNumber((cur - 247) * 256 + (data[i + 1] & 0xFF) + 108, 2));
                i++;
            } else if (cur >= 251 && cur <= 254) {
                operands.add(new BytesNumber(-(cur - 251) * 256 - (data[i + 1] & 0xFF) - 108, 2));
                i++;
            } else if (cur == 255) {
                int b1 = data[i + 1] & 0xFF;
                int b2 = data[i + 2] & 0xFF;
                int b3 = data[i + 3] & 0xFF;
                int b4 = data[i + 4] & 0xFF;
                int value = b1 << 24 | b2 << 16 | b3 << 8 | b4;
                operands.add(new BytesNumber(value, 5));
                i += 4;
            }
        }
        return data;
    }

    private String getName(int operator, int next) {
        switch (operator) {
        case 14: return "endchar";
        case 13: return "hsbw";
        case 12:
            switch (next) {
            case 0: return "dotsection";
            case 1: return "vstem3";
            case 2: return "hstem3";
            case 6: return "seac";
            case 7: return "sbw";
            case 16: return "callothersubr";
            case 17: return "pop";
            case 33: return "setcurrentpoint";
            default: return "unknown";
            }
        case 9: return "closepath";
        case 6: return "hlineto";
        case 22: return "hmoveto";
        case 31: return "hvcurveto";
        case 5: return "rlineto";
        case 21: return "rmoveto";
        case 8: return "rrcurveto";
        case 30: return "vhcurveto";
        case 7: return "vlineto";
        case 4: return "vmoveto";
        case 1: return "hstem";
        case 3: return "vstem";
        case 10: return "callsubr";
        case 11: return "return";
        default: return "unknown";
        }
    }

    private byte[] replaceReference(byte[] data, byte[] subroutine, int startRef, int endRef) {
        byte[] preBytes = new byte[startRef - 1];
        System.arraycopy(data, 0, preBytes, 0, startRef - 1);
        byte[] postBytes = new byte[data.length - endRef - 1];
        System.arraycopy(data, endRef + 1, postBytes, 0, data.length - endRef - 1);
        data = concatArray(preBytes, subroutine, 1);
        data = concatArray(data, postBytes, 0);
        return data;
    }

    private byte[] getBinaryEntry(int[] position, byte[] decoded) {
        int start = position[0];
        int finish = position[1];
        byte[] line = new byte[finish - start];
        System.arraycopy(decoded, start, line, 0, finish - start);
        return line;
    }

    private byte[] concatArray(byte[] a, byte[] b, int subtract) {
        int aLen = a.length;
        int bLen = b.length - subtract;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
