package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;

/*
 * Frequency.java
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

public class Frequency extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /*
     * Constant for Unit names
     */
    public static final int
        NOUNIT = 0
           ,HZ = 1
          ,KHZ = 2
            ;

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

    private double frequency = 0.0;
    private int units = 0;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Frequency(int property, int unit, double value)
        throws PropertyException
    {
        super(property);
        units = unit;
        frequency = value * hzPerUnit[unit];
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param unit the <tt>int</tt> unit name code, as defined in the
     * constants of this class.
     * @param value the <tt>double</tt> value.
     * @exception PropertyException
     */
    public Frequency(String propertyName, int unit, double value)
        throws PropertyException
    {
        super(propertyName);
        units = unit;
        frequency = value * hzPerUnit[unit];
    }

    /**
     * @return <tt>double</tt> frequency in hertz
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @return <tt>double</tt> frequency value
     */
    public double getFrequency(int unit) {
        return frequency / hzPerUnit[unit];
    }

    /**
     * @param <tt>int</tt> unit as per constants defined in this class
     * @param <tt>double</tt> frequency in specified units
     */
    public void setFrequency(int unit, double value) {
        units = unit;
        frequency = value * hzPerUnit[unit];
    }

    /**
     * @param <tt>double</tt> frequency in hertz
     */
    public void setFrequency(double frequency) {
        units = HZ;
        this.frequency = frequency;
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

    /**
     * validate the <i>Frequency</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.FREQUENCY);
    }

    public String toString() {
        return "" + frequency + getUnitName(units) + "\n" + super.toString();
    }

}
