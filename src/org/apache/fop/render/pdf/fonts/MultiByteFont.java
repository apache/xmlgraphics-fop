/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.fonts.TTFSubSetFile;
import org.apache.fop.fonts.FontFileReader;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFWArray;
import org.apache.fop.pdf.PDFCIDFont;
import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.CMap;
import org.apache.fop.pdf.PDFTTFStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.util.HashMap;

/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont implements FontDescriptor {
    public String fontName = null;
    public String ttcName = null;
    public String encoding = "Identity-H";

    public int capHeight = 0;
    public int xHeight = 0;
    public int ascender = 0;
    public int descender = 0;
    public int[] fontBBox = {
        0, 0, 0, 0
    };

    public String embedFileName = null;
    public String embedResourceName = null;
    public PDFTTFStream embeddedFont = null;

    public int flags = 4;
    public int stemV = 0;
    public int italicAngle = 0;
    public int missingWidth = 0;
    public int defaultWidth = 0;
    public byte cidType = PDFCIDFont.CID_TYPE2;

    public HashMap kerning = new HashMap();
    public boolean useKerning = true;
    private String namePrefix = null;    // Quasi unique prefix
    private static int uniqueCounter = 1;
    public PDFWArray warray = new PDFWArray();
    public int width[] = null;

    public BFEntry[] bfentries = null;


    /**
     * usedGlyphs contains orginal, new glyph index
     */
    private HashMap usedGlyphs = new HashMap();

    /**
     * usedGlyphsIndex contains new glyph, original index
     */
    private HashMap usedGlyphsIndex = new HashMap();
    int usedGlyphsCount = 0;

    public MultiByteFont() {
        // Make sure that the 3 first glyphs are included
        usedGlyphs.put(new Integer(0), new Integer(0));
        usedGlyphsIndex.put(new Integer(0), new Integer(0));
        usedGlyphsCount++;
        usedGlyphs.put(new Integer(1), new Integer(1));
        usedGlyphsIndex.put(new Integer(1), new Integer(1));
        usedGlyphsCount++;
        usedGlyphs.put(new Integer(2), new Integer(2));
        usedGlyphsIndex.put(new Integer(2), new Integer(2));
        usedGlyphsCount++;

        // Create a quasiunique prefix for fontname
        int cnt = 0;
        synchronized (this.getClass()) {
            cnt = uniqueCounter++;
        }
        int ctm = (int)(System.currentTimeMillis() & 0xffff);
        namePrefix = new String(cnt + "E" + Integer.toHexString(ctm));
    }

    public final boolean hasKerningInfo() {
        return (useKerning & kerning.isEmpty());
    }

    public final java.util.HashMap getKerningInfo() {
        if (useKerning)
            return kerning;
        else
            return new HashMap();
    }

    public byte getSubType() {
        return org.apache.fop.pdf.PDFFont.TYPE0;
    }

    public String getLang() {
        return null;
    }

    public String getPanose() {
        return null;
    }

    public int getAvgWidth() {
        return -1;
    }

    public int getMinWidth() {
        return -1;
    }

    public int getMaxWidth() {
        return -1;
    }

    public int getleading() {
        return -1;
    }

    public int getStemH() {
        return 0;
    }

    public int getMissingWidth() {
        return missingWidth;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public String getRegistry() {
        return "Adobe";
    }

    public String getOrdering() {
        return "UCS";
    }

    public int getSupplement() {
        return 0;
    }

    public byte getCidType() {
        return cidType;
    }

    public String getCidBaseFont() {
        return isEmbeddable() ? namePrefix + fontName : fontName;
    }

    public String getCharEncoding() {
        return "Identity-H";
    }

    public PDFWArray getWidths() {
        if (isEmbeddable()) {
            // Create widths for reencoded chars
            warray = new PDFWArray();
            int[] tmpWidth = new int[usedGlyphsCount];

            for (int i = 0; i < usedGlyphsCount; i++) {
                Integer nw = (Integer)usedGlyphsIndex.get(new Integer(i));
                int nwx = (nw == null) ? 0 : nw.intValue();
                tmpWidth[i] = width[nwx];
            }
            warray.addEntry(0, tmpWidth);
        }
        return warray;
    }

    public boolean isEmbeddable() {
        return (embedFileName == null && embedResourceName == null) ? false
               : true;
    }


    public PDFStream getFontFile(int i) {
        try {
            FontFileReader reader = new FontFileReader(embedFileName);
            TTFSubSetFile subset = new TTFSubSetFile();

            byte[] subsetFont = subset.readFont(reader, ttcName, usedGlyphs);
            // Only TrueType CID fonts are supported now

            embeddedFont = new PDFTTFStream(i, subsetFont.length);
            embeddedFont.addFilter("flate");
            embeddedFont.addFilter("ascii-85");
            embeddedFont.setData(subsetFont, subsetFont.length);
        } catch (IOException ioe) {
            //log.error("Failed to embed font [" + i + "] "
            //                       + fontName + ": " + ioe.getMessage());
            return (PDFStream)null;
        }

        return (PDFStream)embeddedFont;
    }

    public String encoding() {
        return encoding;
    }

    public String fontName() {
        return isEmbeddable() ? namePrefix + fontName : fontName;
    }

    public int getAscender() {
        return ascender;
    }

    public int getDescender() {
        return descender;
    }

    public int getCapHeight() {
        return capHeight;
    }

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getFlags() {
        return flags;
    }

    public int[] getFontBBox() {
        return fontBBox;
    }

    public int getItalicAngle() {
        return italicAngle;
    }

    public int getStemV() {
        return stemV;
    }

    public int getFirstChar() {
        return 0;
    }

    public int getLastChar() {
        return 255;
    }

    public int width(int i, int size) {
        if (isEmbeddable()) {
            Integer idx = (Integer)usedGlyphsIndex.get(new Integer(i));
            return size * width[idx.intValue()];
        } else {
            return size * width[i];
        }
    }

    public int[] getWidths(int size) {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        for (int i = 0; i < arr.length; i++)
            arr[i] *= size;
        return arr;
    }

    public Integer reMap(Integer i) {
        if (isEmbeddable()) {
            Integer ret = (Integer)usedGlyphsIndex.get(i);
            if (ret == null)
                ret = i;
            return ret;
        } else {
            return i;
        }

    }

    public char mapChar(char c) {
        int idx = (int)c;
        int retIdx = 0;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {
            if (bfentries[i].unicodeStart <= idx
                    && bfentries[i].unicodeEnd >= idx) {
                retIdx = bfentries[i].glyphStartIndex + idx
                         - bfentries[i].unicodeStart;
            }
        }

        if (isEmbeddable()) {
            // Reencode to a new subset font or get
            // the reencoded value
            Integer newIdx = (Integer)usedGlyphs.get(new Integer(retIdx));
            if (newIdx == null) {
                usedGlyphs.put(new Integer(retIdx),
                               new Integer(usedGlyphsCount));
                usedGlyphsIndex.put(new Integer(usedGlyphsCount),
                                    new Integer(retIdx));
                retIdx = usedGlyphsCount;
                // System.out.println(c+"("+(int)c+") = "+retIdx);
                usedGlyphsCount++;
            } else {
                retIdx = newIdx.intValue();
            }
        }

        return (char)retIdx;
    }

}

