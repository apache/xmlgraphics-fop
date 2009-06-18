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
package org.apache.fop.render.awt;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.SVGImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.DisplaySpace;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.layout.inline.InlineSpace;
import org.apache.fop.layout.inline.LeaderArea;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.svg.SVGArea;
import org.apache.fop.viewer.ProgressListener;
import org.apache.fop.viewer.Translator;

import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.Document;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.Pageable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.Paper;
import java.io.OutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * @author <a href="mailto:Juergen.Verwohlt@jCatalog.com">Juergen Verwohlt</a>
 * @author <a href="mailto:Rainer.Steinkuhle@jCatalog.com">Rainer Steinkuhle</a>
 * @author <a href="mailto:Stanislav.Gorkhover@jCatalog.com">Stanislav
 *   Gorkhover</a>
 * @author <a href="mailto:mark-fop@inomial.com">Mark Lillywhite</a>
 */

/*
 * Mark Lillywhite(?) made the following comment: Did lots of
 * cleaning up and made the class implement the new Renderer
 * interface. This class could also do with a general audit,
 * and I suspect it's not swing-thread-safe either.
*/
public class AWTRenderer extends AbstractRenderer implements Printable, Pageable {

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected Vector pageList = new Vector();
    protected ProgressListener progressListener = null;
    protected Translator res = null;

    protected Map fontNames = new java.util.HashMap();
    protected Map fontStyles = new java.util.HashMap();
    protected Color saveColor = null;

    /**
     * Image Object and Graphics Object. The Graphics Object is the Graphics
     * object that is contained withing the Image Object.
     */
    private BufferedImage pageImage = null;
    protected Graphics2D graphics = null;

    /**
     * The current (internal) font name
     */
    protected String currentFontName;

    /**
     * The current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * The current colour's red, green and blue component
     */
    protected float currentRed = 0;
    protected float currentGreen = 0;
    protected float currentBlue = 0;

    /**
     * Used to make the last font and color available to
     * renderInlineSpace() to render text decorations.
     */
    protected java.awt.Font lastFont = null;
    protected Color lastColor = null;

    /**
     * The parent component, used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     */
    protected Component parent;

