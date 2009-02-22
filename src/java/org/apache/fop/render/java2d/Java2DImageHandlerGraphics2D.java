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

package org.apache.fop.render.java2d;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;

import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;

/**
 * Image handler implementation that paints {@code Graphics2D} image on another {@code Graphics2D}
 * target.
 */
public class Java2DImageHandlerGraphics2D implements ImageHandler {

    /** {@inheritDoc} */
    public int getPriority() {
        return 200;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageGraphics2D.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return new ImageFlavor[] {
            ImageFlavor.GRAPHICS2D
        };
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        Java2DRenderingContext java2dContext = (Java2DRenderingContext)context;
        ImageInfo info = image.getInfo();
        ImageGraphics2D imageG2D = (ImageGraphics2D)image;

        Dimension dim = info.getSize().getDimensionMpt();

        Graphics2D g2d = (Graphics2D)java2dContext.getGraphics2D().create();
        g2d.translate(pos.x, pos.y);
        double sx = pos.width / dim.getWidth();
        double sy = pos.height / dim.getHeight();
        g2d.scale(sx, sy);

        Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, dim.getWidth(), dim.getHeight());
        imageG2D.getGraphics2DImagePainter().paint(g2d, area);
        g2d.dispose();
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        return (image == null || image instanceof ImageGraphics2D)
                && targetContext instanceof Java2DRenderingContext;
    }

}
