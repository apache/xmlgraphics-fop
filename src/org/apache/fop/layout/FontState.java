/*
 * $Id$
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
package org.apache.fop.layout;

import org.apache.fop.apps.FOPException;
import org.apache.fop.render.pdf.CodePointMapping;

import java.util.Map;
import java.util.StringTokenizer;

public class FontState {

    private FontInfo _fontInfo;
    private String _fontName;
    private int _fontSize;
    private String _fontFamily;
    private String _fontStyle;
    private String _fontWeight;
    private int _fontVariant;

    private FontMetric _metric;
    private int _letterSpacing;

    private static final Map EMPTY_MAP = new java.util.HashMap();


    public FontState(FontInfo fontInfo, String fontFamily, String fontStyle,
                     String fontWeight, int fontSize,
                     int fontVariant) throws FOPException {
        _fontInfo = fontInfo;
        _fontFamily = fontFamily;
        _fontStyle = fontStyle;
        _fontWeight = fontWeight;
        _fontSize = fontSize;
        String _fontKey = FontInfo.createFontKey(_fontFamily, _fontStyle, _fontWeight);
        //Quick check-out for simple font family
        if (!fontInfo.hasFont(_fontKey)) {
            //Tokenizes font-family list
            StringTokenizer st = new StringTokenizer(_fontFamily, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                //Checks for quoted font family name
                if (token.charAt(0) == '"' || token.charAt(0) == '\'')
                    token = token.substring(1, token.length()-1);
                else {
                    //In a nonquoted font family name any sequence of whitespace
                    //inside should be converted to a single space
                    StringBuffer sb = new StringBuffer();
                    boolean spaced = false;
                    for (int i=0; i<token.length(); i++) {
                        char c = token.charAt(i);
                        if (!isWhitespace(c)) {
                            sb.append(c);
                            spaced = false;
                        }
                        else if (!spaced) {
                            sb.append(c);
                            spaced = true;
                        }
                    }
                    token = sb.toString();
                }
                //Checks found font family name for existence
                _fontKey = FontInfo.createFontKey(token, _fontStyle, _fontWeight);
                if (fontInfo.hasFont(_fontKey)) {
                    _fontFamily = token;
                    break;
                }
            }
        }
        _fontName = fontInfo.fontLookup(_fontKey);
        _metric = fontInfo.getMetricsFor(_fontName);
        _fontVariant = fontVariant;
        _letterSpacing = 0;
    }

    public FontState(FontInfo fontInfo, String fontFamily, String fontStyle,
                     String fontWeight, int fontSize,
                     int fontVariant, int letterSpacing) throws FOPException {
        this(fontInfo, fontFamily, fontStyle, fontWeight, fontSize,
             fontVariant);
        _letterSpacing = letterSpacing;
    }

    private static boolean isWhitespace(char ch) {
        return (ch <= 0x0020) &&
            (((((1L << 0x0009) |
            (1L << 0x000A) |
            (1L << 0x000C) |
            (1L << 0x000D) |
            (1L << 0x0020)) >> ch) & 1L) != 0);
    }

    public int getAscender() {
        return _metric.getAscender(_fontSize) / 1000;
    }

    public int getLetterSpacing() {
        return _letterSpacing;
    }


    public int getCapHeight() {
        return _metric.getCapHeight(_fontSize) / 1000;
    }

    public int getDescender() {
        return _metric.getDescender(_fontSize) / 1000;
    }

    public String getFontName() {
        return _fontName;
    }

    public int getFontSize() {
        return _fontSize;
    }

    public String getFontWeight() {
        return _fontWeight;
    }

    public String getFontFamily() {
        return _fontFamily;
    }

    public String getFontStyle() {
        return _fontStyle;
    }

    public int getFontVariant() {
        return _fontVariant;
    }

    public FontInfo getFontInfo() {
        return _fontInfo;
    }

    public int getXHeight() {
        return _metric.getXHeight(_fontSize) / 1000;
    }

    public Map getKerning() {
        if (_metric instanceof FontDescriptor) {
            Map ret = ((FontDescriptor)_metric).getKerningInfo();
            if (ret != null)
                return ret;
        }
        return EMPTY_MAP;
    }

    public int width(int charnum) {
        // returns width of given character number in millipoints
        return _letterSpacing + (_metric.width(charnum, _fontSize) / 1000);
    }

    /**
     * Map a java character (unicode) to a font character
     * Default uses CodePointMapping
     */
    public char mapChar(char c) {

        if (_metric instanceof org.apache.fop.render.pdf.Font) {
            return ((org.apache.fop.render.pdf.Font)_metric).mapChar(c);
        } else if (_metric instanceof org.apache.fop.render.awt.FontMetricsMapper) {
            return c;
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

    private int enWidth=-1;
    private int emWidth=-1;

    private final int getEmWidth() {
        if (emWidth<0) {
            char mappedChar = mapChar('m');
            // The mapping returns '#' for unmapped characters in
            // standard fonts.  What happens for other fonts?
            if (mappedChar == '#') {
                emWidth = 500 * getFontSize();
            } else {
                emWidth = width(mappedChar);
            }
        }
        return emWidth;
    }

    private final int getEnWidth() {
        if (enWidth<0) {
            char mappedChar = mapChar('n');
            // The mapping returns '#' for unmapped characters in
            // standard fonts.  What happens for other fonts?
            if (mappedChar != '#') {
                // Should do something to discover non-proportional fonts.
                enWidth = (getEmWidth()*9)/10;
            } else {
                enWidth = width(mappedChar);
            }
        }
        return enWidth;
    }

    /**
     * Helper method for getting the width of a unicode char
     * from the current fontstate.
     * This also performs some guessing on widths on various
     * versions of space that might not exists in the font.
     */
    public int getCharWidth(char c) {
        if ((c == '\n') || (c == '\r') || (c == '\t')) {
            return getCharWidth(' ');
        } else {
            char mappedChar = mapChar(c);
            if (mappedChar == '#' || mappedChar == 0) {
                // Estimate the width of spaces not represented in
                // the font
                if (c == '#') {
                    return width(mappedChar);
                } else if (c == ' ') {
                    return getEmWidth();
                } else if (c == '\u00A0') {
                    return getCharWidth(' ');
                } else if (c == '\u2000') {
                    return getEnWidth();
                } else if (c == '\u2001') {
                    return getEmWidth();
                } else if (c == '\u2002') {
                    return getEnWidth();
                } else if (c == '\u2003') {
                    return getEmWidth();
                } else if (c == '\u2004') {
                    return getEmWidth() / 3;
                } else if (c == '\u2005') {
                    return getEmWidth() / 4;
                } else if (c == '\u2006') {
                    return getEmWidth() / 6;
                } else if (c == '\u2007') {
                    return getCharWidth(' ');
                } else if (c == '\u2008') {
                    return getCharWidth('.');
                } else if (c == '\u2009') {
                    return getEmWidth() / 5;
                } else if (c == '\u200A') {
                    return getEmWidth() / 10;
                } else if (c == '\u200B') {
                    return 1;
                } else if (c == '\u202F') {
                    return getCharWidth(' ') / 2;
                } else if (c == '\u3000') {
                    return getCharWidth(' ') * 2;
                } else {
                    return width(mappedChar);
                }
            } else {
                return width(mappedChar);
            }
        }
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



