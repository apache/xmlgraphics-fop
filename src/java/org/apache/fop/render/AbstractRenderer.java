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

package org.apache.fop.render;

// Java
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.InlineViewport;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.FontInfo;

/**
 * Abstract base class for all renderers. The Abstract renderer does all the
 * top level processing of the area tree and adds some abstract methods to
 * handle viewports. This keeps track of the current block and inline position.
 */
public abstract class AbstractRenderer
         implements Renderer, Constants {

    /** logging instance */
    protected static final Log log = LogFactory.getLog("org.apache.fop.render");

    /**
     * user agent
     */
    protected FOUserAgent userAgent = null;

    /**
     * block progression position
     */
    protected int currentBPPosition = 0;

    /**
     * inline progression position
     */
    protected int currentIPPosition = 0;

    /**
     * the block progression position of the containing block used for
     * absolutely positioned blocks
     */
    protected int containingBPPosition = 0;

    /**
     * the inline progression position of the containing block used for
     * absolutely positioned blocks
     */
    protected int containingIPPosition = 0;

    /** the currently active PageViewport */
    protected PageViewport currentPageViewport;

    private Set warnedXMLHandlers;

    /** {@inheritDoc} */
    public abstract void setupFontInfo(FontInfo fontInfo) throws FOPException;

    /**
     *
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public AbstractRenderer(FOUserAgent userAgent) {
        this.userAgent = userAgent;
    }

    /** {@inheritDoc} */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream)
            throws IOException {
        if (userAgent == null) {
            throw new IllegalStateException("FOUserAgent has not been set on Renderer");
        }
    }

    /** {@inheritDoc} */
    public void stopRenderer()
        throws IOException { }

    /**
     * Check if this renderer supports out of order rendering. If this renderer
     * supports out of order rendering then it means that the pages that are
     * not ready will be prepared and a future page will be rendered.
     *
     * @return   True if the renderer supports out of order rendering
     */
    public boolean supportsOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public void setDocumentLocale(Locale locale) {
    }

    /**
     * {@inheritDoc}
     */
    public void processOffDocumentItem(OffDocumentItem odi) { }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return null;
    }

    /** {@inheritDoc} */
    public ImageAdapter getImageAdapter() {
        return null;
    }

    /** @return the current PageViewport or null, if none is active */
    protected PageViewport getCurrentPageViewport() {
        return this.currentPageViewport;
    }

    /** {@inheritDoc} */
    public void preparePage(PageViewport page) { }

    /**
     * Utility method to convert a page sequence title to a string. Some
     * renderers may only be able to use a string title. A title is a sequence
     * of inline areas that this method attempts to convert to an equivalent
     * string.
     *
     * @param title  The Title to convert
     * @return       An expanded string representing the title
     */
    protected String convertTitleToString(LineArea title) {
        List children = title.getInlineAreas();
        String str = convertToString(children);
        return str.trim();
    }

    private String convertToString(List children) {
        StringBuffer sb = new StringBuffer();
        for (int count = 0; count < children.size(); count++) {
            InlineArea inline = (InlineArea) children.get(count);
            //if (inline instanceof Character) {
            //    sb.append(((Character) inline).getChar());
            /*} else*/ if (inline instanceof TextArea) {
                sb.append(((TextArea) inline).getText());
            } else if (inline instanceof InlineParent) {
                sb.append(convertToString(
                        ((InlineParent) inline).getChildAreas()));
            } else {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public void startPageSequence(LineArea seqTitle) {
        //do nothing
    }

    /** {@inheritDoc} */
    public void startPageSequence(PageSequence pageSequence) {
        // do nothing
    }

    // normally this would be overriden to create a page in the
    // output
    /** {@inheritDoc} */
    public void renderPage(PageViewport page)
        throws IOException, FOPException {

        this.currentPageViewport = page;
        try {
            Page p = page.getPage();
            renderPageAreas(p);
        } finally {
            this.currentPageViewport = null;
        }
    }

    /**
     * Renders page areas.
     *
     * @param page  The page whos page areas are to be rendered
     */
    protected void renderPageAreas(Page page) {
        /* Spec does not appear to specify whether fo:region-body should
        appear above or below side regions in cases of overlap.  FOP
        decision is to have fo:region-body on top, hence it is rendered
        last here. */
        RegionViewport viewport;
        viewport = page.getRegionViewport(FO_REGION_BEFORE);
        if (viewport != null) {
            renderRegionViewport(viewport);
        }
        viewport = page.getRegionViewport(FO_REGION_START);
        if (viewport != null) {
            renderRegionViewport(viewport);
        }
        viewport = page.getRegionViewport(FO_REGION_END);
        if (viewport != null) {
            renderRegionViewport(viewport);
        }
        viewport = page.getRegionViewport(FO_REGION_AFTER);
        if (viewport != null) {
            renderRegionViewport(viewport);
        }
        viewport = page.getRegionViewport(FO_REGION_BODY);
        if (viewport != null) {
            renderRegionViewport(viewport);
        }
    }

    /**
     * Renders a region viewport. <p>
     *
     * The region may clip the area and it establishes a position from where
     * the region is placed.</p>
     *
     * @param port  The region viewport to be rendered
     */
    protected void renderRegionViewport(RegionViewport port) {
        // The CTM will transform coordinates relative to
        // this region-reference area into page coords, so
        // set origin for the region to 0,0.
        currentBPPosition = 0;
        currentIPPosition = 0;

        RegionReference regionReference = port.getRegionReference();
        handleRegionTraits(port);

        //  shouldn't the viewport have the CTM
        startVParea(regionReference.getCTM(), port.getClipRectangle());
        // do after starting viewport area
        if (regionReference.getRegionClass() == FO_REGION_BODY) {
            renderBodyRegion((BodyRegion) regionReference);
        } else {
            renderRegion(regionReference);
        }
        endVParea();
    }

    /**
     * Establishes a new viewport area.
     *
     * @param ctm the coordinate transformation matrix to use
     * @param clippingRect the clipping rectangle if the viewport should be clipping,
     *                     null if no clipping is performed.
     */
    protected abstract void startVParea(CTM ctm, Rectangle clippingRect);

    /**
     * Signals exit from a viewport area. Subclasses can restore transformation matrices
     * valid before the viewport area was started.
     */
    protected abstract void endVParea();

    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param rv the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport rv) {
        // draw border and background
    }

    /**
     * Renders a region reference area.
     *
     * @param region  The region reference area
     */
    protected void renderRegion(RegionReference region) {
        renderBlocks(null, region.getBlocks());
    }

    /**
     * Renders a body region area.
     *
     * @param region  The body region
     */
    protected void renderBodyRegion(BodyRegion region) {
        BeforeFloat bf = region.getBeforeFloat();
        if (bf != null) {
            renderBeforeFloat(bf);
        }
        MainReference mr = region.getMainReference();
        if (mr != null) {
            renderMainReference(mr);
        }
        Footnote foot = region.getFootnote();
        if (foot != null) {
            renderFootnote(foot);
        }
    }

    /**
     * Renders a before float area.
     *
     * @param bf  The before float area
     */
    protected void renderBeforeFloat(BeforeFloat bf) {
        List blocks = bf.getChildAreas();
        if (blocks != null) {
            renderBlocks(null, blocks);
            Block sep = bf.getSeparator();
            if (sep != null) {
                renderBlock(sep);
            }
        }
    }

    /**
     * Renders a footnote
     *
     * @param footnote  The footnote
     */
    protected void renderFootnote(Footnote footnote) {
        currentBPPosition += footnote.getTop();
        List blocks = footnote.getChildAreas();
        if (blocks != null) {
            Block sep = footnote.getSeparator();
            if (sep != null) {
                renderBlock(sep);
            }
            renderBlocks(null, blocks);
        }
    }

    /**
     * Renders the main reference area.
     * <p>
     * The main reference area contains a list of spans that are
     * stacked on the page.
     * The spans contain a list of normal flow reference areas
     * that are positioned into columns.
     * </p>
     *
     * @param mr  The main reference area
     */
    protected void renderMainReference(MainReference mr) {
        Span span = null;
        List spans = mr.getSpans();
        int saveBPPos = currentBPPosition;
        int saveSpanBPPos = saveBPPos;
        int saveIPPos = currentIPPosition;
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            int level = span.getBidiLevel();
            if (level < 0) {
                level = 0;
            }
            if ((level & 1) == 1) {
                currentIPPosition += span.getIPD();
                currentIPPosition += mr.getColumnGap();
            }
            for (int c = 0; c < span.getColumnCount(); c++) {
                NormalFlow flow = span.getNormalFlow(c);
                if (flow != null) {
                    currentBPPosition = saveSpanBPPos;
                    if ((level & 1) == 1) {
                        currentIPPosition -= flow.getIPD();
                        currentIPPosition -= mr.getColumnGap();
                    }
                    renderFlow(flow);
                    if ((level & 1) == 0) {
                        currentIPPosition += flow.getIPD();
                        currentIPPosition += mr.getColumnGap();
                    }
                }
            }
            currentIPPosition = saveIPPos;
            currentBPPosition = saveSpanBPPos + span.getHeight();
            saveSpanBPPos = currentBPPosition;
        }
        currentBPPosition = saveBPPos;
    }

    /**
     * Renders a flow reference area.
     *
     * @param flow  The flow reference area
     */
    protected void renderFlow(NormalFlow flow) {
        // the normal flow reference area contains stacked blocks
        List blocks = flow.getChildAreas();
        if (blocks != null) {
            renderBlocks(null, blocks);
        }
    }

    /**
     * Handle block traits.
     * This method is called when the correct ip and bp posiiton is
     * set. This should be overridden to draw border and background
     * traits for the block area.
     *
     * @param block the block area
     */
    protected void handleBlockTraits(Block block) {
        // draw border and background
    }

    /**
     * Renders a block viewport.
     *
     * @param bv        The block viewport
     * @param children  The children to render within the block viewport
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary
        if (bv.getPositioning() == Block.ABSOLUTE) {
            // save positions
            int saveIP = currentIPPosition;
            int saveBP = currentBPPosition;

            Rectangle clippingRect = null;
            if (bv.hasClip()) {
                clippingRect = new Rectangle(saveIP, saveBP, bv.getIPD(), bv.getBPD());
            }

            CTM ctm = bv.getCTM();
            currentIPPosition = 0;
            currentBPPosition = 0;

            startVParea(ctm, clippingRect);
            handleBlockTraits(bv);
            renderBlocks(bv, children);
            endVParea();

            // clip if necessary

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {
            // save position and offset
            int saveIP = currentIPPosition;
            int saveBP = currentBPPosition;

            handleBlockTraits(bv);
            renderBlocks(bv, children);

            currentIPPosition = saveIP;
            currentBPPosition = saveBP + bv.getAllocBPD();
        }
    }

    /**
     * Renders a block area that represents a reference area. The reference area establishes
     * a new coordinate system.
     * @param block the block area
     */
    protected abstract void renderReferenceArea(Block block);

    /**
     * Renders a list of block areas.
     *
     * @param parent  the parent block if the parent is a block, otherwise
     *                a null value.
     * @param blocks  The block areas
     */
    protected void renderBlocks(Block parent, List blocks) {
        int saveIP = currentIPPosition;

        // Calculate the position of the content rectangle.
        if (parent != null && !parent.getTraitAsBoolean(Trait.IS_VIEWPORT_AREA)) {
            currentBPPosition += parent.getBorderAndPaddingWidthBefore();
        }

        // the position of the containing block is used for
        // absolutely positioned areas
        int contBP = currentBPPosition;
        int contIP = currentIPPosition;
        containingBPPosition = currentBPPosition;
        containingIPPosition = currentIPPosition;

        for (int count = 0; count < blocks.size(); count++) {
            Object obj = blocks.get(count);
            if (obj instanceof Block) {
                currentIPPosition = contIP;
                containingBPPosition = contBP;
                containingIPPosition = contIP;
                renderBlock((Block) obj);
                containingBPPosition = contBP;
                containingIPPosition = contIP;
            } else if (obj instanceof LineArea) {
                // a line area is rendered from the top left position
                // of the line, each inline object is offset from there
                LineArea line = (LineArea) obj;
                if (parent != null) {
                    int level = parent.getBidiLevel();
                    if ((level == -1) || ((level & 1) == 0)) {
                        currentIPPosition += parent.getStartIndent();
                    } else {
                        currentIPPosition += parent.getEndIndent();
                    }
                }
                renderLineArea(line);
                currentBPPosition += line.getAllocBPD();
            }
            currentIPPosition = saveIP;
        }
    }

    /**
     * Renders a block area.
     *
     * @param block  The block area
     */
    protected void renderBlock(Block block) {
        assert block != null;
        List children = block.getChildAreas();
        if (block instanceof BlockViewport) {
            if (children != null) {
                renderBlockViewport((BlockViewport) block, children);
            } else {
                handleBlockTraits(block);
                // simply move position
                currentBPPosition += block.getAllocBPD();
            }
        } else if (block.getTraitAsBoolean(Trait.IS_REFERENCE_AREA)) {
            renderReferenceArea(block);
        } else {
            // save position and offset
            int saveIP = currentIPPosition;
            int saveBP = currentBPPosition;

            currentIPPosition += block.getXOffset();
            currentBPPosition += block.getYOffset();
            currentBPPosition += block.getSpaceBefore();

            handleBlockTraits(block);

            if (children != null) {
                renderBlocks(block, children);
            }

            if (block.getPositioning() == Block.ABSOLUTE) {
                // absolute blocks do not effect the layout
                currentBPPosition = saveBP;
            } else {
                // stacked and relative blocks effect stacking
                currentIPPosition = saveIP;
                currentBPPosition = saveBP + block.getAllocBPD();
            }
        }
    }

    /**
     * Renders a line area. <p>
     *
     * A line area may have grouped styling for its children such as underline,
     * background.</p>
     *
     * @param line  The line area
     */
    protected void renderLineArea(LineArea line) {
        List children = line.getInlineAreas();
        int saveBP = currentBPPosition;
        currentBPPosition += line.getSpaceBefore();
        int bl = line.getBidiLevel();
        if (bl >= 0) {
            if ((bl & 1) == 0) {
                currentIPPosition += line.getStartIndent();
            } else {
                currentIPPosition += line.getEndIndent();
                // if line's content overflows line area, then
                // ensure that overflow is drawn (extends)
                // outside of left side of line area
                int overflow = computeInlinesOverflow (line);
                if (overflow > 0) {
                    currentIPPosition -= overflow;
                }
            }
        } else {
            currentIPPosition += line.getStartIndent();
        }
        for (int i = 0, l = children.size(); i < l; i++) {
            InlineArea inline = (InlineArea) children.get(i);
            renderInlineArea(inline);
        }
        currentBPPosition = saveBP;
    }

    private int computeInlinesOverflow (LineArea line) {
        List children = line.getInlineAreas();
        int ipdConsumed = 0;
        for (int i = 0, l = children.size(); i < l; i++) {
            InlineArea inline = (InlineArea) children.get(i);
            ipdConsumed += inline.getIPD();
        }
        return ipdConsumed - line.getIPD();
    }

    /**
     * Render the given InlineArea.
     * @param inlineArea inline area text to render
     */
    protected void renderInlineArea(InlineArea inlineArea) {
        if (inlineArea instanceof TextArea) {
            renderText((TextArea) inlineArea);
        //} else if (inlineArea instanceof Character) {
            //renderCharacter((Character) inlineArea);
        } else if (inlineArea instanceof WordArea) {
            renderWord((WordArea) inlineArea);
        } else if (inlineArea instanceof SpaceArea) {
            renderSpace((SpaceArea) inlineArea);
        } else if (inlineArea instanceof InlineParent) {
            renderInlineParent((InlineParent) inlineArea);
        } else if (inlineArea instanceof InlineBlockParent) {
            renderInlineBlockParent((InlineBlockParent) inlineArea);
        } else if (inlineArea instanceof Space) {
            renderInlineSpace((Space) inlineArea);
        } else if (inlineArea instanceof InlineViewport) {
            renderInlineViewport((InlineViewport) inlineArea);
        } else if (inlineArea instanceof Leader) {
            renderLeader((Leader) inlineArea);
        }
    }

    /**
     * Common method to render the background and borders for any inline area.
     * The all borders and padding are drawn outside the specified area.
     * @param area the inline area for which the background, border and padding is to be
     * rendered
     */
    protected abstract void renderInlineAreaBackAndBorders(InlineArea area);

    /**
     * Render the given Space.
     * @param space the space to render
     */
    protected void renderInlineSpace(Space space) {
        renderInlineAreaBackAndBorders(space);
        // an inline space moves the inline progression position
        // for the current block by the width or height of the space
        // it may also have styling (only on this object) that needs
        // handling
        currentIPPosition += space.getAllocIPD();
    }

    /**
     * Render the given Leader.
     * @param area the leader to render
     */
    protected void renderLeader(Leader area) {
        currentIPPosition += area.getAllocIPD();
    }

    /**
     * Render the given TextArea.
     * @param text the text to render
     */
    protected void renderText(TextArea text) {
        List children = text.getChildAreas();
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;
        for (int i = 0, l = children.size(); i < l; i++) {
            InlineArea inline = (InlineArea) children.get(i);
            renderInlineArea(inline);
        }
        currentIPPosition = saveIP + text.getAllocIPD();
    }

    /**
     * Render the given WordArea.
     * @param word the word to render
     */
    protected void renderWord(WordArea word) {
        currentIPPosition += word.getAllocIPD();
    }

    /**
     * Render the given SpaceArea.
     * @param space the space to render
     */
    protected void renderSpace(SpaceArea space) {
        currentIPPosition += space.getAllocIPD();
    }

    /**
     * Render the given InlineParent.
     * @param ip the inline parent to render
     */
    protected void renderInlineParent(InlineParent ip) {
        int level = ip.getBidiLevel();
        List children = ip.getChildAreas();
        renderInlineAreaBackAndBorders(ip);
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;
        // if inline parent is a filled area (generated by Leader), and if
        // it is right-to-left, then adjust starting ip position in order to
        // align children to starting (right) edge of filled area
        int ipAdjust;
        if ((ip instanceof FilledArea) && ((level & 1) != 0)) {
            int ipdChildren = 0;
            for (int i = 0, l = children.size(); i < l; i++) {
                InlineArea inline = (InlineArea) children.get(i);
                ipdChildren += inline.getAllocIPD();
            }
            ipAdjust = ip.getAllocIPD() - ipdChildren;
        } else {
            ipAdjust = 0;
        }
        // perform inline position adjustments
        if ((level == -1) || ((level & 1) == 0)) {
            currentIPPosition += ip.getBorderAndPaddingWidthStart();
        } else {
            currentIPPosition += ip.getBorderAndPaddingWidthEnd();
            if (ipAdjust > 0) {
                currentIPPosition += ipAdjust;
            }
        }
        currentBPPosition += ip.getBlockProgressionOffset();
        // render children inlines
        for (int i = 0, l = children.size(); i < l; i++) {
            InlineArea inline = (InlineArea) children.get(i);
            renderInlineArea(inline);
        }
        currentIPPosition = saveIP + ip.getAllocIPD();
        currentBPPosition = saveBP;
    }

    /**
     * Render the given InlineBlockParent.
     * @param ibp the inline block parent to render
     */
    protected void renderInlineBlockParent(InlineBlockParent ibp) {
        int level = ibp.getBidiLevel();
        renderInlineAreaBackAndBorders(ibp);
        if ((level == -1) || ((level & 1) == 0)) {
            currentIPPosition += ibp.getBorderAndPaddingWidthStart();
        } else {
            currentIPPosition += ibp.getBorderAndPaddingWidthEnd();
        }
        // For inline content the BP position is updated by the enclosing line area
        int saveBP = currentBPPosition;
        currentBPPosition += ibp.getBlockProgressionOffset();
        renderBlock(ibp.getChildArea());
        currentBPPosition = saveBP;
    }

    /**
     * Render the given Viewport.
     * @param viewport the viewport to render
     */
    protected void renderInlineViewport(InlineViewport viewport) {
        Area content = viewport.getContent();
        int saveBP = currentBPPosition;
        currentBPPosition += viewport.getBlockProgressionOffset();
        Rectangle2D contpos = viewport.getContentPosition();
        if (content instanceof Image) {
            renderImage((Image) content, contpos);
        } else if (content instanceof Container) {
            renderContainer((Container) content);
        } else if (content instanceof ForeignObject) {
            renderForeignObject((ForeignObject) content, contpos);
        } else if (content instanceof InlineBlockParent) {
            renderInlineBlockParent((InlineBlockParent) content);
        }
        currentIPPosition += viewport.getAllocIPD();
        currentBPPosition = saveBP;
    }

    /**
     * Renders an image area.
     *
     * @param image  The image
     * @param pos    The target position of the image
     * (todo) Make renderImage() protected
     */
    public void renderImage(Image image, Rectangle2D pos) {
        // Default: do nothing.
        // Some renderers (ex. Text) don't support images.
    }

    /**
     * Tells the renderer to render an inline container.
     * @param cont  The inline container area
     */
    protected void renderContainer(Container cont) {
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        List blocks = cont.getBlocks();
        renderBlocks(null, blocks);
        currentIPPosition = saveIP;
        currentBPPosition = saveBP;
    }

    /**
     * Renders a foreign object area.
     *
     * @param fo   The foreign object area
     * @param pos  The target position of the foreign object
     * (todo) Make renderForeignObject() protected
     */
    protected void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        // Default: do nothing.
        // Some renderers (ex. Text) don't support foreign objects.
    }

    /**
     * Render the xml document with the given xml namespace.
     * The Render Context is by the handle to render into the current
     * rendering target.
     * @param ctx rendering context
     * @param doc DOM Document containing the source document
     * @param namespace Namespace URI of the document
     */
    public void renderXML(RendererContext ctx, Document doc,
                          String namespace) {
        XMLHandler handler = userAgent.getXMLHandlerRegistry().getXMLHandler(
                this, namespace);
        if (handler != null) {
            try {
                XMLHandlerConfigurator configurator
                    = new XMLHandlerConfigurator(userAgent);
                configurator.configure(ctx, namespace);
                handler.handleXML(ctx, doc, namespace);
            } catch (Exception e) {
                // could not handle document
                ResourceEventProducer eventProducer
                        = ResourceEventProducer.Provider.get(
                            ctx.getUserAgent().getEventBroadcaster());
                eventProducer.foreignXMLProcessingError(this, doc, namespace, e);
            }
        } else {
            if (warnedXMLHandlers == null) {
                warnedXMLHandlers = new java.util.HashSet();
            }
            if (!warnedXMLHandlers.contains(namespace)) {
                // no handler found for document
                warnedXMLHandlers.add(namespace);
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                        ctx.getUserAgent().getEventBroadcaster());
                eventProducer.foreignXMLNoHandler(this, doc, namespace);
            }
        }
    }

    /**
     * Converts a millipoint-based transformation matrix to points.
     * @param at a millipoint-based transformation matrix
     * @return a point-based transformation matrix
     */
    protected AffineTransform mptToPt(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        //Convert to points
        matrix[4] = matrix[4] / 1000;
        matrix[5] = matrix[5] / 1000;
        return new AffineTransform(matrix);
    }

    /**
     * Converts a point-based transformation matrix to millipoints.
     * @param at a point-based transformation matrix
     * @return a millipoint-based transformation matrix
     */
    protected AffineTransform ptToMpt(AffineTransform at) {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        //Convert to millipoints
        //Math.round() because things like this can happen: 65.6 * 1000 = 65.599999999999999
        //which is bad for testing
        matrix[4] = Math.round(matrix[4] * 1000);
        matrix[5] = Math.round(matrix[5] * 1000);
        return new AffineTransform(matrix);
    }
}
