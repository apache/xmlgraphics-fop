/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.render.pcl;

// Java
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

// DOM
import org.w3c.dom.Document;

// Batik
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;

// FOP
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.Graphics2DImagePainter;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;
import org.apache.fop.svg.SVGUserAgent;

// Commons-Logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PCL XML handler for SVG. Uses Apache Batik for SVG processing.
 * This handler handles XML for foreign objects when rendering to HP GL/2.
 * It renders SVG to HP GL/2 using the PCLGraphics2D.
 *
 * @version $Id$
 */
public class PCLSVGHandler implements XMLHandler, RendererContextConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLSVGHandler.class);

    /**
     * Create a new XML handler for use by the PCL renderer.
     */
    public PCLSVGHandler() {
    }

    /** @see org.apache.fop.render.XMLHandler */
    public void handleXML(RendererContext context, 
                Document doc, String ns) throws Exception {
        PCLRendererContext pclContext = PCLRendererContext.wrapRendererContext(context);

        if (SVGDOMImplementation.SVG_NAMESPACE_URI.equals(ns)) {
            renderSVGDocument(context, doc, pclContext);
        }
    }

    /**
     * Render the SVG document.
     * @param context the renderer context
     * @param doc the SVG document
     * @param pclContext the information of the current context
     */
    protected void renderSVGDocument(final RendererContext context,
            final Document doc, final PCLRendererContext pclContext) {
        int x = pclContext.getCurrentXPosition();
        int y = pclContext.getCurrentYPosition();

        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                SVGUserAgent ua = new SVGUserAgent(
                        context.getUserAgent().getSourcePixelUnitToMillimeter(),
                        new AffineTransform());
                GVTBuilder builder = new GVTBuilder();
                BridgeContext ctx = new BridgeContext(ua);

                GraphicsNode root;
                try {
                    root = builder.build(ctx, doc);
                    
                    // If no viewbox is defined in the svg file, a viewbox of 100x100 is
                    // assumed, as defined in SVGUserAgent.getViewportSize()
                    float iw = (float) ctx.getDocumentSize().getWidth() * 1000f;
                    float ih = (float) ctx.getDocumentSize().getHeight() * 1000f;
                    float w = (float) area.getWidth();
                    float h = (float) area.getHeight();
                    g2d.scale(w / iw, h / ih);

                    root.paint(g2d);
                } catch (Exception e) {
                    log.error("SVG graphic could not be built: "
                                           + e.getMessage(), e);
                    return;
                }
            }

            public Dimension getImageSize() {
                return new Dimension(pclContext.getWidth(), pclContext.getHeight());
            }

        };

        try {
            Graphics2DAdapter adapter = context.getRenderer().getGraphics2DAdapter();
            adapter.paintImage(painter, context, 
                    x, y, pclContext.getWidth(), pclContext.getHeight()); 
        } catch (IOException ioe) {
            ((PCLRenderer)context.getRenderer()).handleIOTrouble(ioe);
        }
    }

    /** @see org.apache.fop.render.XMLHandler#supportsRenderer(org.apache.fop.render.Renderer) */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof PCLRenderer);
    }
    
    /** @see org.apache.fop.render.XMLHandler#getNamespace() */
    public String getNamespace() {
        return SVGDOMImplementation.SVG_NAMESPACE_URI;
    }

}

