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
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;

// Java
import java.util.Iterator;
import java.util.List;

public class ListItem extends FObj {
    ListItemLabel label = null;
    ListItemBody body = null;

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

    public void addLayoutManager(List list) {
        if(label != null && body != null) {
            ListItemLayoutManager blm = new ListItemLayoutManager(this);
            blm.setLabel(label.getItemLayoutManager());
            blm.setBody(body.getItemLayoutManager());
            list.add(blm);
        }
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

    public void addChild(FONode child) {
        if ("fo:list-item-label".equals(child.getName())) {
            label = (ListItemLabel)child;
        } else if ("fo:list-item-body".equals(child.getName())) {
            body = (ListItemBody)child;
        } else if("fo:marker".equals(child.getName())) {
            // marker
        } else {
            // error
        }
    } 

    public boolean generatesInlineAreas() {
        return false;
    }

    protected boolean containsMarkers() {
        return true;
    }

}

