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
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Vector;

import org.xml.sax.Attributes;

public class RetrieveMarker extends FObjMixed {

    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;

    public RetrieveMarker(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.retrieveClassName =
            this.properties.get("retrieve-class-name").getString();
        this.retrievePosition =
            this.properties.get("retrieve-position").getEnum();
        this.retrieveBoundary =
            this.properties.get("retrieve-boundary").getEnum();
    }

}
