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
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

/**
 * A base abstract AFP raw stream image handler
 */
public abstract class AbstractAFPImageHandlerRawStream extends AFPImageHandler {

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPDataObjectInfo dataObjectInfo = super.generateDataObjectInfo(rendererImageInfo);

        ImageInfo imageInfo = rendererImageInfo.getImageInfo();
        String mimeType = imageInfo.getMimeType();
        if (mimeType != null) {
            dataObjectInfo.setMimeType(mimeType);
        }
        ImageRawStream rawStream = (ImageRawStream) rendererImageInfo.getImage();
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

        // set object area info
        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
        AFPRendererContext rendererContext
            = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        int resolution = paintingState.getResolution();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        return dataObjectInfo;
    }

}
