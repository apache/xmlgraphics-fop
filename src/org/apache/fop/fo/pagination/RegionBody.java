/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.layout.MarginProps;

public class RegionBody extends Region {

    ColorType backgroundColor;

    public RegionBody(FONode parent) {
        super(parent);
    }

    protected Rectangle getViewportRectangle (Rectangle pageRefRect)
    {
        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();
        return
	    new Rectangle((int)pageRefRect.getX() + mProps.marginLeft,
			  (int)pageRefRect.getY() - mProps.marginTop,
			  (int)pageRefRect.getWidth() - mProps.marginLeft -
			  mProps.marginRight,
			  (int)pageRefRect.getHeight() - mProps.marginTop -
			  mProps.marginBottom);
    }

    protected void setRegionTraits(RegionReference r) {
	super.setRegionTraits(r);

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
//         r.setColumnCount(columnCount);

//         int columnGap =
//             this.properties.get("column-gap").getLength().mvalue();
//         r.setColumnGap(columnGap);

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
    public RegionReference makeRegionReferenceArea() {
	// Should set some column stuff here I think, or put it elsewhere
	return new BodyRegion();
    }

}
