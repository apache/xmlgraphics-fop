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
import org.apache.fop.layoutmgr.list.Item;

// Java
import java.util.Iterator;

public class ListItemBody extends FObj {

    public ListItemBody(FONode parent) {
        super(parent);
    }

    public Item getItemLayoutManager() {
        Item item = new Item(this);
        return item;
    }

    public void setup() {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            setupID();
            // this.properties.get("keep-together");

        /*
         * For calculating the lineage - The fo:list-item-body formatting object
         * does not generate any areas. The fo:list-item-body formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the children of the fo:list-item-body.
         */

    }

    protected boolean containsMarkers() {
        return true;
    }

}

