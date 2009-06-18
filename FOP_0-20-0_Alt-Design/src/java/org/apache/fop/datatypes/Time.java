/*
 * Time.java
 * $Id$
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;


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
        super(property, PropertyValue.TIME);
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
        super(propertyName, PropertyValue.TIME);
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
     * @param unit type of unit as per constants defined in this class
     * @return <tt>double</tt> time value
     */
    public double getTime(int unit) {
        return time / msPerUnit[unit];
    }

    /**
     * @param unit  type of unit as per constants defined in this class
     * @param value  time in specified units
     */
    public void setTime(int unit, double value) {
        units = unit;
        time = value * msPerUnit[unit];
    }

    /**
     * @param time  in millisecs
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
        super.validate(Property.TIME);
    }

    public String toString() {
        return "" + time + getUnitName(units) + "\n" + super.toString();
    }

}
