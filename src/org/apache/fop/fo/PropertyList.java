package org.apache.xml.fop.fo;

import java.util.Hashtable;

import org.apache.xml.fop.apps.FOPException;

public class PropertyList extends Hashtable {
  private PropertyListBuilder builder;
  private PropertyList parentPropertyList = null;

  public PropertyList(PropertyList parentPropertyList) {
    this.parentPropertyList = parentPropertyList;
  }

  public Property get(String propertyName) {

    if (builder == null)
      System.err.println("OH OH, builder has not been set");
    Property p = (Property)super.get(propertyName);
		
    if (p == null) { // if not explicit
      p = this.builder.computeProperty(this,propertyName);
      if (p == null) { // else inherit
        if ((this.parentPropertyList != null)&&(this.builder.isInherited(propertyName))) { // check for parent
          p = this.parentPropertyList.get(propertyName); // retrieve parent's value
        } else { // default
          try {
            p = this.builder.makeProperty(this,propertyName);
          } catch (FOPException e) {
            // don't know what to do here
          }
        }
      }
    }
    return p;
  }

  public void setBuilder(PropertyListBuilder builder) {
    this.builder = builder;
  }
}
