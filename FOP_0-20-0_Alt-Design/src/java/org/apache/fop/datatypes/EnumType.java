/*
 * EnumType.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
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

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * Base class for representing enumerated values.  The value is maintained as
 * an <tt>int</tt> constant value.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class EnumType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * An integer enumeration value.
     */
    protected int enumValue;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public EnumType(int property, String enumText)
        throws PropertyException
    {
        this(property, enumText, PropertyValue.ENUM);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumval the <tt>int</tt> enumeration constant.
     * @exception PropertyException
     */
    public EnumType(int property, int enumval)
        throws PropertyException
    {
        this(property, enumval, PropertyValue.ENUM);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public EnumType(String propertyName, String enumText)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName),
                                                enumText, PropertyValue.ENUM);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumval the <tt>int</tt> enumeration constant.
     * @exception PropertyException
     */
    public EnumType(String propertyName, int enumval)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName),
                                                    enumval, PropertyValue.ENUM);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(int property, String enumText, int type)
        throws PropertyException
    {
        super(property, type);
        // Get the enumval integer or mapped enumval integer
        enumValue = propertyConsts.getEnumIndex(property, enumText);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumval the <tt>int</tt> enumeration constant.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(int property, int enumval, int type)
        throws PropertyException
    {
        super(property, type);
        enumValue = enumval;
        // Validate the text; getEnumText will throw a PropertyException
        // if the enumval integer is invalid
        String enumText = propertyConsts.getEnumText(property, enumval);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(String propertyName, String enumText, int type)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), enumText, type);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumval the <tt>int</tt> enumeration constant.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(String propertyName, int enumval, int type)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), enumval, type);
    }

    /**
     * @return the <tt>int</tt> ENUM value.
     */
    public int getEnumValue() {
        return enumValue;
    }

    /**
     * Return the ENUM value from a PropertyValue. 
     * @param pv
     * @return the ENUM constant
     * @exception PropertyException if the <code>PropertyValue</code> is not
     * an <code>EnumType</code>
     */
    public static int getEnumValue(PropertyValue pv)
    throws PropertyException {
        if (pv.getType() == PropertyValue.ENUM) {
            return ((EnumType)pv).getEnumValue();
        }
        throw new PropertyException("PropertyValue not an ENUM type");
    }

    /**
     * @return the <tt>String</tt> enumeration token.
     */
    public String getEnumToken() throws PropertyException {
        return propertyConsts.getEnumText(property, enumValue);
    }

    /**
     * validate the <i>EnumType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.ENUM);
    }

    public String toString() {
        String enumText;
        try {
            enumText = propertyConsts.getEnumText(property, enumValue);
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
        return enumText + " " + enumValue + "\n" + super.toString();
    }

}
