/*
 * AbstractPropertyValue.java
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
package org.apache.fop.datatypes;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

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
     * The PropertyConsts singleton.
     */
    public final PropertyConsts propertyConsts;
    
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
        propertyConsts = PropertyConsts.getPropertyConsts();
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     */
    public AbstractPropertyValue(String propertyName, int type)
        throws PropertyException
    {
        propertyConsts = PropertyConsts.getPropertyConsts();
        property = PropNames.getPropertyIndex(propertyName);
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
        //if (propertyConsts.inherited.get(testProperty) == Property.NO
        //&& (propertyConsts.getDataTypes(testProperty) & type) == 0) {

            if ((propertyConsts.getDataTypes(testProperty) & type) == 0) {
            String pname = PropNames.getPropertyName(testProperty);
            throw new PropertyException
                    ("Datatype(s) " +
                     Property.listDataTypes(type) +
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

    public static String typeString(int type) {
        if (type < 0 || type >PropertyValue.LAST_PROPERTY_TYPE)
            return "Property type out of range";
        return PropertyValue.propertyTypes.get(type);
    }

    public String toString() {
        try {
            return "Property: " + PropNames.getPropertyName(property)
                    + " Index: " + property + " Type: " +
                    typeString(type);
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
