/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.render.Renderer;

import java.util.List;
import java.util.ArrayList;

/**
 * Container area for inline container.
 * This area should be placed in a viewport as a result of the
 * inline container formatting object.
 * This allows an inline area to have blocks as children.
 */
public class Container extends Area {
    /**
     * The list of block areas stacked inside this container
     */
    protected List blocks = new ArrayList();

    /**
     * The width of this container
     */
    protected int width;

    /**
     * Create a new container area
     */
    public Container() {
    }

    /**
     * Add the block to this area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        blocks.add(block);
    }

    /**
     * Get the block areas stacked inside this container area.
     *
     * @return the list of block areas
     */
    public List getBlocks() {
        return blocks;
    }

    /**
     * Get the width of this container area.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }
}

