package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.datatypes.Inherit;

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
 * A class used to indicate that a value in a compound sub-property
 * should be inherited as a result of the 'inherit' keyword being specified
 * on the compound shorthand.
 */

public class InheritCompound extends Inherit {

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param sourceProperty the <tt>int</tt> index of the property from
     * which the inherited value is derived.
     * @exception PropertyException
     */
    public InheritCompound(int property, int sourceProperty)
        throws PropertyException
    {
        super(property);
        this.sourceProperty = sourceProperty;
    }

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public InheritCompound(int property)
        throws PropertyException
    {
        this(property, property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param sourcePropertyName the <tt>String</tt> name of the property
     * from which the inherited value is derived.
     * @exception PropertyException
     */
    public InheritCompound(String propertyName, String sourcePropertyName)
        throws PropertyException
    {
        super(propertyName);
        property = PropertyConsts.getPropertyIndex(propertyName);
        sourceProperty = PropertyConsts.getPropertyIndex(sourcePropertyName);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public InheritCompound(String propertyName)
        throws PropertyException
    {
        this(propertyName, propertyName);
    }

    /**
     * validate the <i>Inherit</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(sourceProperty, Properties.INHERIT_COMPOUND);
    }

}
