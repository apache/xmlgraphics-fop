/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.expr.Numeric;

public class NumberProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String propName) {
            super(propName);
        }

        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof NumberProperty)
                return p;
            Number val = p.getNumber();
            if (val != null)
                return new NumberProperty(val);
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    private Number number;

    public NumberProperty(Number num) {
        this.number = num;
    }

    public NumberProperty(double num) {
        this.number = new Double(num);
    }

    public NumberProperty(int num) {
        this.number = new Integer(num);
    }

    public Number getNumber() {
        return this.number;
    }

    /**
     * public Double getDouble() {
     * return new Double(this.number.doubleValue());
     * }
     * public Integer getInteger() {
     * return new Integer(this.number.intValue());
     * }
     */

    public Object getObject() {
        return this.number;
    }

    public Numeric getNumeric() {
        return new Numeric(this.number);
    }

    public ColorType getColorType() {
        // Convert numeric value to color ???
        // Convert to hexadecimal and then try to make it into a color?
        return new ColorType((float)0.0, (float)0.0, (float)0.0);
    }

}
