/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.datatypes.IDReferences;

import org.w3c.dom.*;
import org.xml.sax.Attributes;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.*;
import java.awt.geom.Point2D;

/**
 * Generic XML object.
 * This is used by xml objects (other than fo) than will build a DOM
 * with each element.
 */
public abstract class XMLObj extends FONode {

    // temp reference for attributes
    Attributes attr = null;

    protected Element element;
    protected Document doc;

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLObj(FONode parent) {
        super(parent);
    }

    public void setName(String str) {
        name = str;
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        attr = attlist;
    }

    public Document getDocument() {
        return doc;
    }

    public Point2D getDimension(Point2D view) {
         return null;
    }

    public abstract String getNameSpace();

    public String getDocumentNamespace() {
        return getNameSpace();
    }

    protected static HashMap ns = new HashMap();
    static {
        ns.put("xlink", "http://www.w3.org/1999/xlink");
    }

    public void addElement(Document doc, Element parent) {
        this.doc = doc;
        element = doc.createElementNS(getNameSpace(), name);

        for (int count = 0; count < attr.getLength(); count++) {
            String rf = attr.getValue(count);
            String qname = attr.getQName(count);
            int idx = qname.indexOf(":");
            if (idx == -1) {
                element.setAttribute(qname, rf);
            } else {
                String pref = qname.substring(0, idx);
                String tail = qname.substring(idx + 1);
                if (pref.equals("xmlns")) {
                    ns.put(tail, rf);
                } else {
                    element.setAttributeNS((String)ns.get(pref), tail, rf);
                }
            }
        }
        attr = null;
        parent.appendChild(element);
    }

    public void buildTopLevel(Document doc, Element svgRoot) {
        // build up the info for the top level element
        for (int count = 0; count < attr.getLength(); count++) {
            String rf = attr.getValue(count);
            String qname = attr.getQName(count);
            int idx = qname.indexOf(":");
            if (idx == -1) {
                element.setAttribute(qname, rf);
            } else {
                String pref = qname.substring(0, idx);
                String tail = qname.substring(idx + 1);
                if (pref.equals("xmlns")) {
                    ns.put(tail, rf);
                } else {
                    element.setAttributeNS((String)ns.get(pref), tail, rf);
                }
            }
        }
    }

    public Document createBasicDocument() {
        doc = null;

        element = null;
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            doc = fact.newDocumentBuilder().newDocument();
            Element el = doc.createElement(name);
            doc.appendChild(el);

            element = doc.getDocumentElement();
            buildTopLevel(doc, element);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    protected void addChild(FONode child) {
        if (child instanceof XMLObj) {
            ((XMLObj)child).addElement(doc, element);
        } else {
            // in theory someone might want to embed some defined
            // xml (eg. fo) inside the foreign xml
            // they could use a different namespace
            log.debug("Invalid element: " + child.getName() + " inside foreign xml markup");
        }
    }

    protected void addCharacters(char data[], int start, int length) {
        String str = new String(data, start, length - start);
        org.w3c.dom.Text text = doc.createTextNode(str);
        element.appendChild(text);
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     * @return the status of the layout
     */
    public Status layout(Area area) throws FOPException {
        /* generate a warning */
        log.warn("" + this.name + " outside foreign xml");

        /* return status */
        return new Status(Status.OK);
    }
}

