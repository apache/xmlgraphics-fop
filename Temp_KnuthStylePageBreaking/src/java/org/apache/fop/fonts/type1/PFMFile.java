/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.fonts.type1;

// Java
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


// FOP
import org.apache.fop.fonts.Glyphs;

/**
 * This class represents a PFM file (or parts of it) as a Java object.
 */
public class PFMFile {

    // Header stuff
    private String windowsName;
    private String postscriptName;
    private short dfItalic;
    private int dfWeight;
    private short dfCharSet;
    private short dfPitchAndFamily;
    private int dfAvgWidth;
    private int dfMaxWidth;
    private int dfMinWidth;
    private short dfFirstChar;
    private short dfLastChar;

    // Extension stuff
    // ---

    // Extend Text Metrics
    private int etmCapHeight;
    private int etmXHeight;
    private int etmLowerCaseAscent;
    private int etmLowerCaseDescent;

    // Extent table
    private int[] extentTable;

    private Map kerningTab = new java.util.HashMap();

    /**
     * logging instance
     */
    protected Log log = LogFactory.getLog(PFMFile.class);

    /**
     * Parses a PFM file
     *
     * @param  inStream The stream from which to read the PFM file.
     * @throws IOException In case of an I/O problem
     */
    public void load(InputStream inStream) throws IOException {
        final byte[] buf = IOUtils.toByteArray(inStream);
        final InputStream bufin = new java.io.ByteArrayInputStream(buf);
        PFMInputStream in = new PFMInputStream(bufin);
        /*final int version =*/ in.readShort();
        final long filesize = in.readInt();
        if (filesize != buf.length) {
            log.warn("Effective file size is not the same as indicated in the header.");
        }
        bufin.reset();

        loadHeader(in);
        loadExtension(in);
    }

    /**
     * Parses the header of the PFM file.
     *
     * @param  inStream The stream from which to read the PFM file.
     * @throws IOException In case of an I/O problem
     */
    private void loadHeader(PFMInputStream inStream) throws IOException {
        inStream.skip(80);
        dfItalic = inStream.readByte();
        inStream.skip(2);
        dfWeight = inStream.readShort();
        dfCharSet = inStream.readByte();
        inStream.skip(4);
        dfPitchAndFamily = inStream.readByte();
        dfAvgWidth = inStream.readShort();
        dfMaxWidth = inStream.readShort();
        dfFirstChar = inStream.readByte();
        dfLastChar = inStream.readByte();
        inStream.skip(8);
        long faceOffset = inStream.readInt();

        inStream.reset();
        inStream.skip(faceOffset);
        windowsName = inStream.readString();

        inStream.reset();
        inStream.skip(117);
    }

    /**
     * Parses the extension part of the PFM file.
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtension(PFMInputStream inStream) throws IOException {
        final int size = inStream.readShort();
        if (size != 30) {
            log.warn("Size of extension block was expected to be "
                + "30 bytes, but was " + size + " bytes.");
        }
        final long extMetricsOffset = inStream.readInt();
        final long extentTableOffset = inStream.readInt();
        inStream.skip(4); //Skip dfOriginTable
        final long kernPairOffset = inStream.readInt();
        inStream.skip(4); //Skip dfTrackKernTable
        long driverInfoOffset = inStream.readInt();

        if (kernPairOffset > 0) {
            inStream.reset();
            inStream.skip(kernPairOffset);
            loadKernPairs(inStream);
        }

        inStream.reset();
        inStream.skip(driverInfoOffset);
        postscriptName = inStream.readString();

        if (extMetricsOffset != 0) {
            inStream.reset();
            inStream.skip(extMetricsOffset);
            loadExtMetrics(inStream);
        }
        if (extentTableOffset != 0) {
            inStream.reset();
            inStream.skip(extentTableOffset);
            loadExtentTable(inStream);
        }

    }

    /**
     * Parses the kernPairs part of the pfm file
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadKernPairs(PFMInputStream inStream) throws IOException {
        int i = inStream.readShort();


        log.info(i + " kerning pairs");
        while (i > 0) {
            int g1 = (int)inStream.readByte();
            i--;

            int g2 = (int)inStream.readByte();

            int adj = inStream.readShort();
            if (adj > 0x8000) {
                adj = -(0x10000 - adj);
            }
            log.debug("Char no: (" + g1 + ", " + g2 + ") kern: " + adj);

            if (log.isDebugEnabled()) {
                final String glyph1 = Glyphs.TEX8R_GLYPH_NAMES[g1];
                final String glyph2 = Glyphs.TEX8R_GLYPH_NAMES[g2];
                log.debug("glyphs: " + glyph1 + ", " + glyph2);
            }

            Map adjTab = (Map)kerningTab.get(new Integer(g1));
            if (adjTab == null) {
                adjTab = new java.util.HashMap();
            }
            adjTab.put(new Integer(g2), new Integer(adj));
            kerningTab.put(new Integer(g1), adjTab);
        }
    }

    /**
     * Parses the extended metrics part of the PFM file.
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtMetrics(PFMInputStream inStream) throws IOException {
        final int size = inStream.readShort();
        if (size != 52) {
            log.warn("Size of extension block was expected to be "
                + "52 bytes, but was " + size + " bytes.");
        }
        inStream.skip(12); //Skip etmPointSize, etmOrientation, etmMasterHeight,
                           //etmMinScale, etmMaxScale, emtMasterUnits
        etmCapHeight = inStream.readShort();
        etmXHeight = inStream.readShort();
        etmLowerCaseAscent = inStream.readShort();
        etmLowerCaseDescent = inStream.readShort();
        //Ignore the rest of the values
    }

    /**
     * Parses the extent table of the PFM file.
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtentTable(PFMInputStream inStream) throws IOException {
        extentTable = new int[dfLastChar - dfFirstChar + 1];
        dfMinWidth = dfMaxWidth;
        for (short i = dfFirstChar; i <= dfLastChar; i++) {
            extentTable[i - dfFirstChar] = inStream.readShort();
            if (extentTable[i - dfFirstChar] < dfMinWidth) {
                dfMinWidth = extentTable[i - dfFirstChar];
            }
        }
    }

    /**
     * Returns the Windows name of the font.
     *
     * @return The Windows name.
     */
    public String getWindowsName() {
        return windowsName;
    }

