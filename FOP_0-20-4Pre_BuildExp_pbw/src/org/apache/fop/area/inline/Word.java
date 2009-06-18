/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

public class Word extends InlineArea {

    // character info: font, char spacing, colour, baseline
    String word;

    public void render(Renderer renderer) {
        renderer.renderWord(this);
    }

    public void setWord(String w) {
        word = w;
    }

    public String getWord() {
        return word;
    }
}
