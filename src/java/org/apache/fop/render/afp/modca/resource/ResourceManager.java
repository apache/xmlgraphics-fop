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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.Registry;

/**
 * Manages the creation and storage of document resources
 */
public class ResourceManager {
    /** Static logging instance */
    static final Log log = LogFactory.getLog(ResourceManager.class);

    /** Resource storage */
    private ResourceStore store;

    /** Resource creation factory */
    private ResourceFactory factory;

    private ExternalResourceManager external;

    /** Mapping of resource info --> store info */
    private Map/*<ResourceInfo,StoreInfo>*/ resourceStorageMap
            = new java.util.HashMap/*<ResourceInfo,StoreInfo>*/();

    /**
     * Main constructor
     */
    public ResourceManager() {
        this.factory = new ResourceFactory(this);
        this.store = new ResourceStore();
        this.external = new ExternalResourceManager(this);
    }

    /**
     * Creates and adds a new data object and stores the save record to a temporary file.
     *
     * @param dataObjectInfo a data object info
     *
     * @return a new store save information record
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public StoreInfo create(DataObjectInfo dataObjectInfo) throws IOException {
        StoreInfo storeInfo = null;
        Registry.ObjectType objectType = dataObjectInfo.getObjectType();
        if (objectType == null || !objectType.isIncludable()) {
            AbstractNamedAFPObject dataObj = factory.create(dataObjectInfo);
            if (dataObj == null) {
                log.error("Failed to create object: " + dataObjectInfo);
                return null;
            }
            storeInfo = store.save(dataObj);
        } else {
            ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
            storeInfo = (StoreInfo)resourceStorageMap.get(resourceInfo);
            if (storeInfo == null) {
                AbstractNamedAFPObject dataObj = factory.create(dataObjectInfo);
                if (dataObj == null) {
                    log.error("Failed to create object: " + dataObjectInfo);
                    return null;
                }
                storeInfo = store.save(dataObj);
                resourceStorageMap.put(resourceInfo, storeInfo);
            }
        }
        return storeInfo;
    }

    /**
     * Returns the resource factory
     *
     * @return the resource factory
     */
    public ResourceFactory getFactory() {
        return this.factory;
    }

    /**
     * Returns the resource store
     *
     * @return the resource store
     */
    public ResourceStore getStore() {
        return this.store;
    }

    /**
     * Returns the resource group manager
     *
     * @return the resource group manager
     */
    public ExternalResourceManager getExternalManager() {
        return this.external;
    }

    /**
     * Writes out all external resource groups that are held
     *
     * @throws java.io.IOException an I/O exception of some sort has occurred.
     */
    public void writeExternal() throws IOException {
        external.write();
    }

    /**
     * Clears the store
     * @throws IOException if an error occurs while clearing the resource store
     */
    public void clearStore() throws IOException {
        store.clear();
    }
}
