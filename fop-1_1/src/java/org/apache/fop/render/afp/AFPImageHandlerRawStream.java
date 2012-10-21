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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawEPS;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.render.RenderingContext;

/**
 * AFPImageHandler implementation which handles raw stream images.
 */
public class AFPImageHandlerRawStream extends AbstractAFPImageHandlerRawStream {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_JPEG,
        ImageFlavor.RAW_TIFF,
        ImageFlavor.RAW_EPS,
    };

    /** logging instance */
    private final Log log = LogFactory.getLog(AFPImageHandlerRawJPEG.class);

    /** {@inheritDoc} */
    public int getPriority() {
        return 200;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRawStream.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    @Override
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPDataObjectInfo();
    }

    /** {@inheritDoc} */
    @Override
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Embedding undecoded image data (" + image.getInfo().getMimeType()
                    + ") as data container...");
        }
        super.handleImage(context, image, pos);
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        if (targetContext instanceof AFPRenderingContext) {
            AFPRenderingContext afpContext = (AFPRenderingContext)targetContext;
            return (afpContext.getPaintingState().isNativeImagesSupported())
                && (image == null
                        || image instanceof ImageRawJPEG
                        || image instanceof ImageRawEPS
                        || ((image instanceof ImageRawStream)
                                && (MimeConstants.MIME_TIFF.equals(
                                        ((ImageRawStream)image).getMimeType()))));
        }
        return false;
    }
}
