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
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;

import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.svg.AFPBridgeContext;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.image.loader.batik.BatikUtil;
import org.apache.fop.image.loader.batik.Graphics2DImagePainterImpl;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;

/**
 * AFP XML handler for SVG. Uses Apache Batik for SVG processing.
 * This handler handles XML for foreign objects and delegates to AFPGraphics2DAdapter.
 * @see AFPGraphics2DAdapter
 */
public class AFPSVGHandler extends AbstractGenericSVGHandler {

    private boolean paintAsBitmap = false;

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
     * @param rendererContext the renderer context
     * @param doc the SVG document
     * @throws IOException In case of an I/O error while painting the image
     */
    protected void renderSVGDocument(final RendererContext rendererContext,
            final Document doc) throws IOException {

        AFPRendererContext afpRendererContext = (AFPRendererContext)rendererContext;
        AFPInfo afpInfo = afpRendererContext.getInfo();

        this.paintAsBitmap = afpInfo.paintAsBitmap();

        FOUserAgent userAgent = rendererContext.getUserAgent();

        // fallback paint as bitmap
        String uri = getDocumentURI(doc);
        if (paintAsBitmap) {
            try {
                super.renderSVGDocument(rendererContext, doc);
            } catch (IOException ioe) {
                SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                        userAgent.getEventBroadcaster());
                eventProducer.svgRenderingError(this, ioe, uri);
            }
            return;
        }

        // Create a new AFPGraphics2D
        final boolean textAsShapes = afpInfo.strokeText();
        AFPGraphics2D g2d = afpInfo.createGraphics2D(textAsShapes);

        AFPPaintingState paintingState = g2d.getPaintingState();
        paintingState.setImageUri(uri);

        // Create an AFPBridgeContext
        BridgeContext bridgeContext = createBridgeContext(userAgent, g2d);

        //Cloning SVG DOM as Batik attaches non-thread-safe facilities (like the CSS engine)
        //to it.
        Document clonedDoc = BatikUtil.cloneSVGDocument(doc);

        // Build the SVG DOM and provide the painter with it
        GraphicsNode root = buildGraphicsNode(userAgent, bridgeContext, clonedDoc);

        // Create Graphics2DImagePainter
        final RendererContextWrapper wrappedContext
            = RendererContext.wrapRendererContext(rendererContext);
        Dimension imageSize = getImageSize(wrappedContext);
        Graphics2DImagePainter painter
            = createGraphics2DImagePainter(bridgeContext, root, imageSize);

        // Create AFPObjectAreaInfo
        RendererContextWrapper rctx = RendererContext.wrapRendererContext(rendererContext);
        int x = rctx.getCurrentXPosition();
        int y = rctx.getCurrentYPosition();
        int width = afpInfo.getWidth();
        int height = afpInfo.getHeight();
        int resolution = afpInfo.getResolution();

        paintingState.save(); // save

        AFPObjectAreaInfo objectAreaInfo
            = createObjectAreaInfo(paintingState, x, y, width, height, resolution);

        // Create AFPGraphicsObjectInfo
        AFPResourceInfo resourceInfo = afpInfo.getResourceInfo();
        AFPGraphicsObjectInfo graphicsObjectInfo = createGraphicsObjectInfo(
                paintingState, painter, userAgent, resourceInfo, g2d);
        graphicsObjectInfo.setObjectAreaInfo(objectAreaInfo);

        // Create the GOCA GraphicsObject in the DataStream
        AFPResourceManager resourceManager = afpInfo.getResourceManager();
        resourceManager.createObject(graphicsObjectInfo);

        paintingState.restore(); // resume
    }

    private AFPObjectAreaInfo createObjectAreaInfo(AFPPaintingState paintingState,
            int x, int y, int width, int height, int resolution) {
        // set the data object parameters

        AffineTransform at = paintingState.getData().getTransform();
        at.translate(x, y);
        AFPUnitConverter unitConv = paintingState.getUnitConverter();

        int rotation = paintingState.getRotation();
        int objX = (int) Math.round(at.getTranslateX());
        int objY = (int) Math.round(at.getTranslateY());
        int objWidth = Math.round(unitConv.mpt2units(width));
        int objHeight = Math.round(unitConv.mpt2units(height));
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo(objX, objY, objWidth, objHeight,
                resolution, rotation);
        return objectAreaInfo;
    }

    private AFPGraphicsObjectInfo createGraphicsObjectInfo
        (AFPPaintingState paintingState, Graphics2DImagePainter painter,
            FOUserAgent userAgent, AFPResourceInfo resourceInfo, AFPGraphics2D g2d) {
        AFPGraphicsObjectInfo graphicsObjectInfo = new AFPGraphicsObjectInfo();

        String uri = paintingState.getImageUri();
        graphicsObjectInfo.setUri(uri);

        graphicsObjectInfo.setMimeType(MimeConstants.MIME_AFP_GOCA);

        graphicsObjectInfo.setResourceInfo(resourceInfo);

        graphicsObjectInfo.setPainter(painter);

        // Set the afp graphics 2d implementation
        graphicsObjectInfo.setGraphics2D(g2d);

        return graphicsObjectInfo;
    }

    /**
     * @param userAgent a user agent instance
     * @param g2d a graphics context
     * @return a bridge context
     */
    public static BridgeContext createBridgeContext(FOUserAgent userAgent, AFPGraphics2D g2d) {
        ImageManager imageManager = userAgent.getImageManager();

        SVGUserAgent svgUserAgent
            = new SVGUserAgent(userAgent, new AffineTransform());

        ImageSessionContext imageSessionContext = userAgent.getImageSessionContext();

        FontInfo fontInfo = g2d.getFontInfo();
        return new AFPBridgeContext(svgUserAgent, fontInfo, imageManager, imageSessionContext,
                new AffineTransform(), g2d);
    }

    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return false;
    }

    /** {@inheritDoc} */
    protected void updateRendererContext(RendererContext context) {
        //Work around a problem in Batik: Gradients cannot be done in ColorSpace.CS_GRAY
        context.setProperty(AFPRendererContextConstants.AFP_GRAYSCALE, Boolean.FALSE);
    }

    private Graphics2DImagePainter createGraphics2DImagePainter(BridgeContext ctx,
            GraphicsNode root, Dimension imageSize) {
        Graphics2DImagePainter painter = null;
        if (paintAsBitmap()) {
            // paint as IOCA Image
            painter = super.createGraphics2DImagePainter(root, ctx, imageSize);
        } else {
            // paint as GOCA Graphics
            painter = new Graphics2DImagePainterImpl(root, ctx, imageSize);
        }
        return painter;
    }

    /**
     * Returns true if the SVG is to be painted as a bitmap
     *
     * @return true if the SVG is to be painted as a bitmap
     */
    private boolean paintAsBitmap() {
        return paintAsBitmap;
    }

}
