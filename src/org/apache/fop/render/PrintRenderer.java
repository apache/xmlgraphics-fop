/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

// PrintRenderer is an abstract base class for renderers that produce printed type output.
// Subclasses would be PDFRenderer, PCLRenderer and similar renderers.

package org.apache.fop.render;

// FOP
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.apps.FOPException;

import org.apache.fop.svg.SVGArea;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract base class of "Print" type renderers.
 * 
 * Modified by Mark Lillywhite mark-fop@inomial.com. Removed
 * the render(AreaTree, OutputStream) method, and added
 * no-op concrete implementation of startRenderer() and
 * stopRenderer(). The method render(Page, OutputStream)
 * is not mentioned in this class but it is inherited from
 * the Renderer interface.
 */
public abstract class PrintRenderer extends AbstractRenderer {
    // vvv These are not currently referenced by the PrintRenderer, but are common to PCL and PDF renderers - so declare here.

    /**
     * the current (internal) font name
     */
    protected String currentFontName;

    /**
     * the current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * the current color/gradient for borders, letters, etc.
     */
    protected PDFPathPaint currentStroke = null;

    /**
     * the current color/gradient to fill shapes with
     */
    protected PDFPathPaint currentFill = null;

    /**
     * the current colour's red component
     */
    // protected float currentRed = 0;

    /**
     * the current colour's green component
     */
    // protected float currentGreen = 0;

    /**
     * the current colour's blue component
     */
    // protected float currentBlue = 0;
    // ^^^

    // previous values used for text-decoration drawing
    protected int prevUnderlineXEndPos;
    protected int prevUnderlineYEndPos;
    protected int prevUnderlineSize;
    protected PDFColor prevUnderlineColor;
    protected int prevOverlineXEndPos;
    protected int prevOverlineYEndPos;
    protected int prevOverlineSize;
    protected PDFColor prevOverlineColor;
    protected int prevLineThroughXEndPos;
    protected int prevLineThroughYEndPos;
    protected int prevLineThroughSize;
    protected PDFColor prevLineThroughColor;

    protected FontInfo fontInfo;

    /**
     * the IDReferences for this document
     */
    protected IDReferences idReferences;

    /**
     * set the document's producer
     * 
     * @param producer string indicating application producing PDF
     */
    public abstract void setProducer(String producer);

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
    protected abstract void addLine(int x1, int y1, int x2, int y2, int th,
                                    PDFPathPaint stroke);

    /**
     * add a line to the current stream
     * 
     * @param x1 the start x location in millipoints
     * @param y1 the start y location in millipoints
     * @param x2 the end x location in millipoints
     * @param y2 the end y location in millipoints
     * @param th the thickness in millipoints
     * @param rs the rule style
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     */
    protected abstract void addLine(int x1, int y1, int x2, int y2, int th,
                                    int rs, PDFPathPaint stroke);

    /**
     * add a rectangle to the current stream
     * 
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param stroke the stroke color/gradient
     */
    protected abstract void addRect(int x, int y, int w, int h,
                                    PDFPathPaint stroke);

    /**
     * add a filled and stroked rectangle to the current stream
     * 
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param fill the fill color/gradient
     * @param stroke the stroke color/gradient
     */
    protected abstract void addRect(int x, int y, int w, int h,
                                    PDFPathPaint stroke, PDFPathPaint fill);

    /**
     * Add a filled rectangle to the current stream
     * This default implementation calls addRect
     * using the same color for fill and border.
     * 
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param fill the fill color/gradient
     */
    protected void addFilledRect(int x, int y, int w, int h,
                                 PDFPathPaint fill) {
        addRect(x, y, w, h, fill, fill);
    }

