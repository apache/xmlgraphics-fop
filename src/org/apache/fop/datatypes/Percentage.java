
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/*
 * Percentage.java
 *
 *
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
