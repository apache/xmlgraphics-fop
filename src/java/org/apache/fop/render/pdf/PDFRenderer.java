/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

/* $Id: PDFRenderer.java,v 1.38 2004/04/07 14:24:17 cbowditch Exp $ */

package org.apache.fop.render.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.List;

// XML
import org.w3c.dom.Document;

// Avalon
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.inline.Character;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.XMLImage;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEncryptionManager;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFOutline;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFState;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.fo.Constants;


/*
todo:

word rendering and optimistion
pdf state optimisation
line and border
background pattern
writing mode
text decoration

*/

/**
 * Renderer that renders areas to PDF
 *
 */
public class PDFRenderer extends PrintRenderer {
    /**
     * The mime type for pdf
     */
    public static final String MIME_TYPE = "application/pdf";

    /**
     * the PDF Document being created
     */
    protected PDFDocument pdfDoc;

    /**
     * Map of pages using the PageViewport as the key
     * this is used for prepared pages that cannot be immediately
     * rendered
     */
    protected Map pages = null;

    /**
     * Page references are stored using the PageViewport as the key
     * when a reference is made the PageViewport is used
     * for pdf this means we need the pdf page reference
     */
    protected Map pageReferences = new java.util.HashMap();

    /** Page viewport references */
    protected Map pvReferences = new java.util.HashMap();

    /**
     * The output stream to write the document to
     */
    protected OutputStream ostream;

    /**
     * the /Resources object of the PDF document being created
     */
    protected PDFResources pdfResources;

    /**
     * the current stream to add PDF commands to
     */
    protected PDFStream currentStream;

    /**
     * the current annotation list to add annotations to
     */
    protected PDFResourceContext currentContext = null;

    /**
     * the current page to add annotations to
     */
    protected PDFPage currentPage;

    /** drawing state */
    protected PDFState currentState = null;

    /** Name of currently selected font */
    protected String currentFontName = "";
    /** Size of currently selected font */
    protected int currentFontSize = 0;
    /** page height */
    protected int pageHeight;

    /** Registry of PDF filters */
    protected Map filterMap;

    /**
     * true if a TJ command is left to be written
     */
    protected boolean textOpen = false;

    /**
     * true if a BT command has been written. 
     */
    protected boolean inTextMode = false;

    /**
     * the previous Y coordinate of the last word written.
     * Used to decide if we can draw the next word on the same line.
     */
    protected int prevWordY = 0;

    /**
     * the previous X coordinate of the last word written.
     * used to calculate how much space between two words
     */
    protected int prevWordX = 0;

    /**
     * The width of the previous word. Used to calculate space between
     */
    protected int prevWordWidth = 0;

    /**
     * reusable word area string buffer to reduce memory usage
     */
    private StringBuffer wordAreaPDF = new StringBuffer();

    /**
     * create the PDF renderer
     */
    public PDFRenderer() {
    }

    /**
     * Configure the PDF renderer.
     * Get the configuration to be used for pdf stream filters,
     * fonts etc.
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        //PDF filters
        this.filterMap = PDFFilterList.buildFilterMapFromConfiguration(cfg);

        //Font configuration
        List cfgFonts = FontSetup.buildFontListFromConfiguration(cfg);
        if (this.fontList == null) {
            this.fontList = cfgFonts;
        } else {
            this.fontList.addAll(cfgFonts);
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        PDFXMLHandler xmlHandler = new PDFXMLHandler();
        //userAgent.setDefaultXMLHandler(MIME_TYPE, xmlHandler);
        String svg = "http://www.w3.org/2000/svg";
        addXMLHandler(userAgent, MIME_TYPE, svg, xmlHandler);
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream stream) throws IOException {
        if (userAgent == null) {
            throw new IllegalStateException("UserAgent must be set before starting the renderer");
        }
        ostream = stream;
        this.pdfDoc = new PDFDocument(
                userAgent.getProducer() != null ? userAgent.getProducer() : "");
        this.pdfDoc.setCreator(userAgent.getCreator());
        this.pdfDoc.setCreationDate(userAgent.getCreationDate());
        this.pdfDoc.setFilterMap(filterMap);
        this.pdfDoc.outputHeader(stream);

        //Setup encryption if necessary
        PDFEncryptionManager.setupPDFEncryption(userAgent, this.pdfDoc, getLogger());
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
        pdfDoc.outputTrailer(ostream);

        this.pdfDoc = null;
        ostream = null;

        pages = null;

        pageReferences.clear();
        pvReferences.clear();
        pdfResources = null;
        currentStream = null;
        currentContext = null;
        currentPage = null;
        currentState = null;
        currentFontName = "";
        wordAreaPDF = new StringBuffer();
    }

    /**
     * @see org.apache.fop.render.Renderer#supportsOutOfOrder()
     */
    public boolean supportsOutOfOrder() {
        return false;
    }

