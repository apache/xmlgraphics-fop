
package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;
import org.apache.fop.configuration.Configuration;

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

public class Country extends NCName {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public Country(int property, String countryCode) throws PropertyException
    {
        super(property, countryCode);
        // Validate the code
        if (Configuration.getHashMapEntry("countriesMap", countryCode)
            == null) throw new PropertyException
                             ("Invalid country code: " + countryCode);
    }

    public Country(String propertyName, String countryCode)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), countryCode);
    }

    /**
     * @return the <tt>String</tt> country code.
     */
    public String getCountry() {
        return string;
    }

    /**
     * Validate the <i>Country</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.COUNTRY_T);
    }

}
