/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.util.Service;
import org.apache.fop.fo.ElementMapping.Maker;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.image.ImageFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX Handler that passes parsed data to the various
 * FO objects, where they can be used either to build
 * an FO Tree, or used by Structure Renderers to build
 * other data structures.
 */
public class FOTreeBuilder extends DefaultHandler {

    /**
     * Table mapping element names to the makers of objects
     * representing formatting objects.
     */
    protected Map fobjTable = new java.util.HashMap();

    /**
     * logging instance
     */
    protected Log log = LogFactory.getLog(FOTreeBuilder.class);

    /**
     * Set of mapped namespaces.
     */
    protected Set namespaces = new java.util.HashSet();

    /**
     * The root of the formatting object tree
     */
    protected Root rootFObj = null;

    /**
     * Current formatting object being handled
     */
    protected FONode currentFObj = null;

    /**
     * Current propertyList for the node being handled.
     */
    protected PropertyList currentPropertyList;

    /**
     * The class that handles formatting and rendering to a stream
     * (mark-fop@inomial.com)
     */
    private FOEventHandler foEventHandler;

    /** The SAX locator object managing the line and column counters */
    private Locator locator; 
    
    /** The user agent for this processing run. */
    private FOUserAgent userAgent;
    
    /**
     * FOTreeBuilder constructor
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param foUserAgent in effect for this process
     * @param stream OutputStream to direct results
     * @throws FOPException if the FOTreeBuilder cannot be properly created
     */
    public FOTreeBuilder(String outputFormat, FOUserAgent foUserAgent, 
        OutputStream stream) throws FOPException {

        this.userAgent = foUserAgent;
        
        //This creates either an AreaTreeHandler and ultimately a Renderer, or
        //one of the RTF-, MIF- etc. Handlers.
        foEventHandler = foUserAgent.getRendererFactory().createFOEventHandler(
                foUserAgent, outputFormat, stream);
        foEventHandler.setPropertyListMaker(new PropertyListMaker() {
            public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
                return new StaticPropertyList(fobj, parentPropertyList);
            }
        });

        // Add standard element mappings
        setupDefaultMappings();

        // add additional ElementMappings defined within FOUserAgent
        List addlEMs = foUserAgent.getAdditionalElementMappings();

