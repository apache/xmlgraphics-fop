package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;

/*
 * Auto.java
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
 * Class for "auto" objects.
 */

public class Auto extends AbstractPropertyValue {

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Auto(int property)
        throws PropertyException
    {
        super(property);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Auto(String propertyName)
        throws PropertyException
    {
        super(propertyName);
    }

    /**
     * validate the <i>Auto</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.AUTO);
    }

}
