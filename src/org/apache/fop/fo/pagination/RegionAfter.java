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
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.area.RegionReference;

// Java
import java.awt.Rectangle;

public class RegionAfter extends RegionBA {

    public RegionAfter(FONode parent) {
        super(parent);
    }

    protected Rectangle getViewportRectangle (FODimension reldims) {
        // Depends on extent, precedence ans writing mode
        Rectangle vpRect;
        if (this.wm == WritingMode.LR_TB || this.wm == WritingMode.RL_TB)
            vpRect = new Rectangle(0, reldims.bpd - getExtent(), reldims.ipd, getExtent());
        else
            vpRect = new Rectangle(0, reldims.bpd - getExtent(), getExtent(), reldims.ipd);
        if (getPrecedence() == false) {
            adjustIPD(vpRect, this.wm);
        }
        return vpRect;
    }

    protected String getDefaultRegionName() {
        return "xsl-region-after";
    }

    public int getRegionAreaClass() {
        return RegionReference.AFTER;
    }

    public String getRegionClass() {
        return Region.AFTER;
    }
}

