package org.apache.xml.fop.layout;

// FOP
import org.apache.xml.fop.render.Renderer;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class BlockArea extends Area {
	
    /* relative to area container */
    protected int startIndent;
    protected int endIndent;

    /* first line startIndent modifier */
    protected int textIndent;

    protected int lineHeight;
	
    protected int halfLeading;

    /* text-align of all but the last line */
    protected int align;

    /* text-align of the last line */
    protected int alignLastLine;
	
    protected LineArea currentLineArea;

    /* have any line areas been used? */
    protected boolean hasLines = false;

    public BlockArea(FontState fontState, int allocationWidth,
		     int maxHeight, int startIndent, int endIndent,
		     int textIndent, int align, int alignLastLine,
		     int lineHeight) {
	super(fontState, allocationWidth, maxHeight);

	this.startIndent = startIndent;
	this.endIndent = endIndent;
	this.textIndent = textIndent;
	this.contentRectangleWidth = allocationWidth - startIndent - endIndent;
	this.align = align;
	this.alignLastLine = alignLastLine;
	this.lineHeight = lineHeight;

	this.halfLeading = (lineHeight - fontState.getFontSize())/2;
    }

    public void render(Renderer renderer) {
	renderer.renderBlockArea(this);
    }

    public void addLineArea(LineArea la) {
	if (!la.isEmpty()) {
	    this.addDisplaySpace(this.halfLeading);
	    int size = la.getHeight();
	    this.addChild(la);
	    this.increaseHeight(size);
	    this.addDisplaySpace(this.halfLeading);
	}
    }

    public int addText(FontState fontState, float red, float green,
		       float blue, int wrapOption,
		       int whiteSpaceTreatment, char data[],
		       int start, int end) { 
	int ts, te;
	char[] ca;
	
	ts = start;
	te = end;
	ca = data;

	if (currentHeight + currentLineArea.getHeight() > maxHeight) {
	    return start;
	}
		
	this.currentLineArea.changeFont(fontState);
	this.currentLineArea.changeColor(red, green, blue);
	this.currentLineArea.changeWrapOption(wrapOption);
	this.currentLineArea.changeWhiteSpaceTreatment(whiteSpaceTreatment);
	ts = this.currentLineArea.addText(ca, ts, te);
	this.hasLines = true;
		
	while (ts != -1) {
	    this.currentLineArea.align(this.align);
	    this.addLineArea(this.currentLineArea);
	    this.currentLineArea = new
		LineArea(fontState, lineHeight, halfLeading,
			 allocationWidth, startIndent, endIndent);  
	    if (currentHeight + currentLineArea.getHeight() >
		this.maxHeight) {
		return ts;
	    }
	    this.currentLineArea.changeFont(fontState);
	    this.currentLineArea.changeColor(red, green, blue);
	    this.currentLineArea.changeWrapOption(wrapOption);
	    this.currentLineArea.changeWhiteSpaceTreatment(whiteSpaceTreatment);
	    ts = this.currentLineArea.addText(ca, ts, te);
	}
	return -1;
    }

    public void end() {
	if (this.hasLines) {
	    this.currentLineArea.addPending();
	    this.currentLineArea.align(this.alignLastLine);
	    this.addLineArea(this.currentLineArea);
	}
    }

    public void start() {
	currentLineArea = new LineArea(fontState, lineHeight,
				       halfLeading, allocationWidth,
				       startIndent + textIndent,
				       endIndent);
    }

    public int getEndIndent() {
	return endIndent;
    }

    public int getStartIndent() {
	return startIndent;
    }

    public int spaceLeft() {
	return maxHeight - currentHeight;
    }
}
