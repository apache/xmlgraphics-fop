package org.apache.xml.fop.fo.pagination;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.PageMasterFactory;
import org.apache.xml.fop.layout.AlternatingPageMasterFactory;
import org.apache.xml.fop.layout.PageMaster;
import org.apache.xml.fop.apps.FOPException;				   

public class SequenceSpecifierAlternating extends SequenceSpecifier {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new SequenceSpecifierAlternating(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new SequenceSpecifierAlternating.Maker();
    }
	
    private SequenceSpecification sequenceSpecification;
    private LayoutMasterSet layoutMasterSet;
    private AlternatingPageMasterFactory pageMasterFactory;
	
    protected SequenceSpecifierAlternating(FObj parent,
					   PropertyList propertyList)
	throws FOPException { 
	super(parent, propertyList);
	PageMaster pf, pe, po;

	this.name =  "fo:sequence-specifer-alternating";
		
	if (parent.getName().equals("fo:sequence-specification")) {
	    this.sequenceSpecification = (SequenceSpecification) parent;
	    this.layoutMasterSet = this.sequenceSpecification.getLayoutMasterSet();
	} else {
	    throw new FOPException("fo:sequence-specifier-alternating must be "
				   + " child of fo:sequence-specification, not "
				   + parent.getName());
	}
         
       String pageMasterFirst = this.properties.get("page-master-first").getString();
       String pageMasterOdd = this.properties.get("page-master-odd").getString();
       String pageMasterEven = this.properties.get("page-master-even").getString();
       
       try {
	   pf = this.layoutMasterSet.getLayoutMaster(pageMasterFirst).getPageMaster();
	   pe = this.layoutMasterSet.getLayoutMaster(pageMasterEven).getPageMaster();
	   po = this.layoutMasterSet.getLayoutMaster(pageMasterOdd).getPageMaster();
	   this.pageMasterFactory = new AlternatingPageMasterFactory(pf,pe,po);
       } catch (java.lang.NullPointerException e) {
	   throw new FOPException("at least one of the page-master names in"
				  + " sequence-specifier-alternating is not in"
				  + " layout-master-set");
       }
       this.sequenceSpecification.addSequenceSpecifier(this);
    }

    public PageMasterFactory getPageMasterFactory() {
	return this.pageMasterFactory;
    }
}
