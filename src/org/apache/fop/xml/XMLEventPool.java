package org.apache.fop.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.messaging.MessageHandler;

import java.util.ArrayList;
import java.util.BitSet;

/*
 * $Id$
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
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
    protected final XMLNamespaces namespaces;
    /** The pool realized as a ArrayList. */
    protected final ArrayList pool;
    /** The number of events in the list. */
    protected int poolSize = 0;
    /** The maximum number of events in the list. */
    protected int maxPoolSize = 0;

    /**
     * Set of currently pooled events.  The size of this set is limited by
     * the range of values that the <tt>XMLEvent</tt> <i>id</i> field can
     * assume.  This, in turn, is limited by to the range of values returned
     * by the <tt>XMLNamespaces</tt> <i>getSequence()</i> method.
     * If there is a signficant disparity between the frequency of
     * pool acquire and surrender invocations, an id clash may arise in
     * the current set.
     */
    protected final BitSet eventSet;

    /**
     * The one-argument constructor requires <i>namespaces</i>.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     */
    public XMLEventPool(XMLNamespaces namespaces) {
        this.namespaces = namespaces;
        pool = new ArrayList();
        eventSet = new BitSet();
    }

    /**
     * Construct a pool with a given initial size.
     * @param namespaces - an <tt>XMLNamespaces</tt> object.
     * @param initialSize - the initial size of the pool.
     */
    public XMLEventPool(XMLNamespaces namespaces, int initialSize) {
        this.namespaces = namespaces;
        pool = new ArrayList(initialSize);
        eventSet = new BitSet(initialSize);
    }

    /**
     * Acquire an <tt>XMLEvent</tt>.
     * @return an <tt>XMLEvent</tt>.
     */
    public synchronized XMLEvent acquireXMLEvent() {
        if (poolSize == 0)
            return new XMLEvent(namespaces);
        XMLEvent ev = ((XMLEvent)(pool.get(--poolSize))).clear();
        eventSet.clear(ev.id);
        return ev;
    }

    /**
     * Return an <tt>XMLEvent</tt> to the pool.
     * @param ev - the event being returned.
     */
    public synchronized void surrenderEvent(XMLEvent ev) {
        //System.out.println("surrenderEvent " + ev.id
                                           //+ "  poolSize " + poolSize);
        if (ev == null) return;
        if (eventSet.get(ev.id)) {
            //System.out.println("Event clash: " + ev);
            MessageHandler.logln
                    ("Event clash in XMLEvent pool. Id " + ev.id);
            return;
        }
        eventSet.set(ev.id);
        if (pool.size() > poolSize)
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

    /**
     * Get the size of the event pool.
     * @return pool size.
     */
    public int getPoolSize() { return pool.size(); }

}
