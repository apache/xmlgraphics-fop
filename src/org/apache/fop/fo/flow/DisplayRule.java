package org.apache.xml.fop.fo.flow;

// FOP
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.layout.Area;
import org.apache.xml.fop.layout.BlockArea;
import org.apache.xml.fop.layout.RuleArea;
import org.apache.xml.fop.layout.FontState;
import org.apache.xml.fop.apps.FOPException;

public class DisplayRule extends FObj {

    public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
	    throws FOPException {
	    return new DisplayRule(parent, propertyList);
	}
    }

    public static FObj.Maker maker() {
	return new DisplayRule.Maker();
    }

    public DisplayRule(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:display-rule";
    }

    public int layout(Area area) throws FOPException {
	// FIXME: doesn't check to see if it will fit

	String fontFamily = this.properties.get("font-family").getString();
	String fontStyle = this.properties.get("font-style").getString();
	String fontWeight = this.properties.get("font-weight").getString();
	int fontSize = this.properties.get("font-size").getLength().mvalue();
		
	FontState fs = new FontState(area.getFontInfo(), fontFamily,
				     fontStyle, fontWeight, fontSize);

	int align = this.properties.get("text-align").getEnum(); 
	int startIndent =
	    this.properties.get("start-indent").getLength().mvalue(); 
	int endIndent =
	    this.properties.get("end-indent").getLength().mvalue(); 
	int spaceBefore =
	    this.properties.get("space-before.optimum").getLength().mvalue(); 
	int spaceAfter =
	    this.properties.get("space-after.optimum").getLength().mvalue(); 
	int ruleThickness =
	    this.properties.get("rule-thickness").getLength().mvalue(); 
	int ruleLength = 0; // not used;

	ColorType c = this.properties.get("color").getColorType();
	float red = c.red();
	float green = c.green();
	float blue = c.blue();
	
	if (area instanceof BlockArea) {
	    area.end();
	}

	if (spaceBefore != 0) {
	    area.addDisplaySpace(spaceBefore);
	}

	RuleArea ruleArea = new RuleArea(fs,
					 area.getAllocationWidth(),
					 area.spaceLeft(),
					 startIndent, endIndent,
					 align, ruleThickness,
					 ruleLength, red, green,
					 blue);
	area.addChild(ruleArea);
	area.increaseHeight(ruleArea.getHeight());
	
	if (spaceAfter != 0) {
	    area.addDisplaySpace(spaceAfter);
	}

	if (area instanceof BlockArea) {
	    area.start();
	}

	return OK;
    }
}
