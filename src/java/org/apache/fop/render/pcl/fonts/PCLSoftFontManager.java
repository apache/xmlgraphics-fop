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
import java.util.ArrayList;
import java.util.List;

import org.apache.fop.fonts.Typeface;

public class PCLSoftFontManager {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PCLFontReader fontReader;
    private PCLByteWriterUtil pclByteWriter = new PCLByteWriterUtil();
    private int byte64Offset;
    private List<PCLSoftFont> fonts = new ArrayList<PCLSoftFont>();
    private PCLFontReaderFactory fontReaderFactory;

    public ByteArrayOutputStream makeSoftFont(Typeface font) throws IOException {
        PCLSoftFont softFont = new PCLSoftFont(fonts.size() + 1, font);
        fontReaderFactory = PCLFontReaderFactory.getInstance(pclByteWriter);
        fontReader = fontReaderFactory.createInstance(font);
        if (fontReader != null) {
            initialize();
            assignFontID();
            writeFontHeader();
            softFont.setCharacterOffsets(fontReader.getCharacterOffsets());
            softFont.setOpenFont(fontReader.getFontFile());
            softFont.setReader(fontReader.getFontFileReader());
            fonts.add(softFont);
            return baos;
        } else {
            return null;
        }
    }

    private void initialize() {
        baos.reset();
    }

    private void assignFontID() throws IOException {
        baos.write(pclByteWriter.writeCommand(String.format("*c%dD", fonts.size() + 1)));
    }

    private void writeFontHeader() throws IOException {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(pclByteWriter.unsignedInt(fontReader.getDescriptorSize()));
        header.write(pclByteWriter.unsignedByte(fontReader.getHeaderFormat()));
        header.write(pclByteWriter.unsignedByte(fontReader.getFontType()));
        header.write(pclByteWriter.unsignedByte(fontReader.getStyleMSB()));
        header.write(0); // Reserved
        header.write(pclByteWriter.unsignedInt(fontReader.getBaselinePosition()));
        header.write(pclByteWriter.unsignedInt(fontReader.getCellWidth()));
        header.write(pclByteWriter.unsignedInt(fontReader.getCellHeight()));
        header.write(pclByteWriter.unsignedByte(fontReader.getOrientation()));
        header.write(fontReader.getSpacing());
        header.write(pclByteWriter.unsignedInt(fontReader.getSymbolSet()));
        header.write(pclByteWriter.unsignedInt(fontReader.getPitch()));
        header.write(pclByteWriter.unsignedInt(fontReader.getHeight()));
        header.write(pclByteWriter.unsignedInt(fontReader.getXHeight()));
        header.write(pclByteWriter.signedByte(fontReader.getWidthType()));
        header.write(pclByteWriter.unsignedByte(fontReader.getStyleLSB()));
        header.write(pclByteWriter.signedByte(fontReader.getStrokeWeight()));
        header.write(pclByteWriter.unsignedByte(fontReader.getTypefaceLSB()));
        header.write(pclByteWriter.unsignedByte(fontReader.getTypefaceMSB()));
        header.write(pclByteWriter.unsignedByte(fontReader.getSerifStyle()));
        header.write(pclByteWriter.unsignedByte(fontReader.getQuality()));
        header.write(pclByteWriter.signedByte(fontReader.getPlacement()));
        header.write(pclByteWriter.signedByte(fontReader.getUnderlinePosition()));
        header.write(pclByteWriter.unsignedByte(fontReader.getUnderlineThickness()));
        header.write(pclByteWriter.unsignedInt(fontReader.getTextHeight()));
        header.write(pclByteWriter.unsignedInt(fontReader.getTextWidth()));
        header.write(pclByteWriter.unsignedInt(fontReader.getFirstCode()));
        header.write(pclByteWriter.unsignedInt(fontReader.getLastCode()));
        header.write(pclByteWriter.unsignedByte(fontReader.getPitchExtended()));
        header.write(pclByteWriter.unsignedByte(fontReader.getHeightExtended()));
        header.write(pclByteWriter.unsignedInt(fontReader.getCapHeight()));
        header.write(pclByteWriter.unsignedLongInt(fontReader.getFontNumber()));
        header.write(pclByteWriter.padBytes(fontReader.getFontName().getBytes("US-ASCII"), 16, 32));
        // Byte 64 starting point stored for checksum
        byte64Offset = header.size();
        header.write(pclByteWriter.unsignedInt(fontReader.getScaleFactor()));
        header.write(pclByteWriter.signedInt(fontReader.getMasterUnderlinePosition()));
        header.write(pclByteWriter.unsignedInt(fontReader.getMasterUnderlineThickness()));
        header.write(pclByteWriter.unsignedByte(fontReader.getFontScalingTechnology()));
        header.write(pclByteWriter.unsignedByte(fontReader.getVariety()));

        writeSegmentedFontData(header, byte64Offset);

        baos.write(getFontHeaderCommand(header.size()));
        baos.write(header.toByteArray());
    }

    private void writeSegmentedFontData(ByteArrayOutputStream header, int byte64Offset) throws IOException {
        List<PCLFontSegment> fontSegments = fontReader.getFontSegments();
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
        return pclByteWriter.writeCommand(String.format(")s%dW", headerSize));
    }

    private void writeFontSegment(ByteArrayOutputStream header, PCLFontSegment segment) throws IOException {
        header.write(pclByteWriter.unsignedInt(segment.getIdentifier().getValue()));
        header.write(pclByteWriter.unsignedInt(segment.getData().length));
        header.write(segment.getData());
    }

    public List<PCLSoftFont> getSoftFonts() {
        return fonts;
    }

    /**
     * Finds a soft font associated with the given typeface. If more than one instance of the font exists (as each font
     * is bound and restricted to 255 characters) it will find the last font with available capacity.
     * @param font The typeface associated with the soft font
     * @return Returns the PCLSoftFont with available capacity
     */
    public PCLSoftFont getSoftFont(Typeface font) {
        for (PCLSoftFont sftFont : fonts) {
            if (sftFont.getTypeface().equals(font) && sftFont.getCharCount() < 255) {
                return sftFont;
            }
        }
        return null;
    }

    public int getSoftFontID(Typeface tf) throws IOException {
        PCLSoftFont font = getSoftFont(tf);
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).equals(font)) {
                return i + 1;
            }
        }
        return -1;
    }

    public byte[] writeFontIDCommand(int fontID) throws IOException {
        return pclByteWriter.writeCommand(String.format("*c%dD", fontID));
    }
}
