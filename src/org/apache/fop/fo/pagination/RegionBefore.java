package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Region;
import org.apache.xml.fop.apps.FOPException;				   

public class RegionBefore extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList) throws FOPException {
	    return new RegionBefore(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new RegionBefore.Maker();
    }	
	
    SimplePageMaster layoutMaster;

    protected RegionBefore(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:region-before";

	if (parent.getName().equals("fo:simple-page-master")) {
	    this.layoutMaster = (SimplePageMaster) parent;
	    this.layoutMaster.setRegionBefore(this);
	} else {
	    throw new FOPException("region-before must be child of " 
				   + "simple-page-master, not " 
				   + parent.getName());
	}
    }

    Region makeRegion(int allocationRectangleXPosition,
		      int allocationRectangleYPosition,
		      int allocationRectangleWidth,
		      int allocationRectangleHeight) {
	int marginTop = this.properties.get("margin-top").getLength().mvalue();
	int marginBottom = this.properties.get("margin-bottom").getLength().mvalue();
	int marginLeft = this.properties.get("margin-left").getLength().mvalue();
	int marginRight = this.properties.get("margin-right").getLength().mvalue();
	int extent = this.properties.get("extent").getLength().mvalue();
		
	return new Region(allocationRectangleXPosition + marginLeft,
			  allocationRectangleYPosition - marginTop,
			  allocationRectangleWidth - marginLeft -
			  marginRight, extent);
    }
}
