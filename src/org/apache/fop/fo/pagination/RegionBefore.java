/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
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


    protected Rectangle getViewportRectangle (Rectangle pageRefRect) {
	// Depends on extent and precedence
	Rectangle vpRect = new Rectangle(pageRefRect);
	vpRect.height = getExtent();
	if (getPrecedence() == false) {
	    adjustIPD(vpRect);
	}
	return vpRect;
    }

}
