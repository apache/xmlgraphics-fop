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

package org.apache.fop.image.loader.batik;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.XMLNamespaceEnabledImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.writer.ImageWriterUtil;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;

/**
 * Tests for bundled ImageLoader implementations.
 */
@Ignore("Batik fails big time")
public class ImageLoaderTestCase {

    private static final File DEBUG_TARGET_DIR = null;

    private FopFactory fopFactory;

    public ImageLoaderTestCase() {
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setSourceResolution(72);
        builder.setTargetResolution(300);
        fopFactory = builder.build();
    }

    @Test
    public void testSVG() throws Exception {
        String uri = "test/resources/images/img-w-size.svg";

        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, XMLNamespaceEnabledImageFlavor.SVG_DOM,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(XMLNamespaceEnabledImageFlavor.SVG_DOM, img.getFlavor());
        ImageXMLDOM imgDom = (ImageXMLDOM)img;
        assertNotNull(imgDom.getDocument());
        assertEquals("http://www.w3.org/2000/svg", imgDom.getRootNamespace());
        info = imgDom.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(16000, info.getSize().getWidthMpt());
        assertEquals(16000, info.getSize().getHeightMpt());

        img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        if (DEBUG_TARGET_DIR != null) {
            ImageWriterUtil.saveAsPNG(imgRed.getRenderedImage(),
                    (int)userAgent.getTargetResolution(),
                    new File(DEBUG_TARGET_DIR, "out.svg.png"));
        }
        assertEquals(67, imgRed.getRenderedImage().getWidth());
        assertEquals(67, imgRed.getRenderedImage().getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(16000, info.getSize().getWidthMpt());
        assertEquals(16000, info.getSize().getHeightMpt());
    }

    @Test
    public void testSVGNoViewbox() throws Exception {
        String uri = "test/resources/images/circles.svg";

        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setSourceResolution(96);
        builder.setTargetResolution(300);
        FopFactory ff = builder.build();

        FOUserAgent userAgent = ff.newFOUserAgent();

        ImageManager manager = ff.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, XMLNamespaceEnabledImageFlavor.SVG_DOM,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(XMLNamespaceEnabledImageFlavor.SVG_DOM, img.getFlavor());
        ImageXMLDOM imgDom = (ImageXMLDOM)img;
        assertNotNull(imgDom.getDocument());
        assertEquals("http://www.w3.org/2000/svg", imgDom.getRootNamespace());
        info = imgDom.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(96, info.getSize().getDpiHorizontal(), 0);
        assertEquals(340158, info.getSize().getWidthMpt());
        assertEquals(340158, info.getSize().getHeightMpt());
        assertEquals(454, info.getSize().getWidthPx());
        assertEquals(454, info.getSize().getHeightPx());

        img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        if (DEBUG_TARGET_DIR != null) {
            ImageWriterUtil.saveAsPNG(imgRed.getRenderedImage(),
                    (int)userAgent.getTargetResolution(),
                    new File(DEBUG_TARGET_DIR, "circles.svg.png"));
        }
        assertEquals(1418, imgRed.getRenderedImage().getWidth());
        assertEquals(1418, imgRed.getRenderedImage().getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(340158, info.getSize().getWidthMpt());
        assertEquals(340158, info.getSize().getHeightMpt());
    }

    @Test
    public void testWMF() throws Exception {
        String uri = "test/resources/images/testChart.wmf";

        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        assertNotNull(imgRed.getRenderedImage());
        if (DEBUG_TARGET_DIR != null) {
            ImageWriterUtil.saveAsPNG(imgRed.getRenderedImage(),
                    (int)userAgent.getTargetResolution(),
                    new File(DEBUG_TARGET_DIR, "out.wmf.png"));
        }
        assertEquals(3300, imgRed.getRenderedImage().getWidth());
        assertEquals(2550, imgRed.getRenderedImage().getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(792000, info.getSize().getWidthMpt());
        assertEquals(612000, info.getSize().getHeightMpt());
    }

    @Test
    public void testSVGWithReferences() throws Exception {
        String uri = "test/resources/fop/svg/images.svg";
        FopFactory ff = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent userAgent = ff.newFOUserAgent();

        ImageManager manager = ff.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, XMLNamespaceEnabledImageFlavor.SVG_DOM,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(XMLNamespaceEnabledImageFlavor.SVG_DOM, img.getFlavor());
        ImageXMLDOM imgDom = (ImageXMLDOM)img;
        assertNotNull(imgDom.getDocument());
        assertEquals("http://www.w3.org/2000/svg", imgDom.getRootNamespace());
        info = imgDom.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(400000, info.getSize().getWidthMpt());
        assertEquals(400000, info.getSize().getHeightMpt());
        assertEquals(400, info.getSize().getWidthPx());
        assertEquals(400, info.getSize().getHeightPx());

        img = manager.getImage(info, ImageFlavor.RENDERED_IMAGE,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RENDERED_IMAGE, img.getFlavor());
        ImageRendered imgRed = (ImageRendered)img;
        RenderedImage renImg = imgRed.getRenderedImage();
        assertNotNull(renImg);
        if (DEBUG_TARGET_DIR != null) {
            ImageWriterUtil.saveAsPNG(renImg,
                    (int)userAgent.getTargetResolution(),
                    new File(DEBUG_TARGET_DIR, "images.svg.png"));
        }
        assertEquals(400, renImg.getWidth());
        assertEquals(400, renImg.getHeight());
        info = imgRed.getInfo(); //Switch to the ImageInfo returned by the image
        assertEquals(400000, info.getSize().getWidthMpt());
        assertEquals(400000, info.getSize().getHeightMpt());
        Raster raster = renImg.getData();
        // This pixel is white
        int[] pixel1 = raster.getPixel(1, 1, (int[] )null);
        // This pixel is from the embedded JPG and is not white
        int[] pixel80 = raster.getPixel(80, 80, (int[]) null);
        assertEquals(pixel1.length, pixel80.length);
        boolean same = true;
        for (int i = 0; i < pixel1.length; i++) {
            same &= (pixel1[i] == pixel80[i]);
        }
        assertFalse("Embedding JPG into SVG failed", same);
    }
}
