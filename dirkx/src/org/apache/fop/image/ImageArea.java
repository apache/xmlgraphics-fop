/* modified by JKT to integrate into 0.12.0 */

package org.apache.xml.fop.image;

import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.render.Renderer;

import java.util.Vector;
import java.util.Enumeration;

public class ImageArea extends Area {

    protected int xOffset = 0;
    protected FopImage image;

    public ImageArea(FontState fontState, FopImage img,
		     int AllocationWidth, int width, int height,
		     int startIndent, int endIndent, int align)  {
	super(fontState,width,height);
	this.currentHeight = height;
	this.contentRectangleWidth = width;
	this.image = img;

	switch (align) {
	case 1:
	    xOffset = startIndent;
	    break;
	case 2:
	    if (endIndent == 0)
		endIndent = AllocationWidth;
	    xOffset = (endIndent - width);
	    break;
	case 3:
	case 4:
	    if (endIndent == 0)
		endIndent = AllocationWidth;
	    xOffset = startIndent + ((endIndent - startIndent) - width)/2;
	    break;
	}
    }

    public int getXOffset() {
	return this.xOffset;
    }

    public FopImage getImage() {
	return this.image;
    }
    
    public void render(Renderer renderer) {
	renderer.renderImageArea(this);
    }

    public int getImageHeight() {
	return currentHeight;
    }
}
