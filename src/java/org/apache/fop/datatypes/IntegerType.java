/*
 * IntegerType.java
 *
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;


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
     * Return the int value from a PropertyValue. 
     * @param pv
     * @return the int value
     * @exception PropertyException if the <code>PropertyValue</code> is not
     * an <code>IntegerType</code>
     */
    public static int getIntValue(PropertyValue pv)
    throws PropertyException {
        if (pv.getType() == PropertyValue.INTEGER) {
            return ((IntegerType)pv).getInt();
        }
        throw new PropertyException("PropertyValue not an INTEGER type");
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
