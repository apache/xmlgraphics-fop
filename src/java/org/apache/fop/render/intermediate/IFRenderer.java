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

package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import org.apache.batik.parser.AWTTransformProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

import org.apache.fop.Version;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.CTM;
import org.apache.fop.area.DestinationData;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.intermediate.extensions.NamedDestination;
import org.apache.fop.render.pdf.PDFEventProducer;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * This renderer implementation is an adapter to the {@code IFPainter} interface. It is used
 * to generate content using FOP's intermediate format.
 */
public class IFRenderer extends AbstractPathOrientedRenderer {

    //TODO Many parts of the Renderer infrastructure are using floats (coordinates in points)
    //instead of ints (in millipoints). A lot of conversion to and from is performed.
    //When the new IF is established, the Renderer infrastructure should be revisited so check
    //if optimizations can be done to avoid int->float->int conversions.

    /** logging instance */
    protected static Log log = LogFactory.getLog(IFRenderer.class);

    /** XML MIME type */
    public static final String IF_MIME_TYPE = MimeConstants.MIME_FOP_IF;

    private IFDocumentHandler documentHandler;
    private IFPainter painter;

    /** If not null, the XMLRenderer will mimic another renderer by using its font setup. */
    protected Renderer mimic;

    private boolean inPageSequence = false;

    private Stack graphicContextStack = new Stack();
    private Stack viewportDimensionStack = new Stack();
    private IFGraphicContext graphicContext = new IFGraphicContext();
    //private Stack groupStack = new Stack();

    private Metadata documentMetadata;

    /**
     * Maps XSL-FO element IDs to their on-page XY-positions
     * Must be used in conjunction with the page reference to fully specify the details
     * of a "go-to" action.
     */
    private Map idPositions = new java.util.HashMap();

    /**
     * Maps XSL-FO element IDs to "go-to" actions targeting the corresponding areas
     * These objects may not all be fully filled in yet
     */
    private Map idGoTos = new java.util.HashMap();

    /**
     * The "go-to" actions in idGoTos that are not complete yet
     */
    private List unfinishedGoTos = new java.util.ArrayList();
    // can't use a Set because PDFGoTo.equals returns true if the target is the same,
    // even if the object number differs

    private BookmarkTree bookmarkTree;

    private TextUtil textUtil = new TextUtil();

    /**
     * Main constructor
     */
    public IFRenderer() {
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return IF_MIME_TYPE;
    }

    /**
     * Sets the {@code IFDocumentHandler} to be used by the {@code IFRenderer}.
     * @param documentHandler the {@code IFDocumentHandler}
     */
    public void setDocumentHandler(IFDocumentHandler documentHandler) {
        this.documentHandler = documentHandler;
    }

    /** {@inheritDoc} */
    public void setupFontInfo(FontInfo inFontInfo) throws FOPException {
        if (this.documentHandler == null) {
            this.documentHandler = createDefaultDocumentHandler();
        }
        IFDocumentHandlerConfigurator configurator = this.documentHandler.getConfigurator();
        if (configurator != null) {
            configurator.setupFontInfo(documentHandler, inFontInfo);
        } else {
            this.documentHandler.setDefaultFontInfo(inFontInfo);
        }
        this.fontInfo = inFontInfo;
    }

    private void handleIFException(IFException ife) {
        if (ife.getCause() instanceof SAXException) {
            throw new RuntimeException(ife.getCause());
        } else {
            throw new RuntimeException(ife);
        }
    }

