/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// Java
import java.awt.Rectangle;

import org.apache.fop.layout.inline.InlineArea;

/**
 * an object that stores a rectangle that is linked, and the LineArea
 * that it is logically associated with
 * @author Arved Sandstrom
 * @author James Tauber
 */
public class LinkedRectangle {

    /**
     * the linked Rectangle
     */
    protected Rectangle link;

    /**
     * the associated LineArea
     */
    protected LineArea lineArea;

    /**
     * the associated InlineArea
     */
    protected InlineArea inlineArea;

    public LinkedRectangle(Rectangle link, LineArea lineArea,
                           InlineArea inlineArea) {
        this.link = link;
        this.lineArea = lineArea;
        this.inlineArea = inlineArea;
    }

    public LinkedRectangle(LinkedRectangle lr) {
        this.link = new Rectangle(lr.getRectangle());
        this.lineArea = lr.getLineArea();
        this.inlineArea = lr.getInlineArea();
    }

    public void setRectangle(Rectangle link) {
        this.link = link;
    }

    public Rectangle getRectangle() {
        return this.link;
    }

    public LineArea getLineArea() {
        return this.lineArea;
    }

    public void setLineArea(LineArea lineArea) {
        this.lineArea = lineArea;
    }

    public InlineArea getInlineArea() {
        return this.inlineArea;
    }

    public void setLineArea(InlineArea inlineArea) {
        this.inlineArea = inlineArea;
    }

    public void setX(int x) {
        this.link.x = x;
    }

    public void setY(int y) {
        this.link.y = y;
    }

    public void setWidth(int width) {
        this.link.width = width;
    }

    public void setHeight(int height) {
        this.link.height = height;
    }

    public int getX() {
        return this.link.x;
    }

    public int getY() {
        return this.link.y;
    }

    public int getWidth() {
        return this.link.width;
    }

    public int getHeight() {
        return this.link.height;
    }

}
