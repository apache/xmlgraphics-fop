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

    private String iso639_1Code = null;

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
        iso639_1Code = CountryLanguageScript.canonical639_1Code(code);
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
     * Gets the ISO 639-2T language code
     * @return the code.
     */
    public String getLanguage() {
        return string;
    }

    /**
     * Gets the ISO 639-2T language code from a PropertyValue
     * @param pv
     * @return the language code
     * @exception PropertyException if the <code>PropertyValue</code> is not
     * a <code>LanguageType</code>
     */
    public static String getLanguage(PropertyValue pv)
    throws PropertyException {
        if (pv.getType() == PropertyValue.LANGUAGE) {
            return ((LanguageType)pv).getLanguage();
        }
        throw new PropertyException("PropertyValue not an LANGUAGE type");
    }

    /**
     * Gets the ISO 639-1 language code
     * @return the language code
     */
    public String getISO639_1Language() {
        return iso639_1Code;
    }

    /**
     * Gets the ISO 639-1 language code from a PropertyValue
     * @param pv
     * @return the language code
     * @exception PropertyException if the <code>PropertyValue</code> is not
     * a <code>LanguageType</code>
     */
    public static String getISO639_1Language(PropertyValue pv)
    throws PropertyException {
        if (pv.getType() == PropertyValue.LANGUAGE) {
            return ((LanguageType)pv).getISO639_1Language();
        }
        throw new PropertyException("PropertyValue not an LANGUAGE type");
    }
}
