package org.apache.fop.fo;

public interface ShorthandParser {
  public Property getValueForProperty(String propName, Property.Maker maker,
    PropertyList propertyList);
}
