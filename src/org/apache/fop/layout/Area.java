package org.apache.xml.fop.layout;

// Java
import java.util.Vector;

abstract public class Area extends Box {

    /* nominal font size and nominal font family incorporated in
       fontState */
    protected FontState fontState;

    protected Vector children = new Vector();
	
    /* max size in line-progression-direction */
    protected int maxHeight;

    protected int currentHeight = 0;

    protected int contentRectangleWidth;

    protected int allocationWidth;

    /* the inner-most area container the area is in */
    protected AreaContainer areaContainer;

    /* the page this area is on */
    protected Page page;

    public Area (FontState fontState) {
	this.fontState = fontState;
    }

    public Area (FontState fontState, int allocationWidth, int maxHeight) {
	this.fontState = fontState;
	this.allocationWidth = allocationWidth;
	this.maxHeight = maxHeight;
    }

    public void addChild(Box child) {
	this.children.addElement(child);
	child.parent = this;
    }
    
    public void addChildAtStart(Box child) {
	this.children.insertElementAt(child,0);
	child.parent = this;
    }
	
    public void addDisplaySpace(int size) {
	this.addChild(new DisplaySpace(size));
	this.currentHeight += size;
    }

    public FontInfo getFontInfo() {
	return this.page.getFontInfo();
    }
	
    public void end() {
    }
    
    public int getAllocationWidth() {
	return this.allocationWidth;
    }
    
    public Vector getChildren() {
	return this.children;
    }
	
    public int getContentWidth() {
	return this.contentRectangleWidth;
    }

    public FontState getFontState() {
	return this.fontState;
    }

    public int getHeight() {
	return this.currentHeight;
    }

    public int getMaxHeight() {
	return this.maxHeight;
    }

    public Page getPage() {
	return this.page;
    }

    public void increaseHeight(int amount) {
	this.currentHeight += amount;
    }
	
    public void setPage(Page page) {
	this.page = page;
    }

    public int spaceLeft() {
	return maxHeight - currentHeight;
    }

    public void start() {
    }
}
