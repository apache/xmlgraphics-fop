package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;

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
 * Class for property values of "none".
 */

public class None extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public None(int property)
        throws PropertyException
    {
        super(property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public None(String propertyName)
        throws PropertyException
    {
        super(propertyName);
    }

    /**
     * validate the <i>None</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.NONE);
    }

}
