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

// Java
import java.util.Iterator;

public class ListItemBody extends FObj {

    public ListItemBody(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // this.properties.get("id");
            // this.properties.get("keep-together");

            this.marker = 0;
            // initialize id
            String id = this.properties.get("id").getString();
            area.getIDReferences().initializeID(id, area);
        }

        /*
         * For calculating the lineage - The fo:list-item-body formatting object
         * does not generate any areas. The fo:list-item-body formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the children of the fo:list-item-body.
         */

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);

            Status status;
            if ((status = fo.layout(area)).isIncomplete()) {
                this.marker = i;
                if ((i == 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
                    return new Status(Status.AREA_FULL_NONE);
                } else {
                    return new Status(Status.AREA_FULL_SOME);
                }
            }
        }
        return new Status(Status.OK);
    }

}
