/*
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * $Id$
 */
package org.apache.fop.pool;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Logger;

import org.apache.fop.apps.Fop;

/**
 * This class provides a pool of <tt>Poolable</tt> objects.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public abstract class FopPool {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    
    protected static final Logger logger = Logger.getLogger(Fop.fopPackage);

    /** The pool realized as a ArrayList. */
    protected final ArrayList pool;
    /** The number of events in the list. */
    protected int poolSize = 0;
    /** The maximum number of events in the list. */
    protected int maxPoolSize = 0;

    /**
     * Set of currently pooled events.  The size of this set is limited by
     * the range of values that the <tt>Poolable</tt> <i>id</i> field can
     * assume.
     * If there is a significant disparity between the frequency of
     * pool acquire and relinquish invocations, an id clash may arise in
     * the current set.
     */
    protected final BitSet eventSet;

    /**
     * The zero-argument constructor
     */
    public FopPool() {
        pool = new ArrayList();
        eventSet = new BitSet();
    }
    
    /**
     * Construct a pool with a given initial size.
     * @param initialSize - the initial size of the pool.
     */
    public FopPool(int initialSize) {
        pool = new ArrayList(initialSize);
        eventSet = new BitSet(initialSize);
    }

    /**
     * Acquire a <tt>Poolable</tt> object.
     * @return a <tt>Poolable</tt> object from the current pool, or null
     * if no obect is available.  The <code>clear()</code> method is
     * invoked on the object before it is returned.
     */
    public synchronized Poolable acquirePoolable() {
        if (poolSize == 0)
            return null;
        Poolable ev = ((Poolable)(pool.get(--poolSize))).clear();
        eventSet.clear(ev.id);
        return ev;
    }

    /**
     * Return an <tt>Poolable</tt> to the pool.
     * @param ev - the event being returned.
     */
    public synchronized void relinquishPoolable(Poolable ev) {
        if (ev == null) return;
        if (eventSet.get(ev.id)) {
            logger.warning
                    ("Event clash in Poolable pool. Id " + ev.id);
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
     * Get the size of the event pool.
     * @return pool size.
     */
    public int getPoolSize() { return pool.size(); }

}
