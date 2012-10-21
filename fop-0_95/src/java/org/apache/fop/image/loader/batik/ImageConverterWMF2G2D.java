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
 
package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.batik.transcoder.wmf.tosvg.WMFPainter;
import org.apache.batik.transcoder.wmf.tosvg.WMFRecordStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageConverter;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * This ImageConverter converts WMF (Windows Metafile) images (represented by Batik's
 * WMFRecordStore) to Java2D.
 */
public class ImageConverterWMF2G2D extends AbstractImageConverter {

    /** logger */
    private static Log log = LogFactory.getLog(ImageConverterWMF2G2D.class);

    /** {@inheritDoc} */
    public Image convert(Image src, Map hints) {
        checkSourceFlavor(src);
        ImageWMF wmf = (ImageWMF)src;

        Graphics2DImagePainter painter;
        painter = new Graphics2DImagePainterWMF(wmf);
        
        ImageGraphics2D g2dImage = new ImageGraphics2D(src.getInfo(), painter);
        return g2dImage;
    }

    /** {@inheritDoc} */
    public ImageFlavor getSourceFlavor() {
        return ImageWMF.WMF_IMAGE;
    }

    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return ImageFlavor.GRAPHICS2D;
    }

    private static class Graphics2DImagePainterWMF implements Graphics2DImagePainter {

        private ImageWMF wmf;
        
        public Graphics2DImagePainterWMF(ImageWMF wmf) {
            this.wmf = wmf;
        }
        
        /** {@inheritDoc} */
        public Dimension getImageSize() {
            return wmf.getSize().getDimensionMpt();
        }

        /** {@inheritDoc} */
        public void paint(Graphics2D g2d, Rectangle2D area) {
            WMFRecordStore wmfStore = wmf.getRecordStore();
            double w = area.getWidth();
            double h = area.getHeight();
            
            //Fit in paint area
            g2d.translate(area.getX(), area.getY());
            double sx = w / wmfStore.getWidthPixels();
            double sy = h / wmfStore.getHeightPixels();
            if (sx != 1.0 || sy != 1.0) {
                g2d.scale(sx, sy);
            }

            WMFPainter painter = new WMFPainter(wmfStore, 1.0f);
            long start = System.currentTimeMillis();
            painter.paint(g2d);
            if (log.isDebugEnabled()) {
                long duration = System.currentTimeMillis() - start;
                log.debug("Painting WMF took " + duration + " ms.");
            }
        }
        
    }
    
}
