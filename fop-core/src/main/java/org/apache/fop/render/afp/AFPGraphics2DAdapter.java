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

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.AFPGraphicsObjectInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContext.RendererContextWrapper;

/**
 * Graphics2DAdapter implementation for AFP.
 */
public class AFPGraphics2DAdapter extends AbstractGraphics2DAdapter {

    private final AFPPaintingState paintingState;

    /**
     * Main constructor
     *
     * @param paintingState the AFP painting state
     */
    public AFPGraphics2DAdapter(AFPPaintingState paintingState) {
        this.paintingState = paintingState;
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext rendererContext,
            int x, int y, int width, int height) throws IOException {

        AFPRendererContext afpRendererContext = (AFPRendererContext)rendererContext;
        AFPInfo afpInfo = afpRendererContext.getInfo();

        final boolean textAsShapes = false;
        AFPGraphics2D g2d = afpInfo.createGraphics2D(textAsShapes);

        paintingState.save();

        //Fallback solution: Paint to a BufferedImage
        if (afpInfo.paintAsBitmap()) {

            // paint image
            RendererContextWrapper rendererContextWrapper
                = RendererContext.wrapRendererContext(rendererContext);
            float targetResolution = rendererContext.getUserAgent().getTargetResolution();
            int resolution = Math.round(targetResolution);
            boolean colorImages = afpInfo.isColorSupported();
            BufferedImage bufferedImage = paintToBufferedImage(
                    painter, rendererContextWrapper, resolution, !colorImages, false);

            // draw image
            AffineTransform at = paintingState.getData().getTransform();
            at.translate(x, y);
            g2d.drawImage(bufferedImage, at, null);
        } else {
            AFPGraphicsObjectInfo graphicsObjectInfo = new AFPGraphicsObjectInfo();
            graphicsObjectInfo.setPainter(painter);
            graphicsObjectInfo.setGraphics2D(g2d);

            // get the 'width' and 'height' attributes of the SVG document
            Dimension imageSize = painter.getImageSize();
            float imw = (float)imageSize.getWidth() / 1000f;
            float imh = (float)imageSize.getHeight() / 1000f;

            Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
            graphicsObjectInfo.setArea(area);
            AFPResourceManager resourceManager = afpInfo.getResourceManager();
            resourceManager.createObject(graphicsObjectInfo);
        }

        paintingState.restore();
    }

    /** {@inheritDoc} */
    protected int mpt2px(int unit, int resolution) {
        return Math.round(paintingState.getUnitConverter().mpt2units(unit));
    }
}
