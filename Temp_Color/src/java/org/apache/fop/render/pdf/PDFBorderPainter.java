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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;

/**
 * PDF-specific implementation of the {@link BorderPainter}.
 */
public class PDFBorderPainter extends BorderPainter {

    /** logging instance */
    private static final Log LOG = LogFactory.getLog(PDFBorderPainter.class);

    private PDFContentGenerator generator;

    /**
     * Construct a border painter.
     * @param generator a pdf content generator
     */
    public PDFBorderPainter(PDFContentGenerator generator) {
        this.generator = generator;
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(                               // CSOK: ParameterNumber
            int x1, int y1, int x2, int y2, boolean horz,
            boolean startOrBefore, int style, Color col) {
       drawBorderLine(generator, x1 / 1000f, y1 / 1000f, x2 / 1000f, y2 / 1000f,
               horz, startOrBefore, style, col);
    }

    /**
     * @param generator pdf content generator
     * @see BorderPainter#drawBorderLine
     */
    public static void drawBorderLine(       // CSOK: ParameterNumber|MethodLength
            PDFContentGenerator generator,
            float x1, float y1, float x2, float y2, boolean horz,  // CSOK: JavadocMethod
            boolean startOrBefore, int style, Color col) {         // CSOK: JavadocMethod
        float colFactor;
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            LOG.error("Negative extent received (w=" + w + ", h=" + h
                    + "). Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED:
                generator.setColor(col, false);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    generator.add("[" + format(unit) + "] 0 d ");
                    generator.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    generator.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    generator.add("[" + format(unit) + "] 0 d ");
                    generator.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    generator.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_DOTTED:
                generator.setColor(col, false);
                generator.add("1 J ");
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    generator.add("[0 " + format(unit) + "] 0 d ");
                    generator.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    generator.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    generator.add("[0 " + format(unit) + " ] 0 d ");
                    generator.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    generator.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_DOUBLE:
                generator.setColor(col, false);
                generator.add("[] 0 d ");
                if (horz) {
                    float h3 = h / 3;
                    generator.add(format(h3) + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    generator.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                    generator.add(format(x1) + " " + format(ym2) + " m "
                            + format(x2) + " " + format(ym2) + " l S\n");
                } else {
                    float w3 = w / 3;
                    generator.add(format(w3) + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    generator.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                    generator.add(format(xm2) + " " + format(y1) + " m "
                            + format(xm2) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
                colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
                generator.add("[] 0 d ");
                if (horz) {
                    Color uppercol = ColorUtil.lightenColor(col, -colFactor);
                    Color lowercol = ColorUtil.lightenColor(col, colFactor);
                    float h3 = h / 3;
                    generator.add(format(h3) + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    generator.setColor(uppercol, false);
                    generator.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                    generator.setColor(col, false);
                    generator.add(format(x1) + " " + format(ym1 + h3) + " m "
                                        + format(x2) + " " + format(ym1 + h3) + " l S\n");
                    generator.setColor(lowercol, false);
                    generator.add(format(x1) + " " + format(ym1 + h3 + h3) + " m "
                                        + format(x2) + " " + format(ym1 + h3 + h3) + " l S\n");
                } else {
                    Color leftcol = ColorUtil.lightenColor(col, -colFactor);
                    Color rightcol = ColorUtil.lightenColor(col, colFactor);
                    float w3 = w / 3;
                    generator.add(format(w3) + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    generator.setColor(leftcol, false);
                    generator.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                    generator.setColor(col, false);
                    generator.add(format(xm1 + w3) + " " + format(y1) + " m "
                                        + format(xm1 + w3) + " " + format(y2) + " l S\n");
                    generator.setColor(rightcol, false);
                    generator.add(format(xm1 + w3 + w3) + " " + format(y1) + " m "
                                        + format(xm1 + w3 + w3) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
                colFactor = (style == Constants.EN_OUTSET ? 0.4f : -0.4f);
                generator.add("[] 0 d ");
                Color c = col;
                if (horz) {
                    c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    generator.add(format(h) + " w\n");
                    float ym1 = y1 + (h / 2);
                    generator.setColor(c, false);
                    generator.add(format(x1) + " " + format(ym1) + " m "
                            + format(x2) + " " + format(ym1) + " l S\n");
                } else {
                    c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    generator.add(format(w) + " w\n");
                    float xm1 = x1 + (w / 2);
                    generator.setColor(c, false);
                    generator.add(format(xm1) + " " + format(y1) + " m "
                            + format(xm1) + " " + format(y2) + " l S\n");
                }
                break;
            case Constants.EN_HIDDEN:
                break;
            default:
                generator.setColor(col, false);
                generator.add("[] 0 d ");
                if (horz) {
                    generator.add(format(h) + " w\n");
                    float ym = y1 + (h / 2);
                    generator.add(format(x1) + " " + format(ym) + " m "
                            + format(x2) + " " + format(ym) + " l S\n");
                } else {
                    generator.add(format(w) + " w\n");
                    float xm = x1 + (w / 2);
                    generator.add(format(xm) + " " + format(y1) + " m "
                            + format(xm) + " " + format(y2) + " l S\n");
                }
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end,
            int width, Color color, RuleStyle style) {
        if (start.y != end.y) {
            //TODO Support arbitrary lines if necessary
            throw new UnsupportedOperationException(
                    "Can only deal with horizontal lines right now");
        }

        saveGraphicsState();
        int half = width / 2;
        int starty = start.y - half;
        Rectangle boundingRect = new Rectangle(start.x, start.y - half, end.x - start.x, width);
        switch (style.getEnumValue()) {
        case Constants.EN_SOLID:
        case Constants.EN_DASHED:
        case Constants.EN_DOUBLE:
            drawBorderLine(start.x, start.y - half, end.x, end.y + half,
                    true, true, style.getEnumValue(), color);
            break;
        case Constants.EN_DOTTED:
            generator.clipRect(boundingRect);
            //This displaces the dots to the right by half a dot's width
            //TODO There's room for improvement here
            generator.add("1 0 0 1 " + format(half) + " 0 cm\n");
            drawBorderLine(start.x, start.y - half, end.x, end.y + half,
                    true, true, style.getEnumValue(), color);
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            generator.setColor(ColorUtil.lightenColor(color, 0.6f), true);
            generator.add(format(start.x) + " " + format(starty) + " m\n");
            generator.add(format(end.x) + " " + format(starty) + " l\n");
            generator.add(format(end.x) + " " + format(starty + 2 * half) + " l\n");
            generator.add(format(start.x) + " " + format(starty + 2 * half) + " l\n");
            generator.add("h\n");
            generator.add("f\n");
            generator.setColor(color, true);
            if (style == RuleStyle.GROOVE) {
                generator.add(format(start.x) + " " + format(starty) + " m\n");
                generator.add(format(end.x) + " " + format(starty) + " l\n");
                generator.add(format(end.x) + " " + format(starty + half) + " l\n");
                generator.add(format(start.x + half) + " " + format(starty + half) + " l\n");
                generator.add(format(start.x) + " " + format(starty + 2 * half) + " l\n");
            } else {
                generator.add(format(end.x) + " " + format(starty) + " m\n");
                generator.add(format(end.x) + " " + format(starty + 2 * half) + " l\n");
                generator.add(format(start.x) + " " + format(starty + 2 * half) + " l\n");
                generator.add(format(start.x) + " " + format(starty + half) + " l\n");
                generator.add(format(end.x - half) + " " + format(starty + half) + " l\n");
            }
            generator.add("h\n");
            generator.add("f\n");
            break;
        default:
            throw new UnsupportedOperationException("rule style not supported");
        }
        restoreGraphicsState();
    }

    static final String format(int coordinate) {
        return format(coordinate / 1000f);
    }

    static final String format(float coordinate) {
        return PDFContentGenerator.format(coordinate);
    }

    /** {@inheritDoc} */
    protected void moveTo(int x, int y) {
        generator.add(format(x) + " " + format(y) + " m ");
    }

    /** {@inheritDoc} */
    protected void lineTo(int x, int y) {
        generator.add(format(x) + " " + format(y) + " l ");
    }

    /** {@inheritDoc} */
    protected void closePath() {
        generator.add("h ");
    }

    /** {@inheritDoc} */
    protected void clip() {
        generator.add("W\n" + "n\n");
    }

    /** {@inheritDoc} */
    protected void saveGraphicsState() {
        generator.add("q\n");
    }

    /** {@inheritDoc} */
    protected void restoreGraphicsState() {
        generator.add("Q\n");
    }

}
