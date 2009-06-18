package org.apache.xml.fop.layout;

import org.apache.xml.fop.render.Renderer;

public class InlineSpace extends Space {
    private int size; // in millipoints
    
    public InlineSpace(int amount) {
	this.size = amount;
    }

    public int getSize() {
	return size;
    }

    public void setSize(int amount) {
	this.size = amount;
    }

    public void render(Renderer renderer) {
	renderer.renderInlineSpace(this);
    }
}
