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
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 */
public class InlineContainer extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new InlineContainer(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new InlineContainer.Maker();
    }

    protected InlineContainer(FObj parent,
                              PropertyList propertyList) throws FOPException {
        super(parent, propertyList);

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("block-progression-dimension");
        // this.properties.get("clip");
        // this.properties.get("display-align");
        // this.properties.get("dominant-baseline");
        // this.properties.get("height");
        // this.properties.get("id");
        // this.properties.get("inline-progression-dimension");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("overflow");
        // this.properties.get("reference-orientation");
        // this.properties.get("width");
        // this.properties.get("writing-mode");
    }

    public String getName() {
        return "fo:inline-container";
    }

}
