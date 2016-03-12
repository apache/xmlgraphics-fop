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
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DefaultFontFamilyResolver;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.image.loader.batik.BatikUtil;
import org.apache.fop.image.loader.batik.Graphics2DImagePainterImpl;
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

    /** {@inheritDoc} */
    public void handleXML(RendererContext context,
                Document doc, String ns) throws Exception {

        if (SVGDOMImplementation.SVG_NAMESPACE_URI.equals(ns)) {
            renderSVGDocument(context, doc);
        }
    }

    /**
     * Creates a graphics 2D image painter implementation
     *
     * @param root the batik graphics node root
     * @param ctx the batik bridge context
     * @param imageSize the image size
     * @return a new graphics 2D image painter implementation
     */
    protected Graphics2DImagePainter createGraphics2DImagePainter(
            GraphicsNode root, BridgeContext ctx, Dimension imageSize) {
        return new Graphics2DImagePainterImpl(root, ctx, imageSize);
    }

    /**
     * Builds the GVT root.
     *
     * @param userAgent the user agent
     * @param ctx the batik bridge context
     * @param doc the document
     * @return a built GVT root tree
     */
    protected GraphicsNode buildGraphicsNode(
            FOUserAgent userAgent, BridgeContext ctx, Document doc) {
        GVTBuilder builder = new GVTBuilder();
        final GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
        } catch (Exception e) {
            EventBroadcaster eventBroadcaster
                = userAgent.getEventBroadcaster();
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(eventBroadcaster);
            final String uri = getDocumentURI(doc);
            eventProducer.svgNotBuilt(this, e, uri);
            return null;
        }
        return root;
    }

    /**
     * Returns the image size
     * @param wrappedContext renderer context wrapper
     *
     * @return the image size
     */
    protected Dimension getImageSize(RendererContextWrapper wrappedContext) {
        final int width = wrappedContext.getWidth();
        final int height = wrappedContext.getHeight();
        return new Dimension(width, height);
    }

    /**
     * Render the SVG document.
     *
     * @param rendererContext the renderer context
     * @param doc the SVG document
     * @throws IOException In case of an I/O error while painting the image
     */
    protected void renderSVGDocument(final RendererContext rendererContext,
            final Document doc) throws IOException {
        updateRendererContext(rendererContext);

        //Prepare
        FOUserAgent userAgent = rendererContext.getUserAgent();
        SVGUserAgent svgUserAgent = new SVGUserAgent(userAgent, DefaultFontFamilyResolver.SINGLETON,
                new AffineTransform());

        //Create Batik BridgeContext
        final BridgeContext bridgeContext = new BridgeContext(svgUserAgent);

        //Cloning SVG DOM as Batik attaches non-thread-safe facilities (like the CSS engine)
        //to it.
        Document clonedDoc = BatikUtil.cloneSVGDocument(doc);

        //Build the GVT tree
        final GraphicsNode root = buildGraphicsNode(userAgent, bridgeContext, clonedDoc);

        // Create Graphics2DImagePainter
        final RendererContextWrapper wrappedContext = RendererContext.wrapRendererContext(
                rendererContext);
        Dimension imageSize = getImageSize(wrappedContext);
        final Graphics2DImagePainter painter = createGraphics2DImagePainter(
                root, bridgeContext, imageSize);

        //Let the painter paint the SVG on the Graphics2D instance
        Graphics2DAdapter g2dAdapter = rendererContext.getRenderer().getGraphics2DAdapter();

        //Paint the image
        final int x = wrappedContext.getCurrentXPosition();
        final int y = wrappedContext.getCurrentYPosition();
        final int width = wrappedContext.getWidth();
        final int height = wrappedContext.getHeight();
        g2dAdapter.paintImage(painter, rendererContext, x, y, width, height);
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

