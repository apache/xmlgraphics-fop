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

    private static final int X1 = 0;
    private static final int Y1 = 1;
    private static final int X2 = 2;
    private static final int Y2 = 3;

    private final AFPUnitConverter unitConv;
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
        this.unitConv = state.getUnitConverter();
        this.dataStream = dataStream;
    }

    /** {@inheritDoc} */
    public void fillRect(float x, float y, float width, float height) {
        AffineTransform at = state.getData().getTransform();
        float transX = (float)at.getTranslateX();
        float transY = (float)at.getTranslateY();
        int x1 = Math.round(transX + unitConv.pt2units(x));
        int y1 = Math.round(transY + unitConv.pt2units(y));
        int x2 = Math.round(transX + unitConv.pt2units(x) + unitConv.pt2units(width));
        LineDataInfo lineDataInfo = new LineDataInfo();
        lineDataInfo.x1 = x1;
        lineDataInfo.y1 = y1;
        lineDataInfo.x2 = x2;
        lineDataInfo.y2 = y1;
        lineDataInfo.thickness = Math.round(unitConv.pt2units(height));
        lineDataInfo.color = state.getColor();
        dataStream.createLine(lineDataInfo);
    }

    /** {@inheritDoc} */
    public void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {
        float[] srcPts = new float[] {x1 * 1000, y1 * 1000, x2 * 1000, y2 * 1000};
        float[] dstPts = new float[srcPts.length];
        int[] coords = unitConv.mpts2units(srcPts, dstPts);

        float width = dstPts[X2] - dstPts[X1];
        float height = dstPts[Y2] - dstPts[Y1];
        if ((width < 0) || (height < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }

        LineDataInfo lineDataInfo = new LineDataInfo();
        lineDataInfo.color = col;

        switch (style) {
        case Constants.EN_DOUBLE:
            lineDataInfo.x1 = coords[X1];
            lineDataInfo.y1 = coords[Y1];
            if (horz) {
                float h3 = height / 3;
                lineDataInfo.thickness = Math.round(h3);
                lineDataInfo.x2 = coords[X2];
                lineDataInfo.y2 = coords[Y1];
                dataStream.createLine(lineDataInfo);
                int ym2 = Math.round(dstPts[Y1] + h3 + h3);
                lineDataInfo.y1 = ym2;
                lineDataInfo.y2 = ym2;
                dataStream.createLine(lineDataInfo);
            } else {
                float w3 = width / 3;
                lineDataInfo.thickness = Math.round(w3);
                lineDataInfo.x2 = coords[X1];
                lineDataInfo.y2 = coords[Y2];
                dataStream.createLine(lineDataInfo);
                int xm2 = Math.round(dstPts[X1] + w3 + w3);
                lineDataInfo.x1 = xm2;
                lineDataInfo.x2 = xm2;
                dataStream.createLine(lineDataInfo);
            }
            break;

        case Constants.EN_DASHED:
            lineDataInfo.x1 = coords[X1];
            if (horz) {
                float w2 = 2 * height;
                lineDataInfo.y1 = coords[Y1];
                lineDataInfo.x2 = coords[X1] + Math.round(w2);
                lineDataInfo.y2 = coords[Y1];
                lineDataInfo.thickness = Math.round(height);
                while (lineDataInfo.x1 + w2 < coords[X2]) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.x1 += 2 * w2;
                }
            } else {
                float h2 = 2 * width;
                lineDataInfo.y1 = coords[Y2];
                lineDataInfo.x2 = coords[X1];
                lineDataInfo.y2 = coords[Y1] + Math.round(h2);
                lineDataInfo.thickness = Math.round(width);
                while (lineDataInfo.y2 < coords[Y2]) {
                    dataStream.createLine(lineDataInfo);
                    lineDataInfo.y2 += 2 * h2;
                }
            }
            break;

        case Constants.EN_DOTTED:
            lineDataInfo.x1 = coords[X1];
            lineDataInfo.y1 = coords[Y1];
            if (horz) {
                lineDataInfo.thickness = Math.round(height);
                lineDataInfo.x2 = coords[X1] + lineDataInfo.thickness;
                lineDataInfo.y2 = coords[Y1];
                while (lineDataInfo.x2 < coords[X2]) {
                    dataStream.createLine(lineDataInfo);
                    coords[X1] += 2 * height;
                    lineDataInfo.x1 = coords[X1];
                    lineDataInfo.x2 = coords[X1] + lineDataInfo.thickness;
                }
            } else {
                lineDataInfo.thickness = Math.round(width);
                lineDataInfo.x2 = coords[X1];
                lineDataInfo.y2 = coords[Y1] + lineDataInfo.thickness;
                while (lineDataInfo.y2 < coords[Y2]) {
                    dataStream.createLine(lineDataInfo);
                    coords[Y1] += 2 * width;
                    lineDataInfo.y1 = coords[Y1];
                    lineDataInfo.y2 = coords[Y1] + lineDataInfo.thickness;
                }
            }
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            float colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
            if (horz) {
                lineDataInfo.x1 = coords[X1];
                lineDataInfo.x2 = coords[X2];
                float h3 = height / 3;
                lineDataInfo.color = ColorUtil.lightenColor(col, -colFactor);
                lineDataInfo.thickness = Math.round(h3);
                lineDataInfo.y1 = lineDataInfo.y2 = coords[Y1];
                dataStream.createLine(lineDataInfo);
                lineDataInfo.color = col;
                lineDataInfo.y1 = lineDataInfo.y2 = Math.round(dstPts[Y1] + h3);
                dataStream.createLine(lineDataInfo);
                lineDataInfo.color = ColorUtil.lightenColor(col, colFactor);
                lineDataInfo.y1 = lineDataInfo.y2 = Math.round(dstPts[Y1] + h3 + h3);
                dataStream.createLine(lineDataInfo);
            } else {
                lineDataInfo.y1 = coords[Y1];
                lineDataInfo.y2 = coords[Y2];
                float w3 = width / 3;
                float xm1 = dstPts[X1] + (w3 / 2);
                lineDataInfo.color = ColorUtil.lightenColor(col, -colFactor);
                lineDataInfo.x1 = lineDataInfo.x2 = Math.round(xm1);
                dataStream.createLine(lineDataInfo);
                lineDataInfo.color = col;
                lineDataInfo.x1 = lineDataInfo.x2 = Math.round(xm1 + w3);
                dataStream.createLine(lineDataInfo);
                lineDataInfo.color = ColorUtil.lightenColor(col, colFactor);
                lineDataInfo.x1 = lineDataInfo.x2 = Math.round(xm1 + w3 + w3);
                dataStream.createLine(lineDataInfo);
            }
            break;

        case Constants.EN_HIDDEN:
            break;

        case Constants.EN_INSET:
        case Constants.EN_OUTSET:
        default:
              lineDataInfo.x1 = coords[X1];
              lineDataInfo.y1 = coords[Y1];
              if (horz) {
                  lineDataInfo.thickness = Math.round(height);
                  lineDataInfo.x2 = coords[X2];
                  lineDataInfo.y2 = coords[Y1];
              } else {
                  lineDataInfo.thickness = Math.round(width);
                  lineDataInfo.x2 = coords[X1];
                  lineDataInfo.y2 = coords[Y2];
              }
              lineDataInfo.x2 = (horz ? coords[X2] : coords[X1]);
              lineDataInfo.y2 = (horz ? coords[Y1] : coords[Y2]);
              dataStream.createLine(lineDataInfo);
        }
    }

}
