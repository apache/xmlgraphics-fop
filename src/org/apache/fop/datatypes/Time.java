
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
//import org.apache.fop.fo.Properties;
//import org.apache.fop.fo.PropertyConsts;
//import org.apache.fop.fo.PropNames;

/*
 * Time.java
 * $Id$
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Time is a front for the manufacture of <tt>Numeric</tt> objects.
 * Because Numerics are so malleable, it makes no sense to tie
 * a Numeric object to any particular type by subclassing <tt>Numeric</tt>.
 * Instead, each of the Numeric types provides static methods to generate
 * a Numeric representing, as originally created, a particular type of
 * number or measure.  The constructor for this class is <tt>private</tt>.
 */

public class Time {
    /*
     * Constants for UnitNames
     */
    public static final int NOUNIT = 0;
    public static final int MSEC = 1;
    public static final int SEC = 2;

    /**
     * Array of constant conversion factors from unit to milliseconds,
     * indexed by integer unit constant.  Keep this array in sync with
     * the integer constants or bear the consequences.
     */
    public static final double[] msPerUnit = {
        0.0
        ,1.0
        ,1000.0
    };

    /**
     * Private constructor - don't instantiate a <i>Time</i> object.
     */
    private Time() {}

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is milliseconds.
     * @param property the index of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit
     * @return a <tt>Numeric</tt> representing this <i>Time</i>.
     */
    public static Numeric makeTime(int property, double value, int unit)
        throws PropertyException
    {
        return new Numeric(property, value * msPerUnit[unit],
                           Numeric.MILLISECS, 1, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is milliseconds.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit
     * @return a <tt>Numeric</tt> representing this <i>Time</i>.
     */
    public static Numeric makeTime(String propertyName, double value, int unit)
        throws PropertyException
    {
        return new Numeric(propertyName, value * msPerUnit[unit],
                           Numeric.MILLISECS, 1, unit);
    }

    /**
     * @param unit an <tt>int</tt> encoding a <i>Time</i> unit.
     * @return the <tt>String</tt> name of the unit.
     */
    public static String getUnitName(int unit) {
        switch (unit) {
        case MSEC:
            return "ms";
        case SEC:
            return "s";
        default:
            return "";
        }
    }

}
