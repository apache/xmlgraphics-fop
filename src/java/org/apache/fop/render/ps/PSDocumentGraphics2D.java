/*
 * $Id: PDFDocumentGraphics2D.java,v 1.27 2003/03/07 09:51:26 jeremias Exp $
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
package org.apache.fop.render.ps;

//Java
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.io.OutputStream;
import java.io.IOException;

//FOP
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.layout.FontInfo;

/**
 * This class is a wrapper for the <tt>PSGraphics2D</tt> that
 * is used to create a full document around the PostScript rendering from
 * <tt>PSGraphics2D</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: PDFDocumentGraphics2D.java,v 1.27 2003/03/07 09:51:26 jeremias Exp $
 * @see org.apache.fop.svg.PSGraphics2D
 */
public class PSDocumentGraphics2D extends PSGraphics2D {
    
    private int width;
    private int height;

    /**
     * Create a new PSDocumentGraphics2D.
     * This is used to create a new PostScript document, the height,
     * width and output stream can be setup later.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     */
    PSDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);

        if (!textAsShapes) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo, null);
            //FontState fontState = new FontState("Helvetica", "normal",
            //                          FontInfo.NORMAL, 12, 0);
        }

        currentFontName = "";
        currentFontSize = 0;
    }

    /**
     * Setup the document.
     * @param stream the output stream to write the document
     * @param width the width of the page
     * @param height the height of the page
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public void setupDocument(OutputStream stream, int width, int height) throws IOException {
        this.width = width;
        this.height = height;

        final Integer zero = new Integer(0);
        final Long pagewidth = new Long(this.width);
        final Long pageheight = new Long(this.height);

        //Setup for PostScript generation
        setPSGenerator(new PSGenerator(stream));
        
        //PostScript Header
        gen.writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR, 
                    new String[] {"FOP PostScript Transcoder for SVG"});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, 
                    new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.PAGES, new Object[] {new Integer(1)});
        gen.writeDSCComment(DSCConstants.BBOX, new Object[]
                {zero, zero, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.END_COMMENTS);
        
        //Defaults
        gen.writeDSCComment(DSCConstants.BEGIN_DEFAULTS);
        gen.writeDSCComment(DSCConstants.END_DEFAULTS);
        
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        gen.writeDSCComment(DSCConstants.END_PROLOG);
        
        //Setup
        gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
        PSProcSets.writeFOPStdProcSet(gen);
        PSProcSets.writeFOPEPSProcSet(gen);
        PSRenderer.writeFontDict(gen, fontInfo);
        gen.writeDSCComment(DSCConstants.END_SETUP);

        //Start page
        Integer pageNumber = new Integer(1);
        gen.writeDSCComment(DSCConstants.PAGE, new Object[] 
                {pageNumber.toString(), pageNumber});
        gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                {zero, zero, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);         
        gen.writeln("FOPFonts begin");
        gen.writeln("0.001 0.001 scale");
        gen.concatMatrix(1, 0, 0, -1, 0, pageheight.doubleValue() * 1000);
        gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);         

    }

    /**
     * Create a new PSDocumentGraphics2D.
     * This is used to create a new PostScript document of the given height
     * and width.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     * @param stream the stream that the final document should be written to.
     * @param width the width of the document
     * @param height the height of the document
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public PSDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws IOException {
        this(textAsShapes);
        setupDocument(stream, width, height);
    }

    /**
     * Get the font info for this PostScript document.
     * @return the font information
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    /**
     * Set the dimensions of the SVG document that will be drawn.
     * This is useful if the dimensions of the SVG document are different
     * from the PostScript document that is to be created.
     * The result is scaled so that the SVG fits correctly inside the
     * PostScript document.
     * @param w the width of the page
     * @param h the height of the page
     * @throws IOException in case of an I/O problem
     */
    public void setSVGDimension(float w, float h) throws IOException {
        gen.concatMatrix(width / w, 0, 0, height / h, 0, 0);
    }

    /**
     * Set the background of the PostScript document.
     * This is used to set the background for the PostScript document
     * Rather than leaving it as the default white.
     * @param col the background colour to fill
     */
    public void setBackgroundColor(Color col) {
        /**@todo Implement this */
        /*
        Color c = col;
        PDFColor currentColour = new PDFColor(c.getRed(), c.getGreen(), c.getBlue());
        currentStream.write("q\n");
        currentStream.write(currentColour.getColorSpaceOut(true));

        currentStream.write("0 0 " + width + " " + height + " re\n");

        currentStream.write("f\n");
        currentStream.write("Q\n");
        */
    }

    /**
     * The rendering process has finished.
     * This should be called after the rendering has completed as there is
     * no other indication it is complete.
     * This will then write the results to the output stream.
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public void finish() throws IOException {
        //Finish page
        gen.writeln("showpage");        
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeDSCComment(DSCConstants.END_PAGE);

        //Finish document
        gen.writeDSCComment(DSCConstants.TRAILER);
        gen.writeDSCComment(DSCConstants.EOF);
        gen.flush();
    }

    /**
     * This constructor supports the create method
     * @param g the PostScript document graphics to make a copy of
     */
    public PSDocumentGraphics2D(PSDocumentGraphics2D g) {
        super(g);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    public Graphics create() {
        return new PSDocumentGraphics2D(this);
    }

    /**
     * Draw a string to the PostScript document.
     * This either draws the string directly or if drawing text as
     * shapes it converts the string into shapes and draws that.
     * @param s the string to draw
     * @param x the x position
     * @param y the y position
     */
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

