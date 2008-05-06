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

package org.apache.fop.render.java2d;

// Java
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactoryConfigurator;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.pdf.CTMHelper;
import org.apache.fop.util.CharUtilities;

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
public abstract class Java2DRenderer extends AbstractPathOrientedRenderer implements Printable {

    /** Rendering Options key for the controlling the transparent page background option. */
    public static final String JAVA2D_TRANSPARENT_PAGE_BACKGROUND = "transparent-page-background";

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

    /** true if antialiasing is set */
    protected boolean antialiasing = true;

    /** true if qualityRendering is set */
    protected boolean qualityRendering = true;

    /** false: paints a non-transparent white background, true: for a transparent background */
    protected boolean transparentPageBackground = false;
    
    /** The current state, holds a Graphics2D and its context */
    protected Java2DGraphicsState state;
    
    private Stack stateStack = new Stack();

    /** true if the renderer has finished rendering all the pages */
    private boolean renderingDone;

    private GeneralPath currentPath = null;
    
    /** Default constructor */
    public Java2DRenderer() {
    }

    /**
     * {@inheritDoc}
     */
    public void setUserAgent(FOUserAgent foUserAgent) {
        super.setUserAgent(foUserAgent);
        userAgent.setRendererOverride(this); // for document regeneration
        
        String s = (String)userAgent.getRendererOptions().get(JAVA2D_TRANSPARENT_PAGE_BACKGROUND);
        if (s != null) {
            this.transparentPageBackground = "true".equalsIgnoreCase(s);
        }
    }

