package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

// Java
import java.util.Enumeration;

public class ListItemLabel extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new ListItemLabel(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new ListItemLabel.Maker();
    }

    public ListItemLabel(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:list-item-label";
    }

    public int layout(Area area) throws FOPException {
	int numChildren = this.children.size();

	if (numChildren != 1) {
	    throw new FOPException("list-item-label must have exactly one block in this version of FOP");
	}
	Block block = (Block) children.elementAt(0);

	block.setIsInLabel();
	block.setDistanceBetweenStarts(this.distanceBetweenStarts);
	block.setLabelSeparation(this.labelSeparation);
	block.setBodyIndent(this.bodyIndent);

	int status;
	status = block.layout(area);
	area.addDisplaySpace(-block.getAreaHeight());
	return status;
    }
}
