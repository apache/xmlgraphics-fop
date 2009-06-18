/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

/**
 * The CMap entry contains information of a Unicode range and the
 * the glyph indexes related to the range
 */
public class TTFCmapEntry {
    public int unicodeStart;
    public int unicodeEnd;
    public int glyphStartIndex;

    TTFCmapEntry() {
        unicodeStart = 0;
        unicodeEnd = 0;
        glyphStartIndex = 0;
    }

    TTFCmapEntry(int unicodeStart, int unicodeEnd, int glyphStartIndex) {
        this.unicodeStart = unicodeStart;
        this.unicodeEnd = unicodeEnd;
        this.glyphStartIndex = glyphStartIndex;
    }

    public boolean equals(Object o) {
        if (o instanceof TTFCmapEntry) {
            TTFCmapEntry ce = (TTFCmapEntry)o;
            if (ce.unicodeStart == this.unicodeStart
                    && ce.unicodeEnd == this.unicodeEnd
                    && ce.glyphStartIndex == this.glyphStartIndex)
                return true;
        }
        return false;
    }

}
