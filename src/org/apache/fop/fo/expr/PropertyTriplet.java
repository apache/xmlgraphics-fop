package org.apache.fop.fo.expr;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * PropertyTriplet.java
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
 * A <tt><i>PropertyTriplet</i></tt> is a set of possible values of an
 * instance of a property at a given point in the FO tree.  The three
 * possible values are specified, computed and actual.  All three values
 * are represented by a subclass of <tt>PropertyValue</tt>.  In addition
 * the object may contain a string with the specified expression.
 * <p>
 * Values may, and frequently will be, null, especially specified and actual.
 */

public class PropertyTriplet {

    private int property;
    private PropertyValue specified;
    private PropertyValue computed;
    private PropertyValue actual;
    private String expression;
    private boolean wasSpecified;

    public PropertyTriplet() {
        // PropertyValues and expression are null
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     * @param actual a <tt>PropertyValue</tt>.
     * @param expression a <tt>String</tt>.
     * @exception PropertyException if <i>property</i> is not within the
     * range of valid property indices.
     */
    public PropertyTriplet(int property, PropertyValue specified,
                           PropertyValue computed, PropertyValue actual,
                           String expression)
        throws PropertyException
    {
        if (property < 0 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index.");
        
        this.property = property;
        this.specified = specified;
        this.computed = computed;
        this.actual = actual;
        this.expression = expression;
    }

    /**
     * @param propertyName a <tt>String</tt> containing property name.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     * @param actual a <tt>PropertyValue</tt>.
     * @param expression a <tt>String</tt>.
     * @exception PropertyException if <i>property</i> is not within the
     * range of valid property indices.
     */
    public PropertyTriplet(String propertyName, PropertyValue specified,
                           PropertyValue computed, PropertyValue actual,
                           String expression)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
             specified, computed, actual, expression);
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     */
    public PropertyTriplet(int property, PropertyValue specified)
        throws PropertyException
    {
        this(property, specified, null, null, null);
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param expression a <tt>String</tt>.
     * @param specified a <tt>PropertyValue</tt>.
     */
    public PropertyTriplet(int property, PropertyValue specified,
                           String expression)
        throws PropertyException
    {
        this(property, specified, null, null, expression);
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     */
    public PropertyTriplet
        (int property, PropertyValue specified, PropertyValue computed)
        throws PropertyException
    {
        this(property, specified, computed, null, null);
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     * @param expression a <tt>String</tt>.
     */
    public PropertyTriplet
        (int property, PropertyValue specified, PropertyValue computed,
         String expression)
        throws PropertyException
    {
        this(property, specified, computed, null, expression);
    }

    /**
     * N.B. This sets expression to null as a side-effect.
     * @param value a <tt>PropertyValue</tt>.
     */
    public void setSpecified(PropertyValue value) {
        specified = value;
        expression = null;
    }

    /**
     * @param value a <tt>PropertyValue</tt>.
     * @param expr a <tt>String</tt>, the specified expression.
     */
    public void setSpecified(PropertyValue value, String expr) {
        specified = value;
        expression = expr;
    }

    public void setComputed(PropertyValue value) {
        computed = value;
    }

    public void setActual(PropertyValue value) {
        actual = value;
    }

    public String getExpression() {
        return expression;
    }

    public PropertyValue getSpecified() {
        return specified;
    }

    public PropertyValue getComputed() {
        return computed;
    }

    public PropertyValue getActual() {
        return actual;
    }

    public PropertyValue getComputedOrSpecified() {
        return computed != null ? computed
                : specified != null ? specified : null;
    }

    public int getProperty() {
        return property;
    }

    public int getSpecifiedProperty() {
        return specified.getProperty();
    }

    public int getComputedProperty() {
        return computed.getProperty();
    }

    public int getActualProperty() {
        return actual.getProperty();
    }

    public String toString() {
        String tmpstr = "Specified: ";
        if (specified != null) tmpstr += specified.toString();
        else tmpstr += "null";
        tmpstr += "\nExpression: ";
        if (expression != null) tmpstr += expression;
        else tmpstr += "null";
        tmpstr += "\nComputed: ";
        if (computed != null) tmpstr += computed.toString();
        else tmpstr += "null";
        tmpstr += "\nActual: ";
        if (actual != null) tmpstr += actual.toString();
        else tmpstr += "null";
        tmpstr += "\n";
        return tmpstr;
    }

}
