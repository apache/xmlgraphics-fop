/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
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
 * Inline parent area.
 * This is an inline area that can have other inlines as children.
 */
public class InlineParent extends InlineArea {
    /**
     * The list of inline areas added to this inline parent.
     */
    protected ArrayList inlines = new ArrayList();

    /**
     * Create a new inline parent to add areas to.
     */
    public InlineParent() {
    }

    /**
     * Render this area.
     *
     * @param renderer the renderer to render this area in
     */
    public void render(Renderer renderer) {
        renderer.renderInlineParent(this);
    }

    /**
     * Override generic Area method.
     *
     * @param childArea the child area to add
     */
    public void addChild(Area childArea) {
        if (childArea instanceof InlineArea) {
            inlines.add(childArea);
            increaseIPD(((InlineArea) childArea).getAllocIPD());
        }
    }

    /**
     * Get the child areas for this inline parent.
     *
     * @return the list of child areas
     */
    public List getChildAreas() {
        return inlines;
    }

}
