/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.Enumeration;
import java.util.HashMap;

import org.apache.fop.fo.DirectPropertyListBuilder;
import org.apache.fop.fo.TreeBuilder;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.apps.Driver;

import org.apache.batik.util.XMLResourceDescriptor;

public class SVGElementMapping implements ElementMapping {

    private static HashMap foObjs = null;    
    
    private static synchronized void setupSVG() {

        if(foObjs == null) {
            // this sets the parser that will be used
            // by default (SVGBrokenLinkProvider)
            // normally the user agent value is used
            XMLResourceDescriptor.setXMLParserClassName(Driver.getParserClassName());

            foObjs = new HashMap();
            foObjs.put("svg", SVGElement.maker());
            foObjs.put("rect", SVGObj.maker("rect"));
            foObjs.put("line", SVGObj.maker("line"));
            foObjs.put("text", SVGObj.maker("text"));

            foObjs.put("desc", SVGObj.maker("desc"));
            foObjs.put("title", SVGObj.maker("title"));
            foObjs.put("circle", SVGObj.maker("circle"));
            foObjs.put("ellipse", SVGObj.maker("ellipse"));
            foObjs.put("g", SVGObj.maker("g"));
            foObjs.put("polyline", SVGObj.maker("polyline"));
            foObjs.put("polygon", SVGObj.maker("polygon"));
            foObjs.put("defs", SVGObj.maker("defs"));
            foObjs.put("path", SVGObj.maker("path"));
            foObjs.put("use", SVGObj.maker("use"));
            foObjs.put("tspan", SVGObj.maker("tspan"));
            foObjs.put("tref", SVGObj.maker("tref"));
            foObjs.put("image", SVGObj.maker("image"));
            foObjs.put("style", SVGObj.maker("style"));

            foObjs.put("textPath", SVGObj.maker("textPath"));
            foObjs.put("clipPath", SVGObj.maker("clipPath"));
            foObjs.put("mask", SVGObj.maker("mask"));
            foObjs.put("linearGradient", SVGObj.maker("linearGradient"));
            foObjs.put("radialGradient", SVGObj.maker("radialGradient"));
            foObjs.put("stop", SVGObj.maker("stop"));
            foObjs.put("a", SVGObj.maker("a"));
            foObjs.put("switch", SVGObj.maker("switch"));
            foObjs.put("symbol", SVGObj.maker("symbol"));

            foObjs.put("pattern", SVGObj.maker("pattern"));

            foObjs.put("marker", SVGObj.maker("marker"));
            foObjs.put("animate", SVGObj.maker("animate"));
            foObjs.put("altGlyph", SVGObj.maker("altGlyph"));
            foObjs.put("font", SVGObj.maker("font"));
            foObjs.put("glyph", SVGObj.maker("glyph"));
            foObjs.put("missing-glyph", SVGObj.maker("missing-glyph"));
            foObjs.put("hkern", SVGObj.maker("hkern"));
            foObjs.put("vkern", SVGObj.maker("vkern"));
            foObjs.put("set", SVGObj.maker("set"));
            foObjs.put("animateMotion", SVGObj.maker("animateMotion"));
            foObjs.put("animateColor", SVGObj.maker("animateColor"));
            foObjs.put("animateTransform", SVGObj.maker("animateTransform"));
            foObjs.put("cursor", SVGObj.maker("cursor"));
            foObjs.put("filter", SVGObj.maker("filter"));

            foObjs.put("feFlood", SVGObj.maker("feFlood"));
            foObjs.put("feGaussianBlur", SVGObj.maker("feGaussianBlur"));
            foObjs.put("feOffset", SVGObj.maker("feOffset"));
            foObjs.put("feMerge", SVGObj.maker("feMerge"));
            foObjs.put("feMergeNode", SVGObj.maker("feMergeNode"));
        }
    }

    public void addToBuilder(TreeBuilder builder) {
        setupSVG();
        String uri = "http://www.w3.org/2000/svg";
        builder.addMapping(uri, foObjs);

        builder.addPropertyListBuilder(uri, new DirectPropertyListBuilder());
    }
}

