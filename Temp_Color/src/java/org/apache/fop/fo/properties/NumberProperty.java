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

package org.apache.fop.fo.properties;

import java.awt.Color;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Class for handling numeric properties
 */
public final class NumberProperty extends Property implements Numeric {

    /**
     * Inner class for making NumberProperty objects
     */
    public static class Maker extends PropertyMaker {

        /**
         * Constructor for NumberProperty.Maker
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * {@inheritDoc}
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo)
                    throws PropertyException {
            if (p instanceof NumberProperty) {
                return p;
            }
            if (p instanceof EnumProperty) {
                return EnumNumber.getInstance(p);
            }
            Number val = p.getNumber();
            if (val != null) {
                return getInstance(val.doubleValue());
            }
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    /**
     * A positive integer property maker.
     */
    public static class PositiveIntegerMaker extends PropertyMaker {

        /**
         * Constructor for NumberProperty.PositiveIntegerMaker
         * @param propId the id of the property for which a PositiveIntegerMaker should be created
         */
        public PositiveIntegerMaker(int propId) {
            super(propId);
        }

        /**
         * If the value is not positive, return a property with value 1
         *
         * {@inheritDoc}
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo)
                    throws PropertyException {
            if (p instanceof EnumProperty) {
                return EnumNumber.getInstance(p);
            }
            Number val = p.getNumber();
            if (val != null) {
                int i = Math.round(val.floatValue());
                if (i <= 0) {
                    i = 1;
                }
                return getInstance(i);
            }
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    /** cache holding all canonical NumberProperty instances */
    private static final PropertyCache CACHE
        = new PropertyCache(NumberProperty.class);

    private final Number number;

    /**
     * Constructor for double input
     * @param num double numeric value for property
     */
    private NumberProperty(double num) {
        //Store the number as an int or a long,
        //if possible
        if (num == Math.floor(num)) {
            if (num < Integer.MAX_VALUE) {
                this.number = new Integer((int)num);
            } else {
                this.number = new Long((long)num);
            }
        } else {
            this.number = new Double(num);
        }
    }

    /**
     * Constructor for integer input
     * @param num integer numeric value for property
     */
    private NumberProperty(int num) {
        this.number = new Integer(num);
    }

    /**
     * Returns the canonical NumberProperty instance
     * corresponding to the given Number
     * @param num   the base Double
     * @return  the canonical NumberProperty
     */
    public static NumberProperty getInstance(Double num) {
        return (NumberProperty)CACHE.fetch(
                    new NumberProperty(num.doubleValue()));
    }

    /**
     * Returns the canonical NumberProperty instance
     * corresponding to the given Integer
     * @param num   the base Integer
     * @return  the canonical NumberProperty
     */
    public static NumberProperty getInstance(Integer num) {
        return (NumberProperty)CACHE.fetch(
                    new NumberProperty(num.intValue()));
    }

    /**
     * Returns the canonical NumberProperty instance
     * corresponding to the given double
     * @param num   the base double value
     * @return  the canonical NumberProperty
     */
    public static NumberProperty getInstance(double num) {
        return (NumberProperty)CACHE.fetch(
                    new NumberProperty(num));
    }

    /**
     * Returns the canonical NumberProperty instance
     * corresponding to the given int
     * @param num   the base int value
     * @return  the canonical NumberProperty
     */
    public static NumberProperty getInstance(int num) {
        return (NumberProperty)CACHE.fetch(
                    new NumberProperty(num));
    }

    /**
     * Plain number always has a dimension of 0.
     * @return a dimension of 0.
     */
    public int getDimension() {
        return 0;
    }

    /**
     * Return the value of this Numeric.
     * @return The value as a double.
     */
    public double getNumericValue() {
        return number.doubleValue();
    }

    /**
     * Return the value of this Numeric.
     * @param context Evaluation context
     * @return The value as a double.
     */
    public double getNumericValue(PercentBaseContext context) {
        return getNumericValue();
    }

    /** {@inheritDoc} */
    public int getValue() {
        return number.intValue();
    }

    /**
     * Return the value
     * @param context Evaluation context
     * @return The value as an int.
     */
    public int getValue(PercentBaseContext context) {
        return getValue();
    }

    /**
     * Return true because all numbers are absolute.
     * @return true.
     */
    public boolean isAbsolute() {
        return true;
    }

    /**
     * @return this.number cast as a Number
     */
    public Number getNumber() {
        return this.number;
    }

    /**
     * @return this.number cast as an Object
     */
    public Object getObject() {
        return this.number;
    }

    /**
     * Convert NumberProperty to Numeric object
     * @return Numeric object corresponding to this
     */
    public Numeric getNumeric() {
        return this;
    }

    /** {@inheritDoc} */
    public Length getLength() {
        //Assume pixels (like in HTML) when there's no unit
        return FixedLength.getInstance(getNumericValue(), "px");
    }

    /**
     * Convert NumberProperty to a Color. Not sure why this is needed.
     * @param foUserAgent FOUserAgent
     * @return Color that corresponds to black
     */
    public Color getColor(FOUserAgent foUserAgent) {
        // TODO: Implement somehow
        // Convert numeric value to color ???
        // Convert to hexadecimal and then try to make it into a color?
        return Color.black;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return number.hashCode();
    }

    /** {@inheritDoc} */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NumberProperty) {
            NumberProperty np = (NumberProperty) o;
            return (np.number == this.number
                    || (this.number != null
                        && this.number.equals(np.number)));
        }
        return false;
    }
}
