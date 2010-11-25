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

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.SimplePercentBaseContext;
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
public class Page extends AreaTreeObject implements Serializable, Cloneable {

    private static final long serialVersionUID = 6272157047421543866L;

    // contains before, start, body, end and after regions
    private RegionViewport regionBefore = null;
    private RegionViewport regionStart = null;
    private RegionViewport regionBody = null;
    private RegionViewport regionEnd = null;
    private RegionViewport regionAfter = null;

    // temporary map of unresolved objects used when serializing the page
    private Map unresolved = null;

    /** Set to true to make this page behave as if it were not empty. */
    private boolean fakeNonEmpty = false;

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
        // Width and Height of the page view port
        FODimension pageViewPortDims = new FODimension(spm.getPageWidth().getValue()
                            ,  spm.getPageHeight().getValue());

        // Get absolute margin properties (top, left, bottom, right)
        CommonMarginBlock mProps = spm.getCommonMarginBlock();

        /*
         * Create the page reference area rectangle (0,0 is at top left
         * of the "page media" and y increases
         * when moving towards the bottom of the page.
         * The media rectangle itself is (0,0,pageWidth,pageHeight).
         */
        /* Special rules apply to resolving margins in the page context.
         * Contrary to normal margins in this case top and bottom margin
         * are resolved relative to the height. In the property subsystem
         * all margin properties are configured to using BLOCK_WIDTH.
         * That's why we 'cheat' here and setup a context for the height but
         * use the LengthBase.BLOCK_WIDTH.
         */
        SimplePercentBaseContext pageWidthContext
            = new SimplePercentBaseContext(null, LengthBase.CONTAINING_BLOCK_WIDTH
                                            , pageViewPortDims.ipd);
        SimplePercentBaseContext pageHeightContext
            = new SimplePercentBaseContext(null, LengthBase.CONTAINING_BLOCK_WIDTH
                                            , pageViewPortDims.bpd);

        Rectangle pageRefRect
            =  new Rectangle(mProps.marginLeft.getValue(pageWidthContext)
                            , mProps.marginTop.getValue(pageHeightContext)
                            , pageViewPortDims.ipd
                                - mProps.marginLeft.getValue(pageWidthContext)
                                - mProps.marginRight.getValue(pageWidthContext)
                            , pageViewPortDims.bpd
                                - mProps.marginTop.getValue(pageHeightContext)
                                - mProps.marginBottom.getValue(pageHeightContext));

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
            RegionViewport rvp = makeRegionViewport(r, reldims, pageCTM, spm);
            if (r.getNameId() == Constants.FO_REGION_BODY) {
                rr = new BodyRegion((RegionBody) r, rvp);
            } else {
                rr = new RegionReference(r, rvp);
            }
            // set borders and padding traits
            // (a little extensions wrt what prescribed by the specs at 6.4.14)
            TraitSetter.addBorders(rr, r.getCommonBorderPaddingBackground(),
                    false, false, false, false, null);
            TraitSetter.addPadding(rr, r.getCommonBorderPaddingBackground(),
                    false, false, false, false, null);
            setRegionReferencePosition(rr, r, rvp.getViewArea());
            rvp.setRegionReference(rr);
            setRegionViewport(r.getNameId(), rvp);
       }
    }

    /**
     * Call this method to force this page to pretend not to be empty.
     */
    public void fakeNonEmpty() {
        this.fakeNonEmpty = true;
    }

    /**
     * Creates a RegionViewport Area object for this pagination Region.
     * @param r the region the viewport is to be created for
     * @param reldims relative dimensions
     * @param pageCTM page coordinate transformation matrix
     * @param spm the simple-page-master for this page
     * @return the new region viewport
     */
    private RegionViewport makeRegionViewport(Region r, FODimension reldims, CTM pageCTM,
        SimplePageMaster spm) {
        Rectangle2D relRegionRect = r.getViewportRectangle(reldims, spm);
        Rectangle2D absRegionRect = pageCTM.transform(relRegionRect);
        // Get the region viewport rectangle in absolute coords by
        // transforming it using the page CTM
        RegionViewport rv = new RegionViewport(absRegionRect);
        rv.setBPD((int)relRegionRect.getHeight());
        rv.setIPD((int)relRegionRect.getWidth());
        TraitSetter.addBackground(rv, r.getCommonBorderPaddingBackground(), null);
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
        rr.setIPD(reldims.ipd
                - rr.getBorderAndPaddingWidthStart()
                - rr.getBorderAndPaddingWidthEnd());
        rr.setBPD(reldims.bpd
                - rr.getBorderAndPaddingWidthBefore()
                - rr.getBorderAndPaddingWidthAfter());
    }

    /**
     * Set the region on this page.
     *
     * @param areaclass the area class of the region to set
     * @param port the region viewport to set
     */
    public void setRegionViewport(int areaclass, RegionViewport port) {
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
     * @param areaClass the region area class
     * @return the region viewport or null if none
     */
    public RegionViewport getRegionViewport(int areaClass) {
        switch (areaClass) {
        case Constants.FO_REGION_BEFORE:
            return regionBefore;
        case Constants.FO_REGION_START:
            return regionStart;
        case Constants.FO_REGION_BODY:
            return regionBody;
        case Constants.FO_REGION_END:
            return regionEnd;
        case Constants.FO_REGION_AFTER:
            return regionAfter;
        default:
            throw new IllegalArgumentException("No such area class with ID = " + areaClass);
        }
    }

    /**
     * Indicates whether any FOs have been added to the body region
     *
     * @return whether any FOs have been added to the body region
     */
    public boolean isEmpty() {
        if (fakeNonEmpty) {
            return false;
        } else if (regionBody == null) {
            return true;
        } else {
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
     * @param unres the Map of unresolved objects
     */
    public void setUnresolvedReferences(Map unres) {
        unresolved = unres;
    }

    /**
     * Get the map unresolved references from this page.
     * This should be called after deserializing to retrieve
     * the map of unresolved references that were serialized.
     *
     * @return the de-serialized HashMap of unresolved objects
     */
    public Map getUnresolvedReferences() {
        return unresolved;
    }

}


