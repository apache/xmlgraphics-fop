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

import java.util.*;

import org.xml.sax.Attributes;

public class SimplePageMaster extends FObj {

    /**
     * Page regions (regionClass, Region)
     */
    private Hashtable _regions;

    LayoutMasterSet layoutMasterSet;
    PageMaster pageMaster;
    String masterName;

    // before and after data as required by start and end
    boolean beforePrecedence;
    int beforeHeight;
    boolean afterPrecedence;
    int afterHeight;

    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

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
        _regions = new Hashtable();

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
        if (getRegion(RegionBody.REGION_CLASS) != null) {
            BodyRegionArea body =
                (BodyRegionArea)getRegion(RegionBody.REGION_CLASS).makeRegionArea(contentRectangleXPosition,
                                          contentRectangleYPosition,
                                          contentRectangleWidth,
                                          contentRectangleHeight);
            this.pageMaster.addBody(body);
        } else {
            log.error("simple-page-master must have a region of class "
                                   + RegionBody.REGION_CLASS);
        }

        if (getRegion(RegionBefore.REGION_CLASS) != null) {
            RegionArea before =
                getRegion(RegionBefore.REGION_CLASS).makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight);
            this.pageMaster.addBefore(before);
            beforePrecedence =
                ((RegionBefore)getRegion(RegionBefore.REGION_CLASS)).getPrecedence();
            beforeHeight = before.getHeight();
        } else {
            beforePrecedence = false;
        }

        if (getRegion(RegionAfter.REGION_CLASS) != null) {
            RegionArea after =
                getRegion(RegionAfter.REGION_CLASS).makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight);
            this.pageMaster.addAfter(after);
            afterPrecedence =
                ((RegionAfter)getRegion(RegionAfter.REGION_CLASS)).getPrecedence();
            afterHeight = after.getHeight();
        } else {
            afterPrecedence = false;
        }

        if (getRegion(RegionStart.REGION_CLASS) != null) {
            RegionArea start =
                ((RegionStart)getRegion(RegionStart.REGION_CLASS)).makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeHeight, afterHeight);
            this.pageMaster.addStart(start);
        }

        if (getRegion(RegionEnd.REGION_CLASS) != null) {
            RegionArea end =
                ((RegionEnd)getRegion(RegionEnd.REGION_CLASS)).makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeHeight, afterHeight);
            this.pageMaster.addEnd(end);
        }
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

    protected Hashtable getRegions() {
        return _regions;
    }

    protected boolean regionNameExists(String regionName) {
        for (Enumeration regenum = _regions.elements();
                regenum.hasMoreElements(); ) {
            Region r = (Region)regenum.nextElement();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;

    }

}
