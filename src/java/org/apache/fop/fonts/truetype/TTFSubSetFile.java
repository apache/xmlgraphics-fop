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
import java.util.List;
import java.util.Map;


/**
 * Reads a TrueType file and generates a subset
 * that can be used to embed a TrueType CID font.
 * TrueType tables needed for embedded CID fonts are:
 * "head", "hhea", "loca", "maxp", "cvt ", "prep", "glyf", "hmtx" and "fpgm".
 * The TrueType spec can be found at the Microsoft
 * Typography site: http://www.microsoft.com/truetype/
 */
public class TTFSubSetFile extends TTFFile {

    private static enum OperatingMode {
        PDF, POSTSCRIPT_GLYPH_DIRECTORY;
    }

    private byte[] output = null;
    private int realSize = 0;
    private int currentPos = 0;

    /*
     * Offsets in name table to be filled out by table.
     * The offsets are to the checkSum field
     */
    private Map<String, Integer> offsets = new java.util.HashMap<String, Integer>();
    private int glyfDirOffset = 0;
    private int headDirOffset = 0;
    private int hmtxDirOffset = 0;
    private int locaDirOffset = 0;
    private int maxpDirOffset = 0;

    private int checkSumAdjustmentOffset = 0;
    private int locaOffset = 0;

    private int determineTableCount(OperatingMode operatingMode) {
        int numTables = 4; //4 req'd tables: head,hhea,hmtx,maxp
        if (isCFF()) {
            throw new UnsupportedOperationException(
                    "OpenType fonts with CFF glyphs are not supported");
        } else {
            if (operatingMode == OperatingMode.POSTSCRIPT_GLYPH_DIRECTORY) {
                numTables++; //1 table: gdir
            } else {
                numTables += 2; //2 req'd tables: glyf,loca
            }
            if (hasCvt()) {
                numTables++;
            }
            if (hasFpgm()) {
                numTables++;
            }
            if (hasPrep()) {
                numTables++;
            }
        }
        return numTables;
    }

    /**
     * Create the directory table
     */
    private void createDirectory(OperatingMode operatingMode) {
        int numTables = determineTableCount(operatingMode);
        // Create the TrueType header
        writeByte((byte)0);
        writeByte((byte)1);
        writeByte((byte)0);
        writeByte((byte)0);
        realSize += 4;

        writeUShort(numTables);
        realSize += 2;

        // Create searchRange, entrySelector and rangeShift
        int maxPow = maxPow2(numTables);
        int searchRange = maxPow * 16;
        writeUShort(searchRange);
        realSize += 2;

        writeUShort(maxPow);
        realSize += 2;

        writeUShort((numTables * 16) - searchRange);
        realSize += 2;

        // Create space for the table entries
        if (hasCvt()) {
            writeString("cvt ");
            offsets.put("cvt ", currentPos);
            currentPos += 12;
            realSize += 16;
        }

        if (hasFpgm()) {
            writeString("fpgm");
            offsets.put("fpgm", currentPos);
            currentPos += 12;
            realSize += 16;
        }

        if (operatingMode != OperatingMode.POSTSCRIPT_GLYPH_DIRECTORY) {
            writeString("glyf");
            glyfDirOffset = currentPos;
            currentPos += 12;
            realSize += 16;
        }

        writeString("head");
        headDirOffset = currentPos;
        currentPos += 12;
        realSize += 16;

        writeString("hhea");
        offsets.put("hhea", currentPos);
        currentPos += 12;
        realSize += 16;

        writeString("hmtx");
        hmtxDirOffset = currentPos;
        currentPos += 12;
        realSize += 16;

        if (operatingMode != OperatingMode.POSTSCRIPT_GLYPH_DIRECTORY) {
            writeString("loca");
            locaDirOffset = currentPos;
            currentPos += 12;
            realSize += 16;
        }

        writeString("maxp");
        maxpDirOffset = currentPos;
        currentPos += 12;
        realSize += 16;

        if (hasPrep()) {
            writeString("prep");
            offsets.put("prep", currentPos);
            currentPos += 12;
            realSize += 16;
        }

        if (operatingMode == OperatingMode.POSTSCRIPT_GLYPH_DIRECTORY) {
            //"gdir" indicates to the PostScript interpreter that the GlyphDirectory approach
            //is in use.
            writeString("gdir");
            currentPos += 12;
            realSize += 16;
        }
    }


