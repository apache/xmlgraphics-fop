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

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;

/**
 * AFPImageHandler implementation which handles CCITT encoded images (CCITT fax group 3/4).
 */
public class AFPImageHandlerRawCCITTFax extends AbstractAFPImageHandlerRawStream {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_CCITTFAX,
    };

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPImageObjectInfo imageObjectInfo
            = (AFPImageObjectInfo)super.generateDataObjectInfo(rendererImageInfo);

        ImageRawCCITTFax ccitt = (ImageRawCCITTFax) rendererImageInfo.getImage();
        int compression = ccitt.getCompression();
        imageObjectInfo.setCompression(compression);

        imageObjectInfo.setBitsPerPixel(1);
        return imageObjectInfo;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPImageObjectInfo();
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 400;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRawCCITTFax.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

}
