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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.parser.AWTTransformProducer;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreAdapter;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

import org.apache.fop.Version;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeObject;
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
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
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
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.ActionSet;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.NamedDestination;
import org.apache.fop.render.intermediate.extensions.URIAction;
import org.apache.fop.render.pdf.PDFEventProducer;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * This renderer implementation is an adapter to the {@link IFPainter} interface. It is used
 * to generate content using FOP's intermediate format.
 */
public class IFRenderer extends AbstractPathOrientedRenderer {

    //TODO Many parts of the Renderer infrastructure are using floats (coordinates in points)
    //instead of ints (in millipoints). A lot of conversion to and from is performed.
    //When the new IF is established, the Renderer infrastructure should be revisited so check
    //if optimizations can be done to avoid int->float->int conversions.

    /** logging instance */
    protected static final Log log = LogFactory.getLog(IFRenderer.class);

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
     * The "go-to" actions in idGoTos that are not complete yet
     */
    private List unfinishedGoTos = new java.util.ArrayList();
    // can't use a Set because PDFGoTo.equals returns true if the target is the same,
    // even if the object number differs

    /** Maps unique PageViewport key to page indices (for link target handling) */
    protected Map pageIndices = new java.util.HashMap();

    private BookmarkTree bookmarkTree;
    private List deferredDestinations = new java.util.ArrayList();
    private List deferredLinks = new java.util.ArrayList();
    private ActionSet actionSet = new ActionSet();

    private TextUtil textUtil = new TextUtil();

    private Stack<String> ids = new Stack<String>();

    /**
     * Main constructor
     *
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public IFRenderer(FOUserAgent userAgent) {
        super(userAgent);
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return IF_MIME_TYPE;
    }

    /**
     * Sets the {@link IFDocumentHandler} to be used by the {@link IFRenderer}.
     * @param documentHandler the {@link IFDocumentHandler}
     */
    public void setDocumentHandler(IFDocumentHandler documentHandler) {
        this.documentHandler = documentHandler;
    }

    /** {@inheritDoc} */
    public void setupFontInfo(FontInfo inFontInfo) throws FOPException {
        if (this.documentHandler == null) {
            this.documentHandler = createDefaultDocumentHandler();
        }
        IFUtil.setupFonts(this.documentHandler, inFontInfo);
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
     * Returns the document navigation handler if available/supported.
     * @return the document navigation handler or null if not supported
     */
    protected IFDocumentNavigationHandler getDocumentNavigationHandler() {
        return this.documentHandler.getDocumentNavigationHandler();
    }

    /**
     * Indicates whether document navigation features are supported by the document handler.
     * @return true if document navigation features are available
     */
    protected boolean hasDocumentNavigation() {
        return getDocumentNavigationHandler() != null;
    }

    /**
     * Creates a default {@link IFDocumentHandler} when none has been set.
     * @return the default IFDocumentHandler
     */
    protected IFDocumentHandler createDefaultDocumentHandler() {
        FOUserAgent userAgent = getUserAgent();
        IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
        if (userAgent.isAccessibilityEnabled()) {
            userAgent.setStructureTreeEventHandler(serializer.getStructureTreeEventHandler());
        }
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

            //Wrap up document navigation
            if (hasDocumentNavigation()) {
                finishOpenGoTos();
                Iterator iter = this.deferredDestinations.iterator();
                while (iter.hasNext()) {
                    NamedDestination dest = (NamedDestination)iter.next();
                    iter.remove();
                    getDocumentNavigationHandler().renderNamedDestination(dest);
                }

                if (this.bookmarkTree != null) {
                    getDocumentNavigationHandler().renderBookmarkTree(this.bookmarkTree);
                }
            }

            documentHandler.endDocumentTrailer();
            documentHandler.endDocument();
        } catch (IFException e) {
            handleIFExceptionWithIOException(e);
        }
        pageIndices.clear();
        idPositions.clear();
        actionSet.clear();
        super.stopRenderer();
        log.debug("Rendering finished.");
    }

