package org.apache.xml.fop.layout;

import org.apache.xml.fop.render.Renderer;

public class InlineArea extends Area {

    private String text;
    private float red, green, blue;

    public InlineArea(FontState fontState, float red, float green, float blue, String text, int width) {
	super(fontState);
	this.red = red;
	this.green = green;
	this.blue = blue;
	this.text = text;
	this.contentRectangleWidth = width;
    }

    public void render(Renderer renderer) {
	renderer.renderInlineArea(this);
    }

    public float getBlue() {
	return this.blue;
    }

    public float getGreen() {
	return this.green;
    }

    public float getRed() {
	return this.red;
    }

    public String getText() {
	return this.text;
    }
}
