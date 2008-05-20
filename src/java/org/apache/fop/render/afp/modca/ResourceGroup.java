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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ImageObjectInfo;
import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.ResourceLevel;
import org.apache.fop.render.afp.tools.StringUtils;

/**
 * A Resource Group contains a set of overlays.
 */
public final class ResourceGroup extends AbstractNamedAFPObject {
    
    /**
     * Default name for the resource group
     */
    private static final String DEFAULT_NAME = "RG000001";

    /**
     * Mapping of resource uri to data resource object (image/graphic) 
     */
    private Map/*<String, Writeable>*/ resourceMap = null;

    /**
     * This resource groups container
     */
    private AbstractResourceGroupContainer container = null;

    /**
     * Default constructor
     * @param container the resource group container 
     */
    public ResourceGroup(AbstractResourceGroupContainer container) {
        this(DEFAULT_NAME, container);
    }

    /**
     * Constructor for the ResourceGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the resource group name
     * @param container the parent resource group container
     */
    public ResourceGroup(String name, AbstractResourceGroupContainer container) {
        super(name);
        this.container = container;
    }

    private AbstractResourceGroupContainer getContainer() {
        return this.container;
    }

    private static final String OBJECT_CONTAINER_NAME_PREFIX = "OC";

    private ObjectContainer createObjectContainer() {
        String name = OBJECT_CONTAINER_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(getResourceCount() + 1), '0', 6);
        return new ObjectContainer(name);
    }
    
    /**
     * Creates a data object in this resource group
     * @param dataObjectInfo the data object info
     * @return an include object reference
     */
    public IncludeObject createObject(DataObjectInfo dataObjectInfo) {
        DataObjectAccessor dataObjectAccessor
            = (DataObjectAccessor)getResourceMap().get(dataObjectInfo.getUri());
        ResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        ResourceLevel resourceLevel = resourceInfo.getLevel();
        AbstractDataObject dataObj;
        if (dataObjectAccessor == null) {
            if (dataObjectInfo instanceof ImageObjectInfo) {
                dataObj = getContainer().createImage((ImageObjectInfo)dataObjectInfo);
            } else {
                dataObj = getContainer().createGraphic(dataObjectInfo);
            }

            dataObj.setViewport(dataObjectInfo.getX(), dataObjectInfo.getY(),
                    dataObjectInfo.getWidth(), dataObjectInfo.getHeight(),
                    dataObjectInfo.getWidthRes(), dataObjectInfo.getHeightRes(),
                    dataObjectInfo.getRotation());

            ObjectContainer objectContainer = null;
            String resourceName = resourceInfo.getName();
            if (resourceName != null) {
                objectContainer = new ObjectContainer(resourceName);
            } else {
                objectContainer = createObjectContainer();
                resourceName = objectContainer.getName();
            }
            objectContainer.setDataObject(dataObj);
            objectContainer.setDataObjectInfo(dataObjectInfo);
            
            // When externally located, wrap the object container in a resource object
            if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
                ResourceObject resourceObject = new ResourceObject(resourceName);
                resourceObject.setDataObject(objectContainer);
                resourceObject.setDataObjectInfo(dataObjectInfo);
                dataObjectAccessor = resourceObject;
            } else { // Access data object through container
                dataObjectAccessor = objectContainer;
            }
            
            // Add to resource map
            getResourceMap().put(dataObjectInfo.getUri(), dataObjectAccessor);            
        }
        String name = dataObjectAccessor.getName();
        IncludeObject includeObj = new IncludeObject(name, dataObjectAccessor);
        return includeObj;
    }
    
    /**
     * Checks if a named object is of a valid type to be added to a resource group
     * @param namedObj a named object
     * @return true if the named object is of a valid type to be added to a resource group
     */
    private boolean isValidObjectType(AbstractNamedAFPObject namedObj) {
        return (namedObj instanceof Overlay
                || namedObj instanceof ResourceObject
                || namedObj instanceof PageSegment
                || namedObj instanceof GraphicsObject
                || namedObj instanceof ImageObject
                || namedObj instanceof ObjectContainer
                || namedObj instanceof Document
                // || namedObj instanceof FormMap
                // || namedObj instanceof BarcodeObject 
                );
    }
    /**
     * Adds a named object to this resource group
     * @param namedObj a named AFP object
     */
    protected void addObject(AbstractNamedAFPObject namedObj) {
        if (isValidObjectType(namedObj)) {
            getResourceMap().put(namedObj.getName(), namedObj);
        } else {
            throw new IllegalArgumentException("invalid object type " + namedObj);
        }
    }
    
    /**
     * @return the number of resources contained in this resource group
     */
    public int getResourceCount() {
        if (resourceMap != null) {
            return resourceMap.size(); 
        }
        return 0;
    }
    
    /**
     * Returns true if the resource exists within this resource group,
     * false otherwise.
     * 
     * @param uri the uri of the resource
     * @return true if the resource exists within this resource group
     */
    public boolean resourceExists(String uri) {
        return getResourceMap().containsKey(uri);
    }
    
    /**
     * Returns the list of resources
     * @return the list of resources
     */
    public Map/*<String, DataObjectAccessor>*/ getResourceMap() {
        if (resourceMap == null) {
            resourceMap = new java.util.HashMap/*<String, Writeable>*/();
        }
        return resourceMap;
    }

    /**
     * {@inheritDoc}
     */
    public void writeContent(OutputStream os) throws IOException {
        if (resourceMap != null) {
            Collection includes = resourceMap.values();
            Iterator it = includes.iterator();
            while (it.hasNext()) {
                Writable dataObject = (Writable)it.next();
                dataObject.write(os);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xC6; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xC6; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.name + " " + getResourceMap();
    }
}
