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

package org.apache.fop.render.java2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;

/**
 * Java2D-specific implementation of the {@code BorderPainter}.
 */
public class Java2DBorderPainter extends BorderPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(Java2DBorderPainter.class);

    private Java2DPainter painter;

    private GeneralPath currentPath = null;

    public Java2DBorderPainter(Java2DPainter painter) {
        this.painter = painter;
    }

    private Java2DGraphicsState getG2DState() {
        return this.painter.g2dState;
    }

    private Graphics2D getG2D() {
        return getG2DState().getGraph();
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz,
            boolean startOrBefore, int style, Color color) {
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED:
                getG2D().setColor(color);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    float ym = y1 + (h / 2);
                    BasicStroke s = new BasicStroke(h, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {unit}, 0);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    float xm = x1 + (w / 2);
                    BasicStroke s = new BasicStroke(w, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {unit}, 0);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(xm, y1, xm, y2));
                }
                break;
            case Constants.EN_DOTTED:
                getG2D().setColor(color);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    float ym = y1 + (h / 2);
                    BasicStroke s = new BasicStroke(h, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {0, unit}, 0);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    float xm = x1 + (w / 2);
                    BasicStroke s = new BasicStroke(w, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {0, unit}, 0);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(xm, y1, xm, y2));
                }
                break;
            case Constants.EN_DOUBLE:
                getG2D().setColor(color);
                if (horz) {
                    float h3 = h / 3;
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    BasicStroke s = new BasicStroke(h3);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(x1, ym1, x2, ym1));
                    getG2D().draw(new Line2D.Float(x1, ym2, x2, ym2));
                } else {
                    float w3 = w / 3;
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    BasicStroke s = new BasicStroke(w3);
                    getG2D().setStroke(s);
                    getG2D().draw(new Line2D.Float(xm1, y1, xm1, y2));
                    getG2D().draw(new Line2D.Float(xm2, y1, xm2, y2));
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
                float colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
                if (horz) {
                    Color uppercol = ColorUtil.lightenColor(color, -colFactor);
                    Color lowercol = ColorUtil.lightenColor(color, colFactor);
                    float h3 = h / 3;
                    float ym1 = y1 + (h3 / 2);
                    getG2D().setStroke(new BasicStroke(h3));
                    getG2D().setColor(uppercol);
                    getG2D().draw(new Line2D.Float(x1, ym1, x2, ym1));
                    getG2D().setColor(color);
                    getG2D().draw(new Line2D.Float(x1, ym1 + h3, x2, ym1 + h3));
                    getG2D().setColor(lowercol);
                    getG2D().draw(new Line2D.Float(x1, ym1 + h3 + h3, x2, ym1 + h3 + h3));
                } else {
                    Color leftcol = ColorUtil.lightenColor(color, -colFactor);
                    Color rightcol = ColorUtil.lightenColor(color, colFactor);
                    float w3 = w / 3;
                    float xm1 = x1 + (w3 / 2);
                    getG2D().setStroke(new BasicStroke(w3));
                    getG2D().setColor(leftcol);
                    getG2D().draw(new Line2D.Float(xm1, y1, xm1, y2));
                    getG2D().setColor(color);
                    getG2D().draw(new Line2D.Float(xm1 + w3, y1, xm1 + w3, y2));
                    getG2D().setColor(rightcol);
                    getG2D().draw(new Line2D.Float(xm1 + w3 + w3, y1, xm1 + w3 + w3, y2));
                }
                break;
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
                colFactor = (style == Constants.EN_OUTSET ? 0.4f : -0.4f);
                if (horz) {
                    color = ColorUtil.lightenColor(color, (startOrBefore ? 1 : -1) * colFactor);
                    getG2D().setStroke(new BasicStroke(h));
                    float ym1 = y1 + (h / 2);
                    getG2D().setColor(color);
                    getG2D().draw(new Line2D.Float(x1, ym1, x2, ym1));
                } else {
                    color = ColorUtil.lightenColor(color, (startOrBefore ? 1 : -1) * colFactor);
                    float xm1 = x1 + (w / 2);
                    getG2D().setStroke(new BasicStroke(w));
                    getG2D().setColor(color);
                    getG2D().draw(new Line2D.Float(xm1, y1, xm1, y2));
                }
                break;
            case Constants.EN_HIDDEN:
                break;
            default:
                getG2D().setColor(color);
                if (horz) {
                    float ym = y1 + (h / 2);
                    getG2D().setStroke(new BasicStroke(h));
                    getG2D().draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float xm = x1 + (w / 2);
                    getG2D().setStroke(new BasicStroke(w));
                    getG2D().draw(new Line2D.Float(xm, y1, xm, y2));
                }
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style) {
        if (start.y != end.y) {
            //TODO Support arbitrary lines if necessary
            throw new UnsupportedOperationException(
                    "Can only deal with horizontal lines right now");
        }

        saveGraphicsState();
        int half = width / 2;
        int starty = start.y - half;
        Rectangle boundingRect = new Rectangle(start.x, start.y - half, end.x - start.x, width);
        getG2DState().updateClip(boundingRect);

        switch (style.getEnumValue()) {
        case Constants.EN_SOLID:
        case Constants.EN_DASHED:
        case Constants.EN_DOUBLE:
            drawBorderLine(start.x, start.y - half, end.x, end.y + half,
                    true, true, style.getEnumValue(), color);
            break;
        case Constants.EN_DOTTED:
            int shift = half; //This shifts the dots to the right by half a dot's width
            drawBorderLine(start.x + shift, start.y - half, end.x + shift, end.y + half,
                    true, true, style.getEnumValue(), color);
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            getG2DState().updateColor(ColorUtil.lightenColor(color, 0.6f));
            moveTo(start.x, starty);
            lineTo(end.x, starty);
            lineTo(end.x, starty + 2 * half);
            lineTo(start.x, starty + 2 * half);
            closePath();
            getG2D().fill(currentPath);
            currentPath = null;
            getG2DState().updateColor(color);
            if (style.getEnumValue() == Constants.EN_GROOVE) {
                moveTo(start.x, starty);
                lineTo(end.x, starty);
                lineTo(end.x, starty + half);
                lineTo(start.x + half, starty + half);
                lineTo(start.x, starty + 2 * half);
            } else {
                moveTo(end.x, starty);
                lineTo(end.x, starty + 2 * half);
                lineTo(start.x, starty + 2 * half);
                lineTo(start.x, starty + half);
                lineTo(end.x - half, starty + half);
            }
            closePath();
            getG2D().fill(currentPath);
            currentPath = null;

        case Constants.EN_NONE:
            // No rule is drawn
            break;
        default:
        } // end switch
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void clip() {
        if (currentPath == null) {
            throw new IllegalStateException("No current path available!");
        }
        getG2DState().updateClip(currentPath);
        currentPath = null;
    }

    /** {@inheritDoc} */
    protected void closePath() {
        currentPath.closePath();
    }

    /** {@inheritDoc} */
    protected void lineTo(int x, int y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.lineTo(x, y);
    }

    /** {@inheritDoc} */
    protected void moveTo(int x, int y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.moveTo(x, y);
    }

    /** {@inheritDoc} */
    protected void saveGraphicsState() {
        this.painter.saveGraphicsState();
    }

    /** {@inheritDoc} */
    protected void restoreGraphicsState() {
        this.painter.restoreGraphicsState();
        this.currentPath = null;
    }

}
