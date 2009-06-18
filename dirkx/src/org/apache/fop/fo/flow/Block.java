package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.apps.FOPException;

public class Block extends FObjMixed {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Block(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new Block.Maker();
    }

    FontState fs;
    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int textIndent;

    BlockArea blockArea;

    // this may be helpful on other FOs too
    boolean anythingLaidOut = false;

    public Block(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:block";
    }

    public int layout(Area area) throws FOPException {
	// System.err.print(" b:LAY[" + marker + "] ");

	if (this.marker == BREAK_AFTER) {
	    return OK;
	}

	if (this.marker == START) {
	    String fontFamily =
		this.properties.get("font-family").getString(); 
	    String fontStyle =
		this.properties.get("font-style").getString(); 
	    String fontWeight =
		this.properties.get("font-weight").getString(); 
	    int fontSize =
		this.properties.get("font-size").getLength().mvalue(); 
	    
	    this.fs = new FontState(area.getFontInfo(), fontFamily, 
				    fontStyle, fontWeight, fontSize);  
	    this.align = this.properties.get("text-align").getEnum(); 
	    this.alignLast =
		this.properties.get("text-align-last").getEnum(); 
	    this.breakBefore =
		this.properties.get("break-before").getEnum(); 
	    this.breakAfter =
		this.properties.get("break-after").getEnum(); 
	    this.lineHeight =
		this.properties.get("line-height").getLength().mvalue(); 
	    this.startIndent =
		this.properties.get("start-indent").getLength().mvalue(); 
	    this.endIndent =
		this.properties.get("end-indent").getLength().mvalue(); 
	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();  
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue(); 
	    this.textIndent =
		this.properties.get("text-indent").getLength().mvalue(); 

	    if (area instanceof BlockArea) {
		area.end();
	    }
	    if (this.isInLabel) {
		startIndent += bodyIndent;
		endIndent += (area.getAllocationWidth()
			      - distanceBetweenStarts - startIndent)
		    + labelSeparation;
	    }

	    if (this.isInListBody) {
		startIndent += bodyIndent + distanceBetweenStarts;
	    }

	    if (this.isInTableCell) {
		startIndent += forcedStartOffset;
		endIndent = area.getAllocationWidth() - startIndent -
		    forcedWidth;
	    }

	    this.marker = 0;

	    if (breakBefore == BreakBefore.PAGE) {
		return FORCE_PAGE_BREAK;
	    }

	    if (breakBefore == BreakBefore.ODD_PAGE) {
		return FORCE_PAGE_BREAK_ODD;
	    }
	
	    if (breakBefore == BreakBefore.EVEN_PAGE) {
		return FORCE_PAGE_BREAK_EVEN;
	    }
	}

	if ((spaceBefore != 0) && (this.marker ==0)) {
	    area.addDisplaySpace(spaceBefore);
	}

	if (anythingLaidOut) {
	    this.textIndent = 0;
	}

	this.blockArea =
	    new BlockArea(fs, area.getAllocationWidth(), 
			  area.spaceLeft(), startIndent, endIndent,
			  textIndent, align, alignLast, lineHeight);
	blockArea.setPage(area.getPage());
	blockArea.start();

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FONode fo = (FONode) children.elementAt(i);
	    if (this.isInListBody) {
		fo.setIsInListBody();
		fo.setDistanceBetweenStarts(this.distanceBetweenStarts);
		fo.setBodyIndent(this.bodyIndent);
	    }
	    int status;
	    if ((status = fo.layout(blockArea)) != OK) {
		this.marker = i;
		if ((i != 0) && (status == AREA_FULL_NONE)) {
		    status = AREA_FULL_SOME;
		}
		//blockArea.end();
		area.addChild(blockArea);
		area.increaseHeight(blockArea.getHeight());
		anythingLaidOut = true;
		return status;
	    }
	    anythingLaidOut = true;
	}

	blockArea.end();
	area.addChild(blockArea);

	/* should this be combined into above? */
	area.increaseHeight(blockArea.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	if (area instanceof BlockArea) {
	    area.start();
	}

	if (breakAfter == BreakAfter.PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK;
	}

	if (breakAfter == BreakAfter.ODD_PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK_ODD;
	}
	
	if (breakAfter == BreakAfter.EVEN_PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK_EVEN;
	}

	//System.err.print(" b:OK" + marker + " ");
	return OK;
    }

    public int getAreaHeight() {
	return blockArea.getHeight();
    }
}
