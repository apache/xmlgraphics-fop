package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.Properties;

/*
 * MimeType.java
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
 * Class for mime-type subset of <tt>content-type</tt>.
 */

public class MimeType extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * A mimetype; one of the possible types of value for
     * <i>content-type</i>.
     */
    private String mimetype;

    /**
     * @param property the <tt>int</tt> index of the property on which
     * this value is being defined.
     * @param mimetype the <tt>String</tt> containing the mimetype
     * extracted from <tt>url(...)</tt>.
     * @exception PropertyException
     */
    public MimeType(int property, String mimetype)
        throws PropertyException
    {
        super(property);
        this.mimetype = mimetype;
    }

    /**
     * @param propertyName the <tt>String</tt> name of the property on which
     * this value is being defined.
     * @param mimetype the <tt>String</tt> containing the mimetype
     * extracted from <tt>url(...)</tt>.
     * @exception PropertyException
     */
    public MimeType(String propertyName, String mimetype)
        throws PropertyException
    {
        super(propertyName);
        this.mimetype = mimetype;
    }

    /**
     * @return a <tt>String</tt> containing the MIMETYPE.
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * validate the <i>MimeType</i> against the associated property.
     */
    public void validate() throws PropertyException {
        super.validate(Properties.MIMETYPE);
    }

}
