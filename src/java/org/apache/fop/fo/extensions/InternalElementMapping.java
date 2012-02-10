/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.extensions;

import java.util.HashMap;
import java.util.Set;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.ElementMapping;

/**
 * Element mapping for FOP's internal extension to XSL-FO.
 */
public class InternalElementMapping extends ElementMapping {

    /** The FOP extension namespace URI */
    public static final String URI = "http://xmlgraphics.apache.org/fop/internal";

    /** The standard XML prefix for elements and attributes in this namespace. */
    public static final String STANDARD_PREFIX = "foi";

    /** The "struct-id" attribute, to identify a structure tree element. */
    public static final String STRUCT_ID = "struct-id";

    /** The "struct-ref" attribute, to refer to a structure tree element. */
    public static final String STRUCT_REF = "struct-ref";

    private static final Set<String> PROPERTY_ATTRIBUTES = new java.util.HashSet<String>();

    static {
        //These are FOP's extension properties for accessibility
        PROPERTY_ATTRIBUTES.add(STRUCT_ID);
        PROPERTY_ATTRIBUTES.add(STRUCT_REF);
    }

    /**
     * Constructor.
     */
    public InternalElementMapping() {
        namespaceURI = URI;
    }

    /**
     * Initialize the data structures.
     */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap<String, Maker>();
        }
    }

    /** {@inheritDoc} */
    public String getStandardPrefix() {
        return STANDARD_PREFIX;
    }

    /** {@inheritDoc} */
    public boolean isAttributeProperty(QName attributeName) {
        if (!URI.equals(attributeName.getNamespaceURI())) {
            throw new IllegalArgumentException("The namespace URIs don't match");
        }
        return PROPERTY_ATTRIBUTES.contains(attributeName.getLocalName());
    }

}
