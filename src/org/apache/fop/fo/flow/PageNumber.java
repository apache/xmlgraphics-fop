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
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

public class PageNumber extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new PageNumber(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new PageNumber.Maker();
    }

    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceCollapse;
    TextState ts;

    public PageNumber(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:page-number";
    }

    public Status layout(Area area) throws FOPException {
        if (!(area instanceof BlockArea)) {
            log.warn("page-number outside block area");
            return new Status(Status.OK);
        }
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
        
            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(area.getFontInfo());
 
            // Common Margin Properties-Inline
            MarginInlineProps mProps = propMgr.getMarginInlineProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
        
            // this.properties.get("alignment-adjust");
            // this.properties.get("alignment-baseline");
            // this.properties.get("baseline-shift");
            // this.properties.get("dominant-baseline");
            // this.properties.get("id");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("letter-spacing");
            // this.properties.get("line-height");
            // this.properties.get("line-height-shift-adjustment");
            // this.properties.get("score-spaces");
            // this.properties.get("text-decoration");
            // this.properties.get("text-shadow");
            // this.properties.get("text-transform");
            // this.properties.get("word-spacing");

            ColorType c = this.properties.get("color").getColorType();
            this.red = c.red();
            this.green = c.green();
            this.blue = c.blue();

            this.wrapOption = this.properties.get("wrap-option").getEnum();
            this.whiteSpaceCollapse =
                this.properties.get("white-space-collapse").getEnum();
            ts = new TextState();
            this.marker = 0;

            // initialize id
            String id = this.properties.get("id").getString();
            area.getIDReferences().initializeID(id, area);
        }

        String p = area.getPage().getFormattedNumber();
        this.marker = FOText.addText((BlockArea)area,
                                     propMgr.getFontState(area.getFontInfo()),
                                     red, green, blue, wrapOption, null,
                                     whiteSpaceCollapse, p.toCharArray(), 0,
                                     p.length(), ts, VerticalAlign.BASELINE);
        return new Status(Status.OK);
    }

}
