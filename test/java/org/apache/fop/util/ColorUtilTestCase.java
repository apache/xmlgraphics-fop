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

package org.apache.fop.util;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.net.URI;

import junit.framework.TestCase;

import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;

/**
 * Tests the ColorUtil class.
 */
public class ColorUtilTestCase extends TestCase {

    /**
     * Test serialization to String.
     * @throws Exception if an error occurs
     */
    public void testSerialization() throws Exception {
        Color col = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        String s = ColorUtil.colorToString(col);

        //This is what the old color spit out. Now it is 80 due to rounding
        //assertEquals("#ffff7f", s);
        assertEquals("#ffff80", s);

        col = new Color(1.0f, 0.0f, 0.0f, 0.8f);
        s = ColorUtil.colorToString(col);
        assertEquals("#ff0000cc", s);
    }

    /**
     * Test deserialization from String.
     * @throws Exception if an error occurs
     */
    public void testDeserialization() throws Exception {
        Color col = ColorUtil.parseColorString(null, "#ffff7f");
        assertEquals(255, col.getRed());
        assertEquals(255, col.getGreen());
        assertEquals(127, col.getBlue());
        assertEquals(255, col.getAlpha());

        col = ColorUtil.parseColorString(null, "#ff0000cc");
        assertEquals(255, col.getRed());
        assertEquals(0, col.getGreen());
        assertEquals(0, col.getBlue());
        assertEquals(204, col.getAlpha());
    }

    /**
     * Test equals().
     * @throws Exception if an error occurs
     */
    public void testEquals() throws Exception {
        Color col1 = ColorUtil.parseColorString(null, "#ff0000cc");
        Color col2 = ColorUtil.parseColorString(null, "#ff0000cc");
        assertEquals(col1, col2);

        col1 = ColorUtil.parseColorString(null, "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)");
        /* The following doesn't work since java.awt.Color from Sun doesn't round consistently
        col2 = ColorUtil.parseColorString(null, "cmyk(0.0,0.0,0.0,0.5)");
        assertEquals(col1, col2);
        */

        col2 = ColorUtil.parseColorString(null, "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.5,0.5,0.5,0.0)");
        assertTrue(col1.equals(col2));
        assertFalse(org.apache.xmlgraphics.java2d.color.ColorUtil.isSameColor(col1, col2));
    }

    /**
     * Tests the rgb() function.
     * @throws Exception if an error occurs
     */
    public void testRGB() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        FOUserAgent ua = fopFactory.newFOUserAgent();
        Color colActual;

