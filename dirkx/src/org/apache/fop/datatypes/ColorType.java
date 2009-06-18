package org.apache.xml.fop.datatypes;

/**
 * a colour quantity in XSL
 */
public class ColorType {

    /** the red component */
    protected float red;

    /** the green component */
    protected float green;

    /** the blue component */
    protected float blue;

    /**
     * set the colour given a particular String specifying either a
     * colour name or #RGB or #RRGGBB 
     */
    public ColorType (String value) {
	if (value.startsWith("#")) {
	    try {
		if (value.length()==4) {
		    // note: divide by 15 so F = FF = 1 and so on
		    this.red = Integer.parseInt(value.substring(1,2),16)/15f;
		    this.green = Integer.parseInt(value.substring(2,3),16)/15f;
		    this.blue = Integer.parseInt(value.substring(3),16)/15f;
		} else if (value.length()==7) {
		    // note: divide by 255 so FF = 1
		    this.red = Integer.parseInt(value.substring(1,3),16)/255f;
		    this.green = Integer.parseInt(value.substring(3,5),16)/255f;
		    this.blue = Integer.parseInt(value.substring(5),16)/255f;
		} else {
		    this.red = 0;
		    this.green = 0;
		    this.blue = 0;
		    System.err.println("ERROR: unknown colour format. Must be #RGB or #RRGGBB");
		}
	    } catch (Exception e) {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		System.err.println("ERROR: unknown colour format. Must be #RGB or #RRGGBB");
	    }
	} else {
	    if (value.toLowerCase().equals("black")) {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("green")) {
		this.red = 0;
		this.green = 0.5f;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("silver")) {
		this.red = 0.75f;
		this.green = 0.75f;
		this.blue = 0.75f;
	    } else if (value.toLowerCase().equals("lime")) {
		this.red = 0;
		this.green = 1;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("gray")) {
		this.red = 0.5f;
		this.green = 0.5f;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("olive")) {
		this.red = 0.5f;
		this.green = 0.5f;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("white")) {
		this.red = 1;
		this.green = 1;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("yellow")) {
		this.red = 1;
		this.green = 1;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("maroon")) {
		this.red = 0.5f;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("navy")) {
		this.red = 0;
		this.green = 0;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("red")) {
		this.red = 1;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("blue")) {
		this.red = 0;
		this.green = 0;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("purple")) {
		this.red = 0.5f;
		this.green = 0;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("teal")) {
		this.red = 0;
		this.green = 0.5f;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("fuchsia")) {
		this.red = 1;
		this.green = 0;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("aqua")) {
		this.red = 0;
		this.green = 1;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("orange")) {
		// for compatibility with passiveTex
		this.red = 0.7f;
		this.green = 0.5f;
		this.blue = 0;
	    } else {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		System.err.println("ERROR: unknown colour name: " + value);
	    }
	}
    }
	
    public float blue() {
	return this.blue;
    }
	
    public float green() {
	return this.green;
    }
	
    public float red() {
	return this.red;
    }
}
