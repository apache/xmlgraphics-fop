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

/* $Id: $ */

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * A Resource Environment Group contains a set of resources for a document
 * or for a group of pages in a document.
 */
public class ResourceEnvironmentGroup extends AbstractEnvironmentGroup {
    /**
     * Default name for the resource group
     */
    private static final String DEFAULT_NAME = "REG00001";

    /**
     * The maps data resources contained in this resource environment group
     */
    private List/*<MapDataResource>*/ mapDataResources = null;
    
    /**
     * The maps page overlays contained in this resource environment group
     */
    private List mapPageOverlays = null;
    
    /**
     * The pre-process presentation objects contained in this resource environment group
     */
    private List/*<PreprocessPresentationObject>*/ preProcessPresentationObjects = null;

    /**
     * The resource environment group state
     */
    private boolean complete = false;

    /**
     * Default constructor
     */
    public ResourceEnvironmentGroup() {
        this(DEFAULT_NAME);
    }

    private List/*<MapDataResource>*/ getMapDataResources() {
        if (mapDataResources == null) {
            this.mapDataResources = new java.util.ArrayList/*<MapDataResource>*/();
        }
        return this.mapDataResources;
    }

    private List getMapPageOverlays() {
        if (mapPageOverlays == null) {
            this.mapPageOverlays = new java.util.ArrayList();
        }
        return this.mapPageOverlays;
    }

    private List/*<PreprocessPresentationObject>*/ getPreprocessPresentationObjects() {
        if (preProcessPresentationObjects == null) {
            this.preProcessPresentationObjects
                = new java.util.ArrayList/*<PreprocessPresentationObject>*/();
        }
        return this.preProcessPresentationObjects;
    }

    /**
     * Constructor for the ResourceEnvironmentGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the resource environment group name
     */
    public ResourceEnvironmentGroup(String name) {
        super(name);
    }

//    /**
//     * Adds an AFP object mapping reference to this resource environment group
//     * @param obj the object to add
//     */
//    public void addObject(AbstractStructuredAFPObject obj) {
//        getMapDataResources().add(new MapDataResource(obj));
//        createOverlay(obj.get);
//        getPreprocessPresentationObjects().add(new PreprocessPresentationObject(obj));
//    }
        
    /**
     * Returns an indication if the resource environment group is complete
     * @return whether or not this resource environment group is complete or not
     */
    public boolean isComplete() {
        return complete;
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
        data[5] = (byte) 0xD9; // Structured field id byte 3
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
    protected void writeContent(OutputStream os) throws IOException {
        writeObjects(mapDataResources, os);
        writeObjects(mapPageOverlays, os);
        writeObjects(preProcessPresentationObjects, os);
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
        data[5] = (byte) 0xD9; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }
}
