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

package org.apache.fop.render.afp;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceLevel.ResourceType;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.apps.AFPRendererConfBuilder;
import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.afp.AFPRendererConfig.AFPRendererConfigParser;
import org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class AFPRendererConfiguratorTestCase extends
        AbstractRendererConfiguratorTest<AFPRendererConfigurator, AFPRendererConfBuilder> {

    public AFPRendererConfiguratorTestCase() {
        super(MimeConstants.MIME_AFP, AFPRendererConfBuilder.class, AFPDocumentHandler.class);
    }

    @Override
    public void setUpDocumentHandler() {
    }

    @Override
    protected AFPRendererConfigurator createConfigurator() {
        return new AFPRendererConfigurator(userAgent, new AFPRendererConfigParser());
    }

    private AFPDocumentHandler getDocHandler() {
        return (AFPDocumentHandler) docHandler;
    }

    @Test
    public void testColorImages() throws Exception {
        parseConfig(createBuilder().startImages(ImagesModeOptions.MODE_COLOR)
                                   .endImages());
        verify(getDocHandler()).setColorImages(true);

        parseConfig(createBuilder().startImages(ImagesModeOptions.MODE_GRAYSCALE)
                                   .endImages());
        verify(getDocHandler()).setColorImages(false);
    }

    @Test
    public void testCMYKImagesSupport() throws Exception {
        parseConfig(createBuilder().startImages(ImagesModeOptions.MODE_COLOR)
                                       .setModeAttribute("cmyk", "true")
                                   .endImages());
        verify(getDocHandler()).setCMYKImagesSupported(true);

        parseConfig(createBuilder().startImages(ImagesModeOptions.MODE_COLOR)
                                       .setModeAttribute("cmyk", "false")
                                   .endImages());
        verify(getDocHandler()).setCMYKImagesSupported(false);
    }

    @Test
    public void testBitsPerPixel() throws Exception {
        for (int bpp = 0; bpp < 40; bpp += 8) {
            parseConfig(createBuilder().startImages()
                                       .setModeAttribute("bits-per-pixel", String.valueOf(bpp))
                                       .endImages());
            verify(getDocHandler()).setBitsPerPixel(bpp);
        }
    }

    @Test
    public void testDitheringQuality() throws Exception {
        float ditheringQuality = 100f;
        parseConfig(createBuilder().startImages()
                                       .setDitheringQuality(ditheringQuality)
                                   .endImages());
        verify(getDocHandler()).setDitheringQuality(ditheringQuality);

        ditheringQuality = 1000f;
        parseConfig(createBuilder().startImages()
                                       .setDitheringQuality(ditheringQuality)
                                   .endImages());
        verify(getDocHandler()).setDitheringQuality(ditheringQuality);
    }

    @Test
    public void testNativeImagesSupported() throws Exception {
        parseConfig(createBuilder().startImages()
                                       .setNativeImageSupport(true)
                                   .endImages());
        verify(getDocHandler()).setNativeImagesSupported(true);

        parseConfig(createBuilder().startImages()
                                       .setNativeImageSupport(false)
                                   .endImages());
        verify(getDocHandler()).setNativeImagesSupported(false);
    }

    @Test
    public void testShadingMode() throws Exception {
        for (AFPShadingMode mode : AFPShadingMode.values()) {
            parseConfig(createBuilder().setShading(mode));
            verify(getDocHandler()).setShadingMode(mode);
        }
    }

    @Test
    public void testRendererResolution() throws Exception {
        for (int resolution = 0; resolution < 1000; resolution += 100) {
            parseConfig(createBuilder().setRenderingResolution(resolution));
            verify(getDocHandler()).setResolution(resolution);
        }
    }

    @Test
    public void testLineWidthCorrection() throws Exception {
        for (float resolution = 0; resolution < 50; resolution += 5) {
            parseConfig(createBuilder().setLineWidthCorrection(resolution));
            verify(getDocHandler()).setLineWidthCorrection(resolution);
        }
    }

    @Test
    public void testResourceGroupURI() throws Exception {
        URI uri = URI.create("test://URI/just/used/for/testing");
        parseConfig(createBuilder().setResourceGroupUri(uri.toASCIIString()));
        verify(getDocHandler()).setDefaultResourceGroupUri(uri);
    }

    @Test
    public void testResourceLevelDefaults() throws Exception {
        testResourceLevelDefault(ResourceType.DOCUMENT);
    }

    private void testResourceLevelDefault(ResourceType resType) throws Exception {
        Map<String, String> resourceLevels = new HashMap<String, String>();
        resourceLevels.put("goca", resType.getName());
        parseConfig(createBuilder().setDefaultResourceLevels(resourceLevels));
        ArgumentCaptor<AFPResourceLevelDefaults> argument = ArgumentCaptor.forClass(AFPResourceLevelDefaults.class);
        verify(getDocHandler()).setResourceLevelDefaults(argument.capture());
        AFPResourceLevel expectedLevel = new AFPResourceLevel(resType);
        assertEquals(expectedLevel, argument.getValue().getDefaultResourceLevel((byte) 3));
    }

    @Test
    public void testExternalResourceDefault() throws Exception {
        testResourceLevelDefault(ResourceType.EXTERNAL);
    }

    @Test
    public void testInlineResourceDefault() throws Exception {
        testResourceLevelDefault(ResourceType.INLINE);
    }

    @Test
    public void testPageResourceDefault() throws Exception {
        testResourceLevelDefault(ResourceType.PAGE);
    }

    @Test
    public void testPageGroupResourceDefault() throws Exception {
        testResourceLevelDefault(ResourceType.PAGE_GROUP);
    }

    @Test
    public void testPrintFileResourceDefault() throws Exception {
        testResourceLevelDefault(ResourceType.PRINT_FILE);
    }

    @Test
    public void testBitmapEncodeQuality() throws Exception {
        parseConfig(createBuilder().startImages()
                                       .setBitmapEncodingQuality(0.5f)
                                   .endImages());
        verify(getDocHandler()).setBitmapEncodingQuality(0.5f);
    }

    @Test
    public void testCanEmbedJpeg() throws Exception {
        parseConfig(createBuilder().startImages()
                                       .setAllowJpegEmbedding(true)
                                   .endImages());
        verify(getDocHandler()).canEmbedJpeg(true);

        parseConfig(createBuilder().startImages()
                                       .setAllowJpegEmbedding(false)
                                   .endImages());
        verify(getDocHandler()).canEmbedJpeg(false);
    }

}
