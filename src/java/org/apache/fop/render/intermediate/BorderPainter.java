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
import java.awt.Rectangle;
import java.io.IOException;

import org.apache.fop.traits.BorderProps;

/**
 * This is an abstract base class for handling border painting.
 */
public class BorderPainter {

    /** TODO remove before integration*/
    public static final String ROUNDED_CORNERS = "fop.round-corners";

    /** TODO Use a class to model border instead of an array
     * convention index of top, bottom, right and left borders */
    protected static final int TOP = 0, RIGHT = 1, BOTTOM = 2, LEFT = 3;
    /** TODO Use a class to model border corners instead of an array
     convention index of top-left, top-right, bottom-right and bottom-left border corners*/
    protected static final int TOP_LEFT = 0, TOP_RIGHT = 1, BOTTOM_RIGHT = 2, BOTTOM_LEFT = 3;

    private final GraphicsPainter graphicsPainter;

    public BorderPainter(GraphicsPainter graphicsPainter) {
        this.graphicsPainter = graphicsPainter;
    }

    /**
     * Draws borders.
     * @param borderRect the border rectangle
     * @param bpsTop the border specification on the top side
     * @param bpsBottom the border specification on the bottom side
     * @param bpsLeft the border specification on the left side
     * @param bpsRight the border specification on the end side
     * @param innerBackgroundColor the inner background color
     * @throws IFException if an error occurs while drawing the borders
     */
    public void drawBorders(Rectangle borderRect,               // CSOK: MethodLength
            BorderProps bpsTop, BorderProps bpsBottom,
            BorderProps bpsLeft, BorderProps bpsRight, Color innerBackgroundColor)
                throws IFException {
        try {
            drawRoundedBorders(borderRect, bpsTop, bpsBottom, bpsLeft, bpsRight);
        } catch (IOException ioe) {
            throw new IFException("IO error drawing borders", ioe);
        }
    }

    private BorderProps sanitizeBorderProps(BorderProps bps) {
        return bps == null ? bps : bps.width == 0 ? (BorderProps) null : bps;
    }

