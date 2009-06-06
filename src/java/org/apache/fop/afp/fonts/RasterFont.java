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

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A font where each character is stored as an array of pixels (a bitmap). Such
 * fonts are not easily scalable, in contrast to vectored fonts. With this type
 * of font, the font metrics information is held in character set files (one for
 * each size and style). <p/>
 *
 */
public class RasterFont extends AFPFont {

    /** Static logging instance */
    protected static final Log log = LogFactory.getLog("org.apache.fop.afp.fonts");

    private final SortedMap/*<Integer,CharacterSet>*/ charSets
            = new java.util.TreeMap/*<Integer,CharacterSet>*/();
    private Map/*<Integer,CharacterSet>*/ substitutionCharSets;

    private CharacterSet charSet = null;

    /**
     * Constructor for the raster font requires the name, weight and style
     * attribute to be available as this forms the key to the font.
     *
     * @param name
     *            the name of the font
     */
    public RasterFont(String name) {
        super(name);
    }

    /**
     * Adds the character set for the given point size
     * @param size point size (in mpt)
     * @param characterSet character set
     */
    public void addCharacterSet(int size, CharacterSet characterSet) {
        //TODO: replace with Integer.valueOf() once we switch to Java 5
        this.charSets.put(new Integer(size), characterSet);
        this.charSet = characterSet;
    }

    /**
     * Get the character set metrics for the specified point size.
     *
     * @param size the point size (in mpt)
     * @return the character set metrics
     */
    public CharacterSet getCharacterSet(int size) {

        //TODO: replace with Integer.valueOf() once we switch to Java 5
        Integer requestedSize = new Integer(size);
        CharacterSet csm = (CharacterSet) charSets.get(requestedSize);

        if (csm != null) {
            return csm;
        }

        if (substitutionCharSets != null) {
            //Check first if a substitution has already been added
            csm = (CharacterSet) substitutionCharSets.get(requestedSize);
        }

        if (csm == null && !charSets.isEmpty()) {
            // No match or substitution found, but there exist entries
            // for other sizes
            // Get char set with nearest, smallest font size
            SortedMap smallerSizes = charSets.headMap(requestedSize);
            SortedMap largerSizes = charSets.tailMap(requestedSize);
            int smallerSize = smallerSizes.isEmpty() ? 0
                    : ((Integer)smallerSizes.lastKey()).intValue();
            int largerSize = largerSizes.isEmpty() ? Integer.MAX_VALUE
                    : ((Integer)largerSizes.firstKey()).intValue();

            Integer fontSize
                    = (size - smallerSize) <= (largerSize - size)
                        ? new Integer(smallerSize) : new Integer(largerSize);
            csm = (CharacterSet) charSets.get(fontSize);

            if (csm != null) {
                // Add the substitute mapping, so subsequent calls will
                // find it immediately
                if (substitutionCharSets == null) {
                    substitutionCharSets = new HashMap();
                }
                substitutionCharSets.put(requestedSize, csm);
                String msg = "No " + (size / 1000f) + "pt font " + getFontName()
                    + " found, substituted with " + fontSize.intValue() / 1000f + "pt font";
                log.warn(msg);
            }
        }

        if (csm == null) {
            // Still no match -> error
            String msg = "No font found for font " + getFontName()
                + " with point size " + size / 1000f;
            log.error(msg);
            throw new FontRuntimeException(msg);
        }

        return csm;

    }

    /**
     * Get the first character in this font.
     * @return the first character in this font.
     */
    public int getFirstChar() {
        Iterator it = charSets.values().iterator();
        if (it.hasNext()) {
            CharacterSet csm = (CharacterSet) it.next();
            return csm.getFirstChar();
        } else {
            String msg = "getFirstChar() - No character set found for font:" + getFontName();
            log.error(msg);
            throw new FontRuntimeException(msg);
        }
    }

    /**
     * Get the last character in this font.
     * @return the last character in this font.
     */
    public int getLastChar() {

        Iterator it = charSets.values().iterator();
        if (it.hasNext()) {
            CharacterSet csm = (CharacterSet) it.next();
            return csm.getLastChar();
        } else {
            String msg = "getLastChar() - No character set found for font:" + getFontName();
            log.error(msg);
            throw new FontRuntimeException(msg);
        }

    }

    /**
     * The ascender is the part of a lowercase letter that extends above the
     * "x-height" (the height of the letter "x"), such as "d", "t", or "h". Also
     * used to denote the part of the letter extending above the x-height.
     *
     * @param size the point size (in mpt)
     * @return the ascender for the given point size
     */
    public int getAscender(int size) {
        return getCharacterSet(size).getAscender() * size;
    }

    /**
     * Obtains the height of capital letters for the specified point size.
     *
     * @param size the point size (in mpt)
     * @return the cap height for the specified point size
     */
    public int getCapHeight(int size) {
        return getCharacterSet(size).getCapHeight() * size;
    }

    /**
     * The descender is the part of a lowercase letter that extends below the
     * base line, such as "g", "j", or "p". Also used to denote the part of the
     * letter extending below the base line.
     *
     * @param size the point size (in mpt)
     * @return the descender for the specified point size
     */
    public int getDescender(int size) {
        return getCharacterSet(size).getDescender() * size;
    }

    /**
     * The "x-height" (the height of the letter "x").
     *
     * @param size the point size (in mpt)
     * @return the x height for the given point size
     */
    public int getXHeight(int size) {
        return getCharacterSet(size).getXHeight() * size;
    }

    /**
     * Obtain the width of the character for the specified point size.
     * @param character the character
     * @param size the point size (in mpt)
     * @return the width for the given point size
     */
    public int getWidth(int character, int size) {
        return getCharacterSet(size).getWidth(character) * size;
    }

    /**
     * Get the getWidth (in 1/1000ths of a point size) of all characters in this
     * character set.
     *
     * @param size  the point size (in mpt)
     * @return the widths of all characters
     */
    public int[] getWidths(int size) {
        return getCharacterSet(size).getWidths();
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

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return charSet.hasChar(c);
    }

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public char mapChar(char c) {
        return charSet.mapChar(c);
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return charSet.getEncoding();
    }
}