    /** @return the FOUserAgent */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /**
     * {@inheritDoc}
     */
    public void setupFontInfo(FontInfo inFontInfo) {
        //Don't call super.setupFontInfo() here! Java2D needs a special font setup
        // create a temp Image to test font metrics on
        this.fontInfo = inFontInfo;
        BufferedImage fontImage = new BufferedImage(100, 100,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = fontImage.createGraphics();
        //The next line is important to get accurate font metrics!
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, 
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        userAgent.getFactory().getFontManager().setupRenderer(this, graphics2D);
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new Java2DGraphics2DAdapter();
    }

    /**
     * Sets the new scale factor.
     * @param newScaleFactor ]0 ; 1]
     */
    public void setScaleFactor(double newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    /** @return the scale factor */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream out) throws IOException {
        super.startRenderer(out);
        // do nothing by default
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        log.debug("Java2DRenderer stopped");
        renderingDone = true;
        int numberOfPages = currentPageNumber;
        // TODO set all vars to null for gc
        if (numberOfPages == 0) {
            new FOPException("No page could be rendered");
        }
    }

    /** @return true if the renderer is not currently processing */
    public boolean isRenderingDone() {
        return this.renderingDone;
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
     * Returns the number of pages available. This method is also part of the Pageable interface.
     * @return The 0-based total number of rendered pages
     * @see java.awt.print.Pageable
     */
    public int getNumberOfPages() {
        return pageViewportList.size();
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
     * freshly produced PageViewport, and render them on the fly (producing the
     * desired BufferedImages by calling getPageImage(), which lazily starts the
     * rendering process).
     *
     * @param pageViewport the <code>PageViewport</code> object supplied by
     * the Area Tree
     * @throws IOException In case of an I/O error
     * @see org.apache.fop.render.Renderer
     */
    public void renderPage(PageViewport pageViewport) throws IOException {
        rememberPage((PageViewport)pageViewport.clone());
        //The clone() call is necessary as we store the page for later. Otherwise, the
        //RenderPagesModel calls PageViewport.clear() to release memory as early as possible.
        currentPageNumber++;
    }

    /**
     * Stores the pageViewport in a list of page viewports so they can be rendered later.
     * Subclasses can override this method to filter pages, for example.
     * @param pageViewport the page viewport
     */
    protected void rememberPage(PageViewport pageViewport) {
        assert pageViewport.getPageIndex() >= 0;
        pageViewportList.add(pageViewport);
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
                * (25.4 / FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION) 
                / userAgent.getTargetPixelUnitToMillimeter();
            double scaleY = scaleFactor
                * (25.4 / FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION)
                / userAgent.getTargetPixelUnitToMillimeter();
            int bitmapWidth = (int) ((pageWidth * scaleX) + 0.5);
            int bitmapHeight = (int) ((pageHeight * scaleY) + 0.5);
                    
            
            BufferedImage currentPageImage = getBufferedImage(bitmapWidth, bitmapHeight);
            
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
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);

            // transform page based on scale factor supplied
            AffineTransform at = graphics.getTransform();
            at.scale(scaleX, scaleY);
            graphics.setTransform(at);

            // draw page frame
            if (!transparentPageBackground) {
                graphics.setColor(Color.white);
                graphics.fillRect(0, 0, pageWidth, pageHeight);
            }
            graphics.setColor(Color.black);
            graphics.drawRect(-1, -1, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(pageWidth + 2, 0, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(pageWidth + 3, 1, pageWidth + 3, pageHeight + 3);
            graphics.drawLine(0, pageHeight + 2, pageWidth + 2, pageHeight + 2);
            graphics.drawLine(1, pageHeight + 3, pageWidth + 3, pageHeight + 3);

            state = new Java2DGraphicsState(graphics, this.fontInfo, at);
            try {
                // reset the current Positions
                currentBPPosition = 0;
                currentIPPosition = 0;

                // this toggles the rendering of all areas
                renderPageAreas(pageViewport.getPage());
            } finally {
                state = null;
            }

            return currentPageImage;
        } finally {
            this.currentPageViewport = null;
        }
    }

    /**
     * Returns a specific <code>BufferedImage</code> to paint a page image on. This method can
     * be overridden in subclasses to produce different image formats (ex. grayscale or b/w).
     * @param bitmapWidth width of the image in pixels
     * @param bitmapHeight heigth of the image in pixels
     * @return the newly created BufferedImage
     */
    protected BufferedImage getBufferedImage(int bitmapWidth, int bitmapHeight) {
       return new BufferedImage(
                bitmapWidth, bitmapHeight, BufferedImage.TYPE_INT_ARGB);
    }
    
    /**
     * Returns a page viewport.
     * @param pageIndex the page index (zero-based)
     * @return the requested PageViewport instance
     * @exception FOPException If the page is out of range.
     */
    public PageViewport getPageViewport(int pageIndex) throws FOPException {
        if (pageIndex < 0 || pageIndex >= pageViewportList.size()) {
            throw new FOPException("Requested page number is out of range: " + pageIndex
                     + "; only " + pageViewportList.size()
                     + " page(s) available.");
        }
        return (PageViewport) pageViewportList.get(pageIndex);
    }

    /**
     * Generates a desired page from the renderer's page viewport list.
     *
     * @param pageNum the 0-based page number to generate
     * @return the <code>java.awt.image.BufferedImage</code> corresponding to
     * the page or null if the page doesn't exist.
     * @throws FOPException If there's a problem preparing the page image
     */
    public BufferedImage getPageImage(int pageNum) throws FOPException {
        return getPageImage(getPageViewport(pageNum));
    }

    /** Saves the graphics state of the rendering engine. */
    protected void saveGraphicsState() {
        // push (and save) the current graphics state
        stateStack.push(state);
        state = new Java2DGraphicsState(state);
    }

    /** Restores the last graphics state of the rendering engine. */
    protected void restoreGraphicsState() {
        state.dispose();
        state = (Java2DGraphicsState)stateStack.pop();
    }
    
    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        state.transform(at);
    }
    
    /** {@inheritDoc} */
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
     * {@inheritDoc}
     */
    protected void endVParea() {
        restoreGraphicsState();
    }

    /**
     * {@inheritDoc}
     */
    protected List breakOutOfStateStack() {
        log.debug("Block.FIXED --> break out");
        List breakOutList;
        breakOutList = new java.util.ArrayList();
        while (!stateStack.isEmpty()) {
            breakOutList.add(0, state);
            //We only pop, we don't dispose, because we can use the instances again later
            state = (Java2DGraphicsState)stateStack.pop();
        }
        return breakOutList;
    }

    /**
     * {@inheritDoc}
     *          java.util.List)
     */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
        log.debug("Block.FIXED --> restoring context after break-out");
        
        Iterator i = breakOutList.iterator();
        while (i.hasNext()) {
            Java2DGraphicsState s = (Java2DGraphicsState)i.next();
            stateStack.push(state);
            state = s;
        }
    }

    /**
     * {@inheritDoc} 
     */
    protected void updateColor(Color col, boolean fill) {
        state.updateColor(col);
    }

    /**
     * {@inheritDoc}
     */
    protected void clip() {
        if (currentPath == null) {
            throw new IllegalStateException("No current path available!");
        }
        state.updateClip(currentPath);
        currentPath = null;
    }

    /**
     * {@inheritDoc}
     */
    protected void closePath() {
        currentPath.closePath();
    }

    /**
     * {@inheritDoc} 
     */
    protected void lineTo(float x, float y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.lineTo(x, y);
    }

    /**
     * {@inheritDoc} 
     */
    protected void moveTo(float x, float y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.moveTo(x, y);
    }

    /**
     * {@inheritDoc} 
     */
    protected void clipRect(float x, float y, float width, float height) {
        state.updateClip(new Rectangle2D.Float(x, y, width, height));
    }

    /**
     * {@inheritDoc} 
     */
    protected void fillRect(float x, float y, float width, float height) {
        state.getGraph().fill(new Rectangle2D.Float(x, y, width, height));
    }
    
    /**
     * {@inheritDoc} 
     */
    protected void drawBorderLine(float x1, float y1, float x2, float y2, 
            boolean horz, boolean startOrBefore, int style, Color col) {
        Graphics2D g2d = state.getGraph();
        drawBorderLine(new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1), 
                horz, startOrBefore, style, col, g2d);
    }

