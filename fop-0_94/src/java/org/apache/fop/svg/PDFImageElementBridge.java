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

import org.apache.batik.bridge.SVGImageElementBridge;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.InputStream;

import org.w3c.dom.Element;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;

import org.apache.fop.image.JpegImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.analyser.ImageReaderFactory;

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
        try {
            InputStream is = purl.openStream();
            if (!is.markSupported()) {
                is = new BufferedInputStream(is, 1024);
            }
            
            is.mark(3);
            byte [] data = new byte[3];
            is.read(data);
            is.reset();
            if ((data[0] == (byte)0xFF) 
                    && (data[1] == (byte)0xD8) 
                    && (data[2] == (byte)0xFF)) {
                FopImage.ImageInfo ii = ImageReaderFactory.make
                        (purl.toString(), is, null);
                JpegImage jpeg = new JpegImage(ii);
                jpeg.load(FopImage.ORIGINAL_DATA);
                PDFJpegNode node = new PDFJpegNode(jpeg, ctx, imageElement, purl);
    
                Rectangle2D imgBounds = getImageBounds(ctx, imageElement);
                Rectangle2D bounds = node.getPrimitiveBounds();
                float [] vb = new float[4];
                vb[0] = 0; // x
                vb[1] = 0; // y
                vb[2] = (float) bounds.getWidth(); // width
                vb[3] = (float) bounds.getHeight(); // height
    
                // handles the 'preserveAspectRatio', 'overflow' and 'clip' 
                // and sets the appropriate AffineTransform to the image node
                initializeViewport(ctx, imageElement, node, vb, imgBounds);
                return node;
            }
        } catch (Exception ex) {
            //TODO Handle this exception
        }

        return superCreateGraphicsNode(ctx, imageElement, purl);
    }

    /**
     * @see org.apache.batik.bridge.SVGImageElementBridge
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
        
        private JpegImage jpeg;
        private BridgeContext ctx;
        private Element imageElement;
        private ParsedURL purl;
        private GraphicsNode origGraphicsNode = null;
        
        /**
         * Create a new pdf jpeg node for drawing jpeg images
         * into pdf graphics.
         * @param j the jpeg image
         * @param ctx the bridge context
         * @param imageElement the SVG image element
         * @param purl the URL to the image
         */
        public PDFJpegNode(JpegImage j, BridgeContext ctx, 
                           Element imageElement, ParsedURL purl) {
            this.jpeg = j;
            this.ctx  = ctx;
            this.imageElement = imageElement;
            this.purl = purl;
        }

        /**
         * Get the outline of this image.
         * @return the outline shape which is the primitive bounds
         */
        public Shape getOutline() {
            return getPrimitiveBounds();
        }

        /**
         * Paint this jpeg image.
         * As this is used for inserting jpeg into pdf
         * it adds the jpeg image to the PDFGraphics2D.
         * @param g2d the graphics to draw the image on
         */
        public void primitivePaint(Graphics2D g2d) {
            if (g2d instanceof PDFGraphics2D) {
                PDFGraphics2D pdfg = (PDFGraphics2D) g2d;
                float x = 0;
                float y = 0;
                try {
                    float width = jpeg.getWidth();
                    float height = jpeg.getHeight();
                    pdfg.addJpegImage(jpeg, x, y, width, height);
                } catch (Exception e) {
                    //TODO Handle this exception properly
                    e.printStackTrace();
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

        /**
         * Get the geometrix bounds of the image.
         * @return the primitive bounds
         */
        public Rectangle2D getGeometryBounds() {
            return getPrimitiveBounds();
        }

        /**
         * Get the primitive bounds of this bridge element.
         * @return the bounds of the jpeg image
         */
        public Rectangle2D getPrimitiveBounds() {
            try {
                return new Rectangle2D.Double(0, 0, jpeg.getWidth(),
                                              jpeg.getHeight());
            } catch (Exception e) {
                //TODO Handle this exception properly
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Returns the bounds of the sensitive area covered by this node,
         * This includes the stroked area but does not include the effects
         * of clipping, masking or filtering.
         * @return the bounds of the sensitive area
         */
        public Rectangle2D getSensitiveBounds() {
            //No interactive features, just return primitive bounds
            return getPrimitiveBounds();
        }

    }

}
