/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.layout.FontMetric;

/**
 * base class for PDF font classes
 */
public abstract class Font implements FontMetric {

    /**
     * get the encoding of the font
     */
    public abstract String encoding();

    /**
     * get the base font name
     */
    public abstract String fontName();

    /**
     * get the subtype of the font, default is TYPE1
     */
    public byte getSubType() {
        return org.apache.fop.pdf.PDFFont.TYPE1;
    }

    /**
     * Provide a default mapping
     */
    public char mapChar(char c) {
        // Use default CodePointMapping
        if (c > 127) {
            char d = org.apache.fop.render.pdf.CodePointMapping.map[c];
            if (d != 0) {
                c = d;
            } else {
                c = '#';
            }
        }
        return c;
    }

    public boolean isMultiByte() {
        return false;
    }

}