    protected void addFilledRect(int x, int y, int w, int h,
                                 ColorType col) {
        PDFColor pdfcol = new PDFColor(col);
        addRect(x, y, w, h, pdfcol, pdfcol);
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

        // rx = rx - area.getBorderLeftWidth();
        // ry = ry + area.getBorderTopWidth();
        // w = w + area.getBorderLeftWidth() + area.getBorderRightWidth();
        // h = h + area.getBorderTopWidth() + area.getBorderBottomWidth();

        // Handle line style
        // Offset for haft the line width!
        BorderAndPadding bp = area.getBorderAndPadding();
        // int left = rx - area.getBorderLeftWidth() / 2;
        // int right = rx + w + area.getBorderRightWidth() / 2;
        // int top = ry + area.getBorderTopWidth() / 2;
        // int bottom = ry - h - area.getBorderBottomWidth() / 2;
        // if (area.getBorderTopWidth() != 0)
        // addLine(left, top, right, top, area.getBorderTopWidth(),
        // new PDFColor(bp.getBorderColor(BorderAndPadding.TOP)));
        // if (area.getBorderLeftWidth() != 0)
        // addLine(left, ry + area.getBorderTopWidth(), left, bottom, area.getBorderLeftWidth(),
        // new PDFColor(bp.getBorderColor(BorderAndPadding.LEFT)));
        // if (area.getBorderRightWidth() != 0)
        // addLine(right, ry + area.getBorderTopWidth(), right, bottom, area.getBorderRightWidth(),
        // new PDFColor(bp.getBorderColor(BorderAndPadding.RIGHT)));
        // if (area.getBorderBottomWidth() != 0)
        // addLine(rx - area.getBorderLeftWidth(), bottom, rx + w + area.getBorderRightWidth(), bottom, area.getBorderBottomWidth(),
        // new PDFColor(bp.getBorderColor(BorderAndPadding.BOTTOM)));
        // Try using rectangles instead of lines. Line style will be a
        // problem though?
        int left = area.getBorderLeftWidth();
        int right = area.getBorderRightWidth();
        int top = area.getBorderTopWidth();
        int bottom = area.getBorderBottomWidth();
        // If style is solid, use filled rectangles
        if (top != 0)
            addFilledRect(rx, ry, w, top,
                          new PDFColor(bp.getBorderColor(BorderAndPadding.TOP)));
        if (left != 0)
            addFilledRect(rx - left, ry - h - bottom, left, h + top + bottom,
                          new PDFColor(bp.getBorderColor(BorderAndPadding.LEFT)));
        if (right != 0)
            addFilledRect(rx + w, ry - h - bottom, right, h + top + bottom,
                          new PDFColor(bp.getBorderColor(BorderAndPadding.RIGHT)));
        if (bottom != 0)
            addFilledRect(rx, ry - h - bottom, w, bottom,
                          new PDFColor(bp.getBorderColor(BorderAndPadding.BOTTOM)));
    }

    /**
     * render display space
     * 
     * @param space the display space to render
     */
    public void renderDisplaySpace(DisplaySpace space) {
        int d = space.getSize();
        this.currentYPosition -= d;
    }

    /**
     * render a foreign object area
     */
    public abstract void renderForeignObjectArea(ForeignObjectArea area);

    /**
     * render SVG area
     * 
     * @param area the SVG area to render
     */
    public abstract void renderSVGArea(SVGArea area);

    /**
     * render inline area
     * 
     * @param area inline area to render
     */
    public abstract void renderWordArea(WordArea area);

    protected void addWordLines(WordArea area, int rx, int bl, int size,
                                PDFColor theAreaColor) {
        if (area.getUnderlined()) {
            int yPos = bl - size / 10;
            addLine(rx, yPos, rx + area.getContentWidth(), yPos, size / 14,
                    theAreaColor);
            // save position for underlining a following InlineSpace
            prevUnderlineXEndPos = rx + area.getContentWidth();
            prevUnderlineYEndPos = yPos;
            prevUnderlineSize = size / 14;
            prevUnderlineColor = theAreaColor;
        }

        if (area.getOverlined()) {
            int yPos = bl + area.getFontState().getAscender() + size / 10;
            addLine(rx, yPos, rx + area.getContentWidth(), yPos, size / 14,
                    theAreaColor);
            prevOverlineXEndPos = rx + area.getContentWidth();
            prevOverlineYEndPos = yPos;
            prevOverlineSize = size / 14;
            prevOverlineColor = theAreaColor;
        }

        if (area.getLineThrough()) {
            int yPos = bl + area.getFontState().getAscender() * 3 / 8;
            addLine(rx, yPos, rx + area.getContentWidth(), yPos, size / 14,
                    theAreaColor);
            prevLineThroughXEndPos = rx + area.getContentWidth();
            prevLineThroughYEndPos = yPos;
            prevLineThroughSize = size / 14;
            prevLineThroughColor = theAreaColor;
        }
    }

