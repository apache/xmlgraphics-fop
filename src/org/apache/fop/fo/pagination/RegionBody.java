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
import org.apache.fop.fo.PropertyList;
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
        // Common Margin Properties-Block
	// Need these in writing-mode relative coordinates
	// Or shall we get absolute and transform to relative using writing mode?
        MarginProps mProps = propMgr.getMarginProps();
	/**
	System.err.println("BodyRegion margin props=" + mProps.startIndent + ","
			   + mProps.spaceBefore + "," + mProps.endIndent + ","
			   + mProps.spaceAfter);

        return new Rectangle( mProps.startIndent, mProps.spaceBefore,
			      reldims.ipd - mProps.startIndent - mProps.endIndent,
			      reldims.bpd - mProps.spaceBefore - mProps.spaceAfter);
	**/
	return new Rectangle( mProps.marginLeft, mProps.marginTop,
			      reldims.ipd - mProps.marginLeft - mProps.marginRight,
			      reldims.bpd - mProps.marginTop - mProps.marginBottom);
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
