/*
 * Ems.java
 * $Id$
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
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;

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
