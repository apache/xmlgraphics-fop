/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

public class Space extends Stretch {
    public boolean collapse = true;
    public boolean fixed = false;

    public void render(Renderer renderer) {
        renderer.renderInlineSpace(this);
    }
}
