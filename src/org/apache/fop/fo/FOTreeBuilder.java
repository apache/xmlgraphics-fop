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
import org.apache.fop.system.BufferManager;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.extensions.ExtensionObj;

import org.apache.log.Logger;

// SAX
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;

// Java
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
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
public class FOTreeBuilder extends DefaultHandler implements TreeBuilder {

    /**
     * table mapping element names to the makers of objects
     * representing formatting objects
     */
    protected HashMap fobjTable = new HashMap();

    protected Vector namespaces = new Vector();

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

    public BufferManager bufferManager;

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

    public FOTreeBuilder() {}

    public void setLogger(Logger logger) {
        log = logger;
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
        this.namespaces.addElement(namespaceURI.intern());
    }

    /**
     * add a mapping from element name to maker.
     *
     * @param namespaceURI namespace URI of formatting object element
     * @param localName local name of formatting object element
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
            currentFObj.addCharacters(data, start, start + length);
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

    /**
     * SAX Handler for the start of an element
     */
    public void startElement(String uri, String localName, String rawName,
                             Attributes attlist) throws SAXException {
        /* the formatting object started */
        FObj fobj;

        /* the maker for the formatting object started */
        FObj.Maker fobjMaker;

        // String fullName = mapName(rawName);
        //String fullName = uri + "^" + localName;
        HashMap table = (HashMap)fobjTable.get(uri);
        fobjMaker = (FObj.Maker)table.get(localName);
        PropertyListBuilder currentListBuilder =
            (PropertyListBuilder)this.propertylistTable.get(uri);

        boolean foreignXML = false;
        if (fobjMaker == null) {
            String fullName = uri + "^" + localName;
            if (!this.unknownFOs.containsKey(fullName)) {
                this.unknownFOs.put(fullName, "");
                log.error("Unknown formatting object "
                                       + fullName);
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
                    throw new FOPException("Invalid XML or missing namespace");
                }
                list = currentFObj.properties;
            }
            fobj = fobjMaker.make(currentFObj, list);
            fobj.setLogger(log);
        } catch (FOPException e) {
            throw new SAXException(e);
        }

        if (rootFObj == null) {
            rootFObj = fobj;
            rootFObj.setBufferManager(this.bufferManager);
            if (!fobj.getName().equals("fo:root")) {
                throw new SAXException(new FOPException("Root element must"
                                                        + " be root, not "
                                                        + fobj.getName()));
            }
        } else if(!(fobj instanceof org.apache.fop.fo.pagination.PageSequence)) {
            currentFObj.addChild(fobj);
        }

        currentFObj = fobj;
    }

    /**
     * format this formatting object tree
     *
     * @param areaTree the area tree to format into
     */
/*    public void format(AreaTree areaTree) throws FOPException {
        log.info("formatting FOs into areas");
        this.bufferManager.readComplete();
        ((Root)this.rootFObj).format(areaTree);
    }
*/
    public void reset() {
        currentFObj = null;
        rootFObj = null;
        streamRenderer = null;
    }

    public boolean hasData() {
        return (rootFObj != null);
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

}
