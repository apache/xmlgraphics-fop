package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class ListItemBody extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ListItemBody(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ListItemBody.Maker();
    }
	
    public ListItemBody(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:list-item-body";
    }

    public int layout(Area area) throws FOPException {
	if (this.marker == START) {
	    this.marker = 0;
	}
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    fo.setIsInListBody();
	    fo.setDistanceBetweenStarts(this.distanceBetweenStarts);
	    fo.setLabelSeparation(this.labelSeparation);
	    fo.setBodyIndent(this.bodyIndent);
	    int status;
	    if ((status = fo.layout(area)) != OK) {
		this.marker = i;
		if ((i == 0) && (status == AREA_FULL_NONE)) {
		    return AREA_FULL_NONE;
		} else {
		    return AREA_FULL_SOME;
		}
	    }
	}
	return OK;
    }
}
