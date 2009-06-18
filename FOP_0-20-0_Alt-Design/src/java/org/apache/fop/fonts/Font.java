/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.fonts;

import java.util.Map;




/**
 * This class holds font state information and provides access to the font
 * metrics.
 */
public class Font {

    /** Default fallback key */
    public static final String DEFAULT_FONT = "any,normal,400";
    /** Normal font weight */
    public static final int NORMAL = 400;
    /** Bold font weight */
    public static final int BOLD = 700;

    private String fontName;
    private int fontSize;
    //private String fontFamily;
    //private String fontStyle;
    //private int fontWeight;

    /**
     * normal or small-caps font
     */
    //private int fontVariant;

    private FontMetrics metric;

    /**
     * Main constructor
     * @param key key of the font
     * @param met font metrics
     * @param fontSize font size
     */
    public Font(String key, FontMetrics met, int fontSize) {
        this.fontName = key;
        this.metric = met;
        this.fontSize = fontSize;
    }

    /**
     * Returns the font's ascender.
     * @return the ascender
     */
    public int getAscender() {
        return metric.getAscender(fontSize) / 1000;
    }

    /**
     * Returns the font's CapHeight.
     * @return the capital height
     */
    public int getCapHeight() {
        return metric.getCapHeight(fontSize) / 1000;
    }

    /**
     * Returns the font's Descender.
     * @return the descender
     */
    public int getDescender() {
        return metric.getDescender(fontSize) / 1000;
    }

    /**
     * Returns the font's name.
     * @return the font name
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Returns the font size
     * @return the font size
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Returns the XHeight
     * @return the XHeight
     */
    public int getXHeight() {
        return metric.getXHeight(fontSize) / 1000;
    }

    /**
     * Returns the font's kerning table
     * @return the kerning table
     */
    public Map getKerning() {
        Map ret = metric.getKerningInfo();
        if (ret != null) {
            return ret;
        } else {
            return java.util.Collections.EMPTY_MAP;
        }
    }

    /**
     * Returns the width of a character
     * @param charnum character to look up
     * @return width of the character
     */
    public int getWidth(int charnum) {
        // returns width of given character number in millipoints
        return (metric.getWidth(charnum, fontSize) / 1000);
    }

    /**
     * Map a java character (unicode) to a font character.
     * Default uses CodePointMapping.
     * @param c character to map
     * @return the mapped character
     */
    public char mapChar(char c) {

        if (metric instanceof org.apache.fop.fonts.Typeface) {
            return ((org.apache.fop.fonts.Typeface)metric).mapChar(c);
        }

        // Use default CodePointMapping
        char d = CodePointMapping.getMapping("WinAnsiEncoding").mapChar(c);
        if (d != 0) {
            c = d;
        } else {
            c = '#';
        }

        return c;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        /*
        sbuf.append(fontFamily);
        sbuf.append(',');*/
        sbuf.append(fontName);
        sbuf.append(',');
        sbuf.append(fontSize);
        /*
        sbuf.append(',');
        sbuf.append(fontStyle);
        sbuf.append(',');
        sbuf.append(fontWeight);*/
        sbuf.append(')');
        return sbuf.toString();
    }

    /**
     * Helper method for getting the width of a unicode char
     * from the current fontstate.
     * This also performs some guessing on widths on various
     * versions of space that might not exists in the font.
     * @param c character to inspect
     * @return the width of the character
     */
    public int getCharWidth(char c) {
        int width;

        if ((c == '\n') || (c == '\r') || (c == '\t') || (c == '\u00A0')) {
            width = getCharWidth(' ');
        } else {
            width = getWidth(mapChar(c));
            if (width <= 0) {
                // Estimate the width of spaces not represented in
                // the font
                int em = getWidth(mapChar('m'));
                int en = getWidth(mapChar('n'));
                if (em <= 0) {
                    em = 500 * getFontSize();
                }
                if (en <= 0) {
                    en = em - 10;
                }

                if (c == ' ') {
                    width = em;
                }
                if (c == '\u2000') {
                    width = en;
                }
                if (c == '\u2001') {
                    width = em;
                }
                if (c == '\u2002') {
                    width = em / 2;
                }
                if (c == '\u2003') {
                    width = getFontSize();
                }
                if (c == '\u2004') {
                    width = em / 3;
                }
                if (c == '\u2005') {
                    width = em / 4;
                }
                if (c == '\u2006') {
                    width = em / 6;
                }
                if (c == '\u2007') {
                    width = getCharWidth(' ');
                }
                if (c == '\u2008') {
                    width = getCharWidth('.');
                }
                if (c == '\u2009') {
                    width = em / 5;
                }
                if (c == '\u200A') {
                    width = 5;
                }
                if (c == '\u200B') {
                    width = 100;
                }
                if (c == '\u202F') {
                    width = getCharWidth(' ') / 2;
                }
                if (c == '\u3000') {
                    width = getCharWidth(' ') * 2;
                }
            }
        }

        return width;
    }

    /**
     * Calculates the word width.
     */
    public int getWordWidth(String word) {
        if (word == null)
            return 0;
        int wordLength = word.length();
        int width = 0;
        char[] characters = new char[wordLength];
        word.getChars(0, wordLength, characters, 0);
        for (int i = 0; i < wordLength; i++) {
            width += getCharWidth(characters[i]);
        }
        return width;
    }

}


