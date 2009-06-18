package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class InlineSequence extends FObjMixed {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException { 
	    return new InlineSequence(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new InlineSequence.Maker();
    }
    
    public InlineSequence(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
	this.name = "fo:inline-sequence";
	
	if (parent.getName().equals("fo:flow")) {
	    throw new FOPException("inline-sequence can't be directly"
				   + " under flow"); 
	}
    }

}
