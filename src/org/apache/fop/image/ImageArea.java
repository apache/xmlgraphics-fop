/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;

import org.apache.fop.render.Renderer;

import java.util.Vector;
import java.util.Enumeration;

public class ImageArea extends InlineArea {

    protected int xOffset = 0;
    protected int align;
    protected int valign;
    protected FopImage image;


    public ImageArea(FontState fontState, FopImage img, int AllocationWidth,
                     int width, int height, int startIndent, int endIndent,
                     int align) {
        super(fontState, width, 0, 0, 0);
        this.currentHeight = height;
        this.contentRectangleWidth = width;
        this.height = height;
        this.image = img;
        this.align = align;

        /*
         * switch (align) {
         * case TextAlign.START:
         * xOffset = startIndent;
         * break;
         * case TextAlign.END:
         * if (endIndent == 0)
         * endIndent = AllocationWidth;
         * xOffset = (endIndent - width);
         * break;
         * case TextAlign.JUSTIFY:
         * xOffset = startIndent;
         * break;
         * case TextAlign.CENTER:
         * if (endIndent == 0)
         * endIndent = AllocationWidth;
         * xOffset = startIndent + ((endIndent - startIndent) - width)/2;
         * break;
         * }
         */
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public FopImage getImage() {
        return this.image;
    }

    public int getImageHeight() {
        return currentHeight;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public int getAlign() {
        return this.align;
    }

    public void setVerticalAlign(int align) {
        this.valign = align;
    }

    public int getVerticalAlign() {
        return this.valign;
    }

    public void setStartIndent(int startIndent) {
        xOffset = startIndent;
    }



}


