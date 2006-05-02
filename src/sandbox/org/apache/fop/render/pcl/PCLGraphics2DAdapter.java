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
 
package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.Graphics2DImagePainter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.util.UnitConv;
import org.apache.xmlgraphics.java2d.GraphicContext;

/**
 * Graphics2DAdapter implementation for PCL and HP GL/2.
 */
public class PCLGraphics2DAdapter implements Graphics2DAdapter {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLGraphics2DAdapter.class);

    /**
     * Main constructor
     */
    public PCLGraphics2DAdapter() {
    }
    
    /** @see org.apache.fop.render.Graphics2DAdapter */
    public void paintImage(Graphics2DImagePainter painter, 
            RendererContext context,
            int x, int y, int width, int height) throws IOException {
        PCLRendererContext pclContext = PCLRendererContext.wrapRendererContext(context);
        PCLRenderer pcl = (PCLRenderer)context.getRenderer();
        PCLGenerator gen = pcl.gen;
        
        // get the 'width' and 'height' attributes of the SVG document
        Dimension dim = painter.getImageSize();
        float imw = (float)dim.getWidth();
        float imh = (float)dim.getHeight();

        boolean painted = false;
        boolean paintAsBitmap = pclContext.paintAsBitmap();
        if (!paintAsBitmap) {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            PCLGenerator tempGen = new PCLGenerator(baout);
            try {
                GraphicContext ctx = (GraphicContext)pcl.getGraphicContext().clone();

                AffineTransform prepareHPGL2 = new AffineTransform();
                prepareHPGL2.scale(0.001, 0.001);
                ctx.setTransform(prepareHPGL2);

                PCLGraphics2D graphics = new PCLGraphics2D(tempGen);
                graphics.setGraphicContext(ctx);
                Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
                painter.paint(graphics, area);
                
                //If we arrive here, the graphic is natively paintable, so write the graphic
                pcl.saveGraphicsState();
                pcl.setCursorPos(x, y);
                gen.writeCommand("*c" + gen.formatDouble4(width / 100f) + "x" 
                        + gen.formatDouble4(height / 100f) + "Y");
                gen.writeCommand("*c0T");
                gen.enterHPGL2Mode(false);
                gen.writeText("\nIN;");
                gen.writeText("SP1;");
                //One Plotter unit is 0.025mm!
                double scale = imw / UnitConv.mm2pt(imw * 0.025);
                gen.writeText("SC0," + gen.formatDouble4(scale) 
                        + ",0,-" + gen.formatDouble4(scale) + ",2;");
                gen.writeText("IR0,100,0,100;");
                gen.writeText("PU;PA0,0;\n");
                baout.writeTo(gen.getOutputStream()); //Buffer is written to output stream
                gen.writeText("\n");

                gen.enterPCLMode(false);
                pcl.restoreGraphicsState();
                painted = true;
            } catch (UnsupportedOperationException uoe) {
                log.debug(
                    "Cannot paint graphic natively. Falling back to bitmap painting. Reason: " 
                        + uoe.getMessage());
            }
        }
        
        if (!painted) {
            int resolution = 300; //TODO not hard-coded, please!
            int bmw = UnitConv.mpt2px(pclContext.getWidth(), resolution);
            int bmh = UnitConv.mpt2px(pclContext.getHeight(), resolution);
            BufferedImage bi = new BufferedImage(
                    bmw, bmh,
                    BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = bi.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                
                g2d.setBackground(Color.white);
                g2d.setColor(Color.black);
                g2d.clearRect(0, 0, bmw, bmh);
                double sx = (double)bmw / pclContext.getWidth();
                double sy = (double)bmh / pclContext.getHeight();
                g2d.scale(sx, sy);

                //Paint the SVG on the BufferedImage
                Rectangle2D area = new Rectangle2D.Double(
                        0.0, 0.0, pclContext.getWidth(), pclContext.getHeight());
                painter.paint(g2d, area);
            } finally {
                g2d.dispose();
            }

            pcl.setCursorPos(x, y);
            gen.paintBitmap(bi, resolution);
        }
    }

}
