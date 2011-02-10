/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.properties;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyInfo;
import org.apache.fop.fo.expr.PropertyParser;


/**
 * Base class for all property makers
 */
public class PropertyMaker implements Cloneable {

    /** Logger instance */
    private static Log log = LogFactory.getLog(PropertyMaker.class);

    /** the property ID */
    protected int propId;
    private boolean inherited = true;
    private Map enums = null;
    private Map keywords = null;
    /** the default value for the maker */
    protected String defaultValue = null;
    /** Indicates whether the property is context-dependant and therefore can't be cached. */
    protected boolean contextDep = false;
    /** Indicates whether the property is set through a shorthand. */
    protected boolean setByShorthand = false;
    private int percentBase = -1;
    private PropertyMaker[] shorthands = null;
    private ShorthandParser datatypeParser;

    /** default property **/
    protected Property defaultProperty;
    /** Maker for 'corresponding' properties **/
    protected CorrespondingPropertyMaker corresponding;

    /**
     * @return the name of the property for this Maker
     */
    public int getPropId() {
        return propId;
    }

    /**
     * Construct an instance of a Property.Maker for the given property.
     * @param propId The Constant ID of the property to be made.
     */
    public PropertyMaker(int propId) {
        this.propId = propId;
    }

    /**
     * Copy all the values from the generic maker to this maker.
     * @param generic a generic property maker.
     */
    public void useGeneric(PropertyMaker generic) {
        contextDep = generic.contextDep;
        inherited = generic.inherited;
        defaultValue = generic.defaultValue;
        percentBase = generic.percentBase;
        if (generic.shorthands != null) {
            shorthands = new PropertyMaker[generic.shorthands.length];
            System.arraycopy(generic.shorthands, 0, shorthands, 0, shorthands.length);
        }
        if (generic.enums != null) {
            enums = new HashMap(generic.enums);
        }
        if (generic.keywords != null) {
            keywords = new HashMap(generic.keywords);
        }
    }

    /**
     * Set the inherited flag.
     * @param inherited true if this is an inherited property
     */
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    /**
     * Add a keyword-equiv to the maker.
     * @param keyword the keyword
     * @param value the value to be used when the keyword is specified
     */
    public void addKeyword(String keyword, String value) {
        if (keywords == null) {
            keywords = new HashMap();
        }
        keywords.put(keyword, value);
    }

    /**
     * Add a enum constant.
     * @param constant the enum constant
     * @param value the Property value to use when the constant is specified
     */
    public void addEnum(String constant, Property value) {
        if (enums == null) {
            enums = new HashMap();
        }
        enums.put(constant, value);
    }

    /**
     * Add a subproperty to this maker.
     * @param subproperty the PropertyMaker for the subproperty
     */
    public void addSubpropMaker(PropertyMaker subproperty) {
        throw new RuntimeException("Unable to add subproperties " + getClass());
    }

    /**
     * Return a subproperty maker for the subpropertyId.
     * @param subpropertyId The subpropertyId of the maker.
     * @return The subproperty maker.
     */
    public PropertyMaker getSubpropMaker(int subpropertyId) {
        throw new RuntimeException("Unable to add subproperties");
    }

    /**
     * Add a shorthand to this maker. Only an Integer is added to the
     * shorthands list. Later the Integers are replaced with references
     * to the actual shorthand property makers.
     * @param shorthand a property maker thar is that is checked for
     *        shorthand values.
     */
    public void addShorthand(PropertyMaker shorthand) {
        if (shorthands == null) {
            shorthands = new PropertyMaker[3];
        }
        for (int i = 0; i < shorthands.length; i++) {
            if (shorthands[i] == null) {
                shorthands[i] = shorthand;
                break;
            }
        }
    }

    /**
     * Set the shorthand datatype parser.
     * @param parser the shorthand parser
     */
    public void setDatatypeParser(ShorthandParser parser) {
        datatypeParser = parser;
    }

    /**
     * Set the default value for this maker.
     * @param defaultValue the default value.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set the default value for this maker.
     * @param defaultValue the default value
     * @param contextDep true when the value context dependent and
     *        must not be cached.
     */
    public void setDefault(String defaultValue, boolean contextDep) {
        this.defaultValue = defaultValue;
        this.contextDep = contextDep;
    }

    /**
     * Set the percent base identifier for this maker.
     * @param percentBase the percent base (ex. LengthBase.FONTSIZE)
     */
    public void setPercentBase(int percentBase) {
        this.percentBase = percentBase;
    }

