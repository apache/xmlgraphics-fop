package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;

/*
 * FromNearestSpecified.java
 * <br/>
 * $Id$
 * <br/>
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/**
 * <p>
 * A pseudo-class to represent a call to the core property value function
 * from-nearest-specified-value(), <i>only</i> in the cases where the property
 * assigned to is identical to the <tt>NCName</tt> argument, and this is a
 * shorthand.
 * <p>
 * Further, the function call must be the only component of the expression
 * in which it occurs.  (See Rec. Section 5.10.4 Property Value Functions.)
 * In these circumstances, the function call resolves to a
 * from-nearest-specified-value() function call on each of the properties to
 * which the shorthand resolves.
 * <p>
 * The use of the pseudo-type should ensure that the function call is not
 * involved in any arithmetic components of a more complex expression.  I.e,
 * the function evaluator in the parser must check to see whether the
 * property for which the from-nearest-specified-value() function is being
 * evaluated is a shorthand.  If not, the function is normally evaluated.
 * If so, the parser must further check that the property assigned to (i.e.
 * the property against which this function is being evaluated) is the same
 * as the <tt>NCName</tt> argument.  If not, it is an error.  If so, the
 * property evaluates to an instance of this class.  The value must itself
 * be later resolved before the property value can be utilised in the fo
 * node, but, in the meantime, any attempt to involve the function call in
 * any more complex expression will throw an exception.
 * <p>
 * This mechanism ensures, without greatly complicating the parser,
 * that the constraint on the from-nearest-specified-value() function, with
 * respect to shorthands, is met.
 * <p>
 * This pseudo-datatype is also used as the first stage of shorthand
 * expansion.  After a shorthand's expression is parsed, the next stage of
 * resolution will generate a FromNearestSpecified object for each property
 * in the expansion of the shorthand.
 *
 * @see FromParent
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FromNearestSpecified extends AbstractPropertyValue {

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromNearestSpecified(int property)
        throws PropertyException
    {
        super(property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromNearestSpecified(String propertyName)
        throws PropertyException
    {
        super(propertyName);
    }

    /**
     * validate the <i>FromNearestSpecified</i> against the associated
     * property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.SHORTHAND);
    }

}