        if (addlEMs != null) {
            for (int i = 0; i < addlEMs.size(); i++) {
                addElementMapping((ElementMapping) addlEMs.get(i));
            }
        }
    }

    /**
     * Sets all the element and property list mappings to their default values.
     *
     */
    private void setupDefaultMappings() {
        addElementMapping("org.apache.fop.fo.FOElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.svg.SVGElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.svg.BatikExtensionElementMapping");
        addElementMapping("org.apache.fop.fo.extensions.ExtensionElementMapping");
        addElementMapping("org.apache.fop.render.ps.extensions.PSExtensionElementMapping");

        // add mappings from available services
        Iterator providers = Service.providers(ElementMapping.class);
        if (providers != null) {
            while (providers.hasNext()) {
                String str = (String)providers.next();
                try {
                    addElementMapping(str);
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

    private void addElementMapping(ElementMapping mapping) {
        this.fobjTable.put(mapping.getNamespaceURI(), mapping.getTable());
        this.namespaces.add(mapping.getNamespaceURI().intern());
    }

    /**
     * This method enables to reduce memory consumption of the FO tree slightly. When it returns
     * true no Locator is passed to the FO tree nodes which would copy the information into
     * a SAX LocatorImpl instance.
     * @return true if no context information should be stored on each node in the FO tree.
     */
    protected boolean isLocatorDisabled() {
        //TODO make this configurable through the FOUserAgent so people can optimize memory
        //consumption.
        return false;
    }
    
    /**
     * SAX Handler for locator
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    /** @return a Locator instance if it is available and not disabled */
    protected Locator getEffectiveLocator() {
        return (isLocatorDisabled() ? null : this.locator);
    }
    
    /**
     * SAX Handler for characters
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] data, int start, int length) 
        throws FOPException {
            if (currentFObj != null) {
                currentFObj.addCharacters(data, start, start + length, 
                        currentPropertyList, getEffectiveLocator());
            }
    }

    /**
     * SAX Handler for the start of the document
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        rootFObj = null;    // allows FOTreeBuilder to be reused
        if (log.isDebugEnabled()) {
            log.debug("Building formatting object tree");
        }
        foEventHandler.startDocument();
    }

    /**
     * SAX Handler for the end of the document
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        rootFObj = null;
        currentFObj = null;
        if (log.isDebugEnabled()) {
            log.debug("Parsing of document complete");
        }
        foEventHandler.endDocument();
        
        //Notify the image factory that this user agent has expired.
        ImageFactory.getInstance().removeContext(this.userAgent);
    }

    /**
     * SAX Handler for the start of an element
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String rawName,
                             Attributes attlist) throws SAXException {

        /* the node found in the FO document */
        FONode foNode;
        PropertyList propertyList;

        // Check to ensure first node encountered is an fo:root
        if (rootFObj == null) {
            if (!namespaceURI.equals(FOElementMapping.URI) 
                || !localName.equals("root")) {
                throw new SAXException(new ValidationException(
                    "Error: First element must be the fo:root formatting object. Found " 
                        + FONode.getNodeString(namespaceURI, localName) + " instead."
                        + " Please make sure you're producing a valid XSL-FO document."));
            }
        } else { // check that incoming node is valid for currentFObj
            if (namespaceURI.equals(FOElementMapping.URI)) {
                // currently no fox: elements to validate
                // || namespaceURI.equals(ExtensionElementMapping.URI) */) {
                try {
                    currentFObj.validateChildNode(locator, namespaceURI, localName);
                } catch (ValidationException e) {
                    throw e;
                }
            }
        }
        
        ElementMapping.Maker fobjMaker = findFOMaker(namespaceURI, localName);
//      log.debug("found a " + fobjMaker.toString());

        try {
            foNode = fobjMaker.make(currentFObj);
            propertyList = foNode.createPropertyList(currentPropertyList, foEventHandler);
            foNode.processNode(localName, getEffectiveLocator(), attlist, propertyList);
            foNode.startOfNode();
        } catch (IllegalArgumentException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            rootFObj = (Root) foNode;
            rootFObj.setFOEventHandler(foEventHandler);
        } else {
            currentFObj.addChildNode(foNode);
        }

        currentFObj = foNode;
        if (propertyList != null) {
            currentPropertyList = propertyList;
        }
    }

    /**
     * SAX Handler for the end of an element
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String rawName)
                throws FOPException {
        if (currentFObj == null) {
            throw new IllegalStateException(
                    "endElement() called for " + rawName + " where there is no current element.");
        } else if (!currentFObj.getLocalName().equals(localName) 
                || !currentFObj.getNamespaceURI().equals(uri)) {
            log.warn("Mismatch: " + currentFObj.getLocalName() 
                    + " (" + currentFObj.getNamespaceURI() 
                    + ") vs. " + localName + " (" + uri + ")");
        }
        currentFObj.endOfNode();

        if (currentPropertyList.getFObj() == currentFObj) {
            currentPropertyList = currentPropertyList.getParentPropertyList();
        }
        if (currentFObj.getParent() == null) {
            log.debug("endElement for top-level " + currentFObj.getName());
        }
        currentFObj = currentFObj.getParent();
    }

    /**
     * Finds the Maker used to create node objects of a particular type
     * @param namespaceURI URI for the namespace of the element
     * @param localName name of the Element
     * @return the ElementMapping.Maker that can create an FO object for this element
     * @throws FOPException if a Maker could not be found for a bound namespace.
     */
    private Maker findFOMaker(String namespaceURI, String localName) throws FOPException {
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
          if (namespaces.contains(namespaceURI.intern())) {
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

    /** @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException) */
    public void warning(SAXParseException e) {
        log.warn(e.toString());
    }

    /** @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException) */
    public void error(SAXParseException e) {
        log.error(e.toString());
    }

    /** @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException) */
    public void fatalError(SAXParseException e) throws SAXException {
        log.error(e.toString());
        throw e;
    }

    /**
     * Provides access to the underlying FOEventHandler object.
     * @return the FOEventHandler object
     */
    public FOEventHandler getEventHandler() {
        return foEventHandler;
    }

    /**
     * Returns the results of the rendering process. Information includes
     * the total number of pages generated and the number of pages per
     * page-sequence.
     * @return the results of the rendering process.
     */
    public FormattingResults getResults() {
        if (getEventHandler() instanceof AreaTreeHandler) {
            return ((AreaTreeHandler)getEventHandler()).getResults();
        } else {
            //No formatting results available for output formats no 
            //involving the layout engine.
            return null;   
        }
    }
    
    /**
     * Resets this object for another run.
     */
    public void reset() {
        currentFObj = null;
        rootFObj = null;
        foEventHandler = null;
    }

}

