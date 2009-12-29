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

import java.awt.geom.AffineTransform;


/**
 * A painter of rectangles in AFP
 */
public class AFPRectanglePainter extends AbstractAFPPainter {

    /**
     * Main constructor
     *
     * @param paintingState the AFP painting state
     * @param dataStream the AFP datastream
     */
    public AFPRectanglePainter(AFPPaintingState paintingState, DataStream dataStream) {
        super(paintingState, dataStream);
    }

    /** {@inheritDoc} */
    public void paint(PaintingInfo paintInfo) {
        RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
        int pageWidth = dataStream.getCurrentPage().getWidth();
        int pageHeight = dataStream.getCurrentPage().getHeight();

        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        float width = unitConv.pt2units(rectanglePaintInfo.getWidth());
        float height = unitConv.pt2units(rectanglePaintInfo.getHeight());
        float x = unitConv.pt2units(rectanglePaintInfo.getX());
        float y = unitConv.pt2units(rectanglePaintInfo.getY());

        AffineTransform at = paintingState.getData().getTransform();

        AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
        lineDataInfo.color = paintingState.getColor();
        lineDataInfo.rotation = paintingState.getRotation();
        lineDataInfo.thickness = Math.round(height);

        switch (lineDataInfo.rotation) {
        case 0:
            lineDataInfo.x1 = Math.round((float)at.getTranslateX() + x);
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round((float)at.getTranslateY() + y);
            lineDataInfo.x2 = Math.round((float)at.getTranslateX() + x + width);
            break;
        case 90:
            lineDataInfo.x1 = Math.round((float)at.getTranslateY() + x);
            lineDataInfo.y1 = lineDataInfo.y2
                = pageWidth - Math.round((float)at.getTranslateX()) + Math.round(y);
            lineDataInfo.x2 = Math.round(width + (float)at.getTranslateY() + x);
            break;
        case 180:
            lineDataInfo.x1 = pageWidth - Math.round((float)at.getTranslateX() - x);
            lineDataInfo.y1 = lineDataInfo.y2 = pageHeight - Math.round((float)at.getTranslateY() - y);
            lineDataInfo.x2 = pageWidth - Math.round((float)at.getTranslateX() - x - width);
            break;
        case 270:
            lineDataInfo.x1 = pageHeight - Math.round((float)at.getTranslateY() - x);
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round((float)at.getTranslateX() + y);
            lineDataInfo.x2 = pageHeight - Math.round((float)at.getTranslateY() - x - width);
            break;
        }
        dataStream.createLine(lineDataInfo);
    }
}
