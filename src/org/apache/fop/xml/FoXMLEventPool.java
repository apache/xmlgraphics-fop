package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.XMLEventPool;

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
 * This class provides a pool of <tt>FoXMLEvent</tt> objects.
 */

public class FoXMLEventPool extends XMLEventPool {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     */
    public FoXMLEventPool(XMLNamespaces namespaces) {
        super(namespaces);
    }

    /**
     * Acquire an <tt>FoXMLEvent</tt>.
     * @return an <tt>FoXMLEvent</tt>.
     */
    public synchronized FoXMLEvent acquireFoXMLEvent() {
        return (FoXMLEvent)(acquireXMLEvent());
    }

    /**
     * Return an <tt>FoXMLEvent</tt> to the pool.
     * @param ev - the event being returned.
     */
    public synchronized void surrenderFoXMLEvent(FoXMLEvent ev) {
        ev.clear();
        pool.add(ev);
    }

    /**
     * Get the <tt>XMLNamespaces</tt> object associated with this pool.
     * @return the <tt>XMLNamespaces</tt> object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

}
