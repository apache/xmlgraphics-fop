/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

// FOP


/**
 * Base class for PDF font classes
 */
public abstract class Font implements FontMetrics {

    /**
     * Get the encoding of the font.
     * @return the encoding
     */
    public abstract String getEncoding();

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public abstract char mapChar(char c);

    /**
     * Determines whether the font is a multibyte font.
     * @return True if it is multibyte
     */
    public boolean isMultiByte() {
        return false;
    }

}

