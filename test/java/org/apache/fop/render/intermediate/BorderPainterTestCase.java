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

package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.apache.fop.fo.Constants;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.BorderProps.Mode;

public class BorderPainterTestCase {

    private static final BorderProps BORDER_PROPS = new BorderProps(Constants.EN_SOLID, 10, 50, 50,
            Color.BLACK, BorderProps.Mode.SEPARATE);

    @Test
    public void clipBackground() throws Exception {
        // Rectangular borders
        test(new ClipBackgroundTester(0, 0, 10, 10));
        test(new ClipBackgroundTester(5, 10, 10, 10));
        test(new ClipBackgroundTester(0, 0, 10, 10).setBorderWidth(1));
        test(new ClipBackgroundTester(0, 0, 10, 10).beforeBorder().setWidth(10).tester());
        // Rounded corners
        test(new ClipBackgroundTester(0, 0, 10, 10).setEndBefore(1, 1));
        test(new ClipBackgroundTester(0, 0, 10, 10).setEndAfter(1, 1));
        test(new ClipBackgroundTester(0, 0, 10, 10).setStartAfter(1, 1));
        test(new ClipBackgroundTester(0, 0, 10, 10).setStartBefore(1, 1));
        test(new ClipBackgroundTester(0, 0, 100, 100)
                .setCornerRadii(10)
                .beforeBorder().setWidth(5).tester()
                .startBorder().setWidth(5).tester());
        test(new ClipBackgroundTester(0, 0, 100, 100)
                .setCornerRadii(10)
                .beforeBorder().setWidth(10).tester()
                .startBorder().setWidth(10).tester());
        test(new ClipBackgroundTester(0, 0, 100, 100)
                .setCornerRadii(10)
                .beforeBorder().setWidth(5).tester());
        test(new ClipBackgroundTester(0, 0, 100, 100)
                .setCornerRadii(10)
                .setStartBefore(10, 10)
                .beforeBorder().setWidth(10).tester());
    }

    private void test(BorderPainterTester<?> tester) throws IOException {
        tester.test();
    }

    @Test (expected = IFException.class)
    public void drawBordersThrowsIFException() throws Exception {
        GraphicsPainter graphicsPainter = mock(GraphicsPainter.class);
        doThrow(new IOException()).when(graphicsPainter).saveGraphicsState();
        new BorderPainter(graphicsPainter).drawBorders(new Rectangle(0, 0, 1000, 1000), BORDER_PROPS,
                BORDER_PROPS, BORDER_PROPS, BORDER_PROPS, Color.WHITE);
    }

    @Test
    public void testDrawRectangularBorders() throws IOException {
        test(new DrawRectangularBordersTester(0, 0, 1000, 1000).setBorderWidth(10));
        test(new DrawRectangularBordersTester(0, 0, 1000, 1000));
        test(new DrawRectangularBordersTester(0, 0, 1000, 1000).setBorderWidth(10)
                .beforeBorder().setWidth(0).tester());
    }

    @Test
    public void testDrawRectangularBordersWithNullBorders() throws IOException, IFException {
        GraphicsPainter graphicsPainter = mock(GraphicsPainter.class);
        BorderProps nullBorderProps = null;
        new BorderPainter(graphicsPainter).drawRectangularBorders(new Rectangle(0, 0, 1000, 1000),
                nullBorderProps, nullBorderProps, nullBorderProps, nullBorderProps);
        verifyZeroInteractions(graphicsPainter);
    }

