
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.fo.expr.PropertyValue;

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
 * An NCName.  NCName may be instantiated directly, and it also serves as a
 * base class for a number of specific <tt>PropertyValue</tt> types.
 */

public class NCName extends StringType {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Used when <tt>NCName</tt> is used directly as the
     * <tt>PropertyValue</tt> type.
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public NCName (int property, String string)
        throws PropertyException
    {
        super(property, string, PropertyValue.NCNAME);
    }

    /**
     * Used when <tt>NCName</tt> is used directly as the
     * <tt>PropertyValue</tt> type.
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @exception PropertyException
     */
    public NCName (String propertyName, String string)
        throws PropertyException
    {
        super(propertyName, string, PropertyValue.NCNAME);
    }

    /**
     * Used when <tt>NCName</tt> is a base class for another type.
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @param type the type of <tt>PropertyValue</tt> being instantiated.
     * @exception PropertyException
     */
    public NCName (int property, String string, int type)
        throws PropertyException
    {
        super(property, string, type);
    }

    /**
     * Used when <tt>NCName</tt> is a base class for another type.
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @param type the type of <tt>PropertyValue</tt> being instantiated.
     * @exception PropertyException
     */
    public NCName (String propertyName, String string, int type)
        throws PropertyException
    {
        super(propertyName, string, type);
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
