/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.fo;

import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.fo.expr.PropertyInfo;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.apps.FOPException;

public class Property {

  public static class Maker {
    private static final String UNKNOWN = "UNKNOWN";
    private String propName;

    /**
     * Return the name of the property whose value is being set.
     */
    protected String getPropName() { return propName; }

    /**
     * Construct an instance of a Property.Maker for the given property.
     * @param propName The name of the property to be made.
     */
    protected Maker(String propName) {
      this.propName = propName;
    }

    /**
     * Construct an instance of a Property.Maker.
     * Note: the property name is set to "UNKNOWN".
     */
    protected Maker() {
      this.propName = UNKNOWN;
    }


    /**
     * Default implementation of isInherited.
     * @return A boolean indicating whether this property is inherited.
     */
    public boolean isInherited() { return false; }

    /**
     * Return a boolean indicating whether this property inherits the
     * "specified" value rather than the "computed" value. The default is
     * to inherit the "computed" value.
     * @return If true, property inherits the value specified.
     */
    public boolean inheritsSpecified() { return false; }


    /**
     * Return an object implementing the PercentBase interface.
     * This is used to handle properties specified as a percentage of
     * some "base length", such as the content width of their containing
     * box.
     * Overridden by subclasses which allow percent specifications. See
     * the documentation on properties.xsl for details.
     */
    public PercentBase getPercentBase(FObj fo, PropertyList pl) {
      return null;
    }

    /**
     * Return a Maker object which is used to set the values on components
     * of compound property types, such as "space".
     * Overridden by property maker subclasses which handle
     * compound properties.
     * @param subprop The name of the component for which a Maker is to
     * returned, for example "optimum", if the FO attribute is
     * space.optimum='10pt'.
     */
    protected Maker getSubpropMaker(String subprop) {
      return null;
    }

    /**
     * Return a property value for the given component of a compound
     * property.
     * @param p A property value for a compound property type such as
     * SpaceProperty.
     * @param subprop The name of the component whose value is to be
     * returned.
     * NOTE: this is only to ease porting when calls are made to
     * PropertyList.get() using a component name of a compound property,
     * such as get("space.optimum"). The recommended technique is:
     * get("space").getOptimum().
     * Overridden by property maker subclasses which handle
     * compound properties.
     */
    public Property getSubpropValue(Property p, String subprop) {
      return null;
    }

    /**
     * Return a property value for a compound property. If the property
     * value is already partially initialized, this method will modify it.
     * @param baseProp The Property object representing the compound property,
     * such as SpaceProperty.
     * @param partName The name of the component whose value is specified.
     * @param propertyList The propertyList being built.
     * @param fo The FO whose properties are being set.
     * @return A compound property object.
     */
    public Property make(Property baseProp, String partName,
			 PropertyList propertyList, String value, FObj fo)
      throws FOPException
    {
      if (baseProp==null) {
	baseProp = makeCompound(propertyList, fo);
      }
      Maker spMaker = getSubpropMaker(partName);
      if (spMaker != null) {
	Property p = spMaker.make(propertyList, value, fo);
	if (p != null) {
	  return setSubprop(baseProp, partName, p);
	}
      }
      else {
	MessageHandler.errorln("WARNING: compound property component " + partName + " unknown.");
      }
      return baseProp;
    }

    /**
     * Set a component in a compound property and return the modified
     * compound property object.
     * This default implementation returns the original base property
     * without modifying it.
     * It is overridden by property maker subclasses which handle
     * compound properties.
     * @param baseProp The Property object representing the compound property,
     * such as SpaceProperty.
     * @param partName The name of the component whose value is specified.
     * @param subProp A Property object holding the specified value of the
     * component to be set.
     * @return The modified compound property object.
     */
    protected Property setSubprop(Property baseProp, String partName,
				  Property subProp) {
      return baseProp;
    }

    /**
     * Create a Property object from an attribute specification.
     * @param propertyList The PropertyList object being built for this FO.
     * @param value The attribute value.
     * @param fo The current FO whose properties are being set.
     * @return The initialized Property object.
     */
    public Property make(PropertyList propertyList, String value, FObj fo)
      throws FOPException {
      try {
	Property pret=null;
	String pvalue=value;
	pret = checkEnumValues(value);
	if (pret == null) {
	  /* Check for keyword shorthand values to be substituted. */
	  pvalue = checkValueKeywords(value);
	  // Override parsePropertyValue in each subclass of Property.Maker
	  Property p = PropertyParser.parse(pvalue,
			     new PropertyInfo(this, propertyList, fo));
	  pret = convertProperty(p, propertyList, fo);
	}
	else if (isCompoundMaker()) {
	  pret = convertProperty(pret, propertyList, fo);
	}
	if (pret == null) {
	  throw new org.apache.fop.fo.expr.PropertyException("No conversion defined");
	}
	else if (inheritsSpecified()) {
	  pret.setSpecifiedValue(pvalue);
	}
	return pret;
      } catch (org.apache.fop.fo.expr.PropertyException propEx) {
	MessageHandler.errorln("Error in " + propName + " property value '"
			       + value + "': " + propEx);
	throw new FOPException("Property error");
      }
    }

