/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.RegionArea;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;

import org.xml.sax.Attributes;

public class RegionAfter extends Region {

    public static final String REGION_CLASS = "after";

    private int precedence;

    public RegionAfter(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        precedence = this.properties.get("precedence").getEnum();
    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight) {

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("clip");
        // this.properties.get("display-align");
        int extent = this.properties.get("extent").getLength().mvalue();
        // this.properties.get("overflow");
        // this.properties.get("precedence");
        // this.properties.get("region-name");
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        return new RegionArea(allocationRectangleXPosition,
                              allocationRectangleYPosition
                              - allocationRectangleHeight + extent,
                              allocationRectangleWidth, extent);
    }


    protected String getDefaultRegionName() {
        return "xsl-region-after";
    }

    protected String getElementName() {
        return "fo:region-after";
    }

    public String getRegionClass() {
        return REGION_CLASS;
    }

    public boolean getPrecedence() {
        return (precedence == Precedence.TRUE ? true : false);
    }

}
