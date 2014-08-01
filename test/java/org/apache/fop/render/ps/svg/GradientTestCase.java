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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.io.IOUtils;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.ps.PSGenerator;

public class GradientTestCase {

    @Test
    public void testLinearGradient() throws IOException {
        float[] fractions = {0f, 1f};
        Color[] colors = {new Color(255, 255, 0), new Color(255, 0, 0)};
        LinearGradientPaint gradient = new LinearGradientPaint(115f, 285f, 15f, 15f, fractions, colors);
        testGradientRendering(gradient, "expected-linear-gradient.ps");
    }

    @Test
    public void testRadialGradient() throws IOException {
        float cx = 840f;
        float cy = 180f;
        float r = 16f;
        float[] fractions = {0.2f, 0.6f, 0.8f, 1.0f};
        Color[] colors = {
                new Color(255, 255, 255),
                new Color(200, 200, 200),
                new Color(170, 170, 170),
                new Color(140, 140, 140)};
        RadialGradientPaint gradient = new RadialGradientPaint(cx, cy, r, fractions, colors);
        testGradientRendering(gradient, "expected-radial-gradient.ps");
    }

    private void testGradientRendering(MultipleGradientPaint gradient, String expectedResourceName)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PSSVGGraphics2D svgGraphics2D = new PSSVGGraphics2D(false, new PSGenerator(out));
        svgGraphics2D.setGraphicContext(new GraphicContext());
        svgGraphics2D.translate(100, 100);
        svgGraphics2D.applyPaint(gradient, true);
        byte[] actual = out.toByteArray();
        byte[] expected = IOUtils.toByteArray(getClass().getResourceAsStream(expectedResourceName));
        assertEquals(new String(expected, "US-ASCII"), new String(actual, "US-ASCII"));
    }

}