    /**
     * @see org.apache.fop.render.Renderer#processOffDocumentItem(OffDocumentItem)
     */
    public void processOffDocumentItem(OffDocumentItem odi) {
        // render Bookmark-Tree
        if (odi instanceof BookmarkData) {
            renderBookmarkTree((BookmarkData) odi);
        }
    }

    /**
     * Renders a Bookmark-Tree object
     * @param bookmarks the BookmarkData object containing all the Bookmark-Items
     */
    protected void renderBookmarkTree(BookmarkData bookmarks) {
        for (int i = 0; i < bookmarks.getCount(); i++) {
            BookmarkData ext = bookmarks.getSubData(i);
            renderBookmarkItem(ext, null);
        }
    }

    private void renderBookmarkItem(BookmarkData bookmarkItem, 
            PDFOutline parentBookmarkItem) {
        PDFOutline pdfOutline = null;
        PageViewport pv = bookmarkItem.getPageViewport();
        if (pv != null) {
            Rectangle2D bounds = pv.getViewArea();
            double h = bounds.getHeight();
            float yoffset = (float)h / 1000f;
            String intDest = (String)pageReferences.get(pv.getKey());
            if (parentBookmarkItem == null) {
                PDFOutline outlineRoot = pdfDoc.getOutlineRoot();
                pdfOutline = pdfDoc.getFactory().makeOutline(outlineRoot,
                                        bookmarkItem.getBookmarkTitle(), 
                                        intDest, yoffset,
                                        bookmarkItem.showChildItems());
            } else {
                pdfOutline = pdfDoc.getFactory().makeOutline(parentBookmarkItem,
                                        bookmarkItem.getBookmarkTitle(), 
                                        intDest, yoffset, 
                                        bookmarkItem.showChildItems());
            }
        }

        for (int i = 0; i < bookmarkItem.getCount(); i++) {
            renderBookmarkItem(bookmarkItem.getSubData(i), pdfOutline);
        }
    }

    /** Saves the graphics state of the rendering engine. */
    protected void saveGraphicsState() {
        endTextObject();
        currentStream.add("q\n");
    }

    /** Restores the last graphics state of the rendering engine. */
    protected void restoreGraphicsState() {
        endTextObject();
        currentStream.add("Q\n");
    }

    /** Indicates the beginning of a text object. */
    protected void beginTextObject() {
        if (!inTextMode) {
            currentStream.add("BT\n");
            inTextMode = true;
        }
    }

    /** Indicates the end of a text object. */
    protected void endTextObject() {
        closeText();
        if (inTextMode) {
            currentStream.add("ET\n");
            inTextMode = false;
        }
    }

    /**
     * Start the next page sequence.
     * For the pdf renderer there is no concept of page sequences
     * but it uses the first available page sequence title to set
     * as the title of the pdf document.
     *
     * @param seqTitle the title of the page sequence
     */
    public void startPageSequence(LineArea seqTitle) {
        if (seqTitle != null) {
            String str = convertTitleToString(seqTitle);
            PDFInfo info = this.pdfDoc.getInfo();
            info.setTitle(str);
        }
    }

    /**
     * The pdf page is prepared by making the page.
     * The page is made in the pdf document without any contents
     * and then stored to add the contents later.
     * The page objects is stored using the area tree PageViewport
     * as a key.
     *
     * @param page the page to prepare
     */
    public void preparePage(PageViewport page) {
        this.pdfResources = this.pdfDoc.getResources();

        Rectangle2D bounds = page.getViewArea();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        currentPage = this.pdfDoc.getFactory().makePage(
            this.pdfResources,
            (int) Math.round(w / 1000), (int) Math.round(h / 1000));
        if (pages == null) {
            pages = new java.util.HashMap();
        }
        pages.put(page, currentPage);
        pageReferences.put(page.getKey(), currentPage.referencePDF());
        pvReferences.put(page.getKey(), page);
    }

