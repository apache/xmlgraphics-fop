/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.layout.RegionArea;
import org.apache.fop.layout.BodyRegionArea;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.apps.FOPException;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class modeling the fo:simple-page-master object.
 *
 * @see <a href="@XSLFO-STD@#fo_simple-page-master"
 *     target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.12</a>
 */
public class SimplePageMaster extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new SimplePageMaster(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new SimplePageMaster.Maker();
    }

    /**
     * Page regions (regionClass, Region)
     */
    private HashMap _regions;


    LayoutMasterSet layoutMasterSet;
    PageMaster pageMaster;
    String masterName;

    // before and after data as required by start and end
    boolean beforePrecedence = false;
    int beforeExtent, afterExtent, startExtent, endExtent;
    boolean afterPrecedence = false;

    protected SimplePageMaster(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.properties.get("master-name").getString();
            if (masterName == null) {
                log.warn("simple-page-master does not have "
                                       + "a master-name and so is being ignored");
            } else {
                this.layoutMasterSet.addSimplePageMaster(this);
            }
        } else {
            throw new FOPException("fo:simple-page-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName());
        }
        _regions = new HashMap();

    }

    public String getName() {
        return "fo:simple-page-master";
    }

    protected void end() {
        int pageWidth =
            this.properties.get("page-width").getLength().mvalue();
        int pageHeight =
            this.properties.get("page-height").getLength().mvalue();
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();

        int contentRectangleXPosition = mProps.marginLeft;
        int contentRectangleYPosition = pageHeight - mProps.marginTop;
        int contentRectangleWidth = pageWidth - mProps.marginLeft
                                    - mProps.marginRight;
        int contentRectangleHeight = pageHeight - mProps.marginTop
                                     - mProps.marginBottom;
        this.pageMaster = new PageMaster(pageWidth, pageHeight);
        Region body = getRegion(RegionBody.REGION_CLASS);
        RegionBefore before = (RegionBefore)getRegion(RegionBefore.REGION_CLASS);
        RegionAfter after = (RegionAfter)getRegion(RegionAfter.REGION_CLASS);
        RegionStart start = (RegionStart)getRegion(RegionStart.REGION_CLASS);
        RegionEnd end = (RegionEnd)getRegion(RegionEnd.REGION_CLASS);
        if (before != null) {
            beforePrecedence = before.getPrecedence();
            beforeExtent = before.getExtent();
        }
        if (after != null) {
            afterPrecedence = after.getPrecedence();
            afterExtent = after.getExtent();
        }
        if (start != null)
            startExtent = start.getExtent();
        if (end != null)
            endExtent = end.getExtent();

        if (body != null)
            this.pageMaster.addBody((BodyRegionArea)body.makeRegionArea(
                    contentRectangleXPosition,
                    contentRectangleYPosition,
                    contentRectangleWidth,
                    contentRectangleHeight));
        else
            log.error("simple-page-master must have a region of class "
                                   + RegionBody.REGION_CLASS);

        if (before != null)
            this.pageMaster.addBefore(before.makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight, startExtent, endExtent));

        if (after != null)
            this.pageMaster.addAfter(after.makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight, startExtent, endExtent));

        if (start != null)
            this.pageMaster.addStart(start.makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeExtent, afterExtent));

        if (end != null)
            this.pageMaster.addEnd(end.makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeExtent, afterExtent));
    }

    public PageMaster getPageMaster() {
        return this.pageMaster;
    }

    public PageMaster getNextPageMaster() {
        return this.pageMaster;
    }

    public String getMasterName() {
        return masterName;
    }


    protected void addRegion(Region region) throws FOPException {
        if (_regions.containsKey(region.getRegionClass())) {
            throw new FOPException("Only one region of class "
                                   + region.getRegionClass()
                                   + " allowed within a simple-page-master.");
        } else {
            _regions.put(region.getRegionClass(), region);
        }
    }

    protected Region getRegion(String regionClass) {
        return (Region)_regions.get(regionClass);
    }

    protected HashMap getRegions() {
        return _regions;
    }

    protected boolean regionNameExists(String regionName) {
        for (Iterator i = _regions.values().iterator(); i.hasNext(); ) {
            Region r = (Region)i.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }

}
