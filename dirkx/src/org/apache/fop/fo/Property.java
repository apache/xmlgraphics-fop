package org.apache.xml.fop.fo;

import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.apps.FOPException;

public class Property {

  public static class Maker {

    public boolean isInherited() { return false; }

    public Property make(PropertyList propertyList, String value) throws FOPException {
      return null;
    }

    public Property make(PropertyList propertyList) throws FOPException { // default
      return null;
    }

    public Property compute(PropertyList propertyList) { // compute
      return null;
    }
  }
    protected PropertyList propertyList;
    
  public Length getLength() { return null; }
  public String getString() { return null; }
  public ColorType getColorType() { return null; }
  public int getEnum() { return 0; }

  public static double toDouble(String s) {
    double d;
    try {
      d = Double.valueOf(s).doubleValue();
    } catch (NumberFormatException e) {
      d = Double.NaN;
    }
    return d;
  }

}
