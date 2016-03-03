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

public interface BezierCurvePainter {
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
    void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y) throws IOException;
}
