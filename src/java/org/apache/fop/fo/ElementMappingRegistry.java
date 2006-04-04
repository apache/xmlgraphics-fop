/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.DOMImplementation;
import org.xml.sax.Locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.Service;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.ElementMapping.Maker;

/**
 * This class keeps track of all configured ElementMapping implementations which are responsible
 * for properly handling all kinds of different XML namespaces.
 */
public class ElementMappingRegistry {

    /** logging instance */
    protected Log log = LogFactory.getLog(ElementMappingRegistry.class);
    
    /**
     * Table mapping element names to the makers of objects
     * representing formatting objects.
     */
    protected Map fobjTable = new java.util.HashMap();

    /**
     * Map of mapped namespaces and their associated ElementMapping instances.
     */
    protected Map namespaces = new java.util.HashMap();

    /**
     * Main constructor. Adds all default element mapping as well as detects ElementMapping
     * through the Service discovery.
     * @param factory the Fop Factory
     */
    public ElementMappingRegistry(FopFactory factory) {
        // Add standard element mappings
        setupDefaultMappings();
    }
    
    /**
     * Sets all the element and property list mappings to their default values.
     */
    private void setupDefaultMappings() {
        // add mappings from available services
        Iterator providers = Service.providers(ElementMapping.class);
        if (providers != null) {
            while (providers.hasNext()) {
                ElementMapping mapping = (ElementMapping)providers.next();
                try {
                    addElementMapping(mapping);
                } catch (IllegalArgumentException e) {
                    log.warn("Error while adding element mapping", e);
                }

            }
        }
    }

    /**
     * Add the element mapping with the given class name.
     * @param mappingClassName the class name representing the element mapping.
     * @throws IllegalArgumentException if there was not such element mapping.
     */
    public void addElementMapping(String mappingClassName)
                throws IllegalArgumentException {

        try {
            ElementMapping mapping
                = (ElementMapping)Class.forName(mappingClassName).newInstance();
            addElementMapping(mapping);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not find "
                                               + mappingClassName);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate "
                                               + mappingClassName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access "
                                               + mappingClassName);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(mappingClassName
                                               + " is not an ElementMapping");
        }
    }

    /**
     * Add the element mapping.
     * @param mapping the element mapping instance
     */
    public void addElementMapping(ElementMapping mapping) {
        this.fobjTable.put(mapping.getNamespaceURI(), mapping.getTable());
        this.namespaces.put(mapping.getNamespaceURI().intern(), mapping);
    }

    /**
     * Finds the Maker used to create node objects of a particular type
     * @param namespaceURI URI for the namespace of the element
     * @param localName name of the Element
     * @param locator the Locator instance for context information
     * @return the ElementMapping.Maker that can create an FO object for this element
     * @throws FOPException if a Maker could not be found for a bound namespace.
     */
    public Maker findFOMaker(String namespaceURI, String localName, Locator locator) 
                throws FOPException {
        Map table = (Map)fobjTable.get(namespaceURI);
        Maker fobjMaker = null;
        if (table != null) {
            fobjMaker = (ElementMapping.Maker)table.get(localName);
            // try default
            if (fobjMaker == null) {
                fobjMaker = (ElementMapping.Maker)table.get(ElementMapping.DEFAULT);
            }
        }

        if (fobjMaker == null) {
            if (namespaces.containsKey(namespaceURI.intern())) {
                  throw new FOPException(FONode.errorText(locator) 
                      + "No element mapping definition found for "
                      + FONode.getNodeString(namespaceURI, localName), locator);
            } else {
                log.warn("Unknown formatting object " + namespaceURI + "^" + localName);
                fobjMaker = new UnknownXMLObj.Maker(namespaceURI);
            }
        }
        return fobjMaker;
    }

    /**
     * Tries to determine the DOMImplementation that is used to handled a particular namespace.
     * The method may return null for namespaces that don't result in a DOM. It is mostly used
     * in namespaces occurring in foreign objects. 
     * @param namespaceURI the namespace URI
     * @return the handling DOMImplementation, or null if not applicable
     */
    public DOMImplementation getDOMImplementationForNamespace(String namespaceURI) {
        ElementMapping mapping = (ElementMapping)this.namespaces.get(namespaceURI);
        return mapping.getDOMImplementation();
    }
    
}
