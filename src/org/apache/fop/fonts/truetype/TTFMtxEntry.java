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
package org.apache.fop.fonts.truetype;

import java.util.List;

/**
 * This class represents a TrueType Mtx Entry.
 */
class TTFMtxEntry {

    private int wx;
    private int lsb;
    private String name = "";
    private int index;
    private List unicodeIndex = new java.util.ArrayList();
    private int[] boundingBox = new int[4];
    private long offset;
    private byte found = 0;

    /**
     * Returns a String representation of this object.
     * 
     * @param t TTFFile to use for unit conversion
     * @return String String representation
     */
    public String toString(TTFFile t) {
        return "Glyph " + name + " index: " + getIndexAsString() + " bbox ["
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
     * Determines whether this index represents a reserved character.
     * @return True if it is reserved
     */
    public boolean isIndexReserved() {
        return (getIndex() >= 32768) && (getIndex() <= 65535);
    }
    
    /**
     * Returns a String representation of the index taking into account if
     * the index is in the reserved range.
     * @return index as String
     */
    public String getIndexAsString() {
        if (isIndexReserved()) {
            return Integer.toString(getIndex()) + " (reserved)";
        } else {
            return Integer.toString(getIndex());
        }
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
