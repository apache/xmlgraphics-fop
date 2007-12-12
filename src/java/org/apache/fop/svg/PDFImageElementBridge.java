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
 
package org.apache.fop.svg;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageManager;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.impl.ImageGraphics2D;
import org.apache.fop.image2.impl.ImageRawJPEG;
import org.apache.fop.image2.impl.ImageXMLDOM;

/**
 * Bridge class for the &lt;image> element when jpeg images.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFImageElementBridge extends SVGImageElementBridge {

    /**
     * Constructs a new bridge for the &lt;image> element.
     */
    public PDFImageElementBridge() { }

    private final ImageFlavor[] supportedFlavors = new ImageFlavor[]
                                               {ImageFlavor.RAW_JPEG,
                                                ImageFlavor.GRAPHICS2D,
                                                ImageFlavor.XML_DOM};
    /**
     * Create the raster image node.
     * THis checks if it is a jpeg file and creates a jpeg node
     * so the jpeg can be inserted directly into the pdf document.
     * @param ctx the bridge context
     * @param imageElement the svg element for the image
     * @param purl the parsed url for the image resource
     * @return a new graphics node
     */
    protected GraphicsNode createImageGraphicsNode
                (BridgeContext ctx, Element imageElement, ParsedURL purl) {
        PDFBridgeContext pdfCtx = (PDFBridgeContext)ctx;
        
        ImageManager manager = pdfCtx.getImageManager();
        ImageSessionContext sessionContext = pdfCtx.getImageSessionContext();
        try {
            ImageInfo info = manager.getImageInfo(purl.toString(), sessionContext);
            Image image = manager.getImage(info, supportedFlavors, sessionContext);
            
            AbstractGraphicsNode specializedNode = null;
            if (image instanceof ImageXMLDOM) {
                ImageXMLDOM xmlImage = (ImageXMLDOM)image;
                if (xmlImage.getDocument() instanceof SVGDocument) {
                    return createSVGImageNode(ctx, imageElement,
                            (SVGDocument)xmlImage.getDocument());
                } else {
                    //Convert image to Graphics2D
                    image = manager.convertImage(xmlImage,
                            new ImageFlavor[] {ImageFlavor.GRAPHICS2D});
                }
            }
            if (image instanceof ImageRawJPEG) {
                ImageRawJPEG jpegImage = (ImageRawJPEG)image;
                specializedNode = new PDFJpegNode(jpegImage, ctx, imageElement, purl);
                
            } else if (image instanceof ImageGraphics2D) {
                ImageGraphics2D g2dImage = (ImageGraphics2D)image;
                specializedNode = new Graphics2DNode(g2dImage);
            } else {
                ctx.getUserAgent().displayError(
                        new ImageException("Cannot convert an image to a usable format: " + purl));
            }
            
            Rectangle2D imgBounds = getImageBounds(ctx, imageElement);
            Rectangle2D bounds = specializedNode.getPrimitiveBounds();
            float [] vb = new float[4];
            vb[0] = 0; // x
            vb[1] = 0; // y
            vb[2] = (float) bounds.getWidth(); // width
            vb[3] = (float) bounds.getHeight(); // height

            // handles the 'preserveAspectRatio', 'overflow' and 'clip' 
            // and sets the appropriate AffineTransform to the image node
            initializeViewport(ctx, imageElement, specializedNode, vb, imgBounds);
            return specializedNode;
        } catch (Exception e) {
            ctx.getUserAgent().displayError(e);
        }
        
        return superCreateGraphicsNode(ctx, imageElement, purl);
    }

    /**
     * Calls the superclass' createImageGraphicNode() method to create the normal GraphicsNode.
     * @param ctx the bridge context
     * @param imageElement the image element
     * @param purl the parsed URL
     * @return the newly created graphics node
     * @see org.apache.batik.bridge.SVGImageElementBridge#createGraphicsNode(BridgeContext, Element)
     */
    protected GraphicsNode superCreateGraphicsNode
            (BridgeContext ctx, Element imageElement, ParsedURL purl) {
        return super.createImageGraphicsNode(ctx, imageElement, purl);
    }


    /**
     * A PDF jpeg node.
     * This holds a jpeg image so that it can be drawn into
     * the PDFGraphics2D.
     */
    public class PDFJpegNode extends AbstractGraphicsNode {
        
        private ImageRawJPEG jpeg;
        private BridgeContext ctx;
        private Element imageElement;
        private ParsedURL purl;
        private GraphicsNode origGraphicsNode = null;
        
        /**
         * Create a new PDF JPEG node for drawing JPEG images
         * into pdf graphics.
         * @param j the JPEG image
         * @param ctx the bridge context
         * @param imageElement the SVG image element
         * @param purl the URL to the image
         */
        public PDFJpegNode(ImageRawJPEG j, BridgeContext ctx, 
                           Element imageElement, ParsedURL purl) {
            this.jpeg = j;
            this.ctx  = ctx;
            this.imageElement = imageElement;
            this.purl = purl;
        }

        /** {@inheritDoc} */
        public Shape getOutline() {
            return getPrimitiveBounds();
        }

        /** {@inheritDoc} */
        public void primitivePaint(Graphics2D g2d) {
            if (g2d instanceof PDFGraphics2D) {
                PDFGraphics2D pdfg = (PDFGraphics2D) g2d;
                float x = 0;
                float y = 0;
                try {
                    float width = jpeg.getSize().getWidthPx();
                    float height = jpeg.getSize().getHeightPx();
                    pdfg.addJpegImage(jpeg, x, y, width, height);
                } catch (Exception e) {
                    ctx.getUserAgent().displayError(e);
                }
            } else {
                // Not going directly into PDF so use
                // original implementation so filters etc work.
                if (origGraphicsNode == null) {
                    // Haven't constructed baseclass Graphics Node,
                    // so do so now.
                    origGraphicsNode 
                        = PDFImageElementBridge.this.superCreateGraphicsNode
                            (ctx,  imageElement, purl);
                }
                origGraphicsNode.primitivePaint(g2d);
            }
        }

        /** {@inheritDoc} */
        public Rectangle2D getGeometryBounds() {
            return getPrimitiveBounds();
        }

        /** {@inheritDoc} */
        public Rectangle2D getPrimitiveBounds() {
            return new Rectangle2D.Double(0, 0,
                       jpeg.getSize().getWidthPx(),
                       jpeg.getSize().getHeightPx());
        }

        /** {@inheritDoc} */
        public Rectangle2D getSensitiveBounds() {
            //No interactive features, just return primitive bounds
            return getPrimitiveBounds();
        }

    }

    /**
     * A node that holds a Graphics2D image.
     */
    public class Graphics2DNode extends AbstractGraphicsNode {
        
        private ImageGraphics2D image;
        
        /**
         * Create a new Graphics2D node.
         * @param g2d the Graphics2D image
         */
        public Graphics2DNode(ImageGraphics2D g2d) {
            this.image = g2d;
        }

        /** {@inheritDoc} */
        public Shape getOutline() {
            return getPrimitiveBounds();
        }

        /** {@inheritDoc} */
        public void primitivePaint(Graphics2D g2d) {
            int width = image.getSize().getWidthPx();
            int height = image.getSize().getHeightPx();
            Rectangle2D area = new Rectangle2D.Double(0, 0, width, height);
            image.getGraphics2DImagePainter().paint(g2d, area);
        }

        /** {@inheritDoc} */
        public Rectangle2D getGeometryBounds() {
            return getPrimitiveBounds();
        }

        /** {@inheritDoc} */
        public Rectangle2D getPrimitiveBounds() {
            return new Rectangle2D.Double(0, 0,
                    image.getSize().getWidthPx(),
                    image.getSize().getHeightPx());
        }

        /** {@inheritDoc} */
        public Rectangle2D getSensitiveBounds() {
            //No interactive features, just return primitive bounds
            return getPrimitiveBounds();
        }

    }
}
