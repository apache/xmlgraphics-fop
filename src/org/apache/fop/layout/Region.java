package org.apache.xml.fop.layout;

public class Region {

    private int xPosition;
    private int yPosition;
    private int width;
    private int height;
    
    public Region(int xPosition, int yPosition, int width, int height) {
	this.xPosition = xPosition;
	this.yPosition = yPosition;
	this.width = width;
	this.height = height;
    }

    public AreaContainer makeAreaContainer() {
	return new AreaContainer(xPosition, yPosition, width, height);
    }
}
