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

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import junit.framework.TestCase;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * Tests for bundled image preloader implementations.
 */
public class ImagePreloaderTestCase extends TestCase {

    private FopFactory fopFactory;
    
    public ImagePreloaderTestCase(String name) {
        super(name);
        fopFactory = FopFactory.newInstance();
        fopFactory.setSourceResolution(72);
        fopFactory.setTargetResolution(300);
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

    public void testSVGWithDOM() throws Exception {
        String uri = "my:SVGImage";
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        
        userAgent.setURIResolver(new URIResolver() {

            public Source resolve(String href, String base) throws TransformerException {
                if (href.startsWith("my:")) {
                    DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
                    String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
                    Document doc = impl.createDocument(svgNS, "svg", null);
                    Element element = doc.getDocumentElement();
                    element.setAttribute("viewBox", "0 0 20 20");
                    element.setAttribute("width", "20pt");
                    element.setAttribute("height", "20pt");
                    
                    Element rect = doc.createElementNS(svgNS, "rect");
                    rect.setAttribute("x", "5");
                    rect.setAttribute("y", "5");
                    rect.setAttribute("width", "10");
                    rect.setAttribute("height", "10");
                    element.appendChild(rect);
                    
                    DOMSource src = new DOMSource(doc);
                    return src;
                } else {
                    return null;
                }
            }
            
        });
        
        ImageManager manager = fopFactory.getImageManager();
        ImageInfo info = manager.preloadImage(uri, userAgent.getImageSessionContext());
        assertNotNull("ImageInfo must not be null", info);
        assertEquals(MimeConstants.MIME_SVG, info.getMimeType());
        assertEquals(uri, info.getOriginalURI());
        assertEquals(20, info.getSize().getWidthPx()); //100 = default viewport size
        assertEquals(20, info.getSize().getHeightPx());
        assertEquals(userAgent.getSourceResolution(), info.getSize().getDpiHorizontal(), 0.1);
        assertEquals(20000, info.getSize().getWidthMpt());
        assertEquals(20000, info.getSize().getHeightMpt());
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
 
}
