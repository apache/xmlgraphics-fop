/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fonts.Font;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.AbstractRenderer;


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
 * @todo Rebuild the PostScript renderer
 */
public class PSRenderer extends AbstractRenderer {

    /**
     * the application producing the PostScript
     */
    protected String producer;

    private boolean enableComments = true;

    /**
     * the stream used to output the PostScript
     */
    protected PSStream out;
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
     * Write out a command
     * @param cmd PostScript command
     */
    protected void write(String cmd) {
        try {
            out.write(cmd);
        } catch (IOException e) {
            if (!ioTrouble) {
                e.printStackTrace();
            }
            ioTrouble = true;
        }
    }

    /**
     * Write out a comment
     * @param comment Comment to write
     */
    protected void comment(String comment) {
        if (this.enableComments) {
            write(comment);
        }
    }

    /**
     * Generates the PostScript code for the font dictionary.
     * @param fontInfo available fonts
     */
    protected void writeFontDict(FontInfo fontInfo) {
        write("%%BeginResource: procset FOPFonts");
        write("%%Title: Font setup (shortcuts) for this file");
        write("/FOPFonts 100 dict dup begin");
        write("/bd{bind def}bind def");
        write("/ld{load def}bd");
        write("/M/moveto ld");
        write("/RM/rmoveto ld");
        write("/t/show ld");

        write("/ux 0.0 def");
        write("/uy 0.0 def");
        // write("/cf /Helvetica def");
        // write("/cs 12000 def");

        // <font> <size> F
        write("/F {");
        write("  /Tp exch def");
        // write("  currentdict exch get");
        write("  /Tf exch def");
        write("  Tf findfont Tp scalefont setfont");
        write("  /cf Tf def  /cs Tp def  /cw ( ) stringwidth pop def");
        write("} bd");

        write("/ULS {currentpoint /uy exch def /ux exch def} bd");
        write("/ULE {");
        write("  /Tcx currentpoint pop def");
        write("  gsave");
        write("  newpath");
        write("  cf findfont cs scalefont dup");
        write("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
        write("  /UnderlinePosition get Ts mul /To exch def");
        write("  /UnderlineThickness get Ts mul /Tt exch def");
        write("  ux uy To add moveto  Tcx uy To add lineto");
        write("  Tt setlinewidth stroke");
        write("  grestore");
        write("} bd");

        write("/OLE {");
        write("  /Tcx currentpoint pop def");
        write("  gsave");
        write("  newpath");
        write("  cf findfont cs scalefont dup");
        write("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
        write("  /UnderlinePosition get Ts mul /To exch def");
        write("  /UnderlineThickness get Ts mul /Tt exch def");
        write("  ux uy To add cs add moveto Tcx uy To add cs add lineto");
        write("  Tt setlinewidth stroke");
        write("  grestore");
        write("} bd");

        write("/SOE {");
        write("  /Tcx currentpoint pop def");
        write("  gsave");
        write("  newpath");
        write("  cf findfont cs scalefont dup");
        write("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
        write("  /UnderlinePosition get Ts mul /To exch def");
        write("  /UnderlineThickness get Ts mul /Tt exch def");
        write("  ux uy To add cs 10 mul 26 idiv add moveto "
                    + "Tcx uy To add cs 10 mul 26 idiv add lineto");
        write("  Tt setlinewidth stroke");
        write("  grestore");
        write("} bd");



        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        Map fonts = fontInfo.getFonts();
        Iterator enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            write("/" + key + " /" + fm.getFontName() + " def");
        }
        write("end def");
        write("%%EndResource");
        enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            write("/" + fm.getFontName() + " findfont");
            write("dup length dict begin");
            write("  {1 index /FID ne {def} {pop pop} ifelse} forall");
            write("  /Encoding ISOLatin1Encoding def");
            write("  currentdict");
            write("end");
            write("/" + fm.getFontName() + " exch definefont pop");
        }
    }

    /**
     * Make sure the cursor is in the right place.
     */
    protected void movetoCurrPosition() {
        write(this.currentIPPosition + " " + this.currentBPPosition + " M");
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
    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
        write("newpath");
        write(x + " " + y + " M");
        write(w + " 0 rlineto");
        write("0 " + (-h) + " rlineto");
        write((-w) + " 0 rlineto");
        write("0 " + h + " rlineto");
        write("closepath");
        useColor(col);
        write("fill");
    }

    /**
     * Changes the currently used font.
     * @param name name of the font
     * @param size font size
     */
    public void useFont(String name, int size) {
        if ((currentFontName != name) || (currentFontSize != size)) {
            write(name + " " + size + " F");
            currentFontName = name;
            currentFontSize = size;
        }
    }

    private void useColor(ColorType col) {
        useColor(col.red(), col.green(), col.blue());
    }

    private void useColor(float red, float green, float blue) {
        if ((red != currRed) || (green != currGreen) || (blue != currBlue)) {
            write(red + " " + green + " " + blue + " setrgbcolor");
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

        this.out = new PSStream(outputStream);
        write("%!PS-Adobe-3.0");
        write("%%Creator: " + this.producer);
        write("%%DocumentProcessColors: Black");
        write("%%DocumentSuppliedResources: procset FOPFonts");
        write("%%EndComments");
        write("%%BeginDefaults");
        write("%%EndDefaults");
        write("%%BeginProlog");
        write("%%EndProlog");
        write("%%BeginSetup");
        writeFontDict(fontInfo);

        /* Write proc for including EPS */
        write("%%BeginResource: procset EPSprocs");
        write("%%Title: EPS encapsulation procs");

        write("/BeginEPSF { %def");
        write("/b4_Inc_state save def         % Save state for cleanup");
        write("/dict_count countdictstack def % Count objects on dict stack");
        write("/op_count count 1 sub def      % Count objects on operand stack");
        write("userdict begin                 % Push userdict on dict stack");
        write("/showpage { } def              % Redefine showpage, { } = null proc");
        write("0 setgray 0 setlinecap         % Prepare graphics state");
        write("1 setlinewidth 0 setlinejoin");
        write("10 setmiterlimit [ ] 0 setdash newpath");
        write("/languagelevel where           % If level not equal to 1 then");
        write("{pop languagelevel             % set strokeadjust and");
        write("1 ne                           % overprint to their defaults.");
        write("{false setstrokeadjust false setoverprint");
        write("} if");
        write("} if");
        write("} bind def");

        write("/EndEPSF { %def");
        write("count op_count sub {pop} repeat            % Clean up stacks");
        write("countdictstack dict_count sub {end} repeat");
        write("b4_Inc_state restore");
        write("} bind def");
        write("%%EndResource");

        write("%%EndSetup");
        write("FOPFonts begin");
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        write("%%Trailer");
        write("%%EOF");
        this.out.flush();
    }

}
