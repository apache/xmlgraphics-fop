/*
 * $Id: Property.java,v 1.22 2003/03/05 21:48:02 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.CondLength;
import org.apache.fop.datatypes.Keep;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthPair;
import org.apache.fop.datatypes.LengthRange;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.datatypes.Space;
import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.fo.expr.PropertyInfo;
import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.apps.FOPException;
import java.util.Vector;

/**
 * Base class for all property objects
 * @author unascribed
 */
public class Property {

    /**
     * Base class for all property makers
     * @author unascribed
     */
    public static class Maker {
        private int propId;

        /**
         * @return the name of the property for this Maker
         */
        protected int getPropId() {
            return propId;
        }

        /**
         * Construct an instance of a Property.Maker for the given property.
         * @param propId The Constant ID of the property to be made.
         */
        protected Maker(int propId) {
            this.propId = propId;
        }

        /**
         * Construct an instance of a Property.Maker.
         * Note: the property ID is set to zero
         */
        protected Maker() {
            this.propId = 0;
        }


        /**
         * Default implementation of isInherited.
         * @return A boolean indicating whether this property is inherited.
         */
        public boolean isInherited() {
            return false;
        }

        /**
         * Return a boolean indicating whether this property inherits the
         * "specified" value rather than the "computed" value. The default is
         * to inherit the "computed" value.
         * @return true, if the property inherits the value specified.
         */
        public boolean inheritsSpecified() {
            return false;
        }


        /**
         * This is used to handle properties specified as a percentage of
         * some "base length", such as the content width of their containing
         * box.
         * Overridden by subclasses which allow percent specifications. See
         * the documentation on properties.xsl for details.
         * @param fo the FObj containing the PercentBase
         * @param pl the PropertyList containing the property. (TODO: explain
         * what this is used for, or remove it from the signature.)
         * @return an object implementing the PercentBase interface.
         */
        public PercentBase getPercentBase(FObj fo, PropertyList pl) {
            return null;
        }

        /**
         * Return a Maker object which is used to set the values on components
         * of compound property types, such as "space".
         * Overridden by property maker subclasses which handle
         * compound properties.
         * @param subprop The Constants ID of the component for which a Maker is to
         * returned, for example CP_OPTIMUM, if the FO attribute is
         * space.optimum='10pt'.
         * @return the Maker object specified
         */
        protected Maker getSubpropMaker(int subpropId) {
            return null;
        }

        /**
         * Return a property value for the given component of a compound
         * property.
         * @param p A property value for a compound property type such as
         * SpaceProperty.
         * @param subprop The Constants ID of the component whose value is to be
         * returned.
         * NOTE: this is only to ease porting when calls are made to
         * PropertyList.get() using a component name of a compound property,
         * such as get("space.optimum"). The recommended technique is:
         * get("space").getOptimum().
         * Overridden by property maker subclasses which handle
         * compound properties.
         * @return the Property containing the subproperty
         */
        public Property getSubpropValue(Property p, int subpropId) {
            return null;
        }

