package org.apache.fop.xml;

import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.ArrayList;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
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
