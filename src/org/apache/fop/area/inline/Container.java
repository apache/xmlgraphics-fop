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

// this is an inline area that can have blocks as children
public class Container extends Area {
    ArrayList blocks = new ArrayList();
    int width;

    public Container() {
    }

    public void render(Renderer renderer) {
        renderer.renderContainer(this);
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public List getBlocks() {
        return blocks;
    }

    public int getWidth() {
        return width;
    }

}
