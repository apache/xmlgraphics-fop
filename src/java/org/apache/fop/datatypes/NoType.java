/*
 * None.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
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

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * Class for dummy property values; e.g. unsupported properties or null
 * initial values.
 */

public class NoType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public NoType(int property)
        throws PropertyException
    {
        super(property, PropertyValue.NO_TYPE);
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @exception PropertyException
     */
    public NoType(String propertyName)
        throws PropertyException
    {
        super(propertyName, PropertyValue.NO_TYPE);
    }

    /**
     * validate the <i>None</i> against the associated property.
     */
    public void validate() throws PropertyException {
        if ((propertyConsts.getDataTypes(property) & Property.AURAL) != 0)
            return;
        if (propertyConsts.getInitialValueType(property)
                                                    == Property.NOTYPE_IT)
            return;
        throw new PropertyException
                ("NoType property is invalid for property "
                 + property + " " + PropNames.getPropertyName(property));
    }

}
