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

package org.apache.fop.pdf;

import java.io.IOException;

/**
 * This class is serves as a factory from
 */
public class StreamCacheFactory {

    private static boolean defaultCacheToFile = false;
    private static StreamCacheFactory fileInstance = null;
    private static StreamCacheFactory memoryInstance = null;

    private boolean cacheToFile = false;

    /**
     * Returns an instance of a StreamCacheFactory with the requested features.
     * @param cacheToFile True if file shall be cached using a temporary file
     * @return StreamCacheFactory the requested factory
     */
    public static StreamCacheFactory getInstance(boolean cacheToFile) {
        if (cacheToFile) {
            if (fileInstance == null) {
                fileInstance = new StreamCacheFactory(true);
            }
            return fileInstance;
        } else {
            if (memoryInstance == null) {
                memoryInstance = new StreamCacheFactory(false);
            }
            return memoryInstance;
        }
    }

    /**
     * Returns an instance of a StreamCacheFactory depending on the default
     * setting for cacheToFile.
     * @return StreamCacheFactory the requested factory
     */
    public static StreamCacheFactory getInstance() {
        return getInstance(defaultCacheToFile);
    }

    /**
     * Sets the global default for cacheToFile
     * @param cacheToFile True if stream caches should be held in files.
     */
    public static void setDefaultCacheToFile(boolean cacheToFile) {
        defaultCacheToFile = cacheToFile;
    }

    /**
     * Creates a new StreamCacheFactory.
     * @param cacheToFile True if file shall be cached using a temporary file
     */
    public StreamCacheFactory(boolean cacheToFile) {
        this.cacheToFile = cacheToFile;
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     * @throws IOException if there is an IO error
     * @return a new StreamCache for caching streams
     */
    public StreamCache createStreamCache() throws IOException {
        if (this.cacheToFile) {
            return new TempFileStreamCache();
        } else {
            return new InMemoryStreamCache();
        }
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     * @param hintSize a hint about the approximate expected size of the buffer
     * @throws IOException if there is an IO error
     * @return a new StreamCache for caching streams
     */
    public StreamCache createStreamCache(int hintSize) throws IOException {
        if (this.cacheToFile) {
            return new TempFileStreamCache();
        } else {
            return new InMemoryStreamCache(hintSize);
        }
    }

    /**
     * Get the value of the global cacheToFile flag.
     * @return the current cache to file flag
     */
    public boolean getCacheToFile() {
        return this.cacheToFile;
    }


}
