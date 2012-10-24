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

import java.io.IOException;

public class ArcToBezierCurveTransformer {

    private final BezierCurvePainter bezierCurvePainter;

    public ArcToBezierCurveTransformer(BezierCurvePainter bezierCurvePainter) {
        this.bezierCurvePainter = bezierCurvePainter;
    }

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
    public void arcTo(final double startAngle, final double endAngle, final int cx, final int cy,
            final int width, final int height) throws IOException {

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
        bezierCurvePainter.cubicBezierTo((int) p1x, (int) p1y, (int) p2x, (int) p2y, (int) p3x, (int) p3y);
    }

    private double quadrant(double angle) {
        if (angle <= Math.PI) {
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
}
