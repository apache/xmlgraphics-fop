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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContext.RendererContextWrapper;

/**
 * Graphics2DAdapter implementation for AFP.
 */
public class AFPGraphics2DAdapter extends AbstractGraphics2DAdapter {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPGraphics2DAdapter.class);

    /**
     * Main constructor
     */
    public AFPGraphics2DAdapter() {
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext context,
            int x, int y, int width, int height) throws IOException {

        AFPInfo afpInfo = AFPSVGHandler.getAFPInfo(context);

        final boolean textAsShapes = false;
        AFPGraphics2D graphics = new AFPGraphics2D(textAsShapes);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
        
        if (afpInfo.paintAsBitmap) {
            //Fallback solution: Paint to a BufferedImage
            int resolution = (int)Math.round(context.getUserAgent().getTargetResolution());
            RendererContextWrapper ctx = RendererContext.wrapRendererContext(context);
            BufferedImage bi = paintToBufferedImage(painter, ctx, resolution, false, false);

            float scale = AFPRenderer.NORMAL_AFP_RESOLUTION 
                            / context.getUserAgent().getTargetResolution();
            graphics.drawImage(bi, new AffineTransform(scale, 0, 0, scale, 0, 0), null);
        } else {
            // get the 'width' and 'height' attributes of the SVG document
            Dimension dim = painter.getImageSize();
            float imw = (float)dim.getWidth() / 1000f;
            float imh = (float)dim.getHeight() / 1000f;

            Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
            painter.paint(graphics, area);
        }
    }
}
