/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.layoutmgr.RetrieveMarkerLayoutManager;
import org.xml.sax.Attributes;

import java.util.List;

/**
 * The retrieve-marker formatting object.
 * This will create a layout manager that will retrieve
 * a marker based on the information.
 */
public class RetrieveMarker extends FObjMixed {

    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;

    /**
     * Create a retrieve marker object.
     *
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RetrieveMarker(FONode parent) {
        super(parent);
    }

    /**
     * Handle the attributes for the retrieve-marker.
     *
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.retrieveClassName =
            this.properties.get("retrieve-class-name").getString();
        this.retrievePosition =
            this.properties.get("retrieve-position").getEnum();
        this.retrieveBoundary =
            this.properties.get("retrieve-boundary").getEnum();
    }

    public void addLayoutManager(List lms) {
        RetrieveMarkerLayoutManager rmlm;
        rmlm = new RetrieveMarkerLayoutManager(retrieveClassName,
                                                retrievePosition,
                                                retrieveBoundary);
        rmlm.setUserAgent(getUserAgent());
        rmlm.setFObj(this);
        lms.add(rmlm);
    }
}