    private boolean hasCvt() {
        return dirTabs.containsKey("cvt ");
    }

    private boolean hasFpgm() {
        return dirTabs.containsKey("fpgm");
    }

    private boolean hasPrep() {
        return dirTabs.containsKey("prep");
    }

    /**
     * Create an empty loca table without updating checksum
     */
    private void createLoca(int size) throws IOException {
        pad4();
        locaOffset = currentPos;
        writeULong(locaDirOffset + 4, currentPos);
        writeULong(locaDirOffset + 8, size * 4 + 4);
        currentPos += size * 4 + 4;
        realSize += size * 4 + 4;
    }

    private boolean copyTable(FontFileReader in, String tableName) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get(tableName);
        if (entry != null) {
            pad4();
            seekTab(in, tableName, 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());
            int checksum = getCheckSum(currentPos, (int)entry.getLength());
            int offset = offsets.get(tableName);
            writeULong(offset, checksum);
            writeULong(offset + 4, currentPos);
            writeULong(offset + 8, (int)entry.getLength());
            currentPos += (int)entry.getLength();
            realSize += (int)entry.getLength();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Copy the cvt table as is from original font to subset font
     */
    private boolean createCvt(FontFileReader in) throws IOException {
        return copyTable(in, "cvt ");
    }

    /**
     * Copy the fpgm table as is from original font to subset font
     */
    private boolean createFpgm(FontFileReader in) throws IOException {
        return copyTable(in, "fpgm");
    }

    /**
     * Copy the maxp table as is from original font to subset font
     * and set num glyphs to size
     */
    private void createMaxp(FontFileReader in, int size) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("maxp");
        if (entry != null) {
            pad4();
            seekTab(in, "maxp", 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());
            writeUShort(currentPos + 4, size);

            int checksum = getCheckSum(currentPos, (int)entry.getLength());
            writeULong(maxpDirOffset, checksum);
            writeULong(maxpDirOffset + 4, currentPos);
            writeULong(maxpDirOffset + 8, (int)entry.getLength());
            currentPos += (int)entry.getLength();
            realSize += (int)entry.getLength();
        } else {
            throw new IOException("Can't find maxp table");
        }
    }


    /**
     * Copy the prep table as is from original font to subset font
     */
    private boolean createPrep(FontFileReader in) throws IOException {
        return copyTable(in, "prep");
    }


    /**
     * Copy the hhea table as is from original font to subset font
     * and fill in size of hmtx table
     */
    private void createHhea(FontFileReader in, int size) throws IOException {
        boolean copied = copyTable(in, "hhea");
        if (!copied) {
            throw new IOException("Can't find hhea table");
        }
    }


