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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import org.apache.fop.render.afp.DataObjectCache;
import org.apache.fop.render.afp.DataObjectCache.Record;

/**
 * A Resource Group contains a set of overlays.
 */
public final class ResourceGroup extends AbstractNamedAFPObject {
    
    /** Default name for the resource group */
    private static final String DEFAULT_NAME = "RG000001";

    /** Set of resource uri */
    private Set/*<String>*/ resourceSet = new java.util.HashSet/*<String>*/();

    private DataObjectCache cache = DataObjectCache.getInstance();

    /**
     * Default constructor
     */
    public ResourceGroup() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor for the ResourceGroup, this takes a
     * name parameter which must be 8 characters long.
     * 
     * @param name the resource group name
     */
    public ResourceGroup(String name) {
        super(name);
    }
    
//    /**
//     * Creates a data object in this resource group
//     * 
//     * @param dataObjectInfo the data object info
//     * @return an include object reference
//     */
//    public IncludeObject createObject(DataObjectInfo dataObjectInfo) {
//        String uri = dataObjectInfo.getUri();
//        resourceSet.get();
//        DataObjectAccessor dataObjectAccessor
//            = (DataObjectAccessor)getResourceMap().getData(dataObjectInfo.getUri());
//        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
//        ResourceLevel resourceLevel = resourceInfo.getLevel();
//        AbstractDataObject dataObj;
//        if (dataObjectAccessor == null) {
//            dataObj = dataObjectFactory.createObject(dataObjectInfo);
//            ObjectContainer objectContainer = null;
//            String resourceName = resourceInfo.getName();
//            if (resourceName != null) {
//                objectContainer = new ObjectContainer(resourceName);
//            } else {
//                objectContainer = createObjectContainer();
//                resourceName = objectContainer.getName();
//            }
//            objectContainer.setDataObject(dataObj);
//            objectContainer.setDataObjectInfo(dataObjectInfo);
//            
//            // When located at print-file level or externally,
//            // wrap the object container in a resource object
//            if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
//                ResourceObject resourceObject = new ResourceObject(resourceName);
//                resourceObject.setDataObject(objectContainer);
//                resourceObject.setDataObjectInfo(dataObjectInfo);
//                dataObjectAccessor = resourceObject;
//            } else { // Access data object through container
//                dataObjectAccessor = objectContainer;
//            }
//            
//            // Add to resource map
//            getResourceMap().put(dataObjectInfo.getUri(), dataObjectAccessor);            
//        }
//        String name = dataObjectAccessor.getName();
//        IncludeObject includeObj = dataObjectFactory.createInclude(dataObjectInfo);
//        return includeObj;
//    }
    
//    /**
//     * Checks if a named object is of a valid type to be added to a resource group
//     * 
//     * @param namedObj a named object
//     * @return true if the named object is of a valid type to be added to a resource group
//     */
//    private boolean isValidObjectType(AbstractNamedAFPObject namedObj) {
//        return (namedObj instanceof Overlay
//                || namedObj instanceof ResourceObject
//                || namedObj instanceof PageSegment
//                || namedObj instanceof GraphicsObject
//                || namedObj instanceof ImageObject
//                || namedObj instanceof ObjectContainer
//                || namedObj instanceof Document
//                // || namedObj instanceof FormMap
//                // || namedObj instanceof BarcodeObject 
//                );
//    }

    /**
     * Add this object cache record to this resource group
     * 
     * @param record the cache record
     */
    public void addObject(Record record) {
        resourceSet.add(record);
    }
    
    /**
     * Returns the number of resources contained in this resource group
     * 
     * @return the number of resources contained in this resource group
     */
    public int getResourceCount() {
        return resourceSet.size(); 
    }
    
    /**
     * Returns true if the resource exists within this resource group,
     * false otherwise.
     * 
     * @param uri the uri of the resource
     * @return true if the resource exists within this resource group
     */
    public boolean resourceExists(String uri) {
        return resourceSet.contains(uri);
    }
    
    /** {@inheritDoc} */
    public void writeContent(OutputStream os) throws IOException {
        Iterator it = resourceSet.iterator();
        while (it.hasNext()) {
            Record record = (Record)it.next();
            byte[] data = cache.retrieve(record);
            if (data != null) {
                os.write(data);
            } else {
                log.error("data was null");
            }
        }
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.BEGIN, Category.RESOURCE_GROUP);
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        copySF(data, Type.END, Category.RESOURCE_GROUP);
        os.write(data);
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return this.name + " " + resourceSet/*getResourceMap()*/;
    }
}
