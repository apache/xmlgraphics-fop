package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;

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
 * This class provides a pool of <tt>XMLEvent</tt> objects.
 */

public class XMLEventPool {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Required argument for constructing new <tt>XMLEvent</tt>s. */
    protected XMLNamespaces namespaces;
    /** The pool realized as a ArrayList. */
    protected ArrayList pool;
    /** The number of events in the list. */
    protected int poolSize = 0;
    /** The maximum number of events in the list. */
    protected int maxPoolSize = 0;

    /**
     * The one-argument constructor requires <i>namespaces</i>.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     */
    public XMLEventPool(XMLNamespaces namespaces) {
        this.namespaces = namespaces;
        pool = new ArrayList();
    }

    /**
     * Construct a pool with a given initial size.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     * @param initialSize - the initial size of the pool.
     */
    public XMLEventPool(XMLNamespaces namespaces, int initialSize) {
        this.namespaces = namespaces;
        pool = new ArrayList(initialSize);
    }

    /**
     * Acquire an <tt>XMLEvent</tt>.
     * @return an <tt>XMLEvent</tt>.
     */
    public synchronized XMLEvent acquireXMLEvent() {
        if (poolSize == 0)
            return new XMLEvent(namespaces);
        return ((XMLEvent)(pool.get(--poolSize))).clear();
    }

    /**
     * Return an <tt>XMLEvent</tt> to the pool.
     * @param ev - the event being returned.
     */
    public synchronized void surrenderEvent(XMLEvent ev) {
        if (maxPoolSize > poolSize)
            pool.set(poolSize++, ev);
        else {
            pool.add(ev);
            poolSize++;
            maxPoolSize = poolSize;
        }
    }

    /**
     * Get the <tt>XMLNamespaces</tt> object associated with this pool.
     * @return the <tt>XMLNamespaces</tt> object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

}
