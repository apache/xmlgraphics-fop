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

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.Property;

/**
 * A numeric property which hold the final absolute result of an expression
 * calculations.  
 */
public class NumericProperty extends Property implements Numeric, Length {
    private double value;
    private int dim;

    /**
     * Construct a Numeric object by specifying one or more components,
     * including absolute length, percent length, table units.
     * @param valType A combination of bits representing the value types.
     * @param value The value of the numeric.
     * @param dim The dimension of the value. 0 for a Number, 1 for a Length
     * (any type), >1, <0 if Lengths have been multiplied or divided.
     */
    protected NumericProperty(double value, int dim) {
        this.value = value;
        this.dim = dim;
    }

    /**
     * Return the dimension.
     * @see Numeric#getDimension()
     */
    public int getDimension() {
        return dim;
    }

    /**
     * Return the value.
     * @see Numeric#getNumericValue()
     */
    public double getNumericValue() {
        return value;
    }

    /**
     * Return true of the numeric is absolute.
     * @see Numeric#isAbsolute()
     */
    public boolean isAbsolute() {
        return true;
    }

    /**
     * Cast this as a Numeric.
     */
    public Numeric getNumeric() {
        return this;
    }

    /**
     * Cast this as a number.
     */
    public Number getNumber() {
        return new Double(value);
    }

    /**
     * Return the value of this numeric as a length in millipoints. 
     */
    public int getValue() {
        return (int) value;
    }

    /**
     * Cast this as a length. That is only possible when the dimension is 
     * one.
     */
    public Length getLength() {
        if (dim == 1) {
            return this;
        }
        log.error("Can't create length with dimension " + dim);
        return null;
    }

    /**
     * Cast this as a ColorTypeProperty.
     */
    public ColorTypeProperty getColorType() {
        // try converting to numeric number and then to color
        return null;
    }

    /**
     * Cast this as an Object.
     */
    public Object getObject() {
        return this;
    }

    /**
     * Return a string representation of this Numeric. It is only useable for
     * debugging.
     */
    public String toString() {
        if (dim == 1) {
            return (int) value + "mpt";
        } else {
            return value + "^" + dim;
        }
    }
}
