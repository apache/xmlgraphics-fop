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

package org.apache.fop.render.ps;

import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;

import org.apache.fop.render.RawPNGTestUtil;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageEncoderPNGTestCase {

    @Test
    public void testWriteToWithRGBPNG() throws IOException {
        testWriteToWithGRGBAPNG(-1, 128, 128, 128, -1);
    }

    @Test
    public void testWriteToWithGPNG() throws IOException {
        testWriteToWithGRGBAPNG(128, -1, -1, -1, -1);
    }

    @Test
    public void testWriteToWithRGBAPNG() throws IOException {
        testWriteToWithGRGBAPNG(-1, 128, 128, 128, 128);
    }

    @Test
    public void testWriteToWithGAPNG() throws IOException {
        testWriteToWithGRGBAPNG(128, -1, -1, -1, 128);
    }

    private void testWriteToWithGRGBAPNG(int gray, int red, int green, int blue, int alpha)
            throws IOException {
        int numComponents = (gray > -1 ? 1 : 3) + (alpha > -1 ? 1 : 0);
        ImageSize is = RawPNGTestUtil.getImageSize();
        ComponentColorModel cm = mock(ComponentColorModel.class);
        when(cm.getNumComponents()).thenReturn(numComponents);
        ImageRawPNG irpng = mock(ImageRawPNG.class);
        when(irpng.getColorModel()).thenReturn(cm);
        when(irpng.getSize()).thenReturn(is);
        ImageEncoderPNG iepng = new ImageEncoderPNG(irpng);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = RawPNGTestUtil.buildGRGBAData(gray, red, green, blue, alpha);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        when(irpng.createInputStream()).thenReturn(bais);
        iepng.writeTo(baos);
        if (alpha > -1) {
            byte[] expected = RawPNGTestUtil.buildGRGBAData(gray, red, green, blue, -1);
            assertArrayEquals(expected, baos.toByteArray());
        } else {
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    @Test
    public void testWriteToWithPalettePNG() throws IOException {
        ImageSize is = RawPNGTestUtil.getImageSize();
        IndexColorModel cm = mock(IndexColorModel.class);
        ImageRawPNG irpng = mock(ImageRawPNG.class);
        when(irpng.getColorModel()).thenReturn(cm);
        when(irpng.getSize()).thenReturn(is);
        ImageEncoderPNG iepng = new ImageEncoderPNG(irpng);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = RawPNGTestUtil.buildGRGBAData(128, -1, -1, -1, -1);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        when(irpng.createInputStream()).thenReturn(bais);
        iepng.writeTo(baos);
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    public void testGetImplicitFilterWithIndexColorModel() {
        ImageSize is = RawPNGTestUtil.getImageSize();
        IndexColorModel cm = mock(IndexColorModel.class);
        ImageRawPNG irpng = mock(ImageRawPNG.class);
        when(irpng.getColorModel()).thenReturn(cm);
        when(irpng.getBitDepth()).thenReturn(8);
        when(irpng.getSize()).thenReturn(is);
        ImageEncoderPNG iepng = new ImageEncoderPNG(irpng);

        String expectedFilter = "<< /Predictor 15 /Columns 32 /Colors 1 /BitsPerComponent 8 >> /FlateDecode";
        assertEquals(expectedFilter, iepng.getImplicitFilter());
    }

    @Test
    public void testGetImplicitFilterWithComponentColorModel() {
        ImageSize is = RawPNGTestUtil.getImageSize();
        ComponentColorModel cm = mock(ComponentColorModel.class);
        when(cm.getNumComponents()).thenReturn(3);
        ImageRawPNG irpng = mock(ImageRawPNG.class);
        when(irpng.getColorModel()).thenReturn(cm);
        when(irpng.getBitDepth()).thenReturn(8);
        when(irpng.getSize()).thenReturn(is);
        ImageEncoderPNG iepng = new ImageEncoderPNG(irpng);

        String expectedFilter = "<< /Predictor 15 /Columns 32 /Colors 3 /BitsPerComponent 8 >> /FlateDecode";
        assertEquals(expectedFilter, iepng.getImplicitFilter());
    }

}
