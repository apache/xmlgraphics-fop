/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

/**
 * This interface enhances the font metrics interface with access methods to
 * value needed to register fonts in various target formats like PDF or 
 * PostScript.
 */
public interface FontDescriptor extends FontMetrics {

    /**
     * Returns the ascender value of the font. (Ascent in pdf spec)
     * @return the ascender
     */
    int getAscender();
    
    
    /**
     * Returns the capital height of the font.
     * @return the capiptal height
     */
    int getCapHeight();
    
    
    /**
     * Returns the descender value of the font. (Descent in pdf spec)
     * @return the descender value
     */
    int getDescender();
    
    
    /**
     * Returns the flags for the font. (See pdf spec)
     * @return the flags
     */
    int getFlags();
    
    
    /**
     * Returns the font's bounding box.
     * @return the bounding box
     */
    int[] getFontBBox();
    
    
    /**
     * Returns the italic angle for the font.
     * @return the italic angle
     */
    int getItalicAngle();
    
    
    /**
     * Returns the vertical stem width for the font.
     * @return the vertical stem width
     */
    int getStemV();

    
    /**
     * Indicates if this font may be embedded.
     * @return True, if embedding is possible/permitted
     */
    boolean isEmbeddable();
    
    
}