    private void handleIFExceptionWithIOException(IFException ife) throws IOException {
        if (ife.getCause() instanceof IOException) {
            throw (IOException)ife.getCause();
        } else {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    public boolean supportsOutOfOrder() {
        return (this.documentHandler != null
                ? this.documentHandler.supportsPagesOutOfOrder() : false);
    }

    /**
     * Creates a default {@code IFDocumentHandler} when none has been set.
     * @return the default IFDocumentHandler
     */
    protected IFDocumentHandler createDefaultDocumentHandler() {
        IFSerializer serializer = new IFSerializer();
        serializer.setUserAgent(getUserAgent());
        return serializer;
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        try {
            if (outputStream != null) {
                StreamResult result = new StreamResult(outputStream);
                if (getUserAgent().getOutputFile() != null) {
                    result.setSystemId(
                            getUserAgent().getOutputFile().toURI().toURL().toExternalForm());
                }
                if (this.documentHandler == null) {
                    this.documentHandler = createDefaultDocumentHandler();
                }
                this.documentHandler.setResult(result);
            }
            super.startRenderer(null);
            if (log.isDebugEnabled()) {
                log.debug("Rendering areas via IF document handler ("
                        + this.documentHandler.getClass().getName() + ")...");
            }
            documentHandler.startDocument();
            documentHandler.startDocumentHeader();
        } catch (IFException e) {
            handleIFExceptionWithIOException(e);
        }
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        try {
            if (this.inPageSequence) {
                documentHandler.endPageSequence();
                this.inPageSequence = false;
            }
            documentHandler.startDocumentTrailer();
            finishOpenGoTos();
            if (this.bookmarkTree != null) {
                documentHandler.handleExtensionObject(this.bookmarkTree);
            }
            documentHandler.endDocumentTrailer();
            documentHandler.endDocument();
        } catch (IFException e) {
            handleIFExceptionWithIOException(e);
        }
        idPositions.clear();
        idGoTos.clear();
        super.stopRenderer();
        log.debug("Rendering finished.");
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem odi) {
        if (odi instanceof DestinationData) {
            // render Destinations
            renderDestination((DestinationData) odi);
        } else if (odi instanceof BookmarkData) {
            // render Bookmark-Tree
            renderBookmarkTree((BookmarkData) odi);
        } else if (odi instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)odi).getAttachment();
            if (XMPMetadata.CATEGORY.equals(attachment.getCategory())) {
                renderXMPMetadata((XMPMetadata)attachment);
            }
        }
    }

    private void renderDestination(DestinationData dd) {
        String targetID = dd.getIDRef();
        if (targetID == null || targetID.length() == 0) {
            throw new IllegalArgumentException("DestinationData must contain a ID reference");
        }
        PageViewport pv = dd.getPageViewport();
        if (pv != null) {
            GoToXYAction action = getGoToActionForID(targetID, pv.getPageIndex());
            NamedDestination namedDestination = new NamedDestination(targetID, action);
            try {
                documentHandler.handleExtensionObject(namedDestination);
            } catch (IFException ife) {
                handleIFException(ife);
            }
        } else {
            //Warning already issued by AreaTreeHandler (debug level is sufficient)
            log.debug("Unresolved destination item received: " + dd.getIDRef());
        }
    }

    /**
     * Renders a Bookmark-Tree object
     * @param bookmarks the BookmarkData object containing all the Bookmark-Items
     */
    protected void renderBookmarkTree(BookmarkData bookmarks) {
        assert this.bookmarkTree == null;
        this.bookmarkTree = new BookmarkTree();
        for (int i = 0; i < bookmarks.getCount(); i++) {
            BookmarkData ext = bookmarks.getSubData(i);
            Bookmark b = renderBookmarkItem(ext);
            bookmarkTree.addBookmark(b);
        }
    }

    private Bookmark renderBookmarkItem(BookmarkData bookmarkItem) {

        String targetID = bookmarkItem.getIDRef();
        if (targetID == null || targetID.length() == 0) {
            throw new IllegalArgumentException("DestinationData must contain a ID reference");
        }
        GoToXYAction action = null;
        PageViewport pv = bookmarkItem.getPageViewport();

        if (pv != null) {
            action = getGoToActionForID(targetID, pv.getPageIndex());
        } else {
            //Warning already issued by AreaTreeHandler (debug level is sufficient)
            log.debug("Bookmark with IDRef \"" + targetID + "\" has a null PageViewport.");
        }

        Bookmark b = new Bookmark(
                bookmarkItem.getBookmarkTitle(),
                bookmarkItem.showChildItems(),
                action);
        for (int i = 0; i < bookmarkItem.getCount(); i++) {
            b.addChildBookmark(renderBookmarkItem(bookmarkItem.getSubData(i)));
        }
        return b;
    }

    private void renderXMPMetadata(XMPMetadata metadata) {
        this.documentMetadata = metadata.getMetadata();
    }

