package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.XMLEventPool;

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
     * The one-argument constructor requires <i>namespaces</i>.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     */
    public FoXMLEventPool(XMLNamespaces namespaces) {
        super(namespaces);
    }

    /**
     * Construct a pool with a given initial size.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     * @param initialSize - the initial size of the pool.
     */
    public FoXMLEventPool(XMLNamespaces namespaces, int initialSize) {
        super(namespaces, initialSize);
    }

    /**
     * Acquire an <tt>FoXMLEvent</tt>.
     * @return an <tt>FoXMLEvent</tt>.
     */
    public synchronized FoXMLEvent acquireFoXMLEvent() {
        FoXMLEvent ev = (FoXMLEvent)(acquireXMLEvent());
        ev.setFoType(FObjectNames.NO_FO);
        return ev;
    }

    /**
     * Get the <tt>XMLNamespaces</tt> object associated with this pool.
     * @return the <tt>XMLNamespaces</tt> object.
     */
    public XMLNamespaces getNamespaces() { return namespaces; }

}
