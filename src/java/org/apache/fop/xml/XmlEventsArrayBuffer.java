/*
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
 * $Id$
 */
package org.apache.fop.xml;

import java.util.ArrayList;

import org.apache.fop.apps.FOPException;

/**
 * An ArrayList buffer for XMLEvents.
 * It implements XmlEventSource.  This buffer is designed for single-stream
 * two-phase operation.  <code>add</code>s are allowed on the private
 * <code>ArrayList</code> until the first <code>get</code>.  Thereafter they
 * are illegal.
 * 
 * @see java.util.ArrayList
 * @see XmlEventSource
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class XmlEventsArrayBuffer
implements XmlEventSource {

    /**
     * The datastructure for the events buffer.
     */
    private ArrayList buffer;
    
    /**
     * The next event pointer for the buffer.
     */
    private int nextEvent = 0;
    
    /**
     * Set true if any event has been read from the buffer
     */
    private boolean bufferRead = false;
    
    /**
     * Maintains an index of namespace URIs.  These can then be referred to
     * by an <tt>int</tt> index.
     */
    private Namespaces namespaces;

    /**
     * Sets up an array buffer for XmlEvents.
     * @param namespaces the applicable <code>Namespaces</code> object
     * @throws IllegalArgumentException
     */
    public XmlEventsArrayBuffer(Namespaces namespaces)
        throws IllegalArgumentException
    {
        this.namespaces = namespaces;
        buffer = new ArrayList();
    }

    /**
     * Sets up an array buffer of a given initial size for XmlEvents.
     * @param namespaces the applicable <code>Namespaces</code> object
     * @param size the initial size of the buffer
     * @throws IllegalArgumentException
     */
    public XmlEventsArrayBuffer(Namespaces namespaces, int size)
        throws IllegalArgumentException
    {
        this.namespaces = namespaces;
        buffer = new ArrayList(size);
    }

    /**
     * Get the <tt>Namespaces</tt> from this buffer.
     * @return - the namespaces object.
     */
    public Namespaces getNamespaces() { return namespaces; }

    /**
     * @return next event from the buffer
     * @exception FOPException  exception into which
     * any InterruptedException exceptions thrown by the
     * <tt>SyncedCircularBuffer</tt> are transformed
     */
    public XmlEvent getEvent() throws FOPException {
        synchronized (buffer) {
            XmlEvent ev;
            if (nextEvent < buffer.size()) {
                ev = (XmlEvent)(buffer.get(nextEvent++));
                bufferRead = true;
                return ev;
            }
            throw new FOPException("XmlEventsArrayBuffer empty");
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.xml.XmlEventSource#pushBack(org.apache.fop.xml.XmlEvent)
     */
    public void pushBack(XmlEvent event) {
        // TODO Should check for legality of this operation - this will involve
        // a change to the SyncedXmlEventsBuffer implementation and the
        // XmlEventSource interface.
        if (nextEvent <= 0) {
            throw new IndexOutOfBoundsException("No events read from buffer");
        }
        nextEvent--;
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.xml.XmlEventSource#isExhausted()
     */
    public boolean isExhausted() {
        return nextEvent >= buffer.size();
    }

    /**
     * Add event to this buffer.  This is only legal before any events have
     * been read from the buffer.
     * @param event to add
     * @throws FOPException if buffer has been read
     */
    public void addEvent(XmlEvent event) throws FOPException {
        if (nextEvent > 0) {
            throw new FOPException(
                    "Buffer has been accessed - addEvent is illegal");
        }
        buffer.add(event);
    }
    
    public void rewindBuffer() {
        // Note - buffer may not be extended after a rewind.  If this is a
        // requirement, set bufferRead to false.
        nextEvent = 0;
    }
    
    public void emptyBuffer() {
        rewindBuffer();
        for (int i = buffer.size(); i >= 0; ) {
            namespaces.relinquishEvent((XmlEvent)buffer.get(--i));
            buffer.remove(i);
        }
    }
    
}
