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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPResourceLevel.ResourceType;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.ActiveEnvironmentGroup;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.IncludedResourceObject;
import org.apache.fop.afp.modca.ObjectContainer;
import org.apache.fop.afp.modca.PageSegment;
import org.apache.fop.afp.modca.Registry;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.AFPResourceUtil;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontType;
import org.apache.fop.render.afp.AFPFontConfig;

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
    private int instreamObjectCount;

    /** Mapping of resourceInfo to AbstractCachedObject */
    private final Map<AFPResourceInfo, List<AbstractCachedObject>> includeObjectCache =
            new HashMap<AFPResourceInfo, List<AbstractCachedObject>>();

    private AFPResourceLevelDefaults resourceLevelDefaults = new AFPResourceLevelDefaults();

    protected boolean includeCached = true;

    /**
     * Main constructor
     *
     * @param resourceResolver  the associated {@link InternalResourceResolver} instance
     */
    public AFPResourceManager(InternalResourceResolver resourceResolver) {
        this.factory = new Factory();
        this.streamer = new AFPStreamer(factory, resourceResolver);
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
     * Sets the default resource group URI.
     *
     * @param uri the default resource group URI
     */

    public void setDefaultResourceGroupUri(URI uri) {
        streamer.setDefaultResourceGroupUri(uri);
    }

    /**
     * Tries to create an include of a data object that has been previously added to the
     * AFP data stream. If no such object was available, the method returns false which serves
     * as a signal that the object has to be created.
     * @param dataObjectInfo the data object info
     * @return true if the inclusion succeeded, false if the object was not available
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public boolean tryIncludeObject(AFPDataObjectInfo dataObjectInfo) throws IOException {
        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        updateResourceInfoUri(resourceInfo);
        return includeCachedObject(resourceInfo, dataObjectInfo.getObjectAreaInfo());
    }

    /**
     * Creates a new data object in the AFP datastream
     *
     * @param dataObjectInfo the data object info
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void createObject(AFPDataObjectInfo dataObjectInfo) throws IOException {
        if (tryIncludeObject(dataObjectInfo)) {
            //Object has already been produced and is available by inclusion, so return early.
            return;
        }

        AbstractNamedAFPObject namedObj = null;
        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();

        boolean useInclude = true;
        Registry.ObjectType objectType = null;

        // new resource so create
        if (dataObjectInfo.isUseIocaImages() && dataObjectInfo instanceof AFPImageObjectInfo) {
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
            final boolean usePageSegment = dataObjectInfo.isCreatePageSegment();

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
                namedObj = dataObjectFactory.createResource(namedObj, resourceInfo, objectType, dataObjectInfo);
            }

            // add data object into its resource group destination
            resourceGroup.addObject(namedObj);
            includeObject(namedObj, dataObjectInfo);
        } else {
            // not to be included so inline data object directly into the current page
            dataStream.getCurrentPage().addObject(namedObj);
        }
    }

    private abstract class AbstractCachedObject {
        protected String objectName;
        protected AFPDataObjectInfo dataObjectInfo;

        public AbstractCachedObject(String objectName, AFPDataObjectInfo dataObjectInfo) {
            this.objectName = objectName;
            this.dataObjectInfo = dataObjectInfo;


        }
        protected abstract void includeObject();
    }

    private class CachedPageSegment extends AbstractCachedObject {

        public CachedPageSegment(String objectName, AFPDataObjectInfo dataObjectInfo) {
           super(objectName, dataObjectInfo);
        }

        protected void includeObject() {
            includePageSegment(dataObjectInfo, objectName);
        }

    }

    private class CachedObject extends AbstractCachedObject {

        public CachedObject(String objectName, AFPDataObjectInfo dataObjectInfo) {
           super(objectName, dataObjectInfo);
        }

        protected void includeObject() {
            AFPResourceManager.this.includeObject(dataObjectInfo, objectName);
        }

    }


    private void includeObject(AbstractNamedAFPObject namedObj, AFPDataObjectInfo dataObjectInfo) {

        // create the include object
        String objectName = namedObj.getName();

        AbstractCachedObject cachedObject;

        if (dataObjectInfo.isCreatePageSegment()) {
            cachedObject = new CachedPageSegment(objectName, dataObjectInfo);
        } else {
            cachedObject = new CachedObject(objectName, dataObjectInfo);
        }

        cachedObject.includeObject();

        addToCache(dataObjectInfo.getResourceInfo(), cachedObject);

        //The data field of dataObjectInfo is not further required
        // therefore we are safe to null the reference, saving memory
        dataObjectInfo.setData(null);

    }

    private void addToCache(AFPResourceInfo resourceInfo, AbstractCachedObject cachedObject) {
        List<AbstractCachedObject> objs = includeObjectCache.get(resourceInfo);
        if (objs == null) {
            objs = new ArrayList<AbstractCachedObject>();
            includeObjectCache.put(resourceInfo, objs);
        }
        objs.add(cachedObject);
    }

    /**
     * Returns {@code true} if the passed {@link AFPResourceInfo} instance is already cached.
     *
     * @param resourceInfo  the resource info to check
     * @return  {@code true} if the object is cached
     */
    public boolean isObjectCached(AFPResourceInfo resourceInfo) {
        return includeObjectCache.containsKey(resourceInfo);
    }

    /**
     *
     * @param resourceInfo  the resource info to check
     * @param areaInfo  the area info to check
     * @return  {@code true} if ...
     */
    public boolean includeCachedObject(AFPResourceInfo resourceInfo, AFPObjectAreaInfo areaInfo) {
        List<AbstractCachedObject> cachedObjectList = includeObjectCache.get(resourceInfo);
        if (cachedObjectList != null && includeCached) {
            for (AbstractCachedObject cachedObject : cachedObjectList) {
                if (areaInfo != null && cachedObjectList.size() == 1) {
                    cachedObject.dataObjectInfo.setObjectAreaInfo(areaInfo);
                }
                cachedObject.includeObject();
            }
            return true;
        } else {
            return false;
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
        IncludeObject includeObject = dataObjectFactory.createInclude(objectName, dataObjectInfo);
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
                AFPResourceAccessor accessor = charSet.getResourceAccessor();
                if (afpFont.getFontType() == FontType.TRUETYPE) {

                    createIncludedResource(afpFont.getFontName(),
                            ((AFPFontConfig.AFPTrueTypeFont) afpFont).getUri(), accessor,
                            ResourceObject.TYPE_OBJECT_CONTAINER, true,
                            ((AFPFontConfig.AFPTrueTypeFont) afpFont).getTTC());
                } else {
                    createIncludedResource(
                            charSet.getName(), accessor,
                            ResourceObject.TYPE_FONT_CHARACTER_SET);
                    createIncludedResource(
                            charSet.getCodePage(), accessor,
                            ResourceObject.TYPE_CODE_PAGE);
                }
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
    public void createIncludedResource(String resourceName, AFPResourceAccessor accessor,
                byte resourceObjectType) throws IOException {
        URI uri;
        try {
            uri = new URI(resourceName.trim());
        } catch (URISyntaxException e) {
            throw new IOException("Could not create URI from resource name: " + resourceName
                    + " (" + e.getMessage() + ")");
        }

        createIncludedResource(resourceName, uri, accessor, resourceObjectType, false, null);
    }

    /**
     * Creates an included resource object by loading the contained object from a file.
     * @param resourceName the name of the resource
     * @param uri the URI for the resource
     * @param accessor resource accessor to access the resource with
     * @param resourceObjectType the resource object type ({@link ResourceObject}.*)
     * @throws IOException if an I/O error occurs while loading the resource
     */
    public void createIncludedResource(String resourceName, URI uri, AFPResourceAccessor accessor,
                                       byte resourceObjectType, boolean truetype, String ttc) throws IOException {
        AFPResourceLevel resourceLevel = new AFPResourceLevel(ResourceType.PRINT_FILE);

        AFPResourceInfo resourceInfo = new AFPResourceInfo();
        resourceInfo.setLevel(resourceLevel);
        resourceInfo.setName(resourceName);
        resourceInfo.setUri(uri.toASCIIString());

        List<AbstractCachedObject> cachedObject = includeObjectCache.get(resourceInfo);
        if (cachedObject == null) {
            if (log.isDebugEnabled()) {
                log.debug("Adding included resource: " + resourceName);
            }

            ResourceGroup resourceGroup = streamer.getResourceGroup(resourceLevel);

            if (truetype) {
                ResourceObject res = factory.createResource();
                res.setType(ResourceObject.TYPE_OBJECT_CONTAINER);

                ActiveEnvironmentGroup.setupTruetypeMDR(res, false);

                ObjectContainer oc = factory.createObjectContainer();
                InputStream is = accessor.createInputStream(uri);

                if (ttc != null) {
                    oc.setData(extractTTC(ttc, is));
                } else {
                    oc.setData(IOUtils.toByteArray(is));
                }

                ActiveEnvironmentGroup.setupTruetypeMDR(oc, true);

                res.addTriplet(new EncodingTriplet(1200));

                res.setFullyQualifiedName(FullyQualifiedNameTriplet.TYPE_REPLACE_FIRST_GID_NAME,
                        FullyQualifiedNameTriplet.FORMAT_CHARSTR, resourceName, true);

                res.setDataObject(oc);
                resourceGroup.addObject(res);
            } else {
                ResourceObject resourceObject = factory.createResource(resourceName);
                IncludedResourceObject resourceContent = new IncludedResourceObject(
                        resourceName, accessor, uri);
                resourceObject.setDataObject(resourceContent);
                resourceObject.setType(resourceObjectType);
                resourceGroup.addObject(resourceObject);
            }

            //TODO what is the data object?
            CachedObject newcachedObject = new CachedObject(resourceName, null);

            // record mapping of resource info to data object resource name
            addToCache(resourceInfo, newcachedObject);
        } else {
            //skip, already created
        }
    }

    private byte[] extractTTC(String ttc, InputStream is) throws IOException {
//        TrueTypeCollection trueTypeCollection = new TrueTypeCollection(is);
//        for (TrueTypeFont ttf : trueTypeCollection.getFonts()) {
//            String name = ttf.getNaming().getFontFamily();
//            if (name.equals(ttc)) {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                TTFSubsetter s = new TTFSubsetter(ttf, null);
//                for (int i = 0; i < 256 * 256; i++) {
//                    s.addCharCode(i);
//                }
//                s.writeToStream(bos);
//                return bos.toByteArray();
//            }
//        }
        throw new IOException(ttc + " not supported");
    }

    /**
     * Creates an included resource extracting the named resource from an external source.
     * @param resourceName the name of the resource
     * @param uri the URI for the resource
     * @param accessor resource accessor to access the resource with
     * @throws IOException if an I/O error occurs while loading the resource
     */
    public void createIncludedResourceFromExternal(final String resourceName,
            final URI uri, final AFPResourceAccessor accessor) throws IOException {

        AFPResourceLevel resourceLevel = new AFPResourceLevel(ResourceType.PRINT_FILE);

        AFPResourceInfo resourceInfo = new AFPResourceInfo();
        resourceInfo.setLevel(resourceLevel);
        resourceInfo.setName(resourceName);
        resourceInfo.setUri(uri.toASCIIString());

        List<AbstractCachedObject> resource = includeObjectCache.get(resourceInfo);
        if (resource == null) {
            ResourceGroup resourceGroup = streamer.getResourceGroup(resourceLevel);

            //resourceObject delegates write commands to copyNamedResource()
            //The included resource may already be wrapped in a resource object
            AbstractNamedAFPObject resourceObject = new AbstractNamedAFPObject(null) {

                @Override
                protected void writeContent(OutputStream os) throws IOException {
                    InputStream inputStream = null;
                    try {
                        inputStream = accessor.createInputStream(uri);
                        BufferedInputStream bin = new BufferedInputStream(inputStream);
                        AFPResourceUtil.copyNamedResource(resourceName, bin, os);
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }

                //bypass super.writeStart
                @Override
                protected void writeStart(OutputStream os) throws IOException { }
                //bypass super.writeEnd
                @Override
                protected void writeEnd(OutputStream os) throws IOException { }
            };
            resourceGroup.addObject(resourceObject);
            CachedObject newresource = new CachedObject(resourceName, null);
            addToCache(resourceInfo, newresource);
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
