/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.LinkSet;

import org.w3c.dom.*;

import java.util.*;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class XMLObj extends FObj {

    protected String tagName = "";
    protected String[] props = {};

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLObj(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
    }

    public abstract String getNameSpace();

    protected static Hashtable ns = new Hashtable();

    public void addGraphic(Document doc, Element parent) {
        Element element = doc.createElementNS(getNameSpace(), tagName);
        // Element element = doc.createElement(tagName);
        for (int count = 0; count < props.length; count++) {
            if (this.properties.get(props[count]) != null) {
                String rf = this.properties.get(props[count]).getString();
                if (rf != null) {
                    if (props[count].indexOf(":") == -1) {
                        element.setAttribute(props[count], rf);
                    } else {
                        String pref =
                            props[count].substring(0,
                                                   props[count].indexOf(":"));
                        if (pref.equals("xmlns")) {
                            ns.put(props[count].substring(props[count].indexOf(":")
                                                          + 1), rf);
                        }
                        ns.put("xlink", "http://www.w3.org/1999/xlink");
                        element.setAttributeNS((String)ns.get(pref),
                                               props[count], rf);
                    }
                }
            }
        }
        parent.appendChild(element);
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            Object child = children.elementAt(i);
            if (child instanceof XMLObj) {
                ((XMLObj)child).addGraphic(doc, element);
            } else if (child instanceof String) {
                org.w3c.dom.Text text = doc.createTextNode((String)child);
                element.appendChild(text);
            }
        }
    }

    public void buildTopLevel(Document doc, Element svgRoot) {
        // build up the info for the top level element
        for (int count = 0; count < props.length; count++) {
            if (this.properties.get(props[count]) != null) {
                String rf = this.properties.get(props[count]).getString();
                if (rf != null)
                    svgRoot.setAttributeNS(null, props[count], rf);
            }
        }
        // doc.appendChild(topLevel);
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            Object child = children.elementAt(i);
            if (child instanceof XMLObj) {
                ((XMLObj)child).addGraphic(doc, svgRoot);
            } else if (child instanceof String) {
                org.w3c.dom.Text text = doc.createTextNode((String)child);
                svgRoot.appendChild(text);
            }
        }
    }

    public Document createBasicDocument() {
        Document doc = null;

        Element svgRoot = null;
        try {
            // DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            // String ns = GraphElementMapping.URI;
            // doc = impl.createDocument(ns, "graph", null);
            doc =
                javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element el = doc.createElement("graph");
            doc.appendChild(el);

            svgRoot = doc.getDocumentElement();
            buildTopLevel(doc, svgRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     * @return the status of the layout
     */
    public Status layout(Area area) throws FOPException {
        /* generate a warning */
        System.err.println("WARNING: " + this.name + " outside foreign xml");

        /* return status */
        return new Status(Status.OK);
    }

    /**
     * These method overrides prevent problems with the different types.
     */
    public void setIsInTableCell() {}

    public void forceStartOffset(int offset) {}

    public void forceWidth(int width) {}

    public void resetMarker() {}

    public void setLinkSet(LinkSet linkSet) {}

    public Vector getMarkerSnapshot(Vector snapshot) {
        return snapshot;
    }

    public void rollback(Vector snapshot) {}

}

