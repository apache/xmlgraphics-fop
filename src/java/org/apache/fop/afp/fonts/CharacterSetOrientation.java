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

package org.apache.fop.afp.fonts;

import java.util.Arrays;

/**
 * The IBM Font Object Content Architecture (FOCA) supports presentation
 * of character shapes by defining their characteristics, which include
 * Font-Description information for identifying the characters, Font-Metric
 * information for positioning the characters, and Character-Shape
 * information for presenting the character images.
 *
 * Presenting a graphic character on a presentation surface requires
 * that you communicate this information clearly to rotate and position
 * characters correctly on the physical or logical page.
 *
 * This class provides font metric information for a particular font
 * as by the orientation.
 *
 * This information is obtained directly from the AFP font files which must
 * be installed in the classpath under in the location specified by the path
 * attribute in the afp-font.xml file.
 * <p/>
 */
public class CharacterSetOrientation {

    /**
     * The ascender height for the character set
     */
    private int ascender;

    /**
     * The descender depth for the character set
     */
    private int descender;

    /**
     * The height of capital letters
     */
    private int capHeight;

    /**
     * The character widths in the character set
     */
    private int[] charsWidths = null;

    /**
     * The height of lowercase letters
     */
    private int xHeight;

    /**
     * The first character
     */
    private int firstChar;

    /**
     * The last character
     */
    private int lastChar;


    /**
     * The character set orientation
     */
    private int orientation = 0;

    /** space increment */
    private int spaceIncrement;
    /** em space increment */
    private int emSpaceIncrement = -1;


    /**
     * Constructor for the CharacterSetOrientation, the orientation is
     * expressed as the degrees rotation (i.e 0, 90, 180, 270)
     * @param orientation the character set orientation
     */
    public CharacterSetOrientation(int orientation) {
        this.orientation = orientation;
        charsWidths = new int[256];
        Arrays.fill(charsWidths, -1);
    }

    /**
     * Ascender height is the distance from the character baseline to the
     * top of the character box. A negative ascender height signifies that
     * all of the graphic character is below the character baseline. For
     * a character rotation other than 0, ascender height loses its
     * meaning when the character is lying on its side or is upside down
     * with respect to normal viewing orientation. For the general case,
     * Ascender Height is the character�s most positive y-axis value.
     * For bounded character boxes, for a given character having an
     * ascender, ascender height and baseline offset are equal.
     * @return the ascender value in millipoints
     */
    public int getAscender() {
        return ascender;
    }

    /**
     * Cap height is the average height of the uppercase characters in
     * a font. This value is specified by the designer of a font and is
     * usually the height of the uppercase M.
     * @return the cap height value in millipoints
     */
    public int getCapHeight() {
        return capHeight;
    }

    /**
     * Descender depth is the distance from the character baseline to
     * the bottom of a character box. A negative descender depth signifies
     * that all of the graphic character is above the character baseline.
     * @return the descender value in millipoints
     */
    public int getDescender() {
        return descender;
    }

    /**
     * The first character in the character set
     * @return the first character
     */
    public int getFirstChar() {
        return firstChar;
    }

    /**
     * The last character in the character set
     * @return the last character
     */
    public int getLastChar() {
        return lastChar;
    }

    /**
     * The orientation for these metrics in the character set
     * @return the orientation
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Get the width (in 1/1000ths of a point size) of all characters
     * in this character set.
     * @return the widths of all characters
     */
    public int[] getWidths() {
        int[] arr = new int[(getLastChar() - getFirstChar()) + 1];
        System.arraycopy(charsWidths, getFirstChar(), arr, 0, (getLastChar() - getFirstChar()) + 1);
        return arr;
    }

    /**
     * XHeight refers to the height of the lower case letters above
     * the baseline.
     * @return heightX the typical height of characters
     */
    public int getXHeight() {
        return xHeight;
    }

    /**
     * Get the width (in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     * @param characterIndex the character to evaluate
     * @return the widths of the character
     */
    public int getWidth(int characterIndex) {
        if (characterIndex >= charsWidths.length) {
            throw new IllegalArgumentException("Invalid character index: "
                    + characterIndex + ", maximum is " + (charsWidths.length - 1));
        }
        return charsWidths[characterIndex];
    }

    /**
     * Ascender height is the distance from the character baseline to the
     * top of the character box. A negative ascender height signifies that
     * all of the graphic character is below the character baseline. For
     * a character rotation other than 0, ascender height loses its
     * meaning when the character is lying on its side or is upside down
     * with respect to normal viewing orientation. For the general case,
     * Ascender Height is the character's most positive y-axis value.
     * For bounded character boxes, for a given character having an
     * ascender, ascender height and baseline offset are equal.
     * @param ascender the ascender to set
     */
    public void setAscender(int ascender) {
        this.ascender = ascender;
    }

    /**
     * Cap height is the average height of the uppercase characters in
     * a font. This value is specified by the designer of a font and is
     * usually the height of the uppercase M.
     * @param capHeight the cap height to set
     */
    public void setCapHeight(int capHeight) {
        this.capHeight = capHeight;
    }

    /**
     * Descender depth is the distance from the character baseline to
     * the bottom of a character box. A negative descender depth signifies
     * that all of the graphic character is above the character baseline.
     * @param descender the descender value in millipoints
     */
    public void setDescender(int descender) {
        this.descender = descender;
    }

    /**
     * The first character in the character set
     * @param firstChar the first character
     */
    public void setFirstChar(int firstChar) {
        this.firstChar = firstChar;
    }

    /**
     * The last character in the character set
     * @param lastChar the last character
     */
    public void setLastChar(int lastChar) {
        this.lastChar = lastChar;
    }

    /**
     * Set the width (in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     * @param character the character for which the width is being set
     * @param width the widths of the character
     */
    public void setWidth(int character, int width) {
        if (character >= charsWidths.length) {
            // Increase the size of the array if necessary
            //  TODO Can we remove firstChar? surely firstChar==0 at this stage?
            int[] arr = new int[(character - firstChar) + 1];
            System.arraycopy(charsWidths, 0, arr, 0, charsWidths.length);
            Arrays.fill(arr, charsWidths.length, character - firstChar, -1);
            charsWidths = arr;
        }
        charsWidths[character] = width;

    }

    /**
     * XHeight refers to the height of the lower case letters above
     * the baseline.
     * @param xHeight the typical height of characters
     */
    public void setXHeight(int xHeight) {
        this.xHeight = xHeight;
    }

    /**
     * Returns the space increment.
     * @return the space increment
     */
    public int getSpaceIncrement(){
        return this.spaceIncrement;
    }

    /**
     * Sets the space increment.
     * @param value the space increment
     */
    public void setSpaceIncrement(int value) {
        this.spaceIncrement = value;
    }

    /**
     * Returns the em space increment.
     * @return the em space increment
     */
    public int getEmSpaceIncrement(){
        return this.emSpaceIncrement;
    }

    /**
     * Sets the em space increment.
     * @param value the em space increment
     */
    public void setEmSpaceIncrement(int value) {
        this.emSpaceIncrement = value;
    }

}
