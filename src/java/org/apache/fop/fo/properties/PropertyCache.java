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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

/**
 *  Dedicated cache, meant for storing canonical instances
 *  of property-related classes.
 *  The public access point is a generic <code>fetch()</code> method.
 *  Internally, the instances are wrapped in a <code>java.lang.ref.WeakReference</code>,
 *  so that the actual instance only remains in the cache until no reference
 *  to that instance exists anywhere else (i.e. as long as it is needed).
 *  Classes that want to use this cache to store canonical instances should
 *  override {@link Object#hashCode()} and {@link Object#equals(Object)} to
 *  make sure the cache exhibits the expected behavior.
 *
 *  It is designed especially to be used concurrently by multiple threads,
 *  drawing heavily upon the principles behind Java 5's
 *  <code>ConcurrentHashMap</code>, but then limited to store only keys.
 *  (a more proper comparison would be a <code>ConcurrentWeakHashSet</code>)
 *
 * @param <T> the type of object that will be stored in the cache
 *
 */
//TODO: With generics, this actually has the potential of a more general utility class??
public final class PropertyCache<T> {

    private static final int SEGMENT_COUNT = 32; //0x20
    private static final int INITIAL_BUCKET_COUNT = SEGMENT_COUNT;

    /** bitmask to apply to the hash to get to the
     *  corresponding cache segment */
    private static final int SEGMENT_MASK = SEGMENT_COUNT - 1; //0x1F
    /**
     * Indicates whether the cache should be used at all
     * Can be controlled by the system property:
     *   org.apache.fop.fo.properties.use-cache
     */
    private final boolean useCache;

    /** the segments array (length = 32) */
    private final CacheSegment[] segments = new CacheSegment[SEGMENT_COUNT];
    /** the table of hash-buckets */
    @SuppressWarnings(value = "unchecked") //guaranteed by design
    private CacheEntry<T>[] table = new CacheEntry[INITIAL_BUCKET_COUNT];

    private Type runtimeType;

    private final boolean[] votesForRehash = new boolean[SEGMENT_COUNT];

    /* same hash function as used by java.util.HashMap */
    private static int hash(Object x) {
        return hash(x.hashCode());
    }

    private static int hash(int hashCode) {
        int h = hashCode;
        h += ~(h << 9);
        h ^= (h >>> 14);
        h += (h << 4);
        h ^= (h >>> 10);
        return h;
    }

    /* shortcut function */
    private static boolean eq(Object p, Object q) {
        return (p == q || (p != null && p.equals(q)));
    }

    /* Class modeling a cached entry */
    private static class CacheEntry<T> extends WeakReference<T> {
        private volatile CacheEntry<T> nextEntry;
        private final int hash;

        /* main constructor */
        @SuppressWarnings(value = "unchecked") //see below
        public CacheEntry(T p, CacheEntry<T> nextEntry, ReferenceQueue refQueue) {
            super(p, refQueue); //unchecked operation, but constructor unused?
            this.nextEntry = nextEntry;
            this.hash = hash(p);
        }

        /* main constructor */
        public CacheEntry(T p, CacheEntry<T> nextEntry) {
            super(p);
            this.nextEntry = nextEntry;
            this.hash = hash(p);
        }

    }

    /* Wrapper objects to synchronize on */
    private static final class CacheSegment {
        CacheSegment() {
        }
        private int count = 0;
        int getCount() {
            return count;
        }
    }

    private void cleanSegment(int segmentIndex) {
        CacheSegment segment = segments[segmentIndex];

        int oldCount = segment.count;

        /* clean all buckets in this segment */
        for (int bucketIndex = segmentIndex;
                    bucketIndex < table.length;
                    bucketIndex += SEGMENT_COUNT) {
            CacheEntry<T> prev = null;
            CacheEntry<T> entry = table[bucketIndex];
            if (entry == null) {
                continue;
            }
            do {
                if (entry.get() == null) {
                    if (prev == null) {
                        table[bucketIndex] = entry.nextEntry;
                    } else {
                        prev.nextEntry = entry.nextEntry;
                    }
                    segment.count--;
                    assert segment.count >= 0;
                } else {
                    prev = entry;
                }
                entry = entry.nextEntry;
            } while (entry != null);
        }

        synchronized (votesForRehash) {
            if (oldCount > segment.count) {
                votesForRehash[segmentIndex] = false;
                return;
            }
            /* cleanup had no effect */
            if (!votesForRehash[segmentIndex]) {
                /* first time for this segment */
                votesForRehash[segmentIndex] = true;
                int voteCount = 0;
                for (int i = SEGMENT_MASK + 1; --i >= 0;) {
                    if (votesForRehash[i]) {
                        voteCount++;
                    }
                }
                if (voteCount > SEGMENT_MASK / 4) {
                    rehash(SEGMENT_MASK);
                    /* reset votes */
                    for (int i = SEGMENT_MASK + 1; --i >= 0;) {
                        votesForRehash[i] = false;
                    }
                }
            }
        }
    }

