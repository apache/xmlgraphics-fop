package org.apache.xml.fop.layout;

// FOP
import org.apache.xml.fop.render.Renderer;

// Java
import java.util.Vector;
import java.util.Enumeration;
						  
public class AreaContainer extends Area {

    private int xPosition; // should be able to take value 'left' and 'right' too
    private int yPosition; // should be able to take value 'top' and 'bottom' too

    AreaContainer(int xPosition, int yPosition, int allocationWidth, int maxHeight) {
	super(null, allocationWidth, maxHeight);
	this.xPosition = xPosition;
	this.yPosition = yPosition;
    }

    public void render(Renderer renderer) {
	renderer.renderAreaContainer(this);
    }

    public int getXPosition() {
	return xPosition;
    }

    public int getYPosition() {
	return yPosition;
    }
}
