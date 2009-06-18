package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.BlockArea;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

/**
 * class representing svg:svg pseudo flow object.
 */
public class SVG extends FObj {

    /**
     * inner class for making SVG objects.
     */
    public static class Maker extends FObj.Maker {

	/**
	 * make an SVG object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the SVG object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SVG(parent, propertyList);
	}
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for SVG objects
     */
    public static FObj.Maker maker() {
	return new SVG.Maker();
    }

    FontState fs;
    int breakBefore;
    int breakAfter;
    int width;
    int height;
    int spaceBefore;
    int spaceAfter;

    /**
     * constructs an SVG object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public SVG(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:svg";
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     */
    public int layout(Area area) throws FOPException {
	
	if (this.marker == BREAK_AFTER) {
	    return OK;
	}

	if (this.marker == START) {
	    /* retrieve properties */
	    String fontFamily = this.properties.get("font-family").getString();
	    String fontStyle = this.properties.get("font-style").getString();
	    String fontWeight = this.properties.get("font-weight").getString();
	    int fontSize = this.properties.get("font-size").getLength().mvalue();
	    
	    this.fs = new FontState(area.getFontInfo(), fontFamily,
					 fontStyle, fontWeight, fontSize);
	    
	    this.breakBefore = this.properties.get("break-before").getEnum();
	    this.breakAfter = this.properties.get("break-after").getEnum();
	    this.width = this.properties.get("width").getLength().mvalue();
	    this.height = this.properties.get("height").getLength().mvalue();
	    
	    this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();
	    this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue();
	    /* if the SVG is embedded in a block area */
	    if (area instanceof BlockArea) {
		/* temporarily end the block area */
		area.end();
	    }
	    
	    this.marker = 0;

	    if (breakBefore == BreakBefore.PAGE) {
		return FORCE_PAGE_BREAK;
	    }

	    if (breakBefore == BreakBefore.ODD_PAGE) {
		return FORCE_PAGE_BREAK_ODD;
	    }

	    if (breakBefore == BreakBefore.EVEN_PAGE) {
		return FORCE_PAGE_BREAK_EVEN;
	    }
	}
       
	/* if there is a space-before */
	if (spaceBefore != 0) {
	    /* add a display space */
	    area.addDisplaySpace(spaceBefore);
	}

	/* create an SVG area */
	SVGArea svgArea = new SVGArea(fs, width, height);
	svgArea.start();

	/* add the SVG area to the containing area */
	area.addChild(svgArea);

	/* iterate over the child formatting objects and lay them out
	   into the SVG area */
	int numChildren = this.children.size();
	for (int i = 0; i < numChildren; i++) {
	    FONode fo = (FONode) children.elementAt(i);
	    int status;
	    if ((status = fo.layout(svgArea)) != OK) {
		return status;
	    }
	}

	/* finish off the SVG area */
	svgArea.end();

	/* increase the height of the containing area accordingly */
	area.increaseHeight(svgArea.getHeight());

	/* if there is a space-after */
	if (spaceAfter != 0) {
	    /* add a display space */
	    area.addDisplaySpace(spaceAfter);
	}

	/* if the SVG is embedded in a block area */
	if (area instanceof BlockArea) {
	    /* re-start the block area */
	    area.start();
	}
	
	if (breakAfter == BreakAfter.PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK;
	}

	if (breakAfter == BreakAfter.ODD_PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK_ODD;
	}
	
	if (breakAfter == BreakAfter.EVEN_PAGE) {
	    this.marker = BREAK_AFTER;
	    return FORCE_PAGE_BREAK_EVEN;
	}

	/* return status */
	return OK;
    }
}
