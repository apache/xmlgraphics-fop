/*-- $Id$ -- 
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */
package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.RegionArea;
import org.apache.fop.apps.FOPException;				   

public class RegionBefore extends Region {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList) throws FOPException {
	    return new RegionBefore(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new RegionBefore.Maker();
    }	
	
    public static final String REGION_CLASS = "before";
    
	private int precedence;

    protected RegionBefore(FObj parent, PropertyList propertyList)
	throws FOPException 
    {
	super(parent, propertyList);
	precedence = this.properties.get("precedence").getEnum();
    }
    

    RegionArea makeRegionArea(int allocationRectangleXPosition,
		      int allocationRectangleYPosition,
		      int allocationRectangleWidth,
		      int allocationRectangleHeight) {
	int extent = this.properties.get("extent").getLength().mvalue();
		
	return new RegionArea(allocationRectangleXPosition,
			  allocationRectangleYPosition,
			  allocationRectangleWidth, extent);
    }


    protected String getDefaultRegionName() 
    {
	return "xsl-region-before";
    }
    
    protected String getElementName() 
    {
	return "fo:region-before";
    }

    public String getRegionClass() 
    {
	return REGION_CLASS;
    }

	public boolean getPrecedence()
	{
		return (precedence == Precedence.TRUE ? true : false);
	}
}
