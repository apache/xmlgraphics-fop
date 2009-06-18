/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.RegionReference;


public class RegionEnd extends RegionSE {



    public RegionEnd(FONode parent) {
        super(parent);
    }


    protected Rectangle getViewportRectangle (FODimension reldims) {
	// Depends on extent and precedence
	Rectangle vpRect =
	    new Rectangle(reldims.ipd - getExtent(), 0,
			  getExtent(), reldims.bpd);
	adjustIPD(vpRect);
	return vpRect;
    }

    protected String getDefaultRegionName() {
        return "xsl-region-end";
    }


    public String getRegionClass() {
        return Region.END;
    }

    public int getRegionAreaClass() {
        return RegionReference.END;
    }

}
