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
package org.apache.fop.render.mif;

// FOP
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.SpanArea;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.DisplaySpace;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.LineArea;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.layout.inline.ForeignObjectArea;
import org.apache.fop.layout.inline.WordArea;
import org.apache.fop.layout.inline.InlineArea;
import org.apache.fop.layout.inline.InlineSpace;
import org.apache.fop.layout.inline.LeaderArea;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.svg.SVGArea;
import org.apache.fop.mif.MIFDocument;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.SVGImage;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * Renders areas to MIF. Collects all the Pages and print them out at the end.
 * This means that the MIF renderer does not stream.
 *
 * @author Seshadri G
 * @author <a href="mailto:mark-fop@inomial.com">Mark Lillywhite</a>
 */
public class MIFRenderer extends AbstractRenderer {

    private String currentFontName;
    private String currentFontSize;
    private int pageHeight;
    private int pageWidth;

    /**
     * the current vertical position in millipoints from bottom
     */
    protected int currentYPosition = 0;

    /**
     * the current horizontal position in millipoints from left
     */
    protected int currentXPosition = 0;

    /**
     * the horizontal position of the current area container
     */
    private int currentAreaContainerXPosition = 0;


    /**
     * the MIF Document being created
     */
    protected MIFDocument mifDoc;


    /* is a table currently open? */
    private boolean inTable = false;

    /**
     * options
     */
    protected java.util.Map options;

    /**
     * create the MIF renderer
     */
    public MIFRenderer() {
        this.mifDoc = new MIFDocument();
    }

    /**
     * set up renderer options
     */
    public void setOptions(java.util.Map options) {
        this.options = options;
    }

    /**
     * set up the given FontInfo
     */
    public void setupFontInfo(FontInfo fontInfo) {

        FontSetup.setup(fontInfo);
        // FontSetup.addToFontFormat(this.mifDoc, fontInfo);

    }

    /**
     * set the producer of the rendering
     */
    public void setProducer(String producer) {}


