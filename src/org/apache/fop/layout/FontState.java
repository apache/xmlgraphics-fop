/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.FontVariant;
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

    private static Map EMPTY_MAP = new java.util.HashMap();


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

}



