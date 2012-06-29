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

package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;

/**
 * PostScript-specific implementation of the {@link BorderPainter}.
 */
public class PSBorderPainter extends BorderPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(PSBorderPainter.class);

    private PSGenerator generator;

    /**
     * Creates a new border painter for PostScript.
     * @param generator the PostScript generator
     */
    public PSBorderPainter(PSGenerator generator) {
        this.generator = generator;
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(                               // CSOK: ParameterNumber
            int x1, int y1, int x2, int y2, boolean horz,
            boolean startOrBefore, int style, Color col) throws IOException {
       drawBorderLine(generator, toPoints(x1), toPoints(y1), toPoints(x2), toPoints(y2),
               horz, startOrBefore, style, col);
    }

    private static void drawLine(PSGenerator gen,
            float startx, float starty, float endx, float endy) throws IOException {
        gen.writeln(gen.formatDouble(startx) + " "
                + gen.formatDouble(starty) + " " + gen.mapCommand("moveto") + " "
                + gen.formatDouble(endx) + " "
                + gen.formatDouble(endy) + " " + gen.mapCommand("lineto") + " "
                + gen.mapCommand("stroke") + " " + gen.mapCommand("newpath"));
    }

    /**
     * @param gen ps content generator
     * @see BorderPainter#drawBorderLine
     */
    public static void drawBorderLine(                           // CSOK: ParameterNumber
            PSGenerator gen,
            float x1, float y1, float x2, float y2, boolean horz,  // CSOK: JavadocMethod
            boolean startOrBefore, int style, Color col)           // CSOK: JavadocMethod
            throws IOException {                                   // CSOK: JavadocMethod
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED:
                gen.useColor(col);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    gen.useDash("[" + unit + "] 0");
                    gen.useLineCap(0);
                    gen.useLineWidth(h);
                    float ym = y1 + (h / 2);
                    drawLine(gen, x1, ym, x2, ym);
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    gen.useDash("[" + unit + "] 0");
                    gen.useLineCap(0);
                    gen.useLineWidth(w);
                    float xm = x1 + (w / 2);
                    drawLine(gen, xm, y1, xm, y2);
                }
                break;
            case Constants.EN_DOTTED:
                gen.useColor(col);
                gen.useLineCap(1); //Rounded!
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    gen.useDash("[0 " + unit + "] 0");
                    gen.useLineWidth(h);
                    float ym = y1 + (h / 2);
                    drawLine(gen, x1, ym, x2, ym);
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    gen.useDash("[0 " + unit + "] 0");
                    gen.useLineWidth(w);
                    float xm = x1 + (w / 2);
                    drawLine(gen, xm, y1, xm, y2);
                }
                break;
            case Constants.EN_DOUBLE:
                gen.useColor(col);
                gen.useDash(null);
                if (horz) {
                    float h3 = h / 3;
                    gen.useLineWidth(h3);
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    drawLine(gen, x1, ym1, x2, ym1);
                    drawLine(gen, x1, ym2, x2, ym2);
                } else {
                    float w3 = w / 3;
                    gen.useLineWidth(w3);
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    drawLine(gen, xm1, y1, xm1, y2);
                    drawLine(gen, xm2, y1, xm2, y2);
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
                float colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
                gen.useDash(null);
                if (horz) {
                    Color uppercol = ColorUtil.lightenColor(col, -colFactor);
                    Color lowercol = ColorUtil.lightenColor(col, colFactor);
                    float h3 = h / 3;
                    gen.useLineWidth(h3);
                    float ym1 = y1 + (h3 / 2);
                    gen.useColor(uppercol);
                    drawLine(gen, x1, ym1, x2, ym1);
                    gen.useColor(col);
                    drawLine(gen, x1, ym1 + h3, x2, ym1 + h3);
                    gen.useColor(lowercol);
                    drawLine(gen, x1, ym1 + h3 + h3, x2, ym1 + h3 + h3);
                } else {
                    Color leftcol = ColorUtil.lightenColor(col, -colFactor);
                    Color rightcol = ColorUtil.lightenColor(col, colFactor);
                    float w3 = w / 3;
                    gen.useLineWidth(w3);
                    float xm1 = x1 + (w3 / 2);
                    gen.useColor(leftcol);
                    drawLine(gen, xm1, y1, xm1, y2);
                    gen.useColor(col);
                    drawLine(gen, xm1 + w3, y1, xm1 + w3, y2);
                    gen.useColor(rightcol);
                    drawLine(gen, xm1 + w3 + w3, y1, xm1 + w3 + w3, y2);
                }
                break;
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
                colFactor = (style == Constants.EN_OUTSET ? 0.4f : -0.4f);
                gen.useDash(null);
                if (horz) {
                    Color c = ColorUtil.lightenColor(col, (startOrBefore ? 1 : -1) * colFactor);
                    gen.useLineWidth(h);
                    float ym1 = y1 + (h / 2);
                    gen.useColor(c);
                    drawLine(gen, x1, ym1, x2, ym1);
                } else {
                    Color c = ColorUtil.lightenColor(col, (startOrBefore ? 1 : -1) * colFactor);
                    gen.useLineWidth(w);
                    float xm1 = x1 + (w / 2);
                    gen.useColor(c);
                    drawLine(gen, xm1, y1, xm1, y2);
                }
                break;
            case Constants.EN_HIDDEN:
                break;
            default:
                gen.useColor(col);
                gen.useDash(null);
                gen.useLineCap(0);
                if (horz) {
                    gen.useLineWidth(h);
                    float ym = y1 + (h / 2);
                    drawLine(gen, x1, ym, x2, ym);
                } else {
                    gen.useLineWidth(w);
                    float xm = x1 + (w / 2);
                    drawLine(gen, xm, y1, xm, y2);
                }
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end,
            int width, Color color, RuleStyle style) throws IOException {
        if (start.y != end.y) {
            //TODO Support arbitrary lines if necessary
            throw new UnsupportedOperationException(
                    "Can only deal with horizontal lines right now");
        }

        saveGraphicsState();
        int half = width / 2;
        int starty = start.y - half;
        //Rectangle boundingRect = new Rectangle(start.x, start.y - half, end.x - start.x, width);

        switch (style.getEnumValue()) {
            case Constants.EN_SOLID:
            case Constants.EN_DASHED:
            case Constants.EN_DOUBLE:
                drawBorderLine(start.x, starty, end.x, starty + width,
                        true, true, style.getEnumValue(), color);
                break;
            case Constants.EN_DOTTED:
                clipRect(start.x, starty, end.x - start.x, width);
                //This displaces the dots to the right by half a dot's width
                //TODO There's room for improvement here
                generator.concatMatrix(1, 0, 0, 1, toPoints(half), 0);
                drawBorderLine(start.x, starty, end.x, starty + width,
                        true, true, style.getEnumValue(), color);
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
                generator.useColor(ColorUtil.lightenColor(color, 0.6f));
                moveTo(start.x, starty);
                lineTo(end.x, starty);
                lineTo(end.x, starty + 2 * half);
                lineTo(start.x, starty + 2 * half);
                closePath();
                generator.write(" " + generator.mapCommand("fill"));
                generator.writeln(" " + generator.mapCommand("newpath"));
                generator.useColor(color);
                if (style == RuleStyle.GROOVE) {
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
                generator.write(" " + generator.mapCommand("fill"));
                generator.writeln(" " + generator.mapCommand("newpath"));
                break;
            default:
                throw new UnsupportedOperationException("rule style not supported");
        }

        restoreGraphicsState();

    }

    private static float toPoints(int mpt) {
        return mpt / 1000f;
    }

    /** {@inheritDoc} */
    protected void moveTo(int x, int y) throws IOException {
        generator.writeln(generator.formatDouble(toPoints(x)) + " "
                + generator.formatDouble(toPoints(y)) + " " + generator.mapCommand("moveto"));
    }

    /** {@inheritDoc} */
    protected void lineTo(int x, int y) throws IOException {
        generator.writeln(generator.formatDouble(toPoints(x)) + " "
                + generator.formatDouble(toPoints(y)) + " " + generator.mapCommand("lineto"));
    }

    /** {@inheritDoc} */
    protected void closePath() throws IOException {
        generator.writeln("cp");
    }

    private void clipRect(int x, int y, int width, int height) throws IOException {
        generator.defineRect(toPoints(x), toPoints(y), toPoints(width), toPoints(height));
        clip();
    }

    /** {@inheritDoc} */
    protected void clip() throws IOException {
        generator.writeln(generator.mapCommand("clip") + " " + generator.mapCommand("newpath"));
    }

    /** {@inheritDoc} */
    protected void saveGraphicsState() throws IOException {
        generator.saveGraphicsState();
    }

    /** {@inheritDoc} */
    protected void restoreGraphicsState() throws IOException {
        generator.restoreGraphicsState();
    }




    /** {@inheritDoc} */
    protected void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(generator.formatDouble(toPoints(p1x)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(p1y)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(p2x)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(p2y)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(p3x)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(p3y)));
        sb.append(" curveto ");
        generator.writeln(sb.toString());

    }

    /** {@inheritDoc} */
    protected void rotateCoordinates(double angle) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(generator.formatDouble(angle * 180d / Math.PI));
        sb.append(" ");
        sb.append(" rotate ");
        generator.writeln(sb.toString());
    }

    /** {@inheritDoc} */
    protected void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(generator.formatDouble(toPoints(xTranslate)));
        sb.append(" ");
        sb.append(generator.formatDouble(toPoints(yTranslate)));
        sb.append(" ");
        sb.append(" translate ");
        generator.writeln(sb.toString());
    }

    /** {@inheritDoc} */
    protected void scaleCoordinates(float xScale, float yScale) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(generator.formatDouble(xScale));
        sb.append(" ");
        sb.append(generator.formatDouble(yScale));
        sb.append(" ");
        sb.append(" scale ");
        generator.writeln(sb.toString());
    }

}
