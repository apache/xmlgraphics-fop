/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A BlockParent holds block-level areas.
 */
public class BlockParent extends Area implements Serializable {

    // this position is used for absolute position
    // or as an indent
    // this has the size in the block progression dimension

    /**
     * The x offset position of this block parent.
     * Used for relative and absolute positioning.
     */
    protected int xOffset = 0;

    /**
     * The y offset position of this block parent.
     * Used for relative and absolute positioning.
     */
    protected int yOffset = 0;

    /**
     * The width of this block parent.
     */
    protected int width = 0;

    /**
     * The height of this block parent.
     */
    protected int height = 0;

    /**
     * The children of this block parent area.
     */
    protected ArrayList children = null;

    // orientation if reference area
    private int orientation = ORIENT_0;

    /**
     * Add the block area to this block parent.
     *
     * @param block the child block area to add
     */
    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(block);
    }

    /**
     * Get the list of child areas for this block area.
     *
     * @return the list of child areas
     */
    public List getChildAreas() {
        return children;
    }

    /**
     * Set the X offset of this block parent area.
     *
     * @param off the x offset of the block parent area
     */
    public void setXOffset(int off) {
        xOffset = off;
    }

    /**
     * Set the Y offset of this block parent area.
     *
     * @param off the y offset of the block parent area
     */
    public void setYOffset(int off) {
        yOffset = off;
    }

    /**
     * Set the width of this block parent area.
     *
     * @param w the width of the area
     */
    public void setWidth(int w) {
        width = w;
    }

    /**
     * Set the height of this block parent area.
     *
     * @param h the height of the block parent area
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Get the X offset of this block parent area.
     *
     * @return the x offset of the block parent area
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Get the Y offset of this block parent area.
     *
     * @return the y offset of the block parent area
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Get the width of this block parent area.
     *
     * @return the width of the area
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of this block parent area.
     *
     * @return the height of the block parent area
     */
    public int getHeight() {
        return height;
    }

}
