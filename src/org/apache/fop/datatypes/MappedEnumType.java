package org.apache.fop.datatypes;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;

/*
 * MappedEnumType.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class to represent an enumerated type whose values map onto another
 * <tt>String</tt>.
 */

public class MappedEnumType extends EnumType {

    /**
     * The String value to which the associated ENUM token maps.
     * It expresses some underlying type other than an ENUM.
     */
    private String mappedEnum;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedEnumType(int property, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(property, enumText);
        mappedEnum = PropertyConsts.getMappedEnumValue(property, enumValue);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enumText the <tt>String</tt> containing the enumeration text.
     * An <i>NCName</i>.
     * @exception PropertyException
     */
    public MappedEnumType(String propertyName, String enumText)
        throws PropertyException
    {
        // Set property index in AbstractPropertyValue
        // and enumValue enum constant in EnumType
        super(propertyName, enumText);
        mappedEnum = PropertyConsts.getMappedEnumValue(property, enumValue);
    }

    /**
     * @return a <tt>String</tt> containing the text of the value to which
     * this ENUM token is mapped.
     */
    public String getMappedEnumValue() {
        return mappedEnum;
    }

    /**
     * validate the <i>MappedEnumType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.MAPPED_ENUM);
    }

    public String toString() {
        return mappedEnum + " " + super.toString();
    }

}
