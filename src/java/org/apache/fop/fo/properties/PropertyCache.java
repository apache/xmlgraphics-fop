/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.properties;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread-safe cache that minimizes the memory requirements by fetching an instance from the cache
 * that is equal to the given one. Internally the instances are stored in WeakReferences in order to
 * be reclaimed when they are no longer referenced.
 * @param <T> The type of values that are cached
 */
public final class PropertyCache<T> {

    private static final Log LOG = LogFactory.getLog(PropertyCache.class);

    /**
     * Determines if the cache is used based on the value of the system property
     * org.apache.fop.fo.properties.use-cache
     */
    private final boolean useCache;

    /**
     * The underlying map that stores WeakReferences to the cached entries. The map keys are the
     * hashCode of the cached entries. The map values are a WeakRefence to the cached entries. When
     * two cached entries have the same hash code, the last one is kept but this should be an
     * exception case (otherwise the hashCode() method of T needs to be fixed).
     */
    private final ConcurrentMap<Integer, WeakReference<T>> map;

    /**
     * Counts the number of entries put in the map in order to periodically check and remove the
     * entries whose referents have been reclaimed.
     */
    private final AtomicInteger putCounter;

    /**
     * Lock to prevent concurrent cleanup of the map.
     */
    private final Lock cleanupLock;

    private final AtomicInteger hashCodeCollisionCounter;

    /**
     * Creates a new cache. The "org.apache.fop.fo.properties.use-cache" system
     * property is used to determine whether properties should actually be
     * cached or not. If not, then the {@link #fetch(Object)} method will simply
     * return its argument. To enable the cache, set this property to "true"
     * (case insensitive).
     */
    public PropertyCache() {
        boolean useCache;
        try {
            useCache = Boolean.valueOf(
                    System.getProperty("org.apache.fop.fo.properties.use-cache", "true"))
                    .booleanValue();
        } catch ( SecurityException e ) {
            useCache = false;
        }
        if ( useCache ) {
            this.map = new ConcurrentHashMap<Integer, WeakReference<T>>();
            this.putCounter = new AtomicInteger();
            this.cleanupLock = new ReentrantLock();
            this.hashCodeCollisionCounter = new AtomicInteger();
        } else {
            this.map = null;
            this.putCounter = null;
            this.cleanupLock = null;
            this.hashCodeCollisionCounter = null;
        }
        this.useCache = useCache;
    }

    /**
     * Returns a cached version of the given object. If the object is not yet in
     * the cache, it will be added and then returned.
     *
     * @param obj an object
     * @return a cached version of the object
     */
    public T fetch(T obj) {
        if (!this.useCache) {
            return obj;
        }

        if (obj == null) {
            return null;
        }

        Integer hashCode = Integer.valueOf(obj.hashCode());

        WeakReference<T> weakRef = map.get(hashCode);
        if (weakRef == null) {
            weakRef = map.putIfAbsent(hashCode, new WeakReference<T>(obj));
            attemptCleanup();

            if (weakRef == null) {
                return obj;
            }
            // else another thread added a value, continue.
        }

        T cached = weakRef.get();
        if (cached != null) {
            if (eq(cached, obj)) {
                return cached;
            } else {
                /*
                 * Log a message when obj.getClass() does not implement correctly the equals() or
                 * hashCode() method. It is expected that only very few objects will have the
                 * same hashCode but will not be equal.
                 */
                if ((hashCodeCollisionCounter.incrementAndGet() % 10) == 0) {
                    LOG.info(hashCodeCollisionCounter.get() + " hashCode() collisions for "
                            + obj.getClass().getName());
                }
            }

        }

        // Adds a new or replaces an existing entry with obj that has the same hash code
        map.put(hashCode, new WeakReference<T>(obj));
        attemptCleanup();
        return obj;

        /*
         * Another thread might add first. We could check this using map.replace() instead of
         * map.put() and then recursively call fetch(obj). But if in the meantime, garbage
         * collection kicks in, we might end up with a StackOverflowException. Not caching an entry
         * is tolerable, after all it's configurable.
         */
    }


    private void attemptCleanup() {
        if ((putCounter.incrementAndGet() % 10000) != 0) {
            return;
        }

        // Lock as there is no need for concurrent cleanup and protect us, on JDK5, from
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6312056
        if (cleanupLock.tryLock()) {
            try {
                cleanReclaimedMapEntries();
            } finally {
                cleanupLock.unlock();
            }
        }
    }

    private void cleanReclaimedMapEntries() {
        Iterator<Map.Entry<Integer, WeakReference<T>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, WeakReference<T>> entry = iterator.next();
            WeakReference<T> weakRef = entry.getValue();
            T r = weakRef.get();
            if (r == null) {
                iterator.remove();
            }
        }
    }

    private boolean eq(Object p, Object q) {
        return (p == q || p.equals(q));
    }
}