        colActual = ColorUtil.parseColorString(ua, "rgb(255, 40, 0)");
        assertEquals(255, colActual.getRed());
        assertEquals(40, colActual.getGreen());
        assertEquals(0, colActual.getBlue());
        assertEquals(255, colActual.getAlpha());
        assertEquals(ColorSpace.getInstance(ColorSpace.CS_sRGB), colActual.getColorSpace());
    }

    /**
     * Tests the fop-rgb-icc() function.
     * @throws Exception if an error occurs
     */
    public void testRGBICC() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        URI sRGBLoc = new URI(
                "file:src/java/org/apache/fop/pdf/sRGB%20Color%20Space%20Profile.icm");
        ColorSpace cs = fopFactory.getColorSpaceCache().get(
                "sRGBAlt", null, sRGBLoc.toASCIIString(), RenderingIntent.AUTO);
        assertNotNull("Color profile not found", cs);

        FOUserAgent ua = fopFactory.newFOUserAgent();
        ColorWithFallback colActual;

        //fop-rgb-icc() is used instead of rgb-icc() inside FOP!
        String colSpec = "fop-rgb-icc(1.0,0.0,0.0,sRGBAlt,"
            + "\"" + sRGBLoc.toASCIIString() + "\",1.0,0.0,0.0)";
        colActual = (ColorWithFallback)ColorUtil.parseColorString(ua, colSpec);
        assertEquals(cs, colActual.getColorSpace());
        assertEquals(255, colActual.getRed(), 2f); //Java 5: 253, Java 6: 255
        assertEquals(0, colActual.getGreen(), 25f); //Java 5: 25, Java 6: 0
        assertEquals(0, colActual.getBlue());
        //I don't understand the difference. Maybe Java's sRGB and HP's sRGB are somehow not
        //equivalent. This is only going to be a problem if anyone actually makes use of the
        //RGB fallback in any renderer.
        //TODO Anyone know what's going on here?
        float[] comps = colActual.getColorComponents(null);
        assertEquals(3, comps.length);
        assertEquals(1f, comps[0], 0);
        assertEquals(0f, comps[1], 0);
        assertEquals(0f, comps[2], 0);
        assertEquals(0, colActual.getAlternativeColors().length);

        Color fallback = colActual.getFallbackColor();
        assertTrue(fallback.getColorSpace().isCS_sRGB());
        assertEquals(255, fallback.getRed());
        assertEquals(0, fallback.getGreen());
        assertEquals(0, fallback.getBlue());

        assertEquals(colSpec, ColorUtil.colorToString(colActual));

        colSpec = "fop-rgb-icc(1.0,0.5,0.0,blah,"
            + "\"invalid.icm\",1.0,0.5,0.0,0.15)";
        Color colFallback = ColorUtil.parseColorString(ua, colSpec);
        assertEquals(new Color(1.0f, 0.5f, 0.0f), colFallback);
    }

    /**
     * Tests the cmyk() function.
     * @throws Exception if an error occurs
     */
    public void testCMYK() throws Exception {
        ColorWithAlternatives colActual;
        String colSpec;

        colSpec = "cmyk(0.0, 0.0, 1.0, 0.0)";
        colActual = (ColorWithAlternatives)ColorUtil.parseColorString(null, colSpec);
        assertEquals(255, colActual.getRed());
        assertEquals(255, colActual.getGreen());
        assertEquals(0, colActual.getBlue());
        Color alt = colActual.getAlternativeColors()[0];
        assertEquals(ColorSpaces.getDeviceCMYKColorSpace(), alt.getColorSpace());
        float[] comps = alt.getColorComponents(null);
        assertEquals(4, comps.length);
        assertEquals(0f, comps[0], 0);
        assertEquals(0f, comps[1], 0);
        assertEquals(1f, comps[2], 0);
        assertEquals(0f, comps[3], 0);
        assertEquals("fop-rgb-icc(1.0,1.0,0.0,#CMYK,,0.0,0.0,1.0,0.0)",
                ColorUtil.colorToString(colActual));

        colSpec = "cmyk(0.0274, 0.2196, 0.3216, 0.0)";
        colActual = (ColorWithAlternatives)ColorUtil.parseColorString(null, colSpec);
        assertEquals(248, colActual.getRed(), 1);
        assertEquals(199, colActual.getGreen(), 1);
        assertEquals(172, colActual.getBlue(), 1);
        alt = colActual.getAlternativeColors()[0];
        assertEquals(ColorSpaces.getDeviceCMYKColorSpace(), alt.getColorSpace());
        comps = alt.getColorComponents(null);
        assertEquals(0.0274f, comps[0], 0.001);
        assertEquals(0.2196f, comps[1], 0.001);
        assertEquals(0.3216f, comps[2], 0.001);
        assertEquals(0f, comps[3], 0);
        assertEquals("fop-rgb-icc(0.9726,0.7804,0.67840004,#CMYK,,0.0274,0.2196,0.3216,0.0)",
                ColorUtil.colorToString(colActual));

        colSpec = "fop-rgb-icc(1.0,1.0,0.0,#CMYK,,0.0,0.0,1.0,0.0)";
        colActual = (ColorWithAlternatives)ColorUtil.parseColorString(null, colSpec);
        assertEquals(255, colActual.getRed());
        assertEquals(255, colActual.getGreen());
        assertEquals(0, colActual.getBlue());
        alt = colActual.getAlternativeColors()[0];
        assertEquals(ColorSpaces.getDeviceCMYKColorSpace(), alt.getColorSpace());
        comps = alt.getColorComponents(null);
        assertEquals(4, comps.length);
        assertEquals(0f, comps[0], 0);
        assertEquals(0f, comps[1], 0);
        assertEquals(1f, comps[2], 0);
        assertEquals(0f, comps[3], 0);
        assertEquals("fop-rgb-icc(1.0,1.0,0.0,#CMYK,,0.0,0.0,1.0,0.0)",
                ColorUtil.colorToString(colActual));

        colSpec = "fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)";
        colActual = (ColorWithAlternatives)ColorUtil.parseColorString(null, colSpec);
        assertEquals(127, colActual.getRed(), 1);
        assertEquals(127, colActual.getGreen(), 1);
        assertEquals(127, colActual.getBlue(), 1);
        alt = colActual.getAlternativeColors()[0];
        assertEquals(ColorSpaces.getDeviceCMYKColorSpace(), alt.getColorSpace());
        comps = alt.getColorComponents(null);
        assertEquals(4, comps.length);
        assertEquals(0f, comps[0], 0);
        assertEquals(0f, comps[1], 0);
        assertEquals(0f, comps[2], 0);
        assertEquals(0.5f, comps[3], 0);
        assertEquals("fop-rgb-icc(0.5,0.5,0.5,#CMYK,,0.0,0.0,0.0,0.5)",
                ColorUtil.colorToString(colActual));

        //Verify that the cmyk() and fop-rgb-icc(#CMYK) functions have the same results
        ColorWithAlternatives colCMYK = (ColorWithAlternatives)ColorUtil.parseColorString(
                null, "cmyk(0,0,0,0.5)");
        assertEquals(colCMYK.getAlternativeColors()[0], colActual.getAlternativeColors()[0]);
        //The following doesn't work:
        //assertEquals(colCMYK, colActual);
        //java.awt.Color does not consistenly calculate the int RGB values:
        //Color(ColorSpace cspace, float components[], float alpha): 0.5 --> 127
        //Color(float r, float g, float b): 0.5 --> 128
        if (!colCMYK.equals(colActual)) {
            System.out.println("Info: java.awt.Color does not consistently calculate"
                    + " int RGB values from float RGB values.");
        }
    }

    /**
     * Tests color for the #Separation pseudo-colorspace.
     * @throws Exception if an error occurs
     */
    public void testSeparationColor() throws Exception {
        ColorWithFallback colActual;
        String colSpec;

        colSpec = "fop-rgb-icc(1.0,0.8,0.0,#Separation,,Postgelb)";
        colActual = (ColorWithFallback)ColorUtil.parseColorString(null, colSpec);
        assertEquals(255, colActual.getRed(), 2);
        assertEquals(204, colActual.getGreen(), 3);
        assertEquals(0, colActual.getBlue(), 6);
        //sRGB results differ between JDKs

        Color fallback = colActual.getFallbackColor();
        assertEquals(255, fallback.getRed());
        assertEquals(204, fallback.getGreen());
        assertEquals(0, fallback.getBlue());

        assertFalse(colActual.hasAlternativeColors());

        assertTrue(colActual.getColorSpace() instanceof NamedColorSpace);
        NamedColorSpace ncs;
        ncs = (NamedColorSpace)colActual.getColorSpace();
        assertEquals("Postgelb", ncs.getColorName());
        float[] comps = colActual.getColorComponents(null);
        assertEquals(1, comps.length);
        assertEquals(1f, comps[0], 0);
        assertEquals(colSpec, ColorUtil.colorToString(colActual));

    }

    /**
     * Tests the fop-rgb-named-color() function.
     * @throws Exception if an error occurs
     */
    public void testNamedColorProfile() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance();
        URI ncpLoc = new URI("file:test/resources/color/ncp-example.icc");
        ColorSpace cs = fopFactory.getColorSpaceCache().get(
                "NCP", null, ncpLoc.toASCIIString(), RenderingIntent.AUTO);
        assertNotNull("Color profile not found", cs);

        FOUserAgent ua = fopFactory.newFOUserAgent();
        ColorWithFallback colActual;

        //fop-rgb-named-color() is used instead of rgb-named-color() inside FOP!
        String colSpec = "fop-rgb-named-color(1.0,0.8,0.0,NCP,"
            + "\"" + ncpLoc.toASCIIString() + "\",Postgelb)";
        colActual = (ColorWithFallback)ColorUtil.parseColorString(ua, colSpec);
        assertEquals(255, colActual.getRed(), 1);
        assertEquals(193, colActual.getGreen(), 2);
        assertEquals(0, colActual.getBlue());

        Color fallback = colActual.getFallbackColor();
        assertEquals(255, fallback.getRed());
        assertEquals(204, fallback.getGreen());
        assertEquals(0, fallback.getBlue());
        assertEquals(ColorSpace.getInstance(ColorSpace.CS_sRGB), fallback.getColorSpace());

        float[] comps = fallback.getColorComponents(null);
        assertEquals(3, comps.length);
        assertEquals(1f, comps[0], 0);
        assertEquals(0.8f, comps[1], 0);
        assertEquals(0f, comps[2], 0);

        assertTrue(colActual.getColorSpace() instanceof NamedColorSpace);
        NamedColorSpace ncs;
        ncs = (NamedColorSpace)colActual.getColorSpace();
        assertEquals("Postgelb", ncs.getColorName());
        comps = colActual.getColorComponents(null);
        assertEquals(1, comps.length);
        assertEquals(1f, comps[0], 0);

        assertEquals(colSpec, ColorUtil.colorToString(colActual));
    }
}
