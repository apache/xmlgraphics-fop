/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.render.Renderer;

public class InlineArea extends Area {
    int width;
    int height;
    // position within the line area, either top or baseline
    int verticalPosition;
    // width, height, vertical alignment

    // inline areas are expected to implement this method
    // to render themselves
    public void render(Renderer renderer) {

    }

    public void setWidth(int w) {
        width = w;
    }

    public int getWidth() {
        return width;
    }
}
