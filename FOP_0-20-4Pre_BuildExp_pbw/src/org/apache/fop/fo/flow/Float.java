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
public class Float extends ToBeImplementedElement {

    public Float(FONode parent) {
        super(parent);
        this.name = "fo:float";
    }

    public Status layout(Area area) throws FOPException {

        // this.properties.get("float");
        // this.properties.get("clear");

        return super.layout(area);
    }
}
