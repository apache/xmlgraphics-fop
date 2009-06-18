/*
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
 */
package org.apache.fop.datatypes.indirect;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * A pseudo-class to represent a call to the core function from-parent().
 * Used <i>only</i> in the cases where the property assigned to
 * is identical to the <tt>NCName</tt> argument, and this is a shorthand.
 * <p>Further, the function call must be the only component of the expression
 * in which it occurs.  (See Rec. Section 5.10.4 Property Value Functions.)
 * In these circumstances, the function call resolves to a
 * from-parent() function call on each of the properties to
 * which the shorthand resolves.
 * <p>The use of the pseudo-type should ensure that the function call is not
 * involved in any arithmetic components of a more complex expression.  I.e,
 * the function evaluator in the parser must check to see whether the
 * property for which the from-parent() function is being
 * evaluated is a shorthand.  If not, the function is normally evaluated.
 * If so, the parser must further check that the property assigned to (i.e.
 * the property against which this function is being evaluated) is the same
 * as the <tt>NCName</tt> argument.  If not, it is an error.  If so, the
 * property evaluates to an instance of this class.  The value must itself
 * be later resolved before the property value can be utilised in the fo
 * node, but, in the meantime, any attempt to involve the function call in
 * any more complex expression will throw an exception.
 * <p>This mechanism ensures, without greatly complicating the parser,
 * that the constraint on the from-parent() function, with
 * respect to shorthands, is met.
 * <p>This pseudo-datatype is also used as the first stage of shorthand
 * expansion.  After a shorthand's expression is parsed, the next stage of
 * resolution will generate a FromParent object for each property
 * in the expansion of the shorthand.
 * <p>Once created, this class acts as an <tt>IndirectValue</tt> in the
 * event that it cannot immediately be resolved.  This association exists
 * simply to save creating another object. 
 *
 * @see FromNearestSpecified
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FromParent extends IndirectValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromParent(int property)
        throws PropertyException
    {
        super(property, PropertyValue.FROM_PARENT);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromParent(String propertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.FROM_PARENT);
    }

    /**
     * validate the <i>FromParent</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.SHORTHAND);
    }

}
