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
import org.apache.fop.pool.*;

/**
 * Maintains the namespaces encountered by an invocation of
 *  <tt>XMLSerialHandler</tt>.
 * One instance of <i>Namespaces</i> is maintained across all documents
 * that may be processed in a single invocation of <tt>XMLSerialhandler</tt>.
 * A reference to that instance is kept with every instance of <tt>XmlEvent</tt>.
 * An <code>XmlEventPool</code> pool of event objects is maintained for every
 * namesapce encountered in parsing. The pool for the
 * http://www.w3.org/1999/XSL/Format (XSL_FO) namespace is created immediately;
 * other pools are created only as elements from a particular namespace are
 * encountered.
 */

public class Namespaces {

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
    
    /**
     * Generic undefined type for namespace-specific event types.
     */
    public static final int NO_NS_TYPE = -1;
    
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
     * An array of <code>XmlEventPool</code>s. This ArrayList is indexed by
     * the namespace index used in this <code>Namespaces</code> object.
     * This allows for the maintenance of individual event pools for each
     * namespace active in the current document.
     */
    private XmlEventPool[] pools = new XmlEventPool[LAST_NS_INDEX + 1];
    
    /**
     * The pool for <code>UriLocalName</code> objects.
     */
    private UriLocalNamePool uriLocalNamePool;

    /**
     * Sequenced objects for use by <tt>XmlEvent</tt>s. Because an
     * <tt>XmlEvent</tt> object must always be associated with an
     * <i>XMLNamespace</i> object, this namespace object will act as a
     * singleton for <tt>XmlEvent</tt>s. This field provides a counter for
     * those objects. The range of values which may be assigned to
     * <i>nsSequences</i> is restricted by <i>nsSeqMasks</i>.
     */
    private int nsSequences[] = new int[LAST_NS_INDEX + 1];
    
    /**
     * This field is used to provide sequence numbers for
     * <code>Poolable UriLocalName</code> objects.
     */
    private int uriLocalSeq = 0;

    /**
     * Number of bits in the nsSequences mask for the associated namespace.
     * This value will determine the number of nsSequences values the pool
     * for the associated namespace will track.
     */
    private static final int DEF_ATTR_SEQ_BITS = 14
                               ,FO_SEQ_BITS = 18
                               ,FOX_SEQ_BITS = 14
                               ,SVG_SEQ_BITS = 16
                               ;

    /**
     * Masks to restrict the range of values within which the nsSequences value
     * for each namespace may cycle.
     */
    private final int[] nsSeqMasks =
        {
            (1 << DEF_ATTR_SEQ_BITS) - 1
            ,(1 << FO_SEQ_BITS) - 1
            ,(1 << FOX_SEQ_BITS) - 1
            ,(1 << SVG_SEQ_BITS) - 1 };
    
    /**
     * Mask to restrict the range of values within which uriLocalSeq will
     * cycle.
     */
    private final int uriLocalSeqMask = (1 << FO_SEQ_BITS) - 1;

    public Namespaces() {
        for (int i = 0; i <= LAST_NS_INDEX; i++) {
            pools[i] = null;
            nsSequences[i] = 0;
        }
        pools[DefAttrNSIndex] =
            new XmlEventPool(INITIAL_DEF_ATTR_NS_POOL_SIZE);
        pools[XSLNSpaceIndex] =
            new XmlEventPool(INITIAL_XSL_NS_POOL_SIZE);
        pools[FOXNSpaceIndex] =
            new XmlEventPool(INITIAL_FOX_NS_POOL_SIZE);
        pools[SVGNSpaceIndex] =
            new XmlEventPool(INITIAL_SVG_NS_POOL_SIZE);
        uriLocalNamePool = new UriLocalNamePool(BUFFER_SIZE);
    }
    
