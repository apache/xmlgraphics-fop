package org.apache.xml.fop.layout;

import org.apache.xml.fop.render.Renderer;

import java.util.Vector;
import java.util.Enumeration;

public class RuleArea extends Area {
	
	int align;          // text-align
	int length;			// length in millipoints
	int ruleThickness;
	
	int startIndent;
	int endIndent;

	float red, green, blue;
	public RuleArea(FontState fontState, int allocationWidth, int maxHeight, int startIndent, int endIndent, int align, int ruleThickness, int length, float red, float green, float blue)  {
		super(fontState,allocationWidth,maxHeight);

		this.contentRectangleWidth = allocationWidth - startIndent - endIndent;
		this.align = align;

		this.startIndent = startIndent;
		this.endIndent = endIndent;
		this.ruleThickness = ruleThickness;
		this.length = length;
		this.currentHeight = maxHeight;

		this.red = red;
		this.green = green;
		this.blue = blue;
	}

    public void render(Renderer renderer) {
	renderer.renderRuleArea(this);
    }
	public float getBlue() {
		return this.blue;
	}
	public int getEndIndent() {
		return endIndent;
	}
	public float getGreen() {
		return this.green;
	}
	public int getHeight() {
		return this.ruleThickness;
	}
	public float getRed() {
		return this.red;
	}
	public int getRuleThickness() {
		return this.ruleThickness;
	}
	public int getStartIndent() {
		return startIndent;
	}
}
