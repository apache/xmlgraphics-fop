/*
 * $Id$
 *
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
