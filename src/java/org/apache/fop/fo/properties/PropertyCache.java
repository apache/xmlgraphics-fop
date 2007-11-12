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
    
    /** the segments array (length = 32) */
    private CacheSegment[] segments = new CacheSegment[SEGMENT_MASK + 1];
    /** the table of hash-buckets */
    private CacheEntry[] table = new CacheEntry[8];
    
    /* same hash function as used by java.util.HashMap */
    private static int hash(Object x) {
        int h = x.hashCode();

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
    private final class CacheEntry {
        final CacheEntry next;
        volatile WeakReference ref;
        final int hash;
        
        /* main constructor */
        CacheEntry(Object p, CacheEntry next) {
            this.next = next;
            this.ref = new WeakReference(p);
            this.hash = p.hashCode();
        }
        
        /* clone constructor */
        CacheEntry(CacheEntry old, CacheEntry next) {
            this.next = next;
            this.ref = old.ref;
            this.hash = old.hash;
        }
        
    }
    
    /* Wrapper objects to synchronize on */
    private final class CacheSegment {
        private int count = 0;
    }
    
    /*
     * Class modeling a cleanup thread.
     * 
     * Once run() is called, the segment is locked and the hash-bucket
     * will be traversed, removing any obsolete entries.
     * If the cleanup has no effect, rehash() is called.
     */
    private final class CacheCleaner implements Runnable {
        
        private int hash;
        
        CacheCleaner(int hash) {
            this.hash = hash;
        }
        
        public void run() {
            //System.out.println("Cleaning segment " + this.segment);
            CacheSegment segment = segments[this.hash & SEGMENT_MASK];
            int oldCount;
            int newCount;
            synchronized (segment) {
                oldCount = segment.count;
                /* check first to see if another cleaner thread already
                 * pushed the number of entries back below the threshold
                 * if so, return immediately
                 */
                if (segment.count < (2 * table.length)) {
                    return;
                }
                
                int index = this.hash & (table.length - 1);
                CacheEntry first = table[index];
                WeakReference ref;
                for (CacheEntry e = first; e != null; e = e.next) {
                    ref = e.ref;
                    if (ref != null && ref.get() == null) {
                        /* remove obsolete entry
                        /* 1. clear value, cause interference for non-blocking get() */
                        e.ref = null;
                        
                        /* 2. clone the segment, without the obsolete entry */
                        CacheEntry head = e.next;
                        for (CacheEntry c = first; c != e; c = c.next) {
                            head = new CacheEntry(c, head);
                        }
                        table[index] = head;
                        segment.count--;
                    }
                }
                newCount = segment.count;
            }
            if (oldCount == newCount) {
                /* cleanup had no effect, try rehashing */
                rehash(SEGMENT_MASK);
            }
        }
    }
    
    /*
     * Puts a new instance in the cache.
     * If the total number of entries for the corresponding
     * segment exceeds twice the amount of hash-buckets, a
     * cleanup thread will be launched to remove obsolete
     * entries.
     */
    private final void put(Object o) {
        
        int hash = hash(o);
        CacheSegment segment = segments[hash & SEGMENT_MASK];
        
        synchronized (segment) {
            int index = hash & (table.length - 1);
            CacheEntry entry = table[index];
            
            if (entry == null) {
                entry = new CacheEntry(o, null);
                table[index] = entry;
                segment.count++;
            } else {
                WeakReference ref = entry.ref;
                if (ref != null && eq(ref.get(), o)) {
                    return;
                } else {
                    CacheEntry newEntry = new CacheEntry(o, entry);
                    table[index] = newEntry;
                    segment.count++;
                }
            }
            
            if (segment.count > (2 * table.length)) {
                /* launch cleanup in a separate thread, 
                 * so it acquires its own lock, and put()
                 * can return immediately */
                Thread cleaner = new Thread(new CacheCleaner(hash));
                cleaner.start();
            }
        }
    }
    

    /* Gets a cached instance. Returns null if not found */
    private final Object get(Object o) {
        
        int hash = hash(o);
        int index = hash & (table.length - 1);
        
        CacheEntry entry = table[index];
        WeakReference r;
        Object q;
        
        /* try non-synched first */
        for (CacheEntry e = entry; e != null; e = e.next) {
            if (e.hash == o.hashCode()
                    && (r = e.ref) != null
                    && (q = r.get()) != null
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
            for (CacheEntry e = entry; e != null; e = e.next) {
                if (e.hash == o.hashCode()
                        && (r = e.ref) != null
                        && (q = r.get()) != null
                        &&  eq(q, o)) {
                    return q;
                }
            }
        }
        return null;
    }
    
    /*
     * Recursively acquires locks on all 32 segments,
     * then performs a check on the segments first to see `
     * how many precisely exceed the threshold ( 2 x table.length ). 
     * If this number exceeds half the amount of buckets, 
     * extends the cache and redistributes the entries.
     * 
     * Example:
     * For a cache with default size of 8 buckets, each bucket is
     * a segment, and as such, rehash() will only have effect
     * if more than 4 buckets exceed the size of 16 entries.
     * 
     */
    private final void rehash(int index) {
        
        CacheSegment seg = segments[index];
        synchronized (seg) {
            if (index > 0) {
                /* need to recursively acquire locks on all segments */
                rehash(index - 1);
            } else {
                /* double the amount of buckets */
                int newLength = table.length << 1;
                if (newLength > 0) { //no overflow?
                    /* Check segmentcounts first */
                    int countSegments = 0;
                    int threshold = table.length * 2;
                    for (int i = segments.length; --i >= 0;) {
                        if (segments[i].count > threshold) {
                            countSegments++;
                        }
                    }
                    
                    if (countSegments <= (table.length / 2)) {
                        return;
                    } else {
                        /* reset segmentcounts */
                        for (int i = segments.length; --i >= 0;) {
                            segments[i].count = 0;
                        }
                    }
                    
                    CacheEntry[] newTable = new CacheEntry[newLength];
                    
                    int hash, idx;
                    WeakReference ref;
                    Object o;
                    newLength--;
                    for (int i = table.length; --i >= 0;) {
                        for (CacheEntry c = table[i]; c != null; c = c.next) {
                            ref = c.ref;
                            if (ref != null) {
                                if ((o = ref.get()) != null) {
                                    hash = hash(o);
                                    idx = hash & newLength;
                                    newTable[idx] = new CacheEntry(c, newTable[idx]);
                                    segments[hash & SEGMENT_MASK].count++;
                                }
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
     */
    public PropertyCache() {
        for (int i = SEGMENT_MASK + 1; --i >= 0;) {
            segments[i] = new CacheSegment();
        }
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
    private final Object fetch(Object obj) {
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
     *  Checks if the given <code>Property</code> is present in the cache - 
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
     *  Checks if the given <code>CommonHyphenation</code> is present in the cache - 
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
     *  Checks if the given <code>CachedCommonFont</code> is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param ccf the CachedCommonFont instance to check for
     *  @return the cached instance
     */
    public final CommonFont.CachedCommonFont fetch(CommonFont.CachedCommonFont ccf) {
        
        return (CommonFont.CachedCommonFont) fetch((Object) ccf);
    }
    
    /**
     *  Checks if the given <code>CommonFont</code> is present in the cache - 
     *  if so, returns a reference to the cached instance. 
     *  Otherwise the given object is added to the cache and returned.
     *  
     *  @param cf the CommonFont instance to check for
     *  @return the cached instance
     */
    public final CommonFont fetch(CommonFont cf) {
        
        return (CommonFont) fetch((Object) cf);
    }
}
