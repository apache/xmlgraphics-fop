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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Glyphs;

/**
 * Reads a TrueType file or a TrueType Collection.
 * The TrueType spec can be found at the Microsoft.
 * Typography site: http://www.microsoft.com/truetype/
 */
public class TTFFile {

    static final byte NTABS = 24;
    static final int NMACGLYPHS = 258;
    static final int MAX_CHAR_CODE = 255;
    static final int ENC_BUF_SIZE = 1024;

    /** Set to true to get even more debug output than with level DEBUG */
    public static final boolean TRACE_ENABLED = false;

    private String encoding = "WinAnsiEncoding";    // Default encoding

    private short firstChar = 0;
    private boolean isEmbeddable = true;
    private boolean hasSerifs = true;
    /**
     * Table directory
     */
    protected Map dirTabs;
    private Map kerningTab;                          // for CIDs
    private Map ansiKerningTab;                      // For winAnsiEncoding
    private List cmaps;
    private List unicodeMapping;

    private int upem;                                // unitsPerEm from "head" table
    private int nhmtx;                               // Number of horizontal metrics
    private int postFormat;
    private int locaFormat;
    /**
     * Offset to last loca
     */
    protected long lastLoca = 0;
    private int numberOfGlyphs; // Number of glyphs in font (read from "maxp" table)
    private int nmGlyphs;                            // Used in fixWidths - remove?

    /**
     * Contains glyph data
     */
    protected TTFMtxEntry[] mtxTab;                  // Contains glyph data
    private int[] mtxEncoded = null;

    private String fontName = "";
    private String fullName = "";
    private String notice = "";
    private String familyName = "";
    private String subFamilyName = "";

    private long italicAngle = 0;
    private long isFixedPitch = 0;
    private int fontBBox1 = 0;
    private int fontBBox2 = 0;
    private int fontBBox3 = 0;
    private int fontBBox4 = 0;
    private int capHeight = 0;
    private int underlinePosition = 0;
    private int underlineThickness = 0;
    private int xHeight = 0;
    //Effective ascender/descender
    private int ascender = 0;
    private int descender = 0;
    //Ascender/descender from hhea table
    private int hheaAscender = 0;
    private int hheaDescender = 0;
    //Ascender/descender from OS/2 table
    private int os2Ascender = 0;
    private int os2Descender = 0;

    private short lastChar = 0;

    private int[] ansiWidth;
    private Map ansiIndex;

    private TTFDirTabEntry currentDirTab;

    /**
     * logging instance
     */
    protected Log log = LogFactory.getLog(TTFFile.class);

    /**
     * Position inputstream to position indicated
     * in the dirtab offset + offset
     */
    void seekTab(FontFileReader in, String name,
                  long offset) throws IOException {
        TTFDirTabEntry dt = (TTFDirTabEntry)dirTabs.get(name);
        if (dt == null) {
            log.error("Dirtab " + name + " not found.");
        } else {
            in.seekSet(dt.getOffset() + offset);
            this.currentDirTab = dt;
        }
    }

    /**
     * Convert from truetype unit to pdf unit based on the
     * unitsPerEm field in the "head" table
     * @param n truetype unit
     * @return pdf unit
     */
    public int convertTTFUnit2PDFUnit(int n) {
        int ret;
        if (n < 0) {
            long rest1 = n % upem;
            long storrest = 1000 * rest1;
            long ledd2 = (storrest != 0 ? rest1 / storrest : 0);  
            ret = -((-1000 * n) / upem - (int)ledd2);
        } else {
            ret = (n / upem) * 1000 + ((n % upem) * 1000) / upem;
        }

        return ret;
    }

