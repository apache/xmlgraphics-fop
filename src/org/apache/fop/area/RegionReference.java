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

public class RegionReference extends Area implements Serializable, Cloneable {
    public static final int BEFORE = 0;
    public static final int START = 1;
    public static final int BODY = 2;
    public static final int END = 3;
    public static final int AFTER = 4;
    int regionClass = BEFORE;
    private CTM ctm;

    public RegionReference(int type) {
        regionClass = type;
    }

    /**
     * Set the Coordinate Transformation Matrix which transforms content
     * coordinates in this region reference area which are specified in
     * terms of "start" and "before" into coordinates in a system which
     * is positioned in "absolute" directions (with origin at lower left of
     * the region reference area.
     */
    public void setCTM(CTM ctm) {
        this.ctm = ctm;
    }

    public CTM getCTM() {
        return this.ctm;
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

    public Object clone() {
        RegionReference rr = new RegionReference(regionClass);
        rr.ctm = ctm;
        rr.setIPD(getIPD());
        return rr;
    }
}
