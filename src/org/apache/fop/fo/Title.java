/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

/**
 */
public class Title extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Title(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Title.Maker();
    }

    protected Title(FObj parent,
                    PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:title";
    }

    public int layout(Area area) throws FOPException {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        FontState fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        Property prop;
        prop = this.properties.get("baseline-shift");
        if (prop instanceof LengthProperty) {
            Length bShift = prop.getLength();
        } else if (prop instanceof EnumProperty) {
            int bShift = prop.getEnum();
        }
        ColorType col = this.properties.get("color").getColorType();
        Length lHeight = this.properties.get("line-height").getLength();
        int lShiftAdj = this.properties.get(
                          "line-height-shift-adjustment").getEnum();
        int vis = this.properties.get("visibility").getEnum();
        Length zIndex = this.properties.get("z-index").getLength();

        return super.layout(area);
    }
}
