/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.pdf.*;
import org.apache.fop.image.*;
import org.apache.fop.extensions.*;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.render.pdf.fonts.LazyFont;

import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;

import org.w3c.dom.Document;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/*
TODO:

viewport clipping
word rendering and optimistion
pdf state optimisation
line and border
leader
background pattern
user agent xml (svg) rendering
orientation
writing mode
text decoration

 */

/**
 * Renderer that renders areas to PDF
 *
 */
public class PDFRenderer extends PrintRenderer {

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    // map of pages using the PageViewport as the key
    // this is used for prepared pages that cannot be immediately
    // rendered
    protected HashMap pages = null;

    // page references are stored using the PageViewport as the key
    // when a reference is made the PageViewport is used
    // for pdf this means we need the pdf page reference
    protected HashMap pageReferences = new HashMap();

    protected String producer;

    protected OutputStream ostream;

    /**
     * the /Resources object of the PDF document being created
     */
    protected PDFResources pdfResources;

    /**
     * the current stream to add PDF commands to
     */
    PDFStream currentStream;

    /**
     * the current annotation list to add annotations to
     */
    PDFAnnotList currentAnnotList;

    /**
     * the current page to add annotations to
     */
    PDFPage currentPage;

    PDFColor currentColor;

    /**
     * true if a TJ command is left to be written
     */
    boolean textOpen = false;

    /**
     * the previous Y coordinate of the last word written.
     * Used to decide if we can draw the next word on the same line.
     */
    int prevWordY = 0;

    /**
     * the previous X coordinate of the last word written.
     * used to calculate how much space between two words
     */
    int prevWordX = 0;

    /**
     * The width of the previous word. Used to calculate space between
     */
    int prevWordWidth = 0;

    /**
     * reusable word area string buffer to reduce memory usage
     */
    private StringBuffer _wordAreaPDF = new StringBuffer();

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
    }

    /**
     * set the PDF document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String prod) {
        producer = prod;
    }

    public void startRenderer(OutputStream stream) throws IOException {
        ostream = stream;
        this.pdfDoc = new PDFDocument();
        this.pdfDoc.setProducer(producer);
        pdfDoc.outputHeader(stream);
    }

    public void stopRenderer() throws IOException {
        FontSetup.addToResources(this.pdfDoc, fontInfo);
        pdfDoc.outputTrailer(ostream);

        this.pdfDoc = null;
        ostream = null;
    }

    public boolean supportsOutOfOrder() {
        return true;
    }

    /**
     * The pdf page is prepared by making the page.
     * The page is made in the pdf document without any contents
     * and then stored to add the contents later.
     * The page objects is stored using the area tree PageViewport
     * as a key.
     */
    public void preparePage(PageViewport page) {
        this.pdfResources = this.pdfDoc.getResources();
    
        Rectangle2D bounds = page.getViewArea();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        currentPage = this.pdfDoc.makePage(this.pdfResources,
                                           (int) Math.round(w / 1000), (int) Math.round(h / 1000));
        if(pages == null) {
            pages = new HashMap();
        }
        pages.put(page, currentPage);
        pageReferences.put(page, currentPage.referencePDF());
    }

    /**
     * This method creates a pdf stream for the current page
     * uses it as the contents of a new page. The page is written
     * immediately to the output stream.
     */
    public void renderPage(PageViewport page) throws IOException,
    FOPException {
        if(pages != null && (currentPage = (PDFPage)pages.get(page)) != null) {
            pages.remove(page);
        } else {
            this.pdfResources = this.pdfDoc.getResources();
            Rectangle2D bounds = page.getViewArea();
            double w = bounds.getWidth();
            double h = bounds.getHeight();
            currentPage = this.pdfDoc.makePage(this.pdfResources, 
                                           (int) Math.round(w / 1000), (int) Math.round(h / 1000));
            pageReferences.put(page, currentPage.referencePDF());
        }
        currentStream = this.pdfDoc.makeStream();
        currentStream.add("BT\n");

        Page p = page.getPage();
        renderPageAreas(p);

        currentStream.add("ET\n");

        currentPage.setContents(currentStream);
        this.pdfDoc.addPage(currentPage);
        this.pdfDoc.output(ostream);
    }

}
