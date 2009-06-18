package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

/**
 * class representing svg:line pseudo flow object.
 */
public class Line extends FObj {

    /**
     * inner class for making Line objects.
     */
    public static class Maker extends FObj.Maker {

	/**
	 * make a Line object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the Line object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException { 
	    return new Line(parent, propertyList);
	}
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for Line objects
     */
    public static FObj.Maker maker() {
	return new Line.Maker();
    }

    /**
     * constructs a Line object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected Line(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:line";
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
	int x1 = this.properties.get("x1").getLength().mvalue();
	int x2 = this.properties.get("x2").getLength().mvalue();
	int y1 = this.properties.get("y1").getLength().mvalue();
	int y2 = this.properties.get("y2").getLength().mvalue();
	
	/* if the area this is being put into is an SVGArea */
	if (area instanceof SVGArea) {
	    /* add a line to the SVGArea */
	    ((SVGArea) area).addGraphic(new LineGraphic(x1, y1, x2, y2));
	} else {
	    /* otherwise generate a warning */
	    System.err.println("WARNING: svg:line outside svg:svg");
	}

	/* return status */
	return OK;
    }
}
