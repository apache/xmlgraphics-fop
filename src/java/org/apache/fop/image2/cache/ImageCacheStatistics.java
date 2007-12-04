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

package org.apache.fop.image2.cache;

import java.util.Collections;
import java.util.Map;

/**
 * Convenience class that gathers statistical information about the image cache.
 */
public class ImageCacheStatistics implements ImageCacheListener {

    private int invalidHits;
    private int imageInfoCacheHits;
    private int imageInfoCacheMisses;
    private int imageCacheHits;
    private int imageCacheMisses;
    private Map imageCacheHitMap;
    private Map imageCacheMissMap;
    
    /**
     * Main constructor.
     * @param detailed true if the cache hits/misses for each Image instance should be recorded.
     */
    public ImageCacheStatistics(boolean detailed) {
        if (detailed) {
            imageCacheHitMap = new java.util.HashMap();
            imageCacheMissMap = new java.util.HashMap();
        }
    }
    
    /**
     * Reset the gathered statistics information.
     */
    public void reset() {
        this.imageInfoCacheHits = 0;
        this.imageInfoCacheMisses = 0;
        this.invalidHits = 0;
    }
    
    /** {@inheritDoc} */
    public void invalidHit(String uri) {
        invalidHits++;
    }

    /** {@inheritDoc} */
    public void cacheHitImageInfo(String uri) {
        imageInfoCacheHits++;
    }

    /** {@inheritDoc} */
    public void cacheMissImageInfo(String uri) {
        imageInfoCacheMisses++;
    }

    private void increaseEntry(Map map, Object key) {
        Integer v = (Integer)map.get(key);
        if (v == null) {
            v = new Integer(1);
        } else {
            v = new Integer(v.intValue() + 1);
        }
        map.put(key, v);
    }

    /** {@inheritDoc} */
    public void cacheHitImage(ImageKey key) {
        imageCacheHits++;
        if (imageCacheHitMap != null) {
            increaseEntry(imageCacheHitMap, key);
        }
    }
    
    /** {@inheritDoc} */
    public void cacheMissImage(ImageKey key) {
        imageCacheMisses++;
        if (imageCacheMissMap != null) {
            increaseEntry(imageCacheMissMap, key);
        }
    }

    /**
     * Returns the number of times an invalid URI is tried.
     * @return the number of times an invalid URI is tried.
     */
    public int getInvalidHits() {
        return invalidHits;
    }

    /**
     * Returns the number of cache hits for ImageInfo instances.
     * @return the number of cache hits for ImageInfo instances.
     */
    public int getImageInfoCacheHits() {
        return imageInfoCacheHits;
    }

    /**
     * Returns the number of cache misses for ImageInfo instances.
     * @return the number of cache misses for ImageInfo instances.
     */
    public int getImageInfoCacheMisses() {
        return imageInfoCacheMisses;
    }

    /**
     * Returns the number of cache hits for Image instances.
     * @return the number of cache hits for Image instances.
     */
    public int getImageCacheHits() {
        return imageCacheHits;
    }

    /**
     * Returns the number of cache misses for Image instances.
     * @return the number of cache misses for Image instances.
     */
    public int getImageCacheMisses() {
        return imageCacheMisses;
    }

    /**
     * Returns a Map<ImageKey, Integer> with the number of cache hits.
     * @return a Map<ImageKey, Integer> with the number of cache hits
     */
    public Map getImageCacheHitMap() {
        return Collections.unmodifiableMap(imageCacheHitMap);
    }

    /**
     * Returns a Map<ImageKey, Integer> with the number of cache misses.
     * @return a Map<ImageKey, Integer> with the number of cache misses
     */
    public Map getImageCacheMissMap() {
        return Collections.unmodifiableMap(imageCacheMissMap);
    }
    
}