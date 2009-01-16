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
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.util.BitmapImageUtil;

/**
 * PDFImageHandler implementation which handles RenderedImage instances.
 */
public class AFPImageHandlerRenderedImage extends AFPImageHandler implements ImageHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPImageHandlerRenderedImage.class);

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.BUFFERED_IMAGE,
        ImageFlavor.RENDERED_IMAGE
    };

    /** {@inheritDoc} */
    public AFPDataObjectInfo generateDataObjectInfo(
            AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPImageObjectInfo imageObjectInfo
            = (AFPImageObjectInfo)super.generateDataObjectInfo(rendererImageInfo);

        AFPRendererContext rendererContext
            = (AFPRendererContext)rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = rendererContext.getInfo();
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        ImageRendered imageRendered = (ImageRendered) rendererImageInfo.img;

        updateDataObjectInfo(imageObjectInfo, paintingState, imageRendered);
        return imageObjectInfo;
    }

    private AFPDataObjectInfo updateDataObjectInfo(AFPImageObjectInfo imageObjectInfo,
            AFPPaintingState paintingState, ImageRendered imageRendered)
            throws IOException {

        int resolution = paintingState.getResolution();

        imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS45);
        imageObjectInfo.setDataHeightRes(resolution);
        imageObjectInfo.setDataWidthRes(resolution);

        RenderedImage renderedImage = imageRendered.getRenderedImage();

        int dataHeight = renderedImage.getHeight();
        imageObjectInfo.setDataHeight(dataHeight);

        int dataWidth = renderedImage.getWidth();
        imageObjectInfo.setDataWidth(dataWidth);

        //TODO To reduce AFP file size, investigate using a compression scheme.
        //Currently, all image data is uncompressed.

        int maxPixelSize = paintingState.getBitsPerPixel();
        if (paintingState.isColorImages()) {
            maxPixelSize *= 3; //RGB only at the moment
        }

        ColorModel cm = renderedImage.getColorModel();
        if (log.isTraceEnabled()) {
            log.trace("ColorModel: " + cm);
        }
        int pixelSize = cm.getPixelSize();
        if (cm.hasAlpha()) {
            pixelSize -= 8;
        }
        //TODO Add support for CMYK images

        byte[] imageData = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean allowDirectEncoding = true;
        if (allowDirectEncoding && pixelSize <= maxPixelSize) {
            //Attempt to encode without resampling the image
            ImageEncodingHelper helper = new ImageEncodingHelper(renderedImage);
            ColorModel encodedColorModel = helper.getEncodedColorModel();
            boolean directEncode = true;
            if (helper.getEncodedColorModel().getPixelSize() > maxPixelSize) {
                directEncode = false; //pixel size needs to be reduced
            }
            if (BitmapImageUtil.getColorIndexSize(renderedImage) > 2) {
                directEncode = false; //Lookup tables are not implemented, yet
            }
            if (directEncode) {
                log.debug("Encoding image directly...");
                imageObjectInfo.setBitsPerPixel(encodedColorModel.getPixelSize());
                if (BitmapImageUtil.isMonochromeImage(renderedImage)
                        && !BitmapImageUtil.isZeroBlack(renderedImage)) {
                    log.trace("set subtractive mode");
                    imageObjectInfo.setSubtractive(true);
                }

                helper.encode(baos);
                imageData = baos.toByteArray();
            }
        }
        if (imageData == null) {
            log.debug("Encoding image via RGB...");
            //Convert image to 24bit RGB
            ImageEncodingHelper.encodeRenderedImageAsRGB(renderedImage, baos);
            imageData = baos.toByteArray();

            boolean colorImages = paintingState.isColorImages();
            imageObjectInfo.setColor(colorImages);

            // convert to grayscale
            if (!colorImages) {
                log.debug("Converting RGB image to grayscale...");
                baos.reset();
                int bitsPerPixel = paintingState.getBitsPerPixel();
                imageObjectInfo.setBitsPerPixel(bitsPerPixel);
                //TODO this should be done off the RenderedImage to avoid buffering the
                //intermediate 24bit image
                ImageEncodingHelper.encodeRGBAsGrayScale(
                      imageData, dataWidth, dataHeight, bitsPerPixel, baos);
                imageData = baos.toByteArray();
            }
        }

        imageObjectInfo.setData(imageData);

        // set object area info
        AFPObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        return imageObjectInfo;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPImageObjectInfo();
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 300;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRendered.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        AFPRenderingContext afpContext = (AFPRenderingContext)context;

        AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)createDataObjectInfo();

        // set resource information
        setResourceInformation(imageObjectInfo,
                image.getInfo().getOriginalURI(),
                afpContext.getForeignAttributes());

        // Positioning
        imageObjectInfo.setObjectAreaInfo(createObjectAreaInfo(afpContext.getPaintingState(), pos));

        // Image content
        ImageRendered imageRend = (ImageRendered)image;
        updateDataObjectInfo(imageObjectInfo, afpContext.getPaintingState(), imageRend);

        // Create image
        afpContext.getResourceManager().createObject(imageObjectInfo);
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        return (image == null || image instanceof ImageRendered)
            && targetContext instanceof AFPRenderingContext;
    }

}
