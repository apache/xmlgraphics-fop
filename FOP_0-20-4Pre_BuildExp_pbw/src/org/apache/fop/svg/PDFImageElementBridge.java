/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.Cursor;
import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.*;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.fop.pdf.*;
import org.apache.fop.image.*;
import org.apache.fop.apps.*;

import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGAElement;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.color.ICC_Profile;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.color.ICCColorSpaceExt;
import org.apache.batik.ext.awt.image.renderable.ClipRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.gvt.RasterImageNode;
import org.apache.batik.gvt.AbstractGraphicsNode;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;
import org.apache.batik.util.ParsedURL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Bridge class for the &lt;image> element when jpeg images.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 */
public class PDFImageElementBridge extends SVGImageElementBridge {

    /**
     * Constructs a new bridge for the &lt;image> element.
     */
    public PDFImageElementBridge() {}
/*
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
    public static class PDFJpegNode extends AbstractGraphicsNode {
        JpegImage jpeg;
        public PDFJpegNode(JpegImage j) {
            jpeg = j;
        }

        public Shape getOutline() {
            return getPrimitiveBounds();
        }

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

        public Rectangle2D getGeometryBounds() {
            return getPrimitiveBounds();
        }

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
