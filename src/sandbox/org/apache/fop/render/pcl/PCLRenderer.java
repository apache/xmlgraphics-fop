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
 
package org.apache.fop.render.pcl;

//Java
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;

import org.apache.xmlgraphics.java2d.GraphicContext;

// FOP
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fonts.Font;
import org.apache.fop.image.EPSImage;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.XMLImage;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.Graphics2DImagePainter;
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.RendererContextConstants;
import org.apache.fop.render.java2d.Java2DRenderer;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.QName;

/**
 * Renderer for the PCL 5 printer language. It also uses HP GL/2 for certain graphic elements.
 */
public class PCLRenderer extends PrintRenderer {

    /** The MIME type for PCL */
    public static final String MIME_TYPE = MimeConstants.MIME_PCL_ALT;

    /** The OutputStream to write the PCL stream to */
    protected OutputStream out;
    
    /** The PCL generator */
    protected PCLGenerator gen;
    private boolean ioTrouble = false;

    private Stack graphicContextStack = new Stack();
    private GraphicContext graphicContext = new GraphicContext();

    private GeneralPath currentPath = null;
    private java.awt.Color currentFillColor = null;
    
    /**
     * Controls whether appearance is more important than speed. False can cause some FO feature
     * to be ignored (like the advanced borders). 
     */
    private boolean qualityBeforeSpeed = false;
    
    /**
     * Create the PCL renderer
     */
    public PCLRenderer() {
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        super.configure(cfg);
        String rendering = cfg.getChild("rendering").getValue(null);
        if ("quality".equalsIgnoreCase(rendering)) {
            this.qualityBeforeSpeed = true;
        } else if ("speed".equalsIgnoreCase(rendering)) {
            this.qualityBeforeSpeed = false;
        } else if (rendering != null) {
            throw new ConfigurationException(
                    "Valid values for 'rendering' are 'quality' and 'speed'. Value found: " 
                        + rendering);
        }
    }

    /**
     * Central exception handler for I/O exceptions.
     * @param ioe IOException to handle
     */
    protected void handleIOTrouble(IOException ioe) {
        if (!ioTrouble) {
            log.error("Error while writing to target file", ioe);
            ioTrouble = true;
        }
    }

