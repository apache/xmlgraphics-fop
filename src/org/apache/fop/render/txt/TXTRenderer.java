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
package org.apache.fop.render.txt;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.VerticalAlign;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.layout.inline.InlineSpace;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImageFactory;
import org.apache.fop.image.SVGImage;

import org.apache.fop.svg.SVGArea;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Renderer that renders areas to plain text.
 *
 * @author Art Welch
 * @author <a href="mailto:mark-fop@inomial.com">Mark Lillywhite</a> (to use
 *     the new Renderer interface)
 */
public class TXTRenderer extends PrintRenderer {

    /**
     * the current stream to add Text commands to
     */
    TXTStream currentStream;
    public static final String encodingOptionName = "txt.encoding";
    private static final String DEFAULT_ENCODING = "UTF-8";

    private int pageHeight = 7920;

    // These variables control the virtual paggination functionality.
    public int curdiv = 0;
    private int divisions = -1;
    private int paperheight = -1;    // Paper height in decipoints?
    public int orientation =
        -1;                          // -1=default/unknown, 0=portrait, 1=landscape.
    public int topmargin = -1;       // Top margin in decipoints?
    public int leftmargin = -1;      // Left margin in decipoints?
    private int fullmargin = 0;
    final boolean debug = false;

    // Variables for rendering text.
    StringBuffer charData[];
    StringBuffer decoData[];
    public float textCPI = 16.67f;
    public float textLPI = 8;
    int maxX = (int)(8.5f * textCPI + 1);
    int maxY = (int)(11f * textLPI + 1);
    float xFactor;
    float yFactor;
    public String lineEnding =
        "\r\n";    // Every line except the last line on a page (which will end with pageEnding) will be terminated with this string.
    public String pageEnding =
        "\f";                        // Every page except the last one will end with this string.
    public boolean suppressGraphics =
        false;    // If true then graphics/decorations will not be rendered - text only.
    boolean firstPage = false;
    /**
     * options
     */
    protected java.util.Map options;

