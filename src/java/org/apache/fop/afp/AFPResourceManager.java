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

package org.apache.fop.afp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.PageSegment;
import org.apache.fop.afp.modca.Registry;
import org.apache.fop.afp.modca.ResourceGroup;

/**
 * Manages the creation and storage of document resources
 */
public class AFPResourceManager {
    /** The AFP datastream (document tree) */
    private DataStream dataStream;

    /** Resource creation factory */
    private final Factory factory;

    private final AFPStreamer streamer;

    private final AFPDataObjectFactory dataObjectFactory;

    /** Maintain a reference count of instream objects for referencing purposes */
    private int instreamObjectCount = 0;

    /** a mapping of resourceInfo --> names of includable objects */
    private final Map/*<AFPResourceInfo,String>*/ includableObjectsMap
        = new java.util.HashMap()/*<AFPResourceInfo,String>*/;

    private Map pageSegmentMap = new java.util.HashMap();

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
     * @param paintingState the AFP painting state
     * @param outputStream the outputstream
     * @return a new AFP DataStream
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public DataStream createDataStream(AFPPaintingState paintingState, OutputStream outputStream)
    throws IOException {
        this.dataStream = streamer.createDataStream(paintingState);
        streamer.setOutputStream(outputStream);
        return this.dataStream;
    }

    /**
     * Returns the AFP DataStream
     *
     * @return the AFP DataStream
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
        updateResourceInfoUri(resourceInfo);

        String objectName = (String)includableObjectsMap.get(resourceInfo);
        if (objectName != null) {
            // an existing data resource so reference it by adding an include to the current page
            includeObject(dataObjectInfo, objectName);
            return;
        }

        objectName = (String)pageSegmentMap.get(resourceInfo);
        if (objectName != null) {
            // an existing data resource so reference it by adding an include to the current page
            includePageSegment(dataObjectInfo, objectName);
            return;
        }

        boolean useInclude = true;
        Registry.ObjectType objectType = null;

        // new resource so create
        if (dataObjectInfo instanceof AFPImageObjectInfo) {
            AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)dataObjectInfo;
            namedObj = dataObjectFactory.createImage(imageObjectInfo);
        } else if (dataObjectInfo instanceof AFPGraphicsObjectInfo) {
            AFPGraphicsObjectInfo graphicsObjectInfo = (AFPGraphicsObjectInfo)dataObjectInfo;
            namedObj = dataObjectFactory.createGraphic(graphicsObjectInfo);
        } else {
            // natively embedded data object
            namedObj = dataObjectFactory.createObjectContainer(dataObjectInfo);
            objectType = dataObjectInfo.getObjectType();
            useInclude = objectType != null && objectType.isIncludable();
        }

        AFPResourceLevel resourceLevel = resourceInfo.getLevel();
        ResourceGroup resourceGroup = streamer.getResourceGroup(resourceLevel);
        useInclude &= resourceGroup != null;
        if (useInclude) {

            boolean usePageSegment = dataObjectInfo.isCreatePageSegment();

            // if it is to reside within a resource group at print-file or external level
            if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
                if (usePageSegment) {
                    String pageSegmentName = "S10" + namedObj.getName().substring(3);
                    namedObj.setName(pageSegmentName);
                    PageSegment seg = new PageSegment(pageSegmentName);
                    seg.addObject(namedObj);
                    namedObj = seg;
                }

                // wrap newly created data object in a resource object
                namedObj = dataObjectFactory.createResource(namedObj, resourceInfo, objectType);
            }

            // add data object into its resource group destination
            resourceGroup.addObject(namedObj);

            // create the include object
            objectName = namedObj.getName();
            if (usePageSegment) {
                includePageSegment(dataObjectInfo, objectName);
                pageSegmentMap.put(resourceInfo, objectName);
            } else {
                includeObject(dataObjectInfo, objectName);
                // record mapping of resource info to data object resource name
                includableObjectsMap.put(resourceInfo, objectName);
            }

        } else {
            // not to be included so inline data object directly into the current page
            dataStream.getCurrentPage().addObject(namedObj);
        }
    }

    private void updateResourceInfoUri(AFPResourceInfo resourceInfo) {
        String uri = resourceInfo.getUri();
        if (uri == null) {
            uri = "/";
        }
        // if this is an instream data object adjust the uri to ensure that its unique
        if (uri.endsWith("/")) {
            uri += "#" + (++instreamObjectCount);
            resourceInfo.setUri(uri);
        }
    }

    private void includeObject(AFPDataObjectInfo dataObjectInfo,
            String objectName) {
        IncludeObject includeObject
            = dataObjectFactory.createInclude(objectName, dataObjectInfo);
        dataStream.getCurrentPage().addObject(includeObject);
    }

    private void includePageSegment(AFPDataObjectInfo dataObjectInfo,
            String pageSegmentName) {
        int x = dataObjectInfo.getObjectAreaInfo().getX();
        int y = dataObjectInfo.getObjectAreaInfo().getY();
        AbstractPageObject currentPage = dataStream.getCurrentPage();
        boolean createHardPageSegments = true;
        currentPage.createIncludePageSegment(pageSegmentName, x, y, createHardPageSegments);
    }

}