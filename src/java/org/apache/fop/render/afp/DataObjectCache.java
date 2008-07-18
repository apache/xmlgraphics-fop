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

package org.apache.fop.render.afp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.DataObjectFactory;

/**
 * Caches and creates (as necessary using an instance of DataObjectFactory)
 * descendants of AbstractDataObject
 */
public final class DataObjectCache {

    /** Static logging instance */
    private static final Log log = LogFactory.getLog(DataObjectCache.class);

    private static final String CACHE_FILENAME_PREFIX = "AFP_";

    /** Static mapping of data object caches id --> cache */
    private static Map/*<Integer,DataObjectCache>*/ cacheMap
        = new java.util.HashMap/*<Integer,DataObjectCache>*/();    
    
    
    /** Mapping of data object uri --> cache entry */
    private Map/*<String,DataObjectCache.Entry>*/ cacheRecordMap
        = new java.util.HashMap/*<String,DataObjectCache.Entry>*/();
    
    /** Used for create data objects */
    private DataObjectFactory factory = new DataObjectFactory();

    /** Used for storage of data objects */
    private RandomAccessFile raFile;
    
    /** File channel used for manipulating the temporary file */
    private FileChannel channel;
    
    /** The temporary cache file */
    private File tempFile;
    
    /** The cache id */
    private int id;

    /** The next file pointer position in the cache file */
    private long nextPos;
    
    /**
     * Returns an instance of the cache
     * 
     * @return an instance of the cache or null if failed
     */
    public static DataObjectCache getInstance() {
        synchronized (cacheMap) {
            int id = System.identityHashCode(Thread.currentThread());
            Integer cacheKey = new Integer(id);
            DataObjectCache cache = (DataObjectCache)cacheMap.get(cacheKey);
            if (cache == null) {
                try {
                    cache = new DataObjectCache();
                    cacheMap.put(cacheKey, cache);
                } catch (IOException e) {
                    log.error("Failed to create cache");
                }
            }
            return cache;
        }
    }

    /**
     * Default constructor
     */
    private DataObjectCache() throws IOException {
        this.id = System.identityHashCode(Thread.currentThread());        
        this.tempFile = File.createTempFile(CACHE_FILENAME_PREFIX + id, null);
        this.raFile = new RandomAccessFile(tempFile, "rw");
        this.channel = raFile.getChannel();
    }

    /**
     * Clears the data object cache
     */
    public void clear() {
        try {
            raFile.close();
            tempFile.delete();
        } catch (IOException e) {
            log.error("Failed to close temporary file");
        }
    }
    
    /**
     * Creates and adds a new data object and record to the cache as necessary.  
     * 
     * @param dataObjectInfo a data object info
     * 
     * @return the name of the related data object
     */
    public String put(DataObjectInfo dataObjectInfo) {
        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        Record record = (Record)cacheRecordMap.get(resourceInfo);
        if (record == null) {
            record = new Record();
            AbstractNamedAFPObject dataObj = factory.createObject(dataObjectInfo);
            record.objectName = dataObj.getName();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                channel.position(nextPos);
                record.position = channel.position();
                dataObj.write(os);
                record.size = os.size();
                MappedByteBuffer byteBuffer
                    = channel.map(FileChannel.MapMode.READ_WRITE, record.position, record.size);
                byte[] data = os.toByteArray();
                byteBuffer.put(data);
                channel.write(byteBuffer);
                nextPos += record.size + 1;
            } catch (IOException e) {
                log.error("Failed to write cache record for '"
                        + resourceInfo + "', " + e.getMessage());
            }
            cacheRecordMap.put(resourceInfo, record);
        }
        return record.objectName;
    }
    
    /**
     * Returns the written binary data of the AbstractDataObject from the cache file
     * 
     * @param resourceInfo the data resource info
     * @return the binary data of the AbstractDataObject or null if failed.
     */
    public byte[] get(ResourceInfo resourceInfo) {
        Record record = (Record)cacheRecordMap.get(resourceInfo);
        if (record == null) {
            throw new IllegalArgumentException("Unknown data object " + resourceInfo);
        }
        MappedByteBuffer byteBuffer = null;
        try {
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, record.position, record.size);
        } catch (IOException e) {
            log.error("Failed to read cache record for '" + resourceInfo + "', " + e.getMessage());
            return null;
        }
        if (byteBuffer.hasArray()) {
            return byteBuffer.array();
        } else {
            byte[] data = new byte[record.size];
            byteBuffer.get(data);
            return data;
        }
    }
    
    /**
     * Returns the data object factory
     * 
     * @return the data object factory
     */
    public DataObjectFactory getFactory() {
        return this.factory;
    }

    /**
     * A cache record
     */
    private class Record {
        protected long position; 
        protected int size;
        protected String objectName;
        
        public String toString() {
            return "Record{name=" + objectName
                + ", pos=" + position
                + ", size=" + size
                + "}";
        }
    }
}
