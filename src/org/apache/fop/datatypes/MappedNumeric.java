package org.apache.fop.datatypes;

import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.FONode;

/*
 * MappedEnumType.java
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
