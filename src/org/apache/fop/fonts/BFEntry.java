/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

/**
 * This is just a holder class for bfentries.
 */
public class BFEntry {
    
    private int unicodeStart;
    private int unicodeEnd;
    private int glyphStartIndex;

    /**
     * Main constructor.
     * @param unicodeStart Unicode start index
     * @param unicodeEnd Unicode end index
     * @param glyphStartIndex glyph start index
     */
    public BFEntry(int unicodeStart, int unicodeEnd, int glyphStartIndex) {
        this.unicodeStart = unicodeStart;
        this.unicodeEnd = unicodeEnd;
        this.glyphStartIndex = glyphStartIndex;
    }

    /**
     * Returns the unicodeStart.
     * @return the Unicode start index
     */
    public int getUnicodeStart() {
        return unicodeStart;
    }

    /**
     * Returns the unicodeEnd.
     * @return the Unicode end index
     */
    public int getUnicodeEnd() {
        return unicodeEnd;
    }

    /**
     * Returns the glyphStartIndex.
     * @return the glyph start index
     */
    public int getGlyphStartIndex() {
        return glyphStartIndex;
    }

}
