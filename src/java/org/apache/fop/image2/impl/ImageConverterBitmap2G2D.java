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
 
package org.apache.fop.image2.impl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.render.Graphics2DImagePainter;

/**
 * This ImageConverter wraps a bitmap image in a Graphics2D image.
 */
public class ImageConverterBitmap2G2D extends AbstractImageConverter {

    /** {@inheritDoc} */
    public Image convert(Image src, Map hints) {
        checkSourceFlavor(src);
        final ImageRendered rendImage = (ImageRendered)src;

        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public Dimension getImageSize() {
                return rendImage.getSize().getDimensionMpt();
            }

            public void paint(Graphics2D g2d, Rectangle2D area) {
                ImageSize imageSize = rendImage.getSize();
                double w = area.getWidth();
                double h = area.getHeight();

                AffineTransform at = new AffineTransform();
                //Fit in paint area
                Dimension2D sizePt = imageSize.getDimensionPt();
                double sx = w / sizePt.getWidth();
                double sy = h / sizePt.getHeight();
                if (sx != 1.0 || sy != 1.0) {
                    at.scale(sx, sy);
                }
                //Scale image to fit
                sx = w / imageSize.getWidthPx();
                sy = h / imageSize.getHeightPx();
                if (sx != 1.0 || sy != 1.0) {
                    at.scale(sx, sy);
                }
                g2d.drawRenderedImage(rendImage.getRenderedImage(), at);
            }
            
        };
        
        ImageGraphics2D g2dImage = new ImageGraphics2D(src.getInfo(), painter);
        return g2dImage;
    }

    /** {@inheritDoc} */
    public ImageFlavor getSourceFlavor() {
        return ImageFlavor.RENDERED_IMAGE;
    }

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return ImageFlavor.GRAPHICS2D;
    }

}
