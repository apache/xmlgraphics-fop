/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.RegionArea;
import org.apache.fop.layout.BodyRegionArea;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginProps;

public class RegionBody extends Region {

    public static final String REGION_CLASS = "body";

    ColorType backgroundColor;

    public RegionBody(FObj parent) {
        super(parent);
    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight) {

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();

        // this.properties.get("clip");
        // this.properties.get("display-align");
        // this.properties.get("region-name");
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        BodyRegionArea body = new BodyRegionArea(allocationRectangleXPosition
                                                 + mProps.marginLeft,
                                                 allocationRectangleYPosition
                                                 - mProps.marginTop,
                                                 allocationRectangleWidth
                                                 - mProps.marginLeft
                                                 - mProps.marginRight,
                                                 allocationRectangleHeight
                                                 - mProps.marginTop
                                                 - mProps.marginBottom);

        int overflow = this.properties.get("overflow").getEnum();
        String columnCountAsString =
            this.properties.get("column-count").getString();
        int columnCount = 1;
        try {
            columnCount = Integer.parseInt(columnCountAsString);
        } catch (NumberFormatException nfe) {
            log.error("Bad value on region body 'column-count'");
            columnCount = 1;
        }
        if ((columnCount > 1) && (overflow == Overflow.SCROLL)) {
            // recover by setting 'column-count' to 1. This is allowed but
            // not required by the spec.
            log.error("Setting 'column-count' to 1 because "
                                   + "'overflow' is set to 'scroll'");
            columnCount = 1;
        }
        body.setColumnCount(columnCount);

        int columnGap =
            this.properties.get("column-gap").getLength().mvalue();
        body.setColumnGap(columnGap);

        body.setBackgroundColor(backgroundColor);

        return body;
    }

    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }

    protected String getElementName() {
        return "fo:region-body";
    }

    public String getRegionClass() {
        return REGION_CLASS;
    }

}