    /**
     * Set the setByShorthand flag which only is applicable for subproperty
     * makers. It should be true for the subproperties which must be
     * assigned a value when the base property is assigned a attribute
     * value directly.
     * @param setByShorthand true if this subproperty must be set when the base property is set
     */
    public void setByShorthand(boolean setByShorthand) {
        this.setByShorthand = setByShorthand;
    }

    /**
     * Set the correspoding property information.
     * @param corresponding a corresponding maker where the
     *        isForcedCorresponding and compute methods are delegated to.
     */
    public void setCorresponding(CorrespondingPropertyMaker corresponding) {
        this.corresponding = corresponding;
    }

    /**
     * Create a new empty property. Must be overriden in compound
     * subclasses.
     * @return a new instance of the Property for which this is a maker.
     */
    public Property makeNewProperty() {
        return null;
    }

    /**
     * If the property is a relative property with a corresponding absolute
     * value specified, the absolute value is used. This is also true of
     * the inheritance priority (I think...)
     * If the property is an "absolute" property and it isn't specified, then
     * we try to compute it from the corresponding relative property: this
     * happens in computeProperty.
     * @param propertyList the applicable property list
     * @param tryInherit true if inherited properties should be examined.
     * @return the property value
     * @throws PropertyException if there is a problem evaluating the property
     */
    public Property findProperty(PropertyList propertyList,
                                 boolean tryInherit)
                throws PropertyException {
        Property p = null;

        if (log.isTraceEnabled()) {
            log.trace("PropertyMaker.findProperty: "
                  + FOPropertyMapping.getPropertyName(propId)
                  + ", " + propertyList.getFObj().getName());
        }

        if (corresponding != null && corresponding.isCorrespondingForced(propertyList)) {
            p = corresponding.compute(propertyList);
        } else {
            p = propertyList.getExplicit(propId);
            if (p == null) {    // check for shorthand specification
                p = getShorthand(propertyList);
            }
            if (p == null) {
                p = this.compute(propertyList);
            }
        }
        if (p == null && tryInherit) {
            // else inherit (if has parent and is inheritable)
            PropertyList parentPropertyList = propertyList.getParentPropertyList();
            if (parentPropertyList != null && isInherited()) {
                p = parentPropertyList.get(propId, true, false);
            }
        }
        return p;
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param subpropertyId  The subproperty id of the property being retrieved.
     *        Is 0 when retrieving a base property.
     * @param propertyList The PropertyList object being built for this FO.
     * @param tryInherit true if inherited properties should be examined.
     * @param tryDefault true if the default value should be returned.
     * @return the property value
     * @throws PropertyException if there is a problem evaluating the property
     */
    public Property get(int subpropertyId, PropertyList propertyList,
                        boolean tryInherit, boolean tryDefault)
                    throws PropertyException {
        Property p = findProperty(propertyList, tryInherit);

        if (p == null && tryDefault) {    // default value for this FO!
            p = make(propertyList);
        }
        return p;
    }

    /**
     * Default implementation of isInherited.
     * @return A boolean indicating whether this property is inherited.
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * This is used to handle properties specified as a percentage of
     * some "base length", such as the content width of their containing
     * box.
     * Overridden by subclasses which allow percent specifications. See
     * the documentation on properties.xsl for details.
     * @param pl the PropertyList containing the property. (TODO: explain
     * what this is used for, or remove it from the signature.)
     * @return an object implementing the PercentBase interface.
     * @throws PropertyException if there is a problem while evaluating the base property
     */
    public PercentBase getPercentBase(PropertyList pl) throws PropertyException {
        if (percentBase == -1) {
            return null;
        } else {
            return new LengthBase(pl, percentBase);
        }
    }

    /**
     * Return a property value for the given component of a compound
     * property.
     * @param p A property value for a compound property type such as
     * SpaceProperty.
     * @param subpropertyId the id of the component whose value is to be
     * returned.
     * NOTE: this is only to ease porting when calls are made to
     * PropertyList.get() using a component name of a compound property,
     * such as get("space.optimum"). The recommended technique is:
     * get("space").getOptimum().
     * Overridden by property maker subclasses which handle
     * compound properties.
     * @return the Property containing the subproperty
     */
    public Property getSubprop(Property p, int subpropertyId) {
        CompoundDatatype val = (CompoundDatatype) p.getObject();
        return val.getComponent(subpropertyId);
    }

    /**
     * Set a component in a compound property and return the modified
     * compound property object.
     * This default implementation returns the original base property
     * without modifying it.
     * It is overridden by property maker subclasses which handle
     * compound properties.
     * @param baseProperty The Property object representing the compound property,
     * such as SpaceProperty.
     * @param subpropertyId The ID of the component whose value is specified.
     * @param subproperty A Property object holding the specified value of the
     * component to be set.
     * @return The modified compound property object.
     */
    protected Property setSubprop(Property baseProperty, int subpropertyId,
                                  Property subproperty) {
        CompoundDatatype val = (CompoundDatatype) baseProperty.getObject();
        val.setComponent(subpropertyId, subproperty, false);
        return baseProperty;
    }

    /**
     * Return the default value.
     * @param propertyList The PropertyList object being built for this FO.
     * @return the Property object corresponding to the parameters
     * @throws PropertyException for invalid or inconsisten FO input
     */
    public Property make(PropertyList propertyList) throws PropertyException {
        if (defaultProperty != null) {
            if (log.isTraceEnabled()) {
                log.trace("PropertyMaker.make: reusing defaultProperty, "
                      + FOPropertyMapping.getPropertyName(propId));
            }
            return defaultProperty;
        }
        if (log.isTraceEnabled()) {
            log.trace("PropertyMaker.make: making default property value, "
                  + FOPropertyMapping.getPropertyName(propId)
                  + ", " + propertyList.getFObj().getName());
        }
        Property p = make(propertyList, defaultValue, propertyList.getParentFObj());
        if (!contextDep) {
            defaultProperty = p;
        }
        return p;
    }

    /**
     * Create a Property object from an attribute specification.
     * @param propertyList The PropertyList object being built for this FO.
     * @param value The attribute value.
     * @param fo The parent FO for the FO whose property is being made.
     * @return The initialized Property object.
     * @throws PropertyException for invalid or inconsistent FO input
     */
     public Property make(PropertyList propertyList, String value,
                         FObj fo) throws PropertyException {
        try {
            Property newProp = null;
            String pvalue = value;
            if ("inherit".equals(value)) {
                newProp = propertyList.getFromParent(this.propId & Constants.PROPERTY_MASK);
                if ((propId & Constants.COMPOUND_MASK) != 0) {
                    newProp = getSubprop(newProp, propId & Constants.COMPOUND_MASK);
                }
                if (!isInherited() && log.isWarnEnabled()) {
                    /* check whether explicit value is available on the parent
                     * (for inherited properties, an inherited value will always
                     *  be available)
                     */
                    Property parentExplicit = propertyList.getParentPropertyList()
                                                .getExplicit(getPropId());
                    if (parentExplicit == null) {
                        log.warn(FOPropertyMapping.getPropertyName(getPropId())
                                + "=\"inherit\" on " + propertyList.getFObj().getName()
                                + ", but no explicit value found on the parent FO.");
                    }
                }
            } else {
                // Check for keyword shorthand values to be substituted.
                pvalue = checkValueKeywords(pvalue.trim());
                newProp = checkEnumValues(pvalue);
            }
            if (newProp == null) {
                // Override parsePropertyValue in each subclass of Property.Maker
                newProp = PropertyParser.parse(pvalue,
                                                  new PropertyInfo(this,
                                                  propertyList));
            }
            if (newProp != null) {
                newProp = convertProperty(newProp, propertyList, fo);
            }
            if (newProp == null) {
                throw new PropertyException("No conversion defined " + pvalue);
            }
            return newProp;
        } catch (PropertyException propEx) {
            propEx.setLocator(fo.getLocator());
            propEx.setPropertyName(getName());
            throw propEx;
        }
    }

    /**
     * Make a property value for a compound property. If the property
     * value is already partially initialized, this method will modify it.
     * @param baseProperty The Property object representing the compound property,
     * for example: SpaceProperty.
     * @param subpropertyId The Constants ID of the subproperty (component)
     *        whose value is specified.
     * @param propertyList The propertyList being built.
     * @param fo The parent FO for the FO whose property is being made.
     * @param value the value of the
     * @return baseProperty (or if null, a new compound property object) with
     * the new subproperty added
     * @throws PropertyException for invalid or inconsistent FO input
     */
    public Property make(Property baseProperty, int subpropertyId,
                         PropertyList propertyList, String value,
                         FObj fo) throws PropertyException {
        //getLogger().error("compound property component "
        //                       + partName + " unknown.");
        return baseProperty;
    }

    /**
     * Converts a shorthand property
     *
     * @param propertyList  the propertyList for which to convert
     * @param prop          the shorthand property
     * @param fo            ...
     * @return  the converted property
     * @throws PropertyException ...
     */
    public Property convertShorthandProperty(PropertyList propertyList,
                                             Property prop, FObj fo)
        throws PropertyException {
        Property pret = convertProperty(prop, propertyList, fo);
        if (pret == null) {
            // If value is a name token, may be keyword or Enum
            String sval = prop.getNCname();
            if (sval != null) {
                //log.debug("Convert shorthand ncname " + sval);
                pret = checkEnumValues(sval);
                if (pret == null) {
                    /* Check for keyword shorthand values to be substituted. */
                    String pvalue = checkValueKeywords(sval);
                    if (!pvalue.equals(sval)) {
                        //log.debug("Convert shorthand keyword" + pvalue);
                        // Substituted a value: must parse it
                        Property p = PropertyParser.parse(pvalue,
                                                 new PropertyInfo(this,
                                                                  propertyList));
                        pret = convertProperty(p, propertyList, fo);
                    }
                }
            }
        }
        if (pret != null) {
            /*
             * log.debug("Return shorthand value " + pret.getString() +
             * " for " + getPropName());
             */
        }
        return pret;
    }

    /**
     * For properties that contain enumerated values.
     * This method should be overridden by subclasses.
     * @param value the string containing the property value
     * @return the Property encapsulating the enumerated equivalent of the
     * input value
     */
    protected Property checkEnumValues(String value) {
        if (enums != null) {
            Property p = (Property) enums.get(value);
            return p;
        }
        return null;
    }

    /**
     * Return a String to be parsed if the passed value corresponds to
     * a keyword which can be parsed and used to initialize the property.
     * For example, the border-width family of properties can have the
     * initializers "thin", "medium", or "thick". The FOPropertyMapping
     * file specifies a length value equivalent for these keywords,
     * such as "0.5pt" for "thin".
     * @param keyword the string value of property attribute.
     * @return a String containing a parseable equivalent or null if
     * the passed value isn't a keyword initializer for this Property
     */
    protected String checkValueKeywords(String keyword) {
        if (keywords != null) {
            String value = (String)keywords.get(keyword);
            if (value != null) {
                return value;
            }
        }
        // TODO: should return null here?
        return keyword;
    }

    /**
     * Return a Property object based on the passed Property object.
     * This method is called if the Property object built by the parser
     * isn't the right type for this property.
     * It is overridden by subclasses.
     * @param p The Property object return by the expression parser
     * @param propertyList The PropertyList object being built for this FO.
     * @param fo The parent FO for the FO whose property is being made.
     * @return A Property of the correct type or null if the parsed value
     * can't be converted to the correct type.
     * @throws PropertyException for invalid or inconsistent FO input
     */
    protected Property convertProperty(Property p,
                                    PropertyList propertyList,
                                    FObj fo) throws PropertyException {
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
     * @param fo The parent FO for the FO whose property is being made.
     * why this is needed, or remove it from the signature).
     * @return an Property with the appropriate datatype used
     * @throws PropertyException for invalid or inconsistent input
     */
    protected Property convertPropertyDatatype(Property p,
                                               PropertyList propertyList,
                                               FObj fo) throws PropertyException {
        return null;
    }

    /**
     * Return a Property object representing the value of this property,
     * based on other property values for this FO.
     * A special case is properties which inherit the specified value,
     * rather than the computed value.
     * @param propertyList The PropertyList for the FO.
     * @return Property A computed Property value or null if no rules
     * are specified to compute the value.
     * @throws PropertyException for invalid or inconsistent FO input
     */
    protected Property compute(PropertyList propertyList)
            throws PropertyException {
        if (corresponding != null) {
            return corresponding.compute(propertyList);
        }
        return null;    // standard
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
     * @throws PropertyException if there is a problem while evaluating the shorthand
     */
    public Property getShorthand(PropertyList propertyList)
                throws PropertyException {
        if (shorthands == null) {
            return null;
        }
        Property prop;
        int n = shorthands.length;
        for (int i = 0; i < n && shorthands[i] != null; i++) {
            PropertyMaker shorthand = shorthands[i];
            prop = propertyList.getExplicit(shorthand.propId);
            if (prop != null) {
                ShorthandParser parser = shorthand.datatypeParser;
                Property p = parser.getValueForProperty(getPropId(),
                                        prop, this, propertyList);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    /** @return the name of the property this maker is used for. */
    public String getName() {
        return FOPropertyMapping.getPropertyName(propId);
    }

    /**
     * Return a clone of the makers. Used by useGeneric() to clone the
     * subproperty makers of the generic compound makers.
     * {@inheritDoc}
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }
}
