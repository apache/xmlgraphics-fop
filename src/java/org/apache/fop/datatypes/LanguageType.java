/*
 * LanguageType.java
 * $Id$
 *
 * Created: Mon Nov 26 22:46:05 2001
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
 * A class for <tt>language</tt> specifiers.
 */

public class LanguageType extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public LanguageType(int property, String languageCode)
        throws PropertyException
    {
        super(property, languageCode, PropertyValue.LANGUAGE);
        // Validate the code
        String code;
        if ((code = CountryLanguageScript.canonicalLangCode(languageCode))
                == null)
            throw new PropertyException
                             ("Invalid language code: " + languageCode);
        setString(code);
    }

    public LanguageType(String propertyName, String languageCode)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), languageCode);
    }

    /**
     * Validate the <i>LanguageType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Property.LANGUAGE_T);
    }

    /**
     * @return the <tt>String</tt> language code.
     */
    public String getLanguage() {
        return string;
    }

}
