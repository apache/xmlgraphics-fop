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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
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
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageSequence;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
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

/**
 * This renderer implementation is an adapter to the {@code IFPainter} interface. It is used
 * to generate content using FOP's intermediate format.
 */
public class IFRenderer extends AbstractPathOrientedRenderer {

    /** logging instance */
    protected static Log log = LogFactory.getLog(IFRenderer.class);

    /** XML MIME type */
    public static final String IF_MIME_TYPE = MimeConstants.MIME_FOP_IF;

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
     * Main constructor
     */
    public IFRenderer() {
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return IF_MIME_TYPE;
    }

    /**
     * Sets the {@code IFPainter} to be used by the {@code IFRenderer}.
     * @param painter the {@code IFPainter}
     */
    public void setPainter(IFPainter painter) {
        this.painter = painter;
    }

    /**
     * Call this method to make the XMLRenderer mimic a different renderer by using its font
     * setup. This is useful when working with the intermediate format parser.
     * @param renderer the renderer to mimic
     */
    public void mimicRenderer(Renderer renderer) {
        this.mimic = renderer;
    }

    /** {@inheritDoc} */
    public void setupFontInfo(FontInfo inFontInfo) {
        if (mimic != null) {
            mimic.setupFontInfo(inFontInfo);
            this.fontInfo = inFontInfo;
        } else {
            super.setupFontInfo(inFontInfo);
        }
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

    /**
     * Creates a default {@code IFPainter} when none has been set.
     * @return the default IFPainter
     */
    protected IFPainter createDefaultPainter() {
        return new IFSerializer();
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
                if (this.painter == null) {
                    this.painter = new IFSerializer();
                    this.painter.setUserAgent(getUserAgent());
                }
                this.painter.setFontInfo(fontInfo);
                this.painter.setResult(result);
            }
            super.startRenderer(null);
            if (log.isDebugEnabled()) {
                log.debug("Rendering areas via painter ("
                        + this.painter.getClass().getName() + ")...");
            }
            painter.startDocument();
            painter.startDocumentHeader();
        } catch (IFException e) {
            handleIFExceptionWithIOException(e);
        }
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        try {
            if (this.inPageSequence) {
                painter.endPageSequence();
                this.inPageSequence = false;
            }
            painter.endDocument();
        } catch (IFException e) {
            handleIFExceptionWithIOException(e);
        }
        super.stopRenderer();
        log.debug("Rendering finished.");
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem odi) {
        if (odi instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)odi).getAttachment();
            if (XMPMetadata.CATEGORY.equals(attachment.getCategory())) {
                renderXMPMetadata((XMPMetadata)attachment);
            }
        }
    }

    private void renderXMPMetadata(XMPMetadata metadata) {
        this.documentMetadata = metadata.getMetadata();
    }

    /** {@inheritDoc} */
    public void startPageSequence(PageSequence pageSequence) {
        try {
            if (this.inPageSequence) {
                painter.endPageSequence();
            } else {
                if (this.documentMetadata == null) {
                    this.documentMetadata = createDefaultDocumentMetadata();
                }
                painter.handleExtensionObject(this.documentMetadata);
                painter.endDocumentHeader();
                this.inPageSequence = true;
            }
            //TODO Put the page-sequence's ID in the area tree
            painter.startPageSequence(null);
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
            painter.startPage(page.getPageIndex(), page.getPageNumberString(), dim);
            painter.startPageHeader();
            //TODO Handle page header
            painter.endPageHeader();
            painter.startPageContent();
            super.renderPage(page);
            painter.endPageContent();
            painter.startPageTrailer();
            //TODO Handle page trailer
            painter.endPageTrailer();
            painter.endPage();
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
            concatenateTransformationMatrixMpt(ptToMpt(at));
        }
    }

    private void concatenateTransformationMatrixMpt(AffineTransform at) {
        if (!at.isIdentity()) {
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
            concatenateTransformationMatrixMpt(positionTransform);

            //Background and borders
            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd());
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter());
            drawBackAndBorders(bv, 0, 0,
                    (dim.width + bpwidth) / 1000f, (dim.height + bpheight) / 1000f);

            //Shift to content rectangle after border painting
            AffineTransform contentRectTransform = new AffineTransform();
            contentRectTransform.translate(borderPaddingStart, borderPaddingBefore);
            concatenateTransformationMatrixMpt(contentRectTransform);

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

    /*
    protected void renderReferenceArea(Block block) {
        // TODO Auto-generated method stub
    }*/

    /** {@inheritDoc} */
    protected void renderBlock(Block block) {
        if (log.isTraceEnabled()) {
            log.trace("renderBlock() " + block);
        }
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

        super.renderText(text);

        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();
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
            int tws = -((TextArea) space.getParentArea()).getTextWordSpaceAdjust()
                         - 2 * textArea.getTextLetterSpaceAdjust();
            this.currentIPPosition -= tws;
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
        int curX = currentIPPosition;
        float fontSize = font.getFontSize() / 1000f;

        int l = s.length();

        int[] dx = new int[l];
        boolean hasDX = false;
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            float glyphAdjust = 0;
            if (font.hasChar(ch)) {
                int tls = (i < l - 1 ? parentArea.getTextLetterSpaceAdjust() : 0);
                glyphAdjust -= tls;
            }
            curX += font.getCharWidth(ch);
            if (letterAdjust != null && i < l) {
                glyphAdjust -= letterAdjust[i];
            }

            float adjust = glyphAdjust / fontSize;

            if (adjust != 0) {
                dx[i] = Math.round(adjust * -10);
                if (dx[i] != 0) {
                    hasDX = true;
                }
            }
            curX += adjust;
        }
        try {
            int rx = currentIPPosition + parentArea.getBorderAndPaddingWidthStart();
            int bl = currentBPPosition + parentArea.getOffset() + parentArea.getBaselineOffset();
            painter.drawText(rx, bl, (hasDX ? dx : null), null, s);
        } catch (IFException e) {
            handleIFException(e);
        }
        this.currentIPPosition = curX;
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
    protected void clip() {
        // TODO Auto-generated method stub
        log.warn("clip() NYI");
    }

    /** {@inheritDoc} */
    protected void clipRect(float x, float y, float width, float height) {
        // TODO Auto-generated method stub
        log.warn("clipRect() NYI");
    }

    /** {@inheritDoc} */
    protected void closePath() {
        // TODO Auto-generated method stub
        log.warn("closePath() NYI");
    }

    /** {@inheritDoc} */
    protected void drawBorderLine(float x1, float y1, float x2, float y2, boolean horz,
            boolean startOrBefore, int style, Color col) {
        // TODO Auto-generated method stub
        //log.warn("drawBorderLine() NYI");
        updateColor(col, true);
        fillRect(x1, y1, x2 - x1, y2 - y1);
    }

    private Rectangle toMillipointRectangle(float x, float y, float width, float height) {
        return new Rectangle(
                (int)(x * 1000), (int)(y * 1000), (int)(width * 1000), (int)(height * 1000));
    }

    /** {@inheritDoc} */
    protected void fillRect(float x, float y, float width, float height) {
        try {
            painter.drawRect(
                    toMillipointRectangle(x, y, width, height),
                    this.graphicContext.getPaint(), null);
        } catch (IFException e) {
            handleIFException(e);
        }
    }

    /** {@inheritDoc} */
    protected void moveTo(float x, float y) {
        // TODO Auto-generated method stub
        log.warn("moveTo() NYI");
    }

    /** {@inheritDoc} */
    protected void lineTo(float x, float y) {
        // TODO Auto-generated method stub
        log.warn("lineTo() NYI");
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
