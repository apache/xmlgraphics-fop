package org.apache.xml.fop.fo;

// FOP
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.BlockArea;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.apps.FOPException;

/**
 * a text node in the formatting object tree
 */
public class FOText extends FONode {

    protected char[] ca;
    protected int start;
    protected int length;

    FontState fs;
    float red;
    float green;
    float blue;
    int wrapOption;
    int whiteSpaceTreatment;

    protected FOText(char[] chars, int s, int e, FObj parent) {
	super(parent);
	this.start = 0;
	this.ca = new char[e - s];
	for (int i = s; i < e; i++)
	    this.ca[i - s] = chars[i];
	this.length = e - s;
    }

    public int layout(Area area) throws FOPException {
	if (!(area instanceof BlockArea)) {
	    System.err.println("WARNING: text outside block area" + new String(ca, start, length));
	    return OK;
	}
	if (this.marker == START) {
	    String fontFamily =
		this.parent.properties.get("font-family").getString(); 
	    String fontStyle =
		this.parent.properties.get("font-style").getString(); 
	    String fontWeight =
		this.parent.properties.get("font-weight").getString(); 
	    int fontSize =
		this.parent.properties.get("font-size").getLength().mvalue(); 
	    
	    this.fs = new FontState(area.getFontInfo(), fontFamily, fontStyle,
				    fontWeight, fontSize); 
	    
	    ColorType c =
		this.parent.properties.get("color").getColorType();
	    this.red = c.red();
	    this.green = c.green();
	    this.blue = c.blue();
	    
	    this.wrapOption =
		this.parent.properties.get("wrap-option").getEnum(); 
	    this.whiteSpaceTreatment =
		this.parent.properties.get("white-space-treatment").getEnum();

	    this.marker = this.start;
	}
	int orig_start = this.marker;
	this.marker = ((BlockArea) area).addText(fs, red, green, blue,
						 wrapOption,
						 whiteSpaceTreatment,
						 ca, this.marker, length);
	if (this.marker == -1) {
	    return OK;
	} else if (this.marker != orig_start) {
	    return AREA_FULL_SOME;
	} else {
	    return AREA_FULL_NONE;
	}
    }
}
