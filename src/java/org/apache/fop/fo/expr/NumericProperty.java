/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.Color;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.util.CompareUtil;

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
     * {@inheritDoc}
     */
    public int getDimension() {
        return dim;
    }

    /**
     * Return the value.
     * {@inheritDoc}
     */
    public double getNumericValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public double getNumericValue(PercentBaseContext context) {
        return value;
    }

    /**
     * Return true of the numeric is absolute.
     * {@inheritDoc}
     */
    public boolean isAbsolute() {
        return true;
    }

    /** {@inheritDoc} */
    public Numeric getNumeric() {
        return this;
    }

    /** {@inheritDoc} */
    public Number getNumber() {
        return new Double(value);
    }

    /** {@inheritDoc} */
    public int getValue() {
        return (int) value;
    }

    /** {@inheritDoc} */
    public int getValue(PercentBaseContext context) {
        return (int) value;
    }

    /** {@inheritDoc} */
    public Length getLength() {
        if (dim == 1) {
            return this;
        }
        log.error("Can't create length with dimension " + dim);
        return null;
    }

    /** {@inheritDoc} */
    public Color getColor(FOUserAgent foUserAgent) {
        // TODO:  try converting to numeric number and then to color
        return null;
    }

    /** {@inheritDoc} */
    public Object getObject() {
        return this;
    }

    /** {@inheritDoc} */
    public String toString() {
        if (dim == 1) {
            return (int) value + FixedLength.MPT;
        } else {
            return value + "^" + dim;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dim;
        result = prime * result + CompareUtil.getHashCode(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NumericProperty)) {
            return false;
        }
        NumericProperty other = (NumericProperty) obj;
        return dim == other.dim && CompareUtil.equal(value, other.value);
    }
}
