package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.layout.*;
import org.apache.xml.fop.apps.FOPException;

public class TableColumn extends FObj {
	
    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new TableColumn(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new TableColumn.Maker();
    }

    public TableColumn(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:table-column";
    }

    public int getColumnWidth() {
	return this.properties.get("column-width").getLength().mvalue();
    }

    public int getColumnNumber() {
	return 0; // not implemented yet
    }
}
