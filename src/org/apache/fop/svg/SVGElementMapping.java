/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.Enumeration;

import org.apache.fop.fo.DirectPropertyListBuilder;
import org.apache.fop.fo.TreeBuilder;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;

public class SVGElementMapping implements ElementMapping {

    public void addToBuilder(TreeBuilder builder) {
        String uri = "http://www.w3.org/2000/svg";
        builder.addMapping(uri, "svg", SVGElement.maker());
        builder.addMapping(uri, "rect", SVGObj.maker("rect"));
        builder.addMapping(uri, "line", SVGObj.maker("line"));
        builder.addMapping(uri, "text", SVGObj.maker("text"));

        builder.addMapping(uri, "desc", SVGObj.maker("desc"));
        builder.addMapping(uri, "title", SVGObj.maker("title"));
        builder.addMapping(uri, "circle", SVGObj.maker("circle"));
        builder.addMapping(uri, "ellipse", SVGObj.maker("ellipse"));
        builder.addMapping(uri, "g", SVGObj.maker("g"));
        builder.addMapping(uri, "polyline", SVGObj.maker("polyline"));
        builder.addMapping(uri, "polygon", SVGObj.maker("polygon"));
        builder.addMapping(uri, "defs", SVGObj.maker("defs"));
        builder.addMapping(uri, "path", SVGObj.maker("path"));
        builder.addMapping(uri, "use", SVGObj.maker("use"));
        builder.addMapping(uri, "tspan", SVGObj.maker("tspan"));
        builder.addMapping(uri, "tref", SVGObj.maker("tref"));
        builder.addMapping(uri, "image", SVGObj.maker("image"));
        builder.addMapping(uri, "style", SVGObj.maker("style"));

        builder.addMapping(uri, "textPath", SVGObj.maker("textPath"));
        builder.addMapping(uri, "clipPath", SVGObj.maker("clipPath"));
        builder.addMapping(uri, "mask", SVGObj.maker("mask"));
        builder.addMapping(uri, "linearGradient", SVGObj.maker("linearGradient"));
        builder.addMapping(uri, "radialGradient", SVGObj.maker("radialGradient"));
        builder.addMapping(uri, "stop", SVGObj.maker("stop"));
        builder.addMapping(uri, "a", SVGObj.maker("a"));
        builder.addMapping(uri, "switch", SVGObj.maker("switch"));
        builder.addMapping(uri, "symbol", SVGObj.maker("symbol"));

        builder.addMapping(uri, "pattern", SVGObj.maker("pattern"));

        builder.addMapping(uri, "marker", SVGObj.maker("marker"));
        builder.addMapping(uri, "animate", SVGObj.maker("animate"));
        builder.addMapping(uri, "altGlyph", SVGObj.maker("altGlyph"));
        builder.addMapping(uri, "font", SVGObj.maker("font"));
        builder.addMapping(uri, "glyph", SVGObj.maker("glyph"));
        builder.addMapping(uri, "missing-glyph", SVGObj.maker("missing-glyph"));
        builder.addMapping(uri, "hkern", SVGObj.maker("hkern"));
        builder.addMapping(uri, "vkern", SVGObj.maker("vkern"));
        builder.addMapping(uri, "set", SVGObj.maker("set"));
        builder.addMapping(uri, "animateMotion", SVGObj.maker("animateMotion"));
        builder.addMapping(uri, "animateColor", SVGObj.maker("animateColor"));
        builder.addMapping(uri, "animateTransform", SVGObj.maker("animateTransform"));
        builder.addMapping(uri, "cursor", SVGObj.maker("cursor"));
        builder.addMapping(uri, "filter", SVGObj.maker("filter"));

        builder.addMapping(uri, "feFlood", SVGObj.maker("feFlood"));
        builder.addMapping(uri, "feGaussianBlur", SVGObj.maker("feGaussianBlur"));
        builder.addMapping(uri, "feOffset", SVGObj.maker("feOffset"));
        builder.addMapping(uri, "feMerge", SVGObj.maker("feMerge"));
        builder.addMapping(uri, "feMergeNode", SVGObj.maker("feMergeNode"));

        builder.addPropertyListBuilder(uri, new DirectPropertyListBuilder());
    }

}
