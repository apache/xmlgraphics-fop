package org.apache.fop.fo.expr;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;

/*
 * PropertyValueList.java
 * $Id$
 *
 * Created: Tue Dec 11 22:37:16 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
     * @param property <tt>int</tt> index of the property.
     */
    public PropertyValueList(int property) throws PropertyException {
        super();
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        this.property = property;
        type = PropertyValue.LIST;
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     */
    public PropertyValueList(String propertyName)
        throws PropertyException
    {
        super();
        property = PropertyConsts.getPropertyIndex(propertyName);
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        type = PropertyValue.LIST;
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
        property = PropertyConsts.getPropertyIndex(propertyName);
        if (property < 1 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index: " + property);
        type = PropertyValue.LIST;
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
     * @return a <tt>boolean</tt> success or failure(?).
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
        String str, cstr;
        try {
            str = "Property: " + PropNames.getPropertyName(property)
                    + " Index: " + property + " List contents:\n";
            Iterator contents = iterator();
            while (contents.hasNext()) {
                int i = 0, j = 0;
                cstr = contents.next().toString();
                while (i < cstr.length() && j >= 0) {
                    j = cstr.indexOf('\n');
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
