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

// this is an inline area that can have other inlines as children
public class InlineParent extends InlineArea {
    ArrayList inlines = new ArrayList();
    int width;

    public InlineParent() {
    }

    public void render(Renderer renderer) {
        renderer.renderInlineParent(this);
    }

    public void addChild(InlineArea child) {
        inlines.add(child);
    }

    public List getChildAreas() {
        return inlines;
    }

    public int getWidth() {
        return width;
    }

}
