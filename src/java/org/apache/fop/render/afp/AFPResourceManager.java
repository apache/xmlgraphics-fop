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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.AbstractDataObject;
import org.apache.fop.render.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.render.afp.modca.DataStream;
import org.apache.fop.render.afp.modca.Factory;
import org.apache.fop.render.afp.modca.IncludeObject;
import org.apache.fop.render.afp.modca.Registry;
import org.apache.fop.render.afp.modca.ResourceGroup;

/**
 * Manages the creation and storage of document resources
 */
public class AFPResourceManager {
    /** Static logging instance */
    private static final Log log = LogFactory.getLog(AFPResourceManager.class);

    /** The AFP datastream (document tree) */
    private DataStream dataStream;

    /** Resource creation factory */
    private final Factory factory;

    private final AFPStreamer streamer;

    private final AFPDataObjectFactory dataObjectFactory;

    /** Maintain a reference count of instream objects for referencing purposes */
    private int instreamObjectCount = 0;

    /** a mapping of resourceInfo --> include name */
    private final Map/*<AFPResourceInfo,String>*/ includeNameMap
        = new java.util.HashMap()/*<AFPResourceInfo,String>*/;

    /**
     * Main constructor
     */
    public AFPResourceManager() {
        this.factory = new Factory();
        this.streamer = new AFPStreamer(factory);
        this.dataObjectFactory = new AFPDataObjectFactory(factory);
    }

    /**
     * Sets the outputstream
     *
     * @param state the afp state
     * @param outputStream the outputstream
     */
    public void createDataStream(AFPState state, OutputStream outputStream) {
        this.dataStream = streamer.createDataStream(state);
        streamer.setOutputStream(outputStream);
    }

    /**
     * Returns the AFPDocumentStream
     *
     * @return the AFPDocumentStream
     */
    public DataStream getDataStream() {
        return this.dataStream;
    }

    /**
     * Tells the streamer to write
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void writeToStream() throws IOException {
        streamer.close();
    }

    /**
     * Sets the default resource group file path
     *
     * @param filePath the default resource group file path
     */

    public void setDefaultResourceGroupFilePath(String filePath) {
        streamer.setDefaultResourceGroupFilePath(filePath);
    }

    /**
     * Creates a new data object in the AFP datastream
     *
     * @param dataObjectInfo the data object info
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void createObject(AFPDataObjectInfo dataObjectInfo) throws IOException {
        AbstractNamedAFPObject namedObj = null;

        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        String uri = resourceInfo.getUri();
        if (uri == null) {
            uri = "/";
        }
        // if this is an instream data object adjust the uri to ensure that its unique
        if (uri.endsWith("/")) {
            uri += "#" + (++instreamObjectCount);
            resourceInfo.setUri(uri);
        }

        String objectName = (String)includeNameMap.get(resourceInfo);
        if (objectName == null) {
            boolean useInclude = true;
            Registry.ObjectType objectType = null;

            // new resource so create
            if (dataObjectInfo instanceof AFPImageObjectInfo) {
                AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)dataObjectInfo;
                namedObj = dataObjectFactory.createImage(imageObjectInfo);
            } else if (dataObjectInfo instanceof AFPGraphicsObjectInfo) {
                namedObj = dataObjectFactory.createGraphic((AFPGraphicsObjectInfo)dataObjectInfo);
            } else {
                // natively embedded object
                namedObj = dataObjectFactory.createObjectContainer(dataObjectInfo);
                objectType = dataObjectInfo.getObjectType();
                useInclude = objectType != null && objectType.isIncludable();
            }

            // set data object viewport (i.e. position, rotation, dimension, resolution)
            if (namedObj instanceof AbstractDataObject) {
                AbstractDataObject dataObj = (AbstractDataObject)namedObj;
                dataObj.setViewport(dataObjectInfo);
            }

            AFPResourceLevel resourceLevel = resourceInfo.getLevel();
            ResourceGroup resourceGroup = streamer.getResourceGroup(resourceLevel);
            useInclude &= resourceGroup != null;
            if (useInclude) {
                // if it is to reside within a resource group at print-file or external level
                if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
                    // wrap newly created data object in a resource object
                    namedObj = dataObjectFactory.createResource(namedObj, resourceInfo, objectType);
                }

                // add data object into its resource group destination
                resourceGroup.addObject(namedObj);

                // create the include object
                objectName = namedObj.getName();
                IncludeObject includeObject
                    = dataObjectFactory.createInclude(objectName, dataObjectInfo);

                // add an include to the current page
                dataStream.getCurrentPage().addObject(includeObject);

                // record mapping of resource info to data object resource name
                includeNameMap.put(resourceInfo, objectName);
            } else {
                // not to be included so inline data object directly into the current page
                dataStream.getCurrentPage().addObject(namedObj);
            }
        } else {
            // an existing data resource so reference it by adding an include to the current page
            IncludeObject includeObject
                = dataObjectFactory.createInclude(objectName, dataObjectInfo);
            dataStream.getCurrentPage().addObject(includeObject);
        }
    }

}