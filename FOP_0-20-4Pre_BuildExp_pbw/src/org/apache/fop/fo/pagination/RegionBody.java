/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// FOP

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.layout.MarginProps;

public class RegionBody extends Region {

    ColorType backgroundColor;

    public RegionBody(FONode parent) {
        super(parent);
    }

    protected Rectangle getViewportRectangle (FODimension reldims)
    {
	/*
	 * Use space-before and space-after which will use corresponding
	 * absolute margin properties if specified. For indents:
	 * try to get corresponding absolute margin property using the
	 * writing-mode on the page (not on the region-body!). If that's not
	 * set but indent is explicitly set, it will return that.
	 */
        MarginProps mProps = propMgr.getMarginProps();
	int start = getRelMargin(PropertyList.START, "start-indent");
	return new Rectangle( start, mProps.spaceBefore,
			      reldims.ipd - start -
			      getRelMargin(PropertyList.END, "end-indent"),
			      reldims.bpd - mProps.spaceBefore -
			      mProps.spaceAfter);
    }

    /**
     * Get the relative margin using parent's writing mode, not own
     * writing mode.
     */
    private int getRelMargin(int reldir, String sRelPropName) {
	FObj parent = (FObj) getParent();
	String sPropName = "margin-" +
	    parent.properties.wmRelToAbs(reldir);
	Property prop = properties.getExplicitBaseProp(sPropName);
	if (prop == null) {
	   prop = properties.getExplicitBaseProp(sRelPropName);
	}
	return ((prop != null)? prop.getLength().mvalue() : 0);
    }

    protected void setRegionTraits(RegionReference r, Rectangle2D absRegVPRect) {
	super.setRegionTraits(r, absRegVPRect);

//         r.setBackgroundColor(backgroundColor);
    }

    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }


    public String getRegionClass() {
        return Region.BODY;
    }

    public int getRegionAreaClass() {
        return RegionReference.BODY;
    }

    /**
     * Override the inherited method.
     */
    public RegionReference makeRegionReferenceArea(Rectangle2D absRegVPRect) {
	// Should set some column stuff here I think, or put it elsewhere
	BodyRegion body = new BodyRegion();
	setRegionTraits(body, absRegVPRect);
        int columnCount=
            this.properties.get("column-count").getNumber().intValue();
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
	return body;
    }

}
