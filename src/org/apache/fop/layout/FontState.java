/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.util.Map;

import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.FontMetrics;

/**
 * This class holds font state information and provides access to the font 
 * metrics.
 */
public class FontState {

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
    public FontState(String key, FontMetrics met, int fontSize) {
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

        if (metric instanceof org.apache.fop.fonts.Font) {
            return ((org.apache.fop.fonts.Font)metric).mapChar(c);
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
}