    /**
     * Read the cmap table,
     * return false if the table is not present or only unsupported
     * tables are present. Currently only unicode cmaps are supported.
     * Set the unicodeIndex in the TTFMtxEntries and fills in the
     * cmaps vector.
     */
    private boolean readCMAP(FontFileReader in) throws IOException {

        unicodeMapping = new java.util.ArrayList();

        //Read CMAP table and correct mtxTab.index
        int mtxPtr = 0;

        seekTab(in, "cmap", 2);
        int numCMap = in.readTTFUShort();    // Number of cmap subtables
        long cmapUniOffset = 0;

        log.info(numCMap + " cmap tables");

        //Read offset for all tables. We are only interested in the unicode table
        for (int i = 0; i < numCMap; i++) {
            int cmapPID = in.readTTFUShort();
            int cmapEID = in.readTTFUShort();
            long cmapOffset = in.readTTFULong();

            log.debug("Platform ID: " + cmapPID + " Encoding: " + cmapEID);

            if (cmapPID == 3 && cmapEID == 1) {
                cmapUniOffset = cmapOffset;
            }
        }

        if (cmapUniOffset <= 0) {
            log.fatal("Unicode cmap table not present");
            log.fatal("Unsupported format: Aborting");
            return false;
        }

        // Read unicode cmap
        seekTab(in, "cmap", cmapUniOffset);
        int cmapFormat = in.readTTFUShort();
        /*int cmap_length =*/ in.readTTFUShort(); //skip cmap length

        log.info("CMAP format: " + cmapFormat);

        if (cmapFormat == 4) {
            in.skip(2);    // Skip version number
            int cmapSegCountX2 = in.readTTFUShort();
            int cmapSearchRange = in.readTTFUShort();
            int cmapEntrySelector = in.readTTFUShort();
            int cmapRangeShift = in.readTTFUShort();

            if (log.isDebugEnabled()) {
                log.debug("segCountX2   : " + cmapSegCountX2);
                log.debug("searchRange  : " + cmapSearchRange);
                log.debug("entrySelector: " + cmapEntrySelector);
                log.debug("rangeShift   : " + cmapRangeShift);
            }


            int[] cmapEndCounts = new int[cmapSegCountX2 / 2];
            int[] cmapStartCounts = new int[cmapSegCountX2 / 2];
            int[] cmapDeltas = new int[cmapSegCountX2 / 2];
            int[] cmapRangeOffsets = new int[cmapSegCountX2 / 2];

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapEndCounts[i] = in.readTTFUShort();
            }

            in.skip(2);    // Skip reservedPad

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapStartCounts[i] = in.readTTFUShort();
            }

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapDeltas[i] = in.readTTFShort();
            }

            //int startRangeOffset = in.getCurrentPos();

            for (int i = 0; i < (cmapSegCountX2 / 2); i++) {
                cmapRangeOffsets[i] = in.readTTFUShort();
            }

            int glyphIdArrayOffset = in.getCurrentPos();

            // Insert the unicode id for the glyphs in mtxTab
            // and fill in the cmaps ArrayList

            for (int i = 0; i < cmapStartCounts.length; i++) {

                if (log.isTraceEnabled()) {
                    log.trace(i + ": " + cmapStartCounts[i]
                                                         + " - " + cmapEndCounts[i]);
                }

                for (int j = cmapStartCounts[i]; j <= cmapEndCounts[i]; j++) {

                    // Update lastChar
                    if (j < 256 && j > lastChar) {
                        lastChar = (short)j;
                    }

                    if (mtxPtr < mtxTab.length) {
                        int glyphIdx;
                        // the last character 65535 = .notdef
                        // may have a range offset
                        if (cmapRangeOffsets[i] != 0 && j != 65535) {
                            int glyphOffset = glyphIdArrayOffset
                                + ((cmapRangeOffsets[i] / 2)
                                    + (j - cmapStartCounts[i])
                                    + (i)
                                    - cmapSegCountX2 / 2) * 2;
                            in.seekSet(glyphOffset);
                            glyphIdx = (in.readTTFUShort() + cmapDeltas[i])
                                       & 0xffff;

                            unicodeMapping.add(new UnicodeMapping(glyphIdx, j));
                            mtxTab[glyphIdx].getUnicodeIndex().add(new Integer(j));


                            // Also add winAnsiWidth
                            List v = (List)ansiIndex.get(new Integer(j));
                            if (v != null) {
                                Iterator e = v.listIterator();
                                while (e.hasNext()) {
                                    Integer aIdx = (Integer)e.next();
                                    ansiWidth[aIdx.intValue()] 
                                        = mtxTab[glyphIdx].getWx();

                                    if (log.isTraceEnabled()) {
                                        log.trace("Added width "
                                                + mtxTab[glyphIdx].getWx()
                                                + " uni: " + j
                                                + " ansi: " + aIdx.intValue());
                                    }
                                }
                            }

                            if (log.isTraceEnabled()) {
                                log.trace("Idx: "
                                        + glyphIdx
                                        + " Delta: " + cmapDeltas[i]
                                        + " Unicode: " + j
                                        + " name: " + mtxTab[glyphIdx].getName());
                            }
                        } else {
                            glyphIdx = (j + cmapDeltas[i]) & 0xffff;

                            if (glyphIdx < mtxTab.length) {
                                mtxTab[glyphIdx].getUnicodeIndex().add(new Integer(j));
                            } else {
                                log.debug("Glyph " + glyphIdx
                                                   + " out of range: "
                                                   + mtxTab.length);
                            }

                            unicodeMapping.add(new UnicodeMapping(glyphIdx, j));
                            if (glyphIdx < mtxTab.length) {
                                mtxTab[glyphIdx].getUnicodeIndex().add(new Integer(j));
                            } else {
                                log.debug("Glyph " + glyphIdx
                                                   + " out of range: "
                                                   + mtxTab.length);
                            }

                            // Also add winAnsiWidth
                            List v = (List)ansiIndex.get(new Integer(j));
                            if (v != null) {
                                Iterator e = v.listIterator();
                                while (e.hasNext()) {
                                    Integer aIdx = (Integer)e.next();
                                    ansiWidth[aIdx.intValue()] = mtxTab[glyphIdx].getWx();
                                }
                            }

                            //getLogger().debug("IIdx: " +
                            //    mtxPtr +
                            //    " Delta: " + cmap_deltas[i] +
                            //    " Unicode: " + j +
                            //    " name: " +
                            //    mtxTab[(j+cmap_deltas[i]) & 0xffff].name);

                        }
                        if (glyphIdx < mtxTab.length) {
                            if (mtxTab[glyphIdx].getUnicodeIndex().size() < 2) {
                                mtxPtr++;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Print first char/last char
     */
    private void printMaxMin() {
        int min = 255;
        int max = 0;
        for (int i = 0; i < mtxTab.length; i++) {
            if (mtxTab[i].getIndex() < min) {
                min = mtxTab[i].getIndex();
            }
            if (mtxTab[i].getIndex() > max) {
                max = mtxTab[i].getIndex();
            }
        }
        log.info("Min: " + min);
        log.info("Max: " + max);
    }


    /**
     * Reads the font using a FontFileReader.
     *
     * @param in The FontFileReader to use
     * @throws IOException In case of an I/O problem
     */
    public void readFont(FontFileReader in) throws IOException {
        readFont(in, (String)null);
    }

    /**
     * initialize the ansiWidths array (for winAnsiEncoding)
     * and fill with the missingwidth
     */
    private void initAnsiWidths() {
        ansiWidth = new int[256];
        for (int i = 0; i < 256; i++) {
            ansiWidth[i] = mtxTab[0].getWx();
        }

        // Create an index hash to the ansiWidth
        // Can't just index the winAnsiEncoding when inserting widths
        // same char (eg bullet) is repeated more than one place
        ansiIndex = new java.util.HashMap();
        for (int i = 32; i < Glyphs.WINANSI_ENCODING.length; i++) {
            Integer ansi = new Integer(i);
            Integer uni = new Integer((int)Glyphs.WINANSI_ENCODING[i]);

            List v = (List)ansiIndex.get(uni);
            if (v == null) {
                v = new java.util.ArrayList();
                ansiIndex.put(uni, v);
            }
            v.add(ansi);
        }
    }

    /**
     * Read the font data.
     * If the fontfile is a TrueType Collection (.ttc file)
     * the name of the font to read data for must be supplied,
     * else the name is ignored.
     *
     * @param in The FontFileReader to use
     * @param name The name of the font
     * @return boolean Returns true if the font is valid
     * @throws IOException In case of an I/O problem
     */
    public boolean readFont(FontFileReader in, String name) throws IOException {

        /*
         * Check if TrueType collection, and that the name
         * exists in the collection
         */
        if (!checkTTC(in, name)) {
            if (name == null) {
                throw new IllegalArgumentException(
                    "For TrueType collection you must specify which font "
                    + "to select (-ttcname)");
            } else {
                throw new IOException(
                    "Name does not exist in the TrueType collection: " + name);
            }
        }

        readDirTabs(in);
        readFontHeader(in);
        getNumGlyphs(in);
        log.info("Number of glyphs in font: " + numberOfGlyphs);
        readHorizontalHeader(in);
        readHorizontalMetrics(in);
        initAnsiWidths();
        readPostScript(in);
        readOS2(in);
        determineAscDesc();
        readIndexToLocation(in);
        readGlyf(in);
        readName(in);
        boolean pcltFound = readPCLT(in);
        // Read cmap table and fill in ansiwidths
        boolean valid = readCMAP(in);
        if (!valid) {
            return false;
        }
        // Create cmaps for bfentries
        createCMaps();
        // print_max_min();

        readKerning(in);
        guessVerticalMetricsFromGlyphBBox();
        return true;
    }

    private void createCMaps() {
        cmaps = new java.util.ArrayList();
        TTFCmapEntry tce = new TTFCmapEntry();

        Iterator e = unicodeMapping.listIterator();
        UnicodeMapping um = (UnicodeMapping)e.next();
        UnicodeMapping lastMapping = um;

        tce.setUnicodeStart(um.getUnicodeIndex());
        tce.setGlyphStartIndex(um.getGlyphIndex());

        while (e.hasNext()) {
            um = (UnicodeMapping)e.next();
            if (((lastMapping.getUnicodeIndex() + 1) != um.getUnicodeIndex())
                    || ((lastMapping.getGlyphIndex() + 1) != um.getGlyphIndex())) {
                tce.setUnicodeEnd(lastMapping.getUnicodeIndex());
                cmaps.add(tce);

                tce = new TTFCmapEntry();
                tce.setUnicodeStart(um.getUnicodeIndex());
                tce.setGlyphStartIndex(um.getGlyphIndex());
            }
            lastMapping = um;
        }

        tce.setUnicodeEnd(um.getUnicodeIndex());
        cmaps.add(tce);
    }

    /**
     * Returns the Windows name of the font.
     * @return String The Windows name
     */
    public String getWindowsName() {
        return familyName + "," + subFamilyName;
    }

    /**
     * Returns the PostScript name of the font.
     * @return String The PostScript name
     */
    public String getPostScriptName() {
        if ("Regular".equals(subFamilyName) || "Roman".equals(subFamilyName)) {
            return familyName;
        } else {
            return familyName + "," + subFamilyName;
        }
    }

    /**
     * Returns the font family name of the font.
     * @return String The family name
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Returns the name of the character set used.
     * @return String The caracter set
     */
    public String getCharSetName() {
        return encoding;
    }

    /**
     * Returns the CapHeight attribute of the font.
     * @return int The CapHeight
     */
    public int getCapHeight() {
        return (int)convertTTFUnit2PDFUnit(capHeight);
    }

    /**
     * Returns the XHeight attribute of the font.
     * @return int The XHeight
     */
    public int getXHeight() {
        return (int)convertTTFUnit2PDFUnit(xHeight);
    }

    /**
     * Returns the Flags attribute of the font.
     * @return int The Flags
     */
    public int getFlags() {
        int flags = 32;    // Use Adobe Standard charset
        if (italicAngle != 0) {
            flags = flags | 64;
        }
        if (isFixedPitch != 0) {
            flags = flags | 2;
        }
        if (hasSerifs) {
            flags = flags | 1;
        }
        return flags;
    }


    /**
     * Returns the StemV attribute of the font.
     * @return String The StemV
     */
    public String getStemV() {
        return "0";
    }

    /**
     * Returns the ItalicAngle attribute of the font.
     * @return String The ItalicAngle
     */
    public String getItalicAngle() {
        String ia = Short.toString((short)(italicAngle / 0x10000));

        // This is the correct italic angle, however only int italic
        // angles are supported at the moment so this is commented out.
        /*
         * if ((italicAngle % 0x10000) > 0 )
         * ia=ia+(comma+Short.toString((short)((short)((italicAngle % 0x10000)*1000)/0x10000)));
         */
        return ia;
    }

    /**
     * Returns the font bounding box.
     * @return int[] The font bbox
     */
    public int[] getFontBBox() {
        final int[] fbb = new int[4];
        fbb[0] = (int)convertTTFUnit2PDFUnit(fontBBox1);
        fbb[1] = (int)convertTTFUnit2PDFUnit(fontBBox2);
        fbb[2] = (int)convertTTFUnit2PDFUnit(fontBBox3);
        fbb[3] = (int)convertTTFUnit2PDFUnit(fontBBox4);

        return fbb;
    }

    /**
     * Returns the LowerCaseAscent attribute of the font.
     * @return int The LowerCaseAscent
     */
    public int getLowerCaseAscent() {
        return (int)convertTTFUnit2PDFUnit(ascender);
    }

    /**
     * Returns the LowerCaseDescent attribute of the font.
     * @return int The LowerCaseDescent
     */
    public int getLowerCaseDescent() {
        return (int)convertTTFUnit2PDFUnit(descender);
    }

    /**
     * Returns the index of the last character, but this is for WinAnsiEncoding
     * only, so the last char is < 256.
     * @return short Index of the last character (<256)
     */
    public short getLastChar() {
        return lastChar;
    }

    /**
     * Returns the index of the first character.
     * @return short Index of the first character
     */
    public short getFirstChar() {
        return firstChar;
    }

    /**
     * Returns an array of character widths.
     * @return int[] The character widths
     */
    public int[] getWidths() {
        int[] wx = new int[mtxTab.length];
        for (int i = 0; i < wx.length; i++) {
            wx[i] = (int)convertTTFUnit2PDFUnit(mtxTab[i].getWx());
        }

        return wx;
    }

    /**
     * Returns the width of a given character.
     * @param idx Index of the character
     * @return int Standard width
     */
    public int getCharWidth(int idx) {
        return (int)convertTTFUnit2PDFUnit(ansiWidth[idx]);
    }

    /**
     * Returns the kerning table.
     * @return Map The kerning table
     */
    public Map getKerning() {
        return kerningTab;
    }

    /**
     * Returns the ANSI kerning table.
     * @return Map The ANSI kerning table
     */
    public Map getAnsiKerning() {
        return ansiKerningTab;
    }

    /**
     * Indicates if the font may be embedded.
     * @return boolean True if it may be embedded
     */
    public boolean isEmbeddable() {
        return isEmbeddable;
    }


    /**
     * Read Table Directory from the current position in the
     * FontFileReader and fill the global HashMap dirTabs
     * with the table name (String) as key and a TTFDirTabEntry
     * as value.
     * @param in FontFileReader to read the table directory from
     * @throws IOException in case of an I/O problem
     */
    protected void readDirTabs(FontFileReader in) throws IOException {
        in.skip(4);    // TTF_FIXED_SIZE
        int ntabs = in.readTTFUShort();
        in.skip(6);    // 3xTTF_USHORT_SIZE

        dirTabs = new java.util.HashMap();
        TTFDirTabEntry[] pd = new TTFDirTabEntry[ntabs];
        log.debug("Reading " + ntabs + " dir tables");
        for (int i = 0; i < ntabs; i++) {
            pd[i] = new TTFDirTabEntry();
            dirTabs.put(pd[i].read(in), pd[i]);
        }
    }

    /**
     * Read the "head" table, this reads the bounding box and
     * sets the upem (unitsPerEM) variable
     * @param in FontFileReader to read the header from
     * @throws IOException in case of an I/O problem
     */
    protected void readFontHeader(FontFileReader in) throws IOException {
        seekTab(in, "head", 2 * 4 + 2 * 4 + 2);
        upem = in.readTTFUShort();
        log.debug("unit per em: " + upem);

        in.skip(16);

        fontBBox1 = in.readTTFShort();
        fontBBox2 = in.readTTFShort();
        fontBBox3 = in.readTTFShort();
        fontBBox4 = in.readTTFShort();

        in.skip(2 + 2 + 2);

        locaFormat = in.readTTFShort();
    }

    /**
     * Read the number of glyphs from the "maxp" table
     * @param in FontFileReader to read the number of glyphs from
     * @throws IOException in case of an I/O problem
     */
    protected void getNumGlyphs(FontFileReader in) throws IOException {
        seekTab(in, "maxp", 4);
        numberOfGlyphs = in.readTTFUShort();
    }


    /**
     * Read the "hhea" table to find the ascender and descender and
     * size of "hmtx" table, as a fixed size font might have only
     * one width.
     * @param in FontFileReader to read the hhea table from
     * @throws IOException in case of an I/O problem
     */
    protected void readHorizontalHeader(FontFileReader in)
            throws IOException {
        seekTab(in, "hhea", 4);
        hheaAscender = in.readTTFShort(); 
        log.debug("hhea.Ascender: " + hheaAscender + " " + convertTTFUnit2PDFUnit(hheaAscender));
        hheaDescender = in.readTTFShort();
        log.debug("hhea.Descender: " + hheaDescender + " " + convertTTFUnit2PDFUnit(hheaDescender));
        
        in.skip(2 + 2 + 3 * 2 + 8 * 2);
        nhmtx = in.readTTFUShort();
        log.debug("Number of horizontal metrics: " + nhmtx);


    }

    /**
     * Read "hmtx" table and put the horizontal metrics
     * in the mtxTab array. If the number of metrics is less
     * than the number of glyphs (eg fixed size fonts), extend
     * the mtxTab array and fill in the missing widths
     * @param in FontFileReader to read the hmtx table from
     * @throws IOException in case of an I/O problem
     */
    protected void readHorizontalMetrics(FontFileReader in)
            throws IOException {
        seekTab(in, "hmtx", 0);

        int mtxSize = Math.max(numberOfGlyphs, nhmtx);
        mtxTab = new TTFMtxEntry[mtxSize];

        if (TRACE_ENABLED) {
            log.debug("*** Widths array: \n");
        }
        for (int i = 0; i < mtxSize; i++) {
            mtxTab[i] = new TTFMtxEntry();
        }
        for (int i = 0; i < nhmtx; i++) {
            mtxTab[i].setWx(in.readTTFUShort());
            mtxTab[i].setLsb(in.readTTFUShort());

            if (TRACE_ENABLED) {
                if (log.isDebugEnabled()) {
                    log.debug("   width[" + i + "] = "
                        + convertTTFUnit2PDFUnit(mtxTab[i].getWx()) + ";");
                }
            }
        }

        if (nhmtx < mtxSize) {
            // Fill in the missing widths
            int lastWidth = mtxTab[nhmtx - 1].getWx();
            for (int i = nhmtx; i < mtxSize; i++) {
                mtxTab[i].setWx(lastWidth);
                mtxTab[i].setLsb(in.readTTFUShort());
            }
        }
    }


    /**
     * Read the "post" table
     * containing the PostScript names of the glyphs.
     */
    private final void readPostScript(FontFileReader in) throws IOException {
        seekTab(in, "post", 0);
        postFormat = in.readTTFLong();
        italicAngle = in.readTTFULong();
        underlinePosition = in.readTTFShort();
        underlineThickness = in.readTTFShort();
        isFixedPitch = in.readTTFULong();

        //Skip memory usage values
        in.skip(4 * 4);

        log.debug("PostScript format: 0x" + Integer.toHexString(postFormat));
        switch (postFormat) {
        case 0x00010000:
            log.debug("PostScript format 1");
            for (int i = 0; i < Glyphs.MAC_GLYPH_NAMES.length; i++) {
                mtxTab[i].setName(Glyphs.MAC_GLYPH_NAMES[i]);
            }
            break;
        case 0x00020000:
            log.debug("PostScript format 2");
            int numGlyphStrings = 0;

            // Read Number of Glyphs
            int l = in.readTTFUShort();

            // Read indexes
            for (int i = 0; i < l; i++) {
                mtxTab[i].setIndex(in.readTTFUShort());

                if (mtxTab[i].getIndex() > 257) {
                    //Index is not in the Macintosh standard set
                    numGlyphStrings++;
                }

                if (log.isTraceEnabled()) {
                    log.trace("PostScript index: " + mtxTab[i].getIndexAsString());
                }
            }

            // firstChar=minIndex;
            String[] psGlyphsBuffer = new String[numGlyphStrings];
            if (log.isDebugEnabled()) {
                log.debug("Reading " + numGlyphStrings
                        + " glyphnames, that are not in the standard Macintosh"
                        + " set. Total number of glyphs=" + l);
            }
            for (int i = 0; i < psGlyphsBuffer.length; i++) {
                psGlyphsBuffer[i] = in.readTTFString(in.readTTFUByte());
            }

            //Set glyph names
            for (int i = 0; i < l; i++) {
                if (mtxTab[i].getIndex() < NMACGLYPHS) {
                    mtxTab[i].setName(Glyphs.MAC_GLYPH_NAMES[mtxTab[i].getIndex()]);
                } else {
                    if (!mtxTab[i].isIndexReserved()) {
                        int k = mtxTab[i].getIndex() - NMACGLYPHS;

                        if (log.isTraceEnabled()) {
                            log.trace(k + " i=" + i + " mtx=" + mtxTab.length
                                + " ps=" + psGlyphsBuffer.length);
                        }

                        mtxTab[i].setName(psGlyphsBuffer[k]);
                    }
                }
            }

            break;
        case 0x00030000:
            // PostScript format 3 contains no glyph names
            log.debug("PostScript format 3");
            break;
        default:
            log.error("Unknown PostScript format: " + postFormat);
        }
    }


    /**
     * Read the "OS/2" table
     */
    private final void readOS2(FontFileReader in) throws IOException {
        // Check if font is embeddable
        if (dirTabs.get("OS/2") != null) {
            seekTab(in, "OS/2", 2 * 4);
            int fsType = in.readTTFUShort();
            if (fsType == 2) {
                isEmbeddable = false;
            } else {
                isEmbeddable = true;
            }
            in.skip(11 * 2);
            in.skip(10); //panose array
            in.skip(4 * 4); //unicode ranges
            in.skip(4);
            in.skip(3 * 2);
            int v;
            os2Ascender = in.readTTFShort(); //sTypoAscender
            log.debug("sTypoAscender: " + os2Ascender 
                        + " " + convertTTFUnit2PDFUnit(os2Ascender));
            os2Descender = in.readTTFShort(); //sTypoDescender
            log.debug("sTypoDescender: " + os2Descender 
                        + " " + convertTTFUnit2PDFUnit(os2Descender));
            v = in.readTTFShort(); //sTypoLineGap
            log.debug("sTypoLineGap: " + v);
            v = in.readTTFUShort(); //usWinAscent
            log.debug("usWinAscent: " + v  + " " + convertTTFUnit2PDFUnit(v));
            v = in.readTTFUShort(); //usWinDescent
            log.debug("usWinDescent: " + v + " " + convertTTFUnit2PDFUnit(v));
            in.skip(2 * 4);
            v = in.readTTFShort(); //sxHeight
            log.debug("sxHeight: " + v);
            v = in.readTTFShort(); //sCapHeight
            log.debug("sCapHeight: " + v);
            
        } else {
            isEmbeddable = true;
        }
    }

    /**
     * Read the "loca" table.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    protected final void readIndexToLocation(FontFileReader in)
            throws IOException {
        seekTab(in, "loca", 0);
        for (int i = 0; i < numberOfGlyphs; i++) {
            mtxTab[i].setOffset(locaFormat == 1 ? in.readTTFULong()
                                 : (in.readTTFUShort() << 1));
        }
        lastLoca = (locaFormat == 1 ? in.readTTFULong()
                    : (in.readTTFUShort() << 1));
    }

    /**
     * Read the "glyf" table to find the bounding boxes.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private final void readGlyf(FontFileReader in) throws IOException {
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get("glyf");
        for (int i = 0; i < (numberOfGlyphs - 1); i++) {
            if (mtxTab[i].getOffset() != mtxTab[i + 1].getOffset()) {
                in.seekSet(dirTab.getOffset() + mtxTab[i].getOffset());
                in.skip(2);
                final int[] bbox = {
                    in.readTTFShort(),
                    in.readTTFShort(),
                    in.readTTFShort(),
                    in.readTTFShort()};
                mtxTab[i].setBoundingBox(bbox);
            } else {
                mtxTab[i].setBoundingBox(mtxTab[0].getBoundingBox());
            }
        }


        long n = ((TTFDirTabEntry)dirTabs.get("glyf")).getOffset();
        for (int i = 0; i < numberOfGlyphs; i++) {
            if ((i + 1) >= mtxTab.length
                    || mtxTab[i].getOffset() != mtxTab[i + 1].getOffset()) {
                in.seekSet(n + mtxTab[i].getOffset());
                in.skip(2);
                final int[] bbox = {
                    in.readTTFShort(),
                    in.readTTFShort(),
                    in.readTTFShort(),
                    in.readTTFShort()};
                mtxTab[i].setBoundingBox(bbox);
            } else {
                /**@todo Verify that this is correct, looks like a copy/paste bug (jm)*/
                final int bbox0 = mtxTab[0].getBoundingBox()[0];
                final int[] bbox = {bbox0, bbox0, bbox0, bbox0};
                mtxTab[i].setBoundingBox(bbox);
                /* Original code
                mtxTab[i].bbox[0] = mtxTab[0].bbox[0];
                mtxTab[i].bbox[1] = mtxTab[0].bbox[0];
                mtxTab[i].bbox[2] = mtxTab[0].bbox[0];
                mtxTab[i].bbox[3] = mtxTab[0].bbox[0]; */
            }
            if (log.isTraceEnabled()) {
                log.trace(mtxTab[i].toString(this));
            }
        }
    }

    /**
     * Read the "name" table.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private final void readName(FontFileReader in) throws IOException {
        seekTab(in, "name", 2);
        int i = in.getCurrentPos();
        int n = in.readTTFUShort();
        int j = in.readTTFUShort() + i - 2;
        i += 2 * 2;

        while (n-- > 0) {
            // getLogger().debug("Iteration: " + n);
            in.seekSet(i);
            final int platformID = in.readTTFUShort();
            final int encodingID = in.readTTFUShort();
            final int languageID = in.readTTFUShort();

            int k = in.readTTFUShort();
            int l = in.readTTFUShort();

            if (((platformID == 1 || platformID == 3) 
                    && (encodingID == 0 || encodingID == 1))
                    && (k == 1 || k == 2 || k == 0 || k == 4 || k == 6)) {
                in.seekSet(j + in.readTTFUShort());
                String txt = in.readTTFString(l);
                
                log.debug(platformID + " " 
                    + encodingID + " "
                    + languageID + " "
                    + k + " " + txt);
                switch (k) {
                case 0:
                    notice = txt;
                    break;
                case 1:
                    familyName = txt;
                    break;
                case 2:
                    subFamilyName = txt;
                    break;
                case 4:
                    fullName = txt;
                    break;
                case 6:
                    fontName = txt;
                    break;
                }
                if (!notice.equals("")
                        && !fullName.equals("")
                        && !fontName.equals("")
                        && !familyName.equals("")
                        && !subFamilyName.equals("")) {
                    break;
                }
            }
            i += 6 * 2;
        }
    }

    /**
     * Read the "PCLT" table to find xHeight and capHeight.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private final boolean readPCLT(FontFileReader in) throws IOException {
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get("PCLT");
        if (dirTab != null) {
            in.seekSet(dirTab.getOffset() + 4 + 4 + 2);
            xHeight = in.readTTFUShort();
            log.debug("xHeight from PCLT: " + xHeight 
                            + " " + convertTTFUnit2PDFUnit(xHeight));
            in.skip(2 * 2);
            capHeight = in.readTTFUShort();
            log.debug("capHeight from PCLT: " + capHeight 
                            + " " + convertTTFUnit2PDFUnit(capHeight));
            in.skip(2 + 16 + 8 + 6 + 1 + 1);

            int serifStyle = in.readTTFUByte();
            serifStyle = serifStyle >> 6;
            serifStyle = serifStyle & 3;
            if (serifStyle == 1) {
                hasSerifs = false;
            } else {
                hasSerifs = true;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines the right source for the ascender and descender values. The problem here is
     * that the interpretation of these values is not the same for every font. There doesn't seem
     * to be a uniform definition of an ascender and a descender. In some fonts
     * the hhea values are defined after the Apple interpretation, but not in every font. The
     * same problem is in the OS/2 table. FOP needs the ascender and descender to determine the
     * baseline so we need values which add up more or less to the "em box". However, due to
     * accent modifiers a character can grow beyond the em box. 
     */
    private void determineAscDesc() {
        int hheaBoxHeight = hheaAscender - hheaDescender;
        int os2BoxHeight = os2Ascender - os2Descender;
        if (os2Ascender > 0 && os2BoxHeight <= upem) {
            ascender = os2Ascender;
            descender = os2Descender;
        } else if (hheaAscender > 0 && hheaBoxHeight <= upem) {
            ascender = hheaAscender;
            descender = hheaDescender;
        } else {
            if (os2Ascender > 0) {
                //Fall back to info from OS/2 if possible
                ascender = os2Ascender;
                descender = os2Descender;
            } else {
                ascender = hheaAscender;
                descender = hheaDescender;
            }
        }

        log.debug("Font box height: " + (ascender - descender));
        if (ascender - descender > upem) {
            log.warn("Ascender and descender together are larger than the em box."
                    + " This could lead to a wrong baseline placement in Apache FOP.");
        }
    }

    private void guessVerticalMetricsFromGlyphBBox() {
        // Approximate capHeight from height of "H"
        // It's most unlikely that a font misses the PCLT table
        // This also assumes that postscriptnames exists ("H")
        // Should look it up int the cmap (that wouldn't help
        // for charsets without H anyway...)
        // Same for xHeight with the letter "x"
        int localCapHeight = 0;
        int localXHeight = 0;
        int localAscender = 0;
        int localDescender = 0;
        for (int i = 0; i < mtxTab.length; i++) {
            if ("H".equals(mtxTab[i].getName())) {
                localCapHeight = mtxTab[i].getBoundingBox()[3];
            } else if ("x".equals(mtxTab[i].getName())) {
                localXHeight = mtxTab[i].getBoundingBox()[3];
            } else if ("d".equals(mtxTab[i].getName())) {
                localAscender = mtxTab[i].getBoundingBox()[3];
            } else if ("p".equals(mtxTab[i].getName())) {
                localDescender = mtxTab[i].getBoundingBox()[1];
            } else {
                // OpenType Fonts with a version 3.0 "post" table don't have glyph names.
                // Use Unicode indices instead.
                List unicodeIndex = mtxTab[i].getUnicodeIndex();
                if (unicodeIndex.size() > 0) {
                    //Only the first index is used
                    char ch = (char)((Integer)unicodeIndex.get(0)).intValue();
                    if (ch == 'H') {
                        localCapHeight = mtxTab[i].getBoundingBox()[3];
                    } else if (ch == 'x') {
                        localXHeight = mtxTab[i].getBoundingBox()[3];
                    } else if (ch == 'd') {
                        localAscender = mtxTab[i].getBoundingBox()[3];
                    } else if (ch == 'p') {
                        localDescender = mtxTab[i].getBoundingBox()[1];
                    }
                }
            }
        }
        log.debug("Ascender from glyph 'd': " + localAscender 
                + " " + convertTTFUnit2PDFUnit(localAscender));
        log.debug("Descender from glyph 'p': " + localDescender
                + " " + convertTTFUnit2PDFUnit(localDescender));
        if (ascender - descender > upem) {
            log.debug("Replacing specified ascender/descender with derived values to get values"
                    + " which fit in the em box.");
            ascender = localAscender;
            descender = localDescender;
        }

        log.debug("xHeight from glyph 'x': " + localXHeight
                + " " + convertTTFUnit2PDFUnit(localXHeight));
        log.debug("CapHeight from glyph 'H': " + localCapHeight
                + " " + convertTTFUnit2PDFUnit(localCapHeight));
        if (capHeight == 0) {
            capHeight = localCapHeight;
            if (localCapHeight == 0) {
                log.warn("capHeight value could not be determined."
                        + " The font may not work as expected.");
            }
        }
        if (xHeight == 0) {
            xHeight = localXHeight;
            if (xHeight == 0) {
                log.warn("xHeight value could not be determined."
                        + " The font may not work as expected.");
            }
        }
    }

    /**
     * Read the kerning table, create a table for both CIDs and
     * winAnsiEncoding.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private final void readKerning(FontFileReader in) throws IOException {
        // Read kerning
        kerningTab = new java.util.HashMap();
        ansiKerningTab = new java.util.HashMap();
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get("kern");
        if (dirTab != null) {
            seekTab(in, "kern", 2);
            for (int n = in.readTTFUShort(); n > 0; n--) {
                in.skip(2 * 2);
                int k = in.readTTFUShort();
                if (!((k & 1) != 0) || (k & 2) != 0 || (k & 4) != 0) {
                    return;
                }
                if ((k >> 8) != 0) {
                    continue;
                }

                k = in.readTTFUShort();
                in.skip(3 * 2);
                while (k-- > 0) {
                    int i = in.readTTFUShort();
                    int j = in.readTTFUShort();
                    int kpx = in.readTTFShort();
                    if (kpx != 0) {
                        // CID table
                        Integer iObj = new Integer(i);
                        Map adjTab = (Map)kerningTab.get(iObj);
                        if (adjTab == null) {
                            adjTab = new java.util.HashMap();
                        }
                        adjTab.put(new Integer(j),
                                   new Integer((int)convertTTFUnit2PDFUnit(kpx)));
                        kerningTab.put(iObj, adjTab);
                    }
                }
            }
            // getLogger().debug(kerningTab.toString());

            // Create winAnsiEncoded kerning table
            Iterator ae = kerningTab.keySet().iterator();
            while (ae.hasNext()) {
                Integer cidKey = (Integer)ae.next();
                Map akpx = new java.util.HashMap();
                Map ckpx = (Map)kerningTab.get(cidKey);

                Iterator aee = ckpx.keySet().iterator();
                while (aee.hasNext()) {
                    Integer cidKey2 = (Integer)aee.next();
                    Integer kern = (Integer)ckpx.get(cidKey2);

                    Iterator uniMap = mtxTab[cidKey2.intValue()].getUnicodeIndex().listIterator();
                    while (uniMap.hasNext()) {
                        Integer unicodeKey = (Integer)uniMap.next();
                        Integer[] ansiKeys = unicodeToWinAnsi(unicodeKey.intValue());
                        for (int u = 0; u < ansiKeys.length; u++) {
                            akpx.put(ansiKeys[u], kern);
                        }
                    }
                }

                if (akpx.size() > 0) {
                    Iterator uniMap = mtxTab[cidKey.intValue()].getUnicodeIndex().listIterator();
                    while (uniMap.hasNext()) {
                        Integer unicodeKey = (Integer)uniMap.next();
                        Integer[] ansiKeys = unicodeToWinAnsi(unicodeKey.intValue());
                        for (int u = 0; u < ansiKeys.length; u++) {
                            ansiKerningTab.put(ansiKeys[u], akpx);
                        }
                    }
                }
            }
        }
    }

    /**
     * Return a List with TTFCmapEntry.
     * @return A list of TTFCmapEntry objects
     */
    public List getCMaps() {
        return cmaps;
    }

    /**
     * Check if this is a TrueType collection and that the given
     * name exists in the collection.
     * If it does, set offset in fontfile to the beginning of
     * the Table Directory for that font.
     * @param in FontFileReader to read from
     * @param name The name to check
     * @return True if not collection or font name present, false otherwise
     * @throws IOException In case of an I/O problem
     */
    protected final boolean checkTTC(FontFileReader in, String name) throws IOException {
        String tag = in.readTTFString(4);

        if ("ttcf".equals(tag)) {
            // This is a TrueType Collection
            in.skip(4);

            // Read directory offsets
            int numDirectories = (int)in.readTTFULong();
            // int numDirectories=in.readTTFUShort();
            long[] dirOffsets = new long[numDirectories];
            for (int i = 0; i < numDirectories; i++) {
                dirOffsets[i] = in.readTTFULong();
            }

            log.info("This is a TrueType collection file with "
                                   + numDirectories + " fonts");
            log.info("Containing the following fonts: ");
            // Read all the directories and name tables to check
            // If the font exists - this is a bit ugly, but...
            boolean found = false;

            // Iterate through all name tables even if font
            // Is found, just to show all the names
            long dirTabOffset = 0;
            for (int i = 0; (i < numDirectories); i++) {
                in.seekSet(dirOffsets[i]);
                readDirTabs(in);

                readName(in);

                if (fullName.equals(name)) {
                    found = true;
                    dirTabOffset = dirOffsets[i];
                    log.info(fullName + " <-- selected");
                } else {
                    log.info(fullName);
                }

                // Reset names
                notice = "";
                fullName = "";
                familyName = "";
                fontName = "";
                subFamilyName = "";
            }

            in.seekSet(dirTabOffset);
            return found;
        } else {
            in.seekSet(0);
            return true;
        }
    }

    /*
     * Helper classes, they are not very efficient, but that really
     * doesn't matter...
     */
    private Integer[] unicodeToWinAnsi(int unicode) {
        List ret = new java.util.ArrayList();
        for (int i = 32; i < Glyphs.WINANSI_ENCODING.length; i++) {
            if (unicode == Glyphs.WINANSI_ENCODING[i]) {
                ret.add(new Integer(i));
            }
        }
        return (Integer[])ret.toArray(new Integer[0]);
    }

    /**
     * Dumps a few informational values to System.out.
     */
    public void printStuff() {
        System.out.println("Font name:   " + fontName);
        System.out.println("Full name:   " + fullName);
        System.out.println("Family name: " + familyName);
        System.out.println("Subfamily name: " + subFamilyName);
        System.out.println("Notice:      " + notice);
        System.out.println("xHeight:     " + (int)convertTTFUnit2PDFUnit(xHeight));
        System.out.println("capheight:   " + (int)convertTTFUnit2PDFUnit(capHeight));

        int italic = (int)(italicAngle >> 16);
        System.out.println("Italic:      " + italic);
        System.out.print("ItalicAngle: " + (short)(italicAngle / 0x10000));
        if ((italicAngle % 0x10000) > 0) {
            System.out.print("."
                             + (short)((italicAngle % 0x10000) * 1000)
                               / 0x10000);
        }
        System.out.println();
        System.out.println("Ascender:    " + convertTTFUnit2PDFUnit(ascender));
        System.out.println("Descender:   " + convertTTFUnit2PDFUnit(descender));
        System.out.println("FontBBox:    [" + (int)convertTTFUnit2PDFUnit(fontBBox1)
                           + " " + (int)convertTTFUnit2PDFUnit(fontBBox2) + " "
                           + (int)convertTTFUnit2PDFUnit(fontBBox3) + " "
                           + (int)convertTTFUnit2PDFUnit(fontBBox4) + "]");
    }

    /**
     * Static main method to get info about a TrueType font.
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        try {
            TTFFile ttfFile = new TTFFile();

            FontFileReader reader = new FontFileReader(args[0]);

            String name = null;
            if (args.length >= 2) {
                name = args[1];
            }

            ttfFile.readFont(reader, name);
            ttfFile.printStuff();

        } catch (IOException ioe) {
            System.err.println("Problem reading font: " + ioe.toString());
            ioe.printStackTrace(System.err);
        }
    }

}


/**
 * Key-value helper class
 */
class UnicodeMapping {

    private int unicodeIndex;
    private int glyphIndex;

    UnicodeMapping(int glyphIndex, int unicodeIndex) {
        this.unicodeIndex = unicodeIndex;
        this.glyphIndex = glyphIndex;
    }

    /**
     * Returns the glyphIndex.
     * @return the glyph index
     */
    public int getGlyphIndex() {
        return glyphIndex;
    }

    /**
     * Returns the unicodeIndex.
     * @return the Unicode index
     */
    public int getUnicodeIndex() {
        return unicodeIndex;
    }

}
