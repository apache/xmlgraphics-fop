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
import java.io.IOException;

import org.apache.fop.traits.RuleStyle;

/**
 * Used primarily by {@link BorderPainter}, implementations are created for rendering
 * primitive graphical operations.
 *
 */
public interface GraphicsPainter {

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
    void drawBorderLine(int x1, int y1, int x2, int y2,
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
    void drawLine(Point start, Point end,
            int width, Color color, RuleStyle style) throws IOException;

    /**
     * Moves the cursor to the given coordinate.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException if an I/O error occurs
     */
    void moveTo(int x, int y) throws IOException;

    /**
     * Draws a line from the current cursor position to the given coordinates.
     * @param x the X coordinate (in millipoints)
     * @param y the Y coordinate (in millipoints)
     * @throws IOException if an I/O error occurs
     */
    void lineTo(int x, int y) throws IOException;

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
    void arcTo(final double startAngle, final double endAngle, final int cx, final int cy,
            final int width, final int height) throws IOException;

    /**
     * Rotate the coordinate frame
     * @param angle angle in radians to rotate the coordinate frame
     * @throws IOException if an I/O error occurs
     */
    void rotateCoordinates(double angle) throws IOException;

    /**
     * Translate the coordinate frame
     * @param xTranslate translation in the x direction
     * @param yTranslate translation in the y direction
     * @throws IOException if an I/O error occurs
     */
    void translateCoordinates(int xTranslate, int yTranslate) throws IOException;

    /**
     * Scale the coordinate frame
     * @param xScale scale factor in the x direction
     * @param yScale scale factor in the y direction
     * @throws IOException if an I/O error occurs
     */
    void scaleCoordinates(float xScale, float yScale) throws IOException;

    /**
     * Closes the current path.
     * @throws IOException if an I/O error occurs
     */
    void closePath() throws IOException;

    /**
     * Reduces the current clipping region to the current path.
     * @throws IOException if an I/O error occurs
     */
    void clip() throws IOException;

    /**
     * Save the graphics state on the stack.
     * @throws IOException if an I/O error occurs
     */
    void saveGraphicsState() throws IOException;

    /**
     * Restore the last graphics state from the stack.
     * @throws IOException if an I/O error occurs
     */
    void restoreGraphicsState() throws IOException;
}
