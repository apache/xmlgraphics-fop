package org.apache.xml.fop.svg;

/**
 * class representing a line in an SVG Area
 */
public class LineGraphic extends Graphic {

    /** x-coordinate of start */
    public int x1;

    /** y-coordinate of start */
    public int y1;

    /** x-coordinate of end */
    public int x2;

    /** y-coordinate of end */
    public int y2;

    /**
     * construct a line graphic
     *
     * @param x1 x-coordinate of start
     * @param y1 y-coordinate of start
     * @param x2 x-coordinate of end
     * @param y2 y-coordinate of end
     */
    public LineGraphic(int x1, int y1, int x2, int y2) {
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
    }
}
