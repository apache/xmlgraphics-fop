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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ImageCacheListener implementation for debugging purposes.
 */
public class ImageCacheLoggingStatistics extends ImageCacheStatistics {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageCacheLoggingStatistics.class);

    /**
     * Main constructor.
     * @param detailed true if statistics for each URI should be kept.
     */
    public ImageCacheLoggingStatistics(boolean detailed) {
        super(detailed);
    }

    /** {@inheritDoc} */
    public void invalidHit(String uri) {
        super.invalidHit(uri);
        log.info("Invalid HIT: " + uri);
    }

    /** {@inheritDoc} */
    public void cacheHitImage(ImageKey key) {
        super.cacheHitImage(key);
        log.info("Image Cache HIT: " + key);
    }

    /** {@inheritDoc} */
    public void cacheHitImageInfo(String uri) {
        super.cacheHitImageInfo(uri);
        log.info("ImageInfo Cache HIT: " + uri);
    }

    /** {@inheritDoc} */
    public void cacheMissImage(ImageKey key) {
        super.cacheMissImage(key);
        log.info("Image Cache MISS: " + key);
    }

    /** {@inheritDoc} */
    public void cacheMissImageInfo(String uri) {
        super.cacheMissImageInfo(uri);
        log.info("ImageInfo Cache MISS: " + uri);
    }
    
    

}