    public void renderAreaContainer(AreaContainer area) {

        if (area.foCreator != null
                && area.foCreator.getName() == "fo:table") {

            this.mifDoc.createTable();
            this.inTable = true;
        } else if (area.foCreator != null
                   && area.foCreator.getName() == "fo:table-body") {

            this.mifDoc.setCurrent("fo:table-body");
        } else if (area.foCreator != null
                   && area.foCreator.getName() == "fo:table-column") {

            int colWidth =
                ((org.apache.fop.fo.flow.TableColumn)area.foCreator).getColumnWidth();
            this.mifDoc.setColumnProp(colWidth);
        } else if (area.foCreator != null
                   && area.foCreator.getName() == "fo:table-row") {

            this.mifDoc.startRow();
        } else if (area.foCreator != null
                   && area.foCreator.getName() == "fo:table-cell") {

            int rowSpan =
                ((org.apache.fop.fo.flow.TableCell)area.foCreator).getNumRowsSpanned();
            int colSpan =
                ((org.apache.fop.fo.flow.TableCell)area.foCreator).getNumColumnsSpanned();
            this.mifDoc.startCell(rowSpan, colSpan);
        } else if (inTable) {

            inTable = false;
            this.mifDoc.endTable();

        }
        super.renderAreaContainer(area);
    }

    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
    }

    protected void doFrame(Area area) {
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();

        if (area instanceof BlockArea)
            rx += ((BlockArea)area).getStartIndent();

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

        // Create a textrect with these dimensions.
        // The y co-ordinate is measured +ve downwards so subtract page-height

        this.mifDoc.setTextRectProp(rx, pageHeight - ry, w, h);

        /*
         * BorderAndPadding bp = area.getBorderAndPadding();
         * if (area.getBorderTopWidth() != 0)
         * addLine(rx, ry, rx + w, ry, area.getBorderTopWidth(),
         * new PDFColor(bp.getBorderColor(BorderAndPadding.TOP)));
         * if (area.getBorderLeftWidth() != 0)
         * addLine(rx, ry, rx, ry - h, area.getBorderLeftWidth(),
         * new PDFColor(bp.getBorderColor(BorderAndPadding.LEFT)));
         * if (area.getBorderRightWidth() != 0)
         * addLine(rx + w, ry, rx + w, ry - h, area.getBorderRightWidth(),
         * new PDFColor(bp.getBorderColor(BorderAndPadding.RIGHT)));
         * if (area.getBorderBottomWidth() != 0)
         * addLine(rx, ry - h, rx + w, ry - h, area.getBorderBottomWidth(),
         * new PDFColor(bp.getBorderColor(BorderAndPadding.BOTTOM)));
         */
    }

    public void renderSpanArea(SpanArea area) {
        // A span maps to a textframe
        this.mifDoc.createTextRect(area.getColumnCount());
        super.renderSpanArea(area);
    }

    /**
     * render the given block area
     */
    public void renderBlockArea(BlockArea area) {
        this.mifDoc.setBlockProp(area.getStartIndent(), area.getEndIndent());
        super.renderBlockArea(area);
    }

    /**
     * render the given display space
     */
    public void renderDisplaySpace(DisplaySpace space) {
        int d = space.getSize();
        this.currentYPosition -= d;
    }

    /**
     * render the given SVG area
     */
    public void renderSVGArea(SVGArea area) {}

    /**
     * render a foreign object area
     */
    public void renderForeignObjectArea(ForeignObjectArea area) {}

    public void renderWordArea(WordArea area) {
        String s;
        s = area.getText();
        this.mifDoc.addToStream(s);

        this.currentXPosition += area.getContentWidth();
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

    /**
     * render the given image area
     */
    public void renderImageArea(ImageArea area) {

        int x = this.currentAreaContainerXPosition + area.getXOffset();
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= h;

        FopImage img = area.getImage();
        if (img instanceof SVGImage) {
            /*
             * try {
             * SVGSVGElement svg =
             * ((SVGImage) img).getSVGDocument().getRootElement();
             * currentStream.add("ET\nq\n" + (((float) w) / 1000f) +
             * " 0 0 " + (((float) h) / 1000f) + " " +
             * (((float) x) / 1000f) + " " +
             * (((float)(y - h)) / 1000f) + " cm\n");
             * //        renderSVG(svg, (int) x, (int) y);
             * currentStream.add("Q\nBT\n");
             * } catch (FopImageException e) {
             * }
             */

            log.warn("SVG images not supported in this version");
        } else {
            String url = img.getURL();
            this.mifDoc.addImage(url, x, pageHeight - y, w, h);

        }
    }

    /**
     * render the given inline area
     */
    public void renderInlineArea(InlineArea area) {}

    /**
     * render the given inline space
     */
    public void renderInlineSpace(InlineSpace space) {

        // I dont need the size of space! I just need to
        // leave a blank space each time
        String s = " ";
        this.mifDoc.addToStream(s);    // cool!
        this.currentXPosition += space.getSize();
    }

    /**
     * render the given line area
     */
    public void renderLineArea(LineArea area) {
        // The start of a new linearea corresponds to a new para in FM
        this.mifDoc.startLine();
        super.renderLineArea(area);
    }

    /**
     * render the given page
     */
    public void renderPage(Page page) {

        AreaContainer before, after;
        BodyAreaContainer body;
        body = page.getBody();
        before = page.getBefore();
        after = page.getAfter();

        this.currentFontName = "";
        this.currentFontSize = "0";

        pageHeight = page.getHeight();
        pageWidth = page.getWidth();
        this.mifDoc.setDocumentHeightWidth(pageHeight, pageWidth);

        this.mifDoc.createPage();

        body.render(this);


        // If the area is an instance of anything other than body, it goes into the
        // corresponding master page.


        if (before != null) {

            this.mifDoc.createTextRect(1);    // Create a rect with one col
            before.render(this);
        }

        if (after != null) {

            this.mifDoc.createTextRect(1);    // Create a rect with one col
            after.render(this);
        }

    }

    /**
     * render the given leader area
     */
    public void renderLeaderArea(LeaderArea area) {}

    /**
      Default start renderer method. This would
      normally be overridden. (mark-fop@inomial.com).
    */
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.info("rendering areas to MIF");
    }

    /**
      Default stop renderer method. This would
      normally be overridden. (mark-fop@inomial.com)
    */
    public void stopRenderer(OutputStream outputStream)
    throws IOException {
        log.info("writing out MIF");
        this.mifDoc.output(outputStream);
        outputStream.flush();
    }

    public void render(Page page, OutputStream outputStream) {
        this.renderPage(page);
    }
}

