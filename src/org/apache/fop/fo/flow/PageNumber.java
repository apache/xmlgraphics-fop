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

import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Word;

// Java
import java.util.List;

public class PageNumber extends FObj {

    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceCollapse;
    TextState ts;

    public PageNumber(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List lms) {
        lms.add(new LeafNodeLayoutManager(this) {
            public InlineArea get(int index) {
                if(index > 0)
                    return null;
                // get page string from parent, build area
                Word inline = new Word();
                //String parentLM.getCurrentPageNumber();
                inline.setWord("01");
                return inline;
            }

            public boolean resolved() {
                return true;
            }
        });
    }

    public void setup() throws FOPException {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
        
            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Font Properties
            //this.fontState = propMgr.getFontState(structHandler.getFontInfo());
 
            // Common Margin Properties-Inline
            MarginInlineProps mProps = propMgr.getMarginInlineProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
        
            // this.properties.get("alignment-adjust");
            // this.properties.get("alignment-baseline");
            // this.properties.get("baseline-shift");
            // this.properties.get("dominant-baseline");
            setupID();
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

    }

}
