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

package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;

public class PCLSoftFontManager {
    private Map<Typeface, PCLFontReader> fontReaderMap;
    private PCLFontReader fontReader;
    private List<PCLSoftFont> fonts = new ArrayList<PCLSoftFont>();

    private static final int SOFT_FONT_SIZE = 255;

    public PCLSoftFontManager(Map<Typeface, PCLFontReader> fontReaderMap) {
        this.fontReaderMap = fontReaderMap;
    }

    public ByteArrayOutputStream makeSoftFont(Typeface font, String text) throws IOException {
        List<Map<Character, Integer>> mappedGlyphs = mapFontGlyphs(font);
        if (!fontReaderMap.containsKey(font)) {
            fontReaderMap.put(font, PCLFontReaderFactory.createInstance(font));
        }
        fontReader = fontReaderMap.get(font);
        if (mappedGlyphs.isEmpty()) {
            mappedGlyphs.add(new HashMap<Character, Integer>());
        }
        if (fontReader != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PCLSoftFont softFont = null;
            for (Map<Character, Integer> glyphSet : mappedGlyphs) {
                softFont = getSoftFont(font, text, mappedGlyphs, softFont);
                softFont.setMappedChars(glyphSet);
                writeFontID(softFont.getFontID(), baos);
                writeFontHeader(softFont.getMappedChars(), baos);
                softFont.setCharacterOffsets(fontReader.getCharacterOffsets());
                softFont.setOpenFont(fontReader.getFontFile());
                softFont.setReader(fontReader.getFontFileReader());
                softFont.setMtxCharIndexes(fontReader.scanMtxCharacters());
            }
            return baos;
        } else {
            return null;
        }
    }

    private PCLSoftFont getSoftFont(Typeface font, String text, List<Map<Character, Integer>> mappedGlyphs,
                                    PCLSoftFont last) {
        if (text == null) {
            Iterator<PCLSoftFont> fontIterator = fonts.iterator();
            while (fontIterator.hasNext()) {
                PCLSoftFont sftFont = fontIterator.next();
                if (sftFont.getTypeface().equals(font)) {
                    fontIterator.remove();
                    return sftFont;
                }
            }
        }
        for (PCLSoftFont sftFont : fonts) {
            if (sftFont.getTypeface().equals(font) && sftFont != last
                    && (sftFont.getCharCount() + countNonMatches(sftFont, text)) < SOFT_FONT_SIZE) {
                return sftFont;
            }
        }
        PCLSoftFont f = new PCLSoftFont(fonts.size() + 1, font, mappedGlyphs.get(0).size() != 0);
        fonts.add(f);
        return f;
    }