    private GoToXYAction getGoToActionForID(String targetID, int pageIndex) {
        // Already a PDFGoTo present for this target? If not, create.
        GoToXYAction action = (GoToXYAction)idGoTos.get(targetID);
        if (action == null) {
            //String pdfPageRef = (String) pageReferences.get(pvKey);
            Point position = (Point)idPositions.get(targetID);
            // can the GoTo already be fully filled in?
            if (/*pdfPageRef != null &&*/ position != null) {
                // getPDFGoTo shares PDFGoTo objects as much as possible.
                // It also takes care of assignObjectNumber and addTrailerObject.
                //action = pdfDoc.getFactory().getPDFGoTo(pdfPageRef, position);
                action = new GoToXYAction(pageIndex, position);
            } else {
                // Not complete yet, can't use getPDFGoTo:
                action = new GoToXYAction(pageIndex, null);
                unfinishedGoTos.add(action);
            }
            idGoTos.put(targetID, action);
        }
        return action;
    }

    private void finishOpenGoTos() {
        int count = unfinishedGoTos.size();
        if (count > 0) {
            Point defaultPos = new Point(0, 0);  // top-o-page
            while (!unfinishedGoTos.isEmpty()) {
                GoToXYAction action = (GoToXYAction)unfinishedGoTos.get(0);
                noteGoToPosition(action, defaultPos);
            }
            PDFEventProducer eventProducer = PDFEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.nonFullyResolvedLinkTargets(this, count);
            // dysfunctional if pageref is null
        }
    }

    private void noteGoToPosition(GoToXYAction action, Point position) {
        action.setTargetLocation(position);
        unfinishedGoTos.remove(action);
    }

    private void noteGoToPosition(GoToXYAction action, PageViewport pv, Point position) {
        noteGoToPosition(action, position);
    }

    private void saveAbsolutePosition(String id, PageViewport pv,
            int relativeIPP, int relativeBPP, AffineTransform tf) {
        Point position = new Point(relativeIPP, relativeBPP);
        tf.transform(position, position);
        idPositions.put(id, position);
        // is there already a PDFGoTo waiting to be completed?
        GoToXYAction action = (GoToXYAction)idGoTos.get(id);
        if (action != null) {
            noteGoToPosition(action, pv, position);
        }
    }

    private void saveAbsolutePosition(String id, int relativeIPP, int relativeBPP) {
        saveAbsolutePosition(id, this.currentPageViewport,
                             relativeIPP, relativeBPP, graphicContext.getTransform());
    }

    protected void saveBlockPosIfTargetable(Block block) {
        String id = getTargetableID(block);
        if (id != null) {
            // FIXME: Like elsewhere in the renderer code, absolute and relative
            //        directions are happily mixed here. This makes sure that the
            //        links point to the right location, but it is not correct.
            int ipp = block.getXOffset();
            int bpp = block.getYOffset() + block.getSpaceBefore();
            int positioning = block.getPositioning();
            if (!(positioning == Block.FIXED || positioning == Block.ABSOLUTE)) {
                ipp += currentIPPosition;
                bpp += currentBPPosition;
            }
            saveAbsolutePosition(id, currentPageViewport, ipp, bpp, graphicContext.getTransform());
        }
    }

    private void saveInlinePosIfTargetable(InlineArea inlineArea) {
        String id = getTargetableID(inlineArea);
        if (id != null) {
            int extraMarginBefore = 5000; // millipoints
            int ipp = currentIPPosition;
            int bpp = currentBPPosition + inlineArea.getOffset() - extraMarginBefore;
            saveAbsolutePosition(id, ipp, bpp);
        }
    }

    private String getTargetableID(Area area) {
        String id = (String) area.getTrait(Trait.PROD_ID);
        if (id == null || id.length() == 0
            || !currentPageViewport.isFirstWithID(id)
            || idPositions.containsKey(id)) {
            return null;
        } else {
            return id;
        }
    }

