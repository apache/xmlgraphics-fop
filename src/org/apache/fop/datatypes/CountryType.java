package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.PropertyValue;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
