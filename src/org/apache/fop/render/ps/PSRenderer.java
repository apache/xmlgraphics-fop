/*
 * $Id$
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

// FOP
import org.apache.fop.svg.SVGArea;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.image.JpegImage;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.DisplaySpace;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.LineArea;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.Box;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.layout.inline.InlineSpace;
import org.apache.fop.layout.inline.LeaderArea;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.fo.properties.RuleStyle;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.render.pdf.Font;
import org.apache.fop.image.SVGImage;
import org.apache.fop.image.EPSImage;
import org.apache.fop.apps.FOPException;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.GraphicsNode;

// SVG
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.awt.geom.AffineTransform;

/*
PostScript renderer

Remarks:
- If anyone modifies this renderer please make sure to also follow the DSC to
  make it simpler to programmatically modify the generated Postscript files
  (ex. extract pages etc.).
- The filters in use are hardcoded at the moment.
- Modified by Mark Lillywhite mark-fop@inomial.com, to use the new
  Renderer interface. This PostScript renderer appears to be the
  most efficient at producing output.

TODO-List:
- Character size/spacing
- SVG Transcoder for Batik
- configuration
- move to PrintRenderer
- maybe improve filters (I'm not very proud of them)
- add a RunLengthEncode filter (useful for Level 2 Postscript)
- Improve DocumentProcessColors stuff (probably needs to be configurable, then maybe
  add a color to grayscale conversion for bitmaps to make output smaller (See
  PCLRenderer)
- enhanced font support and font embedding
- fix character encodings (Helvetica uses WinAnsiEncoding internally but is
  encoded as ISOLatin1 in PS)
- try to implement image transparency
- Add PPD support
- fix border painting (see table.fo)

*/

/**
 * Renderer that renders to PostScript.
 * <br>
 * This class currently generates PostScript Level 2 code. The only exception
 * is the FlateEncode filter which is a Level 3 feature. The PostScript code
 * generated follows the Document Structuring Conventions (DSC) version 3.0.
 *
 * @author Jeremias Märki
 */
public class PSRenderer extends AbstractRenderer {

    /**
     * the application producing the PostScript
     */
    protected String producer;

    int imagecount = 0;    // DEBUG
    int pagecount = 0;

    private boolean enableComments = true;
    private boolean autoRotateLandscape = false;

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

    private int psLevel = 3;

    protected java.util.Map options;


    /**
     * set the document's producer
     *
     * @param producer string indicating application producing the PostScript
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }


    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }


    /**
     * Sets the PostScript Level to generate.
     *
     * @param level You can specify either 2 or 3 for the PostScript Level
     */
    public void setPSLevel(int level) {
        switch (level) {
            case 2:
            case 3:
                this.psLevel = level;
                break;
            default:
                throw new IllegalArgumentException("Only PostScript Levels 2 and 3 are supported");
        }
    }


    public int getPSLevel() {
        return this.psLevel;
    }

    public void setAutoRotateLandscape(boolean value) {
        this.autoRotateLandscape = value;
    }

