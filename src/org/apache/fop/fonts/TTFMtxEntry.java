/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

import java.io.*;
import java.util.ArrayList;

class TTFMtxEntry {
    int wx;
    int lsb;
    String name;
    int index;
    ArrayList unicodeIndex;
    int[] bbox;
    long offset;
    byte found;

    TTFMtxEntry() {
        name = "";
        found = 0;
        unicodeIndex = new ArrayList();
        bbox = new int[4];
    }

    public String toString(TTFFile t) {
        return new String("Glyph " + name + " index: " + getIndexAsString() + " bbox ["
                          + t.get_ttf_funit(bbox[0]) + " "
                          + t.get_ttf_funit(bbox[1]) + " "
                          + t.get_ttf_funit(bbox[2]) + " "
                          + t.get_ttf_funit(bbox[3]) + "] wx: "
                          + t.get_ttf_funit(wx));
    }

    /**
     * Returns the index.
     * @return int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Determines whether this index represents a reserved character.
     * @return True if it is reserved
     */
    public boolean isIndexReserved() {
        return (getIndex() >= 32768) && (getIndex() <= 65535);
    }

    /**
     * Returns a String representation of the index taking into account if
     * the index is in the reserved range.
     * @return index as String
     */
    public String getIndexAsString() {
        if (isIndexReserved()) {
            return Integer.toString(getIndex()) + " (reserved)";
        } else {
            return Integer.toString(getIndex());
        }
    }


}
