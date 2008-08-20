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

import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * This is an abstract base class for handling border painting.
 */
public abstract class BorderPainter {

    /**
     * Draws borders.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     */
    public void drawBorders(Rectangle borderRect,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
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
            (b[0] ? bpsBefore.width : 0),
            (b[1] ? bpsEnd.width : 0),
            (b[2] ? bpsAfter.width : 0),
            (b[3] ? bpsStart.width : 0)};
        int[] clipw = new int[] {
            BorderProps.getClippedWidth(bpsBefore),
            BorderProps.getClippedWidth(bpsEnd),
            BorderProps.getClippedWidth(bpsAfter),
            BorderProps.getClippedWidth(bpsStart)};
        starty += clipw[0];
        height -= clipw[0];
        height -= clipw[2];
        startx += clipw[3];
        width -= clipw[3];
        width -= clipw[1];

        boolean[] slant = new boolean[] {
            (b[3] && b[0]), (b[0] && b[1]), (b[1] && b[2]), (b[2] && b[3])};
        if (bpsBefore != null) {
            int sx1 = startx;
            int sx2 = (slant[0] ? sx1 + bw[3] - clipw[3] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[1] ? ex1 - bw[1] + clipw[1] : ex1);
            int outery = starty - clipw[0];
            int clipy = outery + clipw[0];
            int innery = outery + bw[0];

            saveGraphicsState();
            moveTo(sx1, clipy);
            int sx1a = sx1;
            int ex1a = ex1;
            if (bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[3];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[1];
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
            int sy2 = (slant[1] ? sy1 + bw[0] - clipw[0] : sy1);
            int ey1 = starty + height;
            int ey2 = (slant[2] ? ey1 - bw[2] + clipw[2] : ey1);
            int outerx = startx + width + clipw[1];
            int clipx = outerx - clipw[1];
            int innerx = outerx - bw[1];

            saveGraphicsState();
            moveTo(clipx, sy1);
            int sy1a = sy1;
            int ey1a = ey1;
            if (bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[0];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[2];
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
            int sx2 = (slant[3] ? sx1 + bw[3] - clipw[3] : sx1);
            int ex1 = startx + width;
            int ex2 = (slant[2] ? ex1 - bw[1] + clipw[1] : ex1);
            int outery = starty + height + clipw[2];
            int clipy = outery - clipw[2];
            int innery = outery - bw[2];

            saveGraphicsState();
            moveTo(ex1, clipy);
            int sx1a = sx1;
            int ex1a = ex1;
            if (bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[3];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[1];
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
            int sy2 = (slant[0] ? sy1 + bw[0] - clipw[0] : sy1);
            int ey1 = sy1 + height;
            int ey2 = (slant[3] ? ey1 - bw[2] + clipw[2] : ey1);
            int outerx = startx - clipw[3];
            int clipx = outerx + clipw[3];
            int innerx = outerx + bw[3];

            saveGraphicsState();
            moveTo(clipx, ey1);
            int sy1a = sy1;
            int ey1a = ey1;
            if (bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[0];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[2];
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


    protected abstract void drawBorderLine(int x1, int y1, int x2, int y2,
            boolean horz, boolean startOrBefore, int style, Color color);

    public abstract void drawLine(Point start, Point end,
            int width, Color color, RuleStyle style);

    protected abstract void moveTo(int x, int y);

    protected abstract void lineTo(int x, int y);

    protected abstract void closePath();

    protected abstract void clip();

    protected abstract void saveGraphicsState();
    protected abstract void restoreGraphicsState();

}
