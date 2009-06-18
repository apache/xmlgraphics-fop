/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.render.Renderer;

public class DisplaySpace extends Space {
    private int size;

    public DisplaySpace(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

}
