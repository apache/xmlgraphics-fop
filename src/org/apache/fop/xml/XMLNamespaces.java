/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * Maintains the namespaces encountered by an invocation of
 * <tt>XMLSerialHandler</tt>.
 * <p>One instance of <i>XMLNamespaces</i> is maintained across all
 * documents that may be processed in a single invocation of
 * <tt>XMLSerialhandler</tt>.  A reference to that instance is kept with
 * every instance of <tt>XMLEvent</tt>.
 */

public class XMLNamespaces {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static final String DefAttrNSpace = "";
    public static final String XSLNamespace =
        "http://www.w3.org/1999/XSL/Format";
    public static final String SVGNamespace = "http://www.w3.org/2000/svg";
    public static final String XlinkNamespace =
        "http://www.w3.org/1999/xlink";
    public static final int DefAttrNSIndex = 0;
    public static final int XSLNSpaceIndex = 1;
    public static final int SVGNSpaceIndex = 2;
    public static final int XLinkNSpaceIndex = 3;

    /**
     * A <tt>HashMap</tt> mapping a namespace URI to an <tt>int</tt>
     * index.  The HashMap is initialized with a few well-known URIs.  As
     * URIs are encountered in parsing, they are converted to an integer
     * index by lookup in this HashMap.  If the URI has not been seen, it
     * is added to the <i>uriIndices</i> and <i>uris</i> for future reference.
     * <b>It is vital that no URI, once added, ever be deleted
     * from this <tt>HashMap</tt></b>.
     * <p><tt>HashMap</> is unsynchronized, so accesses and updates must be
     * protected.
     * <p>
     * Updates will be very rare, and accesses are directly related to the
     * number of elements (and attributes) encountered.
     */
    private HashMap uriIndices;
    /**
     * A <tt>ArrayList</tt> of namespace URIs.  Effectively, a mapping of
     * an <tt>int</tt> index onto a URI.
     * ArrayList is initialized with a few well-known URIs.  As
     * URIs are encountered in parsing, they are converted to an integer
     * index by lookup in the <i>uriIndices</i> Hashmap. If the URI has not
     * been seen, it is added to <i>uriIndices</i> and <i>uris</i>
     * for future reference.
     * <p>
     * <tt>ArrayList</> is unsynchronized, so access and updates must be
     * protected.  Both will be more rare than accesses to <i>uriIndices</i>.
     */
    private ArrayList uris;

    /**
     * A sequence object for use by <tt>XMLEvent</tt>s.  Because an
     * <tt>XMLEvent</tt> object must always be associated with an
     * <i>XMLNamespace</i> object, this namespace object will act as a
     * singleton for <tt>XMLEvent</tt>s.  This field provides a
     * counter for those objects.  The range of values which may be
     * assigned to <i>sequence</i> is restricted by <i>seqMask</i>.
     */
    private int sequence = 0;

    /** Mask to restrict the range of values within which <i>sequence</i>
     * may cycle.
     */
    public final int seqMask = (1 << 20) - 1;

    /**
     * The access function for the sequence.
     * @return the next positive sequence number.  This number may wrap
     * but is guaranteed to be within the range seqMask >= sequence >= 0.
     */
    public int getSequence() {
        sequence = ++sequence & seqMask;
        return sequence;
    }

    public XMLNamespaces() {
        uriIndices = new HashMap(4);
        uris = new ArrayList(4);
        uriIndices.put(DefAttrNSpace, Ints.consts.get(DefAttrNSIndex));
        uris.add(DefAttrNSIndex, DefAttrNSpace);
        uriIndices.put(XSLNamespace, Ints.consts.get(XSLNSpaceIndex));
        uris.add(XSLNSpaceIndex, XSLNamespace);
        uriIndices.put(SVGNamespace, Ints.consts.get(SVGNSpaceIndex));
        uris.add(SVGNSpaceIndex, SVGNamespace);
        uriIndices.put(XlinkNamespace, Ints.consts.get(XLinkNSpaceIndex));
        uris.add(XLinkNSpaceIndex, XlinkNamespace);
    }

    /**
     * @return size of the <tt>uris</tt> <ttArrayList</tt>.
     */
    public synchronized int getUrisSize() {
        return uris.size();
    }

    /**
     * If the URI is not pre-defined, and has not been seen before, add
     * it to the stored namespaces, and return the index.
     * @param uri the namespace uri
     * @return integer index of the namespace URI
     */
    public synchronized int getURIIndex(String uri) {
        int i;
        Integer intg = (Integer)uriIndices.get(uri);
        if (intg == null) {
            // update the indices
            i = uris.size();
            //System.out.println("****Adding namespace " + uri + " " + i);
            uriIndices.put(uri, Ints.consts.get(i));
            uris.add(i, uri);
            return i;
        }
        // not null - found the integer
        return intg.intValue();
    }

    /**
     * @param index the integer index of the namespace URI
     * @return the corresponding namespace URI
     */
    public synchronized String getIndexURI(int index) {
        return (String)uris.get(index);
    }

}
