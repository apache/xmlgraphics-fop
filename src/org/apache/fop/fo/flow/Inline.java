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
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;

// Java
import java.util.Enumeration;

public class Inline extends FObjMixed {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Inline(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Inline.Maker();
    }

    // Textdecoration
    protected boolean underlined = false;
    protected boolean overlined = false;
    protected boolean lineThrough = false;


    public Inline(FObj parent,
                  PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "fo:inline";
        if (parent.getName().equals("fo:flow")) {
            throw new FOPException("inline formatting objects cannot"
                                   + " be directly under flow");
        }

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
        // this.properties.get("color");
        // this.properties.get("dominant-baseline");
        // this.properties.get("id");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("text-devoration");
        // this.properties.get("visibility");
        // this.properties.get("z-index");

        int textDecoration = this.properties.get("text-decoration").getEnum();

        if (textDecoration == TextDecoration.UNDERLINE) {
            this.underlined = true;
        }

        if (textDecoration == TextDecoration.OVERLINE) {
            this.overlined = true;
        }

        if (textDecoration == TextDecoration.LINE_THROUGH) {
            this.lineThrough = true;
        }
    }

    protected void addCharacters(char data[], int start, int length) {
        FOText ft = new FOText(data, start, length, this);
        ft.setLogger(log);
        ft.setUnderlined(underlined);
        ft.setOverlined(overlined);
        ft.setLineThrough(lineThrough);
        children.addElement(ft);
    }

}
