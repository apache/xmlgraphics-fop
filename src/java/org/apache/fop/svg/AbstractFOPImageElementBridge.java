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

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 * Bridge class for the &lt;image> element when jpeg images.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public abstract class AbstractFOPImageElementBridge extends SVGImageElementBridge {

    /**
     * Constructs a new bridge for the &lt;image> element.
     */
    public AbstractFOPImageElementBridge() { }

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
        AbstractFOPBridgeContext bridgeCtx = (AbstractFOPBridgeContext)ctx;

        ImageManager manager = bridgeCtx.getImageManager();
        ImageSessionContext sessionContext = bridgeCtx.getImageSessionContext();
        try {
            ImageInfo info = manager.getImageInfo(purl.toString(), sessionContext);
            ImageFlavor[] supportedFlavors = getSupportedFlavours();
            Image image = manager.getImage(info, supportedFlavors, sessionContext);

            //TODO color profile overrides aren't handled, yet!
            //ICCColorSpaceExt colorspaceOverride = extractColorSpace(e, ctx);
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
                specializedNode = createLoaderImageNode(image, ctx, imageElement, purl);
            } else if (image instanceof ImageRawCCITTFax) {
                specializedNode = createLoaderImageNode(image, ctx, imageElement, purl);
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
     * Returns an array of supported image flavours
     *
     * @return an array of supported image flavours
     */
    protected abstract ImageFlavor[] getSupportedFlavours();

    /**
     * Creates a loader image node implementation
     * @param purl the parsed url
     * @param imageElement the image element
     * @param ctx the batik bridge context
     * @param image the image
     *
     * @return a loader image node implementation
     */
    protected LoaderImageNode createLoaderImageNode(
            Image image, BridgeContext ctx, Element imageElement, ParsedURL purl) {
        return new LoaderImageNode(image, ctx, imageElement, purl);
    }

    /**
     * An image node for natively handled Image instance.
     * This holds a natively handled image so that it can be drawn into
     * the PDFGraphics2D.
     */
    public class LoaderImageNode extends AbstractGraphicsNode {

        protected final Image image;
        protected final BridgeContext ctx;
        protected final Element imageElement;
        protected final ParsedURL purl;
        protected GraphicsNode origGraphicsNode = null;

        /**
         * Create a new image node for drawing natively handled images
         * into PDF graphics.
         * @param image the JPEG image
         * @param ctx the bridge context
         * @param imageElement the SVG image element
         * @param purl the URL to the image
         */
        public LoaderImageNode(Image image, BridgeContext ctx,
                           Element imageElement, ParsedURL purl) {
            this.image = image;
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
            if (g2d instanceof NativeImageHandler) {
                NativeImageHandler nativeImageHandler = (NativeImageHandler) g2d;
                float x = 0;
                float y = 0;
                try {
                    float width = image.getSize().getWidthPx();
                    float height = image.getSize().getHeightPx();
                    nativeImageHandler.addNativeImage(image, x, y, width, height);
                } catch (Exception e) {
                    ctx.getUserAgent().displayError(e);
                }
            } else {
                // Not going directly into PDF so use
                // original implementation so filters etc work.
                if (origGraphicsNode == null) {
                    // Haven't constructed base class Graphics Node,
                    // so do so now.
                    origGraphicsNode
                        = superCreateGraphicsNode(ctx,  imageElement, purl);
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
                       image.getSize().getWidthPx(),
                       image.getSize().getHeightPx());
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

        private final ImageGraphics2D image;

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
            Graphics2DImagePainter painter = image.getGraphics2DImagePainter();
            painter.paint(g2d, area);
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
