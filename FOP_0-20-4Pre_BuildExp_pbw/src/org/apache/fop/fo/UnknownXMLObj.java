/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.inline.*;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Element;

public class UnknownXMLObj extends XMLObj {
    String namespace;

    public static class Maker extends ElementMapping.Maker {
        String space;

        Maker(String sp) {
            space = sp;
        }

        public FONode make(FONode parent) {
            return new UnknownXMLObj(parent, space);
        }
    }

    /**
     * constructs an unknown xml object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected UnknownXMLObj(FONode parent, String space) {
        super(parent);
        this.namespace = space;
    }

    public String getNameSpace() {
        return this.namespace;
    }

    protected void addChild(FONode child) {
        if(doc == null) {
            createBasicDocument();
        }
        super.addChild(child);
    }

    protected void addCharacters(char data[], int start, int length) {
        if(doc == null) {
            createBasicDocument();
        }
        super.addCharacters(data, start, length);
    }

    public Status layout(Area area) throws FOPException {
        if(!"".equals(this.namespace)) {
            log.error("no handler defined for " + this.namespace + ":" + this.name + " foreign xml");
        } else {
            log.error("no handler defined for (none):" + this.name + " foreign xml");
        }

        /* return status */
        return new Status(Status.OK);
    }
}

