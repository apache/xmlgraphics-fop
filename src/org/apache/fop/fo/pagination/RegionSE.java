/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.WritingMode;

// Java
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
    protected void adjustIPD(Rectangle refRect, int wm) {
        int offset = 0;
        Region before = getSiblingRegion(Region.BEFORE);
        if (before != null && before.getPrecedence()) {
            offset = before.getExtent();
            refRect.translate(0, offset);
        }
        Region after = getSiblingRegion(Region.AFTER);
        if (after != null && after.getPrecedence()) {
            offset += after.getExtent();
        }
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB)
                refRect.height-=offset;
            else
                refRect.width-=offset;
        }
    }
}

