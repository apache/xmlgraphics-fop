package org.apache.fop.fo.expr;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FONode;
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
 * are represented by a subclass of <tt>PropertyValue</tt>.
 * <p>
 * Values may, and frequently will be, null, especially specified and actual.
 */

public class PropertyTriplet {

    /** The property with which this triplet is associated. */
    private int property;
    /**
     * The <i>specified</i> property value.  Note that this may be a
     * reference to a value associated with another property, so its
     * property index may be different from <i>property</i>.
     * TODO - ensure that whenever a value is derived from another property,
     * that the value is cloned and the property correctly set, rather than
     * simply held here as a reference.  This will render the above note
     * obsolete.  Same for <i>computed</i> and <i>actual</i>.
     */
    private PropertyValue specified;
    /**
     * The <i>computed</i> property value.  Note that this may be a
     * reference to a value associated with another property, so its
     * property index may be different from <i>property</i>.
     */
    private PropertyValue computed;
    /**
     * The <i>actual</i> property value.  Note that this may be a
     * reference to a value associated with another property, so its
     * property index may be different from <i>property</i>.
     */
    private PropertyValue actual;
    private boolean wasSpecified;

    /**
     * The <tt>FONode</tt> that stacked this triplet.  This is required in
     * event that later a triplet is overriden by a higher priority property
     * within the same FO; e.g. when the expansion of a shorthand is
     * overriden by a direct assignment.
     */
    private FONode stackedBy = null;

    public PropertyTriplet() {
        // PropertyValues are null
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     * @param actual a <tt>PropertyValue</tt>.
     * @exception PropertyException if <i>property</i> is not within the
     * range of valid property indices.
     */
    public PropertyTriplet(int property, PropertyValue specified,
                           PropertyValue computed, PropertyValue actual)
        throws PropertyException
    {
        if (property < 0 || property > PropNames.LAST_PROPERTY_INDEX)
            throw new PropertyException("Invalid property index.");
        
        this.property = property;
        this.specified = specified;
        this.computed = computed;
        this.actual = actual;
    }

    /**
     * @param propertyName a <tt>String</tt> containing property name.
     * @param specified a <tt>PropertyValue</tt>.
     * @param computed a <tt>PropertyValue</tt>.
     * @param actual a <tt>PropertyValue</tt>.
     * @exception PropertyException if <i>property</i> is not within the
     * range of valid property indices.
     */
    public PropertyTriplet(String propertyName, PropertyValue specified,
                           PropertyValue computed, PropertyValue actual)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
             specified, computed, actual);
    }

    /**
     * @param property an <tt>int</tt> property index.
     * @param specified a <tt>PropertyValue</tt>.
     */
    public PropertyTriplet(int property, PropertyValue specified)
        throws PropertyException
    {
        this(property, specified, null, null);
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
        this(property, specified, computed, null);
    }

    /**
     * Set the <i>specified</i> value.
     * @param value a <tt>PropertyValue</tt>.
     */
    public void setSpecified(PropertyValue value) {
        specified = value;
    }

    /**
     * Set the <i>computed</i> value.
     * @param value a <tt>PropertyValue</tt>.
     */
    public void setComputed(PropertyValue value) {
        computed = value;
    }

    /**
     * Set the <i>actual</i> value.
     * @param value a <tt>PropertyValue</tt>.
     */
    public void setActual(PropertyValue value) {
        actual = value;
    }

    /**
     * Get the <i>specified</i> value.
     * @return the <i>specified</i> <tt>PropertyValue</tt>.
     */
    public PropertyValue getSpecified() {
        return specified;
    }

    /**
     * Get the <i>computed</i> value.
     * @return the <i>computed</i> <tt>PropertyValue</tt>.
     */
    public PropertyValue getComputed() {
        return computed;
    }

    /**
     * Get the <i>actual</i> value.
     * @return the <i>actual</i> <tt>PropertyValue</tt>.
     */
    public PropertyValue getActual() {
        return actual;
    }

    /**
     * Get the property index associated with this triplet.
     * @return the property index.
     */
    public int getProperty() {
        return property;
    }

    /**
     * Get the property index associated with the specified value.
     * @return the property index.
     */
    public int getSpecifiedProperty() {
        return specified.getProperty();
    }

    /**
     * Get the property index associated with the computed value.
     * @return the property index.
     */
    public int getComputedProperty() {
        return computed.getProperty();
    }

    /**
     * Get the property index associated with the actual value.
     * @return the property index.
     */
    public int getActualProperty() {
        return actual.getProperty();
    }

    /**
     * Retrieve the <tt>FONode</tt> that stacked this <tt>PropertyTriplet</tt>.
     */
    public FONode getStackedBy() {
        return stackedBy;
    }

    /**
     * Record the <tt>FONode</tt> that stacked this <tt>PropertyTriplet</tt>.
     */
    public void setStackedBy(FONode stackedBy) {
        this.stackedBy = stackedBy;
    }

    public String toString() {
        String tmpstr = "Specified: ";
        if (specified != null) tmpstr += specified.toString();
        else tmpstr += "null";
        tmpstr += "\nComputed: ";
        if (computed != null) tmpstr += computed.toString();
        else tmpstr += "null";
        tmpstr += "\nActual: ";
        if (actual != null) tmpstr += actual.toString();
        else tmpstr += "null";
        tmpstr += "\nStacked by: ";
        if (stackedBy != null) tmpstr+= stackedBy.id;
        else tmpstr += "null";
        tmpstr += "\n";
        return tmpstr;
    }

}
