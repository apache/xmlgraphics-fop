/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.batik.bridge.SVGImageElementBridge;

import org.apache.fop.image.JpegImage;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.AbstractGraphicsNode;

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

/*
    /**
     * Create the raster image node.
     * THis checks if it is a jpeg file and creates a jpeg node
     * so the jpeg can be inserted directly into the pdf document.
     * @param ctx the bridge context
     * @param e the svg element for the image
     * @param purl the parsed url for the image resource
     * @return a new graphics node
     *
    protected GraphicsNode createRasterImageNode(BridgeContext ctx,
            Element e, ParsedURL purl) {

        try {
            JpegImage jpeg = new JpegImage(new URL(purl.toString()));
            PDFFilter filter = jpeg.getPDFFilter();
            PDFJpegNode node = new PDFJpegNode(jpeg);
            Rectangle2D bounds = node.getPrimitiveBounds();
            float [] vb = new float[4];
            vb[0] = 0; // x
            vb[1] = 0; // y
            vb[2] = (float) bounds.getWidth(); // width
            vb[3] = (float) bounds.getHeight(); // height

            // handles the 'preserveAspectRatio', 'overflow' and 'clip' and sets the
            // appropriate AffineTransform to the image node
            initializeViewport(ctx, e, node, vb, bounds);

            return node;
        } catch (Exception ex) {
        }

        return super.createRasterImageNode(ctx, e, purl);
    }
*/

    /**
     * A PDF jpeg node.
     * This holds a jpeg image so that it can be drawn into
     * the PDFGraphics2D.
     */
    public static class PDFJpegNode extends AbstractGraphicsNode {
        private JpegImage jpeg;

        /**
         * Create a new pdf jpeg node for drawing jpeg images
         * into pdf graphics.
         * @param j the jpeg image
         */
        public PDFJpegNode(JpegImage j) {
            jpeg = j;
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
                pdfg.setTransform(getTransform());
                float x = 0;
                float y = 0;
                try {
                    float width = jpeg.getWidth();
                    float height = jpeg.getHeight();
                    pdfg.addJpegImage(jpeg, x, y, width, height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                e.printStackTrace();
            }
            return null;
        }
    }

}
