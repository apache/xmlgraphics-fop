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
            System.out.println("getEvent: " + ev);
            return ev;
        } catch (InterruptedException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Get the next event of the given type from the buffer.  Discard
     * intervening events.
     * @param eventType - the <tt>int</tt> event type.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XMLEvent getTypedEvent(int eventType) throws FOPException {
        XMLEvent ev = getEvent();
        while (ev != null && ev.type != eventType) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType) + " not found.");
        }
        return ev;
    }

    /**
     * Get the next event of the given type and with the given <tt>QName</tt>
     * from the buffer.  Discard intervening events.
     * @param eventType - the <tt>int</tt> event type.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required element.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XMLEvent getTypedEvent(int eventType, String qName)
                throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == eventType && ev.qName.equals(qName))) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
            (XMLEvent.eventTypeName(eventType) + " " + qName + " not found.");
        }
        return ev;
    }

    /**
     * Get the next event of the given type, and with the given URI index and
     * local name, from the buffer.  Discard intervening events.
     * @param eventType - the <tt>int</tt> event type.
     * @param uriIndex - the <tt>int</tt> URI index maintained in the
     * <tt>XMLNamespaces</tt> object.
     * @param localName a <tt>String</tt> with the local name of the
     * required element.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XMLEvent getTypedEvent
                            (int eventType, int uriIndex, String localName)
                throws FOPException
    {
        XMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == eventType
                  && ev.type == eventType
                  && ev.localName.equals(localName))) {
            ev = getEvent();
        }
        if (ev == null)
            throw new NoSuchElementException
                    (XMLEvent.eventTypeName(eventType)
                             + namespaces.getIndexURI(uriIndex)
                                       + ":" + localName + " not found.");
        return ev;
    }

    /**
     * Return the next element if it is of the required type.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an event of the required type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the next
     * event detected is not of the required type.
     * The erroneous event is pushed back.
     */
    public XMLEvent expectTypedEvent(int eventType, boolean discardWhiteSpace)
                throws FOPException
    {
        XMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null && ev.type == eventType) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType) + " not found.");
    }

    /**
     * Return the next element if it is of the required type and has the
     * required <tt>QName</tt>.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required element.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an event of the required type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the next
     * event detected is not of the required type.
     * The erroneous event is pushed back.
     */
    public XMLEvent expectTypedEvent
                    (int eventType, String qName, boolean discardWhiteSpace)
                throws FOPException
    {
        XMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null && ev.type == eventType && ev.qName.equals(qName)) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
            (XMLEvent.eventTypeName(eventType) + " " + qName + " not found.");
    }

    /**
     * Return the next element if it is of the required type and has the
     * required URI index and local name.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param uriIndex - the <tt>int</tt> URI index.
     * @param localName a <tt>String</tt> with the local name of the
     * required element.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an event of the required type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the next
     * event detected is not of the required type.
     * The erroneous event is pushed back.
     */
    public XMLEvent expectTypedEvent
                            (int eventType, int uriIndex,
                                 String localName, boolean discardWhiteSpace)
                throws FOPException
    {
        XMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null
                && ev.type == eventType
                   && ev.uriIndex == uriIndex
                       && ev.localName.equals(localName)) {
            return ev;
        }
        pushBack(ev);
        throw new NoSuchElementException
                    (XMLEvent.eventTypeName(eventType)
                             + namespaces.getIndexURI(uriIndex)
                                       + ":" + localName + " not found.");
    }

    /**
     * Get the next ENDDOCUMENT event from the buffer.  Discard any other
     * events preceding the ENDDOCUMENT event.
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndDocument() throws FOPException {
        return getTypedEvent(XMLEvent.ENDDOCUMENT);
    }

    /**
     * Return the next element if it is an ENDDOCUMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDDOCUMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndDocument(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectTypedEvent(XMLEvent.ENDDOCUMENT, discardWhiteSpace);
    }

    /**
     * Get the next STARTELEMENT event from the buffer.  Discard any other
     * events preceding the STARTELEMENT event.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement() throws FOPException {
        return getTypedEvent(XMLEvent.STARTELEMENT);
    }

    /**
     * Return the next element if it is a STARTELEMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectTypedEvent(XMLEvent.STARTELEMENT, discardWhiteSpace);
    }

    /**
     * Get the next STARTELEMENT event with the given <tt>QName</tt>
     * from the buffer.  Discard any other events preceding the
     * STARTELEMENT event.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement(String qName) throws FOPException
    {
        return getTypedEvent(XMLEvent.STARTELEMENT, qName);
    }

    /**
     * Return the next element if it is a STARTELEMENT with the given
     * <tt>QName</tt>.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement
                                (String qName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
                        (XMLEvent.STARTELEMENT, qName, discardWhiteSpace);
    }

    /**
     * Get the next STARTELEMENT event with the given URI index and local name
     * from the buffer.  Discard any other events preceding the
     * STARTELEMENT event.
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
        return getTypedEvent(XMLEvent.STARTELEMENT, uriIndex, localName);
    }

    /**
     * Return the next element if it is a STARTELEMENT with the given
     * URI index and local name.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param uriIndex an <tt>int</tt> URI index.
     * @param localName a <tt>String</tt> with the local name of the
     * required STARTELEMENT
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectStartElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
            (XMLEvent.STARTELEMENT, uriIndex, localName, discardWhiteSpace);
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param list a <tt>LinkedList</tt> containing either <tt>String</tt>s
     * with the <tt>QName</tt>, or <tt>UriLocalName</tt>
     * objects with the URI index and local name of one of the required
     * STARTELEMENT events.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        do {
            try {
                ev = expectStartElement(list, discardWhiteSpace);
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
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent expectStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            Object o = elements.next();
            if (o instanceof String) {
                try {
                    ev = expectStartElement((String) o, discardWhiteSpace);
                    // Found it!
                    return ev;
                } catch (NoSuchElementException e) {
                    // Keep trying
                }
            } else if (o instanceof XMLEvent.UriLocalName) {
                try {
                    ev = expectStartElement
                            (((XMLEvent.UriLocalName) o).getUriIndex(),
                             ((XMLEvent.UriLocalName) o).getLocalName(),
                             discardWhiteSpace);
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
     * Get one of a array of possible STARTELEMENT events.  Scan and discard
     * events until a STARTELEMENT event is found whose URI index and
     * local name matches one of those in the argument
     * <tt>XMLEvent.UriLocalName[]</tt> array.
     * @param list an array containing <tt>UriLocalName</tt>
     * objects with the URI index and local name of
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * STARTELEMENT events, one of which is required.
     * @return the next matching STARTELEMENT event from the buffer.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement
                    (XMLEvent.UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        do {
            try {
                ev = expectStartElement(list, discardWhiteSpace);
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
     * Expect one of an array of possible STARTELEMENT events.  The next
     * STARTELEMENT must have a URI index and local name which match
     * an element of the argument <tt>XMLEvent.UriLocalName[]</tt> list.
     * @param list an <tt>XMLEvent.UriLocalName[]</tt> array containing the
     * namespace Uri index and LocalName
     * of possible events, one of which must be the next returned.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the matching STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent expectStartElement
                    (XMLEvent.UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        for (int i = 0; i < list.length; i++) {
            try {
                ev = expectStartElement(list[i].getUriIndex(),
                                        list[i].getLocalName(),
                                        discardWhiteSpace);
                // Found it!
                return ev;
            } catch (NoSuchElementException e) {
                // Keep trying
            }
        }
        throw new NoSuchElementException
                ("StartElement from array not found.");
    }

    /**
     * Get one of a array of possible STARTELEMENT events.  Scan and discard
     * events until a STARTELEMENT event is found whose <tt>QName</tt>
     * matches one of those in the argument <tt>String[]</tt> array.
     * @param list a <tt>String[]</tt> array containing <tt>QName</tt>s,
     * one of which is required.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the next matching STARTELEMENT event from the buffer.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getStartElement(String[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        do {
            try {
                ev = expectStartElement(list, discardWhiteSpace);
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
                    ("StartElement from array not found.");
    }

    /**
     * Expect one of an array of possible STARTELEMENT events.  The next
     * STARTELEMENT must have a <tt>QName</tt> which matches an element
     * of the argument <tt>String[]</tt> list.
     * @param list a <tt>String[]</tt> array containing <tt>QName</tt>s
     * of possible events, one of which must be the next returned.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the matching STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent expectStartElement
                                    (String[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XMLEvent ev;
        for (int i = 0; i < list.length; i++) {
            try {
                ev = expectStartElement(list[i], discardWhiteSpace);
                // Found it!
                return ev;
            } catch (NoSuchElementException e) {
                // Keep trying
            }
        }
        throw new NoSuchElementException
                ("StartElement from array not found.");
    }

    /**
     * Get the next ENDELEMENT event from the buffer.  Discard any other
     * events preceding the ENDELEMENT event.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement() throws FOPException {
        return getTypedEvent(XMLEvent.ENDELEMENT);
    }

    /**
     * Return the next element if it is an ENDELEMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectTypedEvent(XMLEvent.ENDELEMENT, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event with the given <tt>QName</tt>
     * from the buffer.  Discard any other events preceding the
     * ENDELEMENT event.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required STARTELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(String qName) throws FOPException
    {
        return getTypedEvent(XMLEvent.ENDELEMENT, qName);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the given
     * <tt>QName</tt>.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param qName a <tt>String</tt> with the <tt>QName</tt> of the
     * required ENDELEMENT
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(String qName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent(XMLEvent.ENDELEMENT, qName, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event with the given URI index and local name
     * from the buffer.  Discard any other events preceding the
     * ENDELEMENT event.
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required ENDELEMENT
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(int uriIndex, String localName)
        throws FOPException
    {
        return getTypedEvent(XMLEvent.ENDELEMENT, uriIndex, localName);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the given
     * URI index and local name.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param localName a <tt>String</tt> with the local name of the
     * required ENDELEMENT
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
                (XMLEvent.ENDELEMENT, uriIndex, localName, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event, with the same URI index and local name
     * as the <tt>XMLEvent</tt> argument, from the buffer.
     * Discard any other events preceding the ENDELEMENT event.
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XMLEvent getEndElement(XMLEvent event) throws FOPException
    {
        return getTypedEvent
                    (XMLEvent.ENDELEMENT, event.uriIndex, event.localName);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the same
     * URI index and local name as the <tt>XMLEvent argument</tt>.  If the
     * next element is not of the required type, push it back onto the buffer.
     * @param event an <tt>XMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if STARTELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     */
    public XMLEvent expectEndElement(XMLEvent event, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
                (XMLEvent.ENDELEMENT, event.uriIndex, event.localName,
                                                         discardWhiteSpace);
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
