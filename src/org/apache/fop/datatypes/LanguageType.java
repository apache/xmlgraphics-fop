
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.PropertyValue;

/*
 * LanguageType.java
 * $Id$
 *
 * Created: Mon Nov 26 22:46:05 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
