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

package org.apache.fop.render.pcl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;

/**
 * Image handler implementation that paints {@code RenderedImage} instances in PCL.
 */
public class PCLImageHandlerRenderedImage implements ImageHandler {

    /** {@inheritDoc} */
    public int getPriority() {
        return 300;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageRendered.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return new ImageFlavor[] {
            ImageFlavor.BUFFERED_IMAGE,
            ImageFlavor.RENDERED_IMAGE,
        };
    }

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
            throws IOException {
        PCLRenderingContext pclContext = (PCLRenderingContext)context;
        ImageRendered imageRend = (ImageRendered)image;
        PCLGenerator gen = pclContext.getPCLGenerator();

        RenderedImage ri = imageRend.getRenderedImage();
        Point2D transPoint = pclContext.transformedPoint(pos.x, pos.y);
        gen.setCursorPos(transPoint.getX(), transPoint.getY());
        gen.paintBitmap(ri, new Dimension(pos.width, pos.height),
                pclContext.isSourceTransparencyEnabled());
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        return (image == null || image instanceof ImageRendered)
                && targetContext instanceof PCLRenderingContext;
    }

}