    /** @see org.apache.fop.render.Renderer#getGraphics2DAdapter() */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new PCLGraphics2DAdapter();
    }

    /** @return the GraphicContext used to track coordinate system transformations */
    public GraphicContext getGraphicContext() {
        return this.graphicContext;
    }
    
    /**
     * Sets the current font (NOTE: Hard-coded font mappings ATM!)
     * @param name the font name (internal F* names for now)
     * @param size the font size
     * @throws IOException if an I/O problem occurs
     */
    public void setFont(String name, float size) throws IOException {
        int fontcode = 0;
        if (name.length() > 1 && name.charAt(0) == 'F') {
            try {
                fontcode = Integer.parseInt(name.substring(1));
            } catch (Exception e) {
                log.error(e);
            }
        }
        String formattedSize = gen.formatDouble2(size / 1000);
        switch (fontcode) {
        case 1:     // F1 = Helvetica
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b24580T");
            // Arial is more common among PCL5 printers than Helvetica - so use Arial

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16602T");
            break;
        case 2:     // F2 = Helvetica Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16602T");
            break;
        case 3:     // F3 = Helvetica Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16602T");
            break;
        case 4:     // F4 = Helvetica Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16602T");
            break;
        case 5:     // F5 = Times Roman
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            // Times New is more common among PCL5 printers than Times - so use Times New

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16901T");
            break;
        case 6:     // F6 = Times Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16901T");
            break;
        case 7:     // F7 = Times Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16901T");
            break;
        case 8:     // F8 = Times Bold Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16901T");
            break;
        case 9:     // F9 = Courier

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f)) 
                    + "h0s0b4099T");
            break;
        case 10:    // F10 = Courier Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f)) 
                    + "h1s0b4099T");
            break;
        case 11:    // F11 = Courier Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f)) 
                    + "h0s3b4099T");
            break;
        case 12:    // F12 = Courier Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f)) 
                    + "h1s3b4099T");
            break;
        case 13:    // F13 = Symbol

            gen.writeCommand("(19M");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16686T");
            // ECMA Latin 1 Symbol Set in Times Roman???
            // gen.writeCommand("(9U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            break;
        case 14:    // F14 = Zapf Dingbats

            gen.writeCommand("(14L");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b45101T");
            break;
        default:
            gen.writeCommand("(0N");
            gen.writeCommand("(s" + formattedSize + "V");
            break;
        }
    }

    /** @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream) */
    public void startRenderer(OutputStream outputStream) throws IOException {
        log.debug("Rendering areas to PCL...");
        this.out = outputStream;
        this.gen = new PCLGenerator(out);

        gen.universalEndOfLanguage();
        gen.writeText("@PJL JOB NAME = \"" + userAgent.getTitle() + "\"\n");
        gen.writeText("@PJL ENTER LANGUAGE = PCL\n");
        gen.resetPrinter();
    }

    /** @see org.apache.fop.render.Renderer#stopRenderer() */
    public void stopRenderer() throws IOException {
        gen.separateJobs();
        gen.resetPrinter();
        gen.universalEndOfLanguage();
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderPage(org.apache.fop.area.PageViewport)
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        saveGraphicsState();
        final long pagewidth = Math.round(page.getViewArea().getWidth());
        final long pageheight = Math.round(page.getViewArea().getHeight());
        selectPageFormat(pagewidth, pageheight);
        
        super.renderPage(page);
        gen.formFeed();
        restoreGraphicsState();
    }

    private void selectPageFormat(long pagewidth, long pageheight) throws IOException {
        
        PCLPageDefinition pageDef = PCLPageDefinition.getPageDefinition(
                pagewidth, pageheight, 1000);
        if (pageDef != null) {
            // Adjust for the offset into the logical page
            graphicContext.translate(-pageDef.getLogicalPageXOffset(), 0);
            if (pageDef.isLandscapeFormat()) {
                gen.writeCommand("&l1O"); //Orientation
            } else {
                gen.writeCommand("&l0O"); //Orientation
            }
        } else {
            // Adjust for the offset into the logical page
            // X Offset to allow for PCL implicit 1/4" left margin (= 180 decipoints)
            graphicContext.translate(-18000, 18000);
            gen.writeCommand("&l0O"); //Orientation
        }
        gen.clearHorizontalMargins();
        gen.setTopMargin(0);
    }

    /** Saves the current graphics state on the stack. */
    protected void saveGraphicsState() {
        graphicContextStack.push(graphicContext);
        graphicContext = (GraphicContext)graphicContext.clone();
    }

    /** Restores the last graphics state from the stack. */
    protected void restoreGraphicsState() {
        graphicContext = (GraphicContext)graphicContextStack.pop();
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
        //PCL cannot clip (only HP GL/2 can)
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        saveGraphicsState();
        AffineTransform at = new AffineTransform(ctm.toArray());
        log.debug("startVPArea: " + at);
        graphicContext.transform(at);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        restoreGraphicsState();
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

        startx += block.getStartIndent() / 1000f;
        startx -= block.getBorderAndPaddingWidthStart() / 1000f;

        width += borderPaddingStart / 1000f;
        width += block.getBorderAndPaddingWidthEnd() / 1000f;
        height += borderPaddingBefore / 1000f;
        height += block.getBorderAndPaddingWidthAfter() / 1000f;

        drawBackAndBorders(block, startx, starty, width, height);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    protected void renderText(TextArea area) {
        //renderInlineAreaBackAndBorders(area);
        String fontname = getInternalFontNameForArea(area);
        int fontsize = area.getTraitAsInteger(Trait.FONT_SIZE);

        //Determine position
        //int saveIP = currentIPPosition;
        //int saveBP = currentBPPosition;
        int rx = currentIPPosition + area.getBorderAndPaddingWidthStart();
        int bl = currentBPPosition + area.getOffset() + area.getBaselineOffset();

        try {
            setFont(fontname, fontsize);
            Color col = (Color)area.getTrait(Trait.COLOR);
            //this.currentFill = col;
            if (col != null) {
                //useColor(ct);
                gen.setPatternTransparencyMode(false);
                gen.selectCurrentPattern(gen.convertToPCLShade(col), 2);
            }
            
            saveGraphicsState();
            updatePrintDirection();
            graphicContext.translate(rx, bl);
            setCursorPos(0, 0);
        
            super.renderText(area); //Updates IPD
        
            //renderTextDecoration(tf, fontsize, area, bl, rx);
            restoreGraphicsState();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Sets the current cursor position. The coordinates are transformed to the absolute position
     * on the logical PCL page and then passed on to the PCLGenerator.
     * @param x the x coordinate (in millipoints)
     * @param y the y coordinate (in millipoints)
     */
    void setCursorPos(float x, float y) {
        try {
            Point2D transPoint = transformedPoint(x, y);
            gen.setCursorPos(transPoint.getX(), transPoint.getY());
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * @see org.apache.fop.render.AbstractPathOrientedRenderer#clip()
     */
    protected void clip() {
        if (currentPath == null) {
            throw new IllegalStateException("No current path available!");
        }
        //TODO Find a good way to do clipping
        currentPath = null;
    }

    /**
     * @see org.apache.fop.render.AbstractPathOrientedRenderer#closePath()
     */
    protected void closePath() {
        currentPath.closePath();
    }

    /**
     * @see org.apache.fop.render.AbstractPathOrientedRenderer#lineTo(float, float)
     */
    protected void lineTo(float x, float y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.lineTo(x, y);
    }

    /**
     * @see org.apache.fop.render.AbstractPathOrientedRenderer#moveTo(float, float)
     */
    protected void moveTo(float x, float y) {
        if (currentPath == null) {
            currentPath = new GeneralPath();
        }
        currentPath.moveTo(x, y);
    }
    
    /**
     * Fill a rectangular area.
     * @param x the x coordinate (in pt)
     * @param y the y coordinate (in pt)
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    protected void fillRect(float x, float y, float width, float height) {
        try {
            Point2D p = transformedPoint(x * 1000, y * 1000);
            gen.fillRect((int)p.getX(), (int)p.getY(), 
                    (int)width * 1000, (int)height * 1000, 
                    this.currentFillColor);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /**
     * Sets the new current fill color.
     * @param color the color
     */
    protected void updateFillColor(java.awt.Color color) {
        this.currentFillColor = color;
    }

    private void updatePrintDirection() throws IOException {
        AffineTransform at = graphicContext.getTransform();
        if (log.isDebugEnabled()) {
            log.debug(at.getScaleX() + " " + at.getScaleY() + " " 
                    + at.getShearX() + " " + at.getShearY() );
        }
        if (at.getScaleX() == 0 && at.getScaleY() == 0 
                && at.getShearX() == 1 && at.getShearY() == -1) {
            gen.writeCommand("&a90P");
        } else if (at.getScaleX() == -1 && at.getScaleY() == -1 
                && at.getShearX() == 0 && at.getShearY() == 0) {
            gen.writeCommand("&a180P");
        } else if (at.getScaleX() == 0 && at.getScaleY() == 0 
                && at.getShearX() == -1 && at.getShearY() == 1) {
            gen.writeCommand("&a270P");
        } else {
            gen.writeCommand("&a0P");
        }
    }

    private Point2D transformedPoint(float x, float y) {
        return transformedPoint(Math.round(x), Math.round(y));
    }
    
    private Point2D transformedPoint(int x, int y) {
        AffineTransform at = graphicContext.getTransform();
        if (log.isDebugEnabled()) {
            log.debug("Current transform: " + at);
        }
        Point2D orgPoint = new Point2D.Float(x, y);
        Point2D transPoint = new Point2D.Float();
        at.transform(orgPoint, transPoint);
        return transPoint;
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#renderWord(org.apache.fop.area.inline.WordArea)
     */
    protected void renderWord(WordArea word) {
        //Font font = getFontFromArea(word.getParentArea());

        String s = word.getWord();

        try {
            gen.writeText(s);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }

        super.renderWord(word);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderSpace(org.apache.fop.area.inline.SpaceArea)
     */
    protected void renderSpace(SpaceArea space) {
        AbstractTextArea textArea = (AbstractTextArea)space.getParentArea();
        String s = space.getSpace();
        char sp = s.charAt(0);
        Font font = getFontFromArea(textArea);
        
        int tws = (space.isAdjustable() 
                ? ((TextArea) space.getParentArea()).getTextWordSpaceAdjust() 
                        + 2 * textArea.getTextLetterSpaceAdjust()
                : 0);

        double dx = (font.getCharWidth(sp) + tws) / 100f;
        try {
            gen.writeCommand("&a+" + gen.formatDouble2(dx) + "H");
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
        super.renderSpace(space);
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
                //breakOutList = breakOutOfStateStack();
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
                //restoreStateStackAfterBreakOut(breakOutList);
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
        //currentFontName = saveFontName;
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderImage(Image, Rectangle2D)
     */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos, image.getForeignAttributes());
    }

    /**
     * Draw an image at the indicated location.
     * @param url the URI/URL of the image
     * @param pos the position of the image
     * @param foreignAttributes an optional Map with foreign attributes, may be null
     */
    protected void drawImage(String url, Rectangle2D pos, Map foreignAttributes) {
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
        if ("text/xml".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            Document doc = ((XMLImage) fopimage).getDocument();
            String ns = ((XMLImage) fopimage).getNameSpace();

            renderDocument(doc, ns, pos, foreignAttributes);
        } else if ("image/svg+xml".equals(mime)) {
            if (!fopimage.load(FopImage.ORIGINAL_DATA)) {
                return;
            }
            Document doc = ((XMLImage) fopimage).getDocument();
            String ns = ((XMLImage) fopimage).getNameSpace();

            renderDocument(doc, ns, pos, foreignAttributes);
        } else if (fopimage instanceof EPSImage) {
            log.warn("EPS images are not supported by this renderer");
        } else {
            if (!fopimage.load(FopImage.BITMAP)) {
                log.error("Bitmap image could not be processed: " + fopimage);
                return;
            }
            byte[] imgmap = fopimage.getBitmaps();
            
            ColorModel cm = new ComponentColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB), 
                    new int[] {8, 8, 8},
                    false, false,
                    ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            int imgw = fopimage.getWidth();
            int imgh = fopimage.getHeight();
            SampleModel sampleModel = new PixelInterleavedSampleModel(
                    DataBuffer.TYPE_BYTE, imgw, imgh, 3, imgw * 3, new int[] {0, 1, 2});
            DataBuffer dbuf = new DataBufferByte(imgmap, imgw * imgh * 3);

            WritableRaster raster = Raster.createWritableRaster(sampleModel,
                    dbuf, null);

            // Combine the color model and raster into a buffered image
            RenderedImage img = new BufferedImage(cm, raster, false, null);

            try {
                setCursorPos(this.currentIPPosition + (int)pos.getX(),
                        this.currentBPPosition + (int)pos.getY());
                int resolution = (int)Math.round(Math.max(fopimage.getHorizontalResolution(), 
                                        fopimage.getVerticalResolution()));
                gen.paintBitmap(img, resolution);
            } catch (IOException ioe) {
                handleIOTrouble(ioe);
            }
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos, fo.getForeignAttributes());
    }

    /** 
     * Common method to render the background and borders for any inline area.
     * The all borders and padding are drawn outside the specified area.
     * @param area the inline area for which the background, border and padding is to be
     * rendered
     * @todo Copied from AbstractPathOrientedRenderer
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
        try {
            updatePrintDirection();
            BorderProps bpsBefore = (BorderProps) area.getTrait(Trait.BORDER_BEFORE);
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
                    updateFillColor(back.getColor());
                    fillRect(sx, sy, paddRectWidth, paddRectHeight);
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
                                // Image positions are relative to the currentIP/BP
                                pos = new Rectangle2D.Float(
                                        sx - currentIPPosition 
                                            + (x * fopimage.getIntrinsicWidth()),
                                        sy - currentBPPosition
                                            + (y * fopimage.getIntrinsicHeight()),
                                        fopimage.getIntrinsicWidth(),
                                        fopimage.getIntrinsicHeight());
                                drawImage(back.getURL(), pos, null);
                            }
                        }
                        restoreGraphicsState();
                    } else {
                        log.warn(
                                "Can't find background image: " + back.getURL());
                    }
                }
            }
            
            Rectangle2D.Float borderRect = new Rectangle2D.Float(startx, starty, width, height);
            drawBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
            
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Draws borders.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     */
    protected void drawBorders(Rectangle2D.Float borderRect, 
            final BorderProps bpsBefore, final BorderProps bpsAfter, 
            final BorderProps bpsStart, final BorderProps bpsEnd) {
        if (bpsBefore == null && bpsAfter == null && bpsStart == null && bpsEnd == null) {
            return; //no borders to paint
        }
        if (qualityBeforeSpeed) {
            drawQualityBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
        } else {
            drawFastBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
        }
    }
    
    /**
     * Draws borders. Borders are drawn as shaded rectangles with no clipping.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     */
    protected void drawFastBorders(Rectangle2D.Float borderRect, 
            final BorderProps bpsBefore, final BorderProps bpsAfter, 
            final BorderProps bpsStart, final BorderProps bpsEnd) {
        float startx = borderRect.x;
        float starty = borderRect.y;
        float width = borderRect.width;
        float height = borderRect.height;
        if (bpsBefore != null) {
            int borderWidth = (int) Math.round((bpsBefore.width / 1000f));
            updateFillColor(bpsBefore.color);
            fillRect((int) startx, (int) starty, (int) width,
                    borderWidth);
        }
        if (bpsAfter != null) {
            int borderWidth = (int) Math.round((bpsAfter.width / 1000f));
            updateFillColor(bpsAfter.color);
            fillRect((int) startx,
                    (int) (starty + height - borderWidth), (int) width,
                    borderWidth);
        }
        if (bpsStart != null) {
            int borderWidth = (int) Math.round((bpsStart.width / 1000f));
            updateFillColor(bpsStart.color);
            fillRect((int) startx, (int) starty, borderWidth,
                    (int) height);
        }
        if (bpsEnd != null) {
            int borderWidth = (int) Math.round((bpsEnd.width / 1000f));
            updateFillColor(bpsEnd.color);
            fillRect((int) (startx + width - borderWidth),
                    (int) starty, borderWidth, (int) height);
        }
    }
    
    /**
     * Draws borders. Borders are drawn in-memory and painted as a bitmap.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     */
    protected void drawQualityBorders(Rectangle2D.Float borderRect, 
            final BorderProps bpsBefore, final BorderProps bpsAfter, 
            final BorderProps bpsStart, final BorderProps bpsEnd) {
        Graphics2DAdapter g2a = getGraphics2DAdapter();
        final Rectangle.Float effBorderRect = new Rectangle2D.Float(
                 borderRect.x - (currentIPPosition / 1000f),
                 borderRect.y - (currentBPPosition / 1000f),
                 borderRect.width, borderRect.height);
        final Rectangle paintRect = new Rectangle(
                (int)Math.round(borderRect.x * 1000),
                (int)Math.round(borderRect.y * 1000), 
                (int)Math.floor(borderRect.width * 1000) + 1,
                (int)Math.floor(borderRect.height * 1000) + 1);
        int xoffset = (bpsStart != null ? bpsStart.width : 0);
        paintRect.x += xoffset;
        paintRect.width += xoffset;
        paintRect.width += (bpsEnd != null ? bpsEnd.width : 0);
        
        RendererContext rc = createRendererContext(paintRect.x, paintRect.y, 
                paintRect.width, paintRect.height, null);
        if (false) {
            Map atts = new java.util.HashMap();
            atts.put(new QName(ExtensionElementMapping.URI, null, "conversion-mode"), "bitmap");
            rc.setProperty(RendererContextConstants.FOREIGN_ATTRIBUTES, atts);
        }
        
        Graphics2DImagePainter painter = new Graphics2DImagePainter() {

            public void paint(Graphics2D g2d, Rectangle2D area) {
                g2d.translate((bpsStart != null ? bpsStart.width : 0), 0);
                g2d.scale(1000, 1000);
                float startx = effBorderRect.x;
                float starty = effBorderRect.y;
                float width = effBorderRect.width;
                float height = effBorderRect.height;
                boolean[] b = new boolean[] {
                    (bpsBefore != null), (bpsEnd != null), 
                    (bpsAfter != null), (bpsStart != null)};
                if (!b[0] && !b[1] && !b[2] && !b[3]) {
                    return;
                }
                float[] bw = new float[] {
                    (b[0] ? bpsBefore.width / 1000f : 0.0f),
                    (b[1] ? bpsEnd.width / 1000f : 0.0f),
                    (b[2] ? bpsAfter.width / 1000f : 0.0f),
                    (b[3] ? bpsStart.width / 1000f : 0.0f)};
                float[] clipw = new float[] {
                    BorderProps.getClippedWidth(bpsBefore) / 1000f,    
                    BorderProps.getClippedWidth(bpsEnd) / 1000f,    
                    BorderProps.getClippedWidth(bpsAfter) / 1000f,    
                    BorderProps.getClippedWidth(bpsStart) / 1000f};
                starty += clipw[0];
                height -= clipw[0];
                height -= clipw[2];
                startx += clipw[3];
                width -= clipw[3];
                width -= clipw[1];
                
                boolean[] slant = new boolean[] {
                    (b[3] && b[0]), (b[0] && b[1]), (b[1] && b[2]), (b[2] && b[3])};
                if (bpsBefore != null) {
                    //endTextObject();

                    float sx1 = startx;
                    float sx2 = (slant[0] ? sx1 + bw[3] - clipw[3] : sx1);
                    float ex1 = startx + width;
                    float ex2 = (slant[1] ? ex1 - bw[1] + clipw[1] : ex1);
                    float outery = starty - clipw[0];
                    float clipy = outery + clipw[0];
                    float innery = outery + bw[0];

                    //saveGraphicsState();
                    Graphics2D g = (Graphics2D)g2d.create();
                    moveTo(sx1, clipy);
                    float sx1a = sx1;
                    float ex1a = ex1;
                    if (bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                        if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                            sx1a -= clipw[3];
                        }
                        if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                            ex1a += clipw[1];
                        }
                        lineTo(sx1a, outery);
                        lineTo(ex1a, outery);
                    }
                    lineTo(ex1, clipy);
                    lineTo(ex2, innery);
                    lineTo(sx2, innery);
                    closePath();
                    //clip();
                    g.clip(currentPath);
                    currentPath = null;
                    Rectangle2D.Float lineRect = new Rectangle2D.Float(
                            sx1a, outery, ex1a - sx1a, innery - outery);
                    Java2DRenderer.drawBorderLine(lineRect, true, true, 
                            bpsBefore.style, bpsBefore.color, g);
                    //restoreGraphicsState();
                }
                if (bpsEnd != null) {
                    //endTextObject();

                    float sy1 = starty;
                    float sy2 = (slant[1] ? sy1 + bw[0] - clipw[0] : sy1);
                    float ey1 = starty + height;
                    float ey2 = (slant[2] ? ey1 - bw[2] + clipw[2] : ey1);
                    float outerx = startx + width + clipw[1];
                    float clipx = outerx - clipw[1];
                    float innerx = outerx - bw[1];
                    
                    //saveGraphicsState();
                    Graphics2D g = (Graphics2D)g2d.create();
                    moveTo(clipx, sy1);
                    float sy1a = sy1;
                    float ey1a = ey1;
                    if (bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                        if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                            sy1a -= clipw[0];
                        }
                        if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                            ey1a += clipw[2];
                        }
                        lineTo(outerx, sy1a);
                        lineTo(outerx, ey1a);
                    }
                    lineTo(clipx, ey1);
                    lineTo(innerx, ey2);
                    lineTo(innerx, sy2);
                    closePath();
                    //clip();
                    g.setClip(currentPath);
                    currentPath = null;
                    Rectangle2D.Float lineRect = new Rectangle2D.Float(
                            innerx, sy1a, outerx - innerx, ey1a - sy1a);
                    Java2DRenderer.drawBorderLine(lineRect, false, false, 
                            bpsEnd.style, bpsEnd.color, g);
                    //restoreGraphicsState();
                }
                if (bpsAfter != null) {
                    //endTextObject();

                    float sx1 = startx;
                    float sx2 = (slant[3] ? sx1 + bw[3] - clipw[3] : sx1);
                    float ex1 = startx + width;
                    float ex2 = (slant[2] ? ex1 - bw[1] + clipw[1] : ex1);
                    float outery = starty + height + clipw[2];
                    float clipy = outery - clipw[2];
                    float innery = outery - bw[2];

                    //saveGraphicsState();
                    Graphics2D g = (Graphics2D)g2d.create();
                    moveTo(ex1, clipy);
                    float sx1a = sx1;
                    float ex1a = ex1;
                    if (bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                        if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                            sx1a -= clipw[3];
                        }
                        if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                            ex1a += clipw[1];
                        }
                        lineTo(ex1a, outery);
                        lineTo(sx1a, outery);
                    }
                    lineTo(sx1, clipy);
                    lineTo(sx2, innery);
                    lineTo(ex2, innery);
                    closePath();
                    //clip();
                    g.setClip(currentPath);
                    currentPath = null;
                    Rectangle2D.Float lineRect = new Rectangle2D.Float(
                            sx1a, innery, ex1a - sx1a, outery - innery);
                    Java2DRenderer.drawBorderLine(lineRect, true, false, 
                            bpsAfter.style, bpsAfter.color, g);
                    //restoreGraphicsState();
                }
                if (bpsStart != null) {
                    //endTextObject();

                    float sy1 = starty;
                    float sy2 = (slant[0] ? sy1 + bw[0] - clipw[0] : sy1);
                    float ey1 = sy1 + height;
                    float ey2 = (slant[3] ? ey1 - bw[2] + clipw[2] : ey1);
                    float outerx = startx - clipw[3];
                    float clipx = outerx + clipw[3];
                    float innerx = outerx + bw[3];

                    //saveGraphicsState();
                    Graphics2D g = (Graphics2D)g2d.create();
                    moveTo(clipx, ey1);
                    float sy1a = sy1;
                    float ey1a = ey1;
                    if (bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                        if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                            sy1a -= clipw[0];
                        }
                        if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                            ey1a += clipw[2];
                        }
                        lineTo(outerx, ey1a);
                        lineTo(outerx, sy1a);
                    }
                    lineTo(clipx, sy1);
                    lineTo(innerx, sy2);
                    lineTo(innerx, ey2);
                    closePath();
                    //clip();
                    g.setClip(currentPath);
                    currentPath = null;
                    Rectangle2D.Float lineRect = new Rectangle2D.Float(
                            outerx, sy1a, innerx - outerx, ey1a - sy1a);
                    Java2DRenderer.drawBorderLine(lineRect, false, false, 
                            bpsStart.style, bpsStart.color, g);
                    //restoreGraphicsState();
                }
            }

            public Dimension getImageSize() {
                return paintRect.getSize();
            }
            
        };
        try {
            g2a.paintImage(painter, rc, 
                    paintRect.x - xoffset, paintRect.y, paintRect.width, paintRect.height);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    
    
}
