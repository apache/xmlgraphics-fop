/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.util.HashMap;

import org.apache.fop.fo.properties.FontVariant;
import org.apache.fop.render.pdf.CodePointMapping;

public class FontState {

    private String _fontName;
    private int _fontSize;
    private String _fontFamily;
    private String _fontStyle;
    private int _fontWeight;
    private int _fontVariant;

    private FontMetric _metric;

    private static HashMap EMPTY_HASHMAP = new HashMap();


    public FontState(String key, FontMetric met, int fontSize) {
        _fontSize = fontSize;
        _fontName = key;
        _metric = met;
    }

    public int getAscender() {
        return _metric.getAscender(_fontSize) / 1000;
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

    public int getXHeight() {
        return _metric.getXHeight(_fontSize) / 1000;
    }

    public HashMap getKerning() {
        if (_metric instanceof FontDescriptor) {
            HashMap ret = ((FontDescriptor)_metric).getKerningInfo();
            if (ret != null)
                return ret;
        }
        return EMPTY_HASHMAP;
    }

    public int width(int charnum) {
        // returns width of given character number in millipoints
        return (_metric.width(charnum, _fontSize) / 1000);
    }

    /**
     * Map a java character (unicode) to a font character
     * Default uses CodePointMapping
     */
    public char mapChar(char c) {

        if (_metric instanceof org.apache.fop.render.pdf.Font) {
            return ((org.apache.fop.render.pdf.Font)_metric).mapChar(c);
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

    public String toString() {
	StringBuffer sbuf = new StringBuffer();
	sbuf.append('(');
	sbuf.append(_fontFamily);
	sbuf.append(',');
	sbuf.append(_fontName);
	sbuf.append(',');
	sbuf.append(_fontSize);
	sbuf.append(',');
	sbuf.append(_fontStyle);
	sbuf.append(',');
	sbuf.append(_fontWeight);
	sbuf.append(')');
	return sbuf.toString();
    }
}



