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
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

/**
 */
public class MultiCase extends ToBeImplementedElement {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new MultiCase(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new MultiCase.Maker();
    }

    protected MultiCase(FObj parent,
                        PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = "fo:multi-case";
    }

    public Status layout(Area area) throws FOPException {

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // this.properties.get("id");
        // this.properties.get("starting-state");
        // this.properties.get("case-name");
        // this.properties.get("case-title");

        return super.layout(area);
    }
}
