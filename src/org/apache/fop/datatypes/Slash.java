
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;

/*
 * Slash.java
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
/**
 * A datatype representing an isolated forward slash character.
 * The only known occurence of an isolated forward slash is as a separator
 * between <em>font-size</em> and <em>line-height</em> specifiers in a
 * <em>font </em> shorthand value.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Slash extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Slash (int property)
        throws PropertyException
    {
        super(property, PropertyValue.SLASH);
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public Slash (String propertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.SLASH);
    }

    public String toString() {
        return "/";
    }

    /**
     * Validation not supported for <tt>Slash</tt>.
     */
    public void validate() throws PropertyException {
        throw new PropertyException
                ("Slash datatype should never be validated");
    }

}
