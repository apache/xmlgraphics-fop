/*
 * Percentage.java
 *
 *
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
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/**
 * Constructor class for Percentage datatype.  Constructs a <tt>Numeric</tt>.
 */

public class Percentage {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Private constructor - don't instantiate a <i>Percentage</i> object.
     */
    private Percentage() {}

    /**
     * Construct a <tt>Numeric</tt> with a given quantity.
     * The unit power is assumed as 0.  The base unit is PERCENTAGE.
     * @param property the index of the property with which this value
     * is associated.
     * @param percentage.  This value will be normalized.
     * @return a <tt>Numeric</tt> representing this <i>Percentage</i>.
     */
    public static Numeric makePercentage(int property, double percentage)
        throws PropertyException
    {
        return new Numeric(property, percentage / 100.0,
                           Numeric.PERCENTAGE, 0, 0);
    }

    /**
     * Construct a <tt>Numeric</tt> with a given quantity.
     * The unit power is assumed as 0.  The base unit is PERCENTAGE.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param percentage.  This value will be normalized.
     * @return a <tt>Numeric</tt> representing this <i>Percentage</i>.
     */
    public static Numeric makePercentage
        (String propertyName, double percentage)
        throws PropertyException
    {
        return new Numeric(propertyName, percentage / 100.0,
                           Numeric.PERCENTAGE, 0, 0);
    }

}
