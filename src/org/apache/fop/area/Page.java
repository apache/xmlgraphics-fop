/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * The page.
 * This holds the contents of the page. Each region is added.
 * The unresolved references area added so that if the page is
 * serialized then it will handle the resolving properly after
 * being reloaded.
 * This is serializable so it can be saved to cache to save
 * memory if there are forward references.
 * The page is cloneable so the page master can make copies of
 * the top level page and regions.
 */
public class Page implements Serializable, Cloneable {
    // contains before, start, body, end and after regions
    private RegionViewport regionBefore = null;
    private RegionViewport regionStart = null;
    private RegionViewport regionBody = null;
    private RegionViewport regionEnd = null;
    private RegionViewport regionAfter = null;

    // hashmap of markers for this page
    // start and end are added by the fo that contains the markers
    private Map markerStart = null;
    private Map markerEnd = null;

    // temporary map of unresolved objects used when serializing the page
    private Map unresolved = null;

    /**
     * Set the region on this page.
     *
     * @param areaclass the area class of the region to set
     * @param port the region viewport to set
     */
    public void setRegion(int areaclass, RegionViewport port) {
        if (areaclass == RegionReference.BEFORE) {
            regionBefore = port;
        } else if (areaclass == RegionReference.START) {
            regionStart = port;
        } else if (areaclass == RegionReference.BODY) {
            regionBody = port;
        } else if (areaclass == RegionReference.END) {
            regionEnd = port;
        } else if (areaclass == RegionReference.AFTER) {
            regionAfter = port;
        }
    }

    /**
     * Get the region from this page.
     *
     * @param areaclass the region area class
     * @return the region viewport or null if none
     */
    public RegionViewport getRegion(int areaclass) {
        if (areaclass == RegionReference.BEFORE) {
            return regionBefore;
        } else if (areaclass == RegionReference.START) {
            return regionStart;
        } else if (areaclass == RegionReference.BODY) {
            return regionBody;
        } else if (areaclass == RegionReference.END) {
            return regionEnd;
        } else if (areaclass == RegionReference.AFTER) {
            return regionAfter;
        }
        return null;
    }

    /**
     * Clone this page.
     * This returns a new page with a clone of all the regions.
     *
     * @return a new clone of this page
     */
    public Object clone() {
        Page p = new Page();
        if (regionBefore != null) {
            p.regionBefore = (RegionViewport)regionBefore.clone();
        }
        if (regionStart != null) {
            p.regionStart = (RegionViewport)regionStart.clone();
        }
        if (regionBody != null) {
            p.regionBody = (RegionViewport)regionBody.clone();
        }
        if (regionEnd != null) {
            p.regionEnd = (RegionViewport)regionEnd.clone();
        }
        if (regionAfter != null) {
            p.regionAfter = (RegionViewport)regionAfter.clone();
        }

        return p;
    }

    /**
     * Set the unresolved references on this page for serializing.
     *
     * @param unres the map of unresolved objects
     */
    public void setUnresolvedReferences(Map unres) {
        unresolved = unres;
    }

    /**
     * Get the map unresolved references from this page.
     * This should be called after deserializing to retrieve
     * the map of unresolved references that were serialized.
     *
     * @return the de-serialized map of unresolved objects
     */
    public Map getUnresolvedReferences() {
        return unresolved;
    }
}

