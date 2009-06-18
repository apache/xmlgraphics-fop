/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
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
        return new String("Glyph " + name + " index: " + index + " bbox [ "
                          + t.get_ttf_funit(bbox[0]) + " "
                          + t.get_ttf_funit(bbox[1]) + " "
                          + t.get_ttf_funit(bbox[2]) + " "
                          + t.get_ttf_funit(bbox[3]) + "]" + "wx: "
                          + t.get_ttf_funit(wx));
    }

}
