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
import org.apache.fop.layout.RegionArea;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.apps.FOPException;

public class RegionEnd extends Region {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new RegionEnd(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new RegionEnd.Maker();
    }

    public static final String REGION_CLASS = "end";


    protected RegionEnd(FObj parent,
                        PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:region-end";
    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight,
                              boolean beforePrecedence,
                              boolean afterPrecedence, int beforeExtent,
                              int afterExtent) {
        int extent = this.properties.get("extent").getLength().mvalue();

        int startY = allocationRectangleYPosition;
        int startH = allocationRectangleHeight;
        if (beforePrecedence) {
            startY -= beforeExtent;
            startH -= beforeExtent;
        }
        if (afterPrecedence)
            startH -= afterExtent;
        RegionArea area = new RegionArea(allocationRectangleXPosition
					 + allocationRectangleWidth - extent,
					 startY, extent, startH);
	area.setBackground(propMgr.getBackgroundProps());
	return area;

    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight) {

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("clip");
        // this.properties.get("display-align");
        int extent = this.properties.get("extent").getLength().mvalue();
        // this.properties.get("overflow");
        // this.properties.get("region-name");
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        return makeRegionArea(allocationRectangleXPosition,
                              allocationRectangleYPosition,
                              allocationRectangleWidth, extent, false, false,
                              0, 0);
    }

    protected String getDefaultRegionName() {
        return "xsl-region-end";
    }

    public String getRegionClass() {
        return REGION_CLASS;
    }

    public int getExtent() {
        return properties.get("extent").getLength().mvalue();
    }
}