    /*
     * Puts a new instance in the cache.
     * If the total number of entries for the corresponding
     * segment exceeds twice the amount of hash-buckets, a
     * cleanup will be performed to try and remove obsolete
     * entries.
     */
    private void put(T o) {

        int hash = hash(o);
        int segmentIndex = hash & SEGMENT_MASK;
        CacheSegment segment = segments[segmentIndex];

        synchronized (segments[segmentIndex]) {
            int index = hash & (table.length - 1);
            CacheEntry<T> entry = table[index];

            if (entry == null) {
                entry = new CacheEntry<T>(o, null);
                table[index] = entry;
                segment.count++;
            } else {
                T p = entry.get();
                if (eq(p, o)) {
                    return;
                } else {
                    CacheEntry<T> newEntry = new CacheEntry<T>(o, entry);
                    table[index] = newEntry;
                    segment.count++;
                }
            }

            if (segment.count > (2 * table.length)) {
                  cleanSegment(segmentIndex);
            }
        }
    }


    /* Gets a cached instance. Returns null if not found */
    private T get(T o) {

        int hash = hash(o);
        int index = hash & (table.length - 1);

        CacheEntry<T> entry = table[index];
        T q;

        /* try non-synched first */
        for (CacheEntry<T> e = entry; e != null; e = e.nextEntry) {
            if (e.hash == hash) {
                q = e.get();
                if ((q != null) && eq(q, o)) {
                    return q;
                }
            }
        }

        /* retry synched, only if the above attempt did not succeed,
         * as another thread may, in the meantime, have added a
         * corresponding entry */
        synchronized (segments[hash & SEGMENT_MASK]) {
            entry = table[index];
            for (CacheEntry<T> e = entry; e != null; e = e.nextEntry) {
                if (e.hash == hash) {
                    q = e.get();
                    if ((q != null) && eq(q, o)) {
                        return q;
                    }
                }
            }
        }
        return null;
    }

    /*
     * Recursively acquires locks on all 32 segments,
     * extends the cache and redistributes the entries.
     *
     */
    private void rehash(int index) {

        synchronized (segments[index]) {
            if (index > 0) {
                /* need to recursively acquire locks on all segments */
                rehash(index - 1);
            } else {
                /* double the amount of buckets */
                int newLength = table.length << 1;
                if (newLength > 0) { //no overflow?
                    /* reset segment counts */
                    for (int i = segments.length; --i >= 0;) {
                        segments[i].count = 0;
                    }

                    @SuppressWarnings(value = "unchecked") //guaranteed by design
                    CacheEntry<T>[] newTable = new CacheEntry[newLength];

                    int hash, idx;
                    T o;
                    newLength--;
                    for (int i = table.length; --i >= 0;) {
                        for (CacheEntry<T> c = table[i]; c != null; c = c.nextEntry) {
                            o = c.get();
                            if (o != null) {
                                hash = c.hash;
                                idx = hash & newLength;
                                newTable[idx] = new CacheEntry<T>(o, newTable[idx]);
                                segments[hash & SEGMENT_MASK].count++;
                            }
                        }
                    }
                    table = newTable;
                }
            }
        }
    }

    /*
     * Recursively acquires locks on all 32 segments,
     * counts all the entries, and returns the total number
     * of elements in the cache
     */
    private int size(int index) {
        synchronized (segments[index]) {
            if (index > 0) {
                /* need to recursively acquire locks on all segments */
                return size(index - 1);
            } else {
                int size = 0;
                T o;
                for (int i = table.length; --i >= 0;) {
                    for (CacheEntry<T> c = table[i]; c != null; c = c.nextEntry) {
                        o = c.get();
                        if (o != null) {
                            size++;
                        }
                    }
                }
                return size;
            }
        }
    }

    /**
     * Return the number of elements stored in this cache (approximation).
     * <br/><em>Note: only meant for use during debugging or unit/regression testing.
     * As the cache only keeps weak references, it is not feasible to cache this
     * number internally. This method will lock the entire cache and trigger
     * a recount upon every call.
     * While it is guaranteed that instances to which hard references still exist elsewhere,
     * will be present in the cache, it is not guaranteed that all the instances in the cache
     * are actually referenced. Hence, it is possible for this method to return different results
     * for subsequent calls, even though the {@link #fetch(Object)} method has not been called
     * in between, depending on the JVM (= implementation of WeakReference and GC)</em>
     * @return the number of elements stored in this cache (approx.)
     */
    protected int size() {
        return size(SEGMENT_MASK);
    }

    /**
     * Default constructor
     */
    public PropertyCache() {
        this(null);
    }

    /**
     * Alternate constructor. Can be used to set the runtimeType, in
     * order to facilitate tracking of specific caches.
     *
     * @param c    Runtime type of the objects that will be stored in the cache
     */
    protected PropertyCache(Class<T> c) {
        //TODO Tie this in to the config in FopFactory?
        // Should really avoid System.getProperty()...
        // See also Bugzilla #50435
        this.useCache = Boolean.valueOf(
                System.getProperty("org.apache.fop.fo.properties.use-cache", "true"));
        if (useCache) {
            for (int i = SEGMENT_MASK + 1; --i >= 0;) {
                segments[i] = new CacheSegment();
            }
        }
        this.runtimeType = c;
    }

    /**
     * Generic fetch() method.
     * Checks if an equivalent for the given instance is present in the cache.
     * If so, it returns a reference to the cached instance, and the object
     * passed in is discarded.
     * Otherwise the given object is added to the cache and returned.
     *
     * @param obj   the object to check for
     * @return  the cached instance
     */
    public T fetch(T obj) {
        if (!this.useCache) {
            return obj;
        }

        if (obj == null) {
            return null;
        }

        T cacheEntry = get(obj);
        if (cacheEntry != null) {
            return cacheEntry;
        }
        put(obj);
        return obj;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString() + "[runtimeType=" + this.runtimeType + "]";
    }

}
