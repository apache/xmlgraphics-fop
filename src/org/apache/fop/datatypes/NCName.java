
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.datatypes.StringType;

/*
 * NCName.java
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
 * An NCName.
 */

public class NCName extends StringType {

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public NCName (int property, String string)
        throws PropertyException
    {
        super(property, string);
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public NCName (String propertyName, String string)
        throws PropertyException
    {
        super(propertyName, string);
    }

    /**
     * @return the String.
     */
    public String getNCName() {
        return string;
    }

    /**
     * validate the <i>NCName</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.NCNAME);
    }

}
