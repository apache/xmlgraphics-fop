package org.apache.fop.datatypes;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;

/*
 * None.java
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
 * Class for dummy property values; e.g. unsupported properties or null
 * initial values.
 */

public class NoType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public NoType(int property)
        throws PropertyException
    {
        super(property, PropertyValue.NO_TYPE);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public NoType(String propertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.NONE);
    }

    /**
     * validate the <i>None</i> against the associated property.
     */
    public void validate() throws PropertyException {
        if ((PropertyConsts.dataTypes.get(property) & Properties.AURAL) != 0)
            return;
        if (PropertyConsts.getInitialValueType(property)
                                                    == Properties.NOTYPE_IT)
            return;
        throw new PropertyException
                ("NoType property is invalid for property "
                 + property + " " + PropNames.getPropertyName(property));
    }

}
