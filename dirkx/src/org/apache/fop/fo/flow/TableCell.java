package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.apps.FOPException;

public class TableCell extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new TableCell(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new TableCell.Maker();
    }

    FontState fs;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;

    protected int startOffset;
    protected int width;
    protected int height = 0;

    BlockArea blockArea;

    public TableCell(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:table-cell";
    }

    public void setStartOffset(int offset) {
	startOffset = offset;
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public int layout(Area area) throws FOPException {
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
	    this.startIndent =
		this.properties.get("start-indent").getLength().mvalue(); 
	    this.endIndent =
		this.properties.get("end-indent").getLength().mvalue(); 
	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();  
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue(); 
	    if (area instanceof BlockArea) {
		area.end();
	    }

	    //if (this.isInListBody) {
	    //startIndent += bodyIndent + distanceBetweenStarts;
	    //}

	    this.marker = 0;

	}

	if ((spaceBefore != 0) && (this.marker ==0)) {
	    area.addDisplaySpace(spaceBefore);
	}

	this.blockArea =
	    new BlockArea(fs, area.getAllocationWidth(), 
			  area.spaceLeft(), startIndent, endIndent, 0,
			  0, 0, 0);
	blockArea.setPage(area.getPage());
	blockArea.start();

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    fo.setIsInTableCell();
	    fo.forceStartOffset(startOffset);
	    fo.forceWidth(width);
	    int status;
	    if ((status = fo.layout(blockArea)) != OK) {
		this.marker = i;
		if ((i == 0) && (status == AREA_FULL_NONE)) {
		    return AREA_FULL_NONE;
		} else {
		    return AREA_FULL_SOME;
		}
	    }
	    height += blockArea.getHeight();

	}
	blockArea.end();
	area.addChild(blockArea);

	return OK;
    }

    public int getHeight() {
	return height;
    }
}
