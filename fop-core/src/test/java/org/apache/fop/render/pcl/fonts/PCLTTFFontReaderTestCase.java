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
package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.PCLFontSegment.SegmentID;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFFontReader;

public class PCLTTFFontReaderTestCase {

    private CustomFontMetricsMapper customFont = mock(CustomFontMetricsMapper.class);
    private PCLByteWriterUtil byteWriter;
    private static final String TEST_FONT_A = "./test/resources/fonts/ttf/DejaVuLGCSerif.ttf";

    @Before
    public void setUp() {
        byteWriter = new PCLByteWriterUtil();
    }

    @Test
    public void verifyFontAData() throws Exception {
        CustomFont sbFont = mock(CustomFont.class);
        when(sbFont.getInputStream()).thenReturn(new FileInputStream(new File(TEST_FONT_A)));
        when(customFont.getRealFont()).thenReturn(sbFont);
        SingleByteFont font = mock(SingleByteFont.class);
        when(font.getGIDFromChar('h')).thenReturn(104);
        when(font.getGIDFromChar('e')).thenReturn(101);
        when(font.getGIDFromChar('l')).thenReturn(108);
        when(font.getGIDFromChar('o')).thenReturn(111);
        PCLTTFFontReader reader = new MockPCLTTFFontReader(customFont, byteWriter);
        reader.setFont(font);
        verifyFontData(reader);
        validateOffsets(reader);
        validateFontSegments(reader);
    }

    /**
     * Compares the input font data against a sample of the data read and calculated by the reader. The assertions are
     * made against data taken from the TrueType Font Analyzer tool.
     * @param reader The reader
     */
    private void verifyFontData(PCLTTFFontReader reader) {
        assertEquals(reader.getCellWidth(), 5015); // Bounding box X2 - X1
        assertEquals(reader.getCellHeight(), 3254); // Bounding box Y2 - Y1
        assertEquals(reader.getCapHeight(), 0); // OS2Table.capHeight
        assertEquals(reader.getFontName(), "DejaVu LGC Serif"); // Full name read by TTFFont object
        assertEquals(reader.getFirstCode(), 32); // Always 32 for bound font
        assertEquals(reader.getLastCode(), 255); // Always 255 for bound font

        // Values that require conversion tables (See PCLTTFFontReader.java)
        assertEquals(reader.getStrokeWeight(), 0); // Weight Class 400 (regular) should be equivalent 0
        assertEquals(reader.getSerifStyle(), 128); // Serif Style 0 should equal 0
        assertEquals(reader.getWidthType(), 0); // Width Class 5 (regular) should be equivalent 0
    }

    private void validateOffsets(PCLTTFFontReader reader) throws IOException {
        // Offsets are stored with their character ID with the array [offset, length]
        Map<Integer, int[]> offsets = reader.getCharacterOffsets();

        // Test data
        int[] charC = {27644, 144}; // Char index = 99
        int[] charDollar = {16044, 264}; // Char index = 36
        int[] charOne = {17808, 176}; // Char index = 49
        int[] charUpperD = {21236, 148}; // Char index = 68
        int[] charUpperJ = {22140, 176}; // Char index = 74

        assertArrayEquals(offsets.get(99), charC);
        assertArrayEquals(offsets.get(36), charDollar);
        assertArrayEquals(offsets.get(49), charOne);
        assertArrayEquals(offsets.get(68), charUpperD);
        assertArrayEquals(offsets.get(74), charUpperJ);
    }

    /**
     * Verifies the font segment data copied originally from the TrueType font. Data was verified using TrueType Font
     * Analyzer and PCLParaphernalia tool.
     * @param reader The reader
     * @throws IOException
     */
    private void validateFontSegments(PCLTTFFontReader reader) throws IOException {
        HashMap<Character, Integer> mappedChars = new HashMap<Character, Integer>();
        mappedChars.put('H', 1);
        mappedChars.put('e', 1);
        mappedChars.put('l', 1);
        mappedChars.put('o', 1);

        List<PCLFontSegment> segments = reader.getFontSegments(mappedChars);
        assertEquals(segments.size(), 5);
        for (PCLFontSegment segment : segments) {
            if (segment.getIdentifier() == SegmentID.PA) {
                // Panose
                assertEquals(segment.getData().length, 10);
                byte[] panose = {2, 6, 6, 3, 5, 6, 5, 2, 2, 4};
                assertArrayEquals(segment.getData(), panose);
            } else if (segment.getIdentifier() == SegmentID.GT) {
                verifyGlobalTrueTypeData(segment, mappedChars.size());
            } else if (segment.getIdentifier() == SegmentID.NULL) {
                // Terminating segment
                assertEquals(segment.getData().length, 0);
            }
        }
    }

    private void verifyGlobalTrueTypeData(PCLFontSegment segment, int mappedCharsSize)
            throws IOException {
        byte[] ttfData = segment.getData();
        int currentPos = 0;
        //Version
        assertEquals(readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]}), 1);
        assertEquals(readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]}), 0);
        //Number of tables
        int numTables = readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]});
        assertEquals(numTables, 8);
        //Search range
        assertEquals(readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]}), 128);
        //Entry Selector
        assertEquals(readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]}), 3);
        //Range shift
        assertEquals(readInt(new byte[]{ttfData[currentPos++], ttfData[currentPos++]}), 0);
        String[] validTags = {"head", "hhea", "hmtx", "maxp", "gdir"};
        int matches = 0;
        for (int i = 0; i < numTables; i++) {
            String tag = readTag(new byte[]{ttfData[currentPos++], ttfData[currentPos++],
                    ttfData[currentPos++], ttfData[currentPos++]});
            if (Arrays.asList(validTags).contains(tag)) {
                matches++;
            }
            if (tag.equals("hmtx")) {
                currentPos += 4;
                int offset = readLong(new byte[]{ttfData[currentPos++], ttfData[currentPos++],
                        ttfData[currentPos++], ttfData[currentPos++]});
                int length = readLong(new byte[]{ttfData[currentPos++], ttfData[currentPos++],
                        ttfData[currentPos++], ttfData[currentPos++]});
                verifyHmtx(ttfData, offset, length, mappedCharsSize);
            } else {
                currentPos += 12;
            }
        }
        assertEquals(matches, 5);
    }

    private void verifyHmtx(byte[] ttfData, int offset, int length, int mappedCharsSize)
            throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ttfData);
        byte[] subsetHmtx = new byte[length];
        bais.skip(offset);
        bais.read(subsetHmtx);
        assertEquals(subsetHmtx.length, (mappedCharsSize + 32) * 4);
    }

    private int readInt(byte[] bytes) {
        return ((0xFF & bytes[0]) << 8) | (0xFF & bytes[1]);
    }

    private int readLong(byte[] bytes) {
        return ((0xFF & bytes[0]) << 24) | ((0xFF & bytes[1]) << 16) | ((0xFF & bytes[2]) << 8)
                | (0xFF & bytes[3]);
    }

    private String readTag(byte[] tag) {
        return new String(tag);
    }
}
