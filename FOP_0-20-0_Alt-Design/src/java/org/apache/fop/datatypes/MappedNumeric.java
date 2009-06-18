/*
 * MappedEnumType.java
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

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * Class to represent an enumerated type whose values map onto a
 * <tt>Numeric</tt>.  This is a rethinking of the MappedEnumType, because
 * all mapped enums mapped to one form or other of <tt>Numeric</tt>.
 */

public class MappedNumeric extends EnumType {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The <tt>Numeric</tt> value to which the associated ENUM token maps.
     */
    private Numeric mappedNum;

    /**
     * @param foNode the <tt>FONode</tt> being built
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedNumeric(FONode foNode, int property, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(property, enumText, PropertyValue.MAPPED_NUMERIC);
        mappedNum =
                propertyConsts.getMappedNumeric(foNode, property, enumValue);
    }

    /**
     * @param foNode the <tt>FONode</tt> being built
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedNumeric(FONode foNode, String propertyName, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(propertyName, enumText, PropertyValue.MAPPED_NUMERIC);
        mappedNum =
                propertyConsts.getMappedNumeric(foNode, property, enumValue);
    }

    /**
     * @return a <tt>Numeric</tt> containing the value to which
     * this ENUM token is mapped.
     */
    public Numeric getMappedNumValue() {
        return mappedNum;
    }

    /**
     * validate the <i>MappedNumeric</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.MAPPED_LENGTH);
    }

    public String toString() {
        return mappedNum.toString() + " " + super.toString();
    }

}
