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
public final class StreamCacheFactory {

    private static StreamCacheFactory memoryInstance = new StreamCacheFactory();

    /**
     * Returns an instance of a StreamCacheFactory with the requested features.
     * @param cacheToFile True if file shall be cached using a temporary file
     * @return StreamCacheFactory the requested factory
     */
    public static StreamCacheFactory getInstance() {
        return memoryInstance;
    }

    /**
     * Creates a new StreamCacheFactory.
     * @param cacheToFile True if file shall be cached using a temporary file
     */
    private StreamCacheFactory() {
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     * @throws IOException if there is an IO error
     * @return a new StreamCache for caching streams
     */
    public StreamCache createStreamCache() throws IOException {
        return new InMemoryStreamCache();
    }

    /**
     * Get the correct implementation (based on cacheToFile) of
     * StreamCache.
     * @param hintSize a hint about the approximate expected size of the buffer
     * @throws IOException if there is an IO error
     * @return a new StreamCache for caching streams
     */
    public StreamCache createStreamCache(int hintSize) throws IOException {
        return new InMemoryStreamCache(hintSize);
    }
}
