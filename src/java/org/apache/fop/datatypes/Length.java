/*
 * Length.java
 * $Id: Length.java,v 1.12.2.5 2003/03/31 02:32:52 pbwest Exp $
 * Created: Wed Nov 21 15:39:30 2001
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
 * @version $Revision: 1.12.2.5 $ $Name:  $
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/**
 * A constructor class for Length datatype.  Constructs a <tt>Numeric</tt>.
 */

public class Length {

    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.12.2.5 $";

    /*
     * Constants for LengthUnitNames
     */
    public static final int
         NOUNIT = 0
            ,CM = 1
            ,MM = 2
            ,IN = 3
            ,PT = 4
            ,PC = 5
            ,PX = 6
           ,MPT = 7  // Millipoints
                ;

    /*
     * Constants for conversions into millipoints
     */
    public static final double
          PTSPERINCH = 72.0
         ,PTSPERPICA = 12.0
         ,CMSPERINCH = 2.54
         ,MMSPERINCH = 25.4
           ,PTSPERCM = PTSPERINCH / CMSPERINCH
           ,PTSPERMM = PTSPERINCH / MMSPERINCH
         ,PXSPERINCH = 92.0
           ,PTSPERPX = PTSPERINCH / PXSPERINCH
           ,PXSPERPT = PXSPERINCH / PTSPERINCH
         ,PXSPERPICA = PTSPERPICA * PXSPERPT
           ,PXSPERCM = PXSPERINCH / CMSPERINCH
           ,PXSPERMM = PXSPERINCH / MMSPERINCH
                   ;

    /**
     * Array of constant conversion factors from unit to millipoints,
     * indexed by integer unit constant.  Keep this array in sync with
     * the integer constants or bear the consequences.
     */
    public static final double[] milliPtsPerUnit = {
        0.0
        ,PTSPERCM * 1000.0
        ,PTSPERMM * 1000.0
        ,PTSPERINCH * 1000.0
        ,1000.0
        ,PTSPERPICA * 1000.0
        ,PTSPERPX * 1000.0
        ,1.0
    };

    /**
     * Private constructor - don't instantiate a <i>Length</i> object.
     */
    private Length() {}

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is millipoints.
     * @param property the index of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit
     * @return a <tt>Numeric</tt> representing this <i>Length</i>.
     */
    public static Numeric makeLength(int property, double value, int unit)
        throws PropertyException
    {
        return new Numeric(property, value * milliPtsPerUnit[unit],
                           Numeric.MILLIPOINTS, 1, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is millipoints.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the number of units.
     * @param unit an integer constant representing the unit.
     * @return a <tt>Numeric</tt> representing this <i>Length</i>.
     */
    public static Numeric makeLength
        (String propertyName, double value, int unit)
        throws PropertyException
    {
        return new Numeric(propertyName, value * milliPtsPerUnit[unit],
                           Numeric.MILLIPOINTS, 1, unit);
    }

    /**
     * @param unit an <tt>int</tt> encoding a <i>Length</i> unit.
     * @return the <tt>String</tt> name of the unit.
     */
    public static String getUnitName(int unit) {
        switch (unit) {
        case CM:
            return "cm";
        case MM:
            return "mm";
        case IN:
            return "in";
        case PT:
            return "pt";
        case PC:
            return "pc";
        case PX:
            return "px";
        case MPT:
            return "millipt";
        default:
            return "";
        }
    }

}
