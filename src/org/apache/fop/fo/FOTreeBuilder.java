/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StructureHandler;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// SAX
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

// Java
import java.util.*;

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
    protected Map fobjTable = new HashMap();

    /**
     * Set of mapped namespaces.
     */
    protected Set namespaces = new HashSet();

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

    public FOTreeBuilder() {}

    public Logger getLogger() {
        return userAgent.getLogger();
    }

    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    public FOUserAgent getUserAgent() {
        return userAgent;
    }

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
     */
    public void characters(char data[], int start, int length) {
        if(currentFObj != null) {
            currentFObj.addCharacters(data, start, start + length);
        }
    }

    /**
     * SAX Handler for the end of an element
     */
    public void endElement(String uri, String localName, String rawName)
    throws SAXException {
        currentFObj.end();
        currentFObj = currentFObj.getParent();
    }

    /**
     * SAX Handler for the start of the document
     */
    public void startDocument()
    throws SAXException {
        rootFObj = null;    // allows FOTreeBuilder to be reused
        if (getLogger().isDebugEnabled())
            getLogger().debug("Building formatting object tree");
        structHandler.startDocument();
    }

    /**
     * SAX Handler for the end of the document
     */
    public void endDocument()
    throws SAXException {
        if (getLogger().isDebugEnabled())
            getLogger().debug("Parsing of document complete");
        structHandler.endDocument();
    }

    /**
     * SAX Handler for the start of an element
     */
    public void startElement(String uri, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        /* the formatting object started */
        FONode fobj;

        /* the maker for the formatting object started */
        ElementMapping.Maker fobjMaker = null;

        Map table = (Map)fobjTable.get(uri);
        if(table != null) {
            fobjMaker = (ElementMapping.Maker)table.get(localName);
            // try default
            if(fobjMaker == null) {
                fobjMaker = (ElementMapping.Maker)table.get(ElementMapping.DEFAULT);
            }
        }

        if (fobjMaker == null) {
            if (getLogger().isWarnEnabled())
                getLogger().warn("Unknown formatting object " + uri + "^" + localName);
            if(namespaces.contains(uri.intern())) {
                // fall back
                fobjMaker = new Unknown.Maker();
            } else {
                fobjMaker = new UnknownXMLObj.Maker(uri);
            }
        }

        try {
            fobj = fobjMaker.make(currentFObj);
            fobj.setName(localName);
            // set the user agent for resolving user agent values
            fobj.setUserAgent(userAgent);
            // set the stream renderer so that appropriate
            // elements can add pages and handle resolving references
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

    public void reset() {
        currentFObj = null;
        rootFObj = null;
        structHandler = null;
    }

    public boolean hasData() {
        return (rootFObj != null);
    }
}
