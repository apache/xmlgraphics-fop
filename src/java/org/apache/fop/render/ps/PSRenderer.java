/*
 * $Id: PSRenderer.java,v 1.31 2003/03/11 08:42:24 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.ps;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

// FOP
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.fo.properties.BackgroundRepeat;
import org.apache.fop.area.Area;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Word;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.RendererContext;

import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.traits.BorderProps;

import org.w3c.dom.Document;
/**
 * Renderer that renders to PostScript.
 * <br>
 * This class currently generates PostScript Level 2 code. The only exception
 * is the FlateEncode filter which is a Level 3 feature. The filters in use
 * are hardcoded at the moment.
 * <br>
 * This class follows the Document Structuring Conventions (DSC) version 3.0.
 * If anyone modifies this renderer please make
 * sure to also follow the DSC to make it simpler to programmatically modify
 * the generated Postscript files (ex. extract pages etc.).
 * <br>
 * The PS renderer operates in millipoints as the layout engine. Since PostScript
 * initially uses points, scaling is applied as needed.
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id: PSRenderer.java,v 1.31 2003/03/11 08:42:24 jeremias Exp $
 */
public class PSRenderer extends AbstractRenderer {

    /** The MIME type for PostScript */
    public static final String MIME_TYPE = "application/postscript";

    /** The application producing the PostScript */
    private int currentPageNumber = 0;

    private boolean enableComments = true;
    private boolean autoRotateLandscape = false;

    /** The PostScript generator used to output the PostScript */
    protected PSGenerator gen;
    private boolean ioTrouble = false;

    private String currentFontName;
    private int currentFontSize;
    private float currRed;
    private float currGreen;
    private float currBlue;

