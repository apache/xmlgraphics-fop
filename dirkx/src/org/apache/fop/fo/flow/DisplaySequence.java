package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class DisplaySequence extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new DisplaySequence(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new DisplaySequence.Maker();
    }

    public DisplaySequence(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:display-sequence";
    }

    public int layout(Area area) throws FOPException {
	if (this.marker == START) {
	    this.marker = 0;
	}
	// this is such common code, perhaps it should be in the super class
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FObj fo = (FObj) children.elementAt(i);
	    int status;
	    if ((status = fo.layout(area)) != OK) {
		/* message from child */
		if (status > OK) {
		    /* child still successful */
		    this.marker = i+1;
		} else {
		    /* child unsucessful */
		    this.marker = i;
		}
		return status;
	    }
	}
	return OK;
    }
}
