package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.BlockArea;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;
import org.apache.xml.fop.image.*;

// Java
import java.util.Enumeration;
import java.util.Hashtable;

public class DisplayGraphic extends FObj {
    public static class Maker extends FObj.Maker { 
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new DisplayGraphic(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new DisplayGraphic.Maker();
    }

    FontState fs;
    int align;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    String href;
    int height;
    int width;

    ImageArea imageArea;

    public DisplayGraphic(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:display-graphic";
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

	    // FIXME
	    this.align = this.properties.get("text-align").getEnum();

	    this.startIndent =
		this.properties.get("start-indent").getLength().mvalue();
	    this.endIndent =
		this.properties.get("end-indent").getLength().mvalue();

	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue();

	    this.href = this.properties.get("href").getString();
	    this.width =
		this.properties.get("width").getLength().mvalue();
	    this.height =
		this.properties.get("height").getLength().mvalue();

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
	}

	if ((spaceBefore != 0) && (this.marker == 0)) {
	    area.addDisplaySpace(spaceBefore);
	}

	FopImage img = FopImageFactory.Make(href, 0, 0, width, height);

	this.imageArea = new ImageArea(fs,
				       img,
				       area.getAllocationWidth(),
				       img.getWidth(),
				       img.getHeight(),
				       startIndent, endIndent,
				       align);

	imageArea.start();
	imageArea.end();
	area.addChild(imageArea);
	area.increaseHeight(imageArea.getHeight());

	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	if (area instanceof BlockArea) {
	    area.start();
	}

	return OK;
    }
}
