package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

/**
 * class representing svg:rect pseudo flow object.
 */
public class Rect extends FObj {

    /**
     * inner class for making Rect objects.
     */
    public static class Maker extends FObj.Maker {

	/**
	 * make a Rect object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the Rect object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Rect(parent, propertyList);
	}
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for Rect objects
     */
    public static FObj.Maker maker() {
	return new Rect.Maker();
    }

    /**
     * constructs a Rect object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected Rect(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:rect";
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
	int width = this.properties.get("width").getLength().mvalue();
	int height = this.properties.get("height").getLength().mvalue();
	int x = this.properties.get("x").getLength().mvalue();
	int y = this.properties.get("y").getLength().mvalue();
	
	/* if the area this is being put into is an SVGArea */
	if (area instanceof SVGArea) {
	    /* add a rectangle to the SVGArea */
	    ((SVGArea) area).addGraphic(new RectGraphic(x, y, width, height));
	} else {
	    /* otherwise generate a warning */
	    System.err.println("WARNING: svg:rect outside svg:svg");
	}

	/* return status */
	return OK;
    }
}
