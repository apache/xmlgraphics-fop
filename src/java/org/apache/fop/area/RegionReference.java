/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.fo.pagination.Region;

/**
 * This is a region reference area for a page regions.
 * This area is the direct child of a region-viewport-area. It is cloneable
 * so the page master can make copies from the original page and regions.
 */
public class RegionReference extends Area implements Cloneable {

    private static final long serialVersionUID = -298980963268244238L;

    private int regionClass;
    private String regionName;
    private CTM ctm;

    // the list of block areas from the static flow
    private ArrayList<Area> blocks = new ArrayList<Area>();

    /** the parent {@link RegionViewport} for this object */
    protected RegionViewport regionViewport;

    /**
     * Create a new region reference area.
     *
     * @param regionFO the region.
     * @param parent the viewport for this region.
     */
    public RegionReference(Region regionFO, RegionViewport parent) {
        this(regionFO.getNameId(), regionFO.getRegionName(), parent);
    }

    /**
     * Create a new region reference area.
     *
     * @param regionClass the region class (as returned by Region.getNameId())
     * @param regionName the name of the region (as returned by Region.getRegionName())
     * @param parent the viewport for this region.
     */
    public RegionReference(int regionClass, String regionName, RegionViewport parent) {
        this.regionClass = regionClass;
        this.regionName = regionName;
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
        regionViewport = parent;
    }

    /** {@inheritDoc} */
    @Override
    public void addChildArea(Area child) {
        blocks.add(child);
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
     * @return Returns the parent RegionViewport.
     */
    public RegionViewport getRegionViewport() {
        return regionViewport;
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
    public List<Area> getBlocks() {
        return blocks;
    }

    /**
     * Get the region class of this region.
     *
     * @return the region class
     */
    public int getRegionClass() {
        return this.regionClass;
    }

    /** @return the region name */
    public String getRegionName() {
        return this.regionName;
    }

    /**
     * Add a block area to this region reference area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        addChildArea(block);
    }

    /**
     * Clone this region.
     * This is used when cloning the page by the page master.
     *
     * @return a copy of this region reference area
     */
    public Object clone() {
        RegionReference rr = new RegionReference(regionClass, regionName, regionViewport);
        rr.ctm = ctm;
        rr.setIPD(getIPD());
        rr.blocks = (ArrayList<Area>)blocks.clone();
        return rr;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {regionName=").append(regionName);
        sb.append(", regionClass=").append(regionClass);
        sb.append(", ctm=").append(ctm);
        sb.append("}");
        return sb.toString();
    }
}
