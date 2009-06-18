/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.svg;

import org.apache.batik.bridge.SVGImageElementBridge;

import org.apache.fop.image.JpegImage;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
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

        public Rectangle2D getSensitiveBounds() {
            return getPrimitiveBounds();
        }

    }

}
