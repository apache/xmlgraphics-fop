package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Vector;

public class TableBody extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new TableBody(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new TableBody.Maker();
    }

    FontState fs;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;

    Vector columns;

    BlockArea blockArea;

    public TableBody(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:table-body";
    }

    public void setColumns(Vector columns) {
	this.columns = columns;
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
	    TableRow row = (TableRow) children.elementAt(i);

	    //if (this.isInListBody) {
	    //fo.setIsInListBody();
	    //fo.setDistanceBetweenStarts(this.distanceBetweenStarts);
	    //fo.setBodyIndent(this.bodyIndent);
	    //}

	    row.setColumns(columns);

	    int status;
	    if ((status = row.layout(blockArea)) != OK) {
		this.marker = i;
		if ((i != 0) && (status == AREA_FULL_NONE)) {
		    status = AREA_FULL_SOME;
		}
		//blockArea.end();
		area.addChild(blockArea);
		area.increaseHeight(blockArea.getHeight());
		return status;
	    }
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

	return OK;
    }

    public int getAreaHeight() {
	return blockArea.getHeight();
    }
}
