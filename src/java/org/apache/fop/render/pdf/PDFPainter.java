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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.util.CharUtilities;
import org.apache.fop.util.ColorUtil;

/**
 * IFPainter implementation that produces PDF.
 */
public class PDFPainter extends AbstractBinaryWritingIFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFPainter.class);

    /** Holds the intermediate format state */
    protected IFState state;

    /** the PDF Document being created */
    protected PDFDocument pdfDoc;

    /**
     * Utility class which enables all sorts of features that are not directly connected to the
     * normal rendering process.
     */
    protected PDFRenderingUtil pdfUtil;

    /** the /Resources object of the PDF document being created */
    protected PDFResources pdfResources;

    /** the current stream to add PDF commands to */
    protected PDFStream currentStream;

    /** the current annotation list to add annotations to */
    protected PDFResourceContext currentContext;

    /**
     * Map of pages using the PageViewport as the key
     * this is used for prepared pages that cannot be immediately
     * rendered
     */
    protected Map pages;

    /** the current page to add annotations to */
    protected PDFPage currentPage;

    /** the current page's PDF reference string (to avoid numerous function calls) */
    protected String currentPageRef;

    /** drawing state */
    protected PDFState currentState;

    /** Text generation utility holding the current font status */
    protected PDFTextUtil textutil;


    /** Image handler registry */
    private PDFImageHandlerRegistry imageHandlerRegistry = new PDFImageHandlerRegistry();

    /**
     * Default constructor.
     */
    public PDFPainter() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return true;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PDF;
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        super.setUserAgent(ua);
        this.pdfUtil = new PDFRenderingUtil(ua);
    }

    PDFRenderingUtil getPDFUtil() {
        return this.pdfUtil;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PDF generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }
            this.pdfDoc = pdfUtil.setupPDFDocument(this.outputStream);
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        pdfUtil.generateDefaultXMPMetadata();
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            //finishOpenGoTos();

            pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
            pdfDoc.outputTrailer(this.outputStream);

            this.pdfDoc = null;

            this.pages = null;

            //pageReferences.clear();
            pdfResources = null;
            currentStream = null;
            currentContext = null;
            currentPage = null;
            currentState = null;
            this.textutil = null;

            //idPositions.clear();
            //idGoTos.clear();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //TODO page sequence title, country and language
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, Dimension size) throws IFException {
        this.pdfResources = this.pdfDoc.getResources();

        this.currentPage = this.pdfDoc.getFactory().makePage(
            this.pdfResources,
            (int)Math.round(size.getWidth() / 1000),
            (int)Math.round(size.getHeight() / 1000),
            index);
        //pageReferences.put(new Integer(index)/*page.getKey()*/, currentPage.referencePDF());
        //pvReferences.put(page.getKey(), page);

        pdfUtil.generatePageLabel(index, name);

        currentPageRef = currentPage.referencePDF();

        currentStream = this.pdfDoc.getFactory()
            .makeStream(PDFFilterList.CONTENT_FILTER, false);
        this.textutil = new PDFTextUtil() {
            protected void write(String code) {
                currentStream.add(code);
            }
        };

        currentState = new PDFState();
        // Transform the PDF's default coordinate system (0,0 at lower left) to the PDFPainter's
        AffineTransform basicPageTransform = new AffineTransform(1, 0, 0, -1, 0,
                size.height);
        currentState.concatenate(basicPageTransform);
        currentStream.add(CTMHelper.toPDFString(basicPageTransform, true) + " cm\n");
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void startPageContent() throws IFException {
        this.state = IFState.create();
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        assert this.state.pop() == null;
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            this.pdfDoc.registerObject(currentStream);
            currentPage.setContents(currentStream);
            PDFAnnotList annots = currentPage.getAnnotations();
            if (annots != null) {
                this.pdfDoc.addObject(annots);
            }
            this.pdfDoc.addObject(currentPage);
            this.pdfDoc.output(this.outputStream);
            this.textutil = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    private void saveGraphicsState() {
        //endTextObject();
        currentState.push();
        this.state = this.state.push();
        currentStream.add("q\n");
    }

    private void restoreGraphicsState(boolean popState) {
        endTextObject();
        currentStream.add("Q\n");
        if (popState) {
            currentState.pop();
            this.state = this.state.pop();
        }
    }

    private void restoreGraphicsState() {
        restoreGraphicsState(true);
    }

    /** {@inheritDoc} */
    public void startBox(AffineTransform transform, Dimension size, boolean clip)
            throws IFException {
        saveGraphicsState();
        currentStream.add(CTMHelper.toPDFString(transform, true) + " cm\n");
    }

    /** {@inheritDoc} */
    public void startBox(AffineTransform[] transforms, Dimension size, boolean clip)
            throws IFException {
        AffineTransform at = new AffineTransform();
        for (int i = 0, c = transforms.length; i < c; i++) {
            at.concatenate(transforms[i]);
        }
        startBox(at, size, clip);
    }

    /** {@inheritDoc} */
    public void endBox() throws IFException {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void startImage(Rectangle rect) throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void endImage() throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void addTarget(String name, int x, int y) throws IFException {
        // TODO Auto-generated method stub

    }

    private static String toString(Paint paint) {
        if (paint instanceof Color) {
            return ColorUtil.colorToString((Color)paint);
        } else {
            throw new UnsupportedOperationException("Paint not supported: " + paint);
        }
    }

    /**
     * Formats a int value (normally coordinates in millipoints) as Strings.
     * @param value the value (in millipoints)
     * @return the formatted value
     */
    protected static String format(int value) {
        return PDFNumber.doubleOut(value / 1000f);
    }

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     */
    private void updateColor(Color col, boolean fill) {
        if (col == null) {
            return;
        }
        boolean update = false;
        if (fill) {
            update = currentState.setBackColor(col);
        } else {
            update = currentState.setColor(col);
        }

        if (update) {
            pdfUtil.setColor(col, fill, this.currentStream);
        }
    }

    /** {@inheritDoc} */
    public void drawRect(Rectangle rect, Paint fill, Color stroke) throws IFException {
        if (fill == null && stroke == null) {
            return;
        }
        endTextObject();
        if (rect.width != 0 && rect.height != 0) {
            if (fill != null) {
                if (fill instanceof Color) {
                    updateColor((Color)fill, true);
                } else {
                    throw new UnsupportedOperationException("Non-Color paints NYI");
                }
            }
            if (stroke != null) {
                throw new UnsupportedOperationException("stroke NYI");
            }
            StringBuffer sb = new StringBuffer();
            sb.append(format(rect.x)).append(' ');
            sb.append(format(rect.y)).append(' ');
            sb.append(format(rect.width)).append(' ');
            sb.append(format(rect.height)).append(" re");
            if (fill != null) {
                sb.append(" f");
            }
            if (stroke != null) {
                sb.append(" S");
            }
            sb.append('\n');
            currentStream.add(sb.toString());
        }
    }

    /** Indicates the beginning of a text object. */
    private void beginTextObject() {
        if (!textutil.isInTextObject()) {
            textutil.beginTextObject();
        }
    }

    /** Indicates the end of a text object. */
    private void endTextObject() {
        if (textutil.isInTextObject()) {
            textutil.endTextObject();
        }
    }

    private Typeface getTypeface(String fontName) {
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        //Note: dy is currently ignored
        beginTextObject();
        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        String fontKey = fontInfo.getInternalFontKey(triplet);
        int sizeMillipoints = state.getFontSize();
        float fontSize = sizeMillipoints / 1000f;
        updateColor(state.getTextColor(), true);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontKey);
        SingleByteFont singleByteFont = null;
        if (tf instanceof SingleByteFont) {
            singleByteFont = (SingleByteFont)tf;
        }
        Font font = fontInfo.getFontInstance(triplet, sizeMillipoints);
        String fontName = font.getFontName();

        textutil.updateTf(fontKey, fontSize, tf.isMultiByte());

        textutil.writeTextMatrix(new AffineTransform(1, 0, 0, -1, x / 1000f, y / 1000f));
        int l = text.length();
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            textutil.adjustGlyphTJ(dx[0] / fontSize);
        }
        for (int i = 0; i < l; i++) {
            char orgChar = text.charAt(i);
            char ch;
            float glyphAdjust = 0;
            if (font.hasChar(orgChar)) {
                ch = font.mapChar(orgChar);
                if (singleByteFont != null && singleByteFont.hasAdditionalEncodings()) {
                    int encoding = ch / 256;
                    if (encoding == 0) {
                        textutil.updateTf(fontName, fontSize, tf.isMultiByte());
                    } else {
                        textutil.updateTf(fontName + "_" + Integer.toString(encoding),
                                fontSize, tf.isMultiByte());
                        ch = (char)(ch % 256);
                    }
                }
                //int tls = (i < l - 1 ? parentArea.getTextLetterSpaceAdjust() : 0);
                //glyphAdjust -= tls;
            } else {
                if (CharUtilities.isFixedWidthSpace(orgChar)) {
                    //Fixed width space are rendered as spaces so copy/paste works in a reader
                    ch = font.mapChar(CharUtilities.SPACE);
                    glyphAdjust = font.getCharWidth(ch) - font.getCharWidth(orgChar);
                } else {
                    ch = font.mapChar(orgChar);
                }
            }
            textutil.writeTJMappedChar(ch);

            if (dx != null && i < dxl) {
                glyphAdjust += dx[i + 1];
            }

            if (glyphAdjust != 0) {
                textutil.adjustGlyphTJ(glyphAdjust / fontSize);
            }

        }
        textutil.writeTJ();
    }

    /** {@inheritDoc} */
    public void setFont(String family, String style, Integer weight, String variant, Integer size,
            Color color) throws IFException {
        if (family != null) {
            state.setFontFamily(family);
        }
        if (style != null) {
            state.setFontStyle(style);
        }
        if (weight != null) {
            state.setFontWeight(weight.intValue());
        }
        if (variant != null) {
            state.setFontVariant(variant);
        }
        if (size != null) {
            state.setFontSize(size.intValue());
        }
        if (color != null) {
            state.setTextColor(color);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof XMPMetadata) {
            pdfUtil.renderXMPMetadata((XMPMetadata)extension);
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to handle extension object: " + extension);
        }
    }

}
