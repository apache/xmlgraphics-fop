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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.base14.Courier;
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.base14.TimesRoman;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.extensions.AFPPageSetup;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.fonts.AFPFontInfo;
import org.apache.fop.render.afp.fonts.CharacterSet;
import org.apache.fop.render.afp.fonts.FopCharacterSet;
import org.apache.fop.render.afp.fonts.OutlineFont;
import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.render.afp.modca.ImageObject;
import org.apache.fop.render.afp.modca.PageObject;

/**
 * This is an implementation of a FOP Renderer that renders areas to AFP.
 * <p>
 * A renderer is primarily designed to convert a given area tree into the output
 * document format. It should be able to produce pages and fill the pages with
 * the text and graphical content. Usually the output is sent to an output
 * stream. Some output formats may support extra information that is not
 * available from the area tree or depends on the destination of the document.
 * Each renderer is given an area tree to render to its output format. The area
 * tree is simply a representation of the pages and the placement of text and
 * graphical objects on those pages.
 * </p>
 * <p>
 * The renderer will be given each page as it is ready and an output stream to
 * write the data out. All pages are supplied in the order they appear in the
 * document. In order to save memory it is possible to render the pages out of
 * order. Any page that is not ready to be rendered is setup by the renderer
 * first so that it can reserve a space or reference for when the page is ready
 * to be rendered.The renderer is responsible for managing the output format and
 * associated data and flow.
 * </p>
 * <p>
 * Each renderer is totally responsible for its output format. Because font
 * metrics (and therefore layout) are obtained in two different ways depending
 * on the renderer, the renderer actually sets up the fonts being used. The font
 * metrics are used during the layout process to determine the size of
 * characters.
 * </p>
 * <p>
 * The render context is used by handlers. It contains information about the
 * current state of the renderer, such as the page, the position, and any other
 * miscellaneous objects that are required to draw into the page.
 * </p>
 * <p>
 * A renderer is created by implementing the Renderer interface. However, the
 * AbstractRenderer does most of what is needed, including iterating through the
 * tree parts, so it is this that is extended. This means that this object only
 * need to implement the basic functionality such as text, images, and lines.
 * AbstractRenderer's methods can easily be overridden to handle things in a
 * different way or do some extra processing.
 * </p>
 * <p>
 * The relevant AreaTree structures that will need to be rendered are Page,
 * Viewport, Region, Span, Block, Line, Inline. A renderer implementation
 * renders each individual page, clips and aligns child areas to a viewport,
 * handle all types of inline area, text, image etc and draws various lines and
 * rectangles.
 * </p>
 *
 * Note: There are specific extensions that have been added to the
 * FO. They are specific to their location within the FO and have to be
 * processed accordingly (ie. at the start or end of the page).
 *
 */
public class AFPRenderer extends AbstractPathOrientedRenderer {

    /**
     * The default afp renderer output resolution
     */
    private static final int DEFAULT_DPI_RESOLUTION = 240;

    /**
     * The afp factor for calculating resolutions (e.g. 72000/240 = 300)
     */
    private static final int DPI_CONVERSION_FACTOR = 72000;

    /**
     * The afp data stream object responsible for generating afp data
     */
    private AFPDataStream afpDataStream = null;

    /**
     * The map of afp root extensions
     */
    // UNUSED
    // private HashMap rootExtensionMap = null;
    /**
     * The map of page segments
     */
    private HashMap pageSegmentsMap = null;

    /**
     * The fonts on the current page
     */
    private HashMap currentPageFonts = null;

    /**
     * The current color object
     */
    private Color currentColor = null;

    /**
     * The page font number counter, used to determine the next font reference
     */
    private int pageFontCounter = 0;

    /**
     * The current font family
     */
    // UNUSED
    // private String currentFontFamily = "";
    /**
     * The current font size
     */
    private int currentFontSize = 0;

    /**
     * The Options to be set on the AFPRenderer
     */
    // UNUSED
    // private Map afpOptions = null;
    /**
     * The page width
     */
    private int pageWidth = 0;

    /**
     * The page height
     */
    private int pageHeight = 0;

    /**
     * The current page sequence id
     */
    // UNUSED
    // private String pageSequenceId = null;
    /**
     * The portrait rotation
     */
    private int portraitRotation = 0;

    /**
     * The landscape rotation
     */
    private int landscapeRotation = 270;

    /**
     * The line cache, avoids drawing duplicate lines in tables.
     */
    // UNUSED
    // private HashSet lineCache = null;
    /**
     * The current x position for line drawing
     */
    // UNUSED
    // private float x;
    /**
     * The current y position for line drawing
     */
    // UNUSED
    // private float y;
    /**
     * The map of saved incomplete pages
     */
    private Map pages = null;

    /**
     * Flag to the set the output object type for images
     */
    private boolean colorImages = false;

    /**
     * Default value for image depth
     */
    private int bitsPerPixel = 8;

    /**
     * The output resolution
     */
    private int resolution = DEFAULT_DPI_RESOLUTION;

    /**
     * Constructor for AFPRenderer.
     */
    public AFPRenderer() {
        super();
    }

