
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/*
 * Angle.java
 * $Id$
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/**
 * Constructor class for Angle datatype.  Constructs a <tt>Numeric</tt>.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Angle {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /*
     * Constants for UnitNames
     */
    public static final int NOUNIT = 0;
    public static final int DEG = 1;
    public static final int GRAD = 2;
    public static final int RAD = 3;

    /**
     * Array of constant conversion factors from unit to milliseconds,
     * indexed by integer unit constant.  Keep this array in sync with
     * the integer constants or bear the consequences.
     */
    public static final double[] degPerUnit = {
        0.0
        ,1.0
        ,57.29578	// Degrees per grade
        ,63.661977      // Degrees per radian
    };

    /**
     * Private constructor - don't instantiate a <i>Time</i> object.
     */
    private Angle() {}

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is
     * assumed as 1.  The base unit is degrees.
     * @param property <tt>int</tt> index of the property.
     * @param value the number of units.
     * @param unit an integer value representing the unit of definition.
     */
    public static Numeric makeAngle(int property, double value, int unit)
        throws PropertyException
    {
        return new Numeric(property, value * degPerUnit[unit],
                           Numeric.DEGREES, 1, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is
     * assumed as 1.  The base unit is degrees.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer value representing the unit of definition.
     */
    public static Numeric makeAngle
        (String propertyName, double value, int unit)
        throws PropertyException
    {
        return new Numeric(propertyName, value * degPerUnit[unit],
                           Numeric.DEGREES, 1, unit);
    }

    /**
     * @param unit an <tt>int</tt> encoding an <i>Angle</i>.
     * @return the <tt>String</tt> name of the unit in which this
     * <i>Numeric</i> was defined.
     */
    public static String getUnitName(int unit) {
        switch (unit) {
        case DEG:
            return "deg";
        case GRAD:
            return "grad";
        case RAD:
            return "rad";
        default:
            return "";
        }
    }

    /**
     * Normalize the angle value in the range 0 <= angle <= 360
     * @param numeric a <tt>Numeric</tt> representing the value to be
     * normalized.
     * @return a <tt>double</tt> containing the normalized value.
     */
    public static double normalize(Numeric numeric) {
        if (numeric.power == 1)
            return numeric.value % 360;  // Negative angles still negative
        return numeric.value;
    }

}
