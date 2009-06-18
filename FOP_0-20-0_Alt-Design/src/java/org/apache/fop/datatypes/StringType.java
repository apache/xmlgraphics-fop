/*
 * StringType.java
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
 * <tt>StringType</tt> is not intended to be instantiated directly. It
 * is a base class for the two basic types of <tt>String</tt>
 * properties: <tt>NCName</tt> and <tt>Literal</tt>.
 */

public class StringType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    protected String string;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @param type the type of <tt>PropertyValue</tt> being instantiated.
     * @exception PropertyException
     */
    public StringType (int property, String string, int type)
        throws PropertyException
    {
        super(property, type);
        this.string = string;
    }

    /**
     * @param propertyName the <tt>String</tt< name of the property on which
     * this value is being defined.
     * @param string the <tt>String</tt>.
     * @param type the type of <tt>PropertyValue</tt> being instantiated.
     * @exception PropertyException
     */
    public StringType (String propertyName, String string, int type)
        throws PropertyException
    {
        super(propertyName, type);
        this.string = string;
    }

    /**
     * Set the string value.
     * @param string - the <tt>String</tt> value.
     */
    protected void setString(String string) {
        this.string = string;
    }

    /**
     * Get the string value.
     * @return the String.
     */
    public String getString() {
        return string;
    }

    /**
     * validate the <i>StringType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.STRING_TYPE);
    }

    public String toString() {
        return string + "\n" + super.toString();
    }
}
