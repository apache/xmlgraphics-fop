/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.ColorType;

// Java
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.List;

public class BasicLink extends Inline {

    public BasicLink(FONode parent) {
        super(parent);
    }

    // add start and end properties for the link
    public void addLayoutManager(List lms) {
       super.addLayoutManager(lms);
    }

    public void setup() {
        String destination;
        int linkType;

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

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
        // this.properties.get("destination-place-offset");
        // this.properties.get("dominant-baseline");
        // this.properties.get("external-destination");        
        setupID();
        // this.properties.get("indicate-destination");  
        // this.properties.get("internal-destination");  
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("show-destination");  
        // this.properties.get("target-processing-context");  
        // this.properties.get("target-presentation-context");  
        // this.properties.get("target-stylesheet");  

    }

}
