/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.ps;

// FOP
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.Renderer;
import org.apache.fop.image.FopImage;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.render.pdf.Font;
import org.apache.fop.image.*;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

// SVG
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

// Java
import java.io.*;
import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.HashMap;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Dimension;

/**
 * Renderer that renders to PostScript.
 * <br>
 * This class currently generates PostScript Level 2 code. The only exception
 * is the FlateEncode filter which is a Level 3 feature. The filters in use
 * are hardcoded at the moment.
 * <br>
 * This class follows the Document Structuring Conventions (DSC) version 3.0
 * (If I did everything right). If anyone modifies this renderer please make
 * sure to also follow the DSC to make it simpler to programmatically modify
 * the generated Postscript files (ex. extract pages etc.).
 * <br>
 * TODO: Character size/spacing, SVG Transcoder for Batik, configuration, move
 * to PrintRenderer, maybe improve filters (I'm not very proud of them), add a
 * RunLengthEncode filter (useful for Level 2 Postscript), Improve
 * DocumentProcessColors stuff (probably needs to be configurable, then maybe
 * add a color to grayscale conversion for bitmaps to make output smaller (See
 * PCLRenderer), font embedding, support different character encodings, try to
 * implement image transparency, positioning of images is wrong etc. <P>
 *
 * Modified by Mark Lillywhite mark-fop@inomial.com, to use the new
 * Renderer interface. This PostScript renderer appears to be the
 * most efficient at producing output.
 *
 * @author Jeremias Märki
 */
public class PSRenderer extends AbstractRenderer {

    /**
     * the application producing the PostScript
     */
    protected String producer;

    int imagecount = 0;    // DEBUG

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

    protected IDReferences idReferences;

    /**
     * set the document's producer
     *
     * @param producer string indicating application producing the PostScript
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * write out a command
     */
    protected void write(String cmd) {
        try {
            out.write(cmd);
        } catch (IOException e) {
            if (!ioTrouble)
                e.printStackTrace();
            ioTrouble = true;
        }
    }

    /**
     * write out a comment
     */
    protected void comment(String comment) {
        if (this.enableComments)
            write(comment);
    }

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
        write("  ux uy To add cs 10 mul 26 idiv add moveto Tcx uy To add cs 10 mul 26 idiv add lineto");
        write("  Tt setlinewidth stroke");
        write("  grestore");
        write("} bd");



        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        HashMap fonts = fontInfo.getFonts();
        Iterator enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            write("/" + key + " /" + fm.fontName() + " def");
        }
        write("end def");
        write("%%EndResource");
        enum = fonts.keySet().iterator();
        while (enum.hasNext()) {
            String key = (String)enum.next();
            Font fm = (Font)fonts.get(key);
            write("/" + fm.fontName() + " findfont");
            write("dup length dict begin");
            write("  {1 index /FID ne {def} {pop pop} ifelse} forall");
            write("  /Encoding ISOLatin1Encoding def");
            write("  currentdict");
            write("end");
            write("/" + fm.fontName() + " exch definefont pop");
        }
    }

    protected void movetoCurrPosition() {
        write(this.currentIPPosition + " " + this.currentBPPosition + " M");
    }

    /**
     * set up the font info
     *
     * @param fontInfo the font info object to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo);
        this.fontInfo = fontInfo;
    }

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

    private long copyStream(InputStream in, OutputStream out,
                            int bufferSize) throws IOException {
        long bytes_total = 0;
        byte[] buf = new byte[bufferSize];
        int bytes_read;
        while ((bytes_read = in.read(buf)) != -1) {
            bytes_total += bytes_read;
            out.write(buf, 0, bytes_read);
        }
        return bytes_total;
    }


    private long copyStream(InputStream in,
                            OutputStream out) throws IOException {
        return copyStream(in, out, 4096);
    }

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
    */
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.debug("rendering areas to PostScript");

        this.out = new PSStream(outputStream);
        write("%!PS-Adobe-3.0");
        write("%%Creator: "+this.producer);
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
    */
    public void stopRenderer()
    throws IOException {
        write("%%Trailer");
        write("%%EOF");
        this.out.flush();
    }

}