    /** {@inheritDoc} */
    public void startPageSequence(PageSequence pageSequence) {
        try {
            if (this.inPageSequence) {
                documentHandler.endPageSequence();
            } else {
                if (this.documentMetadata == null) {
                    this.documentMetadata = createDefaultDocumentMetadata();
                }
                documentHandler.handleExtensionObject(this.documentMetadata);
                documentHandler.endDocumentHeader();
                this.inPageSequence = true;
            }
            documentHandler.startPageSequence(null);
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    private Metadata createDefaultDocumentMetadata() {
        Metadata xmp = new Metadata();
        DublinCoreAdapter dc = DublinCoreSchema.getAdapter(xmp);
        if (getUserAgent().getTitle() != null) {
            dc.setTitle(getUserAgent().getTitle());
        }
        if (getUserAgent().getAuthor() != null) {
            dc.addCreator(getUserAgent().getAuthor());
        }
        if (getUserAgent().getKeywords() != null) {
            dc.addSubject(getUserAgent().getKeywords());
        }
        XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(xmp);
        if (getUserAgent().getProducer() != null) {
            xmpBasic.setCreatorTool(getUserAgent().getProducer());
        } else {
            xmpBasic.setCreatorTool(Version.getVersion());
        }
        xmpBasic.setMetadataDate(new java.util.Date());
        if (getUserAgent().getCreationDate() != null) {
            xmpBasic.setCreateDate(getUserAgent().getCreationDate());
        } else {
            xmpBasic.setCreateDate(xmpBasic.getMetadataDate());
        }
        return xmp;
    }

    /** {@inheritDoc} */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        if (log.isTraceEnabled()) {
            log.trace("renderPage() " + page);
        }
        try {
            Rectangle2D viewArea = page.getViewArea();
            Dimension dim = new Dimension(
                    (int)Math.ceil(viewArea.getWidth()),
                    (int)Math.ceil(viewArea.getHeight()));
            documentHandler.startPage(page.getPageIndex(), page.getPageNumberString(),
                    page.getSimplePageMasterName(), dim);
            documentHandler.startPageHeader();
            //TODO Handle page header
            documentHandler.endPageHeader();
            this.painter = documentHandler.startPageContent();
            super.renderPage(page);
            this.painter = null;
            documentHandler.endPageContent();
            documentHandler.startPageTrailer();
            //TODO Handle page trailer
            documentHandler.endPageTrailer();
            documentHandler.endPage();
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    /** {@inheritDoc} */
    protected void saveGraphicsState() {
        graphicContextStack.push(graphicContext);
        graphicContext = (IFGraphicContext)graphicContext.clone();
    }

    /** {@inheritDoc} */
    protected void restoreGraphicsState() {
        while (graphicContext.getGroupStackSize() > 0) {
            IFGraphicContext.Group[] groups = graphicContext.dropGroups();
            for (int i = groups.length - 1; i >= 0; i--) {
                try {
                    groups[i].end(painter);
                } catch (IFException ife) {
                    handleIFException(ife);
                }
            }
        }
        graphicContext = (IFGraphicContext)graphicContextStack.pop();
    }

    private void pushGroup(IFGraphicContext.Group group) {
        graphicContext.pushGroup(group);
        try {
            group.start(painter);
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    protected List breakOutOfStateStack() {
        log.debug("Block.FIXED --> break out");
        List breakOutList = new java.util.ArrayList();
        while (!this.graphicContextStack.empty()) {
            //Handle groups
            IFGraphicContext.Group[] groups = graphicContext.getGroups();
            for (int j = groups.length - 1; j >= 0; j--) {
                try {
                    groups[j].end(painter);
                } catch (IFException ife) {
                    handleIFException(ife);
                }
            }

            breakOutList.add(0, this.graphicContext);
            graphicContext = (IFGraphicContext)graphicContextStack.pop();
        }
        return breakOutList;
    }

    /** {@inheritDoc} */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
        log.debug("Block.FIXED --> restoring context after break-out");
        for (int i = 0, c = breakOutList.size(); i < c; i++) {
            graphicContextStack.push(graphicContext);
            this.graphicContext = (IFGraphicContext)breakOutList.get(i);

            //Handle groups
            IFGraphicContext.Group[] groups = graphicContext.getGroups();
            for (int j = 0, jc = groups.length; j < jc; j++) {
                try {
                    groups[j].start(painter);
                } catch (IFException ife) {
                    handleIFException(ife);
                }
            }
        }
        log.debug("restored.");
    }

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        if (!at.isIdentity()) {
            concatenateTransformationMatrixMpt(ptToMpt(at), false);
        }
    }

    private void concatenateTransformationMatrixMpt(AffineTransform at, boolean force) {
        if (force || !at.isIdentity()) {
            if (log.isTraceEnabled()) {
                log.trace("-----concatenateTransformationMatrix: " + at);
            }
            IFGraphicContext.Group group = new IFGraphicContext.Group(at);
            pushGroup(group);
        }
    }

    /** {@inheritDoc} */
    protected void beginTextObject() {
        //nop - Ignore, handled by painter internally
    }

    /** {@inheritDoc} */
    protected void endTextObject() {
        //nop - Ignore, handled by painter internally
    }

    /** {@inheritDoc} */
    protected void renderRegionViewport(RegionViewport viewport) {
        Dimension dim = new Dimension(viewport.getIPD(), viewport.getBPD());
        viewportDimensionStack.push(dim);
        super.renderRegionViewport(viewport);
        viewportDimensionStack.pop();
    }

    /** {@inheritDoc} */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        //Essentially the same code as in the super class but optimized for the IF

        //This is the content-rect
        Dimension dim = new Dimension(bv.getIPD(), bv.getBPD());
        viewportDimensionStack.push(dim);

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();

        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            //For FIXED, we need to break out of the current viewports to the
            //one established by the page. We save the state stack for restoration
            //after the block-container has been painted. See below.
            List breakOutList = null;
            if (bv.getPositioning() == Block.FIXED) {
                breakOutList = breakOutOfStateStack();
            }

            AffineTransform positionTransform = new AffineTransform();
            positionTransform.translate(bv.getXOffset(), bv.getYOffset());

            //"left/"top" (bv.getX/YOffset()) specify the position of the content rectangle
            positionTransform.translate(-borderPaddingStart, -borderPaddingBefore);

            //Free transformation for the block-container viewport
            String transf;
            transf = bv.getForeignAttributeValue(FOX_TRANSFORM);
            if (transf != null) {
                AffineTransform freeTransform = AWTTransformProducer.createAffineTransform(transf);
                positionTransform.concatenate(freeTransform);
            }

            saveGraphicsState();
            //Viewport position
            concatenateTransformationMatrixMpt(positionTransform, false);

            //Background and borders
            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd());
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter());
            drawBackAndBorders(bv, 0, 0,
                    (dim.width + bpwidth) / 1000f, (dim.height + bpheight) / 1000f);

            //Shift to content rectangle after border painting
            AffineTransform contentRectTransform = new AffineTransform();
            contentRectTransform.translate(borderPaddingStart, borderPaddingBefore);
            concatenateTransformationMatrixMpt(contentRectTransform, false);

            //Clipping
            Rectangle clipRect = null;
            if (bv.getClip()) {
                clipRect = new Rectangle(0, 0, dim.width, dim.height);
                //clipRect(0f, 0f, width, height);
            }

            //saveGraphicsState();
            //Set up coordinate system for content rectangle
            AffineTransform contentTransform = ctm.toAffineTransform();
            //concatenateTransformationMatrixMpt(contentTransform);
            startViewport(contentTransform, clipRect);

            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);

