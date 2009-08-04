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

package org.apache.fop.afp.util;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * This class can be used to convert a cubic bezier curve within
 * a path into multiple quadratic bezier curves which will approximate
 * the original cubic curve.
 * The various techniques are described here:
 * http://www.timotheegroleau.com/Flash/articles/cubic_bezier_in_flash.htm
 */
public class CubicBezierApproximator {

    /**
     * This method will take in an array containing the x and y coordinates of the four control
     * points that describe the cubic bezier curve to be approximated using the fixed mid point
     * approximation. The curve will be approximated using four quadratic bezier curves the points
     * for which will be returned in a two dimensional array, with each array within that containing
     * the points for a single quadratic curve. The returned data will not include the start point
     * for any of the curves; the first point passed in to this method should already have been
     * set as the current position and will be the assumed start of the first curve.
     *
     * @param cubicControlPointCoords an array containing the x and y coordinates of the
     *                                four control points.
     * @return an array of arrays containing the x and y coordinates of the quadratic curves
     *         that approximate the original supplied cubic bezier curve.
     */
    public static double[][] fixedMidPointApproximation(double[] cubicControlPointCoords) {
        if (cubicControlPointCoords.length < 8) {
            throw new IllegalArgumentException("Must have at least 8 coordinates");
        }

        //extract point objects from source array
        Point2D p0 = new Point2D.Double(cubicControlPointCoords[0], cubicControlPointCoords[1]);
        Point2D p1 = new Point2D.Double(cubicControlPointCoords[2], cubicControlPointCoords[3]);
        Point2D p2 = new Point2D.Double(cubicControlPointCoords[4], cubicControlPointCoords[5]);
        Point2D p3 = new Point2D.Double(cubicControlPointCoords[6], cubicControlPointCoords[7]);

        //calculates the useful base points
        Point2D pa = getPointOnSegment(p0, p1, 3.0 / 4.0);
        Point2D pb = getPointOnSegment(p3, p2, 3.0 / 4.0);

        //get 1/16 of the [P3, P0] segment
        double dx = (p3.getX() - p0.getX()) / 16.0;
        double dy = (p3.getY() - p0.getY()) / 16.0;

        //calculates control point 1
        Point2D pc1 = getPointOnSegment(p0, p1, 3.0 / 8.0);

        //calculates control point 2
        Point2D pc2 = getPointOnSegment(pa, pb, 3.0 / 8.0);
        pc2 = movePoint(pc2, -dx, -dy);

        //calculates control point 3
        Point2D pc3 = getPointOnSegment(pb, pa, 3.0 / 8.0);
        pc3 = movePoint(pc3, dx, dy);

        //calculates control point 4
        Point2D pc4 = getPointOnSegment(p3, p2, 3.0 / 8.0);

        //calculates the 3 anchor points
        Point2D pa1 = getMidPoint(pc1, pc2);
        Point2D pa2 = getMidPoint(pa, pb);
        Point2D pa3 = getMidPoint(pc3, pc4);

        //return the points for the four quadratic curves
        return new double[][] {
                {pc1.getX(), pc1.getY(), pa1.getX(), pa1.getY()},
                {pc2.getX(), pc2.getY(), pa2.getX(), pa2.getY()},
                {pc3.getX(), pc3.getY(), pa3.getX(), pa3.getY()},
                {pc4.getX(), pc4.getY(), p3.getX(), p3.getY()}};
    }

    private static Double movePoint(Point2D point, double dx, double dy) {
        return new Point2D.Double(point.getX() + dx, point.getY() + dy);
    }

    /**
     * This method will calculate the coordinates of a point half way along a segment [P0, P1]
     *
     * @param p0 - The point describing the start of the segment.
     * @param p1 - The point describing the end of the segment.
     * @return a Point object describing the coordinates of the calculated point on the segment.
     */
    private static Point2D getMidPoint(Point2D p0, Point2D p1) {
        return getPointOnSegment(p0, p1, 0.5);
    }

    /**
     * This method will calculate the coordinates of a point on a segment [P0, P1]
     * whose distance along the segment [P0, P1] from P0, is the given ratio
     * of the length the [P0, P1] segment.
     *
     * @param p0    The point describing the start of the segment.
     * @param p1    The point describing the end of the segment.
     * @param ratio The distance of the point being calculated from P0 as a ratio of
     *                  the segment length.
     * @return a Point object describing the coordinates of the calculated point on the segment.
     */
    private static Point2D getPointOnSegment(Point2D p0, Point2D p1, double ratio) {
        double x = p0.getX() + ((p1.getX() - p0.getX()) * ratio);
        double y = p0.getY() + ((p1.getY() - p0.getY()) * ratio);
        return new Point2D.Double(x, y);
    }

}
