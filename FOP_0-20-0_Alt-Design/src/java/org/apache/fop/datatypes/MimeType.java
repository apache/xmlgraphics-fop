/*
 * MimeType.java
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

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

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
        super(property, PropertyValue.MIME_TYPE);
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
        super(propertyName, PropertyValue.MIME_TYPE);
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
        super.validate(Property.MIMETYPE);
    }

}
