/*
 * EnumType.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
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
        this(PropNames.getPropertyIndex(propertyName),
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
        this(PropNames.getPropertyIndex(propertyName),
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
        enumValue = propertyConsts.getEnumIndex(property, enumText);
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
        String enumText = propertyConsts.getEnumText(property, enum);
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
     * @param enum the <tt>int</tt> enumeration constant.
     * @param type of this value
     * @exception PropertyException
     */
    public EnumType(String propertyName, int enum, int type)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), enum, type);
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
