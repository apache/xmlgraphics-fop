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

package org.apache.fop.render;

// Java
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;

/**
 * Generic XML handler for SVG. Uses Apache Batik for SVG processing and simply paints to
 * a Graphics2DAdapter and thus ultimatively to Graphics2D interface that is presented.
 * <p>
 * To use this class, subclass it and implement the missing methods (supportsRenderer, for example).
 */
public abstract class AbstractGenericSVGHandler implements XMLHandler, RendererContextConstants {

    /** logging instance */
    protected static Log log = LogFactory.getLog(AbstractGenericSVGHandler.class);

    /** {@inheritDoc} */
    public void handleXML(RendererContext context, 
                Document doc, String ns) throws Exception {

        if (SVGDOMImplementation.SVG_NAMESPACE_URI.equals(ns)) {
            renderSVGDocument(context, doc);
        }
    }

    /**
     * Render the SVG document.
     * 
     * @param context the renderer context
     * @param doc the SVG document
     * @throws IOException In case of an I/O error while painting the image
     */
    protected void renderSVGDocument(final RendererContext context,
            final Document doc) throws IOException {
        updateRendererContext(context);
        final RendererContextWrapper wrappedContext = RendererContext.wrapRendererContext(context);
        int x = wrappedContext.getCurrentXPosition();
        int y = wrappedContext.getCurrentYPosition();

        //Prepare
        SVGUserAgent ua = new SVGUserAgent(
                context.getUserAgent(),
                new AffineTransform());
        GVTBuilder builder = new GVTBuilder();
        final BridgeContext ctx = new BridgeContext(ua);

        //Build the GVT tree
        final GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
        } catch (Exception e) {
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                    context.getUserAgent().getEventBroadcaster());
            final String uri = getDocumentURI(doc);
            eventProducer.svgNotBuilt(this, e, uri);
            return;
        }

        //Create the painter
        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                // If no viewbox is defined in the svg file, a viewbox of 100x100 is
                // assumed, as defined in SVGUserAgent.getViewportSize()
                float iw = (float) ctx.getDocumentSize().getWidth();
                float ih = (float) ctx.getDocumentSize().getHeight();
                float w = (float) area.getWidth();
                float h = (float) area.getHeight();
                g2d.scale(w / iw, h / ih);

                root.paint(g2d);
            }

            public Dimension getImageSize() {
                return new Dimension(wrappedContext.getWidth(), wrappedContext.getHeight());
            }
            
        };

        //Let the painter paint the SVG on the Graphics2D instance
        Graphics2DAdapter adapter = context.getRenderer().getGraphics2DAdapter();
        adapter.paintImage(painter, context, 
                x, y, wrappedContext.getWidth(), wrappedContext.getHeight()); 
    }

    /**
     * Gets the document URI from a Document instance if possible.
     * 
     * @param doc the Document
     * @return the URI or null
     */
    protected String getDocumentURI(Document doc) {
        String docURI = null;
        if (doc instanceof AbstractDocument) {
            AbstractDocument level3Doc = (AbstractDocument)doc;
            docURI = level3Doc.getDocumentURI();
        }
        return docURI;
    }
    
    /**
     * Override this method to update the renderer context if it needs special settings for
     * certain conditions.
     * 
     * @param context the renderer context
     */
    protected void updateRendererContext(RendererContext context) {
        //nop
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return SVGDOMImplementation.SVG_NAMESPACE_URI;
    }

}

