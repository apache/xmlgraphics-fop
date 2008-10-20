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

package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.render.AbstractGraphics2DAdapter;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.RendererContext.RendererContextWrapper;
import org.apache.fop.render.pdf.PDFRenderer;

/**
 * Graphics2DAdapter implementation for PostScript.
 */
public class PSGraphics2DAdapter extends AbstractGraphics2DAdapter {

    /** Qualified name for the "conversion-mode" extension attribute. */
    protected static final QName CONVERSION_MODE = new QName(
            ExtensionElementMapping.URI, null, "conversion-mode");

    private PSGenerator gen;
    private boolean clip = true;

    /**
     * Main constructor
     * @param renderer the Renderer instance to which this instance belongs
     */
    public PSGraphics2DAdapter(PSRenderer renderer) {
        this(renderer.gen, true);
    }

    /**
     * Constructor for use without a PSRenderer instance.
     * @param gen the PostScript generator
     * @param clip true if the image should be clipped
     */
    public PSGraphics2DAdapter(PSGenerator gen, boolean clip) {
        this.gen = gen;
        this.clip = clip;
    }

    /** {@inheritDoc} */
    public void paintImage(Graphics2DImagePainter painter,
            RendererContext context,
            int x, int y, int width, int height) throws IOException {
        float fwidth = width / 1000f;
        float fheight = height / 1000f;
        float fx = x / 1000f;
        float fy = y / 1000f;

        // get the 'width' and 'height' attributes of the SVG document
        Dimension dim = painter.getImageSize();
        float imw = (float)dim.getWidth() / 1000f;
        float imh = (float)dim.getHeight() / 1000f;

        boolean paintAsBitmap = false;
        if (context != null) {
            Map foreign = (Map)context.getProperty(RendererContextConstants.FOREIGN_ATTRIBUTES);
            paintAsBitmap = (foreign != null
                   && "bitmap".equalsIgnoreCase((String)foreign.get(CONVERSION_MODE)));
        }

        float sx = paintAsBitmap ? 1.0f : (fwidth / (float)imw);
        float sy = paintAsBitmap ? 1.0f : (fheight / (float)imh);

        gen.commentln("%FOPBeginGraphics2D");
        gen.saveGraphicsState();
        if (clip) {
            // Clip to the image area.
            gen.writeln("newpath");
            gen.defineRect(fx, fy, fwidth, fheight);
            gen.writeln("clip");
        }

        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right. (0,0) is where the
        // viewBox puts it.
        gen.concatMatrix(sx, 0, 0, sy, fx, fy);

        final boolean textAsShapes = false;
        PSGraphics2D graphics = new PSGraphics2D(textAsShapes, gen);
        graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
        AffineTransform transform = new AffineTransform();
        // scale to viewbox
        transform.translate(fx, fy);
        gen.getCurrentState().concatMatrix(transform);
        if (paintAsBitmap) {
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

        gen.restoreGraphicsState();
        gen.commentln("%FOPEndGraphics2D");
    }

}
