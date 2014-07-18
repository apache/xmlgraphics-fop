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

package org.apache.fop.render.svg;

import java.awt.Rectangle;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.util.QName;

import org.apache.fop.image.loader.batik.BatikImageFlavors;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.DelegatingFragmentContentHandler;

/**
 * Image handler implementation that embeds SVG images in the target SVG file.
 */
public class EmbeddedSVGImageHandler implements ImageHandler, SVGConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(EmbeddedSVGImageHandler.class);

    /** Constant for the "CDATA" attribute type. */
    private static final String CDATA = "CDATA";

    /** {@inheritDoc} */
    public int getPriority() {
        return 500;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRawStream.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return new ImageFlavor[] {
            BatikImageFlavors.SVG_DOM
        };
    }

    private void addAttribute(AttributesImpl atts, QName attribute, String value) {
        atts.addAttribute(attribute.getNamespaceURI(),
                attribute.getLocalName(), attribute.getQName(), CDATA, value);
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, final Rectangle pos)
            throws IOException {
        SVGRenderingContext svgContext = (SVGRenderingContext)context;
        ImageXMLDOM svg = (ImageXMLDOM)image;
        ContentHandler handler = svgContext.getContentHandler();
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "x", "x", CDATA, SVGUtil.formatMptToPt(pos.x));
        atts.addAttribute("", "y", "y", CDATA, SVGUtil.formatMptToPt(pos.y));
        atts.addAttribute("", "width", "width", CDATA, SVGUtil.formatMptToPt(pos.width));
        atts.addAttribute("", "height", "height", CDATA, SVGUtil.formatMptToPt(pos.height));
        try {

            Document doc = (Document)svg.getDocument();
            Element svgEl = (Element)doc.getDocumentElement();
            if (svgEl.getAttribute("viewBox").length() == 0) {
                log.warn("SVG doesn't have a viewBox. The result might not be scaled correctly!");
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource src = new DOMSource(svg.getDocument());
            SAXResult res = new SAXResult(new DelegatingFragmentContentHandler(handler) {

                private boolean topLevelSVGFound;

                private void setAttribute(AttributesImpl atts, String localName, String value) {
                    int index;
                    index = atts.getIndex("", localName);
                    if (index < 0) {
                        atts.addAttribute("", localName, localName, CDATA, value);
                    } else {
                        atts.setAttribute(index, "", localName, localName, CDATA, value);
                    }
                }

                public void startElement(String uri, String localName, String name, Attributes atts)
                        throws SAXException {
                    if (!topLevelSVGFound
                            && SVG_ELEMENT.getNamespaceURI().equals(uri)
                            && SVG_ELEMENT.getLocalName().equals(localName)) {
                        topLevelSVGFound = true;
                        AttributesImpl modAtts = new AttributesImpl(atts);
                        setAttribute(modAtts, "x", SVGUtil.formatMptToPt(pos.x));
                        setAttribute(modAtts, "y", SVGUtil.formatMptToPt(pos.y));
                        setAttribute(modAtts, "width", SVGUtil.formatMptToPt(pos.width));
                        setAttribute(modAtts, "height", SVGUtil.formatMptToPt(pos.height));
                        super.startElement(uri, localName, name, modAtts);
                    } else {
                        super.startElement(uri, localName, name, atts);
                    }
                }

            });
            transformer.transform(src, res);
        } catch (TransformerException te) {
            throw new IOException(te.getMessage());
        }
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        if (targetContext instanceof SVGRenderingContext) {
            if (image == null) {
                return true;
            }
            if (image instanceof ImageXMLDOM) {
                ImageXMLDOM svg = (ImageXMLDOM)image;
                return NAMESPACE.equals(svg.getRootNamespace());
            }
        }
        return false;
    }

}
