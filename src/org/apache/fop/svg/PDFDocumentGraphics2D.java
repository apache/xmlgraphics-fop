/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.fop.pdf.*;
import org.apache.fop.fonts.*;
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
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
    public PDFDocumentGraphics2D(boolean textAsShapes,
                                 OutputStream stream, int width, int height) {
        super(textAsShapes);

        if(!textAsShapes) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo);
            try {
                fontState = new FontState(fontInfo, "Helvetica", "normal", "normal", 12, 0);
            } catch(FOPException e) {
            }
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

    /**
     * Set the dimensions of the svg document that will be drawn.
     * This is useful if the dimensions of the svg document are different
     * from the pdf document that is to be created.
     * The result is scaled so that the svg fits correctly inside the pdf document.
     */
    public void setSVGDimension(float w, float h) {
        currentStream.write("" + PDFNumber.doubleOut(width / w) + " 0 0 " + PDFNumber.doubleOut(height / h) + " 0 0 cm\n");
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
        PDFPage currentPage =
          this.pdfDoc.makePage(pdfResources, pdfStream, width,
                               height, null);
        if(fontInfo != null) {
            FontSetup.addToResources(this.pdfDoc, fontInfo);
        }
        this.pdfDoc.output(stream);

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
     *             this graphics context.
     */
    public Graphics create() {
        return new PDFDocumentGraphics2D(this);
    }

}
