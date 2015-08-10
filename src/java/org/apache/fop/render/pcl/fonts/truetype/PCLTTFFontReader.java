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

package org.apache.fop.render.pcl.fonts.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.fonts.truetype.OpenFont;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.PCLByteWriterUtil;
import org.apache.fop.render.pcl.fonts.PCLFontReader;
import org.apache.fop.render.pcl.fonts.PCLFontSegment;
import org.apache.fop.render.pcl.fonts.PCLFontSegment.SegmentID;
import org.apache.fop.render.pcl.fonts.PCLSymbolSet;

public class PCLTTFFontReader extends PCLFontReader {
    protected TTFFile ttfFont;
    protected InputStream fontStream;
    protected FontFileReader reader;
    private PCLTTFPCLTFontTable pcltTable;
    private PCLTTFOS2FontTable os2Table;
    private PCLTTFPOSTFontTable postTable;
    private PCLTTFTableFactory ttfTableFactory;

    private static final int HMTX_RESTRICT_SIZE = 50000;

    private static final Map<Integer, Integer> FONT_WEIGHT = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            put(100, -6); // 100 Thin
            put(200, -4); // 200 Extra-Light
            put(300, -3); // 300 Light
            put(400, 0); // 400 Normal (Regular)
            put(500, 0); // 500 Medium
            put(600, 2); // 600 Semi-bold
            put(700, 3); // 700 Bold
            put(800, 4); // 800 Extra-bold
            put(900, 5); // 900 Black (Heavy)
        }
    };

    private static final Map<Integer, Integer> FONT_SERIF = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            /** The following are the best guess conversion between serif styles. Unfortunately
             *  there appears to be no standard and so each specification has it's own set of values.
             *  Please change if better fit found. **/
            put(0, 0); // Any = Normal Sans
            put(1, 64); // No Fit = Sans Serif
            put(2, 9); // Cove = Script Nonconnecting
            put(3, 12); // Obtuse Cove = Script Broken Letter
            put(4, 10); // Square Cove = Script Joining
            put(5, 0); // Obtuse Square Cove = Sans Serif Square
            put(6, 128); // Square = Serif
            put(7, 2); // Thin = Serif Line
            put(8, 7); // Bone = Rounded Bracket
            put(9, 11); // Exeraggerated = Script Calligraphic
            put(10, 3); // Triangle = Serif Triangle
            put(11, 0); // Normal Sans = Sans Serif Square
            put(12, 4); // Obtuse Sans = Serif Swath
            put(13, 6); // Perp Sans = Serif Bracket
            put(14, 8); // Flared = Flair Serif
            put(15, 1); // Rounded = Sans Serif Round
        }
    };

    private static final Map<Integer, Integer> FONT_WIDTH = new HashMap<Integer, Integer>() {
        private static final long serialVersionUID = 1L;
        {
            /** The conversions between TTF and PCL are not 1 to 1 **/
            put(1, -5); // 1 = Ultra Compressed
            put(2, -4); // 2 = Extra Compressed
            put(3, -3); // 3 = Compresses
            put(4, -2); // 4 = Condensed
            put(5, 0); // 5 = Normal
            put(6, 2); // 6 = Expanded
            put(7, 3); // 5 = Extra Expanded
        }
    };

    private int scaleFactor = -1;
    private PCLSymbolSet symbolSet = PCLSymbolSet.Bound_Generic;

    public PCLTTFFontReader(Typeface font, PCLByteWriterUtil pclByteWriter) throws IOException {
        super(font, pclByteWriter);
        loadFont();
    }

    protected void loadFont() throws IOException {
        if (typeface instanceof CustomFontMetricsMapper) {
            CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper) typeface;
            CustomFont font = (CustomFont) fontMetrics.getRealFont();
            setFont((CustomFont) fontMetrics.getRealFont());
            String fontName = font.getFullName();
            fontStream = font.getInputStream();
            reader = new FontFileReader(fontStream);

            ttfFont = new TTFFile();
            String header = OFFontLoader.readHeader(reader);
            ttfFont.readFont(reader, header, fontName);
            readFontTables();
        } else {
            // TODO - Handle when typeface is not in the expected format for a PCL TrueType object
        }
    }

    protected void readFontTables() throws IOException {
        PCLTTFTable fontTable;
        fontTable = readFontTable(OFTableName.PCLT);
        if (fontTable instanceof PCLTTFPCLTFontTable) {
            pcltTable = (PCLTTFPCLTFontTable) fontTable;
        }
        fontTable = readFontTable(OFTableName.OS2);
        if (fontTable instanceof PCLTTFOS2FontTable) {
            os2Table = (PCLTTFOS2FontTable) fontTable;
        }
        fontTable = readFontTable(OFTableName.POST);
        if (fontTable instanceof PCLTTFPOSTFontTable) {
            postTable = (PCLTTFPOSTFontTable) fontTable;
        }
    }

    private PCLTTFTable readFontTable(OFTableName tableName) throws IOException {
        if (ttfFont.seekTab(reader, tableName, 0)) {
            return getTTFTableFactory().newInstance(tableName);
        }
        return null;
    }

    private PCLTTFTableFactory getTTFTableFactory() {
        if (ttfTableFactory == null) {
            ttfTableFactory = PCLTTFTableFactory.getInstance(reader);
        }
        return ttfTableFactory;
    }

    @Override
    public int getDescriptorSize() {
        return 72; // Descriptor size (leave at 72 for our purposes)
    }

    @Override
    public int getHeaderFormat() {
        return 15; // TrueType Scalable Font
    }

    @Override
    public int getFontType() {
        if (symbolSet == PCLSymbolSet.Unbound) {
            return 11; // Font Type - Unbound TrueType Scalable font
        } else {
            return 2; // 0-255 (except 0, 7 and 27)
        }
    }

    @Override
    public int getStyleMSB() {
        if (pcltTable != null) {
            return getMSB(pcltTable.getStyle());
        }
        return 3;
    }

    @Override
    public int getBaselinePosition() {
        return 0; // Baseline position must be set to 0 for TTF fonts
    }

    @Override
    public int getCellWidth() {
        int[] bbox = ttfFont.getBBoxRaw();
        return bbox[2] - bbox[0];
    }

    @Override
    public int getCellHeight() {
        int[] bbox = ttfFont.getBBoxRaw();
        return bbox[3] - bbox[1];
    }

    @Override
    public int getOrientation() {
        return 0; // Scalable fonts (TrueType) must be 0
    }

    @Override
    public int getSpacing() {
        if (os2Table != null) {
            return (os2Table.getPanose()[4] == 9) ? 0 : 1;
        } else if (postTable != null) {
            return postTable.getIsFixedPitch();
        }
        return 1;
    }

    @Override
    public int getSymbolSet() {
        if (pcltTable != null) {
            return pcltTable.getSymbolSet();
        } else {
            return symbolSet.getKind1();
        }
    }

    @Override
    public int getPitch() {
        int pitch = ttfFont.getCharWidthRaw(0x20);
        if (pitch < 0) {
            // No advance width found for the space character
            return 0;
        }
        return pitch;
    }

    @Override
    public int getHeight() {
        return 0; // Fixed zero value for TrueType fonts
    }

    @Override
    public int getXHeight() {
        if (pcltTable != null) {
            return pcltTable.getXHeight();
        } else if (os2Table != null) {
            return os2Table.getXHeight();
        }
        return 0;
    }

    @Override
    public int getWidthType() {
        if (pcltTable != null) {
            return pcltTable.getWidthType();
        } else if (os2Table != null) {
            return convertTTFWidthClass(os2Table.getWidthClass());
        }
        return 0;
    }

    private int convertTTFWidthClass(int widthClass) {
        if (FONT_WIDTH.containsKey(widthClass)) {
            return FONT_WIDTH.get(widthClass);
        } else {
            return 0; // No match - return normal
        }
    }

    @Override
    public int getStyleLSB() {
        if (pcltTable != null) {
            return getLSB(pcltTable.getStyle());
        }
        return 224;
    }

    @Override
    public int getStrokeWeight() {
        if (pcltTable != null) {
            return pcltTable.getStrokeWeight();
        } else if (os2Table != null) {
            return convertTTFWeightClass(os2Table.getWeightClass());
        }
        return 0;
    }

    private int convertTTFWeightClass(int weightClass) {
        if (FONT_WEIGHT.containsKey(weightClass)) {
            return FONT_WEIGHT.get(weightClass);
        } else {
            return 0; // No match - return normal
        }
    }

    @Override
    public int getTypefaceLSB() {
        if (pcltTable != null) {
            return getLSB(pcltTable.getTypeFamily());
        }
        return 254;
    }

    @Override
    public int getTypefaceMSB() {
        if (pcltTable != null) {
            return getMSB(pcltTable.getTypeFamily());
        }
        return 0;
    }

    @Override
    public int getSerifStyle() {
        if (pcltTable != null) {
            return pcltTable.getSerifStyle();
        } else {
            return convertFromTTFSerifStyle();
        }
    }

    private int convertFromTTFSerifStyle() {
        if (os2Table != null) {
            int serifStyle = os2Table.getPanose()[1];
            return FONT_SERIF.get(serifStyle);
        }
        return 0;
    }

    @Override
    public int getQuality() {
        return 2; // Letter quality
    }

    @Override
    public int getPlacement() {
        return 0; // Fixed value of 0 for TrueType (scalable fonts)
    }

    @Override
    public int getUnderlinePosition() {
        return 0; // Scalable fonts has a fixed value of 0 - See Master Underline Position
    }

    @Override
    public int getUnderlineThickness() {
        return 0; // Scalable fonts has a fixed value of 0 - See Master Underline Thickness
    }

    @Override
    public int getTextHeight() {
        return 2048;
    }

    @Override
    public int getTextWidth() {
        if (os2Table != null) {
            return os2Table.getAvgCharWidth();
        }
        return 0;
    }

    @Override
    public int getFirstCode() {
        return 32;
    }

    @Override
    public int getLastCode() {
        return 255; // Bound font with a maximum of 255 characters
    }

    @Override
    public int getPitchExtended() {
        return 0; // Zero for Scalable fonts
    }

    @Override
    public int getHeightExtended() {
        return 0; // Zero for Scalable fonts
    }

    @Override
    public int getCapHeight() {
        if (pcltTable != null) {
            return pcltTable.getStrokeWeight();
        } else if (os2Table != null) {
            return os2Table.getCapHeight();
        }
        return 0;
    }

    @Override
    public int getFontNumber() {
        if (pcltTable != null) {
            return (int) pcltTable.getFontNumber();
        }
        return 0;
    }

    @Override
    public String getFontName() {
        if (pcltTable != null) {
            return pcltTable.getTypeface();
        } else {
            return ttfFont.getFullName();
        }
    }

    @Override
    public int getScaleFactor() throws IOException {
        if (scaleFactor == -1) {
            OFTableName headTag = OFTableName.HEAD;
            if (ttfFont.seekTab(reader, headTag, 0)) {
                reader.readTTFLong(); // Version
                reader.readTTFLong(); // Font revision
                reader.readTTFLong(); // Check sum adjustment
                reader.readTTFLong(); // Magic number
                reader.readTTFShort(); // Flags
                scaleFactor = reader.readTTFUShort(); // Units per em
                return scaleFactor;
            }
        } else {
            return scaleFactor;
        }
        return 0;
    }

    @Override
    public int getMasterUnderlinePosition() throws IOException {
        return (int) Math.round(getScaleFactor() * 0.2);
    }

    @Override
    public int getMasterUnderlineThickness() throws IOException {
        return (int) Math.round(getScaleFactor() * 0.05);
    }

    @Override
    public int getFontScalingTechnology() {
        return 1; // TrueType scalable font
    }

    @Override
    public int getVariety() {
        return 0; // TrueType fonts must be set to zero
    }

    public List<PCLFontSegment> getFontSegments(Map<Character, Integer> mappedGlyphs)
            throws IOException {
        List<PCLFontSegment> fontSegments = new ArrayList<PCLFontSegment>();
        fontSegments.add(new PCLFontSegment(SegmentID.CC, getCharacterComplement()));
        fontSegments.add(new PCLFontSegment(SegmentID.PA, pclByteWriter.toByteArray(os2Table.getPanose())));
        fontSegments.add(new PCLFontSegment(SegmentID.GT, getGlobalTrueTypeData(mappedGlyphs)));
        fontSegments.add(new PCLFontSegment(SegmentID.CP, ttfFont.getCopyrightNotice().getBytes("US-ASCII")));
        fontSegments.add(new PCLFontSegment(SegmentID.NULL, new byte[0]));
        return fontSegments;
    }

    /**
     * See Font Header Format 11-35 (Character Complement Array) in the PCL 5 Specification. Defined as an array of 8
     * bytes specific to certain character sets. In this case specifying 0 for all values (default complement) means the
     * font is compatible with any character sets. '110' on least significant bits signifies unicode. See specification
     * for further customization.
     */
    private byte[] getCharacterComplement() {
        byte[] ccUnicode = new byte[8];
        ccUnicode[7] = 6;
        return ccUnicode;
    }

    private byte[] getGlobalTrueTypeData(Map<Character, Integer> mappedGlyphs) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<TableOffset> tableOffsets = new ArrayList<TableOffset>();
        // Version
        baos.write(pclByteWriter.unsignedInt(1)); // Major
        baos.write(pclByteWriter.unsignedInt(0)); // Minor
        int numTables = 5; // head, hhea, hmtx, maxp and gdir
        // Optional Hint Tables
        OFDirTabEntry headTable = ttfFont.getDirectoryEntry(OFTableName.CVT);
        if (headTable != null) {
            numTables++;
        }
        OFDirTabEntry fpgmTable = ttfFont.getDirectoryEntry(OFTableName.FPGM);
        if (fpgmTable != null) {
            numTables++;
        }
        OFDirTabEntry prepTable = ttfFont.getDirectoryEntry(OFTableName.PREP);
        if (prepTable != null) {
            numTables++;
        }
        baos.write(pclByteWriter.unsignedInt(numTables)); // numTables
        int maxPowerNumTables = pclByteWriter.maxPower2(numTables);
        int searchRange = maxPowerNumTables * 16;
        baos.write(pclByteWriter.unsignedInt(searchRange));
        baos.write(pclByteWriter.unsignedInt(pclByteWriter.log(maxPowerNumTables, 2))); // Entry Selector
        baos.write(pclByteWriter.unsignedInt(numTables * 16 - searchRange)); // Range shift

        // Add default data tables
        writeTrueTypeTable(baos, OFTableName.HEAD, tableOffsets);
        writeTrueTypeTable(baos, OFTableName.HHEA, tableOffsets);
        byte[] hmtxTable = createHmtx(mappedGlyphs);
        writeSubsetHMTX(baos, OFTableName.HMTX, tableOffsets, hmtxTable);
        writeTrueTypeTable(baos, OFTableName.MAXP, tableOffsets);
        // Write the blank GDIR directory which is built in memory on the printer
        writeGDIR(baos);

        // Add optional data tables (for hints)
        writeTrueTypeTable(baos, OFTableName.CVT, tableOffsets);
        writeTrueTypeTable(baos, OFTableName.FPGM, tableOffsets);
        writeTrueTypeTable(baos, OFTableName.PREP, tableOffsets);

        baos = copyTables(tableOffsets, baos, hmtxTable, mappedGlyphs.size());

        return baos.toByteArray();
    }

    private static class TableOffset {
        private long originOffset;
        private long originLength;
        private int newOffset;

        public TableOffset(long originOffset, long originLength, int newOffset) {
            this.originOffset = originOffset;
            this.originLength = originLength;
            this.newOffset = newOffset;
        }

        public long getOriginOffset() {
            return originOffset;
        }

        public long getOriginLength() {
            return originLength;
        }

        public int getNewOffset() {
            return newOffset;
        }
    }

    private void writeTrueTypeTable(ByteArrayOutputStream baos, OFTableName table,
            List<TableOffset> tableOffsets) throws IOException, UnsupportedEncodingException {
        OFDirTabEntry tabEntry = ttfFont.getDirectoryEntry(table);
        if (tabEntry != null) {
            baos.write(tabEntry.getTag());
            baos.write(pclByteWriter.unsignedLongInt(tabEntry.getChecksum()));
            TableOffset newTableOffset = new TableOffset(tabEntry.getOffset(),
                    tabEntry.getLength(), baos.size());
            tableOffsets.add(newTableOffset);
            baos.write(pclByteWriter.unsignedLongInt(0)); // Offset to be set later
            baos.write(pclByteWriter.unsignedLongInt(tabEntry.getLength()));
        }
    }

    private void writeGDIR(ByteArrayOutputStream baos) throws UnsupportedEncodingException, IOException {
        baos.write("gdir".getBytes("ISO-8859-1"));
        baos.write(pclByteWriter.unsignedLongInt(0)); // Checksum
        baos.write(pclByteWriter.unsignedLongInt(0)); // Offset
        baos.write(pclByteWriter.unsignedLongInt(0)); // Length
    }

    private ByteArrayOutputStream copyTables(List<TableOffset> tableOffsets,
            ByteArrayOutputStream baos, byte[] hmtxTable, int hmtxSize)
            throws IOException {
        Map<Integer, byte[]> offsetValues = new HashMap<Integer, byte[]>();
        for (TableOffset tableOffset : tableOffsets) {
            offsetValues.put(tableOffset.getNewOffset(), pclByteWriter.unsignedLongInt(baos.size()));
            if (tableOffset.getOriginOffset() == -1) { // Update the offset in the table directory
                baos.write(hmtxTable);
            } else {
                byte[] tableData = reader.getBytes((int) tableOffset.getOriginOffset(),
                        (int) tableOffset.getOriginLength());
                int index = tableOffsets.indexOf(tableOffset);
                if (index == 1) {
                    tableData = updateHHEA(tableData, hmtxSize + 33);
                }

                // Write the table data to the end of the TrueType segment output
                baos.write(tableData);
            }
        }
        baos = updateOffsets(baos, offsetValues);
        return baos;
    }

    private byte[] updateHHEA(byte[] tableData, int hmtxSize) {
        writeUShort(tableData, tableData.length - 2, hmtxSize);
        return tableData;
    }

    private ByteArrayOutputStream updateOffsets(ByteArrayOutputStream baos, Map<Integer, byte[]> offsets)
            throws IOException {
        byte[] softFont = baos.toByteArray();
        for (int offset : offsets.keySet()) {
            pclByteWriter.updateDataAtLocation(softFont, offsets.get(offset), offset);
        }
        baos = new ByteArrayOutputStream();
        baos.write(softFont);
        return baos;
    }

    @Override
    public Map<Integer, int[]> getCharacterOffsets() throws IOException {
        List<OFMtxEntry> mtx = ttfFont.getMtx();
        OFTableName glyfTag = OFTableName.GLYF;
        Map<Integer, int[]> charOffsets = new HashMap<Integer, int[]>();
        OFDirTabEntry tabEntry = ttfFont.getDirectoryEntry(glyfTag);
        if (ttfFont.seekTab(reader, glyfTag, 0)) {
            for (int i = 1; i < mtx.size(); i++) {
                OFMtxEntry entry = mtx.get(i);
                OFMtxEntry nextEntry;
                int nextOffset = 0;
                int charCode = 0;
                if (entry.getUnicodeIndex().size() > 0) {
                    charCode = (Integer) entry.getUnicodeIndex().get(0);
                } else {
                    charCode = entry.getIndex();
                }

                if (i < mtx.size() - 1) {
                    nextEntry = mtx.get(i + 1);
                    nextOffset = (int) nextEntry.getOffset();
                } else {
                    nextOffset = (int) ttfFont.getLastGlyfLocation();
                }
                int glyphOffset = (int) entry.getOffset();
                int glyphLength = nextOffset - glyphOffset;

                charOffsets.put(charCode, new int[]{(int) tabEntry.getOffset() + glyphOffset, glyphLength});
            }
        }
        return charOffsets;
    }

    @Override
    public OpenFont getFontFile() {
        return ttfFont;
    }

    @Override
    public FontFileReader getFontFileReader() {
        return reader;
    }

    private void writeSubsetHMTX(ByteArrayOutputStream baos, OFTableName table,
            List<TableOffset> tableOffsets, byte[] hmtxTable) throws IOException {
        OFDirTabEntry tabEntry = ttfFont.getDirectoryEntry(table);
        if (tabEntry != null) {
            baos.write(tabEntry.getTag());
            // Override the original checksum for the subset version
            baos.write(pclByteWriter.unsignedLongInt(getCheckSum(hmtxTable, 0, hmtxTable.length)));
            TableOffset newTableOffset = new TableOffset(-1, hmtxTable.length, baos.size());
            tableOffsets.add(newTableOffset);
            baos.write(pclByteWriter.unsignedLongInt(0)); // Offset to be set later
            baos.write(pclByteWriter.unsignedLongInt(hmtxTable.length));
        }
    }

    protected static int getCheckSum(byte[] data, int start, int size) {
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
                if (data.length > (start + i + j)) {
                    l |= data[start + i + j] & 0xff;
                }
            }
            sum += l;
        }
        return (int) sum;
    }

    protected byte[] createHmtx(Map<Character, Integer> mappedGlyphs) throws IOException {
        byte[] hmtxTable = new byte[((mappedGlyphs.size() + 32) * 4)];
        OFDirTabEntry entry = ttfFont.getDirectoryEntry(OFTableName.HMTX);

        if (entry != null) {
            for (Entry<Character, Integer> glyphSubset : mappedGlyphs.entrySet()) {
                char unicode = glyphSubset.getKey();
                int originalIndex = 0;
                int softFontGlyphIndex = glyphSubset.getValue();
                if (font instanceof MultiByteFont) {
                    originalIndex = ((MultiByteFont) font).getGIDFromChar(unicode);

                    writeUShort(hmtxTable, (softFontGlyphIndex) * 4,
                            ttfFont.getMtx().get(originalIndex).getWx());
                    writeUShort(hmtxTable, (softFontGlyphIndex) * 4 + 2,
                            ttfFont.getMtx().get(originalIndex).getLsb());
                } else {
                    originalIndex = ((SingleByteFont) font).getGIDFromChar(unicode);

                    writeUShort(hmtxTable, (softFontGlyphIndex) * 4,
                            ((SingleByteFont) font).getWidth(originalIndex, 1));
                    writeUShort(hmtxTable, (softFontGlyphIndex) * 4 + 2, 0);
                }
            }
        }
        return hmtxTable;
    }

    /**
     * Appends a USHORT to the output array, updates currentPost but not realSize
     */
    private void writeUShort(byte[] out, int offset, int s) {
        byte b1 = (byte) ((s >> 8) & 0xff);
        byte b2 = (byte) (s & 0xff);
        out[offset] = b1;
        out[offset + 1] = b2;
    }
}