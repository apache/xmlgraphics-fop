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
import org.apache.fop.apps.FOPException;

import org.apache.fop.layoutmgr.table.Body;

// Java
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class TableBody extends FObj {

    int spaceBefore;
    int spaceAfter;
    ColorType backgroundColor;

    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * Return a LayoutManager responsible for laying out this FObj's content.
     * Must override in subclasses if their content can be laid out.
     */
    public void addLayoutManager(List list) {
        list.add(getLayoutManager());
    }

    public Body getLayoutManager() {
        Body blm = new Body(this);
        return blm;
    }

    public void setup() throws FOPException {
        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps =
          propMgr.getRelativePositionProps();

        setupID();

        this.spaceBefore = this.properties.get(
                             "space-before.optimum").getLength().mvalue();
        this.spaceAfter = this.properties.get(
                            "space-after.optimum").getLength().mvalue();
        this.backgroundColor =
          this.properties.get("background-color").getColorType();

    }

}

