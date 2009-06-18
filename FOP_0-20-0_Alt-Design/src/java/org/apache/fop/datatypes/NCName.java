/*
 * NCName.java
 * $Id$
 *
 * Created: Fri Nov 23 15:21:37 2001
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
     * Get the NCName string value.
     * @return the String.
     */
    public String getNCName() {
        return string;
    }

    /**
     * Return the NCName value from a PropertyValue. 
     * @param pv
     * @return the NCName string value
     * @exception PropertyException if the <code>PropertyValue</code> is not
     * an <code>NCName</code>
     */
    public static String getNCName(PropertyValue pv)
    throws PropertyException {
        if (pv.getType() == PropertyValue.NCNAME) {
            return ((NCName)pv).getNCName();
        }
        throw new PropertyException("PropertyValue not an NCNAME type");
    }

    /**
     * validate the <i>NCName</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.NCNAME);
    }

}
