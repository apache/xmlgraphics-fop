/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.*;

public abstract class InlineArea extends Area {

    private int yOffset = 0;

    /**
     * amount of space added since the original layout - needed by links
     */
    private int xOffset = 0;
    protected int height = 0;
    private int verticalAlign = 0;
    protected String pageNumberId = null;
    private float red, green, blue;

    // Textdecoration
    protected boolean underlined = false;
    protected boolean overlined = false;
    protected boolean lineThrough = false;


    public InlineArea(FontState fontState, int width, float red, float green,
                      float blue) {
        super(fontState);
        this.contentRectangleWidth = width;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getGreen() {
        return this.green;
    }

    public float getRed() {
        return this.red;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return this.height;
    }

    public void setVerticalAlign(int align) {
        this.verticalAlign = align;
    }

    public int getVerticalAlign() {
        return this.verticalAlign;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public String getPageNumberID() {
        return pageNumberId;
    }

    public void setUnderlined(boolean ul) {
        this.underlined = ul;
    }

    public boolean getUnderlined() {
        return this.underlined;
    }

    public void setOverlined(boolean ol) {
        this.overlined = ol;
    }

    public boolean getOverlined() {
        return this.overlined;
    }

    public void setLineThrough(boolean lt) {
        this.lineThrough = lt;
    }

    public boolean getLineThrough() {
        return this.lineThrough;
    }

}
