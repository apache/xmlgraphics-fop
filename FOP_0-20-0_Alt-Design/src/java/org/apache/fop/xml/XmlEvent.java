/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.apache.fop.pool.Poolable;

import org.xml.sax.helpers.AttributesImpl;

/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event. The current version, while defining accessor methods,
 * leaves the component data of the event as protected.  The
 * <tt>XMLSerialHandler</tt> methods set the values directly.
 */

public class XmlEvent extends Poolable {

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

    // These are made protected specifically so the the values of individual
    // XmlEvent instances can be changed directly, on the assumption that
    // the basic XML events are unlikely to change.
    protected int type = NOEVENT;
    protected String chars;
    protected int uriIndex = Namespaces.DefAttrNSIndex;
    protected String localName;
    protected String qName;
    protected AttributesImpl attributes;
    protected Namespaces namespaces;
    
    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, the default attribute namespace,
     * and null references for all others except <i>namespaces</i>.
     * @param namespaces the object maintaing the URIs and their indices
     * @param sequence the sequence number of this event within its
     * namespace
     * @param uriIndex the namespace index
     */
    public XmlEvent (Namespaces namespaces, int sequence, int uriIndex ) {
        super(sequence);
        this.namespaces = namespaces;
        this.uriIndex = uriIndex;
    }

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     * @param namespaces the object maintaining the namespaces and their
     *         indices
     * @param sequence the next sequence number for this namespace
     * @param type the SAX type of the event
     * @param chars is null unless this is a SAX chars event
     * @param uriIndex the index of the event URI in <b>namespaces</b>
     * @param localName the name associated with the <b>uriIndex</b>
     * @param attributes the AttributesImpl containing the element
     *         attributes, if any
     */
    public XmlEvent(Namespaces namespaces, int sequence,
            int type, String chars, int uriIndex,
                    String localName, String qName,
                    AttributesImpl attributes)
    {
        super(sequence);
        this.namespaces = namespaces;
        this.type = type;
        this.chars = chars;
        this.uriIndex = uriIndex;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
    }

    /**
     * The cloning constructor takes a reference to an existing
     * <tt>XmlEvent</tt> object.  Only the <code>id</code> field is not
     * cloned, but set from the <code>sequence</code> parameter.
     * @param ev the event to clone
     * @param sequence the sequence number of the clone
     */
    public XmlEvent(XmlEvent ev, int sequence) {
        super(sequence);
        namespaces = ev.namespaces;
        type = ev.type;
        chars = ev.chars;
        uriIndex = ev.uriIndex;
        localName = ev.localName;
        qName = ev.qName;
        attributes = ev.attributes;
    }

    
    /**
     * @param type the SAX type of this event
     * @param chars in this event, if any
     * @param namespaces the object maintaing URIs and their indices
     */
    public XmlEvent(Namespaces namespaces, int sequence,
            int uriIndex, int type, String chars) {
        super(sequence);
        this.namespaces = namespaces;
        this.type = type;
        this.chars = chars;
        // N.B. CHARACTERS events have no namespace - they should always
        // belong to the DefAttrNSpace
        this.uriIndex = uriIndex;
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared event.
     */
    public Poolable clear() {
        type = NOEVENT;
        chars = null;
        uriIndex = 0;
        localName = null;
        qName = null;
        attributes = null;
        return this;
    }

    /**
     * Copy the fields of the argument event to this event.
     * Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is copied.
     * @param ev the event to copy.
     * @return this (copied) event.
     */
    public XmlEvent copyEvent(XmlEvent ev) {
        type = ev.type;
        chars = ev.chars;
        uriIndex = ev.uriIndex;
        localName = ev.localName;
        qName = ev.qName;
        attributes = ev.attributes;
        return this;
    }

    /**
     * Get the SAX type of this event
     * @return the type
     */
    public int getType() { return type; }
    
    /**
     * Set the SAX event type for this event
     * @param type to which to set this event
     */
    public void setType(int type) {
        if (type < MIN_XML_EV_TYPE || type > MAX_XML_EV_TYPE) {
            throw new IllegalArgumentException(
                    "XML event type out of range.");
        }
        this.type = type;
    }

    /**
     * Get the chars value of this event
     * @return the chars in this event
     */
    public String getChars() { return chars; }

    /**
     * Set the chars field of this event
     * @param chars to set
     */
    public void setChars(String chars) {
        this.chars = chars;
    }

    /**
     * Set the chars field for this event.   A new string is created from
     * the array passed.
     * @param ch array from which to set chars field
     * @param start index in the <tt>ch</tt> array
     * @param length number of characters to set
     */
    public void setChars(char[] ch, int start, int length) {
        chars = new String(ch, start, length);
    }

    /**
     * Get the URI of this event
     * @return the URI
     */
    public String getUri() { return namespaces.getIndexURI(uriIndex); }
    
    /**
     * Get the URI index of this event
     * @return the index of the event URI from <tt>namespaces</tt>
     */
    public int getUriIndex() { return uriIndex; }
    
    /**
     * Set the URI index of this event
     * @param uriIndex of the event URI
     */
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
     * @param qName - the qualified name.
     */
    public void setQName(String qName) {
        this.qName = qName;
    }

    /**
     * Get the <tt>AttributesImpl</tt> object associated with this event.
     * @return the <tt>AttributesImpl</tt> object.
     */
    public AttributesImpl getAttributes() { return attributes; }

    /**
     * Set the <tt>AttributesImpl</tt> object associated with this event.
     * @param attributes the attributes
     */
    public void setAttributes(AttributesImpl attributes) {
        this.attributes = attributes;
    }

    /**
     * Set the <tt>Namespaces</tt> object associated with this event.
     * @param namespaces  the Namespaces
     */
    public void setNamespaces(Namespaces namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the <tt>Namespaces</tt> object associated with this event.
     * @return the <tt>Namespaces</tt> object.
     */
    public Namespaces getNamespaces() { return namespaces; }

    /**
     * Illegal operation in superclass
     * @return the foType.  Never returns from superclass.
     */
    public int getFoType() {
        throw new UnsupportedOperationException
                                    ("getFoType not supported in XmlEvent");
    }

    /**
     * Illegal operation in superclass
     * @param type the FoType
     */
    public void setFoType(int type) {
        throw new UnsupportedOperationException
        ("setFoType not supported in XmlEvent");
    }
    
    /**
     * Illegal operation in superclass
     * @return the foxType.  Never returns from superclass.
     */
    public int getFoxType() {
        throw new UnsupportedOperationException
        ("getFoxType not supported in XmlEvent");
    }

    /**
     * Illegal operation in superclass
     * @param type the FoxType
     */
    public void setFoxType(int type) {
        throw new UnsupportedOperationException
        ("setFoxType not supported in XmlEvent");
    }
    
    /**
     * Illegal operation in superclass
     * @return the svgType.  Never returns from superclass.
     */
    public int getSvgType() {
        throw new UnsupportedOperationException
        ("getSvgType not supported in XmlEvent");
    }

    /**
     * Illegal operation in superclass
     * @param type the svgType
     */
    public void setSvgType(int type) {
        throw new UnsupportedOperationException
        ("setSvgType not supported in XmlEvent");
    }
    
    public String toString() {
        String tstr;
        tstr = eventTypeName(type);
        tstr = tstr + "\nSeq " + id;
        tstr = tstr + "\nNamespaces " + namespaces.hashCode();
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
