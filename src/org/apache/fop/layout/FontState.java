/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.util.Hashtable;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.FontVariant;
import org.apache.fop.render.pdf.CodePointMapping;

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

    private static Hashtable EMPTY_HASHTABLE = new Hashtable();


    public FontState(FontInfo fontInfo, String fontFamily, String fontStyle,
                     String fontWeight, int fontSize,
                     int fontVariant) throws FOPException {
        _fontInfo = fontInfo;
        _fontFamily = fontFamily;
        _fontStyle = fontStyle;
        _fontWeight = fontWeight;
        _fontSize = fontSize;
        _fontName = fontInfo.fontLookup(fontFamily, fontStyle, fontWeight);
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

    public Hashtable getKerning() {
        if (_metric instanceof FontDescriptor) {
            Hashtable ret = ((FontDescriptor)_metric).getKerningInfo();
            if (ret != null)
                return ret;
        }
        return EMPTY_HASHTABLE;
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



