/*
 * $Id$
 * 
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The end-user documentation included with the redistribution, if any,
 * must include the following acknowledgment: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself, if and
 * wherever such third-party acknowledgments normally appear.
 *  4. The names "FOP" and "Apache Software Foundation" must not be used to
 * endorse or promote products derived from this software without prior written
 * permission. For written permission, please contact apache@apache.org.
 *  5. Products derived from this software may not be called "Apache", nor may
 * "Apache" appear in their name, without prior written permission of the
 * Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com> . For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/> .
 * 
 * @author <a href="mailto:pbwest@powerup.com.au"> Peter B. West </a>
 * 
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.SyncedCircularBuffer;

/**
 * Maintains the namespaces encountered by an invocation of <tt>XMLSerialHandler</tt>.
 * <p>
 * One instance of <i>XMLNamespaces</i> is maintained across all documents
 * that may be processed in a single invocation of <tt>XMLSerialhandler</tt>.
 * A reference to that instance is kept with every instance of <tt>XMLEvent</tt>.
 * An <code>XMLEventPool</code> pool of event objects is maintained for every
 * namesapce encountered in parsing. The pool for the
 * http://www.w3.org/1999/XSL/Format (XSL_FO) namespace is created immediately;
 * other pools are created only as elements from a particular namespace are
 * encountered.
 */

public class XMLNamespaces {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** 
     * Null namespace, as for attribute default and for use with SAX events
     * without namespaces, e.g. START_DOCUMENT.
     */
    public static final String DefAttrNSpace = "";
    /** The XSL_FO namespace */
    public static final String XSLNamespace =
        "http://www.w3.org/1999/XSL/Format";
    /** The FOP extensions namespace */
    public static final String FOXNamespace =
        "http://xml.apache.org/fop/extensions";
    /** The SVG namespace */
    public static final String SVGNamespace = "http://www.w3.org/2000/svg";

    public static final int NO_NAMESPACE = -1;
    /** Index for associated namespace */
    public static final int  DefAttrNSIndex = 0
                               ,XSLNSpaceIndex = 1
                               ,FOXNSpaceIndex = 2
                               ,SVGNSpaceIndex = 3
                               ;
    /** Highest-numbered namespace index */
    public static final int LAST_NS_INDEX = SVGNSpaceIndex;
    
    private static final int BUFFER_SIZE
                                    = SyncedCircularBuffer.DEFAULTBUFSIZE;

    /** Initial size of pool for associated namespace */
    public static final int
        INITIAL_DEF_ATTR_NS_POOL_SIZE = BUFFER_SIZE / 2
            ,INITIAL_XSL_NS_POOL_SIZE = BUFFER_SIZE
            ,INITIAL_FOX_NS_POOL_SIZE = BUFFER_SIZE / 2
            ,INITIAL_SVG_NS_POOL_SIZE = BUFFER_SIZE / 2
            ;

    /**
     * An array of namespace URIs. Effectively, a mapping of an <tt>int</tt>
     * index onto a URI. The array is initialized with the known URIs which FOP
     * is expected to handle.
     */
    private String[] uris =
        { DefAttrNSpace, XSLNamespace, FOXNamespace, SVGNamespace };

    /**
     * An array of <code>XMLEventPool</code>s. This ArrayList is indexed by
     * the namespace index used in this <code>XMLNamespaces</code> object.
     * This allows for the maintenance of individual event pools for each
     * namespace active in the current document.
     */
    private XMLEventPool[] pools = new XMLEventPool[LAST_NS_INDEX + 1];

    /**
     * Sequence objects for use by <tt>XMLEvent</tt>s. Because an
     * <tt>XMLEvent</tt> object must always be associated with an
     * <i>XMLNamespace</i> object, this namespace object will act as a
     * singleton for <tt>XMLEvent</tt>s. This field provides a counter for
     * those objects. The range of values which may be assigned to
     * <i>sequence</i> is restricted by <i>seqMask</i>.
     */
    private int sequence[] = new int[LAST_NS_INDEX + 1];

    /**
     * Number of bits in the sequence mask for the associated namespace.
     * This value will determine the number of sequence values the pool
     * for the associated namespace will track.
     */
    private static final int DEF_ATTR_SEQ_BITS = 14
                               ,FO_SEQ_BITS = 18
                               ,FOX_SEQ_BITS = 14
                               ,SVG_SEQ_BITS = 16
                               ;

    /**
     * Masks to restrict the range of values within which the sequence value
     * for each namespace may cycle.
     */
    private final int[] seqMask =
        {
            (1 << DEF_ATTR_SEQ_BITS) - 1
            ,(1 << FO_SEQ_BITS) - 1
            ,(1 << FOX_SEQ_BITS) - 1
            ,(1 << SVG_SEQ_BITS) - 1 };

    public XMLNamespaces() {
        for (int i = 0; i <= LAST_NS_INDEX; i++) {
            pools[i] = null;
            sequence[i] = 0;
        }
        pools[DefAttrNSIndex] =
            new XMLEventPool(this, INITIAL_DEF_ATTR_NS_POOL_SIZE);
        pools[XSLNSpaceIndex] =
            new XMLEventPool(this, INITIAL_XSL_NS_POOL_SIZE);
        pools[FOXNSpaceIndex] =
            new XMLEventPool(this, INITIAL_FOX_NS_POOL_SIZE);
        pools[SVGNSpaceIndex] =
            new XMLEventPool(this, INITIAL_SVG_NS_POOL_SIZE);
    }
    
