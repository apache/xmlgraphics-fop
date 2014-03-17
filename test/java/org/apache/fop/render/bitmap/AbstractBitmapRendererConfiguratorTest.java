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

package org.apache.fop.render.bitmap;

import java.awt.image.BufferedImage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.BitmapRendererConfBuilder;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.util.ColorUtil;

import static org.apache.fop.render.bitmap.BitmapRendererOption.COLOR_MODE_BILEVEL;
import static org.apache.fop.render.bitmap.BitmapRendererOption.COLOR_MODE_BINARY;
import static org.apache.fop.render.bitmap.BitmapRendererOption.COLOR_MODE_GRAY;
import static org.apache.fop.render.bitmap.BitmapRendererOption.COLOR_MODE_RGB;
import static org.apache.fop.render.bitmap.BitmapRendererOption.COLOR_MODE_RGBA;
import static org.apache.fop.render.bitmap.BitmapRendererOption.RENDERING_QUALITY;
import static org.apache.fop.render.bitmap.BitmapRendererOption.RENDERING_SPEED;

public abstract class AbstractBitmapRendererConfiguratorTest extends
        AbstractRendererConfiguratorTest<BitmapRendererConfigurator, BitmapRendererConfBuilder> {

    public AbstractBitmapRendererConfiguratorTest(String mimeType,
            Class<? extends IFDocumentHandler> docHandlerClass) {
        super(mimeType, BitmapRendererConfBuilder.class, docHandlerClass);
    }

    BitmapRenderingSettings settings;

    @Override
    public void setUpDocumentHandler() {
        settings = new BitmapRenderingSettings();
        when(((AbstractBitmapDocumentHandler) docHandler).getSettings()).thenReturn(settings);
    }

    @Test
    public void testSetPageBackgroundColor() throws Exception {
        // Try a few different colours
        parseConfig(createBuilder().setBackgroundColor("Blue"));
        assertEquals(ColorUtil.parseColorString(null, "Blue"), settings.getPageBackgroundColor());

        parseConfig(createBuilder().setBackgroundColor("Black"));
        assertEquals(ColorUtil.parseColorString(null, "Black"), settings.getPageBackgroundColor());
    }

    @Test
    public void testAntiAliasing() throws Exception {
        parseConfig(createBuilder().setAntiAliasing(true));
        assertTrue(settings.isAntiAliasingEnabled());

        parseConfig(createBuilder().setAntiAliasing(false));
        assertFalse(settings.isAntiAliasingEnabled());
    }

    @Test
    public void testTransparentBackground() throws Exception {
        parseConfig(createBuilder().setPageBackgroundTransparency(true));
        assertTrue(settings.hasTransparentPageBackground());

        parseConfig(createBuilder().setPageBackgroundTransparency(false));
        assertFalse(settings.hasTransparentPageBackground());
    }

    @Test
    public void testRendererQuality() throws Exception {
        parseConfig(createBuilder().setRenderingQuality(RENDERING_QUALITY.getName()));
        assertTrue(settings.isQualityRenderingEnabled());

        parseConfig(createBuilder().setRenderingQuality(RENDERING_SPEED.getName()));
        assertFalse(settings.isQualityRenderingEnabled());

        parseConfig(createBuilder());
        assertTrue(settings.isQualityRenderingEnabled());
    }

    @Test
    public void testColorModes() throws Exception {
        parseConfig(createBuilder().setColorMode(COLOR_MODE_RGBA.getName()));
        assertEquals(BufferedImage.TYPE_INT_ARGB, settings.getBufferedImageType());

        parseConfig(createBuilder().setColorMode(COLOR_MODE_RGB.getName()));
        assertEquals(BufferedImage.TYPE_INT_RGB, settings.getBufferedImageType());

        parseConfig(createBuilder().setColorMode(COLOR_MODE_GRAY.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, settings.getBufferedImageType());

        parseConfig(createBuilder().setColorMode(COLOR_MODE_BINARY.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_BINARY, settings.getBufferedImageType());

        parseConfig(createBuilder().setColorMode(COLOR_MODE_BILEVEL.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_BINARY, settings.getBufferedImageType());

        parseConfig(createBuilder());
        assertEquals(BufferedImage.TYPE_INT_ARGB, settings.getBufferedImageType());
    }
}
