/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

/**
 * Abstract base class for CID fonts.
 */
public abstract class CIDFont extends CustomFont {

    // ---- Required ----
    /**
     * Returns the name of the base font.
     * @return the name of the base font
     */
    public abstract String getCidBaseFont();
    
    /**
     * Returns the type of the CID font.
     * @return the type of the CID font
     */
    public abstract CIDFontType getCIDType();
    
    /**
     * Returns the name of the issuer of the font.
     * @return a String identifying an issuer of character collections — 
     * for example, Adobe
     */
    public abstract String getRegistry();
    
    /**
     * Returns a font name for use within a registry.
     * @return a String that uniquely names a character collection issued by 
     * a specific registry — for example, Japan1.
     */
    public abstract String getOrdering();
    
    /**
     * Returns the supplement number of the character collection.
     * @return the supplement number
     */
    public abstract int getSupplement();
    

    // ---- Optional ----
    /**
     * Returns the default width for this font.
     * @return the default width
     */
    public int getDefaultWidth() {
        return 0;
    }

    /**
     * @see org.apache.fop.fonts.Font#isMultiByte()
     */
    public boolean isMultiByte() {
        return true;
    }
}
