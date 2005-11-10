/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontInfo;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.io.OutputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This class is a wrapper for the <tt>PDFGraphics2D</tt> that
 * is used to create a full document around the pdf rendering from
 * <tt>PDFGraphics2D</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 * @see org.apache.fop.svg.PDFGraphics2D
 */
public class PDFDocumentGraphics2D extends PDFGraphics2D
            implements Configurable, Initializable {

    private PDFContext pdfContext;

    private int width;
    private int height;
    
    //for SVG scaling
    private float svgWidth;
    private float svgHeight;

    /** Normal PDF resolution (72dpi) */
    public static final int NORMAL_PDF_RESOLUTION = 72;
    /** Default device resolution (300dpi is a resonable quality for most purposes) */
    public static final int DEFAULT_NATIVE_DPI = 300;
  
    /**
     * The device resolution may be different from the normal target resolution. See
     * http://issues.apache.org/bugzilla/show_bug.cgi?id=37305
     */
    private float deviceDPI = DEFAULT_NATIVE_DPI;

    /** Initial clipping area, used to restore to original setting
     * when a new page is started. */
    protected Shape initialClip;

    /**
     * Initial transformation matrix, used to restore to original
     * setting when a new page is started.
     */
    protected AffineTransform initialTransform;

    //Avalon component
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

        this.pdfContext = new PDFContext();
        if (!textAsShapes) {
            fontInfo = new FontInfo();
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
        this(false);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        this.pdfContext.setFontList(FontSetup.buildFontListFromConfiguration(cfg));
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if (this.fontInfo == null) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo, this.pdfContext.getFontList());
            //FontState fontState = new FontState("Helvetica", "normal",
            //                          FontInfo.NORMAL, 12, 0);
        }

        this.pdfDoc = new PDFDocument("Apache FOP: SVG to PDF Transcoder");

        if (this.cfg != null) {
            this.pdfDoc.setFilterMap(
                PDFFilterList.buildFilterMapFromConfiguration(cfg));
        }
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

        pdfDoc.outputHeader(stream);
        setOutputStream(stream);
    }

    /**
     * Set the device resolution for rendering.  Will take effect at the
     * start of the next page.
     * @param deviceDPI the device resolution (in dpi)
     */
    public void setDeviceDPI(float deviceDPI) {
        this.deviceDPI = deviceDPI;
    }

    /**
     * @return the device resolution (in dpi) for rendering.
     */
    public float getDeviceDPI() {
        return deviceDPI;
    }

    /**
     * Get the font info for this pdf document.
     * @return the font information
     */
    public FontInfo getFontInfo() {
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
        this.svgWidth = w;
        this.svgHeight = h;
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
     * Is called to prepare the PDFDocumentGraphics2D for the next page to be painted. Basically,
     * this closes the current page. A new page is prepared as soon as painting starts.
     */
    public void nextPage() {
        closePage();
    }
    
    /**
     * Closes the current page and adds it to the PDF file. 
     */
    protected void closePage() {
        if (!pdfContext.isPagePending()) {
            return; //ignore
        }
        //Finish page
        PDFStream pdfStream = this.pdfDoc.getFactory().makeStream(
                PDFFilterList.CONTENT_FILTER, false);
        pdfStream.add(getString());
        currentStream = null;
        this.pdfDoc.registerObject(pdfStream);
        pdfContext.getCurrentPage().setContents(pdfStream);
        PDFAnnotList annots = pdfContext.getCurrentPage().getAnnotations();
        if (annots != null) {
            this.pdfDoc.addObject(annots);
        }
        this.pdfDoc.addObject(pdfContext.getCurrentPage());
        pdfContext.clearCurrentPage();
    }
    
    /** {@inheritDoc} */
    protected void preparePainting() {
        if (pdfContext.isPagePending()) {
            return;
        }
        try {
            startPage();
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    /**
     * Called to prepare a new page
     * @throws IOException if starting the new page fails due to I/O errors.
     */
    protected void startPage() throws IOException {
        if (pdfContext.isPagePending()) {
            throw new IllegalStateException("Close page first before starting another");
        }
        //Start page
        graphicsState = new PDFState();
        if (this.initialTransform == null) {
            //Save initial transformation matrix
            this.initialTransform = getTransform();
            this.initialClip = getClip();      
        } else {
            //Reset transformation matrix
            setTransform(this.initialTransform);
            setClip(this.initialClip);
        }

        currentFontName = "";
        currentFontSize = 0;

        if (currentStream == null) {
            currentStream = new StringWriter();
        }
        
        PDFResources pdfResources = this.pdfDoc.getResources();
        PDFPage page = this.pdfDoc.getFactory().makePage(pdfResources,
                width, height);
        resourceContext = page;
        pdfContext.setCurrentPage(page);
        pageRef = page.referencePDF();

        AffineTransform at = new AffineTransform(1.0, 0.0, 0.0, -1.0, 
                                                 0.0, (double)height);
        currentStream.write("1 0 0 -1 0 " + height + " cm\n");
        if (svgWidth != 0) {
            double scaleX = width / svgWidth;
            double scaleY = height / svgHeight;
            at.scale(scaleX, scaleY);
            currentStream.write("" + PDFNumber.doubleOut(scaleX) + " 0 0 "
                                + PDFNumber.doubleOut(scaleY) + " 0 0 cm\n");
        }
        if (deviceDPI != NORMAL_PDF_RESOLUTION) {
            double s = NORMAL_PDF_RESOLUTION / deviceDPI;
            at.scale(s, s);
            currentStream.write("" + PDFNumber.doubleOut(s) + " 0 0 "
                                + PDFNumber.doubleOut(s) + " 0 0 cm\n");
            
            scale(1 / s, 1 / s);
        }
        // Remember the transform we installed.
        graphicsState.setTransform(at);

        pdfContext.increasePageCount();
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

        closePage();
        if (fontInfo != null) {
            pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
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
        this.pdfContext = g.pdfContext;
        this.cfg = g.cfg;
        this.width = g.width;
        this.height = g.height;
        this.svgWidth = g.svgWidth;
        this.svgHeight = g.svgHeight;
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

