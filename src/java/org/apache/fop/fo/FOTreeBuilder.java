/*
 * $Id: FOTreeBuilder.java,v 1.43 2003/03/05 21:48:01 jeremias Exp $
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.ElementMapping.Maker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
public class FOTreeBuilder extends DefaultHandler {

    /**
     * Table mapping element names to the makers of objects
     * representing formatting objects.
     */
    protected Map fobjTable = new java.util.HashMap();

    /**
     * Set of mapped namespaces.
     */
    protected Set namespaces = new java.util.HashSet();

    /**
     * Current formatting object being handled
     */
    protected FONode currentFObj = null;

    /**
     * The root of the formatting object tree
     */
    protected FONode rootFObj = null;

    /**
     * The class that handles formatting and rendering to a stream
     * (mark-fop@inomial.com)
     */
    private StructureHandler structHandler;

    private FOUserAgent userAgent;

    /**
     * Default constructor
     */
    public FOTreeBuilder() {
    }

    private Logger getLogger() {
        return userAgent.getLogger();
    }

    /**
     * Sets the user agent
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    private FOUserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the structure handler to receive events.
     * @param sh StructureHandler instance
     */
    public void setStructHandler(StructureHandler sh) {
        this.structHandler = sh;
    }

    /**
     * Adds a mapping from a namespace to a table of makers.
     *
     * @param namespaceURI namespace URI of formatting object elements
     * @param table table of makers
     */
    public void addMapping(String namespaceURI, HashMap table) {
        this.fobjTable.put(namespaceURI, table);
        this.namespaces.add(namespaceURI.intern());
    }

    /**
     * SAX Handler for characters
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char data[], int start, int length) {
        if (currentFObj != null) {
            currentFObj.addCharacters(data, start, start + length);
        }
    }

    /**
     * SAX Handler for the end of an element
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String rawName)
                throws SAXException {
        currentFObj.end();
        currentFObj = currentFObj.getParent();
    }

    /**
     * SAX Handler for the start of the document
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        rootFObj = null;    // allows FOTreeBuilder to be reused
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Building formatting object tree");
        }
        structHandler.startDocument();
    }

    /**
     * SAX Handler for the end of the document
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        rootFObj = null;
        currentFObj = null;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Parsing of document complete");
        }
        structHandler.endDocument();
    }

    /**
     * SAX Handler for the start of an element
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        /* the formatting object started */
        FONode fobj;

        /* the maker for the formatting object started */
        ElementMapping.Maker fobjMaker = findFOMaker(namespaceURI, localName);

        try {
            fobj = fobjMaker.make(currentFObj);
            fobj.setName(localName);
            // set the user agent for resolving user agent values
            fobj.setUserAgent(userAgent);
            // set the structure handler so that appropriate
            // elements can signal structure events
            fobj.setStructHandler(structHandler);

            fobj.handleAttrs(attlist);
        } catch (FOPException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            if (!fobj.getName().equals("fo:root")) {
                throw new SAXException(new FOPException("Root element must"
                                                        + " be fo:root, not "
                                                        + fobj.getName()));
            }
            rootFObj = fobj;
        } else {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    /**
     * Finds the Maker used to create FO objects of a particular type
     * @param namespaceURI URI for the namespace of the element
     * @param localName name of the Element
     * @return the ElementMapping.Maker that can create an FO object for this element
     */
    public Maker findFOMaker(String namespaceURI, String localName) {
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
          if (getLogger().isWarnEnabled()) {
              getLogger().warn("Unknown formatting object " + namespaceURI + "^" + localName);
          }
          if (namespaces.contains(namespaceURI.intern())) {
              // fall back
              fobjMaker = new Unknown.Maker();
          } else {
              fobjMaker = new UnknownXMLObj.Maker(namespaceURI);
          }
      }
      return fobjMaker;
    }

    /**
     * Resets this object for another run.
     */
    public void reset() {
        currentFObj = null;
        rootFObj = null;
        structHandler = null;
    }

    /**
     * Indicates if data has been processed.
     * @return True if data has been processed
     */
    public boolean hasData() {
        return (rootFObj != null);
    }
}
