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

package org.apache.fop.afp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.io.RandomAccessReadBuffer;

import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.truetype.CmapWriter;

public class AFPTrailerFontTestCase {
    private static final String FONT = "test/resources/fonts/ttf/DejaVuLGCSerif.ttf";

    @Test
    public void testExtractFullFontPreservesGlyphs() throws Exception {
        TrueTypeFont original = new TTFParser().parse(new RandomAccessReadBuffer(new FileInputStream(FONT)));
        TrueTypeFont extracted = extractFromTTC(wrapInTTC(new File(FONT)));
        Assert.assertEquals(original.getNumberOfGlyphs(), extracted.getNumberOfGlyphs());
        Assert.assertEquals(original.getTables().size(), extracted.getTables().size());
        Assert.assertEquals(original.getPath("A").getBounds(), extracted.getPath("A").getBounds());
    }

    @Test
    public void testAppendedCmapResolvesPuaToOriginalGlyph() throws Exception {
        int gidOfA = new TTFParser().parse(new RandomAccessReadBuffer(new FileInputStream(FONT)))
                .getCmap().getSubtable(3, 1).getGlyphId('A');
        byte[] fontData = AFPTrailerFont.extractFullFont(extractFromTTC(wrapInTTC(new File(FONT))));
        fontData = CmapWriter.appendCmap(fontData, new CMapSegment[] {new CMapSegment(0xE000, 0xE000, gidOfA)});
        TrueTypeFont withPua = new TTFParser().parse(new RandomAccessReadBuffer(new ByteArrayInputStream(fontData)));
        CmapSubtable subtable = withPua.getCmap().getSubtable(3, 1);
        Assert.assertEquals(gidOfA, subtable.getGlyphId(0xE000));
    }

    private TrueTypeFont extractFromTTC(byte[] ttc) throws Exception {
        TrueTypeCollection collection = new TrueTypeCollection(new ByteArrayInputStream(ttc));
        TrueTypeFont font = collection.getFontByName("DejaVuLGCSerif");
        return new TTFParser().parse(new RandomAccessReadBuffer(new ByteArrayInputStream(
                AFPTrailerFont.extractFullFont(font))));
    }

    private byte[] wrapInTTC(File ttf) throws Exception {
        byte[] sfnt = IOUtils.toByteArray(ttf.toURI().toURL());
        int header = 16;
        byte[] ttc = new byte[header + sfnt.length];
        ttc[0] = 't';
        ttc[1] = 't';
        ttc[2] = 'c';
        ttc[3] = 'f';
        writeUInt32(ttc, 4, 0x00010000);   // version 1.0
        writeUInt32(ttc, 8, 1);            // numFonts
        writeUInt32(ttc, 12, header);      // offset to font 0 directory
        System.arraycopy(sfnt, 0, ttc, header, sfnt.length);
        int numTables = readUInt16(ttc, header + 4);
        for (int i = 0; i < numTables; i++) {
            int offsetField = header + 12 + i * 16 + 8;
            writeUInt32(ttc, offsetField, readUInt32(ttc, offsetField) + header);
        }
        return ttc;
    }

    private static int readUInt16(byte[] data, int pos) {
        return ((data[pos] & 0xff) << 8) | (data[pos + 1] & 0xff);
    }

    private static int readUInt32(byte[] data, int pos) {
        return ((data[pos] & 0xff) << 24) | ((data[pos + 1] & 0xff) << 16)
                | ((data[pos + 2] & 0xff) << 8) | (data[pos + 3] & 0xff);
    }

    private static void writeUInt32(byte[] data, int pos, int value) {
        data[pos] = (byte) ((value >> 24) & 0xff);
        data[pos + 1] = (byte) ((value >> 16) & 0xff);
        data[pos + 2] = (byte) ((value >> 8) & 0xff);
        data[pos + 3] = (byte) (value & 0xff);
    }
}
