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

import java.awt.Rectangle;

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
     * The character widths in the character set (indexed using Unicode codepoints)
     */
    private IntegerKeyStore<CharacterMetrics> characterMetrics;

    /**
     * The height of lowercase letters
     */
    private int xHeight;

    /** The character set orientation */
    private final int orientation;
    /** space increment */
    private final int spaceIncrement;
    /** em space increment */
    private final int emSpaceIncrement;
    /** Nominal Character Increment */
    private final int nomCharIncrement;

    private int underscoreWidth;

    private int underscorePosition;

    /**
     * Constructor for the CharacterSetOrientation, the orientation is
     * expressed as the degrees rotation (i.e 0, 90, 180, 270)
     * @param orientation the character set orientation
     */
    public CharacterSetOrientation(int orientation, int spaceIncrement, int emSpaceIncrement,
            int nomCharIncrement) {
        this.orientation = orientation;
        this.spaceIncrement = spaceIncrement;
        this.emSpaceIncrement = emSpaceIncrement;
        this.nomCharIncrement = nomCharIncrement;
        this.characterMetrics = new IntegerKeyStore<CharacterMetrics>();
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
     * TODO
     */
    public int getUnderscoreWidth() {
        return underscoreWidth;
    }

    /**
     * TODO
     */
    public int getUnderscorePosition() {
        return underscorePosition;
    }

    /**
     * The orientation for these metrics in the character set
     * @return the orientation
     */
    public int getOrientation() {
        return orientation;
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
     * @param character the Unicode character to evaluate
     * @return the widths of the character
     */
    public int getWidth(char character, int size) {
        CharacterMetrics cm = getCharacterMetrics(character);
        return cm == null ? -1 : size * cm.width;
    }

    private CharacterMetrics getCharacterMetrics(char character) {
        return characterMetrics.get((int) character);
    }

    /**
     * Get the character box (rectangle with dimensions in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     * @param character the Unicode character to evaluate
     * @return the character box
     */
    public Rectangle getCharacterBox(char character, int size) {
        CharacterMetrics cm = getCharacterMetrics(character);
        return scale(cm == null ? getFallbackCharacterBox() : cm.characterBox, size);
    }

    private static Rectangle scale(Rectangle rectangle, int size) {
        if (rectangle == null) {
            return null;
        } else {
        return new Rectangle((int) (size * rectangle.getX()), (int) (size * rectangle.getY()),
                (int) (size * rectangle.getWidth()), (int) (size * rectangle.getHeight()));
        }
    }

    private Rectangle getFallbackCharacterBox() {
        // TODO replace with something sensible
        return new Rectangle(0, 0, 0, 0);
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
     * TODO
     * @param underscoreWidth the underscore width value in millipoints
     */
    public void setUnderscoreWidth(int underscoreWidth) {
        this.underscoreWidth = underscoreWidth;
    }

    /**
     * TODO
     * @param underscorePosition the underscore position value in millipoints
     */
    public void setUnderscorePosition(int underscorePosition) {
        this.underscorePosition = underscorePosition;
    }

    /**
     * Set the width (in 1/1000ths of a point size) of the character
     * identified by the parameter passed.
     * @param character the Unicode character for which the width is being set
     * @param width the widths of the character
     */
    public void setCharacterMetrics(char character, int width, Rectangle characterBox) {
        characterMetrics.put((int) character, new CharacterMetrics(width, characterBox));
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
    public int getSpaceIncrement() {
        return this.spaceIncrement;
    }

    /**
     * Returns the em space increment.
     * @return the em space increment
     */
    public int getEmSpaceIncrement() {
        return this.emSpaceIncrement;
    }

    /**
     * Returns the nominal character increment.
     * @return the nominal character increment
     */
    public int getNominalCharIncrement() {
        return this.nomCharIncrement;
    }

    private static class CharacterMetrics {

        public final int width;

        public final Rectangle characterBox;

        public CharacterMetrics(int width, Rectangle characterBox) {
            this.width = width;
            this.characterBox = characterBox;
        }
    }
}
