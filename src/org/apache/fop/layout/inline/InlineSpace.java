/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.layout.*;

public class InlineSpace extends Space {
    private int size;    // in millipoints
    private boolean resizeable =
        true;            // to disallow size changes during justification of a line
    // Used to discard some pending spaces in LineArea
    private boolean eatable = false;

    // Textdecoration
    protected boolean underlined = false;
    protected boolean overlined = false;
    protected boolean lineThrough = false;


    public InlineSpace(int amount) {
        this.size = amount;
    }

    public InlineSpace(int amount, boolean resizeable) {
        this.resizeable = resizeable;
        this.size = amount;
    }

    /**
     * @param ul true if text should be underlined
     */
    public void setUnderlined(boolean ul) {
        this.underlined = ul;
    }

    public boolean getUnderlined() {
        return this.underlined;
    }

    public void setOverlined(boolean ol) {
        this.overlined = ol;
    }

    public boolean getOverlined() {
        return this.overlined;
    }

    public void setLineThrough(boolean lt) {
        this.lineThrough = lt;
    }

    public boolean getLineThrough() {
        return this.lineThrough;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int amount) {
        this.size = amount;
    }

    public boolean getResizeable() {
        return resizeable;
    }

    public void setResizeable(boolean resizeable) {
        this.resizeable = resizeable;
    }

    /**
     * And eatable InlineSpace is discarded if it occurs
     * as the first pending element in a LineArea
     */
    public void setEatable(boolean eatable) {
        this.eatable = eatable;
    }

    public boolean isEatable() {
        return eatable;
    }

}
