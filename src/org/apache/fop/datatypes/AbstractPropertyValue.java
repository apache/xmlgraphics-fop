package org.apache.fop.datatypes;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;

/*
 * AbstractPropertyValue.java
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Base abstract class for all property value types.
 */

public abstract class AbstractPropertyValue
    implements PropertyValue, Cloneable
{
    
    /**
     * An integer index to the type of property of which this is a value.
     */
    protected int property;
    
    /**
     * An integer property type.
     */
    public final int type;

    /**
     * The <tt>FONode</tt> that stacked this value.
     */
    private FONode stackedBy = null;
    
    /**
     * @param index index of the property in the property arrays.
     * @param type of this value
     */
    public AbstractPropertyValue(int index, int type)
        throws PropertyException
     {
        if (index < 1 || index > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + index);
        if (type < 0 || type > PropertyValue.LAST_PROPERTY_TYPE)
            throw new PropertyException("Invalid property type: " + type);
        property = index;
        this.type = type;
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     */
    public AbstractPropertyValue(String propertyName, int type)
        throws PropertyException
    {
        property = PropertyConsts.getPropertyIndex(propertyName);
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        if (type < 0 || type > PropertyValue.LAST_PROPERTY_TYPE)
            throw new PropertyException("Invalid property type: " + type);
        this.type = type;
    }

    /**
     * @return <tt>int</tt> property index.
     */
    public int getProperty() {
        return property;
    }

    public void setProperty(int index) throws PropertyException {
        if (index < 0 || index > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + index);
        property = index;
    }

    /**
     * @return type field of the <tt>PropertyValue</tt>.
     */
    public int getType() {
        return type;
    }

    /**
     * Set the node that stacked this value.
     * @param node - the <tt>FONode</tt> that stacked this value.
     */
    public void setStackedBy(FONode node) {
        stackedBy = node;
    }

    /**
     * Get the node that stacked this value.
     */
    public FONode getStackedBy() {
        return stackedBy;
    }

    /**
     * In some circumstances, the property against which a type is to be
     * validated may not be the same as the property against which this
     * <i>AbstractPropertyValue</i> is defined.
     * A specific property argument is then required.
     * @param testProperty <tt>int</tt> property index of the property
     * for which the type is to be validated.
     * @param type <tt>int</tt> bitmap of data types to check for
     * validity against this property.
     */
    public void validate(int testProperty, int type)
        throws PropertyException
    {
        // N.B. PROPERTY_SPECIFIC inheritance may require more specialized
        // checks.  Only line-height comes into this category.

        // N.B. The first commented-out condition means that I cannot validate
        // unless the property is NOT inherited.
        // I can't remember why I put this
        // condition in here.  Removing it.  pbw 2002/02/18
        //if (PropertyConsts.inherited.get(testProperty) == Properties.NO
        //&& (PropertyConsts.dataTypes.get(testProperty) & type) == 0) {

            if ((PropertyConsts.dataTypes.get(testProperty) & type) == 0) {
            String pname = PropNames.getPropertyName(testProperty);
            throw new PropertyException
                    ("Datatype(s) " +
                     Properties.listDataTypes(type) +
                     " not defined on " + pname);
        }
    }

    /**
     * @param type <tt>int</tt> bitmap of data types to check for
     * validity against this property.
     */
    public void validate(int type) throws PropertyException {
        // N.B. PROPERTY_SPECIFIC inheritance may require more specialized
        // checks.  Only line-height comes into this category.
        validate(property, type);
    }

    public String toString() {
        try {
            return "Property: " + PropNames.getPropertyName(property)
                    + " Index: " + property;
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