            endViewport();
            //restoreGraphicsState();
            restoreGraphicsState();

            if (breakOutList != null) {
                restoreStateStackAfterBreakOut(breakOutList);
            }

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {

            currentBPPosition += bv.getSpaceBefore();

            //borders and background in the old coordinate system
            handleBlockTraits(bv);

            //Advance to start of content area
            currentIPPosition += bv.getStartIndent();

            CTM tempctm = new CTM(containingIPPosition, currentBPPosition);
            ctm = tempctm.multiply(ctm);

            //Now adjust for border/padding
            currentBPPosition += borderPaddingBefore;

            Rectangle2D clippingRect = null;
            if (bv.getClip()) {
                clippingRect = new Rectangle(currentIPPosition, currentBPPosition,
                        bv.getIPD(), bv.getBPD());
            }

            startVParea(ctm, clippingRect);
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);
            endVParea();

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;

            currentBPPosition += (int)(bv.getAllocBPD());
        }
        viewportDimensionStack.pop();
    }

    /** {@inheritDoc} */
    public void renderViewport(Viewport viewport) {
        Dimension dim = new Dimension(viewport.getIPD(), viewport.getBPD());
        viewportDimensionStack.push(dim);
        super.renderViewport(viewport);
        viewportDimensionStack.pop();
    }

    /** {@inheritDoc} */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        if (log.isTraceEnabled()) {
            log.trace("startVParea() ctm=" + ctm + ", clippingRect=" + clippingRect);
        }
        AffineTransform at = new AffineTransform(ctm.toArray());
        Rectangle clipRect = null;
        if (clippingRect != null) {
            clipRect = new Rectangle(
                    (int)clippingRect.getMinX() - currentIPPosition,
                    (int)clippingRect.getMinY() - currentBPPosition,
                    (int)clippingRect.getWidth(), (int)clippingRect.getHeight());
        }
        startViewport(at, clipRect);
        if (log.isTraceEnabled()) {
            log.trace("startVPArea: " + at + " --> " + graphicContext.getTransform());
        }
    }

    private void startViewport(AffineTransform at, Rectangle clipRect) {
        saveGraphicsState();
        try {
            IFGraphicContext.Viewport viewport = new IFGraphicContext.Viewport(
                    at, (Dimension)viewportDimensionStack.peek(), clipRect);
            graphicContext.pushGroup(viewport);
            viewport.start(painter);
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    /** {@inheritDoc} */
    protected void endVParea() {
        log.trace("endVParea()");
        endViewport();
        if (log.isTraceEnabled()) {
            log.trace("endVPArea() --> " + graphicContext.getTransform());
        }
    }

    private void endViewport() {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void renderInlineArea(InlineArea inlineArea) {
        saveInlinePosIfTargetable(inlineArea);
        super.renderInlineArea(inlineArea);
    }

    /** {@inheritDoc} */
    protected void renderBlock(Block block) {
        if (log.isTraceEnabled()) {
            log.trace("renderBlock() " + block);
        }
        saveBlockPosIfTargetable(block);
        super.renderBlock(block);
    }

    private Typeface getTypeface(String fontName) {
        Typeface tf = (Typeface) fontInfo.getFonts().get(fontName);
        if (tf instanceof LazyFont) {
            tf = ((LazyFont)tf).getRealFont();
        }
        return tf;
    }

    /** {@inheritDoc} */
    protected void renderText(TextArea text) {
        if (log.isTraceEnabled()) {
            log.trace("renderText() " + text);
        }
        renderInlineAreaBackAndBorders(text);
        Color ct = (Color) text.getTrait(Trait.COLOR);

        beginTextObject();

        String fontName = getInternalFontNameForArea(text);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = getTypeface(fontName);

        FontTriplet triplet = (FontTriplet)text.getTrait(Trait.FONT);
        try {
            painter.setFont(triplet.getName(), triplet.getStyle(), new Integer(triplet.getWeight()),
                    "normal", new Integer(size), ct);
        } catch (IFException e) {
            handleIFException(e);
        }

        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();
        textUtil.flush();
        textUtil.setStartPosition(rx, bl);
        super.renderText(text);

        textUtil.flush();
        renderTextDecoration(tf, size, text, bl, rx);
    }

    /** {@inheritDoc} */
    protected void renderWord(WordArea word) {
        Font font = getFontFromArea(word.getParentArea());
        String s = word.getWord();

        renderText(s, word.getLetterAdjustArray(),
                font, (AbstractTextArea)word.getParentArea());

        super.renderWord(word);
    }

    /** {@inheritDoc} */
    protected void renderSpace(SpaceArea space) {
        Font font = getFontFromArea(space.getParentArea());
        String s = space.getSpace();

        AbstractTextArea textArea = (AbstractTextArea)space.getParentArea();
        renderText(s, null, font, textArea);

        if (space.isAdjustable()) {
            //Used for justified text, for example
            int tws = ((TextArea) space.getParentArea()).getTextWordSpaceAdjust()
                         + 2 * textArea.getTextLetterSpaceAdjust();
            if (tws != 0) {
                textUtil.adjust(tws);
            }
        }
        super.renderSpace(space);
    }

    /**
     * Does low-level rendering of text.
     * @param s text to render
     * @param letterAdjust an array of widths for letter adjustment (may be null)
     * @param font to font in use
     * @param parentArea the parent text area to retrieve certain traits from
     */
    protected void renderText(String s,
                           int[] letterAdjust,
                           Font font, AbstractTextArea parentArea) {
        float fontSize = font.getFontSize() / 1000f;

        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            textUtil.addChar(ch);
            int glyphAdjust = 0;
            if (font.hasChar(ch)) {
                int tls = (i < l - 1 ? parentArea.getTextLetterSpaceAdjust() : 0);
                glyphAdjust += tls;
            }
            if (letterAdjust != null && i < l) {
                glyphAdjust += letterAdjust[i];
            }

            textUtil.adjust(glyphAdjust);
        }
    }

    private class TextUtil {
        private static final int INITIAL_BUFFER_SIZE = 16;
        private int[] dx = new int[INITIAL_BUFFER_SIZE];
        private boolean hasDX = false;
        private StringBuffer text = new StringBuffer();
        private int startx, starty;

        void addChar(char ch) {
            text.append(ch);
        }

        void adjust(int adjust) {
            if (adjust != 0) {
                int idx = text.length();
                if (idx > dx.length - 1) {
                    int newSize = Math.max(dx.length, idx + 1) + INITIAL_BUFFER_SIZE;
                    int[] newDX = new int[newSize];
                    System.arraycopy(dx, 0, newDX, 0, dx.length);
                    dx = newDX;
                }
                dx[idx] += adjust;
                hasDX = true;
            }
        }

        void reset() {
            if (text.length() > 0) {
                text.setLength(0);
                Arrays.fill(dx, 0);
                hasDX = false;
            }
        }

        void setStartPosition(int x, int y) {
            this.startx = x;
            this.starty = y;
        }

        void flush() {
            if (text.length() > 0) {
                try {
                    painter.drawText(startx, starty, (hasDX ? dx : null), null, text.toString());
                } catch (IFException e) {
                    handleIFException(e);
                }
                reset();
            }
        }
    }

    /** {@inheritDoc} */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos, image.getForeignAttributes());
    }

    /** {@inheritDoc} */
    protected void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        Rectangle posInt = new Rectangle(
                currentIPPosition + (int)pos.getX(),
                currentBPPosition + (int)pos.getY(),
                (int)pos.getWidth(),
                (int)pos.getHeight());
        uri = URISpecification.getURL(uri);
        try {
            painter.drawImage(uri, posInt, foreignAttributes);
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        endTextObject();
        Rectangle posInt = new Rectangle(
                currentIPPosition + (int)pos.getX(),
                currentBPPosition + (int)pos.getY(),
                (int)pos.getWidth(),
                (int)pos.getHeight());
        Document doc = fo.getDocument();
        try {
            painter.drawImage(doc, posInt, fo.getForeignAttributes());
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        int style = area.getRuleStyle();
        int ruleThickness = area.getRuleThickness();
        int startx = currentIPPosition + area.getBorderAndPaddingWidthStart();
        int starty = currentBPPosition + area.getOffset() + (ruleThickness / 2);
        int endx = currentIPPosition
                        + area.getBorderAndPaddingWidthStart()
                        + area.getIPD();
        Color col = (Color)area.getTrait(Trait.COLOR);

        Point start = new Point(startx, starty);
        Point end = new Point(endx, starty);
        try {
            painter.drawLine(start, end, ruleThickness, col, RuleStyle.valueOf(style));
        } catch (IFException ife) {
            handleIFException(ife);
        }

        super.renderLeader(area);
    }

    /** {@inheritDoc} */
    protected void clip() {
        throw new IllegalStateException("Not used");
    }

    /** {@inheritDoc} */
    protected void clipRect(float x, float y, float width, float height) {
        pushGroup(new IFGraphicContext.Group());
        try {
            painter.clipRect(toMillipointRectangle(x, y, width, height));
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    protected void closePath() {
        throw new IllegalStateException("Not used");
    }

    /** {@inheritDoc} */
    protected void drawBorders(float startx, float starty,
            float width, float height,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        Rectangle rect = toMillipointRectangle(startx, starty, width, height);
        try {
            painter.drawBorderRect(rect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(float x1, float y1, float x2, float y2, boolean horz,
            boolean startOrBefore, int style, Color col) {
        //Simplified implementation that is only used by renderTextDecoration()
        //drawBorders() is overridden and uses the Painter's high-level method drawBorderRect()
        updateColor(col, true);
        fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    private int toMillipoints(float coordinate) {
        return Math.round(coordinate * 1000);
    }

    private Rectangle toMillipointRectangle(float x, float y, float width, float height) {
        return new Rectangle(
                toMillipoints(x),
                toMillipoints(y),
                toMillipoints(width),
                toMillipoints(height));
    }

    /** {@inheritDoc} */
    protected void fillRect(float x, float y, float width, float height) {
        try {
            painter.fillRect(
                    toMillipointRectangle(x, y, width, height),
                    this.graphicContext.getPaint());
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    /** {@inheritDoc} */
    protected void moveTo(float x, float y) {
        throw new IllegalStateException("Not used");
    }

    /** {@inheritDoc} */
    protected void lineTo(float x, float y) {
        throw new IllegalStateException("Not used");
    }

    /** {@inheritDoc} */
    protected void updateColor(Color col, boolean fill) {
        if (fill) {
            this.graphicContext.setPaint(col);
        } else {
            this.graphicContext.setColor(col);
        }

    }

}
