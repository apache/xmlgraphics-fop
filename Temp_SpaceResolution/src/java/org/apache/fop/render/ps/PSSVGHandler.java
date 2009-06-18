/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;

// FOP
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;
import org.apache.fop.svg.SVGUserAgent;

// Commons-Logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PostScript XML handler for SVG. Uses Apache Batik for SVG processing.
 * This handler handles XML for foreign objects when rendering to PostScript.
 * It renders SVG to the PostScript document using the PSGraphics2D.
 * The properties from the PostScript renderer are subject to change.
 *
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @version $Id$
 */
public class PSSVGHandler implements XMLHandler {

    /**
     * logging instance
     */
    private Log log = LogFactory.getLog(PSSVGHandler.class);

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
    public PSSVGHandler() {
    }

    /** @see org.apache.fop.render.XMLHandler */
    public void handleXML(RendererContext context, 
                org.w3c.dom.Document doc, String ns) throws Exception {
        PSInfo psi = getPSInfo(context);

        if (SVGDOMImplementation.SVG_NAMESPACE_URI.equals(ns)) {
            SVGHandler svghandler = new SVGHandler();
            svghandler.renderSVGDocument(context, doc, psi);
        } else {
            //nop
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
        psi.fontInfo = (org.apache.fop.fonts.FontInfo) context.getProperty(PS_FONT_INFO);
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
        private org.apache.fop.fonts.FontInfo fontInfo;
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
        public FontInfo getFontInfo() {
            return fontInfo;
        }

        /**
         * Sets the fontInfo.
         * @param fontInfo The fontInfo to set
         */
        public void setFontInfo(FontInfo fontInfo) {
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
                 = new SVGUserAgent(
                    context.getUserAgent().getPixelUnitToMillimeter(),
                    new AffineTransform());

            GVTBuilder builder = new GVTBuilder();
            BridgeContext ctx = new BridgeContext(ua);
            PSTextPainter textPainter = new PSTextPainter(psInfo.getFontInfo());
            ctx.setTextPainter(textPainter);            
            PSTextElementBridge tBridge = new PSTextElementBridge(textPainter);
            ctx.putBridge(tBridge);

            //PSAElementBridge aBridge = new PSAElementBridge();
            // to get the correct transform we need to use the PDFState
            AffineTransform transform = gen.getCurrentState().getTransform();
            transform.translate(xOffset / 1000f, yOffset / 1000f);
            //aBridge.setCurrentTransform(transform);
            //ctx.putBridge(aBridge);

            GraphicsNode root;
            try {
                root = builder.build(ctx, doc);
            } catch (Exception e) {
                log.error("SVG graphic could not be built: "
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
                gen.commentln("%FOPBeginSVG");
                gen.saveGraphicsState();
                /*
                 * Clip to the svg area.
                 * Note: To have the svg overlay (under) a text area then use
                 * an fo:block-container
                 */
                gen.writeln("newpath");
                gen.defineRect(xOffset / 1000f, yOffset / 1000f, 
                        psInfo.getWidth() / 1000f, psInfo.getWidth() / 1000f);
                gen.writeln("clip");
                
                // transform so that the coordinates (0,0) is from the top left
                // and positive is down and to the right. (0,0) is where the
                // viewBox puts it.
                gen.concatMatrix(sx, 0, 0, sy, xOffset / 1000f, yOffset / 1000f);

                SVGSVGElement svg = ((SVGDocument)doc).getRootElement();
                AffineTransform at = ViewBox.getPreserveAspectRatioTransform(svg,
                        psInfo.getWidth() / 1000f, psInfo.getHeight() / 1000f);
                if (false && !at.isIdentity()) {
                    double[] vals = new double[6];
                    at.getMatrix(vals);
                    gen.concatMatrix(vals);
                }

                final boolean textAsShapes = false;
                PSGraphics2D graphics = new PSGraphics2D(textAsShapes, gen);
                graphics.setGraphicContext(new org.apache.batik.ext.awt.g2d.GraphicContext());
                transform = new AffineTransform();
                // scale to viewbox
                transform.translate(xOffset, yOffset);
                gen.getCurrentState().concatMatrix(transform);
                try {
                    root.paint(graphics);
                } catch (Exception e) {
                    log.error("SVG graphic could not be rendered: "
                                           + e.getMessage(), e);
                }

                psInfo.psGenerator.restoreGraphicsState();
                gen.commentln("%FOPEndSVG");
            } catch (IOException ioe) {
                log.error("SVG graphic could not be rendered: "
                                       + ioe.getMessage(), ioe);
            }
        }
    }

    /** @see org.apache.fop.render.XMLHandler#getMimeType() */
    public String getMimeType() {
        return PSRenderer.MIME_TYPE;
    }

    /** @see org.apache.fop.render.XMLHandler#getNamespace() */
    public String getNamespace() {
        return SVGDOMImplementation.SVG_NAMESPACE_URI;
    }
    
}

