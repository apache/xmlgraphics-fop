/*
 * Frequency.java
 *
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;


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
        super(property, PropertyValue.FREQUENCY);
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
        super(propertyName, PropertyValue.FREQUENCY);
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
     * @param unit type of unit as per constants defined in this class
     * @return <tt>double</tt> frequency value
     */
    public double getFrequency(int unit) {
        return frequency / hzPerUnit[unit];
    }

    /**
     * @param unit type of unit as per constants defined in this class
     * @param value  frequency in specified units
     */
    public void setFrequency(int unit, double value) {
        units = unit;
        frequency = value * hzPerUnit[unit];
    }

    /**
     * @param frequency  in hertz
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
        super.validate(Property.FREQUENCY);
    }

    public String toString() {
        return "" + frequency + getUnitName(units) + "\n" + super.toString();
    }

}
