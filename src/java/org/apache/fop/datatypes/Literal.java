/*
 * Literal.java
 * $Id$
 * Created: Wed Nov 21 15:39:30 2001
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
 * Class to represent Literal values.  Subclass of <tt>StringType</tt>.
 */

public class Literal extends StringType {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Construct a <i>Literal</i> with a given <tt>String</tt>.
     * @param property the index of the property with which this value
     * is associated.
     * @param string the <tt>String</tt> value.
     */
    public Literal(int property, String string)
        throws PropertyException
    {
        super(property, string, PropertyValue.LITERAL);
    }

    /**
     * Construct a <i>Literal</i> with a given <tt>String</tt>.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param string the <tt>String</tt> value.
     */
    public Literal(String propertyName, String string)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), string);
    }

    /**
     * Validate this <i>Literal</i>.  Check that it is allowed on the
     * associated property.  A <i>Literal</i> may also encode a single
     * character; i.e. a <tt>&lt;character&gt;</tt> type.  If the
     * validation against <i>LITERAL</i> fails, try <i>CHARACTER_T</i>.
     */
    public void validate() throws PropertyException {
        try {
            super.validate(Property.LITERAL);
        } catch (PropertyException e) {
            if (string.length() == 1) {
                super.validate(Property.CHARACTER_T);
            } else {
                throw new PropertyException(e.getMessage());
            }
        }
    }

}
