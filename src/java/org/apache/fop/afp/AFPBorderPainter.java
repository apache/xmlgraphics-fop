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

import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.util.ColorUtil;

/**
 * Handles the drawing of borders/lines in AFP
 */
public class AFPBorderPainter extends AbstractAFPPainter {

    /**
     * Main constructor
     *
     * @param paintingState the AFP painting state converter
     * @param dataStream the AFP datastream
     */
    public AFPBorderPainter(AFPPaintingState paintingState, DataStream dataStream) {
        super(paintingState, dataStream);
    }

    /** {@inheritDoc} */
    public void paint(PaintingInfo paintInfo) { // CSOK: MethodLength
        BorderPaintingInfo borderPaintInfo = (BorderPaintingInfo)paintInfo;
        float w = borderPaintInfo.getX2() - borderPaintInfo.getX1();
        float h = borderPaintInfo.getY2() - borderPaintInfo.getY1();
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }

        int pageWidth = dataStream.getCurrentPage().getWidth();
        int pageHeight = dataStream.getCurrentPage().getHeight();
        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        AffineTransform at = paintingState.getData().getTransform();

        float x1 = unitConv.pt2units(borderPaintInfo.getX1());
        float y1 = unitConv.pt2units(borderPaintInfo.getY1());
        float x2 = unitConv.pt2units(borderPaintInfo.getX2());
        float y2 = unitConv.pt2units(borderPaintInfo.getY2());

        switch (paintingState.getRotation()) {
        case 90:
            x1 += at.getTranslateY();
            y1 += (float) (pageWidth - at.getTranslateX());
            x2 += at.getTranslateY();
            y2 += (float) (pageWidth - at.getTranslateX());
            break;
        case 180:
            x1 += (float) (pageWidth - at.getTranslateX());
            y1 += (float) (pageHeight - at.getTranslateY());
            x2 += (float) (pageWidth - at.getTranslateX());
            y2 += (float) (pageHeight - at.getTranslateY());
            break;
        case 270:
            x1 = (float) (pageHeight - at.getTranslateY());
            y1 += (float) at.getTranslateX();
            x2 += x1;
            y2 += (float) at.getTranslateX();
            break;
        case 0:
        default:
            x1 += at.getTranslateX();
            y1 += at.getTranslateY();
            x2 += at.getTranslateX();
            y2 += at.getTranslateY();
            break;
        }

        AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
        lineDataInfo.setColor(borderPaintInfo.getColor());
        lineDataInfo.setRotation(paintingState.getRotation());
        lineDataInfo.setX1 ( Math.round(x1) );
        lineDataInfo.setY1 ( Math.round(y1) );
        float thickness;
        if (borderPaintInfo.isHorizontal()) {
            thickness = y2 - y1;
        } else {
            thickness = x2 - x1;
        }
        lineDataInfo.setThickness(Math.round(thickness));

