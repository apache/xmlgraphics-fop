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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.svg.PDFAElementBridge;
import org.apache.fop.svg.PDFBridgeContext;
import org.apache.fop.svg.PDFGraphics2D;
import org.apache.fop.svg.SVGUserAgent;

/**
 * PDF XML handler for SVG (uses Apache Batik).
 * This handler handles XML for foreign objects when rendering to PDF.
 * It renders SVG to the PDF document using the PDFGraphics2D.
 * The properties from the PDF renderer are subject to change.
 */
public class PDFSVGHandler extends AbstractGenericSVGHandler 
            implements PDFRendererContextConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFSVGHandler.class);

    /**
     * Get the pdf information from the render context.
     *
     * @param context the renderer context
     * @return the pdf information retrieved from the context
     */
    public static PDFInfo getPDFInfo(RendererContext context) {
        PDFInfo pdfi = new PDFInfo();
        pdfi.pdfDoc = (PDFDocument)context.getProperty(PDF_DOCUMENT);
        pdfi.outputStream = (OutputStream)context.getProperty(OUTPUT_STREAM);
        pdfi.pdfState = (PDFState)context.getProperty(PDF_STATE);
        pdfi.pdfPage = (PDFPage)context.getProperty(PDF_PAGE);
        pdfi.pdfContext = (PDFResourceContext)context.getProperty(PDF_CONTEXT);
        pdfi.currentStream = (PDFStream)context.getProperty(PDF_STREAM);
        pdfi.width = ((Integer)context.getProperty(WIDTH)).intValue();
        pdfi.height = ((Integer)context.getProperty(HEIGHT)).intValue();
        pdfi.fi = (FontInfo)context.getProperty(PDF_FONT_INFO);
        pdfi.currentFontName = (String)context.getProperty(PDF_FONT_NAME);
        pdfi.currentFontSize = ((Integer)context.getProperty(PDF_FONT_SIZE)).intValue();
        pdfi.currentXPosition = ((Integer)context.getProperty(XPOS)).intValue();
        pdfi.currentYPosition = ((Integer)context.getProperty(YPOS)).intValue();
        pdfi.cfg = (Configuration)context.getProperty(HANDLER_CONFIGURATION);
        Map foreign = (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
        QName qName = new QName(ExtensionElementMapping.URI, null, "conversion-mode");
        if (foreign != null 
                && "bitmap".equalsIgnoreCase((String)foreign.get(qName))) {
            pdfi.paintAsBitmap = true;
        }
        return pdfi;
    }

    /**
     * PDF information structure for drawing the XML document.
     */
    public static class PDFInfo {
        /** see PDF_DOCUMENT */
        public PDFDocument pdfDoc;
        /** see OUTPUT_STREAM */
        public OutputStream outputStream;
        /** see PDF_STATE */
        public PDFState pdfState;
        /** see PDF_PAGE */
        public PDFPage pdfPage;
        /** see PDF_CONTEXT */
        public PDFResourceContext pdfContext;
        /** see PDF_STREAM */
        public PDFStream currentStream;
        /** see PDF_WIDTH */
        public int width;
        /** see PDF_HEIGHT */
        public int height;
        /** see PDF_FONT_INFO */
        public FontInfo fi;
        /** see PDF_FONT_NAME */
        public String currentFontName;
        /** see PDF_FONT_SIZE */
        public int currentFontSize;
        /** see PDF_XPOS */
        public int currentXPosition;
        /** see PDF_YPOS */
        public int currentYPosition;
        /** see PDF_HANDLER_CONFIGURATION */
        public Configuration cfg;
        /** true if SVG should be rendered as a bitmap instead of natively */
        public boolean paintAsBitmap;
    }

    /**
     * {@inheritDoc}
     */
    protected void renderSVGDocument(RendererContext context,
            Document doc) {
        PDFRenderer renderer = (PDFRenderer)context.getRenderer();
        PDFInfo pdfInfo = getPDFInfo(context);
        if (pdfInfo.paintAsBitmap) {
            try {
                super.renderSVGDocument(context, doc);
            } catch (IOException ioe) {
                log.error("I/O error while rendering SVG graphic: "
                                       + ioe.getMessage(), ioe);
            }
            return;
        }
        int xOffset = pdfInfo.currentXPosition;
        int yOffset = pdfInfo.currentYPosition;

        FOUserAgent userAgent = context.getUserAgent(); 
        log.debug("Generating SVG at " 
                + userAgent.getTargetResolution()
                + "dpi.");
        final float deviceResolution = userAgent.getTargetResolution();
        log.debug("Generating SVG at " + deviceResolution + "dpi.");
        log.debug("Generating SVG at " + deviceResolution + "dpi.");
        
        final float uaResolution = userAgent.getSourceResolution();
        SVGUserAgent ua = new SVGUserAgent(25.4f / uaResolution, new AffineTransform());

        //Scale for higher resolution on-the-fly images from Batik
        double s = uaResolution / deviceResolution;
        AffineTransform resolutionScaling = new AffineTransform();
        resolutionScaling.scale(s, s);
        
        GVTBuilder builder = new GVTBuilder();
        
        //Controls whether text painted by Batik is generated using text or path operations
        boolean strokeText = false;
        Configuration cfg = pdfInfo.cfg;
        if (cfg != null) {
            strokeText = cfg.getChild("stroke-text", true).getValueAsBoolean(strokeText);
        }
        
        BridgeContext ctx = new PDFBridgeContext(ua, 
                (strokeText ? null : pdfInfo.fi),
                userAgent.getFactory().getImageManager(),
                userAgent.getImageSessionContext(),
                new AffineTransform());
        
        GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
            builder = null;
        } catch (Exception e) {
            log.error("svg graphic could not be built: "
                                   + e.getMessage(), e);
            return;
        }
        // get the 'width' and 'height' attributes of the SVG document
        float w = (float)ctx.getDocumentSize().getWidth() * 1000f;
        float h = (float)ctx.getDocumentSize().getHeight() * 1000f;

        float sx = pdfInfo.width / (float)w;
        float sy = pdfInfo.height / (float)h;

        //Scaling and translation for the bounding box of the image
        AffineTransform scaling = new AffineTransform(
                sx, 0, 0, sy, xOffset / 1000f, yOffset / 1000f);

        //Transformation matrix that establishes the local coordinate system for the SVG graphic
        //in relation to the current coordinate system
        AffineTransform imageTransform = new AffineTransform();
        imageTransform.concatenate(scaling);
        imageTransform.concatenate(resolutionScaling);
        
        /*
         * Clip to the svg area.
         * Note: To have the svg overlay (under) a text area then use
         * an fo:block-container
         */
        pdfInfo.currentStream.add("%SVG setup\n");
        renderer.saveGraphicsState();
        renderer.setColor(Color.black, false, null);
        renderer.setColor(Color.black, true, null);

        if (!scaling.isIdentity()) {
            pdfInfo.currentStream.add("%viewbox\n");
            pdfInfo.currentStream.add(CTMHelper.toPDFString(scaling, false) + " cm\n");
        }

        //SVGSVGElement svg = ((SVGDocument)doc).getRootElement();

        if (pdfInfo.pdfContext == null) {
            pdfInfo.pdfContext = pdfInfo.pdfPage;
        }
        PDFGraphics2D graphics = new PDFGraphics2D(true, pdfInfo.fi, 
                pdfInfo.pdfDoc,
                pdfInfo.pdfContext, pdfInfo.pdfPage.referencePDF(),
                pdfInfo.currentFontName, pdfInfo.currentFontSize);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        if (!resolutionScaling.isIdentity()) {
            pdfInfo.currentStream.add("%resolution scaling for " + uaResolution 
                        + " -> " + deviceResolution + "\n");
            pdfInfo.currentStream.add(
                    CTMHelper.toPDFString(resolutionScaling, false) + " cm\n");
            graphics.scale(1 / s, 1 / s);
        }
        
        pdfInfo.currentStream.add("%SVG start\n");

        //Save state and update coordinate system for the SVG image
        pdfInfo.pdfState.push();
        pdfInfo.pdfState.concatenate(imageTransform);

        //Now that we have the complete transformation matrix for the image, we can update the 
        //transformation matrix for the AElementBridge.
        PDFAElementBridge aBridge = (PDFAElementBridge)ctx.getBridge(
                SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_A_TAG);
        aBridge.getCurrentTransform().setTransform(pdfInfo.pdfState.getTransform());

        graphics.setPDFState(pdfInfo.pdfState);
        graphics.setOutputStream(pdfInfo.outputStream);
        try {
            root.paint(graphics);
            pdfInfo.currentStream.add(graphics.getString());
        } catch (Exception e) {
            log.error("svg graphic could not be rendered: "
                                   + e.getMessage(), e);
        }
        pdfInfo.pdfState.pop();
        renderer.restoreGraphicsState();
        pdfInfo.currentStream.add("%SVG end\n");
    }
    
    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof PDFRenderer);
    }
}
