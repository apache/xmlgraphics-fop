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
    protected AFPGraphics2D getGraphics2D() {
        return g2d;
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext context,
            int x, int y, int width, int height) throws IOException {

        // get the 'width' and 'height' attributes of the SVG document
        Dimension dim = painter.getImageSize();


        AFPInfo afpInfo = AFPSVGHandler.getAFPInfo(context);
        g2d.setAFPInfo(afpInfo);
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

//        // scale/convert to afp units
        AFPState state = afpInfo.getState();
//        AFPUnitConverter unitConv = state.getUnitConverter();
//        float scale = unitConv.mpt2units(1);

        float fwidth = width / 1000f;
        float fheight = height / 1000f;
        float imw = (float)dim.getWidth() / 1000f;
        float imh = (float)dim.getHeight() / 1000f;
        float sx = fwidth / imw;
        float sy = fheight / imh;
//        float fx = x / 1000f;
//        float fy = y / 1000f;
        AffineTransform at = new AffineTransform(sx, 0, 0, sy, x, y);

        renderer.saveGraphicsState();

        if (afpInfo.paintAsBitmap()) {
            //Fallback solution: Paint to a BufferedImage
            int resolution = Math.round(context.getUserAgent().getTargetResolution());
            RendererContextWrapper ctx = RendererContext.wrapRendererContext(context);
            BufferedImage bi = paintToBufferedImage(painter, ctx, resolution, false, false);

            AffineTransform trans = state.getData().getTransform();
            float scale = AFPRenderer.NORMAL_AFP_RESOLUTION
                            / context.getUserAgent().getTargetResolution();
            if (scale != 1) {
                at.scale(scale, scale);
                if (!at.isIdentity()) {
                    trans.concatenate(at);
                }
            }


            // concatenate to transformation matrix
//            state.concatenate(at);

            // draw image using current transformation matrix
//            at = state.getData().getTransform();
            g2d.drawImage(bi, trans, null);
        } else {
            AFPGraphicsObjectInfo graphicsObjectInfo = new AFPGraphicsObjectInfo();
            graphicsObjectInfo.setPainter(painter);
            graphicsObjectInfo.setGraphics2D(g2d);

            Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
            graphicsObjectInfo.setArea(area);
            AFPResourceManager resourceManager = (AFPResourceManager)context.getProperty(
                    AFPRendererContextConstants.AFP_RESOURCE_MANAGER);
            resourceManager.createObject(graphicsObjectInfo);
        }

        renderer.restoreGraphicsState();
    }
}
