package org.apache.fop.xml;

import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjectSets;

import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.BitSet;

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
 * A synchronized circular buffer for FoXMLEvents.
 * @see org.apache.fop.datastructs.SyncedCircularBuffer
 */
public class SyncedFoXmlEventsBuffer extends SyncedCircularBuffer {

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
    public SyncedFoXmlEventsBuffer()
        throws IllegalArgumentException
    {
        super();
        namespaces = new XMLNamespaces();
    }

    /**
     * Constructor taking one argument; the size of the buffer.
     * @param size the size of the buffer.  Must be > 1.
     */
    public SyncedFoXmlEventsBuffer(int size)
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
    public FoXMLEvent getEvent() throws FOPException {
        FoXMLEvent ev;
        try {
            ev = (FoXMLEvent)get();
            //System.out.println("getEvent: " + ev);
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
    public FoXMLEvent getTypedEvent(int eventType) throws FOPException {
        FoXMLEvent ev = getEvent();
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
    public FoXMLEvent getTypedEvent(int eventType, String qName)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
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
    public FoXMLEvent getTypedEvent
                            (int eventType, int uriIndex, String localName)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == eventType
                  && ev.uriIndex == uriIndex
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
     * Get the next event of the given type, from the fo: namespace, with
     * the given FO type.  Discard intervening events.
     * @param eventType - the <tt>int</tt> event type.
     * @param foType - the <tt>int</tt> FO type.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public FoXMLEvent getTypedEvent(int eventType, int foType)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
        while (ev != null &&
               ! (ev.type == eventType && ev.foType == foType)) {
            ev = getEvent();
        }
        if (ev == null)
            throw new NoSuchElementException
                    (XMLEvent.eventTypeName(eventType)
                             + " FO type " + foType + " not found.");
        return ev;
    }

    /**
     * Return the next element if it is of the required type.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an event of the required type.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the buffer is empty.
     */
    public FoXMLEvent expectTypedEvent
                                    (int eventType, boolean discardWhiteSpace)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null && ev.type == eventType) {
            return ev;
        }
        if (ev == null)
            throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        pushBack(ev);
        return null;
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
     * @return an event of the required type.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public FoXMLEvent expectTypedEvent
                    (int eventType, String qName, boolean discardWhiteSpace)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null && ev.type == eventType && ev.qName.equals(qName)) {
            return ev;
        }
        if (ev == null)
            throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        pushBack(ev);
        return null;
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
     * @return an event of the required type.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectTypedEvent
                            (int eventType, int uriIndex,
                                 String localName, boolean discardWhiteSpace)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
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
        if (ev == null)
            throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        pushBack(ev);
        return null;
    }

    /**
     * Return the next element if it is of the required type and has the
     * required FO type.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param foType - the <tt>int</tt> FO type.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an event of the required type.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectTypedEvent
                    (int eventType, int foType, boolean discardWhiteSpace)
                throws FOPException
    {
        FoXMLEvent ev = getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XMLEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                ev = getEvent();
            }
        }
        if (ev != null && ev.type == eventType && ev.foType == foType) {
            return ev;
        }
        if (ev == null)
            throw new NoSuchElementException
                        (XMLEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        pushBack(ev);
        return null;
    }

    /**
     * Get the next ENDDOCUMENT event from the buffer.  Discard any other
     * events preceding the ENDDOCUMENT event.
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getEndDocument() throws FOPException {
        return getTypedEvent(XMLEvent.ENDDOCUMENT);
    }

    /**
     * Return the next element if it is an ENDDOCUMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return an ENDDOCUMENT event. If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndDocument(boolean discardWhiteSpace)
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
    public FoXMLEvent getStartElement() throws FOPException {
        return getTypedEvent(XMLEvent.STARTELEMENT);
    }

    /**
     * Return the next element if it is a STARTELEMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement(boolean discardWhiteSpace)
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
    public FoXMLEvent getStartElement(String qName) throws FOPException
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
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
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
    public FoXMLEvent getStartElement(int uriIndex, String localName)
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
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
            (XMLEvent.STARTELEMENT, uriIndex, localName, discardWhiteSpace);
    }

    /**
     * From the buffer get the next STARTELEMENT event from the fo: namespace
     * with the given FO object type.
     *  Discard any other events preceding the
     * STARTELEMENT event.
     * @param foType - the <tt>int</tt> FO object type, as defined in
     * <tt>FObjectNames</tt>.
     * @return a matching STARTELEMENT event.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getStartElement(int foType)
        throws FOPException
    {
        return getTypedEvent(XMLEvent.STARTELEMENT, foType);
    }

    /**
     * From the buffer return the next STARTELEMENT event from the fo:
     * namespace with the given FO object type.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param foType - the <tt>int</tt> FO object type, as defined in
     * <tt>FObjectNames</tt>.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                                    (int foType, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
            (XMLEvent.STARTELEMENT, foType, discardWhiteSpace);
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
    public FoXMLEvent getStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // getEvent() returns null, the expectStartElement() calls
            // return null.
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
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            Object o = elements.next();
            if (o instanceof String) {
                ev = expectStartElement((String) o, discardWhiteSpace);
                // Found it!
                if (ev != null) return ev;
            } else if (o instanceof UriLocalName) {
                ev = expectStartElement
                        (((UriLocalName) o).uriIndex,
                         ((UriLocalName) o).localName,
                         discardWhiteSpace);
                // Found it!
                if (ev != null) return ev;
            } else if (o instanceof Integer) {
                ev = expectStartElement(((Integer)o).intValue(),
                                        discardWhiteSpace);
                if (ev != null) return ev;
            } else
                throw new FOPException
                        ("Invalid list elements for getStartElement");
        }
        return null;
    }

    /**
     * Get one of a array of possible STARTELEMENT events.  Scan and discard
     * events until a STARTELEMENT event is found whose URI index and
     * local name matches one of those in the argument
     * <tt>UriLocalName[]</tt> array.
     * @param list an array containing <tt>UriLocalName</tt>
     * objects with the URI index and local name of
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * STARTELEMENT events, one of which is required.
     * @return the next matching STARTELEMENT event from the buffer.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getStartElement
                    (UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
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
     * an element of the argument <tt>UriLocalName[]</tt> list.
     * @param list an <tt>UriLocalName[]</tt> array containing the
     * namespace Uri index and LocalName
     * of possible events, one of which must be the next returned.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the matching STARTELEMENT event. If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                    (UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        for (int i = 0; i < list.length; i++) {
            ev = expectStartElement(list[i].uriIndex,
                                    list[i].localName,
                                    discardWhiteSpace);
            // Found it!
            if (ev != null) return ev;
        }
        return null;
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
    public FoXMLEvent getStartElement(String[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
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
     * @return the matching STARTELEMENT event.If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                                    (String[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        for (int i = 0; i < list.length; i++) {
            ev = expectStartElement(list[i], discardWhiteSpace);
            // Found it!
            if (ev != null) return ev;
        }
        return null;
    }

    /**
     * Get one of a array of possible STARTELEMENT events.  Scan and discard
     * events until a STARTELEMENT event is found which is in the fo:
     * namespace and whose FO type matches one of those in the argument
     * <tt>int</tt> array.
     * @param list an <tt>int[]</tt> array containing FO types
     * one of which is required.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the next matching STARTELEMENT event from the buffer.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getStartElement(int[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
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
     * STARTELEMENT must be in the fo: namespace, and must have an FO type
     * which matches one of those in the argument <tt>int[]</tt> list.
     * @param list a <tt>int[]</tt> array containing the FO types
     * of possible events, one of which must be the next returned.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the matching STARTELEMENT event.If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                                    (int[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        for (int i = 0; i < list.length; i++) {
            ev = expectStartElement(list[i], discardWhiteSpace);
            // Found it!
            if (ev != null) return ev;
        }
        return null;
    }

    /**
     * Get one of a <tt>BitSet</tt> of possible STARTELEMENT events.  Scan
     * and discard events until a STARTELEMENT event is found which is in
     * the fo: namespace and whose FO type matches one of those in the
     * argument <tt>BitSet</tt>.
     * @param set a <tt>BitSet</tt> containing FO types one of which is
     * required.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the next matching STARTELEMENT event from the buffer.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getStartElement(BitSet set, boolean discardWhiteSpace)
        throws FOPException
    {
        FoXMLEvent ev;
        do {
            try {
            ev = expectStartElement(set, discardWhiteSpace);
            if (ev != null) return ev;
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // getEvent() returns null, the expectStartElement() calls
            // will throw a NoSuchElementException
            ev = getEvent();
            } catch(UnexpectedStartElementException e) {
                ev = getEvent();
            }
        } while (ev != null);
        // Exit from this while loop is only by discovery of null event
        throw new NoSuchElementException
                    ("StartElement from BitSet not found.");
    }

    /**
     * Expect one of an <tt>BitSet</tt> of possible STARTELEMENT events.
     * The next STARTELEMENT must be in the fo: namespace, and must have an
     * FO type which matches one of those in the argument <tt>BitSet</tt>.
     * <p>TODO:<br>
     * This method should be retro-fitted to list and array versions.
     *
     * @param set a <tt>BitSet</tt> containing the FO types
     * of possible events, one of which must be the next returned.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return the matching STARTELEMENT event.If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectStartElement
                                    (BitSet set, boolean discardWhiteSpace)
        throws FOPException, UnexpectedStartElementException
    {
        FoXMLEvent ev;
        ev = expectTypedEvent(XMLEvent.STARTELEMENT, discardWhiteSpace);
        if (ev == null) return ev;

        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(++i)) {
            if (ev.foType == i)
                return ev; // Found it!
        }
        // Not found - push the STARTELEMENT event back and throw an
        // UnexpectedStartElementException
        pushBack(ev);
        throw new UnexpectedStartElementException
                ("Unexpected START element: " + ev.getQName());
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of %block; from
     * <b>6.2 Formatting Object Content</b>, including out-of-line flow
     * objects which may occur except as descendents of out-of-line formatting
     * objects.  White space is discarded.
     * @return the <tt>FoXMLEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public FoXMLEvent expectBlock()
        throws FOPException, UnexpectedStartElementException
    {
        return expectStartElement
                (FObjectSets.blockEntity, XMLEvent.DISCARD_W_SPACE);
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of %block; from
     * <b>6.2 Formatting Object Content</b>, excluding out-of-line flow
     * objects which may not occur as descendents of out-of-line formatting
     * objects.  White space is discarded.
     * @return the <tt>FoXMLEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public FoXMLEvent expectOutOfLineBlock()
        throws FOPException, UnexpectedStartElementException
    {
        return expectStartElement
                (FObjectSets.outOfLineBlockSet, XMLEvent.DISCARD_W_SPACE);
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of (#PCDATA|%inline;) from
     * <b>6.2 Formatting Object Content</b>, including out-of-line flow
     * objects which may occur except as descendents of out-of-line
     * formatting objects.  White space is retained, and
     * will appear as #PCDATA, i.e, as an instance of FoCharacters.
     * @return the <tt>FoXMLEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public FoXMLEvent expectPcdataOrInline()
        throws FOPException, UnexpectedStartElementException
    {
        FoXMLEvent ev = expectStartElement
                (FObjectSets.normalPcdataInlineSet, XMLEvent.RETAIN_W_SPACE);
        if (ev == null)
            ev = expectCharacters();
        return ev;
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of (#PCDATA|%inline;) from
     * <b>6.2 Formatting Object Content</b>, excluding out-of-line flow
     * objects which may not occur as descendents of out-of-line formatting
     * objects.  White space is retained, and
     * will appear as #PCDATA, i.e, as an instance of FoCharacters.
     * @return the <tt>FoXMLEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public FoXMLEvent expectOutOfLinePcdataOrInline()
        throws FOPException, UnexpectedStartElementException
    {
        FoXMLEvent ev = expectStartElement
                    (FObjectSets.inlineEntity, XMLEvent.RETAIN_W_SPACE);
        if (ev == null)
            ev = expectCharacters();
        return ev;
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of (#PCDATA|%inline;|%block;) from
     * <b>6.2 Formatting Object Content</b>, including out-of-line flow
     * objects which may occur except as descendents of out-of-line
     * formatting objects.  White space is retained, and
     * will appear as #PCDATA, i.e, as an instance of FoCharacters.
     * @return the <tt>FoXMLEvent</tt> found. If any other events are
     * encountered return <tt>null</tt>.
     */
    public FoXMLEvent expectPcdataOrInlineOrBlock()
        throws FOPException, UnexpectedStartElementException
    {
        FoXMLEvent ev = expectStartElement
            (FObjectSets.normalPcdataBlockInlineSet, XMLEvent.RETAIN_W_SPACE);
        if (ev == null)
            ev = expectCharacters();
        return ev;
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of (#PCDATA|%inline;|%block;) from
     * <b>6.2 Formatting Object Content</b>, excluding out-of-line flow
     * objects which may not occur as descendents of out-of-line formatting
     * objects.  White space is retained, and
     * will appear as #PCDATA, i.e, as an instance of FoCharacters.
     * @return the <tt>FoXMLEvent</tt> found. If any other events are
     * encountered return <tt>null</tt>.
     */
    public FoXMLEvent expectOutOfLinePcdataOrInlineOrBlock()
        throws FOPException, UnexpectedStartElementException
    {
        FoXMLEvent ev = expectStartElement
            (FObjectSets.outOfLinePcdataBlockInlineSet,
                                                     XMLEvent.RETAIN_W_SPACE);
        if (ev == null)
            ev = expectCharacters();
        return ev;
    }

    /**
     * Get the next ENDELEMENT event from the buffer.  Discard any other
     * events preceding the ENDELEMENT event.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getEndElement() throws FOPException {
        return getTypedEvent(XMLEvent.ENDELEMENT);
    }

    /**
     * Return the next element if it is an ENDELEMENT.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching ENDELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if ENDELEMENT is not the next
     * event detected.  The erroneous event is pushed back.
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndElement(boolean discardWhiteSpace)
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
    public FoXMLEvent getEndElement(String qName) throws FOPException
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
     * @return an ENDELEMENT with the given qname.  If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndElement(String qName, boolean discardWhiteSpace)
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
    public FoXMLEvent getEndElement(int uriIndex, String localName)
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
     * @return a matching ENDELEMENT event.
     * If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
                (XMLEvent.ENDELEMENT, uriIndex, localName, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event with the given Fo type
     * from the buffer.  Discard any other events preceding the
     * ENDELEMENT event.
     * @param foType - the FO type of the required ENDELEMENT
     * @return a matching ENDELEMENT event.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getEndElement(int foType) throws FOPException
    {
        return getTypedEvent(XMLEvent.ENDELEMENT, foType);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the given
     * FO type.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param foType - the FO type of the required ENDELEMENT
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching ENDELEMENT.  If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndElement(int foType, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectTypedEvent
                            (XMLEvent.ENDELEMENT, foType, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event, with the same URI index and local name
     * as the <tt>FoXMLEvent</tt> argument, from the buffer.
     * Discard any other events preceding the ENDELEMENT event.
     * @param event an <tt>FoXMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the FoXMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getEndElement(FoXMLEvent event) throws FOPException
    {
        if (event.foType != FObjectNames.NO_FO)
            return getTypedEvent(XMLEvent.ENDELEMENT, event.foType);
        return getTypedEvent
                    (XMLEvent.ENDELEMENT, event.uriIndex, event.localName);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the same
     * URI index and local name as the <tt>FoXMLEvent argument</tt>.  If the
     * next element is not of the required type, push it back onto the buffer.
     * @param event an <tt>FoXMLEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the FoXMLEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching ENDELEMENT event.  If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectEndElement
                                (FoXMLEvent event, boolean discardWhiteSpace)
        throws FOPException
    {
        if (event.foType != FObjectNames.NO_FO)
            return expectTypedEvent
                    (XMLEvent.ENDELEMENT, event.foType, discardWhiteSpace);
        return expectTypedEvent
                (XMLEvent.ENDELEMENT, event.uriIndex, event.localName,
                                                         discardWhiteSpace);
    }

    /**
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public FoXMLEvent getCharacters() throws FOPException {
        FoXMLEvent ev = getEvent();
        while (ev != null && ev.type != XMLEvent.CHARACTERS) {
            ev = getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException("Characters not found.");
        }
        return ev;
    }

    /**
     * @return a CHARACTERS event.  If the next event detected is not
     * a CHARACTERS event, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public FoXMLEvent expectCharacters() throws FOPException {
        FoXMLEvent ev = getEvent();
        if (ev != null && ev.type == XMLEvent.CHARACTERS) {
            return ev;
        }
        pushBack(ev);
        return null;
    }

}
