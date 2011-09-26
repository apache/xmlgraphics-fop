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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.FormGenerator;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSProcSets;

/**
 * Image handler implementation which handles vector graphics (Java2D) for PostScript output.
 */
public class PSImageHandlerGraphics2D implements PSImageHandler {

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.GRAPHICS2D
    };

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
                throws IOException {
        PSRenderingContext psContext = (PSRenderingContext)context;
        PSGenerator gen = psContext.getGenerator();
        ImageGraphics2D imageG2D = (ImageGraphics2D)image;
        Graphics2DImagePainter painter = imageG2D.getGraphics2DImagePainter();

        float fx = (float)pos.getX() / 1000f;
        float fy = (float)pos.getY() / 1000f;
        float fwidth = (float)pos.getWidth() / 1000f;
        float fheight = (float)pos.getHeight() / 1000f;

        // get the 'width' and 'height' attributes of the SVG document
        Dimension dim = painter.getImageSize();
        float imw = (float)dim.getWidth() / 1000f;
        float imh = (float)dim.getHeight() / 1000f;

        float sx = fwidth / (float)imw;
        float sy = fheight / (float)imh;

        gen.commentln("%FOPBeginGraphics2D");
        gen.saveGraphicsState();
        final boolean clip = false;
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
        Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imw, imh);
        painter.paint(graphics, area);
        gen.restoreGraphicsState();
        gen.commentln("%FOPEndGraphics2D");
    }

    /** {@inheritDoc} */
    public void generateForm(RenderingContext context, Image image, final PSImageFormResource form)
            throws IOException {
        PSRenderingContext psContext = (PSRenderingContext)context;
        PSGenerator gen = psContext.getGenerator();
        final ImageGraphics2D imageG2D = (ImageGraphics2D)image;
        ImageInfo info = image.getInfo();

        FormGenerator formGen = buildFormGenerator(gen.getPSLevel(), form, info, imageG2D);
        formGen.generate(gen);
    }
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
        return FLAVORS;
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        if (targetContext instanceof PSRenderingContext) {
            return (image == null || image instanceof ImageGraphics2D);
        }
        return false;
    }

    private FormGenerator buildFormGenerator(int psLanguageLevel, final PSImageFormResource form,
            final ImageInfo info, final ImageGraphics2D imageG2D) {
        String imageDescription = info.getMimeType() + " " + info.getOriginalURI();
        final Dimension2D dimensionsPt = info.getSize().getDimensionPt();
        final Dimension2D dimensionsMpt = info.getSize().getDimensionMpt();
        FormGenerator formGen;

        if (psLanguageLevel <= 2) {
            formGen = new EPSFormGenerator(form.getName(), imageDescription, dimensionsPt) {

                @Override
                void doGeneratePaintProc(PSGenerator gen) throws IOException {
                    paintImageG2D(imageG2D, dimensionsMpt, gen);
                }
            };
        } else {
            formGen = new EPSFormGenerator(form.getName(), imageDescription, dimensionsPt) {

                @Override
                protected void generateAdditionalDataStream(PSGenerator gen) throws IOException {
                    gen.writeln("/" + form.getName() + ":Data currentfile <<");
                    gen.writeln("  /Filter /SubFileDecode");
                    gen.writeln("  /DecodeParms << /EODCount 0 /EODString (%FOPEndOfData) >>");
                    gen.writeln(">> /ReusableStreamDecode filter");
                    paintImageG2D(imageG2D, dimensionsMpt, gen);
                    gen.writeln("%FOPEndOfData");
                    gen.writeln("def");
                }

                @Override
                void doGeneratePaintProc(PSGenerator gen) throws IOException {
                    gen.writeln(form.getName() + ":Data 0 setfileposition");
                    gen.writeln(form.getName() + ":Data cvx exec");
                }
            };
        }
        return formGen;
    }

    private static abstract class EPSFormGenerator extends FormGenerator {

        EPSFormGenerator(String formName, String title, Dimension2D dimensions) {
            super(formName, title, dimensions);
        }

        protected void paintImageG2D(final ImageGraphics2D imageG2D, Dimension2D dimensionsMpt,
                PSGenerator gen) throws IOException {
            PSGraphics2DAdapter adapter = new PSGraphics2DAdapter(gen, false);
            adapter.paintImage(imageG2D.getGraphics2DImagePainter(),
                        null,
                        0, 0,
                        (int) Math.round(dimensionsMpt.getWidth()),
                        (int) Math.round(dimensionsMpt.getHeight()));
        }

        @Override
        protected final void generatePaintProc(PSGenerator gen) throws IOException {
            gen.getResourceTracker().notifyResourceUsageOnPage(
                    PSProcSets.EPS_PROCSET);
            gen.writeln("BeginEPSF");
            doGeneratePaintProc(gen);
            gen.writeln("EndEPSF");
        }

        abstract void doGeneratePaintProc(PSGenerator gen) throws IOException;
    }
}
