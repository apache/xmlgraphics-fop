/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.fo;

import java.util.HashMap;

/**
 * Abstract base class for Element Mappings (including FO Element Mappings)
 * which provide the framework of valid elements and attibutes for a given
 * namespace.
 */
public abstract class ElementMapping {
    /** constant for defining the default value */
    public static final String DEFAULT = "<default>";

    /** The HashMap table of formatting objects defined by the ElementMapping */
    protected HashMap foObjs = null;

    /** The namespace for the ElementMapping */
    protected String namespaceURI = null;

    /**
     * Returns a HashMap of maker objects for this element mapping
     *
     * @return Table of Maker objects for this ElementMapping
     */
    public HashMap getTable() {
        if (foObjs == null) {
            initialize();
        }
        return foObjs;
    }

    /**
     * Returns the namespace URI for this element mapping
     *
     * @return Namespace URI for this element mapping
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Initializes the set of maker objects associated with this ElementMapping
     */
    protected abstract void initialize();

    public static class Maker {

        public FONode make(FONode parent) {
            return null;
        }
    }
}
