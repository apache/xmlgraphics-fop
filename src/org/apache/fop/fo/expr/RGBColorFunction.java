/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;


import org.apache.fop.fo.Property;
import org.apache.fop.fo.ColorTypeProperty;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.PercentBase;

class RGBColorFunction extends FunctionBase {
    public int nbArgs() {
        return 3;
    }

    /**
     * Return an object which implements the PercentBase interface.
     * Percents in arguments to this function are interpreted relative
     * to 255.
     */
    public PercentBase getPercentBase() {
        return new RGBPercentBase();
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        // Using CSS rules, numbers are either supposed to be 0-255
        // or percentage values. If percentages value, they've already
        // been converted to reals.
        float[] cfvals = new float[3];    // RGB
        for (int i = 0; i < 3; i++) {
            Number cval = args[i].getNumber();
            if (cval == null) {
                throw new PropertyException("Argument to rgb() must be a Number");
            }
            float colorVal = cval.floatValue() / 255f;
            if (colorVal < 0.0 || colorVal > 255.0) {
                throw new PropertyException("Arguments to rgb() must normalize to the range 0 to 1");
            }
            cfvals[i] = colorVal;
        }
        return new ColorTypeProperty(new ColorType(cfvals[0], cfvals[1],
                                                   cfvals[2]));

    }

    static class RGBPercentBase implements PercentBase {
        public int getDimension() {
            return 0;
        }

        public double getBaseValue() {
            return 255f;
        }

        public int getBaseLength() {
            return 0;
        }

    }
}
