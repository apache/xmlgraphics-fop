/*
 * $Id: XMLObj.java,v 1.13 2003/03/05 21:48:02 jeremias Exp $
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

// Java
import java.awt.geom.Point2D;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import javax.xml.parsers.DocumentBuilderFactory;

// FOP
import org.apache.fop.apps.FOPException;

/**
 * Abstract class modelling generic, non-XSL-FO XML objects. Such objects are
 * stored in a DOM.
 */
public abstract class XMLObj extends FONode {

    // temp reference for attributes
    private Attributes attr = null;
    /** DOM element representing this node */
    protected Element element;
    /** DOM document containing this node */
    protected Document doc;

    /**
     *
     * @param parent the parent formatting object
     */
    public XMLObj(FONode parent) {
        super(parent);
    }

    /**
     * @param str name of the element
     */
    public void setName(String str) {
        name = str;
    }

    /**
     * Store the attributes for this element
     * @param attlist Collection of attributes passed to us from the parser.
     * @throws FOPException for errors in the attributes
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        attr = attlist;
    }

    /**
     * @return DOM document representing this foreign XML
     */
    public Document getDOMDocument() {
        return doc;
    }

    public Point2D getDimension(Point2D view) {
         return null;
    }

    /**
     * @return string containing the namespace for this node
     */
    public abstract String getNameSpace();

    /**
     * @return string containing the namespace for this document (which is the
     * same namespace as for this node ??)
     */
    public String getDocumentNamespace() {
        return getNameSpace();
    }

    private static HashMap ns = new HashMap();
    static {
        ns.put("xlink", "http://www.w3.org/1999/xlink");
    }

    /**
     * Add an element to the DOM document
     * @param doc DOM document to which to add an element
     * @param parent the parent element of the element that is being added
     */
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

    /**
     * Add the top-level element to the DOM document
     * @param doc DOM document
     * @param svgRoot non-XSL-FO element to be added as the root of this document
     */
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

    /**
     * Create an empty DOM document
     * @return DOM document
     */
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

    /**
     * @param child FONode child that should be added to this node
     */
    protected void addChild(FONode child) {
        if (child instanceof XMLObj) {
            ((XMLObj)child).addElement(doc, element);
        } else {
            // in theory someone might want to embed some defined
            // xml (eg. fo) inside the foreign xml
            // they could use a different namespace
            getLogger().debug("Invalid element: " + child.getName() + " inside foreign xml markup");
        }
    }

    /**
     * Add parsed characters to this object
     * @param data array of characters contaning the text to add
     * @param start starting array element to add
     * @param length number of characters from the array to add
     */
    protected void addCharacters(char data[], int start, int length) {
        String str = new String(data, start, length - start);
        org.w3c.dom.Text text = doc.createTextNode(str);
        element.appendChild(text);
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveVisitor(this);
    }

}

