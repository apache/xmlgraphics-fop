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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * StreamCache implementation that uses temporary files rather than heap.
 */
public class InMemoryStreamCache implements StreamCache {

    private int hintSize = -1;

    /**
     * The current output stream.
     */
    private ByteArrayOutputStream output;

    /**
     * Creates a new InMemoryStreamCache.
     */
    public InMemoryStreamCache() {
    }

    /**
     * Creates a new InMemoryStreamCache.
     * @param hintSize a hint about the approximate expected size of the buffer
     */
    public InMemoryStreamCache(int hintSize) {
        this.hintSize = hintSize;
    }

    /**
     * Get the current OutputStream. Do not store it - it may change
     * from call to call.
     * @throws IOException if there is an error getting the output stream
     * @return the output stream containing the data
     */
    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            if (this.hintSize <= 0) {
                output = new ByteArrayOutputStream(512);
            } else {
                output = new ByteArrayOutputStream(this.hintSize);
            }
        }
        return output;
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] data) throws IOException {
        getOutputStream().write(data);
    }

    /**
     * Outputs the cached bytes to the given stream.
     * @param out the output stream to write to
     * @return the number of bytes written
     * @throws IOException if there is an IO error writing to the output stream
     */
    public int outputContents(OutputStream out) throws IOException {
        if (output == null) {
            return 0;
        }

        output.writeTo(out);
        return output.size();
    }

    /**
     * Returns the current size of the stream.
     * @throws IOException if there is an error getting the size
     * @return the length of the stream
     */
    public int getSize() throws IOException {
        if (output == null) {
            return 0;
        } else {
            return output.size();
        }
    }

    /**
     * Clears and resets the cache.
     * @throws IOException if there is an error closing the stream
     */
    public void clear() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }
}
