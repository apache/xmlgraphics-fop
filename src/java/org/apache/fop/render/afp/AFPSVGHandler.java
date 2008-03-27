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
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.fop.render.afp.modca.IncludeObject;
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
        afpi.setWidth(((Integer)context.getProperty(WIDTH)).intValue());
        afpi.setHeight(((Integer)context.getProperty(HEIGHT)).intValue());
        afpi.setX(((Integer)context.getProperty(XPOS)).intValue());
        afpi.setY(((Integer)context.getProperty(YPOS)).intValue());
        afpi.setHandlerConfiguration((Configuration)context.getProperty(HANDLER_CONFIGURATION));
        afpi.setFontInfo((org.apache.fop.fonts.FontInfo)context.getProperty(
                AFPRendererContextConstants.AFP_FONT_INFO));
        afpi.setResolution(((Integer)context.getProperty(
                AFPRendererContextConstants.AFP_RESOLUTION)).intValue());
        afpi.setState((AFPState)context.getProperty(
                AFPRendererContextConstants.AFP_STATE));
        afpi.setAFPDataStream((AFPDataStream)context.getProperty(
                AFPRendererContextConstants.AFP_DATASTREAM));
        afpi.setColor(!((Boolean)context.getProperty(
                AFPRendererContextConstants.AFP_GRAYSCALE)).booleanValue());
        afpi.setBitsPerPixel(((Integer)context.getProperty(
                AFPRendererContextConstants.AFP_BITS_PER_PIXEL)).intValue());
        return afpi;
    }

    /**
     * Render the SVG document.
     * 
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
        
        String uri = ((AbstractDocument)doc).getDocumentURI();
        graphics.setDocumentURI(uri);
        
        GVTBuilder builder = new GVTBuilder();

        boolean strokeText = false;
        Configuration cfg = afpInfo.getHandlerConfiguration();
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
                + afpInfo.getResolution() + "dpi.");

        int res = afpInfo.getResolution();
        double w = ctx.getDocumentSize().getWidth() * 1000f;
        double h = ctx.getDocumentSize().getHeight() * 1000f;
        
        // convert to afp inches
        double sx = ((afpInfo.getWidth() / w) * res) / 72f;
        double sy = ((afpInfo.getHeight() / h) * res) / 72f;
        double xOffset = (afpInfo.getX() * res) / 72000f;
        double yOffset = ((afpInfo.getHeight() - afpInfo.getY()) * res) / 72000f;

        // Transformation matrix that establishes the local coordinate system for the SVG graphic
        // in relation to the current coordinate system (note: y axis is inverted)
        AffineTransform trans = new AffineTransform(sx, 0, 0, -sy, xOffset, yOffset);
        graphics.setTransform(trans);
        
        int x = (int)Math.round((afpInfo.getX() * 25.4f) / 1000);
        int y = (int)Math.round((afpInfo.getY() * 25.4f) / 1000);
        int width = (int)Math.round((afpInfo.getWidth() * res) / 72000f);
        int height = (int)Math.round((afpInfo.getHeight() * res) / 72000f);
        
        DataObjectParameters params = new DataObjectParameters(
                uri, x, y, width, height, res, res);
        
        Map/*<QName, String>*/ foreignAttributes
            = (Map/*<QName, String>*/)context.getProperty(
                RendererContextConstants.FOREIGN_ATTRIBUTES);

        if (foreignAttributes != null) {
            params.setResourceLevelFromForeignAttributes(foreignAttributes);
        }

        IncludeObject includeObj = afpInfo.getAFPDataStream().createGraphicsObject(params);
        GraphicsObject graphicsObj = (GraphicsObject)includeObj.getReferencedObject();
        graphics.setGraphicsObject(graphicsObj);
        
        try {
            root.paint(graphics);
        } catch (Exception e) {
            log.error("SVG graphic could not be rendered: " + e.getMessage(), e);
        }
        
        graphics.dispose();
    }
    
    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
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
