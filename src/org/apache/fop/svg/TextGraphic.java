package org.apache.xml.fop.svg;

/**
 * class representing text in an SVG Area
 */
public class TextGraphic extends Graphic {

    /** x-coordinate of text */
    public int x;

    /** y-coordinate of text */
    public int y;

    /** the text string itself */
    public String s;

    /**
     * construct a text graphic
     *
     * @param x x-coordinate of text
     * @param y y-coordinate of text
     * @param s the text string
     */
    public TextGraphic(int x, int y, String s) {
	this.x = x;
	this.y = y;
	this.s = s;
    }
}
