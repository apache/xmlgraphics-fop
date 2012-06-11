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

package org.apache.fop.render.afp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.apps.AFPRendererConfBuilder;
import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.render.afp.AFPRendererConfig.AFPRendererConfigParser;
import org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions;

import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_COLOR;
import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AFPRendererConfigParserTestCase
        extends AbstractRendererConfigParserTester<AFPRendererConfBuilder, AFPRendererConfig> {

    public AFPRendererConfigParserTestCase() {
        super(new AFPRendererConfigParser(), AFPRendererConfBuilder.class);
    }

    @Test
    public void testShadingMode() throws Exception {
        parseConfig();
        assertEquals(AFPShadingMode.COLOR, conf.getShadingMode());
        parseConfig(createRenderer().setShading(AFPShadingMode.DITHERED));
        assertEquals(AFPShadingMode.DITHERED, conf.getShadingMode());
    }

    @Test
    public void testResolution() throws Exception {
        parseConfig(createRenderer());
        assertEquals(Integer.valueOf(240), conf.getResolution());
        parseConfig(createRenderer().setRenderingResolution(300));
        assertEquals(Integer.valueOf(300), conf.getResolution());
    }

    @Test
    public void testLineWidthCorrection() throws Exception {
        parseConfig(createRenderer());
        assertEquals(AFPConstants.LINE_WIDTH_CORRECTION,
                conf.getLineWidthCorrection().floatValue(), 0.0001f);
        parseConfig(createRenderer().setLineWidthCorrection(1f));
        assertEquals(Float.valueOf(1f), conf.getLineWidthCorrection());
    }

    @Test
    public void testResourceGroupUri() throws Exception {
        parseConfig(createRenderer());
        assertEquals(null, conf.getDefaultResourceGroupUri());
        // TODO yuck!
        File file = File.createTempFile("AFPRendererConfigParserTestCase", "");
        try {
            file.delete();
            parseConfig(createRenderer().setResourceGroupUri(file.toURI().toASCIIString()));
            assertEquals(file.toURI(), conf.getDefaultResourceGroupUri());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testResourceLevelDefaults() throws Exception {
        parseConfig(createRenderer());
        assertNull(conf.getResourceLevelDefaults());
        Map<String, String> levels = new HashMap<String, String>();
        levels.put("goca", "page");
        parseConfig(createRenderer().setDefaultResourceLevels(levels));
        assertNotNull(conf.getResourceLevelDefaults());
    }

    @Test
    public void testImages() throws Exception {
        parseConfig(createRenderer());
        assertEquals(false, conf.isColorImages());
        assertEquals(Integer.valueOf(8), conf.getBitsPerPixel());
        ImagesModeOptions mode = MODE_GRAYSCALE;
        parseConfig(createRenderer().startImages(mode)
                                    .setModeAttribute(mode.getModeAttribute(), String.valueOf(1))
                                    .endImages());
        assertEquals(false, conf.isColorImages());
        assertEquals(Integer.valueOf(1), conf.getBitsPerPixel());
        mode = MODE_COLOR;
        parseConfig(createRenderer()
                                    .startImages(mode)
                                    .setModeAttribute(mode.getModeAttribute(),
                                            String.valueOf(false))
                                    .endImages());
        assertEquals(true, conf.isColorImages());
        assertEquals(false, conf.isCmykImagesSupported());
        parseConfig(createRenderer().startImages(mode)
                                    .setModeAttribute(mode.getModeAttribute(), String.valueOf(true))
                                    .endImages());
        assertEquals(true, conf.isColorImages());
        assertEquals(true, conf.isCmykImagesSupported());
    }

    @Test(expected = IllegalStateException.class)
    public void testImagesException1() throws Exception {
        parseConfig(createRenderer().startImages().endImages());
        conf.isCmykImagesSupported();
    }

    @Test(expected = IllegalStateException.class)
    public void testImagesException2() throws Exception {
        parseConfig(createRenderer().startImages(MODE_COLOR).endImages());
        conf.getBitsPerPixel();
    }

    @Test
    public void testImagesNative() throws Exception {
        parseConfig(createRenderer());
        assertEquals(false, conf.isNativeImagesSupported());
        parseConfig(createRenderer().startImages().setNativeImageSupport(true).endImages());
        assertEquals(true, conf.isNativeImagesSupported());
    }

    @Test
    public void testDitheringQuality() throws Exception {
        parseConfig(createRenderer());
        assertEquals(0.5f, conf.getDitheringQuality(), 0.001f);
        parseConfig(createRenderer().startImages().setDitheringQuality("min").endImages());
        assertEquals(0.0f, conf.getDitheringQuality(), 0.001f);
        parseConfig(createRenderer().startImages().setDitheringQuality("max").endImages());
        assertEquals(1.0f, conf.getDitheringQuality(), 0.001f);
        parseConfig(createRenderer().startImages().setDitheringQuality(0.25f).endImages());
        assertEquals(0.25f, conf.getDitheringQuality(), 0.001f);
    }

    @Test
    public void testAllowJpegEmbedding() throws Exception {
        parseConfig();
        assertEquals(false, conf.allowJpegEmbedding());

        parseConfig(createRenderer().startImages().setAllowJpegEmbedding(true).endImages());
        assertEquals(true, conf.allowJpegEmbedding());
    }

    @Test
    public void testBitmapEncodingQuality() throws Exception {
        parseConfig();
        assertEquals(1.0f, conf.getBitmapEncodingQuality(), 0.001f);
        parseConfig(createRenderer().startImages().setBitmapEncodingQuality(0.5f).endImages());
        assertEquals(0.5f, conf.getBitmapEncodingQuality(), 0.001f);
    }

    @Test
    public void testFS45() throws Exception {
        parseConfig();
        assertEquals(false, conf.isFs45());
        parseConfig(createRenderer().startImages().setFs45(true).endImages());
        assertEquals(true, conf.isFs45());
    }

    @Test
    public void tesPseg() throws Exception {
        parseConfig();
        assertEquals(false, conf.isWrapPseg());
        parseConfig(createRenderer().startImages().setWrapPseg(true).endImages());
        assertEquals(true, conf.isWrapPseg());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForNameException() throws Exception {
        ImagesModeOptions.forName("_");
    }
}
