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

/**
 * Renderer that renders areas to PDF
 *
 */
public class PDFRenderer extends PrintRenderer {

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

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
     * The  width of the previous word. Used to calculate space between
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
        this.pdfDoc = new PDFDocument();
    }

    /**
     * set the PDF document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String producer) {
        this.pdfDoc.setProducer(producer);
    }

    public void startRenderer(OutputStream stream)
    throws IOException {
        pdfDoc.outputHeader(stream);
    }

    public void stopRenderer()
    throws IOException {
    }

    public void renderPage(PageViewport page) throws IOException, FOPException {

        Page p = page.getPage();
renderPageAreas(p);

    }


}
