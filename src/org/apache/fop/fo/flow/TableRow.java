/*
 * -- $Id$ --
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

import org.apache.fop.layoutmgr.table.Row;

// Java
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class TableRow extends FObj {

    boolean setup = false;

    int breakAfter;
    ColorType backgroundColor;

    KeepValue keepWithNext;
    KeepValue keepWithPrevious;
    KeepValue keepTogether;

    int minHeight = 0;    // force row height

    public TableRow(FONode parent) {
        super(parent);
    }

    /**
     */
    public void addLayoutManager(List list) {
        Row rlm = new Row(this);
        list.add(rlm);
    }

    public KeepValue getKeepWithPrevious() {
        return keepWithPrevious;
    }

    public void doSetup() {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
            
        // this.properties.get("block-progression-dimension");

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();
                    
        // this.properties.get("break-before");
        // this.properties.get("break-after");
        setupID();
        // this.properties.get("height");
        // this.properties.get("keep-together");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");


        this.breakAfter = this.properties.get("break-after").getEnum();
        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        this.keepTogether = getKeepValue("keep-together.within-column");
        this.keepWithNext = getKeepValue("keep-with-next.within-column");
        this.keepWithPrevious =
            getKeepValue("keep-with-previous.within-column");

        this.minHeight = this.properties.get("height").getLength().mvalue();
        setup = true;
    }

    private KeepValue getKeepValue(String sPropName) {
        Property p = this.properties.get(sPropName);
        Number n = p.getNumber();
        if (n != null)
            return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
        switch (p.getEnum()) {
        case Constants.ALWAYS:
            return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
        // break;
        case Constants.AUTO:
        default:
            return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
        // break;
        }
    }
}

