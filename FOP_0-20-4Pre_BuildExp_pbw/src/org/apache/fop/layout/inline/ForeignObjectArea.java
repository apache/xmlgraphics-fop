/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.layout.*;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class ForeignObjectArea extends InlineArea {

    protected int xOffset = 0;
    /* text-align of contents */
    protected int align;
    /* vertical align of contents */
    protected int valign;
    /* scaling method */
    protected int scaling;
    protected Area foreignObject;
    /* height according to the instream-foreign-object */
    protected int cheight;
    /* width according to the instream-foreign-object */
    protected int cwidth;
    /* width of the content */
    protected int awidth;
    /* height of the content */
    protected int aheight;
    /* width */
    protected int width;
    boolean wauto;
    boolean hauto;
    boolean cwauto;
    boolean chauto;
    int overflow;

    public ForeignObjectArea(FontState fontState, int width) {
        super(fontState, width, 0, 0, 0);
    }

    /**
     * This is NOT the content width of the instream-foreign-object.
     * This is the content width for a Box.
     */
    public int getContentWidth() {
        return getEffectiveWidth();
    }

    /**
     * This is NOT the content height of the instream-foreign-object.
     * This is the content height for a Box.
     */
    public int getHeight() {
        return getEffectiveHeight();
    }

    public int getXOffset() {
        return this.xOffset;
    }

    public void setStartIndent(int startIndent) {
        xOffset = startIndent;
    }

    public void setObject(Area fobject) {
        foreignObject = fobject;
    }

    public Area getObject() {
        return foreignObject;
    }

    public void setSizeAuto(boolean wa, boolean ha) {
        wauto = wa;
        hauto = ha;
    }

    public void setContentSizeAuto(boolean wa, boolean ha) {
        cwauto = wa;
        chauto = ha;
    }

    public boolean isContentWidthAuto() {
        return cwauto;
    }

    public boolean isContentHeightAuto() {
        return chauto;
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

    public void setOverflow(int o) {
        this.overflow = o;
    }

    public int getOverflow() {
        return this.overflow;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setContentHeight(int cheight) {
        this.cheight = cheight;
    }

    public void setContentWidth(int cwidth) {
        this.cwidth = cwidth;
    }

    public void setScaling(int scaling) {
        this.scaling = scaling;
    }

    public int scalingMethod() {
        return this.scaling;
    }

    public void setIntrinsicWidth(int w) {
        awidth = w;
    }

    public void setIntrinsicHeight(int h) {
        aheight = h;
    }

    public int getIntrinsicHeight() {
        return aheight;
    }

    public int getIntrinsicWidth() {
        return awidth;
    }

    public int getEffectiveHeight() {
        if (this.hauto) {
            if (this.chauto) {
                return aheight;
            } else {
                // need to handle percentages, this would be a scaling factor on the
                // instrinsic height (content determined height)
                // if(this.properties.get("content-height").getLength().isPercentage()) {
                // switch(scaling) {
                // case Scaling.UNIFORM:
                // break;
                // case Scaling.NON_UNIFORM:
                // break;
                // }
                // } else {
                return this.cheight;
            }
        } else {
            return this.height;
        }
    }

    public int getEffectiveWidth() {
        if (this.wauto) {
            if (this.cwauto) {
                return awidth;
            } else {
                // need to handle percentages, this would be a scaling factor on the
                // instrinsic height (content determined height)
                // if(this.properties.get("content-width").getLength().isPercentage()) {
                // switch(scaling) {
                // case Scaling.UNIFORM:
                // break;
                // case Scaling.NON_UNIFORM:
                // break;
                // }
                // } else {
                return this.cwidth;
            }
        } else {
            return this.width;
        }
    }

}
