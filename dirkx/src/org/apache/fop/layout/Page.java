package org.apache.xml.fop.layout;

// FOP
import org.apache.xml.fop.render.Renderer;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class Page {

    private int height;
    private int width;

    private AreaContainer body;
    private AreaContainer before;
    private AreaContainer after;
    private AreaContainer start;
    private AreaContainer end;
	
    private AreaTree areaTree;

    protected int pageNumber = 0;

    Page(AreaTree areaTree, int height, int width) {
	this.areaTree = areaTree;
	this.height = height;
	this.width = width;
    }

    public void setNumber(int number) {
	this.pageNumber = number;
    }

    public int getNumber() {
	return this.pageNumber;
    }

    void addAfter(AreaContainer area) {
	this.after = area;
	area.setPage(this);
    }

    void addBefore(AreaContainer area) {
	this.before = area;
	area.setPage(this);
    }

    void addBody(AreaContainer area) {
	this.body = area;
	area.setPage(this);
    }
	
    void addEnd(AreaContainer area) {
	this.end = area;
	area.setPage(this);
    }

    void addStart(AreaContainer area) {
	this.start = area;
	area.setPage(this);
    }

    public void render(Renderer renderer) {
	renderer.renderPage(this);
    }

    public AreaContainer getAfter() {
	return this.after;
    }

    public AreaContainer getBefore() {
	return this.before;
    }

    public AreaContainer getBody() {
	return this.body;
    }

    public int getHeight() {
	return this.height;
    }

    public int getWidth() {
	return this.width;
    }

    public FontInfo getFontInfo() {
	return this.areaTree.getFontInfo();
    }
}
