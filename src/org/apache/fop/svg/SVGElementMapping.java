package org.apache.xml.fop.svg;

import org.apache.xml.fop.fo.FOTreeBuilder;
import org.apache.xml.fop.fo.ElementMapping;

public class SVGElementMapping implements ElementMapping {

    public void addToBuilder(FOTreeBuilder builder) {
	String uri = "http://www.w3.org/Graphics/SVG/SVG-19990812.dtd";
	builder.addMapping(uri, "svg", SVG.maker());
	builder.addMapping(uri, "rect", Rect.maker());
	builder.addMapping(uri, "line", Line.maker());
	builder.addMapping(uri, "text", Text.maker());
    }
}