    @Test
    public void drawRoundedBorders() throws Exception {
        test(new DrawRoundedBordersTester(0, 0, 10, 10).setBorderWidth(10));
        test(new DrawRoundedBordersTester(0, 0, 10, 10).beforeBorder().setWidth(10).tester());
        test(new DrawRoundedBordersTester(0, 0, 10, 10).setBorderWidth(10).setCornerRadii(5)
                .beforeBorder().setWidth(0).tester());
        test(new DrawRoundedBordersTester(0, 0, 10, 10)
                .beforeBorder().setWidth(10).tester().endBorder().setWidth(10).tester());
        test(new DrawRoundedBordersTester(0, 0, 100, 100).setBorderWidth(15).setCornerRadii(10));
        test(new DrawRoundedBordersTester(0, 0, 100, 100).setBorderWidth(15).setCornerRadii(10)
                .beforeBorder().setWidth(5).tester());
        test(new DrawRoundedBordersTester(0, 0, 60, 60).setBorderWidth(4).setCornerRadii(30));
    }

    @Test
    public void testDrawRoundedBordersWithNullBorders() throws IOException, IFException {
        GraphicsPainter graphicsPainter = mock(GraphicsPainter.class);
        BorderProps nullBorderProps = null;
        new BorderPainter(graphicsPainter).drawRoundedBorders(new Rectangle(0, 0, 1000, 1000),
                nullBorderProps, nullBorderProps, nullBorderProps, nullBorderProps);
        verifyZeroInteractions(graphicsPainter);
    }

    @Test
    public void testCalculateCornerCorrectionFactor() {
        calculateCornerCorrectionFactorHelper(30000, 500000);
        calculateCornerCorrectionFactorHelper(30000, 10000);
    }

    private void calculateCornerCorrectionFactorHelper(int radius, int rectWidth) {
        BorderProps borderProps = new BorderProps(Constants.EN_SOLID, 4000, radius, radius, Color.BLACK,
                BorderProps.Mode.SEPARATE);
        int rectHeight = rectWidth + 100;
        double expected =  (2 * radius > rectWidth) ? (double) rectWidth / (2 * radius) : 1.0;
        double actual = BorderPainter.calculateCornerCorrectionFactor(rectWidth, rectHeight, borderProps,
                borderProps, borderProps, borderProps);
        assertEquals(expected, actual, 0);
    }

    private abstract static class BorderPainterTester<T extends BorderPainterTester<?>> {

        protected final Rectangle borderExtent;

        protected BorderProps before;

        protected BorderProps after;

        protected BorderProps start;

        protected BorderProps end;

        protected final GraphicsPainter graphicsPainter;

        protected final BorderPainter sut;

        private final BorderPropsBuilder<T> beforeBuilder;

        private final BorderPropsBuilder<T> afterBuilder;

        private final BorderPropsBuilder<T> startBuilder;

        private final BorderPropsBuilder<T> endBuilder;

        public BorderPainterTester(int xOrigin, int yOrigin, int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Cannot test degenerate borders");
            }
            beforeBuilder = new BorderPropsBuilder<T>(getThis());
            afterBuilder = new BorderPropsBuilder<T>(getThis());
            startBuilder = new BorderPropsBuilder<T>(getThis());
            endBuilder = new BorderPropsBuilder<T>(getThis());
            this.borderExtent = new Rectangle(xOrigin, yOrigin, width, height);
            this.graphicsPainter = mock(GraphicsPainter.class);
            this.sut = new BorderPainter(graphicsPainter);
        }

        protected abstract T getThis();

        public BorderPropsBuilder<T> beforeBorder() {
            return beforeBuilder;
        }

        public BorderPropsBuilder<T> afterBorder() {
            return afterBuilder;
        }

        public BorderPropsBuilder<T> startBorder() {
            return startBuilder;
        }

        public BorderPropsBuilder<T> endBorder() {
            return endBuilder;
        }

        public T setBorderWidth(int width) {
            beforeBuilder.setWidth(width);
            endBuilder.setWidth(width);
            afterBuilder.setWidth(width);
            startBuilder.setWidth(width);
            return getThis();
        }

        public T setCornerRadii(int radius) {
            return setCornerRadii(radius, radius);
        }

