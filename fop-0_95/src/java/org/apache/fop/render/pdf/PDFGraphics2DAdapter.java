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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.svg.PDFGraphics2D;

/**
 * Graphics2DAdapter implementation for PDF.
 */
public class PDFGraphics2DAdapter extends AbstractGraphics2DAdapter {

    private PDFRenderer renderer;

    /**
     * Main constructor
     * @param renderer the Renderer instance to which this instance belongs
     */
    public PDFGraphics2DAdapter(PDFRenderer renderer) {
        this.renderer = renderer;
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext context,
            int x, int y, int width, int height) throws IOException {

        PDFSVGHandler.PDFInfo pdfInfo = PDFSVGHandler.getPDFInfo(context);
        float fwidth = width / 1000f;
        float fheight = height / 1000f;
        float fx = x / 1000f;
        float fy = y / 1000f;

        // get the 'width' and 'height' attributes of the SVG document
        Dimension dim = painter.getImageSize();
        float imw = (float)dim.getWidth() / 1000f;
        float imh = (float)dim.getHeight() / 1000f;

        float sx = fwidth / (float)imw;
        float sy = fheight / (float)imh;

        renderer.saveGraphicsState();
        renderer.setColor(Color.black, false, null);
        renderer.setColor(Color.black, true, null);

        //TODO Clip to the image area.

        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right. (0,0) is where the
        // viewBox puts it.
        renderer.currentStream.add(sx + " 0 0 " + sy + " " + fx + " "
                          + fy + " cm\n");


        final boolean textAsShapes = false;
        if (pdfInfo.pdfContext == null) {
            pdfInfo.pdfContext = pdfInfo.pdfPage;
        }
        PDFGraphics2D graphics = new PDFGraphics2D(textAsShapes,
                pdfInfo.fi, pdfInfo.pdfDoc,
                pdfInfo.pdfContext, pdfInfo.pdfPage.referencePDF(),
                renderer.currentFontName,
                renderer.currentFontSize);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        AffineTransform transform = new AffineTransform();
        transform.translate(fx, fy);
        pdfInfo.pdfState.concatenate(transform);
        graphics.setPDFState(pdfInfo.pdfState);
        graphics.setOutputStream(pdfInfo.outputStream);

        if (pdfInfo.paintAsBitmap) {
            //Fallback solution: Paint to a BufferedImage
            int resolution = (int)Math.round(context.getUserAgent().getTargetResolution());
            RendererContextWrapper ctx = RendererContext.wrapRendererContext(context);
            BufferedImage bi = paintToBufferedImage(painter, ctx, resolution, false, false);

            float scale = PDFRenderer.NORMAL_PDF_RESOLUTION
                            / context.getUserAgent().getTargetResolution();
            graphics.drawImage(bi, new AffineTransform(scale, 0, 0, scale, 0, 0), null);
        } else {
            Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
            painter.paint(graphics, area);
        }

        pdfInfo.currentStream.add(graphics.getString());
        renderer.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void setRenderingHintsForBufferedImage(Graphics2D g2d) {
        super.setRenderingHintsForBufferedImage(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

}