    /**
     * Draw a border segment of an XSL-FO style border.
     * @param lineRect the line defined by its bounding rectangle
     * @param horz true for horizontal border segments, false for vertical border segments
     * @param startOrBefore true for border segments on the start or before edge, 
     *                      false for end or after.
     * @param style the border style (one of Constants.EN_DASHED etc.)
     * @param col the color for the border segment
     * @param g2d the Graphics2D instance to paint to
     */
    public static void drawBorderLine(Rectangle2D.Float lineRect, 
            boolean horz, boolean startOrBefore, int style, Color col, Graphics2D g2d) {
        float x1 = lineRect.x;
        float y1 = lineRect.y;
        float x2 = x1 + lineRect.width;
        float y2 = y1 + lineRect.height;
        float w = lineRect.width;
        float h = lineRect.height;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED: 
                g2d.setColor(col);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    float ym = y1 + (h / 2);
                    BasicStroke s = new BasicStroke(h, BasicStroke.CAP_BUTT, 
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {unit}, 0);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    float xm = x1 + (w / 2);
                    BasicStroke s = new BasicStroke(w, BasicStroke.CAP_BUTT, 
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {unit}, 0);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(xm, y1, xm, y2));
                }
                break;
            case Constants.EN_DOTTED:
                g2d.setColor(col);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    float ym = y1 + (h / 2);
                    BasicStroke s = new BasicStroke(h, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {0, unit}, 0);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    float xm = x1 + (w / 2);
                    BasicStroke s = new BasicStroke(w, BasicStroke.CAP_ROUND, 
                            BasicStroke.JOIN_MITER, 10.0f, new float[] {0, unit}, 0);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(xm, y1, xm, y2));
                }
                break;
            case Constants.EN_DOUBLE:
                g2d.setColor(col);
                if (horz) {
                    float h3 = h / 3;
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    BasicStroke s = new BasicStroke(h3);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(x1, ym1, x2, ym1));
                    g2d.draw(new Line2D.Float(x1, ym2, x2, ym2));
                } else {
                    float w3 = w / 3;
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    BasicStroke s = new BasicStroke(w3);
                    g2d.setStroke(s);
                    g2d.draw(new Line2D.Float(xm1, y1, xm1, y2));
                    g2d.draw(new Line2D.Float(xm2, y1, xm2, y2));
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
                float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
                if (horz) {
                    Color uppercol = lightenColor(col, -colFactor);
                    Color lowercol = lightenColor(col, colFactor);
                    float h3 = h / 3;
                    float ym1 = y1 + (h3 / 2);
                    g2d.setStroke(new BasicStroke(h3));
                    g2d.setColor(uppercol);
                    g2d.draw(new Line2D.Float(x1, ym1, x2, ym1));
                    g2d.setColor(col);
                    g2d.draw(new Line2D.Float(x1, ym1 + h3, x2, ym1 + h3));
                    g2d.setColor(lowercol);
                    g2d.draw(new Line2D.Float(x1, ym1 + h3 + h3, x2, ym1 + h3 + h3));
                } else {
                    Color leftcol = lightenColor(col, -colFactor);
                    Color rightcol = lightenColor(col, colFactor);
                    float w3 = w / 3;
                    float xm1 = x1 + (w3 / 2);
                    g2d.setStroke(new BasicStroke(w3));
                    g2d.setColor(leftcol);
                    g2d.draw(new Line2D.Float(xm1, y1, xm1, y2));
                    g2d.setColor(col);
                    g2d.draw(new Line2D.Float(xm1 + w3, y1, xm1 + w3, y2));
                    g2d.setColor(rightcol);
                    g2d.draw(new Line2D.Float(xm1 + w3 + w3, y1, xm1 + w3 + w3, y2));
                }
                break;
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
                colFactor = (style == EN_OUTSET ? 0.4f : -0.4f);
                if (horz) {
                    col = lightenColor(col, (startOrBefore ? 1 : -1) * colFactor);
                    g2d.setStroke(new BasicStroke(h));
                    float ym1 = y1 + (h / 2);
                    g2d.setColor(col);
                    g2d.draw(new Line2D.Float(x1, ym1, x2, ym1));
                } else {
                    col = lightenColor(col, (startOrBefore ? 1 : -1) * colFactor);
                    float xm1 = x1 + (w / 2);
                    g2d.setStroke(new BasicStroke(w));
                    g2d.setColor(col);
                    g2d.draw(new Line2D.Float(xm1, y1, xm1, y2));
                }
                break;
            case Constants.EN_HIDDEN:
                break;
            default:
                g2d.setColor(col);
                if (horz) {
                    float ym = y1 + (h / 2);
                    g2d.setStroke(new BasicStroke(h));
                    g2d.draw(new Line2D.Float(x1, ym, x2, ym));
                } else {
                    float xm = x1 + (w / 2);
                    g2d.setStroke(new BasicStroke(w));
                    g2d.draw(new Line2D.Float(xm, y1, xm, y2));
                }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);

        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();
        int saveIP = currentIPPosition;

        Font font = getFontFromArea(text);
        state.updateFont(font.getFontName(), font.getFontSize());
        saveGraphicsState();
        AffineTransform at = new AffineTransform();
        at.translate(rx / 1000f, bl / 1000f);
        state.transform(at);
        renderText(text, state.getGraph(), font);
        restoreGraphicsState();
        
        currentIPPosition = saveIP + text.getAllocIPD();
        //super.renderText(text);

        // rendering text decorations
        Typeface tf = (Typeface) fontInfo.getFonts().get(font.getFontName());
        int fontsize = text.getTraitAsInteger(Trait.FONT_SIZE);
        renderTextDecoration(tf, fontsize, text, bl, rx);
    }

    /**
     * Renders a TextArea to a Graphics2D instance. Adjust the coordinate system so that the
     * start of the baseline of the first character is at coordinate (0,0).
     * @param text the TextArea
     * @param g2d the Graphics2D to render to
     * @param font the font to paint with
     */
    public static void renderText(TextArea text, Graphics2D g2d, Font font) {

        Color col = (Color) text.getTrait(Trait.COLOR);
        g2d.setColor(col);

        float textCursor = 0;

        Iterator iter = text.getChildAreas().iterator();
        while (iter.hasNext()) {
            InlineArea child = (InlineArea)iter.next();
            if (child instanceof WordArea) {
                WordArea word = (WordArea)child;
                String s = word.getWord();
                int[] letterAdjust = word.getLetterAdjustArray();
                GlyphVector gv = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), s);
                double additionalWidth = 0.0;
                if (letterAdjust == null 
                        && text.getTextLetterSpaceAdjust() == 0 
                        && text.getTextWordSpaceAdjust() == 0) {
                    //nop
                } else {
                    int[] offsets = getGlyphOffsets(s, font, text, letterAdjust);
                    float cursor = 0.0f;
                    for (int i = 0; i < offsets.length; i++) {
                        Point2D pt = gv.getGlyphPosition(i);
                        pt.setLocation(cursor, pt.getY());
                        gv.setGlyphPosition(i, pt);
                        cursor += offsets[i] / 1000f;
                    }
                    additionalWidth = cursor - gv.getLogicalBounds().getWidth();
                }
                g2d.drawGlyphVector(gv, textCursor, 0);
                textCursor += gv.getLogicalBounds().getWidth() + additionalWidth;
            } else if (child instanceof SpaceArea) {
                SpaceArea space = (SpaceArea)child;
                String s = space.getSpace();
                char sp = s.charAt(0);
                int tws = (space.isAdjustable() 
                        ? text.getTextWordSpaceAdjust() 
                                + 2 * text.getTextLetterSpaceAdjust()
                        : 0);

                textCursor += (font.getCharWidth(sp) + tws) / 1000f;
            } else {
                throw new IllegalStateException("Unsupported child element: " + child);
            }
        }
    }
    
    private static int[] getGlyphOffsets(String s, Font font, TextArea text, 
            int[] letterAdjust) {
        int textLen = s.length();
        int[] offsets = new int[textLen];
        for (int i = 0; i < textLen; i++) {
            final char c = s.charAt(i);
            final char mapped = font.mapChar(c);
            int wordSpace;

            if (CharUtilities.isAdjustableSpace(mapped)) {
                wordSpace = text.getTextWordSpaceAdjust();
            } else {
                wordSpace = 0;
            }
            int cw = font.getWidth(mapped);
            int ladj = (letterAdjust != null && i < textLen - 1 ? letterAdjust[i + 1] : 0);
            int tls = (i < textLen - 1 ? text.getTextLetterSpaceAdjust() : 0); 
            offsets[i] = cw + ladj + tls + wordSpace;
        }
        return offsets;
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

        Color col = (Color) area.getTrait(Trait.COLOR);
        state.updateColor(col);

        Line2D line = new Line2D.Float();
        line.setLine(startx, starty, endx, starty);
        float ruleThickness = area.getRuleThickness() / 1000f;

        int style = area.getRuleStyle();
        switch (style) {
        case EN_SOLID:
        case EN_DASHED:
        case EN_DOUBLE:
            drawBorderLine(startx, starty, endx, starty + ruleThickness, 
                    true, true, style, col);
            break;
        case EN_DOTTED:
            //TODO Dots should be shifted to the left by ruleThickness / 2
            state.updateStroke(ruleThickness, style);
            float rt2 = ruleThickness / 2f;
            line.setLine(line.getX1(), line.getY1() + rt2, line.getX2(), line.getY2() + rt2);
            state.getGraph().draw(line);
            break;
        case EN_GROOVE:
        case EN_RIDGE:
            float half = area.getRuleThickness() / 2000f;

            state.updateColor(lightenColor(col, 0.6f));
            moveTo(startx, starty);
            lineTo(endx, starty);
            lineTo(endx, starty + 2 * half);
            lineTo(startx, starty + 2 * half);
            closePath();
            state.getGraph().fill(currentPath);
            currentPath = null;
            state.updateColor(col);
            if (style == EN_GROOVE) {
                moveTo(startx, starty);
                lineTo(endx, starty);
                lineTo(endx, starty + half);
                lineTo(startx + half, starty + half);
                lineTo(startx, starty + 2 * half);
            } else {
                moveTo(endx, starty);
                lineTo(endx, starty + 2 * half);
                lineTo(startx, starty + 2 * half);
                lineTo(startx, starty + half);
                lineTo(endx - half, starty + half);
            }
            closePath();
            state.getGraph().fill(currentPath);
            currentPath = null;

        case EN_NONE:
            // No rule is drawn
            break;
        default:
        } // end switch

        super.renderLeader(area);
    }

    /**
     * {@inheritDoc}
     */
    public void renderImage(Image image, Rectangle2D pos) {
        // endTextObject();
        String url = image.getURL();
        drawImage(url, pos);
    }

    /**
     * {@inheritDoc}
     */
    protected void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {

        int x = currentIPPosition + (int)Math.round(pos.getX());
        int y = currentBPPosition + (int)Math.round(pos.getY());
        uri = URISpecification.getURL(uri);
        
        ImageManager manager = getUserAgent().getFactory().getImageManager();
        ImageInfo info = null;
        try {
            ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
            info = manager.getImageInfo(uri, sessionContext);
            final ImageFlavor[] flavors = new ImageFlavor[]
                {ImageFlavor.GRAPHICS2D,
                    ImageFlavor.BUFFERED_IMAGE, 
                    ImageFlavor.RENDERED_IMAGE, 
                    ImageFlavor.XML_DOM};
            Map hints = ImageUtil.getDefaultHints(sessionContext);
            org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                    info, flavors, hints, sessionContext);
            if (img instanceof ImageGraphics2D) {
                ImageGraphics2D imageG2D = (ImageGraphics2D)img;
                int width = (int)pos.getWidth();
                int height = (int)pos.getHeight();
                RendererContext context = createRendererContext(
                        x, y, width, height, foreignAttributes);
                getGraphics2DAdapter().paintImage(imageG2D.getGraphics2DImagePainter(),
                        context, x, y, width, height);
            } else if (img instanceof ImageRendered) {
                ImageRendered imgRend = (ImageRendered)img;
                AffineTransform at = new AffineTransform();
                at.translate(x / 1000f, y / 1000f);
                double sx = pos.getWidth() / info.getSize().getWidthMpt();
                double sy = pos.getHeight() / info.getSize().getHeightMpt();
                sx *= userAgent.getSourceResolution() / info.getSize().getDpiHorizontal();
                sy *= userAgent.getSourceResolution() / info.getSize().getDpiVertical();
                at.scale(sx, sy);
                state.getGraph().drawRenderedImage(imgRend.getRenderedImage(), at);
            } else if (img instanceof ImageXMLDOM) {
                ImageXMLDOM imgXML = (ImageXMLDOM)img;
                renderDocument(imgXML.getDocument(), imgXML.getRootNamespace(),
                        pos, foreignAttributes);
            }
        } catch (ImageException ie) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
        } catch (FileNotFoundException fe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
        }
    }

    /** {@inheritDoc} */
    protected RendererContext createRendererContext(int x, int y, int width, int height, 
            Map foreignAttributes) {
        RendererContext context = super.createRendererContext(
                x, y, width, height, foreignAttributes);
        context.setProperty(Java2DRendererContextConstants.JAVA2D_STATE, state);
        return context;
    }

    /** {@inheritDoc} */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        if (pageIndex >= getNumberOfPages()) {
            return NO_SUCH_PAGE;
        }

        if (state != null) {
            throw new IllegalStateException("state must be null");
        }
        Graphics2D graphics = (Graphics2D) g;
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
            state = null;
        }
    }

    /** {@inheritDoc} */
    protected void beginTextObject() {
        //not necessary in Java2D
    }

    /** {@inheritDoc} */
    protected void endTextObject() {
        //not necessary in Java2D
    }

    /**
     * Controls the page background.
     * @param transparentPageBackground true if the background should be transparent
     */
    public void setTransparentPageBackground(boolean transparentPageBackground) {
        this.transparentPageBackground = transparentPageBackground;
    }

}
