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

public class ListItemLabel extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new ListItemLabel(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new ListItemLabel.Maker();
    }

    public ListItemLabel(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    public String getName() {
        return "fo:list-item-label";
    }

    public Status layout(Area area) throws FOPException {
        int numChildren = this.children.size();

        if (numChildren != 1) {
            throw new FOPException("list-item-label must have exactly one block in this version of FOP");
        }

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();
     
        // this.properties.get("id");
        // this.properties.get("keep-together");

        // initialize id
        String id = this.properties.get("id").getString();
        area.getIDReferences().initializeID(id, area);

        Block block = (Block)children.get(0);

        /*
         * For calculating the lineage - The fo:list-item-label formatting object
         * does not generate any areas. The fo:list-item-label formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the children of the fo:list-item-label.
         */

        Status status;
        status = block.layout(area);
        area.addDisplaySpace(-block.getAreaHeight());
        return status;
    }

}