        public T setCornerRadii(int xRadius, int yRadius) {
            setStartBefore(xRadius, yRadius);
            setEndBefore(xRadius, yRadius);
            setEndAfter(xRadius, yRadius);
            setStartAfter(xRadius, yRadius);
            return getThis();
        }

        public T setStartBefore(int xRadius, int yRadius) {
            startBuilder.setRadiusStart(xRadius);
            beforeBuilder.setRadiusStart(yRadius);
            return getThis();
        }

        public T setEndBefore(int xRadius, int yRadius) {
            endBuilder.setRadiusStart(xRadius);
            beforeBuilder.setRadiusEnd(yRadius);
            return getThis();
        }

        public T setEndAfter(int xRadius, int yRadius) {
            endBuilder.setRadiusEnd(xRadius);
            afterBuilder.setRadiusEnd(yRadius);
            return getThis();
        }

        public T setStartAfter(int xRadius, int yRadius) {
            startBuilder.setRadiusEnd(xRadius);
            afterBuilder.setRadiusStart(yRadius);
            return getThis();
        }

        public final void test() throws IOException {
            before = beforeBuilder.build();
            after = afterBuilder.build();
            end = endBuilder.build();
            start = startBuilder.build();
            testMethod();
        }

        protected abstract void testMethod() throws IOException;

        protected static int numberOfNonZeroBorders(BorderProps first, BorderProps... borders) {
            int i = first.width == 0 ? 0 : 1;
            for (BorderProps borderProp : borders) {
                if (borderProp.width > 0) {
                    i++;
                }
            }
            return i;
        }

