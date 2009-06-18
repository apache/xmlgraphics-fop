package org.apache.xml.fop.datatypes;

import org.apache.xml.fop.fo.Property;

/**
 * a length quantity in XSL
 */
public class Length {

    protected int millipoints = 0;

    protected double fontsize = 12;

    /**
     * set the length given a particular String specifying length and units
     */
    public Length (String len) {
	convert(len);
    }

    /**
     * set the length given a particular String specifying length and units,
     * and the font-size (necessary for an em)
     */
    public Length (String len, int fontsize) {
	this.fontsize = fontsize;
	convert(len);
    }

    /**
     * set the length given a particular multiplier and a length property
     */
    public Length (double multiplier, Property property) {
	this.millipoints = (int)(multiplier * property.getLength().mvalue());
    }

    protected void convert(String len) {
	/* convert the given length to a dimensionless integer representing
	   points. */
	
	int assumed_resolution = 1; // points/pixel
	
	int l = len.length();
	
	if (l == 0) {
	    System.err.println("WARNING: empty length");
	    this.millipoints = 0;
	} else {
	    String unit = len.substring(l-2);
	    double dvalue =
		Double.valueOf(len.substring(0,(l-2))).doubleValue();
	    
	    if (unit.equals("in"))
		dvalue = dvalue * 72;
	    else if (unit.equals("cm"))
		dvalue = dvalue * 28.35;
	    else if (unit.equals("mm"))
		dvalue = dvalue * 2.84;
	    else if (unit.equals("pt"))
		dvalue = dvalue;
	    else if (unit.equals("pc"))
		dvalue = dvalue * 12;
	    else if (unit.equals("em"))
		dvalue = dvalue * fontsize;
	    else if (unit.equals("px"))
		dvalue = dvalue * assumed_resolution;
	    else {
		dvalue = 0;
		System.err.println("ERROR: unknown length units in "
				   + len);
	    }
	    
	    this.millipoints = (int) (dvalue * 1000);
	}
    }

    /**
     * return the length in 1/1000ths of a point
     */
    public int mvalue() {
	return millipoints;
    }

    public String toString() {
	String s = millipoints + "mpt";
	return s;
    }
}
