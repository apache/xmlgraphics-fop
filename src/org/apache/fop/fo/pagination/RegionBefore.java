/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.properties.WritingMode;
import org.apache.fop.fo.FONode;
import org.apache.fop.area.RegionReference;

// Java
import java.awt.Rectangle;

public class RegionBefore extends RegionBA {

    public RegionBefore(FONode parent) {
        super(parent);
    }

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
        // Depends on extent, precedence and writing mode
        // This should return rectangle in writing-mode coordinates relative
        // to the page-reference area rectangle
        // This means the origin is (start, before) and the dimensions are (ipd,bpd)
        // Before is always 0, start depends on extent
        // ipd depends on precedence, bpd=extent
        Rectangle vpRect;
        if (this.wm == WritingMode.LR_TB || this.wm == WritingMode.RL_TB)
            vpRect = new Rectangle(0, 0, reldims.ipd, getExtent());
        else
            vpRect = new Rectangle(0, 0, getExtent(), reldims.ipd);
        if (getPrecedence() == false) {
            adjustIPD(vpRect, this.wm);
        }
        return vpRect;
    }
}

