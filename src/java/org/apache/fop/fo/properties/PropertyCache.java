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

package org.apache.fop.fo.properties;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *  Thin wrapper around a HashMap to implement the property caching idiom
 *  in which a new Property instance is created then tested against cached
 *  instances created previously. If an existing property is found, this is
 *  retained and the newly created one is instantly eligible for garbage
 *  collection.
 */
public class PropertyCache {

    private Map propCache = Collections.synchronizedMap(new WeakHashMap());
    
    
    /**
     *  Checks if the given property is present in the cache - if so, returns
     *  a reference to the cached value. Otherwise the given object is added
     *  to the cache and returned.
     *  @param obj
     *  @return the cached instance
     */
    public Property fetch(Property prop) {
        
        WeakReference ref = (WeakReference) propCache.get(prop);
        if (ref != null) {
            Property cacheEntry = (Property)ref.get();
            if (cacheEntry != null) {
                return cacheEntry;                
            }
        }
        propCache.put(prop, new WeakReference(prop));
        return prop;
    }
}
