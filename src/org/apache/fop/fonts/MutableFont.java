/*
 * $Id$
 * Copyright (C) 2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.util.Map;


/**
 * This interface is used to set the values of a font during configuration time.
 */
public interface MutableFont {

    /**
     * Sets the font name.
     * @param name font name
     */
    void setFontName(String name);
    
    /**
     * Sets the path to the embeddable font file.
     * @param path URI to the file
     */
    void setEmbedFileName(String path);

    /**
     * Sets the resource name of the embeddable font file.
     * @param name resource name
     */
    void setEmbedResourceName(String name);

    /**
     * Sets the capital height value.
     * @param capHeight capital height
     */
    void setCapHeight(int capHeight);
    
    /**
     * Sets the ascent value.
     * @param ascender ascent height
     */
    void setAscender(int ascender);
    
    /**
     * Sets the descent value.
     * @param descender descent value
     */
    void setDescender(int descender);

    /**
     * Sets the font's bounding box
     * @param bbox bounding box
     */
    void setFontBBox(int[] bbox);

    /**
     * Sets the font's flags
     * @param flags flags
     */
    void setFlags(int flags);
    
    /**
     * Sets the font's StemV value.
     * @param stemV StemV
     */
    void setStemV(int stemV);
    
    /**
     * Sets the font's italic angle.
     * @param italicAngle italic angle
     */
    void setItalicAngle(int italicAngle);
    
    /**
     * Sets the font's default width
     * @param width default width
     */
    void setMissingWidth(int width);
 
    /**
     * Sets the font type.
     * @param fontType font type
     */
    void setFontType(FontType fontType);
 
    /**
     * Sets the index of the first character in the character table.
     * @param index index of first character
     */
    void setFirstChar(int index);
    
    /**
     * Sets the index of the last character in the character table.
     * @param index index of the last character
     */
    void setLastChar(int index);
    
    /**
     * Enables/disabled kerning.
     * @param enabled True if kerning should be enabled if available
     */
    void setKerningEnabled(boolean enabled);
    
    /**
     * Adds an entry to the kerning table.
     * @param key Kerning key
     * @param value Kerning value
     */
    void putKerningEntry(Integer key, Map value);
       
}
