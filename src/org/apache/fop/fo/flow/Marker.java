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

import org.xml.sax.Attributes;

/**
 * Marker formatting object.
 * This is the marker formatting object that handles merkers.
 * This attempts to add itself to the parent formatting object.
 */
public class Marker extends FObjMixed {

    private String markerClassName;

    /**
     * Create a marker fo.
     *
     * @param parent the parent fo node
     */
    public Marker(FONode parent) {
        super(parent);
    }

    /**
     * Handle the attributes for this marker.
     * This gets the marker-class-name and attempts to add itself
     * to the parent formatting object.
     *
     * @param attlist the attribute list
     * @throws FOPException if there is an exception
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        this.markerClassName =
            this.properties.get("marker-class-name").getString();
    }

    protected boolean isMarker() {
        return true;
    }

    /**
     * Get the marker class name for this marker.
     *
     * @return the marker class name
     */
    public String getMarkerClassName() {
        return markerClassName;
    }


}
