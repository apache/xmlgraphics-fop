
package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.datastructs.SyncedCircularBuffer;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.xml.sax.helpers.AttributesImpl;

/*
 * XMLEvent.java
 *
 * Created: Thu Nov  8 13:38:17 2001
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event. The current version, while defining accessor methods,
 * leaves the component data of the event as public.  The
 * <tt>XMLSerialHandler</tt> methods set the values directly.
 * This is Very Bad.
 */

public class XMLEvent {
    public static final int NOEVENT = 0;

    public static final int STARTDOCUMENT = 1;
    public static final int ENDDOCUMENT = 2;
    public static final int STARTELEMENT = 3;
    public static final int ENDELEMENT = 4;
    public static final int CHARACTERS = 5;

    private static final int MIN_XML_EV_TYPE = NOEVENT;
    private static final int MAX_XML_EV_TYPE = CHARACTERS;

    // These are made public specifically so the the values of individual
    // XMLEvent instances can be changed directly, on the assumption that
    // the basic XML events are unlikely to change.
    public int type = NOEVENT;
    public String chars;
    public int uriIndex;
    public String localName;
    public String qName;
    public AttributesImpl attributes;

    public static final String DefAttrNSpace = "";
    public static final String XSLNamespace =
        "http://www.w3.org/1999/XSL/Format";
    public static final String SVGNamespace = "http://www.w3.org/2000/svg";
    public static final String XlinkNamespace =
        "http://www.w3.org/1999/xlink";
    public static final int DefAttrNSIndex = 0;
    public static final int XSLNSpaceIndex = 1;

    /**
     * A <tt>HashMap</tt> mapping a namespace URI to an <tt>int</tt>
     * index.  The HashMap is initialized with a few well-known URIs.  As
     * URIs are encountered in parsing, they are converted to an integer
     * index by lookup in this HashMap.  If the URI has not been seen, it
     * is added to the <i>uriIndices</i> and <i>uris</i> for future reference.
     * <p>
     * This is a <tt>private static</tt>
     * maintained across all documents that may be processed in a single
     * invocation. <b>It is vital that no URI, once added, ever be deleted
     * from this <tt>HashMap</tt></b>.
     * <p>
     * <tt>HashMap</> is unsynchronized, so accesses and updates must be
     * protected.
     * <p>
     * Updates will be very rare, and accesses are directly related to the
     * number of elements (and attributes) encountered.
     */
    private static HashMap uriIndices = new HashMap(4);
    /**
     * A <tt>ArrayList</tt> of namespace URIs.  Effectively, a mapping of
     * an <tt>int</tt> index onto a URI.
     * ArrayList is initialized with a few well-known URIs.  As
     * URIs are encountered in parsing, they are converted to an integer
     * index by lookup in the <i>uriIndices</i> Hashmap. If the URI has not
     * been seen, it is added to <i>uriIndices</i> and <i>uris</i>
     * for future reference.
     * <p>
     * This is a <tt>private static</tt>
     * maintained across all documents that may be processed in a single
     * invocation.
     * <p>
     * <tt>ArrayList</> is unsynchronized, so access and updates must be
     * protected.  Both will be more rare than accesses to <i>uriIndices</i>.
     */
    private static ArrayList uris = new ArrayList(4);
    static {
        uriIndices.put(DefAttrNSpace, Ints.consts.get(DefAttrNSIndex));
        uris.add(DefAttrNSIndex, DefAttrNSpace);
        uriIndices.put(XSLNamespace, Ints.consts.get(XSLNSpaceIndex));
        uris.add(XSLNSpaceIndex, XSLNamespace);
        uriIndices.put(SVGNamespace, Ints.consts.get(2));
        uris.add(2, SVGNamespace);
        uriIndices.put(XlinkNamespace, Ints.consts.get(3));
        uris.add(3, XlinkNamespace);
    }

    /**
     * @return size of the <tt>uris</tt> <ttArrayList</tt>.
     */
    public static int getUrisSize() {
        return uris.size();
    }

    /**
     * If the URI is not pre-defined, and has not been seen before, add
     * it to the stored namespaces, and return the index.
     * @param uri the namespace uri
     * @return integer index of the namespace URI
     */
    public static int getURIIndex(String uri) {
        synchronized (uriIndices) {
            int i;
            Integer intg = (Integer)uriIndices.get(uri);
            if (intg == null) {
                // update the indices
                i = uris.size();
                uriIndices.put(uri, Ints.consts.get(i));
                return i;
            }
            // not null - found the integer
            return intg.intValue();
        }
    }

