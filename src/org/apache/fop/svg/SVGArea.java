/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.Area;

import java.util.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.*;

/**
 * class representing an SVG area in which the SVG graphics sit
 */
public class SVGArea extends Area {
    Document doc;

    /**
     * construct an SVG area
     *
     * @param fontState the font state
     * @param width the width of the area
     * @param height the height of the area
     */
    public SVGArea(FontState fontState, float width, float height) {
        super(fontState, (int) width * 1000, (int) height * 1000);
        currentHeight = (int) height * 1000;
        contentRectangleWidth = (int) width * 1000;
    }

    public void setSVGDocument(Document doc) {
        this.doc = doc;
    }

    public Document getSVGDocument() {
        return doc;
    }

    public int getWidth() {
        //        return getSVGDocument().getRootElement().getWidth().getBaseVal().getValue();
        return contentRectangleWidth;
    }

    /**
     * render the SVG.
     *
     * @param renderer the Renderer to use
     */
    public void render(Renderer renderer) {
        renderer.renderSVGArea(this);
    }
}