    public TXTRenderer() {}

    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }

    /**
     * set the TXT document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String producer) {}


    void addStr(int row, int col, String str, boolean ischar) {
        if (debug)
            System.out.println("TXTRenderer.addStr(" + row + ", " + col
                               + ", \"" + str + "\", " + ischar + ")");
        if (suppressGraphics &&!ischar)
            return;
        StringBuffer sb;
        if (row < 0)
            row = 0;
        if (ischar)
            sb = charData[row];
        else
            sb = decoData[row];
        if (sb == null)
            sb = new StringBuffer();
        if ((col + str.length()) > maxX)
            col = maxX - str.length();
        if (col < 0) {
            col = 0;
            if (str.length() > maxX)
                str = str.substring(0, maxX);
        }
        // Pad to col
        for (int countr = sb.length(); countr < col; countr++)
            sb.append(' ');
        if (debug)
            System.out.println("TXTRenderer.addStr() sb.length()="
                               + sb.length());
        for (int countr = col; countr < (col + str.length()); countr++) {
            if (countr >= sb.length())
                sb.append(str.charAt(countr - col));
            else {
                if (debug)
                    System.out.println("TXTRenderer.addStr() sb.length()="
                                       + sb.length() + " countr=" + countr);
                sb.setCharAt(countr, str.charAt(countr - col));
            }
        }

        if (ischar)
            charData[row] = sb;
        else
            decoData[row] = sb;
    }

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
        if (x1 == x2) {
            addRect(x1, y1, th, y2 - y1 + 1, stroke, stroke);
        } else if (y1 == y2) {
            addRect(x1, y1, x2 - x1 + 1, th, stroke, stroke);
        }
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
        PDFColor lstroke = null;
        if (rs == org.apache.fop.fo.properties.RuleStyle.DOTTED)
            lstroke = new PDFColor(0.7f, 0.7f, 0.7f);
        else
            lstroke = (PDFColor)stroke;
        if (x1 == x2) {
            addRect(x1, y1, th, y2 - y1 + 1, lstroke, lstroke);
        } else if (y1 == y2) {
            addRect(x1, y1, x2 - x1 + 1, th, lstroke, lstroke);
        }
    }

    protected void addLine(float x1, float y1, float x2, float y2,
                           PDFColor sc, float sw) {
        /*
         * SVG - Not yet implemented
         * if ( debug )
         * System.out.println("TXTRenderer.addLine(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + sc + ", " + sw + ")");
         * if ( x1 == x2 )
         * {
         * addRect(x1 - sw/2, y1, sw, y2 - y1 + 1, 0, 0, sc, null, 0);
         * }
         * else if ( y1 == y2 || (Math.abs(y1 - y2) <= 0.24) ) // 72/300=0.24
         * {
         * addRect(x1, y1 - sw/2, x2 - x1 + 1, sw, 0, 0, sc, null, 0);
         * }
         * else if ( sc != null )
         * {
         * // Convert dimensions to characters.
         * //float cfact = 300f / 72f; // 300 dpi, 1pt=1/72in
         * int ix1 = (int)(x1 * xFactor);
         * int iy1 = (int)(y1 * yFactor);
         * int ix2 = (int)(x2 * xFactor);
         * int iy2 = (int)(y2 * yFactor);
         * int isw = (int)(sw * xFactor);
         * int origix;
         * // Normalize
         * if ( iy1 > iy2 )
         * {
         * int tmp = ix1;
         * ix1 = ix2;
         * ix2 = tmp;
         * tmp = iy1;
         * iy1 = iy2;
         * iy2 = tmp;
         * }
         * if ( ix1 > ix2 )
         * {
         * origix = ix2;
         * ix1 -=ix2;
         * ix2 = 0;
         * }
         * else
         * {
         * origix = ix1;
         * ix2 -= ix1;
         * ix1 = 0;
         * }
         * // Convert line width to a pixel run length.
         * //System.out.println("TXTRenderer.addLine(" + ix1 + ", " + iy1 + ", " + ix2 + ", " + iy2 + ", " + isw + ")");
         * int runlen = (int)Math.sqrt(Math.pow(isw, 2) * (1 + Math.pow((ix1 - ix2) / (iy1 - iy2), 2)));
         * if ( runlen < 1 )
         * runlen = 1;
         * StringBuffer rlbuff = new StringBuffer();
         * for ( int countr = 0 ; countr < runlen ; countr++ )
         * rlbuff.append('*');
         * String rlstr = rlbuff.toString();
         * //System.out.println("TXTRenderer.addLine: runlen = " + runlen);
         * // Draw the line.
         * int d, dx, dy;
         * int Aincr, Bincr;
         * int xincr = 1;
         * int x, y;
         * dx = Math.abs(ix2 - ix1);
         * dy = iy2 - iy1;
         * if ( dx > dy )
         * {
         * xincr = dx / dy;
         * // Move to starting position.
         * //currentStream.add("\033*p" + origix + "x" + iy1 + "Y");
         * x = ix1 - runlen / 2;
         * iy2 += (isw / 2);
         * // Start raster graphics
         * //currentStream.add("\033*t300R\033*r" + dx + "s1A\033*b1M");
         * }
         * else
         * {
         * // Move to starting position.
         * //currentStream.add("\033*p" + (origix - runlen / 2) + "x" + iy1 + "Y");
         * x = ix1;
         * // Start raster graphics
         * //currentStream.add("\033*t300R\033*r1A\033*b1M");
         * }
         * if ( ix1 > ix2 )
         * xincr *= -1;
         * d = 2 * dx - dy;
         * Aincr = 2 * (dx - dy);
         * Bincr = 2 * dx;
         * y = iy1;
         * xferLineBytes(x, runlen, null, -1);
         *
         * for ( y = iy1 + 1 ; y <= iy2 ; y++ )
         * {
         * if ( d >= 0 )
         * {
         * x += xincr;
         * d += Aincr;
         * }
         * else
         * d += Bincr;
         * xferLineBytes(x, runlen, null, -1);
         * }
         * // End raster graphics
         * //currentStream.add("\033*rB");
         * // Return to regular print mode.
         * //currentStream.add("\033*v0t0n0O");
         * }
         */
    }

    private void xferLineBytes(int startpos, int bitcount, List save,
                               int start2) {
        /*
         * Not yet implemented
         * //System.out.println("TXTRenderer.xferLineBytes(" + startpos + ", " + bitcount + ")");
         * int curbitpos = 0;
         * if ( start2 > 0 && start2 <= (startpos + bitcount) )
         * {
         * bitcount += (start2 - startpos);
         * start2 = 0;
         * }
         * char bytes[] = new char[((start2>startpos?start2:startpos) + bitcount) / 4 + 2];
         * int dlen = 0;
         * byte dbyte = 0;
         * int bytepos = 0;
         * do
         * {
         * int bits2set;
         * if ( startpos < 0 )
         * {
         * bits2set = bitcount + startpos;
         * startpos = 0;
         * }
         * else
         * bits2set = bitcount;
         * byte bittype = 0;
         * do
         * {
         * if ( bytepos > 0 )
         * {
         * int inc = startpos - curbitpos;
         * if ( (inc) >=  (8 - bytepos) )
         * {
         * curbitpos += (8 - bytepos);
         * bytepos = 0;
         * bytes[dlen++] = (char)0;
         * bytes[dlen++] = (char)dbyte;
         * dbyte = 0;
         * }
         * else
         * {
         * bytepos += inc;
         * dbyte = (byte)(dbyte ^ (byte)(Math.pow(2, 8 - bytepos) - 1));
         * curbitpos += inc;
         * }
         * }
         * // Set runs of whole bytes.
         * int setbytes = (startpos - curbitpos) / 8;
         * if ( setbytes > 0 )
         * {
         * curbitpos += setbytes * 8;
         * while ( setbytes > 0 )
         * {
         * if ( setbytes > 256 )
         * {
         * bytes[dlen++] = 0xFF;
         * setbytes -= 256;
         * }
         * else
         * {
         * bytes[dlen++] = (char)((setbytes - 1) & 0xFF);
         * setbytes = 0;
         * }
         * bytes[dlen++] = (char)bittype;
         * }
         * }
         * // move to position in the first byte.
         * if ( curbitpos < startpos )
         * {
         * if ( bytepos == 0 )
         * dbyte = bittype;
         * bytepos += startpos - curbitpos;
         * dbyte = (byte)(dbyte ^ (byte)(Math.pow(2, 8 - bytepos) - 1));
         * curbitpos += bytepos;
         * startpos += bits2set;
         * }
         * else
         * {
         * startpos += bits2set;
         * }
         * if ( bittype == 0 )
         * bittype = (byte)0xFF;
         * else
         * bittype = 7;
         * } while ( bittype != 7 );
         * if ( start2 > 0 )
         * {
         * startpos = start2;
         * start2 = -1;
         * }
         * else
         * startpos = -1;
         * } while ( startpos >= 0 );
         * if ( bytepos > 0 )
         * {
         * bytes[dlen++] = (char)0;
         * bytes[dlen++] = (char)dbyte;
         * }
         * if ( save == null )
         * {
         * //currentStream.add("\033*b" + dlen + "W");
         * //currentStream.add(new String(bytes, 0, dlen));
         * }
         * else
         * {
         * String line = "\033*b" + dlen + "W" + new String(bytes, 0, dlen);
         * //currentStream.add(line);
         * save.add(line);
         * }
         */
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
        if (h < 0)
            h *= -1;

        if (h < 720 || w < 720) {
            if (w < 720)
                w = 720;
            if (h < 720)
                h = 720;
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
        // System.out.println("TXTRenderer.addRect(" + x + ", " + y + ", " + w + ", " + h + ", " + r + ", " + g + ", " + b + ", " + fr + ", " + fg + ", " + fb + ")");
        if ((w == 0) || (h == 0))
            return;
        if (h < 0)
            h *= -1;

        int row = (int)((pageHeight - (y / 100)) * 100 * yFactor);
        int col = (int)(x * xFactor);

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
        if (debug)
            System.out.println("TXTRenderer.addRect(" + x + ", " + y + ", "
                               + w + ", " + h + ", " + stroke + ", " + fill
                               + ") fillshade=" + fillshade);
        char fillchar = ' ';
        if (fillshade >= 75)
            fillchar = '#';
        else if (fillshade >= 50)
            fillchar = '*';
        else if (fillshade >= 25)
            fillchar = ':';


        if (fillchar != ' ') {
            StringBuffer linefill = new StringBuffer();
            int sw = (int)(w * xFactor);
            int sh = (int)(h * yFactor);
            if (sw == 0 || sh == 0) {
                if (fillshade >= 50) {
                    if (h > w)
                        fillchar = '|';
                    else
                        fillchar = '-';
                } else {
                    if (h > w)
                        fillchar = ':';
                    else
                        fillchar = '.';
                }
            }
            if (sw == 0)
                linefill.append(fillchar);
            else
                for (int countr = 0; countr < sw; countr++)
                    linefill.append(fillchar);
            if (sh == 0)
                addStr(row, col, linefill.toString(), false);
            else
                for (int countr = 0; countr < sh; countr++)
                    addStr(row + countr, col, linefill.toString(), false);
        }

        if (lineshade >= 25) {
            char vlinechar = '|';
            char hlinechar = '-';
            if (lineshade < 50) {
                vlinechar = ':';
                hlinechar = '.';
            }
            StringBuffer linefill = new StringBuffer();
            int sw = (int)(w * xFactor);
            for (int countr = 0; countr < sw; countr++)
                linefill.append(hlinechar);
            int sh = (int)(h * yFactor);

            if (w > h) {
                for (int countr = 1; countr < (sh - 1); countr++) {
                    addStr(row + countr, col, String.valueOf(vlinechar),
                           false);
                    addStr(row + countr, col + sw, String.valueOf(vlinechar),
                           false);
                }
                addStr(row, col, linefill.toString(), false);
                addStr(row + sh, col, linefill.toString(), false);

            } else {
                addStr(row, col, linefill.toString(), false);
                addStr(row + sh, col, linefill.toString(), false);
                for (int countr = 1; countr < (sh - 1); countr++) {
                    addStr(row + countr, col, String.valueOf(vlinechar),
                           false);
                    addStr(row + countr, col + sw, String.valueOf(vlinechar),
                           false);
                }

            }
        }
    }


    /**
     * add a filled rectangle to the current stream
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component of edges
     * @param g the green component of edges
     * @param b the blue component of edges
     * @param fr the red component of the fill
     * @param fg the green component of the fill
     * @param fb the blue component of the fill
     */
    protected void addRect(float x, float y, float w, float h, float rx,
                           float ry, PDFColor fc, PDFColor sc, float sw) {
        /*
         * SVG - Not yet implemented
         * if ( debug )
         * System.out.println("TXTRenderer.addRect(" + x + ", " + y + ", " + w + ", " + h + ", " + rx + ", " + ry + ", " + fc + ", " + sc + ", " + sw + ")");
         * float sr = 1;
         * float sg = 1;
         * float sb = 1;
         * float fr = 1;
         * float fg = 1;
         * float fb = 1;
         * if ( sc != null && sw > 0 )
         * {
         * sr = (float)sc.red();
         * sg = (float)sc.green();
         * sb = (float)sc.blue();
         * }
         * if ( fc != null )
         * {
         * fr = (float)fc.red();
         * fg = (float)fc.green();
         * fb = (float)fc.blue();
         * }
         * addRect((int)(x * 1000), (int)(pageHeight * 100 - y * 1000), (int)(w * 1000), (int)(h * 1000), sr, sg, sb, fr, fg, fb);
         * fc = null;
         * sc = null;
         * if ( rx == 0 || ry == 0 )
         * {
         * if ( fc != null )
         * {
         * int fillshade = (int)(100 - ((0.3f * fc.red() + 0.59f * fc.green() + 0.11f * fc.blue()) * 100f));
         * currentStream.add("\033*v0n1O\033&a" + (x * 10) + "h" + ((y * 10)) + "V"
         * + "\033*c" + (w * 10) + "h" + (h * 10) + "v" + fillshade + "g2P\033*v0n0O");
         * }
         * if ( sc != null && sw > 0 )
         * {
         * String lend = "v" + String.valueOf((int)(100 - ((0.3f * sc.red() + 0.59f * sc.green() + 0.11f * sc.blue()) * 100f))) + "g2P";
         * currentStream.add("\033*v0n1O");
         * currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
         * + "\033*c" + ((w + sw) * 10) + "h" + ((sw) * 10) + lend);
         * currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
         * + "\033*c" + ((sw) * 10) + "h" + ((h + sw) * 10) + lend);
         * currentStream.add("\033&a" + ((x + w - sw/2) * 10) + "h" + (((y - sw/2)) * 10) + "V"
         * + "\033*c" + ((sw) * 10) + "h" + ((h + sw) * 10) + lend);
         * currentStream.add("\033&a" + ((x - sw/2) * 10) + "h" + (((y + h - sw/2)) * 10) + "V"
         * + "\033*c" + ((w + sw) * 10) + "h" + ((sw) * 10) + lend);
         * currentStream.add("\033*v0n0O");
         * }
         * }
         * else
         * {
         * // Convert dimensions to pixels.
         * float cfact = 300f / 72f; // 300 dpi, 1pt=1/72in
         * int ix = (int)(x * cfact);
         * int iy = (int)(y * cfact);
         * int iw = (int)(w * cfact);
         * int ih = (int)(h * cfact);
         * int irx = (int)(rx * cfact);
         * int iry = (int)(ry * cfact);
         * int isw = (int)(sw * cfact);
         * int longwidth = 0;
         * int pass = 0;
         * PDFColor thecolor = null;
         * do
         * {
         * if ( pass == 0 && fc != null )
         * {
         * thecolor = fc;
         * }
         * else if ( pass == 1 && sc != null )
         * {
         * int iswdiv2 = isw / 2;
         * thecolor = sc;
         * ix -= iswdiv2;
         * iy -= iswdiv2;
         * irx += iswdiv2;
         * iry += iswdiv2;
         * iw += isw;
         * ih += isw;
         * longwidth = (int)(isw * 1.414);
         * }
         * else
         * thecolor = null;
         * if ( thecolor != null )
         * {
         * int tx = 0;
         * int ty = iry;
         * long a = irx;
         * long b = iry;
         * long Asquared = (long)Math.pow(a, 2);
         * long TwoAsquared = 2 * Asquared;
         * long Bsquared = (long)Math.pow(b, 2);
         * long TwoBsquared = 2 * Bsquared;
         * long d = Bsquared - Asquared * b + Asquared / 4;
         * long dx = 0;
         * long dy = TwoAsquared * b;
         * int rectlen = iw - 2 * irx;
         * List bottomlines = new java.util.ArrayList();
         * int x0 = tx;
         * // Set Transparency modes and select shading.
         * currentStream.add("\033*v0n1O\033*c" + (int)(100 - ((0.3f * thecolor.red() + 0.59f * thecolor.green() + 0.11f * thecolor.blue()) * 100f)) + "G\033*v2T");
         * // Move to starting position.
         * currentStream.add("\033*p" + ix + "x" + iy + "Y");
         * // Start raster graphics
         * currentStream.add("\033*t300R\033*r" + iw + "s1A\033*b1M");
         * while ( dx < dy )
         * {
         * if ( d > 0 )
         * {
         * if ( pass == 0 || ty > (iry - isw) )
         * xferLineBytes(irx - x0, rectlen + 2 * x0, bottomlines, -1);
         * else
         * xferLineBytes(irx - x0, longwidth, bottomlines, iw - irx + x0 - longwidth);
         * x0 = tx + 1;
         * ty--;
         * dy -= TwoAsquared;
         * d -= dy;
         * }
         * tx++;
         * dx += TwoBsquared;
         * d += Bsquared + dx;
         * }
         * d += (3 * (Asquared - Bsquared) / 2 - (dx + dy)) / 2;
         * while ( ty > 0 )
         * {
         * if ( pass == 0 || ty >= (iry - isw) )
         * xferLineBytes(irx - tx, rectlen + 2 * tx, bottomlines, -1);
         * else
         * xferLineBytes(irx - tx, isw, bottomlines, iw - irx + tx - isw);
         *
         * if ( d < 0 )
         * {
         * tx++;
         * dx += TwoBsquared;
         * d += dx;
         * }
         * ty--;
         * dy -= TwoAsquared;
         * d += Asquared - dy;
         * }
         * // Draw the middle part of the rectangle
         * int midlen = ih - 2 * iry;
         * if ( midlen > 0 )
         * {
         * if ( pass == 0 )
         * xferLineBytes(0, iw, null, -1);
         * else
         * xferLineBytes(0, isw, null, iw - isw);
         * currentStream.add("\033*b3M");
         * for ( int countr = midlen - 1 ; countr > 0 ; countr-- )
         * currentStream.add("\033*b0W");
         * currentStream.add("\033*b1M");
         * }
         * // Draw the bottom.
         * for ( int countr = bottomlines.size() - 1 ; countr >= 0 ; countr-- )
         * currentStream.add((String)bottomlines.get(countr));
         * // End raster graphics
         * currentStream.add("\033*rB");
         * // Return to regular print mode.
         * currentStream.add("\033*v0t0n0O");
         * }
         * pass++;
         * } while ( pass < 2 );
         * }
         */
    }

    // Add a polyline or polygon. Does not support fills yet!!!
    protected void addPolyline(List points, int posx, int posy,
                               PDFColor fc, PDFColor sc, float sw,
                               boolean close) {}

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
        if (debug)
            System.out.println("TXTRenderer.printBMP(" + img + ", " + x
                               + ", " + y + ", " + w + ", " + h + ")");
        addRect(x, y, w, h, new PDFColor(1f, 1f, 1f),
                new PDFColor(0f, 0f, 0f));
        int nameh = (int)(h * yFactor / 2);
        if (nameh > 0) {
            int namew = (int)(w * xFactor);

            if (namew > 4) {
                String iname = img.getURL();
                if (iname.length() >= namew)
                    addStr((int)((pageHeight - (y / 100)) * 100 * yFactor)
                           + nameh, (int)(x * xFactor),
                           iname.substring(iname.length() - namew),
                           true);
                else
                    addStr((int)((pageHeight - (y / 100)) * 100 * yFactor)
                           + nameh, (int)(x * xFactor
                                          + (namew - iname.length())
                                          / 2), iname, true);

            }
        }
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

        try {
            printBMP(img, x, y, w, h);
        } catch (FopImageException e) {
            // e.printStackTrace(System.out);
            log.error("TXTRenderer.renderImageArea() printing BMP ("
                                   + e.toString() + ").", e);
        }
    }

    public void renderImage(FontState fontState, String href, float x,
                            float y, float width, float height) {
        try {
            if (href.indexOf(":") == -1)
                href = "file:" + href;
            FopImage img = FopImageFactory.Make(href);
            if (img != null) {
                if (img instanceof SVGImage) {
                    SVGSVGElement svg =
                        ((SVGImage)img).getSVGDocument().getRootElement();
                    renderSVG(fontState, svg, (int)x * 1000, (int)y * 1000);
                } else {
                    printBMP(img, (int)x, (int)y, (int)width, (int)height);
                }
            }
        } catch (Exception e) {
            log.error("could not add image to SVG: " + href, e);
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


    void renderSVG(FontState fontState, SVGSVGElement svg, int x, int y) {
        /*
         * SVG - Not yet implemented
         * NodeList nl = svg.getChildNodes();
         * for(int count = 0; count < nl.getLength(); count++) {
         * Node n = nl.item(count);
         * if(n instanceof SVGElement) {
         * renderElement(fontState, (SVGElement)n, x, y);
         * }
         * }
         */
    }

    /**
     * render SVG area to Text
     *
     * @param area the SVG area to render
     */
    public void renderSVGArea(SVGArea area) {
        if (debug)
            System.out.println("TXTRenderer.renderSVGArea(" + area + ")");
        int x = this.currentAreaContainerXPosition;
        int y = this.currentYPosition;
        SVGSVGElement svg =
            ((SVGDocument)area.getSVGDocument()).getRootElement();
        int w = (int)(svg.getWidth().getBaseVal().getValue() * 1000);
        int h = (int)(svg.getHeight().getBaseVal().getValue() * 1000);

        // currentStream.add("ET\n");
        /*
         * Clip to the svg area.
         * Note: To have the svg overlay (under) a text area then use
         * an fo:block-container
         */
        // currentStream.add("q\n");
        // currentStream.add(x / 1000f + " " + y / 1000f + " m\n");
        // currentStream.add((x + w) / 1000f + " " + y / 1000f + " l\n");
        // currentStream.add((x + w) / 1000f + " " + (y - h) / 1000f + " l\n");
        // currentStream.add(x / 1000f + " " + (y - h) / 1000f + " l\n");
        // currentStream.add("h\n");
        // currentStream.add("W\n");
        // currentStream.add("n\n");
        // transform so that the coordinates (0,0) is from the top left
        // and positive is down and to the right
        // currentStream.add(1 + " " + 0 + " " + 0 + " " + (-1) + " " + x / 1000f + " " + y / 1000f + " cm\n");

        // TODO - translate and clip to viewbox

        renderSVG(area.getFontState(), svg, x, y);

        // Enumeration e = area.getChildren().elements();
        // while (e.hasMoreElements()) {
        // Object o = e.nextElement();
        // if(o instanceof GraphicImpl) {
        // renderElement(area, (GraphicImpl)o, x, y);
        // }
        // }

        // currentStream.add("Q\n");
        // currentStream.add("BT\n");
        // this.currentYPosition -= h;
    }

    /*
     * SVG - Not yet implemented
     * public void renderElement(FontState fontState, SVGElement area, int posx, int posy)
     * {
     * if ( debug )
     * System.out.println("TXTRenderer.renderElement(" + fontState + ", " + area + ", " + posx + ", " + posy + ")");
     * int x = posx;
     * int y = posy;
     * CSSStyleDeclaration style = null;
     * if ( area instanceof SVGStylable )
     * style = ((SVGStylable)area).getStyle();
     * PDFColor fillColour = null;
     * PDFColor strokeColour = null;
     * float strokeWidth = 0;
     * //currentStream.add("q\n");
     * //if( area instanceof SVGTransformable ) {
     * //   SVGTransformable tf = (SVGTransformable)area;
     * //   SVGAnimatedTransformList trans = tf.getTransform();
     * //   SVGRect bbox = tf.getBBox();
     * //   if(trans != null) {
     * //       applyTransform(trans, bbox);
     * //   }
     * //}
     * if(style != null)
     * {
     * CSSValue sp = style.getPropertyCSSValue("fill");
     * if(sp != null)
     * {
     * if( sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * if( ((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR )
     * {
     * RGBColor col = ((CSSPrimitiveValue)sp).getRGBColorValue();
     * CSSPrimitiveValue val;
     * val = col.getRed();
     * float red = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * val = col.getGreen();
     * float green = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * val = col.getBlue();
     * float blue = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * fillColour = new PDFColor(red, green, blue);
     * }
     * }
     * //if(sp instanceof ColorType)
     * //{
     * //   ColorType ct = (ColorType)sp;
     * //   fillColour = new PDFColor(ct.red(), ct.green(), ct.blue());
     * //}
     * }
     * else
     * fillColour = new PDFColor(0, 0, 0);
     * sp = style.getPropertyCSSValue("stroke");
     * if(sp != null)
     * {
     * if( sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * if( ((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_RGBCOLOR )
     * {
     * RGBColor col = ((CSSPrimitiveValue)sp).getRGBColorValue();
     * CSSPrimitiveValue val;
     * val = col.getRed();
     * float red = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * val = col.getGreen();
     * float green = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * val = col.getBlue();
     * float blue = val.getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
     * strokeColour = new PDFColor(red, green, blue);
     * }
     * }
     * //if(sp instanceof ColorType)
     * //{
     * //   ColorType ct = (ColorType)sp;
     * //   strokeColour = new PDFColor(ct.red(), ct.green(), ct.blue());
     * //}
     * }
     * sp = style.getPropertyCSSValue("stroke-width");
     * if(sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE)
     * {
     * strokeWidth = ((CSSPrimitiveValue)sp).getFloatValue(CSSPrimitiveValue.CSS_PT);
     * //PDFNumber pdfNumber = new PDFNumber();
     * //currentStream.add(pdfNumber.doubleOut(width) + " w\n");
     * //strokeWidth = ((SVGLengthImpl)sp).getValue();
     * }
     * else
     * strokeWidth = 1;
     * }
     * if (area instanceof SVGRectElement)
     * {
     * SVGRectElement rg = (SVGRectElement)area;
     * float rectx = rg.getX().getBaseVal().getValue() + posx / 1000;
     * float recty = ((pageHeight / 10) - posy/1000) + rg.getY().getBaseVal().getValue();
     * float rx = rg.getRx().getBaseVal().getValue();
     * float ry = rg.getRy().getBaseVal().getValue();
     * float rw = rg.getWidth().getBaseVal().getValue();
     * float rh = rg.getHeight().getBaseVal().getValue();
     * addRect(rectx, recty, rw, rh, rx, ry, fillColour, strokeColour, strokeWidth);
     * }
     * else if (area instanceof SVGLineElement)
     * {
     * SVGLineElement lg = (SVGLineElement)area;
     * float x1 = lg.getX1().getBaseVal().getValue() + posx / 1000;
     * float y1 = ((pageHeight / 10) - posy/1000) + lg.getY1().getBaseVal().getValue();
     * float x2 = lg.getX2().getBaseVal().getValue() + posx / 1000;
     * float y2 = ((pageHeight / 10) - posy/1000) + lg.getY2().getBaseVal().getValue();
     * addLine(x1,y1,x2,y2, strokeColour, strokeWidth);
     * }
     * else if (area instanceof SVGTextElementImpl)
     * {
     * //currentStream.add("BT\n");
     * renderText(fontState, (SVGTextElementImpl)area, posx / 1000f, ((float)(pageHeight / 10) - posy/1000f));
     * //currentStream.add("ET\n");
     * }
     * else if (area instanceof SVGCircleElement)
     * {
     * SVGCircleElement cg = (SVGCircleElement)area;
     * float cx = cg.getCx().getBaseVal().getValue() + posx / 1000;
     * float cy = ((pageHeight / 10) - posy/1000) + cg.getCy().getBaseVal().getValue();
     * float r = cg.getR().getBaseVal().getValue();
     * //addCircle(cx,cy,r, di);
     * addRect(cx - r, cy - r, 2 * r, 2 * r, r, r, fillColour, strokeColour, strokeWidth);
     * }
     * else if (area instanceof SVGEllipseElement)
     * {
     * SVGEllipseElement cg = (SVGEllipseElement)area;
     * float cx = cg.getCx().getBaseVal().getValue() + posx / 1000;
     * float cy = ((pageHeight / 10) - posy/1000) + cg.getCy().getBaseVal().getValue();
     * float rx = cg.getRx().getBaseVal().getValue();
     * float ry = cg.getRy().getBaseVal().getValue();
     * //addEllipse(cx,cy,rx,ry, di);
     * addRect(cx - rx, cy - ry, 2 * rx, 2 * ry, rx, ry, fillColour, strokeColour, strokeWidth);
     * }
     * else if (area instanceof SVGPathElementImpl)
     * {
     * //addPath(((SVGPathElementImpl)area).pathElements, posx, posy, di);
     * }
     * else if (area instanceof SVGPolylineElementImpl)
     * {
     * addPolyline(((SVGPolylineElementImpl)area).points, posx, posy, fillColour, strokeColour, strokeWidth, false);
     * }
     * else if (area instanceof SVGPolygonElementImpl)
     * {
     * addPolyline(((SVGPolylineElementImpl)area).points, posx, posy, fillColour, strokeColour, strokeWidth, true);
     * }
     * else if (area instanceof SVGGElementImpl)
     * {
     * renderGArea(fontState, (SVGGElementImpl)area, x, y);
     * }
     * else if(area instanceof SVGUseElementImpl)
     * {
     * SVGUseElementImpl ug = (SVGUseElementImpl)area;
     * String ref = ug.link;
     * ref = ref.substring(1, ref.length());
     * SVGElement graph = null;
     * //GraphicImpl graph = null;
     * //graph = area.locateDef(ref);
     * if(graph != null) {
     * // probably not the best way to do this, should be able
     * // to render without the style being set.
     * //GraphicImpl parent = graph.getGraphicParent();
     * //graph.setParent(area);
     * // need to clip (if necessary) to the use area
     * // the style of the linked element is as if is was
     * // a direct descendant of the use element.
     * renderElement(fontState, graph, posx, posy);
     * //graph.setParent(parent);
     * }
     * }
     * else if (area instanceof SVGImageElementImpl)
     * {
     * SVGImageElementImpl ig = (SVGImageElementImpl)area;
     * renderImage(fontState, ig.link, ig.x, ig.y, ig.width, ig.height);
     * }
     * else if (area instanceof SVGSVGElement)
     * {
     * // the x and y pos will be wrong!
     * renderSVG(fontState, (SVGSVGElement)area, x, y);
     * }
     * else if (area instanceof SVGAElement)
     * {
     * SVGAElement ael = (SVGAElement)area;
     * org.w3c.dom.NodeList nl = ael.getChildNodes();
     * for ( int count = 0 ; count < nl.getLength() ; count++ )
     * {
     * org.w3c.dom.Node n = nl.item(count);
     * if ( n instanceof SVGElement )
     * {
     * if ( n instanceof GraphicElement )
     * {
     * SVGRect rect = ((GraphicElement)n).getBBox();
     * if ( rect != null )
     * {
     * //   currentAnnotList = this.pdfDoc.makeAnnotList();
     * //   currentPage.setAnnotList(currentAnnotList);
     * //   String dest = linkSet.getDest();
     * //   int linkType = linkSet.getLinkType();
     * //   currentAnnotList.addLink(
     * //           this.pdfDoc.makeLink(lrect.getRectangle(), dest, linkType));
     * //   currentAnnotList = null;
     * //}
     * }
     * renderElement(fontState, (SVGElement)n, posx, posy);
     * }
     * }
     * }
     * else if ( area instanceof SVGSwitchElement )
     * {
     * handleSwitchElement(fontState, posx, posy, (SVGSwitchElement)area);
     * }
     * // should be done with some cleanup code, so only
     * // required values are reset.
     * //currentStream.add("Q\n");
     * }
     */

    private void setFont(String name, float size) {
        return;
    }

    /*
     * SVG - Not implemented yet.
     * public void renderText(FontState fontState, SVGTextElementImpl tg, float x, float y)
     * {
     * PDFNumber pdfNumber = new PDFNumber();
     * CSSStyleDeclaration styles;
     * styles = tg.getStyle();
     * //applyStyle(tg, styles);
     * // apply transform
     * // text has a Tm and need to handle each element
     * SVGTransformList trans = tg.getTransform().getBaseVal();
     * SVGMatrix matrix = trans.consolidate().getMatrix();
     * String transstr = (pdfNumber.doubleOut(matrix.getA())
     * + " " + pdfNumber.doubleOut(matrix.getB())
     * + " " + pdfNumber.doubleOut(matrix.getC())
     * + " " + pdfNumber.doubleOut(-matrix.getD()) + " ");
     * String fontFamily = null;
     * CSSValue sp = styles.getPropertyCSSValue("font-family");
     * if ( sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * if ( ((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING )
     * fontFamily = sp.getCssText();
     * }
     * if ( fontFamily == null )
     * fontFamily = fontState.getFontFamily();
     * String fontStyle = null;
     * sp = styles.getPropertyCSSValue("font-style");
     * if ( sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * if ( ((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING )
     * fontStyle = sp.getCssText();
     * }
     * if ( fontStyle == null )
     * fontStyle = fontState.getFontStyle();
     * String fontWeight = null;
     * sp = styles.getPropertyCSSValue("font-weight");
     * if( sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * if ( ((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING )
     * fontWeight = sp.getCssText();
     * }
     * if( fontWeight == null )
     * fontWeight = fontState.getFontWeight();
     * float fontSize;
     * sp = styles.getPropertyCSSValue("font-size");
     * if( sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE )
     * {
     * //if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
     * fontSize = ((CSSPrimitiveValue)sp).getFloatValue(CSSPrimitiveValue.CSS_PT);
     * //}
     * }
     * else
     * {
     * fontSize = fontState.getFontSize() / 1000f;
     * }
     * FontState fs = fontState;
     * try
     * {
     * fs = new FontState(fontState.getFontInfo(), fontFamily, fontStyle,
     * fontWeight, (int)(fontSize * 1000));
     * }
     * catch( Exception fope )
     * {
     * //   fope.printStackTrace();
     * }
     * //currentStream.add("/" + fs.getFontName() + " " + fontSize + " Tf\n");
     * setFont(fs.getFontName(), fontSize * 1000);
     * float tx = tg.x;
     * float ty = tg.y;
     * float currentX = x + tx;
     * float currentY = y + ty;
     * List list = tg.textList;
     * for ( Enumeration e = list.elements() ; e.hasMoreElements() ; )
     * {
     * Object o = e.nextElement();
     * styles = tg.getStyle();
     * //applyStyle(tg, styles);
     * if( o instanceof String )
     * {
     * String str = (String)o;
     * //currentStream.add(transstr
     * //   + (currentX + matrix.getE()) + " "
     * //   + (y+ty + matrix.getF()) + " Tm "
     * //   + "(");
     * boolean spacing = "preserve".equals(tg.getXMLspace());
     * //currentX = addSVGStr(fs, currentX, str, spacing);
     * //currentStream.add(") Tj\n");
     * //   for(int count = 0; count < str.length(); count++) {
     * //   }
     * //   currentX += fs.width(' ') / 1000f;
     * currentStream.add("\033&a" + (currentX + matrix.getE())*10 + "h" + (y+ty + matrix.getF())*10 + "V" + str);
     * for ( int count = 0; count < str.length(); count++ )
     * {
     * currentX += fs.width(str.charAt(count)) / 1000f;
     * }
     * currentX += fs.width(' ') / 1000f;
     * } else if(o instanceof SVGTextPathElementImpl) {
     * SVGTextPathElementImpl tpg = (SVGTextPathElementImpl)o;
     * String ref = tpg.str;
     * SVGElement graph = null;
     * //   graph = tpg.locateDef(ref);
     * if(graph != null && graph instanceof SVGPathElementImpl) {
     * // probably not the best way to do this, should be able
     * // to render without the style being set.
     * //   GraphicImpl parent = graph.getGraphicParent();
     * //   graph.setParent(tpg);
     * // set text path??
     * // how should this work
     * //   graph.setParent(parent);
     * }
     * } else if(o instanceof SVGTRefElementImpl) {
     * SVGTRefElementImpl trg = (SVGTRefElementImpl)o;
     * String ref = trg.ref;
     * ref = ref.substring(1, ref.length());
     * SVGElement graph = null;
     * //   graph = trg.locateDef(ref);
     * if(graph != null && graph instanceof SVGTextElementImpl) {
     * //   GraphicImpl parent = graph.getGraphicParent();
     * //   graph.setParent(trg);
     * SVGTextElementImpl te = (SVGTextElementImpl)graph;
     * renderText(fs, te, (int)(x + tx), (int)(y + ty));
     * //   graph.setParent(parent);
     * }
     * } else if(o instanceof SVGTSpanElementImpl) {
     * SVGTSpanElementImpl tsg = (SVGTSpanElementImpl)o;
     * styles = tsg.getStyle();
     * //applyStyle(tsg, styles);
     * boolean changed = false;
     * String newprop = null;
     * sp = styles.getPropertyCSSValue("font-family");
     * if(sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
     * if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
     * newprop = sp.getCssText();
     * }
     * }
     * if(newprop != null && !newprop.equals(fontFamily)) {
     * fontFamily = newprop;
     * changed = true;
     * }
     * sp = styles.getPropertyCSSValue("font-style");
     * if(sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
     * if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
     * newprop = sp.getCssText();
     * }
     * }
     * if(newprop != null && !newprop.equals(fontStyle)) {
     * fontStyle = newprop;
     * changed = true;
     * }
     * sp = styles.getPropertyCSSValue("font-weight");
     * if(sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
     * if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_STRING) {
     * newprop = sp.getCssText();
     * }
     * }
     * if(newprop != null && !newprop.equals(fontWeight)) {
     * fontWeight = newprop;
     * changed = true;
     * }
     * float newSize = fontSize;
     * sp = styles.getPropertyCSSValue("font-size");
     * if(sp != null && sp.getValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
     * //   if(((CSSPrimitiveValue)sp).getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER) {
     * newSize = ((CSSPrimitiveValue)sp).getFloatValue(CSSPrimitiveValue.CSS_PT);
     * //   }
     * }
     * if ( fontSize != newSize )
     * {
     * fontSize = newSize;
     * changed = true;
     * }
     * FontState oldfs = null;
     * if ( changed )
     * {
     * oldfs = fs;
     * try
     * {
     * fs = new FontState(fontState.getFontInfo(), fontFamily, fontStyle,
     * fontWeight, (int)(fontSize * 1000));
     * }
     * catch(Exception fope)
     * {
     * }
     * setFont(fs.getFontName(), fontSize * 1000);
     * //currentStream.add("/" + fs.getFontName() + " " + fontSize + " Tf\n");
     * }
     * float baseX;
     * float baseY;
     * StringBuffer pdf = new StringBuffer();
     * boolean spacing = "preserve".equals(tsg.getXMLspace());
     * boolean inbetween = false;
     * boolean addedspace = false;
     * int charPos = 0;
     * float xpos = currentX;
     * float ypos = currentY;
     * for ( int i=0 ; i < tsg.str.length() ; i++ )
     * {
     * char ch = tsg.str.charAt(i);
     * xpos = currentX;
     * ypos = currentY;
     * if ( tsg.ylist.size() > charPos )
     * ypos = y + ty + ((Float)tsg.ylist.get(charPos)).floatValue();
     * if ( tsg.dylist.size() > charPos )
     * ypos = ypos + ((Float)tsg.dylist.get(charPos)).floatValue();
     * if ( tsg.xlist.size() > charPos )
     * xpos = x + tx + ((Float)tsg.xlist.get(charPos)).floatValue();
     * if ( tsg.dxlist.size() > charPos )
     * xpos = xpos + ((Float)tsg.dxlist.get(charPos)).floatValue();
     * switch (ch)
     * {
     * case '   ':
     * case ' ':
     * if ( spacing )
     * {
     * currentX = xpos + fs.width(' ') / 1000f;
     * currentY = ypos;
     * charPos++;
     * }
     * else
     * {
     * if ( inbetween && !addedspace)
     * {
     * addedspace = true;
     * currentX = xpos + fs.width(' ') / 1000f;
     * currentY = ypos;
     * charPos++;
     * }
     * }
     * break;
     * case '\n':
     * case '\r':
     * if ( spacing )
     * {
     * currentX = xpos + fs.width(' ') / 1000f;
     * currentY = ypos;
     * charPos++;
     * }
     * break;
     * default:
     * addedspace = false;
     * pdf = pdf.append(transstr
     * + (xpos + matrix.getE()) + " "
     * + (ypos + matrix.getF()) + " Tm "
     * + "(" + ch + ") Tj\n");
     * pdf = pdf.append("\033&a" + (xpos + matrix.getE())*10 + "h" + (ypos + matrix.getF())*10 + "V" + ch);
     * currentX = xpos + fs.width(ch) / 1000f;
     * currentY = ypos;
     * charPos++;
     * inbetween = true;
     * break;
     * }
     * //currentStream.add(pdf.toString());
     * }
     * //   currentX += fs.width(' ') / 1000f;
     * if ( changed )
     * {
     * fs = oldfs;
     * setFont(fs.getFontName(), fs.getFontSize() * 1000);
     * //currentStream.add("/" + fs.getFontName() + " " + fs.getFontSize() / 1000f + " Tf\n");
     * }
     * }
     * else
     * {
     * System.err.println("Error: unknown text element " + o);
     * }
     * }
     * }
     */

    /*
     * SVG - Not yet implemented
     * public void renderGArea(FontState fontState, SVGGElement area, int posx, int posy)
     * {
     * NodeList nl = area.getChildNodes();
     * for ( int count = 0 ; count < nl.getLength() ; count++ )
     * {
     * Node n = nl.item(count);
     * if ( n instanceof SVGElement )
     * renderElement(fontState, (SVGElement)n, posx, posy);
     * }
     * }
     */

    /*
     * SVG - Not yet implemented
     * void handleSwitchElement(FontState fontState, int posx, int posy, SVGSwitchElement ael)
     * {
     * SVGList relist = ael.getRequiredExtensions();
     * SVGList rflist = ael.getRequiredFeatures();
     * SVGList sllist = ael.getSystemLanguage();
     * org.w3c.dom.NodeList nl = ael.getChildNodes();
     * for(int count = 0; count < nl.getLength(); count++) {
     * org.w3c.dom.Node n = nl.item(count);
     * // only render the first child that has a valid
     * // test data
     * if(n instanceof GraphicElement) {
     * GraphicElement graphic = (GraphicElement)n;
     * SVGList grelist = graphic.getRequiredExtensions();
     * // if null it evaluates to true
     * if(grelist != null) {
     * for(int i = 0; i < grelist.getNumberOfItems(); i++) {
     * String str = (String)grelist.getItem(i);
     * if(relist == null) {
     * // use default extension set
     * // currently no extensions are supported
     * //   if(!(str.equals("http:// ??"))) {
     * continue;
     * //   }
     * } else {
     * }
     * }
     * }
     * SVGList grflist = graphic.getRequiredFeatures();
     * if(grflist != null) {
     * for(int i = 0; i < grflist.getNumberOfItems(); i++) {
     * String str = (String)grflist.getItem(i);
     * if(rflist == null) {
     * // use default feature set
     * if(!(str.equals("org.w3c.svg.static")
     * || str.equals("org.w3c.dom.svg.all"))) {
     * continue;
     * }
     * } else {
     * boolean found = false;
     * for(int j = 0; j < rflist.getNumberOfItems(); j++) {
     * if(rflist.getItem(j).equals(str)) {
     * found = true;
     * break;
     * }
     * }
     * if(!found)
     * continue;
     * }
     * }
     * }
     * SVGList gsllist = graphic.getSystemLanguage();
     * if(gsllist != null) {
     * for(int i = 0; i < gsllist.getNumberOfItems(); i++) {
     * String str = (String)gsllist.getItem(i);
     * if(sllist == null) {
     * // use default feature set
     * if(!(str.equals("en"))) {
     * continue;
     * }
     * } else {
     * boolean found = false;
     * for(int j = 0; j < sllist.getNumberOfItems(); j++) {
     * if(sllist.getItem(j).equals(str)) {
     * found = true;
     * break;
     * }
     * }
     * if(!found)
     * continue;
     * }
     * }
     * }
     * renderElement(fontState, (SVGElement)n, posx, posy);
     * // only render the first valid one
     * break;
     * }
     * }
     * }
     */

    /**
     * render inline area to Text
     *
     * @param area inline area to render
     */
    public void renderWordArea(WordArea area) {
        // System.out.println("TXTRenderer.renderInlineArea: currentXPosition=" + this.currentXPosition + " currentYPosition=" + this.currentYPosition + " text=" + area.getText());
        int rx = this.currentXPosition;
        int bl = this.currentYPosition;

        String s = area.getText();

        if (debug)
            System.out.println("TXTRenderer.renderInlineArea: rx=" + rx
                               + " bl=" + bl + " pageHeight=" + pageHeight);
        addStr((int)((pageHeight - (bl / 100)) * 100 * yFactor) - 1,
               (int)(rx * xFactor), s, true);

        this.currentXPosition += area.getContentWidth();
    }

    /**
     * render inline space to Text
     *
     * @param space space to render
     */
    public void renderInlineSpace(InlineSpace space) {
        this.currentXPosition += space.getSize();
    }

    /**
     * render page into Text
     *
     * @param page page to render
     */
    public void renderPage(Page page) {
        if (debug)
            System.out.println("TXTRenderer.renderPage() page.getHeight() = "
                               + page.getHeight());

        maxX = (int)(textCPI * page.getWidth() / 72000 + 1);
        maxY = (int)(textLPI * page.getHeight() / 72000 + 1);
        xFactor = (float)(maxX - 1) / (float)page.getWidth();
        yFactor = (float)(maxY - 1) / (float)page.getHeight();
        charData = new StringBuffer[maxY + 1];
        decoData = new StringBuffer[maxY + 1];

        if (paperheight > 0)
            pageHeight = paperheight;
        else
            pageHeight = page.getHeight() / 100;

        if (debug)
            System.out.println("TXTRenderer.renderPage() maxX=" + maxX
                               + " maxY=" + maxY + " xFactor=" + xFactor
                               + " yFactor=" + yFactor + " paperHeight="
                               + pageHeight);

        this.currentFontName = "";
        this.currentFontSize = 0;

        // currentStream.add("BT\n");
        renderRegions(page);

        // Write out the buffers.
        for (int row = 0; row <= maxY; row++) {
            StringBuffer cr = charData[row];
            StringBuffer dr = decoData[row];
            StringBuffer outr = null;

            if (cr != null && dr == null)
                outr = cr;
            else if (dr != null && cr == null)
                outr = dr;
            else if (cr != null && dr != null) {
                int len = dr.length();
                if (cr.length() > len)
                    len = cr.length();
                outr = new StringBuffer();
                for (int countr = 0; countr < len; countr++) {
                    if (countr < cr.length() && cr.charAt(countr) != ' ')
                        outr.append(cr.charAt(countr));
                    else if (countr < dr.length())
                        outr.append(dr.charAt(countr));
                    else
                        outr.append(' ');
                }
            }

            if (outr != null)
                currentStream.add(outr.toString());
            if (row < maxY)
                currentStream.add(lineEnding);
        }

        // End page.
        // if ( ++curdiv == divisions || divisions == -1 )
        // {
        // curdiv = 0;
        // currentStream.add("\f");
        // }

        // Links, etc not implemented...
        /*
         * currentPage = this.pdfDoc.makePage(this.pdfResources, currentStream,
         * page.getWidth()/1000,
         * page.getHeight()/1000, page);
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
        log.info("rendering areas to TEXT");
        currentStream = new TXTStream(outputStream);
        String encoding;
        if (options != null && (encoding=(String)options.get(encodingOptionName))!=null) {
            try {
                byte buff[] = " ".getBytes(encoding);
            } catch (java.io.UnsupportedEncodingException uee) {
                log.warn("Encoding '"+encoding+"' is not supported, so defaulted to " + DEFAULT_ENCODING);
                encoding = DEFAULT_ENCODING;
            }
        }
        else
            encoding = DEFAULT_ENCODING;
        currentStream.setEncoding(encoding);
        firstPage=true;
    }

    /**
      * In Mark's patch, this is endRenderer
      * However, I couldn't see how it builds that way, so
      * i changed it. - Steve gears@apache.org
      */

    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        log.info("writing out TEXT");
        outputStream.flush();
    }

    public void render(Page page, OutputStream outputStream) {
        idReferences = page.getIDReferences();

        if ( firstPage )
            firstPage = false;
        else
            currentStream.add(pageEnding);
        this.renderPage(page);
        currentStream.add(lineEnding);
    }
}
