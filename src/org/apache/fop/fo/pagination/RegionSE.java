/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;


import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

import java.awt.Rectangle;

public abstract class RegionSE extends RegionBASE {

    protected RegionSE(FONode parent) {
        super(parent);
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If  before and after have precedence = true, the start and end
     * regions only go to the limits of their extents, otherwise
     * they extend in the BPD to the page reference rectangle
     * diminish by extend of start and end if present.
     */
    protected void adjustIPD(Rectangle refRect) {
	int yoff = 0;
	Region before = getSiblingRegion(Region.BEFORE);
	if (before != null && before.getPrecedence()) {
	    yoff = before.getExtent();
	    refRect.translate(0, yoff);
	}
	Region after = getSiblingRegion(Region.AFTER);
	if (after != null && after.getPrecedence()) {
	    yoff += after.getExtent();
	}
	if (yoff > 0) {
	    refRect.grow(0,-yoff);
	}
    }
}
