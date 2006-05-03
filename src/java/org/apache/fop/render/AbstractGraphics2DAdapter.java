/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.Graphics2DImagePainter;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.util.UnitConv;

/**
 * Graphics2DAdapter implementation for PCL and HP GL/2.
 */
public abstract class AbstractGraphics2DAdapter implements Graphics2DAdapter {

    /**
     * Paints the image to a BufferedImage and returns that.
     * @param painter the painter which will paint the actual image
     * @param context the renderer context for the current renderer
     * @param resolution the requested bitmap resolution
     * @param gray true if the generated image should be in grayscales
     * @return the generated BufferedImage
     */
    protected BufferedImage paintToBufferedImage(Graphics2DImagePainter painter, 
             RendererContextWrapper context, int resolution, boolean gray) {
        int bmw = UnitConv.mpt2px(context.getWidth(), resolution);
        int bmh = UnitConv.mpt2px(context.getHeight(), resolution);
        BufferedImage bi;
        if (gray) {
            bi = new BufferedImage(bmw, bmh, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            bi = new BufferedImage(bmw, bmh, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g2d = bi.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            setRenderingHintsForBufferedImage(g2d);
            
            g2d.setBackground(Color.white);
            g2d.setColor(Color.black);
            g2d.clearRect(0, 0, bmw, bmh);
            double sx = (double)bmw / context.getWidth();
            double sy = (double)bmh / context.getHeight();
            g2d.scale(sx, sy);

            //Paint the image on the BufferedImage
            Rectangle2D area = new Rectangle2D.Double(
                    0.0, 0.0, context.getWidth(), context.getHeight());
            painter.paint(g2d, area);
        } finally {
            g2d.dispose();
        }
        return bi;
    }

    /**
     * Sets rendering hints on the Graphics2D created for painting to a BufferedImage. Subclasses
     * can modify the settings to customize the behaviour.
     * @param g2d the Graphics2D instance
     */
    protected void setRenderingHintsForBufferedImage(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

}
