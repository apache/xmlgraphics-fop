/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
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
public class ArrayXmlEventsBuffer
implements XmlEventSource {

    /**
     * Constant for <i>discardEvent</i> field of
     * <i>getEndElement(boolean discardEvent, XmlEvent(, boolean)).
     */
    public static final boolean DISCARD_EV = true,
                                 RETAIN_EV = false;

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
    public ArrayXmlEventsBuffer(Namespaces namespaces)
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
    public ArrayXmlEventsBuffer(Namespaces namespaces, int size)
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
            throw new FOPException("ArrayXmlEventsBuffer empty");
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.xml.XmlEventSource#pushBack(org.apache.fop.xml.XmlEvent)
     */
    public void pushBack(XmlEvent event) {
        // TODO Should check for legality of this operation - this will involve
        // a change to the SyncedXmlEventsBuffer implementation and the
        // XmlEventSource interface.
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
    public void pushEvent(XmlEvent event) throws FOPException {
        if (nextEvent > 0) {
            throw new FOPException(
                    "Buffer has been accessed - pushEvent is illegal");
        }
        buffer.add(event);
    }
    
    public void rewindBuffer() {
        // Note - buffer may not be extended after a rewind.  If this is a
        // requirement, set bufferRead to false.
        nextEvent = 0;
    }
}
