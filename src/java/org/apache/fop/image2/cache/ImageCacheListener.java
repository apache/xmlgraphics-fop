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

import java.util.EventListener;

/**
 * This interface can be implemented by classes which want to know what's going on inside the
 * image cache.
 */
public interface ImageCacheListener extends EventListener {

    /**
     * An URi previously identified as invalid was requested again
     * @param uri the invalid URI
     */
    void invalidHit(String uri);
    
    /**
     * An ImageInfo was found in the cache
     * @param uri the image's URI
     */
    void cacheHitImageInfo(String uri);
    
    /**
     * An ImageInfo was not in the cache
     * @param uri the image's URI
     */
    void cacheMissImageInfo(String uri);
    
    /**
     * An Image was found in the cache
     * @param key the image key
     */
    void cacheHitImage(ImageKey key);
    
    /**
     * An Image was not in the cache
     * @param key the image key
     */
    void cacheMissImage(ImageKey key);
    
}
