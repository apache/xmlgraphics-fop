package org.apache.fop.datatypes.indirect;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyTriplet;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.FONode;
import org.apache.fop.datatypes.indirect.IndirectValue;

/*
 * Inherit.java
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
 * A class representing the <tt>inherit</tt> keyword.  This keyword is
 * regarded as a property value which is always equivalent to the computed
 * value of the parent.  It cannot refer to a value defined on any other
 * property.
 */

public class Inherit extends IndirectValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Override the dual-property constructor of <tt>IndirectValue</tt>.
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param sourceProperty the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    public Inherit(int property, int sourceProperty)
        throws PropertyException
    {
        super(property, PropertyValue.INHERIT, sourceProperty);
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Inherit(int property)
        throws PropertyException
    {
        this(property, property);
    }

    /**
     * Override the dual-property constructor of <tt>IndirectValue</tt>.
     * <i>'inherit'</i> cannot draw a value from a different property from
     * the one on which it was defined, so this constructor is private.
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param sourcePropertyName the <tt>String</tt> name of the property
     * from which the inherited value is derived.
     * @exception PropertyException
     */
    private Inherit(String propertyName, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.INHERIT, sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Inherit(String propertyName)
        throws PropertyException
    {
        this(propertyName, propertyName);
    }

    /**
     * validate the <i>Inherit</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(getSourceProperty(), Properties.INHERIT);
    }

}
