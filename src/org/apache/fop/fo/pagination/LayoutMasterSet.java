package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.apps.FOPException;				   

// Java
import java.util.Hashtable;

public class LayoutMasterSet extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new LayoutMasterSet(parent,propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new LayoutMasterSet.Maker();
    }

    private Hashtable layoutMasters;
    private Root root;
	
    protected LayoutMasterSet(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:layout-master-set";

	this.layoutMasters = new Hashtable();
	if (parent.getName().equals("fo:root")) {
	    this.root = (Root)parent;
	    root.setLayoutMasterSet(this);
	} else {
	    throw
		new FOPException("fo:layout-master-set must be child of fo:root, not "
				 + parent.getName());
	}
    }

    protected void addLayoutMaster(String masterName, SimplePageMaster layoutMaster) {
	this.layoutMasters.put(masterName, layoutMaster);
    }

    protected SimplePageMaster getLayoutMaster(String masterName) {
	return (SimplePageMaster)this.layoutMasters.get(masterName);
    }
}
