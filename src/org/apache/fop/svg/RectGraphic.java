package org.apache.xml.fop.svg;

/**
 * class representing a rectangle in an SVG Area
 */
public class RectGraphic extends Graphic {

    /** x-coordinate of corner */
    public int x;

    /** y-coordinate of corner */
    public int y;

    /** width of rectangle */
    public int width;

    /** height of rectangle */
    public int height;

    /**
     * construct a rectangle graphic.
     *
     * @param x x-coordinate of corner
     * @param y y-coordinate of corner
     * @param width width of rectangle
     * @param height height of rectangle
     */
    public RectGraphic(int x, int y, int width, int height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
    }
}
