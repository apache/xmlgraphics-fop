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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.IncludedResourceObject;
import org.apache.fop.afp.modca.PageSegment;
import org.apache.fop.afp.modca.Registry;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.util.ResourceAccessor;

/**
 * Manages the creation and storage of document resources
 */
public class AFPResourceManager {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPResourceManager.class);

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

    private Map pageSegmentMap = new java.util.HashMap();

    private AFPResourceLevelDefaults resourceLevelDefaults = new AFPResourceLevelDefaults();

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

        String objectName = (String)includeNameMap.get(resourceInfo);
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
                includeNameMap.put(resourceInfo, objectName);
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

    /**
     * Handles font embedding. If a font is embeddable and has not already been embedded it will be.
     * @param afpFont the AFP font to be checked for embedding
     * @param charSet the associated character set
     * @throws IOException if there's a problem while embedding the external resources
     */
    public void embedFont(AFPFont afpFont, CharacterSet charSet)
            throws IOException {
        if (afpFont.isEmbeddable()) {
            //Embed fonts (char sets and code pages)
            if (charSet.getResourceAccessor() != null) {
                ResourceAccessor accessor = charSet.getResourceAccessor();
                createIncludedResource(
                        charSet.getName(), accessor,
                        ResourceObject.TYPE_FONT_CHARACTER_SET);
                createIncludedResource(
                        charSet.getCodePage(), accessor,
                        ResourceObject.TYPE_CODE_PAGE);
            }
        }
    }

    private void includePageSegment(AFPDataObjectInfo dataObjectInfo,
            String pageSegmentName) {
        int x = dataObjectInfo.getObjectAreaInfo().getX();
        int y = dataObjectInfo.getObjectAreaInfo().getY();
        AbstractPageObject currentPage = dataStream.getCurrentPage();
        boolean createHardPageSegments = true;
        currentPage.createIncludePageSegment(pageSegmentName, x, y, createHardPageSegments);
    }

    /**
     * Creates an included resource object by loading the contained object from a file.
     * @param resourceName the name of the resource
     * @param accessor resource accessor to access the resource with
     * @param resourceObjectType the resource object type ({@link ResourceObject}.*)
     * @throws IOException if an I/O error occurs while loading the resource
     */
    public void createIncludedResource(String resourceName, ResourceAccessor accessor,
                byte resourceObjectType) throws IOException {
        URI uri;
        try {
            uri = new URI(resourceName.trim());
        } catch (URISyntaxException e) {
            throw new IOException("Could not create URI from resource name: " + resourceName
                    + " (" + e.getMessage() + ")");
        }

        createIncludedResource(resourceName, uri, accessor, resourceObjectType);
    }

    /**
     * Creates an included resource object by loading the contained object from a file.
     * @param resourceName the name of the resource
     * @param uri the URI for the resource
     * @param accessor resource accessor to access the resource with
     * @param resourceObjectType the resource object type ({@link ResourceObject}.*)
     * @throws IOException if an I/O error occurs while loading the resource
     */
    public void createIncludedResource(String resourceName, URI uri, ResourceAccessor accessor,
                byte resourceObjectType) throws IOException {
        AFPResourceLevel resourceLevel = new AFPResourceLevel(AFPResourceLevel.PRINT_FILE);

        AFPResourceInfo resourceInfo = new AFPResourceInfo();
        resourceInfo.setLevel(resourceLevel);
        resourceInfo.setName(resourceName);
        resourceInfo.setUri(uri.toASCIIString());

        String objectName = (String)includeNameMap.get(resourceInfo);
        if (objectName == null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding included resource: " + resourceName);
            }
            IncludedResourceObject resourceContent = new IncludedResourceObject(
                        resourceName, accessor, uri);

            ResourceObject resourceObject = factory.createResource(resourceName);
            resourceObject.setDataObject(resourceContent);
            resourceObject.setType(resourceObjectType);

            ResourceGroup resourceGroup = streamer.getResourceGroup(resourceLevel);
            resourceGroup.addObject(resourceObject);
            // record mapping of resource info to data object resource name
            includeNameMap.put(resourceInfo, resourceName);
        } else {
            //skip, already created
        }
    }

    /**
     * Sets resource level defaults. The existing defaults over merged with the ones passed in
     * as parameter.
     * @param defaults the new defaults
     */
    public void setResourceLevelDefaults(AFPResourceLevelDefaults defaults) {
        this.resourceLevelDefaults.mergeFrom(defaults);
    }

    /**
     * Returns the resource level defaults in use with this resource manager.
     * @return the resource level defaults
     */
    public AFPResourceLevelDefaults getResourceLevelDefaults() {
        return this.resourceLevelDefaults;
    }

}
