package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.fo.properties.*;

/*
 * Angle.java
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

public class Angle extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /**
     * Constant for Unit name
     */
    public static final int
        NOUNIT = 0
          ,DEG = 1
         ,GRAD = 2
          ,RAD = 3
            ;

    /**
     * Array of constant conversion factors from unit to degrees,
     * indexed by integer unit constant.  Keep this array in sync with
     * the integer constants or bear the consequences.
     */
    public static final double[] degPerUnit = {
        0.0
        ,1.0
        ,57.29578	// Degrees per grade
        ,63.661977      // Degrees per radian
    };

    private double degrees = 0.0;
    private int units = 0;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Angle(int property, int unit, double value)
        throws PropertyException
    {
        super(property, PropertyValue.ANGLE);
        units = unit;
        degrees = value * degPerUnit[unit];
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Angle(String propertyName, int unit, double value)
        throws PropertyException
    {
        super(propertyName, PropertyValue.ANGLE);
        units = unit;
        degrees = value * degPerUnit[unit];
    }

    /**
     * @return <tt>double</tt> angle in degrees
     */
    public double getAngle() {
        return degrees;
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @return <tt>double</tt> degrees value
     */
    public double getAngle(int unit) {
        return degrees / degPerUnit[unit];
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @param <tt>double</tt> angle in specified units
     */
    public void setAngle(int unit, double value) {
        units = unit;
        degrees = value * degPerUnit[unit];
    }

    /**
     * @param <tt>double</tt> angle in degrees
     */
    public void setAngle(double degrees) {
        units = DEG;
        this.degrees = degrees;
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
     * validate the <i>Angle</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.ANGLE);
    }

    public String toString() {
        return "" + degrees + getUnitName(units) + "\n" + super.toString();
    }

}
