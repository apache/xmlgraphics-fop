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

// block areas hold either more block areas or line
// areas can also be used as a block spacer
// a block area may have children positioned by stacking
// or by relative to the parent for floats, tables and lists
// cacheable object
// has id information
public class Block extends Area implements Serializable {
    // normally stacked with other blocks
    public static final int STACK = 0;
    // placed relative to the parent area
    public static final int RELATIVE = 1;
    // placed relative to the page or viewport
    public static final int ABSOLUTE = 2;

    // this position is used for absolute position
    // or as an indent
    // this has the size in the block progression dimension
    Rectangle2D bounds = null;

    int stacking = TB;

    // list of marker fo objects that are associated with this area
    // if a retrieve marker resolves this area it will format the
    // available markers, markers are discarded once page complete
    private ArrayList markers = null;

    ArrayList children = null;
    boolean blocks = false;
    // a block with may contain the dominant styling info in
    // terms of most lines or blocks with info


    int positioning = STACK;

    // orientation if reference area
    int orientation = ORIENT_0;

    public void addBlock(Block block) {
        if (children == null) {
            children = new ArrayList();
        } else if (!blocks) {
            // error
        }
        blocks = true;
        children.add(block);
    }

    public void addLineArea(LineArea line) {
        if (children == null) {
            children = new ArrayList();
        } else if (blocks) {
            // error
        }
        children.add(line);

    }

    public boolean isChildrenBlocks() {
        return blocks;
    }

    public List getChildAreas() {
        return children;
    }

    public int getPositioning() {
        return positioning;
    }

    // store properties in array list, need better solution
    ArrayList props = null;

    public void addProperty(Property prop) {
        if (props == null) {
            props = new ArrayList();
        }
        props.add(prop);
    }

    public List getPropertyList() {
        return props;
    }
}