    private List<Map<Character, Integer>> mapFontGlyphs(Typeface tf) {
        List<Map<Character, Integer>> mappedGlyphs = new ArrayList<Map<Character, Integer>>();
        if (tf instanceof CustomFontMetricsMapper) {
            CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper) tf;
            CustomFont customFont = (CustomFont) fontMetrics.getRealFont();
            mappedGlyphs = mapGlyphs(customFont.getUsedGlyphs(), customFont);
        }
        return mappedGlyphs;
    }

    private List<Map<Character, Integer>> mapGlyphs(Map<Integer, Integer> usedGlyphs, CustomFont font) {
        int charCount = 32;
        List<Map<Character, Integer>> mappedGlyphs = new ArrayList<Map<Character, Integer>>();
        Map<Character, Integer> fontGlyphs = new HashMap<Character, Integer>();
        for (Entry<Integer, Integer> entry : usedGlyphs.entrySet()) {
            int glyphID = entry.getKey();
            if (glyphID == 0) {
                continue;
            }
            char unicode = font.getUnicodeFromGID(glyphID);
            if (charCount > SOFT_FONT_SIZE) {
                mappedGlyphs.add(fontGlyphs);
                charCount = 32;
                fontGlyphs = new HashMap<Character, Integer>();
            }
            fontGlyphs.put(unicode, charCount++);
        }
        if (fontGlyphs.size() > 0) {
            mappedGlyphs.add(fontGlyphs);
        }
        return mappedGlyphs;
    }

    private void writeFontID(int fontID, OutputStream os) throws IOException {
        os.write(assignFontID(fontID));
    }

    public byte[] assignFontID(int fontID) throws IOException {
        return PCLByteWriterUtil.writeCommand(String.format("*c%dD", fontID));
    }

    private void writeFontHeader(Map<Character, Integer> mappedGlyphs, OutputStream os) throws IOException {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getDescriptorSize()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getHeaderFormat()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getFontType()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getStyleMSB()));
        header.write(0); // Reserved
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getBaselinePosition()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getCellWidth()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getCellHeight()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getOrientation()));
        header.write(fontReader.getSpacing());
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getSymbolSet()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getPitch()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getHeight()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getXHeight()));
        header.write(PCLByteWriterUtil.signedByte(fontReader.getWidthType()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getStyleLSB()));
        header.write(PCLByteWriterUtil.signedByte(fontReader.getStrokeWeight()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getTypefaceLSB()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getTypefaceMSB()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getSerifStyle()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getQuality()));
        header.write(PCLByteWriterUtil.signedByte(fontReader.getPlacement()));
        header.write(PCLByteWriterUtil.signedByte(fontReader.getUnderlinePosition()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getUnderlineThickness()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getTextHeight()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getTextWidth()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getFirstCode()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getLastCode()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getPitchExtended()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getHeightExtended()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getCapHeight()));
        header.write(PCLByteWriterUtil.unsignedLongInt(fontReader.getFontNumber()));
        header.write(PCLByteWriterUtil.padBytes(fontReader.getFontName().getBytes("US-ASCII"), 16, 32));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getScaleFactor()));
        header.write(PCLByteWriterUtil.signedInt(fontReader.getMasterUnderlinePosition()));
        header.write(PCLByteWriterUtil.unsignedInt(fontReader.getMasterUnderlineThickness()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getFontScalingTechnology()));
        header.write(PCLByteWriterUtil.unsignedByte(fontReader.getVariety()));

        writeSegmentedFontData(header, mappedGlyphs);

        os.write(getFontHeaderCommand(header.size()));
        os.write(header.toByteArray());
    }

    private void writeSegmentedFontData(ByteArrayOutputStream header,
            Map<Character, Integer> mappedGlyphs) throws IOException {
        List<PCLFontSegment> fontSegments = fontReader.getFontSegments(mappedGlyphs);
        for (PCLFontSegment segment : fontSegments) {
            writeFontSegment(header, segment);
        }
        header.write(0); // Reserved
        // Checksum must equal 0 when added to byte 64 offset (modulo 256)
        long sum = 0;
        byte[] headerBytes = header.toByteArray();
        for (int i = 64; i < headerBytes.length; i++) {
            sum += headerBytes[i];
        }
        int remainder = (int) (sum % 256);
        header.write(256 - remainder);
    }

    private byte[] getFontHeaderCommand(int headerSize) throws IOException {
        return PCLByteWriterUtil.writeCommand(String.format(")s%dW", headerSize));
    }

    private void writeFontSegment(ByteArrayOutputStream header, PCLFontSegment segment) throws IOException {
        header.write(PCLByteWriterUtil.unsignedInt(segment.getIdentifier().getValue()));
        header.write(PCLByteWriterUtil.unsignedInt(segment.getData().length));
        header.write(segment.getData());
    }

    /**
     * Finds a soft font associated with the given typeface. If more than one instance of the font exists (as each font
     * is bound and restricted to 255 characters) it will find the last font with available capacity.
     * @param font The typeface associated with the soft font
     * @return Returns the PCLSoftFont with available capacity
     */
    public PCLSoftFont getSoftFont(Typeface font, String text) {
        for (PCLSoftFont sftFont : fonts) {
            if (sftFont.getTypeface().equals(font)
                    && sftFont.getCharCount() + countNonMatches(sftFont, text) < SOFT_FONT_SIZE) {
                return sftFont;
            }
        }
        return null;
    }

    public PCLSoftFont getSoftFontFromID(int index) {
        return fonts.get(index - 1);
    }

    private int countNonMatches(PCLSoftFont font, String text) {
        int result = 0;
        for (char ch : text.toCharArray()) {
            int value = font.getUnicodeCodePoint(ch);
            if (value == -1) {
                result++;
            }
        }
        return result;
    }

    public int getSoftFontID(Typeface tf) throws IOException {
        PCLSoftFont font = getSoftFont(tf, "");
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).equals(font)) {
                return i + 1;
            }
        }
        return -1;
    }

    public List<PCLTextSegment> getTextSegments(String text, Typeface font) {
        List<PCLTextSegment> textSegments = new ArrayList<PCLTextSegment>();
        int curFontID = -1;
        String current = "";
        for (char ch : text.toCharArray()) {
            for (PCLSoftFont softFont : fonts) {
                if (curFontID == -1) {
                    curFontID = softFont.getFontID();
                }
                if (softFont.getCharIndex(ch) == -1 || !softFont.getTypeface().equals(font)) {
                    continue;
                }
                if (current.length() > 0 && curFontID != softFont.getFontID()) {
                    textSegments.add(new PCLTextSegment(curFontID, current));
                    current = "";
                    curFontID = softFont.getFontID();
                }
                if (curFontID != softFont.getFontID()) {
                    curFontID = softFont.getFontID();
                }
                current += ch;
                break;
            }
        }
        if (current.length() > 0) {
            textSegments.add(new PCLTextSegment(curFontID, current));
        }
        return textSegments;
    }

    public static class PCLTextSegment {
        private String text;
        private int fontID;

        public PCLTextSegment(int fontID, String text) {
            this.text = text;
            this.fontID = fontID;
        }

        public String getText() {
            return text;
        }

        public int getFontID() {
            return fontID;
        }
    }
}
