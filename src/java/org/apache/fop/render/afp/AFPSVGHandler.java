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
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.render.AbstractGenericSVGHandler;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.xmlgraphics.util.QName;
import org.w3c.dom.Document;

/**
 * AFP XML handler for SVG. Uses Apache Batik for SVG processing.
 * This handler handles XML for foreign objects and delegates to AFPGraphics2DAdapter.
 * @see AFPGraphics2DAdapter
 */
public class AFPSVGHandler extends AbstractGenericSVGHandler {

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
        afpi.setState((AFPState)context.getProperty(
                AFPRendererContextConstants.AFP_STATE));
        afpi.setAFPDataStream((AFPDataStream)context.getProperty(
                AFPRendererContextConstants.AFP_DATASTREAM));

        Map foreign = (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
        QName qName = new QName(ExtensionElementMapping.URI, null, "conversion-mode");
        if (foreign != null 
                && "bitmap".equalsIgnoreCase((String)foreign.get(qName))) {
            afpi.paintAsBitmap = true;
        }
        return afpi;
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

        AFPRenderer renderer = (AFPRenderer)context.getRenderer();
        AFPInfo afpInfo = getAFPInfo(context);
        if (afpInfo.paintAsBitmap) {
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
        AFPState currentState = (AFPState)renderer.getState();
        currentState.setImageUri(uri);

        // set the data object parameters        
        ObjectAreaInfo objectAreaInfo = new ObjectAreaInfo();

        int x = (int)Math.round((afpInfo.getX() * 25.4f) / 1000f);
        objectAreaInfo.setX(x);

        int y = (int)Math.round((afpInfo.getY() * 25.4f) / 1000f);
        objectAreaInfo.setY(y);

        int resolution = afpInfo.getResolution();
        objectAreaInfo.setWidthRes(resolution);
        objectAreaInfo.setHeightRes(resolution);

        int width = (int)Math.round((afpInfo.getWidth() * resolution)
                / AFPConstants.DPI_72_MPTS);
        objectAreaInfo.setWidth(width);

        int height = (int)Math.round((afpInfo.getHeight() * resolution)
                / AFPConstants.DPI_72_MPTS);
        objectAreaInfo.setHeight(height);

        DataObjectInfo dataObjectInfo = new GraphicsObjectInfo();
        dataObjectInfo.setUri(uri);

        // Configure Graphics2D implementation
        final boolean textAsShapes = false;
        AFPGraphics2D graphics = new AFPGraphics2D(textAsShapes);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
        graphics.setAFPInfo(afpInfo);
        
        // Configure GraphicsObjectPainter with the Graphics2D implementation
        GraphicsObjectPainter painter = new GraphicsObjectPainter();
        painter.setGraphics2D(graphics);
        ((GraphicsObjectInfo)dataObjectInfo).setPainter(painter);

        boolean strokeText = false;
        Configuration cfg = afpInfo.getHandlerConfiguration();
        if (cfg != null) {
            strokeText = cfg.getChild("stroke-text", true).getValueAsBoolean(strokeText);
        }
        SVGUserAgent svgUserAgent
            = new SVGUserAgent(context.getUserAgent(), new AffineTransform());
    
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
                            
        Map/*<QName, String>*/ foreignAttributes
            = (Map/*<QName, String>*/)context.getProperty(
                RendererContextConstants.FOREIGN_ATTRIBUTES);
        dataObjectInfo.setResourceInfoFromForeignAttributes(foreignAttributes);
        
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
        
        Dimension2D dim = ctx.getDocumentSize();
        double w = dim.getWidth() * 1000f;
        double h = dim.getHeight() * 1000f;
        
        // convert to afp inches
        double scaleX = ((afpInfo.getWidth() / w) * resolution) / AFPConstants.DPI_72;
        double scaleY = ((afpInfo.getHeight() / h) * resolution) / AFPConstants.DPI_72;
        double xOffset = (afpInfo.getX() * resolution) / AFPConstants.DPI_72_MPTS;
        double yOffset
            = ((afpInfo.getHeight() - afpInfo.getY()) * resolution) / AFPConstants.DPI_72_MPTS;
    
        // Transformation matrix that establishes the local coordinate system
        // for the SVG graphic in relation to the current coordinate system
        // (note: y axis is inverted)
        AffineTransform trans = new AffineTransform(scaleX, 0, 0, -scaleY, xOffset, yOffset);
        graphics.setTransform(trans);

        // Set the object area info
        dataObjectInfo.setObjectAreaInfo(objectAreaInfo);

        // Create the object
        afpInfo.getAFPDataStream().createObject(dataObjectInfo);
    }
    
    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof AFPRenderer);
    }
    
    /** {@inheritDoc} */
    protected void updateRendererContext(RendererContext context) {
        //Work around a problem in Batik: Gradients cannot be done in ColorSpace.CS_GRAY
        context.setProperty(AFPRendererContextConstants.AFP_GRAYSCALE,
                Boolean.FALSE);
    }
}
