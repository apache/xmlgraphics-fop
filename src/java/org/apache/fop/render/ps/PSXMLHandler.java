/*
 * $Id: PSXMLHandler.java,v 1.4 2003/03/11 08:42:24 jeremias Exp $
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
package org.apache.fop.render.ps;

// Java
import java.awt.geom.AffineTransform;
import java.io.IOException;

// DOM
/* org.w3c.dom.Document is not imported to avoid conflict with
   org.apache.fop.control.Document */
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

// Batik
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextPainter;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;
import org.apache.fop.fo.extensions.svg.SVGUserAgent;
import org.apache.fop.apps.*;

/**
 * PostScript XML handler.
 * This handler handles XML for foreign objects when rendering to PostScript.
 * It renders SVG to the PostScript document using the PSGraphics2D.
 * The properties from the PostScript renderer are subject to change.
 *
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @version $Id: PSXMLHandler.java,v 1.4 2003/03/11 08:42:24 jeremias Exp $
 */
public class PSXMLHandler implements XMLHandler {

    /**
     * The PostScript generator that is being used to drawn into.
     */
    public static final String PS_GENERATOR = "psGenerator";

    /**
     * The font information for the PostScript renderer.
     */
    public static final String PS_FONT_INFO = "psFontInfo";

    /**
     * The width of the SVG graphic.
     */
    public static final String PS_WIDTH = "width";

    /**
     * The height of the SVG graphic.
     */
    public static final String PS_HEIGHT = "height";

    /**
     * The x position that this is being drawn at.
     */
    public static final String PS_XPOS = "xpos";

    /**
     * The y position that this is being drawn at.
     */
    public static final String PS_YPOS = "ypos";

    /**
     * Create a new PostScript XML handler for use by the PostScript renderer.
     */
    public PSXMLHandler() {
    }

    /**
     * Handle the XML.
     * This checks the type of XML and handles appropraitely.
     *
     * @param context the renderer context
     * @param doc the XML document to render
     * @param ns the namespace of the XML document
     * @throws Exception any sort of exception could be thrown and shuld be handled
     */
    public void handleXML(RendererContext context, org.w3c.dom.Document doc,
                          String ns) throws Exception {
        PSInfo psi = getPSInfo(context);

        String svg = "http://www.w3.org/2000/svg";
        if (svg.equals(ns)) {
            SVGHandler svghandler = new SVGHandler();
            svghandler.renderSVGDocument(context, doc, psi);
        } else {
        }
    }

    /**
     * Get the pdf information from the render context.
     *
     * @param context the renderer context
     * @return the pdf information retrieved from the context
     */
    public static PSInfo getPSInfo(RendererContext context) {
        PSInfo psi = new PSInfo();
        psi.psGenerator = (PSGenerator)context.getProperty(PS_GENERATOR);
        psi.fontInfo = (org.apache.fop.apps.Document)context.getProperty(PS_FONT_INFO);
        psi.width = ((Integer)context.getProperty(PS_WIDTH)).intValue();
        psi.height = ((Integer)context.getProperty(PS_HEIGHT)).intValue();
        psi.currentXPosition = ((Integer)context.getProperty(PS_XPOS)).intValue();
        psi.currentYPosition = ((Integer)context.getProperty(PS_YPOS)).intValue();
        return psi;
    }

    /**
     * PostScript information structure for drawing the XML document.
     */
    public static class PSInfo {

        /** see PS_GENERATOR */
        private PSGenerator psGenerator;
        /** see PS_FONT_INFO */
        private org.apache.fop.apps.Document fontInfo;
        /** see PS_PAGE_WIDTH */
        private int width;
        /** see PS_PAGE_HEIGHT */
        private int height;
        /** see PS_XPOS */
        private int currentXPosition;
        /** see PS_YPOS */
        private int currentYPosition;
        /**
         * Returns the PSGenerator.
         * @return PSGenerator
         */
        public PSGenerator getPSGenerator() {
            return psGenerator;
        }

        /**
         * Sets the PSGenerator.
         * @param psGenerator The PSGenerator to set
         */
        public void setPsGenerator(PSGenerator psGenerator) {
            this.psGenerator = psGenerator;
        }

        /**
         * Returns the fontInfo.
         * @return FontInfo
         */
        public org.apache.fop.apps.Document getFontInfo() {
            return fontInfo;
        }

        /**
         * Sets the fontInfo.
         * @param fontInfo The fontInfo to set
         */
        public void setFontInfo(org.apache.fop.apps.Document fontInfo) {
            this.fontInfo = fontInfo;
        }

        /**
         * Returns the currentXPosition.
         * @return int
         */
        public int getCurrentXPosition() {
            return currentXPosition;
        }

