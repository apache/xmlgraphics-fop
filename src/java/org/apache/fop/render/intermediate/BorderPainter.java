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

package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * This is an abstract base class for handling border painting.
 */
public abstract class BorderPainter {

    /** TODO remove before integration*/
    public static final String ROUNDED_CORNERS = "fop.round-corners";

    /** TODO Use a class to model border instead of an array
     * convention index of before, end, after and start borders */
    protected static final int BEFORE = 0, END = 1, AFTER = 2, START = 3;
    /** TODO Use a class to model border corners instead of an array
     convention index of before_start, before_end, after_end and after_start border corners*/
    protected static final int BEFORE_START = 0, BEFORE_END = 1, AFTER_END = 2, AFTER_START = 3;


    /**
     * Draws borders.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     * @param innerBackgroundColor the inner background color
     * @throws IFException if an error occurs while drawing the borders
     */
    public void drawBorders(Rectangle borderRect,               // CSOK: MethodLength
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd, Color innerBackgroundColor)
                throws IFException {

        try {
            if (isRoundedCornersSupported()) {
                drawRoundedBorders(borderRect, bpsBefore, bpsAfter,
                        bpsStart, bpsEnd);

            } else {
                drawRectangularBorders(borderRect, bpsBefore, bpsAfter,
                        bpsStart, bpsEnd);
            }

        } catch (IOException ioe) {
            throw new IFException("IO error drawing borders", ioe);
        }
    }

    private BorderProps sanitizeBorderProps(BorderProps bps) {
        return bps == null ? bps : bps.width == 0 ? (BorderProps)null : bps;
    }

