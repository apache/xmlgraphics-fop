/*
 * Angle.java
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 */
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.fo.properties.*;


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