    /**
     * Set up the font info
     *
     * @param inFontInfo  font info to set up
     */
    public void setupFontInfo(FontInfo inFontInfo) {
        this.fontInfo = inFontInfo;
        int num = 1;
        if (this.fontList != null && this.fontList.size() > 0) {
            for (Iterator it = this.fontList.iterator(); it.hasNext();) {
                AFPFontInfo afi = (AFPFontInfo)it.next();
                AFPFont bf = (AFPFont)afi.getAFPFont();
                for (Iterator it2 = afi.getFontTriplets().iterator(); it2.hasNext();) {
                    FontTriplet ft = (FontTriplet)it2.next();
                    this.fontInfo.addFontProperties("F" + num, ft.getName()
                                                    , ft.getStyle(), ft.getWeight());
                    this.fontInfo.addMetrics("F" + num, bf);
                    num++;
                }
            }
        } else {
            AFPEventProducer eventProducer = AFPEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.warnDefaultFontSetup(this);
        }
        if (this.fontInfo.fontLookup("sans-serif", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZH200  ",
                    1, new Helvetica());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "sans-serif", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("serif", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZN200  ",
                    1, new TimesRoman());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "serif", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("monospace", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZ4200  ",
                    1, new Courier());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "monospace", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("any", "normal", 400) == null) {
            FontTriplet ft = this.fontInfo.fontLookup("sans-serif", "normal", 400);
            this.fontInfo.addFontProperties(
                    this.fontInfo.getInternalFontKey(ft), "any", "normal", 400);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
    }

    /**
     * {@inheritDoc}
     */
    public void startRenderer(OutputStream outputStream) throws IOException {
        currentPageFonts = new HashMap();
        currentColor = new Color(255, 255, 255);
        afpDataStream = new AFPDataStream();
        afpDataStream.setPortraitRotation(portraitRotation);
        afpDataStream.setLandscapeRotation(landscapeRotation);
        afpDataStream.startDocument(outputStream);
    }

    /**
     * {@inheritDoc}
     */
    public void stopRenderer() throws IOException {
        afpDataStream.endDocument();
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsOutOfOrder() {
        //return false;
        return true;
    }

    /**
     * Prepare a page for rendering. This is called if the renderer supports
     * out of order rendering. The renderer should prepare the page so that a
     * page further on in the set of pages can be rendered. The body of the
     * page should not be rendered. The page will be rendered at a later time
     * by the call to render page.
     *
     * {@inheritDoc}
     */
    public void preparePage(PageViewport page) {
        // initializeRootExtensions(page);

        // this.currentFontFamily = "";
        this.currentFontSize = 0;
        this.pageFontCounter = 0;
        this.currentPageFonts.clear();
        // this.lineCache = new HashSet();

        Rectangle2D bounds = page.getViewArea();

        this.pageWidth = mpts2units(bounds.getWidth());
        this.pageHeight = mpts2units(bounds.getHeight());

        // renderPageGroupExtensions(page);

        final int pageRotation = 0;
        this.afpDataStream.startPage(pageWidth, pageHeight, pageRotation,
                getResolution(), getResolution());

        renderPageObjectExtensions(page);

        if (this.pages == null) {
            this.pages = new HashMap();
        }
        this.pages.put(page, afpDataStream.savePage());

    }

    /**
     * {@inheritDoc}
     */
    public void processOffDocumentItem(OffDocumentItem odi) {
        // TODO
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new AFPGraphics2DAdapter();
    }

    /**
     * {@inheritDoc}
     */
    public void startVParea(CTM ctm, Rectangle2D clippingRect) {
        // dummy not used
    }

    /**
     * {@inheritDoc}
     */
    public void endVParea() {
        // dummy not used
    }

    /**
     * Renders a region viewport. <p>
     *
     * The region may clip the area and it establishes a position from where
     * the region is placed.</p>
     *
     * @param port  The region viewport to be rendered
     */
    public void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            Rectangle2D view = port.getViewArea();
            // The CTM will transform coordinates relative to
            // this region-reference area into page coords, so
            // set origin for the region to 0,0.
            currentBPPosition = 0;
            currentIPPosition = 0;

            RegionReference regionReference = port.getRegionReference();
            handleRegionTraits(port);

            /*
            _afpDataStream.startOverlay(mpts2units(view.getX())
                , mpts2units(view.getY())
                , mpts2units(view.getWidth())
                , mpts2units(view.getHeight())
                , rotation);
             */

            pushViewPortPos(new ViewPortPos(view, regionReference.getCTM()));

            if (regionReference.getRegionClass() == FO_REGION_BODY) {
                renderBodyRegion((BodyRegion) regionReference);
            } else {
                renderRegion(regionReference);
            }
            /*
            _afpDataStream.endOverlay();
             */
            popViewPortPos();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();
        //This is the content-rect
        float width = (float)bv.getIPD() / 1000f;
        float height = (float)bv.getBPD() / 1000f;

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

            //skipping fox:transform here

            //saveGraphicsState();
            //Viewport position
            //concatenateTransformationMatrix(mptToPt(positionTransform));
            
            //Background and borders
            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd()) / 1000f;
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter()) / 1000f;
            Point2D ptSrc = new Point(0, 0);
            Point2D ptDst = positionTransform.transform(ptSrc, null);
            Rectangle2D borderRect = new Rectangle2D.Double(ptDst.getX(), ptDst.getY(),
                    1000 * (width + bpwidth), 1000 * (height + bpheight));
            pushViewPortPos(new ViewPortPos(borderRect, new CTM(positionTransform)));
            drawBackAndBorders(bv, 0, 0, width + bpwidth, height + bpheight);

            //Shift to content rectangle after border painting
            AffineTransform contentRectTransform = new AffineTransform();
            contentRectTransform.translate(borderPaddingStart, borderPaddingBefore);
            //concatenateTransformationMatrix(mptToPt(contentRectTransform));
            ptSrc = new Point(0, 0);
            ptDst = contentRectTransform.transform(ptSrc, null);
            Rectangle2D contentRect = new Rectangle2D.Double(ptDst.getX(), ptDst.getY(),
                    1000 * width, 1000 * height);
            pushViewPortPos(new ViewPortPos(contentRect, new CTM(contentRectTransform)));

            //Clipping is not supported, yet
            //Rectangle2D clippingRect = null;
            //clippingRect = new Rectangle(0, 0, bv.getIPD(), bv.getBPD());

