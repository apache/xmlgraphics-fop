/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.util.Enumeration;

import org.apache.fop.fo.properties.SVGPropertyMapping;
import org.apache.fop.fo.TreeBuilder;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fo.ElementMapping;

public class SVGElementMapping implements ElementMapping {

    public void addToBuilder(TreeBuilder builder) {
        String uri = "http://www.w3.org/2000/svg";
        builder.addMapping(uri, "svg", SVGElement.maker());
        builder.addMapping(uri, "rect", Rect.maker());
        builder.addMapping(uri, "line", Line.maker());
        builder.addMapping(uri, "text", Text.maker());

        builder.addMapping(uri, "desc", Desc.maker());
        builder.addMapping(uri, "title", Title.maker());
        builder.addMapping(uri, "circle", Circle.maker());
        builder.addMapping(uri, "ellipse", Ellipse.maker());
        builder.addMapping(uri, "g", G.maker());
        builder.addMapping(uri, "polyline", Polyline.maker());
        builder.addMapping(uri, "polygon", Polygon.maker());
        builder.addMapping(uri, "defs", Defs.maker());
        builder.addMapping(uri, "path", Path.maker());
        builder.addMapping(uri, "use", Use.maker());
        builder.addMapping(uri, "tspan", Tspan.maker());
        builder.addMapping(uri, "tref", Tref.maker());
        builder.addMapping(uri, "image", Image.maker());
        builder.addMapping(uri, "style", Style.maker());

        builder.addMapping(uri, "textPath", TextPath.maker());
        builder.addMapping(uri, "clipPath", ClipPath.maker());
        builder.addMapping(uri, "mask", Mask.maker());
        builder.addMapping(uri, "linearGradient", LinearGradient.maker());
        builder.addMapping(uri, "radialGradient", RadialGradient.maker());
        builder.addMapping(uri, "stop", Stop.maker());
        builder.addMapping(uri, "a", A.maker());
        builder.addMapping(uri, "switch", Switch.maker());
        builder.addMapping(uri, "symbol", Symbol.maker());

        builder.addMapping(uri, "pattern", Pattern.maker());

        builder.addMapping(uri, "marker", Marker.maker());
        builder.addMapping(uri, "animate", Animate.maker());
        builder.addMapping(uri, "altGlyph", AltGlyph.maker());
        builder.addMapping(uri, "font", Font.maker());
        builder.addMapping(uri, "glyph", Glyph.maker());
        builder.addMapping(uri, "missing-glyph", MissingGlyph.maker());
        builder.addMapping(uri, "hkern", Hkern.maker());
        builder.addMapping(uri, "vkern", Vkern.maker());
        builder.addMapping(uri, "set", Set.maker());
        builder.addMapping(uri, "animateMotion", AnimateMotion.maker());
        builder.addMapping(uri, "animateColor", AnimateColor.maker());
        builder.addMapping(uri, "animateTransform", AnimateTransform.maker());
        builder.addMapping(uri, "cursor", Cursor.maker());
        builder.addMapping(uri, "filter", Filter.maker());

        builder.addMapping(uri, "feFlood", FeFlood.maker());
        builder.addMapping(uri, "feGaussianBlur", FeGaussianBlur.maker());
        builder.addMapping(uri, "feOffset", FeOffset.maker());
        builder.addMapping(uri, "feMerge", FeMerge.maker());
        builder.addMapping(uri, "feMergeNode", FeMergeNode.maker());


        builder.addPropertyList(uri, SVGPropertyMapping.getGenericMappings());
        /* Add any element mappings */
        for (Enumeration e = SVGPropertyMapping.getElementMappings();
                e.hasMoreElements(); ) {
            String elem = (String)e.nextElement();
            builder.addElementPropertyList(uri, elem,
                                           SVGPropertyMapping.getElementMapping(elem));
        }
    }

}