    /**
     * options
     */
    protected java.util.Map options;

    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }

    public AWTRenderer(Translator aRes) {
        res = aRes;
    }

    /**
     * Sets parent component which is  used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     * @param parent the live AWT component reference
     */
    public void setComponent(Component parent) {
        this.parent = parent;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int aValue) {
        pageNumber = aValue;
    }

    public void setScaleFactor(double newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public BufferedImage getLastRenderedPage() {
        return pageImage;
    }

    /**
     * add a line to the current stream
     *
     * @param x1 the start x location in millipoints
     * @param y1 the start y location in millipoints
     * @param x2 the end x location in millipoints
     * @param y2 the end y location in millipoints
     * @param th the thickness in millipoints
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */

    // corrected 7/13/01 aml,rlc to properly handle thickness
    //
    protected void addLine(int x1, int y1, int x2, int y2, int th, float r,
                           float g, float b) {
        graphics.setColor(new Color(r, g, b));
        int x = x1;
        int y = y1;
        int height, width;
        if (x1 == x2)    // vertical line
        {
            height = y2 - y1;
            if (height > 0)    // y coordinates are reversed between fo and AWT
            {
                height = -height;
                y = y2;
            }
            width = th;
            if (width < 0) {
                width = -width;
                x -= width;
            }
        } else           // horizontal line
        {
            width = x2 - x1;
            if (width < 0) {
                width = -width;
                x = x2;
            }
            height = th;
            if (height > 0)    // y coordinates are reversed between fo and AWT
            {
                height = -height;
                y -= height;
            }
        }
        addRect(x, y, width, height, false);

        // // graphics.setColor(Color.red);
        // graphics.drawLine((int)(x1 / 1000f),
        // pageHeight - (int)(y1 / 1000f), (int)(x2 / 1000f),
        // pageHeight - (int)(y2 / 1000f));
    }


    /**
     * draw a rectangle
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    // changed by aml/rlc to use helper function that
    // corrects for integer roundoff, and to remove 3D effect
    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b) {
        graphics.setColor(new Color(r, g, b));
        // graphics.setColor(Color.green);
        addRect(x, y, w, h, true);
    }

    /**
     * draw a filled rectangle
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

    // changed by aml/rlc to use helper function that
    // corrects for integer roundoff
    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b, float fr, float fg, float fb) {
        graphics.setColor(new Color(r, g, b));
        addRect(x, y, w, h, true);
        graphics.setColor(new Color(fr, fg, fb));
        addRect(x, y, w, h, false);
    }

    /**
     * draw a filled rectangle in the current color
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param drawAsOutline true for draw, false for fill
     */

    // helper function by aml/rlc to correct integer roundoff problems
    //
    protected void addRect(int x, int y, int w, int h,
                           boolean drawAsOutline) {
        int startx = (x + 500) / 1000;
        int starty = pageHeight - ((y + 500) / 1000);
        int endx = (x + w + 500) / 1000;
        int endy = pageHeight - ((y + h + 500) / 1000);
        if (drawAsOutline) {
            graphics.drawRect(startx, starty, endx - startx, endy - starty);
        } else {
            //don't round down to zero
            if (w != 0 && endx == startx) endx++;
            if (h != 0 && endy == starty) endy++;
            graphics.fillRect(startx, starty, endx - startx, endy - starty);
        }
    }

    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
        float r = col.red();
        float g = col.green();
        float b = col.blue();
        addRect(x, y, w, h, r, g, b, r, g, b);
    }

    /**
     * To configure before print.
     *
     * Choose pages
     * Zoom factor
     * Page format  / Landscape or Portrait
     */
    public void transform(Graphics2D g2d, double zoomPercent, double angle) {
        AffineTransform at = g2d.getTransform();
        at.rotate(angle);
        at.scale(zoomPercent / 100.0, zoomPercent / 100.0);
        g2d.setTransform(at);
    }

    protected void drawFrame() {

        int width = pageWidth;
        int height = pageHeight;

        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.black);
        graphics.drawRect(-1, -1, width + 2, height + 2);
        graphics.drawLine(width + 2, 0, width + 2, height + 2);
        graphics.drawLine(width + 3, 1, width + 3, height + 3);

        graphics.drawLine(0, height + 2, width + 2, height + 2);
        graphics.drawLine(1, height + 3, width + 3, height + 3);
    }

    /**
     * Retrieve the number of pages in this document.
     *
     * @return the number of pages
     */
    public int getPageCount() {
        return pageList.size();
    }

    public void removePage(int page) {
        pageList.removeElementAt(page);
    }

    public void render(int aPageNumber) {
        if(aPageNumber >= pageList.size())
            return;

        try {
            render((Page) pageList.get(aPageNumber));
        } catch(IOException e) {
            e.printStackTrace();
            // This exception can't occur because we are not dealing with
            // any files
        }

    }

    public void render(Page page, OutputStream stream)
    throws IOException {
        pageList.add(page);
    }

    public void render(Page page)
    throws IOException {
        idReferences = page.getIDReferences();

        pageWidth = (int)((float)page.getWidth() / 1000f + .5);
        pageHeight = (int)((float)page.getHeight() / 1000f + .5);


        pageImage =
            new BufferedImage((int)((pageWidth * (int)scaleFactor) / 100),
                              (int)((pageHeight * (int)scaleFactor) / 100),
                              BufferedImage.TYPE_INT_RGB);

        graphics = pageImage.createGraphics();

        // Nov 18, 2002 - [aml/rlc] eliminates layout problems at various scaling

        graphics.setRenderingHint (RenderingHints.KEY_FRACTIONALMETRICS,
                                   RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        transform(graphics, scaleFactor, 0);
        drawFrame();

        renderPage(page);
    }

    public void renderPage(Page page) {

        this.currentFontName = "";
        this.currentFontSize = 0;

        renderRegions(page);
        // SG: Wollen wir Links abbilden?
        /*
         * if (page.hasLinks()) {
         * ....
         * }
         */
    }

    protected void doFrame(org.apache.fop.layout.Area area) {
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();

        if (area instanceof BlockArea) {
            rx += ((BlockArea)area).getStartIndent();
        }

        h = area.getContentHeight();
        int ry = this.currentYPosition;

        rx = rx - area.getPaddingLeft();
        ry = ry + area.getPaddingTop();
        w = w + area.getPaddingLeft() + area.getPaddingRight();
        h = h + area.getPaddingTop() + area.getPaddingBottom();

    doBackground(area, rx, ry, w, h);

        rx = rx - area.getBorderLeftWidth();
        ry = ry + area.getBorderTopWidth();
        w = w + area.getBorderLeftWidth() + area.getBorderRightWidth();
        h = h + area.getBorderTopWidth() + area.getBorderBottomWidth();

        BorderAndPadding bp = area.getBorderAndPadding();
        ColorType borderColor;

        if (area.getBorderTopWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.TOP);
            // addLine(rx, ry, rx + w, ry, area.getBorderTopWidth(),   // corrected aml/rlc
            addLine(rx, ry, rx + w, ry, -area.getBorderTopWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderLeftWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.LEFT);
            addLine(rx, ry, rx, ry - h, area.getBorderLeftWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderRightWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.RIGHT);
            addLine(rx + w, ry, rx + w, ry - h,
                    // area.getBorderRightWidth(), borderColor.red(), // corrected aml/rlc
                    -area.getBorderRightWidth(), borderColor.red(),
                    borderColor.green(),
                    borderColor.blue());
        }

        if (area.getBorderBottomWidth() != 0) {
            borderColor = bp.getBorderColor(BorderAndPadding.BOTTOM);
            addLine(rx, ry - h, rx + w, ry - h, area.getBorderBottomWidth(),
                    borderColor.red(), borderColor.green(),
                    borderColor.blue());
        }
    }



    protected Rectangle2D getBounds(org.apache.fop.layout.Area a) {
        return new Rectangle2D.Double(currentAreaContainerXPosition,
                                      currentYPosition,
                                      a.getAllocationWidth(), a.getHeight());
    }

    public void setupFontInfo(FontInfo fontInfo)
        throws FOPException {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        FontSetup.setup(fontInfo, fontImage.createGraphics());
    }

    public void renderDisplaySpace(DisplaySpace space) {
        int d = space.getSize();
        this.currentYPosition -= d;
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

    // correct integer roundoff    (aml/rlc)

    public void renderImageArea(ImageArea area) {

        int x = currentXPosition + area.getXOffset();
        int y = currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();
        this.currentYPosition -= h;

        FopImage img = area.getImage();

        if (img == null) {
            log.error("Error while loading image : area.getImage() is null");

            // correct integer roundoff
            // graphics.drawRect(x / 1000, pageHeight - y / 1000,
            // w / 1000, h / 1000);
            addRect(x, y, w, h, true);    // use helper function


            java.awt.Font f = graphics.getFont();
            java.awt.Font smallFont = new java.awt.Font(f.getFontName(),
                                      f.getStyle(), 8);

            graphics.setFont(smallFont);

            // correct integer roundoff   // aml/rlc
            // graphics.drawString("area.getImage() is null", x / 1000,
            // pageHeight - y / 1000);

            graphics.drawString("area.getImage() is null", (x + 500) / 1000,
                                pageHeight - (y + 500) / 1000);


            graphics.setFont(f);
        } else {
            if (img instanceof SVGImage) {
                try {
                    SVGDocument svg = ((SVGImage)img).getSVGDocument();
                    renderSVGDocument(svg, (int)x, (int)y);
                } catch (FopImageException e) {}

            } else {

                String urlString = img.getURL();
                try {
                    URL url = new URL(urlString);

                    ImageIcon icon = new ImageIcon(url);
                    Image image = icon.getImage();

                    // correct integer roundoff      aml/rlc
                    // graphics.drawImage(image, x / 1000,
                    // pageHeight - y / 1000, w / 1000, h / 1000,
                    // null);

                    int startx = (x + 500) / 1000;
                    int starty = pageHeight - ((y + 500) / 1000);
                    int endx = (x + w + 500) / 1000;
                    int endy = pageHeight - ((y + h + 500) / 1000);

                    // reverse start and end y because h is positive
                    graphics.drawImage(image, startx, starty, endx - startx,
                                       starty - endy, null);

                } catch (MalformedURLException mue) {
                    // cannot normally occur because, if URL is wrong, constructing FopImage
                    // will already have failed earlier on
                }

            }
        }

        this.currentXPosition += area.getContentWidth();
    }


    public void renderWordArea(WordArea area) {
        char ch;
        StringBuffer pdf = new StringBuffer();

        String fontname = area.getFontState().getFontName();
        int size = area.getFontState().getFontSize();

        float red = area.getRed();
        float green = area.getGreen();
        float blue = area.getBlue();

        FontMetricsMapper mapper;
        try {
            mapper =
                (FontMetricsMapper)area.getFontState().getFontInfo().getMetricsFor(fontname);
        } catch (FOPException iox) {
            mapper = new FontMetricsMapper("MonoSpaced", java.awt.Font.PLAIN,
                                           graphics);
        }

        if ((!fontname.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            this.currentFontName = fontname;
            this.currentFontSize = size;
        }

        if ((red != this.currentRed) || (green != this.currentGreen)
                || (blue != this.currentBlue)) {
            this.currentRed = red;
            this.currentGreen = green;
            this.currentBlue = blue;
        }

        int rx = this.currentXPosition;
        int bl = this.currentYPosition;


        String s = area.getText();

        Color oldColor = graphics.getColor();
        java.awt.Font oldFont = graphics.getFont();
        java.awt.Font f = mapper.getFont(size);

        if (saveColor != null) {
            if (saveColor.getRed() != red || saveColor.getGreen() != green
                    || saveColor.getBlue() != blue) {
                saveColor = new Color(red, green, blue);
            }
        } else {
            saveColor = new Color(red, green, blue);
        }
        graphics.setColor(saveColor);

        // Ralph LaChance (May 16, 2002)
        // AttributedString mechanism removed because of
        // rendering bug in both jdk 1.3.0_x and 1.4.
        // see bug parade 4650042 and others
        //
        graphics.setFont(f);

        // correct starting location for integer roundoff
        int newx = (int)(rx + 500) / 1000;
        int newy = (int)(pageHeight - (bl + 500) / 1000);

        // draw text, corrected for integer roundoff
        graphics.drawString(s, newx, newy);

        FontMetrics fm = graphics.getFontMetrics(f);
        int tdwidth = (int)fm.getStringBounds(s, graphics).getWidth();

        // text decorations
        renderTextDecoration(rx, bl, tdwidth, f, " ",
                area.getUnderlined(),
                area.getOverlined(),
                area.getLineThrough());

        // remember last font and color for possible inline spaces
        // (especially for text decorations)
        this.lastFont = graphics.getFont();
        this.lastColor = graphics.getColor();

        graphics.setFont(oldFont);
        graphics.setColor(oldColor);

        this.currentXPosition += area.getContentWidth();
    }


    public void renderInlineSpace(InlineSpace space) {
        if (space.getUnderlined() || space.getOverlined() || space.getLineThrough()) {
            int rx = this.currentXPosition;
            int bl = this.currentYPosition;

            java.awt.Font oldFont = graphics.getFont();
            if (this.lastFont != null) {
                graphics.setFont(this.lastFont);
            }
            Color oldColor = graphics.getColor();
            if (this.lastColor != null) {
                graphics.setColor(this.lastColor);
            }

            int width = (int)(space.getSize() + 500) / 1000;
            renderTextDecoration(rx, bl, width, graphics.getFont(), " ",
                    space.getUnderlined(),
                    space.getOverlined(),
                    space.getLineThrough());

            graphics.setFont(oldFont);
            graphics.setColor(oldColor);
        }

        this.currentXPosition += space.getSize();
    }


    protected void renderTextDecoration(int x, int bl, int width,
                    java.awt.Font font, String text,
                    boolean underline,
                    boolean overline,
                    boolean linethrough) {
        if (!(underline || overline || linethrough)) return;
        int newx = (int)(x + 500) / 1000;
        int newy = (int)(pageHeight - (bl + 500) / 1000);

        // text decorations
        FontMetrics fm = graphics.getFontMetrics(font);
        LineMetrics lm = fm.getLineMetrics(text, graphics);

        int ulthick = (int)lm.getUnderlineThickness();
        if (ulthick < 1)
            ulthick = 1;   // don't allow it to disappear
        if (underline) {
            // nothing in awt specifies underline location,
            // descent/2 seems to match my word processor
            int deltay = fm.getDescent() / 2;
            graphics.fillRect(newx, newy + deltay, width, ulthick);
        }
        if (overline) {
            // todo: maybe improve positioning of overline
            int deltay = -(int)(lm.getAscent() * 0.8);
            graphics.fillRect(newx, newy + deltay, width, ulthick);
        }
        if (linethrough) {
            int ltthick = (int)lm.getStrikethroughThickness();
            if (ltthick < 1)
                ltthick = 1;   // don't allow it to disappear
            int deltay = (int)lm.getStrikethroughOffset();
            graphics.fillRect(newx, newy + deltay, width, ltthick);
        }
    }


    /**
     * render leader area into AWT
     *
     * @param area area to render
     */

    // call to addRect corrected by aml/rlc

    public void renderLeaderArea(LeaderArea area) {

        int rx = this.currentXPosition;
        int ry = this.currentYPosition;
        int w = area.getLeaderLength();
        int h = area.getHeight();
        int th = area.getRuleThickness();
        int st = area.getRuleStyle();    // not used at the moment
        float r = area.getRed();
        float g = area.getGreen();
        float b = area.getBlue();
        Color oldColor = graphics.getColor();

        graphics.setColor(new Color(r, g, b));

        // use helper function to correct integer roundoff   - aml/rlc
        // graphics.fillRect((int)(rx / 1000f),
        // (int)(pageHeight - ry / 1000f), (int)(w / 1000f),
        // (int)(th / 1000f));

        addRect(rx, ry, w, -th, false);    // NB addRect expects negative height

        graphics.setColor(oldColor);
        this.currentXPosition += area.getContentWidth();
    }

    public void renderSVGArea(SVGArea area) {

        int x = this.currentXPosition;
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        Document doc = area.getSVGDocument();
        renderSVGDocument(doc, x, y);
        this.currentXPosition += area.getContentWidth();
    }

    protected void renderSVGDocument(Document doc, int x, int y) {
        MUserAgent userAgent = new MUserAgent(new AffineTransform());
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
        float w = (float)ctx.getDocumentSize().getWidth() * 1000f;
        float h = (float)ctx.getDocumentSize().getHeight() * 1000f;

        // correct integer roundoff     aml/rlc
        // graphics.translate(x / 1000f, pageHeight - y / 1000f);
        graphics.translate((x + 500) / 1000, pageHeight - (y + 500) / 1000);

        SVGSVGElement svg = ((SVGDocument)doc).getRootElement();
        AffineTransform at = ViewBox.getPreserveAspectRatioTransform(svg, w / 1000f, h / 1000f);
        AffineTransform inverse = null;
        try {
            inverse = at.createInverse();
        } catch(NoninvertibleTransformException e) {
        }
        if(!at.isIdentity()) {
            graphics.transform(at);
        }

        try {
            root.paint(graphics);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(inverse != null && !inverse.isIdentity()) {
            graphics.transform(inverse);
        }
        // correct integer roundoff     aml/rlc
        // graphics.translate(-x / 1000f, y / 1000f - pageHeight);
        graphics.translate(-(x + 500) / 1000, (y + 500) / 1000 - pageHeight);

    }

    public void setProducer(String producer) {
        // defined in Renderer Interface
    }

    public int print(Graphics g, PageFormat pageFormat,
                     int pageIndex) throws PrinterException {
        if (pageIndex >= pageList.size())
            return NO_SUCH_PAGE;

        Graphics2D oldGraphics = graphics;
        int oldPageNumber = pageNumber;

        graphics = (Graphics2D)g;

        // Nov 18, 2002 - [aml/rlc] eliminates layout problems at various scaling

        graphics.setRenderingHint (RenderingHints.KEY_FRACTIONALMETRICS,
                                   RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        Page aPage = (Page)pageList.get(pageIndex);
        renderPage(aPage);
        graphics = oldGraphics;

        return PAGE_EXISTS;
    }

    public int getNumberOfPages() {
        return pageList.size();
    }

    public PageFormat getPageFormat(int pageIndex)
    throws IndexOutOfBoundsException {
        if (pageIndex >= pageList.size())
            return null;

        Page page = (Page)pageList.get(pageIndex);
        PageFormat pageFormat = new PageFormat();
        Paper paper = new Paper();

        double width = page.getWidth();
        double height = page.getHeight();

        // if the width is greater than the height assume lanscape mode
        // and swap the width and height values in the paper format
        if (width > height) {
            paper.setImageableArea(0, 0, height / 1000d, width / 1000d);
            paper.setSize(height / 1000d, width / 1000d);
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        } else {
            paper.setImageableArea(0, 0, width / 1000d, height / 1000d);
            paper.setSize(width / 1000d, height / 1000d);
            pageFormat.setOrientation(PageFormat.PORTRAIT);
        }
        pageFormat.setPaper(paper);
        return pageFormat;
    }

    public Printable getPrintable(int pageIndex)
    throws IndexOutOfBoundsException {
        return this;
    }

    public void setProgressListener(ProgressListener l) {
        progressListener = l;
    }

    public static Color colorType2Color(ColorType ct) {
        if (ct == null) {
            return null;
        }
        return new Color(ct.red(), ct.green(), ct.blue());
    }



    /**
     * Draws an image.
     * TODO: protect other image formats (JIMI)
     */
    /*
     * public void renderImage(String href, float x, float y, float width,
     * float height, Vector transform) {
     * // What is with transformations?
     * try {
     * URL url = new URL(href);
     * ImageIcon imageIcon = new ImageIcon(url);
     * AffineTransform fullTransform = new AffineTransform();
     * AffineTransform aTransform;
     * transform = (transform == null) ? new Vector() : transform;
     * for (int i = 0; i < transform.size(); i++) {
     * org.w3c.dom.svg.SVGTransform t =
     * (org.w3c.dom.svg.SVGTransform)
     * transform.get(i);
     * SVGMatrix matrix = t.getMatrix();
     * aTransform = new AffineTransform(matrix.getA(),
     * matrix.getB(), matrix.getC(), matrix.getD(),
     * matrix.getE(), matrix.getF());
     * fullTransform.concatenate(aTransform);
     * }
     * BufferedImage bi = new BufferedImage((int) width, (int) height,
     * BufferedImage.TYPE_INT_RGB);
     * Graphics2D g2d = bi.createGraphics();
     * BufferedImageOp bop = new AffineTransformOp(fullTransform,
     * AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
     * g2d.drawImage(imageIcon.getImage(), 0, 0, (int) width,
     * (int) height, imageIcon.getImageObserver());
     * graphics.drawImage(bi, bop, (int) x, (int) y);
     * } catch (Exception ex) {
     * log.error("AWTRenderer: renderImage(): " +
     * ex.getMessage(), ex);
     * }
     * }
     */

    public void renderForeignObjectArea(ForeignObjectArea area) {
        area.getObject().render(this);
    }


    protected class MUserAgent extends org.apache.fop.svg.SVGUserAgent {

        /**
         * Creates a new SVGUserAgent.
         */
        protected MUserAgent(AffineTransform at) {
            super(at);
        }

        /**
         * Opens a link in a new component.
         * @param doc The current document.
         * @param uri The document URI.
         */
        public void openLink(SVGAElement elt) {
            // application.openLink(uri);
        }


        public Point getClientAreaLocationOnScreen() {
            return new Point(0, 0);
        }

        public void setSVGCursor(java.awt.Cursor cursor) {}


        public Dimension2D getViewportSize() {
            return new Dimension(100, 100);
        }

        public EventDispatcher getEventDispatcher() {
            return null;
        }
    }

    public void startRenderer(OutputStream outputStream)
    throws IOException {}


    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        render(0);
    }

}
