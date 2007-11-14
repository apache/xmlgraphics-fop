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
 
package org.apache.fop.image2.impl.batik;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image.XMLImage;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.impl.AbstractImagePreloader;
import org.apache.fop.image2.impl.ImageXMLDOM;
import org.apache.fop.image2.util.ImageUtil;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.fop.util.UnclosableInputStream;
import org.apache.fop.util.UnitConv;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Image preloader for SVG images.
 */
public class PreloaderSVG extends AbstractImagePreloader {

    /** Logger instance */
    private static Log log = LogFactory.getLog(PreloaderSVG.class);

    private boolean batikAvailable = true;
    
    /** {@inheritDoc} */ 
    public ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
            throws IOException {
        if (!ImageUtil.hasInputStream(src)) {
            //TODO Remove this and support DOMSource and possibly SAXSource
            return null;
        }
        ImageInfo info = null;
        if (batikAvailable) {
            try {
                Loader loader = new Loader();
                return loader.getImage(uri, src, userAgent);
            } catch (NoClassDefFoundError e) {
                batikAvailable = false;
                log.warn("Batik not in class path", e);
                return null;
            }
        }
        if (info != null) {
            ImageUtil.closeQuietly(src); //Image is fully read
        }
        return info;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_SVG;
    }

    /**
     * This method is put in another class so that the class loader does not
     * attempt to load Batik related classes when constructing the SVGPreloader
     * class.
     */
    class Loader {
        private ImageInfo getImage(String uri, Source src,
                FOUserAgent userAgent) {
            // parse document and get the size attributes of the svg element

            InputStream in = new UnclosableInputStream(ImageUtil.needInputStream(src));
            try {
                int length = in.available();
                in.mark(length + 1);
                SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
                        XMLImage.getParserName());
                SVGDocument doc = (SVGDocument) factory.createSVGDocument(src.getSystemId(), in);

                Element e = doc.getRootElement();
                float pxUnitToMillimeter = (float)UnitConv.pt2mm(0.001); 
                SVGUserAgent userAg = new SVGUserAgent(pxUnitToMillimeter,
                            new AffineTransform());
                BridgeContext ctx = new BridgeContext(userAg);
                UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, e);

                String s;
                // 'width' attribute - default is 100%
                s = e.getAttributeNS(null, SVGOMDocument.SVG_WIDTH_ATTRIBUTE);
                if (s.length() == 0) {
                    s = SVGOMDocument.SVG_SVG_WIDTH_DEFAULT_VALUE;
                }
                int width = Math.round(UnitProcessor.svgHorizontalLengthToUserSpace(
                        s, SVGOMDocument.SVG_WIDTH_ATTRIBUTE, uctx));

                // 'height' attribute - default is 100%
                s = e.getAttributeNS(null, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE);
                if (s.length() == 0) {
                    s = SVGOMDocument.SVG_SVG_HEIGHT_DEFAULT_VALUE;
                }
                int height = Math.round(UnitProcessor.svgVerticalLengthToUserSpace(
                        s, SVGOMDocument.SVG_HEIGHT_ATTRIBUTE, uctx));

                ImageInfo info = new ImageInfo(uri, src, getMimeType());
                ImageSize size = new ImageSize();
                size.setSizeInMillipoints(width, height);
                //Set the resolution to that of the FOUserAgent
                size.setResolution(userAgent.getSourceResolution());
                size.calcPixelsFromSize();
                info.setSize(size);

                //The whole image had to be loaded for this, so keep it
                ImageXMLDOM xmlImage = new ImageXMLDOM(info,
                        doc, SVGDOMImplementation.SVG_NAMESPACE_URI);
                info.getCustomObjects().put(ImageInfo.ORIGINAL_IMAGE, xmlImage);
                
                return info;
            } catch (NoClassDefFoundError ncdfe) {
                try {
                    in.reset();
                } catch (IOException ioe) {
                    // we're more interested in the original exception
                }
                batikAvailable = false;
                log.warn("Batik not in class path", ncdfe);
                return null;
            } catch (IOException e) {
                // If the svg is invalid then it throws an IOException
                // so there is no way of knowing if it is an svg document

                log.debug("Error while trying to load stream as an SVG file: "
                                       + e.getMessage());
                // assuming any exception means this document is not svg
                // or could not be loaded for some reason
                try {
                    in.reset();
                } catch (IOException ioe) {
                    // we're more interested in the original exception
                }
                return null;
            }
        }
    }

}