    /**
     * TODO merge with drawRoundedBorders()?
     * @param borderRect the border rectangle
     * @param bpsTop the border specification on the top side
     * @param bpsBottom the border specification on the bottom side
     * @param bpsLeft the border specification on the left side
     * @param bpsRight the border specification on the end side
     * @throws IOException
     */
    protected void drawRectangularBorders(Rectangle borderRect,
            BorderProps bpsTop, BorderProps bpsBottom,
            BorderProps bpsLeft, BorderProps bpsRight) throws IOException {

        bpsTop = sanitizeBorderProps(bpsTop);
        bpsBottom = sanitizeBorderProps(bpsBottom);
        bpsLeft = sanitizeBorderProps(bpsLeft);
        bpsRight = sanitizeBorderProps(bpsRight);


        int startx = borderRect.x;
        int starty = borderRect.y;
        int width = borderRect.width;
        int height = borderRect.height;
        boolean[] b = new boolean[] {
                (bpsTop != null), (bpsRight != null),
                (bpsBottom != null), (bpsLeft != null)};
        if (!b[TOP] && !b[RIGHT] && !b[BOTTOM] && !b[LEFT]) {
            return;
        }
        int[] bw = new int[] {
                (b[TOP] ? bpsTop.width : 0),
                (b[RIGHT] ? bpsRight.width : 0),
                (b[BOTTOM] ? bpsBottom.width : 0),
                (b[LEFT] ? bpsLeft.width : 0)};
        int[] clipw = new int[] {
                BorderProps.getClippedWidth(bpsTop),
                BorderProps.getClippedWidth(bpsRight),
                BorderProps.getClippedWidth(bpsBottom),
                BorderProps.getClippedWidth(bpsLeft)};
        starty += clipw[TOP];
        height -= clipw[TOP];
        height -= clipw[BOTTOM];
        startx += clipw[LEFT];
        width -= clipw[LEFT];
        width -= clipw[RIGHT];

        boolean[] slant = new boolean[] {
                (b[LEFT] && b[TOP]),
                (b[TOP] && b[RIGHT]),
                (b[RIGHT] && b[BOTTOM]),
                (b[BOTTOM] && b[LEFT])};
        if (bpsTop != null) {
            int sx1 = startx;
            int sx2 = (slant[TOP_LEFT] ? sx1 + bw[LEFT] - clipw[LEFT] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[TOP_RIGHT] ? ex1 - bw[RIGHT] + clipw[RIGHT] : ex1);
            int outery = starty - clipw[TOP];
            int clipy = outery + clipw[TOP];
            int innery = outery + bw[TOP];

            saveGraphicsState();
            moveTo(sx1, clipy);


            int sx1a = sx1;
            int ex1a = ex1;
            if (isCollapseOuter(bpsTop)) {
                if (isCollapseOuter(bpsLeft)) {
                    sx1a -= clipw[LEFT];
                }
                if (isCollapseOuter(bpsRight)) {
                    ex1a += clipw[RIGHT];
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
                    bpsTop.style, bpsTop.color);
            restoreGraphicsState();
        }
        if (bpsRight != null) {
            int sy1 = starty;
            int sy2 = (slant[TOP_RIGHT] ? sy1 + bw[TOP] - clipw[TOP] : sy1);
            int ey1 = starty + height;
            int ey2 = (slant[BOTTOM_RIGHT] ? ey1 - bw[BOTTOM] + clipw[BOTTOM] : ey1);
            int outerx = startx + width + clipw[RIGHT];
            int clipx = outerx - clipw[RIGHT];
            int innerx = outerx - bw[RIGHT];
            saveGraphicsState();
            moveTo(clipx, sy1);
            int sy1a = sy1;
            int ey1a = ey1;

            if (isCollapseOuter(bpsRight)) {
                if (isCollapseOuter(bpsTop)) {
                    sy1a -= clipw[TOP];
                }
                if (isCollapseOuter(bpsBottom)) {
                    ey1a += clipw[BOTTOM];
                }
                lineTo(outerx, sy1a);
                lineTo(outerx, ey1a);
            }
            lineTo(clipx, ey1);
            lineTo(innerx, ey2);
            lineTo(innerx, sy2);
            closePath();
            clip();
            drawBorderLine(innerx, sy1a, outerx, ey1a, false, false,
                           bpsRight.style, bpsRight.color);
            restoreGraphicsState();
        }
        if (bpsBottom != null) {
            int sx1 = startx;
            int sx2 = (slant[BOTTOM_LEFT] ? sx1 + bw[LEFT] - clipw[LEFT] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[BOTTOM_RIGHT] ? ex1 - bw[RIGHT] + clipw[RIGHT] : ex1);
            int outery = starty + height + clipw[BOTTOM];
            int clipy = outery - clipw[BOTTOM];
            int innery = outery - bw[BOTTOM];
            saveGraphicsState();
            moveTo(ex1, clipy);
            int sx1a = sx1;
            int ex1a = ex1;
            if (isCollapseOuter(bpsBottom)) {
                if (isCollapseOuter(bpsLeft)) {
                    sx1a -= clipw[LEFT];
                }
                if (isCollapseOuter(bpsRight)) {
                    ex1a += clipw[RIGHT];
                }
                lineTo(ex1a, outery);
                lineTo(sx1a, outery);
            }
            lineTo(sx1, clipy);
            lineTo(sx2, innery);
            lineTo(ex2, innery);
            closePath();
            clip();
            drawBorderLine(sx1a, innery, ex1a, outery, true, false,
                           bpsBottom.style, bpsBottom.color);
            restoreGraphicsState();
        }
        if (bpsLeft != null) {
            int sy1 = starty;
            int sy2 = (slant[TOP_LEFT] ? sy1 + bw[TOP] - clipw[TOP] : sy1);
            int ey1 = sy1 + height;
            int ey2 = (slant[BOTTOM_LEFT] ? ey1 - bw[BOTTOM] + clipw[BOTTOM] : ey1);
            int outerx = startx - clipw[LEFT];
            int clipx = outerx + clipw[LEFT];
            int innerx = outerx + bw[LEFT];
            
            saveGraphicsState();

            moveTo(clipx, ey1);

            int sy1a = sy1;
            int ey1a = ey1;
            if (isCollapseOuter(bpsLeft)) {
                if (isCollapseOuter(bpsTop)) {
                    sy1a -= clipw[TOP];
                }
                if (isCollapseOuter(bpsBottom)) {
                    ey1a += clipw[BOTTOM];
                }
                lineTo(outerx, ey1a);
                lineTo(outerx, sy1a);
            }
            lineTo(clipx, sy1);
            lineTo(innerx, sy2);
            lineTo(innerx, ey2);
            closePath();
            clip();
            drawBorderLine(outerx, sy1a, innerx, ey1a, false, true, bpsLeft.style, bpsLeft.color);
            restoreGraphicsState();
        }
    }

    private boolean isCollapseOuter(BorderProps bp) {
        return bp != null && bp.isCollapseOuter();
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
            BorderProps beforeBorderProps, BorderProps afterBorderProps,
            BorderProps startBorderProps, BorderProps endBorderProps) throws IOException {
        BorderSegment before = borderSegmentForBefore(beforeBorderProps);
        BorderSegment after = borderSegmentForAfter(afterBorderProps);
        BorderSegment start = borderSegmentForStart(startBorderProps);
        BorderSegment end = borderSegmentForEnd(endBorderProps);
        if (before.getWidth() == 0 && after.getWidth() == 0 && start.getWidth() == 0 && end.getWidth() == 0) {
            return;
        }
        final int startx = borderRect.x + start.getClippedWidth();
        final int starty = borderRect.y + before.getClippedWidth();
        final int width = borderRect.width - start.getClippedWidth() - end.getClippedWidth();
        final int height = borderRect.height - before.getClippedWidth() - after.getClippedWidth();
        //Determine scale factor if any adjacent elliptic corners overlap
        double cornerCorrectionFactor = calculateCornerScaleCorrection(width, height, before, after, start, end);
        drawBorderSegment(start, before, end, 0, width, startx, starty, cornerCorrectionFactor);
        drawBorderSegment(before, end, after, 1, height, startx + width, starty, cornerCorrectionFactor);
        drawBorderSegment(end, after, start, 2, width, startx + width, starty + height, cornerCorrectionFactor);
        drawBorderSegment(after, start, before, 3, height, startx, starty + height, cornerCorrectionFactor);
    }

    private void drawBorderSegment(BorderSegment start, BorderSegment before, BorderSegment end,
            int orientation, int width, int x, int y, double cornerCorrectionFactor) throws IOException {
        if (before.getWidth() != 0) {
            //Let x increase in the START->END direction
            final int sx2 = start.getWidth() - start.getClippedWidth();
            final int ex1 =  width;
            final int ex2 = ex1 - end.getWidth() + end.getClippedWidth();
            final int outery = -before.getClippedWidth();
            final int innery = outery + before.getWidth();
            final int ellipseSBRadiusX = (int) (cornerCorrectionFactor * start.getRadiusEnd());
            final int ellipseSBRadiusY = (int) (cornerCorrectionFactor * before.getRadiusStart());
            final int ellipseBERadiusX = (int) (cornerCorrectionFactor * end.getRadiusStart());
            final int ellipseBERadiusY = (int) (cornerCorrectionFactor * before.getRadiusEnd());
            saveGraphicsState();
            translateCoordinates(x, y);
            if (orientation != 0) {
                rotateCoordinates(Math.PI * orientation / 2d);
            }
            final int ellipseSBX = ellipseSBRadiusX;
            final int ellipseSBY = ellipseSBRadiusY;
            final int ellipseBEX = ex1 - ellipseBERadiusX;
            final int ellipseBEY = ellipseBERadiusY;
            int sx1a = 0;
            int ex1a = ex1;
            if (ellipseSBRadiusX != 0 && ellipseSBRadiusY != 0) {
                final double[] joinMetrics = getCornerBorderJoinMetrics(ellipseSBRadiusX,
                        ellipseSBRadiusY, sx2, innery);
                final double outerJoinPointX = joinMetrics[0];
                final double outerJoinPointY = joinMetrics[1];
                final double sbJoinAngle = joinMetrics[2];
                moveTo((int) outerJoinPointX, (int) outerJoinPointY);
                arcTo(Math.PI + sbJoinAngle, Math.PI * 3 / 2,
                        ellipseSBX, ellipseSBY, ellipseSBRadiusX, ellipseSBRadiusY);
            }  else {
                moveTo(0, 0);
                if (before.isCollapseOuter()) {
                    if (start.isCollapseOuter()) {
                        sx1a -= start.getClippedWidth();
                    }
                    if (end.isCollapseOuter()) {
                        ex1a += end.getClippedWidth();
                    }
                    lineTo(sx1a, outery);
                    lineTo(ex1a, outery);
                }
            }
            if (ellipseBERadiusX != 0 && ellipseBERadiusY != 0) {
                final double[] outerJoinMetrics = getCornerBorderJoinMetrics(
                        ellipseBERadiusX, ellipseBERadiusY,  ex1 - ex2, innery);
                final double beJoinAngle = ex1 == ex2 ? Math.PI / 2 : Math.PI / 2 - outerJoinMetrics[2];
                lineTo(ellipseBEX, 0);
                arcTo(Math.PI * 3 / 2 , Math.PI * 3 / 2 + beJoinAngle,
                        ellipseBEX, ellipseBEY, ellipseBERadiusX, ellipseBERadiusY);
                if (ellipseBEX < ex2 && ellipseBEY > innery) {
                    final double[] innerJoinMetrics = getCornerBorderJoinMetrics(
                            (double) ex2 - ellipseBEX, (double) ellipseBEY - innery, ex1 - ex2, innery);
                    final double innerJoinPointX = innerJoinMetrics[0];
                    final double innerJoinPointY = innerJoinMetrics[1];
                    final double beInnerJoinAngle = Math.PI / 2 - innerJoinMetrics[2];
                    lineTo((int) (ex2 - innerJoinPointX), (int) (innerJoinPointY + innery));
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
                            ellipseSBRadiusY - innery, sx2, innery);
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
                        before.getStyle(), before.getColor());
            } else {
                int innerFillY = Math.max(Math.max(ellipseBEY, ellipseSBY), innery);
                drawBorderLine(sx1a, outery, ex1a, innerFillY, true, true,
                        before.getStyle(), before.getColor());
            }
            restoreGraphicsState();
        }
    }