    /**
     * Return the kerning table. The kerning table is a Map with
     * strings with glyphnames as keys, containing Maps as value.
     * The value map contains a glyph name string key and an Integer value
     *
     * @return A Map containing the kerning table
     */
    public Map getKerning() {
        return kerningTab;
    }

    /**
     * Returns the Postscript name of the font.
     *
     * @return The Postscript name.
     */
    public String getPostscriptName() {
        return postscriptName;
    }

    /**
     * Returns the charset used for the font.
     *
     * @return The charset (0=WinAnsi).
     */
    public short getCharSet() {
        return dfCharSet;
    }

    /**
     * Returns the charset of the font as a string.
     *
     * @return The name of the charset.
     */
    public String getCharSetName() {
        switch (dfCharSet) {
        case 0:
            return "WinAnsi";
        case 128:
            return "Shift-JIS (Japanese)";
        default:
            return "Unknown";
        }
    }

    /**
     * Returns the number of the character that defines
     * the first entry in the widths list.
     *
     * @return The number of the first character.
     */
    public short getFirstChar() {
        return dfFirstChar;
    }

    /**
     * Returns the number of the character that defines
     * the last entry in the widths list.
     *
     * @return The number of the last character.
     */
    public short getLastChar() {
        return dfLastChar;
    }

    /**
     * Returns the CapHeight parameter for the font (height of uppercase H).
     *
     * @return The CapHeight parameter.
     */
    public int getCapHeight() {
        return etmCapHeight;
    }

    /**
     * Returns the XHeight parameter for the font (height of lowercase x).
     *
     * @return The CapHeight parameter.
     */
    public int getXHeight() {
        return etmXHeight;
    }

    /**
     * Returns the LowerCaseAscent parameter for the font (height of lowercase d).
     *
     * @return The LowerCaseAscent parameter.
     */
    public int getLowerCaseAscent() {
        return etmLowerCaseAscent;
    }

    /**
     * Returns the LowerCaseDescent parameter for the font (height of lowercase p).
     *
     * @return The LowerCaseDescent parameter.
     */
    public int getLowerCaseDescent() {
        return etmLowerCaseDescent;
    }

    /**
     * Tells whether the font has proportional character spacing.
     *
     * @return ex. true for Times, false for Courier.
     */
    public boolean getIsProportional() {
        return ((dfPitchAndFamily & 1) == 1);
    }

    /**
     * Returns the bounding box for the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     *
     * @return The calculated Font BBox.
     */
    public int[] getFontBBox() {
        int[] bbox = new int[4];

        // Just guessing....
        if (!getIsProportional() && (dfAvgWidth == dfMaxWidth)) {
            bbox[0] = -20;
        } else {
            bbox[0] = -100;
        }
        bbox[1] = -(getLowerCaseDescent() + 5);
        bbox[2] = dfMaxWidth + 10;
        bbox[3] = getLowerCaseAscent() + 5;
        return bbox;
    }

    /**
     * Returns the characteristics flags for the font as
     * needed for a PDF font descriptor (See PDF specs).
     *
     * @return The characteristics flags.
     */
    public int getFlags() {
        int flags = 0;
        if (!getIsProportional()) {
            flags |= 1;
        }
        if ((dfPitchAndFamily & 16) == 16) {
            flags |= 2;
        }
        if ((dfPitchAndFamily & 64) == 64) {
            flags |= 4;
        }
        if (dfCharSet == 0) {
            flags |= 6;
        }
        if (dfItalic != 0) {
            flags |= 7;
        }
        return flags;
    }

    /**
     * Returns the width of the dominant vertical stems of the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     *
     * @return The vertical stem width.
     */
    public int getStemV() {
        // Just guessing....
        if (dfItalic != 0) {
            return (int)Math.round(dfMinWidth * 0.25);
        } else {
            return (int)Math.round(dfMinWidth * 0.6);
        }
    }

    /**
     * Returns the italic angle of the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     *
     * @return The italic angle.
     */
    public int getItalicAngle() {
        if (dfItalic != 0) {
            return -16;    // Just guessing....
        } else {
            return 0;
        }
    }

    /**
     * Returns the width of a character
     *
     * @param  which The number of the character for which the width is requested.
     * @return The width of a character.
     */
    public int getCharWidth(short which) {
        return extentTable[which - dfFirstChar];
    }

}
