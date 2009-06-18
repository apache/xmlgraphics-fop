package org.apache.xml.fop.fo.pagination;

import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.layout.PageMasterFactory;

abstract public class SequenceSpecifier extends FObj {
	
    protected SequenceSpecifier(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
    }

    public abstract PageMasterFactory getPageMasterFactory();
}
