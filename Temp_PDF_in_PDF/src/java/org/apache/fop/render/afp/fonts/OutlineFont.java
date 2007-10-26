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

package org.apache.fop.render.afp.fonts;

/**
 * A font defined as a set of lines and curves as opposed to a bitmap font. An
 * outline font can be scaled to any size and otherwise transformed more easily
 * than a bitmap font, and with more attractive results. <p/>
 *
 */
public class OutlineFont extends AFPFont {

    /** The character set for this font */
    private CharacterSet _characterSet = null;

    /**
     * Constructor for an outline font.
     *
     * @param name
     *            the name of the font
     * @param characterSet
     *            the chracter set
     */
    public OutlineFont(String name, CharacterSet characterSet) {
        super(name);
        _characterSet = characterSet;
    }

    /**
     * Get the character set metrics.
     *
     * @return the character set
     */
    public CharacterSet getCharacterSet() {

        return _characterSet;

    }

    /**
     * Get the character set metrics.
     * @param size ignored
     * @return the character set
     */
    public CharacterSet getCharacterSet(int size) {

        return _characterSet;

    }

    /**
     * Get the first character in this font.
     */
    public int getFirstChar() {

        return _characterSet.getFirstChar();

    }

    /**
     * Get the last character in this font.
     */
    public int getLastChar() {

        return _characterSet.getLastChar();

    }

    /**
     * The ascender is the part of a lowercase letter that extends above the
     * "x-height" (the height of the letter "x"), such as "d", "t", or "h". Also
     * used to denote the part of the letter extending above the x-height.
     *
     * @param size
     *            the point size
     */
    public int getAscender(int size) {

        return _characterSet.getAscender() / 1000 * size;

    }

    /**
     * Obtains the height of capital letters for the specified point size.
     *
     * @param size
     *            the point size
     */
    public int getCapHeight(int size) {

        return _characterSet.getCapHeight() / 1000 * size;

    }

    /**
     * The descender is the part of a lowercase letter that extends below the
     * base line, such as "g", "j", or "p". Also used to denote the part of the
     * letter extending below the base line.
     *
     * @param size
     *            the point size
     */
    public int getDescender(int size) {

        return _characterSet.getDescender() / 1000 * size;

    }

    /**
     * The "x-height" (the height of the letter "x").
     *
     * @param size
     *            the point size
     */
    public int getXHeight(int size) {

        return _characterSet.getXHeight() / 1000 * size;

    }

    /**
     * Obtain the width of the character for the specified point size.
     */
    public int getWidth(int character, int size) {

        return _characterSet.width(character) / 1000 * size;

    }

    /**
     * Get the getWidth (in 1/1000ths of a point size) of all characters in this
     * character set.
     *
     * @param size
     *            the point size
     * @return the widths of all characters
     */
    public int[] getWidths(int size) {

        int[] widths =  _characterSet.getWidths();
        for (int i = 0; i < widths.length; i++) {
            widths[i] = widths[i] / 1000 * size;
        }
        return widths;

    }

    /**
     * Get the getWidth (in 1/1000ths of a point size) of all characters in this
     * character set.
     *
     * @return the widths of all characters
     */
    public int[] getWidths() {

        return getWidths(1000);

    }

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public char mapChar(char c) {
        return _characterSet.mapChar(c);
    }

    /**
     * Get the encoding of the font.
     * @return the encoding
     */
    public String getEncoding() {
        return _characterSet.getEncoding();
    }

}