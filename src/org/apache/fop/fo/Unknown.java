/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

/**
 * This represents an unknown element.
 * For example with unsupported namespaces.
 * This prevents any further problems arising from the unknown
 * data.
 */
public class Unknown extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Unknown(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Unknown.Maker();
    }

    protected Unknown(FObj parent,
                    PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "unknown";
    }

    public Status layout(Area area) throws FOPException {
        log.debug("Layout Unknown element");
        return new Status(Status.OK);
    }
}
