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

import org.apache.fop.afp.AFPEventProducer;

/**
 * A font defined as a set of lines and curves as opposed to a bitmap font. An
 * outline font can be scaled to any size and otherwise transformed more easily
 * than a bitmap font, and with more attractive results.
 */
public abstract class AbstractOutlineFont extends AFPFont {

    /** The character set for this font */
    protected CharacterSet charSet = null;

    private final AFPEventProducer eventProducer;

    /**
     * Constructor for an outline font.
     *
     * @param name the name of the font
     * @param embeddable sets whether or not this font is to be embedded
     * @param charSet the chracter set
     * @param eventProducer The object to handle any events which occur from the object.
     */
    public AbstractOutlineFont(String name, boolean embeddable, CharacterSet charSet,
            AFPEventProducer eventProducer) {
        super(name, embeddable);
        this.charSet = charSet;
        this.eventProducer = eventProducer;
    }

    AFPEventProducer getAFPEventProducer() {
        return eventProducer;
    }

    /**
     * Get the character set metrics.
     *
     * @return the character set
     */
    public CharacterSet getCharacterSet() {
        return charSet;
    }

    /**
     * Get the character set metrics.
     * @param size ignored
     * @return the character set
     */
    public CharacterSet getCharacterSet(int size) {
        return charSet;
    }

    /**
     * The ascender is the part of a lowercase letter that extends above the
     * "x-height" (the height of the letter "x"), such as "d", "t", or "h". Also
     * used to denote the part of the letter extending above the x-height.
     *
     * @param size the font size (in mpt)
     * @return the ascender for the given size
     */
    public int getAscender(int size) {
        return charSet.getAscender() * size;
    }

    /** {@inheritDoc} */
    public int getUnderlinePosition(int size) {
        return charSet.getUnderscorePosition() * size;
    }

    @Override
    public int getUnderlineThickness(int size) {
        int underscoreWidth = charSet.getUnderscoreWidth();
        return underscoreWidth == 0 ? super.getUnderlineThickness(size) : underscoreWidth * size;
    }

    /**
     * Obtains the height of capital letters for the specified point size.
     *
     * @param size the font size (in mpt)
     * @return the cap height for the given size
     */
    public int getCapHeight(int size) {
        return charSet.getCapHeight() * size;
    }

    /**
     * The descender is the part of a lowercase letter that extends below the
     * base line, such as "g", "j", or "p". Also used to denote the part of the
     * letter extending below the base line.
     *
     * @param size the font size (in mpt)
     * @return the descender for the given size
     */
    public int getDescender(int size) {
        return charSet.getDescender() * size;
    }

    /**
     * The "x-height" (the height of the letter "x").
     *
     * @param size the font size (in mpt)
     * @return the x height for the given size
     */
    public int getXHeight(int size) {
        return charSet.getXHeight() * size;
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
