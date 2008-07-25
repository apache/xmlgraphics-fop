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

package org.apache.fop.fo;

import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;

import org.apache.xmlgraphics.util.QName;

/**
 * Abstract base class for Element Mappings (including FO Element Mappings)
 * which provide the framework of valid elements and attibutes for a given
 * namespace.
 */
public abstract class ElementMapping {
    /** constant for defining the default value */
    public static final String DEFAULT = "<default>";

    /** The HashMap table of formatting objects defined by the ElementMapping */
    protected Map foObjs = null;

    /** The namespace for the ElementMapping */
    protected String namespaceURI = null;

    /**
     * Returns a HashMap of maker objects for this element mapping
     *
     * @return Table of Maker objects for this ElementMapping
     */
    public Map getTable() {
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
     * Returns the DOMImplementation used by this ElementMapping. The value returned may be null
     * for cases where no DOM is used to represent the element tree (XSL-FO, for example). This
     * method is used by the intermediate format to instantiate the right kind of DOM document
     * for foreign objects. For example, SVG handled through Apache Batik has to use a special
     * DOMImplementation.
     * @return the DOMImplementation used by this ElementMapping, may be null
     */
    public DOMImplementation getDOMImplementation() {
        return null; //For namespaces not used in foreign objects
    }

    /**
     * @return the default DOMImplementation when no specialized DOM is necessary.
     */
    public static DOMImplementation getDefaultDOMImplementation() {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setNamespaceAware(true);
        fact.setValidating(false);
        try {
            return fact.newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(
                    "Cannot return default DOM implementation: " + e.getMessage());
        }
    }

    /** @return the standard namespace prefix for this namespace or null if it is not known. */
    public String getStandardPrefix() {
        return null;
    }

    /**
     * Indicates whether a particular attribute of the namespace is a property, i.e. the attribute
     * value should be converted to a property value.
     * @param attributeName the attribute name
     * @return true if the attribute should be converted to a property
     */
    public boolean isAttributeProperty(QName attributeName) {
        return false;
    }

    /**
     * Initializes the set of maker objects associated with this ElementMapping
     */
    protected abstract void initialize();

    /**
     * Base class for all Makers. It is responsible to return the right kind of FONode for a
     * particular element.
     */
    public static class Maker {

        /**
         * Creates a new FONode (or rather a specialized subclass of it).
         * @param parent the parent FONode
         * @return the newly created FONode instance
         */
        public FONode make(FONode parent) {
            return null;
        }
    }
}
