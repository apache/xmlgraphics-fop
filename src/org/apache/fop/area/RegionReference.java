/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a region reference area for the page regions.
 * This area represents a region on the page. It is cloneable
 * so the page master can make copies from the original page and regions.
 */
public class RegionReference extends Area implements Cloneable {
    /**
     * The before region.
     */
    public static final int BEFORE = 0;

    /**
     * The start region.
     */
    public static final int START = 1;

    /**
     * The body region.
     */
    public static final int BODY = 2;

    /**
     * The end region.
     */
    public static final int END = 3;

    /**
     * The after region.
     */
    public static final int AFTER = 4;

    private int regionClass = BEFORE;
    private CTM ctm;
    // the list of block areas from the static flow
    private List blocks = new ArrayList();

    /**
     * Create a new region reference area.
     *
     * @param type the region class type
     */
    public RegionReference(int type) {
        regionClass = type;
    }

    /**
     * Set the Coordinate Transformation Matrix which transforms content
     * coordinates in this region reference area which are specified in
     * terms of "start" and "before" into coordinates in a system which
     * is positioned in "absolute" directions (with origin at lower left of
     * the region reference area.
     *
     * @param ctm the current transform to position this region
     */
    public void setCTM(CTM ctm) {
        this.ctm = ctm;
    }

    /**
     * Get the current transform of this region.
     *
     * @return ctm the current transform to position this region
     */
    public CTM getCTM() {
        return this.ctm;
    }

    /**
     * Get the block in this region.
     *
     * @return the list of blocks in this region
     */
    public List getBlocks() {
        return blocks;
    }

    /**
     * Get the region class of this region.
     *
     * @return the region class
     */
    public int getRegionClass() {
        return regionClass;
    }

    /**
     * Add a block area to this region reference area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        blocks.add(block);
    }

    /**
     * Clone this region.
     * This is used when cloning the page by the page master.
     * The blocks are not copied since the master will have no blocks.
     *
     * @return a copy of this region reference area
     */
    public Object clone() {
        RegionReference rr = new RegionReference(regionClass);
        rr.ctm = ctm;
        rr.setIPD(getIPD());
        return rr;
    }

}
