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
package org.apache.fop.svg;

import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.render.pdf.FontSetup;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.control.Document;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.io.OutputStream;
import java.io.IOException;
import java.util.List;

/**
 * This class is a wrapper for the <tt>PDFGraphics2D</tt> that
 * is used to create a full document around the pdf rendering from
 * <tt>PDFGraphics2D</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id: PDFDocumentGraphics2D.java,v 1.27 2003/03/07 09:51:26 jeremias Exp $
 * @see org.apache.fop.svg.PDFGraphics2D
 */
public class PDFDocumentGraphics2D extends PDFGraphics2D
            implements LogEnabled, Configurable, Initializable {

    private PDFPage currentPage;
    private PDFStream pdfStream;
    private int width;
    private int height;
    private List fontList;

    //Avalon-dependent stuff
    private Logger logger;
    private Configuration cfg;

    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document, the height,
     * width and output stream can be setup later.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     */
    public PDFDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);

        if (!textAsShapes) {
            fontInfo = new Document(null);
            FontSetup.setup(fontInfo, null);
            //FontState fontState = new FontState("Helvetica", "normal",
            //                          FontInfo.NORMAL, 12, 0);
        }
        try {
            initialize();
        } catch (Exception e) {
            //Should never happen
            throw new CascadingRuntimeException("Internal error", e);
        }
    }

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
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public PDFDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws IOException {
        this(textAsShapes);
        setupDocument(stream, width, height);
    }

    /**
     * Create a new PDFDocumentGraphics2D.
     * This is used to create a new pdf document.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     * This constructor is Avalon-style.
     */
    public PDFDocumentGraphics2D() {
        super(false);
    }

    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(Logger)
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the logger.
     * @return Logger the logger
     */
    protected final Logger getLogger() {
        if (this.logger == null) {
            this.logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        }
        return this.logger;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        this.fontList = FontSetup.buildFontListFromConfiguration(cfg);
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if (this.fontInfo == null) {
            fontInfo = new Document(null);
            FontSetup.setup(fontInfo, this.fontList);
            //FontState fontState = new FontState("Helvetica", "normal",
            //                          FontInfo.NORMAL, 12, 0);
        }

        this.pdfDoc = new PDFDocument("Apache FOP: SVG to PDF Transcoder");
        ContainerUtil.enableLogging(this.pdfDoc, getLogger().getChildLogger("pdf"));
        if (this.cfg != null) {
            this.pdfDoc.setFilterMap(
                PDFFilterList.buildFilterMapFromConfiguration(cfg));
        }

        graphicsState = new PDFState();

        currentFontName = "";
        currentFontSize = 0;

        pdfStream = this.pdfDoc.getFactory().makeStream(PDFFilterList.CONTENT_FILTER, false);
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

        PDFResources pdfResources = this.pdfDoc.getResources();
        currentPage = this.pdfDoc.getFactory().makePage(pdfResources,
                                                   width, height);
        resourceContext = currentPage;
        pageRef = currentPage.referencePDF();
        currentStream.write("1 0 0 -1 0 " + height + " cm\n");
        graphicsState.setTransform(new AffineTransform(1.0, 0.0, 0.0, -1.0, 0.0, (double)height));
        pdfDoc.outputHeader(stream);

        setOutputStream(stream);
    }

    /**
     * Get the font info for this pdf document.
     * @return the font information
     */
    public Document getFontInfo() {
        return fontInfo;
    }

    /**
     * Get the pdf document created by this class.
     * @return the pdf document
     */
    public PDFDocument getPDFDocument() {
        return this.pdfDoc;
    }

    /**
     * Set the dimensions of the svg document that will be drawn.
     * This is useful if the dimensions of the svg document are different
     * from the pdf document that is to be created.
     * The result is scaled so that the svg fits correctly inside the
     * pdf document.
     * @param w the width of the page
     * @param h the height of the page
     */
    public void setSVGDimension(float w, float h) {
        currentStream.write("" + PDFNumber.doubleOut(width / w) + " 0 0 "
                            + PDFNumber.doubleOut(height / h) + " 0 0 cm\n");
    }

    /**
     * Set the background of the pdf document.
     * This is used to set the background for the pdf document
     * Rather than leaving it as the default white.
     * @param col the background colour to fill
     */
    public void setBackgroundColor(Color col) {
        Color c = col;
        PDFColor currentColour = new PDFColor(c.getRed(), c.getGreen(), c.getBlue());
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
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public void finish() throws IOException {
        // restorePDFState();

        pdfStream.add(getString());
        this.pdfDoc.registerObject(pdfStream);
        currentPage.setContents(pdfStream);
        PDFAnnotList annots = currentPage.getAnnotations();
        if (annots != null) {
            this.pdfDoc.addObject(annots);
        }
        this.pdfDoc.addObject(currentPage);
        if (fontInfo != null) {
            FontSetup.addToResources(pdfDoc, pdfDoc.getResources(), fontInfo);
        }
        this.pdfDoc.output(outputStream);
        pdfDoc.outputTrailer(outputStream);

        outputStream.flush();
    }

    /**
     * This constructor supports the create method
     * @param g the pdf document graphics to make a copy of
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

    /**
     * Draw a string to the pdf document.
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

