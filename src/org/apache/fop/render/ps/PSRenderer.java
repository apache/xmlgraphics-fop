/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// FOP
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
 * @todo Rebuild the PostScript renderer
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public class PSRenderer extends AbstractRenderer {

    /** The MIME type for PostScript */
    public static final String MIME_TYPE = "application/postscript";

    /** The application producing the PostScript */
    protected String producer;
    private int currentPageNumber = 0;

    private boolean enableComments = true;

    /** The PostScript generator used to output the PostScript */
    protected PSGenerator gen;
    private boolean ioTrouble = false;

    private String currentFontName;
    private int currentFontSize;
    private int pageHeight;
    private int pageWidth;
    private float currRed;
    private float currGreen;
    private float currBlue;

    private FontInfo fontInfo;

    /**
     * Set the document's producer
     *
     * @param producer string indicating application producing the PostScript
     */
    public void setProducer(String producer) {
        this.producer = producer;
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
     * Generates the PostScript code for the font dictionary.
     * @param fontInfo available fonts
     */
    protected void writeFontDict(FontInfo fontInfo) {
        writeln("%%BeginResource: procset FOPFonts");
        writeln("%%Title: Font setup (shortcuts) for this file");
        writeln("/FOPFonts 100 dict dup begin");

        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        Map fonts = fontInfo.getFonts();
        Iterator enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            writeln("/" + key + " /" + fm.getFontName() + " def");
        }
        writeln("end def");
        writeln("%%EndResource");
        enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            writeln("/" + fm.getFontName() + " findfont");
            writeln("dup length dict begin");
            writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
            writeln("  /Encoding ISOLatin1Encoding def");
            writeln("  currentdict");
            writeln("end");
            writeln("/" + fm.getFontName() + " exch definefont pop");
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
        useColor(col.red(), col.green(), col.blue());
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
        writeFontDict(fontInfo);
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
        final Long pagewidth = new Long(Math.round(page.getViewArea().getWidth() / 1000f));
        final Long pageheight = new Long(Math.round(page.getViewArea().getHeight() / 1000f));
        gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                {zero, zero, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);         
        gen.writeln("FOPFonts begin");
        concatMatrix(1, 0, 0, -1, 0, pageheight.doubleValue());
        gen.writeln("0.001 0.001 scale");
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
            gen.escapeChar(mapped, sb);
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
        //currentStream.add(CTMHelper.toPDFString(ctm) + " cm\n");
        final double matrix[] = ctm.toArray();
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
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos);
    }

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
