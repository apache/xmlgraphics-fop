package org.apache.fop.datatypes;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;

/*
 * AbstractPropertyValue.java
 * $Id$
 *
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
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
