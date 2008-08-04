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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.ImageManager;

/**
 * A useful class which is able to easily store and retrieve large resources (such as images)
 */
public class FileStore {

    /** logger */
    protected static Log log = LogFactory.getLog(ImageManager.class);

    /** Internal temporary storage buffer size */
    private static final int BUFFER_SIZE = 4096;

    /** Used for storage of data objects */
    protected RandomAccessFile raFile;

    /** The temporary cache file */
    private File tempFile;

    /** The file outputstream */
    protected FileOutputStream fos;

    /**
     * Default constructor
     *
     * @param prefix file store prefix
     */
    public FileStore(String prefix) {
        try {
            this.tempFile = File.createTempFile(prefix, null);
            this.raFile = new RandomAccessFile(tempFile, "rw");
            FileDescriptor fd = raFile.getFD();
            this.fos = new FileOutputStream(fd);
        } catch (IOException e) {
            // TODO
            log.error(e.getMessage());
        }
    }

    /**
     * Clears the resource store.
     *
     * @throws IOException if an error occurs while clearing the store
     */
    public void clear() throws IOException {
        if (tempFile != null) {
            raFile.close();
            raFile = null;
            fos = null;
            if (tempFile.exists() && !tempFile.delete()) {
                throw new IOException("Could not delete temporary file: " + tempFile);
            }
            tempFile = null;
        }
    }

    /** {@inheritDoc} */
    public void finalize() throws Throwable {
        try {
            clear();
        } finally {
            super.finalize();
        }
    }

    /**
     * Returns the storer of a given object
     *
     * @param object an object to be stored
     * @return a storer of the object
     */
    protected Storer getStorer(Object object) {
        Storer storer;
        if (object instanceof Writable) {
            storer = new WritableStorer(this, (Writable)object);
        } else if (object instanceof InputStream) {
            storer = new InputStreamStorer(this, (InputStream)object);
        } else {
            throw new IllegalArgumentException("Unsupported object " + object);
        }
        return storer;
    }

    /**
     * Stores an object in the cache
     *
     * @param object the object to store
     * @return a new save information record
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public StoreRecord write(Object object) throws IOException {
        return getStorer(object).store();
    }

    /**
     * Reads all the data from a given store information record
     * and returns it in a byte array.
     * This is potentially a memory hungry operation so use with care.
     *
     * @param storeInfo a store information record
     * @return the stored data in a byte array.
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public byte[] read(StoreRecord storeInfo) throws IOException {
        raFile.seek(storeInfo.position);
        byte[] buff = new byte[storeInfo.size];
        raFile.read(buff);
        return buff;
    }

    /**
     * Writes out the resource in full using the store information to the given outputstream.
     *
     * @param storeInfo the save information
     * @param os the outputstream to write to
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public void writeToStream(StoreRecord storeInfo, OutputStream os) throws IOException {
        if (storeInfo == null) {
            throw new IllegalArgumentException("save is null");
        }
        double chunkCount = storeInfo.size / BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        raFile.seek(storeInfo.position);
        for (int cnt = 0; cnt < chunkCount; cnt++) {
            raFile.read(buffer, 0, BUFFER_SIZE);
            os.write(buffer, 0, BUFFER_SIZE);
        }
        int lastChunkLength = storeInfo.size % BUFFER_SIZE;
        raFile.read(buffer, 0, lastChunkLength);
        os.write(buffer, 0, lastChunkLength);
    }
}
