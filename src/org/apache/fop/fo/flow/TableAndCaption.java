/*
 * -- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 */
public class TableAndCaption extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new TableAndCaption(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new TableAndCaption.Maker();
    }

    protected TableAndCaption(FObj parent,
                              PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:table-and-caption";
    }

    public int layout(Area area) throws FOPException {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("caption-side");
        // this.properties.get("id");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");

        return super.layout(area);
    }
}
