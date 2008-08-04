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
 * A Write session
 */
public class WriteSession {
    /** true if the session output was flushed */
    protected boolean flushed = false;

    /** the session storer */
    private final Storer storer;

    /** the storer's file store */
    private final FileStore store;

    /** the store information record */
    protected StoreRecord record;

    /**
     * Constructor
     *
     * @param store our store
     */
    public WriteSession(FileStore store) {
        this.storer = new Storer(store);
        this.store = store;
    }

    /**
     * Constructor
     *
     * @param storer our storer
     */
    public WriteSession(Storer storer) {
        this.storer = storer;
        this.store = storer.getStore();
    }

    /**
     * Begins the session
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void begin() throws IOException {
        // always write to the end of the store
        long length = store.raFile.length();
        if (store.raFile.getFilePointer() < length) {
            store.raFile.seek(length);
        }

        this.record = storer.createRecord();
        record.position = store.raFile.getFilePointer();
    }

    /**
     * Ends the session
     *
     * @return a new store information record
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public StoreRecord end() throws IOException {
        if (!flushed) {
            store.fos.flush();
            flushed = true;
        }
        record.size = (int)(store.raFile.getFilePointer() - record.position);
        return record;
    }

    /**
     * Returns the outputstream of this store
     *
     * @return the outputstream of this store
     */
    public OutputStream getOutputStream() {
        return store.fos;
    }

    /**
     * Returns the store record
     *
     * @return the store record
     */
    public StoreRecord getRecord() {
        return record;
    }
}