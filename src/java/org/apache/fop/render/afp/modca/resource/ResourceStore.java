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

package org.apache.fop.render.afp.modca.resource;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;

/**
 * Caches and creates (as necessary using an instance of DataObjectFactory)
 * descendants of AbstractDataObject
 */
public final class ResourceStore {

    /** Static logging instance */
    private static final Log log = LogFactory.getLog(ResourceStore.class);

    private static final String TEMPFILE_PREFIX = "AFP_";

    /** Internal temporary storage buffer size */
    private static final int BUFFER_SIZE = 4096;

    /** Used for storage of data objects */
    private RandomAccessFile raFile;

    /** The temporary cache file */
    private File tempFile;

    /** The file outputstream */
    private FileOutputStream fos;

    /**
     * Default constructor
     */
    public ResourceStore() {
        try {
            this.tempFile = File.createTempFile(TEMPFILE_PREFIX, null);
            this.raFile = new RandomAccessFile(tempFile, "rw");
            FileDescriptor fd = raFile.getFD();
            this.fos = new FileOutputStream(fd);
        } catch (IOException e) {
            // TODO
            log.error(e.getMessage());
        }
    }

    /**
     * Clears the data object cache.
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
     * Stores a named data object in the cache
     *
     * @param dataObj a named data object
     * @return a new save information record
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public StoreInfo save(AbstractNamedAFPObject dataObj) throws IOException {
        StoreInfo storeInfo = new StoreInfo();
        storeInfo.objectName = dataObj.getName();
        storeInfo.position = raFile.getFilePointer();
        try {
            dataObj.write(fos);
        } finally {
            fos.flush();
        }
        storeInfo.size = (int)(raFile.getFilePointer() - storeInfo.position);
        return storeInfo;
    }

    /**
     * Writes out the resource given the save information to the given outputstream.
     *
     * @param saveInfo the save information
     * @param os the outputstream to write to
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public void writeToStream(StoreInfo saveInfo, OutputStream os) throws IOException {
        if (saveInfo == null) {
            throw new IllegalArgumentException("save is null");
        }
        double chunkCount = saveInfo.size / BUFFER_SIZE;
        byte[] buffer = new byte[BUFFER_SIZE];
        raFile.seek(saveInfo.position);
        for (int cnt = 0; cnt < chunkCount; cnt++) {
            raFile.read(buffer, 0, BUFFER_SIZE);
            os.write(buffer, 0, BUFFER_SIZE);
        }
        int lastChunkLength = saveInfo.size % BUFFER_SIZE;
        raFile.read(buffer, 0, lastChunkLength);
        os.write(buffer, 0, lastChunkLength);
    }

}
