/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

/**
 * Inline space area.
 * This is used for adding a inline space to the output.
 */
public class Space extends InlineArea {

    /**
     * Render this inlien space area.
     *
     * @param renderer the renderer to render this inline area
     */
    public void render(Renderer renderer) {
        renderer.renderInlineSpace(this);
    }
}
