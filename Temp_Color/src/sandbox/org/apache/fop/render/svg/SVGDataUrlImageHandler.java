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
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.uri.DataURLUtil;

import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.IFConstants;

/**
 * Image handler implementation that embeds JPEG bitmaps as RFC 2397 data URLs in the target SVG
 * file.
 */
public class SVGDataUrlImageHandler implements ImageHandler, SVGConstants {

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
            ImageFlavor.RAW_PNG,
            ImageFlavor.RAW_JPEG,
        };
    }

    private void addAttribute(AttributesImpl atts, QName attribute, String value) {
        atts.addAttribute(attribute.getNamespaceURI(),
                attribute.getLocalName(), attribute.getQName(), CDATA, value);
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        SVGRenderingContext svgContext = (SVGRenderingContext)context;
        ImageRawStream raw = (ImageRawStream)image;
        InputStream in = raw.createInputStream();
        try {
            ContentHandler handler = svgContext.getContentHandler();
            String url = DataURLUtil.createDataURL(in, raw.getMimeType());
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, IFConstants.XLINK_HREF, url);
            atts.addAttribute("", "x", "x", CDATA, Integer.toString(pos.x));
            atts.addAttribute("", "y", "y", CDATA, Integer.toString(pos.y));
            atts.addAttribute("", "width", "width", CDATA, Integer.toString(pos.width));
            atts.addAttribute("", "height", "height", CDATA, Integer.toString(pos.height));
            try {
                handler.startElement(NAMESPACE, "image", "image", atts);
                handler.endElement(NAMESPACE, "image", "image");
            } catch (SAXException e) {
                throw new IOException(e.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        return (image == null || image instanceof ImageRawStream)
                && targetContext instanceof SVGRenderingContext;
    }

}
