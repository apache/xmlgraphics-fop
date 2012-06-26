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

package org.apache.fop.svg;

import java.awt.BasicStroke;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PDFGraphics2DTestCase {

    @Test
    public void testApplyStrokeNullDash() {
        PDFGraphics2D g2d = new PDFGraphics2D(false);
        BasicStroke stroke = new BasicStroke();
        g2d.applyStroke(stroke);
        assertTrue(g2d.getString().contains("[] 0 d\n"));
    }

    @Test
    public void testApplyStrokeNonNullDash() {
        PDFGraphics2D g2d = new PDFGraphics2D(false);
        float[] dashArray = {3.0f, 5.0f};
        BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                dashArray, 0.0f);
        g2d.applyStroke(stroke);
        assertTrue(g2d.getString().contains("[3 5] 0 d\n"));
    }

}
