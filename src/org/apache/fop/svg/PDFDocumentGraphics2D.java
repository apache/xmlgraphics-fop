/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.svg;

import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.batik.ext.awt.g2d.GraphicContext;

/**
 * This class is a wrapper for the <tt>PDFGraphics2D</tt> that
 * is used to create a full document around the pdf rendering from
 * <tt>PDFGraphics2D</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 * @see org.apache.fop.svg.PDFGraphics2D
 */
public class PDFDocumentGraphics2D extends PDFGraphics2D {
    OutputStream stream;

    PDFStream pdfStream;
    int width;
    int height;

    FontInfo fontInfo = null;

    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document of the given height
     * and width.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     * @param stream the stream that the final document should be written to.
     * @param width the width of the document
     * @param height the height of the document
     */
    public PDFDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws FOPException {
        super(textAsShapes);

        if (!textAsShapes) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo);
            try {
                fontState = new FontState(fontInfo, "Helvetica", "normal",
                                          "normal", 12, 0);
            } catch (FOPException e) {}
        }
        standalone = true;
        this.stream = stream;
        this.pdfDoc = new PDFDocument();
        this.pdfDoc.setProducer("FOP SVG Renderer");
        pdfStream = this.pdfDoc.makeStream();
        this.width = width;
        this.height = height;

        currentFontName = "";
        currentFontSize = 0;
        currentYPosition = 0;
        currentXPosition = 0;

        currentStream.write("1 0 0 -1 0 " + height + " cm\n");

    }

    public FontState getFontState() {
        return fontState;
    }

    public PDFDocument getPDFDocument() {
        return this.pdfDoc;
    }

    /**
     * Set the dimensions of the svg document that will be drawn.
     * This is useful if the dimensions of the svg document are different
     * from the pdf document that is to be created.
     * The result is scaled so that the svg fits correctly inside the pdf document.
     */
    public void setSVGDimension(float w, float h) {
        currentStream.write("" + PDFNumber.doubleOut(width / w) + " 0 0 "
                            + PDFNumber.doubleOut(height / h) + " 0 0 cm\n");
    }

    /**
     * Set the background of the pdf document.
     * This is used to set the background for the pdf document
     * Rather than leaving it as the default white.
     */
    public void setBackgroundColor(Color col) {
        Color c = col;
        currentColour = new PDFColor(c.getRed(), c.getGreen(), c.getBlue());
        currentStream.write("q\n");
        currentStream.write(currentColour.getColorSpaceOut(true));

        currentStream.write("0 0 " + width + " " + height + " re\n");

        currentStream.write("f\n");
        currentStream.write("Q\n");
    }

    /**
     * The rendering process has finished.
     * This should be called after the rendering has completed as there is
     * no other indication it is complete.
     * This will then write the results to the output stream.
     */
    public void finish() throws IOException {
        pdfStream.add(getString());
        PDFResources pdfResources = this.pdfDoc.getResources();
        PDFPage currentPage = this.pdfDoc.makePage(pdfResources, pdfStream,
                                                   width, height, null);
        if(currentAnnotList != null) {
            currentPage.setAnnotList(currentAnnotList);
        }
        if (fontInfo != null) {
            FontSetup.addToResources(this.pdfDoc, fontInfo);
        }
        pdfDoc.outputHeader(stream);
        this.pdfDoc.output(stream);
        pdfDoc.outputTrailer(stream);
    }

    public void setGraphicContext(GraphicContext c) {
        gc = c;
    }

    /**
     * This constructor supports the create method
     */
    public PDFDocumentGraphics2D(PDFDocumentGraphics2D g) {
        super(g);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    public Graphics create() {
        return new PDFDocumentGraphics2D(this);
    }

    public void drawString(String s, float x, float y) {
        if (super.textAsShapes) {
            Font font = super.getFont();
            FontRenderContext frc = super.getFontRenderContext();
            GlyphVector gv = font.createGlyphVector(frc, s);
            Shape glyphOutline = gv.getOutline(x, y);
            super.fill(glyphOutline);
        } else {
            super.drawString(s, x, y);
        }
    }

}

