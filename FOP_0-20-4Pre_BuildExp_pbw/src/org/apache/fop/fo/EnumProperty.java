/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;

public class EnumProperty extends Property {

    public static class Maker extends Property.Maker {

        protected Maker(String propName) {
            super(propName);
        }


        /**
         * Called by subclass if no match found.
         */
        public Property checkEnumValues(String value) {
            //log.error("Unknown enumerated value for property '"
            //                       + getPropName() + "': " + value);
            return null;
        }

        protected Property findConstant(String value) {
            return null;
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws FOPException {
            if (p instanceof EnumProperty)
                return p;
            else
                return null;
        }

    }

    private int value;

    public EnumProperty(int explicitValue) {
        this.value = explicitValue;
    }

    public int getEnum() {
        return this.value;
    }

    public Object getObject() {
        // FIXME: return String value: property must reference maker
        // return maker.getEnumValue(this.value);
        return new Integer(this.value);
    }

}
