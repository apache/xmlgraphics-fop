package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.render.Renderer;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.layout.Area;

/**
 * class representing an SVG area in which the SVG graphics sit
 */
public class SVGArea extends Area {

    /**
     * construct an SVG area
     *
     * @param fontState the font state
     * @param width the width of the area
     * @param height the height of the area
     */
    public SVGArea(FontState fontState, int width, int height)  {
	super(fontState, width, height);
	currentHeight = height;
	contentRectangleWidth = width;
    }

    /**
     * add a graphic.
     *
     * Graphics include SVG Rectangles, Lines and Text
     *
     * @param graphic the Graphic to add
     */
    public void addGraphic(Graphic graphic) {
	this.children.addElement(graphic);
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
