/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

/**
 */
public class BidiOverride extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new BidiOverride(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new BidiOverride.Maker();
    }

    protected BidiOverride(FObj parent,
                           PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:bidi-override";
    }

    public Status layout(Area area) throws FOPException {

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Font Properties
        //this.fontState = propMgr.getFontState(area.getFontInfo());

        // Common Margin Properties-Inline
        RelativePositionProps mProps = propMgr.getRelativePositionProps();

        // this.properties.get("color");
        // this.properties.get("direction");
        // this.properties.get("id");
        // this.properties.get("letter-spacing");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("score-spaces");
        // this.properties.get("text-shadow");
        // this.properties.get("text-transform");
        // this.properties.get("unicode-bidi");
        // this.properties.get("word-spacing");

        return super.layout(area);
    }
}
