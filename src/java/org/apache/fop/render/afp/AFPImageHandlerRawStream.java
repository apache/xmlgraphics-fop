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

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRawEPS;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

/**
 * AFPImageHandler implementation which handles raw stream images.
 */
public class AFPImageHandlerRawStream extends AFPImageHandler {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_JPEG,
        ImageFlavor.RAW_CCITTFAX,
        ImageFlavor.RAW_EPS,
    };

    private static final Class[] CLASSES = new Class[] {
        ImageRawJPEG.class,
        ImageRawCCITTFax.class,
        ImageRawEPS.class
    };

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

        AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();

        AFPRendererContext rendererContext
        = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        int resolution = paintingState.getResolution();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        InputStream inputStream = rawStream.createInputStream();
        dataObjectInfo.setInputStream(inputStream);

        int dataHeight = rawStream.getSize().getHeightPx();
        dataObjectInfo.setDataHeight(dataHeight);

        int dataWidth = rawStream.getSize().getWidthPx();
        dataObjectInfo.setDataWidth(dataWidth);
        return dataObjectInfo;
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 100;
    }

    /** {@inheritDoc} */
    public Class[] getSupportedImageClasses() {
        return CLASSES;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPDataObjectInfo();
    }
}
