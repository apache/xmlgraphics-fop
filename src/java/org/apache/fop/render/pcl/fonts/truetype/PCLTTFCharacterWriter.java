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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fonts.truetype.GlyfTable;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.pcl.fonts.PCLCharacterDefinition;
import org.apache.fop.render.pcl.fonts.PCLCharacterDefinition.PCLCharacterClass;
import org.apache.fop.render.pcl.fonts.PCLCharacterDefinition.PCLCharacterFormat;
import org.apache.fop.render.pcl.fonts.PCLCharacterWriter;
import org.apache.fop.render.pcl.fonts.PCLSoftFont;

public class PCLTTFCharacterWriter extends PCLCharacterWriter {

    private List<OFMtxEntry> mtx;
    private OFDirTabEntry tabEntry;

    public PCLTTFCharacterWriter(PCLSoftFont softFont) throws IOException {
        super(softFont);
        softFont.setMtxCharIndexes(scanMtxCharacters());
    }

    @Override
    public byte[] writeCharacterDefinitions(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (char ch : text.toCharArray()) {
            int character = (int) ch;
            if (!font.hasPreviouslyWritten(character)) {
                PCLCharacterDefinition pclChar = getCharacterDefinition(ch);
                writePCLCharacter(baos, pclChar);
                List<PCLCharacterDefinition> compositeGlyphs = pclChar.getCompositeGlyphs();
                for (PCLCharacterDefinition composite : compositeGlyphs) {
                    writePCLCharacter(baos, composite);
                }
            }
        }
        return baos.toByteArray();
    }

    private void writePCLCharacter(ByteArrayOutputStream baos, PCLCharacterDefinition pclChar) throws IOException {
        baos.write(pclChar.getCharacterCommand());
        baos.write(pclChar.getCharacterDefinitionCommand());
        baos.write(pclChar.getData());
    }

    private Map<Integer, Integer> scanMtxCharacters() throws IOException {
        Map<Integer, Integer> charMtxOffsets = new HashMap<Integer, Integer>();
        List<OFMtxEntry> mtx = openFont.getMtx();
        OFTableName glyfTag = OFTableName.GLYF;
        if (openFont.seekTab(fontReader, glyfTag, 0)) {
            for (int i = 1; i < mtx.size(); i++) {
                OFMtxEntry entry = mtx.get(i);
                int charCode = 0;
                if (entry.getUnicodeIndex().size() > 0) {
                    charCode = (Integer) entry.getUnicodeIndex().get(0);
                } else {
                    charCode = entry.getIndex();
                }
                charMtxOffsets.put(charCode, i);
            }
        }
        return charMtxOffsets;
    }

    private PCLCharacterDefinition getCharacterDefinition(int unicode) throws IOException {
        if (mtx == null) {
            mtx = openFont.getMtx();
            tabEntry = openFont.getDirectoryEntry(OFTableName.GLYF);
        }
        if (openFont.seekTab(fontReader, OFTableName.GLYF, 0)) {
            int charIndex = font.getMtxCharIndex(unicode);

            // Fallback - only works for MultiByte fonts
            if (charIndex == 0) {
                charIndex = font.getCmapGlyphIndex(unicode);
            }

            Map<Integer, Integer> subsetGlyphs = new HashMap<Integer, Integer>();
            subsetGlyphs.put(charIndex, 1);

            byte[] glyphData = getGlyphData(charIndex);

            font.writeCharacter(unicode);

            PCLCharacterDefinition newChar = new PCLCharacterDefinition(charIndex, font.getUnicodeCodePoint(unicode),
                    PCLCharacterFormat.TrueType,
                    PCLCharacterClass.TrueType, glyphData, pclByteWriter);

            // Handle composite character definitions
            GlyfTable glyfTable = new GlyfTable(fontReader, mtx.toArray(new OFMtxEntry[mtx.size()]),
                    tabEntry, subsetGlyphs);
            if (glyfTable.isComposite(charIndex)) {
                Set<Integer> composite = glyfTable.retrieveComposedGlyphs(charIndex);
                for (Integer compositeIndex : composite) {
                    byte[] compositeData = getGlyphData(compositeIndex);
                    newChar.addCompositeGlyph(new PCLCharacterDefinition(compositeIndex, 65535,
                            PCLCharacterFormat.TrueType, PCLCharacterClass.TrueType, compositeData, pclByteWriter));
                }
            }

            return newChar;
        }
        return null;
    }

    private byte[] getGlyphData(int charIndex) throws IOException {
        OFMtxEntry entry = mtx.get(charIndex);
        OFMtxEntry nextEntry;
        int nextOffset = 0;
        if (charIndex < mtx.size() - 1) {
            nextEntry = mtx.get(charIndex + 1);
            nextOffset = (int) nextEntry.getOffset();
        } else {
            nextOffset = (int) ((TTFFile) openFont).getLastGlyfLocation();
        }
        int glyphOffset = (int) entry.getOffset();
        int glyphLength = nextOffset - glyphOffset;

        byte[] glyphData = new byte[0];
        if (glyphLength > 0) {
            glyphData = fontReader.getBytes((int) tabEntry.getOffset() + glyphOffset, glyphLength);
        }
        return glyphData;
    }
}
