package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;

import java.util.LinkedList;

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
    /** The pool realized as a LinkedList. */
    protected LinkedList pool = new LinkedList();

    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     */
    public XMLEventPool(XMLNamespaces namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Acquire an <tt>XMLEvent</tt>.
     * @return an <tt>XMLEvent</tt>.
     */
    public synchronized XMLEvent acquireXMLEvent() {
        if (pool.size() == 0)
            return new XMLEvent(namespaces);
        return (XMLEvent)(pool.removeLast());
    }

    /**
     * Return an <tt>XMLEvent</tt> to the pool.
     * @param ev - the event being returned.
     */
    public synchronized void surrenderXMLEvent(XMLEvent ev) {
        ev.clear();
        pool.add(ev);
    }

    /**
     * Get the <tt>XMLNamespaces</tt> object associated with this pool.
     * @return the <tt>XMLNamespaces</tt> object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

}
