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

package org.apache.fop.fonts.truetype;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link GlyfTable}.
 */
public class GlyfTableTestCase {

    private static final class DirData {

        final long offset;
        final long length;

        DirData(long offset, long length) {
            this.offset = offset;
            this.length = length;
        }
    }

    private FontFileReader subsetReader;

    private long[] glyphOffsets;

    private FontFileReader originalFontReader;

    @Before
    public void setUp() throws IOException {
        FileInputStream fontStream = new FileInputStream(
                "test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        try {
            originalFontReader = new FontFileReader(fontStream);
        } finally {
            fontStream.close();
        }
    }

    /**
     * Tests that composed glyphs are included in the glyph subset if a composite glyph is used.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testPopulateGlyphsWithComposites() throws IOException {
        // Glyph 408 -> U+01D8 "uni01D8" this is a composite glyph.
        int[] composedIndices = setupTest(408);

        int[] expected = new int[composedIndices.length];
        expected[1] = 6;
        expected[5] = 2;
        expected[6] = 4;

        assertArrayEquals(expected, composedIndices);
    }

    /**
     * Tests that no glyphs are added if there are no composite glyphs the subset.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testPopulateNoCompositeGlyphs() throws IOException {
        int[] composedIndices = setupTest(36, 37, 38); // "A", "B", "C"
        int[] expected = new int[composedIndices.length];

        // There should be NO composite glyphs
        assertArrayEquals(expected, composedIndices);
    }

    /**
     * Tests that glyphs aren't remapped twice if the glyph before a composite glyph has 0-length.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testGlyphsNotRemappedTwice() throws IOException {
        int composedGlyph = 12;
        // The order of these glyph indices, must NOT be changed! (see javadoc above)
        int[] composedIndices = setupTest(1, 2, 3, 16, 2014, 4, 7, 8, 13, 2015, composedGlyph);

        // There are 2 composed glyphs within the subset
        int[] expected = new int[composedIndices.length];
        expected[10] = composedGlyph;

        assertArrayEquals(expected, composedIndices);
    }

    /**
     * Tests that the correct glyph is included in the subset, when a composite glyph composed of a
     * composite glyph is used.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testSingleRecursionStep() throws IOException {
        // Glyph 2077 -> U+283F "uni283F" this is composed of a composite glyph (recursive).
        int[] composedIndices = setupTest(2077);

        int[] expected = new int[composedIndices.length];
        expected[1] = 2;

        assertArrayEquals(expected, composedIndices);
    }

    /**
     * Tests that an empty last glyph is not read past the end of the font file.
     *
     * <p>A glyph whose loca entry gives it a length of zero has no glyph description at all,
     * so there is no numberOfContours to read; when such a glyph is the last in the table and
     * the glyf table is the last in the file, reading it runs off the end. Subsetting a font
     * of that shape threw an EOFException. Arimo and Carlito, whose last glyph is U+00A0,
     * are examples.</p>
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testEmptyLastGlyphIsNotReadPastTheEndOfTheFile() throws IOException {
        GlyfTable glyfTable = syntheticGlyfTable();
        assertFalse("an empty last glyph is not composite", glyfTable.isComposite(3));
    }

    /**
     * Tests that an empty glyph in the middle of the table is not reported as composite: the
     * two bytes at its offset are the <em>next</em> glyph's numberOfContours.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testEmptyGlyphIsNotComposite() throws IOException {
        GlyfTable glyfTable = syntheticGlyfTable();
        assertFalse("a simple glyph is not composite", glyfTable.isComposite(0));
        assertFalse("an empty glyph is not composite", glyfTable.isComposite(1));
        assertTrue("a composite glyph is composite", glyfTable.isComposite(2));
    }

    /**
     * A glyf table of 8 bytes at offset 12, the last thing in a 20 byte file:
     * <ul>
     *   <li>glyph 0 at 0, four bytes, numberOfContours 1 (a simple glyph)</li>
     *   <li>glyph 1 at 4, empty</li>
     *   <li>glyph 2 at 4, four bytes, numberOfContours -1 (a composite glyph)</li>
     *   <li>glyph 3 at 8, empty - and 8 is the length of the table</li>
     * </ul>
     */
    private GlyfTable syntheticGlyfTable() throws IOException {
        byte[] font = new byte[20];
        font[12] = 0x00;
        font[13] = 0x01;
        font[16] = (byte) 0xFF;
        font[17] = (byte) 0xFF;

        long[] offsets = {0, 4, 4, 8};
        OFMtxEntry[] mtx = new OFMtxEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            mtx[i] = new OFMtxEntry();
            mtx[i].setOffset(offsets[i]);
        }

        return new GlyfTable(new FontFileReader(new ByteArrayInputStream(font)),
                mtx, new OFDirTabEntry(12, 8), new HashMap<Integer, Integer>());
    }

    private int[] setupTest(int... glyphIndices) throws IOException {
        Map<Integer, Integer> glyphs = new HashMap<Integer, Integer>();
        int index = 0;
        glyphs.put(0, index++); // Glyph 0 (.notdef) must ALWAYS be in the subset

        for (int glyphIndex : glyphIndices) {
            glyphs.put(glyphIndex, index++);
        }
        setupSubsetReader(glyphs);
        readLoca();

        return retrieveIndicesOfComposedGlyphs();
    }

    private void setupSubsetReader(Map<Integer, Integer> glyphs) throws IOException {
        TTFSubSetFile fontFile = new TTFSubSetFile();
        String header = OFFontLoader.readHeader(subsetReader);
        fontFile.readFont(originalFontReader, "Deja", header, glyphs);
        byte[] subsetFont = fontFile.getFontSubset();
        InputStream intputStream = new ByteArrayInputStream(subsetFont);
        subsetReader = new FontFileReader(intputStream);
    }

    private void readLoca() throws IOException {
        DirData loca = getTableData(OFTableName.LOCA.getName());
        int numberOfGlyphs = (int) (loca.length - 4) / 4;
        glyphOffsets = new long[numberOfGlyphs];
        subsetReader.seekSet(loca.offset);

        for (int i = 0; i < numberOfGlyphs; i++) {
            glyphOffsets[i] = subsetReader.readTTFULong();
        }
    }

    private int[] retrieveIndicesOfComposedGlyphs() throws IOException {
        DirData glyf = getTableData(OFTableName.GLYF.getName());
        int[] composedGlyphIndices = new int[glyphOffsets.length];

        for (int i = 0; i < glyphOffsets.length; i++) {
            long glyphOffset = glyphOffsets[i];
            if (i != glyphOffsets.length - 1 && glyphOffset == glyphOffsets[i + 1]) {
                continue;
            }
            subsetReader.seekSet(glyf.offset + glyphOffset);
            short numberOfContours = subsetReader.readTTFShort();
            if (numberOfContours < 0) {
                subsetReader.skip(8);
                subsetReader.readTTFUShort(); // flags
                int glyphIndex = subsetReader.readTTFUShort();
                composedGlyphIndices[i] = glyphIndex;
            }
        }
        return composedGlyphIndices;
    }

    private DirData getTableData(String tableName) throws IOException {
        subsetReader.seekSet(0);
        subsetReader.skip(12);
        String name;
        do {
            name = subsetReader.readTTFString(4);
            subsetReader.skip(4 * 3);
        } while (!name.equals(tableName));

        subsetReader.skip(-8); // We've found the table, go back to get the data we skipped over
        return new DirData(subsetReader.readTTFLong(), subsetReader.readTTFLong());
    }

    private void assertArrayEquals(int[] expected, int[] actual) {
        assertTrue(Arrays.equals(expected, actual));
    }
}