    /**
     * The increment access function for the sequence associated with the given
     * URI index.
     * 
     * @param nsIndex
     *            the namespace index
     * @return the next positive sequence number. This number may wrap but is
     *         guaranteed to be within the range seqMask >= sequence >= 0.
     * @throws FOPException
     *             if the namespace index is out of range
     */
    public int getNextSequence(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > LAST_NS_INDEX) {
            throw new FOPException(
                "Namespace index " + nsIndex + " out of range.");
        }
        synchronized (sequence) {
            sequence[nsIndex] = ++sequence[nsIndex] & seqMask[nsIndex];
            return sequence[nsIndex];
        }
    }

    /**
     * The access function for the sequence associated with the given URI
     * index.
     * 
     * @param nsIndex
     *            the namespace index
     * @return the current sequence number
     * @throws FOPException
     *             if the index is out of range
     */
    public int getSequenceValue(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > LAST_NS_INDEX) {
            throw new FOPException(
                "Namespace index " + nsIndex + " out of range.");
        }
        return sequence[nsIndex];
    }

    /**
     * @return size of the <tt>uris</tt> <ttArrayList</tt>.
     */
    public int getUrisLength() {
        return uris.length;
    }

    /**
     * Get the index corresponding to the parameter URI.
     * 
     * @param uri
     *            the namespace uri
     * @return integer index of the namespace URI
     * @throws FOPException
     *             if <code>uri</code> not recognized
     */
    public int getURIIndex(String uri) throws FOPException {
        int i;
        for (i = 0; i < uris.length; i++) {
            if (uri.equals(uris[i]))
                return i;
        }
        throw new FOPException("Unknown URI " + uri);
    }

    /**
     * @param index
     *            the integer index of the namespace URI
     * @return the corresponding namespace URI
     */
    public String getIndexURI(int index) {
        return uris[index];
    }

    /**
     * Generate a new XMLEvent, based on the uriIndex argument. The uriIndex
     * must be valid for this XMLNamespaces object; i.e. it must correspond to
     * a namespace being tracked in this object.
     * 
     * @param nsIndex
     *            the namespace index
     * @return an appropriate <code>XMLEvent</code>
     */
    public XMLEvent makeXMLEvent(int nsIndex) throws FOPException {
        if (nsIndex < 0 | nsIndex >= (uris.length)) {
            throw new FOPException("URI index out or range");
        }
        return newXMLEvent(nsIndex);
    }

    /**
     * Generate a new XMLEvent, based on the uriIndex argument. This
     * private method is for interanl use when the <code>nsIndex</code> is
     * known to be in range.
     * 
     * @param nsIndex
     *            the namespace index
     * @return an appropriate <code>XMLEvent</code>
     */
    private XMLEvent newXMLEvent(int nsIndex) {
        // The only currently known subclass of XMLEvent is FoXMLEvent
        switch (nsIndex) {
        case DefAttrNSIndex :
            // Produce an FoXMLEvent, e.g. for START_DOCUMENT
            // Note that FoXMLSerialHandler set the URI index for
            // CHARACTERS events.
            // This is problematical, but non-FO events can carry the
            // NO_FO type.  I think.
        case XSLNSpaceIndex :
            // Make an FoXMLEvent
            synchronized (sequence) {
                sequence[nsIndex] = ++sequence[nsIndex] & seqMask[nsIndex];
                return new FoXMLEvent(this, sequence[nsIndex]);
            }
        case FOXNSpaceIndex :
            // No FoxXMLEvent defined - don't break, but fall through
        case SVGNSpaceIndex :
            // No SvgXMLEvent defined - don't break, but fall through
        default :
            // Just produce a raw XMLEvent
            synchronized (sequence) {
                sequence[nsIndex] = ++sequence[nsIndex] & seqMask[nsIndex];
                return new XMLEvent(this, sequence[nsIndex]);
            }
        }
    }

    /**
     * Acquire an event.
     * 
     * @return an <tt>XMLEvent</tt>.
     */
    public XMLEvent acquireXMLEvent(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > uris.length) {
            throw new FOPException("URI index out of range: " + nsIndex);
        }
        XMLEvent ev;
        if ((ev = pools[nsIndex].acquireXMLEvent()) != null) {
            return ev;
        }
        ev = makeXMLEvent(nsIndex);
        return ev;
    }
    
    /**
     * Surrender an event.  This method selects the appropriate pool
     * according to the event namespace index, and passes the request to
     * the pool.
     * @param event to surrender
     */
    public void surrenderEvent(XMLEvent event) {
        pools[event.uriIndex].surrenderEvent(event);
    }

    /**
     * Get the size of the event pool for a given namespace.
     * 
     * @param nsIndex
     *            the index of the namespace
     * @return pool size.
     */
    public int getPoolSize(int nsIndex) {
        return pools[nsIndex].getPoolSize();
    }

}