        protected int numberOfNonZeroBorders() {
            return numberOfNonZeroBorders(before, end, after, start);
        }

    }

    private static class BorderPropsBuilder<T extends BorderPainterTester<?>> {

        private final int style = 0;

        private final Color color = null;

        private final Mode mode = BorderProps.Mode.SEPARATE;

        private int width;

        private int radiusStart;

        private int radiusEnd;

        private final T tester;

        public BorderPropsBuilder(T tester) {
            this.tester = tester;
        }

        public T tester() {
            return tester;
        }

        public BorderPropsBuilder<T> setWidth(int width) {
            this.width = width;
            return this;
        }

        public BorderPropsBuilder<T> setRadiusStart(int radiusStart) {
            this.radiusStart = radiusStart;
            return this;
        }

        public BorderPropsBuilder<T> setRadiusEnd(int radiusEnd) {
            this.radiusEnd = radiusEnd;
            return this;
        }

        public BorderProps build() {
            return new BorderProps(style, width, radiusStart, radiusEnd, color, mode);
        }
    }

    private static final class DrawRectangularBordersTester
            extends BorderPainterTester<DrawRectangularBordersTester> {

        public DrawRectangularBordersTester(int xOrigin, int yOrigin, int width, int height)
                throws IOException {
            super(xOrigin, yOrigin, width, height);
        }

        public DrawRectangularBordersTester setStartBefore(int xRadius, int yRadius) {
            return notSupported();
        }

        public DrawRectangularBordersTester setEndBefore(int xRadius, int yRadius) {
            return notSupported();
        }

        public DrawRectangularBordersTester setEndAfter(int xRadius, int yRadius) {
            return notSupported();
        }

        public DrawRectangularBordersTester setStartAfter(int xRadius, int yRadius) {
            return notSupported();
        }

        private DrawRectangularBordersTester notSupported() {
            throw new UnsupportedOperationException();
        }

        public void testMethod() throws IOException {
            sut.drawRectangularBorders(borderExtent, before, after, start, end);
            verifyDrawing();
        }

        private void verifyDrawing() throws IOException {
            final int rectX = borderExtent.x;
            final int rectY = borderExtent.y;
            final int rectWidth = borderExtent.width;
            final int rectHeight = borderExtent.height;
            if (before.width > 0) {
                verify(graphicsPainter).moveTo(rectX, rectY);
                verify(graphicsPainter).lineTo(rectWidth, rectY);
                verify(graphicsPainter, times(numberOfNonZeroBorders(before, end)))
                        .lineTo(rectWidth - end.width, rectY + before.width);
                verify(graphicsPainter, times(numberOfNonZeroBorders(before, start)))
                        .lineTo(rectX + start.width, rectY + before.width);
            }
            if (end.width > 0) {
                verify(graphicsPainter).moveTo(rectWidth, rectY);
                verify(graphicsPainter).lineTo(rectWidth, rectHeight);
                verify(graphicsPainter, times(numberOfNonZeroBorders(end, after)))
                        .lineTo(rectWidth - end.width, rectHeight - after.width);
                verify(graphicsPainter, times(numberOfNonZeroBorders(end, before)))
                        .lineTo(rectWidth - end.width, rectY + before.width);
            }
            if (after.width > 0) {
                verify(graphicsPainter).moveTo(rectWidth, rectHeight);
                verify(graphicsPainter).lineTo(rectX, rectHeight);
                verify(graphicsPainter, times(numberOfNonZeroBorders(after, end)))
                        .lineTo(rectX + start.width, rectHeight - after.width);
                verify(graphicsPainter, times(numberOfNonZeroBorders(after, start)))
                        .lineTo(rectWidth - end.width, rectHeight - after.width);
            }
            if (start.width > 0) {
                verify(graphicsPainter).moveTo(rectX, rectHeight);
                verify(graphicsPainter).lineTo(rectX, rectY);
                verify(graphicsPainter, times(numberOfNonZeroBorders(start, before)))
                        .lineTo(rectX + start.width, rectY + before.width);
                verify(graphicsPainter, times(numberOfNonZeroBorders(start, after)))
                        .lineTo(rectX + start.width, rectHeight - after.width);
            }
            int numBorders = numberOfNonZeroBorders();
            verify(graphicsPainter, times(numBorders)).saveGraphicsState();
            verify(graphicsPainter, times(numBorders)).closePath();
            verify(graphicsPainter, times(numBorders)).restoreGraphicsState();
            verify(graphicsPainter, times(numBorders)).clip();
        }

        @Override
        protected DrawRectangularBordersTester getThis() {
            return this;
        }
    }

    private static final class DrawRoundedBordersTester extends BorderPainterTester<DrawRoundedBordersTester> {

        public DrawRoundedBordersTester(int xOrigin, int yOrigin, int width, int height) throws IOException {
            super(xOrigin, yOrigin, width, height);
        }

        public void testMethod() throws IOException {
            sut.drawRoundedBorders(borderExtent, before, after, start, end);
            verifyDrawing();
        }

        private void verifyDrawing() throws IOException {
            int numBorders = numberOfNonZeroBorders();
            final int rectWidth = borderExtent.width;
            final int rectHeight = borderExtent.height;
            if (before.width > 0) {
                verify(graphicsPainter, atLeastOnce()).lineTo(rectWidth - end.getRadiusStart(), 0);
                verify(graphicsPainter, atLeastOnce()).lineTo(calcLineEnd(start.width, before.width,
                        start.getRadiusStart(), before.getRadiusStart()), before.width);
            }
            if (end.width > 0) {
                verify(graphicsPainter, atLeastOnce()).lineTo(rectHeight - after.getRadiusEnd(), 0);
                verify(graphicsPainter, atLeastOnce()).lineTo(calcLineEnd(before.width, end.width,
                        before.getRadiusEnd(), end.getRadiusStart()), end.width);
            }
            if (after.width > 0) {
                verify(graphicsPainter, atLeastOnce()).lineTo(rectWidth - start.getRadiusEnd(), 0);
                verify(graphicsPainter, atLeastOnce()).lineTo(calcLineEnd(start.width, after.width,
                        start.getRadiusEnd(), after.getRadiusStart()), after.width);
            }
            if (start.width > 0) {
                verify(graphicsPainter, atLeastOnce()).lineTo(rectHeight - after.getRadiusStart(), 0);
                verify(graphicsPainter, atLeastOnce()).lineTo(calcLineEnd(before.width, start.width,
                        before.getRadiusStart(), before.getRadiusStart()), start.width);
            }
            // verify the drawing of the symmetric rounded corners (the ones that are a quarter of a circle)
            // verification is restricted to those since it is too complex in the general case
            if (before.width == end.width && before.getRadiusStart() == before.getRadiusEnd()
                    && end.getRadiusStart() == end.getRadiusEnd()
                    && before.getRadiusEnd() == end.getRadiusStart() && end.getRadiusStart() > 0) {
                verify(graphicsPainter, atLeastOnce()).arcTo(Math.PI * 5 / 4, Math.PI * 3 / 2,
                        before.getRadiusStart(), end.getRadiusEnd(), before.getRadiusStart(),
                        end.getRadiusEnd());
            }
            if (end.width == after.width && end.getRadiusStart() == end.getRadiusEnd()
                    && after.getRadiusStart() == after.getRadiusEnd()
                    && end.getRadiusEnd() == after.getRadiusStart() && after.getRadiusStart() > 0) {
                verify(graphicsPainter, atLeastOnce()).arcTo(Math.PI * 5 / 4, Math.PI * 3 / 2,
                        end.getRadiusStart(), after.getRadiusEnd(), end.getRadiusStart(),
                        after.getRadiusEnd());
            }
            if (after.width == start.width && after.getRadiusStart() == after.getRadiusEnd()
                    && start.getRadiusStart() == start.getRadiusEnd()
                    && after.getRadiusEnd() == start.getRadiusStart() && start.getRadiusStart() > 0) {
                verify(graphicsPainter, atLeastOnce()).arcTo(Math.PI * 5 / 4, Math.PI * 3 / 2,
                        after.getRadiusStart(), start.getRadiusEnd(), after.getRadiusStart(),
                        start.getRadiusEnd());
            }
            if (start.width == before.width && start.getRadiusStart() == start.getRadiusEnd()
                    && before.getRadiusStart() == before.getRadiusEnd()
                    && start.getRadiusEnd() == before.getRadiusStart() && before.getRadiusStart() > 0) {
                verify(graphicsPainter, atLeastOnce()).arcTo(Math.PI * 5 / 4, Math.PI * 3 / 2,
                        start.getRadiusStart(), before.getRadiusEnd(), start.getRadiusStart(),
                        before.getRadiusEnd());
            }
            verify(graphicsPainter, times(numBorders)).saveGraphicsState();
            verify(graphicsPainter, times(numBorders)).closePath();
            verify(graphicsPainter, times(numBorders)).restoreGraphicsState();
            verify(graphicsPainter, times(numBorders)).clip();
        }

        private int calcLineEnd(int xWidth, int yWidth, int xRadius, int yRadius) {
            return yWidth > yRadius ? yWidth : xWidth > 0 ? Math.max(xRadius, xWidth) : 0;
        }

        @Override
        protected DrawRoundedBordersTester getThis() {
            return this;
        }

    }

    private static final class ClipBackgroundTester extends BorderPainterTester<ClipBackgroundTester> {

        public ClipBackgroundTester(int xOrigin, int yOrigin, int width, int height) throws IOException {
            super(xOrigin, yOrigin, width, height);
        }

        public void testMethod() throws IOException {
            sut.clipBackground(borderExtent, before, after, start, end);
            verifyClipping();
        }

        private void verifyClipping() throws IOException {
            int xOrigin = borderExtent.x;
            int yOrigin = borderExtent.y;
            int xEnd = xOrigin + borderExtent.width;
            int yEnd = yOrigin + borderExtent.height;

            Corner startBeforeCorner = Corner.createStartBeforeCorner(getInnerRadiusStart(start),
                    getInnerRadiusStart(before));
            Corner endBeforeCorner = Corner.createEndBeforeCorner(getInnerRadiusStart(end), getRadiusEnd(before));
            Corner endAfterCorner = Corner.createEndAfterCorner(getRadiusEnd(end), getRadiusEnd(after));
            Corner startAfterCorner = Corner.createStartAfterCorner(getRadiusEnd(start),
                    getInnerRadiusStart(after));
            verify(graphicsPainter, times(1)).moveTo(xOrigin + startBeforeCorner.xRadius, yOrigin);
            verify(graphicsPainter, times(1)).lineTo(xEnd - endBeforeCorner.xRadius, yOrigin);
            endBeforeCorner.verifyCornerDrawn(graphicsPainter, xEnd - endBeforeCorner.xRadius,
                    yOrigin + endBeforeCorner.yRadius);
            verify(graphicsPainter, times(1)).lineTo(xEnd, yEnd - endAfterCorner.yRadius);
            endAfterCorner.verifyCornerDrawn(graphicsPainter, xEnd - endAfterCorner.xRadius,
                    yEnd - endAfterCorner.yRadius);
            verify(graphicsPainter, times(1)).lineTo(xOrigin + startAfterCorner.xRadius, yEnd);
            startAfterCorner.verifyCornerDrawn(graphicsPainter, xOrigin + startAfterCorner.xRadius,
                    yEnd - startAfterCorner.yRadius);
            verify(graphicsPainter, times(1)).lineTo(xOrigin, yOrigin + startBeforeCorner.yRadius);
            startBeforeCorner.verifyCornerDrawn(graphicsPainter, xOrigin + startBeforeCorner.xRadius,
                    yOrigin + startBeforeCorner.yRadius);
            verify(graphicsPainter, times(1)).clip();
        }

        private int getInnerRadiusStart(BorderProps borderProps) {
            return getInnerRadius(borderProps.getRadiusStart(), borderProps.width);
        }

        private int getRadiusEnd(BorderProps borderProps) {
            return getInnerRadius(borderProps.getRadiusEnd(), borderProps.width);
        }

        private int getInnerRadius(int radius, int borderWidth) {
            return Math.max(radius - borderWidth, 0);
        }

        private static class Corner {

            public final int xRadius;

            public final int yRadius;

            private final double startAngle;

            private final double endAngle;

            public Corner(int xRadius, int yRadius, double startAngle, double endAngle) {
                this.xRadius = xRadius;
                this.yRadius = yRadius;
                this.startAngle = startAngle;
                this.endAngle = endAngle;
            }

            public static Corner createStartBeforeCorner(int xRadius, int yRadius) {
                return new Corner(xRadius, yRadius, Math.PI, Math.PI * 3 / 2);
            }

            public static Corner createEndBeforeCorner(int xRadius, int yRadius) {
                return new Corner(xRadius, yRadius, Math.PI * 3 / 2, 0);
            }

            public static Corner createEndAfterCorner(int xRadius, int yRadius) {
                return new Corner(xRadius, yRadius, 0, Math.PI / 2);
            }

            public static Corner createStartAfterCorner(int xRadius, int yRadius) {
                return new Corner(xRadius, yRadius, Math.PI / 2, Math.PI);
            }

            public void verifyCornerDrawn(GraphicsPainter graphicsPainter, int xCenter, int yCenter)
                    throws IOException {
                if (xRadius != 0 && yRadius != 0) {
                    verify(graphicsPainter, times(1)).arcTo(startAngle, endAngle,
                            xCenter, yCenter, xRadius, yRadius);
                } else {
                    verify(graphicsPainter, never()).arcTo(startAngle, endAngle,
                            xCenter, yCenter, xRadius, yRadius);
                }
            }
        }

        @Override
        protected ClipBackgroundTester getThis() {
            return this;
        }
    }


}