            //saveGraphicsState();
            //Set up coordinate system for content rectangle
            AffineTransform contentTransform = ctm.toAffineTransform();
            //concatenateTransformationMatrix(mptToPt(contentTransform));
            contentRect = new Rectangle2D.Double(0, 0, 1000 * width, 1000 * height);
            pushViewPortPos(new ViewPortPos(contentRect, new CTM(contentTransform)));
            
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);

            popViewPortPos();
            popViewPortPos();
            //restoreGraphicsState();
            popViewPortPos();
            //restoreGraphicsState();

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
            clippingRect = new Rectangle(currentIPPosition, currentBPPosition,
                    bv.getIPD(), bv.getBPD());

            //startVParea(ctm, clippingRect);
            pushViewPortPos(new ViewPortPos(clippingRect, ctm));

            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);
            //endVParea();
            popViewPortPos();

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;

            currentBPPosition += (int)(bv.getAllocBPD());
        }
    }

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        //Not used here since AFPRenderer defines its own renderBlockViewport() method.
        throw new UnsupportedOperationException("NYI");
    }
    
    /**
     * {@inheritDoc}
     */
    public void renderPage(PageViewport pageViewport) {

        // initializeRootExtensions(page);

        // this.currentFontFamily = "";
        this.currentFontSize = 0;
        this.pageFontCounter = 0;
        this.currentPageFonts.clear();
        // this.lineCache = new HashSet();

        Rectangle2D bounds = pageViewport.getViewArea();

        this.pageWidth = mpts2units(bounds.getWidth());
        this.pageHeight = mpts2units(bounds.getHeight());

        if (pages != null && pages.containsKey(pageViewport)) {

            this.afpDataStream.restorePage((PageObject) pages.remove(pageViewport));

        } else {
            // renderPageGroupExtensions(page);

            final int pageRotation = 0;
            this.afpDataStream.startPage(pageWidth, pageHeight, pageRotation,
                    getResolution(), getResolution());

            renderPageObjectExtensions(pageViewport);

        }

        pushViewPortPos(new ViewPortPos());

        renderPageAreas(pageViewport.getPage());

        Iterator i = currentPageFonts.values().iterator();
        while (i.hasNext()) {
            AFPFontAttributes afpFontAttributes = (AFPFontAttributes) i.next();

            afpDataStream.createFont(
                (byte)afpFontAttributes.getFontReference(),
                afpFontAttributes.getFont(),
                afpFontAttributes.getPointSize());

        }

        try {
            afpDataStream.endPage();
        } catch (IOException ioex) {
            // TODO What shall we do?
        }

        popViewPortPos();

    }

    /**
     * {@inheritDoc}
     */
    public void clip() {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public void clipRect(float x, float y, float width, float height) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public void moveTo(float x, float y) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public void lineTo(float x, float y) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public void closePath() {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    public void fillRect(float x, float y, float width, float height) {
        /*
        afpDataStream.createShading(
            pts2units(x),
            pts2units(y),
            pts2units(width),
            pts2units(height),
            currentColor.getRed(),
            currentColor.getGreen(),
            currentColor.getBlue());
         */
        afpDataStream.createLine(
            pts2units(x),
            pts2units(y),
            pts2units(x + width),
            pts2units(y),
            pts2units(height),
            currentColor);
    }

    /**
     * {@inheritDoc}
     */
    public void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DOUBLE:
                if (horz) {
                    float h3 = h / 3;
                    float ym1 = y1;
                    float ym2 = ym1 + h3 + h3;
                    afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1),
                        pts2units(x2),
                        pts2units(ym1),
                        pts2units(h3),
                        col
                    );
                    afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym2),
                        pts2units(x2),
                        pts2units(ym2),
                        pts2units(h3),
                        col
                    );
                } else {
                    float w3 = w / 3;
                    float xm1 = x1;
                    float xm2 = xm1 + w3 + w3;
                    afpDataStream.createLine(
                        pts2units(xm1),
                        pts2units(y1),
                        pts2units(xm1),
                        pts2units(y2),
                        pts2units(w3),
                        col
                    );
                    afpDataStream.createLine(
                        pts2units(xm2),
                        pts2units(y1),
                        pts2units(xm2),
                        pts2units(y2),
                        pts2units(w3),
                        col
                    );
                }
                break;
            case Constants.EN_DASHED:
                if (horz) {
                    float w2 = 2 * h;
                    while (x1 + w2 < x2) {
                        afpDataStream.createLine(
                            pts2units(x1),
                            pts2units(y1),
                            pts2units(x1 + w2),
                            pts2units(y1),
                            pts2units(h),
                            col
                        );
                        x1 += 2 * w2;
                    }
                } else {
                    float h2 = 2 * w;
                    while (y1 + h2 < y2) {
                        afpDataStream.createLine(
                            pts2units(x1),
                            pts2units(y1),
                            pts2units(x1),
                            pts2units(y1 + h2),
                            pts2units(w),
                            col
                        );
                        y1 += 2 * h2;
                    }
                }
                break;
            case Constants.EN_DOTTED:
                if (horz) {
                    while (x1 + h < x2) {
                        afpDataStream.createLine(
                            pts2units(x1),
                            pts2units(y1),
                            pts2units(x1 + h),
                            pts2units(y1),
                            pts2units(h),
                            col
                        );
                        x1 += 2 * h;
                    }
                } else {
                    while (y1 + w < y2) {
                        afpDataStream.createLine(
                            pts2units(x1),
                            pts2units(y1),
                            pts2units(x1),
                            pts2units(y1 + w),
                            pts2units(w),
                            col
                        );
                        y1 += 2 * w;
                    }
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
            {
                float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
                if (horz) {
                    Color uppercol = lightenColor(col, -colFactor);
                    Color lowercol = lightenColor(col, colFactor);
                    float h3 = h / 3;
                    float ym1 = y1;
                    afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1),
                        pts2units(x2),
                        pts2units(ym1),
                        pts2units(h3),
                        uppercol
                    );
                    afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1 + h3),
                        pts2units(x2),
                        pts2units(ym1 + h3),
                        pts2units(h3),
                        col
                    );
                    afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1 + h3 + h3),
                        pts2units(x2),
                        pts2units(ym1 + h3 + h3),
                        pts2units(h3),
                        lowercol
                    );
                } else {
                    Color leftcol = lightenColor(col, -colFactor);
                    Color rightcol = lightenColor(col, colFactor);
                    float w3 = w / 3;
                    float xm1 = x1 + (w3 / 2);
                    afpDataStream.createLine(
                        pts2units(xm1),
                        pts2units(y1),
                        pts2units(xm1),
                        pts2units(y2),
                        pts2units(w3),
                        leftcol
                    );
                    afpDataStream.createLine(
                        pts2units(xm1 + w3),
                        pts2units(y1),
                        pts2units(xm1 + w3),
                        pts2units(y2),
                        pts2units(w3),
                        col
                    );
                    afpDataStream.createLine(
                        pts2units(xm1 + w3 + w3),
                        pts2units(y1),
                        pts2units(xm1 + w3 + w3),
                        pts2units(y2),
                        pts2units(w3),
                        rightcol
                    );
                }
                break;
            }
            case Constants.EN_HIDDEN:
                break;
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
            default:
                afpDataStream.createLine(
                    pts2units(x1),
                    pts2units(y1),
                    pts2units(horz ? x2 : x1),
                    pts2units(horz ? y1 : y2),
                    pts2units(Math.abs(horz ? (y2 - y1) : (x2 - x1))),
                    col
                );
        }
    }

    /**
     * {@inheritDoc}
     */
    protected RendererContext createRendererContext(int x, int y, int width, int height,
            Map foreignAttributes) {
        RendererContext context;
        context = super.createRendererContext(x, y, width, height, foreignAttributes);
        context.setProperty(AFPRendererContextConstants.AFP_GRAYSCALE,
                Boolean.valueOf(!this.colorImages));
        return context;
    }

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[]
                                                     {ImageFlavor.RAW_CCITTFAX,
                                                      ImageFlavor.GRAPHICS2D,
                                                      ImageFlavor.BUFFERED_IMAGE, 
                                                      ImageFlavor.RENDERED_IMAGE,
                                                      ImageFlavor.XML_DOM};

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        uri = URISpecification.getURL(uri);
        Rectangle posInt = new Rectangle(
                (int)pos.getX(),
                (int)pos.getY(),
                (int)pos.getWidth(),
                (int)pos.getHeight());
        Point origin = new Point(currentIPPosition, currentBPPosition);
        int x = origin.x + posInt.x;
        int y = origin.y + posInt.y;

        String name = null;
        if (pageSegmentsMap != null) {
            name = (String) pageSegmentsMap.get(uri);
        }
        if (name != null) {
            afpDataStream.createIncludePageSegment(name, mpts2units(x), mpts2units(y));
        } else {
            ImageManager manager = getUserAgent().getFactory().getImageManager();
            ImageInfo info = null;
            try {
                ImageSessionContext sessionContext = getUserAgent().getImageSessionContext();
                info = manager.getImageInfo(uri, sessionContext);
                
                //Only now fully load/prepare the image
                Map hints = ImageUtil.getDefaultHints(sessionContext);
                org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                        info, FLAVORS, hints, sessionContext);
                
                //...and process the image
                if (img instanceof ImageGraphics2D) {
                    ImageGraphics2D imageG2D = (ImageGraphics2D)img;
                    RendererContext context = createRendererContext(
                            posInt.x, posInt.y,
                            posInt.width, posInt.height, foreignAttributes);
                    getGraphics2DAdapter().paintImage(imageG2D.getGraphics2DImagePainter(),
                            context,
                            origin.x + posInt.x, origin.y + posInt.y,
                            posInt.width, posInt.height);
                } else if (img instanceof ImageRendered) {
                    ImageRendered imgRend = (ImageRendered)img;
                    RenderedImage ri = imgRend.getRenderedImage();
                    
                    drawBufferedImage(ri, getResolution(),
                            posInt.x + currentIPPosition,
                            posInt.y + currentBPPosition,
                            posInt.width,
                            posInt.height);
                } else if (img instanceof ImageRawCCITTFax) {
                    ImageRawCCITTFax ccitt = (ImageRawCCITTFax)img;
                    int afpx = mpts2units(posInt.x + currentIPPosition);
                    int afpy = mpts2units(posInt.y + currentBPPosition);
                    int afpw = mpts2units(posInt.getWidth());
                    int afph = mpts2units(posInt.getHeight());
                    int afpres = getResolution();
                    ImageObject io = afpDataStream.getImageObject(afpx, afpy, afpw, afph,
                            afpres, afpres);
                    io.setImageParameters(
                            (int) (ccitt.getSize().getDpiHorizontal() * 10),
                            (int) (ccitt.getSize().getDpiVertical() * 10),
                            ccitt.getSize().getWidthPx(),
                            ccitt.getSize().getHeightPx());
                    int compression = ccitt.getCompression();
                    switch (compression) {
                    case TIFFImage.COMP_FAX_G3_1D :
                        io.setImageEncoding((byte) 0x80);
                        break;
                    case TIFFImage.COMP_FAX_G3_2D :
                        io.setImageEncoding((byte) 0x81);
                        break;
                    case TIFFImage.COMP_FAX_G4_2D :
                        io.setImageEncoding((byte) 0x82);
                        break;
                    default:
                        throw new IllegalStateException(
                                "Invalid compression scheme: " + compression);
                    }
                    InputStream in = ccitt.createInputStream();
                    try {
                        byte[] buf = IOUtils.toByteArray(in);
                        io.setImageData(buf);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                } else if (img instanceof ImageXMLDOM) {
                    ImageXMLDOM imgXML = (ImageXMLDOM)img;
                    renderDocument(imgXML.getDocument(), imgXML.getRootNamespace(),
                            pos, foreignAttributes);
                } else {
                    throw new UnsupportedOperationException("Unsupported image type: " + img);
                }

            } catch (ImageException ie) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.imageError(this, (info != null ? info.toString() : uri), ie, null);
            } catch (FileNotFoundException fe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.imageNotFound(this, (info != null ? info.toString() : uri), fe, null);
            } catch (IOException ioe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
                eventProducer.imageIOError(this, (info != null ? info.toString() : uri), ioe, null);
            }
            
            /*
            ImageFactory fact = userAgent.getFactory().getImageFactory();
            FopImage fopimage = fact.getImage(url, userAgent);
            if (fopimage == null) {
                return;
            }
            if (!fopimage.load(FopImage.DIMENSIONS)) {
                return;
            }
            String mime = fopimage.getMimeType();
            if ("text/xml".equals(mime) || MimeConstants.MIME_SVG.equals(mime)) {
                if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                    return;
                }
                Document doc = ((XMLImage) fopimage).getDocument();
                String ns = ((XMLImage) fopimage).getNameSpace();

                renderDocument(doc, ns, pos, foreignAttributes);
            } else if (MimeConstants.MIME_EPS.equals(mime)) {
                log.warn("EPS images are not supported by this renderer");
                */
                /*
                 * } else if (MimeConstants.MIME_JPEG.equals(mime)) { if
                 * (!fopimage.load(FopImage.ORIGINAL_DATA)) { return; }
                 * fact.releaseImage(url, userAgent);
                 * 
                 * int x = mpts2units(pos.getX() + currentIPPosition); int y =
                 * mpts2units(pos.getY() + currentBPPosition); int w =
                 * mpts2units(pos.getWidth()); int h =
                 * mpts2units(pos.getHeight()); ImageObject io =
                 * _afpDataStream.getImageObject(); io.setImageViewport(x, y, w,
                 * h); io.setImageParameters(
                 * (int)(fopimage.getHorizontalResolution() * 10),
                 * (int)(fopimage.getVerticalResolution() * 10),
                 * fopimage.getWidth(), fopimage.getHeight() );
                 * io.setImageIDESize((byte)fopimage.getBitsPerPixel());
                 * io.setImageEncoding((byte)0x83);
                 * io.setImageData(fopimage.getRessourceBytes());
                 *//*
            } else if (MimeConstants.MIME_TIFF.equals(mime)
                        && fopimage instanceof TIFFImage) {
                TIFFImage tiffImage = (TIFFImage) fopimage;
                int x = mpts2units(pos.getX() + currentIPPosition);
                int y = mpts2units(pos.getY() + currentBPPosition);
                int w = mpts2units(pos.getWidth());
                int h = mpts2units(pos.getHeight());
                ImageObject io = afpDataStream.getImageObject(x, y, w, h,
                        getResolution(), getResolution());
                io.setImageParameters(
                    (int)(fopimage.getHorizontalResolution() * 10),
                    (int)(fopimage.getVerticalResolution() * 10),
                    fopimage.getWidth(),
                    fopimage.getHeight()
                );
                if (tiffImage.getStripCount() == 1) {
                    int comp = tiffImage.getCompression();
                    if (comp == 3) {
                        if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                            return;
                        }
                        io.setImageEncoding((byte)0x81);
                        io.setImageData(fopimage.getRessourceBytes());
                    } else if (comp == 4) {
                        if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                            return;
                        }
                        io.setImageEncoding((byte)0x82);
                        io.setImageData(fopimage.getRessourceBytes());
                    } else {
                        if (!fopimage.load(FopImage.BITMAP)) {
                            return;
                        }
                        convertToGrayScaleImage(io, fopimage.getBitmaps(), 
                                fopimage.getWidth(), fopimage.getHeight());
                    }
                } else {
                    if (!fopimage.load(FopImage.BITMAP)) {
                        return;
                    }
                    convertToGrayScaleImage(io, fopimage.getBitmaps(),
                            fopimage.getWidth(), fopimage.getHeight());
                }
            } else {
                if (!fopimage.load(FopImage.BITMAP)) {
                    return;
                }
                fact.releaseImage(url, userAgent);

                int x = mpts2units(pos.getX() + currentIPPosition);
                int y = mpts2units(pos.getY() + currentBPPosition);
                int w = mpts2units(pos.getWidth());
                int h = mpts2units(pos.getHeight());
                ImageObject io = afpDataStream.getImageObject(x, y, w, h,
                        getResolution(), getResolution());
                io.setImageParameters(
                    (int)(fopimage.getHorizontalResolution() * 10),
                    (int)(fopimage.getVerticalResolution() * 10),
                    fopimage.getWidth(),
                    fopimage.getHeight()
                );
                if (colorImages) {
                    io.setImageIDESize((byte)24);
                    io.setImageData(fopimage.getBitmaps());
                } else {
                    convertToGrayScaleImage(io, fopimage.getBitmaps(),
                            fopimage.getWidth(), fopimage.getHeight());
                }
            }*/
        }
    }

    /**
     * Writes a RenderedImage to an OutputStream as raw sRGB bitmaps.
     * 
     * @param image
     *            the RenderedImage
     * @param out
     *            the OutputStream
     * @throws IOException
     *             In case of an I/O error.
     */
    public static void writeImage(RenderedImage image, OutputStream out)
            throws IOException {
        ImageEncodingHelper.encodeRenderedImageAsRGB(image, out);
    }

    /**
     * Draws a BufferedImage to AFP.
     * 
     * @param image
     *            the RenderedImage
     * @param imageResolution
     *            the resolution of the BufferedImage
     * @param x
     *            the x coordinate (in mpt)
     * @param y
     *            the y coordinate (in mpt)
     * @param w
     *            the width of the viewport (in mpt)
     * @param h
     *            the height of the viewport (in mpt)
     */
    public void drawBufferedImage(RenderedImage image, int imageResolution, int x,
            int y, int w, int h) {
        int afpx = mpts2units(x);
        int afpy = mpts2units(y);
        int afpw = mpts2units(w);
        int afph = mpts2units(h);
        int afpres = getResolution();
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        try {
            // Serialize image
            //TODO Eventually, this should be changed not to buffer as this increases the
            //memory consumption (see PostScript output)
            writeImage(image, baout);
            byte[] buf = baout.toByteArray();

            // Generate image
            ImageObject io = afpDataStream.getImageObject(afpx, afpy, afpw,
                    afph, afpres, afpres);
            io.setImageParameters(imageResolution, imageResolution,
                    image.getWidth(), image.getHeight());
            if (colorImages) {
                io.setImageIDESize((byte)24);
                io.setImageData(buf);
            } else {
                // TODO Teach it how to handle grayscale BufferedImages directly
                // because this is pretty inefficient
                convertToGrayScaleImage(io, buf,
                        image.getWidth(), image.getHeight(), this.bitsPerPixel);
            }
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Factory.create(
                    getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, ioe);
        }
    }

    /**
     * Establishes a new foreground or fill color.
     * {@inheritDoc}
     */
    public void updateColor(Color col, boolean fill) {
        if (fill) {
            currentColor = col;
        }
    }

    /**
     * Restores the state stack after a break out.
     * @param breakOutList the state stack to restore.
     */
    public void restoreStateStackAfterBreakOut(List breakOutList) {

    }

    /**
     * Breaks out of the state stack to handle fixed block-containers.
     * @return the saved state stack to recreate later
     */
    public List breakOutOfStateStack() {
        return null;
    }

    /** Saves the graphics state of the rendering engine. */
    public void saveGraphicsState() {

    }

    /** Restores the last graphics state of the rendering engine. */
    public void restoreGraphicsState() {

    }

    /** Indicates the beginning of a text object. */
    public void beginTextObject() {

    }

    /** Indicates the end of a text object. */
    public void endTextObject() {

    }

    /**
     * {@inheritDoc}
     */
    public void renderImage(Image image, Rectangle2D pos) {
        String url = image.getURL();
        drawImage(url, pos);
    }

    /**
     * {@inheritDoc}
     */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);

        String name = getInternalFontNameForArea(text);
        currentFontSize = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);

        Color col = (Color) text.getTrait(Trait.COLOR);

        int vsci = mpts2units(tf.getWidth(' ', currentFontSize) / 1000
                                + text.getTextWordSpaceAdjust()
                                + text.getTextLetterSpaceAdjust());

        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        //  where previous line area failed to take up entire allocated space
        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();

        // Set letterSpacing
        //float ls = fs.getLetterSpacing() / this.currentFontSize;

        String worddata = text.getText();

        // Create an AFPFontAttributes object from the current font details
        AFPFontAttributes afpFontAttributes = new AFPFontAttributes(name, tf, currentFontSize);

        if (!currentPageFonts.containsKey(afpFontAttributes.getFontKey())) {
            // Font not found on current page, so add the new one
            pageFontCounter++;
            afpFontAttributes.setFontReference(pageFontCounter);
            currentPageFonts.put(
               afpFontAttributes.getFontKey(),
               afpFontAttributes);

        } else {
            // Use the previously stored font attributes
            afpFontAttributes = (AFPFontAttributes) currentPageFonts.get(
                    afpFontAttributes.getFontKey());
        }

        // Try and get the encoding to use for the font
        String encoding = null;

        try {
            encoding = tf.getCharacterSet(currentFontSize).getEncoding();
        } catch (Throwable ex) {
            encoding = AFPConstants.EBCIDIC_ENCODING;
            log.warn(
                "renderText():: Error getting encoding for font '"
                + tf.getFullName()
                + "' - using default encoding "
                + encoding);
        }

        try {
            afpDataStream.createText(
                afpFontAttributes.getFontReference(),
                mpts2units(rx),
                mpts2units(bl),
                col,
                vsci,
                mpts2units(text.getTextLetterSpaceAdjust()),
                worddata.getBytes(encoding));
        } catch (UnsupportedEncodingException usee) {
            log.error(
                "renderText:: Font "
                + afpFontAttributes.getFontKey()
                + " caused UnsupportedEncodingException");
        }

        super.renderText(text);

        renderTextDecoration(tf, currentFontSize, text, bl, rx);
    }

    /**
     * {@inheritDoc}
     */
    public void renderWord(WordArea word) {
        // UNUSED
        // String name = getInternalFontNameForArea(word.getParentArea());
        // int size = ((Integer)
        // word.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        // AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);
        //
        // String s = word.getWord();
        //
        // FontMetrics metrics = fontInfo.getMetricsFor(name);

        super.renderWord(word);
    }

    /**
     * {@inheritDoc}
     */
    public void renderSpace(SpaceArea space) {
        // UNUSED
        // String name = getInternalFontNameForArea(space.getParentArea());
        // int size = ((Integer)
        // space.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        // AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);
        //
        // String s = space.getSpace();
        //
        // FontMetrics metrics = fontInfo.getMetricsFor(name);

        super.renderSpace(space);
    }

    /**
     * Render leader area.
     * This renders a leader area which is an area with a rule.
     * @param area the leader area to render
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        int style = area.getRuleStyle();
        float startx = (currentIPPosition + area.getBorderAndPaddingWidthStart()) / 1000f;
        float starty = (currentBPPosition + area.getOffset()) / 1000f;
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart()
                        + area.getIPD()) / 1000f;
        float ruleThickness = area.getRuleThickness() / 1000f;
        Color col = (Color)area.getTrait(Trait.COLOR);

        switch (style) {
            case EN_SOLID:
            case EN_DASHED:
            case EN_DOUBLE:
            case EN_DOTTED:
            case EN_GROOVE:
            case EN_RIDGE:
                drawBorderLine(startx, starty, endx, starty + ruleThickness,
                        true, true, style, col);
                break;
            default:
                throw new UnsupportedOperationException("rule style not supported");
        }
        super.renderLeader(area);
    }

    /**
     * Sets the AFPRenderer options
     * @param options   the <code>Map</code> containing the options
     */
