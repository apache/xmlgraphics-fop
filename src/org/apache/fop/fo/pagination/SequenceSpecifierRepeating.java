package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.PageMasterFactory;
import org.apache.xml.fop.layout.RepeatingPageMasterFactory;
import org.apache.xml.fop.layout.PageMaster;
import org.apache.xml.fop.apps.FOPException;				   

public class SequenceSpecifierRepeating extends SequenceSpecifier {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SequenceSpecifierRepeating(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SequenceSpecifierRepeating.Maker();
    }

    private SequenceSpecification sequenceSpecification;
    private LayoutMasterSet layoutMasterSet;
    private RepeatingPageMasterFactory pageMasterFactory;
	
    protected SequenceSpecifierRepeating(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	PageMaster pf, pr;

	this.name =  "fo:sequence-specifer-repeating";
		
	if (parent.getName().equals("fo:sequence-specification")) {
	    this.sequenceSpecification = (SequenceSpecification) parent;
	    this.layoutMasterSet = this.sequenceSpecification.getLayoutMasterSet();
	} else {
	    throw new FOPException("sequence-specifier-repeating must be "
				   + "child of fo:sequence-specification, "
				   + "not " + parent.getName());
	}

	String pageMasterFirst = this.properties.get("page-master-first").getString();
	String pageMasterRepeating = this.properties.get("page-master-repeating").getString();
	try {
	    pf = this.layoutMasterSet.getLayoutMaster(pageMasterFirst).getPageMaster();
	    pr = this.layoutMasterSet.getLayoutMaster(pageMasterRepeating).getPageMaster();
	    this.pageMasterFactory = new RepeatingPageMasterFactory(pf, pr);
	} catch (java.lang.NullPointerException e) {
	    throw new FOPException("at least one of the page-master names in "
				   + "sequence-specifier-repeating is not in "
				   + "layout-master-set");
	}
	this.sequenceSpecification.addSequenceSpecifier(this);
    }
    
    public PageMasterFactory getPageMasterFactory() {
	return this.pageMasterFactory;
    }
}