    @Override
    public void setDocumentLocale(Locale locale) {
        documentHandler.setDocumentLocale(locale);
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
            } else {
                try {
                    this.documentHandler.handleExtensionObject(attachment);
                } catch (IFException ife) {
                    handleIFException(ife);
                }
            }
        }
    }

    private void renderDestination(DestinationData dd) {
        if (!hasDocumentNavigation()) {
            return;
        }
        String targetID = dd.getIDRef();
        if (targetID == null || targetID.length() == 0) {
            throw new IllegalArgumentException("DestinationData must contain a ID reference");
        }
        PageViewport pv = dd.getPageViewport();
        if (pv != null) {
            GoToXYAction action = getGoToActionForID(targetID, pv.getPageIndex());
            NamedDestination namedDestination = new NamedDestination(targetID, action);
            this.deferredDestinations.add(namedDestination);
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
        if (!hasDocumentNavigation()) {
            return;
        }
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
        // Already a GoToXY present for this target? If not, create.
        GoToXYAction action = (GoToXYAction)actionSet.get(targetID);
        //GoToXYAction action = (GoToXYAction)idGoTos.get(targetID);
        if (action == null) {
            if (pageIndex < 0) {
                //pageIndex = page
            }
            Point position = (Point)idPositions.get(targetID);
            // can the GoTo already be fully filled in?
            if (pageIndex >= 0 && position != null) {
                action = new GoToXYAction(targetID, pageIndex, position);
            } else {
                // Not complete yet, can't use getPDFGoTo:
                action = new GoToXYAction(targetID, pageIndex, null);
                unfinishedGoTos.add(action);
            }
            action = (GoToXYAction)actionSet.put(action);
            //idGoTos.put(targetID, action);
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
        try {
            getDocumentNavigationHandler().addResolvedAction(action);
        } catch (IFException ife) {
            handleIFException(ife);
        }
        unfinishedGoTos.remove(action);
    }

    private void noteGoToPosition(GoToXYAction action, PageViewport pv, Point position) {
        action.setPageIndex(pv.getPageIndex());
        noteGoToPosition(action, position);
    }

    private void saveAbsolutePosition(String id, PageViewport pv,
            int relativeIPP, int relativeBPP, AffineTransform tf) {
        Point position = new Point(relativeIPP, relativeBPP);
        tf.transform(position, position);
        idPositions.put(id, position);
        // is there already a GoTo action waiting to be completed?
        GoToXYAction action = (GoToXYAction)actionSet.get(id);
        if (action != null) {
            noteGoToPosition(action, pv, position);
        }
    }

    private void saveAbsolutePosition(String id, int relativeIPP, int relativeBPP) {
        saveAbsolutePosition(id, this.currentPageViewport,
                             relativeIPP, relativeBPP, graphicContext.getTransform());
    }

    private void saveBlockPosIfTargetable(Block block) {
        String id = getTargetableID(block);
        if (hasDocumentNavigation() && id != null) {
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
        if (hasDocumentNavigation() && id != null) {
            int extraMarginBefore = 5000; // millipoints
            int ipp = currentIPPosition;
            int bpp = currentBPPosition
                + inlineArea.getBlockProgressionOffset() - extraMarginBefore;
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
                documentHandler.getContext().setLanguage(null);
            } else {
                if (this.documentMetadata == null) {
                    this.documentMetadata = createDefaultDocumentMetadata();
                }
                documentHandler.handleExtensionObject(this.documentMetadata);
                documentHandler.endDocumentHeader();
                this.inPageSequence = true;
            }
            establishForeignAttributes(pageSequence.getForeignAttributes());
            documentHandler.getContext().setLanguage(toLocale(pageSequence));
            documentHandler.startPageSequence(null);
            resetForeignAttributes();
            processExtensionAttachments(pageSequence);
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    private Locale toLocale(PageSequence pageSequence) {
        if (pageSequence.getLanguage() != null) {
            if (pageSequence.getCountry() != null) {
                return new Locale(pageSequence.getLanguage(), pageSequence.getCountry());
            } else {
                return new Locale(pageSequence.getLanguage());
            }
        }
        return null;
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
    public void preparePage(PageViewport page) {
        super.preparePage(page);
    }

    /** {@inheritDoc} */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        if (log.isTraceEnabled()) {
            log.trace("renderPage() " + page);
        }
        try {
            pageIndices.put(page.getKey(), new Integer(page.getPageIndex()));
            Rectangle viewArea = page.getViewArea();
            Dimension dim = new Dimension(viewArea.width, viewArea.height);

            establishForeignAttributes(page.getForeignAttributes());
            documentHandler.startPage(page.getPageIndex(), page.getPageNumberString(),
                    page.getSimplePageMasterName(), dim);
            resetForeignAttributes();
            documentHandler.startPageHeader();

            //Add page attachments to page header
            processExtensionAttachments(page);

            documentHandler.endPageHeader();
            this.painter = documentHandler.startPageContent();
            super.renderPage(page);
            this.painter = null;
            documentHandler.endPageContent();

            documentHandler.startPageTrailer();
            if (hasDocumentNavigation()) {
                Iterator iter = this.deferredLinks.iterator();
                while (iter.hasNext()) {
                    Link link = (Link)iter.next();
                    iter.remove();
                    getDocumentNavigationHandler().renderLink(link);
                }
            }
            documentHandler.endPageTrailer();

            establishForeignAttributes(page.getForeignAttributes());
            documentHandler.endPage();
            resetForeignAttributes();
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    private void processExtensionAttachments(AreaTreeObject area) throws IFException {
        if (area.hasExtensionAttachments()) {
            for (Iterator iter = area.getExtensionAttachments().iterator();
                iter.hasNext();) {
                ExtensionAttachment attachment = (ExtensionAttachment) iter.next();
                this.documentHandler.handleExtensionObject(attachment);
            }
        }
    }

    private void establishForeignAttributes(Map foreignAttributes) {
        documentHandler.getContext().setForeignAttributes(foreignAttributes);
    }

    private void resetForeignAttributes() {
        documentHandler.getContext().resetForeignAttributes();
    }

    private void establishStructureTreeElement(StructureTreeElement structureTreeElement) {
        documentHandler.getContext().setStructureTreeElement(structureTreeElement);
    }

    private void resetStructurePointer() {
        documentHandler.getContext().resetStructureTreeElement();
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

            //saveGraphicsState();
            //Set up coordinate system for content rectangle
            AffineTransform contentTransform = ctm.toAffineTransform();
            //concatenateTransformationMatrixMpt(contentTransform);
            startViewport(contentTransform, bv.getClipRectangle());

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

            startVParea(ctm, bv.getClipRectangle());
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);
            endVParea();

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;

            currentBPPosition += bv.getAllocBPD();
        }
        viewportDimensionStack.pop();
    }

    /** {@inheritDoc} */
    public void renderInlineViewport(InlineViewport viewport) {
        StructureTreeElement structElem
                = (StructureTreeElement) viewport.getTrait(Trait.STRUCTURE_TREE_ELEMENT);
        establishStructureTreeElement(structElem);
        pushdID(viewport);
        Dimension dim = new Dimension(viewport.getIPD(), viewport.getBPD());
        viewportDimensionStack.push(dim);
        super.renderInlineViewport(viewport);
        viewportDimensionStack.pop();
        resetStructurePointer();
        popID(viewport);
    }

    /** {@inheritDoc} */
    protected void startVParea(CTM ctm, Rectangle clippingRect) {
        if (log.isTraceEnabled()) {
            log.trace("startVParea() ctm=" + ctm + ", clippingRect=" + clippingRect);
        }
        AffineTransform at = new AffineTransform(ctm.toArray());
        startViewport(at, clippingRect);
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
        pushdID(inlineArea);
        super.renderInlineArea(inlineArea);
        popID(inlineArea);
    }

    /** {@inheritDoc} */
    public void renderInlineParent(InlineParent ip) {
        // stuff we only need if a link must be created:
        Rectangle ipRect = null;
        AbstractAction action = null;
        // make sure the rect is determined *before* calling super!
        int ipp = currentIPPosition;
        int bpp = currentBPPosition + ip.getBlockProgressionOffset();
        ipRect = new Rectangle(ipp, bpp, ip.getIPD(), ip.getBPD());
        AffineTransform transform = graphicContext.getTransform();
        ipRect = transform.createTransformedShape(ipRect).getBounds();

        // render contents
        super.renderInlineParent(ip);

        boolean linkTraitFound = false;

        // try INTERNAL_LINK first
        Trait.InternalLink intLink = (Trait.InternalLink) ip.getTrait(Trait.INTERNAL_LINK);
        if (intLink != null) {
            linkTraitFound = true;
            String pvKey = intLink.getPVKey();
            String idRef = intLink.getIDRef();
            boolean pvKeyOK = pvKey != null && pvKey.length() > 0;
            boolean idRefOK = idRef != null && idRef.length() > 0;
            if (pvKeyOK && idRefOK) {
                Integer pageIndex = (Integer)pageIndices.get(pvKey);
                action = getGoToActionForID(idRef, (pageIndex != null ? pageIndex.intValue() : -1));
            } else {
                //Warnings already issued by AreaTreeHandler
            }
        }

        // no INTERNAL_LINK, look for EXTERNAL_LINK
        if (!linkTraitFound) {
            Trait.ExternalLink extLink = (Trait.ExternalLink) ip.getTrait(Trait.EXTERNAL_LINK);
            if (extLink != null) {
                String extDest = extLink.getDestination();
                if (extDest != null && extDest.length() > 0) {
                    linkTraitFound = true;
                    action = new URIAction(extDest, extLink.newWindow());
                    action = actionSet.put(action);
                }
            }
        }

        // warn if link trait found but not allowed, else create link
        if (linkTraitFound) {
            StructureTreeElement structElem
                    = (StructureTreeElement) ip.getTrait(Trait.STRUCTURE_TREE_ELEMENT);
            action.setStructureTreeElement(structElem);
            Link link = new Link(action, ipRect);
            this.deferredLinks.add(link);
        }
    }

    /** {@inheritDoc} */
    protected void renderBlock(Block block) {
        if (log.isTraceEnabled()) {
            log.trace("renderBlock() " + block);
        }
        saveBlockPosIfTargetable(block);
        pushdID(block);
        super.renderBlock(block);
        popID(block);
    }

    private void pushdID(Area area) {
        String prodID = (String) area.getTrait(Trait.PROD_ID);
        if (prodID != null) {
            ids.push(prodID);
            documentHandler.getContext().setID(prodID);
        }
    }

    private void popID(Area area) {
        String prodID = (String) area.getTrait(Trait.PROD_ID);
        if (prodID != null) {
            ids.pop();
            documentHandler.getContext().setID(ids.empty() ? "" : ids.peek());
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
    protected void renderText(TextArea text) {
        if (log.isTraceEnabled()) {
            log.trace("renderText() " + text);
        }
        renderInlineAreaBackAndBorders(text);
        Color ct = (Color) text.getTrait(Trait.COLOR);

        beginTextObject();

        String fontName = getInternalFontNameForArea(text);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        StructureTreeElement structElem
                = (StructureTreeElement) text.getTrait(Trait.STRUCTURE_TREE_ELEMENT);
        establishStructureTreeElement(structElem);

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
        int bl = currentBPPosition + text.getBlockProgressionOffset() + text.getBaselineOffset();
        textUtil.flush();
        textUtil.setStartPosition(rx, bl);
        textUtil.setSpacing(text.getTextLetterSpaceAdjust(), text.getTextWordSpaceAdjust());
        super.renderText(text);

        textUtil.flush();
        renderTextDecoration(tf, size, text, bl, rx);
        resetStructurePointer();
    }

    /** {@inheritDoc} */
    protected void renderWord(WordArea word) {
        Font font = getFontFromArea(word.getParentArea());
        String s = word.getWord();

        int[][] dp = word.getGlyphPositionAdjustments();
        if ( dp == null ) {
            dp = IFUtil.convertDXToDP ( word.getLetterAdjustArray() );
        }

        renderText(s, dp, word.isReversed(),
                font, (AbstractTextArea)word.getParentArea());

        super.renderWord(word);
    }

    /** {@inheritDoc} */
    protected void renderSpace(SpaceArea space) {
        Font font = getFontFromArea(space.getParentArea());
        String s = space.getSpace();

        AbstractTextArea textArea = (AbstractTextArea)space.getParentArea();
        renderText(s, null, false, font, textArea);

        if (textUtil.combined && space.isAdjustable()) {
            //Used for justified text, for example
            int tws = textArea.getTextWordSpaceAdjust()
                         + 2 * textArea.getTextLetterSpaceAdjust();
            if (tws != 0) {
                textUtil.adjust(tws);
            }
        }
        super.renderSpace(space);
    }

    private void renderText(String s,
                              int[][] dp, boolean reversed,
                              Font font, AbstractTextArea parentArea) {
        if ( ( dp == null ) || IFUtil.isDPOnlyDX ( dp ) ) {
            int[] dx = IFUtil.convertDPToDX ( dp );
            renderTextWithAdjustments ( s, dx, reversed, font, parentArea );
        } else {
            renderTextWithAdjustments ( s, dp, reversed, font, parentArea );
        }
    }

    /**
     * Does low-level rendering of text using DX only position adjustments.
     * @param s text to render
     * @param dx an array of widths for letter adjustment (may be null)
     * @param reversed if true then text has been reversed (from logical order)
     * @param font to font in use
     * @param parentArea the parent text area to retrieve certain traits from
     */
    private void renderTextWithAdjustments(String s,
                              int[] dx, boolean reversed,
                              Font font, AbstractTextArea parentArea) {
        int l = s.length();
        if (l == 0) {
            return;
        }
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            textUtil.addChar(ch);
            int glyphAdjust = 0;
            if (textUtil.combined && font.hasChar(ch)) {
                int tls = (i < l - 1 ? parentArea.getTextLetterSpaceAdjust() : 0);
                glyphAdjust += tls;
            }
            if (dx != null && i < l) {
                glyphAdjust += dx[i];
            }
            textUtil.adjust(glyphAdjust);
        }
    }

    /**
     * Does low-level rendering of text using generalized position adjustments.
     * @param s text to render
     * @param dp an array of 4-tuples, expressing [X,Y] placment
     * adjustments and [X,Y] advancement adjustments, in that order (may be null)
     * @param reversed if true then text has been reversed (from logical order)
     * @param font to font in use
     * @param parentArea the parent text area to retrieve certain traits from
     */
    private void renderTextWithAdjustments(String s,
                              int[][] dp, boolean reversed,
                              Font font, AbstractTextArea parentArea) {
        assert !textUtil.combined;
        for ( int i = 0, n = s.length(); i < n; i++ ) {
            textUtil.addChar ( s.charAt ( i ) );
            if ( dp != null ) {
                textUtil.adjust ( dp[i] );
            }
        }
    }

    private class TextUtil {
        private static final int INITIAL_BUFFER_SIZE = 16;
        private int[][] dp = new int[INITIAL_BUFFER_SIZE][4];
        // private int lastDPPos = 0; // TBD - not yet used
        private final StringBuffer text = new StringBuffer();
        private int startx;
        private int starty;
        private int tls;
        private int tws;
        private final boolean combined = false;

        void addChar(char ch) {
            text.append(ch);
        }

        void adjust(int dx) {
            adjust ( new int[] {
                    dx,                         // xPlaAdjust
                    0,                          // yPlaAdjust
                    dx,                         // xAdvAdjust
                    0                           // yAdvAdjust
                } );
        }

        void adjust(int[] pa) {
            if ( !IFUtil.isPAIdentity ( pa ) ) {
                int idx = text.length();
                if (idx > dp.length - 1) {
                    int newSize = Math.max(dp.length, idx + 1) + INITIAL_BUFFER_SIZE;
                    int[][] newDP = new int[newSize][];
                    // reuse prior PA[0]...PA[dp.length-1]
                    System.arraycopy(dp, 0, newDP, 0, dp.length);
                    // populate new PA[dp.length]...PA[newDP.length-1]
                    for ( int i = dp.length, n = newDP.length; i < n; i++ ) {
                        newDP[i] = new int[4];
                    }
                    dp = newDP;
                }
                IFUtil.adjustPA ( dp[idx - 1], pa );
                // lastDPPos = idx;
            }
        }

        void reset() {
            if (text.length() > 0) {
                text.setLength(0);
                for ( int i = 0, n = dp.length; i < n; i++ ) {
                    Arrays.fill(dp[i], 0);
                }
                // lastDPPos = 0;
            }
        }

        void setStartPosition(int x, int y) {
            this.startx = x;
            this.starty = y;
        }

        void setSpacing(int tls, int tws) {
            this.tls = tls;
            this.tws = tws;
        }

        void flush() {
            if (text.length() > 0) {
                try {
                    if (combined) {
                        painter.drawText(startx, starty, 0, 0,
                                         trimAdjustments ( dp, text.length() ), text.toString());
                    } else {
                        painter.drawText(startx, starty, tls, tws,
                                         trimAdjustments ( dp, text.length() ), text.toString());
                    }
                } catch (IFException e) {
                    handleIFException(e);
                }
                reset();
            }
        }

        /**
         * Trim adjustments array <code>dp</code> to be no greater length than
         * text length, and where trailing all-zero entries are removed.
         * @param dp a position adjustments array (or null)
         * @param textLength the length of the associated text
         * @return either the original value of <code>dp</code> or a copy
         * of its first N significant adjustment entries, such that N is
         * no greater than text length, and the last entry has a non-zero
         * adjustment.
         */
        private int[][] trimAdjustments ( int[][] dp, int textLength ) {
            if ( dp != null ) {
                int tl = textLength;
                int pl = dp.length;
                int i  = ( tl < pl ) ? tl : pl;
                while ( i > 0 ) {
                    int[] pa = dp [ i - 1 ];
                    if ( !IFUtil.isPAIdentity ( pa ) ) {
                        break;
                    } else {
                        i--;
                    }
                }
                if ( i == 0 ) {
                    dp = null;
                } else if ( i < pl ) {
                    dp = IFUtil.copyDP ( dp, 0, i );
                }
            }
            return dp;
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
            establishForeignAttributes(foreignAttributes);
            painter.drawImage(uri, posInt);
            resetForeignAttributes();
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
            establishForeignAttributes(fo.getForeignAttributes());
            painter.drawImage(doc, posInt);
            resetForeignAttributes();
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
        int starty = currentBPPosition + area.getBlockProgressionOffset() + (ruleThickness / 2);
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
    protected void clipBackground(float startx, float starty,
            float width, float height,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        pushGroup(new IFGraphicContext.Group());
        Rectangle rect = toMillipointRectangle(startx, starty, width, height);
        try {
            painter.clipBackground( rect,
                 bpsBefore,  bpsAfter, bpsStart,  bpsEnd);
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }
    /** {@inheritDoc} */
    protected void closePath() {
        throw new IllegalStateException("Not used");
    }

    /** {@inheritDoc} */
    protected void drawBackground(float startx, float starty,
            float width, float height,
            Trait.Background back,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        if (painter.isBackgroundRequired(bpsBefore, bpsAfter, bpsStart, bpsEnd)) {
            super.drawBackground(startx, starty, width, height,
                     back,  bpsBefore,  bpsAfter,
                     bpsStart,  bpsEnd);
        }
    }

    /** {@inheritDoc} */
    protected void drawBorders(                                  // CSOK: ParameterNumber
            float startx, float starty,
            float width, float height,
            BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd, int level, Color innerBackgroundColor) {
        Rectangle rect = toMillipointRectangle(startx, starty, width, height);
        try {
            BorderProps bpsTop = bpsBefore;
            BorderProps bpsBottom = bpsAfter;
            BorderProps bpsLeft;
            BorderProps bpsRight;
            if ( ( level == -1 ) || ( ( level & 1 ) == 0 ) ) {
                bpsLeft = bpsStart;
                bpsRight = bpsEnd;
            } else {
                bpsLeft = bpsEnd;
                bpsRight = bpsStart;
            }
            painter.drawBorderRect(rect, bpsTop, bpsBottom, bpsLeft, bpsRight, innerBackgroundColor);
        } catch (IFException ife) {
            handleIFException(ife);
        }
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(                               // CSOK: ParameterNumber
            float x1, float y1, float x2, float y2, boolean horz,
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