    private FontInfo fontInfo;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        super.configure(cfg);
        this.autoRotateLandscape = cfg.getChild("auto-rotate-landscape").getValueAsBoolean(false);
    }
    
    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        PSXMLHandler xmlHandler = new PSXMLHandler();
        //userAgent.setDefaultXMLHandler(MIME_TYPE, xmlHandler);
        String svg = "http://www.w3.org/2000/svg";
        userAgent.addXMLHandler(MIME_TYPE, svg, xmlHandler);
    }

    /**
     * Write out a command
     * @param cmd PostScript command
     */
    protected void writeln(String cmd) {
        try {
            gen.writeln(cmd);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }

    /**
     * Central exception handler for I/O exceptions.
     * @param ioe IOException to handle
     */
    protected void handleIOTrouble(IOException ioe) {
        if (!ioTrouble) {
            getLogger().error("Error while writing to target file", ioe);
            ioTrouble = true;
        }
    }

    /**
     * Write out a comment
     * @param comment Comment to write
     */
    protected void comment(String comment) {
        if (this.enableComments) {
            writeln(comment);
        }
    }

    /**
     * Make sure the cursor is in the right place.
     */
    protected void movetoCurrPosition() {
        moveTo(this.currentIPPosition, this.currentBPPosition);
    }

    /**
     * Moves the cursor.
     * @param x X coordinate
     * @param y Y coordinate
     */
    protected void moveTo(int x, int y) {
        writeln(x + " " + y + " M");
    }

    /** Saves the graphics state of the rendering engine. */
    public void saveGraphicsState() {
        try {
            //delegate
            gen.saveGraphicsState();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /** Restores the last graphics state of the rendering engine. */
    public void restoreGraphicsState() {
        try {
            //delegate
            gen.restoreGraphicsState();
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /** Indicates the beginning of a text object. */
    protected void beginTextObject() {
        writeln("BT");
    }
        
    /** Indicates the end of a text object. */
    protected void endTextObject() {
        writeln("ET");
    }

    /**
     * Concats the transformation matrix.
     * @param a A part
     * @param b B part
     * @param c C part
     * @param d D part
     * @param e E part
     * @param f F part
     */
    protected void concatMatrix(double a, double b,
                                double c, double d, 
                                double e, double f) {
        try {
            gen.concatMatrix(a, b, c, d, e, f);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
    
    /**
     * Concats the transformations matrix.
     * @param matrix Matrix to use
     */
    protected void concatMatrix(double[] matrix) {
        try {
            gen.concatMatrix(matrix);
        } catch (IOException ioe) {
            handleIOTrouble(ioe);
        }
    }
                                
    /**
     * Set up the font info
     *
     * @param fontInfo the font info object to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo, null);
        this.fontInfo = fontInfo;
    }

    /**
     * Draws a filled rectangle.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w width
     * @param h height
     * @param col color to fill with
     */
    protected void fillRect(int x, int y, int w, int h,
                                 ColorType col) {
        useColor(col);
        writeln(x + " " + y + " " + w + " " + h + " rectfill");
    }

    /**
     * Draws a stroked rectangle with the current stroke settings.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w width
     * @param h height
     */
    protected void drawRect(int x, int y, int w, int h) {
        writeln(x + " " + y + " " + w + " " + h + " rectstroke");
    }

    /**
     * Clip an area.
     * Write a clipping operation given coordinates in the current
     * transform.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void clip(float x, float y, float width, float height) {
        writeln(x + " " + y + " " + width + " " + height + " rectclip");
    }

    /**
     * Changes the currently used font.
     * @param name name of the font
     * @param size font size
     */
    public void useFont(String name, int size) {
        if ((currentFontName != name) || (currentFontSize != size)) {
            writeln(name + " " + size + " F");
            currentFontName = name;
            currentFontSize = size;
        }
    }

    private void useColor(ColorType col) {
        useColor(col.getRed(), col.getGreen(), col.getBlue());
    }

    private void useColor(float red, float green, float blue) {
        if ((red != currRed) || (green != currGreen) || (blue != currBlue)) {
            writeln(red + " " + green + " " + blue + " setrgbcolor");
            currRed = red;
            currGreen = green;
            currBlue = blue;
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        getLogger().debug("rendering areas to PostScript");

        //Setup for PostScript generation
        this.gen = new PSGenerator(outputStream);
        this.currentPageNumber = 0;
        
        //PostScript Header
        writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR, new String[] {"FOP " + this.producer});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.PAGES, new Object[] {PSGenerator.ATEND});
        gen.writeDSCComment(DSCConstants.END_COMMENTS);
        
        //Defaults
        gen.writeDSCComment(DSCConstants.BEGIN_DEFAULTS);
        gen.writeDSCComment(DSCConstants.END_DEFAULTS);
        
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        gen.writeDSCComment(DSCConstants.END_PROLOG);
        
        //Setup
        gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
        PSProcSets.writeFOPStdProcSet(gen);
        PSProcSets.writeFOPEPSProcSet(gen);
        PSProcSets.writeFontDict(gen, fontInfo);
        gen.writeln("FOPFonts begin");
        gen.writeDSCComment(DSCConstants.END_SETUP);
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        gen.writeDSCComment(DSCConstants.TRAILER);
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(this.currentPageNumber));
        gen.writeDSCComment(DSCConstants.EOF);
        gen.flush();
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page)
            throws IOException, FOPException {
        getLogger().debug("renderPage(): " + page);
        
        this.currentPageNumber++;
        gen.writeDSCComment(DSCConstants.PAGE, new Object[] 
                {page.getPageNumber(),
                 new Integer(this.currentPageNumber)});
        final Integer zero = new Integer(0);
        final long pagewidth = Math.round(page.getViewArea().getWidth());
        final long pageheight = Math.round(page.getViewArea().getHeight());
        final double pspagewidth = pagewidth / 1000f;
        final double pspageheight = pageheight / 1000f;
        boolean rotate = false;
        if (this.autoRotateLandscape && (pageheight < pagewidth)) {
            rotate = true;
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                    {zero,
                     zero,
                     new Long(Math.round(pspageheight)),
                     new Long(Math.round(pspagewidth))});
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[]
                    {zero,
                     zero,
                     new Double(pspageheight),
                     new Double(pspagewidth)});
            gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION, "Landscape");
        } else {
            gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                    {zero,
                     zero,
                     new Long(Math.round(pspagewidth)),
                     new Long(Math.round(pspageheight))});
            gen.writeDSCComment(DSCConstants.PAGE_HIRES_BBOX, new Object[]
                    {zero,
                     zero,
                     new Double(pspagewidth),
                     new Double(pspageheight)});
            if (this.autoRotateLandscape) {
                gen.writeDSCComment(DSCConstants.PAGE_ORIENTATION, "Portrait");
            }                
        }
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);         
        if (rotate) {
            gen.writeln(Math.round(pspageheight) + " 0 translate");
            gen.writeln("90 rotate");
        }
        gen.writeln("0.001 0.001 scale");
        concatMatrix(1, 0, 0, -1, 0, pageheight);
        
        gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);         
        
        //Process page
        super.renderPage(page);
        
        writeln("showpage");        
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeDSCComment(DSCConstants.END_PAGE);
    }

    /**
     * Paints text.
     * @param rx X coordinate
     * @param bl Y coordinate
     * @param text Text to paint
     * @param font Font to use
     */
    protected void paintText(int rx, int bl, String text, Font font) {
        saveGraphicsState();
        writeln("1 0 0 -1 " + rx + " " + bl + " Tm");
        
        int initialSize = text.length();
        initialSize += initialSize / 2;
        StringBuffer sb = new StringBuffer(initialSize);
        sb.append("(");
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            final char mapped = font.mapChar(c);
            PSGenerator.escapeChar(mapped, sb);
        }
        sb.append(") t");
        writeln(sb.toString());
        restoreGraphicsState();
    }

    /**
     * @see org.apache.fop.render.Renderer#renderWord(Word)
     */
    public void renderWord(Word area) {
        String fontname = (String)area.getTrait(Trait.FONT_NAME);
        int fontsize = area.getTraitAsInteger(Trait.FONT_SIZE);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Font f = (Font)fontInfo.getFonts().get(fontname);
        
        //Determine position
        int rx = currentBlockIPPosition;
        int bl = currentBPPosition + area.getOffset();

        useFont(fontname, fontsize);
        
        paintText(rx, bl, area.getWord(), f);

/*
        String psString = null;
        if (area.getFontState().getLetterSpacing() > 0) {
            //float f = area.getFontState().getLetterSpacing() 
            //    * 1000 / this.currentFontSize;
            float f = area.getFontState().getLetterSpacing();
            psString = (new StringBuffer().append(f).append(" 0.0 (")
              .append(sb.toString()).append(") A")).toString();
        } else {
            psString = (new StringBuffer("(").append(sb.toString())
              .append(") t")).toString();
        }


        // System.out.println("["+s+"] --> ["+sb.toString()+"]");

        // comment("% --- InlineArea font-weight="+fontWeight+": " + sb.toString());
        useFont(fs.getFontName(), fs.getFontSize());
        useColor(area.getRed(), area.getGreen(), area.getBlue());
        if (area.getUnderlined() || area.getLineThrough()
                || area.getOverlined())
            write("ULS");
        write(psString);
        if (area.getUnderlined())
            write("ULE");
        if (area.getLineThrough())
            write("SOE");
        if (area.getOverlined())
            write("OLE");
        this.currentXPosition += area.getContentWidth();
        */
        super.renderWord(area); //Updates IPD
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

        if (bv.getPositioning() == Block.ABSOLUTE) {

            currentIPPosition = 0;
            currentBPPosition = 0;

            //closeText();
            endTextObject();

            if (bv.getClip()) {
                saveGraphicsState();
                int x = bv.getXOffset() + containingIPPosition;
                int y = bv.getYOffset() + containingBPPosition;
                int width = bv.getWidth();
                int height = bv.getHeight();
                clip(x, y, width, height);
            }

            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);

            startVParea(ctm);
            handleBlockTraits(bv);
            renderBlocks(children);
            endVParea();

            if (bv.getClip()) {
                restoreGraphicsState();
            }
            beginTextObject();

            // clip if necessary

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {

            if (ctm != null) {
                currentIPPosition = 0;
                currentBPPosition = 0;

                //closeText();
                endTextObject();

                double[] vals = ctm.toArray();
                //boolean aclock = vals[2] == 1.0;
                if (vals[2] == 1.0) {
                    ctm = ctm.translate(-saveBP - bv.getHeight(), -saveIP);
                } else if (vals[0] == -1.0) {
                    ctm = ctm.translate(-saveIP - bv.getWidth(), -saveBP - bv.getHeight());
                } else {
                    ctm = ctm.translate(saveBP, saveIP - bv.getWidth());
                }
            }

            // clip if necessary
            if (bv.getClip()) {
                if (ctm == null) {
                    //closeText();
                    endTextObject();
                }
                saveGraphicsState();
                int x = bv.getXOffset();
                int y = bv.getYOffset();
                int width = bv.getWidth();
                int height = bv.getHeight();
                clip(x, y, width, height);
            }

            if (ctm != null) {
                startVParea(ctm);
            }
            handleBlockTraits(bv);
            renderBlocks(children);
            if (ctm != null) {
                endVParea();
            }

            if (bv.getClip()) {
                restoreGraphicsState();
                if (ctm == null) {
                    beginTextObject();
                }
            }
            if (ctm != null) {
                beginTextObject();
            }

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
            currentBPPosition += (int)(bv.getHeight());
        }
        currentFontName = saveFontName;
    }
    
    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM)
     */
    protected void startVParea(CTM ctm) {
        // Set the given CTM in the graphics state
        //currentState.push();
        //currentState.setTransform(new AffineTransform(CTMHelper.toPDFArray(ctm)));
        
        saveGraphicsState();
        // multiply with current CTM
        //writeln(CTMHelper.toPDFString(ctm) + " cm\n");
        final double[] matrix = ctm.toArray();
        concatMatrix(matrix);
        
        // Set clip?
        beginTextObject();        
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        endTextObject();
        restoreGraphicsState();
        //currentState.pop();
    }

    /**
     * Handle the viewport traits.
     * This is used to draw the traits for a viewport.
     *
     * @param region the viewport region to handle
     */
    protected void handleViewportTraits(RegionViewport region) {
        currentFontName = "";
        float startx = 0;
        float starty = 0;
        Rectangle2D viewArea = region.getViewArea();
        float width = (float)(viewArea.getWidth());
        float height = (float)(viewArea.getHeight());
        /*
        Trait.Background back;
        back = (Trait.Background)region.getTrait(Trait.BACKGROUND);
        */
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
        float startx = currentIPPosition;
        float starty = currentBPPosition;
        drawBackAndBorders(block, startx, starty,
                           block.getWidth(), block.getHeight());
    }

    /**
     * Draw the background and borders.
     * This draws the background and border traits for an area given
     * the position.
     *
     * @param block the area to get the traits from
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area block,
                                    float startx, float starty, 
                                    float width, float height) {
        // draw background then border

        boolean started = false;
        Trait.Background back;
        back = (Trait.Background)block.getTrait(Trait.BACKGROUND);
        if (back != null) {
            started = true;
//            closeText();
            endTextObject();
            //saveGraphicsState();

            if (back.getColor() != null) {
                updateColor(back.getColor(), true, null);
                writeln(startx + " " + starty + " "
                                  + width + " " + height + " rectfill");
            }
            if (back.getURL() != null) {
                ImageFactory fact = ImageFactory.getInstance();
                FopImage fopimage = fact.getImage(back.getURL(), userAgent);
                if (fopimage != null && fopimage.load(FopImage.DIMENSIONS, userAgent)) {
                    if (back.getRepeat() == BackgroundRepeat.REPEAT) {
                        // create a pattern for the image
                    } else {
                        // place once
                        Rectangle2D pos;
                        pos = new Rectangle2D.Float((startx + back.getHoriz()) * 1000,
                                                    (starty + back.getVertical()) * 1000,
                                                    fopimage.getWidth() * 1000,
                                                    fopimage.getHeight() * 1000);
                       // putImage(back.url, pos);
                    }
                }
            }
        }

        BorderProps bps = (BorderProps)block.getTrait(Trait.BORDER_BEFORE);
        if (bps != null) {
            float endx = startx + width;

            if (!started) {
                started = true;
//                closeText();
                endTextObject();
                //saveGraphicsState();
            }

            float bwidth = bps.width;
            updateColor(bps.color, false, null);
            writeln(bwidth + " setlinewidth");

            drawLine(startx, starty + bwidth / 2, endx, starty + bwidth / 2);
        }
        bps = (BorderProps)block.getTrait(Trait.BORDER_START);
        if (bps != null) {
            float endy = starty + height;

            if (!started) {
                started = true;
//                closeText();
                endTextObject();
                //saveGraphicsState();
            }

            float bwidth = bps.width;
            updateColor(bps.color, false, null);
            writeln(bwidth + " setlinewidth");

            drawLine(startx + bwidth / 2, starty, startx + bwidth / 2, endy);
        }
        bps = (BorderProps)block.getTrait(Trait.BORDER_AFTER);
        if (bps != null) {
            float sy = starty + height;
            float endx = startx + width;

            if (!started) {
                started = true;
//                closeText();
                endTextObject();
                //saveGraphicsState();
            }

            float bwidth = bps.width;
            updateColor(bps.color, false, null);
            writeln(bwidth + " setlinewidth");

            drawLine(startx, sy - bwidth / 2, endx, sy - bwidth / 2);
        }
        bps = (BorderProps)block.getTrait(Trait.BORDER_END);
        if (bps != null) {
            float sx = startx + width;
            float endy = starty + height;

            if (!started) {
                started = true;
 //               closeText();
                endTextObject();
                //saveGraphicsState();
            }

            float bwidth = bps.width;
            updateColor(bps.color, false, null);
            writeln(bwidth + " setlinewidth");
            drawLine(sx - bwidth / 2, starty, sx - bwidth / 2, endy);
        }
        if (started) {
            //restoreGraphicsState();
            beginTextObject();
            // font last set out of scope in text section
            currentFontName = "";
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
        writeln(startx + " " + starty + " M ");
        writeln(endx + " " + endy + " lineto");
    }
    
    private void updateColor(ColorType col, boolean fill, StringBuffer pdf) {
        writeln(gen.formatDouble(col.getRed()) + " " 
                        + gen.formatDouble(col.getGreen()) + " " 
                        + gen.formatDouble(col.getBlue()) + " setrgbcolor");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos);
    }

    /**
     * Renders an XML document (SVG for example).
     * @param doc DOM Document containing the XML document to be rendered
     * @param ns Namespace for the XML document
     * @param pos Position for the generated graphic/image
     */
    public void renderDocument(Document doc, String ns, Rectangle2D pos) {
        RendererContext context;
        context = new RendererContext(MIME_TYPE);
        context.setUserAgent(userAgent);

        context.setProperty(PSXMLHandler.PS_GENERATOR, this.gen);
        context.setProperty(PSXMLHandler.PS_FONT_INFO, fontInfo);
        context.setProperty(PSXMLHandler.PS_WIDTH,
                            new Integer((int) pos.getWidth()));
        context.setProperty(PSXMLHandler.PS_HEIGHT,
                            new Integer((int) pos.getHeight()));
        context.setProperty(PSXMLHandler.PS_XPOS,
                            new Integer(currentBlockIPPosition + (int) pos.getX()));
        context.setProperty(PSXMLHandler.PS_YPOS,
                            new Integer(currentBPPosition + (int) pos.getY()));
        //context.setProperty("strokeSVGText", options.get("strokeSVGText"));
        
        /*
        context.setProperty(PDFXMLHandler.PDF_DOCUMENT, pdfDoc);
        context.setProperty(PDFXMLHandler.OUTPUT_STREAM, ostream);
        context.setProperty(PDFXMLHandler.PDF_STATE, currentState);
        context.setProperty(PDFXMLHandler.PDF_PAGE, currentPage);
        context.setProperty(PDFXMLHandler.PDF_CONTEXT, 
                    currentContext == null ? currentPage: currentContext);
        context.setProperty(PDFXMLHandler.PDF_CONTEXT, currentContext);
        context.setProperty(PDFXMLHandler.PDF_STREAM, currentStream);
        context.setProperty(PDFXMLHandler.PDF_XPOS,
                            new Integer(currentBlockIPPosition + (int) pos.getX()));
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
        */           
        userAgent.renderXML(context, doc, ns);

    }

    


}