    /**
     * @param index the integer index of the namespace URI
     * @return the corresponding namespace URI
     */
    public static String getIndexURI(int index) {
        synchronized (uriIndices) {
            return (String)uris.get(index);
        }
    }

    /**
     * @param events the buffer in which events have been stored
     * @return next event from the SyncedCircularBuffer
     * @exception FOPException.  The purpose of this method is to catch
     * and transform any InterruptedException exceptions thrown by the
     * <tt>SyncedCircularBuffer</tt>.
     */
    public static XMLEvent getEvent(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev;
        try {
            ev = (XMLEvent)events.get();
            //System.out.println("getEvent: " + ev);
            return ev;
        } catch (InterruptedException e) {
            throw new FOPException("InterruptedException");
        }
    }

    /**
     * @param events the buffer in which events have been stored
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getEndDocument(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null && ev.type != XMLEvent.ENDDOCUMENT) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException("EndDocument not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDDOCUMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectEndDocument(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null && ev.type == XMLEvent.ENDDOCUMENT) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException("EndDocument not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getStartElement(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null && ev.type != XMLEvent.STARTELEMENT) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException("StartElement not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectStartElement(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null && ev.type == XMLEvent.STARTELEMENT) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException("StartElement not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getStartElement
        (SyncedCircularBuffer events, String qName) throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null &&
               ! (ev.type == XMLEvent.STARTELEMENT
                  && ev.qName.equals(qName))) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("StartElement "+ qName + " not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectStartElement
        (SyncedCircularBuffer events, String qName) throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null &&
            (ev.type == XMLEvent.STARTELEMENT && ev.qName.equals(qName))) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException
                ("StartElement "+ qName + " not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getStartElement
        (SyncedCircularBuffer events, int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null &&
               ! (ev.type == XMLEvent.STARTELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("StartElement " + XMLEvent.getIndexURI(uriIndex)
                      + ":" + localName + " not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectStartElement
        (SyncedCircularBuffer events, int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null &&
            (ev.type == XMLEvent.STARTELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException
                    ("StartElement " + XMLEvent.getIndexURI(uriIndex)
                      + ":" + localName + " not found.");
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param events the buffer in which events have been stored
     * @param list a <tt>LinkedList</tt> containing either <tt>String</tt>s
     * with the <tt>QName</tt>, or <tt>UriLocalName</tt>
     * objects with the URI index and local name of one of the required
     * STARTELEMENT events.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getStartElement
        (SyncedCircularBuffer events, LinkedList list)
        throws FOPException
    {
        XMLEvent ev;
        do {
            try {
                ev = expectStartElement(events, list);
                return ev;
            } catch (NoSuchElementException e) {
                // keep trying
            }
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // getEvent() returns null, the expectStartElement() calls
            // will throw a NoSuchElementException
            ev = getEvent(events);
        } while (ev != null);
        // Exit from this while loop is only by discovery of null event
        throw new NoSuchElementException
                    ("StartElement from list not found.");
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param events the buffer in which events have been stored
     * @param list a <tt>LinkedList</tt> containing either <tt>String</tt>s
     * with the <tt>QName</tt>, or <tt>UriLocalName</tt>
     * objects with the URI index and local name of one of the required
     * STARTELEMENT events.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent expectStartElement
        (SyncedCircularBuffer events, LinkedList list)
        throws FOPException
    {
        XMLEvent ev;
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            Object o = elements.next();
            if (o instanceof String) {
                try {
                    ev = expectStartElement(events, (String) o);
                    // Found it!
                    return ev;
                } catch (NoSuchElementException e) {
                    // Keep trying
                }
            } else if (o instanceof UriLocalName) {
                try {
                    ev = expectStartElement
                            (events, ((UriLocalName) o).getUriIndex(),
                             ((UriLocalName) o).getLocalName());
                    // Found it!
                    return ev;
                } catch (NoSuchElementException e) {
                    // Keep trying
                }
            } else
                throw new FOPException
                        ("Invalid list elements for getStartElement");
        }
        throw new NoSuchElementException
                ("StartElement from list not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getEndElement(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null && ev.type != XMLEvent.ENDELEMENT) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException("EndElement not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectEndElement(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null && ev.type == XMLEvent.ENDELEMENT) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException("EndElement not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getEndElement
        (SyncedCircularBuffer events, String qName) throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT && ev.qName.equals(qName))) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement "+ qName + " not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectEndElement
        (SyncedCircularBuffer events, String qName) throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT && ev.qName.equals(qName))) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException
                ("EndElement "+ qName + " not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getEndElement
        (SyncedCircularBuffer events, int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement " + XMLEvent.getIndexURI(uriIndex)
                                   + ":" + localName + " not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectEndElement
        (SyncedCircularBuffer events, int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException
                    ("EndElement " + XMLEvent.getIndexURI(uriIndex)
                                   + ":" + localName + " not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getEndElement
        (SyncedCircularBuffer events, XMLEvent event)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(event.localName)
                  && ev.uriIndex == event.uriIndex)) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement " + XMLEvent.getIndexURI(event.uriIndex)
                                   + ":" + event.localName + " not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectEndElement
        (SyncedCircularBuffer events, XMLEvent event)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(event.localName)
                  && ev.uriIndex == event.uriIndex)) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException
                    ("EndElement " + XMLEvent.getIndexURI(event.uriIndex)
                                   + ":" + event.localName + " not found.");
    }

    /**
     * @param events the buffer in which events have been stored
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public static XMLEvent getCharacters(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        while (ev != null && ev.type != XMLEvent.CHARACTERS) {
            ev = getEvent(events);
        }
        if (ev == null) {
            throw new NoSuchElementException("Characters not found.");
        }
        return ev;
    }

    /**
     * @param events the buffer in which events have been stored
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if CHARACTERS is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public static XMLEvent expectCharacters(SyncedCircularBuffer events)
        throws FOPException
    {
        XMLEvent ev = getEvent(events);
        if (ev != null && ev.type == XMLEvent.CHARACTERS) {
            return ev;
        }
        events.pushBack(ev);
        throw new NoSuchElementException("Characters not found.");
    }

    /**
     * The no-argument constructor uses the default initialization values
     * of the data members: NOEVENT for the event type, and null references
     * for all others.
     */
    public XMLEvent (){}

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     */
    public XMLEvent(int type, String chars, int uriIndex,
                    String localName, String qName, AttributesImpl attributes)
    {
        this.type = type;
        this.chars = chars;
        this.uriIndex = uriIndex;
        this.localName = localName;
        this.qName = qName;
        this.attributes = attributes;
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

    public String getUri() { return getIndexURI(uriIndex); }
    public int getUriIndex() { return uriIndex; }
    public void setUriIndex(int uriIndex) {
        this.uriIndex = uriIndex;
    }

    public String getLocalName() { return localName; }
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getQName() { return qName; }

    public String getQNamePrefix() {
        int i;
        if ((i = qName.indexOf(':')) == -1) {
            return "";
        } else {
            return qName.substring(0, i);
        }
    }

    public void setQName(String QName) {
        this.qName = qName;
    }

    public AttributesImpl getAttributes() { return attributes; }
    public void setAttributes(AttributesImpl attributes) {
        this.attributes = attributes;
    }

    public String toString() {
        String tstr;
        switch (type) {
        case NOEVENT:
            tstr = "NOEVENT";
            break;
        case STARTDOCUMENT:
            tstr = "STARTDOCUMENT";
            break;
        case ENDDOCUMENT:
            tstr = "ENDDOCUMENT";
            break;
        case STARTELEMENT:
            tstr = "STARTELEMENT";
            break;
        case ENDELEMENT:
            tstr = "ENDELEMENT";
            break;
        case CHARACTERS:
            tstr = "CHARACTERS";
            break;
        default:
            tstr = "Unknown type " + type;
        }
        tstr = tstr + "\nURI "
                + uriIndex + " " + getIndexURI(uriIndex);
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

    /**
     * A nested class for holding an passing a URI index and local name
     * pair, as used in the contain <tt>XMLEvent</tt> class.
     */
    public static class UriLocalName {
        private int uriIndex;
        private String localName;

        public UriLocalName(int uriIndex, String localName) {
            this.uriIndex = uriIndex;
            this.localName = localName;
        }

        public int getUriIndex() {
            return uriIndex;
        }

        public String getLocalName() {
            return localName;
        }
    }

}
