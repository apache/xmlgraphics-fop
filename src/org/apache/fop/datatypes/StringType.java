
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;

/*
 * StringType.java
 * $Id$
 *
 * Created: Fri Nov 23 15:21:37 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * The base class for most datatypes which resolve to a <tt>String</tt>.
 */

public class StringType extends AbstractPropertyValue {

    protected String string;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public StringType (int property, String string)
        throws PropertyException
    {
        super(property);
        this.string = string;
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public StringType (String propertyName, String string)
        throws PropertyException
    {
        super(propertyName);
        this.string = string;
    }

    /**
     * @return the String.
     */
    public String getString() {
        return string;
    }

    /**
     * validate the <i>StringType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.STRING_TYPE);
    }

    public String toString() {
        return string + "\n" + super.toString();
    }
}
