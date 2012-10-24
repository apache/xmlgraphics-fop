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
import java.io.IOException;

import org.apache.fop.fo.Constants;
import org.apache.fop.render.intermediate.ArcToBezierCurveTransformer;
import org.apache.fop.render.intermediate.BezierCurvePainter;
import org.apache.fop.render.intermediate.GraphicsPainter;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;

/**
 * PDF-specific implementation of the {@link GraphicsPainter}.
 */
public class PDFGraphicsPainter implements GraphicsPainter, BezierCurvePainter {

    private final PDFContentGeneratorHelper generator;

    /** Used for drawing arcs since PS does not natively support drawing elliptic curves */
    private final ArcToBezierCurveTransformer arcToBezierCurveTransformer;

    public PDFGraphicsPainter(PDFContentGenerator generator) {
        this.generator = new PDFContentGeneratorHelper(generator);
        this.arcToBezierCurveTransformer = new ArcToBezierCurveTransformer(this);
    }

    /** {@inheritDoc} */
    public void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz,
            boolean startOrBefore, int style, Color col) {
        //TODO lose scale?
        drawBorderLine2(x1 / 1000f, y1 / 1000f, x2 / 1000f, y2 / 1000f,
                horz, startOrBefore, style, col);
    }

    /** {@inheritDoc} */
    private void drawBorderLine2(float x1, float y1, float x2, float y2, boolean horz,
            boolean startOrBefore, int style, Color col) {
        float w = x2 - x1;
        float h = y2 - y1;
        float colFactor;
        switch (style) {
        case Constants.EN_DASHED:
            generator.setColor(col);
            if (horz) {
                float unit = Math.abs(2 * h);
                int rep = (int) (w / unit);
                if (rep % 2 == 0) {
                    rep++;
                }
                unit = w / rep;
                float ym = y1 + (h / 2);
                generator.setDashLine(unit)
                        .setLineWidth(h)
                        .strokeLine(x1, ym, x2, ym);
            } else {
                float unit = Math.abs(2 * w);
                int rep = (int) (h / unit);
                if (rep % 2 == 0) {
                    rep++;
                }
                unit = h / rep;
                float xm = x1 + (w / 2);
                generator.setDashLine(unit)
                        .setLineWidth(w)
                        .strokeLine(xm, y1, xm, y2);
            }
            break;
        case Constants.EN_DOTTED:
            generator.setColor(col).setRoundCap();
            if (horz) {
                float unit = Math.abs(2 * h);
                int rep = (int) (w / unit);
                if (rep % 2 == 0) {
                    rep++;
                }
                unit = w / rep;
                float ym = y1 + (h / 2);
                generator.setDashLine(0, unit)
                        .setLineWidth(h)
                        .strokeLine(x1, ym, x2, ym);
            } else {
                float unit = Math.abs(2 * w);
                int rep = (int) (h / unit);
                if (rep % 2 == 0) {
                    rep++;
                }
                unit = h / rep;
                float xm = x1 + (w / 2);
                generator.setDashLine(0, unit)
                        .setLineWidth(w)
                        .strokeLine(xm, y1, xm, y2);
            }
            break;
        case Constants.EN_DOUBLE:
            generator.setColor(col)
            .setSolidLine();
            if (horz) {
                float h3 = h / 3;
                float ym1 = y1 + (h3 / 2);
                float ym2 = ym1 + h3 + h3;
                generator.setLineWidth(h3)
                        .strokeLine(x1, ym1, x2, ym1)
                        .strokeLine(x1, ym2, x2, ym2);
            } else {
                float w3 = w / 3;
                float xm1 = x1 + (w3 / 2);
                float xm2 = xm1 + w3 + w3;
                generator.setLineWidth(w3)
                        .strokeLine(xm1, y1, xm1, y2)
                        .strokeLine(xm2, y1, xm2, y2);
            }
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            colFactor = (style == Constants.EN_GROOVE ? 0.4f : -0.4f);
            generator.setSolidLine();
            if (horz) {
                Color uppercol = ColorUtil.lightenColor(col, -colFactor);
                Color lowercol = ColorUtil.lightenColor(col, colFactor);
                float h3 = h / 3;
                float ym1 = y1 + (h3 / 2);
                generator.setLineWidth(h3)
                        .setColor(uppercol)
                        .strokeLine(x1, ym1, x2, ym1)
                        .setColor(col)
                        .strokeLine(x1, ym1 + h3, x2, ym1 + h3)
                        .setColor(lowercol)
                        .strokeLine(x1, ym1 + h3 + h3, x2, ym1 + h3 + h3);
            } else {
                Color leftcol = ColorUtil.lightenColor(col, -colFactor);
                Color rightcol = ColorUtil.lightenColor(col, colFactor);
                float w3 = w / 3;
                float xm1 = x1 + (w3 / 2);
                generator.setLineWidth(w3)
                        .setColor(leftcol)
                        .strokeLine(xm1, y1, xm1, y2)
                        .setColor(col)
                        .strokeLine(xm1 + w3, y1, xm1 + w3, y2)
                        .setColor(rightcol)
                        .strokeLine(xm1 + w3 + w3, y1, xm1 + w3 + w3, y2);
            }
            break;
        case Constants.EN_INSET:
        case Constants.EN_OUTSET:
            colFactor = (style == Constants.EN_OUTSET ? 0.4f : -0.4f);
            generator.setSolidLine();
            Color c = col;
            if (horz) {
                c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                float ym1 = y1 + (h / 2);
                generator.setLineWidth(h)
                        .setColor(c)
                        .strokeLine(x1, ym1, x2, ym1);
            } else {
                c = ColorUtil.lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                float xm1 = x1 + (w / 2);
                generator.setLineWidth(w)
                        .setColor(c)
                        .strokeLine(xm1, y1, xm1, y2);
            }
            break;
        case Constants.EN_HIDDEN:
            break;
        default:
            generator.setColor(col).setSolidLine();
            if (horz) {
                float ym = y1 + (h / 2);
                generator.setLineWidth(h)
                        .strokeLine(x1, ym, x2, ym);
            } else {
                float xm = x1 + (w / 2);
                generator.setLineWidth(w)
                        .strokeLine(xm, y1, xm, y2);
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
            generator.clipRect(boundingRect)
            //This displaces the dots to the right by half a dot's width
            //TODO There's room for improvement here
                    .transformCoordinatesLine(1, 0, 0 , 1, half, 0);
            drawBorderLine(start.x, start.y - half, end.x, end.y + half, true, true, style.getEnumValue(),
                    color);
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE:
            generator.setFillColor(ColorUtil.lightenColor(color, 0.6f))
                    .fillRect(start.x, start.y, end.x, starty + 2 * half)
                    .setFillColor(color)
                    .fillRidge(style, start.x, start.y, end.x, end.y, half);
            break;
        default:
            throw new UnsupportedOperationException("rule style not supported");
        }
        restoreGraphicsState();
    }

    private static String format(int coordinate) {
        //TODO lose scale?
        return format(coordinate / 1000f);
    }

    private static String format(float coordinate) {
        return PDFContentGenerator.format(coordinate);
    }

    /** {@inheritDoc} */
    public void moveTo(int x, int y) {
        generator.moveTo(x, y);
    }

    /** {@inheritDoc} */
    public void lineTo(int x, int y) {
        generator.lineTo(x, y);
    }

    /** {@inheritDoc} */
    public void arcTo(final double startAngle, final double endAngle, final int cx, final int cy,
            final int width, final int height) throws IOException {
        arcToBezierCurveTransformer.arcTo(startAngle, endAngle, cx, cy, width, height);
    }

    /** {@inheritDoc} */
    public void closePath() {
        generator.closePath();
    }

    /** {@inheritDoc} */
    public void clip() {
        generator.clip();
    }

    /** {@inheritDoc} */
    public void saveGraphicsState() {
        generator.saveGraphicsState();
    }

    /** {@inheritDoc} */
    public void restoreGraphicsState() {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void rotateCoordinates(double angle) throws IOException {
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        generator.transformFloatCoordinates(c, s, -s, c, 0, 0);
    }

    /** {@inheritDoc} */
    public void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
        generator.transformCoordinates(1000, 0, 0, 1000, xTranslate, yTranslate);
    }

    /** {@inheritDoc} */
    public void scaleCoordinates(float xScale, float yScale) throws IOException {
        generator.transformFloatCoordinates(xScale, 0, 0, yScale, 0, 0);
    }

    /** {@inheritDoc} */
    public void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
        generator.cubicBezierTo(p1x, p1y, p2x, p2y, p3x, p3y);
    }

    // TODO consider enriching PDFContentGenerator with part of this API
    private static class PDFContentGeneratorHelper {

        private final PDFContentGenerator generator;

        public PDFContentGeneratorHelper(PDFContentGenerator generator) {
            this.generator = generator;
        }

        public PDFContentGeneratorHelper moveTo(int x, int y) {
            return add("m", format(x), format(y));
        }

        public PDFContentGeneratorHelper lineTo(int x, int y) {
            return add("l", format(x), format(y));
        }

        /** {@inheritDoc} */
        public PDFContentGeneratorHelper cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) {
            return add("c", format(p1x), format(p1y), format(p2x), format(p2y), format(p3x), format(p3y));
        }

        public PDFContentGeneratorHelper closePath() {
            return add("h");
        }

        public PDFContentGeneratorHelper clip() {
            return addLine("W\nn");
        }

        public PDFContentGeneratorHelper clipRect(Rectangle rectangle) {
            generator.clipRect(rectangle);
            return this;
        }

        public PDFContentGeneratorHelper saveGraphicsState() {
            return addLine("q");
        }

        public PDFContentGeneratorHelper restoreGraphicsState() {
            return addLine("Q");
        }

        public PDFContentGeneratorHelper setSolidLine() {
            generator.add("[] 0 d ");
            return this;
        }

        public PDFContentGeneratorHelper setRoundCap() {
            return add("J", "1");
        }

        public PDFContentGeneratorHelper strokeLine(float xStart, float yStart, float xEnd, float yEnd) {
            add("m", xStart, yStart);
            return addLine("l S", xEnd, yEnd);
        }

        public PDFContentGeneratorHelper fillRect(int xStart, int yStart, int xEnd, int yEnd) {
            String xS = format(xStart);
            String xE = format(xEnd);
            String yS = format(yStart);
            String yE = format(yEnd);
            return addLine("m", xS, yS)
                    .addLine("l", xE, yS)
                    .addLine("l", xE, yE)
                    .addLine("l", xS, yE)
                    .addLine("h")
                    .addLine("f");
        }

        public PDFContentGeneratorHelper fillRidge(RuleStyle style, int xStart, int yStart, int xEnd,
                int yEnd, int half) {
            String xS = format(xStart);
            String xE = format(xEnd);
            String yS = format(yStart);
            if (style == RuleStyle.GROOVE) {
                addLine("m", xS, yS)
                        .addLine("l", xE, yS)
                        .addLine("l", xE, format(yStart + half))
                        .addLine("l", format(xStart + half), format(yStart + half))
                        .addLine("l", xS, format(yStart + 2 * half));
            } else {
                addLine("m", xE, yS)
                        .addLine("l", xE, format(yStart + 2 * half))
                        .addLine("l", xS, format(yStart + 2 * half))
                        .addLine("l", xS, format(yStart + half))
                        .addLine("l", format(xEnd - half), format(yStart + half));
            }
            return addLine("h").addLine("f");
        }

        public PDFContentGeneratorHelper setLineWidth(float width) {
            return addLine("w", width);
        }

        public PDFContentGeneratorHelper setDashLine(float first, float... rest) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(format(first));
            for (float unit : rest) {
                sb.append(" ").append(format(unit));
            }
            sb.append("] 0 d ");
            generator.add(sb.toString());
            return this;
        }

        public PDFContentGeneratorHelper setColor(Color col) {
            generator.setColor(col, false);
            return this;
        }

        public PDFContentGeneratorHelper setFillColor(Color col) {
            generator.setColor(col, true);
            return this;
        }

        public PDFContentGeneratorHelper transformFloatCoordinates(float a, float b, float c, float d,
                float e, float f) {
            return add("cm", a, b, c, d, e, f);
        }

        public PDFContentGeneratorHelper transformCoordinates(int a, int b, int c, int d, int e, int f) {
            return add("cm", format(a), format(b), format(c), format(d), format(e), format(f));
        }

        public PDFContentGeneratorHelper transformCoordinatesLine(int a, int b, int c, int d, int e, int f) {
            return addLine("cm", format(a), format(b), format(c), format(d), format(e), format(f));
        }

        public PDFContentGeneratorHelper add(String op) {
            assert op.equals(op.trim());
            generator.add(op + " ");
            return this;
        }

        private PDFContentGeneratorHelper add(String op, String... args) {
            add(createArgs(args), op);
            return this;
        }

        public PDFContentGeneratorHelper addLine(String op) {
            assert op.equals(op.trim());
            generator.add(op + "\n");
            return this;
        }

        public PDFContentGeneratorHelper addLine(String op, String... args) {
            addLine(createArgs(args), op);
            return this;
        }

        private PDFContentGeneratorHelper add(String op, float... args) {
            add(createArgs(args), op);
            return this;
        }

        public PDFContentGeneratorHelper addLine(String op, float... args) {
            addLine(createArgs(args), op);
            return this;
        }

        private StringBuilder createArgs(float... args) {
            StringBuilder sb = new StringBuilder();
            for (float arg : args) {
                sb.append(format(arg)).append(" ");
            }
            return sb;
        }

        private StringBuilder createArgs(String... args) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(" ");
            }
            return sb;
        }

        private void add(StringBuilder args, String op) {
            assert op.equals(op.trim());
            generator.add(args.append(op).append(" ").toString());
        }

        private void addLine(StringBuilder args, String op) {
            assert op.equals(op.trim());
            generator.add(args.append(op).append("\n").toString());
        }
    }

}
