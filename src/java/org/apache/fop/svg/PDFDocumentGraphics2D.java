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

package org.apache.fop.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.xmlgraphics.image.GraphicsConstants;

import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFColorHandler;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFPaintingState;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStream;

/**
 * This class is a wrapper for the {@link PDFGraphics2D} that
 * is used to create a full document around the PDF rendering from
 * {@link PDFGraphics2D}.
 *
 * @see org.apache.fop.svg.PDFGraphics2D
 */
public class PDFDocumentGraphics2D extends PDFGraphics2D {

    private final PDFContext pdfContext;

    private int width;
    private int height;

    //for SVG scaling
    private float svgWidth;
    private float svgHeight;

    /** Normal PDF resolution (72dpi) */
    public static final int NORMAL_PDF_RESOLUTION = 72;
    /** Default device resolution (300dpi is a resonable quality for most purposes) */
    public static final int DEFAULT_NATIVE_DPI = GraphicsConstants.DEFAULT_SAMPLE_DPI;

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

        this.pdfDoc = new PDFDocument("Apache FOP Version " + Version.getVersion()
                + ": PDFDocumentGraphics2D");
        this.pdfContext = new PDFContext();
        this.colorHandler = new PDFColorHandler(this.pdfDoc.getResources());
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
     * Setup a default FontInfo instance if none has been setup before.
     */
    public void setupDefaultFontInfo() {
        if (fontInfo == null) {
            //Default minimal fonts
            FontInfo fontInfo = new FontInfo();
            FontSetup.setup(fontInfo);
            setFontInfo(fontInfo);
        }
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
     * Sets the font info for this PDF document.
     * @param fontInfo the font info object with all the fonts
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
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
     * Return the PDFContext for this instance.
     * @return the PDFContext
     */
    public PDFContext getPDFContext() {
        return this.pdfContext;
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
        StringBuffer sb = new StringBuffer();
        sb.append("q\n");
        this.colorHandler.establishColor(sb, col, true);

        sb.append("0 0 ").append(width).append(" ").append(height).append(" re\n");

        sb.append("f\n");
        sb.append("Q\n");
        currentStream.write(sb.toString());
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
        currentStream.write("Q\n");
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
        //Setup default font info if no more font configuration has been done by the user.
        if (!this.textAsShapes && getFontInfo() == null) {
            setupDefaultFontInfo();
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
        paintingState = new PDFPaintingState();
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

        currentStream.write("q\n");
        AffineTransform at = new AffineTransform(1.0, 0.0, 0.0, -1.0,
                                                 0.0, height);
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
        paintingState.concatenate(at);

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

