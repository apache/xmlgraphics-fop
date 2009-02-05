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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;

/**
 * A base abstract AFP raw stream image handler
 */
public abstract class AbstractAFPImageHandlerRawStream extends AFPImageHandler
        implements ImageHandler {

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPDataObjectInfo dataObjectInfo = super.generateDataObjectInfo(rendererImageInfo);
        ImageRawStream rawStream = (ImageRawStream) rendererImageInfo.getImage();
        AFPRendererContext rendererContext
            = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();

        updateDataObjectInfo(dataObjectInfo, rawStream, afpInfo.getResourceManager());

        setAdditionalParameters(dataObjectInfo, rawStream);
        return dataObjectInfo;
    }

    /**
     * Sets additional parameters on the image object info being built. By default, this
     * method does nothing but it can be overridden to provide additional functionality.
     * @param imageObjectInfo the image object info being built
     * @param image the image being processed
     */
    protected void setAdditionalParameters(AFPDataObjectInfo imageObjectInfo,
            ImageRawStream image) {
        //nop
    }

    private void updateDataObjectInfo(AFPDataObjectInfo dataObjectInfo,
            ImageRawStream rawStream, AFPResourceManager resourceManager) throws IOException {
        dataObjectInfo.setMimeType(rawStream.getFlavor().getMimeType());

        AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
        if (!resourceInfo.levelChanged()) {
            resourceInfo.setLevel(resourceManager.getResourceLevelDefaults()
                    .getDefaultResourceLevel(ResourceObject.TYPE_IMAGE));
        }

        InputStream inputStream = rawStream.createInputStream();
        try {
            dataObjectInfo.setData(IOUtils.toByteArray(inputStream));
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        int dataHeight = rawStream.getSize().getHeightPx();
        dataObjectInfo.setDataHeight(dataHeight);

        int dataWidth = rawStream.getSize().getWidthPx();
        dataObjectInfo.setDataWidth(dataWidth);

        ImageSize imageSize = rawStream.getSize();
        dataObjectInfo.setDataHeightRes((int) (imageSize.getDpiHorizontal() * 10));
        dataObjectInfo.setDataWidthRes((int) (imageSize.getDpiVertical() * 10));
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        AFPRenderingContext afpContext = (AFPRenderingContext)context;

        AFPDataObjectInfo dataObjectInfo = createDataObjectInfo();

        // set resource information
        setResourceInformation(dataObjectInfo,
                image.getInfo().getOriginalURI(),
                afpContext.getForeignAttributes());

        // Positioning
        dataObjectInfo.setObjectAreaInfo(createObjectAreaInfo(afpContext.getPaintingState(), pos));

        // set object area info
        //AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
        AFPPaintingState paintingState = afpContext.getPaintingState();
        int resolution = paintingState.getResolution();
        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        // Image content
        ImageRawStream imageStream = (ImageRawStream)image;
        updateDataObjectInfo(dataObjectInfo, imageStream, afpContext.getResourceManager());
        setAdditionalParameters(dataObjectInfo, imageStream);

        // Create image
        afpContext.getResourceManager().createObject(dataObjectInfo);
    }

}
