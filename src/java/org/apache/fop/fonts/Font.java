/*
 * $Id: FontState.java,v 1.23 2003/03/06 22:19:16 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fonts;

import java.util.Map;




/**
 * This class holds font state information and provides access to the font 
 * metrics.
 */
public class Font {

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
}



