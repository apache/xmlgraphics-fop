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

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.impl.batik.ImageWMF;
import org.apache.fop.image2.spi.ImageLoaderFactory;

/**
 * Tests for bundled Imagepreloader implementations.
 */
public class ImagePreloaderTestCase extends TestCase {

    private FopFactory fopFactory;
    
    public ImagePreloaderTestCase(String name) {
        super(name);
        fopFactory = FopFactory.newInstance();
        fopFactory.setSourceResolution(72);
        fopFactory.setTargetResolution(300);
    }
    
    public void testImageLoaderFactory() throws Exception {
        ImageManager manager = fopFactory.getImageManager();
        ImageLoaderFactory ilf = manager.getRegistry().getImageLoaderFactory(
                MimeConstants.MIME_PNG, ImageFlavor.BUFFERED_IMAGE);
        assertNotNull(ilf);
    }
    
    public void testFileNotFound() throws Exception {
        String uri = "doesnotexistanywhere.png";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        ImageManager manager = fopFactory.getImageManager();
        try {
            ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
            fail("Expected a FileNotFoundException!");
        } catch (FileNotFoundException e) {
            //expected!
        }
    }
    
    public void testPNG() throws Exception {
        String uri = "examples/fo/graphics/asf-logo.png";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_PNG, info.getMimeType());
        assertEquals("examples/fo/graphics/asf-logo.png", info.getOriginalURI());
        assertEquals(169, info.getSize().getWidthPx());
        assertEquals(51, info.getSize().getHeightPx());
        assertEquals(96, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(126734, info.getSize().getWidthMpt());
        assertEquals(38245, info.getSize().getHeightMpt());
    }
    
    public void testTIFF() throws Exception {
        String uri = "test/resources/images/tiff_group4.tif";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_TIFF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(1560, info.getSize().getWidthPx());
        assertEquals(189, info.getSize().getHeightPx());
        assertEquals(204, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(550588, info.getSize().getWidthMpt());
        assertEquals(66706, info.getSize().getHeightMpt());
    }
    
    public void testTIFFNoResolution() throws Exception {
        String uri = "test/resources/images/no-resolution.tif";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_TIFF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(51, info.getSize().getWidthPx());
        assertEquals(24, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(51000, info.getSize().getWidthMpt());
        assertEquals(24000, info.getSize().getHeightMpt());
    }
    
    public void testGIF() throws Exception {
        String uri = "test/resources/images/bgimg72dpi.gif";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_GIF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(192000, info.getSize().getWidthMpt());
        assertEquals(192000, info.getSize().getHeightMpt());
    }
    
    public void testSVG() throws Exception {
        String uri = "test/resources/images/img-w-size.svg";
        
        checkSVGFile(uri);
    }

    public void testSVGZ() throws Exception {
        String uri = "test/resources/images/img-w-size.svgz";
        
        checkSVGFile(uri);
    }

    private void checkSVGFile(String uri) throws ImageException, IOException {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_SVG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(16, info.getSize().getWidthPx());
        assertEquals(16, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(16000, info.getSize().getWidthMpt());
        assertEquals(16000, info.getSize().getHeightMpt());
    }
    
    public void testSVGNoSize() throws Exception {
        String uri = "test/resources/images/img.svg";
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_SVG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(100, info.getSize().getWidthPx()); //100 = default viewport size
        assertEquals(100, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(100000, info.getSize().getWidthMpt());
        assertEquals(100000, info.getSize().getHeightMpt());
    }

    public void testWMF() throws Exception {
        String uri = "test/resources/images/testChart.wmf";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(ImageWMF.MIME_WMF, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(27940, info.getSize().getWidthPx());
        assertEquals(21590, info.getSize().getHeightPx());
        assertEquals(2540, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(792000, info.getSize().getWidthMpt());
        assertEquals(612000, info.getSize().getHeightMpt());
    }
 
    public void testEMF() throws Exception {
        String uri = "test/resources/images/img.emf";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/emf", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(76, info.getSize().getWidthPx());
        assertEquals(76, info.getSize().getHeightPx());
        assertEquals(96, info.getSize().getDpiHorizontal(), 1.0);
        assertEquals(56665, info.getSize().getWidthMpt());
        assertEquals(56665, info.getSize().getHeightMpt());
    }
 
    public void testJPEG1() throws Exception {
        String uri = "test/resources/images/bgimg300dpi.jpg";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_JPEG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(300, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(46080, info.getSize().getWidthMpt());
        assertEquals(46080, info.getSize().getHeightMpt());
    }
 
    public void testJPEG2() throws Exception {
        String uri = "test/resources/images/cmyk.jpg";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_JPEG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(160, info.getSize().getWidthPx());
        assertEquals(35, info.getSize().getHeightPx());
        assertEquals(72, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(160000, info.getSize().getWidthMpt());
        assertEquals(35000, info.getSize().getHeightMpt());
    }
 
    public void testBMP() throws Exception {
        String uri = "test/resources/images/bgimg300dpi.bmp";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/bmp", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(192, info.getSize().getWidthPx());
        assertEquals(192, info.getSize().getHeightPx());
        assertEquals(300, info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(46092, info.getSize().getWidthMpt());
        assertEquals(46092, info.getSize().getHeightMpt());
    }
 
    public void testBMPNoResolution() throws Exception {
        String uri = "test/resources/images/no-resolution.bmp";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals("image/bmp", info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(50, info.getSize().getWidthPx());
        assertEquals(50, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(50000, info.getSize().getWidthMpt());
        assertEquals(50000, info.getSize().getHeightMpt());
    }
 
    public void testEPSAscii() throws Exception {
        String uri = "test/resources/images/barcode.eps";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_EPS, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(136, info.getSize().getWidthPx());
        assertEquals(43, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(135655, info.getSize().getWidthMpt());
        assertEquals(42525, info.getSize().getHeightMpt());
    }
 
    public void testEPSBinary() throws Exception {
        String uri = "test/resources/images/img-with-tiff-preview.eps";
        
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_EPS, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(17, info.getSize().getWidthPx());
        assertEquals(17, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(17000, info.getSize().getWidthMpt());
        assertEquals(17000, info.getSize().getHeightMpt());
    }
 
}
