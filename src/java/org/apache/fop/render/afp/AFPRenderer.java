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
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.output.ByteArrayOutputStream;
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
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.TIFFImage;
import org.apache.fop.image.XMLImage;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.extensions.AFPPageSetup;
import org.apache.fop.render.afp.fonts.AFPFontInfo;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.fonts.CharacterSet;
import org.apache.fop.render.afp.fonts.FopCharacterSet;
import org.apache.fop.render.afp.fonts.OutlineFont;
import org.apache.fop.render.afp.fonts.RasterFont;
import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.render.afp.modca.ImageObject;
import org.apache.fop.render.afp.modca.PageObject;
import org.w3c.dom.Document;


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
 * document. In order to save memory it is possble to render the pages out of
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
 * miscellanous objects that are required to draw into the page.
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
 * The relevent AreaTree structures that will need to be rendered are Page,
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
     * AFP factor for a 240 resolution = 72000/240 = 300
     */
    private static final int DPI_CONVERSION_FACTOR_240 = 300;

    /**
     * The afp data stream object responsible for generating afp data
     */
    private AFPDataStream _afpDataStream = null;

    /**
     * The map of afp root extensions
     */
    private HashMap _rootExtensionMap = null;

    /**
     * The map of page segments
     */
    private HashMap _pageSegmentsMap = null;

    /**
     * The fonts on the current page
     */
    private HashMap _currentPageFonts = null;

    /**
     * The current color object
     */
    private Color _currentColor = null;

    /**
     * The page font number counter, used to determine the next font reference
     */
    private int _pageFontCounter = 0;

    /**
     * The current font family
     */
    private String _currentFontFamily = "";

    /**
     * The current font size
     */
    private int _currentFontSize = 0;

    /**
     * The Options to be set on the AFPRenderer
     */
    private Map _afpOptions = null;

    /**
     * The page width
     */
    private int _pageWidth = 0;

    /**
     * The page height
     */
    private int _pageHeight = 0;

    /**
     * The current page sequence id
     */
    private String _pageSequenceId = null;

    /**
     * The portrait rotation
     */
    private int _portraitRotation = 0;

    /**
     * The landscape rotation
     */
    private int _landscapeRotation = 270;

    /**
     * The line cache, avoids drawing duplicate lines in tables.
     */
    private HashSet _lineCache = null;

    /**
     * The current x position for line drawing
     */
    private float _x;

    /**
     * The current y position for line drawing
     */
    private float _y;

    /**
     * The map of saved incomplete pages
     */
    private Map _pages = null;

    /**
     * Flag to the set the output object type for images
     */
    private boolean colorImages = false;

    /**
     * Default value for image depth
     */
    private int bitsPerPixel = 8;

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
            for (Iterator it = this.fontList.iterator(); it.hasNext(); ) {
                AFPFontInfo afi = (AFPFontInfo)it.next();
                AFPFont bf = (AFPFont)afi.getAFPFont();
                for (Iterator it2 = afi.getFontTriplets().iterator(); it2.hasNext(); ) {
                    FontTriplet ft = (FontTriplet)it2.next();
                    this.fontInfo.addFontProperties("F" + num, ft.getName()
                                                    , ft.getStyle(), ft.getWeight());
                    this.fontInfo.addMetrics("F" + num, bf);
                    num++;
                }
            }
        } else {
            log.warn("No AFP fonts configured - using default setup");
        }
        if (this.fontInfo.fontLookup("sans-serif", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZH200  ", 1, new Helvetica());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "sans-serif", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("serif", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZN200  ", 1, new TimesRoman());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "serif", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("monospace", "normal", 400) == null) {
            CharacterSet cs  = new FopCharacterSet("T1V10500", "Cp500", "CZ4200  ", 1, new Courier());
            AFPFont bf = new OutlineFont("Helvetica", cs);
            this.fontInfo.addFontProperties("F" + num, "monospace", "normal", 400);
            this.fontInfo.addMetrics("F" + num, bf);
            num++;
        }
        if (this.fontInfo.fontLookup("any", "normal", 400) == null) {
            FontTriplet ft = this.fontInfo.fontLookup("sans-serif", "normal", 400);
            this.fontInfo.addFontProperties(this.fontInfo.getInternalFontKey(ft), "any", "normal", 400);
        }
    }

    /**
     */
    private AFPFontInfo buildFont(Configuration fontCfg, String _path)
        throws ConfigurationException {

        Configuration[] triple = fontCfg.getChildren("font-triplet");
        List tripleList = new java.util.ArrayList();
        if (triple.length == 0) {
            log.error("Mandatory font configuration element '<font-triplet...' is missing");
            return null;
        }
        for (int j = 0; j < triple.length; j++) {
            int weight = FontUtil.parseCSS2FontWeight(triple[j].getAttribute("weight"));
            tripleList.add(new FontTriplet(triple[j].getAttribute("name"),
                                           triple[j].getAttribute("style"),
                                           weight));
        }

        //build the fonts
        Configuration afpFontCfg = fontCfg.getChild("afp-font");
        if (afpFontCfg == null) {
            log.error("Mandatory font configuration element '<afp-font...' is missing");
            return null;
        }
        String path = afpFontCfg.getAttribute("path", _path);
        String type = afpFontCfg.getAttribute("type");
        if (type == null) {
            log.error("Mandatory afp-font configuration attribute 'type=' is missing");
            return null;
        }
        String codepage = afpFontCfg.getAttribute("codepage");
        if (codepage == null) {
            log.error("Mandatory afp-font configuration attribute 'code=' is missing");
            return null;
        }
        String encoding = afpFontCfg.getAttribute("encoding");
        if (encoding == null) {
            log.error("Mandatory afp-font configuration attribute 'encoding=' is missing");
            return null;
        }

        if ("raster".equalsIgnoreCase(type)) {

            String name = afpFontCfg.getAttribute("name", "Unknown");

            // Create a new font object
            RasterFont font = new RasterFont(name);

            Configuration[] rasters = afpFontCfg.getChildren("afp-raster-font");
            if (rasters.length == 0) {
                log.error("Mandatory font configuration elements '<afp-raster-font...' are missing");
                return null;
            }
            for (int j = 0; j < rasters.length; j++) {
                Configuration rasterCfg = rasters[j];

                String characterset = rasterCfg.getAttribute("characterset");
                if (characterset == null) {
                    log.error("Mandatory afp-raster-font configuration attribute 'characterset=' is missing");
                    return null;
                }
                int size = rasterCfg.getAttributeAsInteger("size");
                String base14 = rasterCfg.getAttribute("base14-font", null);

                if (base14 != null) {
                    try {
                        Class clazz = Class.forName("org.apache.fop.fonts.base14."
                            + base14);
                        try {
                            Typeface tf = (Typeface)clazz.newInstance();
                            font.addCharacterSet(size, new FopCharacterSet(
                                codepage, encoding, characterset, size, tf));
                        } catch (Exception ie) {
                            String msg = "The base 14 font class " + clazz.getName()
                                + " could not be instantiated";
                            log.error(msg);
                        }
                    } catch (ClassNotFoundException cnfe) {
                        String msg = "The base 14 font class for " + characterset
                            + " could not be found";
                        log.error(msg);
                    }
                } else {
                    font.addCharacterSet(size, new CharacterSet(
                        codepage, encoding, characterset, path));
                }
            }
            return new AFPFontInfo(font, tripleList);

        } else if ("outline".equalsIgnoreCase(type)) {

            String characterset = afpFontCfg.getAttribute("characterset");
            if (characterset == null) {
                log.error("Mandatory afp-font configuration attribute 'characterset=' is missing");
                return null;
            }
            String name = afpFontCfg.getAttribute("name", characterset);

            CharacterSet characterSet = null;

            String base14 = afpFontCfg.getAttribute("base14-font", null);

            if (base14 != null) {
                try {
                    Class clazz = Class.forName("org.apache.fop.fonts.base14."
                        + base14);
                    try {
                        Typeface tf = (Typeface)clazz.newInstance();
                        characterSet = new FopCharacterSet(
                                codepage, encoding, characterset, 1, tf);
                    } catch (Exception ie) {
                        String msg = "The base 14 font class " + clazz.getName()
                            + " could not be instantiated";
                        log.error(msg);
                    }
                } catch (ClassNotFoundException cnfe) {
                    String msg = "The base 14 font class for " + characterset
                        + " could not be found";
                    log.error(msg);
                }
            } else {
                characterSet = new CharacterSet(codepage, encoding, characterset, path);
            }
            // Create a new font object
            OutlineFont font = new OutlineFont(name, characterSet);
            return new AFPFontInfo(font, tripleList);
        } else {
            log.error("No or incorrect type attribute");
        }
        return null;
    }

    /**
     * Builds a list of AFPFontInfo objects for use with the setup() method.
     * @param cfg Configuration object
     * @return List the newly created list of fonts
     * @throws ConfigurationException if something's wrong with the config data
     */
    public List buildFontListFromConfiguration(Configuration cfg)
            throws ConfigurationException {
        List fontList = new java.util.ArrayList();
        Configuration[] font = cfg.getChild("fonts").getChildren("font");
        for (int i = 0; i < font.length; i++) {
            AFPFontInfo afi = buildFont(font[i], null);
            if (afi != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding font " + afi.getAFPFont().getFontName());
                    for (int j = 0; j < afi.getFontTriplets().size(); ++j) {
                        FontTriplet triplet = (FontTriplet) afi.getFontTriplets().get(j);
                        log.debug("Font triplet "
                                  + triplet.getName() + ", "
                                  + triplet.getStyle() + ", "
                                  + triplet.getWeight());
                    }
                }

                fontList.add(afi);
            }
        }
        return fontList;
    }

    /**
     * Configure the AFP renderer.
     * Get the configuration to be used for fonts etc.
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        //Font configuration
        this.fontList = buildFontListFromConfiguration(cfg);
        Configuration images = cfg.getChild("images");
        if (!"color".equalsIgnoreCase(images.getAttribute("mode", "b+w"))) {
            bitsPerPixel = images.getAttributeAsInteger("bits-per-pixel", 8);
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
        } else {
            colorImages = true;
        }

    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream)
     */
    public void startRenderer(OutputStream outputStream) throws IOException {
        _currentPageFonts = new HashMap();
        _currentColor = new Color(255, 255, 255);
        _afpDataStream = new AFPDataStream();
        _afpDataStream.setPortraitRotation(_portraitRotation);
        _afpDataStream.setLandscapeRotation(_landscapeRotation);
        _afpDataStream.startDocument(outputStream);
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer(java.io.OutputStream)
     */
    public void stopRenderer() throws IOException {
        _afpDataStream.endDocument();
    }

    /**
     * @see org.apache.fop.render.Renderer#supportsOutOfOrder()
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
     * @see org.apache.fop.render.Renderer#preparePage(PageViewport)
     */
    public void preparePage(PageViewport page) {
        // initializeRootExtensions(page);

        _currentFontFamily = "";
        _currentFontSize = 0;
        _pageFontCounter = 0;
        _currentPageFonts.clear();
        _lineCache = new HashSet();

        Rectangle2D bounds = page.getViewArea();

        _pageWidth = mpts2units(bounds.getWidth());
        _pageHeight = mpts2units(bounds.getHeight());

        // renderPageGroupExtensions(page);

        _afpDataStream.startPage(_pageWidth, _pageHeight, 0);

        renderPageObjectExtensions(page);

        if (_pages == null) {
            _pages = new HashMap();
        }
        _pages.put(page, _afpDataStream.savePage());

    }

    /**
     * @see org.apache.fop.render.Renderer#processOffDocumentItem(OffDocumentItem)
     */
    public void processOffDocumentItem(OffDocumentItem odi) {
        // TODO
    }

    /** @see org.apache.fop.render.Renderer#getGraphics2DAdapter() */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new AFPGraphics2DAdapter();
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    public void startVParea(CTM ctm, Rectangle2D clippingRect) {
        // dummy not used
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
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
     * @see org.apache.fop.render.AbstractRenderer#renderBlockViewport(BlockViewport, List)
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;
        //String saveFontName = currentFontName;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();
        float x, y;
        x = (float)(bv.getXOffset() + containingIPPosition) / 1000f;
        y = (float)(bv.getYOffset() + containingBPPosition) / 1000f;
        //This is the content-rect
        float width = (float)bv.getIPD() / 1000f;
        float height = (float)bv.getBPD() / 1000f;


        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            currentIPPosition = bv.getXOffset();
            currentBPPosition = bv.getYOffset();

            //For FIXED, we need to break out of the current viewports to the
            //one established by the page. We save the state stack for restoration
            //after the block-container has been painted. See below.
            List breakOutList = null;
            if (bv.getPositioning() == Block.FIXED) {
                breakOutList = breakOutOfStateStack();
            }

            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);

            //Adjust for spaces (from margin or indirectly by start-indent etc.
            x += bv.getSpaceStart() / 1000f;
            currentIPPosition += bv.getSpaceStart();

            y += bv.getSpaceBefore() / 1000f;
            currentBPPosition += bv.getSpaceBefore();

            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd()) / 1000f;
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter()) / 1000f;

            drawBackAndBorders(bv, x, y, width + bpwidth, height + bpheight);

            //Now adjust for border/padding
            currentIPPosition += borderPaddingStart;
            currentBPPosition += borderPaddingBefore;

            Rectangle2D clippingRect = null;
            clippingRect = new Rectangle(currentIPPosition, currentBPPosition,
                    bv.getIPD(), bv.getBPD());

            // startVParea(ctm, clippingRect);
            pushViewPortPos(new ViewPortPos(clippingRect, ctm));
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);
            //endVParea();
            popViewPortPos();

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
        //currentFontName = saveFontName;
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page) {

        // initializeRootExtensions(page);

        _currentFontFamily = "";
        _currentFontSize = 0;
        _pageFontCounter = 0;
        _currentPageFonts.clear();
        _lineCache = new HashSet();

        Rectangle2D bounds = page.getViewArea();

        _pageWidth = mpts2units(bounds.getWidth());
        _pageHeight = mpts2units(bounds.getHeight());

        if (_pages != null && _pages.containsKey(page)) {

            _afpDataStream.restorePage((PageObject)_pages.remove(page));

        } else {
            // renderPageGroupExtensions(page);

            _afpDataStream.startPage(_pageWidth, _pageHeight, 0);

            renderPageObjectExtensions(page);

        }

        pushViewPortPos(new ViewPortPos());

        renderPageAreas(page.getPage());

        Iterator i = _currentPageFonts.values().iterator();
        while (i.hasNext()) {
            AFPFontAttributes afpFontAttributes = (AFPFontAttributes) i.next();

            _afpDataStream.createFont(
                afpFontAttributes.getFontReference(),
                afpFontAttributes.getFont(),
                afpFontAttributes.getPointSize());

        }

        try {
            _afpDataStream.endPage();
        } catch (IOException ioex) {
            // TODO What shall we do?
        }

        popViewPortPos();

    }

    /**
     * Clip using the current path.
     * @see org.apache.fop.render.AbstractRenderer#clip
     */
    public void clip() {
        // TODO
    }

    /**
     * Clip using a rectangular area.
     * @see org.apache.fop.render.AbstractRenderer#clipRect(float, float, float, float)
     */
    public void clipRect(float x, float y, float width, float height) {
        // TODO
    }

    /**
     * Moves the current point to (x, y), omitting any connecting line segment.
     * @see org.apache.fop.render.AbstractRenderer#moveTo(float, float)
     */
    public void moveTo(float x, float y) {
        // TODO
    }

    /**
     * Appends a straight line segment from the current point to (x, y). The
     * new current point is (x, y).
     * @see org.apache.fop.render.AbstractRenderer#lineTo(float, float)
     */
    public void lineTo(float x, float y) {
        // TODO
    }

    /**
     * Closes the current subpath by appending a straight line segment from
     * the current point to the starting point of the subpath.
     * @see org.apache.fop.render.AbstractRenderer#closePath
     */
    public void closePath() {
        // TODO
    }

    /**
     * Fill a rectangular area.
     * @see org.apache.fop.render.AbstractRenderer#fillRect(float, float, float, float)
     */
    public void fillRect(float x, float y, float width, float height) {
        /*
        _afpDataStream.createShading(
            pts2units(x),
            pts2units(y),
            pts2units(width),
            pts2units(height),
            _currentColor.getRed(),
            _currentColor.getGreen(),
            _currentColor.getBlue());
         */
        _afpDataStream.createLine(
            pts2units(x),
            pts2units(y),
            pts2units(x + width),
            pts2units(y),
            pts2units(height),
            _currentColor);
    }

    /**
     * Draw a border segment of an XSL-FO style border.
     * @see org.apache.fop.render.AbstractRenderer#drawBorderLine(float, float, float, float,
     *       boolean, boolean, int, Color)
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
                    _afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1),
                        pts2units(x2),
                        pts2units(ym1),
                        pts2units(h3),
                        col
                    );
                    _afpDataStream.createLine(
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
                    _afpDataStream.createLine(
                        pts2units(xm1),
                        pts2units(y1),
                        pts2units(xm1),
                        pts2units(y2),
                        pts2units(w3),
                        col
                    );
                    _afpDataStream.createLine(
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
                        _afpDataStream.createLine(
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
                        _afpDataStream.createLine(
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
                        _afpDataStream.createLine(
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
                        _afpDataStream.createLine(
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
                    _afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1),
                        pts2units(x2),
                        pts2units(ym1),
                        pts2units(h3),
                        uppercol
                    );
                    _afpDataStream.createLine(
                        pts2units(x1),
                        pts2units(ym1 + h3),
                        pts2units(x2),
                        pts2units(ym1 + h3),
                        pts2units(h3),
                        col
                    );
                    _afpDataStream.createLine(
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
                    _afpDataStream.createLine(
                        pts2units(xm1),
                        pts2units(y1),
                        pts2units(xm1),
                        pts2units(y2),
                        pts2units(w3),
                        leftcol
                    );
                    _afpDataStream.createLine(
                        pts2units(xm1 + w3),
                        pts2units(y1),
                        pts2units(xm1 + w3),
                        pts2units(y2),
                        pts2units(w3),
                        col
                    );
                    _afpDataStream.createLine(
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
                _afpDataStream.createLine(
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
     * @see org.apache.fop.render.PrintRenderer#createRendererContext(
     *          int, int, int, int, java.util.Map)
     */
    protected RendererContext createRendererContext(int x, int y, int width, int height, Map foreignAttributes) {
        RendererContext context;
        context = super.createRendererContext(x, y, width, height, foreignAttributes);
        context.setProperty(AFPRendererContextConstants.AFP_GRAYSCALE, 
                new Boolean(!this.colorImages));
        return context;
    }

    /**
     * Draw an image at the indicated location.
     * @see org.apache.fop.render.AbstractRenderer#drawImage(String, Rectangle2D, Map)
     */
    public void drawImage(String url, Rectangle2D pos, Map foreignAttributes) {
        String name = null;
        if (_pageSegmentsMap != null) {
            name = (String)_pageSegmentsMap.get(url);
        }
        if (name != null) {
            int x = mpts2units(pos.getX() + currentIPPosition);
            int y = mpts2units(pos.getY() + currentBPPosition);
            _afpDataStream.createIncludePageSegment(name, x, y);
        } else {
            url = ImageFactory.getURL(url);
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
            /*
            } else if (MimeConstants.MIME_JPEG.equals(mime)) {
                if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                    return;
                }
                fact.releaseImage(url, userAgent);

                int x = mpts2units(pos.getX() + currentIPPosition);
                int y = mpts2units(pos.getY() + currentBPPosition);
                int w = mpts2units(pos.getWidth());
                int h = mpts2units(pos.getHeight());
                ImageObject io = _afpDataStream.getImageObject();
                io.setImageViewport(x, y, w, h);
                io.setImageParameters(
                    (int)(fopimage.getHorizontalResolution() * 10),
                    (int)(fopimage.getVerticalResolution() * 10),
                    fopimage.getWidth(),
                    fopimage.getHeight()
                );
                io.setImageIDESize((byte)fopimage.getBitsPerPixel());
                io.setImageEncoding((byte)0x83);
                io.setImageData(fopimage.getRessourceBytes());
            */
            } else if (MimeConstants.MIME_TIFF.equals(mime)
                        && fopimage instanceof TIFFImage) {
                TIFFImage tiffImage = (TIFFImage) fopimage;
                int x = mpts2units(pos.getX() + currentIPPosition);
                int y = mpts2units(pos.getY() + currentBPPosition);
                int w = mpts2units(pos.getWidth());
                int h = mpts2units(pos.getHeight());
                ImageObject io = _afpDataStream.getImageObject(x, y, w, h);
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
                        convertToGrayScaleImage(io, fopimage.getBitmaps());
                    }
                } else {
                    if (!fopimage.load(FopImage.BITMAP)) {
                        return;
                    }
                    convertToGrayScaleImage(io, fopimage.getBitmaps());
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
                ImageObject io = _afpDataStream.getImageObject(x, y, w, h);
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
                    convertToGrayScaleImage(io, fopimage.getBitmaps());
                }
            }
        }
    }

    /**
     * Writes a BufferedImage to an OutputStream as raw sRGB bitmaps.
     * @param img the BufferedImage
     * @param out the OutputStream
     * @throws IOException In case of an I/O error.
     */
    public static void writeImage(BufferedImage img, OutputStream out) throws IOException {
        int w  = img.getWidth();
        int h  = img.getHeight();
        int[] tmpMap = img.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int p = tmpMap[i * w + j];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = (p) & 0xFF;
                out.write((byte)(r & 0xFF));
                out.write((byte)(g & 0xFF));
                out.write((byte)(b & 0xFF));
            }
        }
    }
    
    /**
     * Draws a BufferedImage to AFP.
     * @param bi the BufferedImage
     * @param resolution the resolution of the BufferedImage
     * @param x the x coordinate (in mpt)
     * @param y the y coordinate (in mpt)
     * @param w the width of the viewport (in mpt)
     * @param h the height of the viewport (in mpt)
     */
    public void drawBufferedImage(BufferedImage bi, int resolution, int x, int y, int w, int h) {
        int afpx = mpts2units(x);
        int afpy = mpts2units(y);
        int afpw = mpts2units(w);
        int afph = mpts2units(h);
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        try {
            //Serialize image
            writeImage(bi, baout);
            byte[] buf = baout.toByteArray();
            
            //Generate image
            ImageObject io = _afpDataStream.getImageObject(afpx, afpy, afpw, afph);
            io.setImageParameters(
                resolution, resolution,
                bi.getWidth(),
                bi.getHeight()
            );
            if (colorImages) {
                io.setImageIDESize((byte)24);
                io.setImageData(buf);
            } else {
                //TODO Teach it how to handle grayscale BufferedImages directly
                //because this is pretty inefficient
                convertToGrayScaleImage(io, buf);
            }
        } catch (IOException ioe) {
            log.error("Error while serializing bitmap: " + ioe.getMessage(), ioe);
        }
    }
    
    /**
     * Establishes a new foreground or fill color.
     * @see org.apache.fop.render.AbstractRenderer#updateColor(Color, boolean)
     */
    public void updateColor(Color col, boolean fill) {
        if (fill) {
            _currentColor = col;
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
     * @see org.apache.fop.render.AbstractRenderer#renderImage(Image, Rectangle2D)
     */
    public void renderImage(Image image, Rectangle2D pos) {
        String url = image.getURL();
        drawImage(url, pos);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);

        String name = getInternalFontNameForArea(text);
        _currentFontSize = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);

        Color col = (Color) text.getTrait(Trait.COLOR);

        int vsci = mpts2units(tf.getWidth(' ', _currentFontSize) / 1000
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
        AFPFontAttributes afpFontAttributes =
            new AFPFontAttributes(name, tf, _currentFontSize);

        if (!_currentPageFonts.containsKey(afpFontAttributes.getFontKey())) {
            // Font not found on current page, so add the new one
            _pageFontCounter++;
            afpFontAttributes.setFontReference(_pageFontCounter);
            _currentPageFonts.put(
                afpFontAttributes.getFontKey(),
                afpFontAttributes);

        } else {
            // Use the previously stored font attributes
            afpFontAttributes =
                (AFPFontAttributes) _currentPageFonts.get(
                afpFontAttributes.getFontKey());
        }

        // Try and get the encoding to use for the font
        String encoding = null;

        try {
            encoding = tf.getCharacterSet(_currentFontSize).getEncoding();
        } catch (Throwable ex) {
            encoding = AFPConstants.EBCIDIC_ENCODING;
            log.warn(
                "renderText():: Error getting encoding for font "
                + " - using default encoding "
                + encoding);
        }

        try {
            _afpDataStream.createText(
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

        renderTextDecoration(tf, _currentFontSize, text, bl, rx);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderWord(WordArea)
     */
    public void renderWord(WordArea word) {
        String name = getInternalFontNameForArea(word.getParentArea());
        int size = ((Integer) word.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);

        String s = word.getWord();

        FontMetrics metrics = fontInfo.getMetricsFor(name);

        super.renderWord(word);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderSpace(SpaceArea)
     */
    public void renderSpace(SpaceArea space) {
        String name = getInternalFontNameForArea(space.getParentArea());
        int size = ((Integer) space.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        AFPFont tf = (AFPFont) fontInfo.getFonts().get(name);

        String s = space.getSpace();

        FontMetrics metrics = fontInfo.getMetricsFor(name);

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
     * @see org.apache.fop.render.Renderer#setProducer(String)
     */
    public void setProducer(String producer) {
        _afpDataStream.setProducer(producer);
    }

    /**
     * @see org.apache.fop.render.Renderer#setOptions(Map)
     */
    public void setOptions(Map options) {

        _afpOptions = options;

    }

    /**
     * Determines the orientation from the string representation, this method
     * guarantees to return a value of either 0, 90, 180 or 270.
     *
     * @return the orientation
     */
    private int getOrientation(String orientationString) {

        int orientation = 0;
        if (orientationString != null && orientationString.length() > 0) {
            try {
                orientation = Integer.parseInt(orientationString);
            } catch (NumberFormatException nfe) {
                log.error(
                    "Cannot use orientation of "
                    + orientation
                    + " defaulting to zero.");
                orientation = 0;
            }
        } else {
            orientation = 0;
        }
        switch (orientation) {
            case 0 :
                break;
            case 90 :
                break;
            case 180 :
                break;
            case 270 :
                break;
            default :
                log.error(
                    "Cannot use orientation of "
                    + orientation
                    + " defaulting to zero.");
                orientation = 0;
                break;
        }

        return orientation;

    }

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
            _portraitRotation = rotation;
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
            _landscapeRotation = rotation;
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
     * @param page
     *            the page object
     */
    private void renderPageObjectExtensions(PageViewport page) {

        _pageSegmentsMap = null;
        if (page.getExtensionAttachments() != null
            && page.getExtensionAttachments().size() > 0) {
            //Extract all AFPPageSetup instances from the attachment list on the s-p-m
            Iterator i = page.getExtensionAttachments().iterator();
            while (i.hasNext()) {
                ExtensionAttachment attachment = (ExtensionAttachment)i.next();
                if (AFPPageSetup.CATEGORY.equals(attachment.getCategory())) {
                    AFPPageSetup aps = (AFPPageSetup)attachment;
                    String element = aps.getElementName();
                    if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(element)) { 
                        String overlay = aps.getName();
                        if (overlay != null) {
                            _afpDataStream.createIncludePageOverlay(overlay);
                        }
                    } else if (AFPElementMapping.INCLUDE_PAGE_SEGMENT.equals(element)) { 
                        String name = aps.getName();
                        String source = aps.getValue();
                        if (_pageSegmentsMap == null) {
                            _pageSegmentsMap = new HashMap();
                        }
                        _pageSegmentsMap.put(source, name);
                    } else if (AFPElementMapping.TAG_LOGICAL_ELEMENT.equals(element)) { 
                        String name = aps.getName();
                        String value = aps.getValue();
                        if (_pageSegmentsMap == null) {
                            _pageSegmentsMap = new HashMap();
                        }
                        _afpDataStream.createTagLogicalElement(name, value);
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
     * @param mpt the millipoints value
     */
    private int mpts2units(double mpt) {
        return (int)Math.round(mpt / DPI_CONVERSION_FACTOR_240);
    }

    private void convertToGrayScaleImage(ImageObject io, byte raw[]) {
        int pixelsPerByte = 8 / bitsPerPixel;
        byte bw[] = new byte[raw.length / (3 * pixelsPerByte)];
        int k = 0;
        for (int i = 0, j = 0; i < raw.length; i += 3, j++) {
            if (j == pixelsPerByte) {
                j = 0;
                k++;
            }
            // see http://www.jguru.com/faq/view.jsp?EID=221919
            double greyVal = 0.212671d * ((int) raw[i] & 0xff)
                + 0.715160d * ((int) raw[i + 1] & 0xff)
                + 0.072169d * ((int) raw[i + 2] & 0xff);
            switch (bitsPerPixel) {
                case 1:
                    if (greyVal > 128) {
                        bw[k] |= (byte)(1 << j);
                    }
                    break;
                case 4:
                    greyVal /= 16;
                    bw[k] |= (byte)((byte)greyVal << (j * 4));
                    break;
                case 8:
                    bw[k] = (byte)greyVal;
                    break;
            }
        }
        io.setImageIDESize((byte)bitsPerPixel);
        io.setImageData(bw);
    }

    private class ViewPortPos {
        int x = 0;
        int y = 0;
        int rot = 0;

        ViewPortPos() {
        }

        ViewPortPos(Rectangle2D view, CTM ctm) {
            ViewPortPos currentVP = (ViewPortPos)viewPortPositions.get(viewPortPositions.size() - 1);
            int xOrigin;
            int yOrigin;
            int width;
            int height;
            switch (currentVP.rot) {
                case 90:
                    width = mpts2units(view.getHeight());
                    height = mpts2units(view.getWidth());
                    xOrigin = _pageWidth - width - mpts2units(view.getY()) - currentVP.y;
                    yOrigin = mpts2units(view.getX()) + currentVP.x;
                    break;
                case 180:
                    width = mpts2units(view.getWidth());
                    height = mpts2units(view.getHeight());
                    xOrigin = _pageWidth - width - mpts2units(view.getX()) - currentVP.x;
                    yOrigin = _pageHeight - height - mpts2units(view.getY()) - currentVP.y;
                    break;
                case 270:
                    width = mpts2units(view.getHeight());
                    height = mpts2units(view.getWidth());
                    xOrigin = mpts2units(view.getY()) + currentVP.y;
                    yOrigin = _pageHeight - height - mpts2units(view.getX()) - currentVP.x;
                    break;
                default:
                    xOrigin = mpts2units(view.getX()) + currentVP.x;
                    yOrigin = mpts2units(view.getY()) + currentVP.y;
                    width = mpts2units(view.getWidth());
                    height = mpts2units(view.getHeight());
                    break;
            }
            this.rot = currentVP.rot;
            double ctmf[] = ctm.toArray();
            if (ctmf[0] == 0.0d && ctmf[1] == -1.0d && ctmf[2] == 1.0d && ctmf[3] == 0.d) {
                this.rot += 270;
            } else if (ctmf[0] == -1.0d && ctmf[1] == 0.0d && ctmf[2] == 0.0d && ctmf[3] == -1.0d) {
                this.rot += 180;
            } else if (ctmf[0] == 0.0d && ctmf[1] == 1.0d && ctmf[2] == -1.0d && ctmf[3] == 0.0d) {
                this.rot += 90;
            }
            this.rot %= 360;
            switch (this.rot) {
                /*
                case 0:
                    this.x = mpts2units(view.getX()) + x;
                    this.y = mpts2units(view.getY()) + y;
                    break;
                case 90:
                    this.x = mpts2units(view.getY()) + y;
                    this.y = _pageWidth - mpts2units(view.getX() + view.getWidth()) - x;
                    break;
                case 180:
                    this.x = _pageWidth - mpts2units(view.getX() + view.getWidth()) - x;
                    this.y = _pageHeight - mpts2units(view.getY() + view.getHeight()) - y;
                    break;
                case 270:
                    this.x = _pageHeight - mpts2units(view.getY() + view.getHeight()) - y;
                    this.y = mpts2units(view.getX()) + x;
                    break;
                 */
                case 0:
                    this.x = xOrigin;
                    this.y = yOrigin;
                    break;
                case 90:
                    this.x = yOrigin;
                    this.y = _pageWidth - width - xOrigin;
                    break;
                case 180:
                    this.x = _pageWidth - width - xOrigin;
                    this.y = _pageHeight - height - yOrigin;
                    break;
                case 270:
                    this.x = _pageHeight - height - yOrigin;
                    this.y = xOrigin;
                    break;
            }
        }

        public String toString() {
            return "x:" + x + " y:" + y + " rot:" + rot;
        }

    }

    private List viewPortPositions = new ArrayList();

    private void pushViewPortPos(ViewPortPos vpp) {
        viewPortPositions.add(vpp);
        _afpDataStream.setOffsets(vpp.x, vpp.y, vpp.rot);
    }

    private void popViewPortPos() {
        viewPortPositions.remove(viewPortPositions.size() - 1);
        if (viewPortPositions.size() > 0) {
            ViewPortPos vpp = (ViewPortPos)viewPortPositions.get(viewPortPositions.size() - 1);
            _afpDataStream.setOffsets(vpp.x, vpp.y, vpp.rot);
        }
    }

}

