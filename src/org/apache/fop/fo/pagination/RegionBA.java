/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.Precedence;
import org.apache.fop.fo.properties.WritingMode;

// Java
import java.awt.Rectangle;

public abstract class RegionBA extends RegionBASE {

    private boolean bPrecedence;

    protected RegionBA(FONode parent) {
        super(parent);
    }

    boolean getPrecedence() {
        return bPrecedence;
    }

    public void end() {
        super.end();
        bPrecedence =
            (this.properties.get("precedence").getEnum()==Precedence.TRUE);
    }

    /**
     * Adjust the viewport reference rectangle for a region as a function
     * of precedence.
     * If precedence is false on a before or after region, its
     * inline-progression-dimension is limited by the extent of the start
     * and end regions if they are present.
     */
    protected void adjustIPD(Rectangle vpRect, int wm) {
        int offset = 0;
        Region start = getSiblingRegion(Region.START);
        if (start != null) {
            offset = start.getExtent();
            vpRect.translate(offset, 0);
        }
        Region end =getSiblingRegion(Region.END);
        if (end != null) {
            offset += end.getExtent();
        }
        if (offset > 0) {
            if (wm == WritingMode.LR_TB || wm == WritingMode.RL_TB)
                vpRect.width-=offset;
            else
                vpRect.height-=offset;
        }
    }
}

