
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyConsts;

/*
 * Ems.java
 * $Id$
 * Created: Wed Nov 21 15:39:30 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/**
 * Constructor class for relative lengths measured in <i>ems</i>.  Constructs
 * a <tt>Numeric</tt>.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Ems {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Private constructor - don't instantiate a <i>Ems</i> object.
     */
    private Ems() {}

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is millipoints.
     * @param node - the <tt>FONode</tt> with reference to which this
     * <i>EM</i> value is being consructed.  A null value imples the
     * construction of an <i>initial value</i>.
     * @param property the index of the property with which this value
     * is associated.
     * @param value the number of units.
     * @return a <tt>Numeric</tt> representing this <i>Ems</i>.
     */
    public static Numeric makeEms(FONode node, int property, double value)
        throws PropertyException
    {
        Numeric numeric = new Numeric(property, value, Numeric.EMS, 0, 0);
        if (node == null)
            numeric.expandEms((Numeric)
            (PropertyConsts.pconsts.getInitialValue(PropNames.FONT_SIZE)));
        else
            numeric.expandEms(node.currentFontSize());
        return numeric;
    }

    /**
     * Construct a <tt>Numeric</tt> with a given unit and quantity.
     * The unit power is assumed as 1.  The base unit is millipoints.
     * @param node - the <tt>FONode</tt> with reference to which this
     * <i>EM</i> value is being consructed.  A null value imples the
     * construction of an <i>initial value</i>.
     * @param property the index of the property with which this value
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param value the number of units.
     * @return a <tt>Numeric</tt> representing this <i>Ems</i>.
     */
    public static Numeric makeEms
                            (FONode node, String propertyName, double value)
        throws PropertyException
    {
        return makeEms(node, PropNames.getPropertyIndex(propertyName), value);
    }

}
