package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.PageMasterFactory;
import org.apache.xml.fop.apps.FOPException;				   

public class SequenceSpecification extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SequenceSpecification(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SequenceSpecification.Maker();
    }

    private PageSequence pageSequence;
    private LayoutMasterSet layoutMasterSet;
    private PageMasterFactory firstPMF;
    private PageMasterFactory currentPMF;
	
    protected SequenceSpecification(FObj parent,
				    PropertyList propertyList)
	throws FOPException { 
	super(parent, propertyList);
	this.name = "fo:sequence-specification";

	if (parent.getName().equals("fo:page-sequence")) {
	    this.pageSequence = (PageSequence) parent;
	    this.pageSequence.setSequenceSpecification(this);
	} else {
	    throw new FOPException("sequence-specification must be child"
				   + " of page-sequence, not "
				   + parent.getName());
	}
	this.firstPMF = null;
	this.currentPMF = null;
    
}

    protected void addSequenceSpecifier(SequenceSpecifier sequenceSpecifier) {
	if (this.firstPMF == null) {
	    this.firstPMF = sequenceSpecifier.getPageMasterFactory();
	} else {
	    this.currentPMF.setNext(sequenceSpecifier.getPageMasterFactory());
	}
	this.currentPMF = sequenceSpecifier.getPageMasterFactory();
    }

    protected PageMasterFactory getFirstPageMasterFactory() {
	return this.firstPMF;
    }

    LayoutMasterSet getLayoutMasterSet() {
	return this.layoutMasterSet;
    }

    protected void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
	this.layoutMasterSet = layoutMasterSet;
    }
}
