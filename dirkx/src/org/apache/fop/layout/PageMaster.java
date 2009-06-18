package org.apache.xml.fop.layout;

public class PageMaster {

    private int width;
    private int height;
	
    private Region body;
    private Region before;
    private Region after;
    private Region start;
    private Region end;

    public PageMaster(int pageWidth, int pageHeight) {
	this.width = pageWidth;
	this.height = pageHeight;
    }

    public void addAfter(Region region) {
	this.after = region;
    }

    public void addBefore(Region region) {
	this.before = region;
    }

    public void addBody(Region region) {
	this.body = region;
    }

    public void addEnd(Region region) {
	this.end = region;
    }
	
    public void addStart(Region region) {
	this.start = region;
    }

    public int getHeight() {
	return this.height;
    }

    public int getWidth() {
	return this.width;
    }

    public Page makePage(AreaTree areaTree) {
	Page p = new Page(areaTree, this.height, this.width);
	if (this.body != null) {
	    p.addBody(body.makeAreaContainer());
	}
	if (this.before != null) {
	    p.addBefore(before.makeAreaContainer());
	}
	if (this.after != null) {
	    p.addAfter(after.makeAreaContainer());
	}
	if (this.start != null) {
	    p.addStart(start.makeAreaContainer());
	}
	if (this.end != null) {
	    p.addEnd(end.makeAreaContainer());
	}
	return p;
    }
}
