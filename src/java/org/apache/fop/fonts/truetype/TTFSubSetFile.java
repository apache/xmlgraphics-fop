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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;


/**
 * Reads a TrueType file and generates a subset
 * that can be used to embed a TrueType CID font.
 * TrueType tables needed for embedded CID fonts are:
 * "head", "hhea", "loca", "maxp", "cvt ", "prep", "glyf", "hmtx" and "fpgm".
 * The TrueType spec can be found at the Microsoft
 * Typography site: http://www.microsoft.com/truetype/
 */
public class TTFSubSetFile extends TTFFile {

    private byte[] output = null;
    private int realSize = 0;
    private int currentPos = 0;

    /*
     * Offsets in name table to be filled out by table.
     * The offsets are to the checkSum field
     */
    private Map<TTFTableName, Integer> offsets = new HashMap<TTFTableName, Integer>();

    private int checkSumAdjustmentOffset = 0;
    private int locaOffset = 0;

    /** Stores the glyph offsets so that we can end strings at glyph boundaries */
    private int[] glyphOffsets;

    /**
     * Default Constructor
     */
    public TTFSubSetFile() {
    }

    /**
     * Constructor
     * @param useKerning true if kerning data should be loaded
     * @param useAdvanced true if advanced typographic tables should be loaded
     */
    public TTFSubSetFile ( boolean useKerning, boolean useAdvanced ) {
        super(useKerning, useAdvanced);
    }

    /**
     * Initalize the output array
     */
    private void init(int size) {
        output = new byte[size];
        realSize = 0;
        currentPos = 0;
    }

    /** The dir tab entries in the new subset font. */
    private Map<TTFTableName, TTFDirTabEntry> newDirTabs
                        = new HashMap<TTFTableName, TTFDirTabEntry>();

