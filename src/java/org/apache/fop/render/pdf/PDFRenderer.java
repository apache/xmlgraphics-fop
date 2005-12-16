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

/* $Id$ */

package org.apache.fop.render.pdf;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
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
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.inline.Character;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.area.inline.SpaceArea;
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
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
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
public class PDFRenderer extends AbstractPathOrientedRenderer {
    
    /**
     * The mime type for pdf
     */
    public static final String MIME_TYPE = MimeConstants.MIME_PDF;

    /** Controls whether comments are written to the PDF stream. */
    protected static final boolean WRITE_COMMENTS = true;
    
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
    
    /** The current Transform */
    protected AffineTransform currentBasicTransform;

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
    //private StringBuffer wordAreaPDF = new StringBuffer();

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
        this.pdfDoc.getInfo().setAuthor(userAgent.getAuthor());
        this.pdfDoc.getInfo().setTitle(userAgent.getTitle());
        this.pdfDoc.getInfo().setKeywords(userAgent.getKeywords());
        this.pdfDoc.setFilterMap(filterMap);
        this.pdfDoc.outputHeader(stream);

        //Setup encryption if necessary
        PDFEncryptionManager.setupPDFEncryption(
                userAgent.getPDFEncryptionParams(), this.pdfDoc);
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
    }

    /**
     * @see org.apache.fop.render.Renderer#supportsOutOfOrder()
     */
    public boolean supportsOutOfOrder() {
        //return false;
        return true;
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
    
    /** @see org.apache.fop.render.Renderer#getGraphics2DAdapter() */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new PDFGraphics2DAdapter(this);
    }

    /** 
     * writes out a comment.
     * @param text text for the comment
     */
    protected void comment(String text) {
        if (WRITE_COMMENTS) {
            currentStream.add("% " + text + "\n");
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
            if (info.getTitle() == null) {
                info.setTitle(str);
            }
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
        setupPage(page);
        if (pages == null) {
            pages = new java.util.HashMap();
        }
        pages.put(page, currentPage);
    }

    private void setupPage(PageViewport page) {
        this.pdfResources = this.pdfDoc.getResources();

        Rectangle2D bounds = page.getViewArea();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        currentPage = this.pdfDoc.getFactory().makePage(
            this.pdfResources,
            (int) Math.round(w / 1000), (int) Math.round(h / 1000),
            page.getPageIndex());
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
            //Retrieve previously prepared page (out-of-line rendering)
            pages.remove(page);
        } else {
            setupPage(page);
        }
        Rectangle2D bounds = page.getViewArea();
        double h = bounds.getHeight();
        pageHeight = (int) h;

        currentStream = this.pdfDoc.getFactory()
            .makeStream(PDFFilterList.CONTENT_FILTER, false);

        currentState = new PDFState();
        /* This transform shouldn't affect PDFState as it only sets the basic
         * coordinate system for the rendering process.
         *
        currentState.setTransform(new AffineTransform(1, 0, 0, -1, 0,
                                   (int) Math.round(pageHeight / 1000)));
        */
        // Transform origin at top left to origin at bottom left
        currentBasicTransform = new AffineTransform(1, 0, 0, -1, 0,
                pageHeight / 1000f);
        currentStream.add(CTMHelper.toPDFString(currentBasicTransform, false) + " cm\n");
        
        
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
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        // Set the given CTM in the graphics state
        currentState.push();
        currentState.setTransform(
                new AffineTransform(CTMHelper.toPDFArray(ctm)));

        saveGraphicsState();
        if (clippingRect != null) {
            clipRect((float)clippingRect.getX() / 1000f, 
                    (float)clippingRect.getY() / 1000f, 
                    (float)clippingRect.getWidth() / 1000f, 
                    (float)clippingRect.getHeight() / 1000f);
        }
        // multiply with current CTM
        currentStream.add(CTMHelper.toPDFString(ctm) + " cm\n");
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
        super.handleRegionTraits(region);
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void drawBorderLine(float x1, float y1, float x2, float y2, 
            boolean horz, boolean startOrBefore, int style, ColorType col) {
        float w = x2 - x1;
        float h = y2 - y1;
        if ((w < 0) || (h < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }
        switch (style) {
            case Constants.EN_DASHED: 
                setColor(toColor(col), false, null);
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    currentStream.add("[" + unit + "] 0 d ");
                    currentStream.add(h + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(x1 + " " + ym + " m " + x2 + " " + ym + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    currentStream.add("[" + unit + "] 0 d ");
                    currentStream.add(w + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(xm + " " + y1 + " m " + xm + " " + y2 + " l S\n");
                }
                break;
            case Constants.EN_DOTTED:
                setColor(toColor(col), false, null);
                currentStream.add("1 J ");
                if (horz) {
                    float unit = Math.abs(2 * h);
                    int rep = (int)(w / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = w / rep;
                    currentStream.add("[0 " + unit + "] 0 d ");
                    currentStream.add(h + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(x1 + " " + ym + " m " + x2 + " " + ym + " l S\n");
                } else {
                    float unit = Math.abs(2 * w);
                    int rep = (int)(h / unit);
                    if (rep % 2 == 0) {
                        rep++;
                    }
                    unit = h / rep;
                    currentStream.add("[0 " + unit + " ] 0 d ");
                    currentStream.add(w + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(xm + " " + y1 + " m " + xm + " " + y2 + " l S\n");
                }
                break;
            case Constants.EN_DOUBLE:
                setColor(toColor(col), false, null);
                currentStream.add("[] 0 d ");
                if (horz) {
                    float h3 = h / 3;
                    currentStream.add(h3 + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    float ym2 = ym1 + h3 + h3;
                    currentStream.add(x1 + " " + ym1 + " m " + x2 + " " + ym1 + " l S\n");
                    currentStream.add(x1 + " " + ym2 + " m " + x2 + " " + ym2 + " l S\n");
                } else {
                    float w3 = w / 3;
                    currentStream.add(w3 + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    float xm2 = xm1 + w3 + w3;
                    currentStream.add(xm1 + " " + y1 + " m " + xm1 + " " + y2 + " l S\n");
                    currentStream.add(xm2 + " " + y1 + " m " + xm2 + " " + y2 + " l S\n");
                }
                break;
            case Constants.EN_GROOVE:
            case Constants.EN_RIDGE:
            {
                float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
                currentStream.add("[] 0 d ");
                Color c = toColor(col);
                if (horz) {
                    Color uppercol = lightenColor(c, -colFactor);
                    Color lowercol = lightenColor(c, colFactor);
                    float h3 = h / 3;
                    currentStream.add(h3 + " w\n");
                    float ym1 = y1 + (h3 / 2);
                    setColor(uppercol, false, null);
                    currentStream.add(x1 + " " + ym1 + " m " + x2 + " " + ym1 + " l S\n");
                    setColor(c, false, null);
                    currentStream.add(x1 + " " + (ym1 + h3) + " m " 
                                        + x2 + " " + (ym1 + h3) + " l S\n");
                    setColor(lowercol, false, null);
                    currentStream.add(x1 + " " + (ym1 + h3 + h3) + " m " 
                                        + x2 + " " + (ym1 + h3 + h3) + " l S\n");
                } else {
                    Color leftcol = lightenColor(c, -colFactor);
                    Color rightcol = lightenColor(c, colFactor);
                    float w3 = w / 3;
                    currentStream.add(w3 + " w\n");
                    float xm1 = x1 + (w3 / 2);
                    setColor(leftcol, false, null);
                    currentStream.add(xm1 + " " + y1 + " m " + xm1 + " " + y2 + " l S\n");
                    setColor(c, false, null);
                    currentStream.add((xm1 + w3) + " " + y1 + " m " 
                                        + (xm1 + w3) + " " + y2 + " l S\n");
                    setColor(rightcol, false, null);
                    currentStream.add((xm1 + w3 + w3) + " " + y1 + " m " 
                                        + (xm1 + w3 + w3) + " " + y2 + " l S\n");
                }
                break;
            }
            case Constants.EN_INSET:
            case Constants.EN_OUTSET:
            {
                float colFactor = (style == EN_OUTSET ? 0.4f : -0.4f);
                currentStream.add("[] 0 d ");
                Color c = toColor(col);
                if (horz) {
                    c = lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    currentStream.add(h + " w\n");
                    float ym1 = y1 + (h / 2);
                    setColor(c, false, null);
                    currentStream.add(x1 + " " + ym1 + " m " + x2 + " " + ym1 + " l S\n");
                } else {
                    c = lightenColor(c, (startOrBefore ? 1 : -1) * colFactor);
                    currentStream.add(w + " w\n");
                    float xm1 = x1 + (w / 2);
                    setColor(c, false, null);
                    currentStream.add(xm1 + " " + y1 + " m " + xm1 + " " + y2 + " l S\n");
                }
                break;
            }
            case Constants.EN_HIDDEN:
                break;
            default:
                setColor(toColor(col), false, null);
                currentStream.add("[] 0 d ");
                if (horz) {
                    currentStream.add(h + " w\n");
                    float ym = y1 + (h / 2);
                    currentStream.add(x1 + " " + ym + " m " + x2 + " " + ym + " l S\n");
                } else {
                    currentStream.add(w + " w\n");
                    float xm = x1 + (w / 2);
                    currentStream.add(xm + " " + y1 + " m " + xm + " " + y2 + " l S\n");
                }
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
    
    /**
     * Clip a rectangular area.
     * write a clipping operation given coordinates in the current
     * transform.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void clipRect(float x, float y, float width, float height) {
        currentStream.add(x + " " + y + " " + width + " " + height + " re ");
        clip();
    }

    /**
     * Clip an area.
     */
    protected void clip() {
        currentStream.add("W\n");
        currentStream.add("n\n");
    }

    /**
     * Moves the current point to (x, y), omitting any connecting line segment. 
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void moveTo(float x, float y) {
        currentStream.add(x + " " + y + " m ");
    }
    
    /**
     * Appends a straight line segment from the current point to (x, y). The 
     * new current point is (x, y). 
     * @param x x coordinate
     * @param y y coordinate
     */
    protected void lineTo(float x, float y) {
        currentStream.add(x + " " + y + " l ");
    }
    
    /**
     * Closes the current subpath by appending a straight line segment from 
     * the current point to the starting point of the subpath.
     */
    protected void closePath() {
        currentStream.add("h ");
    }

    /** 
     * @see org.apache.fop.render.AbstractPathOrientedRenderer#fillRect(float, float, float, float)
     */
    protected void fillRect(float x, float y, float w, float h) {
        if (w != 0 && h != 0) {
            currentStream.add(x + " " + y + " " + w + " " + h + " re f\n");
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
     *//*
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;
        String saveFontName = currentFontName;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();
        float x, y;
        x = (float)(bv.getXOffset() + containingIPPosition) / 1000f;
        y = (float)(bv.getYOffset() + containingBPPosition) / 1000f;

        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            //For FIXED, we need to break out of the current viewports to the
            //one established by the page. We save the state stack for restoration
            //after the block-container has been painted. See below.
            List breakOutList = null;
            if (bv.getPositioning() == Block.FIXED) {
                breakOutList = breakOutOfStateStack();
            }
            
            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);

            //This is the content-rect
            float width = (float)bv.getIPD() / 1000f;
            float height = (float)bv.getBPD() / 1000f;
            
            //Adjust for spaces (from margin or indirectly by start-indent etc.
            Integer spaceStart = (Integer) bv.getTrait(Trait.SPACE_START);
            if (spaceStart != null) {
                x += spaceStart.floatValue() / 1000;
            }
            Integer spaceBefore = (Integer) bv.getTrait(Trait.SPACE_BEFORE);
            if (spaceBefore != null) {
                y += spaceBefore.floatValue() / 1000;
            }

            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd()) / 1000f;
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter()) / 1000f;

            drawBackAndBorders(bv, x, y, width + bpwidth, height + bpheight);

            //Now adjust for border/padding
            x += borderPaddingStart / 1000f;
            y += borderPaddingBefore / 1000f;
            
            if (bv.getClip()) {
                saveGraphicsState();
                clipRect(x, y, width, height);
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

            if (breakOutList != null) {
                restoreStateStackAfterBreakOut(breakOutList);
            }
            
            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {

            Integer spaceBefore = (Integer)bv.getTrait(Trait.SPACE_BEFORE);
            if (spaceBefore != null) {
                currentBPPosition += spaceBefore.intValue();
            }

            //borders and background in the old coordinate system
            handleBlockTraits(bv);

            CTM tempctm = new CTM(containingIPPosition, currentBPPosition);
            ctm = tempctm.multiply(ctm);
            
            //Now adjust for border/padding
            x += borderPaddingStart / 1000f;
            y += borderPaddingBefore / 1000f;

            // clip if necessary
            if (bv.getClip()) {
                saveGraphicsState();
                float width = (float)bv.getIPD() / 1000f;
                float height = (float)bv.getBPD() / 1000f;
                clipRect(x, y, width, height);
            }

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
            
            //Adjust BP position (alloc BPD + spaces)
            if (spaceBefore != null) {
                currentBPPosition += spaceBefore.intValue();
            }
            currentBPPosition += (int)(bv.getAllocBPD());
            Integer spaceAfter = (Integer)bv.getTrait(Trait.SPACE_AFTER);
            if (spaceAfter != null) {
                currentBPPosition += spaceAfter.intValue();
            }
        }
        currentFontName = saveFontName;
    }*/
   
    /**
     * Breaks out of the state stack to handle fixed block-containers.
     * @return the saved state stack to recreate later
     */
    protected List breakOutOfStateStack() {
        List breakOutList = new java.util.ArrayList();
        PDFState.Data data;
        while (true) {
            data = currentState.getData();
            if (currentState.pop() == null) {
                break;
            }
            if (breakOutList.size() == 0) {
                comment("------ break out!");
            }
            breakOutList.add(0, data); //Insert because of stack-popping
            restoreGraphicsState();
        }
        return breakOutList;
    }

    /**
     * Restores the state stack after a break out.
     * @param breakOutList the state stack to restore.
     */
    protected void restoreStateStackAfterBreakOut(List breakOutList) {
        CTM tempctm;
        comment("------ restoring context after break-out...");
        PDFState.Data data;
        Iterator i = breakOutList.iterator();
        double[] matrix = new double[6];
        while (i.hasNext()) {
            data = (PDFState.Data)i.next();
            currentState.push();
            saveGraphicsState();
            AffineTransform at = data.getTransform();
            if (!at.isIdentity()) {
                currentState.setTransform(at);
                at.getMatrix(matrix);
                tempctm = new CTM(matrix[0], matrix[1], matrix[2], matrix[3], 
                                  matrix[4] * 1000, matrix[5] * 1000);
                currentStream.add(CTMHelper.toPDFString(tempctm) + " cm\n");
            }
            //TODO Break-out: Also restore items such as line width and color
            //Left out for now because all this painting stuff is very
            //inconsistent. Some values go over PDFState, some don't.
        }
        comment("------ done.");
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
            rect = transform.createTransformedShape(rect).getBounds2D();
            rect = currentBasicTransform.createTransformedShape(rect).getBounds2D();

            int type = internal ? PDFLink.INTERNAL : PDFLink.EXTERNAL;
            PDFLink pdflink = pdfDoc.getFactory().makeLink(
                        rect, dest, type, yoffset);
            currentPage.addAnnotation(pdflink);
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderCharacter(Character)
     */
    public void renderCharacter(Character ch) {
        renderInlineAreaBackAndBorders(ch);

        beginTextObject();
        StringBuffer pdf = new StringBuffer();

        String name = (String) ch.getTrait(Trait.FONT_NAME);
        int size = ((Integer) ch.getTrait(Trait.FONT_SIZE)).intValue();

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = tf.isMultiByte();

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
        int rx = currentIPPosition + ch.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + ch.getOffset() + ch.getBaselineOffset();

/*        log.debug("Text = " + ch.getTextArea() +
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

        renderTextDecoration(tf, size, ch, bl, rx);
        
        super.renderCharacter(ch);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);
        beginTextObject();
        StringBuffer pdf = new StringBuffer();

        String name = (String) text.getTrait(Trait.FONT_NAME);
        int size = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        
        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface tf = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = tf.isMultiByte();
        
        updateFont(name, size, pdf);
        ColorType ct = (ColorType) text.getTrait(Trait.COLOR);
        updateColor(ct, true, pdf);

        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        //  where previous line area failed to take up entire allocated space
        int rx = currentIPPosition + text.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + text.getOffset() + text.getBaselineOffset();

/*        log.debug("Text = " + text.getTextArea() +
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
                       + (text.getTextWordSpaceAdjust() / 1000f) + " Tw [");
            prevWordY = bl;
            textOpen = true;
        } else {
            closeText();

            pdf.append("1 0 0 -1 " + (rx / 1000f) + " " + (bl / 1000f) + " Tm "
                       + (text.getTextLetterSpaceAdjust() / 1000f) + " Tc "
                       + (text.getTextWordSpaceAdjust() / 1000f) + " Tw [");
            textOpen = true;
        }
        prevWordWidth = text.getIPD();
        prevWordX = rx;

        currentStream.add(pdf.toString());

        super.renderText(text);

        renderTextDecoration(tf, size, text, bl, rx);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderWord(WordArea)
     */
    public void renderWord(WordArea word) {
        String name = (String) word.getParentArea().getTrait(Trait.FONT_NAME);
        int size = ((Integer) word.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        Typeface tf = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = tf.isMultiByte();

        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";
        
        StringBuffer pdf = new StringBuffer();
        
        pdf.append(startText);

        String s = word.getWord();
        
        FontMetrics metrics = fontInfo.getMetricsFor(name);
        Font fs = new Font(name, metrics, size);
        escapeText(s, fs, useMultiByte, pdf);
        pdf.append(endText);
        
        currentStream.add(pdf.toString());

        super.renderWord(word);
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderSpace(SpaceArea)
     */
    public void renderSpace(SpaceArea space) {
        String name = (String) space.getParentArea().getTrait(Trait.FONT_NAME);
        int size = ((Integer) space.getParentArea().getTrait(Trait.FONT_SIZE)).intValue();
        Typeface tf = (Typeface) fontInfo.getFonts().get(name);
        boolean useMultiByte = tf.isMultiByte();

        String startText = useMultiByte ? "<" : "(";
        String endText = useMultiByte ? "> " : ") ";
        
        StringBuffer pdf = new StringBuffer();
        
        pdf.append(startText);

        String s = space.getSpace();
        
        FontMetrics metrics = fontInfo.getMetricsFor(name);
        Font fs = new Font(name, metrics, size);
        escapeText(s, fs, useMultiByte, pdf);
        pdf.append(endText);
        
        if (useMultiByte) {
            pdf.append(-(((TextArea) space.getParentArea()).getTextWordSpaceAdjust() / (size / 1000)) + " ");
        }
        
        currentStream.add(pdf.toString());

        super.renderSpace(space);
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
            //kerningAvailable = true;
            //TODO Reenable me when the layout engine supports kerning, too
            log.warn("Kerning support is disabled until it is supported by the layout engine!");
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

    /**
     * Establishes a new foreground or fill color. In contrast to updateColor
     * this method does not check the PDFState for optimization possibilities.
     * @param col the color to apply 
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    protected void setColor(Color col, boolean fill, StringBuffer pdf) {
        PDFColor color = new PDFColor(col);

        closeText();
        
        if (pdf != null) {
            pdf.append(color.getColorSpaceOut(fill));
        } else {
            currentStream.add(color.getColorSpaceOut(fill));
        }
    }
    
    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    private void updateColor(ColorType col, boolean fill, StringBuffer pdf) {
        if (col == null) {
            return;
        }
        Color newCol = toColor(col);
        boolean update = false;
        if (fill) {
            update = currentState.setBackColor(newCol);
        } else {
            update = currentState.setColor(newCol);
        }

        if (update) {
            setColor(newCol, fill, pdf);
        }
    }

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected  void updateColor(ColorType col, boolean fill) {
        updateColor(col, fill, null);
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

    /** @see org.apache.fop.render.AbstractPathOrientedRenderer */
    protected void drawImage(String url, Rectangle2D pos) {
        endTextObject();
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
            float w = (float) pos.getWidth() / 1000f;
            float h = (float) pos.getHeight() / 1000f;
            placeImage((float)pos.getX() / 1000f,
                       (float)pos.getY() / 1000f, w, h, xobject.getXNumber());
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
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);
        } else if ("image/jpeg".equals(mime) || "image/tiff".equals(mime)) {
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);

            float w = (float)pos.getWidth() / 1000f;
            float h = (float)pos.getHeight() / 1000f;
            placeImage((float) pos.getX() / 1000,
                       (float) pos.getY() / 1000, w, h, xobj);
        } else {
            if (!fopimage.load(FopImage.BITMAP)) {
                return;
            }
            FopPDFImage pdfimage = new FopPDFImage(fopimage, url);
            int xobj = pdfDoc.addImage(currentContext, pdfimage).getXNumber();
            fact.releaseImage(url, userAgent);

            float w = (float) pos.getWidth() / 1000f;
            float h = (float) pos.getHeight() / 1000f;
            placeImage((float) pos.getX() / 1000f,
                       (float) pos.getY() / 1000f, w, h, xobj);
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
    protected void placeImage(float x, float y, float w, float h, int xobj) {
        saveGraphicsState();
        currentStream.add(w + " 0 0 "
                          + -h + " "
                          + (currentIPPosition / 1000f + x) + " "
                          + (currentBPPosition / 1000f + h + y) 
                          + " cm\n" + "/Im" + xobj + " Do\n");
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
        context = new RendererContext(this, MIME_TYPE);
        context.setUserAgent(userAgent);

        context.setProperty(PDFRendererContextConstants.PDF_DOCUMENT, pdfDoc);
        context.setProperty(PDFRendererContextConstants.OUTPUT_STREAM, ostream);
        context.setProperty(PDFRendererContextConstants.PDF_STATE, currentState);
        context.setProperty(PDFRendererContextConstants.PDF_PAGE, currentPage);
        context.setProperty(PDFRendererContextConstants.PDF_CONTEXT,
                    currentContext == null ? currentPage : currentContext);
        context.setProperty(PDFRendererContextConstants.PDF_CONTEXT, currentContext);
        context.setProperty(PDFRendererContextConstants.PDF_STREAM, currentStream);
        context.setProperty(PDFRendererContextConstants.XPOS,
                            new Integer(currentIPPosition + (int) pos.getX()));
        context.setProperty(PDFRendererContextConstants.YPOS,
                            new Integer(currentBPPosition + (int) pos.getY()));
        context.setProperty(PDFRendererContextConstants.PDF_FONT_INFO, fontInfo);
        context.setProperty(PDFRendererContextConstants.PDF_FONT_NAME, currentFontName);
        context.setProperty(PDFRendererContextConstants.PDF_FONT_SIZE,
                            new Integer(currentFontSize));
        context.setProperty(PDFRendererContextConstants.WIDTH,
                            new Integer((int) pos.getWidth()));
        context.setProperty(PDFRendererContextConstants.HEIGHT,
                            new Integer((int) pos.getHeight()));
        renderXML(context, doc, ns);

    }

    /**
     * Render leader area.
     * This renders a leader area which is an area with a rule.
     * @param area the leader area to render
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        currentState.push();
        saveGraphicsState();
        int style = area.getRuleStyle();
        float startx = (currentIPPosition + area.getBorderAndPaddingWidthStart()) / 1000f;
        float starty = (currentBPPosition + area.getOffset()) / 1000f;
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart() 
                        + area.getIPD()) / 1000f;
        float ruleThickness = area.getRuleThickness() / 1000f;
        ColorType col = (ColorType)area.getTrait(Trait.COLOR);

        switch (style) {
            case EN_SOLID:
            case EN_DASHED:
            case EN_DOUBLE:
                drawBorderLine(startx, starty, endx, starty + ruleThickness, 
                        true, true, style, col);
                break;
            case EN_DOTTED:
                clipRect(startx, starty, endx - startx, ruleThickness);
                //This displaces the dots to the right by half a dot's width
                //TODO There's room for improvement here
                currentStream.add("1 0 0 1 " + (ruleThickness / 2) + " 0 cm\n");
                drawBorderLine(startx, starty, endx, starty + ruleThickness, 
                        true, true, style, col);
                break;
            case EN_GROOVE:
            case EN_RIDGE:
                float half = area.getRuleThickness() / 2000f;

                setColor(lightenColor(toColor(col), 0.6f), true, null);
                currentStream.add(startx + " " + starty + " m\n");
                currentStream.add(endx + " " + starty + " l\n");
                currentStream.add(endx + " " + (starty + 2 * half) + " l\n");
                currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                currentStream.add("h\n");
                currentStream.add("f\n");
                setColor(toColor(col), true, null);
                if (style == EN_GROOVE) {
                    currentStream.add(startx + " " + starty + " m\n");
                    currentStream.add(endx + " " + starty + " l\n");
                    currentStream.add(endx + " " + (starty + half) + " l\n");
                    currentStream.add((startx + half) + " " + (starty + half) + " l\n");
                    currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                } else {
                    currentStream.add(endx + " " + starty + " m\n");
                    currentStream.add(endx + " " + (starty + 2 * half) + " l\n");
                    currentStream.add(startx + " " + (starty + 2 * half) + " l\n");
                    currentStream.add(startx + " " + (starty + half) + " l\n");
                    currentStream.add((endx - half) + " " + (starty + half) + " l\n");
                }
                currentStream.add("h\n");
                currentStream.add("f\n");
                break;
            default:
                throw new UnsupportedOperationException("rule style not supported");
        }

        restoreGraphicsState();
        currentState.pop();
        beginTextObject();
        super.renderLeader(area);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }
}

