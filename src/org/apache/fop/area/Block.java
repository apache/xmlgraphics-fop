/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;

// block areas hold either more block areas or line
// areas can also be used as a block spacer
// a block area may have children positioned by stacking
// or by relative to the parent for floats, tables and lists
// cacheable object
// has id information

/**
 * This is the block area class.
 * It holds child block areas such as other blocks or lines.
 */
public class Block extends BlockParent {
    /**
     * Normally stacked with other blocks.
     */
    public static final int STACK = 0;

    /**
     * Placed relative to the flow position.
     * This effects the flow placement of stacking normally.
     */
    public static final int RELATIVE = 1;

    /**
     * Relative to the block parent but not effecting the stacking
     * Used for block-container, tables and lists.
     */
    public static final int ABSOLUTE = 2;

    private int stacking = TB;
    private int positioning = STACK;

    // a block with may contain the dominant styling info in
    // terms of most lines or blocks with info

    /**
     * Add the block to this block area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        }
        height += block.getHeight();
        children.add(block);
    }

    /**
     * Add the line area to this block area.
     *
     * @param line the line area to add
     */
    public void addLineArea(LineArea line) {
        if (children == null) {
            children = new ArrayList();
        }
        height += line.getHeight();
        children.add(line);
    }

    /**
     * Set the positioning of this area.
     *
     * @param pos the positioning to use when rendering this area
     */
    public void setPositioning(int pos) {
        positioning = pos;
    }

    /**
     * Get the positioning of this area.
     *
     * @return the positioning to use when rendering this area
     */
    public int getPositioning() {
        return positioning;
    }

}

