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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.fop.fonts.CMapSegment;

/**
 * Rewrites the {@code cmap} table of an sfnt (TrueType) font so that it contains the character to
 * glyph-index mappings held by FOP for that font, including the Private Use Area code points that
 * {@code MultiByteFont.createPrivateUseMapping} allocates for complex-script (GSUB) output.
 *
 * <p>The AFP renderer writes text as UTF-16 code points and the AFP consumer resolves each code
 * point to a glyph through the embedded font's {@code cmap}. Substituted glyphs are exposed by FOP
 * as PUA code points which are, by construction, absent from the original font's {@code cmap};
 * without this rewrite they would not resolve.</p>
 *
 * <p>The rewrite is deliberately minimal: the original font bytes are preserved, a fresh
 * {@code cmap} table (a single Windows BMP, format&nbsp;4 subtable) is appended on a 4-byte
 * boundary, and only the {@code cmap} entry in the sfnt table directory is repointed to it. The
 * original {@code cmap} bytes remain in the file as unreferenced dead space. The font's glyph
 * numbering is left untouched, so the {@link CMapSegment#getGlyphStartIndex() glyph indices} line up
 * with the embedded glyphs.</p>
 */
public final class CmapWriter {

    private static final int SFNT_HEADER_SIZE = 12;
    private static final int DIR_ENTRY_SIZE = 16;
    private static final int BMP_MAX = 0xFFFF;
    private static final long CHECKSUM_MAGIC = 0xB1B0AFBAL;

    private CmapWriter() {
    }

    /**
     * Returns a copy of the given TrueType font whose {@code cmap} table maps the supplied segments.
     *
     * @param font the original TrueType font bytes (must keep its original glyph numbering)
     * @param segments the character-to-glyph mappings to encode (original mappings plus PUA additions)
     * @return the rewritten font bytes
     * @throws IOException if the font has no {@code cmap} or {@code head} table
     */
    public static byte[] appendCmap(byte[] font, CMapSegment[] segments) throws IOException {
        int numTables = readUShort(font, 4);
        int cmapRecordOffset = -1;
        int headTableOffset = -1;
        for (int i = 0; i < numTables; i++) {
            int recordOffset = SFNT_HEADER_SIZE + i * DIR_ENTRY_SIZE;
            String tag = readTag(font, recordOffset);
            if ("cmap".equals(tag)) {
                cmapRecordOffset = recordOffset;
            } else if ("head".equals(tag)) {
                headTableOffset = readULong(font, recordOffset + 8);
            }
        }
        if (cmapRecordOffset < 0) {
            throw new IOException("Font has no cmap table to rewrite");
        }
        if (headTableOffset < 0) {
            throw new IOException("Font has no head table");
        }

        byte[] cmapTable = buildCmapTable(segments);

        int appendOffset = align4(font.length);
        int newLength = align4(appendOffset + cmapTable.length);
        byte[] out = new byte[newLength];
        System.arraycopy(font, 0, out, 0, font.length);
        System.arraycopy(cmapTable, 0, out, appendOffset, cmapTable.length);

        // Repoint the cmap directory entry at the appended table.
        writeULong(out, cmapRecordOffset + 4, TTFSubSetFile.getCheckSum(out, appendOffset, cmapTable.length));
        writeULong(out, cmapRecordOffset + 8, appendOffset);
        writeULong(out, cmapRecordOffset + 12, cmapTable.length);

        // Recompute head.checkSumAdjustment over the whole file (with the field zeroed first).
        writeULong(out, headTableOffset + 8, 0);
        int checkSum = TTFSubSetFile.getCheckSum(out, 0, newLength);
        writeULong(out, headTableOffset + 8, (int) (CHECKSUM_MAGIC - (checkSum & 0xFFFFFFFFL)));

        return out;
    }

    /** Builds a complete cmap table: table header + one (3,1) encoding record + a format-4 subtable. */
    private static byte[] buildCmapTable(CMapSegment[] segments) throws IOException {
        byte[] subtable = buildFormat4Subtable(segments);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        out.writeShort(0);      // table version
        out.writeShort(1);      // number of encoding records
        out.writeShort(3);      // platformID: Windows
        out.writeShort(1);      // encodingID: Unicode BMP
        out.writeInt(12);       // offset to subtable (4 header + 8 encoding record)
        out.write(subtable);
        out.flush();
        return bos.toByteArray();
    }

