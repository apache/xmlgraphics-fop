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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.SyncedCircularBuffer;

/**
 * A synchronized circular buffer for XMLEvents.
 * Extends SyncedCircularBuffer and implements XmlEventSource.
 * 
 * @see org.apache.fop.datastructs.SyncedCircularBuffer
 * @see XmlEventSource
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class SyncedXmlEventsBuffer
extends SyncedCircularBuffer
implements XmlEventSource {

    /**
     * Maintains an index of namespace URIs.  These can then be referred to
     * by an <tt>int</tt> index.
     */
    private Namespaces namespaces;

    /**
     * One-argument constructor sets up a buffer with the default number of
     * elements and the "global" <code>Namespaces</code> object.
     * The producer and consumer <tt>Thread</tt>s default to the current
     * thread at the time of instantiation.
     * @param namespaces object for this <code>Driver</code> instance
     * @throws IllegalArgumentException
     */
    public SyncedXmlEventsBuffer(Namespaces namespaces)
        throws IllegalArgumentException
    {
        this.namespaces = namespaces;
    }

    /**
     * Constructs a buffer with <code>size</code> elements and the "global"
     * <code>Namespaces</code> object.
     * @param namespaces object for this <code>Driver</code> instance
     * @param size the size of the buffer.  Must be > 1.
     */
    public SyncedXmlEventsBuffer(Namespaces namespaces, int size)
        throws IllegalArgumentException
    {
        super(size);
        this.namespaces = namespaces;
    }

    /**
     * Get the <tt>Namespaces</tt> from this buffer.
     * @return - the namespaces object.
     */
    public Namespaces getNamespaces() { return namespaces; }

    /**
     * @return next event from the SyncedCircularBuffer
     * @exception FOPException  exception into which
     * any InterruptedException exceptions thrown by the
     * <tt>SyncedCircularBuffer</tt> are transformed
     */
    public XmlEvent getEvent() throws FOPException {
        XmlEvent ev;
        try {
            ev = (XmlEvent)get();
            //System.out.println("getEvent: " + ev);
            return ev;
        } catch (InterruptedException e) {
            throw new FOPException(e);
        }
    }

    /**
     * Push back an event into the buffer; generally this will be an
     * evnet previously obtained by a <tt>get</tt> from the buffer.
     * This implementation supports a single entry pushback buffer.
     * @param event to push back
     */
    public void pushBack (XmlEvent event) {
        super.pushBack(event);
    }

}
