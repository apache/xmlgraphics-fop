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

package org.apache.fop.fonts;

//Java
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont {

    private static int uniqueCounter = -1;

    private String ttcName = null;
    private String encoding = "Identity-H";

    private int defaultWidth = 0;
    private CIDFontType cidType = CIDFontType.CIDTYPE2;

    private String namePrefix = null;    // Quasi unique prefix

    private BFEntry[] bfentries = null;

    /**
     * Default constructor
     */
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
        synchronized (this.getClass()) {
            uniqueCounter++;
            if (uniqueCounter > 99999 || uniqueCounter < 0) {
                uniqueCounter = 0; //We need maximum 5 character then we start again
            }
        }
        DecimalFormat counterFormat = new DecimalFormat("00000");
        String cntString = counterFormat.format(uniqueCounter);
        
        //Subset prefix as described in chapter 5.5.3 of PDF 1.4
        StringBuffer sb = new StringBuffer("E");
        for (int i = 0, c = cntString.length(); i < c; i++) {
            //translate numbers to uppercase characters
            sb.append((char)(cntString.charAt(i) + (65 - 48)));
        }
        sb.append("+");
        namePrefix = sb.toString();

        setFontType(FontType.TYPE0);
    }

    /**
     * {@inheritDoc}
     */
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /**
     * {@inheritDoc}
     */
    public String getRegistry() {
        return "Adobe";
    }

    /**
     * {@inheritDoc}
     */
    public String getOrdering() {
        return "UCS";
    }

    /**
     * {@inheritDoc}
     */
    public int getSupplement() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public CIDFontType getCIDType() {
        return cidType;
    }

    /**
     * Sets the CIDType.
     * @param cidType The cidType to set
     */
    public void setCIDType(CIDFontType cidType) {
        this.cidType = cidType;
    }
    
    private String getPrefixedFontName() {
        return namePrefix + FontUtil.stripWhiteSpace(super.getFontName());
    }
    
    /** {@inheritDoc} */
    public String getEmbedFontName() {
        if (isEmbeddable()) {
            return getPrefixedFontName();
        } else {
            return super.getFontName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmbeddable() {
        return !(getEmbedFileName() == null && getEmbedResourceName() == null);
    }

    /**
     * {@inheritDoc}
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * {@inheritDoc} 
     */
    public int getWidth(int i, int size) {
        if (isEmbeddable()) {
            Integer idx = (Integer)usedGlyphsIndex.get(new Integer(i));
            return size * width[idx.intValue()];
        } else {
            return size * width[i];
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        /*
        for (int i = 0; i < arr.length; i++)
            arr[i] *= size;
        */
        return arr;
    }

    /**
     * Remaps a codepoint based.
     * @param i codepoint to remap
     * @return new codepoint
     */
/* unused
    public Integer reMap(Integer i) {
        if (isEmbeddable()) {
            Integer ret = (Integer)usedGlyphsIndex.get(i);
            if (ret == null) {
                ret = i;
            }
            return ret;
        } else {
            return i;
        }

    }
*/

    private int findGlyphIndex(char c) {
        int idx = (int)c;
        int retIdx = 0;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {
            if (bfentries[i].getUnicodeStart() <= idx
                    && bfentries[i].getUnicodeEnd() >= idx) {

                retIdx = bfentries[i].getGlyphStartIndex()
                    + idx
                    - bfentries[i].getUnicodeStart();
            }
        }
        return retIdx;
    }

    /**
     * {@inheritDoc}
     */
    public char mapChar(char c) {
        notifyMapOperation();
        int retIdx = findGlyphIndex(c);

        if (isEmbeddable()) {
            // Reencode to a new subset font or get
            // the reencoded value
            Integer newIdx = (Integer)usedGlyphs.get(new Integer(retIdx));
            if (newIdx == null) {
                usedGlyphs.put(new Integer(retIdx),
                               new Integer(usedGlyphsCount));
                usedGlyphsIndex.put(new Integer(usedGlyphsCount),
                                    new Integer(retIdx));
                usedCharsIndex.put(new Integer(usedGlyphsCount),
                                    new Integer((int) c));
                retIdx = usedGlyphsCount;
                usedGlyphsCount++;
            } else {
                retIdx = newIdx.intValue();
            }
        }

        return (char)retIdx;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChar(char c) {
        return (findGlyphIndex(c) > 0);
    }


    /**
     * Sets the bfentries.
     * @param bfentries The bfentries to set
     */
    public void setBFEntries(BFEntry[] bfentries) {
        this.bfentries = bfentries;
    }

    /**
     * Sets the defaultWidth.
     * @param defaultWidth The defaultWidth to set
     */
    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    /**
     * Returns the TrueType Collection Name.
     * @return the TrueType Collection Name
     */
    public String getTTCName() {
        return ttcName;
    }

    /**
     * Sets the the TrueType Collection Name.
     * @param ttcName the TrueType Collection Name
     */
    public void setTTCName(String ttcName) {
        this.ttcName = ttcName;
    }

    /**
     * Adds a new CID width entry to the font.
     * @param cidWidthIndex index
     * @param wds array of widths
     */
    /*
    public void addCIDWidthEntry(int cidWidthIndex, int[] wds) {
        this.warray.addEntry(cidWidthIndex, wds);
    }*/


    /**
     * Sets the width array.
     * @param wds array of widths.
     */
    public void setWidthArray(int[] wds) {
        this.width = wds;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map getUsedGlyphs() {
        return usedGlyphs;
    }

    /** The invalid Unicode character, suitable as a return value in methods
     * that need to return an invalid character. */
    public static final char INVALID_UNICODE_CHAR = 0xFFFF;

    /** {@inheritDoc} */
    public char[] getCharsUsed() {
        if (!isEmbeddable()) {
            return null;
        }
        char[] charArray = new char[usedGlyphsCount];
        for (int i = 0; i < usedGlyphsCount; i++) {
            Integer mapValue = (Integer)usedCharsIndex.get(new Integer(i));
            if (mapValue != null) {
                char arrayItem = (char) mapValue.intValue();
                charArray[i] = arrayItem;
            }
            else {
                charArray[i] = INVALID_UNICODE_CHAR;
            }
        }
        return charArray;
    }
}

