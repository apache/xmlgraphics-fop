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

import java.awt.Dimension;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.util.MimeConstants;
import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.util.bitmap.BitmapImageUtil;

/**
 * PDFImageHandler implementation which handles RenderedImage instances.
 */
public class AFPImageHandlerRenderedImage extends AFPImageHandler {

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

        AFPResourceInfo resourceInfo = imageObjectInfo.getResourceInfo();
        if (!resourceInfo.levelChanged()) {
            resourceInfo.setLevel(afpInfo.getResourceManager().getResourceLevelDefaults()
                    .getDefaultResourceLevel(ResourceObject.TYPE_IMAGE));
        }

        AFPPaintingState paintingState = afpInfo.getPaintingState();
        int resolution = paintingState.getResolution();
        int maxPixelSize = paintingState.getBitsPerPixel();
        if (paintingState.isColorImages()) {
            maxPixelSize *= 3; //RGB only at the moment
        }
        ImageRendered imageRendered = (ImageRendered) rendererImageInfo.img;
        RenderedImage renderedImage = imageRendered.getRenderedImage();

        ImageInfo imageInfo = rendererImageInfo.getImageInfo();
        ImageSize intrinsicSize = imageInfo.getSize();

        boolean useFS10 = (maxPixelSize == 1) || BitmapImageUtil.isMonochromeImage(renderedImage);
        boolean usePageSegments = useFS10
                    && !resourceInfo.getLevel().isInline();

        ImageSize effIntrinsicSize = intrinsicSize;
        if (usePageSegments) {
            //Resize, optionally resample and convert image
            Dimension resampledDim = new Dimension(
                    (int)Math.ceil(UnitConv.mpt2px(afpInfo.getWidth(), resolution)),
                    (int)Math.ceil(UnitConv.mpt2px(afpInfo.getHeight(), resolution)));

            imageObjectInfo.setCreatePageSegment(true);
            imageObjectInfo.getResourceInfo().setImageDimension(resampledDim);

            //Only resample/downsample if image is smaller than its intrinsic size
            //to make print file smaller
            boolean resample = resampledDim.width < renderedImage.getWidth()
                && resampledDim.height < renderedImage.getHeight();
            if (resample) {
                if (log.isDebugEnabled()) {
                    log.debug("Resample from " + intrinsicSize.getDimensionPx()
                            + " to " + resampledDim);
                }
                renderedImage = BitmapImageUtil.convertToMonochrome(renderedImage, resampledDim);
                effIntrinsicSize = new ImageSize(
                        resampledDim.width, resampledDim.height, resolution);
            }
        }
        if (useFS10) {
            imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS10);
        } else {
            imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS11);
        }

        imageObjectInfo.setDataHeightRes((int)Math.round(
                effIntrinsicSize.getDpiHorizontal() * 10));
        imageObjectInfo.setDataWidthRes((int)Math.round(
                effIntrinsicSize.getDpiVertical() * 10));

        int dataHeight = renderedImage.getHeight();
        imageObjectInfo.setDataHeight(dataHeight);

        int dataWidth = renderedImage.getWidth();
        imageObjectInfo.setDataWidth(dataWidth);

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
            if (useFS10
                    && BitmapImageUtil.isMonochromeImage(renderedImage)
                    && BitmapImageUtil.isZeroBlack(renderedImage)) {
                directEncode = false;
            }
            if (directEncode) {
                log.debug("Encoding image directly...");
                imageObjectInfo.setBitsPerPixel(encodedColorModel.getPixelSize());
                if (BitmapImageUtil.isMonochromeImage(renderedImage)
                        && BitmapImageUtil.isZeroBlack(renderedImage)) {
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
                if (bitsPerPixel == 1) {
                    imageObjectInfo.setSubtractive(true);
                }
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

}
