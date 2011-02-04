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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.writer.ImageWriterUtil;
import org.apache.xmlgraphics.util.WriterOutputStream;
import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;

import org.apache.fop.util.bitmap.BitmapImageUtil;
import org.apache.fop.util.bitmap.JAIMonochromeBitmapConverter;
import org.apache.fop.util.bitmap.MonochromeBitmapConverter;

/**
 * Tests {@link BitmapImageUtil}.
 */
public class BitmapImageUtilTestCase extends TestCase {

    private static final boolean DEBUG = true;
    private static final boolean TEST_PIXELS = true;

    /**
     * Tests the convertTo* methods.
     * @throws Exception if an error occurs
     */
    public void testConvertToMono() throws Exception {
        BufferedImage testImage = createTestImage();
        saveAsPNG(testImage, "test-image");

        RenderedImage img;
        Dimension scaled = new Dimension(320, 240);

        img = BitmapImageUtil.convertToGrayscale(testImage, null);
        saveAsPNG(img, "out-gray");
        assertEquals(1, img.getColorModel().getNumComponents());
        assertEquals(8, img.getColorModel().getPixelSize());
        assertEquals(640, img.getWidth());
        assertEquals(480, img.getHeight());
        assertPixels("5757575757575757575757FFFFFFFFFF", img, 220, 34, 16);

        img = BitmapImageUtil.convertToGrayscale(testImage, scaled);
        saveAsPNG(img, "out-gray-scaled");
        assertEquals(1, img.getColorModel().getNumComponents());
        assertEquals(8, img.getColorModel().getPixelSize());
        assertEquals(320, img.getWidth());
        assertEquals(240, img.getHeight());

        img = BitmapImageUtil.convertToMonochrome(testImage, null);
        saveAsPNG(img, "out-mono");
        assertEquals(1, img.getColorModel().getPixelSize());
        assertEquals(640, img.getWidth());
        assertEquals(480, img.getHeight());
        assertPixels("00000000000000000000000101010101", img, 220, 34, 16);

        if (isJAIAvailable()) {
            img = BitmapImageUtil.convertToMonochrome(testImage, null, 0.5f);
            saveAsPNG(img, "out-mono-jai-0.5");
            assertEquals(1, img.getColorModel().getPixelSize());
            assertEquals(640, img.getWidth());
            assertEquals(480, img.getHeight());
            assertPixels("00010000000100000001000101010101", img, 220, 34, 16);

            img = BitmapImageUtil.convertToMonochrome(testImage, null, 1.0f);
            saveAsPNG(img, "out-mono-jai-1.0");
            assertEquals(1, img.getColorModel().getPixelSize());
            assertEquals(640, img.getWidth());
            assertEquals(480, img.getHeight());
            assertPixels("01000001000001000001000101010101", img, 220, 34, 16);
        }
    }

    private void assertPixels(String expected, RenderedImage img, int x, int y, int w)
                throws IOException {
        if (TEST_PIXELS) {
            byte[] byteArray = (byte[])img.getData().getDataElements(x, y, w, 1, new byte[w]);
            assertEquals(expected, toHex(byteArray));
        }
    }

    private boolean isJAIAvailable() {
        MonochromeBitmapConverter converter
            = BitmapImageUtil.createDefaultMonochromeBitmapConverter();
        return (converter instanceof JAIMonochromeBitmapConverter);
    }

    private void saveAsPNG(RenderedImage img, String name) throws IOException {
        if (DEBUG) {
            File baseDir = new File("./build/test-results/bitmap-conversion");
            baseDir.mkdirs();
            ImageWriterUtil.saveAsPNG(img, new File(baseDir, name + ".png"));
        }
    }

    private String toHex(byte[] byteArray) throws IOException {
        InputStream in = new java.io.ByteArrayInputStream(byteArray);
        StringWriter writer = new StringWriter();
        WriterOutputStream wo = new WriterOutputStream(writer, "US-ASCII");
        ASCIIHexOutputStream hex = new ASCIIHexOutputStream(wo);
        IOUtils.copyLarge(in, hex);
        return writer.toString();
    }

    private BufferedImage createTestImage() {
        BufferedImage buf = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buf.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, buf.getWidth(), buf.getHeight());

        //A few rectangles rotated and with different color
        Graphics2D copy = (Graphics2D)g2d.create();
        copy.translate(170, 170);
        int c = 12;
        for (int i = 0; i < c; i++) {
            float f = ((i + 1) / (float)c);
            Color col = new Color(0.0f, 1 - f, 0.0f);
            copy.setColor(col);
            copy.fillRect(0, 0, 120, 120);
            copy.rotate(-2 * Math.PI / c);
        }
        copy.dispose();

        //the same in gray scales
        copy = (Graphics2D)g2d.create();
        copy.translate(470, 310);
        c = 12;
        for (int i = 0; i < c; i++) {
            float f = ((i + 1) / (float)c);
            Color col = new Color(f, f, f);
            copy.setColor(col);
            copy.fillRect(0, 0, 120, 120);
            copy.rotate(-2 * Math.PI / c);
        }
        copy.dispose();
        return buf;
    }

}