        /**
         * Sets the currentXPosition.
         * @param currentXPosition The currentXPosition to set
         */
        public void setCurrentXPosition(int currentXPosition) {
            this.currentXPosition = currentXPosition;
        }

        /**
         * Returns the currentYPosition.
         * @return int
         */
        public int getCurrentYPosition() {
            return currentYPosition;
        }

        /**
         * Sets the currentYPosition.
         * @param currentYPosition The currentYPosition to set
         */
        public void setCurrentYPosition(int currentYPosition) {
            this.currentYPosition = currentYPosition;
        }

        /**
         * Returns the width.
         * @return int
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width.
         * @param width The pageWidth to set
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * Returns the height.
         * @return int
         */
        public int getHeight() {
            return height;
        }

        /**
         * Sets the height.
         * @param height The height to set
         */
        public void setHeight(int height) {
            this.height = height;
        }

    }

    /**
     * This method is placed in an inner class so that we don't get class
     * loading errors if batik is not present.
     */
    protected class SVGHandler {
        /**
         * Render the svg document.
         * @param context the renderer context
         * @param doc the svg document
         * @param psInfo the pdf information of the current context
         */
        protected void renderSVGDocument(RendererContext context,
                org.w3c.dom.Document doc, PSInfo psInfo) {
            int xOffset = psInfo.currentXPosition;
            int yOffset = psInfo.currentYPosition;
            PSGenerator gen = psInfo.psGenerator;

            SVGUserAgent ua
                 = new SVGUserAgent(context.getUserAgent(), new AffineTransform());


            GVTBuilder builder = new GVTBuilder();
            BridgeContext ctx = new BridgeContext(ua);
            PSTextElementBridge tBridge = new PSTextElementBridge(psInfo.getFontInfo());
            ctx.putBridge(tBridge);

            //PSAElementBridge aBridge = new PSAElementBridge();
            // to get the correct transform we need to use the PDFState
            AffineTransform transform = gen.getCurrentState().getTransform();
            transform.translate(xOffset / 1000f, yOffset / 1000f);
            //aBridge.setCurrentTransform(transform);
            //ctx.putBridge(aBridge);

            TextPainter textPainter = new PSTextPainter(psInfo.getFontInfo());
            ctx.setTextPainter(textPainter);
            GraphicsNode root;
            try {
                root = builder.build(ctx, doc);
            } catch (Exception e) {
                context.getUserAgent().getLogger().error("SVG graphic could not be built: "
                                       + e.getMessage(), e);
                return;
            }
            // get the 'width' and 'height' attributes of the SVG document
            float w = (float)ctx.getDocumentSize().getWidth() * 1000f;
            float h = (float)ctx.getDocumentSize().getHeight() * 1000f;

            float sx = psInfo.getWidth() / (float)w;
            float sy = psInfo.getHeight() / (float)h;

            ctx = null;
            builder = null;

            try {
                gen.writeln("%SVG graphic start ---");
                /*
                 * Clip to the svg area.
                 * Note: To have the svg overlay (under) a text area then use
                 * an fo:block-container
                 */
                gen.saveGraphicsState();
                // transform so that the coordinates (0,0) is from the top left
                // and positive is down and to the right. (0,0) is where the
                // viewBox puts it.
                gen.concatMatrix(sx, 0, 0, sy, xOffset, yOffset);

                SVGSVGElement svg = ((SVGDocument)doc).getRootElement();
                AffineTransform at = ViewBox.getPreserveAspectRatioTransform(svg,
                                    w / 1000f, h / 1000f);
                if (!at.isIdentity()) {
                    double[] vals = new double[6];
                    at.getMatrix(vals);
                    gen.concatMatrix(vals);
                }

                /*
                if (psInfo.pdfContext == null) {
                    psInfo.pdfContext = psInfo.pdfPage;
                }*/
                PSGraphics2D graphics = new PSGraphics2D(true, gen);
                graphics.setGraphicContext(new org.apache.batik.ext.awt.g2d.GraphicContext());
                //psInfo.pdfState.push();
                transform = new AffineTransform();
                // scale to viewbox
                transform.translate(xOffset, yOffset);
                gen.getCurrentState().concatMatrix(transform);
                //graphics.setPDFState(psInfo.pdfState);
                try {
                    root.paint(graphics);
                    //psInfo.currentStream.add(graphics.getString());
                } catch (Exception e) {
                    context.getUserAgent().getLogger().error("SVG graphic could not be rendered: "
                                           + e.getMessage(), e);
                }

                psInfo.psGenerator.restoreGraphicsState();
                //psInfo.pdfState.pop();
                gen.writeln("%SVG graphic end ---");
            } catch (IOException ioe) {
                context.getUserAgent().getLogger().error("SVG graphic could not be rendered: "
                                       + ioe.getMessage(), ioe);
            }
        }
    }
}

