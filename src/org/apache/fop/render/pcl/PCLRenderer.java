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
package org.apache.fop.render.pcl;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.VerticalAlign;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.image.ImageArea;
import org.apache.fop.svg.SVGArea;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * Renders areas to PCL.
 *
 * @author Arthur E Welch III (while at M&I EastPoint Technology --
 *     donated by EastPoint to the Apache FOP project March 2, 2001)
 * @author <a href="mailto:mark-fop@inomial.com">Mark Lillywhite</a> (to use
 *     the new Renderer interface)
 */
public class PCLRenderer extends PrintRenderer {

    /**
     * the current stream to add PCL commands to
     */
    public PCLStream currentStream;

    private int pageHeight = 7920;

    // These variables control the virtual paggination functionality.
    public int curdiv = 0;
    private int divisions = -1;
    public int paperheight = -1;    // Paper height in decipoints?
    public int orientation = 0;     // 0=default/portrait, 1=landscape.
    public int topmargin = -1;      // Top margin in decipoints?
    public int leftmargin = -1;     // Left margin in decipoints?
    private int fullmargin = 0;
    private final boolean debug = false;

    private int xoffset =
        -180;                       // X Offset to allow for PCL implicit 1/4" left margin.

    private java.util.Map options;

    /**
     * Create the PCL renderer
     */
    public PCLRenderer() {}

    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }

    /**
     * set the PCL document's producer
     *
     * @param producer string indicating application producing PCL
     */
    public void setProducer(String producer) {}

    /**
     * add a line to the current stream
     *
     * @param x1 the start x location in millipoints
     * @param y1 the start y location in millipoints
     * @param x2 the end x location in millipoints
     * @param y2 the end y location in millipoints
     * @param th the thickness in millipoints
     * @param stroke the line color
     */
    protected void addLine(int x1, int y1, int x2, int y2, int th,
                           PDFPathPaint stroke) {
        if (x1 == x2)
            addRect(x1 - (th / 2), y1, th, y2 - y1 + 1, stroke, stroke);
        else if (y1 == y2)
            addRect(x1, y1 + (th / 2), x2 - x1 + 1, th, stroke, stroke);
    }

    /**
     * add a line to the current stream
     *
     * @param x1 the start x location in millipoints
     * @param y1 the start y location in millipoints
     * @param x2 the end x location in millipoints
     * @param y2 the end y location in millipoints
     * @param th the thickness in millipoints
     * @param rs the rule style
     * @param stroke the line color
     */
    protected void addLine(int x1, int y1, int x2, int y2, int th, int rs,
                           PDFPathPaint stroke) {
        int dashon = 0;
        int dashoff = 0;
        // if ( rs != null && rs.length() > 5 && rs.charAt(0) == '[' && rs.charAt(1) != ']' && rs.charAt(4) == ']' )
        // {
        // dashon = rs.charAt(1) - '0';
        // dashoff = rs.charAt(3) - '0';
        // }
        switch (rs) {
        case org.apache.fop.fo.properties.RuleStyle.DASHED:
            dashon = 3;
            dashoff = 3;
            break;
        case org.apache.fop.fo.properties.RuleStyle.DOTTED:
            dashon = 1;
            dashoff = 3;
            break;
        }
        if (x1 == x2) {
            if (dashon > 0 && dashoff > 0) {
                int start = y1;
                int len = th * dashon;
                while (start < y2) {
                    if (start + len > y2)
                        len = y2 - start;
                    addRect(x1 - (th / 2), start, th, len, stroke, stroke);
                    start += (len + dashoff * th);
                }
            } else
                addRect(x1 - (th / 2), y1, th, y2 - y1 + 1, stroke, stroke);
        } else if (y1 == y2) {
            if (dashon > 0 && dashoff > 0) {
                int start = x1;
                int len = th * dashon;
                while (start < x2) {
                    if (start + len > x2)
                        len = x2 - start;
                    addRect(start, y1 + (th / 2), len, th, stroke, stroke);
                    start += (len + dashoff * th);
                }
            } else
                addRect(x1, y1 + (th / 2), x2 - x1 + 1, th, stroke, stroke);
        }
    }

    /**
     * add a rectangle to the current stream
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param stroke the stroke color/gradient
     */
    protected void addRect(int x, int y, int w, int h, PDFPathPaint stroke) {
        //if (h < 0)
        //    h *= -1;

        if ((h >= 0 && h < 720) || (h < 0 && h > -720) || w < 720) {
            if (w < 720)
                w = 720;
            if (h > 0 && h < 720)
                h = 720;
            else if (h < 0 && h > -720)
                h = -720;
            addRect(x, y, w, h, stroke, stroke);
        } else {
            addRect(x, y, w, 720, stroke, stroke);
            addRect(x, y, 720, h, stroke, stroke);
            addRect(x + w - 720, y, 720, h, stroke, stroke);
            addRect(x, y - h + 720, w, 720, stroke, stroke);
        }
    }

    /**
     * add a filled rectangle to the current stream
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param fill the fill color/gradient
     * @param stroke the stroke color/gradient
     */
    protected void addRect(int x, int y, int w, int h, PDFPathPaint stroke,
                           PDFPathPaint fill) {
        if ((w == 0) || (h == 0))
            return;
        if (h < 0)
            h *= -1;
        else
            y += h;


        PDFColor sc = (PDFColor)stroke;
        PDFColor fc = (PDFColor)fill;

        sc.setColorSpace(ColorSpace.DEVICE_RGB);
        fc.setColorSpace(ColorSpace.DEVICE_RGB);

        int lineshade =
            (int)(100
                  - ((0.3f * sc.red() + 0.59f * sc.green() + 0.11f * sc.blue())
                     * 100f));
        int fillshade =
            (int)(100
                  - ((0.3f * fc.red() + 0.59f * fc.green() + 0.11f * fc.blue())
                     * 100f));

        int xpos = xoffset + (x / 100);
        if (xpos < 0) {
            xpos = 0;
            log.warn("Horizontal position out of bounds.");
        }

        currentStream.add("\033*v1O\033&a" + xpos + "h"
                          + (pageHeight - (y / 100)) + "V" + "\033*c"
                          + (w / 100) + "h" + (h / 100) + "V" + "\033*c"
                          + lineshade + "G" + "\033*c2P");
        if (fillshade != lineshade && (w >= 720 || h >= 720)) {
            xpos = xoffset + ((x + 240) / 100);
            if (xpos < 0) {
                xpos = 0;
                log.warn("Horizontal position out of bounds.");
            }
            currentStream.add("\033&a" + xpos + "h"
                              + (pageHeight - ((y + 240)) / 100) + "V"
                              + "\033*c" + ((w - 480) / 100) + "h"
                              + ((h - 480) / 100) + "V" + "\033*c"
                              + fillshade + "G" + "\033*c2P");
        }
        // Reset pattern transparency mode.
        currentStream.add("\033*v0O");
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
    // XXX: implement this
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
     * @param fill the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected void drawImageClipped(int x, int y,
                    int clipX, int clipY,
                    int clipW, int clipH,
                    FopImage image,
                    FontState fs) {
    // XXX: implement this
    }

    boolean printBMP(FopImage img, int x, int y, int w,
                     int h) throws FopImageException {
        // Print the passed image file in PCL.
        byte imgmap[] = img.getBitmaps();

        int ix = 0;
        int iy = 0;
        int indx = 0;
        int iw = img.getWidth();
        int ih = img.getHeight();
        int bytewidth = (iw / 8);
        if ((iw % 8) != 0)
            bytewidth++;
        byte ib;
        char ic[] = new char[bytewidth * 2];
        char icu[] = new char[bytewidth];
        int lastcount = -1;
        byte lastbyte = 0;
        int icwidth = 0;
        int cr = 0;
        int cg = 0;
        int cb = 0;
        int grey = 0;
        boolean iscolor = img.getColorSpace().getColorSpace()
                          != ColorSpace.DEVICE_GRAY;
        int dcount = 0;
        int xres = (iw * 72000) / w;
        int yres = (ih * 72000) / h;
        int resolution = xres;
        if (yres > xres)
            resolution = yres;

        if (resolution > 300)
            resolution = 600;
        else if (resolution > 150)
            resolution = 300;
        else if (resolution > 100)
            resolution = 150;
        else if (resolution > 75)
            resolution = 100;
        else
            resolution = 75;
        if (debug)
            System.out.println("PCLRenderer.printBMP() iscolor = " + iscolor);
        // Setup for graphics
        currentStream.add("\033*t" + resolution + "R\033*r0F\033*r1A");

        // Transfer graphics data
        for (iy = 0; iy < ih; iy++) {
            ib = 0;
            // int padding = iw % 8;
            // if ( padding != 0 )
            // padding = 8 - padding;
            // padding = 0;
            // indx = (ih - iy - 1 + padding) * iw;
            indx = iy * iw;
            if (iscolor)
                indx *= 3;
            // currentStream.add("\033*b" + bytewidth + "W");
            for (ix = 0; ix < iw; ix++) {
                if (iscolor) {
                    cr = imgmap[indx++] & 0xFF;
                    cg = imgmap[indx++] & 0xFF;
                    cb = imgmap[indx++] & 0xFF;
                    grey = (cr * 30 + cg * 59 + cb * 11) / 100;
                } else
                    grey = imgmap[indx++] & 0xFF;
                if (grey < 128)
                    ib |= (1 << (7 - (ix % 8)));
                if ((ix % 8) == 7 || ((ix + 1) == iw)) {
                    if (icwidth < bytewidth) {
                        if (lastcount >= 0) {
                            if (ib == lastbyte)
                                lastcount++;
                            else {
                                ic[icwidth++] = (char)(lastcount & 0xFF);
                                ic[icwidth++] = (char)lastbyte;
                                lastbyte = ib;
                                lastcount = 0;
                            }
                        } else {
                            lastbyte = ib;
                            lastcount = 0;
                        }
                        if (lastcount == 255 || ((ix + 1) == iw)) {
                            ic[icwidth++] = (char)(lastcount & 0xFF);
                            ic[icwidth++] = (char)lastbyte;
                            lastbyte = 0;
                            lastcount = -1;
                        }
                    }
                    icu[ix / 8] = (char)ib;
                    ib = 0;
                }
            }
            if (icwidth < bytewidth) {
                currentStream.add("\033*b1m" + icwidth + "W");
                currentStream.add(new String(ic, 0, icwidth));
            } else {
                currentStream.add("\033*b0m" + bytewidth + "W");
                currentStream.add(new String(icu));
            }
            lastcount = -1;
            icwidth = 0;
        }

        // End graphics
        currentStream.add("\033*rB");


        return (true);
    }

    /**
     * render image area to PCL
     *
     * @param area the image area to render
     */
    public void renderImageArea(ImageArea area) {
        int x = this.currentAreaContainerXPosition + area.getXOffset();
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= h;

        FopImage img = area.getImage();

        int xpos = xoffset + (x / 100);
        if (xpos < 0) {
            xpos = 0;
            log.warn("Horizontal position out of bounds.");
        }

        currentStream.add("\033&a" + xpos + "h" + (pageHeight - (y / 100))
                          + "V");

        try {
            printBMP(img, x, y, w, h);
        } catch (FopImageException e) {
            // e.printStackTrace(System.out);
            log.error("PCLRenderer.renderImageArea() Error printing BMP ("
                                   + e.toString() + ")");
        }
    }

    /**
     * render a foreign object area
     */
    public void renderForeignObjectArea(ForeignObjectArea area) {
        // if necessary need to scale and align the content
        this.currentXPosition = this.currentXPosition + area.getXOffset();
        this.currentYPosition = this.currentYPosition;
        switch (area.getAlign()) {
        case TextAlign.START:
            break;
        case TextAlign.END:
            break;
        case TextAlign.CENTER:
        case TextAlign.JUSTIFY:
            break;
        }
        switch (area.getVerticalAlign()) {
        case VerticalAlign.BASELINE:
            break;
        case VerticalAlign.MIDDLE:
            break;
        case VerticalAlign.SUB:
            break;
        case VerticalAlign.SUPER:
            break;
        case VerticalAlign.TEXT_TOP:
            break;
        case VerticalAlign.TEXT_BOTTOM:
            break;
        case VerticalAlign.TOP:
            break;
        case VerticalAlign.BOTTOM:
            break;
        }
        // in general the content will not be text

        // align and scale

        switch (area.scalingMethod()) {
        case Scaling.UNIFORM:
            break;
        case Scaling.NON_UNIFORM:
            break;
        }
        // if the overflow is auto (default), scroll or visible
        // then the contents should not be clipped, since this
        // is considered a printing medium.
        switch (area.getOverflow()) {
        case Overflow.VISIBLE:
        case Overflow.SCROLL:
        case Overflow.AUTO:
            break;
        case Overflow.HIDDEN:
            break;
        }
        area.getObject().render(this);

        this.currentXPosition += area.getEffectiveWidth();
        // this.currentYPosition -= area.getEffectiveHeight();
    }

    /**
     * render SVG area to PCL
     *
     * @param area the SVG area to render
     */
    public void renderSVGArea(SVGArea area) {
        if (debug)
            System.out.println("PCLRenderer.renderSVGArea(" + area + ")");
        int x = this.currentXPosition;
        int y = this.currentYPosition;
        SVGSVGElement svg =
            ((SVGDocument)area.getSVGDocument()).getRootElement();
        int w = (int)(svg.getWidth().getBaseVal().getValue() * 1000);
        int h = (int)(svg.getHeight().getBaseVal().getValue() * 1000);

        /*
         * Clip to the svg area.
         * Note: To have the svg overlay (under) a text area then use
         * an fo:block-container
         */

        // TODO - translate and clip to viewbox

        // currentStream.add(svgRenderer.getString());

        // currentStream.add("Q\n");
    }


    public void setFont(String name, float size) {
        int fontcode = 0;
        if (name.length() > 1 && name.charAt(0) == 'F') {
            try {
                fontcode = Integer.parseInt(name.substring(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        switch (fontcode) {
        case 1:     // F1 = Helvetica
            // currentStream.add("\033(8U\033(s1p" + (size / 1000) + "v0s0b24580T");
            // Arial is more common among PCL5 printers than Helvetica - so use Arial

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s0b16602T");
            break;
        case 2:     // F2 = Helvetica Oblique

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s0b16602T");
            break;
        case 3:     // F3 = Helvetica Bold

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s3b16602T");
            break;
        case 4:     // F4 = Helvetica Bold Oblique

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s3b16602T");
            break;
        case 5:     // F5 = Times Roman
            // currentStream.add("\033(8U\033(s1p" + (size / 1000) + "v0s0b25093T");
            // Times New is more common among PCL5 printers than Times - so use Times New

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s0b16901T");
            break;
        case 6:     // F6 = Times Italic

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s0b16901T");
            break;
        case 7:     // F7 = Times Bold

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s3b16901T");
            break;
        case 8:     // F8 = Times Bold Italic

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s3b16901T");
            break;
        case 9:     // F9 = Courier

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h0s0b4099T");
            break;
        case 10:    // F10 = Courier Oblique

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h1s0b4099T");
            break;
        case 11:    // F11 = Courier Bold

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h0s3b4099T");
            break;
        case 12:    // F12 = Courier Bold Oblique

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h1s3b4099T");
            break;
        case 13:    // F13 = Symbol

            currentStream.add("\033(19M\033(s1p" + (size / 1000)
                              + "v0s0b16686T");
            // currentStream.add("\033(9U\033(s1p" + (size / 1000) + "v0s0b25093T"); // ECMA Latin 1 Symbol Set in Times Roman???
            break;
        case 14:    // F14 = Zapf Dingbats

            currentStream.add("\033(14L\033(s1p" + (size / 1000)
                              + "v0s0b45101T");
            break;
        default:
            currentStream.add("\033(0N\033(s" + (size / 1000) + "V");
            break;
        }
    }

    /**
     * render inline area to PCL
     *
     * @param area inline area to render
     */
    public void renderWordArea(WordArea area) {
        String name = area.getFontState().getFontName();
        int size = area.getFontState().getFontSize();

        float red = area.getRed();
        float green = area.getGreen();
        float blue = area.getBlue();
        PDFColor theAreaColor = new PDFColor((double)area.getRed(),
                                             (double)area.getGreen(),
                                             (double)area.getBlue());

        // currentStream.add("\033*c" + (int)(100 - ((0.3f * red + 0.59f * green + 0.11f * blue) * 100f)) + "G\033*v2T");
        currentStream.add("\033*v1O\033*c"
                          + (int)(100 - ((0.3f * red + 0.59f * green + 0.11f * blue) * 100f))
                          + "G\033*v2T");

        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            this.currentFontName = name;
            this.currentFontSize = size;
            setFont(name, size);
        }

        this.currentFill = theAreaColor;

        int rx = this.currentXPosition;
        int bl = this.currentYPosition;

        String s = area.getText();

        addWordLines(area, rx, bl, size, theAreaColor);

        int xpos = xoffset + (rx / 100);
        if (xpos < 0) {
            xpos = 0;
            log.warn("Horizontal position out of bounds.");
        }
        currentStream.add("\033&a" + xpos + "h" + (pageHeight - (bl / 100))
                          + "V" + s);

        this.currentXPosition += area.getContentWidth();
    }

    /**
     * render page into PCL
     *
     * @param page page to render
     */
    public void renderPage(Page page) {
        if (debug)
            System.out.println("PCLRenderer.renderPage() page.Height() = "
                               + page.getHeight());

        // FIXME: Set orientation. We made this assumption. It's not always correct but we need a
        // method to generate landscape pages
        if (page.getHeight() < page.getWidth()) {
             orientation = 1;
        }
        currentStream.add("\033&l" + orientation + "O");
        if (orientation == 1 || orientation == 3) {
            xoffset = -144;
        } else {
            xoffset = -180;
        }

        if (paperheight > 0 && divisions == -1)
            divisions = paperheight / (page.getHeight() / 100);

        if (debug)
            System.out.println("PCLRenderer.renderPage() paperheight="
                               + paperheight + " divisions=" + divisions);

        // Set top margin.
        // float fullmargin = 0;
        if (divisions > 0)
            fullmargin = paperheight * curdiv / divisions;
        if (topmargin > 0)
            fullmargin += topmargin;
        if (debug)
            System.out.println("PCLRenderer.renderPage() curdiv=" + curdiv
                               + " fullmargin=" + fullmargin);
        // if ( fullmargin > 0 )
        // currentStream.add("\033&l" + (fullmargin / 15f) + "c1e8C");
        // this.currentYPosition = fullmargin * 100;

        if (paperheight > 0)
            pageHeight = (paperheight / divisions) + fullmargin;
        else
            pageHeight = page.getHeight() / 100;
        if (debug)
            System.out.println("PCLRenderer.renderPage() Set currentYPosition="
                               + this.currentYPosition);
        if (leftmargin > 0 && curdiv == 0)
            currentStream.add("\033&k" + (leftmargin / 6f)
                              + "H\033&a1L\033&k12H");

        this.currentFontName = "";
        this.currentFontSize = 0;

        renderRegions(page);

        // End page.
        if (++curdiv == divisions || divisions == -1) {
            curdiv = 0;
            currentStream.add("\f");
        }

        // Links, etc not implemented...
        /*
         * currentPage = this.pdfDoc.makePage(this.pdfResources, currentStream,
         * page.getWidth()/1000, page.getHeight()/1000, page);
         * if (page.hasLinks()) {
         * currentAnnotList = this.pdfDoc.makeAnnotList();
         * currentPage.setAnnotList(currentAnnotList);
         * Enumeration e = page.getLinkSets().elements();
         * while (e.hasMoreElements()) {
         * LinkSet linkSet = (LinkSet) e.nextElement();
         * linkSet.align();
         * String dest = linkSet.getDest();
         * int linkType = linkSet.getLinkType();
         * Enumeration f = linkSet.getRects().elements();
         * while (f.hasMoreElements()) {
         * LinkedRectangle lrect = (LinkedRectangle) f.nextElement();
         * currentAnnotList.addLink(
         * this.pdfDoc.makeLink(lrect.getRectangle(), dest, linkType));
         * }
         * }
         * } else {
         * // just to be on the safe side
         * currentAnnotList = null;
         * }
         */
    }
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.info("rendering areas to PCL");
        currentStream = new PCLStream(outputStream);

        // Reset the margins.
        currentStream.add("\033" + "9\033&l0E");
    }

    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        log.info("writing out PCL");
        outputStream.flush();
    }

    public void render(Page page, OutputStream outputStream)
    throws IOException {
        idReferences = page.getIDReferences();
        this.renderPage(page);
    }

}