    /** Builds a format-4 (segment mapping to delta values) subtable for BMP code points. */
    private static byte[] buildFormat4Subtable(CMapSegment[] segments) throws IOException {
        List<int[]> ranges = buildRanges(segments);
        // Mandatory terminating segment 0xFFFF -> 0xFFFF.
        ranges.add(new int[] {BMP_MAX, BMP_MAX, 1});

        int segCount = ranges.size();
        int segCountX2 = segCount * 2;
        int entrySelector = 0;
        while ((1 << (entrySelector + 1)) <= segCount) {
            entrySelector++;
        }
        int searchRange = (1 << entrySelector) * 2;
        int rangeShift = segCountX2 - searchRange;

        int length = 16 + 8 * segCount;   // header (14) + reservedPad (2) + 4 arrays of segCount USHORTs

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        out.writeShort(4);              // format
        out.writeShort(length);
        out.writeShort(0);              // language
        out.writeShort(segCountX2);
        out.writeShort(searchRange);
        out.writeShort(entrySelector);
        out.writeShort(rangeShift);
        for (int[] range : ranges) {    // endCode[]
            out.writeShort(range[1]);
        }
        out.writeShort(0);              // reservedPad
        for (int[] range : ranges) {    // startCode[]
            out.writeShort(range[0]);
        }
        for (int[] range : ranges) {    // idDelta[]
            out.writeShort(range[2]);
        }
        for (int i = 0; i < segCount; i++) {    // idRangeOffset[] (all zero)
            out.writeShort(0);
        }
        out.flush();
        return bos.toByteArray();
    }

    /**
     * Converts the FOP cmap segments into sorted, non-overlapping BMP ranges of
     * {startCode, endCode, idDelta}. Supplementary-plane segments are dropped and ranges are clamped
     * to the BMP; the terminating 0xFFFF segment is added by the caller.
     */
    private static List<int[]> buildRanges(CMapSegment[] segments) {
        CMapSegment[] sorted = segments.clone();
        Arrays.sort(sorted, new Comparator<CMapSegment>() {
            public int compare(CMapSegment a, CMapSegment b) {
                return Integer.compare(a.getUnicodeStart(), b.getUnicodeStart());
            }
        });
        List<int[]> ranges = new ArrayList<>();
        int lastEnd = -1;
        for (CMapSegment segment : sorted) {
            int start = segment.getUnicodeStart();
            int end = segment.getUnicodeEnd();
            if (start >= BMP_MAX || start <= lastEnd) {
                continue;   // cannot be represented in a format-4 subtable
            }
            if (end >= BMP_MAX) {
                end = BMP_MAX - 1;
            }
            int idDelta = (segment.getGlyphStartIndex() - start) & 0xFFFF;
            ranges.add(new int[] {start, end, idDelta});
            lastEnd = end;
        }
        return ranges;
    }

    private static int align4(int value) {
        int pad = 4 - (value % 4);
        return pad < 4 ? value + pad : value;
    }

    private static String readTag(byte[] data, int pos) {
        return new String(new char[] {
            (char) (data[pos] & 0xff), (char) (data[pos + 1] & 0xff),
            (char) (data[pos + 2] & 0xff), (char) (data[pos + 3] & 0xff),
        });
    }

    private static int readUShort(byte[] data, int pos) {
        return ((data[pos] & 0xff) << 8) | (data[pos + 1] & 0xff);
    }

    private static int readULong(byte[] data, int pos) {
        return ((data[pos] & 0xff) << 24) | ((data[pos + 1] & 0xff) << 16)
                | ((data[pos + 2] & 0xff) << 8) | (data[pos + 3] & 0xff);
    }

    private static void writeULong(byte[] data, int pos, int value) {
        data[pos] = (byte) ((value >> 24) & 0xff);
        data[pos + 1] = (byte) ((value >> 16) & 0xff);
        data[pos + 2] = (byte) ((value >> 8) & 0xff);
        data[pos + 3] = (byte) (value & 0xff);
    }
}
