/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.extensions.ExtensionObj;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// SAX
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// Java
import java.util.HashMap;
import java.util.ArrayList;

/**
 * SAX Handler that builds the formatting object tree.
 * 
 * Modified by Mark Lillywhite mark-fop@inomial.com. Now uses
 * StreamRenderer to automagically render the document as
 * soon as it receives a page-sequence end-tag. Also,
 * calls methods to set up and shut down the renderer at
 * the beginning and end of the FO document. Finally,
 * supresses adding the PageSequence object to the Root,
 * since it is parsed immediately.
 */
public class FOTreeBuilder extends DefaultHandler implements TreeBuilder {

    /**
     * table mapping element names to the makers of objects
     * representing formatting objects
     */
    protected HashMap fobjTable = new HashMap();

    protected ArrayList namespaces = new ArrayList();

    /**
     * class that builds a property list for each formatting object
     */
    protected HashMap propertylistTable = new HashMap();

    /**
     * current formatting object being handled
     */
    protected FObj currentFObj = null;

    /**
     * the root of the formatting object tree
     */
    protected FObj rootFObj = null;

    /**
     * set of names of formatting objects encountered but unknown
     */
    protected HashMap unknownFOs = new HashMap();

    /**
     *
     * The class that handles formatting and rendering to a stream
     * (mark-fop@inomial.com)
     */
    private StreamRenderer streamRenderer;

    private Logger log;

    private Locator locator;
    
    private int errorCount = 0;

    public FOTreeBuilder() {}

    public void setLogger(Logger logger) {
        log = logger;
    }

    
    
    public void setStreamRenderer(StreamRenderer streamRenderer) {
        this.streamRenderer = streamRenderer;
    }

    public StreamRenderer getStreamRenderer() {
        return this.streamRenderer;
    }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param maker Maker for class representing formatting object
     */
    public void addMapping(String namespaceURI, HashMap table) {
        this.fobjTable.put(namespaceURI, table);
        this.namespaces.add(namespaceURI.intern());
    }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param maker Maker for class representing formatting object
     */
    public void addPropertyList(String namespaceURI, HashMap list) {
        PropertyListBuilder plb;
        plb = (PropertyListBuilder)this.propertylistTable.get(namespaceURI);
        if (plb == null) {
            plb = new PropertyListBuilder();
            plb.addList(list);
            this.propertylistTable.put(namespaceURI, plb);
        } else {
            plb.addList(list);
        }
    }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param localName local name of formatting object element
     * @param maker Maker for class representing formatting object
     */
    public void addElementPropertyList(String namespaceURI, String localName,
                                       HashMap list) {
        PropertyListBuilder plb;
        plb = (PropertyListBuilder)this.propertylistTable.get(namespaceURI);
        if (plb == null) {
            plb = new PropertyListBuilder();
            plb.addElementList(localName, list);
            this.propertylistTable.put(namespaceURI, plb);
        } else {
            plb.addElementList(localName, list);
        }
    }

    public void addPropertyListBuilder(String namespaceURI,
                                       PropertyListBuilder propbuilder) {
        PropertyListBuilder plb;
        plb = (PropertyListBuilder)this.propertylistTable.get(namespaceURI);
        if (plb == null) {
            this.propertylistTable.put(namespaceURI, propbuilder);
        } else {
            // Error already added
        }
    }

    /**
     * SAX Handler for characters
     */
    public void characters(char data[], int start, int length) {
        if(currentFObj != null) {
            currentFObj.addCharacters(data, start, length);
        }
    }

    /**
     * SAX Handler for the end of an element
     */
    public void endElement(String uri, String localName, String rawName)
    throws SAXException {
        currentFObj.end();

        //
        // mark-fop@inomial.com - tell the stream renderer to render
        // this page-sequence
        //
        if(currentFObj instanceof PageSequence) {
            streamRenderer.render((PageSequence) currentFObj);
        } else if(currentFObj instanceof ExtensionObj) {
            if(!(currentFObj.getParent() instanceof ExtensionObj)) {
                streamRenderer.addExtension((ExtensionObj)currentFObj);
            }
        }

        currentFObj = (FObj)currentFObj.getParent();
    }

