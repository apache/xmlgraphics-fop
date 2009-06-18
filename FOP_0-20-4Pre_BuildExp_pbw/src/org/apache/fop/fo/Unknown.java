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
public class Unknown extends FONode {

    public static class Maker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Unknown(parent);
        }
    }

    public Unknown(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        log.debug("Layout Unknown element");
        return new Status(Status.OK);
    }
}
