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
    Rectangle2D bounds = null;

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

}
