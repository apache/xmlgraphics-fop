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

import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A superclass for objects which may have deferred <tt>PropertyValue</tt>
 * resolution.  This is because their value is taken from
 * another <tt>PropertyValue</tt> object defined earlier in the FO tree.
 * These include <tt>Inherit</tt>, <tt>FromParent</tt> and
 * <tt>FromNearestSpecified</tt> objects.  If an <tt>InheritedValue</tt>
 * object is defined, it will also extend this class.
 * <p>The required value is usually the computed value of the
 * <tt>PropertyValue</tt> of the source property on the source node.  This
 * property may be different from the property of this object.  This class
 * provides accessors for the referenced <tt>PropertyValue</tt>.
 * In some cases, the specified value is 
 * required.  It is the responsibility of the subclass to determine and
 * act upon these cases.  At the time of writing, the only such exception is
 * when a <i>line-height</i> is defined as a &lt;number&gt;.
 */

public class IndirectValue extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The property from which the inherited value is to be derived.  This
     * may be different from the target property.
     */
    protected int sourceProperty;

    /**
     * The <tt>PropertyValue</tt> from which this object is being
     * inherited.  Set when the inheritance cannot be immediately resolved,
     * e.g. when the specified value is a percentage.
     */
    protected PropertyValue inheritedValue = null;

    /**
     * @param property - the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @param sourceProperty - the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    protected IndirectValue(int property, int type, int sourceProperty)
        throws PropertyException
    {
        super(property, type);
        this.sourceProperty = sourceProperty;
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @exception PropertyException
     */
    protected IndirectValue(int property, int type)
        throws PropertyException
    {
        this(property, type, property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @param sourcePropertyName the <tt>String</tt> name of the property
     * from which the inherited value is derived.
     * @exception PropertyException
     */
    protected IndirectValue
                    (String propertyName, int type, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName, type);
        sourceProperty = PropNames.getPropertyIndex(sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @exception PropertyException
     */
    protected IndirectValue(String propertyName, int type)
        throws PropertyException
    {
        this(propertyName, type, propertyName);
    }

    /**
     * @return <tt>int</tt> containing the source property index.
     */
    public int getSourceProperty() {
        return sourceProperty;
    }

    /**
     * @return <tt>PropertyValue</tt> which contains or will contain the
     * the computed value being inherited.  This field will be null except
     * when an unresolved computed value is being inherited.  If so,
     * a null value will be returned.  N.B. This
     * <tt>PropertyValue</tt> may have a property field different from 
     * this <i>IndirectValue</i> object.  The source property field is held in
     * the <i>sourceProperty</i> field.
     */
    public PropertyValue getInheritedValue() {
        return inheritedValue;
    }

    /**
     * Set the reference to the <tt>PropertyValue</tt> from which the
     * value is being inherited.
     * @param bequeathed - the <tt>PropertyValue</tt> which contains
     * or will contain the the computed value of the percentage being
     * inherited.
     */
    public void setInheritedValue(PropertyValue bequeathed) {
        inheritedValue = bequeathed;
    }

    /**
     * Attempt to resolve the <tt>IndirectValue</tt> object.
     * If no bequeathing <tt>PropertyValue</tt>, assume that the
     * bequeathing node is the parent node.  This is true for the
     * <tt>Inherit</tt>, <tt>InheritedValue</tt> and <tt>FromParent</tt>
     * objects.  <tt>FromNearestSpecified</tt> objects must override this
     * method to ensure that resolution is carried out against the correct
     * property value.
     * <p>If the computed value is
     * null, return this object.  If not, return the computed value.
     * @param node - the <tt>FONode</tt> with which this object is associated.
     * @return - a <tt>PropertyValue</tt> as described above.  A return of
     * the same <tt>IndirectValue</tt> object implies that the inherited
     * computed value has not yet been resolved in the ancestor.
     */
    public PropertyValue resolve(FONode node) throws PropertyException {
        PropertyValue pv;
        if (inheritedValue == null)
            inheritedValue = node.fromParent(sourceProperty);
        if (isUnresolved(inheritedValue))
            return this;
        pv = inheritedValue;
        // Check that the property is the same
        if (property != pv.getProperty()) {
            // Don't clone if it's another indirect value - just keep this one
            // When the value finally resolves into a length, we will clone.
            if (pv instanceof IndirectValue) return this;
            try {
                pv = (PropertyValue)(pv.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e.getMessage());
            }
        }
        return pv;
    }

    public static boolean isUnresolved(PropertyValue value) {
        return (value.getType() == PropertyValue.NUMERIC
                                    && ((Numeric)(value)).isPercentage());
    }

    public static PropertyValue adjustedPropertyValue(PropertyValue value)
                    throws PropertyException
    {
        if (isUnresolved(value)) {
            Inherit inherit = new Inherit(value.getProperty());
            inherit.setInheritedValue(value);
            return inherit;
        }
        return value;
    }

    // N.B. no validation on this class - subclasses will validate
    // against the interface-defined validate(int) method in the
    // superclass.
}
