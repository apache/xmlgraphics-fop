/*
 * $Id$
 * 
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
 */
package org.apache.fop.xml;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjectSets;

/**
 * A synchronized circular buffer for XMLEvents.
 * Extends SyncedCircularBuffer and implements XmlEventSource.
 * 
 * @see org.apache.fop.datastructs.SyncedCircularBuffer
 * @see XmlEventSource
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class XmlEventReader {

    /**
     * Constant for <i>discardEvent</i> field of
     * <i>getEndElement(boolean discardEvent, XmlEvent(, boolean)).
     */
    public static final boolean DISCARD_EV = true,
                                 RETAIN_EV = false;
    
    /**
     * The event source for this reader
     */
    private XmlEventSource source;


    /**
     * Maintains an index of namespace URIs.  These can then be referred to
     * by an <tt>int</tt> index.
     */
    private Namespaces namespaces;

    /**
     * @param namespaces
     * @throws IllegalArgumentException
     */
    public XmlEventReader(XmlEventSource source, Namespaces namespaces) {
        this.source = source;
        this.namespaces = namespaces;
    }

    /**
     * Get the <tt>Namespaces</tt> from this buffer.
     * @return - the namespaces object.
     */
    public Namespaces getNamespaces() { return namespaces; }
    
    /**
     * Get the next event of the given type from the buffer.  Discard
     * intervening events.
     * @param eventType - the <tt>int</tt> event type.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XmlEvent getSaxEvent(int eventType) throws FOPException {
        XmlEvent ev = source.getEvent();
        while (ev != null && ev.type != eventType) {
            ev = source.getEvent();
        }
        if (ev == null) {
            throw new NoSuchElementException
                        (XmlEvent.eventTypeName(eventType) + " not found.");
        }
        return ev;
    }

    /**
     * Get the next event of the given SAX type, from the given namespace
     * (<code>uriIndex</code>) with the given local name, from the buffer.
     * Discard intervening events.
     * @param eventType the SAX event type.
     * @param uriIndex the URI index maintained in the
     * <tt>Namespaces</tt> object.
     * @param localName of the required element.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XmlEvent getSaxUriLocalEvent
                            (int eventType, int uriIndex, String localName)
                throws FOPException
    {
        XmlEvent ev = source.getEvent();
        while (ev != null &&
               ! (ev.type == eventType
                  && ev.uriIndex == uriIndex
                  && ev.localName.equals(localName))) {
            namespaces.relinquishEvent(ev);
            ev = source.getEvent();
        }
        if (ev == null)
            throw new NoSuchElementException
                    (XmlEvent.eventTypeName(eventType)
                             + namespaces.getIndexURI(uriIndex)
                                       + ":" + localName + " not found.");
        return ev;
    }

    /**
     * Get the next event of the given SAX type, from the given namespace
     * (<code>uriIndex</code>) with the given local name, from the buffer.
     * Discard intervening events.
     * @param eventType the SAX event type.
     * @param uriIndex the URI index maintained in the
     * <tt>Namespaces</tt> object.
     * @param localName of the required element.
     * @param buffer into whcih to copy intervening events.
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XmlEvent getSaxUriLocalEvent(
            int eventType, int uriIndex, String localName,
            XmlEventsArrayBuffer buffer)
    throws FOPException
    {
        XmlEvent ev = source.getEvent();
        while (ev != null &&
                ! (ev.type == eventType
                        && ev.uriIndex == uriIndex
                        && ev.localName.equals(localName))) {
            buffer.addEvent(ev);
            ev = source.getEvent();
        }
        if (ev == null)
            throw new NoSuchElementException
            (XmlEvent.eventTypeName(eventType)
                    + namespaces.getIndexURI(uriIndex)
                    + ":" + localName + " not found.");
        return ev;
    }
    
    /**
     * Get the next event with of the given SAX type, whose URI is matched
     * by the namespaces URI indexed by <code>uriIndex</code>, and whose
     * namespace-specific type matches <code>nsType</code>.
     * Discard any intervening events.
     * @param eventType the SAX event type
     * @param uriIndex of the URI in namespaces
     * @param nsType the namespace-specific type
     * @return the matching event
     * @throws FOPException
     */
    public XmlEvent getSaxUriTypedEvent(
            int eventType, int uriIndex, int nsType) throws FOPException {
        XmlEvent ev = source.getEvent();
        while (ev != null) {
            if (ev.type == eventType && ev.uriIndex == uriIndex) {
                switch (uriIndex) {
                case Namespaces.DefAttrNSIndex:
                    throw new NoSuchElementException
                    ("No special types for default attribute namespace");
                case Namespaces.XSLNSpaceIndex:
                    // The FO namespace
                    if (ev.getFoType() == nsType) {
                        return ev;
                    }
                    break;
                case Namespaces.FOXNSpaceIndex:
                    // The FOX namespace
                    if (ev.getFoxType() == nsType) {
                        return ev;
                    }
                    break;
                case Namespaces.SVGNSpaceIndex:
                    // The SVG namespace
                    if (ev.getSvgType() == nsType) {
                        return ev;
                    }
                    break;
                }
            }
            namespaces.relinquishEvent(ev);
            ev = source.getEvent();
        }
        throw new NoSuchElementException
            (XmlEvent.eventTypeName(eventType) + " "
                    + namespaces.getIndexURI(uriIndex)
                    + " type " + nsType + " not found.");
    }
    
    /**
     * Get the next event with of the given SAX type, whose URI is matched
     * by the namespaces URI indexed by <code>uriIndex</code>, and whose
     * namespace-specific type matches <code>nsType</code>.
     * Copy any intervening events into the specified buffer.
     * @param eventType the SAX event type
     * @param uriIndex of the URI in namespaces
     * @param nsType the namespace-specific type
     * @param buffer into whcih to copy intervening events
     * @return the matching event
     * @throws FOPException
     */
    public XmlEvent getSaxUriTypedEvent(
            int eventType, int uriIndex, int nsType,
            XmlEventsArrayBuffer buffer)
    throws FOPException {
        XmlEvent ev = source.getEvent();
        while (ev != null) {
            if (ev.type == eventType && ev.uriIndex == uriIndex) {
                switch (uriIndex) {
                case Namespaces.DefAttrNSIndex:
                    throw new NoSuchElementException
                    ("No special types for default attribute namespace");
                case Namespaces.XSLNSpaceIndex:
                    // The FO namespace
                    if (ev.getFoType() == nsType) {
                        return ev;
                    }
                    break;
                case Namespaces.FOXNSpaceIndex:
                    // The FOX namespace
                    if (ev.getFoxType() == nsType) {
                        return ev;
                    }
                    break;
                case Namespaces.SVGNSpaceIndex:
                    // The SVG namespace
                    if (ev.getSvgType() == nsType) {
                        return ev;
                    }
                    break;
                }
            }
            buffer.addEvent(ev);
            ev = source.getEvent();
        }
        throw new NoSuchElementException
        (XmlEvent.eventTypeName(eventType) + " "
                + namespaces.getIndexURI(uriIndex)
                + " type " + nsType + " not found.");
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
    public XmlEvent getSaxFoEvent(int eventType, int foType)
                throws FOPException
    {
        return getSaxUriTypedEvent(
                eventType, Namespaces.XSLNSpaceIndex, foType);
    }

    /**
     * Get the next event of the given type, from the fo: namespace, with
     * the given FO type.  Copy intervening events into the specified
     * buffer.
     * @param eventType - the <tt>int</tt> event type.
     * @param foType - the <tt>int</tt> FO type.
     * @param buffer the event buffer into which to copy intervening events
     * @return an event of the given type.
     * @exception FOPException if buffer errors or interrupts occur.
     * @exception NoSuchElementException if the event is not found.
     */
    public XmlEvent getSaxFoEvent(
            int eventType, int foType, XmlEventsArrayBuffer buffer)
    throws FOPException
    {
        return getSaxUriTypedEvent(
                eventType, Namespaces.XSLNSpaceIndex, foType, buffer);
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
    public XmlEvent expectSaxEvent
                                    (int eventType, boolean discardWhiteSpace)
                throws FOPException
    {
        XmlEvent ev = source.getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XmlEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                namespaces.relinquishEvent(ev);
                ev = source.getEvent();
            }
        }
        if (ev != null && ev.type == eventType) {
            return ev;
        }
        if (ev == null)
            throw new NoSuchElementException
                        (XmlEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        source.pushBack(ev);
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
    public XmlEvent expectSaxUriLocalEvent
                            (int eventType, int uriIndex,
                                 String localName, boolean discardWhiteSpace)
                throws FOPException
    {
        XmlEvent ev = source.getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XmlEvent.CHARACTERS
                   && ev.chars.trim().equals("")) {
                namespaces.relinquishEvent(ev);
                ev = source.getEvent();
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
                        (XmlEvent.eventTypeName(eventType)
                                           + " not found: end of buffer.");
        source.pushBack(ev);
        return null;
    }
    
    /**
     * Return the next event if it is of the given SAX type, whose URI is
     * matched by the namespaces URI indexed by <code>uriIndex</code>, and
     * whose namespace-specific type matches <code>nsType</code>.
     * If the next element is not of the required type,
     * push it back onto the buffer.
     * @param eventType the SAX event type
     * @param uriIndex of the URI in namespaces
     * @param nsType the namespace-specific type
     * @param discardWhiteSpace - if true, discard any intervening
     * <tt>characters</tt> events which contain only whitespace.
     * @return an event of the required type.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @throws FOPException
     */
    public XmlEvent expectSaxUriTypedEvent(
            int eventType, int uriIndex, int nsType,
            boolean discardWhiteSpace)
    throws FOPException {
        XmlEvent ev = source.getEvent();
        if (discardWhiteSpace) {
            while (ev != null && ev.type == XmlEvent.CHARACTERS
                    && ev.chars.trim().equals("")) {
                namespaces.relinquishEvent(ev);
                ev = source.getEvent();
            }
        }
        if (ev != null && ev.type == eventType) {
            switch (uriIndex) {
            case Namespaces.DefAttrNSIndex:
                throw new NoSuchElementException
                ("No special types for default attribute namespace");
            case Namespaces.XSLNSpaceIndex:
                // The FO namespace
                if (ev.getFoType() == nsType) {
                    return ev;
                }
                break;
            case Namespaces.FOXNSpaceIndex:
                // The FOX namespace
                if (ev.getFoxType() == nsType) {
                    return ev;
                }
                break;
            case Namespaces.SVGNSpaceIndex:
                // The SVG namespace
                if (ev.getSvgType() == nsType) {
                    return ev;
                }
                break;
            }
        }
        if (ev == null)
            throw new NoSuchElementException
            (XmlEvent.eventTypeName(eventType) + " "
                    + namespaces.getIndexURI(uriIndex)
                    + " type " + nsType + " not found.");
        source.pushBack(ev);
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
    public XmlEvent expectSaxFoEvent
                    (int eventType, int foType, boolean discardWhiteSpace)
                throws FOPException
    {
        return expectSaxUriTypedEvent(
                eventType, Namespaces.XSLNSpaceIndex,
                foType, discardWhiteSpace);
    }

    /**
     * Get the next ENDDOCUMENT event from the buffer.  Discard any other
     * events preceding the ENDDOCUMENT event.
     * @return an ENDDOCUMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getEndDocument() throws FOPException {
        return getSaxEvent(XmlEvent.ENDDOCUMENT);
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
    public XmlEvent expectEndDocument(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectSaxEvent(XmlEvent.ENDDOCUMENT, discardWhiteSpace);
    }

    /**
     * Get the next STARTELEMENT event from the buffer.  Discard any other
     * events preceding the STARTELEMENT event.
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getStartElement() throws FOPException {
        return getSaxEvent(XmlEvent.STARTELEMENT);
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
    public XmlEvent expectStartElement(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectSaxEvent(XmlEvent.STARTELEMENT, discardWhiteSpace);
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
    public XmlEvent getStartElement(int uriIndex, String localName)
        throws FOPException
    {
        return getSaxUriLocalEvent(XmlEvent.STARTELEMENT, uriIndex, localName);
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
    public XmlEvent expectStartElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectSaxUriLocalEvent
            (XmlEvent.STARTELEMENT, uriIndex, localName, discardWhiteSpace);
    }

    /**
     * Get the next STARTELEMENT event with the given URI index and local name
     * from the buffer.  Discard any other events preceding the
     * STARTELEMENT event.
     * @param uriIndex an <tt>int</tt> with the index of the URI of the
     * required URI
     * @param nsType the namespace-dependent event type
     * @return a STARTELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getStartElement(int uriIndex, int nsType)
    throws FOPException
    {
        return getSaxUriTypedEvent(XmlEvent.STARTELEMENT, uriIndex, nsType);
    }

    /**
     * Return the next element if it is a STARTELEMENT with the given
     * URI index and local name.  If the next
     * element is not of the required type, push it back onto the buffer.
     * @param uriIndex an <tt>int</tt> URI index.
     * @param nsType the namespace-dependent event type
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public XmlEvent expectStartElement(
            int uriIndex, int nsType, boolean discardWhiteSpace)
    throws FOPException
    {
        return expectSaxUriTypedEvent(
                XmlEvent.STARTELEMENT, uriIndex, nsType, discardWhiteSpace);
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
    public XmlEvent getStartElement(int foType)
    throws FOPException
    {
        return getSaxFoEvent(XmlEvent.STARTELEMENT, foType);
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
    public XmlEvent expectStartElement
    (int foType, boolean discardWhiteSpace)
    throws FOPException
    {
        return expectSaxFoEvent(
                XmlEvent.STARTELEMENT, foType, discardWhiteSpace);
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
    public XmlEvent getStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // source.getEvent() returns null, the expectStartElement() calls
            // return null.
            ev = source.getEvent();
            namespaces.relinquishEvent(ev);
        } while (ev != null);
        // Exit from this while loop is only by discovery of null event
        throw new NoSuchElementException
                    ("StartElement from list not found.");
    }

    /**
     * Get one of a list of possible STARTELEMENT events.
     * @param list a <tt>LinkedList</tt> containing either
     * <tt>UriLocalName</tt> objects with the URI index and local name,
     * <tt>NameSpaceType</tt> objects with the URI index and local name and
     * a namespace-dependent <tt>int</tt> type, or <tt>Integer</tt>s with
     * the FO type of one of the required STARTELEMENT events.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a STARTELEMENT event.  If the next
     * event detected is not of the required type, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public XmlEvent expectStartElement
                                (LinkedList list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        Iterator elements = list.iterator();
        while (elements.hasNext()) {
            Object o = elements.next();
            if (o instanceof UriLocalName) {
                if (o instanceof NameSpaceType) {
                    NameSpaceType nameSpType = (NameSpaceType)o;
                    ev = expectStartElement(
                            nameSpType.uriIndex,
                            nameSpType.nsType,
                            discardWhiteSpace);
                    // Found it!
                    if (ev != null) return ev;
                } else {
                    UriLocalName uriLocalName = (UriLocalName)o;
                    ev = expectStartElement
                    (uriLocalName.uriIndex,
                            uriLocalName.localName,
                            discardWhiteSpace);
                    // Found it!
                    if (ev != null) return ev;
                }
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
    public XmlEvent getStartElement
                    (UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // source.getEvent() returns null, the expectStartElement() calls
            // will throw a NoSuchElementException
            ev = source.getEvent();
            namespaces.relinquishEvent(ev);
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
    public XmlEvent expectStartElement
                    (UriLocalName[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
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
    public XmlEvent getStartElement(int[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        do {
            ev = expectStartElement(list, discardWhiteSpace);
            if (ev != null) return ev;
            // The non-matching event has been pushed back.
            // Get it and discard.  Note that if the first attempt to
            // source.getEvent() returns null, the expectStartElement() calls
            // will throw a NoSuchElementException
            ev = source.getEvent();
            namespaces.relinquishEvent(ev);
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
    public XmlEvent expectStartElement
                                    (int[] list, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
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
    public XmlEvent getStartElement(BitSet set, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        do {
            try {
                ev = expectStartElement(set, discardWhiteSpace);
                if (ev != null) return ev;
                // The non-matching event has been pushed back.
                // Get it and discard.  Note that if the first attempt to
                // source.getEvent() returns null, the expectStartElement() calls
                // will throw a NoSuchElementException
                ev = source.getEvent();
                namespaces.relinquishEvent(ev);
            } catch(UnexpectedStartElementException e) {
                ev = source.getEvent();
                namespaces.relinquishEvent(ev);
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
    public XmlEvent expectStartElement
                                    (BitSet set, boolean discardWhiteSpace)
        throws FOPException, UnexpectedStartElementException
    {
        XmlEvent ev;
        ev = expectSaxEvent(XmlEvent.STARTELEMENT, discardWhiteSpace);
        if (ev == null) return ev;

        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(++i)) {
            if (ev.getFoType() == i)
                return ev; // Found it!
        }
        // Not found - push the STARTELEMENT event back and throw an
        // UnexpectedStartElementException
        source.pushBack(ev);
        throw new UnexpectedStartElementException
                ("Unexpected START element: " + ev.getQName());
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of %block; from
     * <b>6.2 Formatting Object Content</b>, including out-of-line flow
     * objects which may occur except as descendents of out-of-line formatting
     * objects.  White space is discarded.
     * @return the <tt>XmlEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public XmlEvent expectBlock()
        throws FOPException, UnexpectedStartElementException
    {
        return expectStartElement
                (FObjectSets.blockEntity, XmlEvent.DISCARD_W_SPACE);
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of %block; from
     * <b>6.2 Formatting Object Content</b>, excluding out-of-line flow
     * objects which may not occur as descendents of out-of-line formatting
     * objects.  White space is discarded.
     * @return the <tt>XmlEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public XmlEvent expectOutOfLineBlock()
        throws FOPException, UnexpectedStartElementException
    {
        return expectStartElement
                (FObjectSets.outOfLineBlockSet, XmlEvent.DISCARD_W_SPACE);
    }

    /**
     * Expect that the next element will be a STARTELEMENT for one of the
     * flow objects which are members of (#PCDATA|%inline;) from
     * <b>6.2 Formatting Object Content</b>, including out-of-line flow
     * objects which may occur except as descendents of out-of-line
     * formatting objects.  White space is retained, and
     * will appear as #PCDATA, i.e, as an instance of FoCharacters.
     * @return the <tt>XmlEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public XmlEvent expectPcdataOrInline()
        throws FOPException, UnexpectedStartElementException
    {
        XmlEvent ev = expectStartElement
                (FObjectSets.normalPcdataInlineSet, XmlEvent.RETAIN_W_SPACE);
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
     * @return the <tt>XmlEvent found. If any other events are encountered
     * return <tt>null</tt>.
     */
    public XmlEvent expectOutOfLinePcdataOrInline()
        throws FOPException, UnexpectedStartElementException
    {
        XmlEvent ev = expectStartElement
                    (FObjectSets.inlineEntity, XmlEvent.RETAIN_W_SPACE);
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
     * @return the <tt>XmlEvent</tt> found. If any other events are
     * encountered return <tt>null</tt>.
     */
    public XmlEvent expectPcdataOrInlineOrBlock()
        throws FOPException, UnexpectedStartElementException
    {
        XmlEvent ev = expectStartElement
            (FObjectSets.normalPcdataBlockInlineSet, XmlEvent.RETAIN_W_SPACE);
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
     * @return the <tt>XmlEvent</tt> found. If any other events are
     * encountered return <tt>null</tt>.
     */
    public XmlEvent expectOutOfLinePcdataOrInlineOrBlock()
        throws FOPException, UnexpectedStartElementException
    {
        XmlEvent ev = expectStartElement
            (FObjectSets.outOfLinePcdataBlockInlineSet,
                                                     XmlEvent.RETAIN_W_SPACE);
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
    public XmlEvent getEndElement() throws FOPException {
        return getSaxEvent(XmlEvent.ENDELEMENT);
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
     */
    public XmlEvent expectEndElement(boolean discardWhiteSpace)
                throws FOPException
    {
        return expectSaxEvent(XmlEvent.ENDELEMENT, discardWhiteSpace);
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
    public XmlEvent getEndElement(int uriIndex, String localName)
        throws FOPException
    {
        return getSaxUriLocalEvent(XmlEvent.ENDELEMENT, uriIndex, localName);
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
    public XmlEvent expectEndElement
                (int uriIndex, String localName, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectSaxUriLocalEvent
                (XmlEvent.ENDELEMENT, uriIndex, localName, discardWhiteSpace);
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
    public XmlEvent getEndElement(int foType) throws FOPException
    {
        return getSaxFoEvent(XmlEvent.ENDELEMENT, foType);
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
    public XmlEvent expectEndElement(int foType, boolean discardWhiteSpace)
        throws FOPException
    {
        return expectSaxFoEvent
                            (XmlEvent.ENDELEMENT, foType, discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event, with the same URI index and local name
     * as the <tt>XmlEvent</tt> argument, from the buffer.
     * Discard any other events preceding the ENDELEMENT event.
     * @param event an <tt>XmlEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XmlEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getEndElement(XmlEvent event) throws FOPException
    {
        int foType;
        if (event.getType() == XmlEvent.CHARACTERS) {
            throw new FOPException("CHARS event passed to getEndElement");
        }
        if ((foType = event.getFoType()) != FObjectNames.NO_FO)
            return getSaxFoEvent(XmlEvent.ENDELEMENT, foType);
        return getSaxUriLocalEvent
                    (XmlEvent.ENDELEMENT, event.uriIndex, event.localName);
    }

    /**
     * Return the next element if it is an ENDELEMENT with the same
     * URI index and local name as the <tt>XmlEvent argument</tt>.  If the
     * next element is not of the required type, push it back onto the buffer.
     * @param event an <tt>XmlEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XmlEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching ENDELEMENT event.  If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public XmlEvent expectEndElement
                                (XmlEvent event, boolean discardWhiteSpace)
        throws FOPException
    {
        int foType;
        if (event.getType() == XmlEvent.CHARACTERS) {
            throw new FOPException("CHARS event passed to getEndElement");
        }
        if ((foType = event.getFoType()) != FObjectNames.NO_FO)
            return expectSaxFoEvent
                    (XmlEvent.ENDELEMENT, foType, discardWhiteSpace);
        return expectSaxUriLocalEvent
                (XmlEvent.ENDELEMENT, event.uriIndex, event.localName,
                                                         discardWhiteSpace);
    }

    /**
     * Get the next ENDELEMENT event, with the same URI index and local name
     * as the <tt>XmlEvent</tt> argument, from the buffer.
     * Discard any other events preceding the ENDELEMENT event.
     * @param discardEvent the argument event may be discarded.
     * @param event an <tt>XmlEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XmlEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getEndElement(boolean discardEvent, XmlEvent event)
        throws FOPException
    {
        XmlEvent ev;
        int foType;
        if (event.getType() == XmlEvent.CHARACTERS) {
            throw new FOPException("CHARS event passed to getEndElement");
        }
        if ((foType = event.getFoType()) != FObjectNames.NO_FO)
            ev = getSaxFoEvent(XmlEvent.ENDELEMENT, foType);
        else
            ev = getSaxUriLocalEvent
                    (XmlEvent.ENDELEMENT, event.uriIndex, event.localName);
        if (discardEvent) {
            //System.out.println("discardEvent");
            namespaces.relinquishEvent(event);
        }
        return ev;
    }

    /**
     * Get the next ENDELEMENT event, with the same URI index and local name
     * as the <tt>XmlEvent</tt> argument, from the buffer.
     * Place references to all intervening events in the provided
     * <code>XmlEventsArrayBuffer</code>.
     * @param buffer into which to copy the events
     * @param discardEvent the argument event may be discarded.
     * @param event an <tt>XmlEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XmlEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @return an ENDELEMENT event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getEndElement(
            XmlEventsArrayBuffer buffer, boolean discardEvent, XmlEvent event)
    throws FOPException
    {
        XmlEvent ev;
        int foType;
        if (event.getType() == XmlEvent.CHARACTERS) {
            throw new FOPException("CHARS event passed to getEndElement");
        }
        if ((foType = event.getFoType()) != FObjectNames.NO_FO)
            ev = getSaxFoEvent(XmlEvent.ENDELEMENT, foType, buffer);
        else
            ev = getSaxUriLocalEvent(
                    XmlEvent.ENDELEMENT, event.uriIndex, event.localName,
                    buffer);
        if (discardEvent) {
            namespaces.relinquishEvent(event);
        }
        return ev;
    }
    
    /**
     * Return the next element if it is an ENDELEMENT with the same
     * URI index and local name as the <tt>XmlEvent argument</tt>.  If the
     * next element is not of the required type, push it back onto the buffer.
     * @param discardEvent the argument event may be discarded.
     * @param event an <tt>XmlEvent</tt>.  Only the uriIndex and the
     * localName from the event are used.  It is intended that the XmlEvent
     * returned to the corresponding get/expectStartElement() call be used.
     * @param discardWhiteSpace - if true, discard any <tt>characters</tt>
     * events which contain only whitespace.
     * @return a matching ENDELEMENT event.  If the next
     * event detected is not an ENDELEMENT, <tt>null</tt> is returned.
     * The erroneous event is pushed back.
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if end of buffer detected.
     */
    public XmlEvent expectEndElement
        (boolean discardEvent, XmlEvent event, boolean discardWhiteSpace)
        throws FOPException
    {
        XmlEvent ev;
        int foType;
        if (event.getType() == XmlEvent.CHARACTERS) {
            throw new FOPException("CHARS event passed to getEndElement");
        }
        if ((foType = event.getFoType()) != FObjectNames.NO_FO)
            ev = expectSaxFoEvent
                    (XmlEvent.ENDELEMENT, foType, discardWhiteSpace);
        else
            ev = expectSaxUriLocalEvent
                (XmlEvent.ENDELEMENT, event.uriIndex, event.localName,
                                                         discardWhiteSpace);
        if (discardEvent)
            namespaces.relinquishEvent(event);
        return ev;
    }

    /**
     * @return a CHARACTERS event
     * @exception FOPException if buffer errors or interrupts occur
     * @exception NoSuchElementException if the event is not found
     */
    public XmlEvent getCharacters() throws FOPException {
        XmlEvent ev = source.getEvent();
        while (ev != null && ev.type != XmlEvent.CHARACTERS) {
            namespaces.relinquishEvent(ev);
            ev = source.getEvent();
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
    public XmlEvent expectCharacters() throws FOPException {
        XmlEvent ev = source.getEvent();
        if (ev != null && ev.type == XmlEvent.CHARACTERS) {
            return ev;
        }
        source.pushBack(ev);
        return null;
    }

}
