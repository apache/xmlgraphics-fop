package org.apache.xml.fop.fo;

import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;

/**
 * base class for representation of mixed content formatting objects
 * and their processing 
 */
public class FObjMixed extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new FObjMixed(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new FObjMixed.Maker();
    }

    protected FObjMixed(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
    }

    protected void addCharacters(char data[], int start, int length) { 
	children.addElement(new FOText(data,start,length,this));
    }

    public int layout(Area area) throws FOPException {

	if (this.marker == START) {
	    this.marker = 0;
	}

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
	    FONode fo = (FONode) children.elementAt(i);
	    int status;
	    if ((status = fo.layout(area)) != OK) {
		this.marker = i;
		return status;
	    }
	}
	return OK;
    }
}

