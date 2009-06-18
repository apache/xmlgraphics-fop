/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf.fonts;

/**
 * This is just a holder class for bfentries - not get/put methods provided
 */
public class BFEntry {
    public int unicodeStart;
    public int unicodeEnd;
    public int glyphStartIndex;

    public BFEntry() {}

    public BFEntry(int unicodeStart, int unicodeEnd, int glyphStartIndex) {
        this.unicodeStart = unicodeStart;
        this.unicodeEnd = unicodeEnd;
        this.glyphStartIndex = glyphStartIndex;
    }

}
