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
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;

public class Flow extends AbstractFlow {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new Flow(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new Flow.Maker();
    }

    protected Flow(FObj parent,
                   PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        setFlowName(getProperty("flow-name").getString());
        pageSequence.addFlow(this);
    }

    public String getName() {
        return "fo:flow";
    }

    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            log.warn("A 'flow-name' is required for "
                     + getName()
                     + ". This constraint will be enforced in future versions of FOP");
            _flowName = "xsl-region-body";
        } else {
            _flowName = name;
        }

    }

}
