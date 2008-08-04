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

package org.apache.fop.store;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Store save information
 */
public class StoreRecord {

    /** the resource store associated with this store information */
    private final FileStore store;

    /** data position */
    protected long position;

    /** data chunk size */
    protected int size;

    /**
     * Main constructor
     *
     * @param store our resource store
     */
    public StoreRecord(FileStore store) {
        this.store = store;
    }

    /**
     * Returns the storage position
     *
     * @return the storage position
     */
    public long getPosition() {
        return this.position;
    }

    /**
     * Returns the storage size
     *
     * @return the storage size
     */
    public int getLength() {
        return this.size;
    }

    /**
     * Returns the resource store associated with this store record
     *
     * @return the resource store associated with this store record
     */
    public FileStore getStore() {
        return this.store;
    }

    /**
     * Convenience method used to writes the data referenced
     * by this storage record to an outputstream
     *
     * @param os the outputstream to write to
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public void writeToStream(OutputStream os) throws IOException {
        store.writeToStream(this, os);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "pos=" + position + ", size=" + size;
    }
}