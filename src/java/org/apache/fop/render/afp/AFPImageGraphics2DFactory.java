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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.batik.bridge.BridgeContext;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPTextElementBridge;
import org.apache.fop.afp.AFPTextHandler;
import org.apache.fop.afp.AFPTextPainter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;


/**
 * An AFP image graphics 2d factory
 */
public class AFPImageGraphics2DFactory extends AFPDataObjectInfoFactory {

    /**
     * Main constructor
     *
     * @param state the AFP painting state
     */
    public AFPImageGraphics2DFactory(AFPPaintingState state) {
        super(state);
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPGraphicsObjectInfo();
    }

    /** {@inheritDoc} */
    public AFPDataObjectInfo create(AFPRendererImageInfo rendererImageInfo) throws IOException {
        AFPGraphicsObjectInfo graphicsObjectInfo
            = (AFPGraphicsObjectInfo)super.create(rendererImageInfo);

        AFPResourceInfo resourceInfo = graphicsObjectInfo.getResourceInfo();
        // level not explicitly set/changed so default to inline for GOCA graphic objects
        // (due to a bug in the IBM AFP Workbench Viewer (2.04.01.07) - hard copy works just fine)
        if (!resourceInfo.levelChanged()) {
            resourceInfo.setLevel(new AFPResourceLevel(AFPResourceLevel.INLINE));
        }

        // set mime type (unsupported by MOD:CA registry)
        graphicsObjectInfo.setMimeType(MimeConstants.MIME_AFP_GOCA);

        // set graphics 2d
        AFPGraphics2DAdapter g2dAdapter = rendererImageInfo.getGraphics2DAdapter();
        AFPGraphics2D g2d = g2dAdapter.getGraphics2D();
        graphicsObjectInfo.setGraphics2D(g2d);

        // set resource, state and font info
        RendererContext rendererContext = rendererImageInfo.getRendererContext();
        AFPInfo afpInfo = AFPSVGHandler.getAFPInfo(rendererContext);
        g2d.setResourceManager(afpInfo.getResourceManager());
        g2d.setResourceInfo(afpInfo.getResourceInfo());
        g2d.setPaintingState(afpInfo.getPaintingState());
        g2d.setFontInfo(afpInfo.getFontInfo());

        // set to default graphic context
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        // translate to current location
        AffineTransform at = state.getData().getTransform();
        g2d.translate(at.getTranslateX(), at.getTranslateY());

        // set afp state
        g2d.setPaintingState(state);

        // controls whether text painted by Batik is generated using text or path operations
        SVGUserAgent svgUserAgent
            = new SVGUserAgent(rendererContext.getUserAgent(), new AffineTransform());
        BridgeContext ctx = new BridgeContext(svgUserAgent);
        if (!afpInfo.strokeText()) {
            AFPTextHandler textHandler = new AFPTextHandler(g2d);
            g2d.setCustomTextHandler(textHandler);
            AFPTextPainter textPainter = new AFPTextPainter(textHandler);
            ctx.setTextPainter(textPainter);
            AFPTextElementBridge textElementBridge = new AFPTextElementBridge(textPainter);
            ctx.putBridge(textElementBridge);
        }

        // set painter
        ImageGraphics2D imageG2D = (ImageGraphics2D)rendererImageInfo.getImage();
        Graphics2DImagePainter painter = imageG2D.getGraphics2DImagePainter();
        graphicsObjectInfo.setPainter(painter);

        // set object area
        AFPObjectAreaInfo objectAreaInfo = graphicsObjectInfo.getObjectAreaInfo();
        int width = objectAreaInfo.getWidth();
        int height = objectAreaInfo.getHeight();
        Rectangle area = new Rectangle(width, height);
        graphicsObjectInfo.setArea(area);

        // invert y-axis for GOCA
        final int sx = 1;
        final int sy = -1;
        g2d.translate(0, height);
        g2d.scale(sx, sy);

        return graphicsObjectInfo;
    }

}
