package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;

import org.xml.sax.helpers.AttributesImpl;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event. The current version, while defining accessor methods,
 * leaves the component data of the event as protected.  The
 * <tt>XMLSerialHandler</tt> methods set the values directly.
 */

public class XMLEvent {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final int NOEVENT = 0;
    public static final int STARTDOCUMENT = 1;
    public static final int ENDDOCUMENT = 2;
    public static final int STARTELEMENT = 3;
    public static final int ENDELEMENT = 4;
    public static final int CHARACTERS = 5;

    private static final int MIN_XML_EV_TYPE = NOEVENT;
    private static final int MAX_XML_EV_TYPE = CHARACTERS;

    public static final boolean DISCARD_W_SPACE = true;
    public static final boolean RETAIN_W_SPACE = false;

    public static String eventTypeName(int type) {
        switch (type) {
        case NOEVENT:
            return "NOEVENT";
        case STARTDOCUMENT:
            return "STARTDOCUMENT";
        case ENDDOCUMENT:
            return "ENDDOCUMENT";
        case STARTELEMENT:
            return "STARTELEMENT";
        case ENDELEMENT:
            return "ENDELEMENT";
        case CHARACTERS:
            return "CHARACTERS";
        default:
            return "Unknown type " + type;
        }
    }

    // These are made public specifically so the the values of individual
    // XMLEvent instances can be changed directly, on the assumption that
    // the basic XML events are unlikely to change.
    protected int type = NOEVENT;
    protected String chars;
    protected int uriIndex;
    protected String localName;
    protected String qName;
    protected AttributesImpl attributes;
    protected XMLNamespaces namespaces;

    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     */
    public XMLEvent (XMLNamespaces namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     */
    public XMLEvent(int type, String chars, int uriIndex,
                    String localName, String qName,
                    AttributesImpl attributes, XMLNamespaces namespaces)
    {
        this.type = type;
        this.chars = chars;
        this.uriIndex = uriIndex;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
        this.namespaces = namespaces;
    }

    /**
     * The cloning constructor takes a reference to an existing
     * <tt>XMLEvent</tt> object.
     */
    public XMLEvent(XMLEvent ev) {
        type = ev.type;
        chars = ev.chars;
        uriIndex = ev.uriIndex;
        localName = ev.localName;
        qName = ev.qName;
        attributes = ev.attributes;
        namespaces = ev.namespaces;
    }

    public XMLEvent(int type, String chars, XMLNamespaces namespaces) {
        this.type = type;
        this.chars = chars;
        this.namespaces = namespaces;
    }

    public int getType() { return type; }
    public void setType(int type) {
        if (type < MIN_XML_EV_TYPE || type > MAX_XML_EV_TYPE) {
            throw new IllegalArgumentException(
                    "XML event type out of range.");
        }
        this.type = type;
    }

    public String getChars() { return chars; }

    public void setChars(String chars) {
        this.chars = chars;
    }

    public void setChars(char[] ch, int start, int length) {
        chars = new String(ch, start, length);
    }

    public String getUri() { return namespaces.getIndexURI(uriIndex); }
    public int getUriIndex() { return uriIndex; }
    public void setUriIndex(int uriIndex) {
        this.uriIndex = uriIndex;
    }

    /**
     * Get the local name of this event.
     * @return the local name.
     */
    public String getLocalName() { return localName; }

    /**
     * Set the local name of this event.
     * @param localName - the local name.
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    /**
     * Get the qualified name of this event.
     * @return the qualified name.
     */
    public String getQName() { return qName; }

    /**
     * Get the prefix of the qualified name of this evetn.
     * @return - the prefix.
     */
    public String getQNamePrefix() {
        int i;
        if ((i = qName.indexOf(':')) == -1) {
            return "";
        } else {
            return qName.substring(0, i);
        }
    }

    /**
     * Set the qualified name of this event.
     * @param QName - the qualified name.
     */
    public void setQName(String QName) {
        this.qName = qName;
    }

    /**
     * Get the <tt>AttributesImpl</tt> object associated with this event.
     * @return the <tt>AttributesImpl</tt> object.
     */
    public AttributesImpl getAttributes() { return attributes; }

    /**
     * Set the <tt>AttributesImpl</tt> object associated with this event.
     * @param the <tt>AttributesImpl</tt> object.
     */
    public void setAttributes(AttributesImpl attributes) {
        this.attributes = attributes;
    }

    /**
     * Get the <tt>XMLNamespaces</tt> object associated with this event.
     * @return the <tt>XMLNamespaces</tt> object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

    public String toString() {
        String tstr;
        tstr = eventTypeName(type);
        tstr = tstr + "\nURI "
                + uriIndex + " " + namespaces.getIndexURI(uriIndex);
        tstr = tstr + "\n" + "Local Name " + localName;
        tstr = tstr + "\n" + "QName " + qName;
        tstr = tstr + "\n" + "Chars <<<" + chars + ">>>";
        if (attributes == null) {
            tstr = tstr + "\n" + "Attributes null";
        } else {
            int len = attributes.getLength();
            tstr = tstr + "\n" + "No. of attributes " + len;
            for (int i = 0; i < len; i++) {
                tstr = tstr + "\n" + "  URI: " + attributes.getURI(i);
                tstr = tstr + "\n" + "  QName: " + attributes.getQName(i);
                tstr = tstr + "\n" + "  LocalName: "
                                   + attributes.getLocalName(i);
                tstr = tstr + "\n" + "  type: " + attributes.getType(i);
                tstr = tstr + "\n" + "  value: " + attributes.getValue(i);
            }
        }
        return tstr;
    }

}
