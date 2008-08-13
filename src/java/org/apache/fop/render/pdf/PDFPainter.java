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
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.pdf.PDFAction;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.intermediate.extensions.NamedDestination;
import org.apache.fop.util.CharUtilities;

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

    /** The current content generator */
    protected PDFContentGenerator generator;

    /** the current annotation list to add annotations to */
    protected PDFResourceContext currentContext;

    /** the current page to add annotations to */
    protected PDFPage currentPage;

    /** the current page's PDF reference string (to avoid numerous function calls) */
    protected String currentPageRef;

    /** Used for bookmarks/outlines. */
    private Map pageReferences = new java.util.HashMap();

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
    public void endDocumentHeader() throws IFException {
        pdfUtil.generateDefaultXMPMetadata();
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
            pdfDoc.outputTrailer(this.outputStream);

            this.pdfDoc = null;

            pdfResources = null;
            this.generator = null;
            currentContext = null;
            currentPage = null;
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
        this.pageReferences.put(new Integer(index), new PageReference(currentPage, size));

        this.generator = new PDFContentGenerator(this.pdfDoc, this.outputStream, this.currentPage);
        // Transform the PDF's default coordinate system (0,0 at lower left) to the PDFPainter's
        AffineTransform basicPageTransform = new AffineTransform(1, 0, 0, -1, 0,
                size.height / 1000f);
        generator.concatenate(basicPageTransform);
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
    public void endPage() throws IFException {
        try {
            this.pdfDoc.registerObject(generator.getStream());
            currentPage.setContents(generator.getStream());
            PDFAnnotList annots = currentPage.getAnnotations();
            if (annots != null) {
                this.pdfDoc.addObject(annots);
            }
            this.pdfDoc.addObject(currentPage);
            this.generator.flushPDFDoc();
            this.generator = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        generator.saveGraphicsState();
        generator.concatenate(generator.toPoints(transform));
        if (clipRect != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(format(clipRect.x)).append(' ');
            sb.append(format(clipRect.y)).append(' ');
            sb.append(format(clipRect.width)).append(' ');
            sb.append(format(clipRect.height)).append(" re W n\n");
            generator.add(sb.toString());
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        generator.saveGraphicsState();
        generator.concatenate(generator.toPoints(transform));
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect, Map foreignAttributes) throws IFException {
        PDFXObject xobject = pdfDoc.getXObject(uri);
        if (xobject != null) {
            placeImage(rect, xobject);
            return;
        }

        drawImageUsingURI(uri, rect);

        flushPDFDoc();
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        PDFRenderingContext pdfContext = new PDFRenderingContext(
                getUserAgent(), generator, currentPage, getFontInfo());
        return pdfContext;
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj the image XObject
     */
    private void placeImage(Rectangle rect, PDFXObject xobj) {
        generator.saveGraphicsState();
        generator.add(format(rect.width) + " 0 0 "
                          + format(-rect.height) + " "
                          + format(rect.x) + " "
                          + format(rect.y + rect.height )
                          + " cm " + xobj.getName() + " Do\n");
        generator.restoreGraphicsState();
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect, Map foreignAttributes) throws IFException {
        drawImageUsingDocument(doc, rect);

        flushPDFDoc();
    }

    private void flushPDFDoc() throws IFException {
        // output new data
        try {
            generator.flushPDFDoc();
        } catch (IOException ioe) {
            throw new IFException("I/O error flushing the PDF document", ioe);
        }
    }

    /**
     * Formats a integer value (normally coordinates in millipoints) to a String.
     * @param value the value (in millipoints)
     * @return the formatted value
     */
    protected static String format(int value) {
        return PDFNumber.doubleOut(value / 1000f);
    }

    /** {@inheritDoc} */
    public void drawRect(Rectangle rect, Paint fill, Color stroke) throws IFException {
        if (fill == null && stroke == null) {
            return;
        }
        generator.endTextObject();
        if (rect.width != 0 && rect.height != 0) {
            if (fill != null) {
                if (fill instanceof Color) {
                    generator.updateColor((Color)fill, true, null);
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
            generator.add(sb.toString());
        }
    }

    private Typeface getTypeface(String fontName) {
        if (fontName == null) {
            throw new NullPointerException("fontName must not be null");
        }
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        //Note: dy is currently ignored
        generator.beginTextObject();
        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        String fontKey = fontInfo.getInternalFontKey(triplet);
        int sizeMillipoints = state.getFontSize();
        float fontSize = sizeMillipoints / 1000f;
        generator.updateColor(state.getTextColor(), true, null);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontKey);
        SingleByteFont singleByteFont = null;
        if (tf instanceof SingleByteFont) {
            singleByteFont = (SingleByteFont)tf;
        }
        Font font = fontInfo.getFontInstance(triplet, sizeMillipoints);
        String fontName = font.getFontName();

        PDFTextUtil textutil = generator.getTextUtil();
        textutil.updateTf(fontKey, fontSize, tf.isMultiByte());

        textutil.writeTextMatrix(new AffineTransform(1, 0, 0, -1, x / 1000f, y / 1000f));
        int l = text.length();
        int dxl = (dx != null ? dx.length : 0);

        if (dx != null && dxl > 0 && dx[0] != 0) {
            textutil.adjustGlyphTJ(dx[0]);
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

            if (dx != null && i < dxl - 1) {
                glyphAdjust += dx[i + 1];
            }

            if (glyphAdjust != 0) {
                textutil.adjustGlyphTJ(-glyphAdjust / 10f);
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

    private void renderBookmarkTree(BookmarkTree tree) {
        Iterator iter = tree.getBookmarks().iterator();
        while (iter.hasNext()) {
            Bookmark b = (Bookmark)iter.next();
            renderBookmark(b, null);
        }
    }

    private void renderBookmark(Bookmark bookmark, PDFOutline parent) {
        if (parent == null) {
            parent = pdfDoc.getOutlineRoot();
        }
        PDFAction action = getAction(bookmark.getAction());
        PDFOutline pdfOutline = pdfDoc.getFactory().makeOutline(parent,
            bookmark.getTitle(), action, bookmark.isShown());
        Iterator iter = bookmark.getChildBookmarks().iterator();
        while (iter.hasNext()) {
            Bookmark b = (Bookmark)iter.next();
            renderBookmark(b, pdfOutline);
        }
    }

    private void renderNamedDestination(NamedDestination destination) {
        PDFAction action = getAction(destination.getAction());
        pdfDoc.getFactory().makeDestination(
                destination.getName(), action.makeReference());
    }

    private PDFAction getAction(AbstractAction action) {
        if (action instanceof GoToXYAction) {
            GoToXYAction a = (GoToXYAction)action;
            PageReference pageRef = (PageReference)this.pageReferences.get(
                    new Integer(a.getPageIndex()));
            //Convert target location from millipoints to points and adjust for different
            //page origin
            Point2D p2d = new Point2D.Double(
                    a.getTargetLocation().x / 1000.0,
                    (pageRef.pageDimension.height - a.getTargetLocation().y) / 1000.0);
            return pdfDoc.getFactory().getPDFGoTo(pageRef.pageRef.toString(), p2d);
        } else {
            throw new UnsupportedOperationException("Unsupported action type: "
                    + action + " (" + action.getClass().getName() + ")");
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof XMPMetadata) {
            pdfUtil.renderXMPMetadata((XMPMetadata)extension);
        } else if (extension instanceof Metadata) {
            XMPMetadata wrapper = new XMPMetadata(((Metadata)extension));
            pdfUtil.renderXMPMetadata(wrapper);
        } else if (extension instanceof BookmarkTree) {
            renderBookmarkTree((BookmarkTree)extension);
        } else if (extension instanceof NamedDestination) {
            renderNamedDestination((NamedDestination)extension);
        } else {
            log.warn("Don't know how to handle extension object: "
                    + extension + " (" + extension.getClass().getName());
        }
    }

    private static final class PageReference {

        private PDFReference pageRef;
        private Dimension pageDimension;

        private PageReference(PDFPage page, Dimension dim) {
            this.pageRef = page.makeReference();
            this.pageDimension = new Dimension(dim);
        }
    }

}
