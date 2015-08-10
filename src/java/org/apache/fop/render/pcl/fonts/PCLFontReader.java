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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OpenFont;

public abstract class PCLFontReader {

    protected Typeface typeface;
    protected PCLByteWriterUtil pclByteWriter;
    protected CustomFont font;

    public PCLFontReader(Typeface font, PCLByteWriterUtil pclByteWriter) {
        this.typeface = font;
        this.pclByteWriter = pclByteWriter;
    }

    public void setFont(CustomFont mbFont) {
        this.font = mbFont;
    }

    /** Header Data **/
    public abstract int getDescriptorSize();
    public abstract int getHeaderFormat();
    public abstract int getFontType();
    public abstract int getStyleMSB();
    public abstract int getBaselinePosition();
    public abstract int getCellWidth();
    public abstract int getCellHeight();
    public abstract int getOrientation();
    public abstract int getSpacing();
    public abstract int getSymbolSet();
    public abstract int getPitch();
    public abstract int getHeight();
    public abstract int getXHeight();
    public abstract int getWidthType();
    public abstract int getStyleLSB();
    public abstract int getStrokeWeight();
    public abstract int getTypefaceLSB();
    public abstract int getTypefaceMSB();
    public abstract int getSerifStyle();
    public abstract int getQuality();
    public abstract int getPlacement();
    public abstract int getUnderlinePosition();
    public abstract int getUnderlineThickness();
    public abstract int getTextHeight();
    public abstract int getTextWidth();
    public abstract int getFirstCode();
    public abstract int getLastCode();
    public abstract int getPitchExtended();
    public abstract int getHeightExtended();
    public abstract int getCapHeight();
    public abstract int getFontNumber();
    public abstract String getFontName();
    public abstract int getScaleFactor() throws IOException;
    public abstract int getMasterUnderlinePosition() throws IOException;
    public abstract int getMasterUnderlineThickness() throws IOException;
    public abstract int getFontScalingTechnology();
    public abstract int getVariety();

    /** Segmented Font Data **/
    public abstract List<PCLFontSegment> getFontSegments(Map<Character, Integer> mappedGlyphs)
            throws IOException;

    /** Character Definitions **/
    public abstract Map<Integer, int[]> getCharacterOffsets() throws IOException;

    public abstract OpenFont getFontFile();
    public abstract FontFileReader getFontFileReader();

    /**
     * Gets the most significant byte from a 16-bit integer
     * @param s The number
     * @return The resulting byte value as an integer
     */
    protected int getMSB(int s) {
        return s >> 8;
    }

    /**
     * Gets the least significant byte from a 16-bit integer
     * @param s The number
     * @return The resulting byte value as an integer
     */
    protected int getLSB(int s) {
        byte b1 = (byte) (s >> 8);
        return s;
    }
}
