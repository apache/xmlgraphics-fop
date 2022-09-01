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
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.util.bitmap.BitmapImageUtil;

/**
 * A painter of rectangles in AFP
 */
public class AFPRectanglePainter extends AbstractAFPPainter {

    private AFPResourceManager resourceManager;

    /**
     * Main constructor
     *
     * @param paintingState the AFP painting state
     * @param dataStream the AFP datastream
     */
    public AFPRectanglePainter(AFPPaintingState paintingState, DataStream dataStream,
                               AFPResourceManager resourceManager) {
        super(paintingState, dataStream);
        this.resourceManager = resourceManager;
    }

    /** {@inheritDoc} */
    public void paint(PaintingInfo paintInfo) throws IOException {
        Color color = paintingState.getColor();
        if (color instanceof ColorWithAlternatives && color.getAlpha() != 255) {
            paintAlpha(paintInfo);
            return;
        }

        RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
        int pageWidth = dataStream.getCurrentPage().getWidth();
        int pageHeight = dataStream.getCurrentPage().getHeight();
        int yNew;

        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        float width = unitConv.pt2units(rectanglePaintInfo.getWidth());
        float height = unitConv.pt2units(rectanglePaintInfo.getHeight());
        float x = unitConv.pt2units(rectanglePaintInfo.getX());
        float y = unitConv.pt2units(rectanglePaintInfo.getY());

        AffineTransform at = paintingState.getData().getTransform();

        AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
        lineDataInfo.setColor(paintingState.getColor());
        lineDataInfo.setRotation(paintingState.getRotation());
        lineDataInfo.setThickness(Math.round(height));

        switch (lineDataInfo.getRotation()) {
        case 90:
            lineDataInfo.setX1(Math.round((float)at.getTranslateY() + x));
            yNew = pageWidth - Math.round((float)at.getTranslateX()) + Math.round(y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(Math.round(width + (float)at.getTranslateY() + x));
            break;
        case 180:
            lineDataInfo.setX1(pageWidth - Math.round((float)at.getTranslateX() - x));
            yNew = pageHeight - Math.round((float)at.getTranslateY() - y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(pageWidth - Math.round((float)at.getTranslateX() - x - width));
            break;
        case 270:
            lineDataInfo.setX1(pageHeight - Math.round((float)at.getTranslateY() - x));
            yNew = Math.round((float)at.getTranslateX() + y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(pageHeight - Math.round((float)at.getTranslateY() - x - width));
            break;
        case 0:
        default:
            lineDataInfo.setX1(Math.round((float)at.getTranslateX() + x));
            yNew = Math.round((float)at.getTranslateY() + y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(Math.round((float)at.getTranslateX() + x + width));
            break;
        }
        dataStream.createLine(lineDataInfo);
    }

    private void paintAlpha(PaintingInfo paintInfo) throws IOException {
        RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
        if (rectanglePaintInfo.getWidth() <= 0 || rectanglePaintInfo.getHeight() <= 0) {
            return;
        }
        Dimension size = new Dimension((int)rectanglePaintInfo.getWidth(), (int)rectanglePaintInfo.getHeight());

        AFPImageObjectInfo imageObjectInfo = new AFPImageObjectInfo();
        imageObjectInfo.setMimeType(MimeConstants.MIME_AFP_IOCA_FS45);
        imageObjectInfo.getResourceInfo().setLevel(new AFPResourceLevel(AFPResourceLevel.ResourceType.INLINE));
        imageObjectInfo.getResourceInfo().setImageDimension(size);
        imageObjectInfo.setColor(true);

        int width = size.width;
        int height = size.height;
        int resolution = paintingState.getResolution();
        ImageSize bitmapSize = new ImageSize(width, height, resolution);
        imageObjectInfo.setDataHeightRes((int)Math.round(bitmapSize.getDpiHorizontal() * 10));
        imageObjectInfo.setDataWidthRes((int)Math.round(bitmapSize.getDpiVertical() * 10));
        imageObjectInfo.setDataWidth(width);
        imageObjectInfo.setDataHeight(height);

        Color color = paintingState.getColor();
        byte[] image = buildImage(color, width, height);
        imageObjectInfo.setData(image);
        if (color instanceof ColorWithAlternatives) {
            imageObjectInfo.setBitsPerPixel(32);
        } else {
            imageObjectInfo.setBitsPerPixel(24);
        }

        image = buildMaskImage(color, width, height);
        imageObjectInfo.setTransparencyMask(image);

        int rotation = paintingState.getRotation();
        AffineTransform at = paintingState.getData().getTransform();
        Point2D origin = at.transform(new Point2D.Float(
                rectanglePaintInfo.getX() * 1000,
                rectanglePaintInfo.getY() * 1000), null);
        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        float widthf = unitConv.pt2units(rectanglePaintInfo.getWidth());
        float heightf = unitConv.pt2units(rectanglePaintInfo.getHeight());
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo(
                (int) Math.round(origin.getX()),
                (int) Math.round(origin.getY()),
                Math.round(widthf), Math.round(heightf), resolution, rotation);
        imageObjectInfo.setObjectAreaInfo(objectAreaInfo);

        resourceManager.createObject(imageObjectInfo);
    }

    private byte[] buildImage(Color color, int width, int height) {
        float[] components;
        if (color instanceof ColorWithAlternatives) {
            components = ((ColorWithAlternatives)color).getAlternativeColors()[0].getColorComponents(null);
        } else {
            components = color.getColorComponents(null);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (float component : components) {
                    component *= 255;
                    bos.write((byte)component);
                }
            }
        }
        return bos.toByteArray();
    }

    private byte[] buildMaskImage(Color color, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        Color alpha = new Color(color.getAlpha(), color.getAlpha(), color.getAlpha());
        graphics.setColor(alpha);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        RenderedImage renderedImage =
                BitmapImageUtil.convertToMonochrome(bufferedImage, new Dimension(width, height), 1);
        DataBufferByte bufferByte = (DataBufferByte) renderedImage.getData().getDataBuffer();
        return bufferByte.getData();
    }
}
