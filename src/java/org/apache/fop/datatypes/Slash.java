/*
 * Slash.java
 * $Id$
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
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

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
