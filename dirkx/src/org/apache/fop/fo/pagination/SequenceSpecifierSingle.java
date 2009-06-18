package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.PageMasterFactory;
import org.apache.xml.fop.layout.SinglePageMasterFactory;
import org.apache.xml.fop.layout.PageMaster;
import org.apache.xml.fop.apps.FOPException;				   

public class SequenceSpecifierSingle extends SequenceSpecifier {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException { 
	    return new SequenceSpecifierSingle(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SequenceSpecifierSingle.Maker();
    }

    private SequenceSpecification sequenceSpecification;
    private LayoutMasterSet layoutMasterSet;
    private SinglePageMasterFactory pageMasterFactory;
	
    protected SequenceSpecifierSingle(FObj parent, PropertyList propertyList)
	throws FOPException { 
	super(parent, propertyList);
	this.name =  "fo:sequence-specifer-single";
		
	if (parent.getName().equals("fo:sequence-specification")) {
	    this.sequenceSpecification = (SequenceSpecification) parent;
	    this.layoutMasterSet = this.sequenceSpecification.getLayoutMasterSet();
	} else {
	    throw new FOPException("sequence-specifier-single must be "
				   + "child of fo:sequence-specification, "
				   + "not " + parent.getName());
	}

	String pageMasterName = this.properties.get("page-master-name").getString();
	try {
	    this.pageMasterFactory = new SinglePageMasterFactory(this.layoutMasterSet.getLayoutMaster(pageMasterName).getPageMaster());  
	} catch (java.lang.NullPointerException e) {
	    throw new FOPException("page-master-name " +
				   pageMasterName + " must be in layout-master-set");  
	}
	this.sequenceSpecification.addSequenceSpecifier(this);
    }

    public PageMasterFactory getPageMasterFactory() {
	return this.pageMasterFactory;
    }

    public String getPageMasterName() {
	return this.properties.get("page-master-name").getString();
    }
}
