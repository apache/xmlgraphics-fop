package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

/**
 * class representing svg:text pseudo flow object.
 */
public class Text extends FObjMixed {

    /**
     * inner class for making SVG Text objects.
     */
    public static class Maker extends FObj.Maker {

	/**
	 * make an SVG Text object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the SVG Text object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Text(parent, propertyList);
	}
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for SVG Text objects
     */
    public static FObj.Maker maker() {
	return new Text.Maker();
    }

    /**
     * the string of text to display
     */
    protected String text = "";

    /**
     * constructs an SVG Text object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected Text(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:text";
    }

    /**
     * add characters to the string to display.
     *
     * @param data array of characters
     * @param start start offset in character array
     * @param length number of characters to add
     */
    protected void addCharacters(char data[], int start, int length) {
	this.text += new String(data, start, length);
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     */
    public int layout(Area area) throws FOPException {

	/* retrieve properties */
	int x = this.properties.get("x").getLength().mvalue();
	int y = this.properties.get("y").getLength().mvalue();
	
	/* if the area this is being put into is an SVGArea */
	if (area instanceof SVGArea) {
	    /* add the text to the SVGArea */
	    ((SVGArea) area).addGraphic(new TextGraphic(x, y, text));
	} else {
	    /* otherwise generate a warning */
	    System.err.println("WARNING: svg:text outside svg:svg");
	}

	/* return status */
	return OK;
    }
}