    private static BorderSegment borderSegmentForBefore(BorderProps before) {
        return AbstractBorderSegment.asBorderSegment(before);
    }

    private static BorderSegment borderSegmentForAfter(BorderProps after) {
        return AbstractBorderSegment.asFlippedBorderSegment(after);
    }

    private static BorderSegment borderSegmentForStart(BorderProps start) {
        return AbstractBorderSegment.asFlippedBorderSegment(start);
    }

    private static BorderSegment borderSegmentForEnd(BorderProps end) {
        return AbstractBorderSegment.asBorderSegment(end);
    }

    private interface BorderSegment {

        Color getColor();

        int getStyle();

        int getWidth();

        int getClippedWidth();

        int getRadiusStart();

        int getRadiusEnd();

        boolean isCollapseOuter();

        boolean isSpecified();
    }

    private abstract static class AbstractBorderSegment implements BorderSegment {

        private static BorderSegment asBorderSegment(BorderProps borderProps) {
            return borderProps == null ? NullBorderSegment.INSTANCE : new WrappingBorderSegment(borderProps);
        }

        private static BorderSegment asFlippedBorderSegment(BorderProps borderProps) {
            return borderProps == null ? NullBorderSegment.INSTANCE : new FlippedBorderSegment(borderProps);
        }

