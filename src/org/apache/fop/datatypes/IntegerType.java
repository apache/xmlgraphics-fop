
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/*
 * IntegerType.java
 *
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Constructor for Integer datatype.  Constructs a <tt>Numeric</tt>.
 */

public class IntegerType {

    /**
     * Construct a <tt>Numeric</tt> with a given quantity.
     * The unit power is assumed as 0.  The base unit is NUMERIC.
     * @param property the index of the property with which this value
     * is associated.
     * @param value the integer value.
     * @return a <tt>Numeric</tt> representing this <i>IntegerType</i>.
     */
    public static Numeric makeInteger(int property, long value)
        throws PropertyException
    {
        return new Numeric(property, (double)value, Numeric.NUMBER, 0, 0);
    }

    /**
     * Construct a numeric with a given unit and quantity.
     * The unit power is assumed as 0.  The base unit is NUMERIC.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the integer value.
     * @return a <tt>Numeric</tt> representing this <i>IntegerType</i>.
     */
    public static Numeric makeInteger(String propertyName, long value)
        throws PropertyException
    {
        return new Numeric(propertyName, (double)value, Numeric.NUMBER, 0, 0);
    }

}
