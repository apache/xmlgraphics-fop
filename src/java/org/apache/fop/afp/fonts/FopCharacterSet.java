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

import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.fonts.Typeface;

/**
 * A Character set for a normal FOP font
 */
public class FopCharacterSet extends CharacterSet {

    /** The character set for this font */
    private Typeface charSet;

    /**
     * Constructor for the CharacterSetMetric object, the character set is used
     * to load the font information from the actual AFP font.
     * @param codePage the code page identifier
     * @param encoding the encoding of the font
     * @param name the character set name
     * @param charSet the fop character set
     * @param eventProducer for handling AFP related events
     */
    public FopCharacterSet(String codePage, String encoding, String name, Typeface charSet,
            AFPEventProducer eventProducer) {
        super(codePage, encoding, CharacterSetType.SINGLE_BYTE, name, (AFPResourceAccessor) null,
                eventProducer);
        this.charSet = charSet;
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
        return charSet.getAscender(1);
    }

    /**
     * Cap height is the average height of the uppercase characters in
     * a font. This value is specified by the designer of a font and is
     * usually the height of the uppercase M.
     * @return the cap height value in millipoints
     */
    public int getCapHeight() {
        return charSet.getCapHeight(1);
    }

    /**
     * Descender depth is the distance from the character baseline to
     * the bottom of a character box. A negative descender depth signifies
     * that all of the graphic character is above the character baseline.
     * @return the descender value in millipoints
     */
    public int getDescender() {
        return charSet.getDescender(1);
    }

    /**
     * XHeight refers to the height of the lower case letters above the baseline.
     * @return the typical height of characters
     */
    public int getXHeight() {
        return charSet.getXHeight(1);
    }

    @Override
    public int getWidth(char character, int size) {
        return charSet.getWidth(character, size);
    }

    @Override
    public Rectangle getCharacterBox(char character, int size) {
        return charSet.getBoundingBox(character, size);
    };

    @Override
    public int getUnderscoreWidth() {
        return charSet.getUnderlineThickness(1);
    }

    @Override
    public int getUnderscorePosition() {
        return charSet.getUnderlinePosition(1);
    }

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public char mapChar(char c) {
        return charSet.mapChar(c);
    }
}