    protected boolean isCompoundMaker() {
      return false;
    }

    public Property checkEnumValues(String value) {
      return null;
    }

    /**
     * Return a String to be parsed if the passed value corresponds to
     * a keyword which can be parsed and used to initialize the property.
     * For example, the border-width family of properties can have the
     * initializers "thin", "medium", or "thick". The foproperties.xml
     * file specifies a length value equivalent for these keywords,
     * such as "0.5pt" for "thin". These values are considered parseable,
     * since the Length object is no longer responsible for parsing
     * unit expresssions.
     * @param value The string value of property attribute.
     * @return A String containging a parseable equivalent or null if
     * the passed value isn't a keyword initializer for this Property.
     */
    protected String checkValueKeywords(String value) {
      return value;
    }

    /**
     * Return a Property object based on the passed Property object.
     * This method is called if the Property object built by the parser
     * isn't the right type for this property.
     * It is overridden by subclasses when the property specification in
     * foproperties.xml specifies conversion rules.
     * @param p The Property object return by the expression parser
     * @param propertyList The PropertyList object being built for this FO.
     * @param fo The current FO whose properties are being set.
     * @return A Property of the correct type or null if the parsed value
     * can't be converted to the correct type.
     */
    public Property convertProperty(Property p,
	PropertyList propertyList, FObj fo) throws FOPException {
      return null;
    }

    protected Property convertPropertyDatatype(Property p,
	PropertyList propertyList, FObj fo) {
      return null;
    }

    /**
     * Return a Property object representing the initial value.
     * @param propertyList The PropertyList object being built for this FO.
     */
    public Property make(PropertyList propertyList) throws FOPException {
      return null;
    }

    /**
     * Return a Property object representing the initial value.
     * @param propertyList The PropertyList object being built for this FO.
     * @param parentFO The parent FO for the FO whose property is being made.
     * @return a Property subclass object holding a "compound" property object
     * initialized to the default values for each component.
     */
    protected Property makeCompound(PropertyList propertyList, FObj parentFO) throws FOPException {
      return null;
    }

    /**
     * Return a Property object representing the value of this property,
     * based on other property values for this FO.
     * A special case is properties which inherit the specified value,
     * rather than the computed value.
     * @param propertyList The PropertyList for the FO.
     * @return Property A computed Property value or null if no rules
     * are specified (in foproperties.xml) to compute the value.
     */
    public Property compute(PropertyList propertyList) throws FOPException {
      if (inheritsSpecified()) {
	// recalculate based on last specified value
	// Climb up propertylist and find last spec'd value
	// NEED PROPNAME!!! get from Maker
	Property specProp = propertyList.getNearestSpecified(propName);
	if (specProp != null) {
	  // Only need to do this if the value is relative!!!
	  String specVal = specProp.getSpecifiedValue();
	  if (specVal != null) {
	    try {
	      return make(propertyList, specVal, propertyList.getParentFObj());
	    } catch (FOPException e) {
	      MessageHandler.errorln("Error computing property value for " +
				 propName + " from " + specVal);
	      return null;
	    }
	  }
	}
      }
      return null; // standard
    }
    
    public boolean isCorrespondingForced(PropertyList propertyList) {
    	return false;
    }

  } // end of nested Maker class

  /** The original specified value for properties which inherit
   * specified values. */
  private String specVal;

  /**
   * Set the original value specified for the property attribute.
   * @param specVal The specified value.
   */
    public void setSpecifiedValue(String specVal) {
      this.specVal = specVal;
    }

  /**
   * Return the original value specified for the property attribute.
   * @return The specified value as a String.
   */
    public String getSpecifiedValue() {
      return specVal;
    }

  /**
   * Accessor functions for all possible Property datatypes
   */
  public Length getLength() { return null; }
  public ColorType getColorType() { return null; }
  public CondLength getCondLength() { return null; }
  public LengthRange getLengthRange() { return null; }
  public Space getSpace() { return null; }
  public Keep getKeep() { return null; }
  /* KL: first decide which direction to go with this!
  public KeepValue getKeepValue() { return null; }
  public Precedence getPrecedence() { return null; }
  */
  public int getEnum() { return 0; }
  public char getCharacter() { return 0;}

  public Number getNumber() { return null; }

  // Classes used when evaluating property expressions
  public Numeric getNumeric() { return null; }
  public String getNCname() { return null; }

  public Object getObject() { return null; }

  public String getString() {
    Object o = getObject();
    return (o == null)? null : o.toString();
  }

}
