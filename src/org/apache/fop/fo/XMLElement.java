/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

/**
 * class representing svg:svg pseudo flow object.
 */
public class XMLElement extends XMLObj {
    String namespace = "";

    /**
     * constructs an XML object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLElement(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        init();
    }

    private void init() {
        createBasicDocument();
    }

    public String getNameSpace() {
        return namespace;
    }
}
