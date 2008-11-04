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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;

/**
 * Image handler implementation that paints Graphics2D images in PCL. Since PCL is limited in its
 * vector graphics capabilities, there's a fallback built in that switches to bitmap painting
 * if an advanced feature is encountered.
 */
public class PCLImageHandlerGraphics2D implements ImageHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLImageHandlerGraphics2D.class);

    /** {@inheritDoc} */
    public int getPriority() {
        return 400;
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
        PCLRenderingContext pclContext = (PCLRenderingContext)context;
        ImageGraphics2D imageG2D = (ImageGraphics2D)image;
        Dimension imageDim = imageG2D.getSize().getDimensionMpt();
        PCLGenerator gen = pclContext.getPCLGenerator();

        Point2D transPoint = pclContext.transformedPoint(pos.x, pos.y);
        gen.setCursorPos(transPoint.getX(), transPoint.getY());

        boolean painted = false;
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        PCLGenerator tempGen = new PCLGenerator(baout, gen.getMaximumBitmapResolution());
        try {
            GraphicContext ctx = (GraphicContext)pclContext.getGraphicContext().clone();

            AffineTransform prepareHPGL2 = new AffineTransform();
            prepareHPGL2.scale(0.001, 0.001);
            ctx.setTransform(prepareHPGL2);

            PCLGraphics2D graphics = new PCLGraphics2D(tempGen);
            graphics.setGraphicContext(ctx);
            graphics.setClippingDisabled(false /*pclContext.isClippingDisabled()*/);
            Rectangle2D area = new Rectangle2D.Double(
                    0.0, 0.0, imageDim.getWidth(), imageDim.getHeight());
            imageG2D.getGraphics2DImagePainter().paint(graphics, area);

            //If we arrive here, the graphic is natively paintable, so write the graphic
            gen.writeCommand("*c" + gen.formatDouble4(pos.width / 100f) + "x"
                    + gen.formatDouble4(pos.height / 100f) + "Y");
            gen.writeCommand("*c0T");
            gen.enterHPGL2Mode(false);
            gen.writeText("\nIN;");
            gen.writeText("SP1;");
            //One Plotter unit is 0.025mm!
            double scale = imageDim.getWidth() / UnitConv.mm2pt(imageDim.getWidth() * 0.025);
            gen.writeText("SC0," + gen.formatDouble4(scale)
                    + ",0,-" + gen.formatDouble4(scale) + ",2;");
            gen.writeText("IR0,100,0,100;");
            gen.writeText("PU;PA0,0;\n");
            baout.writeTo(gen.getOutputStream()); //Buffer is written to output stream
            gen.writeText("\n");

            gen.enterPCLMode(false);
            painted = true;
        } catch (UnsupportedOperationException uoe) {
            log.debug(
                "Cannot paint graphic natively. Falling back to bitmap painting. Reason: "
                    + uoe.getMessage());
        }

        if (!painted) {
            //Fallback solution: Paint to a BufferedImage
            FOUserAgent ua = context.getUserAgent();
            ImageManager imageManager = ua.getFactory().getImageManager();
            ImageRendered imgRend;
            try {
                imgRend = (ImageRendered)imageManager.convertImage(
                        imageG2D, new ImageFlavor[] {ImageFlavor.RENDERED_IMAGE}/*, hints*/);
            } catch (ImageException e) {
                throw new IOException(
                        "Image conversion error while converting the image to a bitmap"
                          + " as a fallback measure: " + e.getMessage());
            }

            gen.paintBitmap(imgRend.getRenderedImage(), new Dimension(pos.width, pos.height),
                    pclContext.isSourceTransparencyEnabled());
        }
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        boolean supported = (image == null || image instanceof ImageGraphics2D)
                && targetContext instanceof PCLRenderingContext;
        if (supported) {
            Object hint = targetContext.getHint(PCLConstants.CONV_MODE);
            if ("bitmap".equals(hint)) {
                return false;
            }
        }
        return supported;
    }

}
