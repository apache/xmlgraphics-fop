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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
    protected static final Log LOG = LogFactory.getLog("org.apache.fop.afp.fonts");

    private final SortedMap<Integer, CharacterSet> charSets = new TreeMap<Integer, CharacterSet>();
    private Map<Integer, CharacterSet> substitutionCharSets;

    private CharacterSet charSet = null;

    /**
     * Constructor for the raster font requires the name, weight and style
     * attribute to be available as this forms the key to the font.
     *
     * @param name
     *            the name of the font
     */
    public RasterFont(String name, boolean embeddable) {
        super(name, embeddable);
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
     * @param sizeInMpt the point size (in mpt)
     * @return the character set metrics
     */
    public CharacterSet getCharacterSet(int sizeInMpt) {

        Integer requestedSize = Integer.valueOf(sizeInMpt);
        CharacterSet csm = charSets.get(requestedSize);
        double sizeInPt = sizeInMpt / 1000.0;

        if (csm != null) {
            return csm;
        }

        if (substitutionCharSets != null) {
            //Check first if a substitution has already been added
            csm = substitutionCharSets.get(requestedSize);
        }

        if (csm == null && !charSets.isEmpty()) {
            // No match or substitution found, but there exist entries
            // for other sizes
            // Get char set with nearest, smallest font size
            SortedMap<Integer, CharacterSet> smallerSizes = charSets.headMap(requestedSize);
            SortedMap<Integer, CharacterSet> largerSizes = charSets.tailMap(requestedSize);
            int smallerSize = smallerSizes.isEmpty() ? 0
                    : smallerSizes.lastKey().intValue();
            int largerSize = largerSizes.isEmpty() ? Integer.MAX_VALUE
                    : largerSizes.firstKey().intValue();

            Integer fontSize;
            if (!smallerSizes.isEmpty()
                            && (sizeInMpt - smallerSize) <= (largerSize - sizeInMpt)) {
                fontSize = Integer.valueOf(smallerSize);
            } else {
                fontSize = Integer.valueOf(largerSize);
            }
            csm = charSets.get(fontSize);

            if (csm != null) {
                // Add the substitute mapping, so subsequent calls will
                // find it immediately
                if (substitutionCharSets == null) {
                    substitutionCharSets = new HashMap<Integer, CharacterSet>();
                }
                substitutionCharSets.put(requestedSize, csm);
                // do not output the warning if the font size is closer to an integer less than 0.1
                if (!(Math.abs(fontSize.intValue() / 1000.0 - sizeInPt) < 0.1)) {
                    String msg = "No " + sizeInPt + "pt font " + getFontName()
                            + " found, substituted with " + fontSize.intValue() / 1000f + "pt font";
                    LOG.warn(msg);
                }
            }
        }

        if (csm == null) {
            // Still no match -> error
            String msg = "No font found for font " + getFontName() + " with point size " + sizeInPt;
            LOG.error(msg);
            throw new FontRuntimeException(msg);
        }

        return csm;

    }

    private int metricsToAbsoluteSize(CharacterSet cs, int value, int givenSize) {
        int nominalVerticalSize = cs.getNominalVerticalSize();
        if (nominalVerticalSize != 0) {
            return value * nominalVerticalSize;
        } else {
            return value * givenSize;
        }
    }

    private int metricsToAbsoluteSize(CharacterSet cs, double value, int givenSize) {
        int nominalVerticalSize = cs.getNominalVerticalSize();
        if (nominalVerticalSize != 0) {
            return (int) (value * nominalVerticalSize);
        } else {
            return (int) (value * givenSize);
        }
    }

    /**
     * The ascender is the part of a lowercase letter that extends above the
     * "x-height" (the height of the letter "x"), such as "d", "t", or "h". Also
     * used to denote the part of the letter extending above the x-height.
     *
     * @param size the font size (in mpt)
     * @return the ascender for the given point size
     */
    public int getAscender(int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getAscender(), size);
    }

    /** {@inheritDoc} */
    public int getUnderlinePosition(int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getUnderscorePosition(), size);
    }

    @Override
    public int getUnderlineThickness(int size) {
        CharacterSet cs = getCharacterSet(size);
        int underscoreWidth = cs.getUnderscoreWidth();
        return underscoreWidth == 0 ? super.getUnderlineThickness(size)
                : metricsToAbsoluteSize(cs, underscoreWidth, size);
    }

    /**
     * Obtains the height of capital letters for the specified point size.
     *
     * @param size the font size (in mpt)
     * @return the cap height for the specified point size
     */
    public int getCapHeight(int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getCapHeight(), size);
    }

    /**
     * The descender is the part of a lowercase letter that extends below the
     * base line, such as "g", "j", or "p". Also used to denote the part of the
     * letter extending below the base line.
     *
     * @param size the font size (in mpt)
     * @return the descender for the specified point size
     */
    public int getDescender(int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getDescender(), size);
    }

    /**
     * The "x-height" (the height of the letter "x").
     *
     * @param size the font size (in mpt)
     * @return the x height for the given point size
     */
    public int getXHeight(int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getXHeight(), size);
    }

    /**
     * Obtain the width of the character for the specified point size.
     * @param character the character
     * @param size the font size (in mpt)
     * @return the width for the given point size
     */
    public int getWidth(int character, int size) {
        CharacterSet cs = getCharacterSet(size);
        return metricsToAbsoluteSize(cs, cs.getWidth(toUnicodeCodepoint(character), 1), size);
    }

    /**
     * TODO
     */
    public Rectangle getBoundingBox(int character, int size) {
        CharacterSet cs = getCharacterSet(size);
        Rectangle characterBox = cs.getCharacterBox(toUnicodeCodepoint(character), 1);
        int x = metricsToAbsoluteSize(cs, characterBox.getX(), size);
        int y = metricsToAbsoluteSize(cs, characterBox.getY(), size);
        int w = metricsToAbsoluteSize(cs, characterBox.getWidth(), size);
        int h = metricsToAbsoluteSize(cs, characterBox.getHeight(), size);
        return new Rectangle(x, y, w, h);
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