    /**
     * SAX Handler for the start of the document
     */
    public void startDocument()
    throws SAXException {
        rootFObj = null;    // allows FOTreeBuilder to be reused
        log.info("building formatting object tree");
        streamRenderer.startRenderer();
    }

    public void endDocument()
    throws SAXException {
        log.info("Parsing of document complete, stopping renderer");
        streamRenderer.stopRenderer();
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
    
    private String formatLocator(Locator locator) {
        if (locator == null) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            if (locator.getSystemId() != null) {
                sb.append(locator.getSystemId());
            } else if (locator.getPublicId() != null) {
                sb.append(locator.getPublicId());
            } else {
                sb.append("Unknown source");
            }
            sb.append(" (line: ");
            sb.append(locator.getLineNumber());
            sb.append(", col: ");
            sb.append(locator.getColumnNumber());
            sb.append(")");
            return sb.toString();
        }
    }
    
    /**
     * SAX Handler for the start of an element
     */
    public void startElement(String uri, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        /* the formatting object started */
        FObj fobj;

        /* the maker for the formatting object started */
        FObj.Maker fobjMaker = null;

        /* look up Maker for the element */
        HashMap table = (HashMap)fobjTable.get(uri);
        if(table != null) {
            fobjMaker = (FObj.Maker)table.get(localName);
        }

        PropertyListBuilder currentListBuilder =
            (PropertyListBuilder)this.propertylistTable.get(uri);

        boolean foreignXML = false;
        String systemId=null;
        int line = -1;
        int column = -1;
        if (locator!=null) {
            systemId = locator.getSystemId();
            line = locator.getLineNumber();
            column = locator.getColumnNumber();
        }
        if (fobjMaker == null) {
            String fullName = uri + "^" + localName;
            if (!this.unknownFOs.containsKey(fullName)) {
                this.unknownFOs.put(fullName, "");
                StringBuffer sb = new StringBuffer(128);
                sb.append("Unsupported element encountered: ");
                sb.append(localName);
                sb.append(" (Namespace: ");
                sb.append("".equals(uri) ? "default" : uri);
                sb.append("). ");
                sb.append("Source context: ");
                if (locator != null) {
                    sb.append(formatLocator(locator));
                } else {
                    sb.append("unavailable");
                }
                log.error(sb.toString());
                if (this.errorCount == 0) {
                    log.error("Expected XSL-FO (root, page-sequence, etc.), "
                        + "SVG (svg, rect, etc.) or elements from another "
                        + "supported language.");
                }
                this.errorCount++;
            }
            if(namespaces.contains(uri.intern())) {
                // fall back
                fobjMaker = new Unknown.Maker();
            } else {
                fobjMaker = new UnknownXMLObj.Maker(uri, localName);
                foreignXML = true;
            }
        }

        try {
            PropertyList list = null;
            if (currentListBuilder != null) {
                list =
                    currentListBuilder.makeList(uri, localName, attlist,
                                                (currentFObj == null) ? null
                                                : currentFObj.properties, currentFObj);
            } else if(foreignXML) {
                list = new DirectPropertyListBuilder.AttrPropertyList(attlist);
            } else {
                if(currentFObj == null) {
                    throw new FOPException("Invalid XML or missing namespace",
                                           systemId, line, column);
                }
                list = currentFObj.properties;
            }
            fobj = fobjMaker.make(currentFObj, list, systemId, line, column);
            fobj.setLogger(log);
        } catch (FOPException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            rootFObj = fobj;
            if (!fobj.getName().equals("fo:root")) {
                if (fobj.getName().equals("root")) {
                    throw new SAXException(new FOPException(
                        "Root element is missing the namespace declaration: "
                        + "http://www.w3.org/1999/XSL/Format",
                        systemId, line, column));
                } else {
                    throw new SAXException(new FOPException(
                        "Root element must be root, not "
                        + fobj.getName(), systemId, line, column));
                }
            }
        } else if(!(fobj instanceof org.apache.fop.fo.pagination.PageSequence)) {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    public void reset() {
        currentFObj = null;
        rootFObj = null;
        streamRenderer = null;
        this.errorCount = 0;
    }

    public boolean hasData() {
        return (rootFObj != null);
    }

}
