/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.pdf.fonts;

import org.apache.fop.fonts.FontFileReader;
import org.apache.fop.fonts.TTFSubSetFile;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.pdf.PDFCIDFont;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFTTFStream;
import org.apache.fop.pdf.PDFWArray;
import org.apache.fop.render.pdf.CIDFont;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.net.URL;

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

    public URL embedFileName = null;
    public String embedResourceName = null;
    public PDFTTFStream embeddedFont = null;

    public int flags = 4;
    public int stemV = 0;
    public int italicAngle = 0;
    public int missingWidth = 0;
    public int defaultWidth = 0;
    public byte cidType = PDFCIDFont.CID_TYPE2;

    public Map kerning = new java.util.HashMap();
    public boolean useKerning = true;
    private String namePrefix = null;    // Quasi unique prefix
    private static int uniqueCounter = 1;
    public PDFWArray warray = new PDFWArray();
    public int width[] = null;

    public BFEntry[] bfentries = null;


    /**
     * usedGlyphs contains orginal, new glyph index
     */
    private Map usedGlyphs = new java.util.HashMap();

    /**
     * usedGlyphsIndex contains new glyph, original index
     */
    private Map usedGlyphsIndex = new java.util.HashMap();
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

    public final Map getKerningInfo() {
        if (useKerning)
            return kerning;
        else
            return new java.util.HashMap();
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
            InputStream in = embedFileName.openStream();
            FontFileReader reader = new FontFileReader(in);
            in.close();
            TTFSubSetFile subset = new TTFSubSetFile();

            byte[] subsetFont = subset.readFont(reader, ttcName, usedGlyphs);
            // Only TrueType CID fonts are supported now

            embeddedFont = new PDFTTFStream(i, subsetFont.length);
            embeddedFont.addFilter("flate");
            embeddedFont.addFilter("ascii-85");
            embeddedFont.setData(subsetFont, subsetFont.length);
        } catch (IOException ioe) {
            MessageHandler.errorln("Failed to embed font [" + i + "] "
                                   + fontName + ": " + ioe.getMessage());
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










