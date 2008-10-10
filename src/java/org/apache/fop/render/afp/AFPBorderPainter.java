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

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.render.afp.modca.DataStream;
import org.apache.fop.util.ColorUtil;

/**
 * Handles the drawing of borders/lines in AFP
 */
public class AFPBorderPainter {
    /** Static logging instance */
    protected static Log log = LogFactory.getLog("org.apache.fop.render.afp");

    private final DataStream dataStream;
    private final AFPState state;

    /**
     * Main constructor
     *
     * @param state the unit converter
     * @param dataStream the afp datastream
     */
    public AFPBorderPainter(AFPState state, DataStream dataStream) {
        this.state = state;
        this.dataStream = dataStream;
    }

    /** {@inheritDoc} */
    public void fillRect(float x, float y, float width, float height) {
        int pageWidth = dataStream.getCurrentPage().getWidth();
        int pageHeight = dataStream.getCurrentPage().getHeight();

        AFPUnitConverter unitConv = state.getUnitConverter();
        width = unitConv.pt2units(width);
        height = unitConv.pt2units(height);
        x = unitConv.pt2units(x);
        y = unitConv.pt2units(y);

        AffineTransform at = state.getData().getTransform();

        AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
        lineDataInfo.color = state.getColor();
        lineDataInfo.rotation = state.getRotation();
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
                = pageWidth - Math.round((float)at.getTranslateX() + y);
            lineDataInfo.x2 = Math.round(width + (float)at.getTranslateY() + x);
            break;
        case 180:
            lineDataInfo.x1 = pageWidth - Math.round((float)at.getTranslateX() - x);
            lineDataInfo.y1 = lineDataInfo.y2 = pageHeight - Math.round((float)at.getTranslateY() - x);
            lineDataInfo.x2 = pageWidth - Math.round((float)at.getTranslateX() - x - width);
            break;
        case 270:
            lineDataInfo.x1 = pageHeight - Math.round((float)at.getTranslateY() + y - x);
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round((float)at.getTranslateX() + y);
            lineDataInfo.x2 = lineDataInfo.x1 + Math.round(width - x);
            break;
        }
        dataStream.createLine(lineDataInfo);
    }

    /** {@inheritDoc} */
    public void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean isHorizontal, boolean startOrBefore, int style, Color color) {
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }

        int pageWidth = dataStream.getCurrentPage().getWidth();
        int pageHeight = dataStream.getCurrentPage().getHeight();
        AFPUnitConverter unitConv = state.getUnitConverter();
        AffineTransform at = state.getData().getTransform();

        x1 = unitConv.pt2units(x1);
        y1 = unitConv.pt2units(y1);
        x2 = unitConv.pt2units(x2);
        y2 = unitConv.pt2units(y2);

        switch (state.getRotation()) {
        case 0:
            x1 += at.getTranslateX();
            y1 += at.getTranslateY();
            x2 += at.getTranslateX();
            y2 += at.getTranslateY();
            break;
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
        }

        AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
        lineDataInfo.setThickness(Math.round(y2 - y1));
        lineDataInfo.setColor(color);
        lineDataInfo.setRotation(state.getRotation());

        lineDataInfo.x1 = Math.round(x1);
        lineDataInfo.y1 = Math.round(y1);

        // handle border-*-style
        switch (style) {
        case Constants.EN_DOUBLE:
            lineDataInfo.x2 = Math.round(x2);
            lineDataInfo.y2 = lineDataInfo.y1;
            dataStream.createLine(lineDataInfo);
            float w3 = lineDataInfo.thickness / 3;
            lineDataInfo.y1 += Math.round(w3 * 2);
            dataStream.createLine(lineDataInfo);
            break;
        case Constants.EN_DASHED:
        case Constants.EN_DOTTED:
            int factor = style == Constants.EN_DASHED ? 3 : 2;
            int thick = lineDataInfo.thickness * factor;
            lineDataInfo.x2 = lineDataInfo.x1 + thick;
            lineDataInfo.y2 = lineDataInfo.y1;
            int ex2 = Math.round(x2);
            while (lineDataInfo.x1 + thick < ex2) {
                dataStream.createLine(lineDataInfo);
                lineDataInfo.x1 += 2 * thick;
                lineDataInfo.x2 = lineDataInfo.x1 + thick;
            }
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            lineDataInfo.x2 = Math.round(x2);
            float colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
            float h3 = (y2 - y1) / 3;
            lineDataInfo.color = ColorUtil.lightenColor(color, -colFactor);
            lineDataInfo.thickness = Math.round(h3);
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round(y1);
            dataStream.createLine(lineDataInfo);
            lineDataInfo.color = color;
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round(y1 + h3);
            dataStream.createLine(lineDataInfo);
            lineDataInfo.color = ColorUtil.lightenColor(color, colFactor);
            lineDataInfo.y1 = lineDataInfo.y2 = Math.round(y1 + h3 + h3);
            dataStream.createLine(lineDataInfo);
            break;
        case Constants.EN_HIDDEN:
            break;
        case Constants.EN_INSET:
        case Constants.EN_OUTSET:
        case Constants.EN_SOLID:
        default:
            lineDataInfo.x2 = Math.round(x2);
            lineDataInfo.y2 = lineDataInfo.y1;
            dataStream.createLine(lineDataInfo);
        }
    }

}
