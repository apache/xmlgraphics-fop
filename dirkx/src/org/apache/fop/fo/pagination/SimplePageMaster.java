package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.PageMaster;
import org.apache.xml.fop.layout.Region;
import org.apache.xml.fop.apps.FOPException;				   

public class SimplePageMaster extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SimplePageMaster(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SimplePageMaster.Maker();
    }

    RegionBody regionBody;
    RegionBefore regionBefore;
    RegionAfter regionAfter;
	
    LayoutMasterSet layoutMasterSet;
    PageMaster pageMaster;
		
    protected SimplePageMaster(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:simple-page-master";

	if (parent.getName().equals("fo:layout-master-set")) {
	    this.layoutMasterSet = (LayoutMasterSet) parent;
	    String pm = this.properties.get("page-master-name").getString();
	    if (pm == null) {
		System.err.println("WARNING: simple-page-master does not have "
				   + "a page-master-name and so is being ignored");
	    } else {
		this.layoutMasterSet.addLayoutMaster(pm, this);
	    }
	} else {
	    throw new FOPException("fo:simple-page-master must be child "
				   + "of fo:layout-master-set, not " 
				   + parent.getName());
	}
    }
	
    protected void end() {
	int pageWidth = this.properties.get("page-width").getLength().mvalue();
	int pageHeight = this.properties.get("page-height").getLength().mvalue();

	int marginTop = this.properties.get("margin-top").getLength().mvalue();
	int marginBottom = this.properties.get("margin-bottom").getLength().mvalue();
	int marginLeft = this.properties.get("margin-left").getLength().mvalue();
	int marginRight = this.properties.get("margin-right").getLength().mvalue();

	int contentRectangleXPosition = marginLeft;
	int contentRectangleYPosition = pageHeight - marginTop;
	int contentRectangleWidth = pageWidth - marginLeft - marginRight;
	int contentRectangleHeight = pageHeight - marginTop - marginBottom;
		
	this.pageMaster = new PageMaster(pageWidth, pageHeight);
	this.pageMaster.addBody(this.regionBody.makeRegion(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
		
	if (this.regionBefore != null)
	    this.pageMaster.addBefore(this.regionBefore.makeRegion(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
	if (this.regionAfter != null)
	    this.pageMaster.addAfter(this.regionAfter.makeRegion(contentRectangleXPosition,contentRectangleYPosition,contentRectangleWidth,contentRectangleHeight));
    }

    PageMaster getPageMaster() {
	return this.pageMaster;
    }

    protected void setRegionAfter(RegionAfter region) {
	this.regionAfter = region;
    }

    protected void setRegionBefore(RegionBefore region) {
	this.regionBefore = region;
    }

    protected void setRegionBody(RegionBody region) {
	this.regionBody = region;
    }
}
