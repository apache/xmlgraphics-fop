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

package org.apache.fop.render.afp;

import java.awt.image.RenderedImage;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.apps.MimeConstants;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

/**
 * A buffered image configurator
 */
public class AFPImageRenderedFactory extends AFPAbstractImageFactory {

    /**
     * Main constructor
     *
     * @param state the AFP state
     */
    public AFPImageRenderedFactory(AFPState state) {
        super(state);
    }

    /** {@inheritDoc} */
    public AFPDataObjectInfo create(AFPImageInfo afpImageInfo) throws IOException {
        AFPImageObjectInfo imageObjectInfo
            = (AFPImageObjectInfo)super.create(afpImageInfo);

        ImageRendered imageRendered = (ImageRendered) afpImageInfo.img;
        RenderedImage renderedImage = imageRendered.getRenderedImage();

        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        ImageEncodingHelper.encodeRenderedImageAsRGB(renderedImage, baout);

        imageObjectInfo.setData(baout.toByteArray());

        int resolution = state.getResolution();

        AFPObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        imageObjectInfo.setDataHeight(renderedImage.getHeight());
        imageObjectInfo.setDataWidth(renderedImage.getWidth());

        boolean colorImages = state.isColorImages();
        imageObjectInfo.setColor(colorImages);
        imageObjectInfo.setMimeType(colorImages
                ? MimeConstants.MIME_AFP_IOCA_FS45
                        : MimeConstants.MIME_AFP_IOCA_FS10);

        return imageObjectInfo;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPImageObjectInfo();
    }
}
