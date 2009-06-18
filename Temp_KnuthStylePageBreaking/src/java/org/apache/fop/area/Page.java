/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: Page.java,v 1.4 2004/02/27 17:41:26 jeremias Exp $ */

package org.apache.fop.area;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.layoutmgr.TraitSetter;

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

    // temporary map of unresolved objects used when serializing the page
    private HashMap unresolved = null;

    /**
     *  Empty constructor, for cloning 
     */
    public Page() {
    }

    /**
     * Constructor
     * @param spm SimplePageMaster containing the dimensions for this
     *            page-reference-area
     */
    public Page(SimplePageMaster spm) {
        int pageWidth = spm.getPageWidth().getValue();
        int pageHeight = spm.getPageHeight().getValue();

        // Get absolute margin properties (top, left, bottom, right)
        CommonMarginBlock mProps = spm.getCommonMarginBlock();

        /*
         * Create the page reference area rectangle (0,0 is at top left
         * of the "page media" and y increases
         * when moving towards the bottom of the page.
         * The media rectangle itself is (0,0,pageWidth,pageHeight).
         */
        Rectangle pageRefRect =
            new Rectangle(mProps.marginLeft.getValue(), mProps.marginTop.getValue(),
            pageWidth - mProps.marginLeft.getValue() - mProps.marginRight.getValue(),
            pageHeight - mProps.marginTop.getValue() - mProps.marginBottom.getValue());

        // Set up the CTM on the page reference area based on writing-mode
        // and reference-orientation
        FODimension reldims = new FODimension(0, 0);
        CTM pageCTM = CTM.getCTMandRelDims(spm.getReferenceOrientation(),
            spm.getWritingMode(), pageRefRect, reldims);

        // Create a RegionViewport/ reference area pair for each page region
        RegionReference rr = null;
        for (Iterator regenum = spm.getRegions().values().iterator();
            regenum.hasNext();) {
            Region r = (Region)regenum.next();
            RegionViewport rvp = makeRegionViewport(r, reldims, pageCTM);
            r.setLayoutDimension(PercentBase.BLOCK_IPD, rvp.getIPD());
            r.setLayoutDimension(PercentBase.BLOCK_BPD, rvp.getBPD());
            if (r.getNameId() == Constants.FO_REGION_BODY) {
                RegionBody rb = (RegionBody) r;
                rr = new BodyRegion(rb.getColumnCount(), rb.getColumnGap(),
                       rvp);
            } else {
                rr = new RegionReference(r.getNameId(), rvp);
            }
            setRegionReferencePosition(rr, r, rvp.getViewArea());
            rvp.setRegionReference(rr);
            setRegionViewport(r.getNameId(), rvp);
       }
    }

    /**
     * Creates a RegionViewport Area object for this pagination Region.
     * @param reldims relative dimensions
     * @param pageCTM page coordinate transformation matrix
     * @return the new region viewport
     */
    private RegionViewport makeRegionViewport(Region r, FODimension reldims, CTM pageCTM) {
        Rectangle2D relRegionRect = r.getViewportRectangle(reldims);
        Rectangle2D absRegionRect = pageCTM.transform(relRegionRect);
        // Get the region viewport rectangle in absolute coords by
        // transforming it using the page CTM
        RegionViewport rv = new RegionViewport(absRegionRect);
        rv.setBPD((int)relRegionRect.getHeight());
        rv.setIPD((int)relRegionRect.getWidth());
        TraitSetter.addBackground(rv, r.getCommonBorderPaddingBackground());
        rv.setClip(r.getOverflow() == Constants.EN_HIDDEN 
                || r.getOverflow() == Constants.EN_ERROR_IF_OVERFLOW);
        return rv;
    }
   
    /**
     * Set the region reference position within the region viewport.
     * This sets the transform that is used to place the contents of
     * the region reference.
     *
     * @param rr the region reference area
     * @param r the region-xxx formatting object
     * @param absRegVPRect The region viewport rectangle in "absolute" coordinates
     * where x=distance from left, y=distance from bottom, width=right-left
     * height=top-bottom
     */
    private void setRegionReferencePosition(RegionReference rr, Region r, 
                                  Rectangle2D absRegVPRect) {
        FODimension reldims = new FODimension(0, 0);
        rr.setCTM(CTM.getCTMandRelDims(r.getReferenceOrientation(),
                r.getWritingMode(), absRegVPRect, reldims));
        rr.setIPD(reldims.ipd);
        rr.setBPD(reldims.bpd);
    }    
    
    /**
     * Set the region on this page.
     *
     * @param areaclass the area class of the region to set
     * @param port the region viewport to set
     */
    private void setRegionViewport(int areaclass, RegionViewport port) {
        if (areaclass == Constants.FO_REGION_BEFORE) {
            regionBefore = port;
        } else if (areaclass == Constants.FO_REGION_START) {
            regionStart = port;
        } else if (areaclass == Constants.FO_REGION_BODY) {
            regionBody = port;
        } else if (areaclass == Constants.FO_REGION_END) {
            regionEnd = port;
        } else if (areaclass == Constants.FO_REGION_AFTER) {
            regionAfter = port;
        }
    }

    /**
     * Get the region from this page.
     *
     * @param areaclass the region area class
     * @return the region viewport or null if none
     */
    public RegionViewport getRegionViewport(int areaclass) {
        if (areaclass == Constants.FO_REGION_BEFORE) {
            return regionBefore;
        } else if (areaclass == Constants.FO_REGION_START) {
            return regionStart;
        } else if (areaclass == Constants.FO_REGION_BODY) {
            return regionBody;
        } else if (areaclass == Constants.FO_REGION_END) {
            return regionEnd;
        } else if (areaclass == Constants.FO_REGION_AFTER) {
            return regionAfter;
        }
        throw new IllegalArgumentException("No such area class with ID = "
            + areaclass);
    }

    /**
     * indicates whether any FOs have been added to the body region
     *
     * @return whether any FOs have been added to the body region
     */
    public boolean isEmpty() {
        if (regionBody == null) {
            return true;
        }
        else {
            BodyRegion body = (BodyRegion)regionBody.getRegionReference();
            return body.isEmpty();
        }
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
     * @param unres the HashMap of unresolved objects
     */
    public void setUnresolvedReferences(HashMap unres) {
        unresolved = unres;
    }

    /**
     * Get the map unresolved references from this page.
     * This should be called after deserializing to retrieve
     * the map of unresolved references that were serialized.
     *
     * @return the de-serialized HashMap of unresolved objects
     */
    public HashMap getUnresolvedReferences() {
        return unresolved;
    }
}