        // handle border-*-style
        switch (borderPaintInfo.getStyle()) {
        case Constants.EN_DOUBLE:
            int thickness3 = (int)Math.floor(thickness / 3f);
            lineDataInfo.setThickness(thickness3);
            if (borderPaintInfo.isHorizontal()) {
                lineDataInfo.setX2 ( Math.round(x2) );
                lineDataInfo.setY2 ( lineDataInfo.getY1() );
                dataStream.createLine(lineDataInfo);
                int distance = thickness3 * 2;
                lineDataInfo = new AFPLineDataInfo(lineDataInfo);
                lineDataInfo.setY1 ( lineDataInfo.getY1() + distance );
                lineDataInfo.setY2 ( lineDataInfo.getY2() + distance );
                dataStream.createLine(lineDataInfo);
            } else {
                lineDataInfo.setX2 ( lineDataInfo.getX1() );
                lineDataInfo.setY2 ( Math.round(y2) );
                dataStream.createLine(lineDataInfo);
                int distance = thickness3 * 2;
                lineDataInfo = new AFPLineDataInfo(lineDataInfo);
                lineDataInfo.setX1 ( lineDataInfo.getX1() + distance );
                lineDataInfo.setX2 ( lineDataInfo.getX2() + distance );
                dataStream.createLine(lineDataInfo);
            }
            break;
        case Constants.EN_DASHED:
            if (borderPaintInfo.isHorizontal()) {
                int dashWidth = (int) (BorderPainter.dashWidthCalculator(x2 - x1, thickness));
                lineDataInfo.setX2 ( lineDataInfo.getX1() + dashWidth );
                lineDataInfo.setY2 ( lineDataInfo.getY1() );
                int ex2 = Math.round(x2);
                int spaceWidth = (int) (BorderPainter.DASHED_BORDER_SPACE_RATIO * dashWidth);
                while (lineDataInfo.getX2() < ex2) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.setX1 ( lineDataInfo.getX2() + spaceWidth );
                    lineDataInfo.setX2 ( lineDataInfo.getX1() + dashWidth );
                }
            } else {
                int dashWidth = (int) BorderPainter.dashWidthCalculator(y2 - y1, thickness);
                lineDataInfo.setX2 ( lineDataInfo.getX1() );
                lineDataInfo.setY2 ( lineDataInfo.getY1() + dashWidth );
                int ey2 = Math.round(y2);
                int spaceWidth = (int) (BorderPainter.DASHED_BORDER_SPACE_RATIO * dashWidth);
                while (lineDataInfo.getY2() < ey2) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.setY1 ( lineDataInfo.getY2() + spaceWidth );
                    lineDataInfo.setY2 ( lineDataInfo.getY1() + dashWidth );
                }
            }
            break;
        case Constants.EN_DOTTED:
            if (borderPaintInfo.isHorizontal()) {
                lineDataInfo.setX2 ( lineDataInfo.getX1() + lineDataInfo.getThickness() );
                lineDataInfo.setY2 ( lineDataInfo.getY1() );
                int ex2 = Math.round(x2);
                while (lineDataInfo.getX1() + lineDataInfo.getThickness() < ex2) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.setX1 ( lineDataInfo.getX1() + 3 * lineDataInfo.getThickness() );
                    lineDataInfo.setX2 ( lineDataInfo.getX1() + lineDataInfo.getThickness() );
                }
            } else {
                lineDataInfo.setX2 ( lineDataInfo.getX1() );
                lineDataInfo.setY2 ( lineDataInfo.getY1() + lineDataInfo.getThickness() );
                int ey2 = Math.round(y2);
                while (lineDataInfo.getY1() + lineDataInfo.getThickness() < ey2) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.setY1 ( lineDataInfo.getY1() + 3 * lineDataInfo.getThickness() );
                    lineDataInfo.setY2 ( lineDataInfo.getY1() + lineDataInfo.getThickness() );
                }
            }
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            //TODO
            int yNew;
            lineDataInfo.setX2 ( Math.round(x2) );
            float colFactor = (borderPaintInfo.getStyle() == Constants.EN_GROOVE ? 0.4f : -0.4f);
            float h3 = (y2 - y1) / 3;
            lineDataInfo.setColor
                ( ColorUtil.lightenColor(borderPaintInfo.getColor(), -colFactor) );
            lineDataInfo.setThickness ( Math.round(h3) );
            yNew = Math.round(y1);
            lineDataInfo.setY1 ( yNew );
            lineDataInfo.setY2 ( yNew );
            dataStream.createLine(lineDataInfo);
            lineDataInfo.setColor ( borderPaintInfo.getColor() );
            yNew = Math.round(y1 + h3);
            lineDataInfo.setY1 ( yNew );
            lineDataInfo.setY2 ( yNew );
            dataStream.createLine(lineDataInfo);
            lineDataInfo.setColor ( ColorUtil.lightenColor(borderPaintInfo.getColor(), colFactor) );
            yNew = Math.round(y1 + h3 + h3);
            lineDataInfo.setY1 ( yNew );
            lineDataInfo.setY2 ( yNew );
            dataStream.createLine(lineDataInfo);
            break;
        case Constants.EN_HIDDEN:
            break;
        case Constants.EN_INSET:
        case Constants.EN_OUTSET:
        case Constants.EN_SOLID:
        default:
            if (borderPaintInfo.isHorizontal()) {
                lineDataInfo.setX2 ( Math.round(x2) );
                lineDataInfo.setY2 ( lineDataInfo.getY1() );
            } else {
                lineDataInfo.setX2 ( lineDataInfo.getX1() );
                lineDataInfo.setY2 ( Math.round(y2) );
            }
            dataStream.createLine(lineDataInfo);
        }
    }

}
