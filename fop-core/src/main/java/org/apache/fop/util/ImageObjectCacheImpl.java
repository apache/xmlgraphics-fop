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

package org.apache.fop.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.util.SoftMapCache;

/**
 * This class provides a cache for image objects. The main key into the images is the original URI the
 * image was accessed with prepended by the output format tag. This is based on XGC's ImageCache.
 */
public class ImageObjectCacheImpl extends ImageObjectCache {

    /** logger */
    protected static final Log LOG = LogFactory.getLog(ImageObjectCache.class);

    //Actual image cache
    private SoftMapCache cache = new SoftMapCache(true);

    private long lastHouseKeeping;
    private int counter;

    /**
     * Default constructor with default settings.
     */
    public ImageObjectCacheImpl() {
        lastHouseKeeping = System.currentTimeMillis();
    }

    private static final long ONE_HOUR = 60L * 60 * 1000;

    /**
     * Returns a cached image or null if not present.
     * @param key the image's cache key
     * @return the requested image or null if the image is not in the cache
     */
    public ImageObject getImage(String key) {
        ImageObject image = (ImageObject) cache.get(key);
        return image;
    }

    /**
     * Registers an image with the cache.
     * @param image the ImageObject
     * @param key the image's cache key
     */
    public void putImage(String key, ImageObject image) {
        cache.put(key, image);
    }

    /**
     * Checks whether there is a cached image identified by the given key.
     * @param key the image's cache key
     * @return true or false
     */
    public boolean hasImage(String key) {
        // decide whether to do some housekeeping with a simple rule of thumb...
        if (++counter % 1000 == 0) {
            counter = 0;
            considerHouseKeeping();
        }
        return (cache.get(key) != null);
    }

    /**
     * Clears the image cache (all ImageInfo and Image objects).
     */
    public void clearCache() {
        cache.clear();
        doHouseKeeping();
    }

    private void considerHouseKeeping() {
        long ts = System.currentTimeMillis();
        if (lastHouseKeeping + ONE_HOUR > ts) {
            lastHouseKeeping = ts;
            doHouseKeeping();
        }
    }

    /**
     * Triggers some house-keeping, i.e. removes stale entries.
     */
    public void doHouseKeeping() {
        cache.doHouseKeeping();
    }

}