// UNUSED
//     public void setOptions(Map options) {
//    
//         this.afpOptions = options;
//    
//     }
    /**
     * Determines the orientation from the string representation, this method
     * guarantees to return a value of either 0, 90, 180 or 270.
     *
     * @return the orientation
     */
// UNUSED
//     private int getOrientation(String orientationString) {
//
//        int orientation = 0;
//        if (orientationString != null && orientationString.length() > 0) {
//            try {
//                orientation = Integer.parseInt(orientationString);
//            } catch (NumberFormatException nfe) {
//                log.error("Cannot use orientation of " + orientation
//                        + " defaulting to zero.");
//                orientation = 0;
//            }
//        } else {
//            orientation = 0;
//        }
//        switch (orientation) {
//        case 0:
//            break;
//        case 90:
//            break;
//        case 180:
//            break;
//        case 270:
//            break;
//        default:
//            log.error("Cannot use orientation of " + orientation
//                    + " defaulting to zero.");
//            orientation = 0;
//            break;
//        }
//
//        return orientation;
//
//    }
    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     * 
     * @param rotation
     *            The rotation in degrees.
     */
    public void setPortraitRotation(int rotation) {

        if (rotation == 0
            || rotation == 90
            || rotation == 180
            || rotation == 270) {
            portraitRotation = rotation;
        } else {
            throw new IllegalArgumentException("The portrait rotation must be one"
                + " of the values 0, 90, 180, 270");

        }

    }

    /**
     * Sets the rotation to be used for landsacpe pages, valid values are 0, 90,
     * 180, 270 (default).
     *  
     * @param rotation
     *            The rotation in degrees.
     */
    public void setLandscapeRotation(int rotation) {

        if (rotation == 0
            || rotation == 90
            || rotation == 180
            || rotation == 270) {
            landscapeRotation = rotation;
        } else {
            throw new IllegalArgumentException("The landscape rotation must be one"
                + " of the values 0, 90, 180, 270");
        }

    }

    /**
     * Get the MIME type of the renderer.
     * 
     * @return   The MIME type of the renderer
     */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    /**
     * Method to render the page extension.
     * <p>
     * 
     * @param pageViewport the page object
     */
    private void renderPageObjectExtensions(PageViewport pageViewport) {

        pageSegmentsMap = null;
        if (pageViewport.getExtensionAttachments() != null
                && pageViewport.getExtensionAttachments().size() > 0) {
            // Extract all AFPPageSetup instances from the attachment list on
            // the s-p-m
            Iterator i = pageViewport.getExtensionAttachments().iterator();
            while (i.hasNext()) {
                ExtensionAttachment attachment = (ExtensionAttachment)i.next();
                if (AFPPageSetup.CATEGORY.equals(attachment.getCategory())) {
                    AFPPageSetup aps = (AFPPageSetup) attachment;
                    String element = aps.getElementName();
                    if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(element)) {
                        String overlay = aps.getName();
                        if (overlay != null) {
                            afpDataStream.createIncludePageOverlay(overlay);
                        }
                    } else if (AFPElementMapping.INCLUDE_PAGE_SEGMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String source = aps.getValue();
                        if (pageSegmentsMap == null) {
                            pageSegmentsMap = new HashMap();
                        }
                        pageSegmentsMap.put(source, name);
                    } else if (AFPElementMapping.TAG_LOGICAL_ELEMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String value = aps.getValue();
                        if (pageSegmentsMap == null) {
                            pageSegmentsMap = new HashMap();
                        }
                        afpDataStream.createTagLogicalElement(name, value);
                    } else if (AFPElementMapping.NO_OPERATION.equals(element)) {
                        String content = aps.getContent();
                        if (content != null) {
                            afpDataStream.createNoOperation(content);
                        }
                    }
                }
            }
        }

    }

    /**
     * Converts FOP mpt measurement to afp measurement units
     * @param mpt the millipoints value
     */
    private int mpts2units(int mpt) {
        return mpts2units((double) mpt);
    }

    /**
     * Converts FOP pt measurement to afp measurement units
     * @param mpt the millipoints value
     */
    private int pts2units(float mpt) {
        return mpts2units(mpt * 1000d);
    }

    /**
     * Converts FOP mpt measurement to afp measurement units
     * 
     * @param mpt
     *            the millipoints value
     * @return afp measurement unit value
     */
    private int mpts2units(double mpt) {
        return (int)Math.round(mpt / (DPI_CONVERSION_FACTOR / getResolution()));
    }

    /**
     * Converts a byte array containing 24 bit RGB image data to a grayscale
     * image.
     * 
     * @param io
     *            the target image object
     * @param raw
     *            the buffer containing the RGB image data
     * @param width
     *            the width of the image in pixels
     * @param height
     *            the height of the image in pixels
     * @param bitsPerPixel
     *            the number of bits to use per pixel
     */
    protected static void convertToGrayScaleImage(ImageObject io, byte[] raw, int width,
            int height, int bitsPerPixel) {
        int pixelsPerByte = 8 / bitsPerPixel;
        int bytewidth = (width / pixelsPerByte);
        if ((width % pixelsPerByte) != 0) {
            bytewidth++;
        }
        byte[] bw = new byte[height * bytewidth];
        byte ib;
        for (int y = 0; y < height; y++) {
            ib = 0;
            int i = 3 * y * width;
            for (int x = 0; x < width; x++, i += 3) {

                // see http://www.jguru.com/faq/view.jsp?EID=221919
                double greyVal = 0.212671d * ((int) raw[i] & 0xff) + 0.715160d
                        * ((int) raw[i + 1] & 0xff) + 0.072169d
                        * ((int) raw[i + 2] & 0xff);
                switch (bitsPerPixel) {
                case 1:
                    if (greyVal < 128) {
                        ib |= (byte) (1 << (7 - (x % 8)));
                    }
                    break;
                case 4:
                    greyVal /= 16;
                    ib |= (byte) ((byte) greyVal << ((1 - (x % 2)) * 4));
                    break;
                case 8:
                    ib = (byte) greyVal;
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported bits per pixel: " + bitsPerPixel);
                }

                if ((x % pixelsPerByte) == (pixelsPerByte - 1)
                        || ((x + 1) == width)) {
                    bw[(y * bytewidth) + (x / pixelsPerByte)] = ib;
                    ib = 0;
                }
            }
        }
        io.setImageIDESize((byte) bitsPerPixel);
        io.setImageData(bw);
    }
    
    private final class ViewPortPos {
        private int x = 0;

        private int y = 0;

        private int rot = 0;

        private ViewPortPos() {
        }

        private ViewPortPos(Rectangle2D view, CTM ctm) {
            ViewPortPos currentVP = (ViewPortPos) viewPortPositions
                    .get(viewPortPositions.size() - 1);
            int xOrigin;
            int yOrigin;
            int width;
            int height;
            switch (currentVP.rot) {
            case 90:
                width = mpts2units(view.getHeight());
                height = mpts2units(view.getWidth());
                xOrigin = pageWidth - width - mpts2units(view.getY())
                        - currentVP.y;
                yOrigin = mpts2units(view.getX()) + currentVP.x;
                break;
            case 180:
                width = mpts2units(view.getWidth());
                height = mpts2units(view.getHeight());
                xOrigin = pageWidth - width - mpts2units(view.getX())
                        - currentVP.x;
                yOrigin = pageHeight - height - mpts2units(view.getY())
                        - currentVP.y;
                break;
            case 270:
                width = mpts2units(view.getHeight());
                height = mpts2units(view.getWidth());
                xOrigin = mpts2units(view.getY()) + currentVP.y;
                yOrigin = pageHeight - height - mpts2units(view.getX())
                        - currentVP.x;
                break;
            default:
                xOrigin = mpts2units(view.getX()) + currentVP.x;
                yOrigin = mpts2units(view.getY()) + currentVP.y;
                width = mpts2units(view.getWidth());
                height = mpts2units(view.getHeight());
                break;
            }
            this.rot = currentVP.rot;
            double[] ctmf = ctm.toArray();
            if (ctmf[0] == 0.0d && ctmf[1] == -1.0d && ctmf[2] == 1.0d
                    && ctmf[3] == 0.d) {
                this.rot += 270;
            } else if (ctmf[0] == -1.0d && ctmf[1] == 0.0d && ctmf[2] == 0.0d
                    && ctmf[3] == -1.0d) {
                this.rot += 180;
            } else if (ctmf[0] == 0.0d && ctmf[1] == 1.0d && ctmf[2] == -1.0d
                    && ctmf[3] == 0.0d) {
                this.rot += 90;
            }
            this.rot %= 360;
            switch (this.rot) {
            /*
             * case 0: this.x = mpts2units(view.getX()) + x; this.y =
             * mpts2units(view.getY()) + y; break; case 90: this.x =
             * mpts2units(view.getY()) + y; this.y = _pageWidth -
             * mpts2units(view.getX() + view.getWidth()) - x; break; case 180:
             * this.x = _pageWidth - mpts2units(view.getX() + view.getWidth()) -
             * x; this.y = _pageHeight - mpts2units(view.getY() +
             * view.getHeight()) - y; break; case 270: this.x = _pageHeight -
             * mpts2units(view.getY() + view.getHeight()) - y; this.y =
             * mpts2units(view.getX()) + x; break;
             */
            case 0:
                this.x = xOrigin;
                this.y = yOrigin;
                break;
            case 90:
                this.x = yOrigin;
                this.y = pageWidth - width - xOrigin;
                break;
            case 180:
                this.x = pageWidth - width - xOrigin;
                this.y = pageHeight - height - yOrigin;
                break;
            case 270:
                this.x = pageHeight - height - yOrigin;
                this.y = xOrigin;
                break;
            default:
            }
        }

        public String toString() {
            return "x:" + x + " y:" + y + " rot:" + rot;
        }

    }

    private List viewPortPositions = new ArrayList();

    private void pushViewPortPos(ViewPortPos vpp) {
        viewPortPositions.add(vpp);
        afpDataStream.setOffsets(vpp.x, vpp.y, vpp.rot);
    }

    private void popViewPortPos() {
        viewPortPositions.remove(viewPortPositions.size() - 1);
        if (viewPortPositions.size() > 0) {
            ViewPortPos vpp = (ViewPortPos)viewPortPositions.get(viewPortPositions.size() - 1);
            afpDataStream.setOffsets(vpp.x, vpp.y, vpp.rot);
        }
    }

    /**
     * Sets the number of bits used per pixel
     * 
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        this.bitsPerPixel = bitsPerPixel;
        switch (bitsPerPixel) {
            case 1:
            case 4:
            case 8:
            break;
        default:
            log.warn("Invalid bits_per_pixel value, must be 1, 4 or 8.");
            bitsPerPixel = 8;
            break;
        }
    }

    /**
     * Sets whether images are color or not
     * 
     * @param colorImages
     *            color image output
     */
    public void setColorImages(boolean colorImages) {
        this.colorImages = colorImages;
    }

    /**
     * Sets the output/device resolution
     * 
     * @param resolution
     *            the output resolution (dpi)
     */
    public void setResolution(int resolution) {
        if (log.isDebugEnabled()) {
            log.debug("renderer-resolution set to: " + resolution + "dpi");
        }
        this.resolution = resolution;
    }
    
    /**
     * Returns the output/device resolution.
     * @return the resolution in dpi
     */
    public int getResolution() {
        return this.resolution;
    }
}
