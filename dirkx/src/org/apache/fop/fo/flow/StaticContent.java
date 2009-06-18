package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.fo.pagination.PageSequence;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;				   

// Java
import java.util.Enumeration;

public class StaticContent extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new StaticContent(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new StaticContent.Maker();
    }

    PageSequence pageSequence;

    protected StaticContent(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name =  "fo:static-content";

	if (parent.getName().equals("fo:page-sequence")) {
	    this.pageSequence = (PageSequence) parent;
	} else {
	    throw new FOPException("static-content must be child of "
				   + "fo:page-sequence, not "
				   + parent.getName());  
	}
	String flowName = this.properties.get("flow-name").getString();

	pageSequence.setStaticContent(flowName, this);
    }
    
    public int layout(Area area) throws FOPException {
	int numChildren = this.children.size();
	for (int i = 0; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    fo.layout(area);
	}
	resetMarker();
	return OK;
    }
}
