/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;

/**
 */
public class MultiSwitch extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new MultiSwitch(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new MultiSwitch.Maker();
    }

    protected MultiSwitch(FObj parent,
                          PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "fo:multi-switch";
    }

}
