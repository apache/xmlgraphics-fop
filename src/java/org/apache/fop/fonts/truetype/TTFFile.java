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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.fonts.Glyphs;

import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.GlyphClassTable;
import org.apache.fop.fonts.GlyphCoverageTable;
import org.apache.fop.fonts.GlyphDefinitionSubtable;
import org.apache.fop.fonts.GlyphDefinitionTable;
import org.apache.fop.fonts.GlyphMappingTable;
import org.apache.fop.fonts.GlyphPositioningSubtable;
import org.apache.fop.fonts.GlyphPositioningTable;
import org.apache.fop.fonts.GlyphSubstitutionSubtable;
import org.apache.fop.fonts.GlyphSubstitutionTable;
import org.apache.fop.fonts.GlyphSubtable;
import org.apache.fop.fonts.GlyphTable;

import org.apache.fop.util.CharUtilities;

// CSOFF: AvoidNestedBlocksCheck
// CSOFF: NoWhitespaceAfterCheck
// CSOFF: InnerAssignmentCheck
// CSOFF: SimplifyBooleanReturnCheck
// CSOFF: LineLengthCheck

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

    private final String encoding = "WinAnsiEncoding";    // Default encoding

    private final short firstChar = 0;
    private boolean isEmbeddable = true;
    private boolean hasSerifs = true;
    /**
     * Table directory
     */
    protected Map dirTabs;
    private Map<Integer, Map<Integer, Integer>> kerningTab;     // for CIDs
    private Map<Integer, Map<Integer, Integer>> ansiKerningTab; // For winAnsiEncoding
    private List cmaps;
    private Set unicodeMappings;

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
    private final int[] mtxEncoded = null;

    private String postScriptName = "";
    private String fullName = "";
    private String notice = "";
    private final Set familyNames = new java.util.HashSet(); //Set<String>
    private String subFamilyName = "";

    private long italicAngle = 0;
    private long isFixedPitch = 0;
    private int fontBBox1 = 0;
    private int fontBBox2 = 0;
    private int fontBBox3 = 0;
    private int fontBBox4 = 0;
    private int capHeight = 0;
    private int os2CapHeight = 0;
    private int underlinePosition = 0;
    private int underlineThickness = 0;
    private int xHeight = 0;
    private int os2xHeight = 0;
    //Effective ascender/descender
    private int ascender = 0;
    private int descender = 0;
    //Ascender/descender from hhea table
    private int hheaAscender = 0;
    private int hheaDescender = 0;
    //Ascender/descender from OS/2 table
    private int os2Ascender = 0;
    private int os2Descender = 0;
    private int usWeightClass = 0;

    private short lastChar = 0;

    private int[] ansiWidth;
    private Map ansiIndex;

    // internal mapping of glyph indexes to unicode indexes
    // used for quick mappings in this class
    private final Map glyphToUnicodeMap = new java.util.HashMap();
    private final Map unicodeToGlyphMap = new java.util.HashMap();

    private TTFDirTabEntry currentDirTab;

    private boolean isCFF;

    /* advanced typographic support */
    private Map/*<String,Object[3]>*/ seScripts;
    private Map/*<String,Object[2]>*/ seLanguages;
    private Map/*<String,List<String>>*/ seFeatures;
    private GlyphMappingTable seMapping;
    private List seEntries;
    private List seSubtables;
    private GlyphDefinitionTable gdef;
    private GlyphSubstitutionTable gsub;
    private GlyphPositioningTable gpos;

    /**
     * logging instance
     */
    protected Log log = LogFactory.getLog(TTFFile.class);

    /**
     * Key-value helper class
     */
    class UnicodeMapping implements Comparable {

        private final int unicodeIndex;
        private final int glyphIndex;

        UnicodeMapping(int glyphIndex, int unicodeIndex) {
            this.unicodeIndex = unicodeIndex;
            this.glyphIndex = glyphIndex;
            glyphToUnicodeMap.put(new Integer(glyphIndex), new Integer(unicodeIndex));
            unicodeToGlyphMap.put(new Integer(unicodeIndex), new Integer(glyphIndex));
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


        /** {@inheritDoc} */
        public int hashCode() {
            int hc = unicodeIndex;
            hc = 19 * hc + ( hc ^ glyphIndex );
            return hc;
        }

        /** {@inheritDoc} */
        public boolean equals ( Object o ) {
            if ( o instanceof UnicodeMapping ) {
                UnicodeMapping m = (UnicodeMapping) o;
                if ( unicodeIndex != m.unicodeIndex ) {
                    return false;
                } else if ( glyphIndex != m.glyphIndex ) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        /** {@inheritDoc} */
        public int compareTo ( Object o ) {
            if ( o instanceof UnicodeMapping ) {
                UnicodeMapping m = (UnicodeMapping) o;
                if ( unicodeIndex > m.unicodeIndex ) {
                    return 1;
                } else if ( unicodeIndex < m.unicodeIndex ) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        }
    }

    /**
     * Position inputstream to position indicated
     * in the dirtab offset + offset
     */
    boolean seekTab(FontFileReader in, String name,
                  long offset) throws IOException {
        TTFDirTabEntry dt = (TTFDirTabEntry)dirTabs.get(name);
        if (dt == null) {
            log.error("Dirtab " + name + " not found.");
            return false;
        } else {
            in.seekSet(dt.getOffset() + offset);
            this.currentDirTab = dt;
        }
        return true;
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

        unicodeMappings = new java.util.TreeSet();

        seekTab(in, "cmap", 2);
        int numCMap = in.readTTFUShort();    // Number of cmap subtables
        long cmapUniOffset = 0;
        long symbolMapOffset = 0;

        if (log.isDebugEnabled()) {
            log.debug(numCMap + " cmap tables");
        }

        //Read offset for all tables. We are only interested in the unicode table
        for (int i = 0; i < numCMap; i++) {
            int cmapPID = in.readTTFUShort();
            int cmapEID = in.readTTFUShort();
            long cmapOffset = in.readTTFULong();

            if (log.isDebugEnabled()) {
                log.debug("Platform ID: " + cmapPID + " Encoding: " + cmapEID);
            }

            if (cmapPID == 3 && cmapEID == 1) {
                cmapUniOffset = cmapOffset;
            }
            if (cmapPID == 3 && cmapEID == 0) {
                symbolMapOffset = cmapOffset;
            }
        }

        if (cmapUniOffset > 0) {
            return readUnicodeCmap(in, cmapUniOffset, 1);
        } else if (symbolMapOffset > 0) {
            return readUnicodeCmap(in, symbolMapOffset, 0);
        } else {
            log.fatal("Unsupported TrueType font: No Unicode or Symbol cmap table"
                    + " not present. Aborting");
            return false;
        }
    }

    private boolean readUnicodeCmap                             // CSOK: MethodLength
        (FontFileReader in, long cmapUniOffset, int encodingID)
            throws IOException {
        //Read CMAP table and correct mtxTab.index
        int mtxPtr = 0;

        // Read unicode cmap
        seekTab(in, "cmap", cmapUniOffset);
        int cmapFormat = in.readTTFUShort();
        /*int cmap_length =*/ in.readTTFUShort(); //skip cmap length

        if (log.isDebugEnabled()) {
            log.debug("CMAP format: " + cmapFormat);
        }

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

            BitSet eightBitGlyphs = new BitSet(256);

            // Insert the unicode id for the glyphs in mtxTab
            // and fill in the cmaps ArrayList

            for (int i = 0; i < cmapStartCounts.length; i++) {

                if (log.isTraceEnabled()) {
                    log.trace(i + ": " + cmapStartCounts[i]
                                                         + " - " + cmapEndCounts[i]);
                }
                if (log.isDebugEnabled()) {
                    if (isInPrivateUseArea(cmapStartCounts[i], cmapEndCounts[i])) {
                        log.debug("Font contains glyphs in the Unicode private use area: "
                                + Integer.toHexString(cmapStartCounts[i]) + " - "
                                + Integer.toHexString(cmapEndCounts[i]));
                    }
                }

                for (int j = cmapStartCounts[i]; j <= cmapEndCounts[i]; j++) {

                    // Update lastChar
                    if (j < 256 && j > lastChar) {
                        lastChar = (short)j;
                    }

                    if (j < 256) {
                        eightBitGlyphs.set(j);
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

                            unicodeMappings.add(new UnicodeMapping(glyphIdx, j));
                            mtxTab[glyphIdx].getUnicodeIndex().add(new Integer(j));

                            if (encodingID == 0 && j >= 0xF020 && j <= 0xF0FF) {
                                //Experimental: Mapping 0xF020-0xF0FF to 0x0020-0x00FF
                                //Tested with Wingdings and Symbol TTF fonts which map their
                                //glyphs in the region 0xF020-0xF0FF.
                                int mapped = j - 0xF000;
                                if (!eightBitGlyphs.get(mapped)) {
                                    //Only map if Unicode code point hasn't been mapped before
                                    unicodeMappings.add(new UnicodeMapping(glyphIdx, mapped));
                                    mtxTab[glyphIdx].getUnicodeIndex().add(new Integer(mapped));
                                }
                            }

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

                            unicodeMappings.add(new UnicodeMapping(glyphIdx, j));
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
        } else {
            log.error("Cmap format not supported: " + cmapFormat);
            return false;
        }
        return true;
    }

    private boolean isInPrivateUseArea(int start, int end) {
        return (isInPrivateUseArea(start) || isInPrivateUseArea(end));
    }

    private boolean isInPrivateUseArea(int unicode) {
        return (unicode >= 0xE000 && unicode <= 0xF8FF);
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
            Integer uni = new Integer(Glyphs.WINANSI_ENCODING[i]);

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
        if (log.isDebugEnabled()) {
            log.debug("Number of glyphs in font: " + numberOfGlyphs);
        }
        readHorizontalHeader(in);
        readHorizontalMetrics(in);
        initAnsiWidths();
        readPostScript(in);
        readOS2(in);
        determineAscDesc();
        if (!isCFF) {
            readIndexToLocation(in);
            readGlyf(in);
        }
        readName(in);
        boolean pcltFound = readPCLT(in);
        // Read cmap table and fill in ansiwidths
        boolean valid = readCMAP(in);
        if (!valid) {
            return false;
        }
        // Create cmaps for bfentries
        augmentCMaps();
        createCMaps();

        readKerning(in);
        readGDEF(in);
        readGSUB(in);
        readGPOS(in);
        guessVerticalMetricsFromGlyphBBox();
        return true;
    }

    /**
     * Augment the previously ingested CMAP data with new entries to ensure
     * that every glyph index has a corresponding Unicode value. This is required
     * by GSUB/GPOS processing which can emit glyph indices that are not in the
     * normal CMAP (and, for which, on other platforms, the glyph indices are used
     * directly for rendering purposes (rather than character codes). However, in
     * the case of FOP IF representation, character codes are used, and, consequently
     * every glyph needs some character value. Here, we assign them to the Unicode
     * private use range, starting at 0xE000 up to 0xF8FF. If there are existing
     * assignments in this range, we just skip over them. Note that it is possible
     * to exhaust this range of 6400 code values in the case a font has an
     * extraordinary number of unmapped glyphs. In that case, we do not make
     * any further assignments, but print a warning message.
     */
    private void augmentCMaps() {
        int numMapped = 0;
        int numUnmapped = 0;
        int nextPrivateUse = 0xE000;
        int firstPrivate = 0;
        int lastPrivate = 0;
        int firstUnmapped = 0;
        int lastUnmapped = 0;
        for ( int i = 0, n = numberOfGlyphs; i < n; i++ ) {
            Integer uc = glyphToUnicode ( i );
            if ( uc == null ) {
                while ( ( nextPrivateUse < 0xF900 ) && ( unicodeToGlyphMap.get(new Integer(nextPrivateUse)) != null ) ) {
                    nextPrivateUse++;
                }
                if ( nextPrivateUse < 0xF900 ) {
                    int pu = nextPrivateUse;
                    unicodeMappings.add ( new UnicodeMapping ( i, pu ) );
                    if ( firstPrivate == 0 ) {
                        firstPrivate = pu;
                    }
                    lastPrivate = pu;
                    numMapped++;
                } else {
                    if ( firstUnmapped == 0 ) {
                        firstUnmapped = i;
                    }
                    lastUnmapped = i;
                    numUnmapped++;
                }
            }
        }
        if ( numMapped > 0 ) {
            if (log.isDebugEnabled()) {
                log.debug ( "augment CMAP for "
                            + numMapped
                            + " glyphs, mapped to private use characters in the range ["
                            + CharUtilities.format ( firstPrivate ) + ","
                            + CharUtilities.format ( lastPrivate ) + "] (inclusive)" );
            }
        }
        if ( numUnmapped > 0 ) {
            log.warn ( "Exhausted private use area: unable to map "
                       + numUnmapped + " glyphs in glyph index range ["
                       + firstUnmapped + "," + lastUnmapped + "] (inclusive) of font '" + getFullName() + "'" );
        }
    }

    private void createCMaps() {
        cmaps = new java.util.ArrayList();
        TTFCmapEntry tce = new TTFCmapEntry();

        Iterator e = unicodeMappings.iterator();
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
     * Returns the PostScript name of the font.
     * @return String The PostScript name
     */
    public String getPostScriptName() {
        if (postScriptName.length() == 0) {
            return FontUtil.stripWhiteSpace(getFullName());
        } else {
            return postScriptName;
        }
    }

    /**
     * Returns the font family names of the font.
     * @return Set The family names (a Set of Strings)
     */
    public Set getFamilyNames() {
        return familyNames;
    }

    /**
     * Returns the font sub family name of the font.
     * @return String The sub family name
     */
    public String getSubFamilyName() {
        return subFamilyName;
    }

    /**
     * Returns the full name of the font.
     * @return String The full name
     */
    public String getFullName() {
        return fullName;
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
        return convertTTFUnit2PDFUnit(capHeight);
    }

    /**
     * Returns the XHeight attribute of the font.
     * @return int The XHeight
     */
    public int getXHeight() {
        return convertTTFUnit2PDFUnit(xHeight);
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
     * Returns the weight class of this font. Valid values are 100, 200....,800, 900.
     * @return the weight class value (or 0 if there was no OS/2 table in the font)
     */
    public int getWeightClass() {
        return this.usWeightClass;
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
        fbb[0] = convertTTFUnit2PDFUnit(fontBBox1);
        fbb[1] = convertTTFUnit2PDFUnit(fontBBox2);
        fbb[2] = convertTTFUnit2PDFUnit(fontBBox3);
        fbb[3] = convertTTFUnit2PDFUnit(fontBBox4);

        return fbb;
    }

    /**
     * Returns the LowerCaseAscent attribute of the font.
     * @return int The LowerCaseAscent
     */
    public int getLowerCaseAscent() {
        return convertTTFUnit2PDFUnit(ascender);
    }

    /**
     * Returns the LowerCaseDescent attribute of the font.
     * @return int The LowerCaseDescent
     */
    public int getLowerCaseDescent() {
        return convertTTFUnit2PDFUnit(descender);
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
            wx[i] = convertTTFUnit2PDFUnit(mtxTab[i].getWx());
        }

        return wx;
    }

    /**
     * Returns the width of a given character.
     * @param idx Index of the character
     * @return int Standard width
     */
    public int getCharWidth(int idx) {
        return convertTTFUnit2PDFUnit(ansiWidth[idx]);
    }

    /**
     * Returns the kerning table.
     * @return Map The kerning table
     */
    public Map<Integer, Map<Integer, Integer>> getKerning() {
        return kerningTab;
    }

    /**
     * Returns the ANSI kerning table.
     * @return Map The ANSI kerning table
     */
    public Map<Integer, Map<Integer, Integer>> getAnsiKerning() {
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
     * Indicates whether or not the font is an OpenType
     * CFF font (rather than a TrueType font).
     * @return true if the font is in OpenType CFF format.
     */
    public boolean isCFF() {
       return this.isCFF;
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
        int sfntVersion = in.readTTFLong(); // TTF_FIXED_SIZE (4 bytes)
        switch (sfntVersion) {
        case 0x10000:
            log.debug("sfnt version: OpenType 1.0");
            break;
        case 0x4F54544F: //"OTTO"
            this.isCFF = true;
            log.debug("sfnt version: OpenType with CFF data");
            break;
        case 0x74727565: //"true"
            log.debug("sfnt version: Apple TrueType");
            break;
        case 0x74797031: //"typ1"
            log.debug("sfnt version: Apple Type 1 housed in sfnt wrapper");
            break;
        default:
            log.debug("Unknown sfnt version: " + Integer.toHexString(sfntVersion));
            break;
        }
        int ntabs = in.readTTFUShort();
        in.skip(6);    // 3xTTF_USHORT_SIZE

        dirTabs = new java.util.HashMap();
        TTFDirTabEntry[] pd = new TTFDirTabEntry[ntabs];
        log.debug("Reading " + ntabs + " dir tables");
        for (int i = 0; i < ntabs; i++) {
            pd[i] = new TTFDirTabEntry();
            dirTabs.put(pd[i].read(in), pd[i]);
        }
        log.debug("dir tables: " + dirTabs.keySet());
    }

    /**
     * Read the "head" table, this reads the bounding box and
     * sets the upem (unitsPerEM) variable
     * @param in FontFileReader to read the header from
     * @throws IOException in case of an I/O problem
     */
    protected void readFontHeader(FontFileReader in) throws IOException {
        seekTab(in, "head", 2 * 4 + 2 * 4);
        int flags = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug("flags: " + flags + " - " + Integer.toString(flags, 2));
        }
        upem = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug("unit per em: " + upem);
        }

        in.skip(16);

        fontBBox1 = in.readTTFShort();
        fontBBox2 = in.readTTFShort();
        fontBBox3 = in.readTTFShort();
        fontBBox4 = in.readTTFShort();
        if (log.isDebugEnabled()) {
            log.debug("font bbox: xMin=" + fontBBox1
                    + " yMin=" + fontBBox2
                    + " xMax=" + fontBBox3
                    + " yMax=" + fontBBox4);
        }

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
        hheaDescender = in.readTTFShort();

        in.skip(2 + 2 + 3 * 2 + 8 * 2);
        nhmtx = in.readTTFUShort();

        if (log.isDebugEnabled()) {
            log.debug("hhea.Ascender: " + formatUnitsForDebug(hheaAscender));
            log.debug("hhea.Descender: " + formatUnitsForDebug(hheaDescender));
            log.debug("Number of horizontal metrics: " + nhmtx);
        }
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

        if (log.isTraceEnabled()) {
            log.trace("*** Widths array: \n");
        }
        for (int i = 0; i < mtxSize; i++) {
            mtxTab[i] = new TTFMtxEntry();
        }
        for (int i = 0; i < nhmtx; i++) {
            mtxTab[i].setWx(in.readTTFUShort());
            mtxTab[i].setLsb(in.readTTFUShort());

            if (log.isTraceEnabled()) {
                log.trace("   width[" + i + "] = "
                          + convertTTFUnit2PDFUnit(mtxTab[i].getWx()) + ";");
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
    private void readPostScript(FontFileReader in) throws IOException {
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
    private void readOS2(FontFileReader in) throws IOException {
        // Check if font is embeddable
        TTFDirTabEntry os2Entry = (TTFDirTabEntry)dirTabs.get("OS/2");
        if (os2Entry != null) {
            seekTab(in, "OS/2", 0);
            int version = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug("OS/2 table: version=" + version
                        + ", offset=" + os2Entry.getOffset() + ", len=" + os2Entry.getLength());
            }
            in.skip(2); //xAvgCharWidth
            this.usWeightClass = in.readTTFUShort();

            // usWidthClass
            in.skip(2);

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
            os2Descender = in.readTTFShort(); //sTypoDescender
            if (log.isDebugEnabled()) {
                log.debug("sTypoAscender: " + os2Ascender
                        + " -> internal " + convertTTFUnit2PDFUnit(os2Ascender));
                log.debug("sTypoDescender: " + os2Descender
                        + " -> internal " + convertTTFUnit2PDFUnit(os2Descender));
            }
            v = in.readTTFShort(); //sTypoLineGap
            if (log.isDebugEnabled()) {
                log.debug("sTypoLineGap: " + v);
            }
            v = in.readTTFUShort(); //usWinAscent
            if (log.isDebugEnabled()) {
                log.debug("usWinAscent: " + formatUnitsForDebug(v));
            }
            v = in.readTTFUShort(); //usWinDescent
            if (log.isDebugEnabled()) {
                log.debug("usWinDescent: " + formatUnitsForDebug(v));
            }

            //version 1 OS/2 table might end here
            if (os2Entry.getLength() >= 78 + (2 * 4) + (2 * 2)) {
                in.skip(2 * 4);
                this.os2xHeight = in.readTTFShort(); //sxHeight
                this.os2CapHeight = in.readTTFShort(); //sCapHeight
                if (log.isDebugEnabled()) {
                    log.debug("sxHeight: " + this.os2xHeight);
                    log.debug("sCapHeight: " + this.os2CapHeight);
                }
            }

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
        if (!seekTab(in, "loca", 0)) {
            throw new IOException("'loca' table not found, happens when the font file doesn't"
                    + " contain TrueType outlines (trying to read an OpenType CFF font maybe?)");
        }
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
    private void readGlyf(FontFileReader in) throws IOException {
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get("glyf");
        if (dirTab == null) {
            throw new IOException("glyf table not found, cannot continue");
        }
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
    private void readName(FontFileReader in) throws IOException {
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
                    && (encodingID == 0 || encodingID == 1))) {
                in.seekSet(j + in.readTTFUShort());
                String txt;
                if (platformID == 3) {
                    txt = in.readTTFString(l, encodingID);
                } else {
                    txt = in.readTTFString(l);
                }

                if (log.isDebugEnabled()) {
                    log.debug(platformID + " "
                            + encodingID + " "
                            + languageID + " "
                            + k + " " + txt);
                }
                switch (k) {
                case 0:
                    if (notice.length() == 0) {
                        notice = txt;
                    }
                    break;
                case 1: //Font Family Name
                case 16: //Preferred Family
                    familyNames.add(txt);
                    break;
                case 2:
                    if (subFamilyName.length() == 0) {
                        subFamilyName = txt;
                    }
                    break;
                case 4:
                    if (fullName.length() == 0 || (platformID == 3 && languageID == 1033)) {
                        fullName = txt;
                    }
                    break;
                case 6:
                    if (postScriptName.length() == 0) {
                        postScriptName = txt;
                    }
                    break;
                default:
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
    private boolean readPCLT(FontFileReader in) throws IOException {
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get("PCLT");
        if (dirTab != null) {
            in.seekSet(dirTab.getOffset() + 4 + 4 + 2);
            xHeight = in.readTTFUShort();
            log.debug("xHeight from PCLT: " + formatUnitsForDebug(xHeight));
            in.skip(2 * 2);
            capHeight = in.readTTFUShort();
            log.debug("capHeight from PCLT: " + formatUnitsForDebug(capHeight));
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

        if (log.isDebugEnabled()) {
            log.debug("Font box height: " + (ascender - descender));
            if (ascender - descender > upem) {
                log.debug("Ascender and descender together are larger than the em box.");
            }
        }
    }

    private void guessVerticalMetricsFromGlyphBBox() {
        // Approximate capHeight from height of "H"
        // It's most unlikely that a font misses the PCLT table
        // This also assumes that postscriptnames exists ("H")
        // Should look it up in the cmap (that wouldn't help
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
        if (log.isDebugEnabled()) {
            log.debug("Ascender from glyph 'd': " + formatUnitsForDebug(localAscender));
            log.debug("Descender from glyph 'p': " + formatUnitsForDebug(localDescender));
        }
        if (ascender - descender > upem) {
            log.debug("Replacing specified ascender/descender with derived values to get values"
                    + " which fit in the em box.");
            ascender = localAscender;
            descender = localDescender;
        }

        if (log.isDebugEnabled()) {
            log.debug("xHeight from glyph 'x': " + formatUnitsForDebug(localXHeight));
            log.debug("CapHeight from glyph 'H': " + formatUnitsForDebug(localCapHeight));
        }
        if (capHeight == 0) {
            capHeight = localCapHeight;
            if (capHeight == 0) {
                capHeight = os2CapHeight;
            }
            if (capHeight == 0) {
                log.warn("capHeight value could not be determined."
                        + " The font may not work as expected.");
            }
        }
        if (xHeight == 0) {
            xHeight = localXHeight;
            if (xHeight == 0) {
                xHeight = os2xHeight;
            }
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
    private void readKerning(FontFileReader in) throws IOException {
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
                        // CID kerning table entry, using unicode indexes
                        final Integer iObj = glyphToUnicode(i);
                        final Integer u2 = glyphToUnicode(j);
                        if (iObj == null) {
                            // happens for many fonts (Ubuntu font set),
                            // stray entries in the kerning table??
                            log.debug("Ignoring kerning pair because no Unicode index was"
                                    + " found for the first glyph " + i);
                        } else if (u2 == null) {
                            log.debug("Ignoring kerning pair because Unicode index was"
                                    + " found for the second glyph " + i);
                        } else {
                            Map adjTab = kerningTab.get(iObj);
                            if (adjTab == null) {
                                adjTab = new java.util.HashMap();
                            }
                            adjTab.put(u2, new Integer(convertTTFUnit2PDFUnit(kpx)));
                            kerningTab.put(iObj, adjTab);
                        }
                    }
                }
            }

            // Create winAnsiEncoded kerning table from kerningTab
            // (could probably be simplified, for now we remap back to CID indexes and
            // then to winAnsi)
            Iterator ae = kerningTab.keySet().iterator();
            while (ae.hasNext()) {
                Integer unicodeKey1 = (Integer)ae.next();
                Integer cidKey1 = unicodeToGlyph(unicodeKey1.intValue());
                Map<Integer, Integer> akpx = new java.util.HashMap();
                Map ckpx = kerningTab.get(unicodeKey1);

                Iterator aee = ckpx.keySet().iterator();
                while (aee.hasNext()) {
                    Integer unicodeKey2 = (Integer)aee.next();
                    Integer cidKey2 = unicodeToGlyph(unicodeKey2.intValue());
                    Integer kern = (Integer)ckpx.get(unicodeKey2);

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
                    Iterator uniMap = mtxTab[cidKey1.intValue()].getUnicodeIndex().listIterator();
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

    /** helper method for formatting an integer array for output */
    private String toString ( int[] ia ) {
        StringBuffer sb = new StringBuffer();
        if ( ( ia == null ) || ( ia.length == 0 ) ) {
            sb.append ( '-' );
        } else {
            boolean first = true;
            for ( int i = 0; i < ia.length; i++ ) {
                if ( ! first ) {
                    sb.append ( ' ' );
                } else {
                    first = false;
                }
                sb.append ( ia[i] );
            }
        }
        return sb.toString();
    }

    private void readLangSysTable(FontFileReader in, String tableTag, long langSysTable, String langSysTag) throws IOException {
        in.seekSet(langSysTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lang sys table: " + langSysTag );
        }
        // read lookup order (reorder) table offset
        int lo = in.readTTFUShort();
        // read required feature index
        int rf = in.readTTFUShort();
        String rfi;
        if ( rf != 65535 ) {
            rfi = "f" + rf;
        } else {
            rfi = null;
        }
        // read (non-required) feature count
        int nf = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lang sys table reorder table: " + lo );
            log.debug(tableTag + " lang sys table required feature index: " + rf );
            log.debug(tableTag + " lang sys table non-required feature count: " + nf );
        }
        // read (non-required) feature indices
        int[] fia = new int[nf];
        List fl = new java.util.ArrayList();
        for ( int i = 0; i < nf; i++ ) {
            int fi = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lang sys table non-required feature index: " + fi );
            }
            fia[i] = fi;
            fl.add ( "f" + fi );
        }
        if ( seLanguages == null ) {
            seLanguages = new java.util.LinkedHashMap();
        }
        seLanguages.put ( langSysTag, new Object[] { rfi, fl } );
    }

    private static String defaultTag = "dflt";

    private void readScriptTable(FontFileReader in, String tableTag, long scriptTable, String scriptTag) throws IOException {
        in.seekSet(scriptTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script table: " + scriptTag );
        }
        // read default language system table offset
        int dl = in.readTTFUShort();
        String dt = defaultTag;
        if ( dl > 0 ) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " default lang sys tag: " + dt );
                log.debug(tableTag + " default lang sys table offset: " + dl );
            }
        }
        // read language system record count
        int nl = in.readTTFUShort();
        List ll = new java.util.ArrayList();
        if ( nl > 0 ) {
            String[] lta = new String[nl];
            int[] loa = new int[nl];
            // read language system records
            for ( int i = 0, n = nl; i < n; i++ ) {
                String lt = in.readTTFString(4);
                int lo = in.readTTFUShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lang sys tag: " + lt );
                    log.debug(tableTag + " lang sys table offset: " + lo );
                }
                lta[i] = lt;
                loa[i] = lo;
                if ( dl == lo ) {
                    dl = 0;
                    dt = lt;
                }
                ll.add ( lt );
            }
            // read non-default language system tables
            for ( int i = 0, n = nl; i < n; i++ ) {
                readLangSysTable ( in, tableTag, scriptTable + loa [ i ], lta [ i ] );
            }
        }
        // read default language system table (if specified)
        if ( dl > 0 ) {
            readLangSysTable ( in, tableTag, scriptTable + dl, dt );
        } else if ( dt != null ) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lang sys default: " + dt );
            }
        }
        seScripts.put ( scriptTag, new Object[] { dt, ll, seLanguages } );
        seLanguages = null;
    }

    private void readScriptList(FontFileReader in, String tableTag, long scriptList) throws IOException {
        in.seekSet(scriptList);
        // read script record count
        int ns = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " script list record count: " + ns );
        }
        if ( ns > 0 ) {
            String[] sta = new String[ns];
            int[] soa = new int[ns];
            // read script records
            for ( int i = 0, n = ns; i < n; i++ ) {
                String st = in.readTTFString(4);
                int so = in.readTTFUShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " script tag: " + st );
                    log.debug(tableTag + " script table offset: " + so );
                }
                sta[i] = st;
                soa[i] = so;
            }
            // read script tables
            for ( int i = 0, n = ns; i < n; i++ ) {
                seLanguages = null;
                readScriptTable ( in, tableTag, scriptList + soa [ i ], sta [ i ] );
            }
        }
    }

    private void readFeatureTable(FontFileReader in, String tableTag, long featureTable, String featureTag, int featureIndex) throws IOException {
        in.seekSet(featureTable);
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature table: " + featureTag );
        }
        // read feature params offset
        int po = in.readTTFUShort();
        // read lookup list indices count
        int nl = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature table parameters offset: " + po );
            log.debug(tableTag + " feature table lookup list index count: " + nl );
        }
        // read lookup table indices
        int[] lia = new int[nl];
        List lul = new java.util.ArrayList();
        for ( int i = 0; i < nl; i++ ) {
            int li = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " feature table lookup index: " + li );
            }
            lia[i] = li;
            lul.add ( "lu" + li );
        }
        seFeatures.put ( "f" + featureIndex, new Object[] { featureTag, lul } );
    }

    private void readFeatureList(FontFileReader in, String tableTag, long featureList) throws IOException {
        in.seekSet(featureList);
        // read feature record count
        int nf = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " feature list record count: " + nf );
        }
        if ( nf > 0 ) {
            String[] fta = new String[nf];
            int[] foa = new int[nf];
            // read feature records
            for ( int i = 0, n = nf; i < n; i++ ) {
                String ft = in.readTTFString(4);
                int fo = in.readTTFUShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " feature tag: " + ft );
                    log.debug(tableTag + " feature table offset: " + fo );
                }
                fta[i] = ft;
                foa[i] = fo;
            }
            // read feature tables
            for ( int i = 0, n = nf; i < n; i++ ) {
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " feature index: " + i );
                }
                readFeatureTable ( in, tableTag, featureList + foa [ i ], fta [ i ], i );
            }
        }
    }

    /**
     * Determine if script extension is present.
     * @return true if script extension is present
     */
    public boolean hasScriptExtension() {
        return ( gsub != null ) || ( gpos != null );
    }

    /**
     * Returns the GDEF table or null if none present.
     * @return the GDEF table
     */
    public GlyphDefinitionTable getGDEF() {
        return gdef;
    }

    /**
     * Returns the GSUB table or null if none present.
     * @return the GSUB table
     */
    public GlyphSubstitutionTable getGSUB() {
        return gsub;
    }

    /**
     * Returns the GPOS table or null if none present.
     * @return the GPOS table
     */
    public GlyphPositioningTable getGPOS() {
        return gpos;
    }

    static final class GDEFLookupType {
        static final int GLYPH_CLASS                    = 1;
        static final int ATTACHMENT_POINT               = 2;
        static final int LIGATURE_CARET                 = 3;
        static final int MARK_ATTACHMENT                = 4;
        private GDEFLookupType() {
        }
        public static int getSubtableType ( int lt ) {
            int st;
            switch ( lt ) {
            case GDEFLookupType.GLYPH_CLASS:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_GLYPH_CLASS;
                break;
            case GDEFLookupType.ATTACHMENT_POINT:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_ATTACHMENT_POINT;
                break;
            case GDEFLookupType.LIGATURE_CARET:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_LIGATURE_CARET;
                break;
            case GDEFLookupType.MARK_ATTACHMENT:
                st = GlyphDefinitionTable.GDEF_LOOKUP_TYPE_MARK_ATTACHMENT;
                break;
            default:
                st = -1;
                break;
            }
            return st;
        }
        public static String toString(int type) {
            String s;
            switch ( type ) {
            case GLYPH_CLASS:
                s = "GlyphClass";
                break;
            case ATTACHMENT_POINT:
                s = "AttachmentPoint";
                break;
            case LIGATURE_CARET:
                s = "LigatureCaret";
                break;
            case MARK_ATTACHMENT:
                s = "MarkAttachment";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class GSUBLookupType {
        static final int SINGLE                         = 1;
        static final int MULTIPLE                       = 2;
        static final int ALTERNATE                      = 3;
        static final int LIGATURE                       = 4;
        static final int CONTEXTUAL                     = 5;
        static final int CHAINED_CONTEXTUAL             = 6;
        static final int EXTENSION                      = 7;
        static final int REVERSE_CHAINED_SINGLE         = 8;
        private GSUBLookupType() {
        }
        public static int getSubtableType ( int lt ) {
            int st;
            switch ( lt ) {
            case GSUBLookupType.SINGLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_SINGLE;
                break;
            case GSUBLookupType.MULTIPLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_MULTIPLE;
                break;
            case GSUBLookupType.ALTERNATE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_ALTERNATE;
                break;
            case GSUBLookupType.LIGATURE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_LIGATURE;
                break;
            case GSUBLookupType.CONTEXTUAL:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CONTEXTUAL;
                break;
            case GSUBLookupType.CHAINED_CONTEXTUAL:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CHAINED_CONTEXTUAL;
                break;
            case GSUBLookupType.EXTENSION:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION;
                break;
            case GSUBLookupType.REVERSE_CHAINED_SINGLE:
                st = GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_REVERSE_CHAINED_SINGLE;
                break;
            default:
                st = -1;
                break;
            }
            return st;
        }
        public static String toString(int type) {
            String s;
            switch ( type ) {
            case SINGLE:
                s = "Single";
                break;
            case MULTIPLE:
                s = "Multiple";
                break;
            case ALTERNATE:
                s = "Alternate";
                break;
            case LIGATURE:
                s = "Ligature";
                break;
            case CONTEXTUAL:
                s = "Contextual";
                break;
            case CHAINED_CONTEXTUAL:
                s = "ChainedContextual";
                break;
            case EXTENSION:
                s = "Extension";
                break;
            case REVERSE_CHAINED_SINGLE:
                s = "ReverseChainedSingle";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class GPOSLookupType {
        static final int SINGLE                         = 1;
        static final int PAIR                           = 2;
        static final int CURSIVE                        = 3;
        static final int MARK_TO_BASE                   = 4;
        static final int MARK_TO_LIGATURE               = 5;
        static final int MARK_TO_MARK                   = 6;
        static final int CONTEXTUAL                     = 7;
        static final int CHAINED_CONTEXTUAL             = 8;
        static final int EXTENSION                      = 9;
        private GPOSLookupType() {
        }
        public static String toString(int type) {
            String s;
            switch ( type ) {
            case SINGLE:
                s = "Single";
                break;
            case PAIR:
                s = "Pair";
                break;
            case CURSIVE:
                s = "Cursive";
                break;
            case MARK_TO_BASE:
                s = "MarkToBase";
                break;
            case MARK_TO_LIGATURE:
                s = "MarkToLigature";
                break;
            case MARK_TO_MARK:
                s = "MarkToMark";
                break;
            case CONTEXTUAL:
                s = "Contextual";
                break;
            case CHAINED_CONTEXTUAL:
                s = "ChainedContextual";
                break;
            case EXTENSION:
                s = "Extension";
                break;
            default:
                s = "?";
                break;
            }
            return s;
        }
    }

    static final class LookupFlag {
        static final int RIGHT_TO_LEFT                  = 0x0001;
        static final int IGNORE_BASE_GLYPHS             = 0x0002;
        static final int IGNORE_LIGATURE                = 0x0004;
        static final int IGNORE_MARKS                   = 0x0008;
        static final int USE_MARK_FILTERING_SET         = 0x0010;
        static final int MARK_ATTACHMENT_TYPE           = 0xFF00;
        private LookupFlag() {
        }
        public static String toString(int flags) {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            if ( ( flags & RIGHT_TO_LEFT ) != 0 ) {
                if ( first ) {
                    first = false;
                } else {
                    sb.append ( '|' );
                }
                sb.append ( "RightToLeft" );
            }
            if ( ( flags & IGNORE_BASE_GLYPHS ) != 0 ) {
                if ( first ) {
                    first = false;
                } else {
                    sb.append ( '|' );
                }
                sb.append ( "IgnoreBaseGlyphs" );
            }
            if ( ( flags & IGNORE_LIGATURE ) != 0 ) {
                if ( first ) {
                    first = false;
                } else {
                    sb.append ( '|' );
                }
                sb.append ( "IgnoreLigature" );
            }
            if ( ( flags & IGNORE_MARKS ) != 0 ) {
                if ( first ) {
                    first = false;
                } else {
                    sb.append ( '|' );
                }
                sb.append ( "IgnoreMarks" );
            }
            if ( ( flags & USE_MARK_FILTERING_SET ) != 0 ) {
                if ( first ) {
                    first = false;
                } else {
                    sb.append ( '|' );
                }
                sb.append ( "UseMarkFilteringSet" );
            }
            if ( sb.length() == 0 ) {
                sb.append ( '-' );
            }
            return sb.toString();
        }
    }

    private GlyphCoverageTable readCoverageTableFormat1(FontFileReader in, String label, long tableOffset, int coverageFormat) throws IOException {
        List entries = new java.util.ArrayList();
        in.seekSet(tableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read glyph count
        int ng = in.readTTFUShort();
        int[] ga = new int[ng];
        for ( int i = 0, n = ng; i < n; i++ ) {
            int g = in.readTTFUShort();
            ga[i] = g;
            entries.add ( Integer.valueOf(g) );
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(label + " glyphs: " + toString(ga) );
        }
        return GlyphCoverageTable.createCoverageTable ( entries );
    }

    private GlyphCoverageTable readCoverageTableFormat2(FontFileReader in, String label, long tableOffset, int coverageFormat) throws IOException {
        List entries = new java.util.ArrayList();
        in.seekSet(tableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read range record count
        int nr = in.readTTFUShort();
        for ( int i = 0, n = nr; i < n; i++ ) {
            // read range start
            int s = in.readTTFUShort();
            // read range end
            int e = in.readTTFUShort();
            // read range coverage (mapping) index
            int m = in.readTTFUShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m );
            }
            entries.add ( new GlyphCoverageTable.MappingRange ( s, e, m ) );
        }
        return GlyphCoverageTable.createCoverageTable ( entries );
    }

    private GlyphCoverageTable readCoverageTable(FontFileReader in, String label, long tableOffset) throws IOException {
        GlyphCoverageTable gct;
        long cp = in.getCurrentPos();
        in.seekSet(tableOffset);
        // read coverage table format
        int cf = in.readTTFUShort();
        if ( cf == 1 ) {
            gct = readCoverageTableFormat1 ( in, label, tableOffset, cf );
        } else if ( cf == 2 ) {
            gct = readCoverageTableFormat2 ( in, label, tableOffset, cf );
        } else {
            throw new UnsupportedOperationException ( "unsupported coverage table format: " + cf );
        }
        in.seekSet ( cp );
        return gct;
    }

    private GlyphClassTable readClassDefTableFormat1(FontFileReader in, String label, long tableOffset, int classFormat) throws IOException {
        List entries = new java.util.ArrayList();
        in.seekSet(tableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read start glyph
        int sg = in.readTTFUShort();
        entries.add ( Integer.valueOf(sg) );
        // read glyph count
        int ng = in.readTTFUShort();
        // read glyph classes
        int[] ca = new int[ng];
        for ( int i = 0, n = ng; i < n; i++ ) {
            int gc = in.readTTFUShort();
            ca[i] = gc;
            entries.add ( Integer.valueOf(gc) );
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(label + " glyph classes: " + toString(ca) );
        }
        return GlyphClassTable.createClassTable ( entries );
    }

    private GlyphClassTable readClassDefTableFormat2(FontFileReader in, String label, long tableOffset, int classFormat) throws IOException {
        List entries = new java.util.ArrayList();
        in.seekSet(tableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read range record count
        int nr = in.readTTFUShort();
        for ( int i = 0, n = nr; i < n; i++ ) {
            // read range start
            int s = in.readTTFUShort();
            // read range end
            int e = in.readTTFUShort();
            // read range glyph class (mapping) index
            int m = in.readTTFUShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(label + " range[" + i + "]: [" + s + "," + e + "]: " + m );
            }
            entries.add ( new GlyphClassTable.MappingRange ( s, e, m ) );
        }
        return GlyphClassTable.createClassTable ( entries );
    }

    private GlyphClassTable readClassDefTable(FontFileReader in, String label, long tableOffset) throws IOException {
        GlyphClassTable gct;
        long cp = in.getCurrentPos();
        in.seekSet(tableOffset);
        // read class table format
        int cf = in.readTTFUShort();
        if ( cf == 1 ) {
            gct = readClassDefTableFormat1 ( in, label, tableOffset, cf );
        } else if ( cf == 2 ) {
            gct = readClassDefTableFormat2 ( in, label, tableOffset, cf );
        } else {
            throw new UnsupportedOperationException ( "unsupported class definition table format: " + cf );
        }
        in.seekSet ( cp );
        return gct;
    }

    private void readSingleSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read delta glyph
        int dg = in.readTTFShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (delta)" );
            log.debug(tableTag + " single substitution coverage table offset: " + co );
            log.debug(tableTag + " single substitution delta: " + dg );
        }
        // read coverage table
        seMapping = readCoverageTable ( in, tableTag + " single substitution coverage", subtableOffset + co );
        seEntries.add ( Integer.valueOf ( dg ) );
    }

    private void readSingleSubTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read glyph count
        int ng = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single substitution subtable format: " + subtableFormat + " (mapped)" );
            log.debug(tableTag + " single substitution coverage table offset: " + co );
            log.debug(tableTag + " single substitution glyph count: " + ng );
        }
        // read coverage table
        seMapping = readCoverageTable ( in, tableTag + " single substitution coverage", subtableOffset + co );
        // read glyph substitutions
        int[] gsa = new int[ng];
        for ( int i = 0, n = ng; i < n; i++ ) {
            int gs = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " single substitution glyph[" + i + "]: " + gs );
            }
            gsa[i] = gs;
            seEntries.add ( Integer.valueOf ( gs ) );
        }
    }

    private int readSingleSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readSingleSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readSingleSubTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported single substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readMultipleSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read sequence count
        int ns = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " multiple substitution subtable format: " + subtableFormat + " (mapped)" );
            log.debug(tableTag + " multiple substitution coverage table offset: " + co );
            log.debug(tableTag + " multiple substitution sequence count: " + ns );
        }
        // read coverage table
        seMapping = readCoverageTable ( in, tableTag + " multiple substitution coverage", subtableOffset + co );
        // read sequence table offsets
        int[] soa = new int[ns];
        for ( int i = 0, n = ns; i < n; i++ ) {
            soa[i] = in.readTTFUShort();
        }
        // read sequence tables
        int[][] gsa = new int [ ns ] [];
        for ( int i = 0, n = ns; i < n; i++ ) {
            int so = soa[i];
            int[] ga;
            if ( so > 0 ) {
                in.seekSet(subtableOffset + so);
                // read glyph count
                int ng = in.readTTFUShort();
                ga = new int[ng];
                for ( int j = 0; j < ng; j++ ) {
                    ga[j] = in.readTTFUShort();
                }
            } else {
                ga = null;
            }
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " multiple substitution sequence[" + i + "]: " + toString ( ga ) );
            }
            gsa [ i ] = ga;
        }
        seEntries.add ( gsa );
    }

    private int readMultipleSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readMultipleSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported multiple substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readAlternateSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read alternate set count
        int ns = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " alternate substitution subtable format: " + subtableFormat + " (mapped)" );
            log.debug(tableTag + " alternate substitution coverage table offset: " + co );
            log.debug(tableTag + " alternate substitution alternate set count: " + ns );
        }
        // read coverage table
        seMapping = readCoverageTable ( in, tableTag + " alternate substitution coverage", subtableOffset + co );
        // read alternate set table offsets
        int[] soa = new int[ns];
        for ( int i = 0, n = ns; i < n; i++ ) {
            soa[i] = in.readTTFUShort();
        }
        // read alternate set tables
        for ( int i = 0, n = ns; i < n; i++ ) {
            int so = soa[i];
            in.seekSet(subtableOffset + so);
            // read glyph count
            int ng = in.readTTFUShort();
            int[] ga = new int[ng];
            for ( int j = 0; j < ng; j++ ) {
                int gs = in.readTTFUShort();
                ga[j] = gs;
            }
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " alternate substitution alternate set[" + i + "]: " + toString ( ga ) );
            }
            seEntries.add ( ga );
        }
    }

    private int readAlternateSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readAlternateSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported alternate substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readLigatureSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read ligature set count
        int ns = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " ligature substitution subtable format: " + subtableFormat + " (mapped)" );
            log.debug(tableTag + " ligature substitution coverage table offset: " + co );
            log.debug(tableTag + " ligature substitution ligature set count: " + ns );
        }
        // read coverage table
        seMapping = readCoverageTable ( in, tableTag + " ligature substitution coverage", subtableOffset + co );
        // read ligature set table offsets
        int[] soa = new int[ns];
        for ( int i = 0, n = ns; i < n; i++ ) {
            soa[i] = in.readTTFUShort();
        }
        // read ligature set tables
        for ( int i = 0, n = ns; i < n; i++ ) {
            int so = soa[i];
            in.seekSet(subtableOffset + so);
            // read ligature table count
            int nl = in.readTTFUShort();
            int[] loa = new int[nl];
            for ( int j = 0; j < nl; j++ ) {
                loa[j] = in.readTTFUShort();
            }
            List ligs = new java.util.ArrayList();
            for ( int j = 0; j < nl; j++ ) {
                int lo = loa[j];
                in.seekSet(subtableOffset + so + lo);
                // read ligature glyph id
                int lg = in.readTTFUShort();
                // read ligature (input) component count
                int nc = in.readTTFUShort();
                int[] ca = new int [ nc - 1 ];
                // read ligature (input) component glyph ids
                for ( int k = 0; k < nc - 1; k++ ) {
                    ca[k] = in.readTTFUShort();
                }
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " ligature substitution ligature set[" + i + "]: ligature(" + lg + "), components: " + toString ( ca ) );
                }
                ligs.add ( new GlyphSubstitutionTable.Ligature ( lg, ca ) );
            }
            seEntries.add ( new GlyphSubstitutionTable.LigatureSet ( ligs ) );
        }
    }

    private int readLigatureSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readLigatureSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported ligature substitution subtable format: " + sf );
        }
        return sf;
    }

    private GlyphTable.RuleLookup[] readRuleLookups(FontFileReader in, int numLookups, String header) throws IOException {
        GlyphTable.RuleLookup[] la = new GlyphTable.RuleLookup [ numLookups ];
        for ( int i = 0, n = numLookups; i < n; i++ ) {
            int sequenceIndex = in.readTTFUShort();
            int lookupIndex = in.readTTFUShort();
            la [ i ] = new GlyphTable.RuleLookup ( sequenceIndex, lookupIndex );
            // dump info if debugging and header is non-null
            if ( log.isDebugEnabled() && ( header != null ) ) {
                log.debug(header + "lookup[" + i + "]: " + la[i]);
            }
        }
        return la;
    }

    private void readContextualSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read rule set count
        int nrs = in.readTTFUShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for ( int i = 0; i < nrs; i++ ) {
            rsoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyphs)" );
            log.debug(tableTag + " contextual substitution coverage table offset: " + co );
            log.debug(tableTag + " contextual substitution rule set count: " + nrs );
            for ( int i = 0; i < nrs; i++ ) {
                log.debug(tableTag + " contextual substitution rule set offset[" + i + "]: " + rsoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " contextual substitution coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ nrs ];
        String header = null;
        for ( int i = 0; i < nrs; i++ ) {
            GlyphTable.RuleSet rs;
            int rso = rsoa [ i ];
            if ( rso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + rso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    GlyphTable.GlyphSequenceRule r;
                    int ro = roa [ j ];
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + rso + ro );
                        // read glyph count
                        int ng = in.readTTFUShort();
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read glyphs
                        int[] glyphs = new int [ ng - 1 ];
                        for ( int k = 0, nk = glyphs.length; k < nk; k++ ) {
                            glyphs [ k ] = in.readTTFUShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.GlyphSequenceRule ( lookups, ng, glyphs );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( rsa );
    }

    private void readContextualSubTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read class def table offset
        int cdo = in.readTTFUShort();
        // read class rule set count
        int ngc = in.readTTFUShort();
        // read class rule set offsets
        int[] csoa = new int [ ngc ];
        for ( int i = 0; i < ngc; i++ ) {
            csoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph classes)" );
            log.debug(tableTag + " contextual substitution coverage table offset: " + co );
            log.debug(tableTag + " contextual substitution class set count: " + ngc );
            for ( int i = 0; i < ngc; i++ ) {
                log.debug(tableTag + " contextual substitution class set offset[" + i + "]: " + csoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " contextual substitution coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read class definition table
        GlyphClassTable cdt;
        if ( cdo > 0 ) {
            cdt = readClassDefTable ( in, tableTag + " contextual substitution class definition", subtableOffset + cdo );
        } else {
            cdt = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ ngc ];
        String header = null;
        for ( int i = 0; i < ngc; i++ ) {
            int cso = csoa [ i ];
            GlyphTable.RuleSet rs;
            if ( cso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + cso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    int ro = roa [ j ];
                    GlyphTable.ClassSequenceRule r;
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + cso + ro );
                        // read glyph count
                        int ng = in.readTTFUShort();
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read classes
                        int[] classes = new int [ ng - 1 ];
                        for ( int k = 0, nk = classes.length; k < nk; k++ ) {
                            classes [ k ] = in.readTTFUShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ClassSequenceRule ( lookups, ng, classes );
                    } else {
                        assert ro > 0 : "unexpected null subclass rule offset";
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( cdt );
        seEntries.add ( Integer.valueOf ( ngc ) );
        seEntries.add ( rsa );
    }

    private void readContextualSubTableFormat3(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read glyph (input sequence length) count
        int ng = in.readTTFUShort();
        // read substitution lookup count
        int nl = in.readTTFUShort();
        // read glyph coverage offsets, one per glyph input sequence length count
        int[] gcoa = new int [ ng ];
        for ( int i = 0; i < ng; i++ ) {
            gcoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual substitution format: " + subtableFormat + " (glyph sets)" );
            log.debug(tableTag + " contextual substitution glyph input sequence length count: " + ng );
            log.debug(tableTag + " contextual substitution lookup count: " + nl );
            for ( int i = 0; i < ng; i++ ) {
                log.debug(tableTag + " contextual substitution coverage table offset[" + i + "]: " + gcoa[i] );
            }
        }
        // read coverage tables
        GlyphCoverageTable[] gca = new GlyphCoverageTable [ ng ];
        for ( int i = 0; i < ng; i++ ) {
            int gco = gcoa [ i ];
            GlyphCoverageTable gct;
            if ( gco > 0 ) {
                gct = readCoverageTable ( in, tableTag + " contextual substitution coverage[" + i + "]", subtableOffset + gco );
            } else {
                gct = null;
            }
            gca [ i ] = gct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " contextual substitution lookups: ";
        }
        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
        // construct rule, rule set, and rule set array
        GlyphTable.Rule r = new GlyphTable.CoverageSequenceRule ( lookups, ng, gca );
        GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet ( new GlyphTable.Rule[] {r} );
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[] {rs};
        // store results
        assert ( gca != null ) && ( gca.length > 0 );
        seMapping = gca[0];
        seEntries.add ( rsa );
    }

    private int readContextualSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readContextualSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readContextualSubTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 3 ) {
            readContextualSubTableFormat3 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported contextual substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readChainedContextualSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read rule set count
        int nrs = in.readTTFUShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for ( int i = 0; i < nrs; i++ ) {
            rsoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyphs)" );
            log.debug(tableTag + " chained contextual substitution coverage table offset: " + co );
            log.debug(tableTag + " chained contextual substitution rule set count: " + nrs );
            for ( int i = 0; i < nrs; i++ ) {
                log.debug(tableTag + " chained contextual substitution rule set offset[" + i + "]: " + rsoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " chained contextual substitution coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ nrs ];
        String header = null;
        for ( int i = 0; i < nrs; i++ ) {
            GlyphTable.RuleSet rs;
            int rso = rsoa [ i ];
            if ( rso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + rso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    GlyphTable.ChainedGlyphSequenceRule r;
                    int ro = roa [ j ];
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + rso + ro );
                        // read backtrack glyph count
                        int nbg = in.readTTFUShort();
                        // read backtrack glyphs
                        int[] backtrackGlyphs = new int [ nbg ];
                        for ( int k = 0, nk = backtrackGlyphs.length; k < nk; k++ ) {
                            backtrackGlyphs [ k ] = in.readTTFUShort();
                        }
                        // read input glyph count
                        int nig = in.readTTFUShort();
                        // read glyphs
                        int[] glyphs = new int [ nig - 1 ];
                        for ( int k = 0, nk = glyphs.length; k < nk; k++ ) {
                            glyphs [ k ] = in.readTTFUShort();
                        }
                        // read lookahead glyph count
                        int nlg = in.readTTFUShort();
                        // read lookahead glyphs
                        int[] lookaheadGlyphs = new int [ nlg ];
                        for ( int k = 0, nk = lookaheadGlyphs.length; k < nk; k++ ) {
                            lookaheadGlyphs [ k ] = in.readTTFUShort();
                        }
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ChainedGlyphSequenceRule ( lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( rsa );
    }

    private void readChainedContextualSubTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read backtrack class def table offset
        int bcdo = in.readTTFUShort();
        // read input class def table offset
        int icdo = in.readTTFUShort();
        // read lookahead class def table offset
        int lcdo = in.readTTFUShort();
        // read class set count
        int ngc = in.readTTFUShort();
        // read class set offsets
        int[] csoa = new int [ ngc ];
        for ( int i = 0; i < ngc; i++ ) {
            csoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph classes)" );
            log.debug(tableTag + " chained contextual substitution coverage table offset: " + co );
            log.debug(tableTag + " chained contextual substitution class set count: " + ngc );
            for ( int i = 0; i < ngc; i++ ) {
                log.debug(tableTag + " chained contextual substitution class set offset[" + i + "]: " + csoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " chained contextual substitution coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read backtrack class definition table
        GlyphClassTable bcdt;
        if ( bcdo > 0 ) {
            bcdt = readClassDefTable ( in, tableTag + " contextual substitution backtrack class definition", subtableOffset + bcdo );
        } else {
            bcdt = null;
        }
        // read input class definition table
        GlyphClassTable icdt;
        if ( icdo > 0 ) {
            icdt = readClassDefTable ( in, tableTag + " contextual substitution input class definition", subtableOffset + icdo );
        } else {
            icdt = null;
        }
        // read lookahead class definition table
        GlyphClassTable lcdt;
        if ( lcdo > 0 ) {
            lcdt = readClassDefTable ( in, tableTag + " contextual substitution lookahead class definition", subtableOffset + lcdo );
        } else {
            lcdt = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ ngc ];
        String header = null;
        for ( int i = 0; i < ngc; i++ ) {
            int cso = csoa [ i ];
            GlyphTable.RuleSet rs;
            if ( cso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + cso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    int ro = roa [ j ];
                    GlyphTable.ChainedClassSequenceRule r;
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + cso + ro );
                        // read backtrack glyph class count
                        int nbc = in.readTTFUShort();
                        // read backtrack glyph classes
                        int[] backtrackClasses = new int [ nbc ];
                        for ( int k = 0, nk = backtrackClasses.length; k < nk; k++ ) {
                            backtrackClasses [ k ] = in.readTTFUShort();
                        }
                        // read input glyph class count
                        int nic = in.readTTFUShort();
                        // read input glyph classes
                        int[] classes = new int [ nic - 1 ];
                        for ( int k = 0, nk = classes.length; k < nk; k++ ) {
                            classes [ k ] = in.readTTFUShort();
                        }
                        // read lookahead glyph class count
                        int nlc = in.readTTFUShort();
                        // read lookahead glyph classes
                        int[] lookaheadClasses = new int [ nlc ];
                        for ( int k = 0, nk = lookaheadClasses.length; k < nk; k++ ) {
                            lookaheadClasses [ k ] = in.readTTFUShort();
                        }
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual substitution lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ChainedClassSequenceRule ( lookups, nic, classes, backtrackClasses, lookaheadClasses );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( icdt );
        seEntries.add ( bcdt );
        seEntries.add ( lcdt );
        seEntries.add ( Integer.valueOf ( ngc ) );
        seEntries.add ( rsa );
    }

    private void readChainedContextualSubTableFormat3(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read backtrack glyph count
        int nbg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for ( int i = 0; i < nbg; i++ ) {
            bgcoa [ i ] = in.readTTFUShort();
        }
        // read input glyph count
        int nig = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] igcoa = new int [ nig ];
        for ( int i = 0; i < nig; i++ ) {
            igcoa [ i ] = in.readTTFUShort();
        }
        // read lookahead glyph count
        int nlg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for ( int i = 0; i < nlg; i++ ) {
            lgcoa [ i ] = in.readTTFUShort();
        }
        // read substitution lookup count
        int nl = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual substitution format: " + subtableFormat + " (glyph sets)" );
            log.debug(tableTag + " chained contextual substitution backtrack glyph count: " + nbg );
            for ( int i = 0; i < nbg; i++ ) {
                log.debug(tableTag + " chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i] );
            }
            log.debug(tableTag + " chained contextual substitution input glyph count: " + nig );
            for ( int i = 0; i < nig; i++ ) {
                log.debug(tableTag + " chained contextual substitution input coverage table offset[" + i + "]: " + igcoa[i] );
            }
            log.debug(tableTag + " chained contextual substitution lookahead glyph count: " + nlg );
            for ( int i = 0; i < nlg; i++ ) {
                log.debug(tableTag + " chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i] );
            }
            log.debug(tableTag + " chained contextual substitution lookup count: " + nl );
        }
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for ( int i = 0; i < nbg; i++ ) {
            int bgco = bgcoa [ i ];
            GlyphCoverageTable bgct;
            if ( bgco > 0 ) {
                bgct = readCoverageTable ( in, tableTag + " chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + bgco );
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read input coverage tables
        GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];
        for ( int i = 0; i < nig; i++ ) {
            int igco = igcoa [ i ];
            GlyphCoverageTable igct;
            if ( igco > 0 ) {
                igct = readCoverageTable ( in, tableTag + " chained contextual substitution input coverage[" + i + "]", subtableOffset + igco );
            } else {
                igct = null;
            }
            igca[i] = igct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for ( int i = 0; i < nlg; i++ ) {
            int lgco = lgcoa [ i ];
            GlyphCoverageTable lgct;
            if ( lgco > 0 ) {
                lgct = readCoverageTable ( in, tableTag + " chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + lgco );
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " chained contextual substitution lookups: ";
        }
        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
        // construct rule, rule set, and rule set array
        GlyphTable.Rule r = new GlyphTable.ChainedCoverageSequenceRule ( lookups, nig, igca, bgca, lgca );
        GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet ( new GlyphTable.Rule[] {r} );
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[] {rs};
        // store results
        assert ( igca != null ) && ( igca.length > 0 );
        seMapping = igca[0];
        seEntries.add ( rsa );
    }

    private int readChainedContextualSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readChainedContextualSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readChainedContextualSubTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 3 ) {
            readChainedContextualSubTableFormat3 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported chained contextual substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readExtensionSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read extension lookup type
        int lt = in.readTTFUShort();
        // read extension offset
        long eo = in.readTTFULong();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " extension substitution subtable format: " + subtableFormat );
            log.debug(tableTag + " extension substitution lookup type: " + lt );
            log.debug(tableTag + " extension substitution lookup table offset: " + eo );
        }
        // read referenced subtable from extended offset
        readGSUBSubtable ( in, lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo );
    }

    private int readExtensionSubTable(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readExtensionSubTableFormat1 ( in, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported extension substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readReverseChainedSingleSubTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GSUB";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read backtrack glyph count
        int nbg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for ( int i = 0; i < nbg; i++ ) {
            bgcoa [ i ] = in.readTTFUShort();
        }
        // read lookahead glyph count
        int nlg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for ( int i = 0; i < nlg; i++ ) {
            lgcoa [ i ] = in.readTTFUShort();
        }
        // read substitution (output) glyph count
        int ng = in.readTTFUShort();
        // read substitution (output) glyphs
        int[] glyphs = new int [ ng ];
        for ( int i = 0, n = ng; i < n; i++ ) {
            glyphs [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " reverse chained contextual substitution format: " + subtableFormat );
            log.debug(tableTag + " reverse chained contextual substitution coverage table offset: " + co );
            log.debug(tableTag + " reverse chained contextual substitution backtrack glyph count: " + nbg );
            for ( int i = 0; i < nbg; i++ ) {
                log.debug(tableTag + " reverse chained contextual substitution backtrack coverage table offset[" + i + "]: " + bgcoa[i] );
            }
            log.debug(tableTag + " reverse chained contextual substitution lookahead glyph count: " + nlg );
            for ( int i = 0; i < nlg; i++ ) {
                log.debug(tableTag + " reverse chained contextual substitution lookahead coverage table offset[" + i + "]: " + lgcoa[i] );
            }
            log.debug(tableTag + " reverse chained contextual substitution glyphs: " + toString(glyphs) );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " reverse chained contextual substitution coverage", subtableOffset + co );
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for ( int i = 0; i < nbg; i++ ) {
            int bgco = bgcoa[i];
            GlyphCoverageTable bgct;
            if ( bgco > 0 ) {
                bgct = readCoverageTable ( in, tableTag + " reverse chained contextual substitution backtrack coverage[" + i + "]", subtableOffset + bgco );
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for ( int i = 0; i < nlg; i++ ) {
            int lgco = lgcoa[i];
            GlyphCoverageTable lgct;
            if ( lgco > 0 ) {
                lgct = readCoverageTable ( in, tableTag + " reverse chained contextual substitution lookahead coverage[" + i + "]", subtableOffset + lgco );
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // store results
        seMapping = ct;
        seEntries.add ( bgca );
        seEntries.add ( lgca );
        seEntries.add ( glyphs );
    }

    private int readReverseChainedSingleSubTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read substitution subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readReverseChainedSingleSubTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported reverse chained single substitution subtable format: " + sf );
        }
        return sf;
    }

    private void readGSUBSubtable(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        initSESubState();
        int subtableFormat = -1;
        switch ( lookupType ) {
        case GSUBLookupType.SINGLE:
            subtableFormat = readSingleSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.MULTIPLE:
            subtableFormat = readMultipleSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.ALTERNATE:
            subtableFormat = readAlternateSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.LIGATURE:
            subtableFormat = readLigatureSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.CONTEXTUAL:
            subtableFormat = readContextualSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.CHAINED_CONTEXTUAL:
            subtableFormat = readChainedContextualSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.REVERSE_CHAINED_SINGLE:
            subtableFormat = readReverseChainedSingleSubTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GSUBLookupType.EXTENSION:
            subtableFormat = readExtensionSubTable ( in, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset );
            break;
        default:
            break;
        }
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat );
        resetSESubState();
    }

    private GlyphPositioningTable.DeviceTable readPosDeviceTable(FontFileReader in, long subtableOffset, long deviceTableOffset) throws IOException {
        long cp = in.getCurrentPos();
        in.seekSet(subtableOffset + deviceTableOffset);
        // read start size
        int ss = in.readTTFUShort();
        // read end size
        int es = in.readTTFUShort();
        // read delta format
        int df = in.readTTFUShort();
        // read deltas
        int n = ( es - ss ) + 1;
        int[] da = new int [ n ];
        int s1, m1, dm, dd, s2;
        if ( df == 1 ) {
            s1 = 14; m1 = 0x3; dm = 1; dd = 4; s2 = 2;
        } else if ( df == 2 ) {
            s1 = 12; m1 = 0xF; dm = 7; dd = 16; s2 = 4;
        } else if ( df == 3 ) {
            s1 = 8; m1 = 0xFF; dm = 127; dd = 256; s2 = 8;
        } else {
            throw new UnsupportedOperationException ( "unsupported device table delta format: " + df );
        }
        for ( int i = 0; ( i < n ) && ( s2 > 0 );) {
            int p = in.readTTFUShort();
            for ( int j = 0, k = 16 / s2; j < k; j++ ) {
                int d = ( p >> s1 ) & m1;
                if ( d > dm ) {
                    d -= dd;
                }
                if ( i < n ) {
                    da [ i++ ] = d;
                } else {
                    break;
                }
                p <<= s2;
            }
        }
        in.seekSet(cp);
        return new GlyphPositioningTable.DeviceTable ( ss, es, da );
    }

    private GlyphPositioningTable.Value readPosValue(FontFileReader in, long subtableOffset, int valueFormat) throws IOException {
        // XPlacement
        int xp;
        if ( ( valueFormat & GlyphPositioningTable.Value.X_PLACEMENT ) != 0 ) {
            xp = convertTTFUnit2PDFUnit ( in.readTTFShort() );
        } else {
            xp = 0;
        }
        // YPlacement
        int yp;
        if ( ( valueFormat & GlyphPositioningTable.Value.Y_PLACEMENT ) != 0 ) {
            yp = convertTTFUnit2PDFUnit ( in.readTTFShort() );
        } else {
            yp = 0;
        }
        // XAdvance
        int xa;
        if ( ( valueFormat & GlyphPositioningTable.Value.X_ADVANCE ) != 0 ) {
            xa = convertTTFUnit2PDFUnit ( in.readTTFShort() );
        } else {
            xa = 0;
        }
        // YAdvance
        int ya;
        if ( ( valueFormat & GlyphPositioningTable.Value.Y_ADVANCE ) != 0 ) {
            ya = convertTTFUnit2PDFUnit ( in.readTTFShort() );
        } else {
            ya = 0;
        }
        // XPlaDevice
        GlyphPositioningTable.DeviceTable xpd;
        if ( ( valueFormat & GlyphPositioningTable.Value.X_PLACEMENT_DEVICE ) != 0 ) {
            int xpdo = in.readTTFUShort();
            xpd = readPosDeviceTable ( in, subtableOffset, xpdo );
        } else {
            xpd = null;
        }
        // YPlaDevice
        GlyphPositioningTable.DeviceTable ypd;
        if ( ( valueFormat & GlyphPositioningTable.Value.Y_PLACEMENT_DEVICE ) != 0 ) {
            int ypdo = in.readTTFUShort();
            ypd = readPosDeviceTable ( in, subtableOffset, ypdo );
        } else {
            ypd = null;
        }
        // XAdvDevice
        GlyphPositioningTable.DeviceTable xad;
        if ( ( valueFormat & GlyphPositioningTable.Value.X_ADVANCE_DEVICE ) != 0 ) {
            int xado = in.readTTFUShort();
            xad = readPosDeviceTable ( in, subtableOffset, xado );
        } else {
            xad = null;
        }
        // YAdvDevice
        GlyphPositioningTable.DeviceTable yad;
        if ( ( valueFormat & GlyphPositioningTable.Value.Y_ADVANCE_DEVICE ) != 0 ) {
            int yado = in.readTTFUShort();
            yad = readPosDeviceTable ( in, subtableOffset, yado );
        } else {
            yad = null;
        }
        return new GlyphPositioningTable.Value ( xp, yp, xa, ya, xpd, ypd, xad, yad );
    }

    private void readSinglePosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read value format
        int vf = in.readTTFUShort();
        // read value
        GlyphPositioningTable.Value v = readPosValue ( in, subtableOffset, vf );
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (delta)" );
            log.debug(tableTag + " single positioning coverage table offset: " + co );
            log.debug(tableTag + " single positioning value: " + v );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " single positioning coverage", subtableOffset + co );
        // store results
        seMapping = ct;
        seEntries.add ( v );
    }

    private void readSinglePosTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read value format
        int vf = in.readTTFUShort();
        // read value count
        int nv = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " single positioning subtable format: " + subtableFormat + " (mapped)" );
            log.debug(tableTag + " single positioning coverage table offset: " + co );
            log.debug(tableTag + " single positioning value count: " + nv );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " single positioning coverage", subtableOffset + co );
        // read positioning values
        GlyphPositioningTable.Value[] pva = new GlyphPositioningTable.Value[nv];
        for ( int i = 0, n = nv; i < n; i++ ) {
            GlyphPositioningTable.Value pv = readPosValue ( in, subtableOffset, vf );
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " single positioning value[" + i + "]: " + pv );
            }
            pva[i] = pv;
        }
        // store results
        seMapping = ct;
        seEntries.add ( pva );
    }

    private int readSinglePosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positionining subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readSinglePosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readSinglePosTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported single positioning subtable format: " + sf );
        }
        return sf;
    }

    private GlyphPositioningTable.PairValues readPosPairValues(FontFileReader in, long subtableOffset, boolean hasGlyph, int vf1, int vf2) throws IOException {
        // read glyph (if present)
        int glyph;
        if ( hasGlyph ) {
            glyph = in.readTTFUShort();
        } else {
            glyph = 0;
        }
        // read first value (if present)
        GlyphPositioningTable.Value v1;
        if ( vf1 != 0 ) {
            v1 = readPosValue ( in, subtableOffset, vf1 );
        } else {
            v1 = null;
        }
        // read second value (if present)
        GlyphPositioningTable.Value v2;
        if ( vf2 != 0 ) {
            v2 = readPosValue ( in, subtableOffset, vf2 );
        } else {
            v2 = null;
        }
        return new GlyphPositioningTable.PairValues ( glyph, v1, v2 );
    }

    private GlyphPositioningTable.PairValues[] readPosPairSetTable(FontFileReader in, long subtableOffset, int pairSetTableOffset, int vf1, int vf2) throws IOException {
        String tableTag = "GPOS";
        long cp = in.getCurrentPos();
        in.seekSet(subtableOffset + pairSetTableOffset);
        // read pair values count
        int npv = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair set table offset: " + pairSetTableOffset );
            log.debug(tableTag + " pair set table values count: " + npv );
        }
        // read pair values
        GlyphPositioningTable.PairValues[] pva = new GlyphPositioningTable.PairValues [ npv ];
        for ( int i = 0, n = npv; i < n; i++ ) {
            GlyphPositioningTable.PairValues pv = readPosPairValues ( in, subtableOffset, true, vf1, vf2 );
            pva [ i ] = pv;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " pair set table value[" + i + "]: " + pv);
            }
        }
        in.seekSet(cp);
        return pva;
    }

    private void readPairPosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read value format for first glyph
        int vf1 = in.readTTFUShort();
        // read value format for second glyph
        int vf2 = in.readTTFUShort();
        // read number (count) of pair sets
        int nps = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyphs)" );
            log.debug(tableTag + " pair positioning coverage table offset: " + co );
            log.debug(tableTag + " pair positioning value format #1: " + vf1 );
            log.debug(tableTag + " pair positioning value format #2: " + vf2 );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " pair positioning coverage", subtableOffset + co );
        // read pair value matrix
        GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues [ nps ][];
        for ( int i = 0, n = nps; i < n; i++ ) {
            // read pair set offset
            int pso = in.readTTFUShort();
            // read pair set table at offset
            pvm [ i ] = readPosPairSetTable ( in, subtableOffset, pso, vf1, vf2 );
        }
        // store results
        seMapping = ct;
        seEntries.add ( pvm );
    }

    private void readPairPosTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read value format for first glyph
        int vf1 = in.readTTFUShort();
        // read value format for second glyph
        int vf2 = in.readTTFUShort();
        // read class def 1 offset
        int cd1o = in.readTTFUShort();
        // read class def 2 offset
        int cd2o = in.readTTFUShort();
        // read number (count) of classes in class def 1 table
        int nc1 = in.readTTFUShort();
        // read number (count) of classes in class def 2 table
        int nc2 = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " pair positioning subtable format: " + subtableFormat + " (glyph classes)" );
            log.debug(tableTag + " pair positioning coverage table offset: " + co );
            log.debug(tableTag + " pair positioning value format #1: " + vf1 );
            log.debug(tableTag + " pair positioning value format #2: " + vf2 );
            log.debug(tableTag + " pair positioning class def table #1 offset: " + cd1o );
            log.debug(tableTag + " pair positioning class def table #2 offset: " + cd2o );
            log.debug(tableTag + " pair positioning class #1 count: " + nc1 );
            log.debug(tableTag + " pair positioning class #2 count: " + nc2 );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " pair positioning coverage", subtableOffset + co );
        // read class definition table #1
        GlyphClassTable cdt1 = readClassDefTable ( in, tableTag + " pair positioning class definition #1", subtableOffset + cd1o );
        // read class definition table #2
        GlyphClassTable cdt2 = readClassDefTable ( in, tableTag + " pair positioning class definition #2", subtableOffset + cd2o );
        // read pair value matrix
        GlyphPositioningTable.PairValues[][] pvm = new GlyphPositioningTable.PairValues [ nc1 ] [ nc2 ];
        for ( int i = 0; i < nc1; i++ ) {
            for ( int j = 0; j < nc2; j++ ) {
                GlyphPositioningTable.PairValues pv = readPosPairValues ( in, subtableOffset, false, vf1, vf2 );
                pvm [ i ] [ j ] = pv;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " pair set table value[" + i + "][" + j + "]: " + pv);
                }
            }            
        }
        // store results
        seMapping = ct;
        seEntries.add ( cdt1 );
        seEntries.add ( cdt2 );
        seEntries.add ( Integer.valueOf ( nc1 ) );
        seEntries.add ( Integer.valueOf ( nc2 ) );
        seEntries.add ( pvm );
    }

    private int readPairPosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readPairPosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readPairPosTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported pair positioning subtable format: " + sf );
        }
        return sf;
    }

    private GlyphPositioningTable.Anchor readPosAnchor(FontFileReader in, long anchorTableOffset) throws IOException {
        GlyphPositioningTable.Anchor a;
        long cp = in.getCurrentPos();
        in.seekSet(anchorTableOffset);
        // read anchor table format
        int af = in.readTTFUShort();
        if ( af == 1 ) {
            // read x coordinate
            int x = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            // read y coordinate
            int y = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            a = new GlyphPositioningTable.Anchor ( x, y );
        } else if ( af == 2 ) {
            // read x coordinate
            int x = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            // read y coordinate
            int y = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            // read anchor point index
            int ap = in.readTTFUShort();
            a = new GlyphPositioningTable.Anchor ( x, y, ap );
        } else if ( af == 3 ) {
            // read x coordinate
            int x = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            // read y coordinate
            int y = convertTTFUnit2PDFUnit ( in.readTTFShort() );
            // read x device table offset
            int xdo = in.readTTFUShort();
            // read y device table offset
            int ydo = in.readTTFUShort();
            // read x device table (if present)
            GlyphPositioningTable.DeviceTable xd;
            if ( xdo != 0 ) {
                xd = readPosDeviceTable ( in, cp, xdo );
            } else {
                xd = null;
            }
            // read y device table (if present)
            GlyphPositioningTable.DeviceTable yd;
            if ( ydo != 0 ) {
                yd = readPosDeviceTable ( in, cp, ydo );
            } else {
                yd = null;
            }
            a = new GlyphPositioningTable.Anchor ( x, y, xd, yd );
        } else {
            throw new UnsupportedOperationException ( "unsupported positioning anchor format: " + af );
        }
        in.seekSet(cp);
        return a;
    }

    private void readCursivePosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read entry/exit count
        int ec = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " cursive positioning subtable format: " + subtableFormat );
            log.debug(tableTag + " cursive positioning coverage table offset: " + co );
            log.debug(tableTag + " cursive positioning entry/exit count: " + ec );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " cursive positioning coverage", subtableOffset + co );
        // read entry/exit records
        GlyphPositioningTable.Anchor[] aa = new GlyphPositioningTable.Anchor [ ec * 2 ];
        for ( int i = 0, n = ec; i < n; i++ ) {
            // read entry anchor offset
            int eno = in.readTTFUShort();
            // read exit anchor offset
            int exo = in.readTTFUShort();
            // read entry anchor
            GlyphPositioningTable.Anchor ena;
            if ( eno > 0 ) {
                ena = readPosAnchor ( in, subtableOffset + eno );
            } else {
                ena = null;
            }
            // read exit anchor
            GlyphPositioningTable.Anchor exa;
            if ( exo > 0 ) {
                exa = readPosAnchor ( in, subtableOffset + exo );
            } else {
                exa = null;
            }
            aa [ ( i * 2 ) + 0 ] = ena;
            aa [ ( i * 2 ) + 1 ] = exa;
            if (log.isDebugEnabled()) {
                if ( ena != null ) {
                    log.debug(tableTag + " cursive entry anchor [" + i + "]: " + ena );
                }
                if ( exa != null ) {
                    log.debug(tableTag + " cursive exit anchor  [" + i + "]: " + exa );
                }
            }
        }
        // store results
        seMapping = ct;
        seEntries.add ( aa );
    }

    private int readCursivePosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readCursivePosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported cursive positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readMarkToBasePosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read mark coverage offset
        int mco = in.readTTFUShort();
        // read base coverage offset
        int bco = in.readTTFUShort();
        // read mark class count
        int nmc = in.readTTFUShort();
        // read mark array offset
        int mao = in.readTTFUShort();
        // read base array offset
        int bao = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning subtable format: " + subtableFormat );
            log.debug(tableTag + " mark-to-base positioning mark coverage table offset: " + mco );
            log.debug(tableTag + " mark-to-base positioning base coverage table offset: " + bco );
            log.debug(tableTag + " mark-to-base positioning mark class count: " + nmc );
            log.debug(tableTag + " mark-to-base positioning mark array offset: " + mao );
            log.debug(tableTag + " mark-to-base positioning base array offset: " + bao );
        }
        // read mark coverage table
        GlyphCoverageTable mct = readCoverageTable ( in, tableTag + " mark-to-base positioning mark coverage", subtableOffset + mco );
        // read base coverage table
        GlyphCoverageTable bct = readCoverageTable ( in, tableTag + " mark-to-base positioning base coverage", subtableOffset + bco );
        // read mark anchor array
        // seek to mark array
        in.seekSet(subtableOffset + mao);
        // read mark count
        int nm = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning mark count: " + nm );
        }
        // read mark anchor array, where i:{0...markCount}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm ];
        for ( int i = 0; i < nm; i++ ) {
            // read mark class
            int mc = in.readTTFUShort();
            // read mark anchor offset
            int ao = in.readTTFUShort();
            GlyphPositioningTable.Anchor a;
            if ( ao > 0 ) {
                a = readPosAnchor ( in, subtableOffset + mao + ao );
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if ( a != null ) {
                ma = new GlyphPositioningTable.MarkAnchor ( mc, a );
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-base positioning mark anchor[" + i + "]: " + ma);
            }

        }
        // read base anchor matrix
        // seek to base array
        in.seekSet(subtableOffset + bao);
        // read base count
        int nb = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-base positioning base count: " + nb );
        }
        // read anchor matrix, where i:{0...baseCount - 1}, j:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][] bam = new GlyphPositioningTable.Anchor [ nb ] [ nmc ];
        for ( int i = 0; i < nb; i++ ) {
            for ( int j = 0; j < nmc; j++ ) {
                // read base anchor offset
                int ao = in.readTTFUShort();
                GlyphPositioningTable.Anchor a;
                if ( ao > 0 ) {
                    a = readPosAnchor ( in, subtableOffset + bao + ao );
                } else {
                    a = null;
                }
                bam [ i ] [ j ] = a;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " mark-to-base positioning base anchor[" + i + "][" + j + "]: " + a);
                }
            }            
        }
        // store results
        seMapping = mct;
        seEntries.add ( bct );
        seEntries.add ( Integer.valueOf ( nmc ) );
        seEntries.add ( maa );
        seEntries.add ( bam );
    }

    private int readMarkToBasePosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readMarkToBasePosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported mark-to-base positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readMarkToLigaturePosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read mark coverage offset
        int mco = in.readTTFUShort();
        // read ligature coverage offset
        int lco = in.readTTFUShort();
        // read mark class count
        int nmc = in.readTTFUShort();
        // read mark array offset
        int mao = in.readTTFUShort();
        // read ligature array offset
        int lao = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning subtable format: " + subtableFormat );
            log.debug(tableTag + " mark-to-ligature positioning mark coverage table offset: " + mco );
            log.debug(tableTag + " mark-to-ligature positioning ligature coverage table offset: " + lco );
            log.debug(tableTag + " mark-to-ligature positioning mark class count: " + nmc );
            log.debug(tableTag + " mark-to-ligature positioning mark array offset: " + mao );
            log.debug(tableTag + " mark-to-ligature positioning ligature array offset: " + lao );
        }
        // read mark coverage table
        GlyphCoverageTable mct = readCoverageTable ( in, tableTag + " mark-to-ligature positioning mark coverage", subtableOffset + mco );
        // read ligature coverage table
        GlyphCoverageTable lct = readCoverageTable ( in, tableTag + " mark-to-ligature positioning ligature coverage", subtableOffset + lco );
        // read mark anchor array
        // seek to mark array
        in.seekSet(subtableOffset + mao);
        // read mark count
        int nm = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning mark count: " + nm );
        }
        // read mark anchor array, where i:{0...markCount}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm ];
        for ( int i = 0; i < nm; i++ ) {
            // read mark class
            int mc = in.readTTFUShort();
            // read mark anchor offset
            int ao = in.readTTFUShort();
            GlyphPositioningTable.Anchor a;
            if ( ao > 0 ) {
                a = readPosAnchor ( in, subtableOffset + mao + ao );
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if ( a != null ) {
                ma = new GlyphPositioningTable.MarkAnchor ( mc, a );
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-ligature positioning mark anchor[" + i + "]: " + ma);
            }
        }
        // read ligature anchor matrix
        // seek to ligature array
        in.seekSet(subtableOffset + lao);
        // read ligature count
        int nl = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning ligature count: " + nl );
        }
        // read ligature attach table offsets
        int[] laoa = new int [ nl ];
        for ( int i = 0; i < nl; i++ ) {
            laoa [ i ] = in.readTTFUShort();
        }
        // iterate over ligature attach tables, recording maximum component count
        int mxc = 0;
        for ( int i = 0; i < nl; i++ ) {
            int lato = laoa [ i ];
            in.seekSet ( subtableOffset + lao + lato );
            // read component count
            int cc = in.readTTFUShort();
            if ( cc > mxc ) {
                mxc = cc;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-ligature positioning maximum component count: " + mxc );
        }
        // read anchor matrix, where i:{0...ligatureCount - 1}, j:{0...maxComponentCount - 1}, k:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][][] lam = new GlyphPositioningTable.Anchor [ nl ][][];
        for ( int i = 0; i < nl; i++ ) {
            int lato = laoa [ i ];
            // seek to ligature attach table for ligature[i]
            in.seekSet ( subtableOffset + lao + lato );
            // read component count
            int cc = in.readTTFUShort();
            GlyphPositioningTable.Anchor[][] lcm = new GlyphPositioningTable.Anchor [ cc ] [ nmc ];
            for ( int j = 0; j < cc; j++ ) {
                for ( int k = 0; k < nmc; k++ ) {
                    // read ligature anchor offset
                    int ao = in.readTTFUShort();
                    GlyphPositioningTable.Anchor a;
                    if ( ao > 0 ) {
                        a  = readPosAnchor ( in, subtableOffset + lao + lato + ao );
                    } else {
                        a = null;
                    }
                    lcm [ j ] [ k ] = a;
                    if (log.isDebugEnabled()) {
                        log.debug(tableTag + " mark-to-ligature positioning ligature anchor[" + i + "][" + j + "][" + k + "]: " + a);
                    }
                }
            }
            lam [ i ] = lcm;
        }
        // store results
        seMapping = mct;
        seEntries.add ( lct );
        seEntries.add ( Integer.valueOf ( nmc ) );
        seEntries.add ( Integer.valueOf ( mxc ) );
        seEntries.add ( maa );
        seEntries.add ( lam );
    }

    private int readMarkToLigaturePosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readMarkToLigaturePosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported mark-to-ligature positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readMarkToMarkPosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read mark #1 coverage offset
        int m1co = in.readTTFUShort();
        // read mark #2 coverage offset
        int m2co = in.readTTFUShort();
        // read mark class count
        int nmc = in.readTTFUShort();
        // read mark #1 array offset
        int m1ao = in.readTTFUShort();
        // read mark #2 array offset
        int m2ao = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning subtable format: " + subtableFormat );
            log.debug(tableTag + " mark-to-mark positioning mark #1 coverage table offset: " + m1co );
            log.debug(tableTag + " mark-to-mark positioning mark #2 coverage table offset: " + m2co );
            log.debug(tableTag + " mark-to-mark positioning mark class count: " + nmc );
            log.debug(tableTag + " mark-to-mark positioning mark #1 array offset: " + m1ao );
            log.debug(tableTag + " mark-to-mark positioning mark #2 array offset: " + m2ao );
        }
        // read mark #1 coverage table
        GlyphCoverageTable mct1 = readCoverageTable ( in, tableTag + " mark-to-mark positioning mark #1 coverage", subtableOffset + m1co );
        // read mark #2 coverage table
        GlyphCoverageTable mct2 = readCoverageTable ( in, tableTag + " mark-to-mark positioning mark #2 coverage", subtableOffset + m2co );
        // read mark #1 anchor array
        // seek to mark array
        in.seekSet(subtableOffset + m1ao);
        // read mark count
        int nm1 = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning mark #1 count: " + nm1 );
        }
        // read mark anchor array, where i:{0...mark1Count}
        GlyphPositioningTable.MarkAnchor[] maa = new GlyphPositioningTable.MarkAnchor [ nm1 ];
        for ( int i = 0; i < nm1; i++ ) {
            // read mark class
            int mc = in.readTTFUShort();
            // read mark anchor offset
            int ao = in.readTTFUShort();
            GlyphPositioningTable.Anchor a;
            if ( ao > 0 ) {
                a = readPosAnchor ( in, subtableOffset + m1ao + ao );
            } else {
                a = null;
            }
            GlyphPositioningTable.MarkAnchor ma;
            if ( a != null ) {
                ma = new GlyphPositioningTable.MarkAnchor ( mc, a );
            } else {
                ma = null;
            }
            maa [ i ] = ma;
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " mark-to-mark positioning mark #1 anchor[" + i + "]: " + ma);
            }
        }
        // read mark #2 anchor matrix
        // seek to mark #2 array
        in.seekSet(subtableOffset + m2ao);
        // read mark #2 count
        int nm2 = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark-to-mark positioning mark #2 count: " + nm2 );
        }
        // read anchor matrix, where i:{0...mark2Count - 1}, j:{0...markClassCount - 1}
        GlyphPositioningTable.Anchor[][] mam = new GlyphPositioningTable.Anchor [ nm2 ] [ nmc ];
        for ( int i = 0; i < nm2; i++ ) {
            for ( int j = 0; j < nmc; j++ ) {
                // read mark anchor offset
                int ao = in.readTTFUShort();
                GlyphPositioningTable.Anchor a;
                if ( ao > 0 ) {
                    a = readPosAnchor ( in, subtableOffset + m2ao + ao );
                } else {
                    a = null;
                }
                mam [ i ] [ j ] = a;
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " mark-to-mark positioning mark #2 anchor[" + i + "][" + j + "]: " + a);
                }
            }            
        }
        // store results
        seMapping = mct1;
        seEntries.add ( mct2 );
        seEntries.add ( Integer.valueOf ( nmc ) );
        seEntries.add ( maa );
        seEntries.add ( mam );
    }

    private int readMarkToMarkPosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readMarkToMarkPosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported mark-to-mark positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readContextualPosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read rule set count
        int nrs = in.readTTFUShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for ( int i = 0; i < nrs; i++ ) {
            rsoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyphs)" );
            log.debug(tableTag + " contextual positioning coverage table offset: " + co );
            log.debug(tableTag + " contextual positioning rule set count: " + nrs );
            for ( int i = 0; i < nrs; i++ ) {
                log.debug(tableTag + " contextual positioning rule set offset[" + i + "]: " + rsoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " contextual positioning coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ nrs ];
        String header = null;
        for ( int i = 0; i < nrs; i++ ) {
            GlyphTable.RuleSet rs;
            int rso = rsoa [ i ];
            if ( rso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + rso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    GlyphTable.GlyphSequenceRule r;
                    int ro = roa [ j ];
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + rso + ro );
                        // read glyph count
                        int ng = in.readTTFUShort();
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read glyphs
                        int[] glyphs = new int [ ng - 1 ];
                        for ( int k = 0, nk = glyphs.length; k < nk; k++ ) {
                            glyphs [ k ] = in.readTTFUShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.GlyphSequenceRule ( lookups, ng, glyphs );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( rsa );
    }

    private void readContextualPosTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read class def table offset
        int cdo = in.readTTFUShort();
        // read class rule set count
        int ngc = in.readTTFUShort();
        // read class rule set offsets
        int[] csoa = new int [ ngc ];
        for ( int i = 0; i < ngc; i++ ) {
            csoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph classes)" );
            log.debug(tableTag + " contextual positioning coverage table offset: " + co );
            log.debug(tableTag + " contextual positioning class set count: " + ngc );
            for ( int i = 0; i < ngc; i++ ) {
                log.debug(tableTag + " contextual positioning class set offset[" + i + "]: " + csoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " contextual positioning coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read class definition table
        GlyphClassTable cdt;
        if ( cdo > 0 ) {
            cdt = readClassDefTable ( in, tableTag + " contextual positioning class definition", subtableOffset + cdo );
        } else {
            cdt = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ ngc ];
        String header = null;
        for ( int i = 0; i < ngc; i++ ) {
            int cso = csoa [ i ];
            GlyphTable.RuleSet rs;
            if ( cso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + cso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    int ro = roa [ j ];
                    GlyphTable.ClassSequenceRule r;
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + cso + ro );
                        // read glyph count
                        int ng = in.readTTFUShort();
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read classes
                        int[] classes = new int [ ng - 1 ];
                        for ( int k = 0, nk = classes.length; k < nk; k++ ) {
                            classes [ k ] = in.readTTFUShort();
                        }
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ClassSequenceRule ( lookups, ng, classes );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( cdt );
        seEntries.add ( Integer.valueOf ( ngc ) );
        seEntries.add ( rsa );
    }

    private void readContextualPosTableFormat3(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read glyph (input sequence length) count
        int ng = in.readTTFUShort();
        // read positioning lookup count
        int nl = in.readTTFUShort();
        // read glyph coverage offsets, one per glyph input sequence length count
        int[] gcoa = new int [ ng ];
        for ( int i = 0; i < ng; i++ ) {
            gcoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " contextual positioning subtable format: " + subtableFormat + " (glyph sets)" );
            log.debug(tableTag + " contextual positioning glyph input sequence length count: " + ng );
            log.debug(tableTag + " contextual positioning lookup count: " + nl );
            for ( int i = 0; i < ng; i++ ) {
                log.debug(tableTag + " contextual positioning coverage table offset[" + i + "]: " + gcoa[i] );
            }
        }
        // read coverage tables
        GlyphCoverageTable[] gca = new GlyphCoverageTable [ ng ];
        for ( int i = 0; i < ng; i++ ) {
            int gco = gcoa [ i ];
            GlyphCoverageTable gct;
            if ( gco > 0 ) {
                gct = readCoverageTable ( in, tableTag + " contextual positioning coverage[" + i + "]", subtableOffset + gcoa[i] );
            } else {
                gct = null;
            }
            gca [ i ] = gct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " contextual positioning lookups: ";
        }
        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
        // construct rule, rule set, and rule set array
        GlyphTable.Rule r = new GlyphTable.CoverageSequenceRule ( lookups, ng, gca );
        GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet ( new GlyphTable.Rule[] {r} );
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[] {rs};
        // store results
        assert ( gca != null ) && ( gca.length > 0 );
        seMapping = gca[0];
        seEntries.add ( rsa );
    }

    private int readContextualPosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readContextualPosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readContextualPosTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 3 ) {
            readContextualPosTableFormat3 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported contextual positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readChainedContextualPosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read rule set count
        int nrs = in.readTTFUShort();
        // read rule set offsets
        int[] rsoa = new int [ nrs ];
        for ( int i = 0; i < nrs; i++ ) {
            rsoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyphs)" );
            log.debug(tableTag + " chained contextual positioning coverage table offset: " + co );
            log.debug(tableTag + " chained contextual positioning rule set count: " + nrs );
            for ( int i = 0; i < nrs; i++ ) {
                log.debug(tableTag + " chained contextual positioning rule set offset[" + i + "]: " + rsoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " chained contextual positioning coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ nrs ];
        String header = null;
        for ( int i = 0; i < nrs; i++ ) {
            GlyphTable.RuleSet rs;
            int rso = rsoa [ i ];
            if ( rso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + rso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    GlyphTable.ChainedGlyphSequenceRule r;
                    int ro = roa [ j ];
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + rso + ro );
                        // read backtrack glyph count
                        int nbg = in.readTTFUShort();
                        // read backtrack glyphs
                        int[] backtrackGlyphs = new int [ nbg ];
                        for ( int k = 0, nk = backtrackGlyphs.length; k < nk; k++ ) {
                            backtrackGlyphs [ k ] = in.readTTFUShort();
                        }
                        // read input glyph count
                        int nig = in.readTTFUShort();
                        // read glyphs
                        int[] glyphs = new int [ nig - 1 ];
                        for ( int k = 0, nk = glyphs.length; k < nk; k++ ) {
                            glyphs [ k ] = in.readTTFUShort();
                        }
                        // read lookahead glyph count
                        int nlg = in.readTTFUShort();
                        // read lookahead glyphs
                        int[] lookaheadGlyphs = new int [ nlg ];
                        for ( int k = 0, nk = lookaheadGlyphs.length; k < nk; k++ ) {
                            lookaheadGlyphs [ k ] = in.readTTFUShort();
                        }
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ChainedGlyphSequenceRule ( lookups, nig, glyphs, backtrackGlyphs, lookaheadGlyphs );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( rsa );
    }

    private void readChainedContextualPosTableFormat2(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read coverage offset
        int co = in.readTTFUShort();
        // read backtrack class def table offset
        int bcdo = in.readTTFUShort();
        // read input class def table offset
        int icdo = in.readTTFUShort();
        // read lookahead class def table offset
        int lcdo = in.readTTFUShort();
        // read class set count
        int ngc = in.readTTFUShort();
        // read class set offsets
        int[] csoa = new int [ ngc ];
        for ( int i = 0; i < ngc; i++ ) {
            csoa [ i ] = in.readTTFUShort();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph classes)" );
            log.debug(tableTag + " chained contextual positioning coverage table offset: " + co );
            log.debug(tableTag + " chained contextual positioning class set count: " + ngc );
            for ( int i = 0; i < ngc; i++ ) {
                log.debug(tableTag + " chained contextual positioning class set offset[" + i + "]: " + csoa[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct;
        if ( co > 0 ) {
            ct = readCoverageTable ( in, tableTag + " chained contextual positioning coverage", subtableOffset + co );
        } else {
            ct = null;
        }
        // read backtrack class definition table
        GlyphClassTable bcdt;
        if ( bcdo > 0 ) {
            bcdt = readClassDefTable ( in, tableTag + " contextual positioning backtrack class definition", subtableOffset + bcdo );
        } else {
            bcdt = null;
        }
        // read input class definition table
        GlyphClassTable icdt;
        if ( icdo > 0 ) {
            icdt = readClassDefTable ( in, tableTag + " contextual positioning input class definition", subtableOffset + icdo );
        } else {
            icdt = null;
        }
        // read lookahead class definition table
        GlyphClassTable lcdt;
        if ( lcdo > 0 ) {
            lcdt = readClassDefTable ( in, tableTag + " contextual positioning lookahead class definition", subtableOffset + lcdo );
        } else {
            lcdt = null;
        }
        // read rule sets
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet [ ngc ];
        String header = null;
        for ( int i = 0; i < ngc; i++ ) {
            int cso = csoa [ i ];
            GlyphTable.RuleSet rs;
            if ( cso > 0 ) {
                // seek to rule set [ i ]
                in.seekSet ( subtableOffset + cso );
                // read rule count
                int nr = in.readTTFUShort();
                // read rule offsets
                int[] roa = new int [ nr ];
                GlyphTable.Rule[] ra = new GlyphTable.Rule [ nr ];
                for ( int j = 0; j < nr; j++ ) {
                    roa [ j ] = in.readTTFUShort();
                }
                // read glyph sequence rules
                for ( int j = 0; j < nr; j++ ) {
                    GlyphTable.ChainedClassSequenceRule r;
                    int ro = roa [ j ];
                    if ( ro > 0 ) {
                        // seek to rule [ j ]
                        in.seekSet ( subtableOffset + cso + ro );
                        // read backtrack glyph class count
                        int nbc = in.readTTFUShort();
                        // read backtrack glyph classes
                        int[] backtrackClasses = new int [ nbc ];
                        for ( int k = 0, nk = backtrackClasses.length; k < nk; k++ ) {
                            backtrackClasses [ k ] = in.readTTFUShort();
                        }
                        // read input glyph class count
                        int nic = in.readTTFUShort();
                        // read input glyph classes
                        int[] classes = new int [ nic - 1 ];
                        for ( int k = 0, nk = classes.length; k < nk; k++ ) {
                            classes [ k ] = in.readTTFUShort();
                        }
                        // read lookahead glyph class count
                        int nlc = in.readTTFUShort();
                        // read lookahead glyph classes
                        int[] lookaheadClasses = new int [ nlc ];
                        for ( int k = 0, nk = lookaheadClasses.length; k < nk; k++ ) {
                            lookaheadClasses [ k ] = in.readTTFUShort();
                        }
                        // read rule lookup count
                        int nl = in.readTTFUShort();
                        // read rule lookups
                        if (log.isDebugEnabled()) {
                            header = tableTag + " contextual positioning lookups @rule[" + i + "][" + j + "]: ";
                        }
                        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
                        r = new GlyphTable.ChainedClassSequenceRule ( lookups, nic, classes, backtrackClasses, lookaheadClasses );
                    } else {
                        r = null;
                    }
                    ra [ j ] = r;
                }
                rs = new GlyphTable.HomogeneousRuleSet ( ra );
            } else {
                rs = null;
            }
            rsa [ i ] = rs;
        }
        // store results
        seMapping = ct;
        seEntries.add ( icdt );
        seEntries.add ( bcdt );
        seEntries.add ( lcdt );
        seEntries.add ( Integer.valueOf ( ngc ) );
        seEntries.add ( rsa );
    }

    private void readChainedContextualPosTableFormat3(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read backtrack glyph count
        int nbg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] bgcoa = new int [ nbg ];
        for ( int i = 0; i < nbg; i++ ) {
            bgcoa [ i ] = in.readTTFUShort();
        }
        // read input glyph count
        int nig = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] igcoa = new int [ nig ];
        for ( int i = 0; i < nig; i++ ) {
            igcoa [ i ] = in.readTTFUShort();
        }
        // read lookahead glyph count
        int nlg = in.readTTFUShort();
        // read backtrack glyph coverage offsets
        int[] lgcoa = new int [ nlg ];
        for ( int i = 0; i < nlg; i++ ) {
            lgcoa [ i ] = in.readTTFUShort();
        }
        // read positioning lookup count
        int nl = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " chained contextual positioning subtable format: " + subtableFormat + " (glyph sets)" );
            log.debug(tableTag + " chained contextual positioning backtrack glyph count: " + nbg );
            for ( int i = 0; i < nbg; i++ ) {
                log.debug(tableTag + " chained contextual positioning backtrack coverage table offset[" + i + "]: " + bgcoa[i] );
            }
            log.debug(tableTag + " chained contextual positioning input glyph count: " + nig );
            for ( int i = 0; i < nig; i++ ) {
                log.debug(tableTag + " chained contextual positioning input coverage table offset[" + i + "]: " + igcoa[i] );
            }
            log.debug(tableTag + " chained contextual positioning lookahead glyph count: " + nlg );
            for ( int i = 0; i < nlg; i++ ) {
                log.debug(tableTag + " chained contextual positioning lookahead coverage table offset[" + i + "]: " + lgcoa[i] );
            }
            log.debug(tableTag + " chained contextual positioning lookup count: " + nl );
        }
        // read backtrack coverage tables
        GlyphCoverageTable[] bgca = new GlyphCoverageTable[nbg];
        for ( int i = 0; i < nbg; i++ ) {
            int bgco = bgcoa [ i ];
            GlyphCoverageTable bgct;
            if ( bgco > 0 ) {
                bgct = readCoverageTable ( in, tableTag + " chained contextual positioning backtrack coverage[" + i + "]", subtableOffset + bgco );
            } else {
                bgct = null;
            }
            bgca[i] = bgct;
        }
        // read input coverage tables
        GlyphCoverageTable[] igca = new GlyphCoverageTable[nig];
        for ( int i = 0; i < nig; i++ ) {
            int igco = igcoa [ i ];
            GlyphCoverageTable igct;
            if ( igco > 0 ) {
                igct = readCoverageTable ( in, tableTag + " chained contextual positioning input coverage[" + i + "]", subtableOffset + igco );
            } else {
                igct = null;
            }
            igca[i] = igct;
        }
        // read lookahead coverage tables
        GlyphCoverageTable[] lgca = new GlyphCoverageTable[nlg];
        for ( int i = 0; i < nlg; i++ ) {
            int lgco = lgcoa [ i ];
            GlyphCoverageTable lgct;
            if ( lgco > 0 ) {
                lgct = readCoverageTable ( in, tableTag + " chained contextual positioning lookahead coverage[" + i + "]", subtableOffset + lgco );
            } else {
                lgct = null;
            }
            lgca[i] = lgct;
        }
        // read rule lookups
        String header = null;
        if (log.isDebugEnabled()) {
            header = tableTag + " chained contextual positioning lookups: ";
        }
        GlyphTable.RuleLookup[] lookups = readRuleLookups ( in, nl, header );
        // construct rule, rule set, and rule set array
        GlyphTable.Rule r = new GlyphTable.ChainedCoverageSequenceRule ( lookups, nig, igca, bgca, lgca );
        GlyphTable.RuleSet rs = new GlyphTable.HomogeneousRuleSet ( new GlyphTable.Rule[] {r} );
        GlyphTable.RuleSet[] rsa = new GlyphTable.RuleSet[] {rs};
        // store results
        assert ( igca != null ) && ( igca.length > 0 );
        seMapping = igca[0];
        seEntries.add ( rsa );
    }

    private int readChainedContextualPosTable(FontFileReader in, int lookupType, int lookupFlags, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readChainedContextualPosTableFormat1 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 2 ) {
            readChainedContextualPosTableFormat2 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else if ( sf == 3 ) {
            readChainedContextualPosTableFormat3 ( in, lookupType, lookupFlags, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported chained contextual positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readExtensionPosTableFormat1(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset, int subtableFormat) throws IOException {
        String tableTag = "GPOS";
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read extension lookup type
        int lt = in.readTTFUShort();
        // read extension offset
        long eo = in.readTTFULong();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " extension positioning subtable format: " + subtableFormat );
            log.debug(tableTag + " extension positioning lookup type: " + lt );
            log.debug(tableTag + " extension positioning lookup table offset: " + eo );
        }
        // read referenced subtable from extended offset
        readGPOSSubtable ( in, lt, lookupFlags, lookupSequence, subtableSequence, subtableOffset + eo );
    }

    private int readExtensionPosTable(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read positioning subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readExtensionPosTableFormat1 ( in, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported extension positioning subtable format: " + sf );
        }
        return sf;
    }

    private void readGPOSSubtable(FontFileReader in, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, long subtableOffset) throws IOException {
        initSESubState();
        int subtableFormat = -1;
        switch ( lookupType ) {
        case GPOSLookupType.SINGLE:
            subtableFormat = readSinglePosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.PAIR:           
            subtableFormat = readPairPosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.CURSIVE:
            subtableFormat = readCursivePosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.MARK_TO_BASE:
            subtableFormat = readMarkToBasePosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.MARK_TO_LIGATURE:
            subtableFormat = readMarkToLigaturePosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.MARK_TO_MARK:
            subtableFormat = readMarkToMarkPosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.CONTEXTUAL:
            subtableFormat = readContextualPosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.CHAINED_CONTEXTUAL:
            subtableFormat = readChainedContextualPosTable ( in, lookupType, lookupFlags, subtableOffset );
            break;
        case GPOSLookupType.EXTENSION:
            subtableFormat = readExtensionPosTable ( in, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableOffset );
            break;
        default:
            break;
        }
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_POSITIONING, lookupType, lookupFlags, lookupSequence, subtableSequence, subtableFormat );
        resetSESubState();
    }

    private void readLookupTable(FontFileReader in, String tableTag, int lookupSequence, long lookupTable) throws IOException {
        boolean isGSUB = tableTag.equals ( "GSUB" );
        boolean isGPOS = tableTag.equals ( "GPOS" );
        in.seekSet(lookupTable);
        // read lookup type
        int lt = in.readTTFUShort();
        // read lookup flags
        int lf = in.readTTFUShort();
        // read sub-table count
        int ns = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            String lts;
            if ( isGSUB ) {
                lts = GSUBLookupType.toString ( lt );
            } else if ( isGPOS ) {
                lts = GPOSLookupType.toString ( lt );
            } else {
                lts = "?";
            }
            log.debug(tableTag + " lookup table type: " + lt + " (" + lts + ")" );
            log.debug(tableTag + " lookup table flags: " + lf + " (" + LookupFlag.toString ( lf ) + ")" );
            log.debug(tableTag + " lookup table subtable count: " + ns );
        }
        // read subtable offsets
        int[] soa = new int[ns];
        for ( int i = 0; i < ns; i++ ) {
            int so = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lookup table subtable offset: " + so );
            }
            soa[i] = so;
        }
        // read mark filtering set
        if ( ( lf & LookupFlag.USE_MARK_FILTERING_SET ) != 0 ) {
            // read mark filtering set
            int fs = in.readTTFUShort();
            // dump info if debugging
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " lookup table mark filter set: " + fs );
            }
        }
        // read subtables
        for ( int i = 0; i < ns; i++ ) {
            int so = soa[i];
            if ( isGSUB ) {
                readGSUBSubtable ( in, lt, lf, lookupSequence, i, lookupTable + so );
            } else if ( isGPOS ) {
                readGPOSSubtable ( in, lt, lf, lookupSequence, i, lookupTable + so );
            }
        }
    }

    private void readLookupList(FontFileReader in, String tableTag, long lookupList) throws IOException {
        in.seekSet(lookupList);
        // read lookup record count
        int nl = in.readTTFUShort();
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " lookup list record count: " + nl );
        }
        if ( nl > 0 ) {
            int[] loa = new int[nl];
            // read lookup records
            for ( int i = 0, n = nl; i < n; i++ ) {
                int lo = in.readTTFUShort();
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lookup table offset: " + lo );
                }
                loa[i] = lo;
            }
            // read lookup tables
            for ( int i = 0, n = nl; i < n; i++ ) {
                if (log.isDebugEnabled()) {
                    log.debug(tableTag + " lookup index: " + i );
                }
                readLookupTable ( in, tableTag, i, lookupList + loa [ i ] );
            }
        }
    }

    /**
     * Read the common layout tables (used by GSUB and GPOS).
     * @param in FontFileReader to read from
     * @param scriptList offset to script list from beginning of font file
     * @param featureList offset to feature list from beginning of font file
     * @param lookupList offset to lookup list from beginning of font file
     * @throws IOException In case of a I/O problem
     */
    private void readCommonLayoutTables(FontFileReader in, String tableTag, long scriptList, long featureList, long lookupList) throws IOException {
        if ( scriptList > 0 ) {
            readScriptList ( in, tableTag, scriptList );
        }
        if ( featureList > 0 ) {
            readFeatureList ( in, tableTag, featureList );
        }
        if ( lookupList > 0 ) {
            readLookupList ( in, tableTag, lookupList );
        }
    }

    private void readGDEFClassDefTable(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initSESubState();
        in.seekSet(subtableOffset);
        // subtable is a bare class definition table
        GlyphClassTable ct = readClassDefTable ( in, tableTag + " glyph class definition table", subtableOffset );
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.GLYPH_CLASS, 0, lookupSequence, 0, 1 );
        resetSESubState();
    }

    private void readGDEFAttachmentTable(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initSESubState();
        in.seekSet(subtableOffset);
        // read coverage offset
        int co = in.readTTFUShort();
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " attachment point coverage table offset: " + co );
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " attachment point coverage", subtableOffset + co );
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.ATTACHMENT_POINT, 0, lookupSequence, 0, 1 );
        resetSESubState();
    }

    private void readGDEFLigatureCaretTable(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initSESubState();
        in.seekSet(subtableOffset);
        // read coverage offset
        int co = in.readTTFUShort();
        // read ligature glyph count
        int nl = in.readTTFUShort();
        // read ligature glyph table offsets
        int[] lgto = new int [ nl ];
        for ( int i = 0; i < nl; i++ ) {
            lgto [ i ] = in.readTTFUShort();
        }
        
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " ligature caret coverage table offset: " + co );
            log.debug(tableTag + " ligature caret ligature glyph count: " + nl );
            for ( int i = 0; i < nl; i++ ) {
                log.debug(tableTag + " ligature glyph table offset[" + i + "]: " + lgto[i] );
            }
        }
        // read coverage table
        GlyphCoverageTable ct = readCoverageTable ( in, tableTag + " ligature caret coverage", subtableOffset + co );
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.LIGATURE_CARET, 0, lookupSequence, 0, 1 );
        resetSESubState();
    }

    private void readGDEFMarkAttachmentTable(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        initSESubState();
        in.seekSet(subtableOffset);
        // subtable is a bare class definition table
        GlyphClassTable ct = readClassDefTable ( in, tableTag + " glyph class definition table", subtableOffset );
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.MARK_ATTACHMENT, 0, lookupSequence, 0, 1 );
        resetSESubState();
    }

    private void readGDEFMarkGlyphsTableFormat1(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset, int subtableFormat) throws IOException {
        initSESubState();
        in.seekSet(subtableOffset);
        // skip over format (already known)
        in.skip ( 2 );
        // read mark set class count
        int nmc = in.readTTFUShort();
        long[] mso = new long [ nmc ];
        // read mark set coverage offsets
        for ( int i = 0; i < nmc; i++ ) {
            mso [ i ] = in.readTTFULong();
        }
        // dump info if debugging
        if (log.isDebugEnabled()) {
            log.debug(tableTag + " mark set subtable format: " + subtableFormat + " (glyph sets)" );
            log.debug(tableTag + " mark set class count: " + nmc );
            for ( int i = 0; i < nmc; i++ ) {
                log.debug(tableTag + " mark set coverage table offset[" + i + "]: " + mso[i] );
            }
        }
        // read mark set coverage tables, one per class
        GlyphCoverageTable[] msca = new GlyphCoverageTable[nmc];
        for ( int i = 0; i < nmc; i++ ) {
            msca[i] = readCoverageTable ( in, tableTag + " mark set coverage[" + i + "]", subtableOffset + mso[i] );
        }
        // create combined class table from per-class coverage tables
        GlyphClassTable ct = GlyphClassTable.createClassTable ( Arrays.asList ( msca ) );
        // store results
        seMapping = ct;
        // extract subtable
        extractSESubState ( GlyphTable.GLYPH_TABLE_TYPE_DEFINITION, GDEFLookupType.MARK_ATTACHMENT, 0, lookupSequence, 0, 1 );
        resetSESubState();
    }

    private void readGDEFMarkGlyphsTable(FontFileReader in, String tableTag, int lookupSequence, long subtableOffset) throws IOException {
        in.seekSet(subtableOffset);
        // read mark set subtable format
        int sf = in.readTTFUShort();
        if ( sf == 1 ) {
            readGDEFMarkGlyphsTableFormat1 ( in, tableTag, lookupSequence, subtableOffset, sf );
        } else {
            throw new UnsupportedOperationException ( "unsupported mark glyph sets subtable format: " + sf );
        }
    }

    /**
     * Read the GDEF table.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private void readGDEF(FontFileReader in) throws IOException {
        String tableTag = "GDEF";
        // Initialize temporary state
        initSEState();
        // Read glyph definition (GDEF) table
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get(tableTag);
        if ( gdef != null ) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + ": ignoring duplicate table");
            }
        } else if (dirTab != null) {
            seekTab(in, tableTag, 0);
            long version = in.readTTFULong();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " version: " + ( version / 65536 ) + "." + ( version % 65536 ));
            }
            // glyph class definition table offset (may be null)
            int cdo = in.readTTFUShort();
            // attach point list offset (may be null)
            int apo = in.readTTFUShort();
            // ligature caret list offset (may be null)
            int lco = in.readTTFUShort();
            // mark attach class definition table offset (may be null)
            int mao = in.readTTFUShort();
            // mark glyph sets definition table offset (may be null)
            int mgo;
            if ( version >= 0x00010002 ) {
                mgo = in.readTTFUShort();
            } else {
                mgo = 0;
            }
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " glyph class definition table offset: " + cdo );
                log.debug(tableTag + " attachment point list offset: " + apo );
                log.debug(tableTag + " ligature caret list offset: " + lco );
                log.debug(tableTag + " mark attachment class definition table offset: " + mao );
                log.debug(tableTag + " mark glyph set definitions table offset: " + mgo );
            }
            // initialize subtable sequence number
            int seqno = 0;
            // obtain offset to start of gdef table
            long to = dirTab.getOffset();
            // (optionally) read glyph class definition subtable
            if ( cdo != 0 ) {
                readGDEFClassDefTable ( in, tableTag, seqno++, to + cdo );
            }
            // (optionally) read glyph attachment point subtable
            if ( apo != 0 ) {
                readGDEFAttachmentTable ( in, tableTag, seqno++, to + apo );
            }
            // (optionally) read ligature caret subtable
            if ( lco != 0 ) {
                readGDEFLigatureCaretTable ( in, tableTag, seqno++, to + lco );
            }
            // (optionally) read mark attachment class subtable
            if ( mao != 0 ) {
                readGDEFMarkAttachmentTable ( in, tableTag, seqno++, to + mao );
            }
            // (optionally) read mark glyph sets subtable
            if ( mgo != 0 ) {
                readGDEFMarkGlyphsTable ( in, tableTag, seqno++, to + mgo );
            }
            GlyphDefinitionTable gdef;
            if ( ( gdef = constructGDEF() ) != null ) {
                this.gdef = gdef;
            }
        }
    }

    /**
     * Read the GSUB table.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private void readGSUB(FontFileReader in) throws IOException {
        String tableTag = "GSUB";
        // Initialize temporary state
        initSEState();
        // Read glyph substitution (GSUB) table
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get(tableTag);
        if ( gpos != null ) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + ": ignoring duplicate table");
            }
        } else if (dirTab != null) {
            seekTab(in, tableTag, 0);
            int version = in.readTTFLong();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " version: " + ( version / 65536 ) + "." + ( version % 65536 ));
            }
            int slo = in.readTTFUShort();
            int flo = in.readTTFUShort();
            int llo = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " script list offset: " + slo );
                log.debug(tableTag + " feature list offset: " + flo );
                log.debug(tableTag + " lookup list offset: " + llo );
            }
            long to = dirTab.getOffset();
            readCommonLayoutTables ( in, tableTag, to + slo, to + flo, to + llo );
            GlyphSubstitutionTable gsub;
            if ( ( gsub = constructGSUB() ) != null ) {
                this.gsub = gsub;
            }
        }
    }

    /**
     * Read the GPOS table.
     * @param in FontFileReader to read from
     * @throws IOException In case of a I/O problem
     */
    private void readGPOS(FontFileReader in) throws IOException {
        String tableTag = "GPOS";
        // Initialize temporary state
        initSEState();
        // Read glyph positioning (GPOS) table
        TTFDirTabEntry dirTab = (TTFDirTabEntry)dirTabs.get(tableTag);
        if ( gpos != null ) {
            if (log.isDebugEnabled()) {
                log.debug(tableTag + ": ignoring duplicate table");
            }
        } else if (dirTab != null) {
            seekTab(in, tableTag, 0);
            int version = in.readTTFLong();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " version: " + ( version / 65536 ) + "." + ( version % 65536 ));
            }
            int slo = in.readTTFUShort();
            int flo = in.readTTFUShort();
            int llo = in.readTTFUShort();
            if (log.isDebugEnabled()) {
                log.debug(tableTag + " script list offset: " + slo );
                log.debug(tableTag + " feature list offset: " + flo );
                log.debug(tableTag + " lookup list offset: " + llo );
            }
            long to = dirTab.getOffset();
            readCommonLayoutTables ( in, tableTag, to + slo, to + flo, to + llo );
            GlyphPositioningTable gpos;
            if ( ( gpos = constructGPOS() ) != null ) {
                this.gpos = gpos;
            }
        }
    }

    /**
     * Construct the (internal representation of the) GDEF table based on previously
     * parsed state.
     * @returns glyph definition table or null if insufficient or invalid state
     */
    private GlyphDefinitionTable constructGDEF() {
        GlyphDefinitionTable gdef = null;
        List subtables;
        if ( ( subtables = constructGDEFSubtables() ) != null ) {
            if ( subtables.size() > 0 ) {
                gdef = new GlyphDefinitionTable ( subtables );
            }
        }
        resetSEState();
        return gdef;
    }

    /**
     * Construct the (internal representation of the) GSUB table based on previously
     * parsed state.
     * @returns glyph substitution table or null if insufficient or invalid state
     */
    private GlyphSubstitutionTable constructGSUB() {
        GlyphSubstitutionTable gsub = null;
        Map lookups;
        if ( ( lookups = constructLookups() ) != null ) {
            List subtables;
            if ( ( subtables = constructGSUBSubtables() ) != null ) {
                if ( ( lookups.size() > 0 ) && ( subtables.size() > 0 ) ) {
                    gsub = new GlyphSubstitutionTable ( gdef, lookups, subtables );
                }
            }
        }
        resetSEState();
        return gsub;
    }

    /**
     * Construct the (internal representation of the) GPOS table based on previously
     * parsed state.
     * @returns glyph positioning table or null if insufficient or invalid state
     */
    private GlyphPositioningTable constructGPOS() {
        GlyphPositioningTable gpos = null;
        Map lookups;
        if ( ( lookups = constructLookups() ) != null ) {
            List subtables;
            if ( ( subtables = constructGPOSSubtables() ) != null ) {
                if ( ( lookups.size() > 0 ) && ( subtables.size() > 0 ) ) {
                    gpos = new GlyphPositioningTable ( gdef, lookups, subtables );
                }
            }
        }
        resetSEState();
        return gpos;
    }

    private void constructLookupsFeature ( Map lookups, String st, String lt, String fid ) {
        Object[] fp = (Object[]) seFeatures.get ( fid );
        if ( fp != null ) {
            assert fp.length == 2;
            String ft = (String) fp[0];                 // feature tag
            List/*<String>*/ lul = (List) fp[1];        // list of lookup table ids
            if ( ( ft != null ) && ( lul != null ) && ( lul.size() > 0 ) ) {
                GlyphTable.LookupSpec ls = new GlyphTable.LookupSpec ( st, lt, ft );
                lookups.put ( ls, lul );
            }
        }
    }

    private void constructLookupsFeatures ( Map lookups, String st, String lt, List/*<String>*/ fids ) {
        for ( Iterator fit = fids.iterator(); fit.hasNext();) {
            String fid = (String) fit.next();
            constructLookupsFeature ( lookups, st, lt, fid );
        }
    }

    private void constructLookupsLanguage ( Map lookups, String st, String lt, Map/*<String,Object[2]>*/ languages ) {
        Object[] lp = (Object[]) languages.get ( lt );
        if ( lp != null ) {
            assert lp.length == 2;
            if ( lp[0] != null ) {                      // required feature id
                constructLookupsFeature ( lookups, st, lt, (String) lp[0] );
            }
            if ( lp[1] != null ) {                      // non-required features ids
                constructLookupsFeatures ( lookups, st, lt, (List) lp[1] );
            }
        }
    }

    private void constructLookupsLanguages ( Map lookups, String st, List/*<String>*/ ll, Map/*<String,Object[2]>*/ languages ) {
        for ( Iterator lit = ll.iterator(); lit.hasNext();) {
            String lt = (String) lit.next();
            constructLookupsLanguage ( lookups, st, lt, languages );
        }
    }

    private Map constructLookups() {
        Map/*<GlyphTable.LookupSpec,List<String>>*/ lookups = new java.util.LinkedHashMap();
        for ( Iterator sit = seScripts.keySet().iterator(); sit.hasNext();) {
            String st = (String) sit.next();
            Object[] sp = (Object[]) seScripts.get ( st );
            if ( sp != null ) {
                assert sp.length == 3;
                Map/*<String,Object[2]>*/ languages = (Map) sp[2];
                if ( sp[0] != null ) {                  // default language
                    constructLookupsLanguage ( lookups, st, (String) sp[0], languages );
                }
                if ( sp[1] != null ) {                  // non-default languages
                    constructLookupsLanguages ( lookups, st, (List) sp[1], languages );
                }
            }
        }
        return lookups;
    }

    private List constructGDEFSubtables() {
        List/*<GlyphDefinitionSubtable>*/ subtables = new java.util.ArrayList();
        if ( seSubtables != null ) {
            for ( Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ( ( st = constructGDEFSubtable ( stp ) ) != null ) {
                    subtables.add ( st );
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGDEFSubtable ( Object[] stp ) {
        GlyphSubtable st = null;
        assert ( stp != null ) && ( stp.length == 8 );
        Integer tt = (Integer) stp[0];
        Integer lt = (Integer) stp[1];
        Integer ln = (Integer) stp[2];
        Integer lf = (Integer) stp[3];
        Integer sn = (Integer) stp[4];
        Integer sf = (Integer) stp[5];
        GlyphMappingTable mapping = (GlyphMappingTable) stp[6];
        List entries = (List) stp[7];
        if ( tt.intValue() == GlyphTable.GLYPH_TABLE_TYPE_DEFINITION ) {
            int type = GDEFLookupType.getSubtableType ( lt.intValue() );
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphDefinitionTable.createSubtable ( type, lid, sequence, flags, format, mapping, entries );
        }
        return st;
    }

    private List constructGSUBSubtables() {
        List/*<GlyphSubtable>*/ subtables = new java.util.ArrayList();
        if ( seSubtables != null ) {
            for ( Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ( ( st = constructGSUBSubtable ( stp ) ) != null ) {
                    subtables.add ( st );
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGSUBSubtable ( Object[] stp ) {
        GlyphSubtable st = null;
        assert ( stp != null ) && ( stp.length == 8 );
        Integer tt = (Integer) stp[0];
        Integer lt = (Integer) stp[1];
        Integer ln = (Integer) stp[2];
        Integer lf = (Integer) stp[3];
        Integer sn = (Integer) stp[4];
        Integer sf = (Integer) stp[5];
        GlyphCoverageTable coverage = (GlyphCoverageTable) stp[6];
        List entries = (List) stp[7];
        if ( tt.intValue() == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION ) {
            int type = GSUBLookupType.getSubtableType ( lt.intValue() );
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphSubstitutionTable.createSubtable ( type, lid, sequence, flags, format, coverage, entries );
        }
        return st;
    }

    private List constructGPOSSubtables() {
        List/*<GlyphSubtable>*/ subtables = new java.util.ArrayList();
        if ( seSubtables != null ) {
            for ( Iterator it = seSubtables.iterator(); it.hasNext();) {
                Object[] stp = (Object[]) it.next();
                GlyphSubtable st;
                if ( ( st = constructGPOSSubtable ( stp ) ) != null ) {
                    subtables.add ( st );
                }
            }
        }
        return subtables;
    }

    private GlyphSubtable constructGPOSSubtable ( Object[] stp ) {
        GlyphSubtable st = null;
        assert ( stp != null ) && ( stp.length == 8 );
        Integer tt = (Integer) stp[0];
        Integer lt = (Integer) stp[1];
        Integer ln = (Integer) stp[2];
        Integer lf = (Integer) stp[3];
        Integer sn = (Integer) stp[4];
        Integer sf = (Integer) stp[5];
        GlyphCoverageTable coverage = (GlyphCoverageTable) stp[6];
        List entries = (List) stp[7];
        if ( tt.intValue() == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING ) {
            int type = GSUBLookupType.getSubtableType ( lt.intValue() );
            String lid = "lu" + ln.intValue();
            int sequence = sn.intValue();
            int flags = lf.intValue();
            int format = sf.intValue();
            st = GlyphPositioningTable.createSubtable ( type, lid, sequence, flags, format, coverage, entries );
        }
        return st;
    }

    private void initSEState() {
        seScripts = new java.util.LinkedHashMap();
        seLanguages = new java.util.LinkedHashMap();
        seFeatures = new java.util.LinkedHashMap();
        seSubtables = new java.util.ArrayList();
        resetSESubState();
    }

    private void resetSEState() {
        seScripts = null;
        seLanguages = null;
        seFeatures = null;
        seSubtables = null;
        resetSESubState();
    }

    private void initSESubState() {
        seMapping = null;
        seEntries = new java.util.ArrayList();
    }

    private void extractSESubState ( int tableType, int lookupType, int lookupFlags, int lookupSequence, int subtableSequence, int subtableFormat ) {
        if ( seEntries != null ) {
            if ( ( tableType == GlyphTable.GLYPH_TABLE_TYPE_DEFINITION ) || ( seEntries.size() > 0 ) ) {
                if ( seSubtables != null ) {
                    Integer tt = Integer.valueOf ( tableType );
                    Integer lt = Integer.valueOf ( lookupType );
                    Integer ln = Integer.valueOf ( lookupSequence );
                    Integer lf = Integer.valueOf ( lookupFlags );
                    Integer sn = Integer.valueOf ( subtableSequence );
                    Integer sf = Integer.valueOf ( subtableFormat );
                    seSubtables.add ( new Object[] { tt, lt, ln, lf, sn, sf, seMapping, seEntries } );
                }
            }
        }
    }

    private void resetSESubState() {
        seMapping = null;
        seEntries = null;
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
                familyNames.clear();
                postScriptName = "";
                subFamilyName = "";
            }

            in.seekSet(dirTabOffset);
            return found;
        } else {
            in.seekSet(0);
            return true;
        }
    }

    /**
     * Return TTC font names
     * @param in FontFileReader to read from
     * @return True if not collection or font name present, false otherwise
     * @throws IOException In case of an I/O problem
     */
    public final List<String> getTTCnames(FontFileReader in) throws IOException {
        List<String> fontNames = new java.util.ArrayList<String>();

        String tag = in.readTTFString(4);

        if ("ttcf".equals(tag)) {
            // This is a TrueType Collection
            in.skip(4);

            // Read directory offsets
            int numDirectories = (int)in.readTTFULong();
            long[] dirOffsets = new long[numDirectories];
            for (int i = 0; i < numDirectories; i++) {
                dirOffsets[i] = in.readTTFULong();
            }

            if (log.isDebugEnabled()) {
                log.debug("This is a TrueType collection file with "
                        + numDirectories + " fonts");
                log.debug("Containing the following fonts: ");
            }

            for (int i = 0; (i < numDirectories); i++) {
                in.seekSet(dirOffsets[i]);
                readDirTabs(in);

                readName(in);

                log.debug(fullName);
                fontNames.add(fullName);

                // Reset names
                notice = "";
                fullName = "";
                familyNames.clear();
                postScriptName = "";
                subFamilyName = "";
            }

            in.seekSet(0);
            return fontNames;
        } else {
            log.error("Not a TTC!");
            return null;
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
        System.out.println("Font name:   " + postScriptName);
        System.out.println("Full name:   " + fullName);
        System.out.println("Family name: " + familyNames);
        System.out.println("Subfamily name: " + subFamilyName);
        System.out.println("Notice:      " + notice);
        System.out.println("xHeight:     " + convertTTFUnit2PDFUnit(xHeight));
        System.out.println("capheight:   " + convertTTFUnit2PDFUnit(capHeight));

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
        System.out.println("FontBBox:    [" + convertTTFUnit2PDFUnit(fontBBox1)
                           + " " + convertTTFUnit2PDFUnit(fontBBox2) + " "
                           + convertTTFUnit2PDFUnit(fontBBox3) + " "
                           + convertTTFUnit2PDFUnit(fontBBox4) + "]");
    }

    private String formatUnitsForDebug(int units) {
        return units + " -> " + convertTTFUnit2PDFUnit(units) + " internal units";
    }

    /**
     * Map a glyph index to the corresponding unicode code point
     *
     * @param glyphIndex
     * @return unicode code point
     */
    private Integer glyphToUnicode(int glyphIndex) {
        return (Integer) glyphToUnicodeMap.get(new Integer(glyphIndex));
    }

    /**
     * Map a unicode code point to the corresponding glyph index
     *
     * @param unicodeIndex unicode code point
     * @return glyph index
     */
    private Integer unicodeToGlyph(int unicodeIndex) throws IOException {
        final Integer result
            = (Integer) unicodeToGlyphMap.get(new Integer(unicodeIndex));
        if (result == null) {
            throw new IOException(
                    "Glyph index not found for unicode value " + unicodeIndex);
        }
        return result;
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
