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

import java.io.File;

import junit.framework.TestCase;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.writer.ImageWriterUtil;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;

/**
 * Tests for bundled ImageLoader implementations.
 */
public class ImageLoaderTestCase extends TestCase {

    private static final File DEBUG_TARGET_DIR = null; //new File("D:/");

    private FopFactory fopFactory;

    public ImageLoaderTestCase(String name) {
        super(name);
        fopFactory = FopFactory.newInstance();
        fopFactory.setSourceResolution(72);
        fopFactory.setTargetResolution(300);
    }

    public void testSVG() throws Exception {
        String uri = "test/resources/images/img-w-size.svg";

        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        Image img = manager.getImage(info, ImageFlavor.XML_DOM,
                userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.XML_DOM, img.getFlavor());
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

}
