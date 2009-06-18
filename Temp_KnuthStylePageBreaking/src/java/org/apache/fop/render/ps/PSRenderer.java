/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.render.ps;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

// FOP
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.fop.area.Area;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.PrintRenderer;
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
public class PSRenderer extends PrintRenderer {

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

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        super.configure(cfg);
        this.autoRotateLandscape = cfg.getChild("auto-rotate-landscape").getValueAsBoolean(false);

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
        PSXMLHandler xmlHandler = new PSXMLHandler();
        //userAgent.setDefaultXMLHandler(MIME_TYPE, xmlHandler);
        String svg = "http://www.w3.org/2000/svg";
        addXMLHandler(userAgent, MIME_TYPE, svg, xmlHandler);
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
     * Draws a filled rectangle.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w width
     * @param h height
     * @param col color to fill with
     */
    protected void fillRect(float x, float y, float w, float h,
                                 ColorType col) {
        useColor(col);
        writeln(gen.formatDouble(x) 
            + " " + gen.formatDouble(y) 
            + " " + gen.formatDouble(w) 
            + " " + gen.formatDouble(h) 
            + " rectfill");
    }

    /**
     * Draws a stroked rectangle with the current stroke settings.
     * @param x x-coordinate
     * @param y y-coordinate
     * @param w width
     * @param h height
     */
    protected void drawRect(float x, float y, float w, float h) {
        writeln(gen.formatDouble(x) 
            + " " + gen.formatDouble(y) 
            + " " + gen.formatDouble(w) 
            + " " + gen.formatDouble(h) 
            + " rectstroke");
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
            writeln(gen.formatDouble(red)
                + " " + gen.formatDouble(green)
                + " " + gen.formatDouble(blue)
                + " setrgbcolor");
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
        gen.writeDSCComment(DSCConstants.CREATOR, new String[] { userAgent.getProducer() });
        gen.writeDSCComment(DSCConstants.CREATION_DATE, new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(gen.getPSLevel()));
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
                {page.getPageNumberString(),
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
    protected void paintText(int rx, int bl, String text, Typeface font) {
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
     * @see org.apache.fop.render.Renderer#renderText(TextArea)
     */
    public void renderText(TextArea area) {
        String fontname = (String)area.getTrait(Trait.FONT_NAME);
        int fontsize = area.getTraitAsInteger(Trait.FONT_SIZE);

        // This assumes that *all* CIDFonts use a /ToUnicode mapping
        Typeface f = (Typeface) fontInfo.getFonts().get(fontname);

        //Determine position
        int rx = currentIPPosition;
        int bl = currentBPPosition + area.getOffset();

        useFont(fontname, fontsize);
        ColorType ct = (ColorType)area.getTrait(Trait.COLOR);
        if (ct != null) {
            useColor(ct);
        }
        paintText(rx, bl, area.getTextArea(), f);

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
        super.renderText(area); //Updates IPD
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
                int width = bv.getIPD();
                int height = bv.getBPD();
                clip(x, y, width, height);
            }

            CTM tempctm = new CTM(containingIPPosition, containingBPPosition);
            ctm = tempctm.multiply(ctm);

            startVParea(ctm);
            handleBlockTraits(bv);
            renderBlocks(bv, children);
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
                    ctm = ctm.translate(-saveBP - bv.getBPD(), -saveIP);
                } else if (vals[0] == -1.0) {
                    ctm = ctm.translate(-saveIP - bv.getIPD(), -saveBP - bv.getBPD());
                } else {
                    ctm = ctm.translate(saveBP, saveIP - bv.getIPD());
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
                int width = bv.getIPD();
                int height = bv.getBPD();
                clip(x, y, width, height);
            }

            if (ctm != null) {
                startVParea(ctm);
            }
            handleBlockTraits(bv);
            renderBlocks(bv, children);
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
            currentBPPosition += (int)(bv.getAllocBPD());
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
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param region the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport region) {
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
                           block.getIPD(), block.getBPD());
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
                fillRect(startx, starty, width, height, back.getColor());
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
            useColor(bps.color);
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
            useColor(bps.color);
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
            useColor(bps.color);
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
            useColor(bps.color);
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
                            new Integer(currentIPPosition + (int) pos.getX()));
        context.setProperty(PSXMLHandler.PS_YPOS,
                            new Integer(currentBPPosition + (int) pos.getY()));
        //context.setProperty("strokeSVGText", options.get("strokeSVGText"));
        
        renderXML(userAgent, context, doc, ns);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

}
