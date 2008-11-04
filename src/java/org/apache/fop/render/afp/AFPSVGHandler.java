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
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.fop.afp.AFPForeignAttributeReader;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPTextElementBridge;
import org.apache.fop.afp.AFPTextHandler;
import org.apache.fop.afp.AFPTextPainter;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.Graphics2DImagePainterGOCA;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.w3c.dom.Document;

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
     * Get the AFP information from the render context.
     *
     * @param context the renderer context
     * @return the AFP information retrieved from the context
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
        afpi.setPaintingState((AFPPaintingState)context.getProperty(
                AFPRendererContextConstants.AFP_PAINTING_STATE));
        afpi.setResourceManager(((AFPResourceManager)context.getProperty(
                AFPRendererContextConstants.AFP_RESOURCE_MANAGER)));

        Map foreignAttributes = (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
        if (foreignAttributes != null) {
            String conversionMode = (String)foreignAttributes.get(CONVERSION_MODE);
            boolean paintAsBitmap = BITMAP.equalsIgnoreCase(conversionMode);
            afpi.setPaintAsBitmap(paintAsBitmap);

            AFPForeignAttributeReader foreignAttributeReader = new AFPForeignAttributeReader();
            AFPResourceInfo resourceInfo = foreignAttributeReader.getResourceInfo(foreignAttributes);
            // default to inline level if painted as GOCA
            if (!resourceInfo.levelChanged() && !paintAsBitmap) {
                resourceInfo.setLevel(new AFPResourceLevel(AFPResourceLevel.INLINE));
            }
            afpi.setResourceInfo(resourceInfo);
        }
        return afpi;
    }

    private static final int X = 0;
    private static final int Y = 1;

    /**
     * Render the SVG document.
     *
     * @param context the renderer context
     * @param doc the SVG document
     * @throws IOException In case of an I/O error while painting the image
     */
    protected void renderSVGDocument(final RendererContext context,
            final Document doc) throws IOException {

        AFPInfo afpInfo = getAFPInfo(context);

        this.paintAsBitmap = afpInfo.paintAsBitmap();

        // fallback paint as bitmap
        if (paintAsBitmap) {
            try {
                super.renderSVGDocument(context, doc);
            } catch (IOException ioe) {
                SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                        context.getUserAgent().getEventBroadcaster());
                eventProducer.svgRenderingError(this, ioe, getDocumentURI(doc));
            }
            return;
        }

        String uri = ((AbstractDocument)doc).getDocumentURI();
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        paintingState.setImageUri(uri);

        // set the data object parameters
        AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo();

        RendererContextWrapper rctx = RendererContext.wrapRendererContext(context);
        int currx = rctx.getCurrentXPosition();
        int curry = rctx.getCurrentYPosition();
        float[] srcPts = {currx, curry};

        AFPUnitConverter unitConv = paintingState.getUnitConverter();
        int[] coords = unitConv.mpts2units(srcPts);
        objectAreaInfo.setX(coords[X]);
        objectAreaInfo.setY(coords[Y]);

        int resolution = afpInfo.getResolution();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        int width = Math.round(unitConv.mpt2units(afpInfo.getWidth()));
        objectAreaInfo.setWidth(width);

        int height = Math.round(unitConv.mpt2units(afpInfo.getHeight()));
        objectAreaInfo.setHeight(height);

        int rotation = paintingState.getRotation();
        objectAreaInfo.setRotation(rotation);

        AFPGraphicsObjectInfo graphicsObjectInfo = new AFPGraphicsObjectInfo();
        graphicsObjectInfo.setUri(uri);

        // Configure Graphics2D implementation
        final boolean textAsShapes = false;
        AFPGraphics2D g2d = new AFPGraphics2D(textAsShapes);

        g2d.setPaintingState(paintingState);

        AFPResourceManager resourceManager = afpInfo.getResourceManager();
        g2d.setResourceManager(resourceManager);

        AFPResourceInfo resourceInfo = afpInfo.getResourceInfo();
        g2d.setResourceInfo(resourceInfo);
        graphicsObjectInfo.setResourceInfo(resourceInfo);

        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        FontInfo fontInfo = afpInfo.getFontInfo();
        g2d.setFontInfo(fontInfo);

        // Configure GraphicsObjectPainter with the Graphics2D implementation
        GraphicsObjectPainterAFP painter = new GraphicsObjectPainterAFP(g2d);
        (graphicsObjectInfo).setPainter(painter);

        // Controls whether text painted by Batik is generated using text or path operations
        SVGUserAgent svgUserAgent
            = new SVGUserAgent(context.getUserAgent(), new AffineTransform());
        BridgeContext ctx = new BridgeContext(svgUserAgent);
        if (!afpInfo.strokeText()) {
            AFPTextHandler textHandler = new AFPTextHandler(g2d);
            g2d.setCustomTextHandler(textHandler);
            AFPTextPainter textPainter = new AFPTextPainter(textHandler);
            ctx.setTextPainter(textPainter);
            AFPTextElementBridge tBridge = new AFPTextElementBridge(textPainter);
            ctx.putBridge(tBridge);
        }

        // Build the SVG DOM and provide the painter with it
        GraphicsNode root;
        GVTBuilder builder = new GVTBuilder();
        try {
            root = builder.build(ctx, doc);
            painter.setGraphicsNode(root);
        } catch (BridgeException e) {
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                    context.getUserAgent().getEventBroadcaster());
            eventProducer.svgNotBuilt(this, e, uri);
            return;
        }

        // convert to afp inches
        Dimension2D dim = ctx.getDocumentSize();
        double w = dim.getWidth() * 1000f;
        double h = dim.getHeight() * 1000f;
        double wx = (afpInfo.getWidth() / w);
        double hx = (afpInfo.getHeight() / h);
        double scaleX = unitConv.pt2units((float)wx);
        double scaleY = unitConv.pt2units((float)hx);
        double yOffset = unitConv.mpt2units(afpInfo.getHeight());

        // Transformation matrix that establishes the local coordinate system
        // for the SVG graphic in relation to the current coordinate system
        // (note: y axis is inverted)
        AffineTransform trans = new AffineTransform(scaleX, 0, 0, -scaleY, 0, yOffset);
        g2d.setTransform(trans);

        // Set the afp graphics 2d implementation
        graphicsObjectInfo.setGraphics2D(g2d);

        // Set the object area info
        graphicsObjectInfo.setObjectAreaInfo(objectAreaInfo);

        // Create the graphics object
        resourceManager.createObject(graphicsObjectInfo);
    }

    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof AFPRenderer);
    }

    /** {@inheritDoc} */
    protected void updateRendererContext(RendererContext context) {
        //Work around a problem in Batik: Gradients cannot be done in ColorSpace.CS_GRAY
        context.setProperty(AFPRendererContextConstants.AFP_GRAYSCALE, Boolean.FALSE);
    }

    /** {@inheritDoc} */
    protected Graphics2DImagePainter createPainter(BridgeContext ctx, GraphicsNode root, Dimension imageSize) {
        Graphics2DImagePainter painter = null;
        if (paintAsBitmap()) {
            painter = super.createPainter(root, ctx, imageSize);
        } else {
            painter = new Graphics2DImagePainterGOCA(root, ctx, imageSize);
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