    /**
     * The increment access function for the nsSequences associated with the given
     * URI index.
     * 
     * @param nsIndex
     *            the namespace index
     * @return the next positive nsSequences number. This number may wrap but is
     *         guaranteed to be within the range nsSeqMasks >= nsSequences >= 0.
     * @throws FOPException
     *             if the namespace index is out of range
     */
    public int getNextSequence(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > LAST_NS_INDEX) {
            throw new FOPException(
                "Namespace index " + nsIndex + " out of range.");
        }
        synchronized (nsSequences) {
            nsSequences[nsIndex] = ++nsSequences[nsIndex] & nsSeqMasks[nsIndex];
            return nsSequences[nsIndex];
        }
    }

    /**
     * The access function for the nsSequences associated with the given URI
     * index.
     * 
     * @param nsIndex
     *            the namespace index
     * @return the current nsSequences number
     * @throws FOPException
     *             if the index is out of range
     */
    public int getSequenceValue(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > LAST_NS_INDEX) {
            throw new FOPException(
                "Namespace index " + nsIndex + " out of range.");
        }
        return nsSequences[nsIndex];
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
     * Generate a new XmlEvent, based on the uriIndex argument. The uriIndex
     * must be valid for this Namespaces object; i.e. it must correspond to
     * a namespace being tracked in this object.
     * 
     * @param nsIndex
     *            the namespace index
     * @return an appropriate <code>XmlEvent</code>
     */
    public XmlEvent makeXMLEvent(int nsIndex) throws FOPException {
        if (nsIndex < 0 | nsIndex >= (uris.length)) {
            throw new FOPException("URI index out or range");
        }
        return newXMLEvent(nsIndex);
    }

    /**
     * Generate a new XmlEvent, based on the uriIndex argument. This
     * private method is for interanl use when the <code>nsIndex</code> is
     * known to be in range.
     * 
     * @param nsIndex
     *            the namespace index
     * @return an appropriate <code>XmlEvent</code>
     */
    private XmlEvent newXMLEvent(int nsIndex) {
        // The only currently known subclass of XmlEvent is FoXmlEvent
        switch (nsIndex) {
        case DefAttrNSIndex :
            // Produce an XmlEvent, e.g. for START_DOCUMENT and, more
            // importantly, CHARACTERS.
            synchronized (nsSequences) {
                nsSequences[nsIndex] =
                    ++nsSequences[nsIndex] & nsSeqMasks[nsIndex];
                return new XmlEvent(this, nsSequences[nsIndex], nsIndex);
            }
        case XSLNSpaceIndex :
            // Make an FoXmlEvent
            synchronized (nsSequences) {
                nsSequences[nsIndex] =
                    ++nsSequences[nsIndex] & nsSeqMasks[nsIndex];
                return new FoXmlEvent(this, nsSequences[nsIndex], nsIndex);
            }
        case FOXNSpaceIndex :
            // No FoxXMLEvent defined - don't break, but fall through
        case SVGNSpaceIndex :
            // No SvgXMLEvent defined - don't break, but fall through
        default :
            // Just produce a raw XmlEvent
            synchronized (nsSequences) {
                nsSequences[nsIndex] =
                    ++nsSequences[nsIndex] & nsSeqMasks[nsIndex];
                return new XmlEvent(this, nsSequences[nsIndex], nsIndex);
            }
        }
    }

    /**
     * Acquire an event.
     * 
     * @return an <tt>XmlEvent</tt>.
     */
    public XmlEvent acquireXMLEvent(int nsIndex) throws FOPException {
        if (nsIndex < 0 || nsIndex > uris.length) {
            throw new FOPException("URI index out of range: " + nsIndex);
        }
        XmlEvent ev;
        if ((ev = pools[nsIndex].acquireXMLEvent()) != null) {
            return ev;
        }
        ev = makeXMLEvent(nsIndex);
        return ev;
    }
    
    /**
     * Relinquish an event.  This method selects the appropriate pool
     * according to the event namespace index, and passes the request to
     * the pool.
     * @param event to relinquish
     */
    public void relinquishEvent(XmlEvent event) {
        pools[event.uriIndex].relinquishPoolable(event);
    }

    /**
     * Acquire a <code>UriLocalName</code> from the pool, or null if no
     * pooled names exist.
     * 
     * @return a <tt>UriLocalName</tt> or null.
     */
    public UriLocalName acquireUriLocalName() {
        return uriLocalNamePool.acquireUriLocalName();
    }
    
    /**
     * Relinquish a <code>UriLocalName</code>.
     * The name is returned to the pool.
     * @param uriName to relinquish
     */
    public void relinquishUriLocalName(UriLocalName uriName) {
        uriLocalNamePool.relinquishPoolable(uriName);
    }
    
    /**
     * Get the size of the event pool for a given namespace.
     * 
     * @param nsIndex
     *            the index of the namespace
     * @return pool size.
     */
    public int getNSPoolSize(int nsIndex) {
        return pools[nsIndex].getPoolSize();
    }

}
