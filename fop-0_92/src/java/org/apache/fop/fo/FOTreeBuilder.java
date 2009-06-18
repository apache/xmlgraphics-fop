/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.ElementMapping.Maker;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactory.ObjectSource;
import org.apache.fop.util.ContentHandlerFactory.ObjectBuiltListener;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
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

    /** logging instance */
    protected Log log = LogFactory.getLog(FOTreeBuilder.class);

    /** The registry for ElementMapping instances */
    protected ElementMappingRegistry elementMappingRegistry;

    /**
     * The root of the formatting object tree
     */
    protected Root rootFObj = null;

    /** Main DefaultHandler that handles the FO namespace. */
    protected MainFOHandler mainFOHandler;
    
    /** Current delegate ContentHandler to receive the SAX events */
    protected ContentHandler delegate;
    
    /**
     * The class that handles formatting and rendering to a stream
     * (mark-fop@inomial.com)
     */
    private FOEventHandler foEventHandler;

    /** The SAX locator object managing the line and column counters */
    private Locator locator; 
    
    /** The user agent for this processing run. */
    private FOUserAgent userAgent;
    
    private boolean used = false;
    
    private int depth;
    
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
        this.elementMappingRegistry = userAgent.getFactory().getElementMappingRegistry();        
        //This creates either an AreaTreeHandler and ultimately a Renderer, or
        //one of the RTF-, MIF- etc. Handlers.
        foEventHandler = foUserAgent.getRendererFactory().createFOEventHandler(
                foUserAgent, outputFormat, stream);
        foEventHandler.setPropertyListMaker(new PropertyListMaker() {
            public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
                return new StaticPropertyList(fobj, parentPropertyList);
            }
        });
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
                throws SAXException {
        delegate.characters(data, start, length);
    }

    /**
     * SAX Handler for the start of the document
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        if (used) {
            throw new IllegalStateException("FOTreeBuilder (and the Fop class) cannot be reused."
                    + " Please instantiate a new instance.");
        }
        used = true;
        rootFObj = null;    // allows FOTreeBuilder to be reused
        if (log.isDebugEnabled()) {
            log.debug("Building formatting object tree");
        }
        foEventHandler.startDocument();
        this.mainFOHandler = new MainFOHandler(); 
        this.mainFOHandler.startDocument();
        this.delegate = this.mainFOHandler;
    }

    /**
     * SAX Handler for the end of the document
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        this.delegate.endDocument();
        rootFObj = null;
        if (log.isDebugEnabled()) {
            log.debug("Parsing of document complete");
        }
        foEventHandler.endDocument();
        
        //Notify the image factory that this user agent has expired.
        ImageFactory imageFactory = userAgent.getFactory().getImageFactory();
        imageFactory.removeContext(this.userAgent);
    }

    /**
     * SAX Handler for the start of an element
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        this.depth++;
        delegate.startElement(namespaceURI, localName, rawName, attlist);
    }

    /**
     * SAX Handler for the end of an element
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String rawName)
                throws SAXException {
        this.delegate.endElement(uri, localName, rawName);
        this.depth--;
        if (depth == 0) {
            if (delegate != mainFOHandler) {
                //Return from sub-handler back to main handler
                delegate.endDocument();
                delegate = mainFOHandler;
                delegate.endElement(uri, localName, rawName);
            }
        }
    }

    /**
     * Finds the Maker used to create node objects of a particular type
     * @param namespaceURI URI for the namespace of the element
     * @param localName name of the Element
     * @return the ElementMapping.Maker that can create an FO object for this element
     * @throws FOPException if a Maker could not be found for a bound namespace.
     */
    private Maker findFOMaker(String namespaceURI, String localName) throws FOPException {
        return elementMappingRegistry.findFOMaker(namespaceURI, localName, locator);
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
     * Main DefaultHandler implementation which builds the FO tree.
     */
    private class MainFOHandler extends DefaultHandler {
        
        /**
         * Current formatting object being handled
         */
        protected FONode currentFObj = null;

        /**
         * Current propertyList for the node being handled.
         */
        protected PropertyList currentPropertyList;

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

            try {
                foNode = fobjMaker.make(currentFObj);
                propertyList = foNode.createPropertyList(currentPropertyList, foEventHandler);
                foNode.processNode(localName, getEffectiveLocator(), attlist, propertyList);
                foNode.startOfNode();
            } catch (IllegalArgumentException e) {
                throw new SAXException(e);
            }

            ContentHandlerFactory chFactory = foNode.getContentHandlerFactory();
            if (chFactory != null) {
                ContentHandler subHandler = chFactory.createContentHandler();
                if (subHandler instanceof ObjectSource 
                        && foNode instanceof ObjectBuiltListener) {
                    ((ObjectSource)subHandler).setObjectBuiltListener((ObjectBuiltListener)foNode);
                }
                
                subHandler.startDocument();
                subHandler.startElement(namespaceURI, localName, rawName, attlist);
                depth = 1;
                delegate = subHandler;
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
                    throws SAXException {
            if (currentFObj == null) {
                throw new IllegalStateException(
                        "endElement() called for " + rawName 
                            + " where there is no current element.");
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

        public void endDocument() throws SAXException {
            currentFObj = null;
        }

        
        
    }
    
}

