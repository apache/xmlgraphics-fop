package org.apache.fop.datatypes.indirect;

import org.apache.fop.datatypes.indirect.IndirectValue;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.FONode;

/*
 * $Id$
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 */
/**
 * A pseudo-class to represent a call to the core function
 * from-nearest-specified-value().
 * Used <i>only</i> in the cases where the property
 * assigned to is identical to the <tt>NCName</tt> argument, and this is a
 * shorthand.
 * <p>Further, the function call must be the only component of the expression
 * in which it occurs.  (See Rec. Section 5.10.4 Property Value Functions.)
 * In these circumstances, the function call resolves to a
 * from-nearest-specified-value() function call on each of the properties to
 * which the shorthand resolves.
 * <p>The use of the pseudo-type should ensure that the function call is not
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
 * <p>This mechanism ensures, without greatly complicating the parser,
 * that the constraint on the from-nearest-specified-value() function, with
 * respect to shorthands, is met.
 * <p>This pseudo-datatype is also used as the first stage of shorthand
 * expansion.  After a shorthand's expression is parsed, the next stage of
 * resolution will generate a FromNearestSpecified object for each property
 * in the expansion of the shorthand.
 * <p>Once created, this class acts as an <tt>IndirectValue</tt> in the
 * event that it cannot immediately be resolved.  This association exists
 * simply to save creating another object. 
 *
 * @see FromParent
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FromNearestSpecified extends IndirectValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromNearestSpecified(int property)
        throws PropertyException
    {
        super(property, PropertyValue.FROM_NEAREST_SPECIFIED);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.  In this case, a shorthand property.
     * @exception PropertyException
     */
    public FromNearestSpecified(String propertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.FROM_NEAREST_SPECIFIED);
    }

    /**
     * Attempt to resolve this object into a "real" property value.  If the
     * object has no <i>inheritedValue</i>, obtain and set one.  The
     * obtained value is from the nearest ancestor node on which a value
     * has been specified.
     * Then invoke the superclass' <i>resolve()</i> method.
     * @param node - the <tt>FONode</tt> with which this object is associated.
     * @return the resulting <tt>PropertyValue</tt>.  Either a resolved value
     * or <i>this</i>, if bequeathing value has no resolved computed value.
     */
    public PropertyValue resolve(FONode node) throws PropertyException {
        if (inheritedValue == null) {
            inheritedValue = node.getNearestSpecifiedValue(sourceProperty);
        }
        return super.resolve(node);
    }

    /**
     * validate the <i>FromNearestSpecified</i> against the associated
     * property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.SHORTHAND);
    }

}
