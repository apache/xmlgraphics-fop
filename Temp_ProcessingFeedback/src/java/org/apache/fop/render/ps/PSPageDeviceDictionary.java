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

package org.apache.fop.render.ps;

/**
 * Postscript page device dictionary object
 * 
 * This object is used by the postscript renderer to hold postscript page device
 * values.  It can also be used to minimize the number of setpagedevice calls when
 * DSC compliance is false.
 */
public class PSPageDeviceDictionary extends PSDictionary {
    
    private static final long serialVersionUID = 845943256485806509L;

    /**
     * Whether or not the contents of the dictionary are flushed on retrieval
     */
    private boolean flushOnRetrieval = false;

    /**
     * Dictionary content that has not been output/written yet
     */
    private PSDictionary unRetrievedContentDictionary;

    /**
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return the previous value associated with the key or null
     * @see java.util.Map#put(Object, Object)
     */
    public Object put(Object key, Object value) {
        Object previousValue = super.put(key, value);
        if (flushOnRetrieval) {
            if (previousValue == null || !previousValue.equals(value)) {
                unRetrievedContentDictionary.put(key, value);
            }
        }
        return previousValue;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        super.clear();
        if (unRetrievedContentDictionary != null) {
            unRetrievedContentDictionary.clear();
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        if (flushOnRetrieval) {
            return unRetrievedContentDictionary.isEmpty();
        }
        return super.isEmpty();
    }

    /**
     * The contents of the dictionary are flushed when written
     * @param flushOnRetrieval boolean value
     */
    public void setFlushOnRetrieval(boolean flushOnRetrieval) {
        this.flushOnRetrieval = flushOnRetrieval;
        if (flushOnRetrieval) {
            unRetrievedContentDictionary = new PSDictionary();
        }
    }
    
    /**
     * Returns a dictionary string with containing all unwritten content note:
     * unnecessary writes are important as there is a device specific
     * initgraphics call by the underlying postscript interpreter on every
     * setpagedevice call which can result in blank pages etc.
     * 
     * @return unwritten content dictionary string
     */
    public String getContent() {
        String content;
        if (flushOnRetrieval) {
            content = unRetrievedContentDictionary.toString();
            unRetrievedContentDictionary.clear();
        } else {
            content = super.toString();
        }
        return content;
    }    
}