    /**
     * TODO merge with drawRoundedBorders()?
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     * @throws IOException
     */
    protected void drawRectangularBorders(Rectangle borderRect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IOException {

        bpsBefore = sanitizeBorderProps(bpsBefore);
        bpsAfter = sanitizeBorderProps(bpsAfter);
        bpsStart = sanitizeBorderProps(bpsStart);
        bpsEnd = sanitizeBorderProps(bpsEnd);

        int startx = borderRect.x;
        int starty = borderRect.y;
        int width = borderRect.width;
        int height = borderRect.height;
        boolean[] b = new boolean[] {
                (bpsBefore != null), (bpsEnd != null),
                (bpsAfter != null), (bpsStart != null)};
        if (!b[0] && !b[1] && !b[2] && !b[3]) {
            return;
        }
        int[] bw = new int[] {
                (b[BEFORE] ? bpsBefore.width : 0),
                (b[END] ? bpsEnd.width : 0),
                (b[AFTER] ? bpsAfter.width : 0),
                (b[3] ? bpsStart.width : 0)};
        int[] clipw = new int[] {
                BorderProps.getClippedWidth(bpsBefore),
                BorderProps.getClippedWidth(bpsEnd),
                BorderProps.getClippedWidth(bpsAfter),
                BorderProps.getClippedWidth(bpsStart)};
        starty += clipw[BEFORE];
        height -= clipw[BEFORE];
        height -= clipw[AFTER];
        startx += clipw[START];
        width -= clipw[START];
        width -= clipw[END];

        boolean[] slant = new boolean[] {
                (b[START] && b[BEFORE]),
                (b[BEFORE] && b[END]),
                (b[END] && b[AFTER]),
                (b[AFTER] && b[START])};
        if (bpsBefore != null) {
            int sx1 = startx;
            int sx2 = (slant[BEFORE_START] ? sx1 + bw[START] - clipw[START] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[BEFORE_END] ? ex1 - bw[END] + clipw[END] : ex1);
            int outery = starty - clipw[BEFORE];
            int clipy = outery + clipw[BEFORE];
            int innery = outery + bw[BEFORE];

            saveGraphicsState();
            moveTo(sx1, clipy);


            int sx1a = sx1;
            int ex1a = ex1;
            if (bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[START];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[END];
                }
                lineTo(sx1a, outery);
                lineTo(ex1a, outery);
            }
            lineTo(ex1, clipy);
            lineTo(ex2, innery);
            lineTo(sx2, innery);
            closePath();
            clip();
            drawBorderLine(sx1a, outery, ex1a, innery, true, true,
                    bpsBefore.style, bpsBefore.color);
            restoreGraphicsState();
        }
        if (bpsEnd != null) {
            int sy1 = starty;
            int sy2 = (slant[BEFORE_END] ? sy1 + bw[BEFORE] - clipw[BEFORE] : sy1);
            int ey1 = starty + height;
            int ey2 = (slant[AFTER_END] ? ey1 - bw[AFTER] + clipw[AFTER] : ey1);
            int outerx = startx + width + clipw[END];
            int clipx = outerx - clipw[END];
            int innerx = outerx - bw[END];

            saveGraphicsState();
            moveTo(clipx, sy1);
            int sy1a = sy1;
            int ey1a = ey1;
            if (bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[BEFORE];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[AFTER];
                }
                lineTo(outerx, sy1a);
                lineTo(outerx, ey1a);
            }
            lineTo(clipx, ey1);
            lineTo(innerx, ey2);
            lineTo(innerx, sy2);
            closePath();
            clip();
            drawBorderLine(innerx, sy1a, outerx, ey1a, false, false, bpsEnd.style, bpsEnd.color);
            restoreGraphicsState();
        }
        if (bpsAfter != null) {
            int sx1 = startx;
            int sx2 = (slant[AFTER_START] ? sx1 + bw[START] - clipw[START] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[AFTER_END] ? ex1 - bw[END] + clipw[END] : ex1);
            int outery = starty + height + clipw[AFTER];
            int clipy = outery - clipw[AFTER];
            int innery = outery - bw[AFTER];

            saveGraphicsState();
            moveTo(ex1, clipy);
            int sx1a = sx1;
            int ex1a = ex1;
            if (bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[START];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[END];
                }
                lineTo(ex1a, outery);
                lineTo(sx1a, outery);
            }
            lineTo(sx1, clipy);
            lineTo(sx2, innery);
            lineTo(ex2, innery);
            closePath();
            clip();
            drawBorderLine(sx1a, innery, ex1a, outery, true, false, bpsAfter.style, bpsAfter.color);
            restoreGraphicsState();
        }
        if (bpsStart != null) {
            int sy1 = starty;
            int sy2 = (slant[BEFORE_START] ? sy1 + bw[BEFORE] - clipw[BEFORE] : sy1);
            int ey1 = sy1 + height;
            int ey2 = (slant[AFTER_START] ? ey1 - bw[AFTER] + clipw[AFTER] : ey1);
            int outerx = startx - clipw[START];
            int clipx = outerx + clipw[START];
            int innerx = outerx + bw[START];

            saveGraphicsState();

            moveTo(clipx, ey1);

            int sy1a = sy1;
            int ey1a = ey1;
            if (bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[BEFORE];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[AFTER];
                }
                lineTo(outerx, ey1a);
                lineTo(outerx, sy1a);
            }
            lineTo(clipx, sy1);
            lineTo(innerx, sy2);
            lineTo(innerx, ey2);
            closePath();
            clip();
            drawBorderLine(outerx, sy1a, innerx, ey1a, false, true, bpsStart.style, bpsStart.color);
            restoreGraphicsState();
        }
    }

    /** TODO merge with drawRectangularBorders?
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     * @throws IOException on io exception
     * */
    protected void drawRoundedBorders(Rectangle borderRect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IOException {

        bpsBefore = sanitizeBorderProps(bpsBefore);
        bpsAfter = sanitizeBorderProps(bpsAfter);
        bpsStart = sanitizeBorderProps(bpsStart);
        bpsEnd = sanitizeBorderProps(bpsEnd);

        boolean[] b = new boolean[] {
                (bpsBefore != null), (bpsEnd != null),
                (bpsAfter != null), (bpsStart != null)};
        if (!b[BEFORE] && !b[END] && !b[AFTER] && !b[START]) {
            return;
        }
        int[] bw = new int[] {
                (b[BEFORE] ? bpsBefore.width : 0),
                (b[END] ? bpsEnd.width : 0),
                (b[AFTER] ? bpsAfter.width : 0),
                (b[START] ? bpsStart.width : 0)};

        int[] clipw = new int[] {
                BorderProps.getClippedWidth(bpsBefore),
                BorderProps.getClippedWidth(bpsEnd),
                BorderProps.getClippedWidth(bpsAfter),
                BorderProps.getClippedWidth(bpsStart)};

        final int startx = borderRect.x + clipw[START];
        final int starty = borderRect.y + clipw[BEFORE];
        final int width = borderRect.width - clipw[START] - clipw[END];
        final int height = borderRect.height - clipw[BEFORE] - clipw[AFTER];

        boolean[] slant = new boolean[] {
                (b[START] && b[BEFORE]), (b[BEFORE] && b[END]),
                (b[END] && b[AFTER]), (b[START] && b[AFTER])};

        //Determine scale factor if any adjacent elliptic corners overlap
        double esf = cornerScaleFactor(width, height, bpsBefore, bpsAfter, bpsStart, bpsEnd);

        if (bpsBefore != null) {
            //Let x increase in the START->END direction
            final int sx2 = (slant[BEFORE_START] ? bw[START] - clipw[START] : 0);
            final int ex1 =  width;
            final int ex2 = (slant[BEFORE_END] ? ex1 - bw[END] + clipw[END] : ex1);
            final int outery = -clipw[BEFORE];
            final int innery = outery + bw[BEFORE];
            final int clipy = outery + clipw[BEFORE];
            final int ellipseSBW = bpsStart == null ? 0 : (int)(esf * bpsStart.getRadiusStart());
            final int ellipseSBH = (int)(esf * bpsBefore.getRadiusStart());
            final int ellipseSBX = ellipseSBW;
            final int ellipseSBY = clipy + ellipseSBH;
            final int ellipseBEW = bpsEnd == null ? 0 : (int)(esf * bpsEnd.getRadiusStart());
            final int ellipseBEH = (int)(esf * bpsBefore.getRadiusEnd());
            final int ellipseBEX = ex1 - ellipseBEW;
            final int ellipseBEY = clipy + ellipseBEH;

            saveGraphicsState();
            translateCoordinates(startx, starty);
            drawBorderSegment( sx2, ex1, ex2,  outery, innery,
                    clipw[START], clipw[END],
                    ellipseSBX, ellipseSBY, ellipseSBW, ellipseSBH,
                    ellipseBEX, ellipseBEY, ellipseBEW, ellipseBEH,
                    bpsBefore, bpsStart, bpsEnd
            );
            restoreGraphicsState();
        }

        if (bpsStart != null) {
            //Let x increase in the AFTER->BEFORE direction
            final int sx2 = (slant[AFTER_START] ?  bw[AFTER] - clipw[AFTER] : 0);
            final int ex1 = height;
            final int ex2 = (slant[BEFORE_START] ? ex1 - bw[BEFORE] + clipw[BEFORE] : ex1);
            final int outery = -clipw[START];
            final int innery = outery + bw[START];
            final int clipy = outery + clipw[START];
            final int ellipseSBW = bpsAfter == null ? 0 : (int)(esf * bpsAfter.getRadiusStart());
            final int ellipseSBH = (int)(esf * bpsStart.getRadiusEnd());
            final int ellipseSBX = ellipseSBW;
            final int ellipseSBY = clipy + ellipseSBH;
            final int ellipseBEW = bpsBefore == null ? 0 : (int)(esf * bpsBefore.getRadiusStart());
            final int ellipseBEH = (int)(esf * bpsStart.getRadiusStart());
            final int ellipseBEX = ex1 - ellipseBEW;
            final int ellipseBEY = clipy + ellipseBEH;

            saveGraphicsState();
            translateCoordinates(startx, starty + height);
            rotateCoordinates(Math.PI * 3d / 2d);
            drawBorderSegment( sx2, ex1, ex2, outery, innery,
                    clipw[AFTER],  clipw[BEFORE],
                    ellipseSBX, ellipseSBY, ellipseSBW, ellipseSBH,
                    ellipseBEX, ellipseBEY, ellipseBEW, ellipseBEH,
                    bpsStart, bpsAfter, bpsBefore
            );
            restoreGraphicsState();

        }


        if (bpsAfter != null) {
            //Let x increase in the START->END direction
            final int sx2 = (slant[AFTER_START] ?  bw[START] - clipw[START] : 0);
            final int ex1 = width;
            final int ex2 = (slant[AFTER_END] ? ex1 - bw[END] + clipw[END] : ex1);
            final int outery = -clipw[AFTER];
            final int innery = outery + bw[AFTER];
            final int clipy = outery + clipw[AFTER];
            final int ellipseSBW = bpsStart == null ? 0 : (int)(esf * bpsStart.getRadiusEnd());
            final int ellipseSBH =  (int)(esf * bpsAfter.getRadiusStart());
            final int ellipseSBX = ellipseSBW;
            final int ellipseSBY =  clipy + ellipseSBH;
            final int ellipseBEW = bpsEnd == null ? 0 : (int)(esf * bpsEnd.getRadiusEnd());
            final int ellipseBEH = (int)(esf * bpsAfter.getRadiusEnd());
            final int ellipseBEX = ex1 - ellipseBEW;
            final int ellipseBEY = clipy + ellipseBEH;

            saveGraphicsState();
            translateCoordinates(startx, starty + height);
            scaleCoordinates(1, -1);
            drawBorderSegment( sx2, ex1, ex2, outery, innery,
                    clipw[START], clipw[END],
                    ellipseSBX, ellipseSBY, ellipseSBW, ellipseSBH,
                    ellipseBEX, ellipseBEY, ellipseBEW, ellipseBEH,
                    bpsAfter, bpsStart, bpsEnd
            );
            restoreGraphicsState();
        }

        if (bpsEnd != null) {
            //Let x increase in the BEFORE-> AFTER direction
            final int sx2 = (slant[BEFORE_END] ?  bw[BEFORE] - clipw[BEFORE] : 0);
            final int ex1 = height;
            final int ex2 = (slant[AFTER_END] ? ex1 - bw[AFTER] + clipw[AFTER] : ex1);
            final int outery = -clipw[END];
            final int innery = outery + bw[END];
            final int clipy = outery + clipw[END];
            final int ellipseSBW = bpsBefore == null ? 0 : (int)(esf * bpsBefore.getRadiusEnd());
            final int ellipseSBH = (int)(esf * bpsEnd.getRadiusStart());
            final int ellipseSBX = ellipseSBW;
            final int ellipseSBY = clipy + ellipseSBH;
            final int ellipseBEW = bpsAfter == null ? 0 : (int)(esf * bpsAfter.getRadiusEnd());
            final int ellipseBEH = (int)(esf * bpsEnd.getRadiusEnd());
            final int ellipseBEX = ex1 - ellipseBEW;
            final int ellipseBEY = clipy + ellipseBEH;

            saveGraphicsState();
            translateCoordinates(startx + width, starty);
            rotateCoordinates(Math.PI / 2d);
            drawBorderSegment( sx2, ex1, ex2, outery, innery,
                    clipw[BEFORE], clipw[AFTER],
                    ellipseSBX, ellipseSBY, ellipseSBW, ellipseSBH,
                    ellipseBEX, ellipseBEY, ellipseBEW, ellipseBEH,
                    bpsEnd,  bpsBefore, bpsAfter
            );
            restoreGraphicsState();
        }
    }

    /** TODO collect parameters into useful data structures*/
    private void drawBorderSegment(final int sx2, final int ex1, final int ex2,
            final int outery, final int innery,
            final int clipWidthStart, final int clipWidthEnd,
            final int ellipseSBX, final int ellipseSBY,
            final int ellipseSBRadiusX, final int ellipseSBRadiusY,
            final int ellipseBEX,  final int ellipseBEY,
            final int ellipseBERadiusX, final int ellipseBERadiusY,
            final BorderProps bpsThis, final BorderProps bpsStart, final BorderProps bpsEnd )
    throws IOException {

        int sx1a = 0;
        int ex1a = ex1;


        if (ellipseSBRadiusX != 0 && ellipseSBRadiusY != 0 ) {

            final double[] joinMetrics = getCornerBorderJoinMetrics(ellipseSBRadiusX,
                    ellipseSBRadiusY, (double)innery / sx2);

            final double outerJoinPointX = joinMetrics[0];
            final double outerJoinPointY = joinMetrics[1];
            final double sbJoinAngle = joinMetrics[2];

            moveTo((int)outerJoinPointX, (int)outerJoinPointY);
            arcTo(Math.PI + sbJoinAngle, Math.PI * 3 / 2,
                    ellipseSBX, ellipseSBY, ellipseSBRadiusX, ellipseSBRadiusY);
        }  else {

            moveTo(0, 0);

            if (bpsThis.mode == BorderProps.COLLAPSE_OUTER) {

                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipWidthStart;
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipWidthEnd;
                }

                lineTo(sx1a, outery);
                lineTo(ex1a, outery);
            }
        }

        if (ellipseBERadiusX != 0 && ellipseBERadiusY != 0) {

            final double[] outerJoinMetrics = getCornerBorderJoinMetrics(
                    ellipseBERadiusX, ellipseBERadiusY, (double)innery / (ex1 - ex2));
            final double beJoinAngle = Math.PI / 2 - outerJoinMetrics[2];

            lineTo(ellipseBEX, 0);
            arcTo( Math.PI * 3 / 2 , Math.PI * 3 / 2 + beJoinAngle,
                    ellipseBEX, ellipseBEY, ellipseBERadiusX, ellipseBERadiusY);

            if (ellipseBEX < ex2 && ellipseBEY > innery) {

                final double[] innerJoinMetrics = getCornerBorderJoinMetrics(
                        (double)ex2 - ellipseBEX, (double)ellipseBEY - innery,
                        (double)innery / (ex1 - ex2));
                final double innerJoinPointX = innerJoinMetrics[0];
                final double innerJoinPointY = innerJoinMetrics[1];
                final double beInnerJoinAngle = Math.PI / 2 - innerJoinMetrics[2];

                lineTo((int) (ex2 - innerJoinPointX), (int)(innerJoinPointY + innery));
                arcTo(beInnerJoinAngle + Math.PI * 3 / 2, Math.PI * 3 / 2,
                        ellipseBEX, ellipseBEY, ex2 - ellipseBEX, ellipseBEY - innery);
            } else {
                lineTo(ex2, innery);
            }

        } else {
            lineTo(ex1, 0);
            lineTo(ex2, innery);
        }

        if (ellipseSBRadiusX == 0) {
            lineTo(sx2, innery);
        } else {
            if (ellipseSBX > sx2 &&  ellipseSBY > innery) {


                final double[] innerJoinMetrics = getCornerBorderJoinMetrics(ellipseSBRadiusX - sx2,
                        ellipseSBRadiusY - innery, (double)innery / sx2);

                final double sbInnerJoinAngle = innerJoinMetrics[2];

                lineTo(ellipseSBX, innery);
                arcTo(Math.PI * 3 / 2, sbInnerJoinAngle + Math.PI,
                        ellipseSBX, ellipseSBY, ellipseSBX - sx2, ellipseSBY - innery);
            } else {
                lineTo(sx2, innery);
            }
        }

        closePath();
        clip();

        if (ellipseBERadiusY == 0 && ellipseSBRadiusY == 0) {
            drawBorderLine(sx1a, outery, ex1a, innery, true, true,
                    bpsThis.style, bpsThis.color);

        } else {
            int innerFillY = Math.max(Math.max(ellipseBEY, ellipseSBY), innery);
            drawBorderLine(sx1a, outery, ex1a, innerFillY, true, true,
                    bpsThis.style, bpsThis.color);
        }
    }

    private double[] getCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY,
            double borderWidthRatio) {

        //TODO decide on implementation
        boolean invert = System.getProperty("fop.round-corners.border-invert") != null;
        if (invert) {
            borderWidthRatio = 1d / borderWidthRatio;
        }
        String cornerJoinStyle = System.getProperty("fop.round-corners.corner-join-style");
        if ("css".equals(cornerJoinStyle)) {
            return getCSSCornerBorderJoinMetrics(ellipseCenterX, ellipseCenterY, borderWidthRatio);
        } else {
            if (invert) { throw new RuntimeException("non css AND bw inverted!"); }
            return getDefaultCornerBorderJoinMetrics(
                    ellipseCenterX, ellipseCenterY, borderWidthRatio);
        }

    }

    private double[] getCSSCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY,
            double borderWidthRatio) {

        double angle = Math.atan(borderWidthRatio);
        double x = ellipseCenterX * Math.cos(Math.atan(ellipseCenterX
                / ellipseCenterY * borderWidthRatio));
        double y = ellipseCenterY * Math.sqrt(1d - x * x / ellipseCenterX / ellipseCenterX);

        return new double[]{ellipseCenterX - x, ellipseCenterY - y, angle};
    }
    private double[] getDefaultCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY,
            double borderWidthRatio) {

        double x = ellipseCenterY * ellipseCenterX * (
                ellipseCenterY + ellipseCenterX * borderWidthRatio
                - Math.sqrt(2d * ellipseCenterX * ellipseCenterY * borderWidthRatio)
        )
        / (ellipseCenterY * ellipseCenterY
                + ellipseCenterX * ellipseCenterX * borderWidthRatio * borderWidthRatio);
        double y = borderWidthRatio * x;

        return new double[]{x, y, Math.atan((ellipseCenterY - y) / (ellipseCenterX - x))};
    }

    /**
     * Clip the background to the inner border
     * @param rect clipping rectangle
     * @param bpsBefore before border
     * @param bpsAfter after border
     * @param bpsStart start border
     * @param bpsEnd end border
     * @throws IOException if an I/O error occurs
     */
    public void clipBackground(Rectangle rect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IOException {
        int startx = rect.x;
        int starty = rect.y;
        int width = rect.width;
        int height = rect.height;


        int fullWidth = width + ( bpsStart == null ? 0 : bpsStart.width )
            + (bpsStart == null ? 0 : bpsStart.width);
        int fullHeight = height + ( bpsBefore == null ? 0 : bpsBefore.width )
            + (bpsAfter == null ? 0 : bpsAfter.width);

        double esf = cornerScaleFactor( fullWidth,  fullHeight,   bpsBefore,  bpsAfter,
                bpsStart,  bpsEnd);

        int ellipseSS = 0;
        int ellipseBS = 0;
        int ellipseBE = 0;
        int ellipseES = 0;
        int ellipseEE = 0;
        int ellipseAE = 0;
        int ellipseAS = 0;
        int ellipseSE = 0;

        if (bpsBefore != null && bpsBefore.getRadiusStart() > 0
                && bpsStart != null && bpsStart.getRadiusStart() > 0) {
            ellipseSS = Math.max((int)(bpsStart.getRadiusStart() * esf) - bpsStart.width, 0);
            ellipseBS = Math.max((int)(bpsBefore.getRadiusStart() * esf) - bpsBefore.width, 0);
        }

        if (bpsBefore != null && bpsBefore.getRadiusEnd() > 0
                && bpsEnd != null && bpsEnd.getRadiusStart() > 0) {
            ellipseBE = Math.max((int)(bpsBefore.getRadiusEnd() * esf) - bpsBefore.width, 0);
            ellipseES = Math.max((int)(bpsEnd.getRadiusStart() * esf) - bpsEnd.width, 0);
        }

        if (bpsEnd != null && bpsEnd.getRadiusEnd() > 0
                && bpsAfter != null && bpsAfter.getRadiusEnd() > 0) {
            ellipseEE = Math.max((int)(bpsEnd.getRadiusEnd() * esf) - bpsEnd.width, 0);
            ellipseAE = Math.max((int)(bpsAfter.getRadiusEnd() * esf) - bpsAfter.width, 0);
        }

        if (bpsAfter != null && bpsAfter.getRadiusStart() > 0
                && bpsStart != null && bpsStart.getRadiusEnd() > 0) {
            ellipseAS = Math.max((int)(bpsAfter.getRadiusStart() * esf) - bpsAfter.width, 0);
            ellipseSE = Math.max((int)(bpsStart.getRadiusEnd() * esf) - bpsStart.width, 0);
        }

        // Draw clipping region in the order: Before->End->After->Start
        moveTo(startx + ellipseSS, starty);

        lineTo(startx + width - ellipseES, starty);

        if (ellipseBE > 0 && ellipseES > 0) {
            arcTo(Math.PI * 3 / 2, Math.PI * 2,
                    startx + width - ellipseES, starty + ellipseBE, ellipseES, ellipseBE);
        }

        lineTo(startx + width,  starty + height - ellipseAE);

        if (ellipseEE > 0 && ellipseAE > 0) {
            arcTo(0, Math.PI / 2, startx + width - ellipseEE,
                    starty + height - ellipseAE, ellipseEE, ellipseAE);
        }

        lineTo(startx + ellipseSE, starty + height);

        if (ellipseSE > 0 && ellipseAS > 0) {
            arcTo( Math.PI / 2, Math.PI, startx + ellipseSE,
                    starty + height - ellipseAS, ellipseSE, ellipseAS);
        }

        lineTo( startx, starty + ellipseBS);

        if (ellipseSS > 0 && ellipseBS > 0) {
            arcTo( Math.PI, Math.PI * 3 / 2,
                    startx +  ellipseSS, starty + ellipseBS, ellipseSS, ellipseBS);
        }

        clip();

    }

    /**
     * TODO javadocs
     * If an ellipse radii exceed the border edge length then all ellipses must be  rescaled.
     */
    protected double cornerScaleFactor(int width, int height,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        // Ellipse scale factor
        double esf = 1d;

        if (bpsBefore != null) {
            double ellipseExtent = (bpsStart == null ? 0 :  bpsStart.getRadiusStart())
                    + (bpsEnd == null ? 0 :  bpsEnd.getRadiusStart());

            if (ellipseExtent > 0) {
                double f = width / ellipseExtent;
                if (f < esf) {
                    esf = f;
                }
            }
        }

        if (bpsStart != null) {
            double ellipseExtent = (bpsAfter == null ? 0 :  bpsAfter.getRadiusStart())
                    + (bpsBefore == null ? 0 :  bpsBefore.getRadiusStart());

            if (ellipseExtent > 0) {
                double f = height / ellipseExtent;
                if ( f < esf) {
                    esf = f;
                }
            }
        }

        if (bpsAfter != null) {
            double ellipseExtent = (bpsStart == null ? 0 :  bpsStart.getRadiusEnd())
                    + (bpsEnd == null ? 0 :  bpsEnd.getRadiusEnd());

            if (ellipseExtent > 0) {
                double f = width / ellipseExtent;
                if (f < esf) {
                    esf = f;
                }
            }
        }

        if (bpsEnd != null) {
            double ellipseExtent = (bpsAfter == null ? 0 :  bpsAfter.getRadiusEnd())
                    + (bpsBefore == null ? 0 :  bpsBefore.getRadiusEnd());

            if (ellipseExtent > 0) {
                double f = height / ellipseExtent;
                if (f < esf) {
                    esf = f;
                }
            }
        }

        return esf;
    }

    /**
     * Draws a border line.
     * @param x1 X coordinate of the upper left corner
     *                  of the line's bounding rectangle (in millipoints)
     * @param y1 start Y coordinate of the upper left corner
     *                  of the line's bounding rectangle (in millipoints)
     * @param x2 end X coordinate of the lower right corner
     *                  of the line's bounding rectangle (in millipoints)
     * @param y2 end y coordinate of the lower right corner
     *                  of the line's bounding rectangle (in millipoints)
     * @param horz true if it is a horizontal line
     * @param startOrBefore true if the line is the start or end edge of a border box
     * @param style the border style
     * @param color the border color
     * @throws IOException if an I/O error occurs
     */
    protected abstract void drawBorderLine(                      // CSOK: ParameterNumber
            int x1, int y1, int x2, int y2,
            boolean horz, boolean startOrBefore, int style, Color color) throws IOException;

    /**
     * Draws a line/rule.
     * @param start start point (coordinates in millipoints)
     * @param end end point (coordinates in millipoints)
     * @param width width of the line
     * @param color the line color
     * @param style the rule style
     * @throws IOException if an I/O error occurs
     */
    public abstract void drawLine(Point start, Point end,
            int width, Color color, RuleStyle style) throws IOException;

    /**
     * Moves the cursor to the given coordinate.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException if an I/O error occurs
     */
    protected abstract void moveTo(int x, int y) throws IOException;

    /**
     * Draws a line from the current cursor position to the given coordinates.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException if an I/O error occurs
     */
    protected abstract void lineTo(int x, int y) throws IOException;

    /**
     * Draw a cubic bezier from current position to (p3x, p3y) using the control points
     * (p1x, p1y) and (p2x, p2y)
     * @param p1x x coordinate of the first control point
     * @param p1y y coordinate of the first control point
     * @param p2x x coordinate of the second control point
     * @param p2y y coordinate of the second control point
     * @param p3x x coordinate of the end point
     * @param p3y y coordinate of the end point
     * @throws IOException if an I/O error occurs
     */
    protected abstract void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y)
            throws IOException;

    /**
     * Draws an arc on the ellipse centered at (cx, cy) with width width and height height
     * from start angle startAngle (with respect to the x-axis counter-clockwise)
     * to the end angle endAngle.
     * The ellipses major axis are assumed to coincide with the coordinate axis.
     * The current position MUST coincide with the starting position on the ellipse.
     * @param startAngle the start angle
     * @param endAngle the end angle
     * @param cx the x coordinate of the ellipse center
     * @param cy the y coordinate of the ellipse center
     * @param width the extent of the ellipse in the x direction
     * @param height the extent of the ellipse in the y direction
     * @throws IOException if an I/O error occurs
     */
    protected void arcTo(final double startAngle, final double endAngle, final int cx, final int cy,
            final int width, final int height)
                throws IOException {

        //  Implementation follows http://www.spaceroots.org/documents/ellipse/ -
        //      Drawing an elliptical arc using polylines, quadratic or cubic Bézier curves
        //      L. Maisonobe, July 21, 2003

        //  Scaling the coordinate system to represent the ellipse as a circle:
        final double etaStart = Math.atan(Math.tan(startAngle) * width / height)
                                    + quadrant(startAngle);
        final double etaEnd = Math.atan(Math.tan(endAngle) * width / height)
                                    + quadrant(endAngle);

        final double sinStart = Math.sin(etaStart);
        final double cosStart = Math.cos(etaStart);
        final double sinEnd = Math.sin(etaEnd);
        final double cosEnd = Math.cos(etaEnd);

        final double p0x = cx + cosStart * width;
        final double p0y = cy + sinStart * height;
        final double p3x = cx + cosEnd * width;
        final double p3y = cy + sinEnd * height;


        double etaDiff = Math.abs(etaEnd - etaStart);
        double tan = Math.tan((etaDiff) / 2d);
        final double alpha = Math.sin(etaDiff) * (Math.sqrt(4d + 3d * tan * tan) - 1d) / 3d;


        int order = etaEnd > etaStart ? 1 : -1;

        // p1 = p0 + alpha*(-sin(startAngle), cos(startAngle))
        final double p1x = p0x - alpha *  sinStart * width * order;
        final double p1y = p0y + alpha *  cosStart * height * order;

        // p1 = p3 + alpha*(sin(endAngle), -cos(endAngle))
        final double p2x = p3x + alpha *  sinEnd * width * order;
        final double p2y = p3y - alpha * cosEnd * height * order;

        //Draw the curve in original coordinate system
        cubicBezierTo((int)p1x, (int)p1y, (int)p2x, (int)p2y, (int)p3x, (int)p3y);
    }

    private double quadrant(double angle) {
        if (angle <= Math.PI ) {
            if (angle <=  Math.PI / 2d) {
                return 0;
            } else {
                return Math.PI;
            }
        } else {
            if (angle > Math.PI * 3d / 2d) {
                return 2d * Math.PI;
            } else {
                return Math.PI;
            }
        }
    }

    /**
     * Rotate the coordinate frame
     * @param angle angle in radians to rotate the coordinate frame
     * @throws IOException if an I/O error occurs
     */
    protected abstract void rotateCoordinates(double angle) throws IOException;

    /**
     * Translate the coordinate frame
     * @param xTranslate translation in the x direction
     * @param yTranslate translation in the y direction
     * @throws IOException if an I/O error occurs
     */
    protected abstract void translateCoordinates(int xTranslate, int yTranslate) throws IOException;

    /**
     * Scale the coordinate frame
     * @param xScale scale factor in the x direction
     * @param yScale scale factor in the y direction
     * @throws IOException if an I/O error occurs
     */
    protected abstract void scaleCoordinates(float xScale, float yScale) throws IOException;


    /**
     * Closes the current path.
     * @throws IOException if an I/O error occurs
     */
    protected abstract void closePath() throws IOException;

    /**
     * Reduces the current clipping region to the current path.
     * @throws IOException if an I/O error occurs
     */
    protected abstract void clip() throws IOException;

    /**
     * Save the graphics state on the stack.
     * @throws IOException if an I/O error occurs
     */
    protected abstract void saveGraphicsState() throws IOException;

    /**
     * Restore the last graphics state from the stack.
     * @throws IOException if an I/O error occurs
     */
    protected abstract void restoreGraphicsState() throws IOException;

    /**
     * TODO remove the System.props when rounded corners code is stable
     * @return true iff in rounded corners mode
     */
    public static boolean isRoundedCornersSupported() {
        return "true".equalsIgnoreCase(System.getProperty(ROUNDED_CORNERS, "true"));
    }

}
