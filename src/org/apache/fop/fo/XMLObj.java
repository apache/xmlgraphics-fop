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

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class XMLObj extends FObj {

    // temp reference for attributes
    Attributes attr = null;

    protected Element element;
    protected Document doc;

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLObj(FObj parent) {
        super(parent);
    }

    public void setName(String str) {
        name = str;
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        attr = attlist;
    }
    public abstract String getNameSpace();

    protected static Hashtable ns = new Hashtable();

    public void addGraphic(Document doc, Element parent) {
        this.doc = doc;
        element = doc.createElementNS(getNameSpace(), name);

            for (int count = 0; count < attr.getLength(); count++) {
                String rf = attr.getValue(count);
                String qname = attr.getQName(count);
                if (qname.indexOf(":") == -1) {
                    element.setAttribute(qname, rf);
                } else {
                    String pref =
                        qname.substring(0, qname.indexOf(":"));
                    if (pref.equals("xmlns")) {
                        ns.put(qname.substring(qname.indexOf(":")
                                                      + 1), rf);
                    }
                    ns.put("xlink", "http://www.w3.org/1999/xlink");
                    element.setAttributeNS((String)ns.get(pref),
                                           qname, rf);
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
                if (qname.indexOf(":") == -1) {
                    element.setAttribute(qname, rf);
                } else {
                    String pref =
                       qname.substring(0, qname.indexOf(":"));
                    if (pref.equals("xmlns")) {
                        ns.put(qname.substring(qname.indexOf(":")
                                                      + 1), rf);
                    }
                    ns.put("xlink", "http://www.w3.org/1999/xlink");
                    element.setAttributeNS((String)ns.get(pref),
                                           qname, rf);
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
            ((XMLObj)child).addGraphic(doc, element);
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
        log.error("" + this.name + " outside foreign xml");

        /* return status */
        return new Status(Status.OK);
    }

    public void removeID(IDReferences idReferences) {}

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

    protected void setWritingMode() {}
}

