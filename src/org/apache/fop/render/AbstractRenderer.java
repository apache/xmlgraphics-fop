/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render;

// FOP
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.ImageArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.render.pdf.FontSetup;

import org.apache.log.Logger;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Abstract base class for all renderers.
 * 
 */
public abstract class AbstractRenderer implements Renderer {
    protected Logger log;

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
    protected int currentAreaContainerXPosition = 0;

    public void setLogger(Logger logger) {
        log = logger;
    }

    public void renderSpanArea(SpanArea area) {
        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            b.render(this);    // column areas
        }

    }

    protected abstract void doFrame(Area area);

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
    protected abstract void addFilledRect(int x, int y, int w, int h,
                                 ColorType col);

    public void renderBodyAreaContainer(BodyAreaContainer area) {
        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust for negative postscript one
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        int w, h;
        int rx = this.currentAreaContainerXPosition;
        w = area.getContentWidth();
        h = area.getContentHeight();
        int ry = this.currentYPosition;
        ColorType bg = area.getBackgroundColor();

        // I'm not sure I should have to check for bg being null
        // but I do
        if ((bg != null) && (bg.alpha() == 0)) {
            addFilledRect(rx, ry, w, -h, bg);
        }

        // floats & footnotes stuff
        renderAreaContainer(area.getBeforeFloatReferenceArea());
        renderAreaContainer(area.getFootnoteReferenceArea());

        // main reference area
        Enumeration e = area.getMainReferenceArea().getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            b.render(this);    // span areas
        }


        if (area.getPosition() != Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else
            this.currentYPosition -= area.getHeight();

    }

    /**
     * render area container
     *
     * @param area the area container to render
     */
    public void renderAreaContainer(AreaContainer area) {

        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // XPosition and YPosition give the content rectangle position
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        } else if (area.getPosition() == Position.STATIC) {
            this.currentYPosition -= area.getPaddingTop()
                                     + area.getBorderTopWidth();
            /*
             * this.currentAreaContainerXPosition +=
             * area.getPaddingLeft() + area.getBorderLeftWidth();
             */
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        doFrame(area);

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            b.render(this);
        }
        // Restore previous origin
        this.currentYPosition = saveY;
        this.currentAreaContainerXPosition = saveX;
        if (area.getPosition() == Position.STATIC) {
            this.currentYPosition -= area.getHeight();
        }

        /**
         * **
         * if (area.getPosition() != Position.STATIC) {
         * this.currentYPosition = saveY;
         * this.currentAreaContainerXPosition = saveX;
         * } else
         * this.currentYPosition -= area.getHeight();
         * **
         */
    }

    /**
     * render block area
     *
     * @param area the block area to render
     */
    public void renderBlockArea(BlockArea area) {
        // KLease: Temporary test to fix block positioning
        // Offset ypos by padding and border widths
        this.currentYPosition -= (area.getPaddingTop()
                                  + area.getBorderTopWidth());
        doFrame(area);
        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            b.render(this);
        }
        this.currentYPosition -= (area.getPaddingBottom()
                                  + area.getBorderBottomWidth());
    }

    /**
     * render line area
     *
     * @param area area to render
     */
    public void renderLineArea(LineArea area) {
        int rx = this.currentAreaContainerXPosition + area.getStartIndent();
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= area.getPlacementOffset();
        this.currentXPosition = rx;

        int bl = this.currentYPosition;

        Enumeration e = area.getChildren().elements();
        while (e.hasMoreElements()) {
            Box b = (Box)e.nextElement();
            if (b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                this.currentYPosition = ry - ia.getYOffset();
            } else {
                this.currentYPosition = ry - area.getPlacementOffset();
            }
            b.render(this);
        }

        this.currentYPosition = ry - h;
        this.currentXPosition = rx;
    }
}
