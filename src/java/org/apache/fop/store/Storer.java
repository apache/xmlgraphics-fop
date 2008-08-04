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

/**
 * Base storer class
 */
public class Storer {

    /** write session */
    protected final WriteSession session;

    /** file store */
    protected final FileStore store;

    /**
     * Constructor
     *
     * @param store our resource store
     */
    public Storer(FileStore store) {
        this.store = store;
        this.session = new WriteSession(this);
    }

    /**
     * Instantiates the store information record
     *
     * @return a new store information record
     */
    protected StoreRecord createRecord() {
        return new StoreRecord(store);
    }

    /**
     * Stores the object
     *
     * @return a store information record
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public StoreRecord store() throws IOException {
        StoreRecord record = null;
        session.begin();
        try {
            doStore();
        } finally {
            record = session.end();
        }
        return record;
    }

    /**
     * Actually performs the store operation
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    protected void doStore() throws IOException {
    }

    /**
     * Returns the file store associated with this storer
     *
     * @return the file store associated with this storer
     */
    protected FileStore getStore() {
        return store;
    }
}
