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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.DataObjectFactory;
import org.apache.fop.render.afp.modca.Registry;

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
    
    
    /** Mapping of data object uri --> cache record */
    private Map/*<ResourceInfo,Record>*/ includableMap
        = new java.util.HashMap/*<ResourceInfo,Record>*/();
    
    /** Used for create data objects */
    private DataObjectFactory factory = new DataObjectFactory();

    /** Used for storage of data objects */
    private RandomAccessFile raFile;
    
    /** File channel used for manipulating the temporary file */
    private FileChannel channel;
    
    /** The temporary cache file */
    private File tempFile;
    
    /** The next file pointer position in the cache file */
    private long nextPos;

    /** Our assigned cache id */
    private Integer id;
    
    /**
     * Returns an instance of the cache
     * 
     * @return an instance of the cache or null if failed
     */
    public static DataObjectCache getInstance() {
        synchronized (cacheMap) {
            Integer cacheId = new Integer(System.identityHashCode(Thread.currentThread()));
            DataObjectCache cache = (DataObjectCache)cacheMap.get(cacheId);
            if (cache == null) {
                try {
                    cache = new DataObjectCache(cacheId);
                    cacheMap.put(cacheId, cache);
                } catch (IOException e) {
                    log.error("Failed to create cache");
                }
            }
            return cache;
        }
    }

    /**
     * Default constructor
     * 
     * @param cacheId the cache id
     */
    private DataObjectCache(Integer cacheId) throws IOException {
        this.id = cacheId;
        this.tempFile = File.createTempFile(CACHE_FILENAME_PREFIX + cacheId, null);
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
            synchronized (cacheMap) {
                cacheMap.remove(id); // remove ourselves from the cache map
            }
        } catch (IOException e) {
            log.error("Failed to close temporary file");
        }
    }

    /**
     * Stores a named data object in the cache
     * 
     * @param dataObj a named data object
     * @return a new cache record
     */
    public Record store(AbstractNamedAFPObject dataObj) {
        Record record = new Record();
        record.objectName = dataObj.getName();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            channel.position(nextPos);
            record.position = channel.position();
            dataObj.write(os);
            record.size = os.size();
            MappedByteBuffer byteBuffer
                = channel.map(FileChannel.MapMode.READ_WRITE, record.position, record.size);
            byteBuffer.put(os.toByteArray());
            channel.write(byteBuffer);
            nextPos += record.size + 1;
        } catch (IOException e) {
            log.error("Failed to write cache record for '"
                    + dataObj + "', " + e.getMessage());
        }
        return record;
    }
    
    /**
     * Creates and adds a new data object and record to the cache as necessary.  
     * 
     * @param dataObjectInfo a data object info
     * 
     * @return a cache record
     */
    public Record store(DataObjectInfo dataObjectInfo) {
        Registry.ObjectType objectType = dataObjectInfo.getObjectType();
        Record record = null;
        if (!objectType.canBeIncluded()) {
            AbstractNamedAFPObject dataObj = factory.createObject(dataObjectInfo);
            if (dataObj == null) {
                log.error("Failed to create object: " + dataObjectInfo);
                return null;
            }
            record = store(dataObj);
        } else {
            ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
            record = (Record)includableMap.get(resourceInfo);
            if (record == null) {
                AbstractNamedAFPObject dataObj = factory.createObject(dataObjectInfo);
                if (dataObj == null) {
                    log.error("Failed to create object: " + dataObjectInfo);
                    return null;
                }
                record = store(dataObj);
                includableMap.put(resourceInfo, record);
            }
        }
        return record;
    }

    /**
     * Returns the written binary data of the AbstractNamedDataObject from the cache file
     * 
     * @param record the cache record
     * @return the binary data of the AbstractNamedDataObject or null if failed.
     */
    public byte[] retrieve(Record record) {
        if (record == null) {
            throw new IllegalArgumentException("Cache record is null");
        }
        MappedByteBuffer byteBuffer = null;
        try {
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, record.position, record.size);
        } catch (IOException e) {
            log.error("Failed to read cache record for '" + record + "', " + e.getMessage());
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
    public class Record {
        private long position; 
        private int size;
        private String objectName;
        
        /** {@inheritDoc} */
        public String toString() {
            return "Record{name=" + objectName
                + ", pos=" + position
                + ", size=" + size
                + "}";
        }
        
        /**
         * Returns the object name
         * 
         * @return the object name
         */
        public String getObjectName() {
            return this.objectName;
        }
    }
}
