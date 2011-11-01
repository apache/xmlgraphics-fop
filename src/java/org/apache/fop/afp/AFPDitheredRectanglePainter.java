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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.util.bitmap.DitherUtil;


/**
 * A painter of rectangles in AFP
 */
public class AFPDitheredRectanglePainter extends AbstractAFPPainter {

    private AFPResourceManager resourceManager;

    /**
     * Main constructor
     *
     * @param paintingState the AFP painting state
     * @param dataStream the AFP datastream
     * @param resourceManager the resource manager
     */
    public AFPDitheredRectanglePainter(AFPPaintingState paintingState, DataStream dataStream,
            AFPResourceManager resourceManager) {
        super(paintingState, dataStream);
        this.resourceManager = resourceManager;
    }

    /** {@inheritDoc} */
    public void paint(PaintingInfo paintInfo) throws IOException {
        RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
        if (rectanglePaintInfo.getWidth() <= 0 || rectanglePaintInfo.getHeight() <= 0) {
            return;
        }

        int ditherMatrix = DitherUtil.DITHER_MATRIX_8X8;
        Dimension ditherSize = new Dimension(ditherMatrix, ditherMatrix);

        //Prepare an FS10 bi-level image
        AFPImageObjectInfo imageObjectInfo = new AFPImageObjectInfo();
        imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS10);
        //imageObjectInfo.setCreatePageSegment(true);
        imageObjectInfo.getResourceInfo().setLevel(new AFPResourceLevel(AFPResourceLevel.INLINE));
        imageObjectInfo.getResourceInfo().setImageDimension(ditherSize);
        imageObjectInfo.setBitsPerPixel(1);
        imageObjectInfo.setColor(false);
        //Note: the following may not be supported by older implementations
        imageObjectInfo.setMappingOption(MappingOptionTriplet.REPLICATE_AND_TRIM);

        //Dither image size
        int resolution = paintingState.getResolution();
        ImageSize ditherBitmapSize = new ImageSize(
                ditherSize.width, ditherSize.height, resolution);
        imageObjectInfo.setDataHeightRes((int)Math.round(
                ditherBitmapSize.getDpiHorizontal() * 10));
        imageObjectInfo.setDataWidthRes((int)Math.round(
                ditherBitmapSize.getDpiVertical() * 10));
        imageObjectInfo.setDataWidth(ditherSize.width);
        imageObjectInfo.setDataHeight(ditherSize.height);

        //Create dither image
        Color col = paintingState.getColor();
        byte[] dither = DitherUtil.getBayerDither(ditherMatrix, col, false);
        imageObjectInfo.setData(dither);

        //Positioning
        int rotation = paintingState.getRotation();
        AffineTransform at = paintingState.getData().getTransform();
        Point2D origin = at.transform(new Point2D.Float(
                rectanglePaintInfo.getX() * 1000,
                rectanglePaintInfo.getY() * 1000), null);
        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        float width = unitConv.pt2units(rectanglePaintInfo.getWidth());
        float height = unitConv.pt2units(rectanglePaintInfo.getHeight());
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo(
                (int) Math.round(origin.getX()),
                (int) Math.round(origin.getY()),
                Math.round(width), Math.round(height), resolution, rotation);
        imageObjectInfo.setObjectAreaInfo(objectAreaInfo);

        //Create rectangle
        resourceManager.createObject(imageObjectInfo);
    }

}
