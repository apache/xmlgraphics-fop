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
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Iterator;

public class ListBlock extends FObj {

    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int spaceBetweenListRows = 0;
    ColorType backgroundColor;

    public ListBlock(FONode parent) {
        super(parent);
    }

    public void setup() throws FOPException {

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
            // this.properties.get("provisional-distance-between-starts");
            // this.properties.get("provisional-label-separation");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
            this.startIndent =
                this.properties.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.properties.get("end-indent").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.spaceBetweenListRows = 0;    // not used at present
            this.backgroundColor =
                this.properties.get("background-color").getColorType();

    }

    public boolean generatesInlineAreas() {
        return false;
    }


}
