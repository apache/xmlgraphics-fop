package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.expr.PropertyValue;

/*
 * EnumType.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
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
     * @param enum the <tt>int</tt> enumeration constant.
     * @exception PropertyException
     */
    public EnumType(int property, int enum)
        throws PropertyException
    {
        this(property, enum, PropertyValue.ENUM);
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
        this(PropertyConsts.getPropertyIndex(propertyName),
                                                enumText, PropertyValue.ENUM);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enum the <tt>int</tt> enumeration constant.
     * @exception PropertyException
     */
    public EnumType(String propertyName, int enum)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
                                                    enum, PropertyValue.ENUM);
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
        // Get the enum integer or mapped enum integer
        enumValue = PropertyConsts.getEnumIndex(property, enumText);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param enum the <tt>int</tt> enumeration constant.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(int property, int enum, int type)
        throws PropertyException
    {
        super(property, type);
        enumValue = enum;
        // Validate the text; getEnumText will throw a PropertyException
        // if the enum integer is invalid
        String enumText = PropertyConsts.getEnumText(property, enum);
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
        this(PropertyConsts.getPropertyIndex(propertyName), enumText, type);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param enum the <tt>int</tt> enumeration constant.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(String propertyName, int enum, int type)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), enum, type);
    }

    /**
     * @return the <tt>int</tt> ENUM value.
     */
    public int getEnumValue() {
        return enumValue;
    }

    /**
     * @return the <tt>String</tt> enumeration token.
     */
    public String getEnumToken() throws PropertyException {
        return PropertyConsts.getEnumText(property, enumValue);
    }

    /**
     * validate the <i>EnumType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.ENUM);
    }

    public String toString() {
        String enumText;
        try {
            enumText = PropertyConsts.getEnumText(property, enumValue);
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
        return enumText + " " + enumValue + "\n" + super.toString();
    }

}
