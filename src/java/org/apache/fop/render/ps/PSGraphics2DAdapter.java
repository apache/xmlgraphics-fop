/*
 * Copyright 2005-2006 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.Graphics2DImagePainter;
import org.apache.fop.render.RendererContext;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Graphics2DAdapter implementation for PostScript.
 */
public class PSGraphics2DAdapter implements Graphics2DAdapter {

    private PSRenderer renderer;

    /**
     * Main constructor
     * @param renderer the Renderer instance to which this instance belongs
     */
    public PSGraphics2DAdapter(PSRenderer renderer) {
        this.renderer = renderer;
    }
    
    /** @see org.apache.fop.render.Graphics2DAdapter */
    public void paintImage(Graphics2DImagePainter painter, 
            RendererContext context,
            int x, int y, int width, int height) throws IOException {
        PSGenerator gen = renderer.gen;
        
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

        gen.commentln("%FOPBeginGraphics2D");
        gen.saveGraphicsState();
        // Clip to the image area.
        gen.writeln("newpath");
        gen.defineRect(fx, fy, fwidth, fheight);
        gen.writeln("clip");
        
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
        Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
        painter.paint(graphics, area);

        gen.restoreGraphicsState();
        gen.commentln("%FOPEndGraphics2D");
    }

}
