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

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.Test;

import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.apps.BitmapRendererConfBuilder;
import org.apache.fop.render.bitmap.BitmapRendererConfig.BitmapRendererConfigParser;

import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.COLOR_MODE_BILEVEL;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.COLOR_MODE_BINARY;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.COLOR_MODE_GRAY;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.COLOR_MODE_RGB;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.COLOR_MODE_RGBA;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.RENDERING_QUALITY;
import static org.apache.fop.render.bitmap.BitmapRendererConfigOption.RENDERING_SPEED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractBitmapRendererConfigParserTester
        extends AbstractRendererConfigParserTester<BitmapRendererConfBuilder, BitmapRendererConfig> {

    public AbstractBitmapRendererConfigParserTester(BitmapRendererConfigParser parser) {
        super(parser, BitmapRendererConfBuilder.class);
    }

    @Test
    public void testTransparentPageBackground() throws Exception {
        parseConfig(createRenderer().setPageBackgroundTransparency(true));
        assertTrue(conf.hasTransparentBackround());
        assertNull(conf.getBackgroundColor());

        parseConfig(createRenderer().setPageBackgroundTransparency(false));
        assertFalse(conf.hasTransparentBackround());
        assertEquals(Color.WHITE, conf.getBackgroundColor());

        parseConfig(createRenderer());
        assertEquals(JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getDefaultValue(),
                conf.hasTransparentBackround());
    }

    @Test
    public void testBackgroundColor() throws Exception {
        parseConfig(createRenderer().setBackgroundColor("black"));
        assertEquals(Color.BLACK, conf.getBackgroundColor());

        parseConfig(createRenderer().setBackgroundColor("white"));
        assertEquals(Color.WHITE, conf.getBackgroundColor());

        parseConfig(createRenderer().setBackgroundColor("blue"));
        assertEquals(Color.BLUE, conf.getBackgroundColor());

        parseConfig(createRenderer().setBackgroundColor("blue")
                               .setPageBackgroundTransparency(true));
        assertTrue(conf.hasTransparentBackround());
        assertNull(conf.getBackgroundColor());
    }

    @Test
    public void testAntiAliasing() throws Exception {
        parseConfig(createRenderer().setAntiAliasing(true));
        assertTrue(conf.hasAntiAliasing());

        parseConfig(createRenderer().setAntiAliasing(false));
        assertFalse(conf.hasAntiAliasing());
    }

    @Test
    public void testRendererQuality() throws Exception {
        parseConfig(createRenderer().setRenderingQuality(RENDERING_QUALITY.getName()));
        assertTrue(conf.isRenderHighQuality());

        parseConfig(createRenderer().setRenderingQuality(RENDERING_SPEED.getName()));
        assertFalse(conf.isRenderHighQuality());

        parseConfig(createRenderer());
        assertTrue(conf.isRenderHighQuality());
    }

    @Test
    public void testColorModes() throws Exception {
        parseConfig(createRenderer().setColorMode(COLOR_MODE_RGBA.getName()));
        assertEquals(BufferedImage.TYPE_INT_ARGB, (int) conf.getColorMode());

        parseConfig(createRenderer().setColorMode(COLOR_MODE_RGB.getName()));
        assertEquals(BufferedImage.TYPE_INT_RGB, (int) conf.getColorMode());

        parseConfig(createRenderer().setColorMode(COLOR_MODE_GRAY.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, (int) conf.getColorMode());

        parseConfig(createRenderer().setColorMode(COLOR_MODE_BINARY.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_BINARY, (int) conf.getColorMode());

        parseConfig(createRenderer().setColorMode(COLOR_MODE_BILEVEL.getName()));
        assertEquals(BufferedImage.TYPE_BYTE_BINARY, (int) conf.getColorMode());

        parseConfig(createRenderer());
        assertEquals(BufferedImage.TYPE_INT_ARGB, (int) conf.getColorMode());
    }
}
