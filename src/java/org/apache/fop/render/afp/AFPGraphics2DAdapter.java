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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Graphics2DAdapter implementation for AFP.
 */
public class AFPGraphics2DAdapter extends AbstractGraphics2DAdapter {

    private final AFPRenderer renderer;

    private final AFPGraphics2D g2d;

    /**
     * Main constructor
     *
     * @param renderer the afp renderer
     */
    public AFPGraphics2DAdapter(AFPRenderer renderer) {
        this.renderer = renderer;

        final boolean textAsShapes = false;
        this.g2d = new AFPGraphics2D(textAsShapes);
    }

    /**
     * Returns the AFP graphics 2D implementation
     *
     * @return the AFP graphics 2D implementation
     */
    public AFPGraphics2D getGraphics2D() {
        return g2d;
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext context,
            int x, int y, int width, int height) throws IOException {

        AFPInfo afpInfo = AFPSVGHandler.getAFPInfo(context);

        // set resource manager
        AFPResourceManager resourceManager = afpInfo.getResourceManager();
        g2d.setResourceManager(resourceManager);

        // set resource information
        AFPResourceInfo resourceInfo = afpInfo.getResourceInfo();
        g2d.setResourceInfo(resourceInfo);

        // set painting state
        AFPPaintingState paintingState = afpInfo.getPaintingState();
        g2d.setPaintingState(paintingState);

        // set graphic context
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        float fwidth = width / 1000f;
        float fheight = height / 1000f;

        // get the 'width' and 'height' attributes of the SVG document
        Dimension imageSize = painter.getImageSize();
        float imw = (float)imageSize.getWidth() / 1000f;
        float imh = (float)imageSize.getHeight() / 1000f;
        float sx = fwidth / imw;
        float sy = fheight / imh;
        AffineTransform at = new AffineTransform(sx, 0, 0, sy, x, y);

        renderer.saveGraphicsState();

        if (afpInfo.paintAsBitmap()) {
            //Fallback solution: Paint to a BufferedImage
            int resolution = Math.round(context.getUserAgent().getTargetResolution());
            RendererContextWrapper ctx = RendererContext.wrapRendererContext(context);
            BufferedImage bufferedImage = paintToBufferedImage(painter, ctx, resolution, false, false);

            AFPPaintingState state = afpInfo.getPaintingState();
            AffineTransform trans = state.getData().getTransform();
            float scale = AFPRenderer.NORMAL_AFP_RESOLUTION
                            / context.getUserAgent().getTargetResolution();
            if (scale != 1) {
                at.scale(scale, scale);
            }

            if (!at.isIdentity()) {
                trans.concatenate(at);
            }

            g2d.drawImage(bufferedImage, trans, null);
        } else {
            AFPGraphicsObjectInfo graphicsObjectInfo = new AFPGraphicsObjectInfo();
            graphicsObjectInfo.setPainter(painter);
            graphicsObjectInfo.setGraphics2D(g2d);

            Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
            graphicsObjectInfo.setArea(area);
            resourceManager.createObject(graphicsObjectInfo);
        }

        renderer.restoreGraphicsState();
    }
}
