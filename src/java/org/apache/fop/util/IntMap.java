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

package org.apache.fop.util;

/**
 * Dedicated map for storing int-to-int mappings, 
 * where the key is always a positive <code>int</code>.
 * 
 */
public class IntMap {

    private static final int DEFAULT_CAPACITY = 256;
    
    private int[] cachedKeys;
    private int[] cachedValues;
    private Entry[] entries;
    
    private int initialCapacity = DEFAULT_CAPACITY;
    private int currentSize;
    
    /**
     * Creates an IntMap instance with an <code>initialCapacity</code>
     * of 256 mappings
     *
     */
    public IntMap() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Creates an IntMap instance with the supplied 
     * <code>initialCapacity</code>
     * 
     * @param initialCapacity the map's initial capacity
     */
    public IntMap(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        initMap();
    }
    
    private void initMap() {
        this.entries = new Entry[initialCapacity];
        this.currentSize = 0;
    }
    
    /**
     * Clears the map, and re-initializes it
     */
    public void clear() {
        initMap();
    }

    /**
     * Checks whether a mapping for the specified key exists.
     * @param key   the key to look up
     * @return true if the map contains a mapping for the specified key
     */
    public boolean containsKey(int key) {
        return (key >= 0)
            && (currentSize > 0)
            && (searchKeyIndex(key) == key);
    }
    
    /**
     * 
     * @param key   the key
     * @return  the corresponding value; a value of 0 can
     *          either mean that the key is mapped to 0
     *          or that the key is not mapped at all; use
     *          <code>containsKey(int)</code> to find
     *          out if a mapping exists
     */
    public int get(int key) {
        
        if (key >= 0 && currentSize > 0) {
            int idx = searchKeyIndex(key);
            if (entries[idx] != null) {
                return entries[idx].mapping[1];
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    /**
     * Adds a mapping corresponding to the key-value pair
     * @param key   the key for which to create the mapping
     * @param value the mapped value
     * @return  the value that was previously associated with the
     *          specified key; a value of 0 can mean either that
     *          the key was unmapped or mapped to the value 0
     */
    public int put(int key, int value) {
        if (key >= 0) {
            ensureCapacity(key + 1);
            int keyIndex = searchKeyIndex(key);
            int retVal = 0;;
            if (entries[keyIndex] == null) {
                entries[currentSize++] = new Entry(key, value);
                cachedKeys = null;;
            } else {
                retVal = entries[keyIndex].mapping[1];
                entries[keyIndex].mapping[1] = value;
            }
            cachedValues = null;
            return retVal;
        } else {
            throw new IllegalArgumentException(
                    "This map allows only positive integers as keys.");
        }
    }

    /**
     * Removes the mapping corresponding to the key
     * @param key   the key of the mapping to be removed
     * @return  the value that was associated with the given key;
     *          a value of 0 can mean that the key was either
     *          unmapped or mapped to the value 0
     */
    public int remove(int key) {
        if (key >= 0
                && currentSize > 0) {
            int keyIndex = searchKeyIndex(key);
            int retVal = 0;
            if (entries[keyIndex] != null
                    && entries[keyIndex].mapping[0] == key) {
                retVal = entries[key].mapping[0];
                entries[key] = null;
                cachedKeys = null;
                cachedValues = null;
                currentSize--;
            }
            return retVal;
        } else {
            return 0;
        }
    }
    
    /**
     * Get an array containing the mapped keys
     * @return  the keys as an array of <code>int</code>
     */
    public int[] keys() {
        
        if (currentSize > 0
                && cachedKeys == null) {
            cachedKeys = new int[currentSize];
            int keyIndex = currentSize;
            for (int i = entries.length; --i >= 0;) {
                if (entries[i] != null) {
                    cachedKeys[--keyIndex] = entries[i].mapping[0];
                }
            }
        }
        return cachedKeys;
    }
    
    /**
     * Get an array containing the mapped values
     * @return  the values as an array of <code>int</code>
     */
    public int[] values() {
        
        if (currentSize > 0
                && cachedValues == null) {
            cachedValues = new int[currentSize];
            int valIndex = currentSize;
            for (int i = entries.length; --i >= 0;) {
                if (entries[i] != null) {
                    cachedValues[--valIndex] = entries[i].mapping[0];
                }
            }
        }
        return cachedValues;
    }

    /**
     * Get the size of the map (= the number of keys that are mapped)
     * @return  the size of the map
     */
    public int size() {
        return currentSize;
    }
    
    private void ensureCapacity(int minCapacity) {
        if (entries.length == 0) {
            entries = new Entry[minCapacity];
        } else if (entries.length < minCapacity) {
            int newCap = entries.length + 1;
            while (newCap < minCapacity) {
                newCap += (newCap / 2);
            }
            Entry[] oldEntries = entries;
            entries = new Entry[newCap];
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
        }
    }
    
    private final int searchKeyIndex(int key) {
        if (currentSize > 0) {
            int start = 0;
            int end = currentSize;
            int mid;
            while (end >= start) {
                mid = (start + end) / 2;
                if (entries[mid] == null || entries[mid].mapping[0] > key) {
                    end = mid - 1;
                } else if (entries[mid].mapping[0] == key) {
                    return mid;
                } else {
                    start = mid + 1;
                }
            }
            return start;
        } else {
            return 0;
        }
    }
    
    private static final class Entry {
        
        protected int[] mapping;
        
        private Entry(int key, int value) {
            this.mapping = new int[]{key, value};
        }
        
    }
}