    /**
     * This method creates a pdf stream for the current page
     * uses it as the contents of a new page. The page is written
     * immediately to the output stream.
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page)
                throws IOException, FOPException {
        if (pages != null
                && (currentPage = (PDFPage) pages.get(page)) != null) {
            pages.remove(page);
            Rectangle2D bounds = page.getViewArea();
            double h = bounds.getHeight();
            pageHeight = (int) h;
        } else {
            this.pdfResources = this.pdfDoc.getResources();
            Rectangle2D bounds = page.getViewArea();
            double w = bounds.getWidth();
            double h = bounds.getHeight();
            pageHeight = (int) h;
            currentPage = this.pdfDoc.getFactory().makePage(
                this.pdfResources,
                (int) Math.round(w / 1000), (int) Math.round(h / 1000));
            pageReferences.put(page.getKey(), currentPage.referencePDF());
            pvReferences.put(page.getKey(), page);
        }
        currentStream = this.pdfDoc.getFactory()
            .makeStream(PDFFilterList.CONTENT_FILTER, false);

        currentState = new PDFState();
        currentState.setTransform(new AffineTransform(1, 0, 0, -1, 0,
                                   (int) Math.round(pageHeight / 1000)));
        // Transform origin at top left to origin at bottom left
        currentStream.add("1 0 0 -1 0 "
                           + (int) Math.round(pageHeight / 1000) + " cm\n");
        currentFontName = "";

        Page p = page.getPage();
        renderPageAreas(p);

        this.pdfDoc.registerObject(currentStream);
        currentPage.setContents(currentStream);
        PDFAnnotList annots = currentPage.getAnnotations();
        if (annots != null) {
            this.pdfDoc.addObject(annots);
        }
        this.pdfDoc.addObject(currentPage);
        this.pdfDoc.output(ostream);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM)
     */
    protected void startVParea(CTM ctm) {
        // Set the given CTM in the graphics state
        currentState.push();
        currentState.setTransform(
          new AffineTransform(CTMHelper.toPDFArray(ctm)));

        saveGraphicsState();
        // multiply with current CTM
        currentStream.add(CTMHelper.toPDFString(ctm) + " cm\n");
        // Set clip?
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        restoreGraphicsState();
        currentState.pop();
    }

    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param region the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport region) {
        currentFontName = "";
        Rectangle2D viewArea = region.getViewArea();
        float startx = (float)(viewArea.getX() / 1000f);
        float starty = (float)(viewArea.getY() / 1000f);
        float width = (float)(viewArea.getWidth() / 1000f);
        float height = (float)(viewArea.getHeight() / 1000f);

        if (region.getRegion().getRegionClass() == FO_REGION_BODY) {
            currentBPPosition = region.getBorderAndPaddingWidthBefore();
            currentIPPosition = region.getBorderAndPaddingWidthStart();
        }
        drawBackAndBorders(region, startx, starty, width, height);
    }

    /**
     * Handle block traits.
     * The block could be any sort of block with any positioning
     * so this should render the traits such as border and background
     * in its position.
     *
     * @param block the block to render the traits
     */
    protected void handleBlockTraits(Block block) {
        int borderPaddingStart = block.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = block.getBorderAndPaddingWidthBefore();
        
        float startx = currentIPPosition / 1000f;
        float starty = currentBPPosition / 1000f;
        float width = block.getIPD() / 1000f;
        float height = block.getBPD() / 1000f;

        /* using start-indent now
        Integer spaceStart = (Integer) block.getTrait(Trait.SPACE_START);
        if (spaceStart != null) {
            startx += spaceStart.floatValue() / 1000f;
        }*/
        startx += block.getStartIndent() / 1000f;
        startx -= block.getBorderAndPaddingWidthStart() / 1000f;

        width += borderPaddingStart / 1000f;
        width += block.getBorderAndPaddingWidthEnd() / 1000f;
        height += borderPaddingBefore / 1000f;
        height += block.getBorderAndPaddingWidthAfter() / 1000f;

        drawBackAndBorders(block, startx, starty,
            width, height);
    }

    /**
     * Draw the background and borders.
     * This draws the background and border traits for an area given
     * the position.
     *
     * @param area the area to get the traits from
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area area,
                    float startx, float starty,
                    float width, float height) {
        // draw background then border

        Trait.Background back;
        back = (Trait.Background)area.getTrait(Trait.BACKGROUND);
        if (back != null) {
            endTextObject();

            if (back.getColor() != null) {
                updateColor(back.getColor(), true, null);
                currentStream.add(startx + " " + starty + " "
                                  + width + " " + height + " re\n");
                currentStream.add("f\n");
            }
            if (back.getURL() != null) {
                ImageFactory fact = ImageFactory.getInstance();
                FopImage fopimage = fact.getImage(back.getURL(), userAgent);
                if (fopimage != null && fopimage.load(FopImage.DIMENSIONS)) {
                    if (back.getRepeat() == EN_REPEAT) {
                        // create a pattern for the image
                    } else {
                        // place once
                        Rectangle2D pos;
                        pos = new Rectangle2D.Float((startx + back.getHoriz()) * 1000,
                                                    (starty + back.getVertical()) * 1000,
                                                    fopimage.getWidth() * 1000,
                                                    fopimage.getHeight() * 1000);
                        putImage(back.getURL(), pos);
                    }
                }
            }
        }

        BorderProps bps = (BorderProps)area.getTrait(Trait.BORDER_BEFORE);
        if (bps != null) {
            endTextObject();

            float bwidth = bps.width / 1000f;
            updateColor(bps.color, false, null);
            updateLineStyle(bps.style);
            updateLineWidth(bwidth);
            float y1 = starty + bwidth / 2;
            drawLine(startx, y1, startx + width, y1);
        }
        bps = (BorderProps)area.getTrait(Trait.BORDER_AFTER);
        if (bps != null) {
            endTextObject();

            float bwidth = bps.width / 1000f;
            updateColor(bps.color, false, null);
            updateLineStyle(bps.style);
            updateLineWidth(bwidth);
            float y1 = starty - bwidth / 2;
            drawLine(startx, y1 + height, startx + width, y1 + height);
        }
        bps = (BorderProps)area.getTrait(Trait.BORDER_START);
        if (bps != null) {
            endTextObject();

            float bwidth = bps.width / 1000f;
            updateColor(bps.color, false, null);
            updateLineStyle(bps.style);
            updateLineWidth(bwidth);
            float x1 = startx + bwidth / 2;
            drawLine(x1, starty, x1, starty + height);
        }
        bps = (BorderProps)area.getTrait(Trait.BORDER_END);
        if (bps != null) {
            endTextObject();

            float bwidth = bps.width / 1000f;
            updateColor(bps.color, false, null);
            updateLineStyle(bps.style);
            updateLineWidth(bwidth);
            float x1 = startx - bwidth / 2;
            drawLine(x1 + width, starty, x1 + width, starty + height);
        }
    }

    /**
     * Sets the current line width in points.
     * @param width line width in points
     */
    private void updateLineWidth(float width) {
        if (currentState.setLineWidth(width)) {
            //Only write if value has changed WRT the current line width
            currentStream.add(width + " w\n");
        }
    }
    
    private void updateLineStyle(int style) {
        switch (style) {
            case Constants.EN_DASHED:
                currentStream.add("[3] 0 d\n");
                break;
            case Constants.EN_DOTTED:
                currentStream.add("[1 7] 0 d\n");
                break;
            default:
                // solid
                currentStream.add("[] 0 d\n");
                break;
        }
    }

    /**
     * Draw a line.
     *
     * @param startx the start x position
     * @param starty the start y position
     * @param endx the x end position
     * @param endy the y end position
     */
    private void drawLine(float startx, float starty, float endx, float endy) {
        currentStream.add(startx + " " + starty + " m ");
        currentStream.add(endx + " " + endy + " l S\n");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBlockViewport(BlockViewport, List)
     */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;
        String saveFontName = currentFontName;

        CTM ctm = bv.getCTM();

        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            getLogger().debug("containing position ip=" + containingIPPosition + " bp=" + containingBPPosition);
            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);
            getLogger().debug("tempctm=" + tempctm + " ctm=" + ctm);

            float x,y;
            x = (float)(bv.getXOffset() + containingIPPosition) / 1000f;
            y = (float)(bv.getYOffset() + containingBPPosition) / 1000f;
            float width = (float)bv.getIPD() / 1000f;
            float height = (float)bv.getBPD() / 1000f;
            getLogger().debug("renderBlockViewport: x=" + x + " y=" + y + " width=" + width + " height=" + height);
            
            int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
            int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();

            Integer spaceStart = (Integer) bv.getTrait(Trait.SPACE_START);
            if (spaceStart != null) {
                x += spaceStart.floatValue() / 1000;
            }

            width += borderPaddingStart / 1000f;
            width += bv.getBorderAndPaddingWidthEnd() / 1000f;
            height += borderPaddingBefore / 1000f;
            height += bv.getBorderAndPaddingWidthAfter() / 1000f;

            drawBackAndBorders(bv, x, y, width, height);

            if (bv.getClip()) {
                saveGraphicsState();
                clip(x, y, width, height);
            }

            startVParea(ctm);

            currentIPPosition = 0;
            currentBPPosition = 0;

            renderBlocks(bv, children);
            endVParea();

            if (bv.getClip()) {
                restoreGraphicsState();
            }

            // clip if necessary

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {

            if (ctm != null) {
                double[] vals = ctm.toArray();
                //boolean aclock = vals[2] == 1.0;
                if (vals[2] == 1.0) {
                    ctm = ctm.translate(-saveBP - bv.getBPD(), -saveIP);
                } else if (vals[0] == -1.0) {
                    ctm = ctm.translate(-saveIP - bv.getIPD(), -saveBP - bv.getBPD());
                } else {
                    ctm = ctm.translate(saveBP, saveIP - bv.getIPD());
                }
            }

            // clip if necessary
            if (bv.getClip()) {
                saveGraphicsState();
                float x = (float)bv.getXOffset() / 1000f;
                float y = (float)bv.getYOffset() / 1000f;
                float width = (float)bv.getIPD() / 1000f;
                float height = (float)bv.getBPD() / 1000f;
                clip(x, y, width, height);
            }

            handleBlockTraits(bv);
            if (ctm != null) {
                startVParea(ctm);
                currentIPPosition = 0;
                currentBPPosition = 0;
            }
            renderBlocks(bv, children);
            if (ctm != null) {
                endVParea();
            }

            if (bv.getClip()) {
                restoreGraphicsState();
            }

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
            currentBPPosition += (int)(bv.getAllocBPD());
        }
        currentFontName = saveFontName;
    }

    /**
     * Clip an area.
     * write a clipping operation given coordinates in the current
     * transform.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void clip(float x, float y, float width, float height) {
        currentStream.add(x + " " + y + " " + width + " " + height + " re W\n");
        currentStream.add("n\n");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLineArea(LineArea)
     */
    protected void renderLineArea(LineArea line) {
        super.renderLineArea(line);
        closeText();
    }

    /**
     * Render inline parent area.
     * For pdf this handles the inline parent area traits such as
     * links, border, background.
     * @param ip the inline parent area
     */
    public void renderInlineParent(InlineParent ip) {
        float start = currentIPPosition / 1000f;
        float top = (ip.getOffset() + currentBPPosition) / 1000f;
        float width = ip.getIPD() / 1000f;
        float height = ip.getBPD() / 1000f;
        drawBackAndBorders(ip, start, top, width, height);

        // render contents
        super.renderInlineParent(ip);

        // place the link over the top
        Object tr = ip.getTrait(Trait.INTERNAL_LINK);
        boolean internal = false;
        String dest = null;
        float yoffset = 0;
        if (tr == null) {
            dest = (String)ip.getTrait(Trait.EXTERNAL_LINK);
        } else {
            String pvKey = (String)tr;
            dest = (String)pageReferences.get(pvKey);
            if (dest != null) {
                PageViewport pv = (PageViewport)pvReferences.get(pvKey);
                Rectangle2D bounds = pv.getViewArea();
                double h = bounds.getHeight();
                yoffset = (float)h / 1000f;
                internal = true;
            }
        }
        if (dest != null) {
            // add link to pdf document
            Rectangle2D rect = new Rectangle2D.Float(start, top, width, height);
            // transform rect to absolute coords
            AffineTransform transform = currentState.getTransform();
            rect = transform.createTransformedShape(rect).getBounds();

            int type = internal ? PDFLink.INTERNAL : PDFLink.EXTERNAL;
            PDFLink pdflink = pdfDoc.getFactory().makeLink(
                        rect, dest, type, yoffset);
            currentPage.addAnnotation(pdflink);
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#renderCharacter(Character)
     */
    public void renderCharacter(Character ch) {
        StringBuffer pdf = new StringBuffer();

        String name = (String) ch.getTrait(Trait.FONT_NAME);
        int size = ((Integer) ch.getTrait(Trait.FONT_SIZE)).intValue();

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface f = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = f.isMultiByte();

        // String startText = useMultiByte ? "<FEFF" : "(";
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        updateFont(name, size, pdf);
        ColorType ct = (ColorType) ch.getTrait(Trait.COLOR);
        if (ct != null) {
            updateColor(ct, true, pdf);
        }

        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        //  where previous line area failed to take up entire allocated space
        int rx = currentIPPosition;
        int bl = currentBPPosition + ch.getOffset();

/*        System.out.println("Text = " + ch.getTextArea() +
            "; text width: " + ch.getWidth() +
            "; BlockIP Position: " + currentBlockIPPosition +
            "; currentBPPosition: " + currentBPPosition +
            "; offset: " + ch.getOffset());
*/
        // Set letterSpacing
        //float ls = fs.getLetterSpacing() / this.currentFontSize;
        //pdf.append(ls).append(" Tc\n");

        if (!textOpen || bl != prevWordY) {
            closeText();

            pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f) + " Tm "
                       + (ch.getTextLetterSpaceAdjust() / 1000f) + " Tc "
                       + (ch.getTextWordSpaceAdjust() / 1000f) + " Tw [" + startText);
            prevWordY = bl;
            textOpen = true;
        } else {
            closeText();

            pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f) + " Tm "
                           + (ch.getTextLetterSpaceAdjust() / 1000f) + " Tc "
                           + (ch.getTextWordSpaceAdjust() / 1000f) + " Tw [" + startText);
            textOpen = true;
        }
        prevWordWidth = ch.getIPD();
        prevWordX = rx;

        String s = ch.getChar();


        FontMetrics metrics = fontInfo.getMetricsFor(name);
        Font fs = new Font(name, metrics, size);
        escapeText(s, fs, useMultiByte, pdf);
        pdf.append(endText);

        currentStream.add(pdf.toString());

        super.renderCharacter(ch);
    }

    /**
     * @see org.apache.fop.render.Renderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        beginTextObject();
        StringBuffer pdf = new StringBuffer();

        String name = (String) text.getTrait(Trait.FONT_NAME);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface f = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = f.isMultiByte();

        // String startText = useMultiByte ? "<FEFF" : "(";
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        updateFont(name, size, pdf);
        ColorType ct = (ColorType) text.getTrait(Trait.COLOR);
        if (ct != null) {
            updateColor(ct, true, pdf);
        }

        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        //  where previous line area failed to take up entire allocated space
        int rx = currentIPPosition;
        int bl = currentBPPosition + text.getOffset();

/*        System.out.println("Text = " + text.getTextArea() +
            "; text width: " + text.getWidth() +
            "; BlockIP Position: " + currentBlockIPPosition +
            "; currentBPPosition: " + currentBPPosition +
            "; offset: " + text.getOffset());
*/
        // Set letterSpacing
        //float ls = fs.getLetterSpacing() / this.currentFontSize;
        //pdf.append(ls).append(" Tc\n");

        if (!textOpen || bl != prevWordY) {
            closeText();

            pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f) + " Tm "
                       + (text.getTextLetterSpaceAdjust() / 1000f) + " Tc "
                       + (text.getTextWordSpaceAdjust() / 1000f) + " Tw [" + startText);
            prevWordY = bl;
            textOpen = true;
        } else {
            closeText();

            pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f) + " Tm "
                       + (text.getTextLetterSpaceAdjust() / 1000f) + " Tc "
                       + (text.getTextWordSpaceAdjust() / 1000f) + " Tw [" + startText);
            textOpen = true;
        }
        prevWordWidth = text.getIPD();
        prevWordX = rx;

        String s = text.getTextArea();

        FontMetrics metrics = fontInfo.getMetricsFor(name);
        Font fs = new Font(name, metrics, size);
        escapeText(s, fs, useMultiByte, pdf);
        pdf.append(endText);

        currentStream.add(pdf.toString());

        super.renderText(text);
    }

    /**
     * Escapes text according to PDF rules.
     * @param s Text to escape
     * @param fs Font state
     * @param useMultiByte Indicates the use of multi byte convention
     * @param pdf target buffer for the escaped text
     */
    public void escapeText(String s, Font fs,
                           boolean useMultiByte, StringBuffer pdf) {
        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";

        boolean kerningAvailable = false;
        Map kerning = fs.getKerning();
        if (kerning != null && !kerning.isEmpty()) {
            kerningAvailable = true;
        }

        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = fs.mapChar(s.charAt(i));

            if (!useMultiByte) {
                if (ch > 127) {
                    pdf.append("\\");
                    pdf.append(Integer.toOctalString((int) ch));
                } else {
                    switch (ch) {
                        case '(':
                        case ')':
                        case '\\':
                            pdf.append("\\");
                            break;
                    }
                    pdf.append(ch);
                }
            } else {
                pdf.append(PDFText.toUnicodeHex(ch));
            }

            if (kerningAvailable && (i + 1) < l) {
                addKerning(pdf, (new Integer((int) ch)),
                           (new Integer((int) fs.mapChar(s.charAt(i + 1)))
                           ), kerning, startText, endText);
            }
        }
    }

    private void addKerning(StringBuffer buf, Integer ch1, Integer ch2,
                            Map kerning, String startText, String endText) {
        Map kernPair = (Map) kerning.get(ch1);

        if (kernPair != null) {
            Integer width = (Integer) kernPair.get(ch2);
            if (width != null) {
                buf.append(endText).append(-width.intValue());
                buf.append(' ').append(startText);
            }
        }
    }

    /**
     * Checks to see if we have some text rendering commands open
     * still and writes out the TJ command to the stream if we do
     */
    protected void closeText() {
        if (textOpen) {
            currentStream.add("] TJ\n");
            textOpen = false;
            prevWordX = 0;
            prevWordY = 0;
            currentFontName = "";
        }
    }

    private void updateColor(ColorType col, boolean fill, StringBuffer pdf) {
        Color newCol = new Color(col.getRed(), col.getGreen(), col.getBlue());
        boolean update = false;
        if (fill) {
            update = currentState.setBackColor(newCol);
        } else {
            update = currentState.setColor(newCol);
        }

        if (update) {
            PDFColor color = new PDFColor((double)col.getRed(),
                                     (double)col.getGreen(),
                                     (double)col.getBlue());

            closeText();

            if (pdf != null) {
                pdf.append(color.getColorSpaceOut(fill));
            } else {
                currentStream.add(color.getColorSpaceOut(fill));
            }
        }
    }

    private void updateFont(String name, int size, StringBuffer pdf) {
        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            closeText();

            this.currentFontName = name;
            this.currentFontSize = size;
            pdf = pdf.append("/" + name + " " + ((float) size / 1000f)
                              + " Tf\n");
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderImage(Image, Rectangle2D)
     */
    public void renderImage(Image image, Rectangle2D pos) {
        endTextObject();
        String url = image.getURL();
        putImage(url, pos);
    }

    /**
     * Adds a PDF XObject (a bitmap) to the PDF that will later be referenced.
     * @param url URL of the bitmap
     * @param pos Position of the bitmap
     */
    protected void putImage(String url, Rectangle2D pos) {
        PDFXObject xobject = pdfDoc.getImage(url);
        if (xobject != null) {
            int w = (int) pos.getWidth() / 1000;
            int h = (int) pos.getHeight() / 1000;
            placeImage((int) pos.getX() / 1000,
                       (int) pos.getY() / 1000, w, h, xobject.getXNumber());
            return;
        }

        url = ImageFactory.getURL(url);
        ImageFactory fact = ImageFactory.getInstance();
        FopImage fopimage = fact.getImage(url, userAgent);
        if (fopimage == null) {
            return;
        }
        if (!fopimage.load(FopImage.DIMENSIONS)) {
            return;
        }
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
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);
        } else if ("image/jpeg".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);

            int w = (int) pos.getWidth() / 1000;
            int h = (int) pos.getHeight() / 1000;
            placeImage((int) pos.getX() / 1000,
                       (int) pos.getY() / 1000, w, h, xobj);
        } else {
            if (!fopimage.load(FopImage.BITMAP)) {
                return;
            }
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);

            int w = (int) pos.getWidth() / 1000;
            int h = (int) pos.getHeight() / 1000;
            placeImage((int) pos.getX() / 1000,
                       (int) pos.getY() / 1000, w, h, xobj);
        }

        // output new data
        try {
            this.pdfDoc.output(ostream);
        } catch (IOException ioe) {
            // ioexception will be caught later
        }
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj object number of the referenced image
     */
    protected void placeImage(int x, int y, int w, int h, int xobj) {
        saveGraphicsState();
        currentStream.add(((float) w) + " 0 0 "
                          + ((float) -h) + " "
                          + (((float) currentIPPosition) / 1000f + x) + " "
                          + (((float)(currentBPPosition + 1000 * h)) / 1000f
                          + y) + " cm\n" + "/Im" + xobj + " Do\n");
        restoreGraphicsState();
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        endTextObject();
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos);
    }

    /**
     * Renders an XML document (SVG for example).
     * @param doc DOM document representing the XML document
     * @param ns Namespace for the document
     * @param pos Position on the page
     */
    public void renderDocument(Document doc, String ns, Rectangle2D pos) {
        RendererContext context;
        context = new RendererContext(MIME_TYPE);
        context.setUserAgent(userAgent);

        context.setProperty(PDFXMLHandler.PDF_DOCUMENT, pdfDoc);
        context.setProperty(PDFXMLHandler.OUTPUT_STREAM, ostream);
        context.setProperty(PDFXMLHandler.PDF_STATE, currentState);
        context.setProperty(PDFXMLHandler.PDF_PAGE, currentPage);
        context.setProperty(PDFXMLHandler.PDF_CONTEXT,
                    currentContext == null ? currentPage : currentContext);
        context.setProperty(PDFXMLHandler.PDF_CONTEXT, currentContext);
        context.setProperty(PDFXMLHandler.PDF_STREAM, currentStream);
        context.setProperty(PDFXMLHandler.PDF_XPOS,
                            new Integer(currentIPPosition + (int) pos.getX()));
        context.setProperty(PDFXMLHandler.PDF_YPOS,
                            new Integer(currentBPPosition + (int) pos.getY()));
        context.setProperty(PDFXMLHandler.PDF_FONT_INFO, fontInfo);
        context.setProperty(PDFXMLHandler.PDF_FONT_NAME, currentFontName);
        context.setProperty(PDFXMLHandler.PDF_FONT_SIZE,
                            new Integer(currentFontSize));
        context.setProperty(PDFXMLHandler.PDF_WIDTH,
                            new Integer((int) pos.getWidth()));
        context.setProperty(PDFXMLHandler.PDF_HEIGHT,
                            new Integer((int) pos.getHeight()));
        renderXML(userAgent, context, doc, ns);

    }

    /**
     * Render an inline viewport.
     * This renders an inline viewport by clipping if necessary.
     * @param viewport the viewport to handle
     */
    public void renderViewport(Viewport viewport) {

        float x = currentIPPosition / 1000f;
        float y = (currentBPPosition + viewport.getOffset()) / 1000f;
        float width = viewport.getIPD() / 1000f;
        float height = viewport.getBPD() / 1000f;
        // TODO: Calculate the border rect correctly. 
        drawBackAndBorders(viewport, x, y, width, height);

        if (viewport.getClip()) {
            saveGraphicsState();

            clip(x, y, width, height);
        }
        super.renderViewport(viewport);

        if (viewport.getClip()) {
            restoreGraphicsState();
        }
    }

    /**
     * Render leader area.
     * This renders a leader area which is an area with a rule.
     * @param area the leader area to render
     */
    public void renderLeader(Leader area) {
        saveGraphicsState();
        int style = area.getRuleStyle();
        boolean alt = false;
        switch(style) {
            case EN_SOLID:
                currentStream.add("[] 0 d\n");
            break;
            case EN_DOTTED:
                currentStream.add("[2] 0 d\n");
            break;
            case EN_DASHED:
                currentStream.add("[6 4] 0 d\n");
            break;
            case EN_DOUBLE:
            case EN_GROOVE:
            case EN_RIDGE:
                alt = true;
            break;
        }
        float startx = ((float) currentIPPosition) / 1000f;
        float starty = ((currentBPPosition + area.getOffset()) / 1000f);
        float endx = (currentIPPosition + area.getIPD()) / 1000f;
        if (!alt) {
            updateLineWidth(area.getRuleThickness() / 1000f);
            drawLine(startx, starty, endx, starty);
        } else {
            if (style == EN_DOUBLE) {
                float third = area.getRuleThickness() / 3000f;
                updateLineWidth(third);
                drawLine(startx, starty, endx, starty);

                drawLine(startx, (starty + 2 * third), endx, (starty + 2 * third));
            } else {
                float half = area.getRuleThickness() / 2000f;

                currentStream.add("1 g\n");
                currentStream.add(startx + " " + starty + " m\n");
                currentStream.add(endx + " " + starty + " l\n");
                currentStream.add(endx + " " + (starty + 2 * half) + " l\n");
                currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                currentStream.add("h\n");
                currentStream.add("f\n");
                if (style == EN_GROOVE) {
                    currentStream.add("0 g\n");
                    currentStream.add(startx + " " + starty + " m\n");
                    currentStream.add(endx + " " + starty + " l\n");
                    currentStream.add(endx + " " + (starty + half) + " l\n");
                    currentStream.add((startx + half) + " " + (starty + half) + " l\n");
                    currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                } else {
                    currentStream.add("0 g\n");
                    currentStream.add(endx + " " + starty + " m\n");
                    currentStream.add(endx + " " + (starty + 2 * half) + " l\n");
                    currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                    currentStream.add(startx + " " + (starty + half) + " l\n");
                    currentStream.add((endx - half) + " " + (starty + half) + " l\n");
                }
                currentStream.add("h\n");
                currentStream.add("f\n");
            }

        }

        restoreGraphicsState();
        beginTextObject();
        super.renderLeader(area);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }
}

