package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;

/*
 * Time.java
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Time extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /*
     * Constant for Unit name
     */
    public static final int
        NOUNIT = 0
         ,MSEC = 1
          ,SEC = 2
            ;

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

    private double time = 0.0;
    private int units = 0;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Time(int property, int unit, double value)
        throws PropertyException
    {
        super(property);
        units = unit;
        time = value * msPerUnit[unit];
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Time(String propertyName, int unit, double value)
        throws PropertyException
    {
        super(propertyName);
        units = unit;
        time = value * msPerUnit[unit];
    }

    /**
     * @return <tt>double</tt> time in millisecs
     */
    public double getTime() {
        return time;
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @return <tt>double</tt> time value
     */
    public double getTime(int unit) {
        return time / msPerUnit[unit];
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @param <tt>double</tt> time in specified units
     */
    public void setTime(int unit, double value) {
        units = unit;
        time = value * msPerUnit[unit];
    }

    /**
     * @param <tt>double</tt> time in millisecs
     */
    public void setTime(double time) {
        units = MSEC;
        this.time = time;
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

    /**
     * validate the <i>Time</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.TIME);
    }

    public String toString() {
        return "" + time + getUnitName(units) + "\n" + super.toString();
    }

}
