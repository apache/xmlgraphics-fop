/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.layout.FontState;

public class WordArea extends InlineArea {

    private String text;

    // Textdecoration
    protected boolean underlined = false;
    protected boolean overlined = false;
    protected boolean lineThrough = false;


    public WordArea(FontState fontState, float red, float green, float blue,
                    String text, int width) {
        super(fontState, width, red, green, blue);
        this.text = text;
        this.contentRectangleWidth = width;
    }

    public String getText() {
        return this.text;
    }

    public void setUnderlined(boolean ul) {
        this.underlined = ul;
    }

    public boolean getUnderlined() {
        return this.underlined;
    }

}