        public boolean isSpecified() {
            return !(this instanceof NullBorderSegment);
        }

        private static class WrappingBorderSegment extends AbstractBorderSegment {

            protected final BorderProps borderProps;

            private final int clippedWidth;

            WrappingBorderSegment(BorderProps borderProps) {
                this.borderProps = borderProps;
                clippedWidth = BorderProps.getClippedWidth(borderProps);
            }

            public int getStyle() {
                return borderProps.style;
            }

            public Color getColor() {
                return borderProps.color;
            }

            public int getWidth() {
                return borderProps.width;
            }

            public int getClippedWidth() {
                return clippedWidth;
            }
            public boolean isCollapseOuter() {
                return borderProps.isCollapseOuter();
            }

            public int getRadiusStart() {
                return borderProps.getRadiusStart();
            }

            public int getRadiusEnd() {
                return borderProps.getRadiusEnd();
            }
        }

        private static class FlippedBorderSegment extends WrappingBorderSegment {

            FlippedBorderSegment(BorderProps borderProps) {
                super(borderProps);
            }

            public int getRadiusStart() {
                return borderProps.getRadiusEnd();
            }

            public int getRadiusEnd() {
                return borderProps.getRadiusStart();
            }
        }

        private static final class NullBorderSegment extends AbstractBorderSegment {

