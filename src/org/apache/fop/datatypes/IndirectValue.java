package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyTriplet;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.FOTree;

/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A superclass for objects which may have deferred <tt>PropertyValue</tt>
 * resolution.  This is because their value is taken from
 * another <tt>PropertyValue</tt> object defined earlier in the FO tree.
 * These include <tt>Inherit</tt>, <tt>FromParent</tt> and
 * <tt>FromNearestSpecified</tt> objects.  If an <tt>InheritedValue</tt>
 * object is defined, it will also extend this class.
 * <p>The required value is usually the computed field of the
 * <tt>PropertyTriplet</tt> for the source property on the source node.  This
 * property may be different from the property of this object.  This class
 * provides accessors for the referenced <tt>PropertyTriplet</tt> and the
 * computed value of that triplet.  In some cases, the specified value is 
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
    private int sourceProperty;

    /**
     * The <tt>PropertyTriplet</tt> from which this object is being
     * inherited.  Set when the inheritance cannot be immediately resolved,
     * e.g. when the specified value is a percentage.
     */
    private PropertyTriplet inheritedValue = null;

    /**
     * @param property - the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @param sourceProperty - the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    public IndirectValue(int property, int type, int sourceProperty)
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
    public IndirectValue(int property, int type)
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
    public IndirectValue
                    (String propertyName, int type, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName, type);
        sourceProperty = PropertyConsts.getPropertyIndex(sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param type - the type of <tt>PropertyValue</tt>.
     * @exception PropertyException
     */
    public IndirectValue(String propertyName, int type)
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
     * @return <tt>PropertyTriplet</tt> which contains or will contain the
     * the computed value being inherited.  This field will be null except
     * when an unresolved computed value is being inherited.  If so,
     * a null value will be returned.  N.B. This triplet will have a
     * property value different from this <i>IndirectValue</i> object.
     */
    public PropertyTriplet getInheritedTriplet() {
        return inheritedValue;
    }

    /**
     * @return computed <tt>PropertyValue</tt> field from the
     * <tt>PropertyTriplet</tt> from which this object is inherting.
     * If the <i>inheritedValue</i> field is null, no resolution of the
     * inheritance has yet been attempted, and a null value is returned.
     * If the <i>inheritedValue</i> field is not null, return the
     * <i>computed</i> field, which may be null.  N.B. This
     * <tt>PropertyValue</tt> may have a property field different from 
     * this <i>IndirectValue</i> object.  The source property field is held in
     * the <i>sourceProperty</i> field.
     */
    public PropertyValue getInheritedValue() {
        if (inheritedValue != null) return inheritedValue.getComputed();
        return null;
    }

    /**
     * Set the reference to the <tt>PropertyTriplet</tt> from which the
     * value is being inherited.
     * @param bequeathed - the <tt>PropertyTriplet</tt> which contains
     * or will contain the the computed value of the percentage being
     * inherited.
     */
    public void setInheritedTriplet(PropertyTriplet bequeathed) {
        inheritedValue = bequeathed;
    }

    /**
     * Attempt to resove the <tt>IndirectValue</tt> object.  If no bequeathing
     * <tt>PropertyTriplet</tt> is associated with this object, get it
     * from the <i>foTree</i>.  If the computed value of that triplet is
     * null, return this object.  If not, return the computed value.
     * @param foTree - the <tt>FOTree</tt> with which this object is
     * associated.
     * @return - a <tt>PropertyValue</tt> as described above.  A return of
     * the same <tt>IndirectValue</tt> object implies that the inherited
     * computed value has not yet been resolved in the ancestor.
     */
    public PropertyValue resolve(FOTree foTree) throws PropertyException {
        PropertyValue pv;
        if (inheritedValue == null)
            inheritedValue = foTree.getInheritedTriplet(sourceProperty);
        if ((pv = inheritedValue.getComputed()) == null)
            return this;
        // Check that the property is the same
        if (property != pv.getProperty()) {
            try {
                pv = (PropertyValue)(pv.clone());
            } catch (CloneNotSupportedException e) {
                throw new PropertyException(e.getMessage());
            }
        }
        return pv;
    }

    // N.B. no validation on this class - subclasses will validate
    // against the interface-defined validate(int) method in the
    // superclass.
}
