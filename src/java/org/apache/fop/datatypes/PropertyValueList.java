/*
 * PropertyValueList.java
 * $Id$
 *
 * Created: Tue Dec 11 22:37:16 2001
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.datatypes;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * A list of <tt>PropertyValue</tt> elements.
 */

public class PropertyValueList extends LinkedList implements PropertyValue {

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
     * @param property <tt>int</tt> index of the property.
     */
    public PropertyValueList(int property) throws PropertyException {
        super();
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        this.property = property;
        type = PropertyValue.LIST;
        propertyConsts = PropertyConsts.getPropertyConsts();
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     */
    public PropertyValueList(String propertyName)
        throws PropertyException
    {
        super();
        property = PropNames.getPropertyIndex(propertyName);
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        type = PropertyValue.LIST;
        propertyConsts = PropertyConsts.getPropertyConsts();
    }

    /**
     * Constructor with a <tt>Collection</tt>.  Pass through to superclass
     * only if the collection is another instance of a PropertyValueList.
     * @param property <tt>int</tt> index of the property.
     * @param c a <tt>Collection</tt>, which must be another
     * <i>PropertyValueList</i>.
     * @exception IllegalArgumentException if the <tt>Collection</tt> is
     * not a <i>PropertyValueList</i>.
     */
    public PropertyValueList(int property, Collection c)
        throws PropertyException
    {
        super(c);
        // This test only follows the super() call because that call must
        // be the first in a constructor.
        if (! (c instanceof PropertyValueList))
            throw new IllegalArgumentException
                    ("Collection is not a PropertyValueList.");
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        this.property = property;
        type = PropertyValue.LIST;
        propertyConsts = PropertyConsts.getPropertyConsts();
    }

    /**
     * Constructor with a <tt>Collection</tt>.  Pass through to superclass
     * only if the collection is another instance of a PropertyValueList.
     * @param propertyName a <tt>String</tt> containing the property name.
     * @param c a <tt>Collection</tt>, which must be another
     * <i>PropertyValueList</i>.
     * @exception IllegalArgumentException if the <tt>Collection</tt> is
     * not a <i>PropertyValueList</i>.
     */
    public PropertyValueList(String propertyName, Collection c)
        throws PropertyException
    {
        super(c);
        // This test only follows the super() call because that call must
        // be the first in a constructor.
        if (! (c instanceof PropertyValueList))
            throw new IllegalArgumentException
                    ("Collection is not a PropertyValueList.");
        property = PropNames.getPropertyIndex(propertyName);
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        type = PropertyValue.LIST;
        propertyConsts = PropertyConsts.getPropertyConsts();
    }

    /**
     * Append a PropertyValue or PropertyValueList to the end of the list.
     * @param o a <tt>PropertyValue</tt> or a <PropertyValueList</tt>;
     * the element to add.  Defined as an
     * <tt>Object</tt> to override the definition in <tt>LinkedList</tt>.
     * @return a <tt>boolean</tt> success or failure(?).
     * @exception IllegalArgumentException if the object is not a
     * <tt>PropertyValue</tt> or <tt>PropertyValueList</tt>.
     */
    public boolean add(Object o) {
        if (! (o instanceof PropertyValue || o instanceof PropertyValueList))
            throw new IllegalArgumentException
                    ("Object is not a PropertyValue or a PropertyValueList.");
        return super.add(o);
    }

    /**
     * Insert a <tt>PropertyValue</tt> or <tt>PropertyValueList</tt>
     * at the beginning of the list.
     * @param o a <tt>PropertyValue</tt> or a <PropertyValueList</tt>;
     * the element to add.  Defined as an
     * <tt>Object</tt> to override the definition in <tt>LinkedList</tt>.
     * @exception IllegalArgumentException if the object is not a
     * <tt>PropertyValue</tt> or <tt>PropertyValueList</tt>.
     */
    public void addFirst(Object o) {
        if (! (o instanceof PropertyValue || o instanceof PropertyValueList))
            throw new IllegalArgumentException
                    ("Object is not a PropertyValue or a PropertyValueList.");
        super.addFirst(o);
    }

    /**
     * Append a PropertyValue to the end of the list.
     * @param o a <tt>PropertyValue</tt>; the element to add.  Defined as an
     * <tt>Object</tt> to override the definition in <tt>LinkedList</tt>.
     * @exception IllegalArgumentException if the object is not a
     * <tt>PropertyValue</tt>.
     */
    public void addLast(Object o) {
        add(o);
    }

    /*
     * Following fields and methods implement the PropertyValue interface
     */

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

    public String toString() {
        String str, cstr;
        try {
            str = "Property: " + PropNames.getPropertyName(property)
                    + " Index: " + property + " List contents:\n";
            Iterator contents = iterator();
            while (contents.hasNext()) {
                int i = 0, j = 0;
                Object obj = contents.next();
                try {
                    cstr = (String)(obj.getClass()
                                .getMethod("toString", null)
                                .invoke(obj, null));
                } catch (IllegalAccessException e) {
                    throw new PropertyException (e);
                } catch (NoSuchMethodException e) {
                    throw new PropertyException (e);
                } catch (InvocationTargetException e) {
                    throw new PropertyException (e);
                }
                while (i < cstr.length() && j >= 0) {
                    j = cstr.indexOf('\n', j);
                    if (j >= 0) {
                        str = str + ">" + cstr.substring(i, ++j);
                        i = j;
                    } else {
                        str = str + ">" + cstr.substring(i);
                        i = cstr.length();
                    }
                }
            }
            return str;

        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object clone() {
        return super.clone();
    }

}// PropertyValueList