            public static final NullBorderSegment INSTANCE = new NullBorderSegment();

            private NullBorderSegment() {
            }

            public int getWidth() {
                return 0;
            }

            public int getClippedWidth() {
                return 0;
            }

            public int getRadiusStart() {
                return 0;
            }

            public int getRadiusEnd() {
                return 0;
            }

            public boolean isCollapseOuter() {
                return false;
            }

            public Color getColor() {
                throw new UnsupportedOperationException();
            }

            public int getStyle() {
                throw new UnsupportedOperationException();
            }

            public boolean isSpecified() {
                return false;
            }
        }
    }

    private double[] getCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY, double xWidth,
            double yWidth) {
        if (xWidth > 0) {
            return getCornerBorderJoinMetrics(ellipseCenterX, ellipseCenterY, yWidth / xWidth);
        } else {
            return new double[]{0, ellipseCenterY, 0};
        }
    }

    private double[] getCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY,
            double borderWidthRatio) {
        double x = ellipseCenterY * ellipseCenterX * (
                ellipseCenterY + ellipseCenterX * borderWidthRatio
                - Math.sqrt(2d * ellipseCenterX * ellipseCenterY * borderWidthRatio)
                ) / (ellipseCenterY * ellipseCenterY
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
        BorderSegment before = borderSegmentForBefore(bpsBefore);
        BorderSegment after = borderSegmentForAfter(bpsAfter);
        BorderSegment start = borderSegmentForStart(bpsStart);
        BorderSegment end = borderSegmentForEnd(bpsEnd);
        int startx = rect.x;
        int starty = rect.y;
        int width = rect.width;
        int height = rect.height;
        double correctionFactor = calculateCornerCorrectionFactor(width + start.getWidth() + end.getWidth(),
                height + before.getWidth() + after.getWidth(), bpsBefore, bpsAfter, bpsStart, bpsEnd);
        Corner cornerBeforeEnd = Corner.createBeforeEndCorner(before, end, correctionFactor);
        Corner cornerEndAfter = Corner.createEndAfterCorner(end, after, correctionFactor);
        Corner cornerAfterStart = Corner.createAfterStartCorner(after, start, correctionFactor);
        Corner cornerStartBefore = Corner.createStartBeforeCorner(start, before, correctionFactor);
        new PathPainter(startx + cornerStartBefore.radiusX, starty)
                .lineHorizTo(width - cornerStartBefore.radiusX - cornerBeforeEnd.radiusX)
                .drawCorner(cornerBeforeEnd)
                .lineVertTo(height - cornerBeforeEnd.radiusY - cornerEndAfter.radiusY)
                .drawCorner(cornerEndAfter)
                .lineHorizTo(cornerEndAfter.radiusX + cornerAfterStart.radiusX - width)
                .drawCorner(cornerAfterStart)
                .lineVertTo(cornerAfterStart.radiusY + cornerStartBefore.radiusY - height)
                .drawCorner(cornerStartBefore);
        clip();
    }



    /**
     * The four corners
     *      SB - Start-Before
     *      BE - Before-End
     *      EA - End-After
     *      AS - After-Start
     *
     * 0 --> x
     * |
     * v
     * y
     *
     *  SB      BE
     *    *----*
     *    |    |
     *    |    |
     *    *----*
     *  AS      EA
     *
     */
    private enum CornerAngles {
        /** The before-end angles */
        BEFORE_END(Math.PI * 3 / 2, 0),
        /** The end-after angles */
        END_AFTER(0, Math.PI / 2),
        /** The after-start angles*/
        AFTER_START(Math.PI / 2, Math.PI),
        /** The start-before angles */
        START_BEFORE(Math.PI, Math.PI * 3 / 2);

        /** Angle of the start of the corner arch relative to the x-axis in the counter-clockwise direction */
        private final double start;

        /** Angle of the end of the corner arch relative to the x-axis in the counter-clockwise direction */
        private final double end;

        CornerAngles(double start, double end) {
            this.start = start;
            this.end = end;
        }

    }

    private static final class Corner {

        private static final Corner SQUARE = new Corner(0, 0, null, 0, 0, 0, 0);

        /** The radius of the elliptic corner in the x direction */
        protected final int radiusX;

        /** The radius of the elliptic corner in the y direction */
        protected final int radiusY;

        /** The start and end angles of the corner ellipse */
        private final CornerAngles angles;

        /** The offset in the x direction of the center of the ellipse relative to the starting point */
        private final int centerX;

        /** The offset in the y direction of the center of the ellipse relative to the starting point */
        private final int centerY;

        /** The value in the x direction that the corner extends relative to the starting point */
        private final int incrementX;

        /** The value in the y direction that the corner extends relative to the starting point */
        private final int incrementY;

        private Corner(int radiusX, int radiusY, CornerAngles angles, int ellipseOffsetX,
                int ellipseOffsetY, int incrementX, int incrementY) {
            this.radiusX = radiusX;
            this.radiusY = radiusY;
            this.angles = angles;
            this.centerX = ellipseOffsetX;
            this.centerY = ellipseOffsetY;
            this.incrementX = incrementX;
            this.incrementY = incrementY;
        }

        private static int extentFromRadiusStart(BorderSegment border, double correctionFactor) {
            return extentFromRadius(border.getRadiusStart(), border, correctionFactor);
        }

        private static int extentFromRadiusEnd(BorderSegment border, double correctionFactor) {
            return extentFromRadius(border.getRadiusEnd(), border, correctionFactor);
        }

        private static int extentFromRadius(int radius, BorderSegment border, double correctionFactor) {
            return Math.max((int) (radius * correctionFactor) - border.getWidth(), 0);
        }

        public static Corner createBeforeEndCorner(BorderSegment before, BorderSegment end,
                double correctionFactor) {
            int width = end.getRadiusStart();
            int height = before.getRadiusEnd();
            if (width == 0 || height == 0) {
                return SQUARE;
            }
            int x = extentFromRadiusStart(end, correctionFactor);
            int y = extentFromRadiusEnd(before, correctionFactor);
            return new Corner(x, y, CornerAngles.BEFORE_END, 0, y, x, y);
        }

        public static Corner createEndAfterCorner(BorderSegment end, BorderSegment after,
                double correctionFactor) {
            int width = end.getRadiusEnd();
            int height = after.getRadiusStart();
            if (width == 0 || height == 0) {
                return SQUARE;
            }
            int x = extentFromRadiusEnd(end, correctionFactor);
            int y = extentFromRadiusStart(after, correctionFactor);
            return new Corner(x, y, CornerAngles.END_AFTER, -x, 0, -x, y);
        }

        public static Corner createAfterStartCorner(BorderSegment after, BorderSegment start,
                double correctionFactor) {
            int width = start.getRadiusStart();
            int height = after.getRadiusEnd();
            if (width == 0 || height == 0) {
                return SQUARE;
            }
            int x = extentFromRadiusStart(start, correctionFactor);
            int y = extentFromRadiusEnd(after, correctionFactor);
            return new Corner(x, y, CornerAngles.AFTER_START, 0, -y, -x, -y);
        }

        public static Corner createStartBeforeCorner(BorderSegment start, BorderSegment before,
                double correctionFactor) {
            int width = start.getRadiusEnd();
            int height = before.getRadiusStart();
            if (width == 0 || height == 0) {
                return SQUARE;
            }
            int x = extentFromRadiusEnd(start, correctionFactor);
            int y = extentFromRadiusStart(before, correctionFactor);
            return new Corner(x, y, CornerAngles.START_BEFORE, x, 0, x, -y);
        }
    }

    /**
     * This is a helper class for constructing curves composed of move, line and arc operations.  Coordinates
     * are relative to the terminal point of the previous operation
     */
    private final class PathPainter {

        /** Current x position */
        private int x;

        /** Current y position */
        private int y;

        PathPainter(int x, int y) throws IOException {
            moveTo(x, y);
        }

        private void moveTo(int x, int y) throws IOException {
            this.x += x;
            this.y += y;
            BorderPainter.this.moveTo(this.x, this.y);
        }

        public PathPainter lineTo(int x, int y) throws IOException {
            this.x += x;
            this.y += y;
            BorderPainter.this.lineTo(this.x, this.y);
            return this;
        }

        public PathPainter lineHorizTo(int x) throws IOException {
            return lineTo(x, 0);
        }

        public PathPainter lineVertTo(int y) throws IOException {
            return lineTo(0, y);
        }

        PathPainter drawCorner(Corner corner) throws IOException {
            if (corner.radiusX == 0 && corner.radiusY == 0) {
                return this;
            }
            if (corner.radiusX == 0 || corner.radiusY == 0) {
                x += corner.incrementX;
                y += corner.incrementY;
                BorderPainter.this.lineTo(x, y);
                return this;
            }
            BorderPainter.this.arcTo(corner.angles.start, corner.angles.end, x + corner.centerX,
                    y + corner.centerY, corner.radiusX, corner.radiusY);
            x += corner.incrementX;
            y += corner.incrementY;
            return this;
        }
    }

    /**
     * Calculate the correction factor to handle over-sized elliptic corner radii.
     *
     * @param width the border width
     * @param height the border height
     * @param before the before border properties
     * @param after the after border properties
     * @param start the start border properties
     * @param end the end border properties
     *
     */
    protected static double calculateCornerCorrectionFactor(int width, int height, BorderProps before,
            BorderProps after, BorderProps start, BorderProps end) {
        return calculateCornerScaleCorrection(width, height, borderSegmentForBefore(before),
                borderSegmentForAfter(after), borderSegmentForStart(start), borderSegmentForEnd(end));
    }

    /**
     * Calculate the scaling factor to handle over-sized elliptic corner radii.
     *
     * @param width the border width
     * @param height the border height
     * @param before the before border segment
     * @param after the after border segment
     * @param start the start border segment
     * @param end the end border segment
     */
    protected static double calculateCornerScaleCorrection(int width, int height, BorderSegment before,
            BorderSegment after, BorderSegment start, BorderSegment end) {
        return CornerScaleCorrectionCalculator.calculate(width, height, before, after, start, end);
    }

    private static final class CornerScaleCorrectionCalculator {

        private double correctionFactor = 1;

        private CornerScaleCorrectionCalculator(int width, int height,
                BorderSegment before, BorderSegment after,
                BorderSegment start, BorderSegment end) {
            calculateForSegment(width, start, before, end);
            calculateForSegment(height, before, end, after);
            calculateForSegment(width, end, after, start);
            calculateForSegment(height, after, start, before);
        }

        public static double calculate(int width, int height,
                BorderSegment before, BorderSegment after,
                BorderSegment start, BorderSegment end) {
            return new CornerScaleCorrectionCalculator(width, height, before, after, start, end)
                    .correctionFactor;
        }

        private void calculateForSegment(int width, BorderSegment bpsStart, BorderSegment bpsBefore,
                BorderSegment bpsEnd) {
            if (bpsBefore.isSpecified()) {
                double ellipseExtent = bpsStart.getRadiusEnd() + bpsEnd.getRadiusStart();
                if (ellipseExtent > 0) {
                    double thisCorrectionFactor = width / ellipseExtent;
                    if (thisCorrectionFactor < correctionFactor) {
                        correctionFactor = thisCorrectionFactor;
                    }
                }
            }
        }
    }

    private void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz, boolean startOrBefore,
            int style, Color color) throws IOException {
        graphicsPainter.drawBorderLine(x1, y1, x2, y2, horz, startOrBefore, style, color);
    }

    private void moveTo(int x, int y) throws IOException {
        graphicsPainter.moveTo(x, y);
    }

    private void lineTo(int x, int y) throws IOException {
        graphicsPainter.lineTo(x, y);
    }

    private void arcTo(final double startAngle, final double endAngle, final int cx, final int cy,
            final int width, final int height) throws IOException {
        graphicsPainter.arcTo(startAngle, endAngle, cx, cy, width, height);
    }

    private void rotateCoordinates(double angle) throws IOException {
        graphicsPainter.rotateCoordinates(angle);
    }

    private void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
        graphicsPainter.translateCoordinates(xTranslate, yTranslate);
    }

    private void closePath() throws IOException {
        graphicsPainter.closePath();
    }

    private void clip() throws IOException {
        graphicsPainter.clip();
    }

    private void saveGraphicsState() throws IOException {
        graphicsPainter.saveGraphicsState();
    }

    private void restoreGraphicsState() throws IOException {
        graphicsPainter.restoreGraphicsState();
    }

}
