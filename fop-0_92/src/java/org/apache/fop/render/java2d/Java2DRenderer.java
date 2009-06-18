/*
 * Copyright 1999-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.render.java2d;

// Java
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.XMLImage;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.pdf.CTMHelper;
import org.apache.fop.traits.BorderProps;
import org.w3c.dom.Document;

/**
 * The <code>Java2DRenderer</code> class provides the abstract technical
 * foundation for all rendering with the Java2D API. Renderers like
 * <code>AWTRenderer</code> subclass it and provide the concrete output paths.
 * <p>
 * A lot of the logic is performed by <code>AbstractRenderer</code>. The
 * class-variables <code>currentIPPosition</code> and
 * <code>currentBPPosition</code> hold the position of the currently rendered
 * area.
 * <p>
 * <code>Java2DGraphicsState state</code> holds the <code>Graphics2D</code>,
 * which is used along the whole rendering. <code>state</code> also acts as a
 * stack (<code>state.push()</code> and <code>state.pop()</code>).
 * <p>
 * The rendering process is basically always the same:
 * <p>
 * <code>void renderXXXXX(Area area) {
 *    //calculate the currentPosition
 *    state.updateFont(name, size, null);
 *    state.updateColor(ct, false, null);
 *    state.getGraph.draw(new Shape(args));
 * }</code>
 *
 */
public abstract class Java2DRenderer extends AbstractRenderer implements Printable {

    /** The scale factor for the image size, values: ]0 ; 1] */
    protected double scaleFactor = 1;

    /** The page width in pixels */
    protected int pageWidth = 0;

    /** The page height in pixels */
    protected int pageHeight = 0;

    /** List of Viewports */
    protected List pageViewportList = new java.util.ArrayList();

    /** The 0-based current page number */
    private int currentPageNumber = 0;

    /** The 0-based total number of rendered pages */
    private int numberOfPages;

    /** true if antialiasing is set */
    protected boolean antialiasing = true;

    /** true if qualityRendering is set */
    protected boolean qualityRendering = true;

    /** The current state, holds a Graphics2D and its context */
    protected Java2DGraphicsState state;

    /** a Line2D.Float used to draw text decorations and leaders */
    protected Line2D.Float line = new Line2D.Float();

    /** Font configuration */
    protected FontInfo fontInfo;

    protected Map fontNames = new java.util.Hashtable();

    protected Map fontStyles = new java.util.Hashtable();

    /** true if the renderer has finished rendering all the pages */
    public boolean renderingDone;

    /** Default constructor */
    public Java2DRenderer() {
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(org.apache.fop.apps.FOUserAgent)
     */
    public void setUserAgent(FOUserAgent foUserAgent) {
        super.setUserAgent(foUserAgent);
        userAgent.setRendererOverride(this); // for document regeneration
    }

    /** @return the FOUserAgent */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * @see org.apache.fop.render.Renderer#setupFontInfo(org.apache.fop.fonts.FontInfo)
     */
    public void setupFontInfo(FontInfo inFontInfo) {
        // create a temp Image to test font metrics on
        fontInfo = inFontInfo;
        BufferedImage fontImage = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);
        FontSetup.setup(fontInfo, fontImage.createGraphics());
    }

