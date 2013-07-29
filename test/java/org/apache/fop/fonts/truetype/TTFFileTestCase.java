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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.fop.fonts.truetype.TTFFile.PostScriptVersion;

/**
 * Class for testing org.apache.fop.fonts.truetype.TTFFile
 */
public class TTFFileTestCase {
    // We only want to initialize the FontFileReader once (for performance reasons)
    /** The truetype font file (DejaVuLGCSerif) */
    protected final TTFFile dejavuTTFFile;
    /** The FontFileReader for ttfFile (DejaVuLGCSerif) */
    protected final FontFileReader dejavuReader;
    /** The truetype font file (DroidSansMono) */
    protected final TTFFile droidmonoTTFFile;
    /** The FontFileReader for ttfFile (DroidSansMono) */
    protected final FontFileReader droidmonoReader;


    /**
     * Constructor initialises FileFontReader to
     * @throws IOException exception
     */
    public TTFFileTestCase() throws IOException {
        dejavuTTFFile = new TTFFile();
        InputStream dejaStream = new FileInputStream("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        dejavuReader = new FontFileReader(dejaStream);
        dejavuTTFFile.readFont(dejavuReader);
        dejaStream.close();

        InputStream droidStream = new FileInputStream("test/resources/fonts/ttf/DroidSansMono.ttf");

        droidmonoTTFFile = new TTFFile();
        droidmonoReader = new FontFileReader(droidStream);
        droidmonoTTFFile.readFont(droidmonoReader);
        droidStream.close();
    }

    /**
     * Test convertTTFUnit2PDFUnit() - The units per em retrieved reading the HEAD table from
     * the font file. (DroidSansMono has the same units per em as DejaVu so no point testing it)
     */
    @Test
    public void testConvertTTFUnit2PDFUnit() {
        // DejaVu has 2048 units per em (PDF works in millipts, thus the 1000)
        // test rational number
        assertEquals(1000, dejavuTTFFile.convertTTFUnit2PDFUnit(2048));
        // test smallest case, this should = 0.488 (round down to 0)
        assertEquals(0, dejavuTTFFile.convertTTFUnit2PDFUnit(1));
        // this should round up, but since it's millipts...
        assertEquals(0, dejavuTTFFile.convertTTFUnit2PDFUnit(2));
        // ensure behaviour is the same for negative numbers
        assertEquals(0, dejavuTTFFile.convertTTFUnit2PDFUnit(-0));
        assertEquals(-1000, dejavuTTFFile.convertTTFUnit2PDFUnit(-2048));
        assertEquals(0, dejavuTTFFile.convertTTFUnit2PDFUnit(-1));
        assertEquals(0, dejavuTTFFile.convertTTFUnit2PDFUnit(-2));
    }

    /**
     * Test checkTTC()
     * @throws IOException exception
     */
    @Test
    public void testCheckTTC() throws IOException {
        // DejaVu is not a TTC, thus this returns true
        assertTrue(dejavuTTFFile.checkTTC(""));
        assertTrue(droidmonoTTFFile.checkTTC(""));
        /*
         * Cannot reasonably test the rest of this method without an actual truetype collection
         * because all methods in FontFileReader are "final" and thus mocking isn't possible.
         */
    }

    /**
     * Test getAnsiKerning() - Tests values retrieved from the kern table in the font file.
     */
    @Test
    public void testGetAnsiKerning() {
        Map<Integer, Map<Integer, Integer>> ansiKerning = dejavuTTFFile.getKerning();
        if (ansiKerning.isEmpty()) {
            fail();
        }
        Integer k1 = ansiKerning.get(Integer.valueOf('A')).get(
                Integer.valueOf('T'));
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-112), k1.intValue());
        Integer k2 = ansiKerning.get(Integer.valueOf('Y')).get(Integer.valueOf('u'));
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-178), k2.intValue());

        // DroidSansMono doens't have kerning (it's mono-spaced)
        ansiKerning = droidmonoTTFFile.getAnsiKerning();
        if (!ansiKerning.isEmpty()) {
            fail("DroidSansMono shouldn't have any kerning data.");
        }
    }

    /**
     * Test getCapHeight - there are several paths to test:
     * 1) The PCLT table (if present)
     * 2) The yMax (3rd) value, for the bounding box, for 'H' in the glyf table.
     * if not the above:
     * 3) The caps height in the OS/2 table
     * Tests values retrieved from analysing the font file.
     */
    @Test
    public void testGetCapHeight() {
        // DejaVu doesn't have the PCLT table and so these have to be guessed
        // The height is approximated to be the height of the "H" which for
        // Deja = 1493 TTFunits
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(1493), dejavuTTFFile.getCapHeight());
        // DroidSansMono doesn't have a PCLT table either
        // height of "H" = 1462
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(1462),
                droidmonoTTFFile.getCapHeight());
    }

    /**
     * Test getCharSetName() - check that it returns "WinAnsiEncoding".
     */
    @Test
    public void testGetCharSetName() {
        assertTrue("WinAnsiEncoding".equals(dejavuTTFFile.getCharSetName()));
        assertTrue("WinAnsiEncoding".equals(droidmonoTTFFile.getCharSetName()));
    }

    /**
     * Test getCharWidth() - Test values retrieved from the metrics in the glyf table in
     * the font file.
     */
    @Test
    public void testGetCharWidth() {
        // Arbitrarily test a few values:
        // The width of "H" (Unicode index 0x0048) is 1786
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(1786), dejavuTTFFile.getCharWidth(0x48));
        // The width of "i" (unicode index 0x0069) is 655 TTFunits
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(655), dejavuTTFFile.getCharWidth(0x69));
        // final check, "!" (unicode index 0x0021) is 823 TTFunits
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(823), dejavuTTFFile.getCharWidth(0x21));

        // All the glyphs should be the same width in DroidSansMono (mono-spaced)
        int charWidth = droidmonoTTFFile.convertTTFUnit2PDFUnit(1229);
        for (int i = 0; i < 255; i++) {
            assertEquals(charWidth, droidmonoTTFFile.getCharWidth(i));
        }
    }

    /**
     * TODO: add implementation to this test
     */
    public void testGetCMaps() {
    }

    /**
     * Test getFamilyNames() - Test value retrieved from the name table in the font file.
     */
    @Test
    public void testGetFamilyNames() {
        assertEquals(1, dejavuTTFFile.getFamilyNames().size());
        for (String name : dejavuTTFFile.getFamilyNames()) {
            assertEquals("DejaVu LGC Serif", name);
        }
        assertEquals(1, droidmonoTTFFile.getFamilyNames().size());
        for (String name : droidmonoTTFFile.getFamilyNames()) {
            assertEquals("Droid Sans Mono", name);
        }
    }

    /**
     * Test getFirstChar() - TODO: implement a more intelligent test here.
     */
    @Test
    public void testGetFirstChar() {
        // Not really sure how to test this intelligently
        assertEquals(0, dejavuTTFFile.getFirstChar());
        assertEquals(0, droidmonoTTFFile.getFirstChar());
    }

    /**
     * Test getFlags() - Test values retrieved from the POST table in the font file.
     */
    @Test
    public void testGetFlags() {
        /* DejaVu flags are:
         * italic angle = 0
         * fixed pitch = 0
         * has serifs = true (default value; this font doesn't have a PCLT table)
         */
        int flags = dejavuTTFFile.getFlags();
        assertEquals(0, flags  & 64);       // Italics angle = 0
        assertEquals(32, flags & 32);       // Adobe standard charset
        assertEquals(0, flags & 2);         // fixed pitch = 0
        assertEquals(1, flags & 1);         // has serifs = 1 (true)
        /*
         * Droid flags are:
         * italic angle = 0
         * fixed pitch = 1
         * has serifs = true (default value; this font doesn't have a PCLT table)
         */
        flags = droidmonoTTFFile.getFlags();
        assertEquals(0, flags & 64);
        assertEquals(32, flags & 32);
        assertEquals(2, flags & 2);
        assertEquals(1, flags & 1);
    }

    /**
     * Test getFontBBox() - Test values retrieved from values in the HEAD table in the font file.
     */
    @Test
    public void testGetFontBBox() {
        int[] bBox = dejavuTTFFile.getFontBBox();
        /*
         * The head table has the following values(DejaVu):
         * xmin = -1576, ymin = -710, xmax = 3439, ymax = 2544
         */
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-1576), bBox[0]);
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-710), bBox[1]);
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(3439), bBox[2]);
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(2544), bBox[3]);
        /*
         * The head table has the following values (DroidSansMono):
         * xmin = -312, ymin= -555, xmax = 1315, ymax = 2163
         */
        bBox = droidmonoTTFFile.getFontBBox();
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(-312), bBox[0]);
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(-555), bBox[1]);
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(1315), bBox[2]);
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(2163), bBox[3]);
    }

    /**
     * Test getFullName() - Test value retrieved from the name table in the font file.
     */
    @Test
    public void testGetFullName() {
        assertEquals("DejaVu LGC Serif", dejavuTTFFile.getFullName());
        assertEquals("Droid Sans Mono", droidmonoTTFFile.getFullName());
    }

    /**
     * Test getGlyphName - Test value retrieved from the POST table in the font file.
     */
    @Test
    public void testGetGlyphName() {
        assertEquals("H", dejavuTTFFile.getGlyphName(43));
        assertEquals("H", droidmonoTTFFile.getGlyphName(43));
    }

    /**
     * Test getItalicAngle() - Test value retrieved from the POST table in the font file.
     */
    @Test
    public void testGetItalicAngle() {
        assertEquals("0", dejavuTTFFile.getItalicAngle());
        assertEquals("0", droidmonoTTFFile.getItalicAngle());
    }

    /**
     * Test getKerning() - Test values retrieved from the kern table in the font file.
     */
    @Test
    public void testGetKerning() {
        Map<Integer, Map<Integer, Integer>> kerning = dejavuTTFFile.getKerning();
        if (kerning.isEmpty()) {
            fail();
        }
        Integer k1 = kerning.get(Integer.valueOf('A')).get(Integer.valueOf('T'));
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-112), k1.intValue());
        Integer k2 = kerning.get(Integer.valueOf('K')).get(Integer.valueOf('u'));
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(-45), k2.intValue());

        // DroidSansMono has no kerning data (mono-spaced)
        kerning = droidmonoTTFFile.getKerning();
        if (!kerning.isEmpty()) {
            fail("DroidSansMono shouldn't have any kerning data");
        }
    }

    /**
     * Test lastChar() - TODO: implement a more intelligent test
     */
    @Test
    public void testLastChar() {
        assertEquals(0xff, dejavuTTFFile.getLastChar());
        assertEquals(0xff, droidmonoTTFFile.getLastChar());
    }

    /**
     * Test getLowerCaseAscent() - There are several paths to test:
     * 1) The values in the HHEA table (see code)
     * 2) Fall back to values from the OS/2 table
     * Test values retrieved from the font file.
     */
    @Test
    public void testGetLowerCaseAscent() {
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(1556),
                dejavuTTFFile.getLowerCaseAscent());
        // Curiously the same value
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(1556),
                droidmonoTTFFile.getLowerCaseAscent());
    }

    /**
     * Test getPostScriptName() - Test values retrieved from the post table in the font file.
     */
    @Test
    public void testGetPostScriptName() {
        assertEquals(PostScriptVersion.V2, dejavuTTFFile.getPostScriptVersion());
        assertEquals(PostScriptVersion.V2, droidmonoTTFFile.getPostScriptVersion());
    }

    /**
     * Test getStemV() - Undefined.
     */
    @Test
    public void testGetStemV() {
        // Undefined
        assertEquals("0", dejavuTTFFile.getStemV());
        assertEquals("0", droidmonoTTFFile.getStemV());
    }

    /**
     * Test getSubFamilyName() - Test values retrieved from the name table in the font file.
     */
    @Test
    public void testGetSubFamilyName() {
        assertEquals("Book", dejavuTTFFile.getSubFamilyName());
        assertEquals("Regular", droidmonoTTFFile.getSubFamilyName());
    }

    /**
     * Test getTTCnames() - TODO: add implementation with TTC font.
     */
    public void testGetTTCnames() {
        // Can't test with with DejaVu since it's not a TrueType Collection
    }

    /**
     * Test getWeightClass() - Test value retrieved from the OS/2 table in the font file.
     */
    @Test
    public void testGetWeightClass() {
        // Retrieved from OS/2 table
        assertEquals(400, dejavuTTFFile.getWeightClass());
        assertEquals(400, droidmonoTTFFile.getWeightClass());
    }

    /**
     * Test getWidths() - Test values retrieved from the hmtx table in the font file.
     */
    @Test
    public void testGetWidths() {
        int[] widths = dejavuTTFFile.getWidths();
        // using the width of 'A' index = 36
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(1479), widths[36]);
        // using the width of '|' index = 95
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(690), widths[95]);
        widths = droidmonoTTFFile.getWidths();
        // DroidSansMono should have all widths the same size (mono-spaced)
        int width = droidmonoTTFFile.convertTTFUnit2PDFUnit(1229);
        for (int i = 0; i < 255; i++) {
            assertEquals(width, widths[i]);
        }
    }

    /**
     * Test getXHeight() - There are several paths to test:
     * 1) The PCLT table (if available)
     * 2) The yMax for the bounding box for 'x' in the glyf table.
     * Fall back:
     * 3) The xheight in the OS/2 table.
     */
    @Test
    public void testGetXHeight() {
        // Since there's no PCLT table, the height of 'x' is used for both DejaVu and DroidSansMono
        assertEquals(dejavuTTFFile.convertTTFUnit2PDFUnit(1064), dejavuTTFFile.getXHeight());
        assertEquals(droidmonoTTFFile.convertTTFUnit2PDFUnit(1098), droidmonoTTFFile.getXHeight());
    }

    /**
     * Test isCFF() - TODO: add test for a CFF font.
     */
    @Test
    public void testIsCFF() {
        // Neither DejaVu nor DroidSansMono are a compact format font
        assertEquals(false, dejavuTTFFile.isCFF());
        assertEquals(false, droidmonoTTFFile.isCFF());
    }

    /**
     * Test isEmbeddable() - Test value retrieved from the OS/2 table in the font file.
     */
    @Test
    public void testIsEmbeddable() {
        // Dejavu and DroidSansMono are both embeddable
        assertEquals(true, dejavuTTFFile.isEmbeddable());
        assertEquals(true, droidmonoTTFFile.isEmbeddable());
    }

    /** Underline position and thickness. */
    @Test
    public void testUnderline() {
        assertEquals(-63, dejavuTTFFile.getUnderlinePosition());
        assertEquals(43, dejavuTTFFile.getUnderlineThickness());
        assertEquals(-75, droidmonoTTFFile.getUnderlinePosition());
        assertEquals(49, droidmonoTTFFile.getUnderlineThickness());
    }

    /** Strikeout position and thickness. */
    @Test
    public void testStrikeout() {
        assertEquals(258, dejavuTTFFile.getStrikeoutPosition());
        assertEquals(49, dejavuTTFFile.getStrikeoutThickness());
        assertEquals(243, droidmonoTTFFile.getStrikeoutPosition());
        assertEquals(49, droidmonoTTFFile.getStrikeoutThickness());
    }

    /**
     * Test readFont() - Add implementation if necessary.
     */
    public void testReadFont() {
        // I'm pretty sure we've tested this with all the other tests
    }
}
