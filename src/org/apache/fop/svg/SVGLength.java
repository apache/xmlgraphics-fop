package org.apache.xml.fop.svg;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.apps.FOPException;

/**
 * a class representing all the length properties in SVG
 */
public class SVGLength extends Property {
	
    /**
     * inner class for making SVG Length objects.
     */
    public static class Maker extends Property.Maker {

	/**
	 * whether this property is inherited or not.
	 *
	 * @return is this inherited?
	 */
	public boolean isInherited() { return false; }
	
	/**
	 * make an SVG Length property with the given value.
	 *
	 * @param propertyList the property list this is a member of
	 * @param value the explicit string value of the property
	 */
	public Property make(PropertyList propertyList, String value)
	    throws FOPException {
	    return new SVGLength(propertyList, new Length(value));
	}
		
	/** 
	 * make an SVG Length property with the default value.
	 *
	 * @param propertyList the property list the property is a member of
	 */
	public Property make(PropertyList propertyList) throws FOPException {
	    return make(propertyList, "0pt");
	}
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for SVG Length objects
     */
    public static Property.Maker maker() {
	return new SVGLength.Maker();
    }

    /** the length as a Length object */
    protected Length value;
	
    /**
     * construct an SVG length (called by the Maker).
     *
     * @param propertyList the property list this is a member of
     * @param explicitValue the explicit value as a Length object
     */
    protected SVGLength(PropertyList propertyList, Length explicitValue) {
	this.propertyList = propertyList;
	this.value = explicitValue;
    }

    /**
     * get the length
     *
     * @return the length as a Length object
     */
    public Length getLength() {
        return this.value;
    }
}
