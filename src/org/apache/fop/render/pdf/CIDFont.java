/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFWArray;

public abstract class CIDFont extends Font {

    // Required
    public abstract String getCidBaseFont();
    public abstract byte getCidType();
    public abstract String getCharEncoding();
    public abstract String getRegistry();
    public abstract String getOrdering();
    public abstract int getSupplement();
    // Optional
    public int getDefaultWidth() {
        return 0;
    }

    public PDFWArray getWidths() {
        return null;
    }

    // public int getWinCharSet() { return 0; }

    // Need For FOP

    /**
     * Returns CMap Object .
     * <p>
     * If this method does not return null , the mapping from character codes
     * to a font number is performed in FOP . When the getCidType() method
     * returns CIDFontType2 , this method must not return null .
     */
    public CMap getCMap() {
        return null;
    }

    public boolean isMultiByte() {
        return true;
    }
}
