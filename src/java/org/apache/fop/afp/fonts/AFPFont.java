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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Typeface;


/**
 * All implementations of AFP fonts should extend this base class,
 * the object implements the FontMetrics information.
 * <p/>
 */
public abstract class AFPFont extends Typeface {

    /** The font name */
    protected final String name;

    private final boolean embeddable;

    /**
     * Constructor for the base font requires the name.
     * @param name the name of the font
     * @param embeddable whether this font is to be embedded
     */
    public AFPFont(String name, boolean embeddable) {
        this.name = name;
        this.embeddable = embeddable;
    }

    /** {@inheritDoc} */
    public String getFontName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        return this.name;
    }

    /** {@inheritDoc} */
    public String getFullName() {
        return getFontName();
    }

    /** {@inheritDoc} */
    public Set<String> getFamilyNames() {
        Set<String> s = new HashSet<String>();
        s.add(this.name);
        return s;
    }

    /**
     * Returns the type of the font.
     * @return the font type
     */
    public FontType getFontType() {
        return FontType.OTHER;
    }

    /**
     * Indicates if the font has kerning information.
     * @return True, if kerning is available.
     */
    public boolean hasKerningInfo() {
        return false;
    }

    /**
     * Returns the kerning map for the font.
     * @return the kerning map
     */
    public Map<Integer, Map<Integer, Integer>> getKerningInfo() {
        return null;
    }

    /**
     * Returns the character set for a given size
     * @param size the font size
     * @return the character set object
     */
    public abstract CharacterSet getCharacterSet(int size);

    /**
     * Indicates if this font may be embedded.
     * @return True, if embedding is possible/permitted
     */
    public boolean isEmbeddable() {
        return this.embeddable;
    }

    /**
     * Maps mapped code points to Unicode code points.
     * @param character the mapped code point
     * @return the corresponding Unicode code point
     */
    protected static final char toUnicodeCodepoint(int character) {
        //AFP fonts use Unicode directly as their mapped code points, so we can simply cast to char
        return (char)character;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "name=" + name;
    }
}
