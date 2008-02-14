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

package org.apache.fop.fonts.type1;

import java.awt.geom.RectangularShape;


/**
 * Holds the metrics of a single character from an AFM file.
 */
public class AFMCharMetrics {

    private int charCode = -1;
    private String unicodeChars;
    private String charName;
    private double widthX;
    private double widthY;
    private RectangularShape bBox;
    
    /**
     * Returns the character code.
     * @return the charCode (-1 if not part of the encoding)
     */
    public int getCharCode() {
        return charCode;
    }
    
    /**
     * Indicates whether the character has a character code, i.e. is part of the default encoding.
     * @return true if there is a character code.
     */
    public boolean hasCharCode() {
        return charCode >= 0;
    }
    
    /**
     * Sets the character code.
     * @param charCode the charCode to set
     */
    public void setCharCode(int charCode) {
        this.charCode = charCode;
    }
    
    /**
     * Returns the Unicode characters represented by this object. Some character names can be
     * mapped to multiple Unicode code points, so expect to find more than one character in the
     * String.
     * @return the Unicode characters
     */
    public String getUnicodeChars() {
        return this.unicodeChars;
    }
    
    /**
     * Sets the Unicode characters represented by this object.
     * @param unicodeChars the Unicode characters
     */
    public void setUnicodeChars(String unicodeChars) {
        this.unicodeChars = unicodeChars;
    }
    
    /**
     * Returns the PostScript character name.
     * @return the charName
     */
    public String getCharName() {
        return charName;
    }
    
    /**
     * Sets the PostScript character name.
     * @param charName the charName to set
     */
    public void setCharName(String charName) {
        this.charName = charName;
    }
    
    /**
     * Returns the progression dimension in x-direction.
     * @return the widthX
     */
    public double getWidthX() {
        return widthX;
    }
    
    /**
     * Sets the progression dimension in x-direction
     * @param widthX the widthX to set
     */
    public void setWidthX(double widthX) {
        this.widthX = widthX;
    }
    
    /**
     * Returns the progression dimension in y-direction.
     * @return the widthY
     */
    public double getWidthY() {
        return widthY;
    }
    
    /**
     * Sets the progression dimension in y-direction
     * @param widthY the widthY to set
     */
    public void setWidthY(double widthY) {
        this.widthY = widthY;
    }
    
    /**
     * Returns the character's bounding box.
     * @return the bounding box (or null if it isn't available)
     */
    public RectangularShape getBBox() {
        return bBox;
    }

    /**
     * Sets the character's bounding box.
     * @param box the bounding box
     */
    public void setBBox(RectangularShape box) {
        bBox = box;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("AFM Char: ");
        sb.append(getCharCode());
        sb.append(" (");
        if (getUnicodeChars() != null) {
            for (int i = 0, c = getUnicodeChars().length(); i < c; i++) {
                sb.append("0x").append(Integer.toHexString(getUnicodeChars().charAt(i)));
                sb.append(", ");
            }
        }
        sb.append(getCharName()).append(')');
        return sb.toString();
    }
    
}
