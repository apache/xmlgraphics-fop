/*
 * Length.java
 * $Id: Length.java,v 1.12.2.5 2003/03/31 02:32:52 pbwest Exp $
 * Created: Wed Nov 21 15:39:30 2001
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
