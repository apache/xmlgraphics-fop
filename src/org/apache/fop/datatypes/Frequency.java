
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/*
 * Frequency.java
 *
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/**
 * Constructor class for Frequency datatype.  Constructs a <tt>Numeric</tt>.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Frequency {

    /*
     * Constants for UnitNames
     */
    public static final int NOUNIT = 0;
    public static final int HZ = 1;
    public static final int KHZ = 2;

    /**
     * Array of constant conversion factors from unit to Hertz,
     * indexed by integer unit constant.  Keep this array in sync with
     * the integer constants or bear the consequences.
     */
    public static final double[] hzPerUnit = {
        0.0
        ,1.0
        ,1000.0
    };

    /**
     * Private constructor - don't instantiate a <i>Frequency</i> object.
     */
    private Frequency() {}

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is Hertz.
     * @param property the index of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit
     * @return a <tt>Numeric</tt> representing this <i>Frequency</i>.
     */
    public static Numeric makeFrequency(int property, double value, int unit)
        throws PropertyException
    {
        return new Numeric(property, value * hzPerUnit[unit],
                           Numeric.HERTZ, 1, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is Hertz.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit
     * @return a <tt>Numeric</tt> representing this <i>Frequency</i>.
     */
    public static Numeric makeFrequency
        (String propertyName, double value, int unit)
        throws PropertyException
    {
        return new Numeric(propertyName, value * hzPerUnit[unit],
                           Numeric.HERTZ, 1, unit);
    }

    /**
     * @param unit an <tt>int</tt> encoding a <i>Frequency</i> unit.
     * @return the <tt>String</tt> name of the unit in which this
     * <i>Numeric</i> was defined.
     */
    public static String getUnitName(int unit) {
        switch (unit) {
        case HZ:
            return "Hz";
        case KHZ:
            return "kHz";
        default:
            return "";
        }
    }

}
