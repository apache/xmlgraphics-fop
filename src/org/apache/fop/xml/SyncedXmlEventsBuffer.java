package org.apache.fop.xml;

import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.apps.FOPException;

import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.Iterator;

/*
 * $Id$
 * 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A synchronized circular buffer for XMLEvents.
 * @see org.apache.fop.datastructs.SyncedCircularBuffer
 */
public class SyncedXmlEventsBuffer extends SyncedCircularBuffer {

    /**
     * Maintains an index of namespace URIs.  These can then be referred to
     * by an <tt>int</tt> index.
     */
    private XMLNamespaces namespaces;

    /**
     * No-argument constructor sets up a buffer with the default number of
     * elements.
     * The producer and consumer <tt>Thread</tt>s default to the current
     * thread at the time of instantiation.
     */
    public SyncedXmlEventsBuffer()
        throws IllegalArgumentException
    {
        super();
        namespaces = new XMLNamespaces();
    }

    /**
     * Constructor taking one argument; the size of the buffer.
     * @param size the size of the buffer.  Must be > 1.
     */
    public SyncedXmlEventsBuffer(int size)
        throws IllegalArgumentException
    {
        super(size);
        namespaces = new XMLNamespaces();
    }

    /**
     * Get the <tt>XMLNamespaces</tt> from this buffer.
     * @return - the namespaces object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

    /**
     * @return next event from the SyncedCircularBuffer
     * @exception FOPException.  The purpose of this method is to catch
     * and transform any InterruptedException exceptions thrown by the
     * <tt>SyncedCircularBuffer</tt>.
     */
    public XMLEvent getEvent() throws FOPException {
        XMLEvent ev;
        try {
            ev = (XMLEvent)get();
            //System.out.println("getEvent: " + ev);
            return ev;
        } catch (InterruptedException e) {
            throw new FOPException("InterruptedException");
        }
    }

    /**
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndDocument() throws FOPException {
        XMLEvent ev = getEvent();
        while (ev != null && ev.type != XMLEvent.ENDDOCUMENT) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException("EndDocument not found.");
        }
        return ev;
    }

    /**
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDDOCUMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndDocument() throws FOPException {
        XMLEvent ev = getEvent();
        if (ev != null && ev.type == XMLEvent.ENDDOCUMENT) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException("EndDocument not found.");
    }

    /**
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement() throws FOPException {
        XMLEvent ev = getEvent();
        while (ev != null && ev.type != XMLEvent.STARTELEMENT) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException("StartElement not found.");
        }
        return ev;
    }

    /**
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement() throws FOPException {
        XMLEvent ev = getEvent();
        if (ev != null && ev.type == XMLEvent.STARTELEMENT) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException("StartElement not found.");
    }

    /**
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement(String qName) throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == XMLEvent.STARTELEMENT
                  && ev.qName.equals(qName))) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("StartElement "+ qName + " not found.");
        }
        return ev;
    }

    /**
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement(String qName)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        if (ev != null &&
            (ev.type == XMLEvent.STARTELEMENT && ev.qName.equals(qName))) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                ("StartElement "+ qName + " not found.");
    }

    /**
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement(int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == XMLEvent.STARTELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("StartElement " + namespaces.getIndexURI(uriIndex)
                      + ":" + localName + " not found.");
        }
        return ev;
    }

    /**
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement(int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        if (ev != null &&
            (ev.type == XMLEvent.STARTELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                    ("StartElement " + namespaces.getIndexURI(uriIndex)
                      + ":" + localName + " not found.");
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param list a <tt>LinkedList</tt> containing either <tt>String</tt>s
     * with the <tt>QName</tt>, or <tt>UriLocalName</tt>
     * objects with the URI index and local name of one of the required
     * STARTELEMENT events.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement(LinkedList list)
        throws FOPException
    {
        XMLEvent ev;
        do {
            try {
                ev = expectStartElement(list);
                return ev;
            } catch (NoSuchElementException e) {
                // keep trying
            }
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // getEvent() returns null, the expectStartElement() calls
            // will throw a NoSuchElementException
            ev = getEvent();
        } while (ev != null);
        // Exit from this while loop is only by discovery of null event
        throw new NoSuchElementException
                    ("StartElement from list not found.");
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param list a <tt>LinkedList</tt> containing either <tt>String</tt>s
     * with the <tt>QName</tt>, or <tt>UriLocalName</tt>
     * objects with the URI index and local name of one of the required
     * STARTELEMENT events.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent expectStartElement(LinkedList list)
        throws FOPException
    {
        XMLEvent ev;
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            Object o = elements.next();
            if (o instanceof String) {
                try {
                    ev = expectStartElement((String) o);
                    // Found it!
                    return ev;
                } catch (NoSuchElementException e) {
                    // Keep trying
                }
            } else if (o instanceof XMLEvent.UriLocalName) {
                try {
                    ev = expectStartElement
                            (((XMLEvent.UriLocalName) o).getUriIndex(),
                             ((XMLEvent.UriLocalName) o).getLocalName());
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
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement() throws FOPException {
        XMLEvent ev = getEvent();
        while (ev != null && ev.type != XMLEvent.ENDELEMENT) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException("EndElement not found.");
        }
        return ev;
    }

    /**
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement() throws FOPException {
        XMLEvent ev = getEvent();
        if (ev != null && ev.type == XMLEvent.ENDELEMENT) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException("EndElement not found.");
    }

    /**
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(String qName) throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT && ev.qName.equals(qName))) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement "+ qName + " not found.");
        }
        return ev;
    }

    /**
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(String qName) throws FOPException
    {
        XMLEvent ev = getEvent();
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT && ev.qName.equals(qName))) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                ("EndElement "+ qName + " not found.");
    }

    /**
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement " + namespaces.getIndexURI(uriIndex)
                                   + ":" + localName + " not found.");
        }
        return ev;
    }

    /**
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(int uriIndex, String localName)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(localName)
                  && ev.uriIndex == uriIndex)) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                    ("EndElement " + namespaces.getIndexURI(uriIndex)
                                   + ":" + localName + " not found.");
    }

    /**
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(XMLEvent event) throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(event.localName)
                  && ev.uriIndex == event.uriIndex)) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                    ("EndElement " + namespaces.getIndexURI(event.uriIndex)
                                   + ":" + event.localName + " not found.");
        }
        return ev;
    }

    /**
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(XMLEvent event)
        throws FOPException
    {
        XMLEvent ev = getEvent();
        if (ev != null &&
            (ev.type == XMLEvent.ENDELEMENT
                  && ev.localName.equals(event.localName)
                  && ev.uriIndex == event.uriIndex)) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                    ("EndElement " + namespaces.getIndexURI(event.uriIndex)
                                   + ":" + event.localName + " not found.");
    }

    /**
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getCharacters() throws FOPException {
        XMLEvent ev = getEvent();
        while (ev != null && ev.type != XMLEvent.CHARACTERS) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException("Characters not found.");
        }
        return ev;
    }

    /**
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if CHARACTERS is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectCharacters() throws FOPException {
        XMLEvent ev = getEvent();
        if (ev != null && ev.type == XMLEvent.CHARACTERS) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException("Characters not found.");
    }

}
