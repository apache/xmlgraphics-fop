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

public class ListBlock extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ListBlock(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ListBlock.Maker();
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
    int provisionalDistanceBetweenStarts;
    int provisionalLabelSeparation;
    int spaceBetweenListRows = 0;

    public ListBlock(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:list-block";
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
	    this.provisionalDistanceBetweenStarts =
		this.properties.get("provisional-distance-between-starts").getLength().mvalue();
	    this.provisionalLabelSeparation =
		this.properties.get("provisional-label-separation").getLength().mvalue(); 
	    this.spaceBetweenListRows = 0; // not used at present
	    
	    this.marker = 0;

	    if (area instanceof BlockArea) {
		area.end();
	    }

	    if (spaceBefore != 0) {
		area.addDisplaySpace(spaceBefore);
	    }

	    if (this.isInListBody) {
		startIndent += bodyIndent + distanceBetweenStarts;
		bodyIndent = startIndent;
	    }
	}

	BlockArea blockArea =
	    new BlockArea(fs, area.getAllocationWidth(),
			  area.spaceLeft(), startIndent, endIndent, 0,
			  align, alignLast, lineHeight);
	blockArea.setPage(area.getPage());
	blockArea.start();

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    if (!(children.elementAt(i) instanceof ListItem)) {
		System.err.println("WARNING: This version of FOP requires list-items inside list-blocks");
		return OK;
	    }
	    ListItem listItem = (ListItem) children.elementAt(i);
	    listItem.setDistanceBetweenStarts(this.provisionalDistanceBetweenStarts);
	    listItem.setLabelSeparation(this.provisionalLabelSeparation);
	    listItem.setBodyIndent(this.bodyIndent);
	    int status;
	    if ((status = listItem.layout(blockArea)) != OK) {
		/* message from child */
		if (status > OK) {
		    /* child still successful */
		    this.marker = i+1;
		} else {
		    /* child unsucessful */
		    this.marker = i;
		}
		blockArea.end();
		area.addChild(blockArea);
		area.increaseHeight(blockArea.getHeight());
		return status;
	    }
	}

	blockArea.end();
	area.addChild(blockArea);
	area.increaseHeight(blockArea.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	if (area instanceof BlockArea) {
	    area.start();
	}
	
	return OK;
    }
}
