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
import org.apache.fop.fo.pagination.*;
import org.apache.fop.apps.FOPException;

public class StaticContent extends Flow {

    public StaticContent(FONode parent) {
        super(parent);
    }

    public void setup() {

    }

    // flowname checking is more stringient for static content currently
    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            throw new FOPException("A 'flow-name' is required for "
                                   + getName() + ".");
        } else {
            super.setFlowName(name);
        }

    }

}