    private int determineTableCount() {
        int numTables = 4; //4 req'd tables: head,hhea,hmtx,maxp
        if (isCFF()) {
            throw new UnsupportedOperationException(
                    "OpenType fonts with CFF glyphs are not supported");
        } else {
            numTables += 5; //5 req'd tables: glyf,loca,post,name,OS/2
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
    private void createDirectory() {
        int numTables = determineTableCount();
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
        int searchRange = (int) Math.pow(2, maxPow) * 16;
        writeUShort(searchRange);
        realSize += 2;

        writeUShort(maxPow);
        realSize += 2;

        writeUShort((numTables * 16) - searchRange);
        realSize += 2;
        // Create space for the table entries (these must be in ASCII alphabetical order[A-Z] then[a-z])
        writeTableName(TTFTableName.OS2);

        if (hasCvt()) {
            writeTableName(TTFTableName.CVT);
        }
        if (hasFpgm()) {
            writeTableName(TTFTableName.FPGM);
        }
        writeTableName(TTFTableName.GLYF);
        writeTableName(TTFTableName.HEAD);
        writeTableName(TTFTableName.HHEA);
        writeTableName(TTFTableName.HMTX);
        writeTableName(TTFTableName.LOCA);
        writeTableName(TTFTableName.MAXP);
        writeTableName(TTFTableName.NAME);
        writeTableName(TTFTableName.POST);
        if (hasPrep()) {
            writeTableName(TTFTableName.PREP);
        }
        newDirTabs.put(TTFTableName.TABLE_DIRECTORY, new TTFDirTabEntry(0, currentPos));
    }

    private void writeTableName(TTFTableName tableName) {
        writeString(tableName.getName());
        offsets.put(tableName, currentPos);
        currentPos += 12;
        realSize += 16;
    }


    private boolean hasCvt() {
        return dirTabs.containsKey(TTFTableName.CVT);
    }

    private boolean hasFpgm() {
        return dirTabs.containsKey(TTFTableName.FPGM);
    }

    private boolean hasPrep() {
        return dirTabs.containsKey(TTFTableName.PREP);
    }

    /**
     * Create an empty loca table without updating checksum
     */
    private void createLoca(int size) throws IOException {
        pad4();
        locaOffset = currentPos;
        int dirTableOffset = offsets.get(TTFTableName.LOCA);
        writeULong(dirTableOffset + 4, currentPos);
        writeULong(dirTableOffset + 8, size * 4 + 4);
        currentPos += size * 4 + 4;
        realSize += size * 4 + 4;
    }

    private boolean copyTable(FontFileReader in, TTFTableName tableName) throws IOException {
        TTFDirTabEntry entry = dirTabs.get(tableName);
        if (entry != null) {
            pad4();
            seekTab(in, tableName, 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());

            updateCheckSum(currentPos, (int) entry.getLength(), tableName);
            currentPos += (int) entry.getLength();
            realSize += (int) entry.getLength();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Copy the cvt table as is from original font to subset font
     */
    private boolean createCvt(FontFileReader in) throws IOException {
        return copyTable(in, TTFTableName.CVT);
    }

    /**
     * Copy the fpgm table as is from original font to subset font
     */
    private boolean createFpgm(FontFileReader in) throws IOException {
        return copyTable(in, TTFTableName.FPGM);
    }

    /**
     * Copy the name table as is from the original.
     */
    private boolean createName(FontFileReader in) throws IOException {
        return copyTable(in, TTFTableName.NAME);
    }

    /**
     * Copy the OS/2 table as is from the original.
     */
    private boolean createOS2(FontFileReader in) throws IOException {
        return copyTable(in, TTFTableName.OS2);
    }

    /**
     * Copy the maxp table as is from original font to subset font
     * and set num glyphs to size
     */
    private void createMaxp(FontFileReader in, int size) throws IOException {
        TTFTableName maxp = TTFTableName.MAXP;
        TTFDirTabEntry entry = dirTabs.get(maxp);
        if (entry != null) {
            pad4();
            seekTab(in, maxp, 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());
            writeUShort(currentPos + 4, size);

            updateCheckSum(currentPos, (int)entry.getLength(), maxp);
            currentPos += (int)entry.getLength();
            realSize += (int)entry.getLength();
        } else {
            throw new IOException("Can't find maxp table");
        }
    }

    private void createPost(FontFileReader in) throws IOException {
        TTFTableName post = TTFTableName.POST;
        TTFDirTabEntry entry = dirTabs.get(post);
        if (entry != null) {
            pad4();
            seekTab(in, post, 0);
            int newTableSize = 32; // This is the post table size with glyphs truncated
            byte[] newPostTable = new byte[newTableSize];
            // We only want the first 28 bytes (truncate the glyph names);
            System.arraycopy(in.getBytes((int) entry.getOffset(), newTableSize),
                    0, newPostTable, 0, newTableSize);
            // set the post table to Format 3.0
            newPostTable[1] = 0x03;
            System.arraycopy(newPostTable, 0, output, currentPos, newTableSize);
            updateCheckSum(currentPos, newTableSize, post);
            currentPos += newTableSize;
            realSize += newTableSize;
        } else {
            throw new IOException("Can't find post table");
        }
    }


    /**
     * Copy the prep table as is from original font to subset font
     */
    private boolean createPrep(FontFileReader in) throws IOException {
        return copyTable(in, TTFTableName.PREP);
    }


    /**
     * Copy the hhea table as is from original font to subset font
     * and fill in size of hmtx table
     */
    private void createHhea(FontFileReader in, int size) throws IOException {
        TTFDirTabEntry entry = dirTabs.get(TTFTableName.HHEA);
        if (entry != null) {
            pad4();
            seekTab(in, TTFTableName.HHEA, 0);
            System.arraycopy(in.getBytes((int) entry.getOffset(), (int) entry.getLength()), 0,
                    output, currentPos, (int) entry.getLength());
            writeUShort((int) entry.getLength() + currentPos - 2, size);

            updateCheckSum(currentPos, (int) entry.getLength(), TTFTableName.HHEA);
            currentPos += (int) entry.getLength();
            realSize += (int) entry.getLength();
        } else {
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
        TTFTableName head = TTFTableName.HEAD;
        TTFDirTabEntry entry = dirTabs.get(head);
        if (entry != null) {
            pad4();
            seekTab(in, head, 0);
            System.arraycopy(in.getBytes((int)entry.getOffset(), (int)entry.getLength()),
                             0, output, currentPos, (int)entry.getLength());

            checkSumAdjustmentOffset = currentPos + 8;
            output[currentPos + 8] = 0;     // Set checkSumAdjustment to 0
            output[currentPos + 9] = 0;
            output[currentPos + 10] = 0;
            output[currentPos + 11] = 0;
            output[currentPos + 50] = 0;    // long locaformat
            output[currentPos + 51] = 1;    // long locaformat

            updateCheckSum(currentPos, (int)entry.getLength(), head);
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
        TTFTableName glyf = TTFTableName.GLYF;
        TTFDirTabEntry entry = dirTabs.get(glyf);
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
            glyphOffsets = new int[origIndexes.length];

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

                // Store the glyph boundary positions relative to the start of the font
                glyphOffsets[i] = currentPos;
                currentPos += glyphLength;
                realSize += glyphLength;


                endOffset = endOffset1;
            }


            size = currentPos - startPos;

            currentPos += 12;
            realSize += 12;
            updateCheckSum(startPos, size + 12, glyf);

            // Update loca checksum and last loca index
            writeULong(locaOffset + glyphs.size() * 4, endOffset);
            int locaSize = glyphs.size() * 4 + 4;
            int checksum = getCheckSum(output, locaOffset, locaSize);
            writeULong(offsets.get(TTFTableName.LOCA), checksum);
            int padSize = (locaOffset + locaSize) % 4;
            newDirTabs.put(TTFTableName.LOCA,
                    new TTFDirTabEntry(locaOffset, locaSize + padSize));
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
        TTFTableName hmtx = TTFTableName.HMTX;
        TTFDirTabEntry entry = dirTabs.get(hmtx);

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

            updateCheckSum(currentPos, hmtxSize, hmtx);
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
     * Reads a font and creates a subset of the font.
     *
     * @param in FontFileReader to read from
     * @param name Name to be checked for in the font file
     * @param glyphs Map of glyphs (glyphs has old index as (Integer) key and
     * new index as (Integer) value)
     * @throws IOException in case of an I/O problem
     */
    public void readFont(FontFileReader in, String name,
                           Map<Integer, Integer> glyphs) throws IOException {
        fontFile = in;
        //Check if TrueType collection, and that the name exists in the collection
        if (!checkTTC(name)) {
            throw new IOException("Failed to read font");
        }

        //Copy the Map as we're going to modify it
        Map<Integer, Integer> subsetGlyphs = new HashMap<Integer, Integer>(glyphs);

        output = new byte[in.getFileSize()];

        readDirTabs();
        readFontHeader();
        getNumGlyphs();
        readHorizontalHeader();
        readHorizontalMetrics();
        readIndexToLocation();

        scanGlyphs(in, subsetGlyphs);

        createDirectory();     // Create the TrueType header and directory

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
        createLoca(subsetGlyphs.size());    // create empty loca table
        createGlyf(in, subsetGlyphs); //create glyf table and update loca table

        createOS2(in);                          // copy the OS/2 table
        createHead(in);
        createHhea(in, subsetGlyphs.size());    // Create the hhea table
        createHmtx(in, subsetGlyphs);           // Create hmtx table
        createMaxp(in, subsetGlyphs.size());    // copy the maxp table
        createName(in);                         // copy the name table
        createPost(in);                         // copy the post table

        optionalTableFound = createPrep(in);    // copy prep table
        if (!optionalTableFound) {
            // prep is optional (used in TrueType fonts only)
            log.debug("TrueType: prep table not present. Skipped.");
        }

        pad4();
        createCheckSumAdjustment();
    }

    /**
     * Returns a subset of the fonts (readFont() MUST be called first in order to create the
     * subset).
     * @return byte array
     */
    public byte[] getFontSubset() {
        byte[] ret = new byte[realSize];
        System.arraycopy(output, 0, ret, 0, realSize);
        return ret;
    }

    private void handleGlyphSubset(TTFGlyphOutputStream glyphOut) throws IOException {
        glyphOut.startGlyphStream();
        // Stream all but the last glyph
        for (int i = 0; i < glyphOffsets.length - 1; i++) {
            glyphOut.streamGlyph(output, glyphOffsets[i],
                    glyphOffsets[i + 1] - glyphOffsets[i]);
        }
        // Stream the last glyph
        TTFDirTabEntry glyf = newDirTabs.get(TTFTableName.GLYF);
        long lastGlyphLength = glyf.getLength()
            - (glyphOffsets[glyphOffsets.length - 1] - glyf.getOffset());
        glyphOut.streamGlyph(output, glyphOffsets[glyphOffsets.length - 1],
                (int) lastGlyphLength);
        glyphOut.endGlyphStream();
    }

    @Override
    public void stream(TTFOutputStream ttfOut) throws IOException {
        SortedSet<Map.Entry<TTFTableName, TTFDirTabEntry>>  sortedDirTabs
                = sortDirTabMap(newDirTabs);
        TTFTableOutputStream tableOut = ttfOut.getTableOutputStream();
        TTFGlyphOutputStream glyphOut = ttfOut.getGlyphOutputStream();

        ttfOut.startFontStream();
        for (Map.Entry<TTFTableName, TTFDirTabEntry>  entry : sortedDirTabs) {
            if (entry.getKey().equals(TTFTableName.GLYF)) {
                    handleGlyphSubset(glyphOut);
            } else {
                tableOut.streamTable(output, (int) entry.getValue().getOffset(),
                            (int) entry.getValue().getLength());
            }
        }
        ttfOut.endFontStream();
    }

    private void scanGlyphs(FontFileReader in, Map<Integer, Integer> subsetGlyphs)
            throws IOException {
        TTFDirTabEntry glyfTableInfo = dirTabs.get(TTFTableName.GLYF);
        if (glyfTableInfo == null) {
            throw new IOException("Glyf table could not be found");
        }

        GlyfTable glyfTable = new GlyfTable(in, mtxTab, glyfTableInfo, subsetGlyphs);
        glyfTable.populateGlyphsWithComposites();
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
     * Create a padding in the fontfile to align
     * on a 4-byte boundary
     */
    private void pad4() {
        int padSize = getPadSize(currentPos);
        if (padSize < 4) {
            for (int i = 0; i < padSize; i++) {
                output[currentPos++] = 0;
                realSize++;
            }
        }
    }

    /**
     * Returns the maximum power of 2 <= max
     */
    private int maxPow2(int max) {
        int i = 0;
        while (Math.pow(2, i) <= max) {
            i++;
        }

        return (i - 1);
    }


    private void updateCheckSum(int tableStart, int tableSize, TTFTableName tableName) {
        int checksum = getCheckSum(output, tableStart, tableSize);
        int offset = offsets.get(tableName);
        int padSize = getPadSize(tableStart +  tableSize);
        newDirTabs.put(tableName, new TTFDirTabEntry(tableStart, tableSize + padSize));
        writeULong(offset, checksum);
        writeULong(offset + 4, tableStart);
        writeULong(offset + 8, tableSize);
    }

    private static int getCheckSum(byte[] data, int start, int size) {
        // All the tables here are aligned on four byte boundaries
        // Add remainder to size if it's not a multiple of 4
        int remainder = size % 4;
        if (remainder != 0) {
            size += remainder;
        }

        long sum = 0;

        for (int i = 0; i < size; i += 4) {
            long l = 0;
            for (int j = 0; j < 4; j++) {
                l <<= 8;
                l |= data[start + i + j] & 0xff;
            }
            sum += l;
        }
        return (int) sum;
    }

    private void createCheckSumAdjustment() {
        long sum = getCheckSum(output, 0, realSize);
        int checksum = (int)(0xb1b0afba - sum);
        writeULong(checkSumAdjustmentOffset, checksum);
    }
}
