/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.pagination.*;
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;

public class StaticContent extends Flow {

    public StaticContent(FONode parent) {
        super(parent);
        ((PageSequence)parent).setIsFlowSet(false);    // hacquery of sorts
    }

    public Status layout(Area area) throws FOPException {
        return layout(area, null);
    }


    public Status layout(Area area, Region region) throws FOPException {

//         int numChildren = this.children.size();
//         // Set area absolute height so that link rectangles will be drawn correctly in xsl-before and xsl-after
//         String regionClass = "none";
//         if (region != null) {
//             regionClass = region.getRegionClass();
//         } else {
//             if (getFlowName().equals("xsl-region-before")) {
//                 regionClass = RegionBefore.REGION_CLASS;
//             } else if (getFlowName().equals("xsl-region-after")) {
//                 regionClass = RegionAfter.REGION_CLASS;
//             } else if (getFlowName().equals("xsl-region-start")) {
//                 regionClass = RegionStart.REGION_CLASS;
//             } else if (getFlowName().equals("xsl-region-end")) {
//                 regionClass = RegionEnd.REGION_CLASS;
//             }

//         }

//         if (area instanceof org.apache.fop.layout.AreaContainer)
//             ((org.apache.fop.layout.AreaContainer)area).setAreaName(regionClass);

//         if (regionClass.equals(RegionBefore.REGION_CLASS)) {
//             area.setAbsoluteHeight(-area.getMaxHeight());
//         } else if (regionClass.equals(RegionAfter.REGION_CLASS)) {
//             area.setAbsoluteHeight(area.getPage().getBody().getMaxHeight());
//         }
// 	setContentWidth(area.getContentWidth());

//         for (int i = 0; i < numChildren; i++) {
//             FObj fo = (FObj)children.elementAt(i);

//             Status status;
//             if ((status = fo.layout(area)).isIncomplete()) {
//                 // in fact all should be laid out and clip, error etc depending on 'overflow'
//                 log.warn("Some static content could not fit in the area.");
//                 this.marker = i;
//                 if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
//                     status = new Status(Status.AREA_FULL_SOME);
//                 }
//                 return (status);
//             }
//         }
//         resetMarker();
         return new Status(Status.OK);
    }

    // flowname checking is more stringient for static content currently
    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            throw new FOPException("A 'flow-name' is required for "
                                   + getName() + ".");
        } else {
            super.setFlowName(name);
        }

    }

}
