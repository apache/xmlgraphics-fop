
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.properties.*;

/*
 * IntegerType.java
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class IntegerType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The integer value of the type
     */
    private int intval = 0;

    /**
     * @param property the index of the property with which this value
     * is associated.
     * @param value the integer value.
     * @exception PropertyException
     */
    public IntegerType (int property, int value)
        throws PropertyException
    {
        super(property, PropertyValue.INTEGER);
        intval = value;
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param value the integer value.
     * @exception PropertyException
     */
    public IntegerType (String propertyName, int value)
        throws PropertyException
    {
        super(propertyName, PropertyValue.INTEGER);
        intval = value;
    }

    /**
     * @return <tt>int</tt> integer contents
     */
    public int getInt() {
        return intval;
    }

    /**
     * @param value <tt>int</tt> value to set
     */
    public void setInt(int value) {
        intval = value;
    }

    /**
     * validate the <i>IntegerType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.INTEGER);
    }

    public String toString() {
        return "" + intval + "\n" + super.toString();
    }

}
