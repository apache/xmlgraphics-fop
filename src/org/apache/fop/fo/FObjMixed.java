/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;

/**
 * base class for representation of mixed content formatting objects
 * and their processing
 */
public class FObjMixed extends FObj {

    public FObjMixed(FObj parent) {
        super(parent);
    }

    protected void addCharacters(char data[], int start, int length) {
        addChild(new FOText(data, start, length, this));
    }

    public Status layout(Area area) throws FOPException {

        if (this.properties != null) {
            Property prop = this.properties.get("id");
            if (prop != null) {
                String id = prop.getString();

                if (this.marker == START) {
                    if (area.getIDReferences() != null)
                        area.getIDReferences().createID(id);
                    this.marker = 0;
                }

                if (this.marker == 0) {
                    if (area.getIDReferences() != null)
                        area.getIDReferences().configureID(id, area);
                }
            }
        }

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.elementAt(i);
            Status status;
            if ((status = fo.layout(area)).isIncomplete()) {
                this.marker = i;
                return status;
            }
        }
        return new Status(Status.OK);
    }

}

