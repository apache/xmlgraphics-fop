/*
 * $Id$
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
 * A class for <tt>country</tt> specifiers. 
 */

public class CountryType extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public CountryType(int property, String countryCode)
        throws PropertyException
    {
        super(property, countryCode, PropertyValue.COUNTRY);
        // Validate the code
        String code;
        if ((code = CountryLanguageScript.canonicalCountryCode(countryCode))
                == null)
            throw new PropertyException
                             ("Invalid country code: " + countryCode);
        setString(code);
    }

    public CountryType(String propertyName, String countryCode)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), countryCode);
    }

    /**
     * @return the <tt>String</tt> country code.
     */
    public String getCountry() {
        return string;
    }

    /**
     * Validate the <i>CountryType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.COUNTRY_T);
    }

}