    /**
     * render inline space
     * 
     * @param space space to render
     */
    public void renderInlineSpace(InlineSpace space) {
        this.currentXPosition += space.getSize();
        if (space.getUnderlined()) {
            if (prevUnderlineColor != null) {
                addLine(prevUnderlineXEndPos, prevUnderlineYEndPos,
                        prevUnderlineXEndPos + space.getSize(),
                        prevUnderlineYEndPos, prevUnderlineSize,
                        prevUnderlineColor);
                // save position for a following InlineSpace
                prevUnderlineXEndPos = prevUnderlineXEndPos + space.getSize();
            }
        }
        if (space.getOverlined()) {
            if (prevOverlineColor != null) {
                addLine(prevOverlineXEndPos, prevOverlineYEndPos,
                        prevOverlineXEndPos + space.getSize(),
                        prevOverlineYEndPos, prevOverlineSize,
                        prevOverlineColor);
                prevOverlineXEndPos = prevOverlineXEndPos + space.getSize();
            }
        }
        if (space.getLineThrough()) {
            if (prevLineThroughColor != null) {
                addLine(prevLineThroughXEndPos, prevLineThroughYEndPos,
                        prevLineThroughXEndPos + space.getSize(),
                        prevLineThroughYEndPos, prevLineThroughSize,
                        prevLineThroughColor);
                prevLineThroughXEndPos = prevLineThroughXEndPos + space.getSize();
            }
        }
    }

    /**
     * render page
     * 
     * @param page page to render
     */
    public abstract void renderPage(Page page);

    /**
     * render leader area
     * 
     * @param area area to render
     */
    public void renderLeaderArea(LeaderArea area) {
        int rx = this.currentXPosition;
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();
        int th = area.getRuleThickness();
        int st = area.getRuleStyle();

        // checks whether thickness is = 0, because of bug in pdf (or where?),
        // a line with thickness 0 is still displayed
        if (th != 0) {
            switch (st) {
            case org.apache.fop.fo.properties.RuleStyle.DOUBLE:
                addLine(rx, ry, rx + w, ry, th / 3, st,
                        new PDFColor(area.getRed(), area.getGreen(),
                                     area.getBlue()));
                addLine(rx, ry + (2 * th / 3), rx + w, ry + (2 * th / 3),
                        th / 3, st,
                        new PDFColor(area.getRed(), area.getGreen(),
                                     area.getBlue()));
                break;
            case org.apache.fop.fo.properties.RuleStyle.GROOVE:
                addLine(rx, ry, rx + w, ry, th / 2, st,
                        new PDFColor(area.getRed(), area.getGreen(),
                                     area.getBlue()));
                addLine(rx, ry + (th / 2), rx + w, ry + (th / 2), th / 2, st,
                        new PDFColor(255, 255, 255));
                break;
            case org.apache.fop.fo.properties.RuleStyle.RIDGE:
                addLine(rx, ry, rx + w, ry, th / 2, st,
                        new PDFColor(255, 255, 255));
                addLine(rx, ry + (th / 2), rx + w, ry + (th / 2), th / 2, st,
                        new PDFColor(area.getRed(), area.getGreen(),
                                     area.getBlue()));
                break;
            default:
                addLine(rx, ry, rx + w, ry, th, st,
                        new PDFColor(area.getRed(), area.getGreen(),
                                     area.getBlue()));
            }
            this.currentXPosition += area.getContentWidth();
            this.currentYPosition += th;
        }
    }

    /**
     * set up the font info
     * 
     * @param fontInfo font info to set up
     */
    public void setupFontInfo(FontInfo fontInfo) throws FOPException {
        this.fontInfo = fontInfo;
        FontSetup.setup(fontInfo);
    }

    /**
      Default start renderer method. This would
      normally be overridden. (mark-fop@inomial.com).
    */
    public void startRenderer(OutputStream outputStream)
    throws IOException {}

    /**
     Default stop renderer method. This would
     normally be overridden. (mark-fop@inomial.com).
    */
    public void stopRenderer(OutputStream outputStream)
    throws IOException
    {
        this.idReferences = null;
        currentFontName = "";
        currentStroke = null;
        currentFill = null;
        prevUnderlineColor = null;
        prevOverlineColor = null;
        prevLineThroughColor = null;
        fontInfo = null;
        this.idReferences = null;
    }

}