    public boolean isAutoRotateLandscape() {
        return this.autoRotateLandscape;
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

    protected void writeProcs() {
        write("%%BeginResource: procset FOPprocs");
        write("%%Title: Utility procedures");
        write("/FOPprocs 20 dict dup begin");
        write("/bd{bind def}bind def");
        write("/ld{load def}bd");
        write("/M/moveto ld");
        write("/RM/rmoveto ld");
        write("/t/show ld");
        write("/A/ashow ld");
        write("/cp/closepath ld");
        write("/re {4 2 roll M"); //define rectangle
        write("1 index 0 rlineto");
        write("0 exch rlineto");
        write("neg 0 rlineto");
        write("cp } bd");

        write("/ux 0.0 def");
        write("/uy 0.0 def");

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
        write("end def");
        write("%%EndResource");
    }

    protected void writeFontDict(FontInfo fontInfo) {
        write("%%BeginResource: procset FOPFonts");
        write("%%Title: Font setup (shortcuts) for this file");
        write("/FOPFonts 100 dict dup begin");
        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        Map fonts = fontInfo.getFonts();
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Font fm = (Font)fonts.get(key);
            write("/" + key + " /" + fm.fontName() + " def");
        }
        write("end def");
        write("%%EndResource");
        defineWinAnsiEncoding();

        //Rewrite font encodings
        iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Font fm = (Font)fonts.get(key);
            if (null == fm.encoding()) {
                //ignore (ZapfDingbats and Symbol run through here
                //TODO: ZapfDingbats and Symbol should get getEncoding() fixed!
            } else if ("WinAnsiEncoding".equals(fm.encoding())) {
                write("/" + fm.fontName() + " findfont");
                write("dup length dict begin");
                write("  {1 index /FID ne {def} {pop pop} ifelse} forall");
                write("  /Encoding " + fm.encoding() + " def");
                write("  currentdict");
                write("end");
                write("/" + fm.fontName() + " exch definefont pop");
            } else {
                log.warn("Only WinAnsiEncoding is supported. Font '"
                    + fm.fontName() + "' asks for: " + fm.encoding());
            }
        }
    }

    private void defineWinAnsiEncoding() {
        write("/WinAnsiEncoding [");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Glyphs.winAnsiEncoding.length; i++) {
            if (i > 0) {
                if ((i % 5) == 0) {
                    write(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append(" ");
                }
            }
            final char ch = Glyphs.winAnsiEncoding[i];
            final String glyphname = Glyphs.charToGlyphName(ch);
            if ("".equals(glyphname)) {
                sb.append("/" + Glyphs.notdef);
            } else {
                sb.append("/");
                sb.append(glyphname);
            }
        }
        write(sb.toString());
        write("] def");
    }



    protected void movetoCurrPosition() {
        write(this.currentXPosition + " " + this.currentYPosition + " M");
    }

    /**
     * set up the font info
     *
     * @param fontInfo the font info object to set up
     */
    public void setupFontInfo(FontInfo fontInfo)
        throws FOPException {
        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo);
        this.fontInfo = fontInfo;
    }

    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
        // XXX: cater for braindead, legacy -ve heights
        if (h < 0) {
           h = -h;
        }

            write("newpath");
            write(x + " " + y + " " + w + " " + -h + " re");
            /*
            write(x + " " + y + " M");
            write(w + " 0 rlineto");
            write("0 " + (-h) + " rlineto");
            write((-w) + " 0 rlineto");
            write("0 " + h + " rlineto");
            write("closepath");
            */
            useColor(col);
            write("fill");
    }

    /**
     * render a display space to PostScript
     *
     * @param space the space to render
     */
    public void renderDisplaySpace(DisplaySpace space) {
        // write("% --- DisplaySpace size="+space.getSize());
        this.currentYPosition -= space.getSize();
    }

    /**
     * render a foreign object area
     */
    public void renderForeignObjectArea(ForeignObjectArea area) {
        // if necessary need to scale and align the content
        this.currentXPosition = this.currentXPosition + area.getXOffset();
        int plOffset = 0;
        Area parent = area.getParent();
        if (parent instanceof LineArea) {
            plOffset = ((LineArea)parent).getPlacementOffset();
        }
        this.currentYPosition += plOffset;
        area.getObject().render(this);
        this.currentXPosition += area.getEffectiveWidth();
        this.currentYPosition -= plOffset;
    }

    /**
     * render an SVG area to PostScript
     *
     * @param area the area to render
     */
    public void renderSVGArea(SVGArea area) {
        int x = this.currentXPosition;
        int y = this.currentYPosition;
        renderSVGDocument(area.getSVGDocument(), x, y, area.getFontState());
    }


    /**
     * render SVG document to PostScript
     *
     * @param doc  the document to render
     * @param x    the x offset
     * @param y    the y offset
     * @param fs   the fontstate to use
     */
    protected void renderSVGDocument(Document doc, int x, int y,
            FontState fs) {
        org.apache.fop.svg.SVGUserAgent userAgent
            = new org.apache.fop.svg.SVGUserAgent(new AffineTransform());
        userAgent.setLogger(log);

        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);

        GraphicsNode root;
        try {
            root = builder.build(ctx, doc);
        } catch (Exception e) {
            log.error("svg graphic could not be built: "
                                   + e.getMessage(), e);
            return;
        }
        // get the 'width' and 'height' attributes of the SVG document
        float w = (float)ctx.getDocumentSize().getWidth() * 1000f;
        float h = (float)ctx.getDocumentSize().getHeight() * 1000f;

        //log.debug("drawing SVG image: "+x+"/"+y+" "+w+"/"+h);
        SVGSVGElement svg = ((SVGDocument) doc).getRootElement();
        AffineTransform at = ViewBox.getPreserveAspectRatioTransform(svg,
                w/1000f , h/1000f );

        ctx = null;
        builder = null;

        float sx = 1, sy = -1;
        int xOffset = x, yOffset = y;

        comment("% --- SVG Area");
        write("gsave");
        if (w != 0 && h != 0) {
            write("newpath");
            write(x + " " + y + " M");
            write((x + w) + " " + y + " rlineto");
            write((x + w) + " " + (y - h) + " rlineto");
            write(x + " " + (y - h) + " rlineto");
            write("closepath");
            write("clippath");
        }
        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right. (0,0) is where the
        // viewBox puts it.
        write(xOffset + " " + yOffset + " translate");
        write((at.getTranslateX() * 1000) + " "
            + (-at.getTranslateY() * 1000) + " translate");
        write(sx * at.getScaleX() + " " + sy * at.getScaleY() + " scale");

        PSGraphics2D graphics = new PSGraphics2D(false, fs,
                                this, currentFontName,
                                currentFontSize,
                                currentXPosition,
                                currentYPosition);
        graphics.setGraphicContext(new org.apache.batik.ext.awt.g2d.GraphicContext());
        try {
            root.paint(graphics);
        } catch (Exception e) {
            log.error("svg graphic could not be rendered: "
                                   + e.getMessage(), e);
        }

        write("grestore");

        comment("% --- SVG Area end");
    }

    /**
     * Renders an image, scaling it to the given width and height.
     * If the scaled width and height is the same intrinsic size
     * of the image, the image is not scaled.
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param image the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected void drawImageScaled(int x, int y, int w, int h,
                   FopImage image,
                   FontState fs) {
        //log.debug("drawing scaled image: "+x+"/"+y+" "+w+"/"+h);
        if (image instanceof SVGImage) {
            try {
                renderSVGDocument(((SVGImage)image).getSVGDocument(), x, y, fs);
            } catch (FopImageException e) {
                log.error("Error rendering SVG image", e);
            }
        } else if (image instanceof EPSImage) {
            renderEPS(image, x, y, w, h);
        } else {
            renderBitmap(image, x, y, w, h);
        }
    }

    /**
     * Renders an image, clipping it as specified.
     *
     * @param x the x position of left edge in millipoints.
     * @param y the y position of top edge in millipoints.
     * @param clipX the left edge of the clip in millipoints
     * @param clipY the top edge of the clip in millipoints
     * @param clipW the clip width in millipoints
     * @param clipH the clip height in millipoints
     * @param image the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected void drawImageClipped(int x, int y,
                    int clipX, int clipY,
                    int clipW, int clipH,
                    FopImage image,
                    FontState fs) {
        //log.debug("drawing clipped image: "+x+"/"+y+" "+clipX+"/"+clipY+" "+clipW+"/"+clipH);
        write("gsave");
        write(clipX + " " + clipY + " " + clipW + " " + clipH + " re");
        write("clippath");

        try {
            int w = image.getWidth() * 1000;
            int h = image.getHeight() * 1000;

            drawImageScaled(x, y, w, h, image, fs);
        } catch (FopImageException e) {
            log.error("Error getting image extents", e);
        }
        write("grestore");
    }

    public void renderEPS(FopImage img, int x, int y, int w, int h) {
        try {
            EPSImage eimg = (EPSImage)img;
            int[] bbox = eimg.getBBox();
            int bboxw = bbox[2] - bbox[0];
            int bboxh = bbox[3] - bbox[1];


            write("%%BeginDocument: " + eimg.getDocName());
            write("BeginEPSF");

            write(x + " " + (y - h) + " translate");
            write("0.0 rotate");
            write((long)(w/bboxw) + " " + (long)(h/bboxh) + " scale");
            write(-bbox[0] + " " + (-bbox[1]) + " translate");
            write(bbox[0] + " " + bbox[1] + " " + bboxw + " " + bboxh + " rectclip");
            write("newpath");
            out.writeByteArr(img.getBitmaps());
            write("%%EndDocument");
            write("EndEPSF");
            write("");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("PSRenderer.renderImageArea(): Error rendering bitmap ("
                                   + e.getMessage() + ")", e);
        }
    }

    public void renderBitmap(FopImage img, int x, int y, int w, int h) {
        try {
            boolean iscolor = img.getColorSpace().getColorSpace()
                              != ColorSpace.DEVICE_GRAY;
            byte[] imgmap = img.getBitmaps();

            write("gsave");
            if (img.getColorSpace().getColorSpace() == ColorSpace.DEVICE_CMYK)
                write("/DeviceCMYK setcolorspace");
            else if (img.getColorSpace().getColorSpace() == ColorSpace.DEVICE_GRAY)
                write("/DeviceGray setcolorspace");
            else
                write("/DeviceRGB setcolorspace");

            write(x + " " + (y - h) + " translate");
            write(w + " " + h + " scale");

            write("{{");
            // Template: (RawData is used for the EOF signal only)
            // write("/RawData currentfile <first filter> filter def");
            // write("/Data RawData <second filter> <third filter> [...] def");
            if (img instanceof JpegImage) {
                write("/RawData currentfile /ASCII85Decode filter def");
                write("/Data RawData << >> /DCTDecode filter def");
            } else {
                if (this.psLevel >= 3) {
                    write("/RawData currentfile /ASCII85Decode filter def");
                    write("/Data RawData /FlateDecode filter def");
                } else {
                    write("/RawData currentfile /ASCII85Decode filter def");
                    write("/Data RawData /RunLengthDecode filter def");
                }
            }
            write("<<");
            write("  /ImageType 1");
            write("  /Width " + img.getWidth());
            write("  /Height " + img.getHeight());
            write("  /BitsPerComponent 8");
            if (img.getColorSpace().getColorSpace() == ColorSpace.DEVICE_CMYK) {
                if (img.invertImage())
                    write("  /Decode [1 0 1 0 1 0 1 0]");
                else
                    write("  /Decode [0 1 0 1 0 1 0 1]");
            } else if (iscolor) {
                write("  /Decode [0 1 0 1 0 1]");
            } else {
                write("  /Decode [0 1]");
            }
            // Setup scanning for left-to-right and top-to-bottom
            write("  /ImageMatrix [" + img.getWidth() + " 0 0 -"
                  + img.getHeight() + " 0 " + img.getHeight() + "]");

            write("  /DataSource Data");
            write(">>");
            write("image");
            /* the following two lines could be enabled if something still goes wrong
             * write("Data closefile");
             * write("RawData flushfile");
             */
            write("} stopped {handleerror} if");
            write("  RawData flushfile");
            write("} exec");

            /*
             * for (int y=0; y<img.getHeight(); y++) {
             * int indx = y * img.getWidth();
             * if (iscolor) indx*= 3;
             * for (int x=0; x<img.getWidth(); x++) {
             * if (iscolor) {
             * writeASCIIHex(imgmap[indx++] & 0xFF);
             * writeASCIIHex(imgmap[indx++] & 0xFF);
             * writeASCIIHex(imgmap[indx++] & 0xFF);
             * } else {
             * writeASCIIHex(imgmap[indx++] & 0xFF);
             * }
             * }
             * }
             */
            try {
                // imgmap[0] = 1;
                OutputStream out = this.out;
                out = new ASCII85OutputStream(out);
                if (img instanceof JpegImage) {
                    //nop
                } else {
                    if (this.psLevel >= 3) {
                        out = new FlateEncodeOutputStream(out);
                    } else {
                        out = new RunLengthEncodeOutputStream(out);
                    }
                }
                out.write(imgmap);
                if (out instanceof Finalizable) {
                    ((Finalizable)out).finalizeStream();
                } else {
                    out.flush();
                }
            } catch (IOException e) {
                if (!ioTrouble)
                    e.printStackTrace();
                ioTrouble = true;
            }

            write("");
            write("grestore");
        } catch (FopImageException e) {
            log.error("PSRenderer.renderImageArea(): Error rendering bitmap ("
                                   + e.getMessage() + ")", e);
        }
    }

    /**
     * Render an image area.
     *
     * @param area the image area to render
     */
    public void renderImageArea(ImageArea area) {
        // adapted from contribution by BoBoGi
        int x = this.currentXPosition + area.getXOffset();
        int ploffset = 0;
        if (area.getParent() instanceof LineArea) {
            ploffset = ((LineArea)area.getParent()).getPlacementOffset();
        }
        int y = this.currentYPosition + ploffset;
        int w = area.getContentWidth();
        int h = area.getHeight();

        //this.currentYPosition -= h;
        this.currentXPosition += w;

        FopImage img = area.getImage();

        if (img == null) {
            log.error("Error while loading image: area.getImage() is null");
        } else {
            drawImageScaled(x, y, w, h, img, area.getFontState());
        }
    }


    /**
     * render an image area to PostScript
     *
     * @param area the area to render
     */
     /*
    public void renderImageArea(ImageArea area) {
        int x = this.currentXPosition + area.getXOffset();
        int ploffset = 0;
        if (area.getParent() instanceof LineArea) {
            ploffset = ((LineArea)area.getParent()).getPlacementOffset();
        }
        int y = this.currentYPosition + ploffset;
        int w = area.getContentWidth();
        int h = area.getHeight();
        this.currentYPosition -= area.getHeight();

        imagecount++;
        // if (imagecount!=4) return;

        comment("% --- ImageArea");
        if (area.getImage() instanceof SVGImage) {
            renderSVGDocument(((SVGImage)area.getImage()).getSVGDocument(), x, y, area.getFontState());
        } else if (area.getImage() instanceof EPSImage) {
            renderEPS(area.getImage(), x, y, w, h);
        } else {
            renderBitmap(area.getImage(), x, y, w, h);
        }
        comment("% --- ImageArea end");
    }*/

    /**
     * render an inline area to PostScript
     *
     * @param area the area to render
     */
    public void renderWordArea(WordArea area) {
        movetoCurrPosition();
        FontState fs = area.getFontState();
        String fontWeight = fs.getFontWeight();
        StringBuffer sb = new StringBuffer();
        String s = area.getText();
        int l = s.length();

        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            char mch = fs.mapChar(ch);

            if (mch > 127) {
                sb = sb.append("\\" + Integer.toOctalString(mch));
            } else {
                final String escape = "\\()[]{}";
                if (escape.indexOf(mch) >= 0) {
                    sb.append("\\");
                }
                sb = sb.append(mch);
            }
        }

        String psString = null;
        if (area.getFontState().getLetterSpacing() > 0) {
            //float f = area.getFontState().getLetterSpacing() * 1000 / this.currentFontSize;
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
    }

    public void useFont(String name, int size) {
        if ((currentFontName != name) || (currentFontSize != size)) {
            write(name + " " + size + " F");
            currentFontName = name;
            currentFontSize = size;
        }
    }

    /**
     * render an inline space to PostScript
     *
     * @param space the space to render
     */
    public void renderInlineSpace(InlineSpace space) {
        // write("% --- InlineSpace size="+space.getSize());
        if (space.getUnderlined() || space.getLineThrough()
                || space.getOverlined()) {
            //start textdeko
            movetoCurrPosition();
            write("ULS");

            write(space.getSize() + " 0 RM");

            //end textdeko
            if (space.getUnderlined())
                write("ULE");
            if (space.getLineThrough())
                write("SOE");
            if (space.getOverlined())
                write("OLE");
        }
        this.currentXPosition += space.getSize();
    }

    /**
     * render a line area to PostScript
     *
     * @param area the area to render
     */
    public void renderLineArea(LineArea area) {
        int rx = this.currentAreaContainerXPosition + area.getStartIndent();
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= area.getPlacementOffset();
        this.currentXPosition = rx;

        int bl = this.currentYPosition;
        // method is identical to super method except next line
        movetoCurrPosition();

        String fontWeight = area.getFontState().getFontWeight();
        //comment("% --- LineArea begin font-weight="+fontWeight);
        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            this.currentYPosition = ry - area.getPlacementOffset();
            b.render(this);
        }
        //comment("% --- LineArea end");

        this.currentYPosition = ry - h;
        this.currentXPosition = rx;
    }

    /**
     * render a page to PostScript
     *
     * @param page the page to render
     */
    public void renderPage(Page page) {
        this.pagecount++;
        this.idReferences = page.getIDReferences();

        write("%%Page: " + page.getNumber() + " " + this.pagecount);

        final long pagewidth = page.getWidth();
        final long pageheight = page.getHeight();
        final double pspagewidth = pagewidth / 1000f;
        final double pspageheight = pageheight / 1000f;
        boolean rotate = false;
        if (isAutoRotateLandscape() && (pageheight < pagewidth)) {
            rotate = true;
            write("%%PageBoundingBox: 0 0 " +
                    Math.round(pspageheight) + " " +
                    Math.round(pspagewidth));
            write("%%PageOrientation: Landscape");
        } else {
            write("%%PageBoundingBox: 0 0 " +
                    Math.round(pspagewidth) + " " +
                    Math.round(pspageheight));
            if (isAutoRotateLandscape()) {
                write("%%PageOrientation: Portrait");
            }
        }

        write("%%BeginPageSetup");
        if (rotate) {
            write(Math.round(pspageheight) + " 0 translate");
            write("90 rotate");
        }
        write("0.001 0.001 scale");
        write("%%EndPageSetup");
        renderRegions(page);
        write("showpage");
        write("%%PageTrailer");
        write("%%EndPage"); //This is non-standard, but used by Adobe.
    }

    /**
     * render a leader area to PostScript
     *
     * @param area the area to render
     */
    public void renderLeaderArea(LeaderArea area) {
        int rx = this.currentXPosition;
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int th = area.getRuleThickness();
        int th2 = th / 2;
        int th3 = th / 3;
        int th4 = th / 4;

        switch (area.getLeaderPattern()) {
        case LeaderPattern.SPACE:
            // NOP

            break;
        case LeaderPattern.RULE:
            if (area.getRuleStyle() == RuleStyle.NONE)
                break;
            useColor(area.getRed(), area.getGreen(), area.getBlue());
            write("gsave");
            write("0 setlinecap");
            switch (area.getRuleStyle()) {
            case RuleStyle.DOTTED:
                write("newpath");
                write("[1000 3000] 0 setdash");
                write(th + " setlinewidth");
                write(rx + " " + ry + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                break;
            case RuleStyle.DASHED:
                write("newpath");
                write("[3000 3000] 0 setdash");
                write(th + " setlinewidth");
                write(rx + " " + ry + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                break;
            case RuleStyle.SOLID:
                write("newpath");
                write(th + " setlinewidth");
                write(rx + " " + ry + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                break;
            case RuleStyle.DOUBLE:
                write("newpath");
                write(th3 + " setlinewidth");
                write(rx + " " + (ry - th3) + " M");
                write(w + " 0 rlineto");
                write(rx + " " + (ry + th3) + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                break;
            case RuleStyle.GROOVE:
                write(th2 + " setlinewidth");
                write("newpath");
                write(rx + " " + (ry - th4) + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                write("newpath");
                write(rx + " " + (ry + th4) + " M");
                write(w + " 0 rlineto");
                useColor(1, 1, 1);    // white
                write("stroke");
                break;
            case RuleStyle.RIDGE:
                write(th2 + " setlinewidth");
                write("newpath");
                write(rx + " " + (ry - th4) + " M");
                write(w + " 0 rlineto");
                useColor(1, 1, 1);    // white
                write("stroke");
                write("newpath");
                write(rx + " " + (ry + th4) + " M");
                write(w + " 0 rlineto");
                useColor(area.getRed(), area.getGreen(), area.getBlue());
                write("stroke");
                break;
            }
            write("grestore");
            break;
        case LeaderPattern.DOTS:
            comment("% --- Leader dots NYI");
            log.error("Leader dots: Not yet implemented");
            break;
        case LeaderPattern.USECONTENT:
            comment("% --- Leader use-content NYI");
            log.error("Leader use-content: Not yet implemented");
            break;
        }
        this.currentXPosition += area.getContentWidth();
        write(area.getContentWidth() + " 0 RM");
    }

    protected void doFrame(Area area) {
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();
        BorderAndPadding bap = area.getBorderAndPadding();

        if (area instanceof BlockArea)
            rx += ((BlockArea)area).getStartIndent();

        h = area.getContentHeight();
        int ry = this.currentYPosition;

        rx = rx - area.getPaddingLeft();
        ry = ry + area.getPaddingTop();
        w = w + area.getPaddingLeft() + area.getPaddingRight();
        h = h + area.getPaddingTop() + area.getPaddingBottom();

        rx = rx - area.getBorderLeftWidth();
        ry = ry + area.getBorderTopWidth();
        w = w + area.getBorderLeftWidth() + area.getBorderRightWidth();
        h = h + area.getBorderTopWidth() + area.getBorderBottomWidth();

        doBackground(area, rx, ry, w, h);

        if (area.getBorderTopWidth() != 0) {
            write("newpath");
            write(rx + " " + ry + " M");
            write(w + " 0 rlineto");
            write(area.getBorderTopWidth() + " setlinewidth");
            write("0 setlinecap");
            useColor(bap.getBorderColor(BorderAndPadding.TOP));
            write("stroke");
        }
        if (area.getBorderLeftWidth() != 0) {
            write("newpath");
            write(rx + " " + ry + " M");
            write("0 " + (-h) + " rlineto");
            write(area.getBorderLeftWidth() + " setlinewidth");
            write("0 setlinecap");
            useColor(bap.getBorderColor(BorderAndPadding.LEFT));
            write("stroke");
        }
        if (area.getBorderRightWidth() != 0) {
            write("newpath");
            write((rx + w) + " " + ry + " M");
            write("0 " + (-h) + " rlineto");
            write(area.getBorderRightWidth() + " setlinewidth");
            write("0 setlinecap");
            useColor(bap.getBorderColor(BorderAndPadding.RIGHT));
            write("stroke");
        }
        if (area.getBorderBottomWidth() != 0) {
            write("newpath");
            write(rx + " " + (ry - h) + " M");
            write(w + " 0 rlineto");
            write(area.getBorderBottomWidth() + " setlinewidth");
            write("0 setlinecap");
            useColor(bap.getBorderColor(BorderAndPadding.BOTTOM));
            write("stroke");
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
      Default start renderer method. This would
      normally be overridden. (mark-fop@inomial.com).
    */
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.debug("rendering areas to PostScript");

        this.pagecount = 0;
        this.out = new PSStream(outputStream);
        write("%!PS-Adobe-3.0");
        if (this.producer == null) {
            this.producer = org.apache.fop.apps.Version.getVersion();
        }
        write("%%Creator: "+this.producer);
        write("%%Pages: (atend)");
        write("%%DocumentProcessColors: Black");
        write("%%DocumentSuppliedResources: procset FOPFonts");
        write("%%EndComments");
        write("%%BeginDefaults");
        write("%%EndDefaults");
        write("%%BeginProlog");
        write("%%EndProlog");
        write("%%BeginSetup");
        writeProcs();
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

        write("FOPprocs begin");
        write("FOPFonts begin");

        write("%%EndSetup");
    }

    /**
      Default stop renderer method. This would
      normally be overridden. (mark-fop@inomial.com).
    */
    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        write("%%Trailer");
        write("%%Pages: "+this.pagecount);
        write("%%EOF");
        this.out.flush();
        log.debug("written out PostScript");
    }

    public void render(Page page, OutputStream outputStream) {
        this.renderPage(page);
    }
}
