package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class PageNumber extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new PageNumber(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new PageNumber.Maker();
    }

    FontState fs;
    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceTreatment;

    public PageNumber(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:page-number";
    }

    public int layout(Area area) throws FOPException {
	if (!(area instanceof BlockArea)) {
	    System.err.println("WARNING: page-number outside block area");
	    return OK;
	}
	if (this.marker == START) {
	    String fontFamily = this.properties.get("font-family").getString();
	    String fontStyle = this.properties.get("font-style").getString();
	    String fontWeight = this.properties.get("font-weight").getString();
	    int fontSize = this.properties.get("font-size").getLength().mvalue();
		
	    this.fs = new FontState(area.getFontInfo(), fontFamily,
				     fontStyle, fontWeight, fontSize);

	    ColorType c = this.properties.get("color").getColorType();
	    this.red = c.red();
	    this.green = c.green();
	    this.blue = c.blue();

	    this.wrapOption = this.properties.get("wrap-option").getEnum();
	    this.whiteSpaceTreatment = this.properties.get("white-space-treatment").getEnum();
	    
	    this.marker = 0;
	}
	String p = Integer.toString(area.getPage().getNumber());
	this.marker = ((BlockArea) area).addText(fs, red, green, blue, wrapOption, whiteSpaceTreatment, p.toCharArray(), 0, p.length());
	return OK;
    }
}
