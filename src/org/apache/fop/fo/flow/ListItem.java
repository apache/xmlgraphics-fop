package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.BlockArea;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class ListItem extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ListItem(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ListItem.Maker();
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
	
    public ListItem(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:list-item";
    }

    public int layout(Area area) throws FOPException {
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
	    
	    this.marker = 0;
	}

	/* not sure this is needed given we know area is from list block */
	if (area instanceof BlockArea) {
	    area.end();
	}

	if (spaceBefore != 0) {
	    area.addDisplaySpace(spaceBefore);
	}

	startIndent += this.bodyIndent;

	BlockArea blockArea =
	    new BlockArea(fs, area.getAllocationWidth(), 
			  area.spaceLeft(), startIndent, endIndent,
			  0, align, alignLast, lineHeight);
	blockArea.setPage(area.getPage());
	blockArea.start();

	int numChildren = this.children.size();
	if (numChildren != 2) {
	    throw new FOPException("list-item must have exactly two children");
	}
	ListItemLabel label = (ListItemLabel) children.elementAt(0);
	ListItemBody body = (ListItemBody) children.elementAt(1);

	label.setDistanceBetweenStarts(this.distanceBetweenStarts);
	label.setLabelSeparation(this.labelSeparation);
	label.setBodyIndent(this.bodyIndent);

	body.setDistanceBetweenStarts(this.distanceBetweenStarts);
	body.setBodyIndent(this.bodyIndent);

	/* this doesn't actually do anything */
	body.setLabelSeparation(this.labelSeparation);

	int status;

	// what follows doesn't yet take into account whether the
	// body failed completely or only got some text in

	if (this.marker == 0) {
	    status = label.layout(blockArea);
	    if (status != OK) {
		return status;
	    }
	}

	status = body.layout(blockArea);
	if (status != OK) {
	    blockArea.end();
	    area.addChild(blockArea);
	    area.increaseHeight(blockArea.getHeight());
	    this.marker = 1;
	    return status;
	}

	blockArea.end();
	area.addChild(blockArea);
	area.increaseHeight(blockArea.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	/* not sure this is needed given we know area is from list block */
	if (area instanceof BlockArea) {
	    area.start();
	}
	return OK;
    }
}
