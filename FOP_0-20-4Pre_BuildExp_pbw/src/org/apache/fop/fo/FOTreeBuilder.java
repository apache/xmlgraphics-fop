/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.AreaTree;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.StreamRenderer;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.extensions.ExtensionObj;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// SAX
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

// Java
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

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
     * table mapping element names to the makers of objects
     * representing formatting objects
     */
    protected HashMap fobjTable = new HashMap();

    protected ArrayList namespaces = new ArrayList();

    /**
     * current formatting object being handled
     */
    protected FONode currentFObj = null;

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
    private FOUserAgent userAgent;

    public FOTreeBuilder() {}

    public void setLogger(Logger logger) {
        log = logger;
    }

    public void setUserAgent(FOUserAgent ua) {
        userAgent = ua;
    }

    public FOUserAgent getUserAgent() {
        return userAgent;
    }


    public void setStreamRenderer(StreamRenderer streamRenderer) {
        this.streamRenderer = streamRenderer;
    }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param localName local name of formatting object element
     * @param maker Maker for class representing formatting object
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
        log.info("building formatting object tree");
        streamRenderer.startRenderer();
    }

    public void endDocument()
    throws SAXException {
        log.info("Parsing of document complete, stopping renderer");
        streamRenderer.stopRenderer();
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

        HashMap table = (HashMap)fobjTable.get(uri);
        if(table != null) {
            fobjMaker = (ElementMapping.Maker)table.get(localName);
            // try default
            if(fobjMaker == null) {
                fobjMaker = (ElementMapping.Maker)table.get(ElementMapping.DEFAULT);
            }
        }

        boolean foreignXML = false;
        if (fobjMaker == null) {
            String fullName = uri + "^" + localName;
            if (!this.unknownFOs.containsKey(fullName)) {
                this.unknownFOs.put(fullName, "");
                log.warn("Unknown formatting object "
                                       + fullName);
            }
            if(namespaces.contains(uri.intern())) {
                // fall back
                fobjMaker = new Unknown.Maker();
            } else {
                fobjMaker = new UnknownXMLObj.Maker(uri);
                foreignXML = true;
            }
        }

        try {
            fobj = fobjMaker.make(currentFObj);
            fobj.setName(localName);
            fobj.setLogger(log);
            // set the user agent for resolving user agent values
            fobj.setUserAgent(userAgent);
            // set the stream renderer so that appropriate
            // elements can add pages and handle resolving references
            fobj.setStreamRenderer(streamRenderer);

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
            rootFObj = (FObj)fobj;
        } else {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    public void reset() {
        currentFObj = null;
        rootFObj = null;
        streamRenderer = null;
    }

    public boolean hasData() {
        return (rootFObj != null);
    }

}
