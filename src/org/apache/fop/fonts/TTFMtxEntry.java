/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.util.List;

class TTFMtxEntry {

    private int wx;
    private int lsb;
    private String name = "";
    private int index;
    private List unicodeIndex = new java.util.ArrayList();
    private int[] boundingBox = new int[4];
    private long offset;
    private byte found = 0;

    public String toString(TTFFile t) {
        return "Glyph " + name + " index: " + index + " bbox [ "
             + t.convertTTFUnit2PDFUnit(boundingBox[0]) + " "
             + t.convertTTFUnit2PDFUnit(boundingBox[1]) + " "
             + t.convertTTFUnit2PDFUnit(boundingBox[2]) + " "
             + t.convertTTFUnit2PDFUnit(boundingBox[3]) + "] wx: "
             + t.convertTTFUnit2PDFUnit(wx);
    }

    /**
     * Returns the boundingBox.
     * @return int[]
     */
    public int[] getBoundingBox() {
        return boundingBox;
    }

    /**
     * Sets the boundingBox.
     * @param boundingBox The boundingBox to set
     */
    public void setBoundingBox(int[] boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Returns the found.
     * @return byte
     */
    public byte getFound() {
        return found;
    }

    /**
     * Returns the index.
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the lsb.
     * @return int
     */
    public int getLsb() {
        return lsb;
    }

    /**
     * Returns the name.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the offset.
     * @return long
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the unicodeIndex.
     * @return List
     */
    public List getUnicodeIndex() {
        return unicodeIndex;
    }

    /**
     * Returns the wx.
     * @return int
     */
    public int getWx() {
        return wx;
    }

    /**
     * Sets the found.
     * @param found The found to set
     */
    public void setFound(byte found) {
        this.found = found;
    }

    /**
     * Sets the index.
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the lsb.
     * @param lsb The lsb to set
     */
    public void setLsb(int lsb) {
        this.lsb = lsb;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the offset.
     * @param offset The offset to set
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * Sets the wx.
     * @param wx The wx to set
     */
    public void setWx(int wx) {
        this.wx = wx;
    }


}
