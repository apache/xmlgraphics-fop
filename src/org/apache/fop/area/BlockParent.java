/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Rectangle2D;

/**
 * A BlockParent holds block-level areas.
 */
public class BlockParent extends Area implements Serializable {

    // this position is used for absolute position
    // or as an indent
    // this has the size in the block progression dimension
    protected int xOffset = 0;
    protected int yOffset = 0;
    protected int width = 0;
    protected int height = 0;

    ArrayList children = null;

    // orientation if reference area
    int orientation = ORIENT_0;

    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(block);
    }

    public List getChildAreas() {
        return children;
    }

    public void setXOffset(int off) {
        xOffset = off;
    }

    public void setYOffset(int off) {
        yOffset = off;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
