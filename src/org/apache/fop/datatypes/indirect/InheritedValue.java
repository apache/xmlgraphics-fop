/*
 * $Id$
 *
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
 * @version $Revision$ $Name$
 */
package org.apache.fop.datatypes.indirect;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * A class representing an unresolved inherited value.  It may be created as
 * in the process of resolving "normal" default inheritance, when no value is
 * specified for an inheritable property, or it may be created in the process
 * of resolving a call to the core function
 * <tt>inherited-property-value()</tt>.  In both cases, it will only be
 * necessary when the inherited property cannot otherwise be resolved into a
 * <tt>PropertyValue<tt> immediately.
 * <p>Strictly speaking, a distinction should be made between these two
 * cases, because the latter may derive from a property other than the
 * target property whose value ist is resolving.  This is never true of
 * default inheritance.
 * <p><tt>InheritedValue</tt> differs from <tt>Inherit</tt> in that it only
 * applies to properties which support default inheritance, and there is at
 * least one case - that of <i>line-height</i> defined as a &lt;number&gt; -
 * in which the specified value is inherited.
 */

public class InheritedValue extends IndirectValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param sourceProperty the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    public InheritedValue(int property, int sourceProperty)
        throws PropertyException
    {
        super(property, PropertyValue.INHERIT, sourceProperty);
        if ( ! propertyConsts.isInherited(sourceProperty))
            throw new PropertyException
                    ("Non-inherited property "
                     + PropNames.getPropertyName(sourceProperty));
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public InheritedValue(int property)
        throws PropertyException
    {
        this(property, property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param sourcePropertyName the <tt>String</tt> name of the property
     * from which the inherited value is derived.
     * @exception PropertyException
     */
    public InheritedValue(String propertyName, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.INHERIT, sourcePropertyName);
        if ( ! propertyConsts.isInherited(
                    PropNames.getPropertyIndex(sourcePropertyName)))
            throw new PropertyException
                    ("Non-inherited property " + sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public InheritedValue(String propertyName)
        throws PropertyException
    {
        this(propertyName, propertyName);
    }

    /**
     * validate the <i>InheritedValue</i> against the associated property.
     * TODO: validate is a total mess.  It will all require a rethink
     * when the expression parsing is being finalised.
     * @param type - an <tt>int</tt> bitmap of datatypes.  Irrelevant here.
     */
    public void validate(int type) throws PropertyException {
        String propStr = "Unknown";
        String spropStr = "Unknown";
        // Property must be inheritable
        if (! propertyConsts.isInherited(sourceProperty)) {
            try {
                propStr = PropNames.getPropertyName(property);
                spropStr = PropNames.getPropertyName(sourceProperty);
            } catch (PropertyException e) {}
            throw new PropertyException
                    ("Source property " + sourceProperty + " (" + spropStr
                     + ") for " + this.property + " (" + propStr
                     + ") is not inheritable.");
        }
    }

    /**
     * validate the <i>InheritedValue</i> against the <i>source</i> property.
     */
    public void validate() throws PropertyException {
        validate(Property.ANY_TYPE);
    }

}