    /**
     * Copy the head table as is from original font to subset font
     * and set indexToLocaFormat to long and set
     * checkSumAdjustment to 0, store offset to checkSumAdjustment
     * in checkSumAdjustmentOffset
     */
    private void createHead(FontFileReader in) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("head");
        if (entry != null) {
            pad4();
            seekTab(in, "head", 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());

            checkSumAdjustmentOffset = currentPos + 8;
            output[currentPos + 8] = 0;     // Set checkSumAdjustment to 0
            output[currentPos + 9] = 0;
            output[currentPos + 10] = 0;
            output[currentPos + 11] = 0;
            output[currentPos + 50] = 0;    // long locaformat
            output[currentPos + 51] = 1;    // long locaformat

            int checksum = getCheckSum(currentPos, (int)entry.getLength());
            writeULong(headDirOffset, checksum);
            writeULong(headDirOffset + 4, currentPos);
            writeULong(headDirOffset + 8, (int)entry.getLength());

            currentPos += (int)entry.getLength();
            realSize += (int)entry.getLength();
        } else {
            throw new IOException("Can't find head table");
        }
    }


    /**
     * Create the glyf table and fill in loca table
     */
    private void createGlyf(FontFileReader in,
                            Map<Integer, Integer> glyphs) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("glyf");
        int size = 0;
        int startPos = 0;
        int endOffset = 0;    // Store this as the last loca
        if (entry != null) {
            pad4();
            startPos = currentPos;

            /* Loca table must be in order by glyph index, so build
             * an array first and then write the glyph info and
             * location offset.
             */
            int[] origIndexes = buildSubsetIndexToOrigIndexMap(glyphs);

            for (int i = 0; i < origIndexes.length; i++) {
                int nextOffset = 0;
                int origGlyphIndex = origIndexes[i];
                if (origGlyphIndex >= (mtxTab.length - 1)) {
                    nextOffset = (int)lastLoca;
                } else {
                    nextOffset = (int)mtxTab[origGlyphIndex + 1].getOffset();
                }
                int glyphOffset = (int)mtxTab[origGlyphIndex].getOffset();
                int glyphLength = nextOffset - glyphOffset;

                byte[] glyphData = in.getBytes(
                        (int)entry.getOffset() + glyphOffset,
                        glyphLength);
                int endOffset1 = endOffset;
                // Copy glyph
                System.arraycopy(
                    glyphData, 0,
                    output, currentPos,
                    glyphLength);


                // Update loca table
                writeULong(locaOffset + i * 4, currentPos - startPos);
                if ((currentPos - startPos + glyphLength) > endOffset1) {
                    endOffset1 = (currentPos - startPos + glyphLength);
                }

                currentPos += glyphLength;
                realSize += glyphLength;

                endOffset = endOffset1;

            }

            size = currentPos - startPos;

            int checksum = getCheckSum(startPos, size);
            writeULong(glyfDirOffset, checksum);
            writeULong(glyfDirOffset + 4, startPos);
            writeULong(glyfDirOffset + 8, size);
            currentPos += 12;
            realSize += 12;

            // Update loca checksum and last loca index
            writeULong(locaOffset + glyphs.size() * 4, endOffset);

            checksum = getCheckSum(locaOffset, glyphs.size() * 4 + 4);
            writeULong(locaDirOffset, checksum);
        } else {
            throw new IOException("Can't find glyf table");
        }
    }

    private int[] buildSubsetIndexToOrigIndexMap(Map<Integer, Integer> glyphs) {
        int[] origIndexes = new int[glyphs.size()];
        for (Map.Entry<Integer, Integer> glyph : glyphs.entrySet()) {
            int origIndex = glyph.getKey();
            int subsetIndex = glyph.getValue();
            origIndexes[subsetIndex] = origIndex;
        }
        return origIndexes;
    }

    /**
     * Create the hmtx table by copying metrics from original
     * font to subset font. The glyphs Map contains an
     * Integer key and Integer value that maps the original
     * metric (key) to the subset metric (value)
     */
    private void createHmtx(FontFileReader in,
                            Map<Integer, Integer> glyphs) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("hmtx");

        int longHorMetricSize = glyphs.size() * 2;
        int leftSideBearingSize = glyphs.size() * 2;
        int hmtxSize = longHorMetricSize + leftSideBearingSize;

        if (entry != null) {
            pad4();
            //int offset = (int)entry.offset;
            for (Map.Entry<Integer, Integer> glyph : glyphs.entrySet()) {
                Integer origIndex = glyph.getKey();
                Integer subsetIndex = glyph.getValue();

                writeUShort(currentPos + subsetIndex.intValue() * 4,
                            mtxTab[origIndex.intValue()].getWx());
                writeUShort(currentPos + subsetIndex.intValue() * 4 + 2,
                            mtxTab[origIndex.intValue()].getLsb());
            }

            int checksum = getCheckSum(currentPos, hmtxSize);
            writeULong(hmtxDirOffset, checksum);
            writeULong(hmtxDirOffset + 4, currentPos);
            writeULong(hmtxDirOffset + 8, hmtxSize);
            currentPos += hmtxSize;
            realSize += hmtxSize;
        } else {
            throw new IOException("Can't find hmtx table");
        }
    }

    /**
     * Returns a List containing the glyph itself plus all glyphs
     * that this composite glyph uses
     */
    private List<Integer> getIncludedGlyphs(FontFileReader in, int glyphOffset,
                                     Integer glyphIdx) throws IOException {
        List<Integer> ret = new java.util.ArrayList<Integer>();
        ret.add(glyphIdx);
        int offset = glyphOffset + (int)mtxTab[glyphIdx.intValue()].getOffset() + 10;
        Integer compositeIdx = null;
        int flags = 0;
        boolean moreComposites = true;
        while (moreComposites) {
            flags = in.readTTFUShort(offset);
            compositeIdx = Integer.valueOf(in.readTTFUShort(offset + 2));
            ret.add(compositeIdx);

            offset += 4;
            if ((flags & 1) > 0) {
                // ARG_1_AND_ARG_2_ARE_WORDS
                offset += 4;
            } else {
                offset += 2;
            }

            if ((flags & 8) > 0) {
                offset += 2;    // WE_HAVE_A_SCALE
            } else if ((flags & 64) > 0) {
                offset += 4;    // WE_HAVE_AN_X_AND_Y_SCALE
            } else if ((flags & 128) > 0) {
                offset += 8;    // WE_HAVE_A_TWO_BY_TWO
            }

            if ((flags & 32) > 0) {
                moreComposites = true;
            } else {
                moreComposites = false;
            }
        }

        return ret;
    }


    /**
     * Rewrite all compositepointers in glyphindex glyphIdx
     *
     */
    private void remapComposite(FontFileReader in, Map<Integer, Integer> glyphs,
                                int glyphOffset,
                                Integer glyphIdx) throws IOException {
        int offset = glyphOffset + (int)mtxTab[glyphIdx.intValue()].getOffset()
                     + 10;

        Integer compositeIdx = null;
        int flags = 0;
        boolean moreComposites = true;

        while (moreComposites) {
            flags = in.readTTFUShort(offset);
            compositeIdx = Integer.valueOf(in.readTTFUShort(offset + 2));
            Integer newIdx = glyphs.get(compositeIdx);
            if (newIdx == null) {
                // This errormessage would look much better
                // if the fontname was printed to
                //log.error("An embedded font "
                //                     + "contains bad glyph data. "
                //                     + "Characters might not display "
                //                     + "correctly.");
                moreComposites = false;
                continue;
            }

            in.writeTTFUShort(offset + 2, newIdx.intValue());

            offset += 4;

            if ((flags & 1) > 0) {
                // ARG_1_AND_ARG_2_ARE_WORDS
                offset += 4;
            } else {
                offset += 2;
            }

            if ((flags & 8) > 0) {
                offset += 2;    // WE_HAVE_A_SCALE
            } else if ((flags & 64) > 0) {
                offset += 4;    // WE_HAVE_AN_X_AND_Y_SCALE
            } else if ((flags & 128) > 0) {
                offset += 8;    // WE_HAVE_A_TWO_BY_TWO
            }

            if ((flags & 32) > 0) {
                moreComposites = true;
            } else {
                moreComposites = false;
            }
        }
    }


    /**
     * Scan all the original glyphs for composite glyphs and add those glyphs
     * to the glyphmapping also rewrite the composite glyph pointers to the new
     * mapping
     */
    private void scanGlyphs(FontFileReader in,
                            Map<Integer, Integer> glyphs) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("glyf");
        Map<Integer, Integer> newComposites = null;
        Map<Integer, Integer> allComposites = new java.util.HashMap<Integer, Integer>();

        int newIndex = glyphs.size();

        if (entry != null) {
            while (newComposites == null || newComposites.size() > 0) {
                // Inefficient to iterate through all glyphs
                newComposites = new java.util.HashMap<Integer, Integer>();

                for (Map.Entry<Integer, Integer> glyph : glyphs.entrySet()) {
                    int origIndex = glyph.getKey();
                    if (in.readTTFShort(entry.getOffset()
                                        + mtxTab[origIndex].getOffset()) < 0) {
                        // origIndex is a composite glyph
                        allComposites.put(origIndex, glyph.getValue());
                        List<Integer> composites
                            = getIncludedGlyphs(in, (int)entry.getOffset(),
                                              origIndex);

                        // Iterate through all composites pointed to
                        // by this composite and check if they exists
                        // in the glyphs map, add them if not.
                        for (Integer cIdx : composites) {
                            if (glyphs.get(cIdx) == null
                                    && newComposites.get(cIdx) == null) {
                                newComposites.put(cIdx, newIndex);
                                newIndex++;
                            }
                        }
                    }
                }

                // Add composites to glyphs
                for (Map.Entry<Integer, Integer> im : newComposites.entrySet()) {
                    glyphs.put(im.getKey(), im.getValue());
                }
            }

            // Iterate through all composites to remap their composite index
            for (Integer idx : allComposites.keySet()) {
                remapComposite(in, glyphs, (int)entry.getOffset(),
                               idx);
            }

        } else {
            throw new IOException("Can't find glyf table");
        }
    }



    /**
     * Returns a subset of the original font.
     *
     * @param in FontFileReader to read from
     * @param name Name to be checked for in the font file
     * @param glyphs Map of glyphs (glyphs has old index as (Integer) key and
     * new index as (Integer) value)
     * @return A subset of the original font
     * @throws IOException in case of an I/O problem
     */
    public byte[] readFont(FontFileReader in, String name,
                           Map<Integer, Integer> glyphs) throws IOException {

        //Check if TrueType collection, and that the name exists in the collection
        if (!checkTTC(in, name)) {
            throw new IOException("Failed to read font");
        }

        //Copy the Map as we're going to modify it
        Map<Integer, Integer> subsetGlyphs = new java.util.HashMap<Integer, Integer>(glyphs);

        output = new byte[in.getFileSize()];

        readDirTabs(in);
        readFontHeader(in);
        getNumGlyphs(in);
        readHorizontalHeader(in);
        readHorizontalMetrics(in);
        readIndexToLocation(in);

        scanGlyphs(in, subsetGlyphs);

        createDirectory(OperatingMode.PDF);     // Create the TrueType header and directory

        createHead(in);
        createHhea(in, subsetGlyphs.size());    // Create the hhea table
        createHmtx(in, subsetGlyphs);           // Create hmtx table
        createMaxp(in, subsetGlyphs.size());    // copy the maxp table

        boolean optionalTableFound;
        optionalTableFound = createCvt(in);    // copy the cvt table
        if (!optionalTableFound) {
            // cvt is optional (used in TrueType fonts only)
            log.debug("TrueType: ctv table not present. Skipped.");
        }

        optionalTableFound = createFpgm(in);    // copy fpgm table
        if (!optionalTableFound) {
            // fpgm is optional (used in TrueType fonts only)
            log.debug("TrueType: fpgm table not present. Skipped.");
        }

        optionalTableFound = createPrep(in);    // copy prep table
        if (!optionalTableFound) {
            // prep is optional (used in TrueType fonts only)
            log.debug("TrueType: prep table not present. Skipped.");
        }

        createLoca(subsetGlyphs.size());    // create empty loca table
        createGlyf(in, subsetGlyphs);       //create glyf table and update loca table

        pad4();
        createCheckSumAdjustment();

        byte[] ret = new byte[realSize];
        System.arraycopy(output, 0, ret, 0, realSize);

        return ret;
    }

    /**
     * Returns a subset of the original font suitable for use in PostScript programs.
     *
     * @param in FontFileReader to read from
     * @param name Name to be checked for in the font file
     * @param glyphs Map of glyphs (glyphs has old index as (Integer) key and
     * new index as (Integer) value)
     * @param glyphHandler the handler to receive all glyphs of the subset
     * @return A subset of the original font
     * @throws IOException in case of an I/O problem
     */
    public byte[] toPostScriptSubset(FontFileReader in, String name,
                           Map glyphs, GlyphHandler glyphHandler) throws IOException {

        //Check if TrueType collection, and that the name exists in the collection
        if (!checkTTC(in, name)) {
            throw new IOException("Failed to read font");
        }

        //Copy the Map as we're going to modify it
        Map<Integer, Integer> subsetGlyphs = new java.util.HashMap(glyphs);

        output = new byte[in.getFileSize()];

        readDirTabs(in);
        readFontHeader(in);
        getNumGlyphs(in);
        readHorizontalHeader(in);
        readHorizontalMetrics(in);
        readIndexToLocation(in);

        scanGlyphs(in, subsetGlyphs);

        // Create the TrueType header and directory
        createDirectory(OperatingMode.POSTSCRIPT_GLYPH_DIRECTORY);

        createHead(in);
        createHhea(in, subsetGlyphs.size());    // Create the hhea table
        createHmtx(in, subsetGlyphs);           // Create hmtx table
        createMaxp(in, subsetGlyphs.size());    // copy the maxp table

        boolean optionalTableFound;
        optionalTableFound = createCvt(in);    // copy the cvt table
        if (!optionalTableFound) {
            // cvt is optional (used in TrueType fonts only)
            log.debug("TrueType: ctv table not present. Skipped.");
        }

        optionalTableFound = createFpgm(in);    // copy fpgm table
        if (!optionalTableFound) {
            // fpgm is optional (used in TrueType fonts only)
            log.debug("TrueType: fpgm table not present. Skipped.");
        }

        optionalTableFound = createPrep(in);    // copy prep table
        if (!optionalTableFound) {
            // prep is optional (used in TrueType fonts only)
            log.debug("TrueType: prep table not present. Skipped.");
        }

        //Send all the glyphs from the subset
        handleGlyphSubset(in, subsetGlyphs, glyphHandler);

        pad4();
        createCheckSumAdjustment();

        byte[] ret = new byte[realSize];
        System.arraycopy(output, 0, ret, 0, realSize);

        return ret;
    }

    private void handleGlyphSubset(FontFileReader in, Map<Integer, Integer> glyphs,
            GlyphHandler glyphHandler) throws IOException {
        TTFDirTabEntry entry = (TTFDirTabEntry)dirTabs.get("glyf");
        if (entry != null) {

            int[] origIndexes = buildSubsetIndexToOrigIndexMap(glyphs);

            for (int i = 0; i < origIndexes.length; i++) {
                int nextOffset = 0;
                int origGlyphIndex = origIndexes[i];
                if (origGlyphIndex >= (mtxTab.length - 1)) {
                    nextOffset = (int)lastLoca;
                } else {
                    nextOffset = (int)mtxTab[origGlyphIndex + 1].getOffset();
                }
                int glyphOffset = (int)mtxTab[origGlyphIndex].getOffset();
                int glyphLength = nextOffset - glyphOffset;

                byte[] glyphData = in.getBytes(
                        (int)entry.getOffset() + glyphOffset,
                        glyphLength);

                glyphHandler.addGlyph(glyphData);
            }
        } else {
            throw new IOException("Can't find glyf table");
        }
    }

    /**
     * Used as callback to handle a number of glyphs.
     */
    public static interface GlyphHandler {

        /**
         * Adds a glyph.
         * @param glyphData the glyph data
         * @throws IOException if an I/O error occurs
         */
        void addGlyph(byte[] glyphData) throws IOException;

    }

    /**
     * writes a ISO-8859-1 string at the currentPosition
     * updates currentPosition but not realSize
     * @return number of bytes written
     */
    private int writeString(String str) {
        int length = 0;
        try {
            byte[] buf = str.getBytes("ISO-8859-1");
            System.arraycopy(buf, 0, output, currentPos, buf.length);
            length = buf.length;
            currentPos += length;
        } catch (java.io.UnsupportedEncodingException e) {
            // This should never happen!
        }

        return length;
    }

    /**
     * Appends a byte to the output array,
     * updates currentPost but not realSize
     */
    private void writeByte(byte b) {
        output[currentPos++] = b;
    }

    /**
     * Appends a USHORT to the output array,
     * updates currentPost but not realSize
     */
    private void writeUShort(int s) {
        byte b1 = (byte)((s >> 8) & 0xff);
        byte b2 = (byte)(s & 0xff);
        writeByte(b1);
        writeByte(b2);
    }

    /**
     * Appends a USHORT to the output array,
     * at the given position without changing currentPos
     */
    private void writeUShort(int pos, int s) {
        byte b1 = (byte)((s >> 8) & 0xff);
        byte b2 = (byte)(s & 0xff);
        output[pos] = b1;
        output[pos + 1] = b2;
    }

    /**
     * Appends a ULONG to the output array,
     * updates currentPos but not realSize
     */
    private void writeULong(int s) {
        byte b1 = (byte)((s >> 24) & 0xff);
        byte b2 = (byte)((s >> 16) & 0xff);
        byte b3 = (byte)((s >> 8) & 0xff);
        byte b4 = (byte)(s & 0xff);
        writeByte(b1);
        writeByte(b2);
        writeByte(b3);
        writeByte(b4);
    }

    /**
     * Appends a ULONG to the output array,
     * at the given position without changing currentPos
     */
    private void writeULong(int pos, int s) {
        byte b1 = (byte)((s >> 24) & 0xff);
        byte b2 = (byte)((s >> 16) & 0xff);
        byte b3 = (byte)((s >> 8) & 0xff);
        byte b4 = (byte)(s & 0xff);
        output[pos] = b1;
        output[pos + 1] = b2;
        output[pos + 2] = b3;
        output[pos + 3] = b4;
    }

    /**
     * Read a signed short value at given position
     */
    private short readShort(int pos) {
        int ret = readUShort(pos);
        return (short)ret;
    }

    /**
     * Read a unsigned short value at given position
     */
    private int readUShort(int pos) {
        int ret = output[pos];
        if (ret < 0) {
            ret += 256;
        }
        ret = ret << 8;
        if (output[pos + 1] < 0) {
            ret |= output[pos + 1] + 256;
        } else {
            ret |= output[pos + 1];
        }

        return ret;
    }

    /**
     * Create a padding in the fontfile to align
     * on a 4-byte boundary
     */
    private void pad4() {
        int padSize = currentPos % 4;
        for (int i = 0; i < padSize; i++) {
            output[currentPos++] = 0;
            realSize++;
        }
    }

    /**
     * Returns the maximum power of 2 <= max
     */
    private int maxPow2(int max) {
        int i = 0;
        while (Math.pow(2, i) < max) {
            i++;
        }

        return (i - 1);
    }

    private int log2(int num) {
        return (int)(Math.log(num) / Math.log(2));
    }


    private int getCheckSum(int start, int size) {
        return (int)getLongCheckSum(output, start, size);
    }

    private static long getLongCheckSum(byte[] data, int start, int size) {
        // All the tables here are aligned on four byte boundaries
        // Add remainder to size if it's not a multiple of 4
        int remainder = size % 4;
        if (remainder != 0) {
            size += remainder;
        }

        long sum = 0;

        for (int i = 0; i < size; i += 4) {
            int l = (data[start + i] << 24);
            l += (data[start + i + 1] << 16);
            l += (data[start + i + 2] << 16);
            l += (data[start + i + 3] << 16);
            sum += l;
            if (sum > 0xffffffff) {
                sum = sum - 0xffffffff;
            }
        }

        return sum;
    }

    private void createCheckSumAdjustment() {
        long sum = getLongCheckSum(output, 0, realSize);
        int checksum = (int)(0xb1b0afba - sum);
        writeULong(checkSumAdjustmentOffset, checksum);
    }

}



