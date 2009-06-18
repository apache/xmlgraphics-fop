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

import org.xml.sax.Attributes;

public class Inline extends FObjMixed {

    // Textdecoration
    protected boolean underlined = false;
    protected boolean overlined = false;
    protected boolean lineThrough = false;


    public Inline(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

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


    public CharIterator charIterator() {
        return new InlineCharIterator(this, propMgr.getBorderAndPadding());
    }

}
