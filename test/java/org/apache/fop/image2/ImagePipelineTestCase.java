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

package org.apache.fop.image2;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.image2.impl.ImageConverterBuffered2Rendered;
import org.apache.fop.image2.impl.ImageConverterG2D2Bitmap;
import org.apache.fop.image2.impl.ImageConverterRendered2PNG;
import org.apache.fop.image2.impl.ImageRawStream;
import org.apache.fop.image2.impl.ImageXMLDOM;
import org.apache.fop.image2.impl.batik.ImageConverterSVG2G2D;
import org.apache.fop.image2.impl.batik.PreloaderSVG;
import org.apache.fop.image2.impl.imageio.ImageLoaderImageIO;
import org.apache.fop.image2.pipeline.ImageProviderPipeline;
import org.apache.fop.image2.spi.ImageLoader;

/**
 * Tests for the image pipeline functionality.
 */
public class ImagePipelineTestCase extends TestCase {

    private FopFactory fopFactory;
    
    public ImagePipelineTestCase(String name) {
        super(name);
        fopFactory = FopFactory.newInstance();
        fopFactory.setSourceResolution(72);
        fopFactory.setTargetResolution(300);
    }
    
    public void testPipelineWithLoader() throws Exception {
        String uri = "test/resources/images/bgimg72dpi.gif";

        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ImageManager manager = fopFactory.getImageManager();
        
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        
        ImageLoader loader = new ImageLoaderImageIO(ImageFlavor.RENDERED_IMAGE);
        ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), loader);
        pipeline.addConverter(new ImageConverterRendered2PNG());
        
        Image img = pipeline.execute(info, null, userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
    private ImageXMLDOM createSVGImage() throws IOException {
        File svgFile = new File("test/resources/images/img-w-size.svg");
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
                PreloaderSVG.getParserName());
        SVGDocument doc = (SVGDocument)factory.createSVGDocument(
                svgFile.toURL().toExternalForm());
        
        //We simulate an instream-foreign-object where there is no original URI for the image.
        //We also don't "know" the MIME type.
        ImageInfo info = new ImageInfo(null /*null is the intention here*/, null);
        info.setSize(new ImageSize(72, 72, 72));
        info.getSize().calcSizeFromPixels();
        
        ImageXMLDOM svgImage = new ImageXMLDOM(info,
                doc, doc.getDocumentElement().getNamespaceURI());
        return svgImage;
    }
    
    public void testPipelineWithoutLoader() throws Exception {

        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ImageManager manager = fopFactory.getImageManager();

        ImageXMLDOM svgImage = createSVGImage();
        
        ImageProviderPipeline pipeline = new ImageProviderPipeline(manager.getCache(), null);
        pipeline.addConverter(new ImageConverterSVG2G2D());
        pipeline.addConverter(new ImageConverterG2D2Bitmap());
        pipeline.addConverter(new ImageConverterBuffered2Rendered());
        pipeline.addConverter(new ImageConverterRendered2PNG());
        
        Image img = pipeline.execute(svgImage.getInfo(), svgImage, null,
                    userAgent.getImageSessionContext());
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
    public void testPipelineFromURIThroughManager() throws Exception {
        String uri = "examples/fo/graphics/asf-logo.png";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ImageManager manager = fopFactory.getImageManager();
        
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);

        ImageFlavor[] flavors = new ImageFlavor[] {
                ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
        };
        Image img = manager.getImage(info, flavors, userAgent.getImageSessionContext());
        
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
    public void testPipelineWithoutURIThroughManager() throws Exception {
        
        ImageManager manager = fopFactory.getImageManager();
        
        ImageXMLDOM svgImage = createSVGImage();

        ImageFlavor[] flavors = new ImageFlavor[] {
                ImageFlavor.RAW_PNG, ImageFlavor.RAW_JPEG
        };
        Image img = manager.convertImage(svgImage, flavors);
        
        assertNotNull("Image must not be null", img);
        assertEquals(ImageFlavor.RAW_PNG, img.getFlavor());
        assertTrue(img instanceof ImageRawStream);
    }
    
}
