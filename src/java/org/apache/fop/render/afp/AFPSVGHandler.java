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

package org.apache.fop.render.afp;

// FOP
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.svg.SVGUserAgent;
import org.w3c.dom.Document;

/**
 * AFP XML handler for SVG. Uses Apache Batik for SVG processing.
 * This handler handles XML for foreign objects and delegates to AFPGraphics2DAdapter.
 * @see AFPGraphics2DAdapter
 */
public class AFPSVGHandler extends AbstractGenericSVGHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPSVGHandler.class);

    /** {@inheritDoc} */
    public void handleXML(RendererContext context, 
                Document doc, String ns) throws Exception {
        AFPInfo afpi = getAFPInfo(context);

        if (SVGDOMImplementation.SVG_NAMESPACE_URI.equals(ns)) {
            renderSVGDocument(context, doc, afpi);
        }
    }

    /**
     * Get the afp information from the render context.
     *
     * @param context the renderer context
     * @return the afp information retrieved from the context
     */
    public static AFPInfo getAFPInfo(RendererContext context) {
        AFPInfo afpi = new AFPInfo();
        afpi.width = ((Integer)context.getProperty(WIDTH)).intValue();
        afpi.height = ((Integer)context.getProperty(HEIGHT)).intValue();
        afpi.currentXPosition = ((Integer)context.getProperty(XPOS)).intValue();
        afpi.currentYPosition = ((Integer)context.getProperty(YPOS)).intValue();
        afpi.cfg = (Configuration)context.getProperty(HANDLER_CONFIGURATION);
        afpi.fontInfo = (org.apache.fop.fonts.FontInfo)context.getProperty(
                AFPRendererContextConstants.AFP_FONT_INFO);
        afpi.resolution = ((Integer)context.getProperty(
                AFPRendererContextConstants.AFP_RESOLUTION)).intValue();
        afpi.afpState = (AFPState)context.getProperty(
                AFPRendererContextConstants.AFP_STATE);
        afpi.afpDataStream = (AFPDataStream)context.getProperty(
                AFPRendererContextConstants.AFP_DATASTREAM);
        afpi.grayscale = ((Boolean)context.getProperty(
                AFPRendererContextConstants.AFP_GRAYSCALE)).booleanValue();
        afpi.bitsPerPixel = ((Integer)context.getProperty(
                AFPRendererContextConstants.AFP_BITS_PER_PIXEL)).intValue();
        return afpi;
    }

    /**
     * Render the SVG document.
     * @param context the renderer context
     * @param doc the SVG document
     * @param afpInfo the AFPInfo renderer parameters
     * @throws IOException In case of an I/O error while painting the image
     */
    protected void renderSVGDocument(final RendererContext context,
            final Document doc, AFPInfo afpInfo) throws IOException {
        
        final boolean textAsShapes = false;
        AFPGraphics2D graphics = new AFPGraphics2D(textAsShapes);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
        graphics.setAFPInfo(afpInfo);
        
        GVTBuilder builder = new GVTBuilder();

        boolean strokeText = false;
        Configuration cfg = afpInfo.cfg;
        if (cfg != null) {
            strokeText = cfg.getChild("stroke-text", true).getValueAsBoolean(strokeText);
        }
        
        final float uaResolution = context.getUserAgent().getSourceResolution();
        SVGUserAgent svgUserAgent = new SVGUserAgent(25.4f / uaResolution, new AffineTransform());

        BridgeContext ctx = new BridgeContext(svgUserAgent);
        AFPTextHandler afpTextHandler = null;
        //Controls whether text painted by Batik is generated using text or path operations
        if (!strokeText) {
            afpTextHandler = new AFPTextHandler(graphics);
            graphics.setCustomTextHandler(afpTextHandler);
            AFPTextPainter textPainter = new AFPTextPainter(afpTextHandler);
            ctx.setTextPainter(textPainter);            
            AFPTextElementBridge tBridge = new AFPTextElementBridge(textPainter);
            ctx.putBridge(tBridge);
        }

        GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
        } catch (Exception e) {
            log.error("SVG graphic could not be built: "
                                   + e.getMessage(), e);
            return;
        }
        log.debug("Generating SVG at " 
                + afpInfo.resolution + "dpi.");

        int res = afpInfo.resolution;
        
        double w = ctx.getDocumentSize().getWidth() * 1000f;
        double h = ctx.getDocumentSize().getHeight() * 1000f;
        
        // convert to afp inches
        double sx = ((afpInfo.width / w) * res) / 72f;
        double sy = ((afpInfo.height / h) * res) / 72f;
        double xOffset = (afpInfo.currentXPosition * res) / 72000f;
        double yOffset = ((afpInfo.height - afpInfo.currentYPosition) * res) / 72000f;

        // Transformation matrix that establishes the local coordinate system for the SVG graphic
        // in relation to the current coordinate system (note: y axis is inverted)
        AffineTransform trans = new AffineTransform(sx, 0, 0, -sy, xOffset, yOffset);
        graphics.setTransform(trans);
        try {
            root.paint(graphics);
        } catch (Exception e) {
            log.error("SVG graphic could not be rendered: " + e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        //return (renderer instanceof AFPRenderer);
        if (renderer instanceof AFPRenderer) {
            AFPRenderer afpRenderer = (AFPRenderer)renderer;
            return afpRenderer.isGOCAEnabled();
        }
        return false;
    }
    
    /** {@inheritDoc} */
    public String getNamespace() {
        return SVGDOMImplementation.SVG_NAMESPACE_URI;
    }
}

