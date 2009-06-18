package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.AreaTree;
import org.apache.xml.fop.apps.FOPException;				   

// Java
import java.util.Vector;
import java.util.Enumeration;

public class Root extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Root(parent, propertyList);
	}
    }
	
    public static FObj.Maker maker() {
	return new Root.Maker();
    }

    LayoutMasterSet layoutMasterSet;
    Vector pageSequences;
		
    protected Root(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name =  "fo:root";
		
	pageSequences = new Vector();
	if (parent != null) {
	    throw new FOPException("root must be root element");
	}
    }

    public void addPageSequence(PageSequence pageSequence) {
	this.pageSequences.addElement(pageSequence);
    }
	
    public LayoutMasterSet getLayoutMasterSet() {
	return this.layoutMasterSet;
    }
	
    public void format(AreaTree areaTree) throws FOPException {
	if (layoutMasterSet == null) {
	    throw new FOPException("No layout master set.");
	}
	Enumeration e = pageSequences.elements();
	while (e.hasMoreElements()) {
	    ((PageSequence) e.nextElement()).format(areaTree);
	}
    }

    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
	this.layoutMasterSet = layoutMasterSet;
    }
}