    /** @see org.apache.fop.render.Renderer#getGraphics2DAdapter() */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new Java2DGraphics2DAdapter(state);
    }

    /**
     * Sets the new scale factor.
     * @param newScaleFactor ]0 ; 1]
     */
    public void setScaleFactor(double newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void startRenderer(OutputStream out) throws IOException {
        // do nothing by default
    }

    public void stopRenderer() throws IOException {
        log.debug("Java2DRenderer stopped");
        renderingDone = true;
        numberOfPages = currentPageNumber;
        // TODO set all vars to null for gc
        if (numberOfPages == 0) {
            new FOPException("No page could be rendered");
        }
    }

    /**
     * @return The 0-based current page number
     */
    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    /**
     * @param c the 0-based current page number
     */
    public void setCurrentPageNumber(int c) {
        this.currentPageNumber = c;
    }

    /**
     * @return The 0-based total number of rendered pages
     */
    public int getNumberOfPages() {
            return numberOfPages;
    }

    /**
     * Clears the ViewportList.
     * Used if the document is reloaded.
     */
    public void clearViewportList() {
        pageViewportList.clear();
        setCurrentPageNumber(0);
    }

    /**
     * This method override only stores the PageViewport in a List. No actual
     * rendering is performed here. A renderer override renderPage() to get the
     * freshly produced PageViewport, and rendere them on the fly (producing the
     * desired BufferedImages by calling getPageImage(), which lazily starts the
     * rendering process).
     *
     * @param pageViewport the <code>PageViewport</code> object supplied by
     * the Area Tree
     * @see org.apache.fop.render.Renderer
     */
    public void renderPage(PageViewport pageViewport) 
                throws IOException, FOPException {
        // TODO clone
        pageViewportList.add(pageViewport.clone());
        currentPageNumber++;
    }

    /**
     * Generates a desired page from the renderer's page viewport list.
     *
     * @param pageViewport the PageViewport to be rendered
     * @return the <code>java.awt.image.BufferedImage</code> corresponding to
     * the page or null if the page doesn't exist.
     */
    public BufferedImage getPageImage(PageViewport pageViewport) {

        this.currentPageViewport = pageViewport;
        try {
            Rectangle2D bounds = pageViewport.getViewArea();
            pageWidth = (int) Math.round(bounds.getWidth() / 1000f);
            pageHeight = (int) Math.round(bounds.getHeight() / 1000f);

            log.info(
                    "Rendering Page " + pageViewport.getPageNumberString()
                            + " (pageWidth " + pageWidth + ", pageHeight "
                            + pageHeight + ")");

            double scaleX = scaleFactor 
                * (25.4 / FOUserAgent.DEFAULT_TARGET_RESOLUTION) 
                / userAgent.getTargetPixelUnitToMillimeter();
            double scaleY = scaleFactor
                * (25.4 / FOUserAgent.DEFAULT_TARGET_RESOLUTION)
                / userAgent.getTargetPixelUnitToMillimeter();
            int bitmapWidth = (int) ((pageWidth * scaleX) + 0.5);
            int bitmapHeight = (int) ((pageHeight * scaleY) + 0.5);
                    
            
            BufferedImage currentPageImage = new BufferedImage(
                    bitmapWidth, bitmapHeight, BufferedImage.TYPE_INT_ARGB);
            // FIXME TYPE_BYTE_BINARY ?

            Graphics2D graphics = currentPageImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            if (antialiasing) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            if (qualityRendering) {
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
            }

            // transform page based on scale factor supplied
            AffineTransform at = graphics.getTransform();
            at.scale(scaleX, scaleY);
            graphics.setTransform(at);

            // draw page frame
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, pageWidth, pageHeight);
            graphics.setColor(Color.black);
            graphics.drawRect(-1, -1, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(pageWidth + 2, 0, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(pageWidth + 3, 1, pageWidth + 3, pageHeight + 3);
            graphics.drawLine(0, pageHeight + 2, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(1, pageHeight + 3, pageWidth + 3, pageHeight + 3);

            state = new Java2DGraphicsState(graphics, this.fontInfo, at);

            // reset the current Positions
            currentBPPosition = 0;
            currentIPPosition = 0;

            // this toggles the rendering of all areas
            renderPageAreas(pageViewport.getPage());
            return currentPageImage;
        } finally {
            this.currentPageViewport = null;
        }
    }

        
    /**
     * Returns the page viewport
     * @param pageNum the page number
     * @exception FOPException If the page is out of range.
     */
    public PageViewport getPageViewport(int pageNum) throws FOPException {
        if (pageNum < 0 || pageNum >= pageViewportList.size()) {
            throw new FOPException("Requested page number is out of range: " + pageNum
                     + "; only " + pageViewportList.size()
                     + " page(s) available.");
        }
        return (PageViewport) pageViewportList.get(pageNum);
    }

    /**
     * Generates a desired page from the renderer's page viewport list.
     *
     * @param pageNum the 0-based page number to generate
     * @return the <code>java.awt.image.BufferedImage</code> corresponding to
     * the page or null if the page doesn't exist.
     * @throws FOPException
     */
    public BufferedImage getPageImage(int pageNum) throws FOPException {
        return getPageImage(getPageViewport(pageNum));
    }

    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param region the RegionViewport whose region is to be drawn
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    protected void handleRegionTraits(RegionViewport region) {
        Rectangle2D viewArea = region.getViewArea();
        float startx = (float)(viewArea.getX() / 1000f);
        float starty = (float)(viewArea.getY() / 1000f);
        float width = (float)(viewArea.getWidth() / 1000f);
        float height = (float)(viewArea.getHeight() / 1000f);

        if (region.getRegionReference().getRegionClass() == FO_REGION_BODY) {
            currentBPPosition = region.getBorderAndPaddingWidthBefore();
            currentIPPosition = region.getBorderAndPaddingWidthStart();
        }
        drawBackAndBorders(region, startx, starty, width, height);
    }

    /**
     * Render an inline viewport.
     * This renders an inline viewport by clipping if necessary.
     * @param viewport the viewport to handle
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    public void renderViewport(Viewport viewport) {

        float x = currentIPPosition / 1000f;
        float y = (currentBPPosition + viewport.getOffset()) / 1000f;
        float width = viewport.getIPD() / 1000f;
        float height = viewport.getBPD() / 1000f;
        // TODO: Calculate the border rect correctly. 
        float borderPaddingStart = viewport.getBorderAndPaddingWidthStart() / 1000f;
        float borderPaddingBefore = viewport.getBorderAndPaddingWidthBefore() / 1000f;
        float bpwidth = borderPaddingStart 
                + (viewport.getBorderAndPaddingWidthEnd() / 1000f);
        float bpheight = borderPaddingBefore
                + (viewport.getBorderAndPaddingWidthAfter() / 1000f);

        drawBackAndBorders(viewport, x, y, width + bpwidth, height + bpheight);

        if (viewport.getClip()) {
            saveGraphicsState();

            clipRect(x + borderPaddingStart, y + borderPaddingBefore, width, height);
        }
        super.renderViewport(viewport);

        if (viewport.getClip()) {
            restoreGraphicsState();
        }
    }

    /** Saves the graphics state of the rendering engine. */
    protected void saveGraphicsState() {
        // push (and save) the current graphics state
        state.push();
    }

    /** Restores the last graphics state of the rendering engine. */
    protected void restoreGraphicsState() {
        state.pop();
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {

        saveGraphicsState();

        if (clippingRect != null) {
            clipRect((float)clippingRect.getX() / 1000f, 
                    (float)clippingRect.getY() / 1000f, 
                    (float)clippingRect.getWidth() / 1000f, 
                    (float)clippingRect.getHeight() / 1000f);
        }

        // Set the given CTM in the graphics state
        //state.setTransform(new AffineTransform(CTMHelper.toPDFArray(ctm)));
        state.transform(new AffineTransform(CTMHelper.toPDFArray(ctm)));
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        restoreGraphicsState();
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    protected void renderInlineSpace(Space space) {
        space.setBPD(0);
        renderInlineAreaBackAndBorders(space);
        super.renderInlineSpace(space);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    protected void renderInlineParent(InlineParent ip) {
        renderInlineAreaBackAndBorders(ip);
        super.renderInlineParent(ip);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    protected void renderInlineBlockParent(InlineBlockParent ibp) {
        renderInlineAreaBackAndBorders(ibp);
        super.renderInlineBlockParent(ibp);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBlockViewport(BlockViewport,
     * List)
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();
        float x, y;
        x = (float) (bv.getXOffset() + containingIPPosition) / 1000f;
        y = (float) (bv.getYOffset() + containingBPPosition) / 1000f;
        // This is the content-rect
        float width = (float) bv.getIPD() / 1000f;
        float height = (float) bv.getBPD() / 1000f;


        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            currentIPPosition = bv.getXOffset();
            currentBPPosition = bv.getYOffset();
            
            // TODO not tested yet
            // For FIXED, we need to break out of the current viewports to the
            // one established by the page. We save the state stack for
            // restoration
            // after the block-container has been painted. See below.
            List breakOutList = null;
            if (bv.getPositioning() == Block.FIXED) {
                log.debug("Block.FIXED --> break out");
                breakOutList = new java.util.ArrayList();
                Graphics2D graph;
                while (true) {
                    graph = state.getGraph();
                    if (state.pop() == null) {
                        break;
                    }
                    breakOutList.add(0, graph); // Insert because of
                    // stack-popping
                    log.debug("Adding to break out list: " + graph);
                }
            }

            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);

            // Adjust for spaces (from margin or indirectly by start-indent etc.
            x += bv.getSpaceStart() / 1000f;
            currentIPPosition += bv.getSpaceStart();
            
            y += bv.getSpaceBefore() / 1000f;
            currentBPPosition += bv.getSpaceBefore(); 

            float bpwidth = (borderPaddingStart + bv
                    .getBorderAndPaddingWidthEnd()) / 1000f;
            float bpheight = (borderPaddingBefore + bv
                    .getBorderAndPaddingWidthAfter()) / 1000f;

            drawBackAndBorders(bv, x, y, width + bpwidth, height + bpheight);

            // Now adjust for border/padding
            currentIPPosition += borderPaddingStart;
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

            if (breakOutList != null) {
                log.debug(
                        "Block.FIXED --> restoring context after break-out");
                Graphics2D graph;
                Iterator i = breakOutList.iterator();
                while (i.hasNext()) {
                    graph = (Graphics2D) i.next();
                    log.debug("Restoring: " + graph);
                    state.push();
                }
            }

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;

        } else { // orientation = Block.STACK or RELATIVE

            currentBPPosition += bv.getSpaceBefore();

            // borders and background in the old coordinate system
            handleBlockTraits(bv);

            //Advance to start of content area
            currentIPPosition += bv.getStartIndent();

            CTM tempctm = new CTM(containingIPPosition, currentBPPosition
                    + containingBPPosition);
            ctm = tempctm.multiply(ctm);

            // Now adjust for border/padding
            x += borderPaddingStart / 1000f;
            y += borderPaddingBefore / 1000f;

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
    }

    /**
     * Clip an area. write a clipping operation given coordinates in the current
     * transform. Coordinates are in points.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void clipRect(float x, float y, float width, float height) {
        Rectangle2D rect = new Rectangle2D.Float(x, y, width, height);
        state.updateClip(rect);
    }

    /**
     * Draw the background and borders. This draws the background and border
     * traits for an area given the position.
     *
     * @param area the area whose traits are used
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area area, float startx, float starty,
            float width, float height) {

        BorderProps bpsBefore = (BorderProps) area
                .getTrait(Trait.BORDER_BEFORE);
        BorderProps bpsAfter = (BorderProps) area.getTrait(Trait.BORDER_AFTER);
        BorderProps bpsStart = (BorderProps) area.getTrait(Trait.BORDER_START);
        BorderProps bpsEnd = (BorderProps) area.getTrait(Trait.BORDER_END);

        // draw background
        Trait.Background back;
        back = (Trait.Background) area.getTrait(Trait.BACKGROUND);
        if (back != null) {

            // Calculate padding rectangle
            float sx = startx;
            float sy = starty;
            float paddRectWidth = width;
            float paddRectHeight = height;

            if (bpsStart != null) {
                sx += bpsStart.width / 1000f;
                paddRectWidth -= bpsStart.width / 1000f;
            }
            if (bpsBefore != null) {
                sy += bpsBefore.width / 1000f;
                paddRectHeight -= bpsBefore.width / 1000f;
            }
            if (bpsEnd != null) {
                paddRectWidth -= bpsEnd.width / 1000f;
            }
            if (bpsAfter != null) {
                paddRectHeight -= bpsAfter.width / 1000f;
            }

            if (back.getColor() != null) {
                drawBackground(back, sx, sy, paddRectWidth, paddRectHeight);
            }

            // background image
            if (back.getFopImage() != null) {
                FopImage fopimage = back.getFopImage();
                if (fopimage != null && fopimage.load(FopImage.DIMENSIONS)) {
                    saveGraphicsState();
                    clipRect(sx, sy, paddRectWidth, paddRectHeight);
                    int horzCount = (int) ((paddRectWidth * 1000 / fopimage
                            .getIntrinsicWidth()) + 1.0f);
                    int vertCount = (int) ((paddRectHeight * 1000 / fopimage
                            .getIntrinsicHeight()) + 1.0f);
                    if (back.getRepeat() == EN_NOREPEAT) {
                        horzCount = 1;
                        vertCount = 1;
                    } else if (back.getRepeat() == EN_REPEATX) {
                        vertCount = 1;
                    } else if (back.getRepeat() == EN_REPEATY) {
                        horzCount = 1;
                    }
                    // change from points to millipoints
                    sx *= 1000;
                    sy *= 1000;
                    if (horzCount == 1) {
                        sx += back.getHoriz();
                    }
                    if (vertCount == 1) {
                        sy += back.getVertical();
                    }
                    for (int x = 0; x < horzCount; x++) {
                        for (int y = 0; y < vertCount; y++) {
                            // place once
                            Rectangle2D pos;
                            pos = new Rectangle2D.Float(sx
                                    + (x * fopimage.getIntrinsicWidth()), sy
                                    + (y * fopimage.getIntrinsicHeight()),
                                    fopimage.getIntrinsicWidth(), fopimage
                                            .getIntrinsicHeight());
                            putImage(back.getURL(), pos); // TODO test
                        }
                    }
                    restoreGraphicsState();
                } else {
                    log.warn(
                            "Can't find background image: " + back.getURL());
                }
            }
        }

        // draw border
        // BORDER_BEFORE
        if (bpsBefore != null) {
            int borderWidth = (int) Math.round((bpsBefore.width / 1000f));
            state.updateColor(bpsBefore.color);
            state.getGraph().fillRect((int) startx, (int) starty, (int) width,
                    borderWidth);
        }
        // BORDER_AFTER
        if (bpsAfter != null) {
            int borderWidth = (int) Math.round((bpsAfter.width / 1000f));
            float sy = starty + height;
            state.updateColor(bpsAfter.color);
            state.getGraph().fillRect((int) startx,
                    (int) (starty + height - borderWidth), (int) width,
                    borderWidth);
        }
        // BORDER_START
        if (bpsStart != null) {
            int borderWidth = (int) Math.round((bpsStart.width / 1000f));
            state.updateColor(bpsStart.color);
            state.getGraph().fillRect((int) startx, (int) starty, borderWidth,
                    (int) height);
        }
        // BORDER_END
        if (bpsEnd != null) {
            int borderWidth = (int) Math.round((bpsEnd.width / 1000f));
            float sx = startx + width;
            state.updateColor(bpsEnd.color);
            state.getGraph().fillRect((int) (startx + width - borderWidth),
                    (int) starty, borderWidth, (int) height);
        }
    }

    /**
     * Draw the Background Rectangle of a given area.
     *
     * @param back the Trait.Background
     * @param sx x coordinate of the rectangle to be filled.
     * @param sy y the y coordinate of the rectangle to be filled.
     * @param paddRectWidth the width of the rectangle to be filled.
     * @param paddRectHeight the height of the rectangle to be filled.
     */
    protected void drawBackground(Trait.Background back, float sx, float sy,
            float paddRectWidth, float paddRectHeight) {

        state.updateColor(back.getColor());
        state.getGraph().fillRect((int) sx, (int) sy, (int) paddRectWidth,
                (int) paddRectHeight);
    }
    
    /** 
     * Common method to render the background and borders for any inline area.
     * The all borders and padding are drawn outside the specified area.
     * @param area the inline area for which the background, border and padding is to be
     * rendered
     * @TODO This is a copy from AbstractPathOrientedRenderer. Put this method in AbstractRenderer
     */
    protected void renderInlineAreaBackAndBorders(InlineArea area) {
        float x = currentIPPosition / 1000f;
        float y = (currentBPPosition + area.getOffset()) / 1000f;
        float width = area.getIPD() / 1000f;
        float height = area.getBPD() / 1000f;
        float borderPaddingStart = area.getBorderAndPaddingWidthStart() / 1000f;
        float borderPaddingBefore = area.getBorderAndPaddingWidthBefore() / 1000f;
        float bpwidth = borderPaddingStart 
                + (area.getBorderAndPaddingWidthEnd() / 1000f);
        float bpheight = borderPaddingBefore
                + (area.getBorderAndPaddingWidthAfter() / 1000f);
        
        if (height != 0.0f || bpheight != 0.0f && bpwidth != 0.0f) {
            drawBackAndBorders(area, x, y - borderPaddingBefore
                                , width + bpwidth
                                , height + bpheight);
        }
        
    }

    /**
     * Handle block traits. The block could be any sort of block with any
     * positioning so this should render the traits such as border and
     * background in its position.
     *
     * @param block the block to render the traits
     */
    protected void handleBlockTraits(Block block) {
        // copied from pdf
        int borderPaddingStart = block.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = block.getBorderAndPaddingWidthBefore();

        float startx = currentIPPosition / 1000f;
        float starty = currentBPPosition / 1000f;
        float width = block.getIPD() / 1000f;
        float height = block.getBPD() / 1000f;

        startx += block.getStartIndent() / 1000f;
        startx -= block.getBorderAndPaddingWidthStart() / 1000f;
        width += borderPaddingStart / 1000f;
        width += block.getBorderAndPaddingWidthEnd() / 1000f;
        height += borderPaddingBefore / 1000f;
        height += block.getBorderAndPaddingWidthAfter() / 1000f;

        drawBackAndBorders(block, startx, starty, width, height);
    }

    /**
     * Returns a Font object constructed based on the font traits in an area
     * @param area the area from which to retrieve the font triplet information
     * @return the requested Font instance or null if not found
     */
    protected Font getFontFromArea(Area area) {
        FontTriplet triplet = (FontTriplet)area.getTrait(Trait.FONT);
        int size = ((Integer)area.getTrait(Trait.FONT_SIZE)).intValue();
        return fontInfo.getFontInstance(triplet, size);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);

        float x = currentIPPosition + text.getBorderAndPaddingWidthStart();
        float y = currentBPPosition + text.getOffset() + text.getBaselineOffset(); // baseline

        Font font = getFontFromArea(text);
        state.updateFont(font.getFontName(), font.getFontSize(), null);

        ColorType ct = (ColorType) text.getTrait(Trait.COLOR);
        state.updateColor(ct, false, null);

        String s = text.getText();
        state.getGraph().drawString(s, x / 1000f, y / 1000f);

        // getLogger().debug("renderText(): \"" + s + "\", x: "
        // + x + ", y: " + y + state);

        // rendering text decorations

        super.renderText(text);

        renderTextDecoration(font, text, y, x);
    }

    /**
     * Paints the text decoration marks.
     *
     * @param fs Current font
     * @param inline inline area to paint the marks for
     * @param baseline position of the baseline
     * @param startIPD start IPD
     */
    protected void renderTextDecoration(Font fs, InlineArea inline,
            float baseline, float startIPD) {

        boolean hasTextDeco = inline.hasUnderline() || inline.hasOverline()
                || inline.hasLineThrough();

        if (hasTextDeco) {
            state.updateStroke((fs.getDescender() / (-8 * 1000f)),
                    Constants.EN_SOLID);
            float endIPD = startIPD + inline.getIPD();
            if (inline.hasUnderline()) {
                ColorType ct = (ColorType) inline
                        .getTrait(Trait.UNDERLINE_COLOR);
                state.updateColor(ct, false, null);
                float y = baseline - fs.getDescender() / 2;
                line.setLine(startIPD / 1000f, y / 1000f, endIPD / 1000f,
                        y / 1000f);
                state.getGraph().draw(line);
            }
            if (inline.hasOverline()) {
                ColorType ct = (ColorType) inline
                        .getTrait(Trait.OVERLINE_COLOR);
                state.updateColor(ct, false, null);
                float y = (float) (baseline - (1.1 * fs.getCapHeight()));
                line.setLine(startIPD / 1000f, y / 1000f, endIPD / 1000f,
                        y / 1000f);
                state.getGraph().draw(line);
            }
            if (inline.hasLineThrough()) {
                ColorType ct = (ColorType) inline
                        .getTrait(Trait.LINETHROUGH_COLOR);
                state.updateColor(ct, false, null);
                float y = (float) (baseline - (0.45 * fs.getCapHeight()));
                line.setLine(startIPD / 1000f, y / 1000f, endIPD / 1000f,
                        y / 1000f);
                state.getGraph().draw(line);
            }
        }
    }

    /**
     * Render leader area. This renders a leader area which is an area with a
     * rule.
     *
     * @param area the leader area to render
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        // TODO leader-length: 25%, 50%, 75%, 100% not working yet
        // TODO Colors do not work on Leaders yet

        float startx = (currentIPPosition + area.getBorderAndPaddingWidthStart()) / 1000f;
        float starty = ((currentBPPosition + area.getOffset()) / 1000f);
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart() 
                + area.getIPD()) / 1000f;

        ColorType ct = (ColorType) area.getTrait(Trait.COLOR);
        state.updateColor(ct, true, null);

        line.setLine(startx, starty, endx, starty);
        float thickness = area.getRuleThickness() / 1000f;

        int style = area.getRuleStyle();
        switch (style) {
        case EN_SOLID:
        case EN_DOTTED:
        case EN_DASHED:
            state.updateStroke(thickness, style);
            state.getGraph().draw(line);
            break;
        case EN_DOUBLE:

            state.updateStroke(thickness / 3f, EN_SOLID); // only a third

            // upper Leader
            line.setLine(startx, starty, endx, starty);
            state.getGraph().draw(line);
            // lower Leader
            line.setLine(startx, starty + 2 * thickness, endx, starty + 2
                    * thickness);
            state.getGraph().draw(line);

            break;

        case EN_GROOVE:
            // The rule looks as though it were carved into the canvas.
            // (Top/left half of the rule's thickness is the
            // color specified; the other half is white.)

            state.updateStroke(thickness / 2f, EN_SOLID); // only the half

            // upper Leader
            line.setLine(startx, starty, endx, starty);
            state.getGraph().draw(line);
            // lower Leader
            line.setLine(startx, starty + thickness, endx, starty + thickness);
            state.getGraph().setColor(Color.white);
            state.getGraph().draw(line);

            // TODO the implementation could be nicer, f.eg. with triangles at
            // the tip of the lines. See also RenderX's implementation (looks
            // like a button)

            break;

        case EN_RIDGE:
            // The opposite of "groove", the rule looks as though it were
            // coming out of the canvas. (Bottom/right half of the rule's
            // thickness is the color specified; the other half is white.)

            state.updateStroke(thickness / 2f, EN_SOLID); // only the half

            // lower Leader
            line.setLine(startx, starty + thickness, endx, starty + thickness);
            state.getGraph().draw(line);
            // upperLeader
            line.setLine(startx, starty, endx, starty);
            state.getGraph().setColor(Color.white);
            state.getGraph().draw(line);

            // TODO the implementation could be nicer, f.eg. with triangles at
            // the tip of the lines. See also RenderX's implementation (looks
            // like a button)

            break;

        case EN_NONE:
            // No rule is drawn
            break;

        } // end switch

        super.renderLeader(area);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderImage(Image,
     * Rectangle2D)
     */
    public void renderImage(Image image, Rectangle2D pos) {
        // endTextObject();
        String url = image.getURL();
        putImage(url, pos);
    }

    /**
     * Draws an image
     *
     * @param pUrl URL of the bitmap
     * @param pos Position of the bitmap
     */
    protected void putImage(String pUrl, Rectangle2D pos) {

        int x = currentIPPosition; // TODO + area.getXOffset();
        int y = currentBPPosition;
        String url = ImageFactory.getURL(pUrl);

        ImageFactory fact = userAgent.getFactory().getImageFactory();
        FopImage fopimage = fact.getImage(url, userAgent);

        if (fopimage == null) {
            return;
        }
        if (!fopimage.load(FopImage.DIMENSIONS)) {
            return;
        }
        int w = fopimage.getWidth();
        int h = fopimage.getHeight();
        String mime = fopimage.getMimeType();
        if ("text/xml".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            Document doc = ((XMLImage) fopimage).getDocument();
            String ns = ((XMLImage) fopimage).getNameSpace();
            renderDocument(doc, ns, pos);

        } else if ("image/svg+xml".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            Document doc = ((XMLImage) fopimage).getDocument();
            String ns = ((XMLImage) fopimage).getNameSpace();

            renderDocument(doc, ns, pos);
        } else if ("image/eps".equals(mime)) {
            log.warn("EPS images are not supported by this renderer");
        } else if ("image/jpeg".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }

            // TODO Load JPEGs rather through fopimage.load(FopImage.BITMAP),
            // but JpegImage will need to be extended for that

            // url = url.substring(7);
            // url = "C:/eclipse/myWorkbenches/fop4/xml-fop/examples/fo" + url;
            java.awt.Image awtImage = new javax.swing.ImageIcon(url).getImage();

            state.getGraph().drawImage(awtImage, 
                    (int)(x / 1000f), (int)(y / 1000f), 
                    (int)(pos.getWidth() / 1000f), (int)(pos.getHeight() / 1000f), null);
        } else {
            if (!fopimage.load(FopImage.BITMAP)) {
                log.warn("Loading of bitmap failed: " + url);
                return;
            }

            byte[] raw = fopimage.getBitmaps();

            // TODO Hardcoded color and sample models, FIX ME!
            ColorModel cm = new ComponentColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), 
                    new int[] {8, 8, 8},
                    false, false,
                    ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            SampleModel sampleModel = new PixelInterleavedSampleModel(
                    DataBuffer.TYPE_BYTE, w, h, 3, w * 3, new int[] { 0, 1, 2 });
            DataBuffer dbuf = new DataBufferByte(raw, w * h * 3);

            WritableRaster raster = Raster.createWritableRaster(sampleModel,
                    dbuf, null);

            java.awt.Image awtImage;
            // Combine the color model and raster into a buffered image
            awtImage = new BufferedImage(cm, raster, false, null);

            state.getGraph().drawImage(awtImage, 
                    (int)(x / 1000f), (int)(y / 1000f), 
                    (int)(pos.getWidth() / 1000f), (int)(pos.getHeight() / 1000f), null);
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject,
     * Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos);
    }

    /**
     * Renders an XML document (SVG for example).
     *
     * @param doc DOM document representing the XML document
     * @param ns Namespace for the document
     * @param pos Position on the page
     */
    public void renderDocument(Document doc, String ns, Rectangle2D pos) {
        RendererContext context;
        context = new RendererContext(this, getMimeType());
        context.setUserAgent(userAgent);

        context.setProperty(Java2DRendererContextConstants.JAVA2D_STATE, state);
        context.setProperty(Java2DRendererContextConstants.XPOS,
                            new Integer(currentIPPosition + (int)pos.getX()));
        context.setProperty(Java2DRendererContextConstants.YPOS,
                            new Integer(currentBPPosition + (int)pos.getY()));
        context.setProperty(Java2DRendererContextConstants.WIDTH,
                            new Integer((int)pos.getWidth()));
        context.setProperty(Java2DRendererContextConstants.HEIGHT,
                            new Integer((int) pos.getHeight()));
        context.setProperty(Java2DRendererContextConstants.PAGE_VIEWPORT, 
                            getCurrentPageViewport());
        
        renderXML(context, doc, ns);
    }

    /**
     * @see java.awt.print.Printable#print(java.awt.Graphics,
     * java.awt.print.PageFormat, int)
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        if (pageIndex >= getNumberOfPages()) {
            return NO_SUCH_PAGE;
        }

        Graphics2D graphics = (Graphics2D) g;
        Java2DGraphicsState oldState = state;
        BufferedImage image;
        try {
          PageViewport viewport = getPageViewport(pageIndex);
          AffineTransform at = graphics.getTransform();
          state = new Java2DGraphicsState(graphics, this.fontInfo, at);

          // reset the current Positions
          currentBPPosition = 0;
          currentIPPosition = 0;

          renderPageAreas(viewport.getPage());
          return PAGE_EXISTS;
        } catch (FOPException e) {
            log.error(e);
            return NO_SUCH_PAGE;
        } finally {
          oldState = state;
        }
    }
}
