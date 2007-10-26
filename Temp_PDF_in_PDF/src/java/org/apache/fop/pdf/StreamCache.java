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

import java.io.OutputStream;
import java.io.IOException;

/**
 * Interface used to store the bytes for a PDFStream. It's actually a generic
 * cached byte array. There's a factory that returns either an
 * in-memory or tempfile based implementation based on a
 * cacheToFile setting.
 */
public interface StreamCache {

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     *
     * @return an output stream for this cache
     * @throws IOException if there is an IO error
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Convenience method for writing data to the stream cache.
     * @param data byte array to write
     * @throws IOException if there is an IO error
     */
    void write(byte[] data) throws IOException;

    /**
     * Outputs the cached bytes to the given stream.
     *
     * @param out the stream to write to
     * @return the number of bytes written
     * @throws IOException if there is an IO error
     */
    int outputContents(OutputStream out) throws IOException;

    /**
     * Returns the current size of the stream.
     *
     * @return the size of the cache
     * @throws IOException if there is an IO error
     */
    int getSize() throws IOException;

    /**
     * Clears and resets the cache.
     *
     * @throws IOException if there is an IO error
     */
    void clear() throws IOException;
}

