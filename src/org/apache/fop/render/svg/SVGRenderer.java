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
package org.apache.fop.render.svg;

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
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.SVGImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.svg.SVGArea;
import org.apache.fop.svg.SVGUtilities;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.ImageIcon;

import org.apache.fop.render.AbstractRenderer;

public class SVGRenderer extends AbstractRenderer {
    static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document svgDocument;
    Element svgRoot;
    Element currentPageG = null;
    Element lastLink = null;

    float totalWidth = 0;
    float totalHeight = 0;

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected int pageNumber = 0;

    protected Map fontNames = new java.util.HashMap();
    protected Map fontStyles = new java.util.HashMap();
    protected Color saveColor = null;

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
     * options
     */
    protected java.util.Map options;

    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }

    public SVGRenderer() {
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
    protected void addLine(int x1, int y1, int x2, int y2, int th, float r,
                           float g, float b) {
        Element line = SVGUtilities.createLine(svgDocument, x1 / 1000f, pageHeight - (y1 / 1000f), x2 / 1000f, pageHeight - (y2 / 1000f));
        line.setAttributeNS(null, "style", "stroke-width:" + (Math.abs(th) / 1000f)
                            + ";stroke:rgb(" + ((int)(255 * r)) + "," + ((int)(255 * g)) + "," + ((int)(255 * b)) + ")");
        currentPageG.appendChild(line);
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
    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b) {
        Element rect = SVGUtilities.createRect(svgDocument, x / 1000f, pageHeight - (y / 1000f), w / 1000f, h / 1000f);
        rect.setAttributeNS(null, "style", "stroke:rgb(" + ((int)(255 * r)) + "," + ((int)(255 * g)) + "," + ((int)(255 * b)) + ")");
        currentPageG.appendChild(rect);
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
    protected void addRect(int x, int y, int w, int h, float r, float g,
                           float b, float fr, float fg, float fb) {
        Element rect = SVGUtilities.createRect(svgDocument, x / 1000f, pageHeight - (y / 1000f), w / 1000f, h / 1000f);
        rect.setAttributeNS(null, "style", "stroke:rgb(" + ((int)(255 * r)) + "," + ((int)(255 * g)) + "," + ((int)(255 * b)) + ");fill:rgb(" + ((int)(255 * fr)) + "," + ((int)(255 * fg)) + "," + ((int)(255 * fb)) + ")");
        currentPageG.appendChild(rect);
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
    protected void addRect(int x, int y, int w, int h,
                           boolean drawAsOutline) {
        int startx = (x + 500) / 1000;
        int starty = pageHeight - ((y + 500) / 1000);
        int endx = (x + w + 500) / 1000;
        int endy = pageHeight - ((y + h + 500) / 1000);
        if (drawAsOutline) {
            Element rect = SVGUtilities.createRect(svgDocument, startx, starty, endx - startx, endy - starty);
            rect.setAttributeNS(null, "style", "fill:none");
            currentPageG.appendChild(rect);
        } else {
            Element rect = SVGUtilities.createRect(svgDocument, startx, starty, endx - startx, starty - endy);
            rect.setAttributeNS(null, "style", "stroke:none");
            currentPageG.appendChild(rect);
        }
    }

    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
        float r = col.red();
        float g = col.green();
        float b = col.blue();
        addRect(x, y, w, h, r, g, b, r, g, b);
    }

    protected void drawFrame() {
        int width = pageWidth;
        int height = pageHeight;
        Element rect = SVGUtilities.createRect(svgDocument, 0, 0, width, height);
        rect.setAttributeNS(null, "style", "fill:none;stroke:black");
        currentPageG.appendChild(rect);
    }

    public void render(Page page, OutputStream stream)
    throws IOException {
        pageNumber++;
        this.render(page);
    }

    public void render(Page page)
    throws IOException {
        idReferences = page.getIDReferences();

        int lastWidth = pageWidth;
        int lastHeight = pageHeight;

        pageWidth = (int)((float)page.getWidth() / 1000f + .5);
        pageHeight = (int)((float)page.getHeight() / 1000f + .5);

        if(lastLink != null && currentPageG != null) {
            lastLink.setAttributeNS(null, "xlink:href", "#svgView(viewBox(0, "+ (totalHeight) + ", " + pageWidth + ", " + pageHeight + "))");
            currentPageG.appendChild(lastLink);
        }

        totalHeight += pageHeight;
        if(totalWidth < pageWidth) {
            totalWidth = pageWidth;
        }

        currentPageG = SVGUtilities.createG(svgDocument);
        currentPageG.setAttributeNS(null, "id", /*title + */"Page-" + pageNumber);
        currentPageG.setAttributeNS(null, "style", "font-family:sanserif;font-size:12");
        svgRoot.appendChild(currentPageG);

        drawFrame();

        renderPage(page);

        currentPageG.setAttributeNS(null, "transform", "translate(0," + (totalHeight - pageHeight) + ")");

        Element lastPageLink = svgDocument.createElementNS(svgNS, "a");
        if(lastLink != null) {
            lastPageLink.setAttributeNS(null, "xlink:href", "#svgView(viewBox(0, " + (totalHeight - pageHeight - lastHeight) + ", " + lastWidth + ", " + lastHeight + "))");
        } else {
            lastPageLink.setAttributeNS(null, "xlink:href", "#svgView(viewBox(0, " + (totalHeight - pageHeight) + ", " + pageWidth + ", " + pageHeight + "))");
        }
        currentPageG.appendChild(lastPageLink);
        Element rect = SVGUtilities.createRect(svgDocument, 0, 0, pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastPageLink.appendChild(rect);

        lastLink = svgDocument.createElementNS(svgNS, "a");
        rect = SVGUtilities.createRect(svgDocument, pageWidth / 2, 0, pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastLink.appendChild(rect);

        /*
         * if (page.hasLinks()) {
         * ....
         * }
         */

    }

    public void renderPage(Page page) {

        this.currentFontName = "";
        this.currentFontSize = 0;

        renderRegions(page);
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
            addLine(rx, ry, rx + w, ry, area.getBorderTopWidth(),
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
                    area.getBorderRightWidth(), borderColor.red(),
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

    public void setupFontInfo(FontInfo fontInfo) throws FOPException {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        org.apache.fop.render.awt.FontSetup.setup(fontInfo, fontImage.createGraphics());
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

    public void renderImageArea(ImageArea area) {

        int x = currentXPosition + area.getXOffset();
        int y = currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();
        this.currentYPosition -= h;

        FopImage img = area.getImage();

        if (img == null) {
            log.error("Error while loading image : area.getImage() is null");

            addRect(x, y, w, h, true);    // use helper function

        } else {
            if (img instanceof SVGImage) {
                try {
                    SVGDocument svg = ((SVGImage)img).getSVGDocument();
                    renderSVGDocument(svg, x / 1000f, pageHeight - y / 1000f);
                } catch (FopImageException e) {}

            } else {

                String urlString = img.getURL();
                try {
                    URL url = new URL(urlString);

                    ImageIcon icon = new ImageIcon(url);
                    Image image = icon.getImage();

                    int startx = (x + 500) / 1000;
                    int starty = pageHeight - ((y + 500) / 1000);
                    int endx = (x + w + 500) / 1000;
                    int endy = pageHeight - ((y + h + 500) / 1000);

                    // reverse start and end y because h is positive
                    //graphics.drawImage(image, startx, starty, endx - startx,
                    //                   starty - endy, null);

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

        String name = area.getFontState().getFontFamily();
        int size = area.getFontState().getFontSize();
        boolean underlined = area.getUnderlined();

        float red = area.getRed();
        float green = area.getGreen();
        float blue = area.getBlue();

        if ((!name.equals(this.currentFontName))
                || (size != this.currentFontSize)) {
            this.currentFontName = name;
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

        if (saveColor != null) {
            if (saveColor.getRed() != red || saveColor.getGreen() != green
                    || saveColor.getBlue() != blue) {
                saveColor = new Color(red, green, blue);
            }
        } else {
            saveColor = new Color(red, green, blue);
        }

        Element text = SVGUtilities.createText(svgDocument, rx / 1000f, pageHeight - bl / 1000f, s);
        String st = null;
        if(!"sans-serif".equals(this.currentFontName)) {
            st = "font-family:" + this.currentFontName;
        }
        if(this.currentFontSize != 12000) {
            if(st == null) {
                st = "";
            } else {
                st += ";";
            }
            st += "font-size:" + (this.currentFontSize / 1000f);
        }
        if(red != 0 || green != 0 || blue != 0) {
            if(st == null) {
                st = "";
            } else {
                st += ";";
            }
            st += "fill:rgb(" + ((int)(255 * red)) + "," + ((int)(255 * green)) + "," + ((int)(255 * blue)) + ")";
        }
        String fweight = area.getFontState().getFontWeight();
        if(!"normal".equals(fweight)) {
            if(st == null) {
                st = "";
            } else {
                st += ";";
            }
            st += "font-weight:" + fweight;
        }
        String fstyle = area.getFontState().getFontStyle();
        if(!"normal".equals(fstyle)) {
            if(st == null) {
                st = "";
            } else {
                st += ";";
            }
            st += "font-style:" + fstyle;
        }

        if(st != null) {
            text.setAttributeNS(null, "style", st);
        }
        currentPageG.appendChild(text);

        this.currentXPosition += area.getContentWidth();
    }

    public void renderInlineSpace(InlineSpace space) {
        this.currentXPosition += space.getSize();
    }

    /**
     *
     * @param area area to render
     */
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

        //graphics.setColor(new Color(r, g, b));

        addRect(rx, ry, w, th, false);

        this.currentXPosition += area.getContentWidth();
    }

    public void renderSVGArea(SVGArea area) {

        float x = this.currentXPosition / 1000f;
        float y = pageHeight - this.currentYPosition / 1000f;
        int w = area.getContentWidth();
        int h = area.getHeight();

        Document doc = area.getSVGDocument();
        renderSVGDocument(doc, x, y);
        this.currentXPosition += area.getContentWidth();
    }

    protected void renderSVGDocument(Document doc, float x, float y) {
        SVGSVGElement svg = ((SVGDocument)doc).getRootElement();
        Element view = svgDocument.createElementNS(svgNS, "svg");
        Node newsvg = svgDocument.importNode(svg, true);
        //view.setAttributeNS(null, "viewBox", "0 0 ");
        view.setAttributeNS(null, "x", "" + x);
        view.setAttributeNS(null, "y", "" + y);

        // this fixes a problem where the xmlns is repeated sometimes
        Element ele = (Element)newsvg;
        ele.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns", svgNS);
        if(ele.hasAttributeNS(null, "xmlns")) {
           ele.removeAttributeNS(null, "xmlns");
        }

        view.appendChild(newsvg);
        currentPageG.appendChild(view);
    }

    public void setProducer(String producer) {
        // defined in Renderer Interface
    }

    public static Color colorType2Color(ColorType ct) {
        if (ct == null) {
            return null;
        }
        return new Color(ct.red(), ct.green(), ct.blue());
    }

    public void renderForeignObjectArea(ForeignObjectArea area) {
        area.getObject().render(this);
    }

    public void startRenderer(OutputStream outputStream)
              throws IOException {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        svgDocument = impl.createDocument(svgNS, "svg", null);
        ProcessingInstruction pi =
                 svgDocument.createProcessingInstruction(
                         "xml",
                         " version=\"1.0\" encoding=\"ISO-8859-1\"");
                svgRoot = svgDocument.getDocumentElement();
        svgDocument.insertBefore(pi, svgRoot);
        }

    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        svgRoot.setAttributeNS(null, "width", "" + totalWidth);
        svgRoot.setAttributeNS(null, "height", "" + totalHeight);
        //svgRoot.setAttributeNS(null, "viewBox", "0 0 " + pageWidth + " " + pageHeight);
        SVGTranscoder svgT = new SVGTranscoder();
        TranscoderInput input = new TranscoderInput(svgDocument);
        TranscoderOutput output = new TranscoderOutput(new OutputStreamWriter(outputStream));
        try {
            svgT.transcode(input, output);
        } catch(TranscoderException e) {
            log.error("could not write svg file :" + e.getMessage(), e);
        }
        outputStream.flush();

        svgDocument = null;
        svgRoot = null;
        currentPageG = null;
        lastLink = null;

        totalWidth = 0;
        totalHeight = 0;

        pageNumber = 0;
    }

}
