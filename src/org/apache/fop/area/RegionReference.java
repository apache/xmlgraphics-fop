/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RegionReference extends Area implements Serializable {
    public static final int BEFORE = 0;
    public static final int START = 1;
    public static final int BODY = 2;
    public static final int END = 3;
    public static final int AFTER = 4;
    int regionClass = BEFORE;

    public RegionReference(int type) {
        regionClass = type;
    }

    // the list of block areas from the static flow
    ArrayList blocks = new ArrayList();

    public List getBlocks() {
        return blocks;
    }

    public int getRegionClass() {
        return regionClass;
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

}
