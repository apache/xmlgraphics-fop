/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.RegionReference;

import java.awt.Rectangle;
import org.xml.sax.Attributes;

public class RegionBefore extends RegionBA {


    public RegionBefore(FONode parent) {
        super(parent);
    }

//     public void handleAttrs(Attributes attlist) throws FOPException {
//         super.handleAttrs(attlist);
//     }


    protected String getDefaultRegionName() {
        return "xsl-region-before";
    }

    public String getRegionClass() {
        return Region.BEFORE;
    }

    public int getRegionAreaClass() {
        return RegionReference.BEFORE;
    }


    protected Rectangle getViewportRectangle (FODimension reldims) {
	// Depends on extent and precedence
      // This should return rectangle in writing-mode coordinates relative
      // to the page-reference area rectangle
      // This means the origin is (start, before) and the dimensions are (ipd,bpd)
      // Before is always 0, start depends on extent
      // ipd depends on precedence, bpd=extent
	Rectangle vpRect = new Rectangle(0, 0, reldims.ipd, getExtent());
	if (getPrecedence() == false) {
	    adjustIPD(vpRect);
	}
	return vpRect;
    }

}
