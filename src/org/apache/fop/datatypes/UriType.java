package org.apache.fop.datatypes;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;

/*
 * UriType.java
 * $Id$
 *
 * Created: Tue Nov 20 22:18:11 2001
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class for URLs specified with <tt>uri()</tt> function.
 */

public class UriType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * A URI Specification
     */
    private String uri;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param uri the <tt>String</tt> containing the uri extracted from
     * <tt>url(...)</tt>.
     * @exception PropertyException
     */
    public UriType(int property, String uri)
        throws PropertyException
    {
        super(property, PropertyValue.URI_TYPE);
        this.uri = uri;
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param uri the <tt>String</tt> containing the uri extracted from
     * <tt>url(...)</tt>.
     * @exception PropertyException
     */
    public UriType(String propertyName, String uri)
        throws PropertyException
    {
        super(propertyName, PropertyValue.URI_TYPE);
        this.uri = uri;
    }

    /**
     * @return a <tt>String</tt> containing the URI.
     */
    public String getUri() {
        return uri;
    }

    /**
     * validate the <i>UriType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.URI_SPECIFICATION);
    }

}
