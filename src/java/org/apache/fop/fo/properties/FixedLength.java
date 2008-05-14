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

import org.apache.fop.datatypes.PercentBaseContext;

/**
 * An absolute length quantity in XSL
 */
public final class FixedLength extends LengthProperty {
    
    /** Describes the unit pica. */
    public static final String PICA = "pc";

    /** Describes the unit point. */
    public static final String POINT = "pt";

    /** Describes the unit millimeter. */
    public static final String MM = "mm";

    /** Describes the unit centimeter. */
    public static final String CM = "cm";

    /** Describes the unit inch. */
    public static final String INCH = "in";

    /** Describes the unit millipoint. */
    public static final String MPT = "mpt";

    /** cache holding all canonical FixedLength instances */
    private static final PropertyCache cache = new PropertyCache(FixedLength.class);
    
    /** canonical zero-length instance */
    public static final FixedLength ZERO_FIXED_LENGTH = new FixedLength(0, FixedLength.MPT, 1.0f);
    
    private int millipoints;

    /**
     * Set the length given a number of units, a unit name and
     * an assumed resolution (used in case the units are pixels)
     * 
     * @param numUnits  quantity of input units
     * @param units     input unit specifier
     * @param res       input/source resolution
     */
    private FixedLength(double numUnits, String units, float res) {
        this.millipoints = convert(numUnits, units, res);
    }
    
    /**
     * Return the cached {@link FixedLength} instance corresponding
     * to the computed value in base-units (millipoints).
     * 
     * @param numUnits  quantity of input units
     * @param units     input unit specifier
     * @param sourceResolution input/source resolution (= ratio of pixels per pt)
     * @return  the canonical FixedLength instance corresponding
     *          to the given number of units and unit specifier
     *          in the given resolution
     */
    public static FixedLength getInstance(double numUnits, 
                                          String units,
                                          float sourceResolution) {
        if (numUnits == 0.0) {
            return ZERO_FIXED_LENGTH;
        } else {
            return (FixedLength)cache.fetch(
                new FixedLength(numUnits, units, sourceResolution));
        }
        
    }
    
    /**
     * Return the cached {@link FixedLength} instance corresponding
     * to the computed value
     * This method assumes a source-resolution of 1 (1px = 1pt)
     * 
     * @param numUnits  input units
     * @param units     unit specifier
     * @return  the canonical FixedLength instance corresponding
     *          to the given number of units and unit specifier
     */
    public static FixedLength getInstance(double numUnits, 
                                          String units) {
        return getInstance(numUnits, units, 1.0f);
        
    }
    
    /**
     * Return the cached {@link FixedLength} instance corresponding
     * to the computed value.
     * This method assumes 'millipoints' (non-standard) as units, 
     * and an implied source-resolution of 1 (1px = 1pt).
     * 
     * @param numUnits  input units
     * @return  the canonical FixedLength instance corresponding
     *          to the given number of units and unit specifier
     */
    public static FixedLength getInstance(double numUnits) {
        return getInstance(numUnits, FixedLength.MPT, 1.0f);
        
    }
    
    /**
     * Convert the given length to a dimensionless integer representing
     * a whole number of base units (milli-points).
     * 
     * @param dvalue quantity of input units
     * @param unit input unit specifier (in, cm, etc.)
     * @param res   the input/source resolution (in case the unit spec is "px")
     */
    private static int convert(double dvalue, String unit, float res) {
        // TODO: Maybe this method has a better place in org.apache.fop.util.UnitConv?.

        if ("px".equals(unit)) {
            //device-dependent units, take the resolution into account
            dvalue *= (res * 1000);
        } else {
            if (FixedLength.INCH.equals(unit)) {
                dvalue *= 72000;
            } else if (FixedLength.CM.equals(unit)) {
                dvalue *= 28346.4567;
            } else if (FixedLength.MM.equals(unit)) {
                dvalue *= 2834.64567;
            } else if (FixedLength.POINT.equals(unit)) {
                dvalue *= 1000;
            } else if (FixedLength.PICA.equals(unit)) {
                dvalue *= 12000;
            } else if (!FixedLength.MPT.equals(unit)) {
                dvalue = 0;
                log.error("Unknown length unit '" + unit + "'");
            }
        }
        return (int)dvalue;
    }

    /** {@inheritDoc} */
    public int getValue() {
        return millipoints;
    }

    /** {@inheritDoc} */
    public int getValue(PercentBaseContext context) {
        return millipoints;
    }

    /** {@inheritDoc} */
    public double getNumericValue() {
        return millipoints;
    }

    /** {@inheritDoc} */
    public double getNumericValue(PercentBaseContext context) {
        return millipoints;
    }

    /**
     * Return true since a FixedLength is always absolute.
     * {@inheritDoc}
     */
    public boolean isAbsolute() {
        return true;
    }

    /** {@inheritDoc} */
    public String toString() {
        return millipoints + FixedLength.MPT;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof FixedLength) {
            return (((FixedLength)obj).millipoints == this.millipoints);
        }
        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        return millipoints;
    }
}

