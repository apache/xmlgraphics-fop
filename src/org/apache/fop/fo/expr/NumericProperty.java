/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.ColorType;

class NumericProperty extends Property {
    private Numeric numeric;

    NumericProperty(Numeric value) {
        this.numeric = value;
    }

    public Numeric getNumeric() {
        return this.numeric;
    }

    public Number getNumber() {
        return numeric.asNumber();
    }

    public Length getLength() {
        return numeric.asLength();
    }

    public ColorType getColorType() {
        // try converting to numeric number and then to color
        return null;
    }

    public Object getObject() {
        return this.numeric;
    }

}
