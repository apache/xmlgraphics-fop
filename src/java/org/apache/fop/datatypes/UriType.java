/*
 * UriType.java
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
        super.validate(Property.URI_SPECIFICATION);
    }

}
