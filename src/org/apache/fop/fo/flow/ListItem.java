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
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Iterator;

public class ListItem extends FObj {

    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;

    public ListItem(FONode parent) {
        super(parent);
    }

    public void setup() {

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

            // this.properties.get("break-after");
            // this.properties.get("break-before");
            setupID();
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("relative-align");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();

    }

    public boolean generatesInlineAreas() {
        return false;
    }

}
