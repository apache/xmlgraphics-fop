
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

public class InitialPageNumber extends Property {

  public static class Maker extends Property.Maker {
    public boolean isInherited() { return true; }

    public Property make(PropertyList propertyList, String value) throws FOPException {

	int pageNum = 0;
	try {
	    pageNum = Integer.parseInt( value );

	    // round to 0 if less than 0; SL spec and this implementation
	    if (pageNum < 0)
		pageNum = 0;
	}
	catch (NumberFormatException nfe) {
	    System.err.println( "'initial-page-number' not numeric" );
	}

      return new InitialPageNumber(propertyList, pageNum );


    }

    public Property make(PropertyList propertyList) throws FOPException {
      return make(propertyList, "0");
    }
  }

  public static Property.Maker maker() {
    return new InitialPageNumber.Maker();
  }

  private int value;

  public InitialPageNumber(PropertyList propertyList, int explicitValue) {
    this.propertyList = propertyList;
    this.value = explicitValue;
  }

  public int getInitialPageNumber() {
    return this.value;
  }

}
