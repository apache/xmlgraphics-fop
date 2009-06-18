/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.datatypes.ColorType;

public class ColorTypeProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String propName) {
            super(propName);
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof ColorTypeProperty)
                return p;
            ColorType val = p.getColorType();
            if (val != null)
                return new ColorTypeProperty(val);
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    private ColorType colorType;

    public ColorTypeProperty(ColorType colorType) {
        this.colorType = colorType;
    }

    // Can't convert to any other types
    public ColorType getColorType() {
        return this.colorType;
    }

    public Object getObject() {
        return this.colorType;
    }

}
