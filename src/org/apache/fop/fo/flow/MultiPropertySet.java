/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 */
public class MultiPropertySet extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new MultiPropertySet(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new MultiPropertySet.Maker();
    }

    protected MultiPropertySet(FObj parent, PropertyList propertyList)
            throws FOPException {
        super(parent, propertyList);
        this.name = "fo:multi-property-set";
    }

    public Status layout(Area area) throws FOPException {

        // this.properties.get("id");
        // this.properties.get("active-state");

        return super.layout(area);
    }
}
