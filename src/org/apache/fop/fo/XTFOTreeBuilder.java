/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.AreaTree;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.pagination.Root;

// SAX
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.DocumentHandler;
import org.xml.sax.AttributeList;

// Java
import java.util.Hashtable;
import java.util.Stack;
import java.io.IOException;

/**
 * SAX Handler that builds the formatting object tree.
 */
// public class XTFOTreeBuilder extends HandlerBase {
public class XTFOTreeBuilder extends FOTreeBuilder
    implements DocumentHandler {

    // namespace implementation ideas pinched from John Cowan
    protected static class NSMap {
        String prefix;
        String uri;
        int level;

        NSMap(String prefix, String uri, int level) {
            this.prefix = prefix;
            this.uri = uri;
            this.level = level;
        }

    }

    protected int level = 0;
    protected String m_uri = null;
    protected String m_localPart = null;
    protected Stack namespaceStack = new Stack();

    {
        namespaceStack.push(new NSMap("xml",
                                      "http://www.w3.org/XML/1998/namespace",
                                      -1));
        namespaceStack.push(new NSMap("", "", -1));
    }

    protected String findURI(String prefix) {
        for (int i = namespaceStack.size() - 1; i >= 0; i--) {
            NSMap nsMap = (NSMap)(namespaceStack.elementAt(i));
            if (prefix.equals(nsMap.prefix))
                return nsMap.uri;
        }
        return null;
    }

    protected String mapName(String name) throws SAXException {

        int colon = name.indexOf(':');
        String prefix = "";
        m_localPart = name;
        if (colon != -1) {
            prefix = name.substring(0, colon);
            m_localPart = name.substring(colon + 1);
        }
        m_uri = findURI(prefix);
        if (m_uri == null) {
            if (prefix.equals("")) {
                return name;
            } else {
                throw new SAXException(new FOPException("Unknown namespace prefix "
                                                        + prefix));
            }
        }
        return m_uri + "^" + m_localPart;
    }

    /**
     * SAX1 Handler for the end of an element
     */
    public void endElement(String rawName) throws SAXException {
        mapName(rawName);
        super.endElement(m_uri, m_localPart, rawName);
        level--;
        while (((NSMap)namespaceStack.peek()).level > level) {
            namespaceStack.pop();
        }
    }

    /**
     * SAX1 Handler for the start of an element
     */
    public void startElement(String rawName,
                             AttributeList attlist) throws SAXException {

        // SAX2 version of AttributeList
        AttributesImpl newAttrs = new AttributesImpl();

        level++;
        int length = attlist.getLength();
        for (int i = 0; i < length; i++) {
            String att = attlist.getName(i);
            if (att.equals("xmlns")) {
                namespaceStack.push(new NSMap("", attlist.getValue(i),
                                              level));
            } else if (att.startsWith("xmlns:")) {
                String value = attlist.getValue(i);
                namespaceStack.push(new NSMap(att.substring(6), value,
                                              level));
            } else {
                mapName(att);
                newAttrs.addAttribute(m_uri, m_localPart, att,
                                      attlist.getType(i),
                                      attlist.getValue(i));
            }

        }

        mapName(rawName);
        super.startElement(m_uri, m_localPart, rawName, newAttrs);
    }

}