        /**
         * Return a property value for a compound property. If the property
         * value is already partially initialized, this method will modify it.
         * @param baseProp The Property object representing the compound property,
         * for example: SpaceProperty.
         * @param subpropId The Constants ID of the subproperty (component)
         *        whose value is specified.
         * @param propertyList The propertyList being built.
         * @param fo The FO whose properties are being set.
         * @param value the value of the
         * @return baseProp (or if null, a new compound property object) with
         * the new subproperty added
         * @throws FOPException for invalid or inconsistent FO input
         */
        public Property make(Property baseProp, int subpropId,
                             PropertyList propertyList, String value,
                             FObj fo) throws FOPException {
            if (baseProp == null) {
                baseProp = makeCompound(propertyList, fo);
            }

            Maker spMaker = getSubpropMaker(subpropId);

            if (spMaker != null) {
                Property p = spMaker.make(propertyList, value, fo);
                if (p != null) {
                    return setSubprop(baseProp, subpropId, p);
                }
            } else {
                //getLogger().error("compound property component "
                //                       + partName + " unknown.");
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
         * @param partId The ID of the component whose value is specified.
         * @param subProp A Property object holding the specified value of the
         * component to be set.
         * @return The modified compound property object.
         */
        protected Property setSubprop(Property baseProp, int partId,
                                      Property subProp) {
            return baseProp;
        }

        /**
         * Create a Property object from an attribute specification.
         * @param propertyList The PropertyList object being built for this FO.
         * @param value The attribute value.
         * @param fo The current FO whose properties are being set.
         * @return The initialized Property object.
         * @throws FOPException for invalid or inconsistent FO input
         */
        public Property make(PropertyList propertyList, String value,
                             FObj fo) throws FOPException {
            try {
                Property newProp = null;
                String pvalue = value;
                if ("inherit".equals(value)) {
                    newProp = propertyList.getFromParent(this.propId);
                } else {
                    newProp = checkEnumValues(value);
                }
                if (newProp == null) {
                    /* Check for keyword shorthand values to be substituted. */
                    pvalue = checkValueKeywords(value);
                    // Override parsePropertyValue in each subclass of Property.Maker
                    Property p = PropertyParser.parse(pvalue,
                                                      new PropertyInfo(this,
                                                      propertyList, fo));
                    newProp = convertProperty(p, propertyList, fo);
                } else if (isCompoundMaker()) {
                    newProp = convertProperty(newProp, propertyList, fo);
                }
                if (newProp == null) {
                    throw new org.apache.fop.fo.expr.PropertyException("No conversion defined");
                } else if (inheritsSpecified()) {
                    newProp.setSpecifiedValue(pvalue);
                }
                return newProp;
            } catch (org.apache.fop.fo.expr.PropertyException propEx) {
                String propName = FOPropertyMapping.getPropertyName(this.propId);
                throw new FOPException("Error in " + propName 
                                     + " property value '" + value + "': "
                                     + propEx);
            }
        }

        public Property convertShorthandProperty(PropertyList propertyList,
                                                 Property prop, FObj fo) {
            Property pret = null;
            try {
                pret = convertProperty(prop, propertyList, fo);
                if (pret == null) {
                    // If value is a name token, may be keyword or Enum
                    String sval = prop.getNCname();
                    if (sval != null) {
                        // System.err.println("Convert shorthand ncname " + sval);
                        pret = checkEnumValues(sval);
                        if (pret == null) {
                            /* Check for keyword shorthand values to be substituted. */
                            String pvalue = checkValueKeywords(sval);
                            if (!pvalue.equals(sval)) {
                                // System.err.println("Convert shorthand keyword" + pvalue);
                                // Substituted a value: must parse it
                                Property p =
                                    PropertyParser.parse(pvalue,
                                                         new PropertyInfo(this,
                                                                          propertyList,
                                                                          fo));
                                pret = convertProperty(p, propertyList, fo);
                            }
                        }
                    }
                }
            } catch (FOPException e) {

                //getLogger().error("convertShorthandProperty caught FOPException "
                //                       + e);
            } catch (org.apache.fop.fo.expr.PropertyException propEx) {
                //getLogger().error("convertShorthandProperty caught PropertyException "
                //                       + propEx);
            }
            if (pret != null) {
                /*
                 * System.err.println("Return shorthand value " + pret.getString() +
                 * " for " + getPropName());
                 */
            }
            return pret;
        }

        /**
         * Each property is either compound or not, with the default being not.
         * This method should be overridden by subclasses that are Makers of
         * compound properties.
         * @return true if this Maker makes instances of compound properties
         */
        protected boolean isCompoundMaker() {
            return false;
        }

        /**
         * For properties that contain enumerated values.
         * This method should be overridden by subclasses.
         * @param value the string containing the property value
         * @return the Property encapsulating the enumerated equivalent of the
         * input value
         */
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
         * @throws FOPException for invalid or inconsistent FO input
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList,
                                        FObj fo) throws FOPException {
            return null;
        }

        /**
         * For properties that have more than one legal way to be specified,
         * this routine should be overridden to attempt to set them based upon
         * the other methods. For example, colors may be specified using an RGB
         * model, or they may be specified using an NCname.
         * @param p property whose datatype should be converted
         * @param propertyList collection of properties. (TODO: explain why
         * this is needed, or remove it from the signature.)
         * @param fo the FObj to which this property is attached. (TODO: explain
         * why this is needed, or remove it from the signature).
         * @return an Property with the appropriate datatype used
         */
        protected Property convertPropertyDatatype(Property p,
                                                   PropertyList propertyList,
                                                   FObj fo) {
            return null;
        }

        /**
         * This method expects to be overridden by its subclasses.
         * @param propertyList The PropertyList object being built for this FO.
         * @return the Property object corresponding to the parameters
         * @throws FOPException for invalid or inconsisten FO input
         */
        public Property make(PropertyList propertyList) throws FOPException {
            return null;
        }

        /**
         * Return a Property object representing the parameters.
         * This method expects to be overridden by its subclasses.
         * @param propertyList The PropertyList object being built for this FO.
         * @param parentFO The parent FO for the FO whose property is being made.
         * @return a Property subclass object holding a "compound" property object
         * initialized to the default values for each component.
         * @throws FOPException for invalid or inconsistent FO input
         */
        protected Property makeCompound(PropertyList propertyList,
                                        FObj parentFO) throws FOPException {
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
         * @throws FOPException for invalid or inconsistent FO input
         */
        public Property compute(PropertyList propertyList)
                throws FOPException {
            if (inheritsSpecified()) {
                // recalculate based on last specified value
                // Climb up propertylist and find last spec'd value
                Property specProp =
                    propertyList.getNearestSpecified(propId);
                if (specProp != null) {
                    // Only need to do this if the value is relative!!!
                    String specVal = specProp.getSpecifiedValue();
                    if (specVal != null) {
                        try {
                            return make(propertyList, specVal,
                                        propertyList.getParentFObj());
                        } catch (FOPException e) {
                            //getLogger()error("Error computing property value for "
                            //                       + propName + " from "
                            //                       + specVal);
                            return null;
                        }
                    }
                }
            }
            return null;    // standard
        }

        /**
         * For properties that operate on a relative direction (before, after,
         * start, end) instead of an absolute direction (top, bottom, left,
         * right), this method determines whether a corresponding property
         * is specified on the corresponding absolute direction. For example,
         * the border-start-color property in a lr-tb writing-mode specifies
         * the same thing that the border-left-color property specifies. In this
         * example, if the Maker for the border-start-color property is testing,
         * and if the border-left-color is specified in the properties,
         * this method should return true.
         * @param propertyList collection of properties to be tested
         * @return true iff 1) the property operates on a relative direction,
         * AND 2) the property has a corresponding property on an absolute
         * direction, AND 3) the corresponding property on that absolute
         * direction has been specified in the input properties
         */
        public boolean isCorrespondingForced(PropertyList propertyList) {
            return false;
        }

        /**
         * For properties that can be set by shorthand properties, this method
         * should return the Property, if any, that is parsed from any
         * shorthand properties that affect this property.
         * This method expects to be overridden by subclasses.
         * For example, the border-right-width property could be set implicitly
         * from the border shorthand property, the border-width shorthand
         * property, or the border-right shorthand property. This method should
         * be overridden in the appropriate subclass to check each of these, and
         * return an appropriate border-right-width Property object.
         * @param propertyList the collection of properties to be considered
         * @return the Property, if found, the correspons, otherwise, null
         */
        public Property getShorthand(PropertyList propertyList) {
            return null;
        }

    }    // end of nested Maker class

    /**
     * The original specified value for properties which inherit
     * specified values.
     */
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

/*
 * This section contains accessor functions for all possible Property datatypes
 */


    /**
     * This method expects to be overridden by subclasses
     * @return Length property value
     */
    public Length getLength() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return ColorType property value
     */
    public ColorType getColorType() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return CondLength property value
     */
    public CondLength getCondLength() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return LenghtRange property value
     */
    public LengthRange getLengthRange() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return LengthPair property value
     */
    public LengthPair getLengthPair() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return Space property value
     */
    public Space getSpace() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return Keep property value
     */
    public Keep getKeep() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return integer equivalent of enumerated property value
     */
    public int getEnum() {
        return 0;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return char property value
     */
    public char getCharacter() {
        return 0;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return collection of other property (sub-property) objects
     */
    public Vector getList() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return Number property value
     */
    public Number getNumber() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return Numeric property value
     */
    public Numeric getNumeric() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return NCname property value
     */
    public String getNCname() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses
     * @return Object property value
     */
    public Object getObject() {
        return null;
    }

    /**
     * This method expects to be overridden by subclasses.
     * @return String property value
     */
    public String getString() {
        Object o = getObject();
        return (o == null) ? null : o.toString();
    }

}
