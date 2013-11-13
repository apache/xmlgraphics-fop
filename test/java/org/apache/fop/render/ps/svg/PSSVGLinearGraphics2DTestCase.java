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

package org.apache.fop.render.ps.svg;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import org.apache.commons.io.FileUtils;

import org.apache.batik.ext.awt.LinearGradientPaint;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.PSGenerator;

import static org.junit.Assert.assertEquals;

public class PSSVGLinearGraphics2DTestCase {
    float startX = 115f;
    float endX = 15f;
    float startY = 285f;
    float endY=15f;
    float[] fractions = {0.0f, 1.0f};

    /**
     * Tests a linear gradient generated pattern with certain inputs against
     * an expected output.
     * @throws IOException
     */
    @Test
    public void testApplyPaint() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PSGenerator gen = new PSGenerator(os);
        PSSVGGraphics2D svgGraphics2D = new PSSVGGraphics2D(false, gen);
        svgGraphics2D.setGraphicContext(new GraphicContext());
        svgGraphics2D.setTransform(new AffineTransform());
        Color[] colors = {new Color(255, 255, 0), new Color(255, 0, 0)};
        LinearGradientPaint paint = new LinearGradientPaint(startX, startY, endX, endY, fractions, colors);
        Float s = new Rectangle2D.Float(115.0f, 15.0f, 170f, 110f);
        svgGraphics2D.applyPaint(paint, true);
        byte[] test = os.toByteArray();

        byte[] expected = FileUtils.readFileToByteArray(
                new File("test/java/org/apache/fop/render/ps/svg/axial-shading-expected.dat"));
        assertEquals(new String(test), new String(expected));
    }
}
