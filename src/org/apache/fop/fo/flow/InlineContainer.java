/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

/**
 */
public class InlineContainer extends ToBeImplementedElement {

    public InlineContainer(FObj parent) {
        super(parent);
        this.name = "fo:inline-container";
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
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

}
