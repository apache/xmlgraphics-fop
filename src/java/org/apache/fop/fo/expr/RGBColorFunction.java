/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.fo.expr;


import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.Property;
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
                throw new PropertyException(
                        "Arguments to rgb() must normalize to the range 0 to 1");
            }
            cfvals[i] = colorVal;
        }
        return new ColorTypeProperty(cfvals[0], cfvals[1], cfvals[2]);

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
