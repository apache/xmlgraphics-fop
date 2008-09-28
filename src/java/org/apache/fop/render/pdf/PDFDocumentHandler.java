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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.pdf.PDFAction;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.intermediate.extensions.NamedDestination;

/**
 * {@code IFDocumentHandler} implementation that produces PDF.
 */
public class PDFDocumentHandler extends AbstractBinaryWritingIFDocumentHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFDocumentHandler.class);

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
    protected Map pageReferences = new java.util.HashMap();

    /**
     * Default constructor.
     */
    public PDFDocumentHandler() {
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

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new PDFRendererConfigurator(getUserAgent());
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
    public IFPainter startPageContent() throws IFException {
        return new PDFPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        //nop
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
            log.debug("Don't know how to handle extension object. Ignoring: "
                    + extension + " (" + extension.getClass().getName() + ")");
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
