/*
 * $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;

public class ToBeImplementedProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String propName) {
            super(propName);
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof ToBeImplementedProperty)
                return p;
            ToBeImplementedProperty val =
    new ToBeImplementedProperty(getPropName());
            return val;
        }

    }

    public ToBeImplementedProperty(String propName) {
        MessageHandler.errorln("Warning: property - \"" + propName
                               + "\" is not implemented yet.");
    }

}
