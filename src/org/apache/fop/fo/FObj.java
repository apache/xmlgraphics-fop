package org.apache.xml.fop.fo;

// FOP
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * base class for representation of formatting objects and their processing
 */
public class FObj extends FONode {

    public static class Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new FObj(parent, propertyList);
	}
    }

    public static Maker maker() {
	return new Maker();
    }

    protected PropertyList properties;
    protected String name;

    protected FObj(FObj parent, PropertyList propertyList) {
	super(parent);
	this.properties = propertyList;
	this.name = "default FO";
    }

    protected void addCharacters(char data[], int start, int length) {
	// ignore
    }

    public int layout(Area area) throws FOPException {
	// should always be overridden
	return OK;
    }

    public String getName() {
	return this.name;
    }

    protected void start() {
	// do nothing by default
    }

    protected void end() {
	// do nothing by default
    }
}

