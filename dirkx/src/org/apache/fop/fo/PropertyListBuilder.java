package org.apache.xml.fop.fo;

import org.apache.xml.fop.fo.properties.*;
import org.apache.xml.fop.svg.*;

import org.apache.xml.fop.apps.FOPException;

import org.xml.sax.AttributeList;

import java.util.Hashtable;

public class PropertyListBuilder {
  private Hashtable propertyTable;

  public PropertyListBuilder() {
    this.propertyTable = new Hashtable();

    propertyTable.put("end-indent",EndIndent.maker());
    propertyTable.put("page-master-name",PageMasterName.maker());
    propertyTable.put("page-master-first",PageMasterFirst.maker());
    propertyTable.put("page-master-repeating",PageMasterRepeating.maker());
    propertyTable.put("page-master-odd",PageMasterOdd.maker());
    propertyTable.put("page-master-even",PageMasterEven.maker());
    propertyTable.put("margin-top",MarginTop.maker());
    propertyTable.put("margin-bottom",MarginBottom.maker());
    propertyTable.put("margin-left",MarginLeft.maker());
    propertyTable.put("margin-right",MarginRight.maker());
    propertyTable.put("extent",Extent.maker());
    propertyTable.put("page-width",PageWidth.maker());
    propertyTable.put("page-height",PageHeight.maker());
    propertyTable.put("flow-name",FlowName.maker());
    propertyTable.put("font-family",FontFamily.maker());
    propertyTable.put("font-style",FontStyle.maker());
    propertyTable.put("font-weight",FontWeight.maker());
    propertyTable.put("font-size",FontSize.maker());
    propertyTable.put("line-height",LineHeight.maker());
    propertyTable.put("text-align",TextAlign.maker());
    propertyTable.put("text-align-last",TextAlignLast.maker());
    propertyTable.put("space-before.optimum",SpaceBeforeOptimum.maker());
    propertyTable.put("space-after.optimum",SpaceAfterOptimum.maker());
    propertyTable.put("start-indent",StartIndent.maker());
    propertyTable.put("end-indent",EndIndent.maker());
    propertyTable.put("provisional-distance-between-starts",ProvisionalDistanceBetweenStarts.maker());
    propertyTable.put("provisional-label-separation",ProvisionalLabelSeparation.maker());
    propertyTable.put("rule-thickness",RuleThickness.maker());
    propertyTable.put("color",Color.maker());
    propertyTable.put("wrap-option",WrapOption.maker());
    propertyTable.put("white-space-treatment",WhiteSpaceTreatment.maker());
    propertyTable.put("break-before",BreakBefore.maker());
    propertyTable.put("break-after",BreakAfter.maker());
    propertyTable.put("text-indent",TextIndent.maker());
    propertyTable.put("href",HRef.maker());
    propertyTable.put("column-width",ColumnWidth.maker());
    propertyTable.put("height",SVGLength.maker());
    propertyTable.put("width",SVGLength.maker());
    propertyTable.put("x",SVGLength.maker());
    propertyTable.put("y",SVGLength.maker());
    propertyTable.put("x1",SVGLength.maker());
    propertyTable.put("x2",SVGLength.maker());
    propertyTable.put("y1",SVGLength.maker());
    propertyTable.put("y2",SVGLength.maker());
  }

  public Property computeProperty(PropertyList propertyList, String propertyName) {

    Property p = null;
	
    Property.Maker propertyMaker = (Property.Maker)propertyTable.get(propertyName);
    if (propertyMaker != null) {
      p = propertyMaker.compute(propertyList);
    } else {
      //System.err.println("WARNING: property " + propertyName + " ignored");
    }
    return p;
  }

  public boolean isInherited(String propertyName) {
    boolean b;
	
    Property.Maker propertyMaker = (Property.Maker)propertyTable.get(propertyName);
    if (propertyMaker != null) {
      b = propertyMaker.isInherited();
    } else {
      //System.err.println("WARNING: Unknown property " + propertyName);
      b = true;
    }
    return b;
  }

  public PropertyList makeList(AttributeList attributes, PropertyList parentPropertyList) throws FOPException {
	
    PropertyList p = new PropertyList(parentPropertyList);
    p.setBuilder(this);
	
    for (int i = 0; i < attributes.getLength(); i++) {
      String attributeName = attributes.getName(i);
      Property.Maker propertyMaker = (Property.Maker)propertyTable.get(attributeName);
      if (propertyMaker != null) {
        p.put(attributeName,propertyMaker.make(p,attributes.getValue(i)));
      } else {
        //System.err.println("WARNING: property " + attributeName + " ignored");
      }
    }

    return p;
  }

  public Property makeProperty(PropertyList propertyList, String propertyName) throws FOPException {

    Property p = null;
	
    Property.Maker propertyMaker = (Property.Maker)propertyTable.get(propertyName);
    if (propertyMaker != null) {
      p = propertyMaker.make(propertyList);
    } else {
      //System.err.println("WARNING: property " + propertyName + " ignored");
    }
    return p;
  }
}
