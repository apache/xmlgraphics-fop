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

/**
 *  Dedicated cache, meant for storing canonical instances
 *  of property-related classes.
 *  The public access points are overloaded <code>fetch()</code> methods
 *  that each correspond to a cached type.
 *  It is designed especially to be used concurrently by multiple threads,
 *  drawing heavily upon the principles behind Java 1.5's 
 *  <code>ConcurrentHashMap</code>. 
 */
public final class PropertyCache {

    /** bitmask to apply to the hash to get to the 
     *  corresponding cache segment */
    private static final int SEGMENT_MASK = 0x1F;
    /** 
     * Indicates whether the cache should be used at all
     * Can be controlled by the system property:
     *   org.apache.fop.fo.properties.use-cache
     */
    private final boolean useCache;
    
    /** the segments array (length = 32) */
    private CacheSegment[] segments = new CacheSegment[SEGMENT_MASK + 1];
    /** the table of hash-buckets */
    private CacheEntry[] table = new CacheEntry[8];
    
    private Class runtimeType;
    
    final boolean[] votesForRehash = new boolean[SEGMENT_MASK + 1];
    
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
    private final class CacheEntry extends WeakReference {
        volatile CacheEntry nextEntry;
        final int hash;
        
        /* main constructor */
        public CacheEntry(Object p, CacheEntry nextEntry, ReferenceQueue refQueue) {
            super(p, refQueue);
            this.nextEntry = nextEntry;
            this.hash = p.hashCode();
        }
        
    }
    
    /* Wrapper objects to synchronize on */
    private final class CacheSegment {
        private int count = 0;
        private volatile ReferenceQueue staleEntries = new ReferenceQueue();
    }    
    
    private void cleanSegment(int segmentIndex) {
        CacheEntry entry;
        CacheSegment segment = segments[segmentIndex];
        int bucketIndex;
        int oldCount = segment.count;

        while ((entry = (CacheEntry) segment.staleEntries.poll()) != null) {
            bucketIndex = hash(entry.hash) & (table.length - 1);
            /* remove obsolete entry */
            /* 1. move to the corresponding entry */
            CacheEntry prev = null;
            CacheEntry e = table[bucketIndex];
            while (e != null
                    && e.nextEntry != null
                    && e.hash != entry.hash) {
                prev = e;
                e = e.nextEntry;
            }
            if (e != null) {
                /* 2. remove reference from the chain */
                if (prev == null) {
                    table[bucketIndex] = e.nextEntry;
                } else {
                    prev.nextEntry = e.nextEntry;
                }
                segment.count--;
            }
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
                for (int i = SEGMENT_MASK + 1; --i >= 0; ) {
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
    private void put(Object o) {
        
        int hash = hash(o);
        CacheSegment segment = segments[hash & SEGMENT_MASK];
        
        synchronized (segment) {
            int index = hash & (table.length - 1);
            CacheEntry entry = table[index];
            
            if (entry == null) {
                entry = new CacheEntry(o, null, segment.staleEntries);
                table[index] = entry;
                segment.count++;
            } else {
                Object p = entry.get();
                if (eq(p, o)) {
                    return;
                } else {
                    CacheEntry newEntry = new CacheEntry(o, entry, segment.staleEntries);
                    table[index] = newEntry;
                    segment.count++;
                }
            }
            
            if (segment.count > (2 * table.length)) {
                cleanSegment(hash & SEGMENT_MASK);
            }
        }
    }
    

    /* Gets a cached instance. Returns null if not found */
    private Object get(Object o) {
        
        int hash = hash(o);
        int index = hash & (table.length - 1);
        
        CacheEntry entry = table[index];
        Object q;
        
        /* try non-synched first */
        for (CacheEntry e = entry; e != null; e = e.nextEntry) {
            if (e.hash == o.hashCode()
                    && (q = e.get()) != null
                    &&  eq(q, o)) {
                return q;
            }
        }
        
        /* retry synched, only if the above attempt did not succeed,
         * as another thread may, in the meantime, have added a
         * corresponding entry */
        CacheSegment segment = segments[hash & SEGMENT_MASK];
        synchronized (segment) {
            entry = table[index];
            for (CacheEntry e = entry; e != null; e = e.nextEntry) {
                if (e.hash == o.hashCode()
                        && (q = e.get()) != null
                        &&  eq(q, o)) {
                    return q;
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
        
        CacheSegment seg = segments[index];
        synchronized (seg) {
            if (index > 0) {
                /* need to recursively acquire locks on all segments */
                rehash(index - 1);
            } else {
                /* double the amount of buckets */
                int newLength = table.length << 1;
                if (newLength > 0) { //no overflow?
                    /* reset segmentcounts */
                    for (int i = segments.length; --i >= 0;) {
                        segments[i].count = 0;
                    }
                    
                    CacheEntry[] newTable = new CacheEntry[newLength];
                    
                    int hash, idx;
                    Object o;
                    newLength--;
                    for (int i = table.length; --i >= 0;) {
                        for (CacheEntry c = table[i]; c != null; c = c.nextEntry) {
                            if ((o = c.get()) != null) {
                                hash = c.hash;
                                idx = hash & newLength;
                                newTable[idx] = new CacheEntry(o, newTable[idx], 
                                        segments[hash & SEGMENT_MASK].staleEntries);
                                segments[hash & SEGMENT_MASK].count++;
                            }
                        }
                    }
                    table = newTable;
                }
            }
        }
    }
    
    /**
     *  Default constructor.
     *  
     *  @param c    Runtime type of the objects that will be stored in the cache
     */
    public PropertyCache(Class c) {
        this.useCache = Boolean.getBoolean("org.apache.fop.fo.properties.use-cache");
        for (int i = SEGMENT_MASK + 1; --i >= 0;) {
            segments[i] = new CacheSegment();
        }
        this.runtimeType = c;
    }
    
    /**
     *  Generic fetch() method.
     *  Checks if the given <code>Object</code> is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param obj   the Object to check for
     *  @return  the cached instance
     */
    private Object fetch(Object obj) {
        if (!this.useCache) {
            return obj;
        }
        
        if (obj == null) {
            return null;
        }

        Object cacheEntry = get(obj);
        if (cacheEntry != null) {
            return cacheEntry;                
        }
        put(obj);
        return obj;
    }
    
    /**
     *  Checks if the given {@link Property} is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param prop the Property instance to check for
     *  @return the cached instance
     */
    public final Property fetch(Property prop) {
        
        return (Property) fetch((Object) prop);
    }
    
    /**
     *  Checks if the given {@link CommonHyphenation} is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param chy the CommonHyphenation instance to check for
     *  @return the cached instance
     */
    public final CommonHyphenation fetch(CommonHyphenation chy) {
        
        return (CommonHyphenation) fetch((Object) chy);
    }
    
    /**
     *  Checks if the given {@link CommonFont} is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param cf the CommonFont instance to check for
     *  @return the cached instance
     */
    public final CommonFont fetch(CommonFont cf) {
        
        return (CommonFont) fetch((Object) cf);
    }

    /** {@inheritDoc} */
    public String toString() {
        return super.toString() + "[runtimeType=" + this.runtimeType + "]";
    }
    
    
}
