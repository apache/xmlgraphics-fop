package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.fo.pagination.PageSequence;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Hashtable;
import java.util.Enumeration;

public class Flow extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new Flow(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new Flow.Maker();
    }

    PageSequence pageSequence;

    protected Flow(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name =  "fo:flow";

	if (parent.getName().equals("fo:page-sequence")) {
	    this.pageSequence = (PageSequence) parent;
	} else {
	    throw new FOPException("flow must be child of "
				   + "page-sequence, not "
				   + parent.getName());
	}
	pageSequence.setFlow(this);
    }
	
    public int layout(Area area) throws FOPException {
	if (this.marker == START) {
	    this.marker = 0;
	}
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    int status;
	    if ((status = fo.layout(area)) != OK) {
		this.marker = i;
		return status;
	    }
	}
	return OK;
    }
}
