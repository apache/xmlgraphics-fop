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

import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.PSGenerator;

import static org.junit.Assert.assertEquals;

public class PSSVGGraphics2DTestCase {

    float cx = 841.891f;
    float cy = 178.583f;
    float r = 16.4331f;
    float[] fractions = {0.2f, 0.6012f, 0.8094f, 1.0f};

    /**
     * Tests a radial gradient generated pattern with certain inputs against
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
        Color[] colors = {new Color(255, 255, 255), new Color(200, 200, 200),
                new Color(170, 170, 170), new Color(140, 140, 140)};
        RadialGradientPaint paint = new RadialGradientPaint(cx, cy, r, fractions, colors);
        Float s = new Rectangle2D.Float(7.0f, 3.0f, 841.891f, 178.583f);
        svgGraphics2D.applyPaint(paint, true);
        byte[] test = os.toByteArray();

        byte[] expected = FileUtils.readFileToByteArray(
                new File("test/java/org/apache/fop/render/ps/svg/expected.ps"));
        assertEquals(new String(test), new String(expected));
    }
}
