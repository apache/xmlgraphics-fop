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
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.datatypes.IDReferences;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class XMLObj extends FObj {

    protected String tagName;

    protected Element element;
    protected Document doc;

    /**
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public XMLObj(FObj parent, PropertyList propertyList, String tag,
                  String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
        tagName = tag;
    }

    public abstract String getNameSpace();

    protected static HashMap ns = new HashMap();

    public void addGraphic(Document doc, Element parent) {
        this.doc = doc;
        element = doc.createElementNS(getNameSpace(), tagName);

        if(this.properties instanceof DirectPropertyListBuilder.AttrPropertyList) {
            Attributes attr = ((DirectPropertyListBuilder.AttrPropertyList)this.properties).getAttributes();
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
        } else {
        }

        parent.appendChild(element);
    }

    public void buildTopLevel(Document doc, Element svgRoot) {
        // build up the info for the top level element
        if(this.properties instanceof DirectPropertyListBuilder.AttrPropertyList) {
            Attributes attr = ((DirectPropertyListBuilder.AttrPropertyList)this.properties).getAttributes();
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
        } else {
        }
    }

    public Document createBasicDocument() {
        doc = null;

        element = null;
        try {
            // DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            // String ns = GraphElementMapping.URI;
            // doc = impl.createDocument(ns, "graph", null);
            doc =
                javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element el = doc.createElement("graph");
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
        String str = new String(data, start, length);
        org.w3c.dom.Text text = doc.createTextNode(str);
        element.appendChild(text);
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     * @return the status of the layout
     */
    public int layout(Area area) throws FOPException {
        /* generate a warning */
        log.error("" + this.tagName + " outside foreign xml");

        /* return status */
        return Status.OK;
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

    public ArrayList getMarkerSnapshot(ArrayList snapshot) {
        return snapshot;
    }

    public void rollback(ArrayList snapshot) {}

    protected void setWritingMode() {}
}

